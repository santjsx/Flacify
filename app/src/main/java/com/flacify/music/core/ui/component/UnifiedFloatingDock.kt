package com.flacify.music.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.model.Song
import com.flacify.music.core.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

private data class DockColors(
    val containerGradient: List<Color>,
    val borderColor: Color,
    val highlightPill: Color,
    val activeIcon: Color,
    val inactiveIcon: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val dragHandle: Color,
    val isDark: Boolean,
    val accent: Color = Color(0xFFFA2D48),
)

@Composable
private fun rememberDockColors(ambient: Color? = null, isDark: Boolean = FlacifyTheme.colors.isDark): DockColors {
    val ambientTint = ambient?.copy(alpha = if (isDark) 0.16f else 0.08f)

    return if (isDark) {
        val baseTop = Color(0xF01A1A22)
        val baseBottom = Color(0xFA121218)
        DockColors(
            containerGradient = listOf(
                ambientTint?.let { blend(baseTop, it) } ?: baseTop,
                baseBottom
            ),
            borderColor = Color.White.copy(alpha = 0.12f),
            highlightPill = Color(0x33FFFFFF),
            activeIcon = Color(0xFFFA2D48),
            inactiveIcon = Color(0xFF888894),
            primaryText = Color(0xFFF5F5FA),
            secondaryText = Color(0xFF9A9AA6),
            dragHandle = Color.White.copy(alpha = 0.25f),
            isDark = true,
            accent = FlacifyTheme.colors.accent
        )
    } else {
        val baseTop = Color(0xF5FFFFFF)
        val baseBottom = Color(0xF8F0F0F6)
        DockColors(
            containerGradient = listOf(
                ambientTint?.let { blend(baseTop, it) } ?: baseTop,
                baseBottom
            ),
            borderColor = Color.Black.copy(alpha = 0.08f),
            highlightPill = Color(0xFFE4E4EC),
            activeIcon = Color(0xFFFA2D48),
            inactiveIcon = Color(0xFF7A7A86),
            primaryText = Color(0xFF121218),
            secondaryText = Color(0xFF6B6B76),
            dragHandle = Color.Black.copy(alpha = 0.18f),
            isDark = false,
            accent = FlacifyTheme.colors.accent
        )
    }
}

private fun blend(base: Color, tint: Color): Color = Color(
    red = base.red * (1f - tint.alpha) + tint.red * tint.alpha,
    green = base.green * (1f - tint.alpha) + tint.green * tint.alpha,
    blue = base.blue * (1f - tint.alpha) + tint.blue * tint.alpha,
    alpha = 1f
)

/**
 * Calculates the dynamic bottom clearance needed for scrollable lists so that
 * the bottom-most item is never hidden behind the floating capsule dock.
 *
 * Automatically accounts for 3-button navigation, gesture navigation, and display density.
 */
@Composable
fun rememberFloatingDockBottomClearance(hasActiveTrack: Boolean = true): Dp {
    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dockHeight = if (hasActiveTrack) 136.dp else 60.dp
    val verticalPadding = 16.dp
    val safetyBuffer = 20.dp
    return navBarsBottom + dockHeight + verticalPadding + safetyBuffer
}

/**
 * Unified Floating Dock:
 * Integrates the MiniPlayer and the Navigation Bar into a single, minimal, clean floating
 * frosted glass capsule component inspired by the reference design.
 *
 * Supports Light Mode and Dark Mode, tactile spring animations, sliding squircle tab indicator,
 * and seamless expansion when playback begins.
 */
@Composable
fun UnifiedFloatingDock(
    currentSong: Song?,
    isPlaying: Boolean,
    currentDestination: NavigationDestination,
    onDestinationSelected: (NavigationDestination) -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    ambientColor: Color? = null,
) {
    val colors = rememberDockColors(ambient = ambientColor)
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(shape)
                .background(Brush.verticalGradient(colors.containerGradient))
                .border(BorderStroke(1.dp, colors.borderColor), shape)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- Mini Player section (animates open when a song is playing) ----
            AnimatedVisibility(
                visible = currentSong != null,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    expandFrom = Alignment.Bottom
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    shrinkTowards = Alignment.Bottom
                ) + fadeOut()
            ) {
                if (currentSong != null) {
                    MiniPlayerSection(
                        song = currentSong,
                        isPlaying = isPlaying,
                        colors = colors,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onPlayPauseClick = onPlayPauseClick,
                        onSkipNextClick = onSkipNextClick,
                        onContainerClick = onMiniPlayerClick,
                    )
                }
            }

            // ---- Navigation Bar section with sliding squircle pill indicator ----
            NavigationSection(
                destinations = NavigationDestination.entries,
                selectedDestination = currentDestination,
                colors = colors,
                onDestinationSelected = onDestinationSelected,
            )
        }
    }
}

@Composable
private fun MiniPlayerSection(
    song: Song,
    isPlaying: Boolean,
    colors: DockColors,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onContainerClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContainerClick
            )
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.dragHandle)
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean & minimal 42dp Squircle Album Art with fallback
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (song.artworkUri.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.artworkUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album art for ${song.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = FlacifyIcons.MusicNote,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = song.title,
                        color = colors.primaryText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    song.audioQuality?.let { quality ->
                        AudioQualityBadge(
                            quality = quality,
                            isDark = colors.isDark,
                            compact = true
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = song.artistName,
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            TactileIconButton(
                onClick = onPlayPauseClick,
                tint = colors.primaryText,
            ) {
                Icon(
                    imageVector = if (isPlaying) FlacifyIcons.Pause else FlacifyIcons.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            TactileIconButton(
                onClick = onSkipNextClick,
                tint = colors.primaryText,
            ) {
                Icon(
                    imageVector = FlacifyIcons.SkipForward,
                    contentDescription = "Skip next",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2dp track progress indicator
        if (durationMs > 0L) {
            Spacer(Modifier.height(6.dp))
            val progress = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(colors.borderColor.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(colors.activeIcon)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun TactileIconButton(
    onClick: () -> Unit,
    tint: Color,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val target = if (pressed) 0.86f else 1f
    LaunchedEffect(pressed) {
        scale.animateTo(
            targetValue = target,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
        )
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch {
                        scale.animateTo(
                            targetValue = 0.86f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessHigh)
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint
        ) {
            content()
        }
    }
}

@Composable
private fun NavigationSection(
    destinations: List<NavigationDestination>,
    selectedDestination: NavigationDestination,
    colors: DockColors,
    onDestinationSelected: (NavigationDestination) -> Unit,
) {
    val selectedIndex = destinations.indexOf(selectedDestination).coerceAtLeast(0)
    val pillPosition = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        pillPosition.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .height(52.dp),
    ) {
        // Sliding soft squircle pill highlight behind the active tab
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / destinations.size
            val density = LocalDensity.current
            val tabWidthPx = with(density) { tabWidth.toPx() }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = pillPosition.value * tabWidthPx
                    }
                    .width(tabWidth)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.highlightPill)
            )

            // Tabs Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { destination ->
                    NavTabItem(
                        destination = destination,
                        selected = destination == selectedDestination,
                        colors = colors,
                        onClick = { onDestinationSelected(destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    destination: NavigationDestination,
    selected: Boolean,
    colors: DockColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed) {
        scale.animateTo(
            targetValue = if (pressed) 0.85f else 1f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
        )
    }

    val tint = if (selected) colors.activeIcon else colors.inactiveIcon

    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    if (!selected) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = destination.icon(),
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
