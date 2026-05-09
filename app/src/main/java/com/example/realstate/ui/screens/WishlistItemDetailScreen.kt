package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import com.example.realstate.ui.viewmodels.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val uiState by wishlistViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(itemId) {
        wishlistViewModel.getWishlistItemDetails(itemId)
    }

    var showBookingDialog by remember { mutableStateOf(false) }

    if (showBookingDialog && uiState.selectedItem?.property != null) {
        BookingSubmissionDialog(
            propertyTitle = uiState.selectedItem!!.property!!.title,
            onDismiss = { showBookingDialog = false },
            onSubmit = { amount ->
                wishlistViewModel.bookProperty(
                    uiState.selectedItem!!.property!!.id,
                    uiState.selectedItem!!.agentId,
                    amount
                )
                showBookingDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.selectedItem != null) {
            val item = uiState.selectedItem!!
            val property = item.property
            val agent = item.agent

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (property != null) {
                        // Image Header with Parallax-ish feel
                        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                            AsyncImage(
                                model = property.imageUrl,
                                contentDescription = property.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                        }

                        // Content Surface
                        Surface(
                            modifier = Modifier
                                .offset(y = (-40).dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = property.propertyType.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "WISHLISTED",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = property.title,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = property.priceRange,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        if (MockData.currentUser.role == UserRole.USER) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Button(
                                                onClick = { showBookingDialog = true },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Reserve Now", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        property.location,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    property.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // Agent Info
                                if (agent != null) {
                                    Text("Listing Agent", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        color = if (agent.isFraud == true) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(50.dp),
                                                shape = CircleShape,
                                                color = if (agent.isFraud == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        if (agent.isFraud == true) Icons.Default.Warning else Icons.Default.Person,
                                                        null,
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text("Agent ID: ${agent.id.take(8)}", fontWeight = FontWeight.Bold)
                                                Text(
                                                    if (agent.isFraud == true) "Marked as Suspicious" else "Verified Real Estate Agent",
                                                    fontSize = 12.sp,
                                                    color = if (agent.isFraud == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }

                // Top Bar overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Item details not found", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

