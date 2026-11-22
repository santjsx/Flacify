package com.flacify.music.feature.telegram

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.telegram.TelegramTrack
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.component.SongRow
import java.util.Locale

internal data class TelegramGroupKey(val title: String, val artist: String = "")
internal data class TelegramMusicGroup(
    val key: TelegramGroupKey,
    val title: String,
    val subtitle: String,
    val tracks: List<TelegramTrack>
)

internal fun telegramGroups(tracks: List<TelegramTrack>, albums: Boolean): List<TelegramMusicGroup> {
    fun normalized(value: String) = value.trim().lowercase(Locale.ROOT)
    return tracks.groupBy { track ->
        val meta = track.metadata
        if (albums) TelegramGroupKey(normalized(meta.album), normalized(meta.albumArtist.ifBlank { meta.artist }))
        else TelegramGroupKey(normalized(meta.artist))
    }.map { (key, songs) ->
        val meta = songs.first().metadata
        TelegramMusicGroup(key,
            if (albums) meta.album.ifBlank { "Unknown album" } else meta.artist.ifBlank { "Unknown artist" },
            if (albums) meta.albumArtist.ifBlank { meta.artist } else "",
            songs)
    }.sortedBy { it.title.lowercase(Locale.ROOT) }
}

@Composable
internal fun TelegramLibrary(vm: TelegramViewModel, account: Long, sources: @Composable () -> Unit) {
    val cached by vm.library.collectAsStateWithLifecycle()
    val tracks = remember(cached, account) { cached.filter { it.metadata.accountId == account } }
    val current by vm.playingSong.collectAsStateWithLifecycle()
    val playing by vm.playing.collectAsStateWithLifecycle()
    val tab by vm.libraryTab.collectAsStateWithLifecycle()
    TelegramLibraryContent(account, tracks, current, playing, tab, vm::selectLibraryTab, vm::playLibrary, sources)
}

@Composable
internal fun TelegramLibraryContent(
    account: Long,
    tracks: List<TelegramTrack>,
    current: Song?,
    playing: Boolean,
    tab: Int,
    onTabChange: (Int) -> Unit,
    onPlay: (TelegramTrack, List<TelegramTrack>) -> Unit,
    sources: @Composable () -> Unit
) {
    var group by remember(account, tab) { mutableStateOf<TelegramGroupKey?>(null) }
    val groups = remember(tracks, tab) { telegramGroups(tracks, albums = tab == 1) }
    val selected = groups.firstOrNull { it.key == group }
    val visible = if (group == null) tracks else selected?.tracks.orEmpty()
    BackHandler(enabled = group != null) { group = null }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            listOf("Songs", "Albums", "Artists", "Sources").forEachIndexed { index, label ->
                Tab(selected = tab == index, onClick = { onTabChange(index) },
                    text = { Text(label, maxLines = 1) })
            }
        }
        if (tab == 3) {
            sources()
        } else if (tracks.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your Telegram library is empty")
                Text("Open Sources and select a channel, group, or Saved Messages to sync music.")
                Button(onClick = { onTabChange(3) }) { Text("Choose a source") }
            }
        } else {
            if (group != null) {
                TextButton(onClick = { group = null }) { Text("Back to " + if (tab == 1) "albums" else "artists") }
                Text(selected?.title ?: "This collection has changed", modifier = Modifier.padding(horizontal = 16.dp))
            }
            if (tab == 1 && group == null && tracks.any { it.metadata.album.isBlank() }) {
                Text("Album tags are read when audio finishes downloading. Untagged songs appear under Unknown album.",
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (tab == 0 || group != null) {
                    items(visible, key = { it.playbackId }) { track ->
                        val song = remember(track) { track.toSong("") }
                        SongRow(song = song, isPlaying = playing && current?.mediaUri == song.mediaUri,
                            onClick = { onPlay(track, visible) })
                    }
                } else {
                    items(groups) { collection ->
                        Row(Modifier.fillMaxWidth().clickable { group = collection.key }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).clip(MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                                Icon(FlacifyIcons.MusicNote, contentDescription = null)
                                val cover = collection.tracks.firstOrNull { !it.metadata.localThumbnailPath.isNullOrBlank() }
                                if (cover != null) AsyncImage(cover.toSong("").artworkUri,
                                    contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(collection.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (collection.subtitle.isNotBlank()) Text(collection.subtitle,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                                Text("${collection.tracks.size} songs", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
