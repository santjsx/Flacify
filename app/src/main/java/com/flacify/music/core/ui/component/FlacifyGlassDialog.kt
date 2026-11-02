package com.flacify.music.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Ultra-premium glassmorphic dialog with frosted gradient surface,
 * subtle top rim highlight, and 24dp rounded corners.
 */
@Composable
fun FlacifyGlassDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = modifier
                .padding(horizontal = FlacifyTheme.spacing.lg)
                .widthIn(min = 280.dp, max = 380.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FlacifyTheme.glass.dialogBackground)
                .border(
                    FlacifyTheme.glass.borderLight,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            // Subtle top rim highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FlacifyTheme.glass.topRimHighlight)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FlacifyTheme.spacing.xl)
            ) {
                if (title != null) {
                    title()
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))
                }

                if (text != null) {
                    text()
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.lg))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(FlacifyTheme.spacing.xs))
                    }
                    confirmButton()
                }
            }
        }
    }
}

