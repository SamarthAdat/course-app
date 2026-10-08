package com.samarth.courseapp.feature.dashboard

import com.samarth.courseapp.core.network.NetworkMonitor
import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.data.remote.MockApiConfig
import com.samarth.courseapp.domain.model.Lesson
import com.samarth.courseapp.domain.repository.AuthRepository
import com.samarth.courseapp.util.FakeCourseRepository
import com.samarth.courseapp.util.FakeCourseRepository.Companion.course
import com.samarth.courseapp.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The dashboard state machine, which is where the offline requirement actually lives.
 *
 * The behaviour worth protecting is the asymmetry between the two failure cases: a failed refresh
 * may only take over the screen when there is genuinely nothing cached. Getting that backwards is
 * the classic offline bug -- the data is already on the device and the user is shown an error
 * anyway.
 */
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val courseRepository = FakeCourseRepository()

    @Test
    fun `successful first load shows the courses the api returned`() = runTest {
        courseRepository.coursesFromApi = listOf(
            course(id = 1, title = "Python Programming", lessonCount = 20, completedLessonCount = 13),
            course(id = 2, title = "Generative AI", lessonCount = 15, completedLessonCount = 6),
        )

        val state = stateOf(viewModel())

        assertTrue(state is DashboardUiState.Content)
        state as DashboardUiState.Content
        assertEquals(listOf("Python Programming", "Generative AI"), state.courses.map { it.title })
        assertEquals(listOf(65, 40), state.courses.map { it.progressPercent })
        assertNull(state.refreshError)
    }

    @Test
    fun `refresh failure with nothing cached surfaces a full screen error`() = runTest {
        courseRepository.refreshResult = FakeCourseRepository.SERVER_ERROR

        val state = stateOf(viewModel())

        assertEquals(DashboardUiState.Error(AppError.Server), state)
    }

    @Test
    fun `refresh failure with cached courses keeps showing them with a notice`() = runTest {
        // The offline scenario from the brief: the catalog was loaded once, then the radio is off.
        courseRepository.seedCache(
            listOf(course(id = 1, title = "Python Programming", lessonCount = 20, completedLessonCount = 13)),
        )
        courseRepository.refreshResult = FakeCourseRepository.NO_CONNECTION

        val state = stateOf(viewModel(isOnline = false))

        assertTrue("cached data must survive a failed refresh", state is DashboardUiState.Content)
        state as DashboardUiState.Content
        assertEquals(listOf("Python Programming"), state.courses.map { it.title })
        assertEquals(65, state.courses.single().progressPercent)
        assertEquals(AppError.NoConnection, state.refreshError)
    }

    @Test
    fun `successful refresh that returns nothing shows the empty state not an error`() = runTest {
        courseRepository.refreshResult = AppResult.Success(Unit)
        courseRepository.coursesFromApi = emptyList()

        assertEquals(DashboardUiState.Empty, stateOf(viewModel()))
    }

    @Test
    fun `marking a lesson complete raises the progress the dashboard shows`() = runTest {
        val lessons = (1..4).map {
            Lesson(id = it, courseId = 1, title = "Lesson $it", isCompleted = false)
        }
        val courses = listOf(course(id = 1, lessonCount = 4, completedLessonCount = 0))
        courseRepository.seedCache(courses = courses, lessons = mapOf(1 to lessons))
        courseRepository.coursesFromApi = courses // the refresh on init returns the same catalog

        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(0, (viewModel.uiState.value as DashboardUiState.Content).courses.single().progressPercent)

        // Completion is written through the repository; the dashboard sees it via the same Flow.
        courseRepository.setLessonCompleted(lessonId = 1, isCompleted = true)

        assertEquals(25, (viewModel.uiState.value as DashboardUiState.Content).courses.single().progressPercent)
    }

    @Test
    fun `a refresh already in flight is not started twice`() = runTest {
        // Hold the first request open so the second one has something to collide with.
        val inFlight = CompletableDeferred<Unit>()
        courseRepository.refreshGate = inFlight
        courseRepository.coursesFromApi = listOf(course(id = 1))

        val viewModel = viewModel() // init triggers the first refresh, which parks on the gate
        viewModel.refresh()
        viewModel.refresh()

        assertEquals("a double tap on refresh must not fire two requests", 1, courseRepository.refreshCallCount)
        inFlight.complete(Unit)
    }

    private fun viewModel(isOnline: Boolean = true) = DashboardViewModel(
        courseRepository = courseRepository,
        authRepository = NoOpAuthRepository,
        mockApiConfig = MockApiConfig(),
        networkMonitor = FixedNetworkMonitor(isOnline),
    )

    /**
     * `uiState` is a `WhileSubscribed` StateFlow, so it stays at its initial value until something
     * collects it. This keeps a collector alive for the duration of the test and returns the
     * settled state.
     */
    private fun TestScope.stateOf(viewModel: DashboardViewModel): DashboardUiState {
        subscribe(viewModel)
        return viewModel.uiState.value
    }

    private fun TestScope.subscribe(viewModel: DashboardViewModel) {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }
    }

    private class FixedNetworkMonitor(private val online: Boolean) : NetworkMonitor {
        override val isOnline: Flow<Boolean> = flowOf(online)
        override suspend fun isCurrentlyOnline(): Boolean = online
    }

    private object NoOpAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override fun logout() = Unit
    }
}
