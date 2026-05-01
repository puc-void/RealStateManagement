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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.realstate.data.MockData
import com.example.realstate.data.model.BookedPropertyDto
import com.example.realstate.ui.components.MultiPinMapCard
import com.example.realstate.ui.viewmodels.AgentViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: AgentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.notification) {
        uiState.notification?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val agentName = MockData.currentUser.name
    
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.refreshDashboard()
            delay(30000)
        }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<com.example.realstate.data.Property?>(null) }
    var bookingToShowDetail by remember { mutableStateOf<BookedPropertyDto?>(null) }

    if (showAddDialog) {
        PropertyFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, img, loc, price, type ->
                viewModel.addProperty(title, desc, img, loc, price, type)
                showAddDialog = false
            }
        )
    }

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
                            Text("Professional", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Agent Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        }
                        IconButton(
                            onClick = { /* Settings */ },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    agentName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Verified Agent", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Text(agentName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
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
                    AgentStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Listings",
                        value = uiState.properties.size,
                        icon = Icons.Default.HomeWork,
                        gradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7)))
                    )
                    AgentStatCard(
                        modifier = Modifier.weight(1f),
                        title = "New Requests",
                        value = uiState.bookings.size,
                        icon = Icons.Default.NotificationsActive,
                        gradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    "My Properties Map",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Managed Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    TextButton(onClick = { viewModel.refreshDashboard() }) {
                        Text("Refresh", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(uiState.properties, key = { _, prop -> "agent_prop_${prop.id}" }) { index, property ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(600, delayMillis = index * 100)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(600, delayMillis = index * 100))
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

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Booking Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.bookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No pending requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            itemsIndexed(uiState.bookings, key = { _, book -> "agent_book_${book.id}" }) { index, booking ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(600, delayMillis = index * 100)) + slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(600, delayMillis = index * 100))
                ) {
                    AgentBookingItem(
                        booking = booking,
                        onClick = { bookingToShowDetail = booking },
                        onAccept = { viewModel.confirmBooking(booking.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}

@Composable
fun AgentStatCard(modifier: Modifier = Modifier, title: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: Brush) {
    var count by remember { mutableStateOf(0) }
    LaunchedEffect(value) { count = value }
    val animatedCount by animateIntAsState(targetValue = count, animationSpec = tween(1500), label = "countAnim")

    Surface(
        modifier = modifier
            .height(140.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.background(gradient).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Column {
                    Text(animatedCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AgentBookingItem(
    booking: BookedPropertyDto, 
    onClick: () -> Unit,
    onAccept: () -> Unit
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.user?.name ?: "Potential Client", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(
                    "Offer: ${booking.proposedAmount}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    booking.property?.title ?: "Listing",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!booking.isSold) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                }
            } else {
                Icon(Icons.Default.Verified, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
            }
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
    booking: BookedPropertyDto,
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

@Composable
fun WishlistActivityItem(item: com.example.realstate.data.model.WishlistItemDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Potential interest in", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.property?.title ?: "Your Property", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Added to a user's wishlist", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
