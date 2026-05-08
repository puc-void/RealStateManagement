package com.example.realstate.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationManager {
    private val _notifications = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val notifications: SharedFlow<String> = _notifications.asSharedFlow()

    fun showNotification(message: String) {
        _notifications.tryEmit(message)
    }
}
