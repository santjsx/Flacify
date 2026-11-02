package com.aurel.music.core.data.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aurel.music.core.database.dao.SongDao
import com.aurel.music.core.database.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ScanStatus {
    data object Idle : ScanStatus
    data class Scanning(val progress: Int, val total: Int) : ScanStatus
    data class Completed(val addedCount: Int, val deletedCount: Int) : ScanStatus
    data class Error(val message: String) : ScanStatus
}

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao
) {
    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: Flow<ScanStatus> = _scanStatus.asStateFlow()

    suspend fun scanLibrary(minDurationMs: Long = 15000L): ScanStatus = withContext(Dispatchers.IO) {
        try {
            _scanStatus.value = ScanStatus.Scanning(0, 0)

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(minDurationMs.toString())
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            val existingIds = songDao.getAllSongIds().toHashSet()
            val discoveredIds = hashSetOf<Long>()
            val discoveredSongs = mutableListOf<SongEntity>()

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                val total = cursor.count
                var processed = 0

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    discoveredIds.add(id)

                    val uri: Uri = ContentUris.withAppendedId(collection, id)
                    val rawTrack = cursor.getInt(trackCol)
                    val trackNumber = if (rawTrack >= 1000) rawTrack % 1000 else rawTrack
                    val discNumber = if (rawTrack >= 1000) rawTrack / 1000 else 1

                    val song = SongEntity(
                        id = id,
                        mediaUri = uri.toString(),
                        title = cursor.getString(titleCol) ?: "Unknown Title",
                        artistId = cursor.getLong(artistIdCol),
                        artistName = cursor.getString(artistCol) ?: "Unknown Artist",
                        albumId = cursor.getLong(albumIdCol),
                        albumTitle = cursor.getString(albumCol) ?: "Unknown Album",
                        durationMs = cursor.getLong(durationCol),
                        trackNumber = trackNumber,
                        discNumber = discNumber,
                        year = cursor.getInt(yearCol),
                        dateAdded = cursor.getLong(dateAddedCol),
                        dateModified = cursor.getLong(dateModifiedCol),
                        fileSize = cursor.getLong(sizeCol),
                        mimeType = cursor.getString(mimeCol) ?: ""
                    )
                    discoveredSongs.add(song)
                    processed++

                    if (processed % 250 == 0 || processed == total) {
                        _scanStatus.value = ScanStatus.Scanning(processed, total)
                    }
                }
            }

            // Batch upsert in chunks of 500 to keep SQLite transaction buffer bounded
            discoveredSongs.chunked(500).forEach { batch ->
                songDao.upsertSongs(batch)
            }

            // Identify and prune deleted tracks
            val removedIds = existingIds.filter { !discoveredIds.contains(it) }
            if (removedIds.isNotEmpty()) {
                removedIds.chunked(500).forEach { batch ->
                    songDao.deleteSongsByIds(batch)
                }
            }

            val addedCount = discoveredSongs.count { !existingIds.contains(it.id) }
            val completed = ScanStatus.Completed(addedCount = addedCount, deletedCount = removedIds.size)
            _scanStatus.value = completed
            completed
        } catch (e: Exception) {
            val error = ScanStatus.Error(e.message ?: "Failed to scan media store")
            _scanStatus.value = error
            error
        }
    }
}
