package com.flacify.music.core.telegram

import android.content.Context
import android.net.Uri
import androidx.room.*
import com.flacify.music.core.model.Song
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Singleton

@Entity(tableName = "telegram_tracks", primaryKeys = ["accountId", "chatId", "messageId"])
data class TelegramTrackEntity(
    val accountId: Long, val chatId: Long, val messageId: Long,
    val fileId: Int, val title: String, val artist: String, val fileName: String,
    val durationMs: Long, val fileSize: Long, val mimeType: String,
    val localThumbnailPath: String?, val addedDate: Long, val thumbnailKey: String?
)

// SQLite row identities are unique across channels. Negative playback IDs keep them
// separate from Android's MediaStore IDs; the URI always uses Telegram's stable keys.
data class TelegramTrack(@Embedded val metadata: TelegramTrackEntity, val playbackId: Long) {
    fun toSong(channelTitle: String) = with(metadata) {
        Song(id = playbackId, mediaUri = "telegram://audio/$chatId/$messageId",
            title = title, artistName = artist, artistId = -1, albumId = -1,
            albumTitle = channelTitle, durationMs = durationMs, fileSize = fileSize,
            mimeType = mimeType, dateAdded = addedDate,
            remoteArtworkUri = localThumbnailPath?.let { Uri.fromFile(File(it)).toString() })
    }
}

@Entity(tableName = "telegram_channels", primaryKeys = ["accountId", "chatId"])
data class TelegramChannel(val accountId: Long, val chatId: Long, val title: String)

@Dao
interface TelegramDao {
    @Query("SELECT * FROM telegram_channels WHERE accountId=:account ORDER BY title COLLATE NOCASE")
    fun channels(account: Long): Flow<List<TelegramChannel>>

    @Query("SELECT *, -rowid AS playbackId FROM telegram_tracks WHERE accountId=:account AND chatId=:chat ORDER BY messageId DESC")
    fun tracks(account: Long, chat: Long): Flow<List<TelegramTrack>>

    @Query("SELECT EXISTS(SELECT 1 FROM telegram_tracks WHERE accountId=:account AND chatId=:chat AND messageId=:message)")
    suspend fun indexed(account: Long, chat: Long, message: Long): Boolean

    @Query("UPDATE telegram_tracks SET fileId=:fileId WHERE accountId=:account AND chatId=:chat AND messageId=:message AND fileId!=:fileId")
    suspend fun refreshFileId(account: Long, chat: Long, message: Long, fileId: Int)

    @Upsert suspend fun saveChannels(channels: List<TelegramChannel>)
    @Upsert suspend fun saveTracks(tracks: List<TelegramTrackEntity>)

    @Query("UPDATE telegram_tracks SET localThumbnailPath=:path WHERE accountId=:account AND thumbnailKey=:key")
    suspend fun thumbnailReady(account: Long, key: String, path: String)

    @Query("SELECT chatId, messageId FROM telegram_tracks WHERE accountId=:account")
    suspend fun keys(account: Long): List<MessageKey>

    @Query("UPDATE telegram_tracks SET localThumbnailPath=NULL WHERE accountId=:account")
    suspend fun clearArtwork(account: Long)

    @Query("DELETE FROM telegram_tracks WHERE accountId=:account")
    suspend fun deleteTracks(account: Long)
    @Query("DELETE FROM telegram_channels WHERE accountId=:account")
    suspend fun deleteChannels(account: Long)
    @Transaction suspend fun forget(account: Long) {
        deleteTracks(account)
        deleteChannels(account)
    }
}
data class MessageKey(val chatId: Long, val messageId: Long)

@Database(entities = [TelegramTrackEntity::class, TelegramChannel::class], version = 1, exportSchema = false)
abstract class TelegramStore : RoomDatabase() {
    abstract fun tracks(): TelegramDao
}

@Module
@InstallIn(SingletonComponent::class)
object TelegramStorageModule {
    @Provides @Singleton fun dao(@ApplicationContext context: Context): TelegramDao =
        Room.databaseBuilder(context, TelegramStore::class.java,
            File(context.noBackupFilesDir, "telegram-metadata.db").absolutePath).build().tracks()
}
