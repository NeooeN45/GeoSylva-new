package com.forestry.counter.presentation.screens.forestry

import com.forestry.counter.presentation.theme.Motion
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * Anime la caméra avec un easing (non linéaire), en réutilisant les durées
 * standard de l'app (`Motion.NORMAL`/`SLOW`) plutôt qu'une valeur arbitraire —
 * seul point de cohérence exposé par l'API caméra de MapLibre.
 */
internal fun MapboxMap.animateCameraSmooth(update: CameraUpdate, durationMs: Int = Motion.NORMAL) {
    easeCamera(update, durationMs, true, null)
}
