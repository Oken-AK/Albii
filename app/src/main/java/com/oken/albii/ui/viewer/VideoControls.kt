package com.oken.albii.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oken.albii.R

/** material-icons-core には一時停止アイコンが無い（extended は大きい）ので、グリフだけ自前で持つ。 */
private val PauseIcon: ImageVector = materialIcon(name = "Filled.Pause") {
    materialPath {
        moveTo(6f, 19f)
        horizontalLineToRelative(4f)
        verticalLineTo(5f)
        horizontalLineTo(6f)
        verticalLineToRelative(14f)
        close()
        moveTo(14f, 5f)
        verticalLineToRelative(14f)
        horizontalLineToRelative(4f)
        verticalLineTo(5f)
        horizontalLineToRelative(-4f)
        close()
    }
}

/** 再生 / 一時停止ボタン、経過・総時間、シークバー。シークバーを動かすと映像が前後どちらにも追従する。 */
@Composable
fun VideoControls(state: VideoPlayerState, modifier: Modifier = Modifier) {
    val duration = state.durationMs
    val fraction = when {
        state.isScrubbing -> state.scrubFraction
        duration > 0L -> (state.positionMs.toFloat() / duration).coerceIn(0f, 1f)
        else -> 0f
    }
    val showsPause = state.playWhenReady && !state.isEnded

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = state::togglePlayPause, enabled = !state.hasError) {
            Icon(
                imageVector = if (showsPause) PauseIcon else Icons.Default.PlayArrow,
                contentDescription = stringResource(if (showsPause) R.string.viewer_pause else R.string.viewer_play),
                tint = Color.White,
            )
        }
        Text(
            text = formatPlaybackTime(state.positionMs),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = fraction,
            onValueChange = { value ->
                if (!state.isScrubbing) state.beginScrub()
                state.scrubTo(value)
            },
            onValueChangeFinished = state::endScrub,
            enabled = duration > 0L && !state.hasError,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                disabledThumbColor = Color.White.copy(alpha = 0.4f),
                disabledActiveTrackColor = Color.White.copy(alpha = 0.4f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Text(
            text = formatPlaybackTime(duration),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
