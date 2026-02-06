package com.forestry.counter.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppMiniDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    icon: ImageVector? = null,
    description: String? = null,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    confirmIsDestructive: Boolean = false,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val dismissAction = onDismiss ?: onDismissRequest

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon?.let { { Icon(it, contentDescription = null) } },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (animationsEnabled) Modifier.animateContentSize() else Modifier),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                content?.invoke(this)
            }
        },
        confirmButton = {
            val colors = if (confirmIsDestructive) {
                ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.textButtonColors()
            }
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = colors
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            if (!dismissText.isNullOrBlank()) {
                TextButton(onClick = dismissAction) {
                    Text(dismissText)
                }
            }
        }
    )
}
