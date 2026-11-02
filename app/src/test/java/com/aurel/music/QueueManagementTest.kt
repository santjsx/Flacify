package com.aurel.music

import com.aurel.music.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueManagementTest {

    private fun createMockSong(id: Long, title: String) = Song(
        id = id,
        mediaUri = "content://media/external/audio/media/$id",
        title = title,
        artistId = 100L,
        artistName = "Artist 1",
        albumId = 200L,
        albumTitle = "Album 1",
        durationMs = 180000L
    )

    @Test
    fun queueReordering_movesItemCorrectly() {
        val song1 = createMockSong(1L, "Song A")
        val song2 = createMockSong(2L, "Song B")
        val song3 = createMockSong(3L, "Song C")

        val queue = mutableListOf(song1, song2, song3)
        
        // Move item from index 0 to index 2
        val moved = queue.removeAt(0)
        queue.add(2, moved)

        assertEquals("Song B", queue[0].title)
        assertEquals("Song C", queue[1].title)
        assertEquals("Song A", queue[2].title)
    }

    @Test
    fun queueRemoval_removesItemCorrectly() {
        val song1 = createMockSong(1L, "Song A")
        val song2 = createMockSong(2L, "Song B")
        val song3 = createMockSong(3L, "Song C")

        val queue = mutableListOf(song1, song2, song3)
        queue.removeAt(1)

        assertEquals(2, queue.size)
        assertEquals("Song A", queue[0].title)
        assertEquals("Song C", queue[1].title)
    }
}

