package com.flacify.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flacify.music.core.database.dao.PlaylistDao
import com.flacify.music.core.database.dao.SongDao
import com.flacify.music.core.database.entity.PlaylistEntity
import com.flacify.music.core.database.entity.PlaylistTrackEntity
import com.flacify.music.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlacifyDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
}

