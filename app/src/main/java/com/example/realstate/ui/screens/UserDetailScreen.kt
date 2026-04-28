package com.example.realstate.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realstate.data.model.UserDto
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDto?>(null) }
    var agentInfo by remember { mutableStateOf<com.example.realstate.data.model.AgentDetailDto?>(null) }
    var agentProperties by remember { mutableStateOf<List<com.example.realstate.data.model.PropertyDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.userApi.getUserDetails(userId)
            if (response.success) {
                user = response.data
                if (user?.role?.uppercase() == "AGENT") {
                    // Try to find the agent record for this user
                    val agentsResponse = RetrofitClient.agentApi.getAllAgents()
                    if (agentsResponse.success) {
                        val agent = agentsResponse.data.find { it.userId == userId }
                        agentInfo = agent
                        if (agent != null) {
                            val propsResponse = RetrofitClient.propertyApi.getPropertiesByAgent(agent.id)
                            if (propsResponse.success) {
                                agentProperties = propsResponse.data
                            }
                        }
                    }
                }
            } else {
                error = response.message
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val userImg = if (user?.image?.startsWith("data:") == true) "https://i.pravatar.cc/150" else user?.image ?: "https://i.pravatar.cc/150"
                        AsyncImage(
                            model = userImg,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            color = if (user?.status?.lowercase() == "active") Color(0xFF4CAF50) else Color(0xFFF44336)
                        ) {
                            Icon(
                                imageVector = if (user?.status?.lowercase() == "active") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user?.name ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Surface(
                        color = when(user?.role?.uppercase()) {
                            "ADMIN" -> Color(0xFFE91E63)
                            "AGENT" -> Color(0xFF2196F3)
                            else -> Color(0xFF4CAF50)
                         }.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = user?.role?.uppercase() ?: "USER",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = when(user?.role?.uppercase()) {
                                "ADMIN" -> Color(0xFFE91E63)
                                "AGENT" -> Color(0xFF2196F3)
                                else -> Color(0xFF4CAF50)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    DetailItem(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "N/A")
                    DetailItem(icon = Icons.Default.Phone, label = "Contact Number", value = user?.contactNumber ?: "N/A")
                    DetailItem(icon = Icons.Default.LocationOn, label = "Address", value = user?.address ?: "N/A")
                    DetailItem(icon = Icons.Default.Verified, label = "Email Verified", value = if (user?.emailVerified == true) "Yes" else "No")
                    DetailItem(icon = Icons.Default.DateRange, label = "Joined At", value = user?.generatedAt?.take(10) ?: "N/A")

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    if (user?.role?.uppercase() == "AGENT") {
                         Spacer(modifier = Modifier.height(24.dp))
                         HorizontalDivider()
                         Spacer(modifier = Modifier.height(24.dp))
                         
                         Text("Agent Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                         Spacer(modifier = Modifier.height(16.dp))
                         
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                             Surface(
                                 modifier = Modifier.weight(1f),
                                 color = (if (agentInfo?.isVerified == true) Color(0xFF4CAF50) else Color(0xFFF44336)).copy(alpha = 0.1f),
                                 shape = RoundedCornerShape(12.dp)
                             ) {
                                 Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                     Icon(
                                         if (agentInfo?.isVerified == true) Icons.Default.VerifiedUser else Icons.Default.NewReleases,
                                         contentDescription = null,
                                         tint = if (agentInfo?.isVerified == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                                     )
                                     Text(if (agentInfo?.isVerified == true) "Verified" else "Not Verified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                 }
                             }
                             
                             Surface(
                                 modifier = Modifier.weight(1f),
                                 color = (if (agentInfo?.isFraud == true) Color(0xFFF44336) else Color(0xFF4CAF50)).copy(alpha = 0.1f),
                                 shape = RoundedCornerShape(12.dp)
                             ) {
                                 Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                     Icon(
                                         if (agentInfo?.isFraud == true) Icons.Default.ReportProblem else Icons.Default.Shield,
                                         contentDescription = null,
                                         tint = if (agentInfo?.isFraud == true) Color(0xFFF44336) else Color(0xFF4CAF50)
                                     )
                                     Text(if (agentInfo?.isFraud == true) "Fraud Flagged" else "Trustworthy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                 }
                             }
                         }
                         
                         Spacer(modifier = Modifier.height(24.dp))
                         Text("Listed Properties (${agentProperties.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                         Spacer(modifier = Modifier.height(16.dp))
                         
                         agentProperties.forEach { prop ->
                             Card(
                                 modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                 shape = RoundedCornerShape(12.dp)
                             ) {
                                 Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                     AsyncImage(
                                         model = prop.imageUrl,
                                         contentDescription = null,
                                         modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                         contentScale = ContentScale.Crop
                                     )
                                     Spacer(modifier = Modifier.width(12.dp))
                                     Column {
                                         Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                         Text(prop.priceRange, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                     }
                                 }
                             }
                         }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}
