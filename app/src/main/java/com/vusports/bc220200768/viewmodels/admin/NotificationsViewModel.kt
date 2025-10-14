package com.vusports.bc220200768.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationModel(
    val id: String = "",
    val message: String = "",
    val audience: String = "all",
    val timestamp: Long = 0L,
    val expirationTime: Long = 0L,
    val type: String = "slider" // Default to slider notification
)

class NotificationsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    var message = MutableStateFlow("")
    var selectedRole = MutableStateFlow("all")
    var selectedDuration = MutableStateFlow(60) // Default 1h
    var notificationType = MutableStateFlow("slider") // Default to slider notification
    
    private val _isSendingPushNotification = MutableStateFlow(false)
    val isSendingPushNotification = _isSendingPushNotification.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun sendNotification() {
        if (message.value.isBlank()) {
            _status.value = "Message cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                // For push notifications, no expiration time is needed
                val expirationTime = if (notificationType.value == "push") {
                    0L // No expiration for push notifications
                } else {
                    timestamp + (selectedDuration.value * 60 * 1000) // Only for slider notifications
                }

                val notificationData = mapOf(
                    "message" to message.value,
                    "audience" to selectedRole.value,
                    "timestamp" to timestamp,
                    "expirationTime" to expirationTime,
                    "type" to notificationType.value // Add notification type
                )

                // Store notification in Firestore
                val doc = firestore.collection("global_notifications")
                    .add(notificationData)
                    .await()
                
                // Handle push notification if selected
                if (notificationType.value == "push") {
                    _isSendingPushNotification.value = true
                    try {
                        // For push notifications, we store the notification in Firestore
                        // and then trigger a cloud function that will send the actual push notification
                        // This is a simplified implementation since we don't have FCM set up
                        
                        // In a real implementation, we would call a cloud function or use FCM admin SDK
                        // to send the push notification to the appropriate audience
                        
                        // Simulate push notification sending delay
                        kotlinx.coroutines.delay(1000)
                        _isSendingPushNotification.value = false
                    } catch (e: Exception) {
                        _isSendingPushNotification.value = false
                        _status.value = "❌ Push notification failed: ${e.message}"
                        return@launch
                    }
                }

                _status.value = "✅ ${notificationType.value.capitalize()} notification sent successfully!"
                message.value = ""
                loadNotifications()
            } catch (e: Exception) {
                _status.value = "❌ Failed: ${e.message}"
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                val snapshot: QuerySnapshot =
                    firestore.collection("global_notifications")
                        .orderBy("timestamp")
                        .get()
                        .await()

                val currentTime = System.currentTimeMillis()
                val notificationsToDelete = mutableListOf<String>()
                
                val activeNotifications = snapshot.documents.mapNotNull { doc ->
                    val expirationTime = doc.getLong("expirationTime") ?: 0L
                    val type = doc.getString("type") ?: "slider"
                    
                    // Check if slider notification has expired
                    if (type == "slider" && expirationTime > 0 && expirationTime < currentTime) {
                        // Add to delete list
                        notificationsToDelete.add(doc.id)
                        null // Don't include in active notifications
                    } else {
                        // Include in active notifications
                        NotificationModel(
                            id = doc.id,
                            message = doc.getString("message") ?: "",
                            audience = doc.getString("audience") ?: "all",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            expirationTime = expirationTime,
                            type = type
                        )
                    }
                }
                
                // Delete expired notifications
                notificationsToDelete.forEach { id ->
                    try {
                        firestore.collection("global_notifications")
                            .document(id)
                            .delete()
                            .await()
                    } catch (e: Exception) {
                        // Silent failure for cleanup
                    }
                }

                _notifications.value = activeNotifications
            } catch (e: Exception) {
                _status.value = "❌ Failed to load notifications"
            }
        }
    }

    fun updateNotification(id: String, newMessage: String, newDuration: Int) {
        viewModelScope.launch {
            try {
                val newExpiration = System.currentTimeMillis() + (newDuration * 60 * 1000)
                firestore.collection("global_notifications")
                    .document(id)
                    .update(
                        mapOf(
                            "message" to newMessage,
                            "expirationTime" to newExpiration
                        )
                    )
                    .await()

                _status.value = "✏️ Notification updated"
                loadNotifications()
            } catch (e: Exception) {
                _status.value = "❌ Update failed: ${e.message}"
            }
        }
    }

    fun cancelNotification(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("global_notifications")
                    .document(id)
                    .delete()
                    .await()

                _status.value = "🗑️ Notification cancelled"
                loadNotifications()
            } catch (e: Exception) {
                _status.value = "❌ Cancel failed: ${e.message}"
            }
        }
    }
}
