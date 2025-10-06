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

class EventRegistrationViewModel : ViewModel() {
    private val db = FirebaseUtil.firestore
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _registeredEvents = MutableStateFlow<List<String>>(emptyList())
    val registeredEvents: StateFlow<List<String>> = _registeredEvents

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadEvents()
        loadUserRegistrations()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("events").get().await()
                _events.value = snapshot.documents.map {
                    Event(
                        id = it.id,
                        name = it.getString("eventName") ?: "",
                        venue = it.getString("venue") ?: "Unknown",
                        timing = it.getString("timing") ?: "Unknown"
                    )
                }
            } catch (_: Exception) { }
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
            "event" to event.name,
            "user" to userEmail,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("event_registrations").add(data)
            .addOnSuccessListener {
                _registeredEvents.value = _registeredEvents.value + event.name
                onResult(true, "Registered for ${event.name}")
            }
            .addOnFailureListener {
                onResult(false, "Registration failed")
            }
    }
}
