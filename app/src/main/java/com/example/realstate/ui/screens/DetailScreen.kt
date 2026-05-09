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
    


    if (showReviewDialog && property != null) {
        ReviewSubmissionDialog(
            initialRating = reviewToEdit?.rating ?: 5,
            initialDescription = reviewToEdit?.description ?: "",
            onDismiss = { 
                showReviewDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating: Int, comment: String ->
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
                showBookingDialog = false
            }
        )
    }

    val scrollState = rememberScrollState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    
    if (property == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.error ?: "Property details not available", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBackClick, shape = RoundedCornerShape(12.dp)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Parallax Header with Overlapping Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .graphicsLayer {
                            translationY = scrollState.value * 0.4f
                            alpha = 1f - (scrollState.value / 1200f).coerceIn(0f, 1f)
                        }
                ) {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                )
                            )
                    )
                }

                // Details Content
                Surface(
                    modifier = Modifier
                        .offset(y = (-50).dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Title & Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = property.category.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "VERIFIED",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = property.title,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = property.price,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (property.isBought) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = Color.Red.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "PROPERTY IS BOOKED",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Red
                                        )
                                    }
                                } else if (uiState.userRole == UserRole.USER) {
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val encodedAddress = URLEncoder.encode(property.location, "UTF-8")
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress")))
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(property.location, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Features Grid (Premium look)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PremiumFeature(icon = Icons.Default.Bed, value = property.beds.toString(), label = "Beds")
                            PremiumFeature(icon = Icons.Default.Bathtub, value = property.baths.toString(), label = "Baths")
                            PremiumFeature(icon = Icons.Default.SquareFoot, value = property.area, label = "Sqft")
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Agent Section
                        Text("Listing Agent", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = property.agentPicUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(property.agentName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text("Nestora Elite Agent", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = { /* Call */ },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                if (uiState.userRole == UserRole.USER) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { detailViewModel.reportAgentAsFraud(property.agentId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Report Agent as Fraud", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = property.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(16.dp))
                        PropertyLocationMap(location = property.location, title = property.title)

                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Client Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            if (uiState.userRole == UserRole.USER) {
                                TextButton(onClick = { showReviewDialog = true }) {
                                    Text("Add Review", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.reviews.isEmpty()) {
                            Text("Be the first to review this property", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            uiState.reviews.forEach { review ->
                                ReviewItem(
                                    review = review,
                                    isCurrentUser = review.userId == uiState.userId,
                                    onEdit = { reviewToEdit = review; showReviewDialog = true },
                                    onDelete = { detailViewModel.deleteReview(review.id) }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }

            // Top Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                }

                IconButton(
                    onClick = { detailViewModel.toggleWishlist(property.id, property.agentId) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        if (uiState.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (uiState.isWishlisted) Color.Red else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

