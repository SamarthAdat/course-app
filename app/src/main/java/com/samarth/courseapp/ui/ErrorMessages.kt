package com.samarth.courseapp.ui

import androidx.annotation.StringRes
import com.samarth.courseapp.R
import com.samarth.courseapp.core.result.AppError

/**
 * The single place where an [AppError] becomes words a user reads.
 *
 * ViewModels hold the [AppError], not a string: that keeps them free of Android resources and
 * makes localisation a resource-file change instead of a code change.
 */
@StringRes
fun AppError.toMessageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Server -> R.string.error_server
    AppError.InvalidCredentials -> R.string.error_invalid_credentials
    is AppError.Unexpected -> R.string.error_unexpected
}
