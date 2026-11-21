package com.flacify.music.feature.library

import com.flacify.music.feature.playlists.AddToPlaylistSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.paging.LoadState
import com.flacify.music.core.ui.component.ConfirmRemovalDialog
import com.flacify.music.feature.playlists.NewPlaylistDialog
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.model.Album
import com.flacify.music.core.model.AlbumSortOption
import com.flacify.music.core.model.Artist
import com.flacify.music.core.model.Playlist
import com.flacify.music.core.model.Song
import com.flacify.music.core.model.SortOption
import com.flacify.music.core.ui.component.FlacifyGlassDropdownMenu
import com.flacify.music.core.ui.component.FlacifyGlassMenuItem
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.core.ui.component.rememberFloatingDockBottomClearance
import com.flacify.music.core.ui.state.FlacifyEmptyStateLayout
import com.flacify.music.core.ui.state.FlacifyErrorStateLayout
import com.flacify.music.core.ui.state.FlacifyStateLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onScanLibrary: () -> Unit,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val albumSortOption by viewModel.albumSortOption.collectAsStateWithLifecycle()
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
    val bottomClearance = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null)

    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isAlbumSortMenuExpanded by remember { mutableStateOf(false) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistSong by remember { mutableStateOf<Song?>(null) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    var songToRemove by remember(selectedPlaylist?.id) { mutableStateOf<Song?>(null) }

    // Intercept system Back to dismiss any open sub-views (album, artist, or playlist detail)
    BackHandler(enabled = selectedPlaylist != null || selectedAlbum != null || selectedArtist != null) {
        when {
            selectedPlaylist != null -> viewModel.selectPlaylist(null)
            selectedAlbum != null -> viewModel.selectAlbum(null)
            selectedArtist != null -> viewModel.selectArtist(null)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.md, end = FlacifyTheme.spacing.lg, bottom = FlacifyTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = when (selectedTab) {
                        LibraryTab.PLAYLISTS -> "$playlistCount Playlists"
                        LibraryTab.SONGS -> "$songCount Songs"
                        LibraryTab.ALBUMS -> "$albumCount Albums"
                        LibraryTab.ARTISTS -> "$artistCount Artists"
                    },
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
            }

            // Top action button (Add for Playlists, Sort for Songs)
            when (selectedTab) {
                LibraryTab.PLAYLISTS -> {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showNewPlaylistDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = FlacifyIcons.Add,
                            contentDescription = "New Playlist",
                            tint = FlacifyTheme.colors.accent,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
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
                                imageVector = FlacifyIcons.Sort,
                                contentDescription = "Sort library by ${sortOption.displayName}",
                                tint = FlacifyTheme.colors.primaryText,
                                modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                            )
                        }

                        FlacifyGlassDropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                FlacifyGlassMenuItem(
                                    text = option.displayName,
                                    selected = option == sortOption,
                                    onClick = {
                                        viewModel.selectSortOption(option)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                LibraryTab.ALBUMS -> {
                    Box {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAlbumSortMenuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector = FlacifyIcons.Sort,
                                contentDescription = "Sort albums by ${albumSortOption.displayName}",
                                tint = FlacifyTheme.colors.primaryText,
                                modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                            )
                        }

                        FlacifyGlassDropdownMenu(
                            expanded = isAlbumSortMenuExpanded,
                            onDismissRequest = { isAlbumSortMenuExpanded = false }
                        ) {
                            AlbumSortOption.entries.forEach { option ->
                                FlacifyGlassMenuItem(
                                    text = option.displayName,
                                    selected = option == albumSortOption,
                                    onClick = {
                                        viewModel.selectAlbumSortOption(option)
                                        isAlbumSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Spacer for artists
                    Spacer(modifier = Modifier.size(FlacifyTheme.dimensions.minTouchTarget))
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
            modifier = Modifier.padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.xxs)
        )

        Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xs))

        when (selectedTab) {
            LibraryTab.PLAYLISTS -> {
                if (playlists.isEmpty()) {
                    FlacifyEmptyStateLayout(
                        title = "No Playlists Yet",
                        explanation = "Create your first playlist to organize tracks into customized listening queues.",
                        icon = FlacifyIcons.Queue,
                        primaryActionLabel = "New Playlist",
                        onPrimaryAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showNewPlaylistDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomClearance)
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
                val refresh = pagedSongs.loadState.refresh
                if (pagedSongs.itemCount == 0) {
                    when {
                        isScanning || refresh is LoadState.Loading -> {
                            FlacifyStateLoader(text = if (isScanning) "Scanning local storage for music…" else "Loading library…")
                        }
                        refresh is LoadState.Error -> {
                            FlacifyErrorStateLayout(
                                title = "Couldn’t Load Library",
                                explanation = "We encountered an issue reading your local audio tracks. Tap below to retry.",
                                retryLabel = "Try Again",
                                onRetry = pagedSongs::retry
                            )
                        }
                        else -> {
                            FlacifyEmptyStateLayout(
                                title = "No Music In Library",
                                explanation = "Add audio files (FLAC, MP3, WAV) to your device storage, then scan for songs.",
                                icon = FlacifyIcons.MusicNote,
                                primaryActionLabel = "Scan Library",
                                onPrimaryAction = onScanLibrary
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Always Sticky Play All & Shuffle Buttons on top
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
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
                                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                                shape = FlacifyTheme.shapes.medium
                            ) {
                                Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                                Text(
                                    text = "Play All",
                                    color = FlacifyTheme.colors.primaryText,
                                    style = FlacifyTheme.typography.primaryButton
                                )
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
                                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                                shape = FlacifyTheme.shapes.medium
                            ) {
                                Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                                Text(
                                    text = "Shuffle",
                                    color = FlacifyTheme.colors.primaryText,
                                    style = FlacifyTheme.typography.primaryButton
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = bottomClearance)
                        ) {
                            items(
                                count = pagedSongs.itemCount,
                                key = pagedSongs.itemKey { it.id }
                            ) { index ->
                                val song = pagedSongs[index]
                                if (song != null) {
                                    SongRow(
                                        song = song,
                                        isPlaying = currentSong?.id == song.id && isPlaying,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.playSong(song, pagedSongs.itemSnapshotList.items)
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
                }
            }

            LibraryTab.ALBUMS -> {
                if (albums.isEmpty()) {
                    if (isScanning) {
                        FlacifyStateLoader(text = "Scanning library for albums…")
                    } else {
                        FlacifyEmptyStateLayout(
                            title = "No Albums Found",
                            explanation = "Albums will appear here once audio files with album metadata are added to your device.",
                            icon = FlacifyIcons.Album,
                            primaryActionLabel = "Scan Library",
                            onPrimaryAction = onScanLibrary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = FlacifyTheme.spacing.md,
                            end = FlacifyTheme.spacing.md,
                            bottom = bottomClearance
                        ),
                        horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.md)
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
            }

            LibraryTab.ARTISTS -> {
                if (artists.isEmpty()) {
                    if (isScanning) {
                        FlacifyStateLoader(text = "Scanning library for artists…")
                    } else {
                        FlacifyEmptyStateLayout(
                            title = "No Artists Found",
                            explanation = "Artists will appear here once audio files with artist metadata are added to your device.",
                            icon = FlacifyIcons.Artist,
                            primaryActionLabel = "Scan Library",
                            onPrimaryAction = onScanLibrary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomClearance)
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
    }

    // Playlist Detail Modal Sheet
    if (selectedPlaylist != null) {
        val currentSelectedPlaylist = selectedPlaylist!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPlaylist(null) },
            containerColor = FlacifyTheme.colors.surfaceElevated,
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
                    viewModel.playSong(song, selectedPlaylistSongs)
                },
                onSongMoreClick = { song ->
                    selectedSongForOptions = song
                },
                onBrowseSongs = {
                    viewModel.selectPlaylist(null)
                    viewModel.selectTab(LibraryTab.SONGS)
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
        ConfirmRemovalDialog(
            title = "Delete playlist?",
            message = "“${targetPlaylist.name}” will be deleted. Its songs stay in your library.",
            action = "Delete",
            onDismiss = { showDeletePlaylistConfirm = false },
            onConfirm = {
                viewModel.deletePlaylist(targetPlaylist.id)
                showDeletePlaylistConfirm = false
            }
        )
    }

    // Album Detail Modal Sheet
    if (selectedAlbum != null) {
        FlacifyGlassSheet(
            onDismissRequest = { viewModel.selectAlbum(null) }
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
                    viewModel.playSong(song, selectedAlbumSongs)
                },
                onSongMoreClick = { song ->
                    selectedSongForOptions = song
                }
            )
        }
    }

    // Artist Detail Modal Sheet
    if (selectedArtist != null) {
        FlacifyGlassSheet(
            onDismissRequest = { viewModel.selectArtist(null) }
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
                    viewModel.playSong(song, selectedArtistSongs)
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
        FlacifyGlassSheet(
            onDismissRequest = { selectedSongForOptions = null }
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
                            .clip(FlacifyTheme.shapes.small)
                            .background(FlacifyTheme.colors.surfaceVariant),
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
                                imageVector = FlacifyIcons.MusicNote,
                                contentDescription = null,
                                tint = FlacifyTheme.colors.tertiaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = FlacifyTheme.typography.primaryContentBold,
                            color = FlacifyTheme.colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artistName} · ${song.albumTitle}",
                            style = FlacifyTheme.typography.secondaryContent,
                            color = FlacifyTheme.colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Next
                ActionSheetRow(
                    icon = FlacifyIcons.Play,
                    title = "Play Next",
                    onClick = {
                        viewModel.playNext(song)
                        selectedSongForOptions = null
                    }
                )

                // Favorite Toggle
                ActionSheetRow(
                    icon = if (song.isFavorite) FlacifyIcons.HeartFilled else FlacifyIcons.HeartOutline,
                    title = if (song.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    iconTint = if (song.isFavorite) FlacifyTheme.colors.accent else FlacifyTheme.colors.secondaryText,
                    onClick = {
                        viewModel.toggleFavorite(song)
                        selectedSongForOptions = null
                    }
                )

                // Add to Playlist
                ActionSheetRow(
                    icon = FlacifyIcons.PlaylistAdd,
                    title = "Add to Playlist...",
                    onClick = {
                        val currentSongTarget = song
                        selectedSongForOptions = null
                        showAddToPlaylistSong = currentSongTarget
                    }
                )

                // If currently viewing a playlist, show option to remove from this playlist
                if (selectedPlaylist != null) {
                    ActionSheetRow(
                        icon = FlacifyIcons.Delete,
                        title = "Remove from Playlist",
                        iconTint = FlacifyTheme.colors.accent,
                        onClick = {
                            songToRemove = song
                            selectedSongForOptions = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    songToRemove?.let { song ->
        selectedPlaylist?.let { playlist ->
            ConfirmRemovalDialog(
                title = "Remove song?",
                message = "Remove “${song.title}” from “${playlist.name}”?",
                action = "Remove",
                onDismiss = { songToRemove = null },
                onConfirm = {
                    viewModel.removeSongFromPlaylist(playlist.id, song.id)
                    songToRemove = null
                }
            )
        }
    }

    showAddToPlaylistSong?.let { song ->
        AddToPlaylistSheet(
            song = song,
            playlists = playlists,
            onAdd = { viewModel.addSongToPlaylist(it, song.id) },
            onCreate = { viewModel.createPlaylist(it, song.id) },
            onDismiss = { showAddToPlaylistSong = null }
        )
    }

    if (showNewPlaylistDialog) {
        NewPlaylistDialog(
            onDismiss = { showNewPlaylistDialog = false },
            onCreate = { viewModel.createPlaylist(it) },
            onCreated = { showNewPlaylistDialog = false }
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
            .clip(FlacifyTheme.shapes.medium)
            .background(FlacifyTheme.colors.surfaceVariant)
            .padding(FlacifyTheme.spacing.xxs),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LibraryTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(FlacifyTheme.shapes.small)
                    .background(if (isSelected) FlacifyTheme.colors.surfaceElevated else FlacifyTheme.colors.surfaceVariant)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = FlacifyTheme.spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    style = if (isSelected) FlacifyTheme.typography.primaryButton else FlacifyTheme.typography.bodyMedium,
                    color = if (isSelected) FlacifyTheme.colors.primaryText else FlacifyTheme.colors.secondaryText
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
            .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FlacifyTheme.dimensions.songThumbnailSize)
                .clip(FlacifyTheme.shapes.medium)
                .background(FlacifyTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FlacifyIcons.Queue,
                contentDescription = null,
                tint = FlacifyTheme.colors.accent,
                modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
            )
        }
        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = FlacifyTheme.typography.playlistTitle,
                color = FlacifyTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"}",
                style = FlacifyTheme.typography.albumMetadata,
                color = FlacifyTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = FlacifyIcons.ChevronRight,
            contentDescription = null,
            tint = FlacifyTheme.colors.tertiaryText,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
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
    onBrowseSongs: () -> Unit,
    onDeleteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FlacifyTheme.spacing.lg),
        contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(FlacifyTheme.shapes.extraLarge)
                        .background(FlacifyTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Queue,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconHuge)
                    )
                }
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
                Text(
                    text = playlist.name,
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} ${if (songs.size == 1) "track" else "tracks"}",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

                // Play & Shuffle Action Buttons
                if (songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                    ) {
                        Button(
                            onClick = onPlayAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                            shape = FlacifyTheme.shapes.medium
                        ) {
                            Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                            Text(
                                text = "Play All",
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }

                        Button(
                            onClick = onShuffle,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                            shape = FlacifyTheme.shapes.medium
                        ) {
                            Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                            Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                            Text(
                                text = "Shuffle",
                                color = FlacifyTheme.colors.primaryText,
                                style = FlacifyTheme.typography.primaryButton
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = FlacifyTheme.spacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "This playlist is empty.\nAdd tracks using song options.",
                            style = FlacifyTheme.typography.bodyMedium,
                            color = FlacifyTheme.colors.secondaryText,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = onBrowseSongs) { Text("Browse songs") }
                    }
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
            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xl))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FlacifyTheme.shapes.small)
                    .clickable(onClick = onDeleteClick)
                    .padding(vertical = FlacifyTheme.spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FlacifyIcons.Delete,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.accent,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
                )
                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                Text(
                    text = "Delete Playlist",
                    style = FlacifyTheme.typography.primaryButton,
                    color = FlacifyTheme.colors.accent
                )
            }
        }
    }
}

@Composable
private fun ActionSheetRow(
    icon: ImageVector,
    title: String,
    iconTint: androidx.compose.ui.graphics.Color = FlacifyTheme.colors.accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FlacifyTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = FlacifyTheme.spacing.sm, horizontal = FlacifyTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
        )
        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
        Text(
            text = title,
            style = FlacifyTheme.typography.bodyLarge,
            color = FlacifyTheme.colors.primaryText
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
            .clip(FlacifyTheme.shapes.large)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(FlacifyTheme.shapes.large)
                .background(FlacifyTheme.colors.surfaceVariant),
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
                    imageVector = FlacifyIcons.Album,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.tertiaryText,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconHero)
                )
            }
        }
        Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))
        Text(
            text = album.title,
            style = FlacifyTheme.typography.albumTitle,
            color = FlacifyTheme.colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.artistName} · ${album.trackCount} tracks",
            style = FlacifyTheme.typography.artistName,
            color = FlacifyTheme.colors.secondaryText,
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
            .padding(horizontal = FlacifyTheme.spacing.lg, vertical = FlacifyTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FlacifyTheme.dimensions.artistAvatarSize)
                .clip(FlacifyTheme.shapes.circle)
                .background(FlacifyTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FlacifyIcons.Artist,
                contentDescription = null,
                tint = FlacifyTheme.colors.tertiaryText,
                modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
            )
        }
        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = FlacifyTheme.typography.songTitle,
                color = FlacifyTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.albumCount} albums · ${artist.trackCount} songs",
                style = FlacifyTheme.typography.albumMetadata,
                color = FlacifyTheme.colors.secondaryText
            )
        }
        Icon(
            imageVector = FlacifyIcons.ChevronRight,
            contentDescription = null,
            tint = FlacifyTheme.colors.tertiaryText,
            modifier = Modifier.size(FlacifyTheme.dimensions.iconMedium)
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
            .padding(horizontal = FlacifyTheme.spacing.lg),
        contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(FlacifyTheme.shapes.extraLarge)
                        .background(FlacifyTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "content://media/external/audio/albumart/${album.id}",
                        contentDescription = "${album.title} artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
                Text(
                    text = album.title,
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artistName,
                    style = FlacifyTheme.typography.artistName,
                    color = FlacifyTheme.colors.secondaryText
                )
                Text(
                    text = "${songs.size} tracks${if (album.year > 0) " · ${album.year}" else ""}",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.tertiaryText
                )
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

                // Play & Shuffle Action Buttons (PRD Section 5.3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                        Text(
                            text = "Play All",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }

                    Button(
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                        Text(
                            text = "Shuffle",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }
                }
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
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
            .padding(horizontal = FlacifyTheme.spacing.lg),
        contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(FlacifyTheme.shapes.circle)
                        .background(FlacifyTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Artist,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.tertiaryText,
                        modifier = Modifier.size(FlacifyTheme.dimensions.iconHero)
                    )
                }
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))
                Text(
                    text = artist.name,
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = "${artist.albumCount} Albums · ${songs.size} Songs",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.sm)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Play, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                        Text(
                            text = "Play All",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }

                    Button(
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.surfaceVariant),
                        shape = FlacifyTheme.shapes.medium
                    ) {
                        Icon(imageVector = FlacifyIcons.Shuffle, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                        Text(
                            text = "Shuffle",
                            color = FlacifyTheme.colors.primaryText,
                            style = FlacifyTheme.typography.primaryButton
                        )
                    }
                }
                Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
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
