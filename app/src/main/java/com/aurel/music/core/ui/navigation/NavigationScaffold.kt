package com.aurel.music.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.ui.component.MiniPlayer
import com.aurel.music.feature.home.HomeScreen
import com.aurel.music.feature.home.HomeViewModel
import com.aurel.music.feature.library.LibraryScreen
import com.aurel.music.feature.library.LibraryViewModel
import com.aurel.music.feature.more.MoreScreen
import com.aurel.music.feature.nowplaying.NowPlayingScreen
import com.aurel.music.feature.nowplaying.NowPlayingViewModel
import com.aurel.music.feature.search.SearchScreen
import com.aurel.music.feature.search.SearchViewModel

enum class NavigationDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SEARCH("Search", Icons.Default.Search),
    MORE("More", Icons.Default.MoreHoriz)
}

@Composable
fun NavigationScaffold(
    playbackRepository: PlaybackRepository,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    var isNowPlayingOpen by remember { mutableStateOf(false) }

    val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()

    Box(modifier = modifier.fillMaxSize().background(AurelTheme.colors.background)) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // MiniPlayer floats directly above the Navigation Bar
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onPlayPauseClick = {
                            if (isPlaying) playbackRepository.pause() else playbackRepository.resume()
                        },
                        onSkipNextClick = { playbackRepository.skipToNext() },
                        onClick = { isNowPlayingOpen = true }
                    )

                    // Apple-inspired compact 4-item navigation bar
                    NavigationBar(
                        containerColor = AurelTheme.colors.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationDestination.entries.forEach { destination ->
                            val selected = destination == currentDestination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = AurelTheme.typography.metadata
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AurelTheme.colors.accent,
                                    selectedTextColor = AurelTheme.colors.accent,
                                    unselectedIconColor = AurelTheme.colors.secondaryText,
                                    unselectedTextColor = AurelTheme.colors.secondaryText,
                                    indicatorColor = AurelTheme.colors.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
            ) {
                when (currentDestination) {
                    NavigationDestination.HOME -> HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToSearch = { currentDestination = NavigationDestination.SEARCH }
                    )
                    NavigationDestination.LIBRARY -> LibraryScreen(
                        viewModel = libraryViewModel
                    )
                    NavigationDestination.SEARCH -> SearchScreen(
                        viewModel = searchViewModel
                    )
                    NavigationDestination.MORE -> MoreScreen(
                        onRescanClick = { homeViewModel.scanLibrary() }
                    )
                }
            }
        }

        // Full Screen Now Playing Modal Overlay
        AnimatedVisibility(
            visible = isNowPlayingOpen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                viewModel = nowPlayingViewModel,
                onDismiss = { isNowPlayingOpen = false }
            )
        }
    }
}
