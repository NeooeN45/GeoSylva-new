package com.forestry.counter.presentation.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountInputValidatorTest {

    @Test
    fun `verification code accepts normalized eight character value`() {
        assertTrue(AccountInputValidator.isValidActionCode("ABCD-EFGH"))
        assertTrue(AccountInputValidator.isValidActionCode("abcd efgh"))
        assertFalse(AccountInputValidator.isValidActionCode("1234"))
    }

    @Test
    fun `password reset requires matching strong values`() {
        assertTrue(
            AccountInputValidator.isValidPasswordReset(
                "mot-de-passe-vraiment-solide",
                "mot-de-passe-vraiment-solide",
            )
        )
        assertFalse(AccountInputValidator.isValidPasswordReset("trop-court", "trop-court"))
        assertFalse(
            AccountInputValidator.isValidPasswordReset(
                "mot-de-passe-vraiment-solide",
                "mot-de-passe-different",
            )
        )
    }
}
