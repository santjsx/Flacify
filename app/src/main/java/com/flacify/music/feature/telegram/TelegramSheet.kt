package com.flacify.music.feature.telegram

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.telegram.*
import com.flacify.music.core.ui.component.*
import com.flacify.music.core.ui.state.FlacifyEmptyStateLayout
import com.flacify.music.core.ui.state.FlacifyStateBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramSheet(onDismiss: () -> Unit, vm: TelegramViewModel = hiltViewModel()) {
    val auth by vm.auth.collectAsStateWithLifecycle()
    val channel by vm.channel.collectAsStateWithLifecycle()
    val working by vm.working.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (auth == TelegramAuthState.Uninitialized) vm.connect()
    }

    val back = {
        if (channel == null) onDismiss() else vm.backToChannels()
    }

    FlacifyGlassSheet(
        onDismissRequest = {
            vm.backToChannels()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (channel != null || auth is TelegramAuthState.Ready) Modifier.fillMaxHeight(0.85f)
                    else Modifier.wrapContentHeight()
                )
                .imePadding()
                .padding(horizontal = FlacifyTheme.spacing.md)
        ) {
            // Modern Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = FlacifyTheme.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    if (channel != null) {
                        IconButton(
                            onClick = back,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(FlacifyTheme.shapes.circle)
                                .background(FlacifyTheme.colors.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = FlacifyIcons.ChevronDown,
                                contentDescription = "Back to Channels",
                                tint = FlacifyTheme.colors.primaryText,
                                modifier = Modifier.rotate(90f)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = channel?.title ?: "Telegram Music",
                            style = FlacifyTheme.typography.heading2,
                            color = FlacifyTheme.colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (channel != null) "Stream lossless audio directly"
                            else if (auth is TelegramAuthState.Ready) (auth as TelegramAuthState.Ready).displayName
                            else "Log in with your phone number",
                            style = FlacifyTheme.typography.caption,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(FlacifyTheme.shapes.circle)
                        .background(FlacifyTheme.colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Close,
                        contentDescription = "Close",
                        tint = FlacifyTheme.colors.secondaryText
                    )
                }
            }

            if (working || auth == TelegramAuthState.Connecting) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = FlacifyTheme.spacing.xs),
                    color = FlacifyTheme.colors.accent,
                    trackColor = FlacifyTheme.colors.surfaceVariant
                )
            }

            if (status.isNotBlank()) {
                Text(
                    text = status,
                    style = FlacifyTheme.typography.caption,
                    color = if (status.contains("failed", true) || status.contains("error", true))
                        FlacifyTheme.colors.error
                    else
                        FlacifyTheme.colors.secondaryText,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            when (val state = auth) {
                TelegramAuthState.WaitAppCredentials -> {
                    // Fallback only if credentials were never built or wiped
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Initializing Telegram...",
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.secondaryText
                        )
                        Button(
                            onClick = vm::connect,
                            shape = FlacifyTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            modifier = Modifier.fillMaxWidth().bounceClick()
                        ) {
                            Text("Connect", style = FlacifyTheme.typography.button)
                        }
                    }
                }
                is TelegramAuthState.Ready -> {
                    if (channel?.accountId == state.accountId) {
                        TelegramTracks(vm, channel!!, working)
                    } else {
                        TelegramChannels(vm, state, working, { confirm = "cache" }, { confirm = "logout" })
                    }
                }
                TelegramAuthState.Connecting, TelegramAuthState.LoggingOut -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state == TelegramAuthState.LoggingOut) "Disconnecting from Telegram…" else "Connecting to Telegram…",
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.secondaryText
                        )
                    }
                }
                TelegramAuthState.Closed, TelegramAuthState.Uninitialized -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = vm::connect,
                            enabled = !working,
                            shape = FlacifyTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            modifier = Modifier.fillMaxWidth().height(50.dp).bounceClick()
                        ) {
                            Text("Connect Telegram", style = FlacifyTheme.typography.button)
                        }
                    }
                }
                is TelegramAuthState.Notice -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.primaryText
                        )
                        Button(
                            onClick = { confirm = "logout" },
                            enabled = !working,
                            shape = FlacifyTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            modifier = Modifier.fillMaxWidth().bounceClick()
                        ) {
                            Text("Reset Connection", style = FlacifyTheme.typography.button)
                        }
                    }
                }
                else -> {
                    key(state) {
                        TelegramLogin(state, working, vm::submit) { confirm = "logout" }
                    }
                }
            }
        }
    }

    if (confirm.isNotEmpty()) {
        val logout = confirm == "logout"
        ConfirmRemovalDialog(
            title = if (logout) "Disconnect Telegram?" else "Clear Telegram media cache?",
            message = if (logout)
                "Remove this Telegram session and its channel index from this device."
            else
                "Downloaded audio and artwork can be fetched again. Your channel index is kept.",
            action = if (logout) "Disconnect" else "Clear cache",
            onDismiss = { confirm = "" },
            onConfirm = {
                confirm = ""
                if (logout) vm.logout() else vm.clearCache()
            }
        )
    }
}

@Composable
private fun TelegramLogin(
    state: TelegramAuthState,
    busy: Boolean,
    submit: (String) -> Unit,
    reset: () -> Unit
) {
    var value by remember { mutableStateOf("") }
    val isPhone = state == TelegramAuthState.WaitPhoneNumber
    val isCode = state is TelegramAuthState.WaitCode
    val isPassword = state is TelegramAuthState.WaitPassword

    val title = when {
        isPhone -> "Phone Number"
        isCode -> "Verification Code"
        isPassword -> "Two-Step Verification"
        state == TelegramAuthState.WaitEmail -> "Email Address"
        else -> "Email Code"
    }
    val subtitle = when {
        isPhone -> "Enter your phone number with country code (e.g. +1... or +91...) to receive your Telegram login code."
        isCode -> "Enter the login code sent to your Telegram app for ${(state as TelegramAuthState.WaitCode).codeInfo.phoneNumber}."
        isPassword -> "Enter your 2FA password to finish logging in."
        else -> "Enter your verification code."
    }
    val placeholder = when {
        isPhone -> "+1 234 567 8900"
        isCode -> "Enter code"
        isPassword -> "Password"
        else -> "Code"
    }
    val keyboardType = when {
        isPhone -> KeyboardType.Phone
        isPassword -> KeyboardType.Password
        isCode -> KeyboardType.Number
        state == TelegramAuthState.WaitEmail -> KeyboardType.Email
        else -> KeyboardType.Number
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FlacifyTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = FlacifyTheme.typography.heading2,
                color = FlacifyTheme.colors.primaryText
            )
            Text(
                text = subtitle,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.secondaryText
            )
        }

        FlacifyGlassTextField(
            value = value,
            onValueChange = { value = it },
            placeholderText = placeholder,
            singleLine = true,
            enabled = !busy,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (value.isNotBlank() && !busy) submit(value) }
            )
        )

        if (isPassword && (state as TelegramAuthState.WaitPassword).hint.isNotBlank()) {
            Text(
                text = "Password hint: ${state.hint}",
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.tertiaryText
            )
        }

        Button(
            onClick = { submit(value) },
            enabled = !busy && value.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .bounceClick(),
            shape = FlacifyTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = FlacifyTheme.colors.accent,
                contentColor = Color.White,
                disabledContainerColor = FlacifyTheme.colors.accent.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isPhone) "Send Code" else "Continue",
                    style = FlacifyTheme.typography.button
                )
            }
        }

        if (!isPhone) {
            TextButton(
                onClick = reset,
                enabled = !busy,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.secondaryText)
            ) {
                Text(
                    text = "Change phone number",
                    style = FlacifyTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TelegramChannels(
    vm: TelegramViewModel,
    identity: TelegramAuthState.Ready,
    busy: Boolean,
    clear: () -> Unit,
    logout: () -> Unit
) {
    val cached by vm.channels.collectAsStateWithLifecycle()
    val channels = remember(cached, identity.accountId) { cached.filter { it.accountId == identity.accountId } }
    val size by vm.size.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Public Channel Search Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.medium)
                    .background(FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                    .padding(FlacifyTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
            ) {
                Text(
                    text = "Open Public Music Channel",
                    style = FlacifyTheme.typography.songTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = "Enter any public @channel or t.me/... link to explore and stream its music.",
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.secondaryText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FlacifyGlassTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholderText = "@channel or t.me/link",
                            singleLine = true,
                            enabled = !busy,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = { if (username.isNotBlank() && !busy) vm.resolve(username) }
                            )
                        )
                    }
                    Button(
                        onClick = { vm.resolve(username) },
                        enabled = !busy && username.isNotBlank(),
                        shape = FlacifyTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                        modifier = Modifier.height(52.dp).bounceClick()
                    ) {
                        Text("Open", style = FlacifyTheme.typography.button)
                    }
                }
            }
        }

        // Channel list header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR CHANNELS",
                    style = FlacifyTheme.typography.caption.copy(letterSpacing = 1.2.sp),
                    color = FlacifyTheme.colors.secondaryText
                )
                TextButton(
                    onClick = vm::refreshChannels,
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.accent)
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh", style = FlacifyTheme.typography.caption)
                }
            }
        }

        if (channels.isEmpty() && !busy) {
            item {
                FlacifyEmptyStateLayout(
                    title = "No Channels Indexed Yet",
                    explanation = "Enter a public music channel username above or refresh your joined channels list.",
                    icon = FlacifyIcons.Library,
                    primaryActionLabel = "Refresh Channels",
                    onPrimaryAction = vm::refreshChannels,
                    modifier = Modifier.padding(vertical = FlacifyTheme.spacing.lg)
                )
            }
        } else {
            items(channels, key = { it.chatId }) { channel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.surfaceVariant.copy(alpha = 0.35f))
                        .border(FlacifyTheme.glass.borderSubtle, shape = FlacifyTheme.shapes.medium)
                        .clickable(enabled = !busy) { vm.select(channel) }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(FlacifyTheme.shapes.circle)
                            .background(FlacifyTheme.colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.MusicNote,
                            contentDescription = null,
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = channel.title,
                        style = FlacifyTheme.typography.bodyLarge,
                        color = FlacifyTheme.colors.primaryText,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = FlacifyIcons.ChevronRight,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.tertiaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Settings & Cache Footer
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = FlacifyTheme.colors.divider)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = size?.let { "Media cache · ${Formatter.formatFileSize(context, it)}" } ?: "Media cache",
                    style = FlacifyTheme.typography.caption,
                    color = FlacifyTheme.colors.secondaryText
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = clear,
                        enabled = !busy,
                        colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.secondaryText)
                    ) {
                        Text("Clear cache", style = FlacifyTheme.typography.caption)
                    }
                    TextButton(
                        onClick = logout,
                        enabled = !busy,
                        colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.error)
                    ) {
                        Text("Disconnect", style = FlacifyTheme.typography.caption)
                    }
                }
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${tracks.size} tracks indexed",
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.secondaryText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { vm.refresh() },
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.accent)
                ) {
                    Text("Refresh", style = FlacifyTheme.typography.caption)
                }
                TextButton(
                    onClick = { vm.refresh(true) },
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.secondaryText)
                ) {
                    Text("Sync All", style = FlacifyTheme.typography.caption)
                }
            }
        }

        if (current?.isTelegram == true && playbackState is com.flacify.music.core.model.PlaybackState.Error) {
            Text(
                text = "Unable to stream this track. Check your connection and tap to retry.",
                style = FlacifyTheme.typography.caption,
                color = FlacifyTheme.colors.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        PullToRefreshBox(
            isRefreshing = busy,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(state = list, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
                items(tracks, key = { it.playbackId }) { track ->
                    val song = remember(track, channel) { track.toSong(channel.title) }
                    SongRow(
                        song = song,
                        isPlaying = playing && current?.id == song.id,
                        onClick = { vm.play(track) }
                    )
                }
                if (tracks.isEmpty() && !busy) {
                    item {
                        FlacifyEmptyStateLayout(
                            title = "No Audio Tracks Found",
                            explanation = "This channel doesn't have any audio files yet, or indexing is still in progress.",
                            icon = FlacifyIcons.MusicNote,
                            primaryActionLabel = "Refresh Channel",
                            onPrimaryAction = { vm.refresh() },
                            modifier = Modifier.padding(vertical = FlacifyTheme.spacing.lg)
                        )
                    }
                }
                if (more && !busy) {
                    item {
                        TextButton(
                            onClick = vm::loadNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = FlacifyTheme.colors.accent)
                        ) {
                            Text("Load more tracks", style = FlacifyTheme.typography.button)
                        }
                    }
                }
            }
        }
    }
}
