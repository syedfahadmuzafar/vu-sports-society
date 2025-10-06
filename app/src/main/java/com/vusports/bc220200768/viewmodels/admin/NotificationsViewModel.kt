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
    val expirationTime: Long = 0L
)

class NotificationsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    var message = MutableStateFlow("")
    var selectedRole = MutableStateFlow("all")
    var selectedDuration = MutableStateFlow(60) // Default 1h

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
                val expirationTime = timestamp + (selectedDuration.value * 60 * 1000)

                val notificationData = mapOf(
                    "message" to message.value,
                    "audience" to selectedRole.value,
                    "timestamp" to timestamp,
                    "expirationTime" to expirationTime
                )

                val doc = firestore.collection("global_notifications")
                    .add(notificationData)
                    .await()

                _status.value = "✅ Sent successfully!"
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

                _notifications.value = snapshot.documents.map {
                    NotificationModel(
                        id = it.id,
                        message = it.getString("message") ?: "",
                        audience = it.getString("audience") ?: "all",
                        timestamp = it.getLong("timestamp") ?: 0L,
                        expirationTime = it.getLong("expirationTime") ?: 0L
                    )
                }
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
