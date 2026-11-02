package com.aurel.music.core.database.di

import android.content.Context
import androidx.room.Room
import com.aurel.music.core.database.AurelDatabase
import com.aurel.music.core.database.dao.PlaylistDao
import com.aurel.music.core.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAurelDatabase(
        @ApplicationContext context: Context
    ): AurelDatabase {
        return Room.databaseBuilder(
            context,
            AurelDatabase::class.java,
            "aurel.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSongDao(database: AurelDatabase): SongDao = database.songDao()

    @Provides
    fun providePlaylistDao(database: AurelDatabase): PlaylistDao = database.playlistDao()
}
