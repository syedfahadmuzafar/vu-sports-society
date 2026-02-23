package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.components.FirebaseUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Event(
    val id: String = "",
    val name: String = "",
    val venue: String = "",
    val timing: String = ""
)

data class EventRejection(
    val eventId: String = "",
    val eventName: String = "",
    val userId: String = "",
    val reason: String = "",
    val timestamp: Long = 0
)

class EventRegistrationViewModel : ViewModel() {
    private val db = FirebaseUtil.firestore
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _availableEvents = MutableStateFlow<List<Event>>(emptyList())
    val availableEvents: StateFlow<List<Event>> = _availableEvents

    private val _joinedEvents = MutableStateFlow<List<Event>>(emptyList())
    val joinedEvents: StateFlow<List<Event>> = _joinedEvents

    private val _registeredEvents = MutableStateFlow<List<String>>(emptyList())
    val registeredEvents: StateFlow<List<String>> = _registeredEvents

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadEvents()
        loadUserRegistrations()
    }
    
    fun rejectEvent(event: Event, reason: String, onResult: (Boolean, String) -> Unit) {
        if (userEmail == null) {
            onResult(false, "User not logged in")
            return
        }

        val rejection = EventRejection(
            eventId = event.id,
            eventName = event.name,
            userId = userEmail,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )

        db.collection("event_rejections").add(rejection)
            .addOnSuccessListener {
                _message.value = "Event rejected successfully"
                onResult(true, "Event rejected successfully")
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to reject event: ${e.message}"
                onResult(false, "Failed to reject event: ${e.message}")
            }
    }
    
    fun clearMessage() {
        _message.value = null
    }

    fun loadEvents() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email
                if (userEmail == null) {
                    _loading.value = false
                    return@launch
                }
                
                // First, get all teams where the user is a member with approved status
                val userTeams = mutableListOf<String>()
                val teamSnapshot = db.collection("teams").get().await()
                
                for (teamDoc in teamSnapshot) {
                    val members = teamDoc.get("members") as? List<Map<String, Any>> ?: continue
                    val memberEmails = teamDoc.get("memberEmails") as? List<String> ?: continue
                    
                    // Check if user is in this team's member list
                    if (memberEmails.contains(userEmail)) {
                        // Find the member entry to check status
                        val memberEntry = members.find { it["email"] == userEmail }
                        if (memberEntry != null && memberEntry["status"] == "approved") {
                            userTeams.add(teamDoc.id)
                        }
                    }
                }
                
                // Get user's registered events
                val registrations = db.collection("event_registrations")
                    .whereEqualTo("user", userEmail)
                    .get()
                    .await()
                val joinedEventIds = registrations.documents.mapNotNull { it.getString("eventId") }
                
                // Now get events and filter by team association and coach selection
                val snapshot = db.collection("events").get().await()
                val allEvents = snapshot.documents.mapNotNull { doc ->
                    val status = doc.getString("approvalStatus") ?: "approved"
                    if (status != "approved") return@mapNotNull null
                    val eventTeams = doc.get("teams") as? List<String> ?: emptyList()
                    val selectedParticipants = doc.get("selectedParticipants") as? List<String> ?: emptyList()
                    val coachOrganized = doc.getBoolean("coachOrganized") ?: false
                    
                    // Only show events if:
                    // 1. User is directly selected as a participant by a coach OR
                    // 2. User is a member of a team associated with this event
                    // This ensures admin-created events don't show until coach adds the participant
                    if ((selectedParticipants.contains(userEmail) || 
                        eventTeams.any { teamId -> userTeams.contains(teamId) })) {
                        Event(
                            id = doc.id,
                            name = doc.getString("eventName") ?: "",
                            venue = doc.getString("venue") ?: "Unknown",
                            timing = doc.getString("timing") ?: "Unknown"
                        )
                    } else null
                }
                
                // Split events into available and joined
                val available = mutableListOf<Event>()
                val joined = mutableListOf<Event>()
                
                allEvents.forEach { event ->
                    if (joinedEventIds.contains(event.id)) {
                        joined.add(event)
                    } else {
                        available.add(event)
                    }
                }
                
                _availableEvents.value = available
                _joinedEvents.value = joined
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _loading.value = false
        }
    }

    private fun loadUserRegistrations() {
        viewModelScope.launch {
            if (userEmail != null) {
                val snapshot = db.collection("event_registrations")
                    .whereEqualTo("user", userEmail)
                    .get()
                    .await()
                _registeredEvents.value = snapshot.mapNotNull { it.getString("event") }
            }
        }
    }

    fun registerEvent(event: Event, onResult: (Boolean, String) -> Unit) {
        if (userEmail == null) {
            onResult(false, "User not logged in")
            return
        }

        if (_registeredEvents.value.contains(event.name)) {
            onResult(false, "Already registered")
            return
        }

        val data = mapOf(
            "eventId" to event.id,
            "event" to event.name,
            "user" to userEmail,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("event_registrations").add(data)
            .addOnSuccessListener {
                // Update local state
                _registeredEvents.value = _registeredEvents.value + event.name
                
                // Move event from available to joined
                val updatedAvailable = _availableEvents.value.filter { it.id != event.id }
                _availableEvents.value = updatedAvailable
                
                val updatedJoined = _joinedEvents.value + event
                _joinedEvents.value = updatedJoined
                
                onResult(true, "Registered for ${event.name}")
            }
            .addOnFailureListener {
                onResult(false, "Registration failed")
            }
    }
    
    fun leaveEvent(event: Event, onResult: (Boolean, String) -> Unit) {
        if (userEmail == null) {
            onResult(false, "User not logged in")
            return
        }
        
        viewModelScope.launch {
            try {
                // Find and delete the registration
                val query = db.collection("event_registrations")
                    .whereEqualTo("eventId", event.id)
                    .whereEqualTo("user", userEmail)
                    .get()
                    .await()
                
                if (query.documents.isEmpty()) {
                    onResult(false, "Registration not found")
                    return@launch
                }
                
                // Delete all matching registrations (should be just one)
                var success = true
                for (doc in query.documents) {
                    try {
                        db.collection("event_registrations").document(doc.id).delete().await()
                    } catch (e: Exception) {
                        success = false
                    }
                }
                
                if (success) {
                    // Update local state
                    _registeredEvents.value = _registeredEvents.value.filter { it != event.name }
                    
                    // Move event from joined to available
                    val updatedJoined = _joinedEvents.value.filter { it.id != event.id }
                    _joinedEvents.value = updatedJoined
                    
                    val updatedAvailable = _availableEvents.value + event
                    _availableEvents.value = updatedAvailable
                    
                    onResult(true, "Left event successfully")
                } else {
                    onResult(false, "Failed to leave event")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
