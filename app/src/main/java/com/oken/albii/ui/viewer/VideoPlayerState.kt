package com.oken.albii.ui.viewer

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** スクラブ中に seekTo を送る最小間隔。短すぎるとデコーダーのフラッシュが続き、映像が更新されなくなる。 */
private const val SCRUB_SEEK_INTERVAL_MS = 50L

/** スクラブ中のシーク方式。EXACT はコマ単位で追従する。キーフレーム間隔が長く重い動画で遅い場合は CLOSEST_SYNC を検討する。 */
@OptIn(UnstableApi::class)
private val SCRUB_SEEK_PARAMETERS: SeekParameters = SeekParameters.EXACT

/**
 * 動画 1 本ぶんの再生状態。ExoPlayer を包み、Compose から読める状態と操作（再生・一時停止・スクラブ）を公開する。
 * 生成した ExoPlayer は Composition から外れたとき（[onForgotten] / [onAbandoned]）に必ず解放する。
 */
@OptIn(UnstableApi::class)
@Stable
class VideoPlayerState(
    context: Context,
    uri: Uri,
    private val scope: CoroutineScope,
) : RememberObserver {

    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val throttle = ScrubSeekThrottle(SCRUB_SEEK_INTERVAL_MS)
    private var trailingSeek: Job? = null
    private var resumeAfterScrub = false
    private var lastScrubSeekMs = -1L
    private var released = false

    /** 再生する意思があるか（バッファリング待ちでも true）。再生ボタンのアイコン切り替えに使う。 */
    var playWhenReady by mutableStateOf(false)
        private set

    /** 実際に映像が進んでいるか。 */
    var isPlaying by mutableStateOf(false)
        private set

    var isEnded by mutableStateOf(false)
        private set

    var durationMs by mutableLongStateOf(0L)
        private set

    var positionMs by mutableLongStateOf(0L)
        private set

    /** 動画の表示上の縦横比（幅 / 高さ）。未取得なら 0。 */
    var videoAspect by mutableFloatStateOf(0f)
        private set

    var hasRenderedFirstFrame by mutableStateOf(false)
        private set

    var hasError by mutableStateOf(false)
        private set

    var isScrubbing by mutableStateOf(false)
        private set

    /** スクラブ中のシークバーの位置（0〜1）。 */
    var scrubFraction by mutableFloatStateOf(0f)
        private set

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@VideoPlayerState.isPlaying = isPlaying
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            this@VideoPlayerState.playWhenReady = playWhenReady
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            isEnded = playbackState == Player.STATE_ENDED
            if (playbackState == Player.STATE_READY) syncDuration()
            if (!isScrubbing) positionMs = player.currentPosition
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) = syncDuration()

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            videoAspect = if (videoSize.height == 0) {
                0f
            } else {
                videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
            }
        }

        override fun onRenderedFirstFrame() {
            hasRenderedFirstFrame = true
        }

        override fun onPlayerError(error: PlaybackException) {
            hasError = true
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            if (!isScrubbing) positionMs = player.currentPosition
        }
    }

    init {
        player.addListener(listener)
        player.setMediaItem(MediaItem.fromUri(uri))
        player.playWhenReady = false
        player.prepare()
    }

    fun attach(view: TextureView) {
        if (!released) player.setVideoTextureView(view)
    }

    fun detach(view: TextureView) {
        if (!released) player.clearVideoTextureView(view)
    }

    fun togglePlayPause() {
        if (released || hasError) return
        if (player.playWhenReady && !isEnded) {
            player.pause()
        } else {
            if (isEnded) player.seekTo(0L)
            player.play()
        }
    }

    fun pause() {
        if (!released) player.pause()
    }

    /** スクラブ開始。再生中だったかを覚えて一時停止し、シーク方式をスクラブ用にする。 */
    fun beginScrub() {
        if (released || isScrubbing) return
        isScrubbing = true
        resumeAfterScrub = player.playWhenReady && !isEnded
        lastScrubSeekMs = -1L
        throttle.reset()
        player.pause()
        player.setSeekParameters(SCRUB_SEEK_PARAMETERS)
    }

    /** シークバーの位置（0〜1）へ映像を追従させる。前方向・後方向の区別なく、最新の位置を優先して送る。 */
    fun scrubTo(fraction: Float) {
        if (released || !isScrubbing) return
        scrubFraction = fraction.coerceIn(0f, 1f)
        val target = scrubTargetMs(scrubFraction, durationMs)
        positionMs = target
        val due = throttle.submit(target, SystemClock.uptimeMillis())
        if (due != null) seekWhileScrubbing(due) else scheduleTrailingSeek()
    }

    /** スクラブ終了。指を離した位置へ確定し、再生中だったなら再開する。 */
    fun endScrub() {
        if (released || !isScrubbing) return
        isScrubbing = false
        trailingSeek?.cancel()
        throttle.reset()
        player.setSeekParameters(SeekParameters.EXACT)
        val target = scrubTargetMs(scrubFraction, durationMs)
        if (target != lastScrubSeekMs) player.seekTo(target)
        positionMs = target
        if (resumeAfterScrub) player.play()
    }

    /** 再生中は描画フレームごとに位置を読み、シークバーのつまみを滑らかに動かす。 */
    suspend fun trackPosition() {
        snapshotFlow { isPlaying }.collectLatest { playing ->
            if (playing) {
                while (true) {
                    withFrameNanos { }
                    if (!isScrubbing) positionMs = player.currentPosition
                }
            } else if (!isScrubbing) {
                positionMs = player.currentPosition
            }
        }
    }

    private fun seekWhileScrubbing(positionMs: Long) {
        lastScrubSeekMs = positionMs
        player.seekTo(positionMs)
    }

    /** 間引かれて保留になった最新位置を、送れる時刻になったら送る。 */
    private fun scheduleTrailingSeek() {
        if (trailingSeek?.isActive == true) return
        trailingSeek = scope.launch {
            while (true) {
                val wait = throttle.delayUntilDueMs(SystemClock.uptimeMillis()) ?: break
                if (wait > 0L) delay(wait)
                throttle.takeIfDue(SystemClock.uptimeMillis())?.let { seekWhileScrubbing(it) }
            }
        }
    }

    private fun syncDuration() {
        val duration = player.duration
        if (duration != C.TIME_UNSET && duration > 0L) durationMs = duration
    }

    private fun release() {
        if (released) return
        released = true
        trailingSeek?.cancel()
        player.removeListener(listener)
        player.release()
    }

    override fun onRemembered() = Unit

    override fun onForgotten() = release()

    override fun onAbandoned() = release()
}

/** [uri] の動画を再生する状態を作る。バックグラウンドへ回ったら一時停止し、Composition から外れたら解放する。 */
@Composable
fun rememberVideoPlayerState(uri: Uri): VideoPlayerState {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val state = remember(uri) { VideoPlayerState(context, uri, scope) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(state, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) state.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state) { state.trackPosition() }
    return state
}
