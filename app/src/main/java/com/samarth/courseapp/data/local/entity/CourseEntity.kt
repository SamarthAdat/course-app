package com.samarth.courseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    /** Preserves the order the API returned, so the cached list is not re-sorted arbitrarily. */
    val sortOrder: Int,
)
