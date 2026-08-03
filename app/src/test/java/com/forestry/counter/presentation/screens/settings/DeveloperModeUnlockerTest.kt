package com.forestry.counter.presentation.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeveloperModeUnlockerTest {
    @Test
    fun should_activer_apres_huit_pressions_successives() {
        val unlocker = DeveloperModeUnlocker()

        repeat(7) { index ->
            val result = unlocker.registerTap(index * 100L, alreadyEnabled = false)
            assertEquals(7 - index, (result as DeveloperUnlockResult.Progress).remainingTaps)
        }

        assertTrue(
            unlocker.registerTap(700L, alreadyEnabled = false) is DeveloperUnlockResult.Enabled
        )
    }

    @Test
    fun should_reinitialiser_quand_les_pressions_sont_trop_espacees() {
        val unlocker = DeveloperModeUnlocker(resetAfterMillis = 1_000L)

        unlocker.registerTap(100L, alreadyEnabled = false)
        val result = unlocker.registerTap(1_500L, alreadyEnabled = false)

        assertEquals(7, (result as DeveloperUnlockResult.Progress).remainingTaps)
    }

    @Test
    fun should_ouvrir_directement_quand_le_mode_est_deja_actif() {
        val unlocker = DeveloperModeUnlocker()

        val result = unlocker.registerTap(0L, alreadyEnabled = true)

        assertTrue(result is DeveloperUnlockResult.Open)
    }
}
