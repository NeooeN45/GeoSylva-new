package com.forestry.counter.data.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class ParcelSyncPolicyTest {
    @Test
    fun `un conflit ne doit jamais etre relance automatiquement`() {
        assertEquals(SyncFailureAction.CONFLICT, classifySyncHttpFailure(409))
    }

    @Test
    fun `les erreurs transitoires doivent etre relancees`() {
        listOf(408, 425, 429, 500, 503).forEach { code ->
            assertEquals(SyncFailureAction.RETRY, classifySyncHttpFailure(code))
        }
    }

    @Test
    fun `une session expiree doit etre rafraichie une seule fois`() {
        assertEquals(SyncFailureAction.REFRESH_SESSION, classifySyncHttpFailure(401))
        assertEquals(SyncFailureAction.PERMANENT_ERROR, classifySyncHttpFailure(403))
    }

    // --- decideMergeOutcome (GEOSYLVA P0-3, pull et resolution) ---

    @Test
    fun `une modification locale non synchronisee gagne toujours sur le pull`() {
        // Vrai quel que soit l'etat serveur (tombstone, nouvelle, existante) :
        // isLocalDirty court-circuite toute autre consideration.
        listOf(
            Triple(false, false, false),
            Triple(false, true, false),
            Triple(true, true, false),
            Triple(true, true, true),
        ).forEach { (isTombstone, existsLocally, isAlreadyDeletedLocally) ->
            assertEquals(
                MergeOutcome.SKIPPED_LOCAL_DIRTY,
                decideMergeOutcome(
                    isLocalDirty = true,
                    isTombstone = isTombstone,
                    existsLocally = existsLocally,
                    isAlreadyDeletedLocally = isAlreadyDeletedLocally,
                ),
            )
        }
    }

    @Test
    fun `une parcelle serveur inconnue en local est inseree`() {
        assertEquals(
            MergeOutcome.INSERTED,
            decideMergeOutcome(
                isLocalDirty = false,
                isTombstone = false,
                existsLocally = false,
                isAlreadyDeletedLocally = false,
            ),
        )
    }

    @Test
    fun `une parcelle serveur deja connue et non modifiee localement est mise a jour`() {
        assertEquals(
            MergeOutcome.UPDATED,
            decideMergeOutcome(
                isLocalDirty = false,
                isTombstone = false,
                existsLocally = true,
                isAlreadyDeletedLocally = false,
            ),
        )
    }

    @Test
    fun `un tombstone serveur supprime une parcelle locale encore active`() {
        assertEquals(
            MergeOutcome.DELETED,
            decideMergeOutcome(
                isLocalDirty = false,
                isTombstone = true,
                existsLocally = true,
                isAlreadyDeletedLocally = false,
            ),
        )
    }

    @Test
    fun `un tombstone serveur est un noop si absente ou deja supprimee en local`() {
        assertEquals(
            MergeOutcome.NOOP,
            decideMergeOutcome(
                isLocalDirty = false,
                isTombstone = true,
                existsLocally = false,
                isAlreadyDeletedLocally = false,
            ),
        )
        assertEquals(
            MergeOutcome.NOOP,
            decideMergeOutcome(
                isLocalDirty = false,
                isTombstone = true,
                existsLocally = true,
                isAlreadyDeletedLocally = true,
            ),
        )
    }
}
