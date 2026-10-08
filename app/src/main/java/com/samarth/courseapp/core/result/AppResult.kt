package com.samarth.courseapp.core.result

/**
 * Explicit success/failure wrapper for operations that can fail in a way the UI must react to.
 *
 * Preferred over [kotlin.Result] because the failure arm is a closed [AppError] set, which makes
 * the `when` in the presentation layer exhaustive at compile time.
 */
sealed interface AppResult<out T> {

    data class Success<out T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}
