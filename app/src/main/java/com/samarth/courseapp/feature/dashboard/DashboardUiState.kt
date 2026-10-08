package com.samarth.courseapp.feature.dashboard

import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.domain.model.Course

/**
 * The four states the dashboard has to handle, expressed so that only valid combinations exist.
 *
 * The important nuance is [Content.refreshError]: "the refresh failed" and "there is nothing to
 * show" are different situations. Once anything is cached, a failed refresh degrades to a banner
 * over real data instead of replacing the screen with an error.
 */
sealed interface DashboardUiState {

    /** First load, nothing cached yet. */
    data object Loading : DashboardUiState

    /** Refresh succeeded but the catalog is genuinely empty. */
    data object Empty : DashboardUiState

    /** Nothing cached and the refresh failed: the error is all we can show. */
    data class Error(val error: AppError) : DashboardUiState

    data class Content(
        val courses: List<Course>,
        val isRefreshing: Boolean = false,
        /** Non-null when the data on screen is cached and the last refresh failed. */
        val refreshError: AppError? = null,
    ) : DashboardUiState
}
