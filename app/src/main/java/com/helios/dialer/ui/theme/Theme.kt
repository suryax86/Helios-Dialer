package com.helios.dialer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HeliosDarkScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = PitchBlack,
    primaryContainer = ColorCyanContainer,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlue,
    onSecondary = PitchBlack,
    background = PitchBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = TextPrimary
)

@Composable
fun HeliosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeliosDarkScheme,
        typography = Typography,
        content = content
    )
}
