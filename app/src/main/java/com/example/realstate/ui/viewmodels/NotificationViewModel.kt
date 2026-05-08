package com.example.realstate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realstate.data.MockData
import com.example.realstate.data.UserRole
import com.example.realstate.data.model.NotificationDto
import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userRole: UserRole = MockData.currentUser.role
)

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        refreshNotifications()
    }

    fun markAllAsRead() {
        _uiState.update { it.copy(unreadCount = 0) }
    }

    fun refreshNotifications() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = if (MockData.currentUser.role == UserRole.ADMIN) {
                    RetrofitClient.notificationApi.getAllNotifications()
                } else {
                    RetrofitClient.notificationApi.getNotificationsByUserId(MockData.currentUser.id)
                }

                if (response.success) {
                    // Sort by timestamp if available (newest first)
                    val sortedList = response.data?.sortedByDescending { it.generatedAt } ?: emptyList()
                    _uiState.update { 
                        val newItemsCount = (sortedList.size - it.notifications.size).coerceAtLeast(0)
                        val newUnreadCount = if (it.notifications.isEmpty()) sortedList.size else it.unreadCount + newItemsCount
                        it.copy(notifications = sortedList, unreadCount = newUnreadCount, isLoading = false) 
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.notificationApi.deleteNotification(id)
                if (response.success) {
                    com.example.realstate.utils.NotificationManager.showNotification("Notification deleted")
                    refreshNotifications()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
