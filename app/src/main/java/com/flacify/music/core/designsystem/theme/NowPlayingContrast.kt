package com.flacify.music.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Contrast-aware theme tokens derived dynamically from album artwork background
 * to guarantee WCAG-compliant readability for Now Playing controls, text, and scrubbers.
 */
@Immutable
data class ArtworkContrastTheme(
    val isLight: Boolean,
    val primaryText: Color,
    val secondaryText: Color,
    val controlTint: Color,
    val controlInactiveTint: Color,
    val scrubberTrack: Color,
    val scrubberThumb: Color,
)

/** WCAG contrast ratio calculation between two colors. */
fun contrastRatio(a: Color, b: Color): Float {
    val l1 = a.luminance()
    val l2 = b.luminance()
    val lighter = if (l1 > l2) l1 else l2
    val darker = if (l1 > l2) l2 else l1
    return (lighter + 0.05f) / (darker + 0.05f)
}

val LightArtworkContrastTheme = ArtworkContrastTheme(
    isLight = true,
    primaryText = Color(0xFF141418),
    secondaryText = Color(0xFF4C4C56),
    controlTint = Color(0xFF181820),
    controlInactiveTint = Color(0x66181820),
    scrubberTrack = Color(0x33000000),
    scrubberThumb = Color(0xFFFA2D48),
)

val DarkArtworkContrastTheme = ArtworkContrastTheme(
    isLight = false,
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xB3FFFFFF),
    controlTint = Color(0xFFFFFFFF),
    controlInactiveTint = Color(0x80FFFFFF),
    scrubberTrack = Color(0x33FFFFFF),
    scrubberThumb = Color(0xFFFA2D48),
)

/**
 * Dynamically resolves contrast theme based on perceived luminance of the artwork background.
 * If luminance exceeds 0.40f or contrast with white is < 4.5:1 (WCAG AA), activates light theme tokens.
 */
@Composable
fun rememberArtworkContrastTheme(backgroundColor: Color): ArtworkContrastTheme {
    return remember(backgroundColor) {
        val luminance = backgroundColor.luminance()
        val whiteContrast = contrastRatio(backgroundColor, Color.White)
        val useLightTheme = luminance > 0.40f || whiteContrast < 4.5f
        if (useLightTheme) LightArtworkContrastTheme else DarkArtworkContrastTheme
    }
}

