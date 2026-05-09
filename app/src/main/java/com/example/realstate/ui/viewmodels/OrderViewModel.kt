package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.model.*
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderUiState(
    val bookedProperties: List<BookedPropertyDto> = emptyList(),
    val boughtProperties: List<SoldPropertyDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OrderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    fun fetchOrders(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch booked properties
                val bookedResponse = RetrofitClient.bookedPropertyApi.getBookedPropertiesByUser(userId)
                // Fetch bought properties (sold properties)
                val boughtResponse = RetrofitClient.soldPropertyApi.getSoldPropertiesByUser(userId)
                
                val rawBooked = if (bookedResponse.success) bookedResponse.data ?: emptyList() else emptyList()
                val rawBought = if (boughtResponse.success) boughtResponse.data ?: emptyList() else emptyList()
                
                val boughtPropIds = rawBought.map { it.propertyId }.toSet()

                _uiState.update { it.copy(
                    bookedProperties = rawBooked.filter { !it.isSold && it.propertyId !in boughtPropIds },
                    boughtProperties = rawBought,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun confirmPurchase(booking: BookedPropertyDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val body = mapOf(
                    "bookedPropertyId" to booking.id,
                    "propertyId" to booking.propertyId,
                    "userId" to booking.userId,
                    "agentId" to booking.agentId,
                    "amount" to booking.proposedAmount
                )
                val response = RetrofitClient.soldPropertyApi.addSoldProperty(body)
                if (response?.success == true) {
                    com.example.realstate.utils.NotificationManager.showNotification("Purchase confirmed successfully!")
                    fetchOrders(booking.userId)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response?.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
