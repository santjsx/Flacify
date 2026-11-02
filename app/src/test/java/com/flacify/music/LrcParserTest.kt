package com.flacify.music

import com.flacify.music.core.data.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun parse_utf8Bom_stripsBomCleanly() {
        val bomLrc = "\uFEFF[00:05.00]Line with BOM\n[00:10.00]Second line"
        val lyrics = LrcParser.parse(bomLrc)

        assertTrue(lyrics.isSynchronized)
        assertEquals(2, lyrics.lines.size)
        assertEquals(5000L, lyrics.lines[0].timestampMs)
        assertEquals("Line with BOM", lyrics.lines[0].text)
    }

    @Test
    fun parse_multiTimestampLine_expandsLinesAndSorts() {
        val multiLrc = """
            [00:10.00][00:30.00]Repeated chorus
            [00:20.00]Verse between
        """.trimIndent()

        val lyrics = LrcParser.parse(multiLrc)

        assertTrue(lyrics.isSynchronized)
        assertEquals(3, lyrics.lines.size)
        assertEquals(10000L, lyrics.lines[0].timestampMs)
        assertEquals("Repeated chorus", lyrics.lines[0].text)
        assertEquals(20000L, lyrics.lines[1].timestampMs)
        assertEquals("Verse between", lyrics.lines[1].text)
        assertEquals(30000L, lyrics.lines[2].timestampMs)
        assertEquals("Repeated chorus", lyrics.lines[2].text)
    }

    @Test
    fun parse_offsetTag_adjustsAllTimestamps() {
        val offsetLrc = """
            [offset:+500]
            [00:02.00]Line with offset
        """.trimIndent()

        val lyrics = LrcParser.parse(offsetLrc)

        assertTrue(lyrics.isSynchronized)
        assertEquals(1, lyrics.lines.size)
        assertEquals(2500L, lyrics.lines[0].timestampMs)
        assertEquals("Line with offset", lyrics.lines[0].text)
    }

    @Test
    fun parse_plainText_returnsUnsynchronizedLyrics() {
        val plainText = """
            First verse without time
            Second line without time
        """.trimIndent()

        val lyrics = LrcParser.parse(plainText)

        assertFalse(lyrics.isSynchronized)
        assertEquals(2, lyrics.lines.size)
        assertEquals("First verse without time", lyrics.lines[0].text)
        assertEquals("Second line without time", lyrics.lines[1].text)
    }

    @Test
    fun findActiveLineIndex_interpolatesCurrentPosition() {
        val sampleLrc = """
            [00:10.00]Verse 1
            [00:20.00]Verse 2
            [00:30.00]Chorus
        """.trimIndent()

        val lyrics = LrcParser.parse(sampleLrc)

        assertEquals(-1, lyrics.findActiveLineIndex(5000L))  // Before intro -> -1
        assertEquals(0, lyrics.findActiveLineIndex(15000L)) // Between 10s and 20s -> Verse 1
        assertEquals(1, lyrics.findActiveLineIndex(25000L)) // Between 20s and 30s -> Verse 2
        assertEquals(2, lyrics.findActiveLineIndex(35000L)) // After 30s -> Chorus
    }
}
