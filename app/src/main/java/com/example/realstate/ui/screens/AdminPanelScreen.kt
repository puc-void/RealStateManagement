package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.model.*
import com.example.realstate.ui.components.MultiPinMapCard
import com.example.realstate.ui.components.ReviewItem
import com.example.realstate.ui.viewmodels.AdminViewModel
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<com.example.realstate.data.Property?>(null) }
    var userToEdit by remember { mutableStateOf<UserDto?>(null) }

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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("System Overview", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Admin Control", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        }
                        IconButton(
                            onClick = { viewModel.refreshDashboard() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Revenue",
                        value = String.format(Locale.US, "$%.1fK", uiState.totalRevenue / 1000),
                        icon = Icons.Default.Payments,
                        gradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                    )
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Agents",
                        value = uiState.agents.size.toString(),
                        icon = Icons.Default.SupportAgent,
                        gradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Properties",
                        value = uiState.properties.size.toString(),
                        icon = Icons.Default.HomeWork,
                        gradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                    )
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Users",
                        value = uiState.users.size.toString(),
                        icon = Icons.Default.Group,
                        gradient = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)))
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    "Property Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                        height = 200.dp,
                        sectionLabel = "",
                        isLoading = uiState.isLoading && markers.isEmpty()
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    "Manage Inventory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(uiState.properties, key = { _, prop -> "admin_prop_${prop.id}" }) { index, property ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(600, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(600, delayMillis = index * 50))
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
                Text(
                    "User Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(uiState.users, key = { _, user -> "admin_user_${user.id}" }) { index, user ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 20 })
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

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: Brush) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.background(gradient).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                Column {
                    Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(property.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${property.category} • ${property.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(property.price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row {
                if (isAdmin && !property.isVerified) {
                    IconButton(onClick = onApprove) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                    }
                    IconButton(onClick = onReject) {
                        Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementCard(
    user: UserDto, 
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(user.name?.take(1)?.uppercase() ?: "U", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(user.email ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                color = if (user.status?.lowercase() == "active") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF43F5E).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    user.status?.uppercase() ?: "ACTIVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (user.status?.lowercase() == "active") Color(0xFF10B981) else Color(0xFFF43F5E)
                )
            }
            IconButton(onClick = onToggleStatus) {
                Icon(if (user.status?.lowercase() == "active") Icons.Default.Block else Icons.Default.Check, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InfoSmall(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: UserDto,
    onDismiss: () -> Unit,
    onConfirm: (name: String, image: String, contactNumber: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var contactNumber by remember { mutableStateOf(user.contactNumber ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Profile", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, user.image ?: "", contactNumber, address) }, shape = RoundedCornerShape(12.dp)) {
                Text("Save Changes")
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



