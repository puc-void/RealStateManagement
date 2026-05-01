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

data class ProfileUiState(
    val wishlist: List<Property> = emptyList(),
    val reviews: List<com.example.realstate.data.model.ReviewDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userId: String = ""
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val userId get() = MockData.currentUser.id
    private val wishlistId get() = MockData.wishlistId

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null, userId = userId) }
        viewModelScope.launch {
            try {
                val wishlistDeferred = async { RetrofitClient.wishlistApi.getWishlistItems(wishlistId) }
                val reviewsDeferred = async { RetrofitClient.reviewApi.getReviewsByUser(userId) }

                val wishlistResponse = wishlistDeferred.await()
                val reviewsResponse = reviewsDeferred.await()

                if (wishlistResponse.success) {
                    val wishlist = wishlistResponse.data.mapNotNull { it.property }.map { dto ->
                        Property(
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
                            agentName = dto.agent?.id?.take(8) ?: "Unknown",
                            agentPicUrl = "https://i.pravatar.cc/150",
                            amenities = emptyList(),
                            agentId = dto.agentId
                        )
                    }
                    
                    val reviews = if (reviewsResponse.success) reviewsResponse.data else emptyList()

                    _uiState.update { it.copy(
                        wishlist = wishlist,
                        reviews = reviews,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = wishlistResponse.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun removeFromWishlist(propertyId: String) {
        viewModelScope.launch {
            try {
                val wishlistResponse = RetrofitClient.wishlistApi.getWishlistItems(wishlistId)
                if (wishlistResponse.success) {
                    val item = wishlistResponse.data.find { it.propertyId.toString() == propertyId }
                    if (item != null) {
                        val deleteResponse = RetrofitClient.wishlistApi.deleteWishlistItem(item.id)
                        if (deleteResponse.success) {
                            loadProfileData()
                        }
                    }
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
                    loadProfileData()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.reviewApi.deleteReview(reviewId)
                if (response.success) {
                    loadProfileData()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateProfile(name: String, image: String, contactNumber: String, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updateMap = mapOf(
                    "name" to name,
                    "image" to image,
                    "contactNumber" to contactNumber,
                    "address" to address
                )
                val response = RetrofitClient.userApi.updateProfile(userId, updateMap)
                if (response.success) {
                    // Update current user in MockData for UI consistency if needed, 
                    // though real apps would use a proper auth state management
                    MockData.currentUser = MockData.currentUser.copy(
                        name = response.data.name,
                        profilePicUrl = if (response.data.image?.startsWith("data:") == true) MockData.currentUser.profilePicUrl else response.data.image ?: MockData.currentUser.profilePicUrl,
                        phone = response.data.contactNumber ?: MockData.currentUser.phone,
                        location = response.data.address ?: MockData.currentUser.location
                    )
                    loadProfileData()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
