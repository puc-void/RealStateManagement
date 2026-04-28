package com.example.realstate.ui.components

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

// ---------------------------------------------------------------------------
// Shimmer Effect
// ---------------------------------------------------------------------------
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    Box(modifier = modifier.clip(shape).background(brush))
}

// ---------------------------------------------------------------------------
// Single-pin Map Card (for property detail / agent map per-property)
// Shows an embedded Google Map + "Open in Google Maps" button
// ---------------------------------------------------------------------------
@Composable
fun PropertyLocationMap(
    location: String,
    title: String = "",
    height: Dp = 250.dp,
    sectionLabel: String = "Location",
    zoomLevel: Float = 15f
) {
    val context = LocalContext.current
    var latLng by remember(location) { mutableStateOf<LatLng?>(null) }
    var failed by remember(location) { mutableStateOf(false) }

    LaunchedEffect(location) {
        failed = false
        latLng = null
        try {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context).getFromLocationName(location, 1)
                if (!addresses.isNullOrEmpty()) {
                    latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                } else {
                    failed = true
                }
            }
        } catch (e: Exception) {
            failed = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            sectionLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            when {
                latLng != null -> {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng!!, zoomLevel)
                    }
                    LaunchedEffect(latLng) {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(latLng!!, zoomLevel),
                            durationMs = 800
                        )
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = latLng!!),
                            title = title.ifBlank { location }
                        )
                    }
                }
                failed -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Map unavailable for \"$location\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Locating $location…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // "Open in Google Maps" button — always shown
        Spacer(modifier = Modifier.height(12.dp))
        val openMapsLabel = if (latLng != null) "Open in Google Maps" else "Search \"$location\" in Maps"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val uri = if (latLng != null) {
                        "geo:${latLng!!.latitude},${latLng!!.longitude}?q=${URLEncoder.encode(title.ifBlank { location }, "UTF-8")}"
                    } else {
                        "geo:0,0?q=${URLEncoder.encode(location, "UTF-8")}"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query=${URLEncoder.encode(location, "UTF-8")}"
                    ))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(fallback)
                    }
                },
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    openMapsLabel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Multi-pin Map Card (for dashboard overviews)
// ---------------------------------------------------------------------------
@Composable
fun MultiPinMapCard(
    markers: List<Pair<LatLng, String>>,
    height: Dp = 280.dp,
    sectionLabel: String,
    defaultCenter: LatLng = LatLng(23.6850, 90.3563),
    defaultZoom: Float = 7f,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            sectionLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth().height(height),
                shape = RoundedCornerShape(24.dp)
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        if (markers.isNotEmpty()) markers.first().first else defaultCenter,
                        defaultZoom
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    )
                ) {
                    markers.forEach { (latLng, title) ->
                        Marker(
                            state = MarkerState(position = latLng),
                            title = title
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Location Picker Map — Agent picks location by tapping on the map
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    initialLocation: String = "",
    onDismiss: () -> Unit,
    onLocationPicked: (address: String, latLng: LatLng) -> Unit
) {
    val context = LocalContext.current
    val defaultCenter = LatLng(23.8103, 90.4125) // Dhaka, Bangladesh

    // Try to geocode the initial location string
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var pickedAddress by remember { mutableStateOf(initialLocation) }
    var isGeocoding by remember { mutableStateOf(false) }
    var instructionVisible by remember { mutableStateOf(true) }

    LaunchedEffect(initialLocation) {
        if (initialLocation.isNotBlank()) {
            isGeocoding = true
            try {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(context).getFromLocationName(initialLocation, 1)
                    if (!addresses.isNullOrEmpty()) {
                        pickedLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                        pickedAddress = addresses[0].getAddressLine(0) ?: initialLocation
                    }
                }
            } catch (_: Exception) {}
            isGeocoding = false
        }
    }

    val startCenter = pickedLatLng ?: defaultCenter
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startCenter, if (pickedLatLng != null) 14f else 6f)
    }

    LaunchedEffect(pickedLatLng) {
        pickedLatLng?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 600
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Pick Property Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Tap anywhere on the map",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false
                        ),
                        onMapClick = { latLng ->
                            pickedLatLng = latLng
                            instructionVisible = false
                            isGeocoding = true
                            // Reverse geocode on background thread
                        }
                    ) {
                        pickedLatLng?.let { ll ->
                            Marker(
                                state = MarkerState(position = ll),
                                title = pickedAddress.ifBlank { "Selected Location" }
                            )
                        }
                    }

                    // Instruction overlay
                    if (instructionVisible && pickedLatLng == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20.dp),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Tap the map to pin the property",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reverse-geocode whenever pickedLatLng changes
            LaunchedEffect(pickedLatLng) {
                val ll = pickedLatLng ?: return@LaunchedEffect
                isGeocoding = true
                try {
                    withContext(Dispatchers.IO) {
                        @Suppress("DEPRECATION")
                        val results = Geocoder(context).getFromLocation(ll.latitude, ll.longitude, 1)
                        if (!results.isNullOrEmpty()) {
                            pickedAddress = results[0].getAddressLine(0) ?: "${ll.latitude}, ${ll.longitude}"
                        } else {
                            pickedAddress = "${String.format("%.4f", ll.latitude)}, ${String.format("%.4f", ll.longitude)}"
                        }
                    }
                } catch (_: Exception) {
                    pickedAddress = "${String.format("%.4f", ll.latitude)}, ${String.format("%.4f", ll.longitude)}"
                }
                isGeocoding = false
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Picked address display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGeocoding) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (pickedLatLng != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (pickedLatLng == null && !isGeocoding) "No location selected yet"
                               else pickedAddress,
                        color = if (pickedLatLng != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (pickedLatLng != null) FontWeight.SemiBold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm button
            Button(
                onClick = {
                    pickedLatLng?.let { ll ->
                        onLocationPicked(pickedAddress, ll)
                    }
                },
                enabled = pickedLatLng != null && !isGeocoding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm This Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
