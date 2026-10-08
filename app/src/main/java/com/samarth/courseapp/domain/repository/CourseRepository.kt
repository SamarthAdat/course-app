package com.samarth.courseapp.domain.repository

import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.CourseDetail
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first course access.
 *
 * Reads are [Flow]s over the local database, which is the single source of truth for the UI;
 * [refresh] is the only thing that talks to the network. A failed refresh therefore never empties
 * the screen -- it only produces an error the caller can surface next to the cached data.
 */
interface CourseRepository {

    fun observeCourses(): Flow<List<Course>>

    /** Emits `null` while the course is not (or no longer) in the cache. */
    fun observeCourseDetail(courseId: Int): Flow<CourseDetail?>

    /** Pulls the catalog from the API into the cache. Local lesson completion is preserved. */
    suspend fun refresh(): AppResult<Unit>

    suspend fun setLessonCompleted(lessonId: Int, isCompleted: Boolean)
}
