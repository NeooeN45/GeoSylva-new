package com.forestry.counter.presentation.screens.forestry

/** Requête de style MapLibre identifiée pour ignorer les callbacks obsolètes. */
internal data class MapLayerLoadRequest(
    val id: Long,
    val layerIndex: Int,
)

/** Résultat d'une transition : nouvel état et éventuelle requête à démarrer. */
internal data class MapLayerLoadDecision(
    val state: MapLayerLoadState,
    val requestToStart: MapLayerLoadRequest? = null,
)

/**
 * Machine d'état pure du sélecteur de fonds de carte.
 *
 * Une seule transition MapLibre est exécutée à la fois. Si l'utilisateur
 * choisit plusieurs fonds pendant un chargement, seule sa dernière intention
 * est conservée puis démarrée dès la fin de la requête active.
 */
internal data class MapLayerLoadState(
    val activeIndex: Int,
    val loadingRequest: MapLayerLoadRequest? = null,
    val pendingIndex: Int? = null,
    val failedIndex: Int? = null,
    private val nextRequestId: Long = 1L,
) {
    val isLoading: Boolean get() = loadingRequest != null
    val displayedIndex: Int get() = pendingIndex ?: loadingRequest?.layerIndex ?: activeIndex

    fun request(layerIndex: Int): MapLayerLoadDecision {
        if (loadingRequest != null) {
            return MapLayerLoadDecision(
                copy(pendingIndex = layerIndex, failedIndex = null)
            )
        }
        if (layerIndex == activeIndex && failedIndex == null) {
            return MapLayerLoadDecision(this)
        }
        return start(layerIndex)
    }

    fun succeed(requestId: Long): MapLayerLoadDecision {
        val current = loadingRequest ?: return MapLayerLoadDecision(this)
        if (current.id != requestId) return MapLayerLoadDecision(this)

        val loaded = copy(
            activeIndex = current.layerIndex,
            loadingRequest = null,
            failedIndex = null,
        )
        val queued = pendingIndex
        return if (queued != null && queued != current.layerIndex) {
            loaded.copy(pendingIndex = null).start(queued)
        } else {
            MapLayerLoadDecision(loaded.copy(pendingIndex = null))
        }
    }

    fun fail(requestId: Long): MapLayerLoadDecision {
        val current = loadingRequest ?: return MapLayerLoadDecision(this)
        if (current.id != requestId) return MapLayerLoadDecision(this)

        val queued = pendingIndex
        return if (queued != null && queued != current.layerIndex) {
            copy(
                loadingRequest = null,
                pendingIndex = null,
                failedIndex = current.layerIndex,
            ).start(queued)
        } else {
            MapLayerLoadDecision(
                copy(
                    loadingRequest = null,
                    pendingIndex = null,
                    failedIndex = current.layerIndex,
                )
            )
        }
    }

    fun retry(): MapLayerLoadDecision {
        val failed = failedIndex ?: return MapLayerLoadDecision(this)
        if (loadingRequest != null) return MapLayerLoadDecision(this)
        return copy(failedIndex = null).start(failed)
    }

    private fun start(layerIndex: Int): MapLayerLoadDecision {
        val request = MapLayerLoadRequest(nextRequestId, layerIndex)
        return MapLayerLoadDecision(
            state = copy(
                loadingRequest = request,
                pendingIndex = null,
                failedIndex = null,
                nextRequestId = nextRequestId + 1,
            ),
            requestToStart = request,
        )
    }
}
