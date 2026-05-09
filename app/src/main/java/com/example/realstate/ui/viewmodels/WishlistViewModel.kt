package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.model.WishlistItemDto
import com.example.realstate.data.network.RetrofitClient
import com.example.realstate.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistUiState(
    val wishlistItems: List<WishlistItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItem: WishlistItemDto? = null
)

class WishlistViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
        loadWishlist()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            WishlistRepository.wishlistItems.collect { items ->
                _uiState.update { it.copy(wishlistItems = items) }
            }
        }
        viewModelScope.launch {
            WishlistRepository.isLoading.collect { loading ->
                _uiState.update { it.copy(isLoading = loading) }
            }
        }
        viewModelScope.launch {
            WishlistRepository.error.collect { err ->
                _uiState.update { it.copy(error = err) }
            }
        }
    }

    fun loadWishlist() {
        viewModelScope.launch {
            WishlistRepository.loadWishlist()
        }
    }

    fun deleteWishlistItem(itemId: String) {
        viewModelScope.launch {
            WishlistRepository.deleteItem(itemId)
        }
    }

    fun getWishlistItemDetails(itemId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.wishlistApi.getWishlistItemDetails(itemId)
                if (response.success) {
                    _uiState.update { it.copy(selectedItem = response.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = response.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun clearError() {
        WishlistRepository.clearError()
    }

    fun bookProperty(propertyId: Int, agentId: String, amount: String) {
        val userId = MockData.currentUser.id
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "propertyId" to propertyId,
                    "userId" to userId,
                    "agentId" to agentId,
                    "proposedAmount" to amount
                )
                val response = RetrofitClient.bookedPropertyApi.bookProperty(body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Booking request sent successfully!")
                    val prop = _uiState.value.selectedItem?.property
                    val agentUserId = _uiState.value.selectedItem?.agent?.userId ?: ""
                    if (prop != null && agentUserId.isNotEmpty()) {
                        RetrofitClient.notificationApi.addNotification(
                            mapOf(
                                "title" to "New Booking Request",
                                "message" to "A user has requested to book your property: ${prop.title}",
                                "userId" to agentUserId,
                                "receiverId" to agentUserId,
                                "receiverRole" to "AGENT"
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

