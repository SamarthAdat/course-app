package com.samarth.courseapp.data.mapper

import com.samarth.courseapp.data.local.entity.CourseEntity
import com.samarth.courseapp.data.local.entity.CourseWithProgress
import com.samarth.courseapp.data.local.entity.LessonEntity
import com.samarth.courseapp.data.remote.dto.CourseDto
import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.Lesson

/** DTO -> cache. Called once per refresh, inside the repository. */
fun List<CourseDto>.toCourseEntities(): List<CourseEntity> = mapIndexed { index, dto ->
    CourseEntity(
        id = dto.id,
        title = dto.title,
        instructor = dto.instructor,
        sortOrder = index,
    )
}

fun List<CourseDto>.toLessonEntities(): List<LessonEntity> = flatMap { course ->
    course.lessons.mapIndexed { index, lesson ->
        LessonEntity(
            id = lesson.id,
            courseId = course.id,
            title = lesson.title,
            isCompleted = lesson.isCompleted,
            sortOrder = index,
        )
    }
}

/** Cache -> domain. */
fun CourseWithProgress.toDomain(): Course = Course(
    id = id,
    title = title,
    instructor = instructor,
    lessonCount = lessonCount,
    completedLessonCount = completedLessonCount,
)

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = isCompleted,
)
