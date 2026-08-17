package com.forestry.counter.presentation.coachmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Registre borné au scaffold courant des positions des cibles de la visite.
 * Chaque propriétaire possède un jeton : la destruction tardive d'une
 * ancienne composition ne peut pas supprimer l'ancre qui l'a remplacée.
 */
class CoachMarkAnchorRegistry {
    private data class Registration(val owner: Any, val rect: Rect)

    private val anchors = mutableStateMapOf<String, Registration>()

    fun register(key: String, owner: Any, rect: Rect) {
        anchors[key] = Registration(owner, rect)
    }

    fun unregister(key: String, owner: Any) {
        if (anchors[key]?.owner === owner) anchors.remove(key)
    }

    fun rectFor(key: String): Rect? = anchors[key]?.rect
}

@Composable
fun rememberCoachMarkAnchorRegistry(): CoachMarkAnchorRegistry =
    remember { CoachMarkAnchorRegistry() }

/** Enregistre puis désenregistre automatiquement les coordonnées de la cible. */
fun Modifier.coachMarkAnchor(
    registry: CoachMarkAnchorRegistry,
    key: String,
): Modifier = composed {
    val owner = remember { Any() }
    DisposableEffect(registry, key, owner) {
        onDispose { registry.unregister(key, owner) }
    }
    onGloballyPositioned { coordinates ->
        registry.register(key, owner, coordinates.boundsInRoot())
    }
}
