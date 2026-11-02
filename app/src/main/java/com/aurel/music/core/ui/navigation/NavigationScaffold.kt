package com.aurel.music.core.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurel.music.core.audio.equalizer.EqualizerController
import com.aurel.music.core.audio.timer.SleepTimerManager
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.designsystem.theme.AurelTheme
import com.aurel.music.core.ui.component.MiniPlayer
import com.aurel.music.feature.equalizer.EqualizerSheet
import com.aurel.music.feature.favorites.FavoritesSheet
import com.aurel.music.feature.home.HomeScreen
import com.aurel.music.feature.home.HomeViewModel
import com.aurel.music.feature.library.LibraryScreen
import com.aurel.music.feature.library.LibraryTab
import com.aurel.music.feature.library.LibraryViewModel
import com.aurel.music.feature.more.MoreScreen
import com.aurel.music.feature.more.MoreViewModel
import com.aurel.music.feature.nowplaying.NowPlayingScreen
import com.aurel.music.feature.nowplaying.NowPlayingViewModel
import com.aurel.music.feature.playlists.PlaylistsSheet
import com.aurel.music.feature.search.SearchScreen
import com.aurel.music.feature.search.SearchViewModel
import com.aurel.music.feature.timer.SleepTimerSheet

enum class NavigationDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SEARCH("Search", Icons.Default.Search),
    MORE("More", Icons.Default.MoreHoriz)
}

@Composable
fun NavigationScaffold(
    playbackRepository: PlaybackRepository,
    songRepository: SongRepository,
    playlistRepository: PlaylistRepository,
    sleepTimerManager: SleepTimerManager,
    equalizerController: EqualizerController,
    modifier: Modifier = Modifier
) {
    // Navigation Destination Stack: Maintains tab history with HOME as the persistent root
    val destinationStack = remember { mutableStateListOf(NavigationDestination.HOME) }
    val currentDestination = destinationStack.lastOrNull() ?: NavigationDestination.HOME

    fun navigateTo(destination: NavigationDestination) {
        if (destination == NavigationDestination.HOME) {
            destinationStack.clear()
            destinationStack.add(NavigationDestination.HOME)
        } else if (currentDestination != destination) {
            destinationStack.remove(destination)
            destinationStack.add(destination)
        }
    }

    var isNowPlayingOpen by remember { mutableStateOf(false) }
    var isMoreSleepTimerOpen by remember { mutableStateOf(false) }
    var isMoreEqualizerOpen by remember { mutableStateOf(false) }
    var isFavoritesOpen by remember { mutableStateOf(false) }
    var isPlaylistsOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Navigation Back Handling:
    // 1. If Now Playing is open, back gesture / 3-button back dismisses Now Playing
    BackHandler(enabled = isNowPlayingOpen) {
        isNowPlayingOpen = false
    }

    // 2. If on secondary tab (Library, Search, More), back gesture / 3-button back navigates to previous tab / Home
    BackHandler(enabled = !isNowPlayingOpen && destinationStack.size > 1) {
        destinationStack.removeAt(destinationStack.lastIndex)
    }

    // 3. On root Home tab, double-back confirmation within 2s protects against accidental app exits during gesture navigation
    BackHandler(enabled = !isNowPlayingOpen && destinationStack.size <= 1) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
    val moreViewModel: MoreViewModel = hiltViewModel()

    Box(modifier = modifier.fillMaxSize().background(AurelTheme.colors.background)) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                    // Using NavigationBarDefaults.windowInsets ensures correct drawing for both Gesture Nav and 3-Button Nav
                    NavigationBar(
                        containerColor = AurelTheme.colors.surface,
                        tonalElevation = 0.dp,
                        windowInsets = NavigationBarDefaults.windowInsets,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationDestination.entries.forEach { destination ->
                            val selected = destination == currentDestination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(destination) },
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
                        onNavigateToSearch = { navigateTo(NavigationDestination.SEARCH) },
                        onNavigateToSettings = { navigateTo(NavigationDestination.MORE) }
                    )
                    NavigationDestination.LIBRARY -> LibraryScreen(
                        viewModel = libraryViewModel
                    )
                    NavigationDestination.SEARCH -> SearchScreen(
                        viewModel = searchViewModel
                    )
                    NavigationDestination.MORE -> MoreScreen(
                        viewModel = moreViewModel,
                        onRescanClick = { homeViewModel.scanLibrary() },
                        onEqualizerClick = { isMoreEqualizerOpen = true },
                        onSleepTimerClick = { isMoreSleepTimerOpen = true },
                        onFavoritesClick = { isFavoritesOpen = true },
                        onPlaylistsClick = { isPlaylistsOpen = true }
                    )
                }
            }
        }

        // Full Screen Now Playing Modal Overlay with Fluid Apple-like Motion
        AnimatedVisibility(
            visible = isNowPlayingOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        ) {
            NowPlayingScreen(
                viewModel = nowPlayingViewModel,
                sleepTimerManager = sleepTimerManager,
                equalizerController = equalizerController,
                playlistRepository = playlistRepository,
                onDismiss = { isNowPlayingOpen = false },
                onGoToAlbum = { albumId ->
                    val album = libraryViewModel.albums.value.find { it.id == albumId }
                    if (album != null) {
                        libraryViewModel.selectTab(LibraryTab.ALBUMS)
                        libraryViewModel.selectAlbum(album)
                        navigateTo(NavigationDestination.LIBRARY)
                        isNowPlayingOpen = false
                    }
                },
                onGoToArtist = { artistId ->
                    val artist = libraryViewModel.artists.value.find { it.id == artistId }
                    if (artist != null) {
                        libraryViewModel.selectTab(LibraryTab.ARTISTS)
                        libraryViewModel.selectArtist(artist)
                        navigateTo(NavigationDestination.LIBRARY)
                        isNowPlayingOpen = false
                    }
                }
            )
        }

        // Sheets triggered from More Screen
        if (isMoreSleepTimerOpen) {
            SleepTimerSheet(
                timerManager = sleepTimerManager,
                onDismiss = { isMoreSleepTimerOpen = false }
            )
        }

        if (isMoreEqualizerOpen) {
            EqualizerSheet(
                equalizerController = equalizerController,
                onDismiss = { isMoreEqualizerOpen = false }
            )
        }

        // Favorites Sheet (PRD Section 11.2)
        if (isFavoritesOpen) {
            FavoritesSheet(
                songRepository = songRepository,
                playbackRepository = playbackRepository,
                onDismiss = { isFavoritesOpen = false }
            )
        }

        // Playlists Sheet (PRD Section 11.1)
        if (isPlaylistsOpen) {
            PlaylistsSheet(
                playlistRepository = playlistRepository,
                playbackRepository = playbackRepository,
                onDismiss = { isPlaylistsOpen = false }
            )
        }
    }
}
