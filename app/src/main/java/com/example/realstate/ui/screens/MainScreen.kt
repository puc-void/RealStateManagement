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
        val items = mutableListOf(
            BottomNavItem("home", "Home", Icons.Default.Home),
            BottomNavItem("orders", "Orders", Icons.Default.List),
            BottomNavItem("profile", "Profile", Icons.Default.Person)
        )
        if (user.role == UserRole.ADMIN || user.role == UserRole.SUB_ADMIN) {
            items.add(1, BottomNavItem("admin", "Admin", Icons.Default.Security))
        }
        items
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "RealState Pro",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { 
                        navController.navigate("home")
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
                if (user.role == UserRole.ADMIN || user.role == UserRole.SUB_ADMIN) {
                    NavigationDrawerItem(
                        label = { Text(if (user.role == UserRole.ADMIN) "Admin Panel" else "Staff Panel") },
                        selected = false,
                        onClick = { 
                            navController.navigate("admin")
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Security, null) }
                    )
                }
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
                TopAppBar(
                    title = { Text("RealState") },
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
            },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp)),
                        color = MaterialTheme.colorScheme.surface
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
                startDestination = "home",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) {
                composable("home") {
                    HomeScreen(onPropertyClick = onNavigateToDetail)
                }
                composable("admin") {
                    AdminPanelScreen(user.role)
                }
                composable("orders") {
                    OrdersScreen(onPropertyClick = onNavigateToDetail)
                }
                composable("profile") {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
fun AdminPanelScreen(role: UserRole) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            if (role == UserRole.ADMIN) "Admin Dashboard" else "Sub-Admin Dashboard", 
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val stats = if (role == UserRole.ADMIN) {
            listOf("Total Users" to "1,240", "Revenue" to "$45k", "Listings" to "450")
        } else {
            listOf("My Listings" to "12", "Leads" to "45", "Tasks" to "3")
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            stats.forEach { (label, value) ->
                Card(modifier = Modifier.weight(1f).padding(4.dp)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Activity", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(10) { i ->
                ListItem(
                    headlineContent = { Text("Update: Property Listing #10$i") },
                    supportingContent = { Text("${i + 1} hours ago") },
                    leadingContent = { Icon(Icons.Default.Info, null) }
                )
                HorizontalDivider()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
