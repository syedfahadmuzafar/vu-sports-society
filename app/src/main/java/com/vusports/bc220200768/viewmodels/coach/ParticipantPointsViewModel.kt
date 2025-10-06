package com.vusports.bc220200768.viewmodels.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ParticipantPointsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    
    private val _participants = MutableStateFlow<List<ParticipantWithPoints>>(emptyList())
    val participants: StateFlow<List<ParticipantWithPoints>> = _participants
    
    init {
        loadParticipants()
    }
    
    fun loadParticipants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("role", "participant")
                    .whereEqualTo("status", "approved")
                    .get()
                    .await()
                
                val participantsList = snapshot.documents.mapNotNull { doc ->
                    val email = doc.getString("email") ?: return@mapNotNull null
                    val name = doc.getString("name") ?: return@mapNotNull null
                    
                    // Get points from leaderboard collection
                    val pointsDoc = try {
                        db.collection("leaderboard").document(email).get().await()
                    } catch (e: Exception) {
                        null
                    }
                    
                    val points = pointsDoc?.getLong("points")?.toInt() ?: 0
                    
                    ParticipantWithPoints(
                        email = email,
                        name = name,
                        points = points
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
    
    fun awardPoints(email: String, name: String, pointsToAdd: Int) {
        if (pointsToAdd <= 0) {
            _message.value = "Points must be greater than zero"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get current points
                val leaderboardDoc = db.collection("leaderboard").document(email).get().await()
                val currentPoints = leaderboardDoc.getLong("points")?.toInt() ?: 0
                val newPoints = currentPoints + pointsToAdd
                
                // Update or create leaderboard entry
                db.collection("leaderboard").document(email)
                    .set(mapOf(
                        "name" to name,
                        "points" to newPoints,
                        "email" to email
                    ))
                    .await()
                
                _message.value = "$pointsToAdd points awarded to $name"
                
                // Update local state
                _participants.value = _participants.value.map { 
                    if (it.email == email) {
                        it.copy(points = newPoints)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error awarding points: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}

data class ParticipantWithPoints(
    val email: String,
    val name: String,
    val points: Int
)