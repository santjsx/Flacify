package com.flacify.music.core.telegram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.feature.telegram.TelegramLibraryContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TelegramLibraryUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun telegramTabsBrowseCollectionsAndPlayTheSelectedSong() {
        val first = TelegramTrack(TelegramTrackEntity(1, 10, 1, 1, "First song", "Test Artist",
            "one.mp3", 1000, 100, "audio/mpeg", null, 1, null, "Test Album"), -1)
        val second = first.copy(metadata = first.metadata.copy(messageId = 2, title = "Second song"), playbackId = -2)
        val tab = mutableIntStateOf(0)
        var chosen = 0L
        var queue = emptyList<TelegramTrack>()
        compose.setContent {
            FlacifyTheme {
                Box(Modifier.fillMaxSize()) {
                    TelegramLibraryContent(1, listOf(first, second), null, false, tab.intValue,
                        { tab.intValue = it }, { track, visible -> chosen = track.playbackId; queue = visible }) {
                        Text("Telegram source manager")
                    }
                }
            }
        }
        compose.onNodeWithText("Albums").performClick()
        compose.onNodeWithText("Test Album").assertIsDisplayed().performClick()
        compose.onNodeWithText("Second song").performClick()
        compose.runOnIdle {
            assertEquals(-2L, chosen)
            assertEquals(listOf(-1L, -2L), queue.map { it.playbackId })
        }
        compose.onNodeWithText("Artists").performClick()
        compose.onNodeWithText("Test Artist").assertIsDisplayed().performClick()
        compose.onNodeWithText("First song").assertIsDisplayed()
        compose.onNodeWithText("Sources").performClick()
        compose.onNodeWithText("Telegram source manager").assertIsDisplayed()
    }
}
