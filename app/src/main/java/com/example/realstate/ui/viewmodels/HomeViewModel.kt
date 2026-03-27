package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val properties: List<Property> = emptyList(),
    val filteredProperties: List<Property> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        val allProps = MockData.properties
        _uiState.update { 
            it.copy(
                properties = allProps,
                filteredProperties = allProps
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
