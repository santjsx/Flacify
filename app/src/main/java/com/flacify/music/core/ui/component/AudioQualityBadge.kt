package com.flacify.music.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flacify.music.core.designsystem.icon.FlacifyIcons
import com.flacify.music.core.designsystem.theme.bounceClick
import com.flacify.music.core.model.AudioQuality

/**
 * Apple Music-style audiophile badge for LOSSLESS and HI-RES LOSSLESS playback.
 * Features a refined rounded pill container with hairline border, official Lucide audio-waveform icon,
 * and monospaced uppercase typography.
 */
@Composable
fun AudioQualityBadge(
    quality: AudioQuality,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compact: Boolean = false,
    showWaveformIcon: Boolean = !compact,
    onClick: (() -> Unit)? = null
) {
    val borderColor = if (isDark) {
        Color.White.copy(alpha = if (compact) 0.22f else 0.32f)
    } else {
        Color.Black.copy(alpha = if (compact) 0.16f else 0.22f)
    }

    val backgroundColor = if (isDark) {
        Color.White.copy(alpha = if (compact) 0.05f else 0.08f)
    } else {
        Color.Black.copy(alpha = if (compact) 0.04f else 0.06f)
    }

    val textColor = if (isDark) {
        Color.White.copy(alpha = if (compact) 0.85f else 0.92f)
    } else {
        Color.Black.copy(alpha = if (compact) 0.78f else 0.85f)
    }

    val shape = RoundedCornerShape(if (compact) 2.5.dp else 5.dp)

    val clickModifier = if (onClick != null) {
        Modifier.bounceClick(onClick = onClick)
    } else {
        Modifier
    }

    val containerModifier = if (compact) {
        modifier.height(14.dp)
    } else {
        modifier
    }

    Box(
        modifier = containerModifier
            .then(clickModifier)
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(if (compact) 0.5.dp else 0.75.dp, borderColor), shape)
            .padding(
                horizontal = if (compact) 3.5.dp else 6.5.dp,
                vertical = if (compact) 0.dp else 2.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showWaveformIcon) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = FlacifyIcons.AudioWaveform,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.5.dp))
                Text(
                    text = quality.badgeText,
                    color = textColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.1.sp,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = quality.badgeText,
                color = textColor,
                fontSize = if (compact) 7.5.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = if (compact) 0.5.sp else 1.1.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = if (compact) 8.sp else 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
