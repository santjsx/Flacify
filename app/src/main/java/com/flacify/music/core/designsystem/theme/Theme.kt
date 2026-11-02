package com.flacify.music.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalFlacifyColors = staticCompositionLocalOf<FlacifyColors> {
    error("No FlacifyColors provided")
}

val LocalFlacifyTypography = staticCompositionLocalOf<FlacifyTypography> {
    error("No FlacifyTypography provided")
}

val LocalFlacifyShapes = staticCompositionLocalOf<FlacifyShapes> {
    error("No FlacifyShapes provided")
}

val LocalFlacifySpacing = staticCompositionLocalOf<FlacifySpacing> {
    error("No FlacifySpacing provided")
}

val LocalFlacifyDimensions = staticCompositionLocalOf<FlacifyDimensions> {
    error("No FlacifyDimensions provided")
}

val LocalFlacifyElevation = staticCompositionLocalOf<FlacifyElevation> {
    error("No FlacifyElevation provided")
}

val LocalFlacifyMotion = staticCompositionLocalOf<FlacifyMotion> {
    error("No FlacifyMotion provided")
}

val LocalFlacifyGlass = staticCompositionLocalOf<FlacifyGlass> {
    error("No FlacifyGlass provided")
}

val DarkColorPalette = FlacifyColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    surfaceElevated = DarkSurfaceElevated,
    primaryText = DarkPrimaryText,
    secondaryText = DarkSecondaryText,
    tertiaryText = DarkTertiaryText,
    divider = DarkDivider,
    accent = DarkAccent,
    activeState = DarkActiveState,
    disabledState = DarkDisabledState,
    playbackProgress = DarkPlaybackProgress,
    error = DarkError,
    warning = DarkWarning,
    isDark = true
)

val LightColorPalette = FlacifyColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceElevated = LightSurfaceElevated,
    primaryText = LightPrimaryText,
    secondaryText = LightSecondaryText,
    tertiaryText = LightTertiaryText,
    divider = LightDivider,
    accent = LightAccent,
    activeState = LightActiveState,
    disabledState = LightDisabledState,
    playbackProgress = LightPlaybackProgress,
    error = LightError,
    warning = LightWarning,
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
fun FlacifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val glass = if (darkTheme) DarkGlass else LightGlass
    val materialColors = if (darkTheme) MaterialDarkColors else MaterialLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var ctx: Context? = view.context
            while (ctx is ContextWrapper && ctx !is Activity) {
                ctx = ctx.baseContext
            }
            val activity = ctx as? Activity
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFlacifyColors provides colors,
        LocalFlacifyTypography provides DefaultFlacifyTypography,
        LocalFlacifyShapes provides DefaultFlacifyShapes,
        LocalFlacifySpacing provides DefaultFlacifySpacing,
        LocalFlacifyDimensions provides DefaultFlacifyDimensions,
        LocalFlacifyElevation provides DefaultFlacifyElevation,
        LocalFlacifyMotion provides DefaultFlacifyMotion,
        LocalFlacifyGlass provides glass
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = MaterialTypographyBridge,
            content = content
        )
    }
}

object FlacifyTheme {
    val colors: FlacifyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyColors.current

    val typography: FlacifyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyTypography.current

    val shapes: FlacifyShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyShapes.current

    val spacing: FlacifySpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifySpacing.current

    val dimensions: FlacifyDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyDimensions.current

    val elevation: FlacifyElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyElevation.current

    val motion: FlacifyMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyMotion.current

    val glass: FlacifyGlass
        @Composable
        @ReadOnlyComposable
        get() = LocalFlacifyGlass.current
}
