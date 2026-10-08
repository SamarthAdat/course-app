package com.samarth.courseapp.data.local.entity

/**
 * Projection returned by the dashboard query: the course row plus aggregated lesson counts.
 *
 * Counting in SQL keeps the dashboard O(1) rows per course instead of loading every lesson of
 * every course just to show a percentage.
 */
data class CourseWithProgress(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessonCount: Int,
    val completedLessonCount: Int,
)
