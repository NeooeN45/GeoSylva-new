package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * Boutons de zoom, dézoom et réorientation au nord.
 */
@Composable
internal fun MapZoomControls(
    mapLibreMap: MapboxMap?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MapToolButton(
            onClick = {
                val map = mapLibreMap ?: return@MapToolButton
                map.animateCamera(CameraUpdateFactory.zoomIn(), 200)
            },
            icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.map_zoom_in), modifier = Modifier.size(20.dp)) }
        )
        MapToolButton(
            onClick = {
                val map = mapLibreMap ?: return@MapToolButton
                map.animateCamera(CameraUpdateFactory.zoomOut(), 200)
            },
            icon = { Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.map_zoom_out), modifier = Modifier.size(20.dp)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MapToolButton(
            onClick = {
                val map = mapLibreMap ?: return@MapToolButton
                map.animateCamera(CameraUpdateFactory.bearingTo(0.0), 400)
            },
            icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.map_north), modifier = Modifier.size(20.dp)) }
        )
    }
}

/**
 * Bouton outil carte (petit, rond, semi-transparent).
 */
@Composable
internal fun MapToolButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 3.dp,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
