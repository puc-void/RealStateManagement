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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.realstate.data.MockData
import com.example.realstate.ui.components.MultiPinMapCard
import com.example.realstate.ui.viewmodels.AgentViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: AgentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(uiState.notification) {
        uiState.notification?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val agentName = MockData.users.find { it.id == MockData.currentAgentId }?.name ?: "Agent"
    
    // Simple polling for new bookings every 30s
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.refreshDashboard()
            kotlinx.coroutines.delay(30000)
        }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<com.example.realstate.data.Property?>(null) }

    if (showAddDialog) {
        PropertyFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, img, loc, price, type ->
                viewModel.addProperty(title, desc, img, loc, price, type)
                showAddDialog = false
            }
        )
    }
    var bookingToShowDetail by remember { mutableStateOf<com.example.realstate.data.model.BookedPropertyDto?>(null) }

    if (bookingToShowDetail != null) {
        BookingDetailDialog(
            booking = bookingToShowDetail!!,
            onDismiss = { bookingToShowDetail = null },
            onAccept = {
                viewModel.confirmBooking(bookingToShowDetail!!.id)
                bookingToShowDetail = null
            },
            onReject = {
                viewModel.rejectBooking(bookingToShowDetail!!.id)
                bookingToShowDetail = null
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
                title = { Text("Agent Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Welcome back,", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(agentName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AgentStatCard(
                        modifier = Modifier.weight(1f), 
                        title = "Active", 
                        value = uiState.properties.size, 
                        color = if (uiState.selectedFilter == "Active") MaterialTheme.colorScheme.primary else Color(0xFF3F51B5),
                        onClick = { 
                            viewModel.setFilter(if (uiState.selectedFilter == "Active") "All" else "Active")
                            viewModel.refreshDashboard()
                        }
                    )
                    AgentStatCard(
                        modifier = Modifier.weight(1f), 
                        title = "Pending", 
                        value = uiState.pendingApprovals, 
                        color = if (uiState.selectedFilter == "Pending") MaterialTheme.colorScheme.primary else Color(0xFFE91E63),
                        onClick = { 
                            viewModel.setFilter(if (uiState.selectedFilter == "Pending") "All" else "Pending")
                            viewModel.refreshDashboard()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AgentStatCard(
                        modifier = Modifier.weight(1f), 
                        title = "Sold", 
                        value = uiState.soldProperties, 
                        color = if (uiState.selectedFilter == "Sold") MaterialTheme.colorScheme.primary else Color(0xFF009688),
                        onClick = { 
                            viewModel.setFilter(if (uiState.selectedFilter == "Sold") "All" else "Sold")
                            viewModel.refreshDashboard()
                        }
                    )
                    AgentStatCard(
                        modifier = Modifier.weight(1f), 
                        title = "Requests", 
                        value = uiState.activeBookingsCount, 
                        color = Color(0xFFFF9800),
                        onClick = { 
                            scope.launch { listState.animateScrollToItem(uiState.properties.size + 4) }
                            viewModel.refreshDashboard()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Latest Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("View all", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                        height = 250.dp,
                        sectionLabel = "My Properties Map",
                        isLoading = uiState.isLoading && markers.isEmpty()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.isLoading && uiState.properties.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
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
                        isAdmin = false,
                        onClick = { onNavigateToDetail(property.id) },
                        onEdit = { propertyToEdit = property },
                        onDelete = { viewModel.deleteProperty(property.id) }
                    )
                }
            }

            val filteredBookings = when (uiState.selectedFilter) {
                "Sold" -> uiState.bookings.filter { it.isSold }
                else -> uiState.bookings // New bookings should always be shown to the agent
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Recent Bookings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                if (uiState.bookings.isEmpty()) {
                    Text(
                        "No recent bookings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(filteredBookings) { index, booking ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(500, delayMillis = index * 50))
                ) {
                    AgentBookingItem(
                        booking = booking,
                        onClick = { bookingToShowDetail = booking },
                        onAccept = { viewModel.confirmBooking(booking.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AgentStatCard(modifier: Modifier = Modifier, title: String, value: Int, color: Color, onClick: () -> Unit = {}) {
    var count by remember { mutableStateOf(0) }
    LaunchedEffect(value) { count = value }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(1500), label = ""
    )

    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(animatedCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun AgentBookingItem(
    booking: com.example.realstate.data.model.BookedPropertyDto, 
    onClick: () -> Unit,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.user?.name ?: "Anonymous", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Proposed: ${booking.proposedAmount} for ${booking.property?.title ?: "Property"}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!booking.isSold) {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Accept", fontSize = 12.sp)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = "Sold", tint = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
fun BookingDetailDialog(
    booking: com.example.realstate.data.model.BookedPropertyDto,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Booking Request Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(booking.user?.name ?: "Anonymous User", fontWeight = FontWeight.Bold)
                        Text(booking.user?.email ?: "No contact email", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                HorizontalDivider()
                
                Column {
                    Text("Property", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.property?.title ?: "Unknown Property", fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("Proposed Amount", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.proposedAmount, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                if (booking.isSold) {
                    Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("This booking has been confirmed and the property is sold.", color = Color(0xFF4CAF50), fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!booking.isSold) {
                Button(onClick = onAccept) {
                    Text("Accept & Mark as Sold")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = if (booking.isSold) onDismiss else onReject) {
                Text(if (booking.isSold) "Close" else "Reject")
            }
        }
    )
}

