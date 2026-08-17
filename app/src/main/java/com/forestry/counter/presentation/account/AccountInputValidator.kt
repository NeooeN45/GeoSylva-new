package com.forestry.counter.presentation.account

/** Validations pures partagées par les écrans sensibles du compte. */
object AccountInputValidator {
    fun isValidActionCode(value: String): Boolean =
        value.replace("-", "").replace(" ", "").length == 8 &&
            value.replace("-", "").replace(" ", "").all(Char::isLetterOrDigit)

    fun isValidPasswordReset(password: String, confirmation: String): Boolean =
        password.length in 12..128 && password == confirmation
}
