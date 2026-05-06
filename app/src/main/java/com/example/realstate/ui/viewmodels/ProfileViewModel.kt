package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import com.example.realstate.data.repository.WishlistRepository
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
    val successMessage: String? = null,
    val isOtpSent: Boolean = false,
    val userId: String = ""
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val userId get() = MockData.currentUser.id

    init {
        observeWishlist()
        loadProfileData()
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            WishlistRepository.wishlistItems.collect { items ->
                val props = items.mapNotNull { it.property }.map { dto ->
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
                _uiState.update { it.copy(wishlist = props) }
            }
        }
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null, userId = userId) }
        viewModelScope.launch {
            try {
                // Ensure wishlist is up to date in repository
                WishlistRepository.loadWishlist()
                
                val reviewsDeferred = async { RetrofitClient.reviewApi.getReviewsByUser(userId) }
                val reviewsResponse = reviewsDeferred.await()

                val reviews = if (reviewsResponse.success) reviewsResponse.data ?: emptyList() else emptyList()

                _uiState.update { it.copy(
                    reviews = reviews,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun removeFromWishlist(propertyId: String) {
        viewModelScope.launch {
            val item = WishlistRepository.wishlistItems.value.find { it.propertyId.toString() == propertyId }
            if (item != null) {
                WishlistRepository.deleteItem(item.id)
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
                    MockData.currentUser = MockData.currentUser.copy(
                        name = response.data?.name ?: MockData.currentUser.name,
                        profilePicUrl = if (response.data?.image?.startsWith("data:") == true) MockData.currentUser.profilePicUrl else response.data?.image ?: MockData.currentUser.profilePicUrl,
                        phone = response.data?.contactNumber ?: MockData.currentUser.phone,
                        location = response.data?.address ?: MockData.currentUser.location
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

    fun changeEmail(newEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null, isOtpSent = false) }
            try {
                val response = RetrofitClient.authApi.changeEmail(userId, mapOf("email" to newEmail))
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, isOtpSent = true, successMessage = response.message) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun verifyNewEmail(otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val response = RetrofitClient.verificationApi.verifyEmail(userId, mapOf("otp" to otp))
                if (response.success) {
                    val meResponse = RetrofitClient.authApi.getMe()
                    if (meResponse.success) {
                        MockData.currentUser = MockData.currentUser.copy(email = meResponse.data?.email ?: MockData.currentUser.email)
                    }
                    _uiState.update { it.copy(isLoading = false, isOtpSent = false, successMessage = "Email successfully verified and updated!") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val response = RetrofitClient.authApi.updatePassword(
                    userId,
                    mapOf("password" to currentPassword, "newPassword" to newPassword)
                )
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, successMessage = response.message ?: "Password updated successfully!") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

