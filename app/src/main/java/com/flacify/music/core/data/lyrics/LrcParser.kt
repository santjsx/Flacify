package com.flacify.music.core.data.lyrics

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
        return -1
    }
}

object LrcParser {
    // Regex matching standard LRC timestamps: [mm:ss.xx], [m:ss.xx], [mm:ss.xxx], [mm:ss:xx]
    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{2,3}))?]""")
    private val OFFSET_REGEX = Regex("""\[offset:\s*([+-]?\d+)\s*]""", RegexOption.IGNORE_CASE)

    fun parse(lrcContent: String): Lyrics {
        val sanitizedContent = lrcContent.removePrefix("\uFEFF").trim()
        if (sanitizedContent.isBlank()) {
            return Lyrics(emptyList(), isSynchronized = false)
        }

        val lines = mutableListOf<LyricLine>()
        var hasTimestamps = false
        var globalOffsetMs = 0L

        // Pass 1: Look for global [offset:+/-ms] tag
        for (line in sanitizedContent.lineSequence()) {
            val offsetMatch = OFFSET_REGEX.find(line.trim())
            if (offsetMatch != null) {
                globalOffsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                break
            }
        }

        // Pass 2: Parse lyric lines and timestamps
        for (rawLine in sanitizedContent.lineSequence()) {
            val trimmedLine = rawLine.trim()
            if (trimmedLine.isBlank()) continue

            // If line is metadata like [ar:Coldplay] or [offset:...], skip it
            if (trimmedLine.startsWith("[") && OFFSET_REGEX.matches(trimmedLine)) continue

            val timestampMatches = TIMESTAMP_REGEX.findAll(trimmedLine).toList()
            if (timestampMatches.isNotEmpty()) {
                hasTimestamps = true
                val text = trimmedLine.replace(TIMESTAMP_REGEX, "").trim()
                if (text.isNotEmpty()) {
                    for (match in timestampMatches) {
                        val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                        val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                        val fractionStr = match.groupValues[3]
                        val millis = when (fractionStr.length) {
                            1 -> (fractionStr.toLongOrNull() ?: 0L) * 100
                            2 -> (fractionStr.toLongOrNull() ?: 0L) * 10
                            3 -> fractionStr.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        val rawMs = (minutes * 60 + seconds) * 1000 + millis
                        val adjustedMs = maxOf(0L, rawMs + globalOffsetMs)
                        lines.add(LyricLine(adjustedMs, text))
                    }
                }
            } else if (!trimmedLine.startsWith("[")) {
                // Unsynchronized plain text line
                lines.add(LyricLine(0L, trimmedLine))
            }
        }

        return Lyrics(
            lines = lines.sortedBy { it.timestampMs },
            isSynchronized = hasTimestamps
        )
    }
}
