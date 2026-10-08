package com.samarth.courseapp.feature.login

import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.domain.validation.CredentialsValidator

/**
 * Everything the login screen needs to render, in one immutable snapshot.
 *
 * A single state object (rather than several independent flows) means the screen can never show an
 * inconsistent combination such as "submitting" plus an enabled button.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: CredentialsValidator.EmailError? = null,
    val passwordError: CredentialsValidator.PasswordError? = null,
    val isSubmitting: Boolean = false,
    /** Failure from the last submit attempt; cleared as soon as the user edits a field. */
    val submitError: AppError? = null,
) {
    /** Disabled while a request is in flight, so a double tap cannot fire two logins. */
    val isSubmitEnabled: Boolean = !isSubmitting
}
