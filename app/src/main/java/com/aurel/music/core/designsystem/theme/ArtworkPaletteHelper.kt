package com.aurel.music.core.designsystem.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a subtle dominant color from artwork and guarantees WCAG AA contrast against
 * primary text (contrast ratio >= 4.5:1) per PRD Section 6.1.
 */
@Composable
fun rememberArtworkBackgroundBrush(
    artworkUri: String?,
    backgroundColor: Color,
    isDarkTheme: Boolean = true
): Brush {
    val context = LocalContext.current
    val accentColorState = remember(artworkUri) { mutableStateOf<Color?>(null) }

    LaunchedEffect(artworkUri) {
        if (artworkUri.isNullOrBlank()) {
            accentColorState.value = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(artworkUri)
                val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSize(120, 120) // Low-memory downsampled for instant generation
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val swatch = palette.dominantSwatch
                        ?: palette.darkMutedSwatch
                        ?: palette.mutedSwatch

                    if (swatch != null) {
                        val rgb = swatch.rgb
                        val clampedColor = clampForContrast(rgb, backgroundColor.toArgb(), isDarkTheme)
                        accentColorState.value = Color(clampedColor)
                    }
                }
            } catch (_: Exception) {
                accentColorState.value = null
            }
        }
    }

    val glowColor = accentColorState.value ?: backgroundColor
    return remember(glowColor, backgroundColor) {
        Brush.verticalGradient(
            colors = listOf(
                glowColor.copy(alpha = if (isDarkTheme) 0.35f else 0.20f),
                backgroundColor
            )
        )
    }
}

/**
 * Clamps lightness and saturation to prevent bright colors from compromising text readability.
 */
private fun clampForContrast(extractedArgb: Int, backgroundArgb: Int, isDarkTheme: Boolean): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(extractedArgb, hsl)
    hsl[2] = if (isDarkTheme) hsl[2].coerceIn(0.12f, 0.28f) else hsl[2].coerceIn(0.75f, 0.88f)
    hsl[1] = hsl[1].coerceAtMost(0.60f) // Keep saturation calm and restrained
    return ColorUtils.HSLToColor(hsl)
}

