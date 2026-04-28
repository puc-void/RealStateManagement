package com.example.realstate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.ui.theme.GlassDark
import com.example.realstate.ui.viewmodels.AuthState
import com.example.realstate.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    userId: String,
    role: String,
    onVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.state.collectAsState()
    var otp by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    LaunchedEffect(authState) {
        when (val s = authState) {
            is AuthState.VerifySuccess -> {
                showSuccess = true
                delay(1200) // brief success flash
                onVerified()
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                errorMsg = s.message
                otp = "" // clear on error so user retypes
            }
            else -> {}
        }
    }

    val isLoading = authState is AuthState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=1000&q=80",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(600))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Back button
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { authViewModel.resetState(); onNavigateBack() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassDark)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Email icon
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Verify Your Email",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "We sent a 6-digit OTP to your email.\nEnter it below to activate your account.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(32.dp))

                // OTP Input Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassDark)
                        .padding(28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Error
                        AnimatedVisibility(visible = errorMsg != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { errorMsg = null }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }
                        }

                        // Success animation
                        AnimatedVisibility(visible = showSuccess) {
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Email verified! Redirecting…", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // OTP Field — large centered
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it },
                            label = { Text("6-Digit OTP Code", color = Color.LightGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 8.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        // Verify button
                        Button(
                            onClick = {
                                errorMsg = null
                                authViewModel.verifyEmail(userId, otp, role)
                            },
                            enabled = otp.length == 6 && !isLoading && !showSuccess,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Verify Email", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TextButton(onClick = { otp = ""; errorMsg = null }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
                            Spacer(Modifier.width(4.dp))
                            Text("Didn't get the email? Check spam folder", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
