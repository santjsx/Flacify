package com.flacify.music.core.database.dao

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.flacify.music.core.database.entity.SongEntity
import com.flacify.music.core.model.Album
import com.flacify.music.core.model.Artist
import kotlinx.coroutines.flow.Flow

data class AlbumEntityProjection(
    @ColumnInfo(name = "album_id") val id: Long,
    @ColumnInfo(name = "album_title") val title: String,
    @ColumnInfo(name = "artist_id") val artistId: Long,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "track_count") val trackCount: Int,
    @ColumnInfo(name = "year") val year: Int
) {
    fun toDomain(): Album = Album(
        id = id,
        title = title,
        artistId = artistId,
        artistName = artistName,
        trackCount = trackCount,
        year = year,
        artworkUri = "content://media/external/audio/albumart/$id"
    )
}

data class ArtistEntityProjection(
    @ColumnInfo(name = "artist_id") val id: Long,
    @ColumnInfo(name = "artist_name") val name: String,
    @ColumnInfo(name = "album_count") val albumCount: Int,
    @ColumnInfo(name = "track_count") val trackCount: Int
) {
    fun toDomain(): Artist = Artist(
        id = id,
        name = name,
        albumCount = albumCount,
        trackCount = trackCount
    )
}

data class UserSongMetadata(
    val id: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "play_count") val playCount: Int,
    @ColumnInfo(name = "last_played_timestamp") val lastPlayedTimestamp: Long
)

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

    // Albums & Artists Collections
    @Query("""
        SELECT 
            album_id, 
            album_title, 
            artist_id, 
            artist_name, 
            COUNT(*) AS track_count, 
            MAX(year) AS year
        FROM songs 
        GROUP BY album_id 
        ORDER BY album_title COLLATE NOCASE ASC
    """)
    fun getAllAlbums(): Flow<List<AlbumEntityProjection>>

    @Query("""
        SELECT 
            artist_id, 
            artist_name, 
            COUNT(DISTINCT album_id) AS album_count, 
            COUNT(*) AS track_count 
        FROM songs 
        GROUP BY artist_id 
        ORDER BY artist_name COLLATE NOCASE ASC
    """)
    fun getAllArtists(): Flow<List<ArtistEntityProjection>>

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track_number ASC, title COLLATE NOCASE ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artist_id = :artistId ORDER BY album_title COLLATE NOCASE ASC, track_number ASC")
    fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>

    @Query("UPDATE songs SET is_favorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: Long, isFavorite: Boolean)

    @Query("SELECT is_favorite FROM songs WHERE id = :songId")
    fun isFavoriteFlow(songId: Long): Flow<Boolean?>

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

    @Query("SELECT id, is_favorite, play_count, last_played_timestamp FROM songs")
    suspend fun getAllUserMetadata(): List<UserSongMetadata>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>)

}

