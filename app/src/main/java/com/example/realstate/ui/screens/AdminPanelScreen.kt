package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.realstate.ui.components.MultiPinMapCard
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.ui.components.ReviewItem
import com.example.realstate.ui.viewmodels.AdminViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.location.Geocoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<com.example.realstate.data.Property?>(null) }
    var userToEdit by remember { mutableStateOf<com.example.realstate.data.model.UserDto?>(null) }

    if (showAddDialog) {
        PropertyFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, img, loc, price, type ->
                viewModel.addProperty(title, desc, img, loc, price, type)
                showAddDialog = false
            }
        )
    }

    propertyToEdit?.let { prop ->
        PropertyFormDialog(
            initialProperty = prop,
            onDismiss = { propertyToEdit = null },
            onConfirm = { title, desc, img, loc, price, type ->
                viewModel.updateProperty(prop.id, title, desc, img, loc, price, type)
                propertyToEdit = null
            }
        )
    }

    userToEdit?.let { user ->
        UserEditDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onConfirm = { name, img, contact, addr ->
                viewModel.updateUser(user.id ?: "", name, img, contact, addr)
                userToEdit = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.properties.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("System Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Stats Row 1
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AdminStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Agents",
                                value = uiState.totalUsers.toString(),
                                icon = Icons.Default.Group,
                                color = if (uiState.selectedFilter == "Agents") MaterialTheme.colorScheme.primary else Color(0xFF4CAF50),
                                onClick = { viewModel.setFilter(if (uiState.selectedFilter == "Agents") "All" else "Agents") }
                            )
                            AdminStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Revenue",
                                value = String.format(Locale.US, "$%.1fK", uiState.totalRevenue / 1000),
                                icon = Icons.Default.AttachMoney,
                                color = if (uiState.selectedFilter == "Revenue") MaterialTheme.colorScheme.primary else Color(0xFF2196F3),
                                onClick = { viewModel.setFilter(if (uiState.selectedFilter == "Revenue") "All" else "Revenue") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Dynamic Stats Row 2
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AdminStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Active Listings",
                                value = uiState.properties.size.toString(),
                                icon = Icons.Default.Home,
                                color = if (uiState.selectedFilter == "Active") MaterialTheme.colorScheme.primary else Color(0xFFFF9800),
                                onClick = { viewModel.setFilter(if (uiState.selectedFilter == "Active") "All" else "Active") }
                            )
                            AdminStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Pending Sales",
                                value = uiState.pendingApprovals.toString(),
                                icon = Icons.Default.PendingActions,
                                color = if (uiState.selectedFilter == "Pending") MaterialTheme.colorScheme.primary else Color(0xFFF44336),
                                onClick = { viewModel.setFilter(if (uiState.selectedFilter == "Pending") "All" else "Pending") }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            val context = LocalContext.current
                            val geocoder = remember { android.location.Geocoder(context) }
                            val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }
                            
                            LaunchedEffect(uiState.properties) {
                                markers.clear()
                                uiState.properties.forEach { property ->
                                    try {
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocationName(property.location, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            markers.add(LatLng(addresses[0].latitude, addresses[0].longitude) to property.title)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            MultiPinMapCard(
                                markers = markers,
                                height = 300.dp,
                                sectionLabel = "Property Distribution",
                                isLoading = uiState.isLoading && markers.isEmpty()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Property Inventory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState.properties.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No properties found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    val filteredProperties = when (uiState.selectedFilter) {
                        "Active" -> uiState.properties.filter { it.isVerified }
                        "Pending" -> uiState.properties.filter { !it.isVerified }
                        else -> uiState.properties
                    }

                    itemsIndexed(filteredProperties) { index, property ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50)),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            PropertyManagementCard(
                                property = property,
                                isAdmin = true,
                                onClick = { onNavigateToDetail(property.id) },
                                onApprove = { viewModel.approveProperty(property.id) },
                                onReject = { viewModel.deleteProperty(property.id) },
                                onEdit = { propertyToEdit = property },
                                onDelete = { viewModel.deleteProperty(property.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("User & Agent Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    itemsIndexed(uiState.users) { index, user ->
                         AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50)),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            UserManagementCard(
                                user = user,
                                onClick = { onNavigateToUserDetail(user.id ?: "") },
                                onEdit = { userToEdit = user },
                                onDelete = { viewModel.deleteUser(user.id ?: "") },
                                onToggleStatus = { viewModel.toggleUserStatus(user.id ?: "", user.status ?: "active") }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Agent Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Agent Section
                    item {
                        SectionHeader("Agent Management", Icons.Default.SupportAgent)
                    }
                    if (uiState.agents.isEmpty()) {
                        item {
                            Text("No agents found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
                        }
                    }
                    itemsIndexed(uiState.agents) { index, agent ->
                         AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50)),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            AgentManagementCard(
                                agent = agent,
                                onClick = { onNavigateToUserDetail(agent.userId) },
                                onVerify = { viewModel.verifyAgent(agent.id) },
                                onMarkFraud = { viewModel.markAgentFraud(agent.id) },
                                onDelete = { viewModel.deleteAgent(agent.id) }
                            )
                        }
                    }

                    // Sold Properties Section
                    item {
                        SectionHeader("All Sold Properties", Icons.Default.ShoppingBag)
                    }
                    if (uiState.soldProperties.isEmpty()) {
                        item {
                            Text("No properties sold yet", modifier = Modifier.padding(24.dp), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        itemsIndexed(uiState.soldProperties) { index, soldProp ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50)),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                SoldPropertyCard(
                                    soldProp = soldProp,
                                    onClick = { /* Show details dialog if needed */ }
                                )
                            }
                        }
                    }

                    // Reviews Section
                    item {
                        SectionHeader("All Reviews", Icons.Default.RateReview)
                        if (uiState.reviews.isEmpty()) {
                            Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Text("No reviews found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    itemsIndexed(uiState.reviews) { index, review ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50)),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            ReviewItem(
                                review = review,
                                isCurrentUser = true, // Force true to show delete button for admin
                                onDelete = { viewModel.deleteReview(review.id) },
                                onEdit = { /* Admin might not need to edit, but can delete */ }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.refreshDashboard() }) {
                            Text("Retry", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(uiState.error ?: "An error occurred")
                }
            }
        }
    }
}

@Composable
fun PropertyManagementCard(
    property: com.example.realstate.data.Property,
    isAdmin: Boolean = true,
    onClick: () -> Unit,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${property.category} • ${property.location}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        property.price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!property.isVerified) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)) {
                            Text("Pending", fontSize = 10.sp, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin && !property.isVerified) {
                    IconButton(onClick = onApprove, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onReject, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                } else {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMapOverview(properties: List<com.example.realstate.data.Property>) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context) }
    val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }
    
    LaunchedEffect(properties) {
        markers.clear()
        properties.forEach { property ->
            try {
                val addresses = geocoder.getFromLocationName(property.location, 1)
                if (!addresses.isNullOrEmpty()) {
                    markers.add(LatLng(addresses[0].latitude, addresses[0].longitude) to property.title)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text("Property Distribution", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(23.6850, 90.3563), 7f) // Center of Bangladesh
            }
            
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                markers.forEach { (latLng, title) ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = title
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun UserManagementCard(
    user: com.example.realstate.data.model.UserDto, 
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val userImg = if (user.image?.startsWith("data:") == true) "https://i.pravatar.cc/150" else user.image ?: "https://i.pravatar.cc/150"
            AsyncImage(
                model = userImg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name ?: "Unknown User", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(user.email ?: "No Email", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val isActive = user.status?.lowercase() == "active"
                Text(
                    text = "Status: ${user.status?.uppercase() ?: "ACTIVE"}",
                    fontSize = 11.sp,
                    color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
                
                Surface(
                    color = when(user.role?.uppercase()) {
                        "ADMIN" -> Color(0xFFE91E63)
                        "AGENT" -> Color(0xFF2196F3)
                        else -> Color(0xFF4CAF50)
                    }.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = user.role?.uppercase() ?: "USER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(user.role?.uppercase()) {
                            "ADMIN" -> Color(0xFFE91E63)
                            "AGENT" -> Color(0xFF2196F3)
                            else -> Color(0xFF4CAF50)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                
                TextButton(
                    onClick = onToggleStatus,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    val isActive = user.status?.lowercase() == "active"
                    Text(
                        if (isActive) "Suspend" else "Activate",
                        color = if (isActive) Color(0xFFF44336) else Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: com.example.realstate.data.model.UserDto,
    onDismiss: () -> Unit,
    onConfirm: (name: String, image: String, contactNumber: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var image by remember { mutableStateOf(user.image ?: "") }
    var contactNumber by remember { mutableStateOf(user.contactNumber ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                Text("Image processing not implemented in this dialog", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, image, contactNumber, address) }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AgentManagementCard(
    agent: com.example.realstate.data.model.AgentDetailDto,
    onClick: () -> Unit,
    onVerify: () -> Unit,
    onMarkFraud: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val user = agent.user
            val userImg = if (user?.image?.startsWith("data:") == true) "https://i.pravatar.cc/150" else user?.image ?: "https://i.pravatar.cc/150"
            AsyncImage(
                model = userImg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user?.name ?: "Unknown Agent", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(user?.email ?: "No Email", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (agent.isVerified == true) {
                        Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("Verified", fontSize = 10.sp, color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (agent.isFraud == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(color = Color(0xFFF44336).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("Fraud", fontSize = 10.sp, color = Color(0xFFF44336), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (agent.isVerified != true) {
                        IconButton(onClick = onVerify, modifier = Modifier.size(32.dp)) {
                             Icon(Icons.Default.CheckCircle, "Verify", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (agent.isFraud != true) {
                        IconButton(onClick = onMarkFraud, modifier = Modifier.size(32.dp)) {
                             Icon(Icons.Default.TrendingDown, "Fraud", tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SoldPropertyCard(
    soldProp: com.example.realstate.data.model.SoldPropertyDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = soldProp.property?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(soldProp.property?.title ?: "Unknown Property", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Sold for: ${soldProp.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoSmall(label = "Buyer", value = soldProp.user?.name ?: "Unknown")
                InfoSmall(label = "Agent ID", value = (soldProp.agent?.userId ?: soldProp.agentId).take(8))
                InfoSmall(label = "Date", value = soldProp.soldAt?.take(10) ?: "N/A")
            }
        }
    }
}

@Composable
fun InfoSmall(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}



