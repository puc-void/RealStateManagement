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

enum class SortOption(val displayName: String) {
    DEFAULT("Default"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    LOCATION_ASC("Location (A-Z)"),
    LOCATION_DESC("Location (Z-A)"),
    PRICE_ASC("Price (Low to High)"),
    PRICE_DESC("Price (High to Low)")
}

data class HomeUiState(
    val properties: List<Property> = emptyList(),
    val filteredProperties: List<Property> = emptyList(),
    val wishlistedPropertyIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DEFAULT,
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
                            amenities = listOf("Dynamic"),
                            isBought = dto.isBought ?: false
                        )
                    } ?: emptyList()
                    
                    _uiState.update { 
                        it.copy(
                            properties = props,
                            filteredProperties = applyFilterAndSort(props, it.searchQuery, it.sortOption),
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
                filteredProperties = applyFilterAndSort(allProps, it.searchQuery, it.sortOption),
                isLoading = false,
                error = "Offline mode ($errorMsg)"
            )
        }
    }

    private fun applyFilterAndSort(props: List<Property>, query: String, sortOption: SortOption): List<Property> {
        val filtered = if (query.isBlank()) {
            props
        } else {
            props.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.location.contains(query, ignoreCase = true) 
            }
        }
        
        return when (sortOption) {
            SortOption.DEFAULT -> filtered
            SortOption.NAME_ASC -> filtered.sortedBy { it.title }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.title }
            SortOption.LOCATION_ASC -> filtered.sortedBy { it.location }
            SortOption.LOCATION_DESC -> filtered.sortedByDescending { it.location }
            SortOption.PRICE_ASC -> filtered.sortedBy { parsePrice(it.price) }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { parsePrice(it.price) }
        }
    }

    private fun parsePrice(priceStr: String): Double {
        return priceStr.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query, 
                filteredProperties = applyFilterAndSort(state.properties, query, state.sortOption)
            )
        }
    }

    fun onSortOptionChange(option: SortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = option,
                filteredProperties = applyFilterAndSort(state.properties, state.searchQuery, option)
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


