package com.example.realstate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.ui.viewmodels.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val uiState by wishlistViewModel.uiState.collectAsState()
    
    LaunchedEffect(itemId) {
        wishlistViewModel.getWishlistItemDetails(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wishlist Item Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedItem != null) {
            val item = uiState.selectedItem!!
            val property = item.property
            val agent = item.agent

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                if (property != null) {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = property.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(property.location, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = property.priceRange,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Property Type: ${property.propertyType}", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(property.description)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Wishlist Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Added on: ${item.addedAt?.take(16)?.replace("T", " ") ?: "N/A"}")
                        
                        if (agent != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Agent Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (agent.isFraud == true) MaterialTheme.colorScheme.errorContainer 
                                                     else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (agent.isFraud == true) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (agent.isFraud == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Agent ID: ${agent.id.take(8)}...", fontWeight = FontWeight.Bold)
                                        if (agent.isFraud == true) {
                                            Text("WARNING: This agent is marked as fraudulent!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("Status: Verified Agent")
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { /* Could navigate to full property detail */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Full Property Details")
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Failed to load details")
            }
        }
    }
}
