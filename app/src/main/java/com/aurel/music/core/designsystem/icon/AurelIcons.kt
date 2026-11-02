package com.aurel.music.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * AUREL Premium Icon System — SF Symbols / Lucide Inspired
 *
 * Precise 2.0dp optical stroke weights, rounded caps, and fluid geometry
 * replacing outdated chunky default Material 1/2 icons.
 */
object AurelIcons {

    // --- Navigation & Core ---

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 10.5f)
                lineTo(12f, 3f)
                lineTo(21f, 10.5f)
                verticalLineTo(20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                horizontalLineTo(15f)
                curveTo(14.45f, 21f, 14f, 20.55f, 14f, 20f)
                verticalLineTo(16f)
                curveTo(14f, 15.45f, 13.55f, 15f, 13f, 15f)
                horizontalLineTo(11f)
                curveTo(10.45f, 15f, 10f, 15.45f, 10f, 16f)
                verticalLineTo(20f)
                curveTo(10f, 20.55f, 9.55f, 21f, 9f, 21f)
                horizontalLineTo(4f)
                curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
                close()
            }
        }.build()
    }

    val Library: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelLibrary",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Front media card with rounded corners
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 7f)
                horizontalLineTo(19f)
                curveTo(20.1f, 7f, 21f, 7.9f, 21f, 9f)
                verticalLineTo(19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                horizontalLineTo(8f)
                curveTo(6.9f, 21f, 6f, 20.1f, 6f, 19f)
                verticalLineTo(9f)
                curveTo(6f, 7.9f, 6.9f, 7f, 8f, 7f)
                close()
            }
            // Back layered card
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 16f)
                verticalLineTo(5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                horizontalLineTo(16f)
            }
            // Musical eighth note inside front card
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 11f)
                verticalLineTo(15.5f)
            }
            path(
                fill = SolidColor(Color.White)
            ) {
                moveTo(14f, 15.5f)
                curveTo(14f, 16.33f, 13.1f, 17f, 12f, 17f)
                curveTo(10.9f, 17f, 10f, 16.33f, 10f, 15.5f)
                curveTo(10f, 14.67f, 10.9f, 14f, 12f, 14f)
                curveTo(13.1f, 14f, 14f, 14.67f, 14f, 15.5f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 11.5f)
                curveTo(14f, 11.5f, 15.5f, 11f, 16.5f, 12.5f)
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSearch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Elegant thin circle
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 10.5f)
                curveTo(17f, 14.09f, 14.09f, 17f, 10.5f, 17f)
                curveTo(6.91f, 17f, 4f, 14.09f, 4f, 10.5f)
                curveTo(4f, 6.91f, 6.91f, 4f, 10.5f, 4f)
                curveTo(14.09f, 4f, 17f, 6.91f, 17f, 10.5f)
                close()
            }
            // 45-degree handle with rounded cap
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.2f, 15.2f)
                lineTo(20.5f, 20.5f)
            }
        }.build()
    }

    val More: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelMore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Dot 1
                moveTo(6.5f, 12f)
                curveTo(6.5f, 12.83f, 5.83f, 13.5f, 5f, 13.5f)
                curveTo(4.17f, 13.5f, 3.5f, 12.83f, 3.5f, 12f)
                curveTo(3.5f, 11.17f, 4.17f, 10.5f, 5f, 10.5f)
                curveTo(5.83f, 10.5f, 6.5f, 11.17f, 6.5f, 12f)
                close()
                // Dot 2
                moveTo(13.5f, 12f)
                curveTo(13.5f, 12.83f, 12.83f, 13.5f, 12f, 13.5f)
                curveTo(11.17f, 13.5f, 10.5f, 12.83f, 10.5f, 12f)
                curveTo(10.5f, 11.17f, 11.17f, 10.5f, 12f, 10.5f)
                curveTo(12.83f, 10.5f, 13.5f, 11.17f, 13.5f, 12f)
                close()
                // Dot 3
                moveTo(20.5f, 12f)
                curveTo(20.5f, 12.83f, 19.83f, 13.5f, 19f, 13.5f)
                curveTo(18.17f, 13.5f, 17.5f, 12.83f, 17.5f, 12f)
                curveTo(17.5f, 11.17f, 18.17f, 10.5f, 19f, 10.5f)
                curveTo(19.83f, 10.5f, 20.5f, 11.17f, 20.5f, 12f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Track 1
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 6f)
                lineTo(7f, 6f)
                moveTo(13f, 6f)
                lineTo(21f, 6f)
            }
            // Knob 1
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(10f, 9f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, 10f, 3f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 9f)
                close()
            }
            // Track 2
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 12f)
                lineTo(14f, 12f)
                moveTo(20f, 12f)
                lineTo(21f, 12f)
            }
            // Knob 2
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(17f, 15f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17f, 9f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 15f)
                close()
            }
            // Track 3
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 18f)
                lineTo(5f, 18f)
                moveTo(11f, 18f)
                lineTo(21f, 18f)
            }
            // Knob 3
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(8f, 21f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, 8f, 15f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 21f)
                close()
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelRefresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Modern dual-arc sync with clean arrowheads
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.5f, 5.5f)
                lineTo(3f, 8f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 3f)
                verticalLineTo(8f)
                horizontalLineTo(8f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.5f, 18.5f)
                lineTo(21f, 16f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 21f)
                verticalLineTo(16f)
                horizontalLineTo(16f)
            }
        }.build()
    }

    // --- Playback Controls (Apple Music / SF Symbols Style) ---

    val Play: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelPlay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // Rounded triangle, optically centered with +1dp right shift
                moveTo(8.5f, 5.8f)
                curveTo(8.5f, 4.95f, 9.42f, 4.43f, 10.15f, 4.88f)
                lineTo(19.25f, 10.98f)
                curveTo(19.95f, 11.45f, 19.95f, 12.55f, 19.25f, 13.02f)
                lineTo(10.15f, 19.12f)
                curveTo(9.42f, 19.57f, 8.5f, 19.05f, 8.5f, 18.2f)
                close()
            }
        }.build()
    }

    val Pause: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelPause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Twin vertical rounded capsules
            path(fill = SolidColor(Color.White)) {
                // Left Bar
                moveTo(7.5f, 5f)
                curveTo(8.33f, 5f, 9f, 5.67f, 9f, 6.5f)
                verticalLineTo(17.5f)
                curveTo(9f, 18.33f, 8.33f, 19f, 7.5f, 19f)
                curveTo(6.67f, 19f, 6f, 18.33f, 6f, 17.5f)
                verticalLineTo(6.5f)
                curveTo(6f, 5.67f, 6.67f, 5f, 7.5f, 5f)
                close()
                // Right Bar
                moveTo(16.5f, 5f)
                curveTo(17.33f, 5f, 18f, 5.67f, 18f, 6.5f)
                verticalLineTo(17.5f)
                curveTo(18f, 18.33f, 17.33f, 19f, 16.5f, 19f)
                curveTo(15.67f, 19f, 15f, 18.33f, 15f, 17.5f)
                verticalLineTo(6.5f)
                curveTo(15f, 5.67f, 15.67f, 5f, 16.5f, 5f)
                close()
            }
        }.build()
    }

    val SkipForward: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSkipForward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Smooth Play triangle
            path(fill = SolidColor(Color.White)) {
                moveTo(6.5f, 6.2f)
                curveTo(6.5f, 5.4f, 7.4f, 4.9f, 8.1f, 5.35f)
                lineTo(15.2f, 11.15f)
                curveTo(15.8f, 11.55f, 15.8f, 12.45f, 15.2f, 12.85f)
                lineTo(8.1f, 18.65f)
                curveTo(7.4f, 19.1f, 6.5f, 18.6f, 6.5f, 17.8f)
                close()
            }
            // Vertical stop line with rounded cap
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(18f, 6f)
                lineTo(18f, 18f)
            }
        }.build()
    }

    val SkipBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSkipBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Vertical stop line with rounded cap
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6f, 6f)
                lineTo(6f, 18f)
            }
            // Smooth Reverse Play triangle
            path(fill = SolidColor(Color.White)) {
                moveTo(17.5f, 6.2f)
                curveTo(17.5f, 5.4f, 16.6f, 4.9f, 15.9f, 5.35f)
                lineTo(8.8f, 11.15f)
                curveTo(8.2f, 11.55f, 8.2f, 12.45f, 8.8f, 12.85f)
                lineTo(15.9f, 18.65f)
                curveTo(16.6f, 19.1f, 17.5f, 18.6f, 17.5f, 17.8f)
                close()
            }
        }.build()
    }

    val Shuffle: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelShuffle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Top arrowhead
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(8f)
            }
            // Top arrow crossing stroke
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 20f)
                lineTo(9f, 15f)
                moveTo(20f, 4f)
                lineTo(14.5f, 9.5f)
            }
            // Bottom arrowhead
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 20f)
                horizontalLineTo(20f)
                verticalLineTo(16f)
            }
            // Bottom arrow crossing stroke
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 4f)
                lineTo(20f, 20f)
            }
        }.build()
    }

    val Repeat: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelRepeat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Top loop with arrowhead
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 2f)
                lineTo(21f, 6f)
                lineTo(17f, 10f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 12f)
                verticalLineTo(10f)
                curveTo(3f, 7.8f, 4.8f, 6f, 7f, 6f)
                horizontalLineTo(21f)
            }
            // Bottom loop with arrowhead
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 22f)
                lineTo(3f, 18f)
                lineTo(7f, 14f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 12f)
                verticalLineTo(14f)
                curveTo(21f, 16.2f, 19.2f, 18f, 17f, 18f)
                horizontalLineTo(3f)
            }
        }.build()
    }

    val RepeatOne: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelRepeatOne",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Loop paths
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 2f)
                lineTo(21f, 6f)
                lineTo(17f, 10f)
                moveTo(3f, 12f)
                verticalLineTo(10f)
                curveTo(3f, 7.8f, 4.8f, 6f, 7f, 6f)
                horizontalLineTo(21f)
                moveTo(7f, 22f)
                lineTo(3f, 18f)
                lineTo(7f, 14f)
                moveTo(21f, 12f)
                verticalLineTo(14f)
                curveTo(21f, 16.2f, 19.2f, 18f, 17f, 18f)
                horizontalLineTo(3f)
            }
            // Centered elegant numeral '1'
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10.5f, 10.5f)
                lineTo(12f, 9.5f)
                verticalLineTo(14.5f)
            }
        }.build()
    }

    val Queue: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelQueue",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // 3 sleek lines with rounded ends
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 6f)
                horizontalLineTo(14f)
                moveTo(3f, 12f)
                horizontalLineTo(10f)
                moveTo(3f, 18f)
                horizontalLineTo(10f)
            }
            // Musical note aligned on right
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 9f)
                verticalLineTo(16f)
                curveTo(18f, 17.1f, 17.1f, 18f, 16f, 18f)
                curveTo(14.9f, 18f, 14f, 17.1f, 14f, 16f)
                curveTo(14f, 14.9f, 14.9f, 14f, 16f, 14f)
                curveTo(16.7f, 14f, 17.4f, 14.4f, 17.7f, 15f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 10f)
                curveTo(18f, 10f, 19.5f, 9.5f, 21f, 11f)
            }
        }.build()
    }

    val SleepTimer: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSleepTimer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Elegant modern stopwatch outline
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 5f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 21f)
                close()
            }
            // Top stopwatch crown button
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 2f)
                lineTo(12f, 5f)
            }
            // Internal stopwatch hands
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 9f)
                verticalLineTo(13f)
                lineTo(15f, 15f)
            }
        }.build()
    }

    val HeartOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelHeartOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f)
                lineTo(10.55f, 19.68f)
                curveTo(5.4f, 15f, 2f, 11.92f, 2f, 8.14f)
                curveTo(2f, 5.06f, 4.42f, 2.64f, 7.5f, 2.64f)
                curveTo(9.24f, 2.64f, 10.91f, 3.45f, 12f, 4.73f)
                curveTo(13.09f, 3.45f, 14.76f, 2.64f, 16.5f, 2.64f)
                curveTo(19.58f, 2.64f, 22f, 5.06f, 22f, 8.14f)
                curveTo(22f, 11.92f, 18.6f, 15f, 13.45f, 19.68f)
                close()
            }
        }.build()
    }

    val HeartFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelHeartFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFE50914))) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.03f)
                close()
            }
        }.build()
    }

    val ChevronDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelChevronDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }.build()
    }

    val Sort: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelSort",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                moveTo(3f, 12f)
                horizontalLineTo(15f)
                moveTo(3f, 18f)
                horizontalLineTo(9f)
            }
        }.build()
    }

    val Equalizer: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelEqualizer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Modern studio faders
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                // Track 1
                moveTo(4f, 21f)
                lineTo(4f, 14f)
                moveTo(4f, 10f)
                lineTo(4f, 3f)
                moveTo(1f, 14f)
                horizontalLineTo(7f)

                // Track 2
                moveTo(12f, 21f)
                lineTo(12f, 12f)
                moveTo(12f, 8f)
                lineTo(12f, 3f)
                moveTo(9f, 8f)
                horizontalLineTo(15f)

                // Track 3
                moveTo(20f, 21f)
                lineTo(20f, 16f)
                moveTo(20f, 12f)
                lineTo(20f, 3f)
                moveTo(17f, 16f)
                horizontalLineTo(23f)
            }
        }.build()
    }

    val Lyrics: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelLyrics",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Modern karaoke microphone
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Mic head
                moveTo(9f, 5f)
                curveTo(9f, 3.34f, 10.34f, 2f, 12f, 2f)
                curveTo(13.66f, 2f, 15f, 3.34f, 15f, 5f)
                verticalLineTo(11f)
                curveTo(15f, 12.66f, 13.66f, 14f, 12f, 14f)
                curveTo(10.34f, 14f, 9f, 12.66f, 9f, 11f)
                close()
                // Cradle arc
                moveTo(19f, 10f)
                verticalLineTo(11f)
                curveTo(19f, 14.87f, 15.87f, 18f, 12f, 18f)
                curveTo(8.13f, 18f, 5f, 14.87f, 5f, 11f)
                verticalLineTo(10f)
                // Stem & base
                moveTo(12f, 18f)
                verticalLineTo(22f)
                moveTo(8f, 22f)
                horizontalLineTo(16f)
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelInfo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 3f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 21f)
                close()
                moveTo(12f, 16f)
                verticalLineTo(12f)
                moveTo(12f, 8f)
                horizontalLineTo(12.01f)
            }
        }.build()
    }

    val PlaylistAdd: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelPlaylistAdd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 6f)
                horizontalLineTo(16f)
                moveTo(3f, 12f)
                horizontalLineTo(12f)
                moveTo(3f, 18f)
                horizontalLineTo(10f)
                // Refined plus badge
                moveTo(18f, 14f)
                verticalLineTo(20f)
                moveTo(15f, 17f)
                horizontalLineTo(21f)
            }
        }.build()
    }

    val MusicNote: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelMusicNote",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 18f)
                verticalLineTo(5f)
                lineTo(20f, 3f)
                verticalLineTo(16f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 18f)
                curveTo(9f, 19.1f, 7.66f, 20f, 6f, 20f)
                curveTo(4.34f, 20f, 3f, 19.1f, 3f, 18f)
                curveTo(3f, 16.9f, 4.34f, 16f, 6f, 16f)
                curveTo(7.66f, 16f, 9f, 16.9f, 9f, 18f)
                close()
                moveTo(20f, 16f)
                curveTo(20f, 17.1f, 18.66f, 18f, 17f, 18f)
                curveTo(15.34f, 18f, 14f, 17.1f, 14f, 16f)
                curveTo(14f, 14.9f, 15.34f, 14f, 17f, 14f)
                curveTo(18.66f, 14f, 20f, 14.9f, 20f, 16f)
                close()
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(18f, 6f)
                lineTo(6f, 18f)
                moveTo(6f, 6f)
                lineTo(18f, 18f)
            }
        }.build()
    }

    val Album: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelAlbum",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Vinyl disc outer rim
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 21f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 3f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 21f)
                close()
            }
            // Center spindle hole
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 14f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 10f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 14f)
                close()
            }
        }.build()
    }

    val Artist: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelArtist",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Head circle
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 11f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 3f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 11f)
                close()
            }
            // Shoulders arc
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 21f)
                verticalLineTo(19f)
                curveTo(4f, 16.79f, 5.79f, 15f, 8f, 15f)
                horizontalLineTo(16f)
                curveTo(18.21f, 15f, 20f, 16.79f, 20f, 19f)
                verticalLineTo(21f)
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }.build()
    }

    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelAdd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 5f)
                verticalLineTo(19f)
                moveTo(5f, 12f)
                horizontalLineTo(19f)
            }
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelDelete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Can body
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 7f)
                lineTo(6f, 19f)
                curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
                horizontalLineTo(16f)
                curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
                lineTo(19f, 7f)
            }
            // Lid bar & handle
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 7f)
                horizontalLineTo(20f)
                moveTo(9f, 7f)
                verticalLineTo(4f)
                curveTo(9f, 3.45f, 9.45f, 3f, 10f, 3f)
                horizontalLineTo(14f)
                curveTo(14.55f, 3f, 15f, 3.45f, 15f, 4f)
                verticalLineTo(7f)
            }
        }.build()
    }

    val Palette: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelPalette",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 12f)
                curveTo(21f, 14.5f, 19f, 16.5f, 16.5f, 16.5f)
                horizontalLineTo(15f)
                curveTo(13.9f, 16.5f, 13f, 17.4f, 13f, 18.5f)
                curveTo(13f, 19.2f, 13.3f, 19.8f, 13.7f, 20.2f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 21f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 3f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(8.5f, 8.5f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 8.5f, 6.1f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.5f, 8.5f)
                close()
                moveTo(13.5f, 7.5f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13.5f, 5.1f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 7.5f)
                close()
                moveTo(17f, 11.5f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17f, 9.1f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 11.5f)
                close()
            }
        }.build()
    }

    val Storage: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelStorage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Top disk
                moveTo(4f, 7f)
                curveTo(4f, 5.34f, 7.58f, 4f, 12f, 4f)
                curveTo(16.42f, 4f, 20f, 5.34f, 20f, 7f)
                curveTo(20f, 8.66f, 16.42f, 10f, 12f, 10f)
                curveTo(7.58f, 10f, 4f, 8.66f, 4f, 7f)
                close()
                // Middle curve
                moveTo(4f, 12f)
                curveTo(4f, 13.66f, 7.58f, 15f, 12f, 15f)
                curveTo(16.42f, 15f, 20f, 13.66f, 20f, 12f)
                // Bottom curve & outer walls
                moveTo(4f, 7f)
                verticalLineTo(17f)
                curveTo(4f, 18.66f, 7.58f, 20f, 12f, 20f)
                curveTo(16.42f, 20f, 20f, 18.66f, 20f, 17f)
                verticalLineTo(7f)
            }
        }.build()
    }

    val Clean: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelClean",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Sparkle 1 (Large)
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                curveTo(12f, 7.5f, 15.5f, 11f, 20f, 11f)
                curveTo(15.5f, 11f, 12f, 14.5f, 12f, 19f)
                curveTo(12f, 14.5f, 8.5f, 11f, 4f, 11f)
                curveTo(8.5f, 11f, 12f, 7.5f, 12f, 3f)
                close()
            }
            // Sparkle 2 (Small)
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 16f)
                curveTo(19f, 17.5f, 20.5f, 19f, 22f, 19f)
                curveTo(20.5f, 19f, 19f, 20.5f, 19f, 22f)
                curveTo(19f, 20.5f, 17.5f, 19f, 16f, 19f)
                curveTo(17.5f, 19f, 19f, 17.5f, 19f, 16f)
                close()
            }
        }.build()
    }

    val Update: ImageVector by lazy {
        ImageVector.Builder(
            name = "AurelUpdate",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Down arrow
                moveTo(12f, 4f)
                verticalLineTo(14f)
                moveTo(8f, 10f)
                lineTo(12f, 14f)
                lineTo(16f, 10f)
                // Bottom tray
                moveTo(4f, 17f)
                verticalLineTo(19f)
                curveTo(4f, 19.55f, 4.45f, 20f, 5f, 20f)
                horizontalLineTo(19f)
                curveTo(19.55f, 20f, 20f, 19.55f, 20f, 19f)
                verticalLineTo(17f)
            }
        }.build()
    }
}
