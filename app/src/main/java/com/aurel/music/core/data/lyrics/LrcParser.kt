package com.aurel.music.core.data.lyrics

import androidx.compose.runtime.Immutable

@Immutable
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

@Immutable
data class Lyrics(
    val lines: List<LyricLine>,
    val isSynchronized: Boolean
) {
    fun findActiveLineIndex(currentPositionMs: Long): Int {
        if (!isSynchronized || lines.isEmpty()) return -1
        for (i in lines.indices.reversed()) {
            if (currentPositionMs >= lines[i].timestampMs) {
                return i
            }
        }
        return 0
    }
}

object LrcParser {
    // Regex matching standard LRC timestamps: [mm:ss.xx] or [mm:ss.xxx] or [mm:ss:xx]
    private val LRC_REGEX = Regex("""\[(\d{2}):(\d{2})(?:[.:](\d{2,3}))?](.*)""")

    fun parse(lrcContent: String): Lyrics {
        val lines = mutableListOf<LyricLine>()
        var hasTimestamps = false

        lrcContent.lineSequence().forEach { line ->
            val match = LRC_REGEX.find(line.trim())
            if (match != null) {
                hasTimestamps = true
                val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                val fractionStr = match.groupValues[3]
                val millis = when (fractionStr.length) {
                    2 -> (fractionStr.toLongOrNull() ?: 0L) * 10
                    3 -> fractionStr.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = (minutes * 60 + seconds) * 1000 + millis
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            } else if (line.isNotBlank() && !line.startsWith("[")) {
                lines.add(LyricLine(0L, line.trim()))
            }
        }

        return Lyrics(
            lines = lines.sortedBy { it.timestampMs },
            isSynchronized = hasTimestamps
        )
    }
}
