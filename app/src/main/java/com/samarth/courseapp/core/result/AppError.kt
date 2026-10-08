package com.samarth.courseapp.core.result

/**
 * Transport- and framework-agnostic failure vocabulary.
 *
 * The data layer translates exceptions into these once, so neither the ViewModels nor the UI ever
 * have to reason about [java.io.IOException], HTTP codes or SQLite errors.
 */
sealed interface AppError {

    /** The device has no usable network. Cached data, if any, is still valid to show. */
    data object NoConnection : AppError

    /** The backend answered, but not with something we can use (5xx, malformed payload, ...). */
    data object Server : AppError

    /** Credentials were well-formed but rejected by the auth backend. */
    data object InvalidCredentials : AppError

    /** Anything we did not anticipate; [cause] is kept for logging, never for the user. */
    data class Unexpected(val cause: Throwable? = null) : AppError
}
