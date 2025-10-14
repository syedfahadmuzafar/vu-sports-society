package com.vusports.bc220200768.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.models.GlobalNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlobalNotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<GlobalNotification>>(emptyList())
    val notifications: StateFlow<List<GlobalNotification>> = _notifications.asStateFlow()
    
    private val _currentNotification = MutableStateFlow<GlobalNotification?>(null)
    val currentNotification: StateFlow<GlobalNotification?> = _currentNotification.asStateFlow()
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        val currentUser = auth.currentUser ?: return
        
        // Get user role from Firestore
        db.collection("users").whereEqualTo("email", currentUser.email)
            .get()
            .addOnSuccessListener { documents ->
                val userDoc = documents.firstOrNull() ?: return@addOnSuccessListener
                // Convert role to lowercase for case-insensitive comparison
                val userRole = userDoc.getString("role")?.lowercase() ?: "participant"
                
                // Query global notifications that are still valid (not expired)
                val currentTime = System.currentTimeMillis()
                db.collection("global_notifications")
                    .whereGreaterThan("expirationTime", currentTime)
                    .whereEqualTo("type", "slider") // Only fetch slider notifications for dashboard
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val validNotifications = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val message = doc.getString("message") ?: return@mapNotNull null
                            val audience = doc.getString("audience")?.lowercase() ?: return@mapNotNull null
                            val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                            val expirationTime = doc.getLong("expirationTime") ?: return@mapNotNull null
                            
                            // Filter notifications based on user role
                            if (audience == "all" || 
                                (audience == "participant" && userRole == "participant") ||
                                (audience == "coach" && userRole == "coach")) {
                                GlobalNotification(id, message, audience, timestamp, expirationTime)
                            } else {
                                null
                            }
                        }
                        
                        _notifications.value = validNotifications
                        
                        // Set the current notification to display (most recent one)
                        if (validNotifications.isNotEmpty()) {
                            _currentNotification.value = validNotifications.maxByOrNull { it.timestamp }
                        } else {
                            _currentNotification.value = null
                        }
                    }
            }
    }

    // Rotate to next notification if there are multiple
    fun rotateNotification() {
        val allNotifications = _notifications.value
        if (allNotifications.size <= 1) return
        
        val current = _currentNotification.value
        val currentIndex = allNotifications.indexOf(current)
        val nextIndex = (currentIndex + 1) % allNotifications.size
        
        _currentNotification.value = allNotifications[nextIndex]
    }
}