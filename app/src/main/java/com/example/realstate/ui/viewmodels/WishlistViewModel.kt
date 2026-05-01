package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.model.WishlistItemDto
import com.example.realstate.data.network.RetrofitClient
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

    private val userId get() = MockData.currentUser.id

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        if (userId.isEmpty()) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.wishlistApi.getWishlistItemsByUserId(userId)
                if (response.success) {
                    _uiState.update { it.copy(wishlistItems = response.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = response.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun deleteWishlistItem(itemId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.wishlistApi.deleteWishlistItem(itemId)
                if (response.success) {
                    _uiState.update { state ->
                        state.copy(wishlistItems = state.wishlistItems.filter { it.id != itemId })
                    }
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
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
        _uiState.update { it.copy(error = null) }
    }
}
