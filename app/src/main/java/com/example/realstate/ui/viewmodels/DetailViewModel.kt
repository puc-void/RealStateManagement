package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.UserRole
import com.example.realstate.data.network.RetrofitClient
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
    val successMessage: String? = null,
    val userRole: UserRole = MockData.currentUser.role,
    val userId: String = MockData.currentUser.id
)

class DetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Mock constants for now
    private val wishlistId = MockData.currentWishlistId

    fun loadProperty(propertyId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val propertyDeferred = async { RetrofitClient.propertyApi.getPropertyDetails(propertyId) }
                val reviewsDeferred = async { RetrofitClient.reviewApi.getReviewsByProperty(propertyId) }
                val wishlistDeferred = async { RetrofitClient.wishlistApi.getWishlistItems(wishlistId) }

                val propResponse = propertyDeferred.await()
                val reviewsResponse = reviewsDeferred.await()
                val wishlistResponse = wishlistDeferred.await()

                if (propResponse.success && propResponse.data != null) {
                    val dto = propResponse.data
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
                        agentName = MockData.users.find { it.id == dto.agentId }?.name ?: "Unknown",
                        agentPicUrl = "https://i.pravatar.cc/150",
                        amenities = emptyList(),
                        agentId = dto.agentId
                    )
                    
                    val reviews = if (reviewsResponse.success) reviewsResponse.data else emptyList()
                    val isWishlisted = if (wishlistResponse.success) {
                        wishlistResponse.data.any { it.propertyId.toString() == propertyId }
                    } else false

                    _uiState.update { it.copy(
                        property = property,
                        reviews = reviews,
                        isWishlisted = isWishlisted,
                        isLoading = false,
                        error = null
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
            try {
                if (_uiState.value.isWishlisted) {
                    // Need to find the wishlist item ID to delete
                    val wishlistResponse = RetrofitClient.wishlistApi.getWishlistItems(wishlistId)
                    if (wishlistResponse.success) {
                        val item = wishlistResponse.data.find { it.propertyId.toString() == propertyId }
                        if (item != null) {
                            val deleteResponse = RetrofitClient.wishlistApi.deleteWishlistItem(item.id)
                            if (deleteResponse.success) {
                                _uiState.update { it.copy(isWishlisted = false, successMessage = "Removed from wishlist") }
                                clearSuccessMessage()
                            }
                        }
                    }
                } else {
                    val body = mapOf(
                        "wishlistId" to wishlistId,
                        "propertyId" to propertyId.toInt(),
                        "agentId" to agentId
                    )
                    val response = RetrofitClient.wishlistApi.addWishlistItem(body)
                    if (response.success) {
                        _uiState.update { it.copy(isWishlisted = true, successMessage = "Added to wishlist!") }
                        clearSuccessMessage()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun clearSuccessMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(successMessage = null) }
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
                    _uiState.update { it.copy(successMessage = "Review added successfully!") }
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
                    _uiState.update { it.copy(successMessage = "Review updated successfully!") }
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
                    _uiState.update { it.copy(successMessage = "Booking request sent successfully!") }
                    // Clear message after 3 seconds
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(successMessage = null) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
