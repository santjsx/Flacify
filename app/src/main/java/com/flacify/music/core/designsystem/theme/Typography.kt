package com.flacify.music.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * FLACify Typography System
 *
 * Implements strict SP scale with independent line heights and font weights:
 * - Display / Hero: 32sp font -> 40sp line height (Bold)
 * - Heading 1: 24sp font -> 32sp line height (SemiBold)
 * - Heading 2: 20sp font -> 28sp line height (SemiBold / Medium)
 * - Body Large: 16sp font -> 24sp line height (Regular / Medium)
 * - Body Medium: 14sp font -> 20sp line height (Regular)
 * - Caption: 12sp font -> 16sp line height (Regular / Medium)
 * - Button: 16sp font -> 24sp line height (Medium)
 */
@Immutable
data class FlacifyTypography(
    // Base scale
    val displayHero: TextStyle,
    val heading1: TextStyle,
    val heading2: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,

    val display: TextStyle = displayHero,

    // Semantic tokens mapped for music player usage
    val appScreenTitle: TextStyle = heading1,
    val albumTitle: TextStyle = heading2,
    val playlistTitle: TextStyle = heading2,
    val songTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    val artistName: TextStyle = bodyMedium,
    val albumMetadata: TextStyle = caption,
    val sectionHeading: TextStyle = heading2,
    val playerSongTitle: TextStyle = heading2,
    val playerArtistName: TextStyle = bodyMedium,
    val playbackTimestamp: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    val trackDuration: TextStyle = caption,
    val primaryButton: TextStyle = button,
    val settingsDescription: TextStyle = bodyMedium,
    val navigationLabel: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // Backward-compatibility aliases
    val screenTitle: TextStyle = heading1,
    val sectionTitle: TextStyle = heading2,
    val primaryContent: TextStyle = bodyLarge,
    val primaryContentBold: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    val secondaryContent: TextStyle = bodyMedium,
    val metadata: TextStyle = caption,
    val playbackTimer: TextStyle = playbackTimestamp
)

val DefaultFlacifyTypography = FlacifyTypography(
    displayHero = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.6).sp
    ),
    heading1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    heading2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    )
)

val MaterialTypographyBridge = Typography(
    headlineLarge = DefaultFlacifyTypography.displayHero,
    headlineMedium = DefaultFlacifyTypography.heading1,
    titleLarge = DefaultFlacifyTypography.heading2,
    bodyLarge = DefaultFlacifyTypography.bodyLarge,
    bodyMedium = DefaultFlacifyTypography.bodyMedium,
    labelLarge = DefaultFlacifyTypography.button,
    labelSmall = DefaultFlacifyTypography.caption
)
