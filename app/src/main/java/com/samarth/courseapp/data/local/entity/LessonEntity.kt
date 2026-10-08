package com.samarth.courseapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Required for the foreign key and for the per-course lesson queries to stay index-backed.
    indices = [Index("courseId")],
)
data class LessonEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
)
