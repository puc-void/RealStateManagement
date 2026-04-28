package com.example.realstate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import com.example.realstate.ui.theme.GlassDark
import com.example.realstate.ui.components.NestoraButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // High-end real estate background
        AsyncImage(
            model = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient Overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = tween(800)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NESTORA",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Exclusive Real Estate",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Glassmorphic Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassDark)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = Color.LightGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color.LightGray)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.LightGray) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray)
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null, tint = Color.LightGray)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        NestoraButton(
                            text = "Log in as User",
                            onClick = { 
                                MockData.currentUser = MockData.users.find { it.name == "Mehedi Hasan" } ?: MockData.currentUser
                                onLoginSuccess() 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NestoraButton(
                            text = "Log in as Agent",
                            onClick = { 
                                MockData.currentUser = MockData.users.find { it.id == MockData.currentAgentId } ?: MockData.currentUser
                                onLoginSuccess() 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = listOf(Color(0xFF009688), Color(0xFF4CAF50))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NestoraButton(
                            text = "Log in as Admin",
                            onClick = { 
                                MockData.currentUser = MockData.users.find { it.role == UserRole.ADMIN } ?: MockData.currentUser
                                onLoginSuccess() 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "New to Nestora?", color = Color.LightGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create Account",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
