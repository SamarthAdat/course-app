package com.samarth.courseapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Slate40,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Amber40,
    background = SurfaceLight,
    surface = Color.White,
    error = Red40,
    onError = Color.White,
    errorContainer = Red80,
    onErrorContainer = Red10,
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal10,
    primaryContainer = Teal40,
    onPrimaryContainer = Teal90,
    secondary = Slate80,
    secondaryContainer = Slate40,
    onSecondaryContainer = Slate90,
    tertiary = Amber80,
    background = SurfaceDark,
    surface = Color(0xFF171D21),
    error = Red80,
    onError = Red10,
    errorContainer = Red40,
    onErrorContainer = Red80,
)

@Composable
fun CourseAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = CourseAppTypography,
        content = content,
    )
}
