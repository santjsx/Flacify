package com.flacify.music.feature.playlists

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassTextField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun NewPlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: suspend (String) -> Unit,
    onCreated: () -> Unit,
    addSong: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun submit() {
        if (saving) return
        val title = name.trim()
        if (title.isEmpty()) {
            error = "Enter a playlist name."
            return
        }
        saving = true
        scope.launch {
            try {
                onCreate(title)
                Toast.makeText(context, "Created playlist “$title”", Toast.LENGTH_SHORT).show()
                onCreated()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "Couldn’t save the playlist. Try again."
            } finally {
                saving = false
            }
        }
    }

    FlacifyGlassDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("New Playlist", style = FlacifyTheme.typography.heading2, color = FlacifyTheme.colors.primaryText) },
        text = {
            FlacifyGlassTextField(
                value = name,
                onValueChange = {
                    name = it
                    error = null
                },
                placeholderText = "Playlist name",
                errorMessage = error,
                enabled = !saving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() })
            )
        },
        confirmButton = {
            Button(onClick = { submit() }, enabled = !saving) {
                Text(if (saving) "Saving…" else if (addSong) "Create & Add" else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } }
    )
}
