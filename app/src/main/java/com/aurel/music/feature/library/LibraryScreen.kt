package com.aurel.music.feature.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Album
import com.aurel.music.core.model.Artist
import com.aurel.music.core.model.Playlist
import com.aurel.music.core.model.Song
import com.aurel.music.core.model.SortOption
import com.aurel.music.core.ui.component.SongRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val albumCount by viewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by viewModel.artistCount.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playlistCount by viewModel.playlistCount.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistSongs by viewModel.selectedPlaylistSongs.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedAlbumSongs by viewModel.selectedAlbumSongs.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val selectedArtistSongs by viewModel.selectedArtistSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val pagedSongs = viewModel.pagedSongs.collectAsLazyPagingItems()

    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistSong by remember { mutableStateOf<Song?>(null) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    // Intercept system Back to dismiss any open sub-views or return to Songs tab
    BackHandler(enabled = selectedPlaylist != null || selectedAlbum != null || selectedArtist != null || selectedTab != LibraryTab.SONGS) {
        when {
            selectedPlaylist != null -> viewModel.selectPlaylist(null)
            selectedAlbum != null -> viewModel.selectAlbum(null)
            selectedArtist != null -> viewModel.selectArtist(null)
            selectedTab != LibraryTab.SONGS -> viewModel.selectTab(LibraryTab.SONGS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AurelTheme.colors.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    style = AurelTheme.typography.screenTitle,
                    color = AurelTheme.colors.primaryText
                )
                Text(
                    text = when (selectedTab) {
                        LibraryTab.PLAYLISTS -> "$playlistCount Playlists"
                        LibraryTab.SONGS -> "$songCount Songs"
                        LibraryTab.ALBUMS -> "$albumCount Albums"
                        LibraryTab.ARTISTS -> "$artistCount Artists"
                    },
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            }

            // Top action button (Add for Playlists, Sort for Songs)
            when (selectedTab) {
                LibraryTab.PLAYLISTS -> {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            newPlaylistName = ""
                            showNewPlaylistDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = AurelIcons.Add,
                            contentDescription = "New Playlist",
                            tint = AurelTheme.colors.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                LibraryTab.SONGS -> {
                    Box {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isSortMenuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector = AurelIcons.Sort,
                                contentDescription = "Sort library by ${sortOption.displayName}",
                                tint = AurelTheme.colors.primaryText
                            )
                        }

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            color = if (option == sortOption) AurelTheme.colors.accent else AurelTheme.colors.primaryText
                                        )
                                    },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectSortOption(option)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Spacer for albums and artists
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        // Segmented Control (Playlists, Songs, Albums, Artists)
        SegmentedTabBar(
            selectedTab = selectedTab,
            onTabSelected = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.selectTab(it)
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area by Tab
        when (selectedTab) {
            LibraryTab.PLAYLISTS -> {
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(AurelTheme.shapes.medium)
                                    .background(AurelTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AurelIcons.Queue,
                                    contentDescription = null,
                                    tint = AurelTheme.colors.accent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Playlists Yet",
                                style = AurelTheme.typography.sectionTitle,
                                color = AurelTheme.colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create your first playlist to organize your favorite music.",
                                style = AurelTheme.typography.secondaryContent,
                                color = AurelTheme.colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    newPlaylistName = ""
                                    showNewPlaylistDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                                shape = AurelTheme.shapes.small
                            ) {
                                Icon(imageVector = AurelIcons.Add, contentDescription = null, tint = AurelTheme.colors.primaryText)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "New Playlist", color = AurelTheme.colors.primaryText)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistRow(
                                playlist = playlist,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectPlaylist(playlist)
                                }
                            )
                        }
                    }
                }
            }

            LibraryTab.SONGS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Play All & Shuffle All Header (PRD Section 5.1)
                    if (pagedSongs.itemCount > 0) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val firstSong = pagedSongs[0]
                                        if (firstSong != null) {
                                            val currentItems = (0 until minOf(pagedSongs.itemCount, 100)).mapNotNull { pagedSongs[it] }
                                            viewModel.playQueue(currentItems, 0)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                                    shape = AurelTheme.shapes.small
                                ) {
                                    Icon(imageVector = AurelIcons.Play, contentDescription = null, tint = AurelTheme.colors.primaryText)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Play All", color = AurelTheme.colors.primaryText)
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val currentItems = (0 until minOf(pagedSongs.itemCount, 100)).mapNotNull { pagedSongs[it] }.shuffled()
                                        if (currentItems.isNotEmpty()) {
                                            viewModel.playQueue(currentItems, 0)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                                    shape = AurelTheme.shapes.small
                                ) {
                                    Icon(imageVector = AurelIcons.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
                                }
                            }
                        }
                    }

                    items(
                        count = pagedSongs.itemCount,
                        key = { index -> pagedSongs[index]?.id ?: index }
                    ) { index ->
                        val song = pagedSongs[index]
                        if (song != null) {
                            SongRow(
                                song = song,
                                isPlaying = currentSong?.id == song.id && isPlaying,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.playSong(song)
                                },
                                onMoreClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedSongForOptions = song
                                }
                            )
                        }
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectAlbum(album)
                            }
                        )
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(artists, key = { it.id }) { artist ->
                        ArtistRow(
                            artist = artist,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectArtist(artist)
                            }
                        )
                    }
                }
            }
        }
    }

    // Playlist Detail Modal Sheet
    if (selectedPlaylist != null) {
        val currentSelectedPlaylist = selectedPlaylist!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPlaylist(null) },
            containerColor = AurelTheme.colors.surfaceElevated,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PlaylistDetailContent(
                playlist = currentSelectedPlaylist,
                songs = selectedPlaylistSongs,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayAll = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedPlaylistSongs, 0)
                },
                onShuffle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedPlaylistSongs.shuffled(), 0)
                },
                onSongClick = { song ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val index = selectedPlaylistSongs.indexOfFirst { it.id == song.id }
                    viewModel.playQueue(selectedPlaylistSongs, if (index >= 0) index else 0)
                },
                onSongMoreClick = { song ->
                    selectedSongForOptions = song
                },
                onDeleteClick = {
                    showDeletePlaylistConfirm = true
                }
            )
        }
    }

    // Delete Playlist Confirmation Dialog
    if (showDeletePlaylistConfirm && selectedPlaylist != null) {
        val targetPlaylist = selectedPlaylist!!
        AlertDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            title = {
                Text(
                    text = "Delete Playlist",
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${targetPlaylist.name}\"? This action cannot be undone.",
                    style = AurelTheme.typography.primaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlaylist(targetPlaylist.id)
                        showDeletePlaylistConfirm = false
                    }
                ) {
                    Text("Delete", color = AurelTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlaylistConfirm = false }) {
                    Text("Cancel", color = AurelTheme.colors.primaryText)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
        )
    }

    // Album Detail Modal Sheet
    if (selectedAlbum != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectAlbum(null) },
            containerColor = AurelTheme.colors.surfaceElevated,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AlbumDetailContent(
                album = selectedAlbum!!,
                songs = selectedAlbumSongs,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayAll = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedAlbumSongs, 0)
                },
                onShuffle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedAlbumSongs.shuffled(), 0)
                },
                onSongClick = { song ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val index = selectedAlbumSongs.indexOfFirst { it.id == song.id }
                    viewModel.playQueue(selectedAlbumSongs, if (index >= 0) index else 0)
                },
                onSongMoreClick = { song ->
                    selectedSongForOptions = song
                }
            )
        }
    }

    // Artist Detail Modal Sheet
    if (selectedArtist != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectArtist(null) },
            containerColor = AurelTheme.colors.surfaceElevated,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ArtistDetailContent(
                artist = selectedArtist!!,
                songs = selectedArtistSongs,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayAll = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedArtistSongs, 0)
                },
                onShuffle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.playQueue(selectedArtistSongs.shuffled(), 0)
                },
                onSongClick = { song ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val index = selectedArtistSongs.indexOfFirst { it.id == song.id }
                    viewModel.playQueue(selectedArtistSongs, if (index >= 0) index else 0)
                },
                onSongMoreClick = { song ->
                    selectedSongForOptions = song
                }
            )
        }
    }

    // Song Options Bottom Sheet
    if (selectedSongForOptions != null) {
        val song = selectedSongForOptions!!
        ModalBottomSheet(
            onDismissRequest = { selectedSongForOptions = null },
            containerColor = AurelTheme.colors.surfaceElevated,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(AurelTheme.shapes.small)
                            .background(AurelTheme.colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "content://media/external/audio/albumart/${song.albumId}",
                            contentDescription = "${song.title} artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (song.albumId <= 0L) {
                            Icon(
                                imageVector = AurelIcons.MusicNote,
                                contentDescription = null,
                                tint = AurelTheme.colors.tertiaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = AurelTheme.typography.primaryContentBold,
                            color = AurelTheme.colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artistName} · ${song.albumTitle}",
                            style = AurelTheme.typography.secondaryContent,
                            color = AurelTheme.colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Next
                ActionSheetRow(
                    icon = AurelIcons.Play,
                    title = "Play Next",
                    onClick = {
                        viewModel.playNext(song)
                        selectedSongForOptions = null
                    }
                )

                // Favorite Toggle
                ActionSheetRow(
                    icon = if (song.isFavorite) AurelIcons.HeartFilled else AurelIcons.HeartOutline,
                    title = if (song.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    iconTint = if (song.isFavorite) AurelTheme.colors.accent else AurelTheme.colors.secondaryText,
                    onClick = {
                        viewModel.toggleFavorite(song)
                        selectedSongForOptions = null
                    }
                )

                // Add to Playlist
                ActionSheetRow(
                    icon = AurelIcons.PlaylistAdd,
                    title = "Add to Playlist...",
                    onClick = {
                        val currentSongTarget = song
                        selectedSongForOptions = null
                        showAddToPlaylistSong = currentSongTarget
                    }
                )

                // If currently viewing a playlist, show option to remove from this playlist
                if (selectedPlaylist != null) {
                    val currentPlaylistId = selectedPlaylist!!.id
                    ActionSheetRow(
                        icon = AurelIcons.Delete,
                        title = "Remove from Playlist",
                        iconTint = AurelTheme.colors.accent,
                        onClick = {
                            viewModel.removeSongFromPlaylist(currentPlaylistId, song.id)
                            selectedSongForOptions = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add To Playlist Sheet
    if (showAddToPlaylistSong != null) {
        val targetSong = showAddToPlaylistSong!!
        ModalBottomSheet(
            onDismissRequest = { showAddToPlaylistSong = null },
            containerColor = AurelTheme.colors.surfaceElevated,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Playlist",
                        style = AurelTheme.typography.sectionTitle,
                        color = AurelTheme.colors.primaryText
                    )
                    IconButton(onClick = { showAddToPlaylistSong = null }) {
                        Icon(imageVector = AurelIcons.Close, contentDescription = "Close", tint = AurelTheme.colors.secondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Create New Playlist action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AurelTheme.shapes.small)
                        .clickable {
                            showAddToPlaylistSong = null
                            newPlaylistName = ""
                            showNewPlaylistDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AurelIcons.Add,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "New Playlist...",
                        style = AurelTheme.typography.primaryContentBold,
                        color = AurelTheme.colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AurelTheme.shapes.small)
                                .clickable {
                                    viewModel.addSongToPlaylist(playlist.id, targetSong.id)
                                    showAddToPlaylistSong = null
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(AurelTheme.shapes.small)
                                    .background(AurelTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AurelIcons.Queue,
                                    contentDescription = null,
                                    tint = AurelTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = AurelTheme.typography.primaryContentBold,
                                    color = AurelTheme.colors.primaryText
                                )
                                Text(
                                    text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "song" else "songs"}",
                                    style = AurelTheme.typography.secondaryContent,
                                    color = AurelTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // New Playlist Dialog
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = {
                Text(
                    text = "New Playlist",
                    style = AurelTheme.typography.sectionTitle,
                    color = AurelTheme.colors.primaryText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a title for this playlist.",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Playlist Name", color = AurelTheme.colors.tertiaryText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName.trim())
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Create", color = AurelTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) {
                    Text("Cancel", color = AurelTheme.colors.secondaryText)
                }
            },
            containerColor = AurelTheme.colors.surfaceElevated
        )
    }
}

@Composable
private fun SegmentedTabBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AurelTheme.shapes.small)
            .background(AurelTheme.colors.surfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LibraryTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AurelTheme.shapes.small)
                    .background(if (isSelected) AurelTheme.colors.surfaceElevated else AurelTheme.colors.surfaceVariant)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    style = if (isSelected) AurelTheme.typography.primaryContentBold else AurelTheme.typography.primaryContent,
                    color = if (isSelected) AurelTheme.colors.primaryText else AurelTheme.colors.secondaryText
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(AurelTheme.shapes.small)
                .background(AurelTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AurelIcons.Queue,
                contentDescription = null,
                tint = AurelTheme.colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = AurelTheme.typography.primaryContentBold,
                color = AurelTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"}",
                style = AurelTheme.typography.secondaryContent,
                color = AurelTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = AurelIcons.ChevronRight,
            contentDescription = null,
            tint = AurelTheme.colors.tertiaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PlaylistDetailContent(
    playlist: Playlist,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onDeleteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(AurelTheme.shapes.medium)
                        .background(AurelTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AurelIcons.Queue,
                        contentDescription = null,
                        tint = AurelTheme.colors.accent,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = playlist.name,
                    style = AurelTheme.typography.screenTitle,
                    color = AurelTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} ${if (songs.size == 1) "track" else "tracks"}",
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Play & Shuffle Action Buttons
                if (songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPlayAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                            shape = AurelTheme.shapes.small
                        ) {
                            Icon(imageVector = AurelIcons.Play, contentDescription = null, tint = AurelTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play All", color = AurelTheme.colors.primaryText)
                        }

                        Button(
                            onClick = onShuffle,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                            shape = AurelTheme.shapes.small
                        ) {
                            Icon(imageVector = AurelIcons.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This playlist is empty.\nAdd tracks using song options.",
                        style = AurelTheme.typography.secondaryContent,
                        color = AurelTheme.colors.secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(songs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }

        // Delete Playlist action
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AurelTheme.shapes.small)
                    .clickable(onClick = onDeleteClick)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AurelIcons.Delete,
                    contentDescription = null,
                    tint = AurelTheme.colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Playlist",
                    style = AurelTheme.typography.primaryContentBold,
                    color = AurelTheme.colors.accent
                )
            }
        }
    }
}

@Composable
private fun ActionSheetRow(
    icon: ImageVector,
    title: String,
    iconTint: androidx.compose.ui.graphics.Color = AurelTheme.colors.accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AurelTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = AurelTheme.typography.primaryContent,
            color = AurelTheme.colors.primaryText
        )
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AurelTheme.shapes.small)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(AurelTheme.shapes.small)
                .background(AurelTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "content://media/external/audio/albumart/${album.id}",
                contentDescription = "${album.title} artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (album.id <= 0L) {
                Icon(
                    imageVector = AurelIcons.Album,
                    contentDescription = null,
                    tint = AurelTheme.colors.tertiaryText,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.title,
            style = AurelTheme.typography.primaryContentBold,
            color = AurelTheme.colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.artistName} · ${album.trackCount} tracks",
            style = AurelTheme.typography.secondaryContent,
            color = AurelTheme.colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArtistRow(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(AurelTheme.shapes.pill)
                .background(AurelTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AurelIcons.Artist,
                contentDescription = null,
                tint = AurelTheme.colors.tertiaryText,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = AurelTheme.typography.primaryContentBold,
                color = AurelTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.albumCount} albums · ${artist.trackCount} songs",
                style = AurelTheme.typography.secondaryContent,
                color = AurelTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = AurelIcons.ChevronRight,
            contentDescription = null,
            tint = AurelTheme.colors.tertiaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AlbumDetailContent(
    album: Album,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(AurelTheme.shapes.medium)
                        .background(AurelTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "content://media/external/audio/albumart/${album.id}",
                        contentDescription = "${album.title} artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = album.title,
                    style = AurelTheme.typography.screenTitle,
                    color = AurelTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artistName,
                    style = AurelTheme.typography.primaryContent,
                    color = AurelTheme.colors.secondaryText
                )
                Text(
                    text = "${songs.size} tracks${if (album.year > 0) " · ${album.year}" else ""}",
                    style = AurelTheme.typography.metadata,
                    color = AurelTheme.colors.tertiaryText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Play & Shuffle Action Buttons (PRD Section 5.3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = AurelIcons.Play, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Play All", color = AurelTheme.colors.primaryText)
                    }

                    Button(
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = AurelIcons.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                isPlaying = currentSong?.id == song.id && isPlaying,
                onClick = { onSongClick(song) },
                onMoreClick = { onSongMoreClick(song) }
            )
        }
    }
}

@Composable
private fun ArtistDetailContent(
    artist: Artist,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(AurelTheme.shapes.pill)
                        .background(AurelTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AurelIcons.Artist,
                        contentDescription = null,
                        tint = AurelTheme.colors.tertiaryText,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = artist.name,
                    style = AurelTheme.typography.screenTitle,
                    color = AurelTheme.colors.primaryText
                )
                Text(
                    text = "${artist.albumCount} Albums · ${songs.size} Songs",
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.accent),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = AurelIcons.Play, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Play All", color = AurelTheme.colors.primaryText)
                    }

                    Button(
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AurelTheme.colors.surfaceVariant),
                        shape = AurelTheme.shapes.small
                    ) {
                        Icon(imageVector = AurelIcons.Shuffle, contentDescription = null, tint = AurelTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Shuffle", color = AurelTheme.colors.primaryText)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                isPlaying = currentSong?.id == song.id && isPlaying,
                onClick = { onSongClick(song) },
                onMoreClick = { onSongMoreClick(song) }
            )
        }
    }
}
