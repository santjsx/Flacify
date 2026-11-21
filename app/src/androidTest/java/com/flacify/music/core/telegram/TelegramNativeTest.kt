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
