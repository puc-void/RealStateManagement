package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.ui.viewmodels.OrderViewModel
import com.example.realstate.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = MockData.currentUser.id ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchOrders(userId)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Your Activity", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Bookings & Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (uiState.boughtProperties.isNotEmpty()) {
                        item {
                            Text("Purchased Estates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                        }
                        itemsIndexed(uiState.boughtProperties) { index, soldProp ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(600, delayMillis = index * 100)) + slideInHorizontally(initialOffsetX = { 50 })
                            ) {
                                BoughtPropertyCard(soldProp = soldProp, onClick = { onPropertyClick(soldProp.propertyId.toString()) })
                            }
                        }
                    }

                    if (uiState.bookedProperties.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Active Bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                        }
                        itemsIndexed(uiState.bookedProperties) { index, booking ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(600, delayMillis = index * 100)) + slideInHorizontally(initialOffsetX = { 50 })
                            ) {
                                ModernOrderCard(
                                    booking = booking, 
                                    onClick = { onPropertyClick(booking.propertyId.toString()) },
                                    onConfirm = { viewModel.confirmPurchase(booking) }
                                )
                            }
                        }
                    }

                    if (uiState.bookedProperties.isEmpty() && uiState.boughtProperties.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Assignment, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No history found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoughtPropertyCard(soldProp: SoldPropertyDto, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = soldProp.property?.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(soldProp.property?.title ?: "Unknown", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Price: ${soldProp.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Sold At: ${soldProp.soldAt?.take(10)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ModernOrderCard(
    booking: BookedPropertyDto, 
    onClick: () -> Unit,
    onConfirm: () -> Unit
) {
    val statusColor = if (booking.isPropAmountAccepted) Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusText = if (booking.isPropAmountAccepted) "Accepted" else "Pending Review"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = booking.property?.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.property?.title ?: "Unknown", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Offer: ${booking.proposedAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            statusText.uppercase(), 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = statusColor, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 10.sp
                        )
                    }
                    Text("Booked on: ${booking.bookedAt?.take(10)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                
                if (booking.isPropAmountAccepted) {
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Confirm Purchase", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(12.dp)) {
                        Text("Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

