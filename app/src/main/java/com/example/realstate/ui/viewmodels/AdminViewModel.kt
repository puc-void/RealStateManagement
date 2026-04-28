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

data class AdminUiState(
    val properties: List<Property> = emptyList(),
    val totalRevenue: Double = 0.0,
    val pendingApprovals: Int = 0,
    val totalUsers: Int = 0,
    val reviews: List<com.example.realstate.data.model.ReviewDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val selectedFilter: String = "All",
    val users: List<com.example.realstate.data.model.UserDto> = emptyList(),
    val agents: List<com.example.realstate.data.model.AgentDetailDto> = emptyList(),
    val soldProperties: List<com.example.realstate.data.model.SoldPropertyDto> = emptyList()
)

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Launch all requests concurrently
                val propertiesDeferred = async { RetrofitClient.propertyApi.getAllProperties() }
                val bookedPropertiesDeferred = async { RetrofitClient.bookedPropertyApi.getAllBookedProperties() }
                val reviewsDeferred = async { RetrofitClient.reviewApi.getAllReviews() }
                val usersDeferred = async { RetrofitClient.userApi.getAllUsers() }
                val agentsDeferred = async { RetrofitClient.agentApi.getAllAgents() }
                val soldPropertiesDeferred = async { RetrofitClient.soldPropertyApi.getAllSoldProperties() }
                
                // Await all and handle results safely
                val propertiesResponse = try { propertiesDeferred.await() } catch (e: Exception) { null }
                val bookedResponse = try { bookedPropertiesDeferred.await() } catch (e: Exception) { null }
                val reviewsResponse = try { reviewsDeferred.await() } catch (e: Exception) { null }
                val usersResponse = try { usersDeferred.await() } catch (e: Exception) { null }
                val agentsResponse = try { agentsDeferred.await() } catch (e: Exception) { null }
                val soldPropertiesResponse = try { soldPropertiesDeferred.await() } catch (e: Exception) { null }

                if (propertiesResponse != null && propertiesResponse.success && propertiesResponse.data != null) {
                    val props = propertiesResponse.data.map { dto ->
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
                            agentName = MockData.users.find { u -> u.id == dto.agentId }?.name ?: "Unknown",
                            agentPicUrl = "https://i.pravatar.cc/150",
                            amenities = emptyList()
                        )
                    }

                    var revenue = 0.0
                    var pending = props.count { !it.isVerified }
                    
                    bookedResponse?.let { resp ->
                        if (resp.success && resp.data != null) {
                            resp.data.forEach { 
                                if (it.isSold) {
                                    val priceStr = it.proposedAmount.replace("$", "").replace(",", "").split("-").firstOrNull()?.trim()
                                    val price = priceStr?.toDoubleOrNull() ?: 0.0
                                    revenue += price
                                }
                                if (!it.isPropAmountAccepted) {
                                    pending++
                                }
                            }
                        }
                    }

                    val reviews = if (reviewsResponse != null && reviewsResponse.success) reviewsResponse.data ?: emptyList() else emptyList()
                    val users = if (usersResponse != null && usersResponse.success) usersResponse.data ?: emptyList() else emptyList()
                    val agents = if (agentsResponse != null && agentsResponse.success) agentsResponse.data ?: emptyList() else emptyList()
                    val soldProperties = if (soldPropertiesResponse != null && soldPropertiesResponse.success) soldPropertiesResponse.data ?: emptyList() else emptyList()

                    _uiState.update { it.copy(
                        properties = props,
                        totalRevenue = revenue,
                        pendingApprovals = pending,
                        totalUsers = users.size,
                        reviews = reviews,
                        users = users,
                        agents = agents,
                        soldProperties = soldProperties,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = propertiesResponse?.message ?: "Failed to load properties") }
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
                    refreshDashboard() // Rollback
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                refreshDashboard() // Rollback
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
                    "agentId" to MockData.currentAgentId, // Unified agent ID
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

    fun approveProperty(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.propertyApi.updateProperty(id, mapOf("isVerified" to true))
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

    fun toggleUserStatus(userId: String, currentStatus: String) {
        viewModelScope.launch {
            try {
                val newStatus = if (currentStatus == "active") "suspended" else "active"
                val response = RetrofitClient.userApi.updateUserStatus(userId, mapOf("status" to newStatus))
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        // Optimistic update
        val updatedList = _uiState.value.reviews.filter { it.id != reviewId }
        _uiState.update { it.copy(reviews = updatedList) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.reviewApi.deleteReview(reviewId)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                    refreshDashboard()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                refreshDashboard()
            }
        }
    }

    fun deleteUser(userId: String) {
        // Optimistic update
        val updatedList = _uiState.value.users.filter { it.id != userId }
        _uiState.update { it.copy(users = updatedList) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.userApi.deleteUser(userId)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                    refreshDashboard()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                refreshDashboard()
            }
        }
    }

    fun updateUser(userId: String, name: String, image: String, contactNumber: String, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updateMap = mapOf(
                    "name" to name,
                    "image" to image,
                    "contactNumber" to contactNumber,
                    "address" to address
                )
                val response = RetrofitClient.userApi.updateProfile(userId, updateMap)
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

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun verifyAgent(agentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.agentApi.verifyAgent(agentId, mapOf("isVerified" to true))
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

    fun markAgentFraud(agentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.agentApi.markAgentFraud(agentId, mapOf("isFraud" to true))
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

    fun deleteAgent(agentId: String) {
        // Optimistic update
        val updatedList = _uiState.value.agents.filter { it.id != agentId }
        _uiState.update { it.copy(agents = updatedList) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.agentApi.deleteAgent(agentId)
                if (response.success) {
                    refreshDashboard()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                    refreshDashboard()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                refreshDashboard()
            }
        }
    }
}
