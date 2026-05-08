package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class AgentUiState(
    val properties: List<Property> = emptyList(),
    val pendingApprovals: Int = 0,
    val soldProperties: Int = 0,
    val bookings: List<com.example.realstate.data.model.BookedPropertyDto> = emptyList(),
    val isLoading: Boolean = false,
    var error: String? = null,
    val activeBookingsCount: Int = 0,
    val selectedFilter: String = "All",
    val wishlistItems: List<com.example.realstate.data.model.WishlistItemDto> = emptyList(),
    val isUploadingImage: Boolean = false
)

class AgentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    // Use current agent ID from MockData
    private val agentId get() = MockData.currentAgentId

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                if (agentId.isBlank()) {
                    val agentsRes = RetrofitClient.agentApi.getAllAgents()
                    if (agentsRes.success) {
                        val myAgent = agentsRes.data?.find { it.userId == MockData.currentUser.id }
                        if (myAgent != null) {
                            MockData.currentAgentId = myAgent.id
                        }
                    }
                }
                
                val propertiesDeferred = async { RetrofitClient.propertyApi.getPropertiesByAgent(agentId) }
                val bookedDeferred = async { RetrofitClient.bookedPropertyApi.getAllBookedProperties() }
                val wishlistDeferred = async { RetrofitClient.wishlistApi.getAllWishlistItems() }

                val response = propertiesDeferred.await()
                val bookedResponse = bookedDeferred.await()
                val wishlistResponse = wishlistDeferred.await()

                if (response.success && response.data != null) {
                    val props = response.data.map { dto ->
                        Property(
                            id = dto.id.toString(),
                            title = dto.title,
                            description = dto.description,
                            imageUrl = dto.imageUrl,
                            price = dto.priceRange,
                            location = dto.location,
                            category = dto.propertyType,
                            isVerified = dto.isVerified ?: false,
                            beds = 0,
                            baths = 0,
                            area = "TBD",
                            agentName = MockData.currentUser.name,
                            agentPicUrl = MockData.currentUser.profilePicUrl,
                            amenities = emptyList()
                        )
                    }

                    var pending = props.count { !it.isVerified }
                    var sold = 0
                    var bookings = emptyList<com.example.realstate.data.model.BookedPropertyDto>()

                    if (bookedResponse.success) {
                        bookings = bookedResponse.data?.filter { it.agentId == agentId || it.property?.agentId == agentId } ?: emptyList()
                        sold = bookings.count { it.isSold }
                    }

                    var wishlistedItems = emptyList<com.example.realstate.data.model.WishlistItemDto>()
                    if (wishlistResponse.success) {
                        wishlistedItems = wishlistResponse.data?.filter { it.agentId == agentId || it.property?.agentId == agentId } ?: emptyList()
                    }

                    val newBookingsCount = bookings.size
                    val oldBookingsCount = _uiState.value.bookings.size
                    val newWishlistCount = wishlistedItems.size
                    val oldWishlistCount = _uiState.value.wishlistItems.size

                    if (newBookingsCount > oldBookingsCount && !_uiState.value.isLoading) {
                        // com.example.realstate.utils.NotificationManager.showNotification("New booking request from ${bookings.lastOrNull()?.user?.name ?: "a user"}!")
                    } else if (newWishlistCount > oldWishlistCount && !_uiState.value.isLoading) {
                        // com.example.realstate.utils.NotificationManager.showNotification("Someone added your property to their wishlist!")
                    }

                    _uiState.update {
                        it.copy(
                            properties = props,
                            pendingApprovals = pending,
                            soldProperties = sold,
                            bookings = bookings,
                            activeBookingsCount = bookings.count { !it.isSold },
                            wishlistItems = wishlistedItems,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteProperty(propertyId: String) {
        // Optimistic update
        val updatedList = _uiState.value.properties.filter { it.id != propertyId }
        _uiState.update { it.copy(properties = updatedList) }
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.propertyApi.deleteProperty(propertyId)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Property deleted successfully")
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                    refreshDashboard() // Rollback/sync
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                refreshDashboard() // Rollback/sync
            }
        }
    }

    fun addProperty(
        title: String, description: String, imageUrl: String, 
        location: String, priceRange: String, propertyType: String
    ) {
        if (agentId.isBlank()) {
            com.example.realstate.utils.NotificationManager.showNotification("Error: Agent profile not found. Please log out and log in again.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val propertyMap = mapOf(
                    "agentId" to agentId,
                    "title" to title,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "location" to location,
                    "priceRange" to priceRange,
                    "propertyType" to propertyType
                )
                val response = RetrofitClient.propertyApi.addProperty(propertyMap)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Property added successfully")
                    refreshDashboard()
                } else {
                    com.example.realstate.utils.NotificationManager.showNotification("Failed to add property: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                com.example.realstate.utils.NotificationManager.showNotification("Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateProperty(
        id: String,
        title: String, description: String, imageUrl: String, 
        location: String, priceRange: String, propertyType: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val propertyMap = mapOf(
                    "title" to title,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "location" to location,
                    "priceRange" to priceRange,
                    "propertyType" to propertyType
                )
                val response = RetrofitClient.propertyApi.updateProperty(id, propertyMap)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Property updated successfully")
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) { 
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun confirmBooking(bookingId: Int) {
        viewModelScope.launch {
            val booking = _uiState.value.bookings.find { it.id == bookingId }
            if (booking == null) {
                _uiState.update { it.copy(error = "Booking not found") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                // As per spec, inclusion of userId and agentId is required/recommended
                val response = RetrofitClient.bookedPropertyApi.updateBookedPropertyStatus(
                    bookingId.toString(),
                    mapOf(
                        "userId" to booking.userId,
                        "agentId" to booking.agentId,
                        "isPropAmountAccepted" to true,
                        "isSold" to true
                    )
                )
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Booking accepted successfully!")
                    try {
                        RetrofitClient.notificationApi.addNotification(
                            mapOf(
                                "title" to "Booking Accepted",
                                "message" to "Your booking request for ${booking.property?.title ?: "a property"} has been accepted.",
                                "userId" to booking.userId,
                                "receiverId" to booking.userId,
                                "receiverRole" to "USER"
                            )
                        )
                    } catch (e: Exception) {
                        // ignore if notification fails
                    }
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun rejectBooking(bookingId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.bookedPropertyApi.deleteBookedProperty(bookingId.toString())
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Booking rejected and removed.")
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    suspend fun uploadImageToImgBB(context: android.content.Context, uri: android.net.Uri, apiKey: String): String? {
        return try {
            _uiState.update { it.copy(isUploadingImage = true, error = null) }
            
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)
                val multipartBody = okhttp3.MultipartBody.Part.createFormData("image", "property.jpg", requestBody)

                val response = RetrofitClient.imgBbApi.uploadImage(apiKey, multipartBody)
                _uiState.update { it.copy(isUploadingImage = false) }

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.url
                } else {
                    _uiState.update { it.copy(error = "Image upload failed: ${response.message()}") }
                    null
                }
            } else {
                _uiState.update { it.copy(isUploadingImage = false, error = "Failed to read image") }
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isUploadingImage = false, error = e.message) }
            null
        }
    }
}
