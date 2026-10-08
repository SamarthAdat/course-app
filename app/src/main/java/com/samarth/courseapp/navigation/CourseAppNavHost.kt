package com.samarth.courseapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samarth.courseapp.feature.dashboard.DashboardRoute
import com.samarth.courseapp.feature.detail.CourseDetailRoute
import com.samarth.courseapp.feature.login.LoginRoute

@Composable
fun CourseAppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = LoginDestination) {

        composable<LoginDestination> {
            LoginRoute(
                onLoggedIn = {
                    navController.navigate(DashboardDestination) {
                        // The login screen must not be reachable with the back button once a
                        // session exists.
                        popUpTo(LoginDestination) { inclusive = true }
                    }
                },
            )
        }

        composable<DashboardDestination> {
            DashboardRoute(
                onCourseClick = { courseId -> navController.navigate(CourseDetailDestination(courseId)) },
                onLoggedOut = {
                    navController.navigate(LoginDestination) {
                        popUpTo(DashboardDestination) { inclusive = true }
                    }
                },
            )
        }

        composable<CourseDetailDestination> {
            CourseDetailRoute(onBack = navController::navigateUp)
        }
    }
}
