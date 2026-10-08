package com.samarth.courseapp.domain

import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Progress is the one number the whole app agrees on, so its edge cases are pinned down here.
 */
class ProgressCalculatorTest {

    @Test
    fun `no lessons reports zero instead of dividing by zero`() {
        assertEquals(0, ProgressCalculator.percent(completedLessons = 0, totalLessons = 0))
        assertEquals(0, ProgressCalculator.percent(completedLessons = 3, totalLessons = 0))
    }

    @Test
    fun `all lessons complete reports one hundred`() {
        assertEquals(100, ProgressCalculator.percent(completedLessons = 20, totalLessons = 20))
    }

    @Test
    fun `fractional progress is rounded to the nearest whole percent`() {
        // 1/3 -> 33.33, 2/3 -> 66.67: rounds down then up, so truncation would be visible here.
        assertEquals(33, ProgressCalculator.percent(completedLessons = 1, totalLessons = 3))
        assertEquals(67, ProgressCalculator.percent(completedLessons = 2, totalLessons = 3))
    }

    @Test
    fun `out of range counts are clamped so the progress bar cannot overflow`() {
        assertEquals(100, ProgressCalculator.percent(completedLessons = 25, totalLessons = 20))
        assertEquals(0, ProgressCalculator.percent(completedLessons = -4, totalLessons = 20))
    }

    @Test
    fun `course derives its percentage from its lesson counts`() {
        val course = Course(
            id = 1,
            title = "Python Programming",
            instructor = "John Smith",
            lessonCount = 20,
            completedLessonCount = 13,
        )

        assertEquals(65, course.progressPercent)

        // Marking one more lesson complete moves the number with no other state involved.
        assertEquals(70, course.copy(completedLessonCount = 14).progressPercent)
    }
}
