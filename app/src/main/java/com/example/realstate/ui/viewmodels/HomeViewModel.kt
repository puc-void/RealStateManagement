package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import com.example.realstate.data.repository.WishlistRepository
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

    init {
        loadData()
        observeWishlist()
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            WishlistRepository.wishlistItems.collect { items ->
                val ids = items.map { it.propertyId.toString() }.toSet()
                _uiState.update { it.copy(wishlistedPropertyIds = ids) }
            }
        }
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Initial wishlist load
                WishlistRepository.loadWishlist()
                
                val propResponse = RetrofitClient.propertyApi.getAllProperties()
                if (propResponse.success) {
                    val props = propResponse.data?.map { dto ->
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
                    } ?: emptyList()
                    
                    _uiState.update { 
                        it.copy(
                            properties = props,
                            filteredProperties = applyFilter(props, it.searchQuery),
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

    private fun useMockDataFallback(errorMsg: String?) {
        val allProps = MockData.properties
        _uiState.update { 
            it.copy(
                properties = allProps,
                filteredProperties = applyFilter(allProps, it.searchQuery),
                isLoading = false,
                error = "Offline mode ($errorMsg)"
            )
        }
    }

    private fun applyFilter(props: List<Property>, query: String): List<Property> {
        if (query.isBlank()) return props
        return props.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.location.contains(query, ignoreCase = true) 
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query, 
                filteredProperties = applyFilter(state.properties, query)
            )
        }
    }

    fun toggleWishlist(property: Property) {
        viewModelScope.launch {
            val success = WishlistRepository.toggleWishlist(property.id, property.agentId)
            if (!success) {
                _uiState.update { it.copy(error = "Action failed. Check connection.") }
            }
        }
    }
}


