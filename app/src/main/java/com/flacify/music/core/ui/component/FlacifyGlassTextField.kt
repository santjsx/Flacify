package com.flacify.music.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.flacify.music.core.designsystem.theme.FlacifyTheme

/**
 * Premium glass-morphic input field with translucent surface,
 * accent focus highlights, and iOS-like corner radius.
 */
@Composable
fun FlacifyGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholderText,
                style = FlacifyTheme.typography.bodyMedium,
                color = FlacifyTheme.colors.tertiaryText
            )
        },
        textStyle = FlacifyTheme.typography.bodyLarge.copy(color = FlacifyTheme.colors.primaryText),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = FlacifyTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FlacifyTheme.colors.accent,
            unfocusedBorderColor = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
            focusedContainerColor = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f),
            unfocusedContainerColor = if (FlacifyTheme.colors.isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
            cursorColor = FlacifyTheme.colors.accent,
            focusedTextColor = FlacifyTheme.colors.primaryText,
            unfocusedTextColor = FlacifyTheme.colors.primaryText
        ),
        modifier = modifier.fillMaxWidth()
    )
}

