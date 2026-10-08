package com.samarth.courseapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format of the (mock) `GET /courses` response.
 *
 * DTOs are kept separate from the domain models on purpose: the payload shape is the backend's to
 * change, and nothing above the mapper should have to change with it.
 */
@Serializable
data class CoursesResponseDto(
    @SerialName("courses") val courses: List<CourseDto> = emptyList(),
)

@Serializable
data class CourseDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("instructor") val instructor: String,
    /**
     * Server-reported progress. Intentionally *not* persisted: the app treats lesson completion as
     * the source of truth and derives progress from it, so a local completion made offline cannot
     * be contradicted by a stale server number.
     */
    @SerialName("progress") val progress: Int = 0,
    /** Server-reported lesson count, same reasoning as [progress]. */
    @SerialName("lessons") val lessonCount: Int = 0,
    @SerialName("lessonList") val lessons: List<LessonDto> = emptyList(),
)

@Serializable
data class LessonDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("completed") val isCompleted: Boolean = false,
)
