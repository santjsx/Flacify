package com.flacify.music

import com.flacify.music.core.data.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun parse_validLrcString_returnsSynchronizedLyrics() {
        val sampleLrc = """
            [00:12.50]First line of the song
            [01:05.120]Chorus begins here
            [02:30.00]Outro starts
        """.trimIndent()

        val lyrics = LrcParser.parse(sampleLrc)

        assertTrue(lyrics.isSynchronized)
        assertEquals(3, lyrics.lines.size)

        // 12.50 seconds = 12500 ms
        assertEquals(12500L, lyrics.lines[0].timestampMs)
        assertEquals("First line of the song", lyrics.lines[0].text)

        // 1 minute 5.120 seconds = 65120 ms
        assertEquals(65120L, lyrics.lines[1].timestampMs)
        assertEquals("Chorus begins here", lyrics.lines[1].text)

        // 2 minutes 30 seconds = 150000 ms
        assertEquals(150000L, lyrics.lines[2].timestampMs)
    }

    @Test
    fun findActiveLineIndex_interpolatesCurrentPosition() {
        val sampleLrc = """
            [00:10.00]Verse 1
            [00:20.00]Verse 2
            [00:30.00]Chorus
        """.trimIndent()

        val lyrics = LrcParser.parse(sampleLrc)

        assertEquals(0, lyrics.findActiveLineIndex(15000L)) // Between 10s and 20s -> Verse 1
        assertEquals(1, lyrics.findActiveLineIndex(25000L)) // Between 20s and 30s -> Verse 2
        assertEquals(2, lyrics.findActiveLineIndex(35000L)) // After 30s -> Chorus
    }
}

