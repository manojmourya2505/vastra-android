package com.inouiw.vastra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The design is light-only, with black and white as the palette.
private val VastraColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Shade,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Soft,
    outline = Line,
    outlineVariant = Line,
    error = Danger,
)

@Composable
fun VastraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VastraColors, typography = Typography, content = content)
}
