package com.oken.albii.ui.viewer

import android.view.TextureView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.oken.albii.R

/** 縦横比がまだ分からないときの仮の値。映像サイズが分かったら置き換わる。 */
private const val DEFAULT_VIDEO_ASPECT = 16f / 9f

/**
 * 動画 1 ページ分の表示。最初のフレームが描画されるまではサムネイルを見せ、描画されたら映像へ切り替える。
 * [state] が null のページ（表示中でない動画）はサムネイルだけを表示する。画面タップで再生 / 一時停止する。
 */
@Composable
fun VideoPage(
    poster: ImageBitmap?,
    state: VideoPlayerState?,
    modifier: Modifier = Modifier,
) {
    val aspect = state?.videoAspect?.takeIf { it > 0f }
        ?: poster?.let { it.width.toFloat() / it.height }
        ?: DEFAULT_VIDEO_ASPECT
    val tapSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = tapSource,
                indication = null,
                enabled = state != null && !state.hasError,
            ) { state?.togglePlayPause() },
        contentAlignment = Alignment.Center,
    ) {
        if (state != null) {
            AndroidView(
                factory = { TextureView(it) },
                update = { state.attach(it) },
                onRelease = { state.detach(it) },
                modifier = Modifier.aspectRatio(aspect),
            )
        }

        val posterAlpha by animateFloatAsState(
            targetValue = if (state?.hasRenderedFirstFrame == true) 0f else 1f,
            animationSpec = tween(durationMillis = 180),
            label = "videoPosterAlpha",
        )
        if (poster != null && posterAlpha > 0f) {
            Image(
                bitmap = poster,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = posterAlpha },
            )
        }

        if (state != null && !state.hasError && !state.isScrubbing && (!state.playWhenReady || state.isEnded)) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.viewer_play),
                tint = Color.White,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .padding(16.dp),
            )
        }

        if (state?.hasError == true) {
            Text(
                text = stringResource(R.string.video_error),
                color = Color.White,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
