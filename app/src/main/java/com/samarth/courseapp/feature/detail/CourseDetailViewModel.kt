package com.samarth.courseapp.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.samarth.courseapp.domain.repository.CourseRepository
import com.samarth.courseapp.navigation.CourseDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val courseId: Int = savedStateHandle.toRoute<CourseDetailDestination>().courseId

    /**
     * Read straight from the cache. There is no refresh here on purpose: lessons arrive with the
     * catalog, so this screen works with the radio off and never shows a spinner the user has to
     * wait through to tick a checkbox.
     */
    val uiState: StateFlow<CourseDetailUiState> = courseRepository.observeCourseDetail(courseId)
        .map { detail ->
            if (detail == null) {
                CourseDetailUiState.NotFound
            } else {
                CourseDetailUiState.Content(detail)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = CourseDetailUiState.Loading,
        )

    /**
     * Writes the new flag to the cache and lets the Flow push the result back.
     *
     * Nothing is held in the ViewModel, so the recomputed course progress reaches this screen and
     * the dashboard through the same query -- the two cannot drift apart.
     */
    fun onLessonCompletionChange(lessonId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            courseRepository.setLessonCompleted(lessonId, isCompleted)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
