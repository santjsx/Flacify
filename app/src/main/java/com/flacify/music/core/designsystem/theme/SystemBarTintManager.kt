package com.flacify.music.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Dynamically adapts system status bar icons (clock, battery, Wi-Fi, 5G) to match the
 * background luminosity of the active screen.
 * Restores previous appearance cleanly when the composable leaves composition.
 */
@Composable
fun AdaptiveStatusBarsEffect(isLightBackground: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context.findActivity() ?: return

    DisposableEffect(activity, view, isLightBackground) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousLightStatus = controller.isAppearanceLightStatusBars

        controller.isAppearanceLightStatusBars = isLightBackground

        onDispose {
            controller.isAppearanceLightStatusBars = previousLightStatus
        }
    }
}

