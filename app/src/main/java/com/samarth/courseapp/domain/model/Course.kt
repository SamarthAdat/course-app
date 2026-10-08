package com.samarth.courseapp.domain.model

/**
 * A course as the dashboard needs it: enough to render a row without loading every lesson.
 */
data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessonCount: Int,
    val completedLessonCount: Int,
) {
    /** Derived, never stored. See [ProgressCalculator]. */
    val progressPercent: Int = ProgressCalculator.percent(completedLessonCount, lessonCount)
}
