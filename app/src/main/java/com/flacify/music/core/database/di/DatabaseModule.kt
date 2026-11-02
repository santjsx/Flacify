package com.flacify.music.core.database.di

import android.content.Context
import androidx.room.Room
import com.flacify.music.core.database.FlacifyDatabase
import com.flacify.music.core.database.dao.PlaylistDao
import com.flacify.music.core.database.dao.SongDao
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
    ): FlacifyDatabase {
        return Room.databaseBuilder(
            context,
            FlacifyDatabase::class.java,
            "flacify.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSongDao(database: FlacifyDatabase): SongDao = database.songDao()

    @Provides
    fun providePlaylistDao(database: FlacifyDatabase): PlaylistDao = database.playlistDao()
}

