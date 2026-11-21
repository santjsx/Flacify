package com.flacify.music.feature.telegram

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.telegram.*
import com.flacify.music.core.ui.component.*

@Composable
fun TelegramSheet(onDismiss: () -> Unit, vm: TelegramViewModel = hiltViewModel()) {
    val auth by vm.auth.collectAsStateWithLifecycle()
    val channel by vm.channel.collectAsStateWithLifecycle()
    val working by vm.working.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { if (auth == TelegramAuthState.Uninitialized) vm.connect() }
    val back = { if (channel == null) onDismiss() else vm.backToChannels() }
    FlacifyGlassSheet(onDismissRequest = { vm.backToChannels(); onDismiss() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.84f).imePadding().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(channel?.title ?: "Telegram music", modifier = Modifier.weight(1f), style = FlacifyTheme.typography.heading2,
                    color = FlacifyTheme.colors.primaryText)
                TextButton(onClick = back) { Text(if (channel != null) "Channels" else "Close") }
            }
            if (working || auth == TelegramAuthState.Connecting) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (status.isNotBlank()) Text(status, color = FlacifyTheme.colors.secondaryText, modifier = Modifier.padding(vertical = 6.dp))
            when (val state = auth) {
                TelegramAuthState.WaitAppCredentials -> TelegramAppSetup(working, vm::configure)
                is TelegramAuthState.Ready -> {
                    if (channel?.accountId == state.accountId) TelegramTracks(vm, channel!!, working)
                    else TelegramChannels(vm, state, working, { confirm = "cache" }, { confirm = "logout" })
                }
                TelegramAuthState.Connecting, TelegramAuthState.LoggingOut ->
                    Text(if (state == TelegramAuthState.LoggingOut) "Disconnecting…" else "Connecting to Telegram…")
                TelegramAuthState.Closed, TelegramAuthState.Uninitialized ->
                    Button(onClick = vm::connect, enabled = !working) { Text("Connect Telegram") }
                is TelegramAuthState.Notice -> {
                    Text(state.message)
                    TextButton(onClick = { confirm = "logout" }, enabled = !working) { Text("Reset connection") }
                }
                else -> key(state) { TelegramLogin(state, working, vm::submit) { confirm = "logout" } }
            }
        }
    }
    if (confirm.isNotEmpty()) {
        val logout = confirm == "logout"
        ConfirmRemovalDialog(
            title = if (logout) "Disconnect Telegram?" else "Clear Telegram media cache?",
            message = if (logout) "Remove this Telegram session, its saved app credentials, and its channel index from this device."
                else "Downloaded audio and artwork can be fetched again. Your channel index is kept.",
            action = if (logout) "Disconnect" else "Clear cache",
            onDismiss = { confirm = "" },
            onConfirm = { confirm = ""; if (logout) vm.logout() else vm.clearCache() }
        )
    }
}

@Composable
private fun TelegramAppSetup(busy: Boolean, save: (Int, String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var id by remember { mutableStateOf("") }
    var hash by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val number = id.trim().toIntOrNull()?.takeIf { it > 0 }
    val validHash = TelegramCredentials.hashPattern.matches(hash.trim())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Enter your own Telegram app ID and API hash. They are encrypted on this device.") }
        item { TextButton(onClick = { uriHandler.openUri("https://my.telegram.org/apps") }) { Text("Get Telegram app credentials") } }
        item {
            OutlinedTextField(id, { id = it }, label = { Text("API ID") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = submitted && number == null,
                supportingText = if (submitted && number == null) ({ Text("Enter a positive numeric API ID.") }) else null)
        }
        item {
            OutlinedTextField(hash, { hash = it }, label = { Text("API hash") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(), isError = submitted && !validHash,
                supportingText = if (submitted && !validHash) ({ Text("Enter the 32-character hexadecimal hash.") }) else null)
        }
        item {
            Button(onClick = {
                submitted = true
                if (number != null && validHash) save(number, hash.trim())
            }, enabled = !busy) { Text("Continue") }
        }
    }
}

@Composable
private fun TelegramLogin(state: TelegramAuthState, busy: Boolean, submit: (String) -> Unit, reset: () -> Unit) {
    var value by remember { mutableStateOf("") }
    val password = state is TelegramAuthState.WaitPassword
    val label = when (state) {
        TelegramAuthState.WaitPhoneNumber -> "Phone number with country code"
        is TelegramAuthState.WaitCode -> "Telegram code for ${state.codeInfo.phoneNumber}"
        is TelegramAuthState.WaitPassword -> "Two-step verification password"
        TelegramAuthState.WaitEmail -> "Email address"
        else -> "Email verification code"
    }
    val keyboard = when (state) {
        TelegramAuthState.WaitPhoneNumber -> KeyboardType.Phone
        TelegramAuthState.WaitEmail -> KeyboardType.Email
        is TelegramAuthState.WaitPassword -> KeyboardType.Password
        else -> KeyboardType.Number
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(value, { value = it }, label = { Text(label) }, enabled = !busy,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None)
        }
        if (state is TelegramAuthState.WaitPassword && state.hint.isNotBlank()) item { Text("Hint · ${state.hint}") }
        item { Button(onClick = { submit(value) }, enabled = !busy && value.isNotBlank()) { Text("Continue") } }
        item { TextButton(onClick = reset, enabled = !busy) { Text("Reset Telegram setup") } }
    }
}

@Composable
private fun TelegramChannels(vm: TelegramViewModel, identity: TelegramAuthState.Ready, busy: Boolean, clear: () -> Unit, logout: () -> Unit) {
    val cached by vm.channels.collectAsStateWithLifecycle()
    val channels = remember(cached, identity.accountId) { cached.filter { it.accountId == identity.accountId } }
    val size by vm.size.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Text("Connected as ${identity.displayName}") }
        item { OutlinedTextField(username, { username = it }, label = { Text("Public @channel or t.me link") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item {
            Row {
                Button(onClick = { vm.resolve(username) }, enabled = !busy && username.isNotBlank()) { Text("Open channel") }
                TextButton(onClick = vm::refreshChannels, enabled = !busy) { Text("Refresh channels") }
            }
        }
        items(channels, key = { it.chatId }) { channel ->
            TextButton(onClick = { vm.select(channel) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(channel.title) }
        }
        if (channels.isEmpty() && !busy) item { Text("Choose a public channel above or refresh your joined channels.") }
        item { HorizontalDivider() }
        item { Text(size?.let { "Media cache · ${Formatter.formatFileSize(context, it)}" } ?: "Media cache") }
        item {
            Row {
                TextButton(onClick = clear, enabled = !busy) { Text("Clear cache") }
                TextButton(onClick = logout, enabled = !busy) { Text("Disconnect") }
            }
        }
    }
}

@Composable
private fun TelegramTracks(vm: TelegramViewModel, channel: TelegramChannel, busy: Boolean) {
    val cached by vm.tracks.collectAsStateWithLifecycle()
    val tracks = remember(cached, channel) { cached.filter { it.metadata.chatId == channel.chatId && it.metadata.accountId == channel.accountId } }
    val current by vm.playingSong.collectAsStateWithLifecycle()
    val playing by vm.playing.collectAsStateWithLifecycle()
    val playbackState by vm.playbackState.collectAsStateWithLifecycle()
    val more by vm.hasMore.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val list = rememberLazyListState()
    val nearEnd by remember { derivedStateOf {
        val info = list.layoutInfo
        info.totalItemsCount > 0 && (info.visibleItemsInfo.lastOrNull()?.index ?: -1) >= info.totalItemsCount - 4
    } }
    LaunchedEffect(nearEnd, more, tracks.size, busy) {
        if (nearEnd && more && !busy && (status.isEmpty() || status == "Tracks refreshed")) vm.loadNext()
    }
    Row {
        TextButton(onClick = { vm.refresh() }, enabled = !busy) { Text("Refresh") }
        TextButton(onClick = { vm.refresh(true) }, enabled = !busy) { Text("Sync all metadata") }
    }
    if (current?.isTelegram == true && playbackState is com.flacify.music.core.model.PlaybackState.Error) {
        Text("Unable to stream this track. Check your connection and tap the track to retry.")
    }
    PullToRefreshBox(isRefreshing = busy, onRefresh = { vm.refresh() }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = list, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(tracks, key = { it.playbackId }) { track ->
                val song = remember(track, channel) { track.toSong(channel.title) }
                SongRow(song = song, isPlaying = playing && current?.id == song.id, onClick = { vm.play(track) })
            }
            if (tracks.isEmpty() && !busy) item { Text("No indexed audio yet. Refresh to check this channel.") }
            if (more && !busy) item { TextButton(onClick = vm::loadNext) { Text("Load more tracks") } }
        }
    }
}
