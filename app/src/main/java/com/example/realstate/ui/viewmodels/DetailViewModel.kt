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
    val successMessage: String? = null,
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
                        agentName = dto.agent?.user?.name ?: "Verified Agent",
                        agentPicUrl = if (dto.agent?.user?.image?.startsWith("data:") == true) "https://i.pravatar.cc/150"
                                      else dto.agent?.user?.image ?: "https://i.pravatar.cc/150",
                        amenities = emptyList(),
                        agentId = dto.agentId
                    )
                    
                    val reviews = if (reviewsResponse.success) reviewsResponse.data else emptyList()
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
            if (success) {
                val msg = if (_uiState.value.isWishlisted) "Removed from wishlist" else "Added to wishlist!"
                _uiState.update { it.copy(successMessage = msg) }
                clearSuccessMessage()
            } else {
                _uiState.update { it.copy(error = "Action failed.") }
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

