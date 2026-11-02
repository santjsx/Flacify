package com.aurel.music.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAurelColors = staticCompositionLocalOf<AurelColors> {
    error("No AurelColors provided")
}

val LocalAurelTypography = staticCompositionLocalOf<AurelTypography> {
    error("No AurelTypography provided")
}

val LocalAurelShapes = staticCompositionLocalOf<AurelShapes> {
    error("No AurelShapes provided")
}

val DarkColorPalette = AurelColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    surfaceElevated = DarkSurfaceElevated,
    primaryText = DarkPrimaryText,
    secondaryText = DarkSecondaryText,
    tertiaryText = DarkTertiaryText,
    divider = DarkDivider,
    accent = DarkAccent,
    error = DarkError,
    isDark = true
)

val LightColorPalette = AurelColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceElevated = LightSurfaceElevated,
    primaryText = LightPrimaryText,
    secondaryText = LightSecondaryText,
    tertiaryText = LightTertiaryText,
    divider = LightDivider,
    accent = LightAccent,
    error = LightError,
    isDark = false
)

private val MaterialDarkColors = darkColorScheme(
    primary = DarkAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkPrimaryText,
    onBackground = DarkPrimaryText,
    onSurface = DarkPrimaryText,
    error = DarkError
)

private val MaterialLightColors = lightColorScheme(
    primary = LightAccent,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = LightSurface,
    onBackground = LightPrimaryText,
    onSurface = LightPrimaryText,
    error = LightError
)

@Composable
fun AurelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val materialColors = if (darkTheme) MaterialDarkColors else MaterialLightColors

    CompositionLocalProvider(
        LocalAurelColors provides colors,
        LocalAurelTypography provides DefaultAurelTypography,
        LocalAurelShapes provides DefaultAurelShapes
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = MaterialTypographyBridge,
            content = content
        )
    }
}

object AurelTheme {
    val colors: AurelColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAurelColors.current

    val typography: AurelTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAurelTypography.current

    val shapes: AurelShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAurelShapes.current
}

