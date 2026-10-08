package com.samarth.courseapp.data.repository

import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.data.local.CourseDao
import com.samarth.courseapp.data.mapper.toCourseEntities
import com.samarth.courseapp.data.mapper.toDomain
import com.samarth.courseapp.data.mapper.toLessonEntities
import com.samarth.courseapp.data.remote.CourseApi
import com.samarth.courseapp.data.remote.NoConnectionException
import com.samarth.courseapp.data.remote.ServerException
import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.CourseDetail
import com.samarth.courseapp.domain.repository.CourseRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Offline-first implementation: the database is the single source of truth the UI observes, and
 * the network is only ever a way to *update* it.
 *
 * Consequences that matter for this assignment:
 *  - a failed refresh cannot blank the screen, because the UI is not reading from the network;
 *  - a lesson marked complete is written to the cache and the dashboard's derived progress
 *    updates through the same Flow, with no cross-screen event bus;
 *  - everything keeps working with the radio off until the user clears app data.
 */
class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi,
    private val courseDao: CourseDao,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        courseDao.observeCoursesWithProgress().map { rows -> rows.map { it.toDomain() } }

    override fun observeCourseDetail(courseId: Int): Flow<CourseDetail?> = combine(
        courseDao.observeCourseWithProgress(courseId),
        courseDao.observeLessons(courseId),
    ) { course, lessons ->
        course?.let { row ->
            CourseDetail(course = row.toDomain(), lessons = lessons.map { it.toDomain() })
        }
    }

    override suspend fun refresh(): AppResult<Unit> = try {
        val courses = courseApi.getCourses()
        courseDao.replaceCatalog(
            courses = courses.toCourseEntities(),
            lessons = courses.toLessonEntities(),
        )
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        // Never swallow cancellation: it is structured concurrency, not an error.
        throw cancellation
    } catch (offline: NoConnectionException) {
        AppResult.Failure(AppError.NoConnection)
    } catch (server: ServerException) {
        AppResult.Failure(AppError.Server)
    } catch (unexpected: Exception) {
        AppResult.Failure(AppError.Unexpected(unexpected))
    }

    override suspend fun setLessonCompleted(lessonId: Int, isCompleted: Boolean) {
        courseDao.updateLessonCompletion(lessonId, isCompleted)
    }
}
