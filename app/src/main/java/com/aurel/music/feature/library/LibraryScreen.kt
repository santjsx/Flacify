package com.aurel.music.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.model.SortOption
import com.aurel.music.core.ui.component.SongRow

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val pagedSongs = viewModel.pagedSongs.collectAsLazyPagingItems()

    var isSortMenuExpanded by remember { mutableStateOf(false) }

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
                    text = "$songCount Songs",
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            }

            // Sort Selector Button
            Box {
                IconButton(onClick = { isSortMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort library",
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
                                viewModel.selectSortOption(option)
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Segmented Control (Songs, Albums, Artists)
        SegmentedTabBar(
            selectedTab = selectedTab,
            onTabSelected = { viewModel.selectTab(it) },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Paged Songs List (Paging 3 backed by Room for 50,000+ tracks)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
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
                        onClick = { viewModel.playSong(song) },
                        onMoreClick = { /* Option menu */ }
                    )
                }
            }
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

