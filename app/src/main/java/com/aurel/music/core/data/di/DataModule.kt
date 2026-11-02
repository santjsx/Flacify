package com.aurel.music.core.data.di

import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.data.repository.PlaylistRepositoryImpl
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.data.repository.SongRepositoryImpl
import com.aurel.music.core.data.repository.UserPreferencesRepository
import com.aurel.music.core.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(
        impl: SongRepositoryImpl
    ): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}

