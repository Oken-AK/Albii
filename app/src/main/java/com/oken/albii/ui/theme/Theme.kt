package com.oken.albii.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** iOS 風の配色（システムブルー・グループ背景）。 */
private val IosBlue = Color(0xFF007AFF)
private val IosBackground = Color(0xFFF2F2F7)
private val IosSurface = Color(0xFFFFFFFF)
private val IosDarkBackground = Color(0xFF000000)
private val IosDarkSurface = Color(0xFF1C1C1E)

private val LightScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    background = IosBackground,
    surface = IosSurface,
    surfaceContainer = IosSurface,
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    background = IosDarkBackground,
    surface = IosDarkSurface,
    surfaceContainer = IosDarkSurface,
)

/** 角丸を大きめにした iOS 風の形状。 */
private val IosShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun AlbiiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        shapes = IosShapes,
        typography = Typography(),
        content = content,
    )
}
