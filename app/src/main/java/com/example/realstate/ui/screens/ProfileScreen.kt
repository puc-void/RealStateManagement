package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import com.example.realstate.ui.components.ReviewItem
import com.example.realstate.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToVerify: (userId: String, role: String, name: String, email: String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user by MockData.currentUserFlow.collectAsState()
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }
    
    var editName by remember { mutableStateOf(user.name) }
    var editPhone by remember { mutableStateOf(user.phone) }
    var editLocation by remember { mutableStateOf(user.location) }
    var editImageUrl by remember { mutableStateOf(user.profilePicUrl) }
    
    var showReviewEditDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<com.example.realstate.data.model.ReviewDto?>(null) }

    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var newEmailInput by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    
    var showUpdatePasswordDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // TODO: Replace with your actual ImgBB API key
    val imgBbApiKey = "ea1875998a87f632563615dc9b7d7eef" 

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    val url = viewModel.uploadImageToImgBB(context, it, imgBbApiKey)
                    if (url != null) {
                        editImageUrl = url
                        viewModel.updateProfile(editName, url, editPhone, editLocation)
                    }
                }
            }
        }
    )

    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            showChangeEmailDialog = false
            showOtpDialog = true
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text("Change Email", fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = newEmailInput,
                    onValueChange = { newEmailInput = it },
                    label = { Text("New Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.changeEmail(newEmailInput) }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Verify New Email", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("6-digit OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextButton(
                        onClick = { viewModel.resendOtp(newEmailInput) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !uiState.isLoading
                    ) {
                        Text("Resend OTP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.verifyNewEmail(otpInput) 
                    showOtpDialog = false
                }) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUpdatePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showUpdatePasswordDialog = false },
            title = { Text("Update Password", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.updatePassword(currentPasswordInput, newPasswordInput)
                    showUpdatePasswordDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdatePasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showReviewEditDialog && reviewToEdit != null) {
        ReviewSubmissionDialog(
            initialRating = reviewToEdit!!.rating,
            initialDescription = reviewToEdit!!.description,
            onDismiss = {
                showReviewEditDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating: Int, description: String ->
                viewModel.updateReview(reviewToEdit!!.id, rating, description)
                showReviewEditDialog = false
                reviewToEdit = null
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Profile", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editImageUrl, 
                            onValueChange = { editImageUrl = it }, 
                            label = { Text("Profile Image URL") }, 
                            modifier = Modifier.weight(1f), 
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                ) 
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Pick Image", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (uiState.isUploadingImage) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Uploading image...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateProfile(editName, editImageUrl, editPhone, editLocation)
                    showEditDialog = false
                }, shape = RoundedCornerShape(12.dp)) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Premium Header with Gradient and Glass Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = user.profilePicUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Surface(
                                modifier = Modifier.size(32.dp).clickable { 
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    ) 
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (uiState.isUploadingImage) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).clickable { showEditDialog = true })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (!user.isEmailVerified) {
                                Text(
                                    "Verify Now", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = MaterialTheme.colorScheme.error, 
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable { 
                                        viewModel.resendOtp(user.email)
                                        onNavigateToVerify(user.id, user.role.name, user.name, user.email)
                                    }
                                )
                            } else {
                                Text(
                                    "Change email", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = MaterialTheme.colorScheme.primary, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showChangeEmailDialog = true }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                user.role.name.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Personal Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoItem(icon = Icons.Default.Phone, label = "Phone", value = user.phone)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InfoItem(icon = Icons.Default.LocationOn, label = "Location", value = user.location)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InfoItem(icon = Icons.Default.CalendarToday, label = "Member Since", value = user.joinDate)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth().clickable { showUpdatePasswordDialog = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Update Password", fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Update Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Activity Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.wishlist.isNotEmpty()) {
                    Text("Saved Properties", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.wishlist) { property ->
                            ProfileWishlistCard(
                                property = property, 
                                onClick = { onNavigateToDetail(property.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (uiState.reviews.isNotEmpty()) {
                    Text("My Recent Reviews", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.reviews.take(3).forEach { review ->
                        ReviewItem(
                            review = review,
                            isCurrentUser = review.userId == uiState.userId,
                            onEdit = { reviewToEdit = review; showReviewEditDialog = true },
                            onDelete = { viewModel.deleteReview(review.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout from Account", fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun ProfileWishlistCard(property: com.example.realstate.data.Property, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(property.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                Text(property.price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

