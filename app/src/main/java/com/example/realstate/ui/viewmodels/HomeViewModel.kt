package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val properties: List<Property> = emptyList(),
    val filteredProperties: List<Property> = emptyList(),
    val wishlistedPropertyIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val wishlistId: String = ""
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val wishlistId get() = MockData.wishlistId

    init {
        loadData()
    }

    fun loadData() {
        val userId = MockData.currentUser.id
        _uiState.update { it.copy(isLoading = true, error = null, wishlistId = wishlistId) }
        viewModelScope.launch {
            try {
                // Fetch properties and wishlist in parallel
                val propResponse = RetrofitClient.propertyApi.getAllProperties()
                val wishlistResponse = if (userId.isNotEmpty()) {
                    RetrofitClient.wishlistApi.getWishlistItemsByUserId(userId)
                } else null

                if (propResponse.success) {
                    val props = propResponse.data.map { dto ->
                        Property(
                            id = dto.id.toString(),
                            title = dto.title,
                            description = dto.description,
                            imageUrl = dto.imageUrl,
                            price = dto.priceRange,
                            location = dto.location,
                            category = dto.propertyType,
                            beds = 0,
                            baths = 0,
                            area = "TBD",
                            agentName = dto.agent?.user?.name ?: "Verified Agent",
                            agentPicUrl = if (dto.agent?.user?.image?.startsWith("data:") == true) "https://i.pravatar.cc/150"
                                          else dto.agent?.user?.image ?: "https://i.pravatar.cc/150",
                            agentId = dto.agentId,
                            amenities = listOf("Dynamic")
                        )
                    }
                    
                    val wishlistedIds = wishlistResponse?.data?.map { it.propertyId.toString() }?.toSet() ?: emptySet()

                    _uiState.update { 
                        it.copy(
                            properties = props,
                            filteredProperties = props,
                            wishlistedPropertyIds = wishlistedIds,
                            isLoading = false
                        )
                    }
                } else {
                    useMockDataFallback("API Error: ${propResponse.message}")
                }
            } catch (e: Exception) {
                useMockDataFallback(e.message)
            }
        }
    }

    private fun loadProperties() {
        // Kept for backward compatibility if needed, but loadData is preferred
        loadData()
    }

    private fun useMockDataFallback(errorMsg: String?) {
        val allProps = MockData.properties
        _uiState.update { 
            it.copy(
                properties = allProps,
                filteredProperties = allProps,
                isLoading = false,
                error = "Offline mode ($errorMsg)"
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.properties
            } else {
                state.properties.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.location.contains(query, ignoreCase = true) 
                }
            }
            state.copy(searchQuery = query, filteredProperties = filtered)
        }
    }

    fun toggleWishlist(property: Property) {
        val userId = MockData.currentUser.id
        if (userId.isEmpty()) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            try {
                val isCurrentlyWishlisted = _uiState.value.wishlistedPropertyIds.contains(property.id)
                
                if (isCurrentlyWishlisted) {
                    val itemsRes = RetrofitClient.wishlistApi.getWishlistItemsByUserId(userId)
                    if (itemsRes.success) {
                        val item = itemsRes.data.find { it.propertyId.toString() == property.id }
                        if (item != null) {
                            val delRes = RetrofitClient.wishlistApi.deleteWishlistItem(item.id)
                            if (delRes.success) {
                                _uiState.update { state ->
                                    state.copy(wishlistedPropertyIds = state.wishlistedPropertyIds - property.id)
                                }
                            }
                        }
                    }
                } else {
                    val body = mapOf(
                        "propertyId" to property.id.toInt(),
                        "agentId" to property.agentId
                    )
                    val addRes = RetrofitClient.wishlistApi.addWishlistItem(userId, body)
                    if (addRes.success) {
                        _uiState.update { state ->
                            state.copy(wishlistedPropertyIds = state.wishlistedPropertyIds + property.id)
                        }
                    } else {
                        _uiState.update { it.copy(error = "Failed to add: ${addRes.message}") }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "Wishlist Error: ${e.message}") }
            }
        }
    }
}

