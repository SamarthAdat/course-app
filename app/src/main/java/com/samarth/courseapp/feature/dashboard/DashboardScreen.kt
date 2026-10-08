package com.samarth.courseapp.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samarth.courseapp.R
import com.samarth.courseapp.ui.components.EmptyState
import com.samarth.courseapp.ui.components.ErrorState
import com.samarth.courseapp.ui.components.LoadingState
import com.samarth.courseapp.ui.components.StaleDataBanner
import com.samarth.courseapp.ui.toMessageRes

@Composable
fun DashboardRoute(
    onCourseClick: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isFailureSimulationOn by viewModel.isFailureSimulationOn.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        isOffline = isOffline,
        isFailureSimulationAvailable = viewModel.isFailureSimulationAvailable,
        isFailureSimulationOn = isFailureSimulationOn,
        onCourseClick = onCourseClick,
        onRefresh = viewModel::refresh,
        onToggleFailureSimulation = viewModel::onToggleFailureSimulation,
        onLogout = {
            viewModel.onLogout()
            onLoggedOut()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    isOffline: Boolean,
    isFailureSimulationAvailable: Boolean,
    isFailureSimulationOn: Boolean,
    onCourseClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onToggleFailureSimulation: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    DashboardMenu(
                        isFailureSimulationAvailable = isFailureSimulationAvailable,
                        isFailureSimulationOn = isFailureSimulationOn,
                        onRefresh = onRefresh,
                        onToggleFailureSimulation = onToggleFailureSimulation,
                        onLogout = onLogout,
                    )
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            DashboardUiState.Loading -> LoadingState(
                modifier = Modifier.padding(innerPadding),
            )

            DashboardUiState.Empty -> EmptyState(
                title = stringResource(R.string.dashboard_empty_title),
                body = stringResource(R.string.dashboard_empty_body),
                modifier = Modifier.padding(innerPadding),
            )

            is DashboardUiState.Error -> ErrorState(
                message = stringResource(uiState.error.toMessageRes()),
                onRetry = onRefresh,
                modifier = Modifier.padding(innerPadding),
            )

            is DashboardUiState.Content -> CourseList(
                uiState = uiState,
                isOffline = isOffline,
                contentPadding = innerPadding,
                onCourseClick = onCourseClick,
                onRefresh = onRefresh,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseList(
    uiState: DashboardUiState.Content,
    isOffline: Boolean,
    contentPadding: PaddingValues,
    onCourseClick: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().padding(contentPadding),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Offline and "refresh failed" are reported separately: the first explains why, the
            // second says what it means for the data below.
            if (isOffline) {
                StaleDataBanner(message = stringResource(R.string.offline_banner))
            } else if (uiState.refreshError != null) {
                StaleDataBanner(message = stringResource(R.string.offline_stale_notice))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = uiState.courses, key = { it.id }) { course ->
                    CourseCard(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
    }
}

@Composable
private fun DashboardMenu(
    isFailureSimulationAvailable: Boolean,
    isFailureSimulationOn: Boolean,
    onRefresh: () -> Unit,
    onToggleFailureSimulation: () -> Unit,
    onLogout: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = stringResource(R.string.dashboard_more_actions),
        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.dashboard_refresh)) },
            onClick = {
                expanded = false
                onRefresh()
            },
        )
        if (isFailureSimulationAvailable) {
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            if (isFailureSimulationOn) {
                                R.string.dashboard_simulate_failure_on
                            } else {
                                R.string.dashboard_simulate_failure_off
                            }
                        )
                    )
                },
                onClick = {
                    expanded = false
                    onToggleFailureSimulation()
                },
            )
        }
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.dashboard_logout),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            onClick = {
                expanded = false
                onLogout()
            },
        )
    }
}
