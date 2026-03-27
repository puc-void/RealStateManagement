package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetailUiState(
    val property: Property? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadProperty(propertyId: String) {
        _uiState.update { it.copy(isLoading = true) }
        val property = MockData.properties.find { it.id == propertyId }
        
        if (property != null) {
            _uiState.update { it.copy(property = property, isLoading = false, error = null) }
        } else {
            _uiState.update { it.copy(property = null, isLoading = false, error = "Property not found") }
        }
    }
}
