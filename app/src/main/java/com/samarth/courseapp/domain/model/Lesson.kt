package com.samarth.courseapp.domain.model

data class Lesson(
    val id: Int,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
)
