package com.aurel.music.core.data.di

import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.data.repository.SongRepositoryImpl
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
}

