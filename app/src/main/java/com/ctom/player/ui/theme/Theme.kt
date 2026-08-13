package com.ctom.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DeepOceanScheme = darkColorScheme(
    primary = WaterBlue,
    onPrimary = OceanBlack,
    secondary = IceBlue,
    onSecondary = OceanBlack,
    background = OceanBlack,
    onBackground = TextPrimary,
    surface = OceanSurface,
    onSurface = TextPrimary,
    surfaceVariant = OceanSurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = StrokeBlue,
)

@Composable
fun CtomPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeepOceanScheme,
        typography = CtomTypography,
        content = content,
    )
}