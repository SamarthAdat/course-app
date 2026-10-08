package com.samarth.courseapp.domain.validation

/**
 * Login input rules, kept in the domain layer rather than in the Compose screen so they can be
 * unit tested on the JVM and reused if a second entry point (deep link, SSO fallback) ever needs
 * them.
 *
 * Note: `android.util.Patterns.EMAIL_ADDRESS` is deliberately avoided -- it is framework code and
 * would force this logic into an instrumented test.
 */
object CredentialsValidator {

    const val MIN_PASSWORD_LENGTH = 6

    // Character classes instead of escapes: same intent, readable without backslash noise.
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*[.][A-Za-z]{2,}$")

    fun validateEmail(email: String): EmailError? = when {
        email.isBlank() -> EmailError.Empty
        !EMAIL_REGEX.matches(email.trim()) -> EmailError.Malformed
        else -> null
    }

    fun validatePassword(password: String): PasswordError? = when {
        password.isEmpty() -> PasswordError.Empty
        password.length < MIN_PASSWORD_LENGTH -> PasswordError.TooShort
        else -> null
    }

    enum class EmailError { Empty, Malformed }

    enum class PasswordError { Empty, TooShort }
}
