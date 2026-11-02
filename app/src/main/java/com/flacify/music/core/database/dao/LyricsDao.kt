package com.flacify.music.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.flacify.music.core.database.entity.SongLyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {

    @Query("SELECT * FROM song_lyrics WHERE song_id = :songId")
    suspend fun getLyrics(songId: Long): SongLyricsEntity?

    @Query("SELECT * FROM song_lyrics WHERE song_id = :songId")
    fun getLyricsFlow(songId: Long): Flow<SongLyricsEntity?>

    @Upsert
    suspend fun upsertLyrics(lyrics: SongLyricsEntity)

    @Query("DELETE FROM song_lyrics WHERE song_id = :songId")
    suspend fun deleteLyrics(songId: Long)
}

