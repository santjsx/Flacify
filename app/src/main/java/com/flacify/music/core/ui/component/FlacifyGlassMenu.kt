package com.flacify.music.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.FlacifyTheme
import com.flacify.music.core.designsystem.theme.bounceClick

/**
 * Premium frosted-glass dropdown menu matching FLACify's unified glassmorphic design system.
 * Features 16dp rounded corners, adaptive translucent backdrop, hairline gradient border,
 * and high-elevation shadow.
 */
@Composable
fun FlacifyGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = FlacifyTheme.colors.isDark
    val containerColor = if (isDark) Color(0xF2181822) else Color(0xF8FFFFFF)
    val shape = RoundedCornerShape(16.dp)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .widthIn(min = 180.dp, max = 240.dp)
            .clip(shape)
            .border(FlacifyTheme.glass.borderLight, shape),
        offset = offset,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        border = null, // Border handled on the clipped modifier to guarantee crisp corners
        properties = properties,
        content = content
    )
}

/**
 * An individual item in an [FlacifyGlassDropdownMenu] with checkmark selection indicator,
 * refined typography, subtle tint highlight, and tactile spring interaction.
 */
@Composable
fun FlacifyGlassMenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val haptic = LocalHapticFeedback.current
    val isDark = FlacifyTheme.colors.isDark
    val selectedBg = if (selected) {
        FlacifyTheme.colors.accent.copy(alpha = if (isDark) 0.12f else 0.08f)
    } else {
        Color.Transparent
    }

    val itemShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(itemShape)
            .background(selectedBg)
            .bounceClick(scaleDown = 0.97f) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (selected) FlacifyTheme.colors.accent else FlacifyTheme.colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = FlacifyTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) FlacifyTheme.colors.accent else FlacifyTheme.colors.primaryText,
                    maxLines = 1
                )
            }

            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = FlacifyIcons.Check,
                    contentDescription = "Selected",
                    tint = FlacifyTheme.colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

