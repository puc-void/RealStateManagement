package com.example.realstate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user = MockData.currentUser
    
    val navItems = remember(user.role) {
        when (user.role) {
            UserRole.USER -> listOf(
                BottomNavItem("home", "Home", Icons.Default.Home),
                BottomNavItem("orders", "Orders", Icons.Default.List),
                BottomNavItem("profile", "Profile", Icons.Default.Person)
            )
            
            UserRole.AGENT -> listOf(
                BottomNavItem("agent_dashboard", "Dashboard", Icons.Default.Dashboard),
                BottomNavItem("profile", "Profile", Icons.Default.Person)
            )
            UserRole.ADMIN -> listOf(
                BottomNavItem("admin_dashboard", "Admin", Icons.Default.AdminPanelSettings),
                BottomNavItem("profile", "Profile", Icons.Default.Person)
            )
        }
    }

    val startDest = remember(user.role) {
        when (user.role) {
            UserRole.USER -> "home"
            UserRole.AGENT -> "agent_dashboard"
            UserRole.ADMIN -> "admin_dashboard"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Nestora",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Home/Dashboard") },
                    selected = false,
                    onClick = { 
                        navController.navigate(startDest)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = onLogout,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // We'll hide the top bar for Agent and Admin since they have custom headers
                if (user.role == UserRole.USER) {
                    TopAppBar(
                        title = { Text("Nestora", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Search */ }) {
                                Icon(Icons.Default.Search, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(32.dp)),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            modifier = Modifier.height(64.dp),
                            tonalElevation = 0.dp
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    icon = { 
                                        Icon(
                                            item.icon, 
                                            contentDescription = item.label,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            item.label, 
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = selected,
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                enterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
                exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
                popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) },
                popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
            ) {
                composable("home") {
                    HomeScreen(onPropertyClick = onNavigateToDetail)
                }
                composable("orders") {
                    OrdersScreen(onPropertyClick = onNavigateToDetail)
                }
                composable("profile") {
                    ProfileScreen(
                        onLogout = onLogout,
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
                composable("agent_dashboard") {
                    AgentDashboardScreen(onNavigateToDetail = onNavigateToDetail)
                }
                composable("admin_dashboard") {
                    AdminPanelScreen(
                        onNavigateToDetail = onNavigateToDetail,
                        onNavigateToUserDetail = { userId ->
                            navController.navigate("user_detail/$userId")
                        }
                    )
                }
                composable("user_detail/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    UserDetailScreen(
                        userId = userId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
