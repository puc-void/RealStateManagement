package com.example.realstate.ui.screens

import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.ui.components.MultiPinMapCard
import com.example.realstate.ui.components.ShimmerBox
import com.example.realstate.ui.viewmodels.HomeViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPropertyClick: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    var isMapView by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context) }
    val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }

    LaunchedEffect(uiState.filteredProperties) {
        markers.clear()
        uiState.filteredProperties.forEach { property ->
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(property.location, 1)
                if (!addresses.isNullOrEmpty()) {
                    markers.add(LatLng(addresses[0].latitude, addresses[0].longitude) to property.title)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Current Location", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Beverly Hills, CA", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isMapView = !isMapView },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                            contentDescription = "Toggle Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { /* TODO: Profile */ }) {
                        AsyncImage(
                            model = MockData.currentUser.profilePicUrl,
                            contentDescription = "Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .shadow(2.dp, CircleShape)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Welcome to\nNestora",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { homeViewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        placeholder = { Text("Search city, neighborhood...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                            .clickable { /* Toggle Filter Sheet */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                    }
                }
            }

            if (isMapView) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        MultiPinMapCard(
                            markers = markers,
                            height = 500.dp,
                            sectionLabel = "Property Locations",
                            defaultZoom = 4f,
                            isLoading = uiState.isLoading
                        )
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Featured Estates",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "See all",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoading) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(3) {
                                ShimmerBox(modifier = Modifier.width(280.dp).height(320.dp), shape = RoundedCornerShape(24.dp))
                            }
                        }
                    } else {
                        val featuredProps = uiState.filteredProperties.take(3)
                        if (featuredProps.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(featuredProps) { property ->
                                    FeaturedPropertyCard(property = property, onClick = { onPropertyClick(property.id) })
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Recommended for you",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLoading) {
                    items(5) {
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth().height(110.dp).padding(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                } else {
                    itemsIndexed(uiState.filteredProperties) { index, property ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = index * 100)) + 
                                    slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(durationMillis = 500, delayMillis = index * 100))
                        ) {
                            PropertyListItem(property = property, onClick = { onPropertyClick(property.id) })
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FeaturedPropertyCard(property: Property, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scaleAnim")

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(320.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = property.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(property.category, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(property.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(property.location, fontSize = 14.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(property.price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PropertyListItem(property: Property, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scaleAnimListItem")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = property.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(property.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(property.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val ctx = LocalContext.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val encoded = java.net.URLEncoder.encode(property.location, "UTF-8")
                            val mapsIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("geo:0,0?q=$encoded")
                            ).apply { setPackage("com.google.android.apps.maps") }
                            try {
                                ctx.startActivity(mapsIntent)
                            } catch (e: Exception) {
                                ctx.startActivity(android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
                                ))
                            }
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(property.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(property.price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp)) // Using dummy icon for bed/bath if no custom vector
                        Text(" ${property.beds}B ${property.baths}B", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

