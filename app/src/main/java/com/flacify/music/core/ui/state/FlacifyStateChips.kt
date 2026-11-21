package com.flacify.music.core.ui.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Reusable metadata chip for indicating item states (Draft, Archived, Pending, Published).
 */
@Composable
fun FlacifyStateChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = FlacifyTheme.colors.surfaceVariant,
    contentColor: Color = FlacifyTheme.colors.secondaryText
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = FlacifyTheme.typography.caption,
                color = contentColor
            )
        }
    }
}

/**
 * Draft State Chip (State 29):
 * Marks an uncommitted playlist or queue draft.
 */
@Composable
fun FlacifyDraftChip(modifier: Modifier = Modifier) {
    FlacifyStateChip(
        text = "Draft",
        containerColor = FlacifyTheme.colors.warning.copy(alpha = 0.15f),
        contentColor = FlacifyTheme.colors.warning,
        modifier = modifier
    )
}

/**
 * Published / Saved State Chip (State 30):
 * Marks a saved item committed to the library.
 */
@Composable
fun FlacifySavedChip(modifier: Modifier = Modifier) {
    FlacifyStateChip(
        text = "Saved",
        icon = FlacifyIcons.Check,
        containerColor = FlacifyTheme.colors.accent.copy(alpha = 0.15f),
        contentColor = FlacifyTheme.colors.accent,
        modifier = modifier
    )
}

/**
 * Archived / Hidden State Chip (State 31):
 * Marks an ignored or hidden track/folder.
 */
@Composable
fun FlacifyArchivedChip(modifier: Modifier = Modifier) {
    FlacifyStateChip(
        text = "Hidden",
        containerColor = FlacifyTheme.colors.surfaceElevated,
        contentColor = FlacifyTheme.colors.tertiaryText,
        modifier = modifier
    )
}

/**
 * Pending State Chip (State 33):
 * Marks an item queued for background processing.
 */
@Composable
fun FlacifyPendingChip(
    modifier: Modifier = Modifier,
    label: String = "Pending"
) {
    FlacifyStateChip(
        text = label,
        icon = FlacifyIcons.Refresh,
        containerColor = FlacifyTheme.colors.surfaceVariant,
        contentColor = FlacifyTheme.colors.secondaryText,
        modifier = modifier
    )
}
