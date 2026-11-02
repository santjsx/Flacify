package com.flacify.music.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Semantic Dark Palette (Near-black charcoal with subtle elevation)
val DarkBackground = Color(0xFF0C0C0E)
val DarkSurface = Color(0xFF16161A)
val DarkSurfaceVariant = Color(0xFF222226)
val DarkSurfaceElevated = Color(0xFF2C2C30)
val DarkPrimaryText = Color(0xFFF5F5F7)
val DarkSecondaryText = Color(0xFF8E8E93)
val DarkTertiaryText = Color(0xFF636366)
val DarkDivider = Color(0xFF1C1C20)
val DarkAccent = Color(0xFFFA2D48) // Apple Music signature coral red
val DarkActiveState = Color(0xFFFA2D48)
val DarkDisabledState = Color(0xFF3A3A3C)
val DarkPlaybackProgress = Color(0xFFFA2D48)
val DarkError = Color(0xFFFF453A)
val DarkWarning = Color(0xFFFF9F0A)

// Semantic Light Palette (Calm off-white with crisp hierarchy)
val LightBackground = Color(0xFFF6F6F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBEBF0)
val LightSurfaceElevated = Color(0xFFE2E2E8)
val LightPrimaryText = Color(0xFF1C1C1E)
val LightSecondaryText = Color(0xFF6E6E73)
val LightTertiaryText = Color(0xFF8E8E93)
val LightDivider = Color(0xFFE5E5EA)
val LightAccent = Color(0xFFFA2D48)
val LightActiveState = Color(0xFFFA2D48)
val LightDisabledState = Color(0xFFD1D1D6)
val LightPlaybackProgress = Color(0xFFFA2D48)
val LightError = Color(0xFFFF3B30)
val LightWarning = Color(0xFFFF9500)

@Immutable
data class FlacifyColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceElevated: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val divider: Color,
    val accent: Color,
    val activeState: Color,
    val disabledState: Color,
    val playbackProgress: Color,
    val error: Color,
    val warning: Color,
    val isDark: Boolean
) {
    // Semantic surface aliases
    val primarySurface: Color get() = surface
    val secondarySurface: Color get() = surfaceVariant
    val elevatedSurface: Color get() = surfaceElevated
    val accentGradientEnd: Color get() = Color(0xFFFF5268)
}
