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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Ultra-premium glassmorphic dialog with frosted gradient surface,
 * subtle top rim highlight, 24dp rounded corners, and scroll-safe content bounding.
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
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxDialogHeight = screenHeight * 0.75f

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = modifier
                .padding(horizontal = FlacifyTheme.spacing.lg)
                .widthIn(min = 280.dp, max = 380.dp)
                .heightIn(max = maxDialogHeight)
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
                    .heightIn(max = maxDialogHeight)
                    .padding(FlacifyTheme.spacing.xl)
            ) {
                if (title != null) {
                    title()
                    Spacer(modifier = Modifier.height(FlacifyTheme.spacing.sm))
                }

                if (text != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        text()
                    }
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
