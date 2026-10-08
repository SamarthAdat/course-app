package com.samarth.courseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.samarth.courseapp.navigation.CourseAppNavHost
import com.samarth.courseapp.ui.theme.CourseAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity hosting the Compose navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CourseAppTheme {
                CourseAppNavHost()
            }
        }
    }
}
