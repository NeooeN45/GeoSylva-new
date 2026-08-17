package com.forestry.counter.data.remote.identity

import com.forestry.counter.domain.model.IdentityProvider
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JwtSessionDecoderTest {
    @Test
    fun should_decoder_les_metadonnees_non_sensibles_quand_le_jeton_est_valide() {
        val token = token(
            """{"sub":"compte-123","exp":4102444800,"auth_provider":"google","roles":["user","founder"]}"""
        )

        val session = JwtSessionDecoder.decode(token)

        assertEquals("compte-123", session?.accountId)
        assertEquals(IdentityProvider.GOOGLE, session?.provider)
        assertEquals(listOf("user", "founder"), session?.roles)
        assertEquals(4_102_444_800L, session?.expiresAtEpochSeconds)
    }

    @Test
    fun should_refuser_quand_le_jeton_est_malforme() {
        assertNull(JwtSessionDecoder.decode("jeton-invalide"))
    }

    @Test
    fun should_marquer_inconnu_quand_le_fournisseur_n_est_pas_reconnu() {
        val token = token(
            """{"sub":"compte-456","exp":4102444800,"auth_provider":"futur","roles":[]}"""
        )

        val session = JwtSessionDecoder.decode(token)

        assertEquals(IdentityProvider.UNKNOWN, session?.provider)
    }

    private fun token(payload: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString(
            """{"alg":"RS256","typ":"JWT"}""".toByteArray(StandardCharsets.UTF_8)
        )
        val body = encoder.encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        return "$header.$body.signature"
    }
}
