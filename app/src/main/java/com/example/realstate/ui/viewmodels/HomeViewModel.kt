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
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.propertyApi.getAllProperties()
                if (response.success) {
                    val props = response.data.map { dto ->
                        Property(
                            id = dto.id.toString(),
                            title = dto.title,
                            description = dto.description,
                            imageUrl = dto.imageUrl,
                            price = dto.priceRange,
                            location = dto.location,
                            category = dto.propertyType,
                            beds = 0, // Fallback since API model lacks specifics
                            baths = 0,
                            area = "TBD",
                            agentName = MockData.users.find { u -> u.id == dto.agentId }?.name ?: "Verified Agent",
                            agentPicUrl = "https://i.pravatar.cc/150",
                            amenities = listOf("Dynamic")
                        )
                    }
                    _uiState.update { 
                        it.copy(
                            properties = props,
                            filteredProperties = props,
                            isLoading = false
                        )
                    }
                } else {
                    useMockDataFallback("API Error: ${response.message}")
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
}
