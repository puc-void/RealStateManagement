package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false
)

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAllProperties()
    }

    fun loadAllProperties() {
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
                            beds = 0,
                            baths = 0,
                            area = "TBD",
                            agentName = dto.agent?.id ?: "Unknown",
                            agentPicUrl = "https://i.pravatar.cc/150",
                            amenities = emptyList()
                        )
                    }
                    _uiState.update { it.copy(properties = props, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteProperty(propertyId: String) {
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.propertyApi.deleteProperty(propertyId)
                if (response.success) {
                    loadAllProperties() // Reload list
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }
}
