package com.example.realstate.data.repository

import com.example.realstate.data.MockData
import com.example.realstate.data.model.WishlistItemDto
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object WishlistRepository {
    private val _wishlistItems = MutableStateFlow<List<WishlistItemDto>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItemDto>> = _wishlistItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadWishlist() {
        val userId = MockData.currentUser.id
        if (userId.isEmpty()) return

        _isLoading.value = true
        try {
            val response = RetrofitClient.wishlistApi.getWishlistItemsByUserId(userId)
            if (response.success) {
                _wishlistItems.value = response.data ?: emptyList()
                _error.value = null
            } else {
                _error.value = response.message
            }
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun toggleWishlist(propertyId: String, agentId: String): Boolean {
        val userId = MockData.currentUser.id
        if (userId.isEmpty()) return false

        try {
            val currentItems = _wishlistItems.value
            val existingItem = currentItems.find { it.propertyId.toString() == propertyId }

            if (existingItem != null) {
                // Remove
                val response = RetrofitClient.wishlistApi.deleteWishlistItem(existingItem.id)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Removed from wishlist")
                    _wishlistItems.update { list -> list.filter { it.id != existingItem.id } }
                    return true
                }
            } else {
                // Add
                val body = mapOf(
                    "propertyId" to propertyId.toInt(),
                    "agentId" to agentId
                )
                val response = RetrofitClient.wishlistApi.addWishlistItem(userId, body)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Added to wishlist")
                    // Refresh fully to get the complete DTO with property info
                    loadWishlist()
                    return true
                }
            }
        } catch (e: Exception) {
            _error.value = e.message
        }
        return false
    }

    suspend fun deleteItem(itemId: String) {
        try {
            val response = RetrofitClient.wishlistApi.deleteWishlistItem(itemId)
            if (response.success) {
                com.example.realstate.utils.NotificationManager.showNotification("Removed from wishlist")
                _wishlistItems.update { list -> list.filter { it.id != itemId } }
            }
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun clearError() {
        _error.value = null
    }
}
