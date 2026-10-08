package com.samarth.courseapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.samarth.courseapp.data.local.entity.CourseEntity
import com.samarth.courseapp.data.local.entity.LessonEntity

@Database(
    entities = [CourseEntity::class, LessonEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CourseDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    companion object {
        const val NAME = "course-cache.db"
    }
}
