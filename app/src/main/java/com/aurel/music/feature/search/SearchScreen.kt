package com.aurel.music.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.ui.component.SongRow

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AurelTheme.colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search",
            style = AurelTheme.typography.screenTitle,
            color = AurelTheme.colors.primaryText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Minimal Search Input Field
        TextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) },
            placeholder = {
                Text(
                    text = "Artists, songs, or albums",
                    style = AurelTheme.typography.primaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AurelTheme.colors.secondaryText
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearQuery() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = AurelTheme.colors.secondaryText
                        )
                    }
                }
            },
            singleLine = true,
            shape = AurelTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AurelTheme.colors.surfaceVariant,
                unfocusedContainerColor = AurelTheme.colors.surfaceVariant,
                disabledContainerColor = AurelTheme.colors.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = AurelTheme.colors.primaryText,
                unfocusedTextColor = AurelTheme.colors.primaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(AurelTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results List
        if (query.isNotBlank() && results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results found for \"$query\"",
                    style = AurelTheme.typography.secondaryContent,
                    color = AurelTheme.colors.secondaryText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(results, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = currentSong?.id == song.id && isPlaying,
                        onClick = { viewModel.playSong(song) },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}

