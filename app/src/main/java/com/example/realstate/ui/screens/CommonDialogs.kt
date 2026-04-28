package com.example.realstate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realstate.ui.components.LocationPickerBottomSheet
import com.google.android.gms.maps.model.LatLng

@Composable
fun ReviewSubmissionDialog(
    initialRating: Int = 5,
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(initialRating) }
    var comment by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialDescription.isEmpty()) "Write a Review" else "Edit Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Rating: $rating / 5")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFormDialog(
    initialProperty: com.example.realstate.data.Property? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, imageUrl: String, location: String, priceRange: String, propertyType: String) -> Unit
) {
    var title by remember { mutableStateOf(initialProperty?.title ?: "") }
    var description by remember { mutableStateOf(initialProperty?.description ?: "") }
    var imageUrl by remember { mutableStateOf(initialProperty?.imageUrl ?: "") }
    var location by remember { mutableStateOf(initialProperty?.location ?: "") }
    var priceRange by remember { mutableStateOf(initialProperty?.price ?: "") }
    var propertyType by remember { mutableStateOf(initialProperty?.category ?: "HOUSE") }
    var showMapPicker by remember { mutableStateOf(false) }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }

    if (showMapPicker) {
        LocationPickerBottomSheet(
            initialLocation = location,
            onDismiss = { showMapPicker = false },
            onLocationPicked = { address, latLng ->
                location = address
                pickedLatLng = latLng
                showMapPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProperty == null) "Add Property" else "Edit Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Location with Map Picker ---
                Text(
                    "Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Address / City") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (pickedLatLng != null) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true
                    )
                    // Map picker button
                    FilledTonalIconButton(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Pick on Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (pickedLatLng != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "📍 Location pinned on map",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                // --- End location ---

                OutlinedTextField(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    label = { Text("Price Range") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = propertyType,
                    onValueChange = { propertyType = it },
                    label = { Text("Property Type (HOUSE/APARTMENT/COMMERCIAL)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(title, description, imageUrl, location, priceRange, propertyType)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
