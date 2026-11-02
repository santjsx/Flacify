package com.aurel.music.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.aurel.music.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getSongsPagedByTitleAsc(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE DESC")
    fun getSongsPagedByTitleDesc(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY artist_name COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun getSongsPagedByArtist(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY album_title COLLATE NOCASE ASC, track_number ASC")
    fun getSongsPagedByAlbum(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY duration_ms DESC")
    fun getSongsPagedByDuration(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY date_added DESC")
    fun getSongsPagedByDateAdded(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY play_count DESC")
    fun getSongsPagedByPlayCount(): PagingSource<Int, SongEntity>

    // Home Screen Sections
    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT album_id) FROM songs")
    fun getAlbumCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT artist_id) FROM songs")
    fun getArtistCount(): Flow<Int>

    @Query("SELECT * FROM songs ORDER BY date_added DESC LIMIT 15")
    fun getRecentlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE play_count > 0 ORDER BY play_count DESC, last_played_timestamp DESC LIMIT 20")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    // Favorites
    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("UPDATE songs SET is_favorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: Long, isFavorite: Boolean)

    // Play count tracker
    @Query("UPDATE songs SET play_count = play_count + 1, last_played_timestamp = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long)

    // Search
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
           OR artist_name LIKE '%' || :query || '%' 
           OR album_title LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC 
        LIMIT 50
    """)
    suspend fun searchSongs(query: String): List<SongEntity>

    // Incremental Scanner operations
    @Query("SELECT id FROM songs")
    suspend fun getAllSongIds(): List<Long>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>)

    @Query("DELETE FROM songs")
    suspend fun clearAll()
}

