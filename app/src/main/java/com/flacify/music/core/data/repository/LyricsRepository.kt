package com.flacify.music.core.data.repository

import com.flacify.music.core.data.lyrics.Lyrics
import com.flacify.music.core.model.Song
import kotlinx.coroutines.flow.Flow

sealed interface LyricsResult {
    data object Loading : LyricsResult
    data class Success(
        val lyrics: Lyrics,
        val isInstrumental: Boolean = false,
        val source: String = "unknown"
    ) : LyricsResult
    data class Empty(val reason: String = "No lyrics found") : LyricsResult
}

interface LyricsRepository {
    fun getLyrics(song: Song): Flow<LyricsResult>
    suspend fun importLyrics(songId: Long, lrcContent: String): LyricsResult
}

