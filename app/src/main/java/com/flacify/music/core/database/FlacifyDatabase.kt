package com.flacify.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flacify.music.core.database.dao.LyricsDao
import com.flacify.music.core.database.dao.PlaylistDao
import com.flacify.music.core.database.dao.SongDao
import com.flacify.music.core.database.entity.PlaylistEntity
import com.flacify.music.core.database.entity.PlaylistTrackEntity
import com.flacify.music.core.database.entity.SongEntity
import com.flacify.music.core.database.entity.SongLyricsEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        SongLyricsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FlacifyDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsDao(): LyricsDao
}
