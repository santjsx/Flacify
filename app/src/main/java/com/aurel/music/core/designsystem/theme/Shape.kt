package com.aurel.music.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class AurelShapes(
    val micro: RoundedCornerShape = RoundedCornerShape(4.dp),
    val small: RoundedCornerShape = RoundedCornerShape(8.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(12.dp),
    val large: RoundedCornerShape = RoundedCornerShape(16.dp),
    val extraLarge: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp)
)

val DefaultAurelShapes = AurelShapes()

