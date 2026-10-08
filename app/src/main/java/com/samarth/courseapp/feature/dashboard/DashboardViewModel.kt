package com.samarth.courseapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samarth.courseapp.BuildConfig
import com.samarth.courseapp.core.network.NetworkMonitor
import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.data.remote.MockApiConfig
import com.samarth.courseapp.domain.repository.AuthRepository
import com.samarth.courseapp.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
    private val mockApiConfig: MockApiConfig,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    /** Everything about the *network* half of the screen. The data half comes from the database. */
    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val error: AppError? = null,
        /** Until the first attempt finishes, an empty cache means "loading", not "empty". */
        val hasAttemptedRefresh: Boolean = false,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    /**
     * Cached courses and refresh status are combined into one state, so the screen cannot render
     * "empty" while a first load is still running, or "error" while real data is available.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        courseRepository.observeCourses(),
        refreshState,
    ) { courses, refresh ->
        when {
            courses.isNotEmpty() -> DashboardUiState.Content(
                courses = courses,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
            )

            refresh.isRefreshing || !refresh.hasAttemptedRefresh -> DashboardUiState.Loading
            refresh.error != null -> DashboardUiState.Error(refresh.error)
            else -> DashboardUiState.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        // Survives a configuration change without re-subscribing to Room on every rotation.
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DashboardUiState.Loading,
    )

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { !it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = false,
        )

    /** Debug-only affordance for exercising the error path; hidden in release builds. */
    val isFailureSimulationAvailable: Boolean = BuildConfig.DEBUG
    val isFailureSimulationOn: StateFlow<Boolean> = mockApiConfig.forceFailure

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            val result = courseRepository.refresh()
            refreshState.update {
                it.copy(
                    isRefreshing = false,
                    hasAttemptedRefresh = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    fun onToggleFailureSimulation() = mockApiConfig.toggleForceFailure()

    fun onLogout() = authRepository.logout()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
