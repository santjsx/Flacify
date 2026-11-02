package com.flacify.music.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_lyrics")
data class SongLyricsEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,
    @ColumnInfo(name = "synced_lyrics")
    val syncedLyrics: String? = null,
    @ColumnInfo(name = "plain_lyrics")
    val plainLyrics: String? = null,
    @ColumnInfo(name = "is_instrumental")
    val isInstrumental: Boolean = false,
    val source: String = "unknown",
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

