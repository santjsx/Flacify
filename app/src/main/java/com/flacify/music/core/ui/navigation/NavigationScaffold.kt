package com.flacify.music.core.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flacify.music.core.audio.equalizer.EqualizerController
import com.flacify.music.core.audio.timer.SleepTimerManager
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.rememberArtworkMood
import com.flacify.music.core.ui.component.UnifiedFloatingDock
import com.flacify.music.feature.equalizer.EqualizerSheet
import com.flacify.music.feature.favorites.FavoritesSheet
import com.flacify.music.feature.home.HomeScreen
import com.flacify.music.feature.home.HomeViewModel
import com.flacify.music.feature.library.LibraryScreen
import com.flacify.music.feature.library.LibraryTab
import com.flacify.music.feature.library.LibraryViewModel
import com.flacify.music.feature.more.MoreScreen
import com.flacify.music.feature.more.MoreViewModel
import com.flacify.music.feature.nowplaying.NowPlayingScreen
import com.flacify.music.feature.nowplaying.NowPlayingViewModel
import com.flacify.music.feature.playlists.PlaylistsSheet
import com.flacify.music.feature.search.SearchScreen
import com.flacify.music.feature.search.SearchViewModel
import com.flacify.music.feature.timer.SleepTimerSheet

enum class NavigationDestination(val label: String, val icon: () -> ImageVector) {
    HOME("Home", { FlacifyIcons.Home }),
    LIBRARY("Library", { FlacifyIcons.Library }),
    SEARCH("Search", { FlacifyIcons.Search }),
    MORE("More", { FlacifyIcons.More })
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

    val hasAnyOverlayOpen = isNowPlayingOpen || isMoreSleepTimerOpen || isMoreEqualizerOpen || isFavoritesOpen || isPlaylistsOpen

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Navigation Back Handling Hierarchy:
    // 1. Independent sheets dismiss first (order of priority)
    BackHandler(enabled = isMoreSleepTimerOpen) { isMoreSleepTimerOpen = false }
    BackHandler(enabled = isMoreEqualizerOpen) { isMoreEqualizerOpen = false }
    BackHandler(enabled = isFavoritesOpen) { isFavoritesOpen = false }
    BackHandler(enabled = isPlaylistsOpen) { isPlaylistsOpen = false }

    // 2. Note: isNowPlayingOpen back handling is owned by NowPlayingScreen itself,
    // so lyrics dismissal and haptic feedback run cleanly without competition.

    // 3. If on secondary tab (Library, Search, More) and no overlay is open, back navigates to previous tab / Home
    BackHandler(enabled = !hasAnyOverlayOpen && destinationStack.size > 1) {
        if (destinationStack.size > 1) {
            destinationStack.removeAt(destinationStack.lastIndex)
        }
    }

    // 4. On root Home tab with no overlay open, double-back confirmation within 2s protects against accidental app exits
    BackHandler(enabled = !hasAnyOverlayOpen && destinationStack.size <= 1) {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastBackPressTime < 2000L) {
            context.findActivity()?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context.applicationContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    val currentSong by playbackRepository.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playbackRepository.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by playbackRepository.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by playbackRepository.durationMs.collectAsStateWithLifecycle()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
    val moreViewModel: MoreViewModel = hiltViewModel()

    val miniPlayerArtworkMood = rememberArtworkMood(
        artworkUri = currentSong?.artworkUri,
        fallbackColor = FlacifyTheme.colors.background,
        isDarkTheme = FlacifyTheme.colors.isDark
    )

    Box(modifier = modifier.fillMaxSize().background(FlacifyTheme.colors.background)) {
        // Main Screen Viewport (Decoupled from dynamic dock height to eliminate 72dp layout shift)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing))
                },
                contentAlignment = Alignment.TopCenter,
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { destination ->
                when (destination) {
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
                        onPlaylistsClick = { isPlaylistsOpen = true },
                        hasActiveTrack = (currentSong != null)
                    )
                }
            }
        }

        // Unified Floating Dock pinned to bottom with zero layout shifts
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            UnifiedFloatingDock(
                currentSong = currentSong,
                isPlaying = isPlaying,
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    navigateTo(destination)
                },
                onPlayPauseClick = {
                    if (isPlaying) playbackRepository.pause() else playbackRepository.resume()
                },
                onSkipNextClick = { playbackRepository.skipToNext() },
                onMiniPlayerClick = { isNowPlayingOpen = true },
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                ambientColor = if (currentSong != null) miniPlayerArtworkMood.topColor else null
            )
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

/**
 * Recursively unwraps ContextWrapper until an Activity is found or returns null.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
