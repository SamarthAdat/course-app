package com.samarth.courseapp.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 *
 * Arguments are declared as properties rather than assembled into a string, so a missing or
 * mistyped argument is a compile error instead of a crash at navigation time.
 */
@Serializable
data object LoginDestination

@Serializable
data object DashboardDestination

@Serializable
data class CourseDetailDestination(val courseId: Int)
