package com.example.realstate.ui.screens

import android.location.Geocoder
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.realstate.ui.viewmodels.AdminViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
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

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Properties", "Users", "Agents")

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Property")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tabs) { index, title ->
                    val isSelected = selectedTab == index
                    Surface(
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .animateContentSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = if (isSelected) 8.dp else 0.dp
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
            if (selectedTab == 0) {
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
                                Text("Welcome back, Admin", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Dashboard Insight", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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
                            value = String.format(java.util.Locale.US, "$%.1fK", uiState.totalRevenue / 1000),
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
                    AdminMapOverview(properties = uiState.properties)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (selectedTab == 1) {
                item {
                    Text(
                        "Manage Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
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
            }

            if (selectedTab == 2) {
                item {
                    Text(
                        "User Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
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
            }

            if (selectedTab == 3) {
                item {
                    Text(
                        "Agent Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                itemsIndexed(uiState.agents, key = { _, agent -> "admin_agent_${agent.id}" }) { index, agent ->
                    AgentManagementCard(
                        agent = agent,
                        agentProperties = uiState.properties.filter { it.agentId == agent.id },
                        onClick = { onNavigateToUserDetail(agent.userId) },
                        onVerify = { viewModel.verifyAgent(agent.id) },
                        onMarkFraud = { viewModel.toggleAgentFraud(agent.id, agent.isFraud ?: false) },
                        onDelete = { viewModel.deleteAgent(agent.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, gradient: Brush) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.background(gradient).padding(20.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = CircleShape
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.padding(10.dp).size(26.dp))
                    }
                }
                Column {
                    Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
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
            Box {
                AsyncImage(
                    model = property.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(14.dp))
                )
                if (property.isBought) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Red.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "BOOKED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
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
            Row {
                IconButton(onClick = onToggleStatus) {
                    Icon(if (user.status?.lowercase() == "active") Icons.Default.Block else Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
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
fun AdminMapOverview(properties: List<com.example.realstate.data.Property>) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context) }
    val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }
    
    LaunchedEffect(properties) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val newMarkers = mutableListOf<Pair<LatLng, String>>()
            properties.forEach { property ->
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(property.location, 1)
                    if (!addresses.isNullOrEmpty()) {
                        newMarkers.add(LatLng(addresses[0].latitude, addresses[0].longitude) to property.title)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                markers.clear()
                markers.addAll(newMarkers)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text("Property Distribution", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(23.6850, 90.3563), 7f)
            }
            
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
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
fun AgentManagementCard(
    agent: AgentDetailDto,
    agentProperties: List<com.example.realstate.data.Property>,
    onClick: () -> Unit,
    onVerify: () -> Unit,
    onMarkFraud: () -> Unit,
    onDelete: () -> Unit
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
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val user = agent.user
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user?.name?.take(1)?.uppercase() ?: "A", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user?.name ?: "Unknown Agent", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (agent.isVerified == true) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(user?.email ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (agent.isFraud == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "FRAUD",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Row {
                    if (agent.isVerified != true) {
                        IconButton(onClick = onVerify) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                        }
                    }
                    IconButton(onClick = onMarkFraud) {
                        Icon(
                            if (agent.isFraud == true) Icons.Default.GppGood else Icons.Default.GppBad,
                            null,
                            tint = if (agent.isFraud == true) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            AnimatedVisibility(visible = agent.isFraud == true) {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f)).padding(16.dp)) {
                    Text("Agent's Properties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (agentProperties.isEmpty()) {
                        Text("No properties found for this agent.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        agentProperties.forEach { prop ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HomeWork, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(prop.title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onMarkFraud,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Revert Agent to Normal")
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



