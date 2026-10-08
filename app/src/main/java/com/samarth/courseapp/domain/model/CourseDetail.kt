package com.samarth.courseapp.domain.model

/**
 * A course together with its lessons, for the details screen.
 */
data class CourseDetail(
    val course: Course,
    val lessons: List<Lesson>,
)
