package com.aurel.music.core.data.repository

import com.aurel.music.core.database.dao.PlaylistDao
import com.aurel.music.core.database.entity.PlaylistEntity
import com.aurel.music.core.database.entity.PlaylistTrackEntity
import com.aurel.music.core.model.Playlist
import com.aurel.music.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsWithCount().map { list ->
            list.map { item ->
                Playlist(
                    id = item.id,
                    name = item.name,
                    trackCount = item.trackCount,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt
                )
            }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        val entity = PlaylistEntity(
            name = name.trim(),
            createdAt = now,
            updatedAt = now
        )
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName.trim(), System.currentTimeMillis())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val track = PlaylistTrackEntity(
            playlistId = playlistId,
            songId = songId,
            sortOrder = System.currentTimeMillis().toInt()
        )
        playlistDao.addSongToPlaylist(track)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
}

