package com.flacify.music.feature.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.ui.component.SongRow
import com.flacify.music.core.ui.component.rememberFloatingDockBottomClearance

enum class SearchFilter(val title: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists")
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val bottomClearance = rememberFloatingDockBottomClearance()

    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    // Intercept system Back to clear active search query before exiting/navigating away
    BackHandler(enabled = query.isNotEmpty()) {
        viewModel.clearQuery()
    }

    val filteredResults = remember(results, selectedFilter, query) {
        if (selectedFilter == SearchFilter.ALL) results
        else {
            results.filter { song ->
                when (selectedFilter) {
                    SearchFilter.SONGS -> song.title.contains(query.trim(), ignoreCase = true)
                    SearchFilter.ALBUMS -> song.albumTitle.contains(query.trim(), ignoreCase = true)
                    SearchFilter.ARTISTS -> song.artistName.contains(query.trim(), ignoreCase = true)
                    SearchFilter.ALL -> true
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FlacifyTheme.colors.background)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FlacifyTheme.spacing.lg, top = FlacifyTheme.spacing.md, end = FlacifyTheme.spacing.lg, bottom = FlacifyTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Search",
                    style = FlacifyTheme.typography.appScreenTitle,
                    color = FlacifyTheme.colors.primaryText
                )
                Text(
                    text = if (query.isNotBlank()) "${filteredResults.size} Results" else "Find tracks, albums, artists",
                    style = FlacifyTheme.typography.albumMetadata,
                    color = FlacifyTheme.colors.secondaryText
                )
            }
        }

        // Minimal Search Input Field (PRD Section 10.1)
        TextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) },
            placeholder = {
                Text(
                    text = "Artists, songs, or albums",
                    style = FlacifyTheme.typography.bodyLarge,
                    color = FlacifyTheme.colors.secondaryText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = FlacifyIcons.Search,
                    contentDescription = null,
                    tint = FlacifyTheme.colors.secondaryText,
                    modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearQuery() }) {
                        Icon(
                            imageVector = FlacifyIcons.Close,
                            contentDescription = "Clear search",
                            tint = FlacifyTheme.colors.secondaryText,
                            modifier = Modifier.size(FlacifyTheme.dimensions.iconRegular)
                        )
                    }
                }
            },
            singleLine = true,
            shape = FlacifyTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FlacifyTheme.colors.surfaceVariant,
                unfocusedContainerColor = FlacifyTheme.colors.surfaceVariant,
                disabledContainerColor = FlacifyTheme.colors.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = FlacifyTheme.colors.primaryText,
                unfocusedTextColor = FlacifyTheme.colors.primaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FlacifyTheme.spacing.lg)
                .clip(FlacifyTheme.shapes.medium)
        )

        // Filter Chips (PRD Section 10.1 Filters: All, Songs, Albums, Artists)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FlacifyTheme.spacing.xs),
            contentPadding = PaddingValues(horizontal = FlacifyTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FlacifyTheme.spacing.xs)
        ) {
            items(SearchFilter.entries) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(FlacifyTheme.shapes.pill)
                        .background(if (isSelected) FlacifyTheme.colors.accent else FlacifyTheme.colors.surfaceVariant)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedFilter = filter
                        }
                        .padding(horizontal = FlacifyTheme.spacing.md, vertical = FlacifyTheme.spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.title,
                        style = if (isSelected) FlacifyTheme.typography.primaryButton else FlacifyTheme.typography.bodyMedium,
                        color = if (isSelected) FlacifyTheme.colors.primaryText else FlacifyTheme.colors.secondaryText
                    )
                }
            }
        }

        // Search Results List
        if (query.isNotBlank() && filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FlacifyTheme.spacing.huge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches found.",
                    style = FlacifyTheme.typography.bodyMedium,
                    color = FlacifyTheme.colors.secondaryText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomClearance)
            ) {
                items(filteredResults, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = currentSong?.id == song.id && isPlaying,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.playSong(song)
                        },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}
