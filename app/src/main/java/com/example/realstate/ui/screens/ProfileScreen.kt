package com.example.realstate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import com.example.realstate.ui.components.NestoraButton
import com.example.realstate.ui.components.ReviewItem
import com.example.realstate.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var user by remember(MockData.currentUser) { mutableStateOf(MockData.currentUser) }
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }
    
    var editName by remember { mutableStateOf(user.name) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editPhone by remember { mutableStateOf(user.phone) }
    var editLocation by remember { mutableStateOf(user.location) }
    
    var showReviewEditDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<com.example.realstate.data.model.ReviewDto?>(null) }

    if (showReviewEditDialog && reviewToEdit != null) {
        ReviewSubmissionDialog(
            initialRating = reviewToEdit!!.rating,
            initialDescription = reviewToEdit!!.description,
            onDismiss = {
                showReviewEditDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating, description ->
                viewModel.updateReview(reviewToEdit!!.id, rating, description)
                showReviewEditDialog = false
                reviewToEdit = null
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(editName, user.profilePicUrl, editPhone, editLocation)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Profile Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                )

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = user.profilePicUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            IconButton(
                                onClick = { /* TODO: Change Pic */ },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val roleColor = when(user.role) {
                                UserRole.ADMIN -> Color(0xFFE91E63)
                                UserRole.AGENT -> Color(0xFF2196F3)
                                UserRole.USER -> Color(0xFF4CAF50)
                            }
                            Surface(
                                color = roleColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = user.role.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = roleColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Info Section
            InfoCard(user, onLocationClick = { showEditDialog = true })

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Sections
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        ProfileOption(icon = Icons.Default.Person, title = "Edit Profile", onClick = { showEditDialog = true })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileOption(icon = Icons.Default.Notifications, title = "Notifications")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileOption(icon = Icons.Default.Lock, title = "Privacy & Security")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                NestoraButton(
                    text = "Logout",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = listOf(Color(0xFFE91E63), Color(0xFFF44336)),
                    textColor = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.wishlist.isNotEmpty()) {
                    Text("My Wishlist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.wishlist) { property ->
                            WishlistCard(
                                property = property, 
                                onClick = { onNavigateToDetail(property.id) },
                                onRemove = { viewModel.removeFromWishlist(property.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (uiState.reviews.isNotEmpty()) {
                    Text("My Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.reviews.forEach { review ->
                        ReviewItem(
                            review = review,
                            isCurrentUser = review.userId == uiState.userId,
                            onEdit = {
                                reviewToEdit = review
                                showReviewEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteReview(review.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun WishlistCard(property: com.example.realstate.data.Property, onClick: () -> Unit, onRemove: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .width(200.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = property.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Remove from Wishlist",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(property.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(property.price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun InfoCard(user: com.example.realstate.data.User, onLocationClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(icon = Icons.Default.Phone, label = "Phone", value = user.phone)
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                icon = Icons.Default.LocationOn, 
                label = "Location", 
                value = user.location,
                isClickable = true,
                onClick = onLocationClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(icon = Icons.Default.DateRange, label = "Joined", value = user.joinDate)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, isClickable: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().then(if (isClickable) Modifier.clickable { onClick() } else Modifier)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        if (isClickable) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}
