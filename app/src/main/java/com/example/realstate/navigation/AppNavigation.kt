package com.example.realstate.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.realstate.ui.screens.DetailScreen
import com.example.realstate.ui.screens.LoginScreen
import com.example.realstate.ui.screens.MainScreen

private const val TRANSITION_DURATION = 400

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_DURATION))
        },
        exitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_DURATION))
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onNavigateToDetail = { propertyId ->
                    navController.navigate("detail/$propertyId")
                }
            )
        }

        composable(
            route = "detail/{propertyId}",
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId")
            DetailScreen(
                propertyId = propertyId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
