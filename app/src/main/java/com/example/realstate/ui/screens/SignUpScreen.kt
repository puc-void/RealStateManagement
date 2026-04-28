package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.ui.theme.GlassDark
import com.example.realstate.ui.viewmodels.AuthState
import com.example.realstate.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: (userId: String, role: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") }
    var passwordVisible by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    LaunchedEffect(authState) {
        when (val s = authState) {
            is AuthState.SignUpSuccess -> {
                onSignUpSuccess(s.userId, s.role)
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                errorMsg = s.message
                if (s.message.contains("already registered", ignoreCase = true)) {
                    // Specific prompt for already registered emails
                    errorMsg = "This email is already in use. Please sign in or use a different email."
                }
            }
            else -> {}
        }
    }

    val isLoading = authState is AuthState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1000&q=80",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(700))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Brand header
                Text(
                    "NESTORA",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Create Your Account",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // Error banner
                AnimatedVisibility(visible = errorMsg != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                errorMsg ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { errorMsg = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Glassmorphic form card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(GlassDark)
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        // Name
                        AuthTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name *",
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text
                        )

                        // Email
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address *",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password * (min 6 chars)", color = Color.LightGray) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.LightGray) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        null,
                                        tint = Color.LightGray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = authFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Role Dropdown
                        Column {
                            Text("Account Type *", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                            ExposedDropdownMenuBox(
                                expanded = roleDropdownExpanded,
                                onExpandedChange = { roleDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = if (selectedRole == "USER") "🏠  User (Buyer/Renter)" else "🏢  Agent (Property Seller)",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                                    colors = authFieldColors(),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🏠  User (Buyer/Renter)") },
                                        onClick = { selectedRole = "USER"; roleDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🏢  Agent (Property Seller)") },
                                        onClick = { selectedRole = "AGENT"; roleDropdownExpanded = false }
                                    )
                                }
                            }
                        }

                        // Divider with optional label
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                            Text("  Optional  ", color = Color.LightGray.copy(alpha = 0.7f), fontSize = 11.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                        }

                        // Contact Number
                        AuthTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = "Contact Number",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone
                        )

                        // Address
                        AuthTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Address",
                            icon = Icons.Default.Home,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(Modifier.height(4.dp))

                        // Sign Up Button
                        Button(
                            onClick = {
                                errorMsg = null
                                authViewModel.signup(name, email, password, selectedRole, contactNumber, address)
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Already have account
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", color = Color.LightGray, fontSize = 15.sp)
                    Text(
                        "Sign In",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.LightGray) },
        singleLine = true,
        leadingIcon = { Icon(icon, null, tint = Color.LightGray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = authFieldColors(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = MaterialTheme.colorScheme.primary
)
