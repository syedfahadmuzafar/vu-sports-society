package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class JoinedEvent(
    val id: String,
    val name: String,
    val venue: String,
    val timing: String,
    val timestamp: Long
)

class JoinedEventsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _upcomingEvents = MutableStateFlow<List<JoinedEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<JoinedEvent>> = _upcomingEvents

    private val _pastEvents = MutableStateFlow<List<JoinedEvent>>(emptyList())
    val pastEvents: StateFlow<List<JoinedEvent>> = _pastEvents

    fun loadEvents() {
        if (userEmail == null) return

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val registrations = db.collection("event_registrations")
                    .whereEqualTo("user", userEmail)
                    .get().await()

                val upcoming = mutableListOf<JoinedEvent>()
                val past = mutableListOf<JoinedEvent>()

                for (doc in registrations) {
                    val eventId = doc.getString("eventId") ?: continue
                    val eventDoc = db.collection("events").document(eventId).get().await()
                    val data = eventDoc.data ?: continue
                    val status = data["approvalStatus"] as? String ?: "approved"
                    if (status != "approved") continue

                    val event = JoinedEvent(
                        id = eventId,
                        name = data["eventName"] as? String ?: "Unknown",
                        venue = data["venue"] as? String ?: "-",
                        timing = data["timing"] as? String ?: "-",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: now
                    )

                    if (event.timestamp >= now) upcoming.add(event)
                    else past.add(event)
                }

                _upcomingEvents.value = upcoming
                _pastEvents.value = past

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun leaveEvent(eventId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (userEmail == null) {
            onError("Not logged in")
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("event_registrations")
                    .whereEqualTo("user", userEmail)
                    .whereEqualTo("eventId", eventId)
                    .get().await()

                for (doc in snapshot) {
                    db.collection("event_registrations").document(doc.id).delete()
                }

                onSuccess("Left event successfully")
                loadEvents()

            } catch (e: Exception) {
                onError("Error leaving event")
            }
        }
    }
}
