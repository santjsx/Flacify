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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.aurel.music.core.designsystem.icon.AurelIcons
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.Album
import com.aurel.music.core.model.Artist
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
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedAlbumSongs by viewModel.selectedAlbumSongs.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val selectedArtistSongs by viewModel.selectedArtistSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val pagedSongs = viewModel.pagedSongs.collectAsLazyPagingItems()

    var isSortMenuExpanded by remember { mutableStateOf(false) }

    // Intercept system Back when viewing Albums or Artists tab to return to Songs tab first
    BackHandler(enabled = selectedTab != LibraryTab.SONGS && selectedAlbum == null && selectedArtist == null) {
        viewModel.selectTab(LibraryTab.SONGS)
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
                        LibraryTab.SONGS -> "$songCount Songs"
                        LibraryTab.ALBUMS -> "$albumCount Albums"
                        LibraryTab.ARTISTS -> "$artistCount Artists"
                    },
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            }

            // Sort Selector Button (active on Songs tab)
            if (selectedTab == LibraryTab.SONGS) {
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
        }

        // Segmented Control (Songs, Albums, Artists)
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
                                onMoreClick = { /* Option menu */ }
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
                }
            )
        }
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
    onSongClick: (Song) -> Unit
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
                onMoreClick = {}
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
    onSongClick: (Song) -> Unit
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
                onMoreClick = {}
            )
        }
    }
}
