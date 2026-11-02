package com.flacify.music

import android.content.Context
import com.flacify.music.core.data.lyrics.LrclibResponse
import com.flacify.music.core.data.lyrics.LrclibService
import com.flacify.music.core.data.repository.LyricsRepositoryImpl
import com.flacify.music.core.data.repository.LyricsResult
import com.flacify.music.core.database.dao.LyricsDao
import com.flacify.music.core.database.entity.SongLyricsEntity
import com.flacify.music.core.model.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class LyricsRepositoryTest {

    private val context: Context = mockk(relaxed = true)
    private val lyricsDao: LyricsDao = mockk(relaxed = true)
    private val lrclibService: LrclibService = mockk(relaxed = true)

    private val repository = LyricsRepositoryImpl(
        context = context,
        lyricsDao = lyricsDao,
        lrclibService = lrclibService
    )

    private val testSong = Song(
        id = 101L,
        mediaUri = "content://media/external/audio/media/101",
        title = "Yellow",
        artistId = 1L,
        artistName = "Coldplay",
        albumId = 2L,
        albumTitle = "Parachutes",
        durationMs = 267000L
    )

    @Test
    fun getLyrics_whenCachedInRoom_emitsCachedWithoutNetwork() = runTest {
        coEvery { lyricsDao.getLyrics(101L) } returns SongLyricsEntity(
            songId = 101L,
            syncedLyrics = "[00:10.00]Look at the stars\n[00:15.00]Look how they shine",
            source = "room_cache"
        )

        val emissions = repository.getLyrics(testSong).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is LyricsResult.Loading)
        val success = emissions[1] as LyricsResult.Success
        assertEquals(2, success.lyrics.lines.size)
        assertEquals("Look at the stars", success.lyrics.lines[0].text)

        // Ensure lrclibService was never called
        coVerify(exactly = 0) { lrclibService.getLyrics(any(), any(), any(), any()) }
    }

    @Test
    fun getLyrics_whenInstrumentalCached_emitsInstrumentalSuccess() = runTest {
        coEvery { lyricsDao.getLyrics(101L) } returns SongLyricsEntity(
            songId = 101L,
            isInstrumental = true,
            source = "lrclib"
        )

        val emissions = repository.getLyrics(testSong).toList()

        val success = emissions[1] as LyricsResult.Success
        assertTrue(success.isInstrumental)
        assertEquals(0, success.lyrics.lines.size)
    }

    @Test
    fun getLyrics_whenNotInCache_fetchesFromLrclibAndCaches() = runTest {
        coEvery { lyricsDao.getLyrics(101L) } returns null
        coEvery {
            lrclibService.getLyrics(
                trackName = "Yellow",
                artistName = "Coldplay",
                albumName = "Parachutes",
                durationSeconds = 267L
            )
        } returns Response.success(
            LrclibResponse(
                id = 16232L,
                trackName = "Yellow",
                artistName = "Coldplay",
                syncedLyrics = "[00:35.00]Look at the stars\n[00:38.00]Look how they shine for you"
            )
        )

        val emissions = repository.getLyrics(testSong).toList()

        val success = emissions[1] as LyricsResult.Success
        assertEquals(2, success.lyrics.lines.size)
        assertEquals("Look at the stars", success.lyrics.lines[0].text)

        // Verify cached in DAO
        coVerify(exactly = 1) { lyricsDao.upsertLyrics(any()) }
    }

    @Test
    fun importLyrics_parsesAndSavesToRoom() = runTest {
        val lrc = "[00:01.00]Custom user lyrics"
        val result = repository.importLyrics(101L, lrc)

        assertTrue(result is LyricsResult.Success)
        val success = result as LyricsResult.Success
        assertEquals(1, success.lyrics.lines.size)
        assertEquals("Custom user lyrics", success.lyrics.lines[0].text)

        coVerify(exactly = 1) {
            lyricsDao.upsertLyrics(
                match { it.songId == 101L && it.source == "user_import" }
            )
        }
    }
}

