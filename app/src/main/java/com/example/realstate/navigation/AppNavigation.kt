package com.example.realstate.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.example.realstate.RealStateApp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.realstate.ui.screens.DetailScreen
import com.example.realstate.ui.screens.LoginScreen
import com.example.realstate.ui.screens.MainScreen
import com.example.realstate.ui.screens.OtpVerificationScreen
import com.example.realstate.ui.screens.SignUpScreen
import com.example.realstate.ui.screens.WishlistItemDetailScreen
import com.example.realstate.ui.viewmodels.AuthViewModel

private const val TRANSITION_DURATION = 400

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Shared AuthViewModel scoped to the nav graph — same instance for Login & SignUp
    val authViewModel: AuthViewModel = viewModel()

    val token = RealStateApp.preferenceManager.getToken()
    val startDest = if (token != null) {
        // Initialize session from stored ID
        authViewModel.loadSession()
        "main"
    } else "login"

    NavHost(
        navController = navController,
        startDestination = startDest,
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
        // ── Login ────────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        // ── Sign Up ──────────────────────────────────────────────────────────
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = { userId, role ->
                    navController.navigate("verify/$userId/$role") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── OTP Verification ─────────────────────────────────────────────────
        composable(
            route = "verify/{userId}/{role}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "USER"
            OtpVerificationScreen(
                userId = userId,
                role = role,
                authViewModel = authViewModel,
                onVerified = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Main (role-based dashboard) ───────────────────────────────────────
        composable("main") {
            MainScreen(
                onLogout = {
                    RealStateApp.preferenceManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onNavigateToDetail = { propertyId ->
                    navController.navigate("detail/$propertyId")
                },
                onNavigateToWishlistItemDetail = { itemId ->
                    navController.navigate("wishlist_item_detail/$itemId")
                }
            )
        }

        // ── Property Detail ───────────────────────────────────────────────────
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

        // ── Wishlist Item Detail ──────────────────────────────────────────────
        composable(
            route = "wishlist_item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            WishlistItemDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
