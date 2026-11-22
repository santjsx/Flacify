package com.flacify.music.core.telegram

import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TelegramNativeTest {
    @Test fun versionOneLibraryMigratesWithoutLosingTracks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = java.io.File(context.cacheDir, "telegram-migration-${System.nanoTime()}.db")
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("CREATE TABLE telegram_tracks (accountId INTEGER NOT NULL, chatId INTEGER NOT NULL, messageId INTEGER NOT NULL, fileId INTEGER NOT NULL, title TEXT NOT NULL, artist TEXT NOT NULL, fileName TEXT NOT NULL, durationMs INTEGER NOT NULL, fileSize INTEGER NOT NULL, mimeType TEXT NOT NULL, localThumbnailPath TEXT, addedDate INTEGER NOT NULL, thumbnailKey TEXT, PRIMARY KEY(accountId, chatId, messageId))")
            old.execSQL("CREATE TABLE telegram_channels (accountId INTEGER NOT NULL, chatId INTEGER NOT NULL, title TEXT NOT NULL, PRIMARY KEY(accountId, chatId))")
            old.execSQL("INSERT INTO telegram_tracks VALUES (1, 10, 20, 30, 'Song', 'Artist', 'song.flac', 1000, 100, 'audio/flac', NULL, 1, NULL)")
            old.version = 1
        }
        val store = Room.databaseBuilder(context, TelegramStore::class.java, file.absolutePath)
            .addMigrations(TelegramStore.MIGRATION_1_2).build()
        try {
            val dao = store.tracks()
            val original = dao.library(1).first().single()
            assertEquals("", original.metadata.album)
            dao.setTags(1, 10, 20, "Album", "Album Artist", "Artist")
            dao.saveTracks(listOf(original.metadata.copy(fileId = 40)))
            val updated = dao.library(1).first().single()
            assertEquals("Album", updated.metadata.album)
            assertEquals("Album Artist", updated.metadata.albumArtist)
            assertEquals(original.playbackId, updated.playbackId)
            assertTrue(dao.library(2).first().isEmpty())
        } finally {
            store.close()
            android.database.sqlite.SQLiteDatabase.deleteDatabase(file)
        }
    }

    @Test fun deviceCredentialsSurviveReopenAndCanBeCleared() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = java.nio.file.Files.createTempDirectory(target.cacheDir.toPath(), "credential-test").toFile()
        val context = object : ContextWrapper(target) {
            override fun getNoBackupFilesDir() = directory
        }
        val store = TelegramCredentialStore(context)
        val example = TelegramCredentials(12345, "a".repeat(32))
        try {
            store.save(example)
            val saved = TelegramCredentialStore(context).load()!!
            assertEquals(example.apiId, saved.apiId)
            assertEquals(example.apiHash, saved.apiHash)
        } finally {
            store.clear()
            directory.delete()
        }
        assertNull(store.load())
    }

    @Test fun cacheSurvivesFileIdChangesAndSeparatesAccountsAndChannels() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = Room.inMemoryDatabaseBuilder(context, TelegramStore::class.java).build()
        try {
            val dao = store.tracks()
            val track = TelegramTrackEntity(1, -100, 42, 12, "Song", "Artist", "song.mp3",
                60_000, 160_000, "audio/mpeg", null, 1, "cover")
            dao.saveTracks(listOf(track, track.copy(chatId = -200), track.copy(accountId = 2)))
            val original = dao.tracks(1, -100).first().single().playbackId
            dao.saveTracks(listOf(track.copy(fileId = 300)))
            val updated = dao.tracks(1, -100).first().single()
            assertEquals(original, updated.playbackId)
            assertEquals(300, updated.metadata.fileId)
            assertTrue(original != dao.tracks(1, -200).first().single().playbackId)
            dao.setArtwork(1, -100, 42, "/new-cover.jpg")
            assertEquals("/new-cover.jpg", dao.tracks(1, -100).first().single().metadata.localThumbnailPath)
            assertNull(dao.tracks(2, -100).first().single().metadata.localThumbnailPath)
            assertEquals(original, dao.tracks(1, -100).first().single().playbackId)
            dao.forget(1)
            assertTrue(dao.tracks(1, -100).first().isEmpty())
            assertEquals(1, dao.tracks(2, -100).first().size)
        } finally {
            store.close()
        }
    }

    @Test fun packagedNativeClientStartsWithoutSendingAccountCredentials() {
        val responses = LinkedBlockingQueue<TdApi.Object>()
        val client = Client.create({}, null, null)
        try {
            client.send(TdApi.GetAuthorizationState()) { responses.offer(it) }
            assertTrue(responses.poll(10, TimeUnit.SECONDS) is TdApi.AuthorizationStateWaitTdlibParameters)
        } finally {
            client.send(TdApi.Close(), null)
        }
    }
}
