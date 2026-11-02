package com.aurel.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aurel.music.core.database.dao.PlaylistDao
import com.aurel.music.core.database.dao.SongDao
import com.aurel.music.core.database.entity.PlaylistEntity
import com.aurel.music.core.database.entity.PlaylistTrackEntity
import com.aurel.music.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AurelDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
}

