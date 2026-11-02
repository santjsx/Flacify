package com.aurel.music

import com.aurel.music.core.model.RepeatMode
import com.aurel.music.core.model.ShuffleMode
import com.aurel.music.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundationSmokeTest {

    @Test
    fun songFormattedDuration_minutesAndSeconds_formatsCorrectly() {
        val song = Song(
            id = 1L,
            mediaUri = "content://media/external/audio/media/1",
            title = "Test Song",
            artistId = 1L,
            artistName = "Test Artist",
            albumId = 1L,
            albumTitle = "Test Album",
            durationMs = 215000 // 3 minutes, 35 seconds
        )
        assertEquals("3:35", song.formattedDuration)
    }

    @Test
    fun songFormattedDuration_hoursMinutesAndSeconds_formatsCorrectly() {
        val longSong = Song(
            id = 2L,
            mediaUri = "content://media/external/audio/media/2",
            title = "Long Track",
            artistId = 1L,
            artistName = "Test Artist",
            albumId = 1L,
            albumTitle = "Test Album",
            durationMs = 3665000 // 1 hour, 1 minute, 5 seconds
        )
        assertEquals("1:01:05", longSong.formattedDuration)
    }

    @Test
    fun repeatMode_cyclesCorrectly() {
        assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
        assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
        assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())
    }

    @Test
    fun shuffleMode_togglesCorrectly() {
        assertEquals(ShuffleMode.ON, ShuffleMode.OFF.toggle())
        assertEquals(ShuffleMode.OFF, ShuffleMode.ON.toggle())
    }
}

