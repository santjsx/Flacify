package com.aurel.music.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aurel.music.core.model.Song

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist_name"]),
        Index(value = ["album_title"]),
        Index(value = ["date_added"]),
        Index(value = ["play_count"]),
        Index(value = ["is_favorite"])
    ]
)
data class SongEntity(
    @PrimaryKey
    val id: Long, // MediaStore._ID
    @ColumnInfo(name = "media_uri")
    val mediaUri: String,
    val title: String,
    @ColumnInfo(name = "artist_id")
    val artistId: Long,
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    @ColumnInfo(name = "album_title")
    val albumTitle: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "track_number")
    val trackNumber: Int = 0,
    @ColumnInfo(name = "disc_number")
    val discNumber: Int = 1,
    val year: Int = 0,
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = 0,
    @ColumnInfo(name = "date_modified")
    val dateModified: Long = 0,
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0,
    @ColumnInfo(name = "mime_type")
    val mimeType: String = "",
    val bitrate: Int = 0,
    @ColumnInfo(name = "sample_rate")
    val sampleRate: Int = 0,
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    @ColumnInfo(name = "last_played_timestamp")
    val lastPlayedTimestamp: Long = 0,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
) {
    fun toDomain(): Song = Song(
        id = id,
        mediaUri = mediaUri,
        title = title,
        artistId = artistId,
        artistName = artistName,
        albumId = albumId,
        albumTitle = albumTitle,
        durationMs = durationMs,
        trackNumber = trackNumber,
        discNumber = discNumber,
        year = year,
        dateAdded = dateAdded,
        dateModified = dateModified,
        fileSize = fileSize,
        mimeType = mimeType,
        bitrate = bitrate,
        sampleRate = sampleRate,
        playCount = playCount,
        lastPlayedTimestamp = lastPlayedTimestamp,
        isFavorite = isFavorite
    )

    companion object {
        fun fromDomain(song: Song): SongEntity = SongEntity(
            id = song.id,
            mediaUri = song.mediaUri,
            title = song.title,
            artistId = song.artistId,
            artistName = song.artistName,
            albumId = song.albumId,
            albumTitle = song.albumTitle,
            durationMs = song.durationMs,
            trackNumber = song.trackNumber,
            discNumber = song.discNumber,
            year = song.year,
            dateAdded = song.dateAdded,
            dateModified = song.dateModified,
            fileSize = song.fileSize,
            mimeType = song.mimeType,
            bitrate = song.bitrate,
            sampleRate = song.sampleRate,
            playCount = song.playCount,
            lastPlayedTimestamp = song.lastPlayedTimestamp,
            isFavorite = song.isFavorite
        )
    }
}

