package com.example.pethub.ui.notifications

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Notification
import com.example.pethub.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel(), DefaultLifecycleObserver { // Implement the observer

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * This function is called when the composable observing this ViewModel becomes active (e.g., enters the screen).
     */
    override fun onResume(owner: LifecycleOwner) {
        // Mark as read only when the screen is resumed
        val unreadIds = _notifications.value.filter { !it.isRead }.map { it.id }
        if (unreadIds.isNotEmpty()) {
            markMultipleAsRead(unreadIds)
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            notificationRepository.getUserNotifications().collect { notificationsList ->
                _notifications.value = notificationsList
                _isLoading.value = false
            }
        }
    }

    // This function is still useful for clicking a single item if needed.
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    /**
     * Marks a list of notifications as read.
     */
    private fun markMultipleAsRead(notificationIds: List<String>) {
        viewModelScope.launch {
            notificationIds.forEach { id ->
                notificationRepository.markAsRead(id)
            }
        }
    }
}
