package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.UserRole
import com.example.realstate.data.network.RetrofitClient
import com.example.realstate.data.repository.WishlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val property: Property? = null,
    val reviews: List<com.example.realstate.data.model.ReviewDto> = emptyList(),
    val isWishlisted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userRole: UserRole = MockData.currentUser.role,
    val userId: String = ""
)

class DetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeWishlist()
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            WishlistRepository.wishlistItems.collect { items ->
                val propId = _uiState.value.property?.id
                if (propId != null) {
                    val wishlisted = items.any { it.propertyId.toString() == propId }
                    _uiState.update { it.copy(isWishlisted = wishlisted) }
                }
            }
        }
    }

    fun loadProperty(propertyId: String) {
        val userId = MockData.currentUser.id
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val propertyDeferred = async { RetrofitClient.propertyApi.getPropertyDetails(propertyId) }
                val reviewsDeferred = async { RetrofitClient.reviewApi.getReviewsByProperty(propertyId) }
                
                // Ensure wishlist is loaded in repo
                if (userId.isNotEmpty()) {
                    WishlistRepository.loadWishlist()
                }

                val propResponse = propertyDeferred.await()
                val reviewsResponse = reviewsDeferred.await()

                if (propResponse.success && propResponse.data != null) {
                    val dto = propResponse.data
                    
                    var agentName = dto.agent?.user?.name
                    var agentPic = dto.agent?.user?.image
                    var agentUserId = dto.agent?.userId ?: ""

                    // Fallback: If agent object is missing, fetch agent details separately
                    if (agentName == null && dto.agentId.isNotEmpty()) {
                        try {
                            val agentResponse = RetrofitClient.agentApi.getAgentDetails(dto.agentId)
                            if (agentResponse.success && agentResponse.data != null) {
                                agentName = agentResponse.data.user?.name
                                agentPic = agentResponse.data.user?.image
                                agentUserId = agentResponse.data.userId ?: ""
                            }
                        } catch (e: Exception) {
                            // Ignore fallback failure
                        }
                    }

                    val property = Property(
                        id = dto.id.toString(),
                        title = dto.title,
                        description = dto.description,
                        imageUrl = dto.imageUrl,
                        price = dto.priceRange,
                        location = dto.location,
                        category = dto.propertyType,
                        beds = 1,
                        baths = 1,
                        area = "TBD",
                        agentName = agentName ?: "Verified Agent",
                        agentPicUrl = if (agentPic?.startsWith("data:") == true) "https://i.pravatar.cc/150"
                                      else agentPic ?: "https://i.pravatar.cc/150",
                        amenities = emptyList(),
                        agentId = dto.agentId,
                        agentUserId = agentUserId,
                        isBought = dto.isBought ?: false
                    )
                    
                    val reviews = if (reviewsResponse.success) reviewsResponse.data ?: emptyList() else emptyList()
                    val isWishlisted = WishlistRepository.wishlistItems.value.any { it.propertyId.toString() == propertyId }

                    _uiState.update { it.copy(
                        property = property,
                        reviews = reviews,
                        isWishlisted = isWishlisted,
                        isLoading = false,
                        error = null,
                        userId = userId
                    ) }
                } else {
                    _uiState.update { it.copy(property = null, isLoading = false, error = propResponse.message ?: "Property not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(property = null, isLoading = false, error = e.message ?: "Network error") }
            }
        }
    }

    fun toggleWishlist(propertyId: String, agentId: String) {
        viewModelScope.launch {
            val success = WishlistRepository.toggleWishlist(propertyId, agentId)
            if (!success) {
                _uiState.update { it.copy(error = "Action failed.") }
            }
        }
    }

    fun addReview(propertyId: Int, rating: Int, description: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "rating" to rating,
                    "description" to description,
                    "propertyId" to propertyId,
                    "userId" to _uiState.value.userId
                )
                val response = RetrofitClient.reviewApi.addReview(body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Review added successfully!")
                    loadProperty(propertyId.toString())
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateReview(reviewId: String, rating: Int, description: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "rating" to rating,
                    "description" to description
                )
                val response = RetrofitClient.reviewApi.updateReview(reviewId, body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Review updated successfully!")
                    _uiState.value.property?.id?.let { loadProperty(it) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.reviewApi.deleteReview(reviewId)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Review deleted")
                    _uiState.value.property?.id?.let { loadProperty(it) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun bookProperty(propertyId: Int, agentId: String, amount: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "propertyId" to propertyId,
                    "userId" to _uiState.value.userId,
                    "agentId" to agentId,
                    "proposedAmount" to amount
                )
                val response = RetrofitClient.bookedPropertyApi.bookProperty(body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Booking request sent successfully!")
                    val prop = _uiState.value.property
                    if (prop != null && prop.agentUserId.isNotEmpty()) {
                        RetrofitClient.notificationApi.addNotification(
                            mapOf(
                                "title" to "New Booking Request",
                                "message" to "A user has requested to book your property: ${prop.title}",
                                "userId" to prop.agentUserId,
                                "receiverId" to prop.agentUserId,
                                "receiverRole" to "AGENT"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun reportAgentAsFraud(agentId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "title" to "Agent Fraud Report",
                    "message" to "Agent ID: $agentId has been reported for fraud.",
                    "receiverRole" to "ADMIN"
                )
                val response = RetrofitClient.notificationApi.addNotification(body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Agent reported to admin.")
                }
            } catch (e: Exception) {
                // handle silently or update error state
            }
        }
    }
}

