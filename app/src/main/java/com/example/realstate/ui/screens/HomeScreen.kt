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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.graphicsLayer
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

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Explore luxury", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("Perfect Home", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isMapView = !isMapView },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                            contentDescription = "Toggle Map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        AsyncImage(
                            model = MockData.currentUser.profilePicUrl,
                            contentDescription = "Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { homeViewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
                        placeholder = { Text("Search by location or name...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (isMapView) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        MultiPinMapCard(
                            markers = markers,
                            height = 550.dp,
                            sectionLabel = "Interactive Property Map",
                            defaultZoom = 5f,
                            isLoading = uiState.isLoading
                        )
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Featured Estates",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { /* See All */ }) {
                            Text("See all", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (uiState.isLoading) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(3) {
                                ShimmerBox(modifier = Modifier.width(300.dp).height(350.dp), shape = RoundedCornerShape(32.dp))
                            }
                        }
                    } else {
                        val featuredProps = uiState.filteredProperties.take(5)
                        if (featuredProps.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(featuredProps) { property ->
                                    val isWishlisted = uiState.wishlistedPropertyIds.contains(property.id)
                                    FeaturedPropertyCard(
                                        property = property, 
                                        isWishlisted = isWishlisted,
                                        onWishlistClick = { homeViewModel.toggleWishlist(property) },
                                        onClick = { onPropertyClick(property.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Recommended for you",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLoading) {
                    items(5) {
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 24.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                } else {
                    itemsIndexed(uiState.filteredProperties) { index, property ->
                        val isWishlisted = uiState.wishlistedPropertyIds.contains(property.id)
                        val infiniteTransition = rememberInfiniteTransition(label = "listAnim")
                        val slideAnim by infiniteTransition.animateFloat(
                            initialValue = 100f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "slide"
                        )

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(600, delayMillis = index * 80)) + 
                                    slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(600, delayMillis = index * 80))
                        ) {
                            PropertyListItem(
                                property = property, 
                                isWishlisted = isWishlisted,
                                onWishlistClick = { homeViewModel.toggleWishlist(property) },
                                onClick = { onPropertyClick(property.id) }
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun FeaturedPropertyCard(
    property: Property, 
    isWishlisted: Boolean,
    onWishlistClick: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "scaleAnim", animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))

    Card(
        modifier = Modifier
            .width(300.dp)
            .height(380.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = property.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Premium Blur Overlay for Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            // Heart Icon with smooth background
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(44.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f),
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = onWishlistClick) {
                    Icon(
                        if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Color(0xFFFF5252) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Price Tag
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = property.price,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Bottom Info Card with Glassmorphism feel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            property.title, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 18.sp, 
                            color = Color.White, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                            Text(" 4.9", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(property.location, fontSize = 13.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PropertyAmenitySmall(Icons.Default.Bed, "${property.beds} Beds")
                        PropertyAmenitySmall(Icons.Default.Bathtub, "${property.baths} Baths")
                        PropertyAmenitySmall(Icons.Default.SquareFoot, property.area)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyAmenitySmall(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PropertyListItem(
    property: Property, 
    isWishlisted: Boolean,
    onWishlistClick: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scaleAnimListItem")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = property.imageUrl,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                ) {
                    Text(
                        property.category, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f).height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            property.title, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 17.sp, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis, 
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onWishlistClick,
                            modifier = Modifier.size(24.dp).offset(x = 4.dp, y = (-4).dp)
                        ) {
                            Icon(
                                if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(property.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        property.price, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 18.sp
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SquareFoot, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text(" ${property.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

