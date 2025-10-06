package com.vusports.bc220200768.viewmodels.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ParticipantEventData(
    val email: String,
    val name: String,
    val eventId: String,
    val eventName: String,
    val currentScore: Int = 0,
    val currentNotes: String = ""
)

class EventPerformanceViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _participants = MutableStateFlow<List<ParticipantEventData>>(emptyList())
    val participants: StateFlow<List<ParticipantEventData>> = _participants
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    
    fun loadEventParticipants(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get event details first
                val eventDoc = db.collection("events").document(eventId).get().await()
                val eventName = eventDoc.getString("eventName") ?: "Unknown Event"
                
                // Get all registrations for this event
                val registrations = db.collection("event_registrations")
                    .whereEqualTo("eventId", eventId)
                    .get().await()
                
                val participantsList = mutableListOf<ParticipantEventData>()
                
                for (doc in registrations) {
                    val participantEmail = doc.getString("user") ?: continue
                    
                    // Get participant details
                    val userDoc = db.collection("users")
                        .whereEqualTo("email", participantEmail)
                        .get().await()
                    
                    val participantName = userDoc.documents.firstOrNull()?.getString("name") ?: "Unknown"
                    
                    // Check if there's existing performance data
                    val performanceDoc = db.collection("eventPerformance")
                        .whereEqualTo("eventId", eventId)
                        .whereEqualTo("participantEmail", participantEmail)
                        .get().await()
                    
                    var score = 0
                    var notes = ""
                    
                    if (!performanceDoc.isEmpty) {
                        val perfData = performanceDoc.documents.firstOrNull()
                        score = perfData?.getLong("score")?.toInt() ?: 0
                        notes = perfData?.getString("notes") ?: ""
                    }
                    
                    participantsList.add(
                        ParticipantEventData(
                            email = participantEmail,
                            name = participantName,
                            eventId = eventId,
                            eventName = eventName,
                            currentScore = score,
                            currentNotes = notes
                        )
                    )
                }
                
                _participants.value = participantsList
            } catch (e: Exception) {
                _message.value = "Error loading participants: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun recordPerformance(participant: ParticipantEventData, score: Int, notes: String) {
        if (score < 0) {
            _message.value = "Score cannot be negative"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if there's an existing record to update
                val performanceQuery = db.collection("eventPerformance")
                    .whereEqualTo("eventId", participant.eventId)
                    .whereEqualTo("participantEmail", participant.email)
                    .get().await()
                
                val performanceData = mapOf(
                    "eventId" to participant.eventId,
                    "eventName" to participant.eventName,
                    "participantEmail" to participant.email,
                    "participantName" to participant.name,
                    "score" to score,
                    "notes" to notes,
                    "timestamp" to System.currentTimeMillis()
                )
                
                if (performanceQuery.isEmpty) {
                    // Create new record
                    db.collection("eventPerformance")
                        .add(performanceData)
                        .await()
                } else {
                    // Update existing record
                    val docId = performanceQuery.documents.first().id
                    db.collection("eventPerformance")
                        .document(docId)
                        .set(performanceData)
                        .await()
                }
                
                _message.value = "Performance recorded for ${participant.name}"
                
                // Update local state
                _participants.value = _participants.value.map { 
                    if (it.email == participant.email) {
                        it.copy(currentScore = score, currentNotes = notes)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error recording performance: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}