package com.samarth.courseapp.util

import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.CourseDetail
import com.samarth.courseapp.domain.model.Lesson
import com.samarth.courseapp.domain.repository.CourseRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Hand-written double instead of a mocking framework: the behaviour under test is a small state
 * machine, and a fake that actually holds a cache exercises it more honestly than stubbed returns.
 */
class FakeCourseRepository : CourseRepository {

    /** Stands in for the Room cache. Tests mutate it to represent "data was already cached". */
    private val cachedCourses = MutableStateFlow<List<Course>>(emptyList())
    private val cachedLessons = MutableStateFlow<Map<Int, List<Lesson>>>(emptyMap())

    /** What the next [refresh] should do. */
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Courses the mocked "API" delivers into the cache on a successful refresh. */
    var coursesFromApi: List<Course> = emptyList()

    /** When set, [refresh] parks on it, so tests can observe a request that is still in flight. */
    var refreshGate: CompletableDeferred<Unit>? = null

    var refreshCallCount: Int = 0
        private set

    fun seedCache(courses: List<Course>, lessons: Map<Int, List<Lesson>> = emptyMap()) {
        cachedCourses.value = courses
        cachedLessons.value = lessons
    }

    override fun observeCourses(): Flow<List<Course>> = cachedCourses

    override fun observeCourseDetail(courseId: Int): Flow<CourseDetail?> =
        cachedCourses.map { courses ->
            courses.firstOrNull { it.id == courseId }?.let { course ->
                CourseDetail(course = course, lessons = cachedLessons.value[courseId].orEmpty())
            }
        }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCallCount++
        refreshGate?.await()
        if (refreshResult is AppResult.Success) {
            cachedCourses.value = coursesFromApi
        }
        return refreshResult
    }

    override suspend fun setLessonCompleted(lessonId: Int, isCompleted: Boolean) {
        val courseId = cachedLessons.value.entries
            .firstOrNull { entry -> entry.value.any { it.id == lessonId } }
            ?.key
            ?: return

        val updatedLessons = cachedLessons.value.getValue(courseId)
            .map { if (it.id == lessonId) it.copy(isCompleted = isCompleted) else it }
        cachedLessons.value = cachedLessons.value + (courseId to updatedLessons)

        // Mirrors the real repository: progress is recomputed from lessons, never stored.
        cachedCourses.value = cachedCourses.value.map { course ->
            if (course.id != courseId) {
                course
            } else {
                course.copy(completedLessonCount = updatedLessons.count { it.isCompleted })
            }
        }
    }

    companion object {

        fun course(
            id: Int,
            title: String = "Course $id",
            instructor: String = "Instructor $id",
            lessonCount: Int = 10,
            completedLessonCount: Int = 0,
        ) = Course(
            id = id,
            title = title,
            instructor = instructor,
            lessonCount = lessonCount,
            completedLessonCount = completedLessonCount,
        )

        val NO_CONNECTION: AppResult<Unit> = AppResult.Failure(AppError.NoConnection)
        val SERVER_ERROR: AppResult<Unit> = AppResult.Failure(AppError.Server)
    }
}
