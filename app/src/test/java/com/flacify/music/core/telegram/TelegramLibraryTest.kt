package com.flacify.music.core.telegram

import com.flacify.music.feature.telegram.telegramGroups
import org.junit.Assert.*
import org.junit.Test

class TelegramLibraryTest {
    private fun track(id: Long, artist: String, album: String) = TelegramTrack(
        TelegramTrackEntity(1, 10, id, 1, "Song $id", artist, "song.flac", 1000,
            10, "audio/flac", null, 1, null, album), -id)

    @Test fun albumsWithTheSameNameStaySeparateAcrossArtists() {
        val groups = telegramGroups(listOf(track(1, "One", "Greatest Hits"), track(2, "Two", "Greatest Hits")), true)
        assertEquals(2, groups.size)
        assertEquals(setOf("One", "Two"), groups.map { it.subtitle }.toSet())
    }

    @Test fun artistGroupingIgnoresCaseAndSurroundingWhitespace() {
        val groups = telegramGroups(listOf(track(1, "Artist", "A"), track(2, " artist ", "B")), false)
        assertEquals(1, groups.size)
        assertEquals(listOf(-1L, -2L), groups.single().tracks.map { it.playbackId })
    }

    @Test fun missingAlbumTagsUseAnExplicitUnknownGroup() {
        assertEquals("Unknown album", telegramGroups(listOf(track(1, "Artist", "")), true).single().title)
    }
}
