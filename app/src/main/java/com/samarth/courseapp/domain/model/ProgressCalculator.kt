package com.samarth.courseapp.domain.model

import kotlin.math.roundToInt

/**
 * Single definition of "course progress" in the app.
 *
 * Progress is always *derived* from lesson completion rather than stored as its own field. That is
 * what makes the dashboard and the details screen impossible to disagree with each other after the
 * user marks a lesson complete offline.
 */
object ProgressCalculator {

    /**
     * Percentage of [totalLessons] that are complete, rounded to the nearest whole percent.
     *
     * Returns `0` for a course with no lessons (rather than dividing by zero), and clamps
     * [completedLessons] into range so a bad payload can never produce 120% or -10%.
     */
    fun percent(completedLessons: Int, totalLessons: Int): Int {
        if (totalLessons <= 0) return 0
        val completed = completedLessons.coerceIn(0, totalLessons)
        return (completed * 100.0 / totalLessons).roundToInt()
    }
}
