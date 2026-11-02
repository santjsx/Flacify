package com.flacify.music.feature.library

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
import coil.compose.AsyncImage
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.Album
import com.flacify.music.core.model.AlbumSortOption
import com.flacify.music.core.model.Artist
import com.flacify.music.core.model.Playlist
import com.flacify.music.core.model.Song
import com.flacify.music.core.model.SortOption
import com.flacify.music.core.ui.component.FlacifyGlassDialog
import com.flacify.music.core.ui.component.FlacifyGlassDropdownMenu
import com.flacify.music.core.ui.component.FlacifyGlassMenuItem
import com.flacify.music.core.ui.component.FlacifyGlassSheet
import com.flacify.music.core.ui.component.FlacifyGlassTextField
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.core.ui.component.rememberFloatingDockBottomClearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
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
                            newPlaylistName = ""
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

        // Content Area by Tab
        when (selectedTab) {
            LibraryTab.PLAYLISTS -> {
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = FlacifyTheme.spacing.xxl, vertical = FlacifyTheme.spacing.huge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(FlacifyTheme.dimensions.playButtonHeroSize)
                                    .clip(FlacifyTheme.shapes.medium)
                                    .background(FlacifyTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.Queue,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.accent,
                                    modifier = Modifier.size(FlacifyTheme.dimensions.iconLarge)
                                )
                            }
                            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.md))
                            Text(
                                text = "No Playlists Yet",
                                style = FlacifyTheme.typography.sectionHeading,
                                color = FlacifyTheme.colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.xxs))
                            Text(
                                text = "Create your first playlist to organize your favorite music.",
                                style = FlacifyTheme.typography.bodyMedium,
                                color = FlacifyTheme.colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    newPlaylistName = ""
                                    showNewPlaylistDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FlacifyTheme.colors.accent),
                                shape = FlacifyTheme.shapes.medium
                            ) {
                                Icon(imageVector = FlacifyIcons.Add, contentDescription = null, tint = FlacifyTheme.colors.primaryText)
                                Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xxs))
                                Text(
                                    text = "New Playlist",
                                    color = FlacifyTheme.colors.primaryText,
                                    style = FlacifyTheme.typography.primaryButton
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Always Sticky Play All & Shuffle Buttons on top
                    if (pagedSongs.itemCount > 0) {
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
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
                    ) {

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
                                    val currentItems = (0 until minOf(pagedSongs.itemCount, 100)).mapNotNull { pagedSongs[it] }
                                    viewModel.playQueue(currentItems, index)
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

            LibraryTab.ALBUMS -> {
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

            LibraryTab.ARTISTS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = rememberFloatingDockBottomClearance(hasActiveTrack = currentSong != null))
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
        FlacifyGlassDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            title = {
                Text(
                    text = "Delete Playlist",
                    style = FlacifyTheme.typography.heading2,
                    color = FlacifyTheme.colors.primaryText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${targetPlaylist.name}\"? This action cannot be undone.",
                    style = FlacifyTheme.typography.bodyMedium,
                    color = FlacifyTheme.colors.secondaryText
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(FlacifyTheme.colors.error)
                        .bounceClick {
                            viewModel.deletePlaylist(targetPlaylist.id)
                            showDeletePlaylistConfirm = false
                        }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Delete", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeletePlaylistConfirm = false },
                    modifier = Modifier.bounceClick()
                ) {
                    Text("Cancel", color = FlacifyTheme.colors.primaryText)
                }
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
                    val currentPlaylistId = selectedPlaylist!!.id
                    ActionSheetRow(
                        icon = FlacifyIcons.Delete,
                        title = "Remove from Playlist",
                        iconTint = FlacifyTheme.colors.accent,
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
            containerColor = FlacifyTheme.colors.surfaceElevated,
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
                        style = FlacifyTheme.typography.sectionTitle,
                        color = FlacifyTheme.colors.primaryText
                    )
                    IconButton(onClick = { showAddToPlaylistSong = null }) {
                        Icon(imageVector = FlacifyIcons.Close, contentDescription = "Close", tint = FlacifyTheme.colors.secondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Create New Playlist action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FlacifyTheme.shapes.small)
                        .clickable {
                            showAddToPlaylistSong = null
                            newPlaylistName = ""
                            showNewPlaylistDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FlacifyIcons.Add,
                        contentDescription = null,
                        tint = FlacifyTheme.colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "New Playlist...",
                        style = FlacifyTheme.typography.primaryContentBold,
                        color = FlacifyTheme.colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FlacifyTheme.shapes.small)
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
                                    .clip(FlacifyTheme.shapes.small)
                                    .background(FlacifyTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FlacifyIcons.Queue,
                                    contentDescription = null,
                                    tint = FlacifyTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = FlacifyTheme.typography.primaryContentBold,
                                    color = FlacifyTheme.colors.primaryText
                                )
                                Text(
                                    text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "song" else "songs"}",
                                    style = FlacifyTheme.typography.secondaryContent,
                                    color = FlacifyTheme.colors.secondaryText
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
        FlacifyGlassDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = {
                Text(
                    text = "New Playlist",
                    style = FlacifyTheme.typography.heading2,
                    color = FlacifyTheme.colors.primaryText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a title for this playlist.",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlacifyGlassTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholderText = "Playlist Name",
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.medium)
                        .background(if (newPlaylistName.isNotBlank()) FlacifyTheme.colors.accent else FlacifyTheme.colors.accent.copy(alpha = 0.4f))
                        .bounceClick {
                            if (newPlaylistName.isNotBlank()) {
                                viewModel.createPlaylist(newPlaylistName.trim())
                                showNewPlaylistDialog = false
                                newPlaylistName = ""
                            }
                        }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = 10.dp)
                ) {
                    Text("Create", color = androidx.compose.ui.graphics.Color.White, style = FlacifyTheme.typography.primaryButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNewPlaylistDialog = false },
                    modifier = Modifier.bounceClick()
                ) {
                    Text("Cancel", color = FlacifyTheme.colors.primaryText)
                }
            }
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
                    Text(
                        text = "This playlist is empty.\nAdd tracks using song options.",
                        style = FlacifyTheme.typography.bodyMedium,
                        color = FlacifyTheme.colors.secondaryText,
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
