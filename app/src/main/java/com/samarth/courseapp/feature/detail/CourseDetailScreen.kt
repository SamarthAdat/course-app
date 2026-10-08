package com.samarth.courseapp.feature.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samarth.courseapp.R
import com.samarth.courseapp.domain.model.Course
import com.samarth.courseapp.domain.model.Lesson
import com.samarth.courseapp.ui.components.EmptyState
import com.samarth.courseapp.ui.components.LoadingState

@Composable
fun CourseDetailRoute(
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onLessonToggle = viewModel::onLessonCompletionChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    uiState: CourseDetailUiState,
    onBack: () -> Unit,
    onLessonToggle: (lessonId: Int, isCompleted: Boolean) -> Unit,
) {
    val title = (uiState as? CourseDetailUiState.Content)?.detail?.course?.title
        ?: stringResource(R.string.course_detail_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            CourseDetailUiState.Loading -> LoadingState(
                modifier = Modifier.padding(innerPadding),
            )

            CourseDetailUiState.NotFound -> EmptyState(
                title = stringResource(R.string.course_detail_not_found),
                body = "",
                modifier = Modifier.padding(innerPadding),
            )

            is CourseDetailUiState.Content -> LessonList(
                course = uiState.detail.course,
                lessons = uiState.detail.lessons,
                contentPadding = innerPadding,
                onLessonToggle = onLessonToggle,
            )
        }
    }
}

@Composable
private fun LessonList(
    course: Course,
    lessons: List<Lesson>,
    contentPadding: PaddingValues,
    onLessonToggle: (lessonId: Int, isCompleted: Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            CourseProgressHeader(course = course)
        }

        item {
            Text(
                text = stringResource(R.string.course_detail_lessons_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            )
        }

        items(items = lessons, key = { it.id }) { lesson ->
            LessonRow(
                lesson = lesson,
                onToggle = { onLessonToggle(lesson.id, !lesson.isCompleted) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun CourseProgressHeader(course: Course) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = course.instructor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { course.progressPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.course_progress, course.progressPercent),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    R.string.course_lessons_progress,
                    course.completedLessonCount,
                    course.lessonCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LessonRow(
    lesson: Lesson,
    onToggle: () -> Unit,
) {
    // The whole row is the touch target: a 24dp checkbox is below the 48dp minimum.
    val actionLabel = stringResource(
        if (lesson.isCompleted) R.string.lesson_mark_incomplete else R.string.lesson_mark_complete,
        lesson.title,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            // One semantics node for the row, so a screen reader announces the title, the state
            // and the action once instead of three times.
            .clearAndSetSemantics { contentDescription = actionLabel },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                if (lesson.isCompleted) R.drawable.ic_check_circle else R.drawable.ic_circle_outline
            ),
            contentDescription = null,
            tint = if (lesson.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
            modifier = Modifier.size(22.dp),
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (lesson.isCompleted) TextDecoration.LineThrough else null,
                color = if (lesson.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = stringResource(
                    if (lesson.isCompleted) R.string.lesson_completed else R.string.lesson_pending
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
