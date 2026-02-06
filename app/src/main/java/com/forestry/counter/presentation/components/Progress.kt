package com.forestry.counter.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.forestry.counter.presentation.utils.ColorUtils

@Composable
fun AppLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    baseBg: Color? = null
) {
    val bg = baseBg ?: MaterialTheme.colorScheme.surface
    val fg = ColorUtils.getContrastingTextColor(bg)
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = fg.copy(alpha = 0.9f),
        trackColor = fg.copy(alpha = 0.2f)
    )
}

@Composable
fun AppCircularProgress(
    modifier: Modifier = Modifier,
    baseBg: Color? = null
) {
    val color = baseBg?.let { ColorUtils.getContrastingTextColor(it) } ?: MaterialTheme.colorScheme.primary
    CircularProgressIndicator(
        modifier = modifier,
        color = color
    )
}
