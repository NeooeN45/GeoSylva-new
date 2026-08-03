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
}
