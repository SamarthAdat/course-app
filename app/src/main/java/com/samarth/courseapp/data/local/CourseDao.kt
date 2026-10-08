package com.samarth.courseapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.samarth.courseapp.data.local.entity.CourseEntity
import com.samarth.courseapp.data.local.entity.CourseWithProgress
import com.samarth.courseapp.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query(
        """
        SELECT c.id AS id,
               c.title AS title,
               c.instructor AS instructor,
               COUNT(l.id) AS lessonCount,
               COALESCE(SUM(CASE WHEN l.isCompleted THEN 1 ELSE 0 END), 0) AS completedLessonCount
        FROM courses AS c
        LEFT JOIN lessons AS l ON l.courseId = c.id
        GROUP BY c.id
        ORDER BY c.sortOrder ASC
        """
    )
    fun observeCoursesWithProgress(): Flow<List<CourseWithProgress>>

    @Query(
        """
        SELECT c.id AS id,
               c.title AS title,
               c.instructor AS instructor,
               COUNT(l.id) AS lessonCount,
               COALESCE(SUM(CASE WHEN l.isCompleted THEN 1 ELSE 0 END), 0) AS completedLessonCount
        FROM courses AS c
        LEFT JOIN lessons AS l ON l.courseId = c.id
        WHERE c.id = :courseId
        GROUP BY c.id
        """
    )
    fun observeCourseWithProgress(courseId: Int): Flow<CourseWithProgress?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sortOrder ASC")
    fun observeLessons(courseId: Int): Flow<List<LessonEntity>>

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun updateLessonCompletion(lessonId: Int, isCompleted: Boolean)

    @Upsert
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("SELECT id FROM lessons WHERE isCompleted = 1")
    suspend fun completedLessonIds(): List<Int>

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    suspend fun deleteCoursesNotIn(keepIds: List<Int>)

    @Query("DELETE FROM lessons WHERE id NOT IN (:keepIds)")
    suspend fun deleteLessonsNotIn(keepIds: List<Int>)

    /**
     * Replaces the cached catalog with [courses]/[lessons] in one transaction, **keeping lesson
     * completion that only exists locally**.
     *
     * Without this merge a refresh would silently undo progress the user made while offline, since
     * the mock backend knows nothing about local completions. A real backend would receive those
     * completions via a sync call and the merge would move server-side; until then the local flag
     * wins, which is the safe direction for the user.
     */
    @Transaction
    suspend fun replaceCatalog(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        val locallyCompleted = completedLessonIds().toSet()

        upsertCourses(courses)
        upsertLessons(
            lessons.map { lesson ->
                if (lesson.id in locallyCompleted) lesson.copy(isCompleted = true) else lesson
            }
        )

        // Drop anything the catalog no longer contains. Courses cascade to their lessons.
        deleteCoursesNotIn(courses.map { it.id })
        deleteLessonsNotIn(lessons.map { it.id })
    }
}
