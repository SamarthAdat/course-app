package com.samarth.courseapp.feature.detail

import com.samarth.courseapp.domain.model.CourseDetail

sealed interface CourseDetailUiState {

    data object Loading : CourseDetailUiState

    /**
     * The course is not in the cache. Reachable if the catalog shrank under a deep link or while
     * the screen sat in the back stack, so it gets a state rather than a crash.
     */
    data object NotFound : CourseDetailUiState

    data class Content(val detail: CourseDetail) : CourseDetailUiState
}
