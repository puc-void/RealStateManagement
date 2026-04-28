package com.example.realstate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.realstate.ui.components.NestoraButton
import com.example.realstate.ui.viewmodels.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = MockData.currentUser.id ?: "" // In real app, get from auth

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchOrders(userId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text("My Activity", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (uiState.boughtProperties.isNotEmpty()) {
                    item {
                        Text("My Bought Properties", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.boughtProperties) { soldProp ->
                        BoughtPropertyCard(soldProp = soldProp, onClick = { onPropertyClick(soldProp.propertyId.toString()) })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                if (uiState.bookedProperties.isNotEmpty()) {
                    item {
                        Text("My Bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.bookedProperties) { booking ->
                        ModernOrderCard(
                            booking = booking, 
                            onClick = { onPropertyClick(booking.propertyId.toString()) },
                            onConfirm = { viewModel.confirmPurchase(booking) }
                        )
                    }
                }

                if (uiState.bookedProperties.isEmpty() && uiState.boughtProperties.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No activity yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoughtPropertyCard(soldProp: com.example.realstate.data.model.SoldPropertyDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = soldProp.property?.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(soldProp.property?.title ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Price Paid: ${soldProp.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Sold At: ${soldProp.soldAt?.take(10)}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("PURCHASED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ModernOrderCard(
    booking: com.example.realstate.data.model.BookedPropertyDto, 
    onClick: () -> Unit,
    onConfirm: () -> Unit
) {
    val statusColor = if (booking.isPropAmountAccepted) Color(0xFF4CAF50) else Color(0xFFFFB300)
    val statusText = if (booking.isPropAmountAccepted) "Price Accepted" else "Pending Review"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = booking.property?.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.property?.title ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Offered: ${booking.proposedAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Booked on: ${booking.bookedAt?.take(10)}", fontSize = 10.sp, color = Color.Gray)
                }
                
                if (booking.isPropAmountAccepted) {
                    NestoraButton(
                        text = "Buy Now",
                        onClick = onConfirm,
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                        textColor = Color.White
                    )
                } else {
                    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(12.dp)) {
                        Text("View Property")
                    }
                }
            }
        }
    }
}
