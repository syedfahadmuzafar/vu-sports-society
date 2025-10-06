package com.vusports.bc220200768.viewmodel.coach

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.vusports.bc220200768.components.ParticipantProfile

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val location = MutableStateFlow("")
    val dateTime = MutableStateFlow("") // Use Long for timestamp if needed
    val loading = MutableStateFlow(false)
    val feedback = MutableStateFlow("")

    private val _participants = MutableStateFlow<List<ParticipantProfile>>(emptyList())
    val participants: StateFlow<List<ParticipantProfile>> = _participants

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected

    fun toggleParticipant(email: String) {
        _selected.value = _selected.value.toMutableSet().apply {
            if (contains(email)) remove(email) else add(email)
        }
    }

    fun loadParticipants() {
        loading.value = true
        db.collection("users").whereEqualTo("role", "participant").get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull {
                    val name = it.getString("name") ?: return@mapNotNull null
                    val email = it.id
                    val skills = it.getString("skills") ?: ""
                    ParticipantProfile(name, email, skills)
                }
                _participants.value = list
                loading.value = false
            }
            .addOnFailureListener {
                feedback.value = "Failed to load participants."
                loading.value = false
            }
    }

    fun createEvent(coachEmail: String) {
        if (title.value.isBlank() || description.value.isBlank() || location.value.isBlank()) {
            feedback.value = "All fields are required."
            return
        }
        loading.value = true
        val data = mapOf(
            "eventName" to title.value,
            "description" to description.value,
            "location" to location.value,
            "createdBy" to coachEmail,
            "dateTime" to System.currentTimeMillis(),
            "invitedParticipants" to _selected.value.toList()
        )
        db.collection("events").add(data)
            .addOnSuccessListener { eventRef ->
                // Create event registrations for invited participants
                _selected.value.forEach { participantEmail ->
                    val registrationData = mapOf(
                        "eventId" to eventRef.id,
                        "user" to participantEmail,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("event_registrations").add(registrationData)
                }
                feedback.value = "Event created successfully!"
                title.value = ""
                description.value = ""
                location.value = ""
                _selected.value = emptySet()
            }
            .addOnFailureListener {
                feedback.value = "Failed to create event."
            }
            .addOnCompleteListener {
                loading.value = false
            }
    }
}
