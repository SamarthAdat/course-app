package com.samarth.courseapp.data

import com.samarth.courseapp.data.mapper.toCourseEntities
import com.samarth.courseapp.data.mapper.toLessonEntities
import com.samarth.courseapp.data.remote.dto.CoursesResponseDto
import com.samarth.courseapp.domain.model.ProgressCalculator
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the contract between the mock payload and the mapping/progress logic.
 *
 * The payload keeps the `progress` and `lessons` fields from the brief, but the app ignores them
 * and derives both from the lesson list instead. That is only safe if the two actually agree on
 * load, which is what this test checks -- it would catch either a hand-edited fixture or a mapper
 * that drops lessons.
 */
class CourseCatalogFixtureTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val catalog: CoursesResponseDto by lazy {
        // Unit tests run with the module directory as the working directory.
        val asset = listOf("src/main/assets/courses.json", "app/src/main/assets/courses.json")
            .map(::File)
            .firstOrNull(File::exists)
            ?: error("courses.json not found; looked relative to ${File("").absolutePath}")

        json.decodeFromString<CoursesResponseDto>(asset.readText())
    }

    @Test
    fun `every course declares a progress value that matches its lessons`() {
        assertTrue("the fixture must not be empty", catalog.courses.isNotEmpty())

        catalog.courses.forEach { course ->
            val completed = course.lessons.count { it.isCompleted }
            val derived = ProgressCalculator.percent(completed, course.lessons.size)

            assertEquals(
                "${course.title}: server progress disagrees with its lesson list",
                course.progress,
                derived,
            )
            assertEquals(
                "${course.title}: server lesson count disagrees with its lesson list",
                course.lessonCount,
                course.lessons.size,
            )
        }
    }

    @Test
    fun `mapping the payload keeps every course and lesson with stable ordering`() {
        val courseEntities = catalog.courses.toCourseEntities()
        val lessonEntities = catalog.courses.toLessonEntities()

        assertEquals(catalog.courses.size, courseEntities.size)
        assertEquals(catalog.courses.sumOf { it.lessons.size }, lessonEntities.size)

        // sortOrder is what the cached list is ordered by, so it has to follow the API order.
        assertEquals(catalog.courses.map { it.title }, courseEntities.sortedBy { it.sortOrder }.map { it.title })

        // Lesson ids must be unique across courses: they are the primary key of the lessons table.
        assertEquals(lessonEntities.size, lessonEntities.map { it.id }.distinct().size)
    }
}
