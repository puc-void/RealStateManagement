package com.example.realstate.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.ui.components.NestoraButton
import com.example.realstate.ui.components.ReviewItem
import com.example.realstate.data.UserRole
import com.example.realstate.data.model.ReviewDto
import com.example.realstate.ui.viewmodels.DetailViewModel
import com.example.realstate.ui.theme.GlassDark
import com.example.realstate.ui.components.PropertyLocationMap
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    propertyId: String?,
    onBackClick: () -> Unit,
    detailViewModel: DetailViewModel = viewModel()
) {
    val uiState by detailViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            detailViewModel.loadProperty(propertyId)
        }
    }

    val property = uiState.property
    var showReviewDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<ReviewDto?>(null) }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    if (showReviewDialog && property != null) {
        ReviewSubmissionDialog(
            initialRating = reviewToEdit?.rating ?: 5,
            initialDescription = reviewToEdit?.description ?: "",
            onDismiss = { 
                showReviewDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating, comment ->
                if (reviewToEdit != null) {
                    detailViewModel.updateReview(reviewToEdit!!.id, rating, comment)
                } else {
                    detailViewModel.addReview(property.id.toInt(), rating, comment)
                }
                showReviewDialog = false
                reviewToEdit = null
            }
        )
    }

    if (showBookingDialog && property != null) {
        BookingSubmissionDialog(
            propertyTitle = property.title,
            onDismiss = { showBookingDialog = false },
            onSubmit = { amount ->
                detailViewModel.bookProperty(property.id.toInt(), property.agentId, amount)
                Toast.makeText(context, "Booking request sent! Agent notified.", Toast.LENGTH_LONG).show()
                showBookingDialog = false
            }
        )
    }

    val scrollState = rememberScrollState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (property == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.error ?: "Property not found", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (uiState.userRole != UserRole.AGENT) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 24.dp,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Price", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Text(property.price, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            NestoraButton(
                                text = "Book Now",
                                onClick = { showBookingDialog = true },
                                modifier = Modifier.width(180.dp).height(55.dp),
                                colors = listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Parallax Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .graphicsLayer {
                            translationY = scrollState.value * 0.4f // Parallax effect
                        }
                ) {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    )
                }

                // Details Section (overlaps header slightly)
                Column(
                    modifier = Modifier
                        .offset(y = (-40).dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp)
                ) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 30 })
                    ) {
                        Column {
                            Text(
                                text = property.title,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Tappable location chip — opens Google Maps
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.clickable {
                                    val encodedAddress = URLEncoder.encode(property.location, "UTF-8")
                                    val geoUri = Uri.parse("geo:0,0?q=$encodedAddress")
                                    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    val fallbackIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
                                    )
                                    try {
                                        context.startActivity(mapsIntent)
                                    } catch (e: Exception) {
                                        context.startActivity(fallbackIntent)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        property.location,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "Open in Maps",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FeatureIcon(icon = Icons.Default.Bed, label = "${property.beds} Bed")
                        FeatureIcon(icon = Icons.Default.Bathtub, label = "${property.baths} Bath")
                        FeatureIcon(icon = Icons.Default.SquareFoot, label = property.area)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Agent Profile Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = property.agentPicUrl,
                            contentDescription = "Agent",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(property.agentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Property Agent", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = { /* TODO Contact Agent */ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary) 
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Description", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = property.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Amenities", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(property.amenities) { amenity ->
                            Chip(text = amenity)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PropertyLocationMap(location = property.location, title = property.title)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Reviews", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.reviews.isEmpty()) {
                        Text("No reviews yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        uiState.reviews.forEachIndexed { index, review ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(500, delayMillis = index * 50)) + slideInVertically(initialOffsetY = { 20 })
                            ) {
                                Column {
                                    ReviewItem(
                                        review = review,
                                        isCurrentUser = review.userId == uiState.userId,
                                        onEdit = {
                                            reviewToEdit = review
                                            showReviewDialog = true
                                        },
                                        onDelete = {
                                            detailViewModel.deleteReview(review.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    if (uiState.userRole == UserRole.USER) {
                        NestoraButton(
                            text = "Write a Review",
                            onClick = { 
                                reviewToEdit = null
                                showReviewDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(120.dp)) // Padding for bottom bar
                }
            }

            // Custom Top Navigation overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassDark)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(
                    onClick = { detailViewModel.toggleWishlist(property.id, property.agentId) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassDark)
                ) {
                    Icon(
                        if (uiState.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isWishlisted) Color(0xFFE91E63) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp))
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun BookingSubmissionDialog(propertyTitle: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book Property") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Requesting: $propertyTitle", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Proposed Amount (e.g. $2500)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(amount) }) {
                Text("Send Booking Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

