package com.samarth.courseapp.di

import android.content.Context
import androidx.room.Room
import com.samarth.courseapp.data.local.CourseDao
import com.samarth.courseapp.data.local.CourseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCourseDatabase(@ApplicationContext context: Context): CourseDatabase =
        Room.databaseBuilder(context, CourseDatabase::class.java, CourseDatabase.NAME).build()

    @Provides
    fun provideCourseDao(database: CourseDatabase): CourseDao = database.courseDao()
}
