package com.flacify.music.core.designsystem.icon

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Official Lucide Icons System for FLACify (https://lucide.dev/icons)
 *
 * Fully calibrated with:
 * - Optical stroke width: 2.5px (strokeLineWidth = 2.5f)
 * - Stroke cap: Round
 * - Stroke join: Round
 * - Coordinate viewport: 24x24 standard Lucide grid
 */
object FlacifyIcons {

    private fun lucideIcon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
    }

    private fun ImageVector.Builder.lucidePath(
        pathData: String,
        strokeLineWidth: Float = 2.5f,
        strokeColor: Color = Color.White,
        fill: Brush? = null
    ) {
        addPath(
            pathData = addPathNodes(pathData),
            stroke = SolidColor(strokeColor),
            strokeLineWidth = strokeLineWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            fill = fill
        )
    }

    private fun ImageVector.Builder.lucideFilledPath(
        pathData: String,
        fillColor: Color = Color.White
    ) {
        addPath(
            pathData = addPathNodes(pathData),
            stroke = null,
            strokeLineWidth = 0f,
            fill = SolidColor(fillColor)
        )
    }

    val Home: ImageVector by lazy {
        lucideIcon("FLACifyHome") {
            lucidePath("M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8")
            lucidePath("M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z")
        }
    }

    val Library: ImageVector by lazy {
        lucideIcon("FLACifyLibrary") {
            lucidePath("M21 16V3a1 1 0 0 0-1.28-.96l-10 2.917a1 1 0 0 0-.72.96V19")
            lucidePath("M 15.0 16.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
            lucidePath("M 3.0 19.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
        }
    }

    val Search: ImageVector by lazy {
        lucideIcon("FLACifySearch") {
            lucidePath("m21 21-4.34-4.34")
            lucidePath("M 3.0 11.0 a 8.0 8.0 0 1 0 16.0 0 a 8.0 8.0 0 1 0 -16.0 0")
        }
    }

    val More: ImageVector by lazy {
        lucideIcon("FLACifyMore") {
            lucidePath("M 11.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0", fill = SolidColor(Color.White))
            lucidePath("M 18.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0", fill = SolidColor(Color.White))
            lucidePath("M 4.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0", fill = SolidColor(Color.White))
        }
    }

    val Settings: ImageVector by lazy {
        lucideIcon("FLACifySettings") {
            lucidePath("M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z")
            lucidePath("M 9.0 12.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
        }
    }

    val Refresh: ImageVector by lazy {
        lucideIcon("FLACifyRefresh") {
            lucidePath("M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8")
            lucidePath("M21 3v5h-5")
        }
    }

    val Play: ImageVector by lazy {
        lucideIcon("FLACifyPlay") {
            lucidePath("M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z", fill = SolidColor(Color.White))
        }
    }

    val Pause: ImageVector by lazy {
        lucideIcon("FLACifyPause") {
            lucidePath("M 15.0 3.0 h 3.0 a 1.0 1.0 0 0 1 1.0 1.0 v 16.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -3.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -16.0 a 1.0 1.0 0 0 1 1.0 -1.0 z", fill = SolidColor(Color.White))
            lucidePath("M 6.0 3.0 h 3.0 a 1.0 1.0 0 0 1 1.0 1.0 v 16.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -3.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -16.0 a 1.0 1.0 0 0 1 1.0 -1.0 z", fill = SolidColor(Color.White))
        }
    }

    val SkipForward: ImageVector by lazy {
        lucideIcon("FLACifySkipForward") {
            lucidePath("M21 4v16")
            lucidePath("M6.029 4.285A2 2 0 0 0 3 6v12a2 2 0 0 0 3.029 1.715l9.997-5.998a2 2 0 0 0 .003-3.432z")
        }
    }

    val SkipBack: ImageVector by lazy {
        lucideIcon("FLACifySkipBack") {
            lucidePath("M17.971 4.285A2 2 0 0 1 21 6v12a2 2 0 0 1-3.029 1.715l-9.997-5.998a2 2 0 0 1-.003-3.432z")
            lucidePath("M3 20V4")
        }
    }

    val Shuffle: ImageVector by lazy {
        lucideIcon("FLACifyShuffle") {
            lucidePath("m18 14 4 4-4 4")
            lucidePath("m18 2 4 4-4 4")
            lucidePath("M2 18h1.973a4 4 0 0 0 3.3-1.7l5.454-8.6a4 4 0 0 1 3.3-1.7H22")
            lucidePath("M2 6h1.972a4 4 0 0 1 3.6 2.2")
            lucidePath("M22 18h-6.041a4 4 0 0 1-3.3-1.8l-.359-.45")
        }
    }

    val Repeat: ImageVector by lazy {
        lucideIcon("FLACifyRepeat") {
            lucidePath("m17 2 4 4-4 4")
            lucidePath("M3 11v-1a4 4 0 0 1 4-4h14")
            lucidePath("m7 22-4-4 4-4")
            lucidePath("M21 13v1a4 4 0 0 1-4 4H3")
        }
    }

    val RepeatOne: ImageVector by lazy {
        lucideIcon("FLACifyRepeatOne") {
            lucidePath("m17 2 4 4-4 4")
            lucidePath("M3 11v-1a4 4 0 0 1 4-4h14")
            lucidePath("m7 22-4-4 4-4")
            lucidePath("M21 13v1a4 4 0 0 1-4 4H3")
            lucidePath("M11 10h1v4")
        }
    }

    val Queue: ImageVector by lazy {
        lucideIcon("FLACifyQueue") {
            lucidePath("M16 5H3")
            lucidePath("M11 12H3")
            lucidePath("M11 19H3")
            lucidePath("M21 16V5")
            lucidePath("M 15.0 16.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
        }
    }

    val SleepTimer: ImageVector by lazy {
        lucideIcon("FLACifySleepTimer") {
            lucidePath("M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401")
        }
    }

    val HeartOutline: ImageVector by lazy {
        lucideIcon("FLACifyHeartOutline") {
            lucidePath("M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5")
        }
    }

    val HeartFilled: ImageVector by lazy {
        lucideIcon("FLACifyHeartFilled") {
            lucideFilledPath("M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5")
        }
    }

    val ChevronDown: ImageVector by lazy {
        lucideIcon("FLACifyChevronDown") {
            lucidePath("m6 9 6 6 6-6")
        }
    }

    val ChevronRight: ImageVector by lazy {
        lucideIcon("FLACifyChevronRight") {
            lucidePath("m9 18 6-6-6-6")
        }
    }

    val Sort: ImageVector by lazy {
        lucideIcon("FLACifySort") {
            lucidePath("m21 16-4 4-4-4")
            lucidePath("M17 20V4")
            lucidePath("m3 8 4-4 4 4")
            lucidePath("M7 4v16")
        }
    }

    val Equalizer: ImageVector by lazy {
        lucideIcon("FLACifyEqualizer") {
            lucidePath("M2 10v3")
            lucidePath("M6 6v11")
            lucidePath("M10 3v18")
            lucidePath("M14 8v7")
            lucidePath("M18 5v13")
            lucidePath("M22 10v3")
        }
    }

    val Lyrics: ImageVector by lazy {
        lucideIcon("FLACifyLyrics") {
            lucidePath("m11 7.601-5.994 8.19a1 1 0 0 0 .1 1.298l.817.818a1 1 0 0 0 1.314.087L15.09 12")
            lucidePath("M16.5 21.174C15.5 20.5 14.372 20 13 20c-2.058 0-3.928 2.356-6 2-2.072-.356-2.775-3.369-1.5-4.5")
            lucidePath("M 11.0 7.0 a 5.0 5.0 0 1 0 10.0 0 a 5.0 5.0 0 1 0 -10.0 0")
        }
    }

    val Info: ImageVector by lazy {
        lucideIcon("FLACifyInfo") {
            lucidePath("M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0")
            lucidePath("M12 16v-4")
            lucidePath("M12 8h.01")
        }
    }

    val Shield: ImageVector by lazy {
        lucideIcon("FLACifyShield") {
            lucidePath("M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z")
            lucidePath("m9 12 2 2 4-4")
        }
    }

    val Developer: ImageVector by lazy {
        lucideIcon("FLACifyDeveloper") {
            lucidePath("m18 16 4-4-4-4")
            lucidePath("m6 8-4 4 4 4")
            lucidePath("m14.5 4-5 16")
        }
    }

    val PlaylistAdd: ImageVector by lazy {
        lucideIcon("FLACifyPlaylistAdd") {
            lucidePath("M16 5H3")
            lucidePath("M11 12H3")
            lucidePath("M16 19H3")
            lucidePath("M18 9v6")
            lucidePath("M21 12h-6")
        }
    }

    val MusicNote: ImageVector by lazy {
        lucideIcon("FLACifyMusicNote") {
            lucidePath("M21 16V3a1 1 0 0 0-1.28-.96l-10 2.917a1 1 0 0 0-.72.96V19")
            lucidePath("M 15.0 16.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
            lucidePath("M 3.0 19.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0")
        }
    }

    val Close: ImageVector by lazy {
        lucideIcon("FLACifyClose") {
            lucidePath("M18 6 6 18")
            lucidePath("m6 6 12 12")
        }
    }

    val Check: ImageVector by lazy {
        lucideIcon("FLACifyCheck") {
            lucidePath("M20 6 9 17l-5-5")
        }
    }

    val Album: ImageVector by lazy {
        lucideIcon("FLACifyAlbum") {
            lucidePath("M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0")
            lucidePath("M 10.0 12.0 a 2.0 2.0 0 1 0 4.0 0 a 2.0 2.0 0 1 0 -4.0 0")
        }
    }

    val Artist: ImageVector by lazy {
        lucideIcon("FLACifyArtist") {
            lucidePath("M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2")
            lucidePath("M 8.0 7.0 a 4.0 4.0 0 1 0 8.0 0 a 4.0 4.0 0 1 0 -8.0 0")
        }
    }

    val Add: ImageVector by lazy {
        lucideIcon("FLACifyAdd") {
            lucidePath("M5 12h14")
            lucidePath("M12 5v14")
        }
    }

    val Delete: ImageVector by lazy {
        lucideIcon("FLACifyDelete") {
            lucidePath("M10 11v6")
            lucidePath("M14 11v6")
            lucidePath("M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6")
            lucidePath("M3 6h18")
            lucidePath("M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2")
        }
    }

    val Palette: ImageVector by lazy {
        lucideIcon("FLACifyPalette") {
            lucidePath("M12 22a1 1 0 0 1 0-20 10 9 0 0 1 10 9 5 5 0 0 1-5 5h-2.25a1.75 1.75 0 0 0-1.4 2.8l.3.4a1.75 1.75 0 0 1-1.4 2.8z")
            lucidePath("M 13.0 6.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0")
            lucidePath("M 17.0 10.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0")
            lucidePath("M 6.0 12.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0")
            lucidePath("M 8.0 7.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0")
        }
    }

    val Storage: ImageVector by lazy {
        lucideIcon("FLACifyStorage") {
            lucidePath("M10 16h.01")
            lucidePath("M2.212 11.577a2 2 0 0 0-.212.896V18a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5.527a2 2 0 0 0-.212-.896L18.55 5.11A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z")
            lucidePath("M21.946 12.013H2.054")
            lucidePath("M6 16h.01")
        }
    }

    val Clean: ImageVector by lazy {
        lucideIcon("FLACifyClean") {
            lucidePath("M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z")
            lucidePath("M20 2v4")
            lucidePath("M22 4h-4")
            lucidePath("M 2.0 20.0 a 2.0 2.0 0 1 0 4.0 0 a 2.0 2.0 0 1 0 -4.0 0")
        }
    }

    val Update: ImageVector by lazy {
        lucideIcon("FLACifyUpdate") {
            lucidePath("M12 15V3")
            lucidePath("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4")
            lucidePath("m7 10 5 5 5-5")
        }
    }

    val AudioWaveform: ImageVector by lazy {
        lucideIcon("FLACifyAudioWaveform") {
            lucidePath("M2 13a2 2 0 0 0 2-2V7a2 2 0 0 1 4 0v13a2 2 0 0 0 4 0V4a2 2 0 0 1 4 0v13a2 2 0 0 0 4 0v-4a2 2 0 0 1 2-2")
        }
    }
}
