package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.Property
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgentUiState(
    val properties: List<Property> = emptyList(),
    val pendingApprovals: Int = 0,
    val soldProperties: Int = 0,
    val bookings: List<com.example.realstate.data.model.BookedPropertyDto> = emptyList(),
    val isLoading: Boolean = false,
    var error: String? = null,
    val activeBookingsCount: Int = 0,
    val selectedFilter: String = "All",
    val notification: String? = null
)

class AgentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    // Use current agent ID from MockData
    private val agentId get() = MockData.currentAgentId

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val propertiesDeferred = async { RetrofitClient.propertyApi.getPropertiesByAgent(agentId) }
                val bookedDeferred = async { RetrofitClient.bookedPropertyApi.getAllBookedProperties() }

                val response = propertiesDeferred.await()
                val bookedResponse = bookedDeferred.await()

                if (response.success && response.data != null) {
                    val props = response.data.map { dto ->
                        Property(
                            id = dto.id.toString(),
                            title = dto.title,
                            description = dto.description,
                            imageUrl = dto.imageUrl,
                            price = dto.priceRange,
                            location = dto.location,
                            category = dto.propertyType,
                            isVerified = dto.isVerified ?: false,
                            beds = 0,
                            baths = 0,
                            area = "TBD",
                            agentName = MockData.currentUser.name,
                            agentPicUrl = MockData.currentUser.profilePicUrl,
                            amenities = emptyList()
                        )
                    }

                    var pending = props.count { !it.isVerified }
                    var sold = 0
                    var bookings = emptyList<com.example.realstate.data.model.BookedPropertyDto>()

                    if (bookedResponse.success) {
                        bookings = bookedResponse.data.filter { it.agentId == agentId || it.property?.agentId == agentId }
                        sold = bookings.count { it.isSold }
                    }

                    val newBookingsCount = bookings.size
                    val oldBookingsCount = _uiState.value.bookings.size
                    val latestBooking = bookings.lastOrNull()
                    val notification = if (newBookingsCount > oldBookingsCount && !_uiState.value.isLoading) {
                        "New booking request from ${latestBooking?.user?.name ?: "a user"}!"
                    } else {
                        null
                    }

                    _uiState.update {
                        it.copy(
                            properties = props,
                            pendingApprovals = pending,
                            soldProperties = sold,
                            bookings = bookings,
                            activeBookingsCount = bookings.count { !it.isSold },
                            notification = notification,
                            isLoading = false
                        )
                    }
                    // Clear notification after 5s
                    if (notification != null) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(5000)
                            _uiState.update { it.copy(notification = null) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteProperty(propertyId: String) {
        // Optimistic update
        val updatedList = _uiState.value.properties.filter { it.id != propertyId }
        _uiState.update { it.copy(properties = updatedList) }
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.propertyApi.deleteProperty(propertyId)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                    refreshDashboard() // Rollback/sync
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                refreshDashboard() // Rollback/sync
            }
        }
    }

    fun addProperty(
        title: String, description: String, imageUrl: String, 
        location: String, priceRange: String, propertyType: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val propertyMap = mapOf(
                    "agentId" to agentId,
                    "title" to title,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "location" to location,
                    "priceRange" to priceRange,
                    "propertyType" to propertyType
                )
                val response = RetrofitClient.propertyApi.addProperty(propertyMap)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateProperty(
        id: String,
        title: String, description: String, imageUrl: String, 
        location: String, priceRange: String, propertyType: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val propertyMap = mapOf(
                    "title" to title,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "location" to location,
                    "priceRange" to priceRange,
                    "propertyType" to propertyType
                )
                val response = RetrofitClient.propertyApi.updateProperty(id, propertyMap)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun confirmBooking(bookingId: Int) {
        viewModelScope.launch {
            val booking = _uiState.value.bookings.find { it.id == bookingId }
            if (booking == null) {
                _uiState.update { it.copy(error = "Booking not found") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                // As per spec, inclusion of userId and agentId is required/recommended
                val response = RetrofitClient.bookedPropertyApi.updateBookedPropertyStatus(
                    bookingId.toString(),
                    mapOf(
                        "userId" to booking.userId,
                        "agentId" to booking.agentId,
                        "isPropAmountAccepted" to true,
                        "isSold" to true
                    )
                )
                if (response.success) {
                    _uiState.update { it.copy(notification = "Booking accepted successfully!") }
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun rejectBooking(bookingId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.bookedPropertyApi.deleteBookedProperty(bookingId.toString())
                if (response.success) {
                    _uiState.update { it.copy(notification = "Booking rejected and removed.") }
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
