package com.aurel.music.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.aurel.music.core.data.scanner.MediaStoreScanner
import com.aurel.music.core.data.scanner.ScanStatus
import com.aurel.music.core.database.dao.SongDao
import com.aurel.music.core.model.Song
import com.aurel.music.core.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SongRepository {
    fun getSongsPaged(sortOption: SortOption): Flow<PagingData<Song>>
    fun getSongCount(): Flow<Int>
    fun getAlbumCount(): Flow<Int>
    fun getArtistCount(): Flow<Int>
    fun getRecentlyAddedSongs(): Flow<List<Song>>
    fun getMostPlayedSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun setFavorite(songId: Long, isFavorite: Boolean)
    suspend fun recordPlayback(songId: Long)
    suspend fun searchSongs(query: String): List<Song>
    suspend fun scanLibrary(): ScanStatus
}

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val scanner: MediaStoreScanner
) : SongRepository {

    override fun getSongsPaged(sortOption: SortOption): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 100,
                enablePlaceholders = true,
                maxSize = 250 // Bounded memory buffer for 50,000+ songs
            ),
            pagingSourceFactory = {
                when (sortOption) {
                    SortOption.TITLE_ASC -> songDao.getSongsPagedByTitleAsc()
                    SortOption.TITLE_DESC -> songDao.getSongsPagedByTitleDesc()
                    SortOption.ARTIST -> songDao.getSongsPagedByArtist()
                    SortOption.ALBUM -> songDao.getSongsPagedByAlbum()
                    SortOption.DURATION -> songDao.getSongsPagedByDuration()
                    SortOption.DATE_ADDED -> songDao.getSongsPagedByDateAdded()
                    SortOption.PLAY_COUNT -> songDao.getSongsPagedByPlayCount()
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getSongCount(): Flow<Int> = songDao.getSongCount()

    override fun getAlbumCount(): Flow<Int> = songDao.getAlbumCount()

    override fun getArtistCount(): Flow<Int> = songDao.getArtistCount()

    override fun getRecentlyAddedSongs(): Flow<List<Song>> =
        songDao.getRecentlyAddedSongs().map { list -> list.map { it.toDomain() } }

    override fun getMostPlayedSongs(): Flow<List<Song>> =
        songDao.getMostPlayedSongs().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteSongs(): Flow<List<Song>> =
        songDao.getFavoriteSongs().map { list -> list.map { it.toDomain() } }

    override suspend fun setFavorite(songId: Long, isFavorite: Boolean) {
        songDao.setFavorite(songId, isFavorite)
    }

    override suspend fun recordPlayback(songId: Long) {
        songDao.incrementPlayCount(songId, System.currentTimeMillis())
    }

    override suspend fun searchSongs(query: String): List<Song> {
        return songDao.searchSongs(query.trim()).map { it.toDomain() }
    }

    override suspend fun scanLibrary(): ScanStatus {
        return scanner.scanLibrary()
    }
}

