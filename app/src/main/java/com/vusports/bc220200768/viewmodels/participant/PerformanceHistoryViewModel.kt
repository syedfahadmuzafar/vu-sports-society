package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PerformanceStats(
    val matchesPlayed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val coachPoints: Int = 0,
    val pastEvents: List<EventPerformance> = emptyList()
)

data class EventPerformance(
    val eventId: String,
    val eventName: String,
    val date: String,
    val score: Int = 0,
    val notes: String = ""
)

class PerformanceHistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email
    
    private val _performanceStats = MutableStateFlow(PerformanceStats())
    val performanceStats: StateFlow<PerformanceStats> = _performanceStats
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    fun loadPerformanceStats() {
        if (userEmail == null) {
            _errorMessage.value = "User not logged in"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                // Load basic performance stats
                val statsDoc = db.collection("performanceStats").document(userEmail).get().await()
                
                val matchesPlayed = statsDoc.getLong("matchesPlayed")?.toInt() ?: 0
                val goals = statsDoc.getLong("goals")?.toInt() ?: 0
                val assists = statsDoc.getLong("assists")?.toInt() ?: 0
                
                // Get coach points from leaderboard
                val leaderboardDoc = db.collection("leaderboard").document(userEmail).get().await()
                val coachPoints = leaderboardDoc.getLong("points")?.toInt() ?: 0
                
                // Get past events performance
                val pastEventsList = mutableListOf<EventPerformance>()
                
                // Get all events the user has joined
                val registrations = db.collection("event_registrations")
                    .whereEqualTo("user", userEmail)
                    .get().await()
                
                for (doc in registrations) {
                    val eventId = doc.getString("eventId") ?: continue
                    val eventDoc = db.collection("events").document(eventId).get().await()
                    
                    if (eventDoc.exists()) {
                        val eventName = eventDoc.getString("eventName") ?: "Unknown Event"
                        val timing = eventDoc.getString("timing") ?: "Unknown Date"
                        
                        // Check if there's performance data for this event
                        val performanceDoc = db.collection("eventPerformance")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("participantEmail", userEmail)
                            .get().await()
                        
                        var score = 0
                        var notes = ""
                        
                        if (!performanceDoc.isEmpty) {
                            val perfData = performanceDoc.documents.firstOrNull()
                            score = perfData?.getLong("score")?.toInt() ?: 0
                            notes = perfData?.getString("notes") ?: ""
                        }
                        
                        pastEventsList.add(EventPerformance(
                            eventId = eventId,
                            eventName = eventName,
                            date = timing,
                            score = score,
                            notes = notes
                        ))
                    }
                }
                
                _performanceStats.value = PerformanceStats(
                    matchesPlayed = matchesPlayed,
                    goals = goals,
                    assists = assists,
                    coachPoints = coachPoints,
                    pastEvents = pastEventsList
                )
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load performance stats: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}