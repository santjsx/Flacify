package com.flacify.music.core.designsystem.theme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
data class ArtworkMood(
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val accentColor: Color = Color(0xFFFA2D48)
)

/**
 * Extracts the album cover's signature color to set the mood of the Now Playing and MiniPlayer screens,
 * with white text at >= 14.7:1 contrast ratio per the teardown specification.
 */
@Composable
fun rememberArtworkMood(
    artworkUri: String?,
    fallbackColor: Color,
    isDarkTheme: Boolean = true
): ArtworkMood {
    val context = LocalContext.current
    val moodState = remember(artworkUri) {
        mutableStateOf<ArtworkMood?>(null)
    }

    LaunchedEffect(artworkUri) {
        if (artworkUri.isNullOrBlank() || artworkUri.endsWith("/-1") || artworkUri.endsWith("/0")) {
            moodState.value = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                // 1. Fetch software bitmap via Coil (reusing Coil memory cache)
                val imageLoader = Coil.imageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .allowHardware(false) // Software bitmap required for Palette.from(bitmap)
                    .size(160, 160)
                    .build()
                val result = imageLoader.execute(request)
                var bitmap: Bitmap? = (result as? SuccessResult)?.drawable?.toBitmap()

                // Fallback via ContentResolver if not in Coil cache
                if (bitmap == null) {
                    val uri = Uri.parse(artworkUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        bitmap = BitmapFactory.decodeStream(stream)
                    }
                }

                if (bitmap != null) {
                    val palette = Palette.from(bitmap!!).generate()
                    val primarySwatch = palette.darkVibrantSwatch
                        ?: palette.vibrantSwatch
                        ?: palette.dominantSwatch
                        ?: palette.darkMutedSwatch
                        ?: palette.mutedSwatch

                    val secondarySwatch = palette.darkMutedSwatch
                        ?: palette.mutedSwatch
                        ?: palette.dominantSwatch

                    if (primarySwatch != null) {
                        val primaryRgb = primarySwatch.rgb
                        val secondaryRgb = secondarySwatch?.rgb ?: primaryRgb
                        val accentRgb = palette.vibrantSwatch?.rgb
                            ?: palette.lightVibrantSwatch?.rgb
                            ?: palette.dominantSwatch?.rgb
                            ?: 0xFFFA2D48.toInt()

                        val top = createMoodColor(primaryRgb, targetLightness = 0.20f, isDarkTheme = isDarkTheme)
                        val mid = createMoodColor(primaryRgb, targetLightness = 0.12f, isDarkTheme = isDarkTheme)
                        val bottom = createMoodColor(secondaryRgb, targetLightness = 0.06f, isDarkTheme = isDarkTheme)

                        val contrast = ColorUtils.calculateContrast(0xFFFFFFFF.toInt(), ColorUtils.HSLToColor(
                            FloatArray(3).also { ColorUtils.colorToHSL(primaryRgb, it); it[2] = 0.20f }
                        ))
                        Log.d("FLACifyArtwork", "Extracted mood from $artworkUri: top=$top, contrast=$contrast")

                        moodState.value = ArtworkMood(
                            topColor = top,
                            midColor = mid,
                            bottomColor = bottom,
                            accentColor = Color(accentRgb)
                        )
                        return@withContext
                    }
                }
                moodState.value = null
            } catch (e: Exception) {
                Log.w("FLACifyArtwork", "Failed to extract mood from $artworkUri", e)
                moodState.value = null
            }
        }
    }

    val mood = moodState.value
    return remember(mood, fallbackColor) {
        mood ?: ArtworkMood(
            topColor = fallbackColor,
            midColor = fallbackColor,
            bottomColor = fallbackColor,
            accentColor = Color(0xFFFA2D48)
        )
    }
}

/**
 * Extracts the album cover's signature color as a vertical gradient brush.
 */
@Composable
fun rememberArtworkBackgroundBrush(
    artworkUri: String?,
    backgroundColor: Color,
    isDarkTheme: Boolean = true
): Brush {
    val mood = rememberArtworkMood(artworkUri, backgroundColor, isDarkTheme)
    return remember(mood) {
        Brush.verticalGradient(
            colors = listOf(
                mood.topColor,
                mood.midColor,
                mood.bottomColor
            )
        )
    }
}

/**
 * Creates a rich, saturated mood color from an extracted RGB value while calibrating
 * lightness so that pure white text (#FFFFFF) achieves at least 14.7:1 contrast ratio.
 */
private fun createMoodColor(
    extractedArgb: Int,
    targetLightness: Float,
    isDarkTheme: Boolean = true
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(extractedArgb, hsl)

    if (isDarkTheme) {
        // Retain rich, vibrant artwork hue and saturation (>= 0.40)
        hsl[1] = hsl[1].coerceIn(0.40f, 0.90f)
        hsl[2] = targetLightness

        // Iteratively calibrate lightness to guarantee >= 14.7:1 contrast against pure white text
        var colorInt = ColorUtils.HSLToColor(hsl)
        while (ColorUtils.calculateContrast(0xFFFFFFFF.toInt(), colorInt) < 14.7 && hsl[2] > 0.04f) {
            hsl[2] -= 0.005f
            colorInt = ColorUtils.HSLToColor(hsl)
        }
        return Color(colorInt)
    } else {
        hsl[1] = hsl[1].coerceIn(0.20f, 0.50f)
        hsl[2] = (1f - targetLightness).coerceIn(0.80f, 0.94f)
        return Color(ColorUtils.HSLToColor(hsl))
    }
}
