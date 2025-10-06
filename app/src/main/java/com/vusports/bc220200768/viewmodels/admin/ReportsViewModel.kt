package com.vusports.bc220200768.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SportStatistics(
    val sportName: String,
    val participantCount: Int,
    val coachCount: Int,
    val teamCount: Int,
    val eventCount: Int
)

data class UserRoleCount(
    val role: String,
    val count: Int
)

class ReportsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _sportStatistics = MutableStateFlow<List<SportStatistics>>(emptyList())
    val sportStatistics: StateFlow<List<SportStatistics>> = _sportStatistics
    
    private val _userRoleCounts = MutableStateFlow<List<UserRoleCount>>(emptyList())
    val userRoleCounts: StateFlow<List<UserRoleCount>> = _userRoleCounts
    
    private val _totalParticipants = MutableStateFlow(0)
    val totalParticipants: StateFlow<Int> = _totalParticipants
    
    private val _totalCoaches = MutableStateFlow(0)
    val totalCoaches: StateFlow<Int> = _totalCoaches
    
    private val _totalEvents = MutableStateFlow(0)
    val totalEvents: StateFlow<Int> = _totalEvents
    
    private val _totalTeams = MutableStateFlow(0)
    val totalTeams: StateFlow<Int> = _totalTeams
    
    fun loadReportData() {
        _isLoading.value = true
        
        viewModelScope.launch {
            // Load categories (sports)
            db.collection("categories").get()
                .addOnSuccessListener { categoriesSnapshot ->
                    val categories = categoriesSnapshot.documents.mapNotNull { it.getString("name") }
                    
                    // For each category, get statistics
                    val statisticsList = mutableListOf<SportStatistics>()
                    var categoriesProcessed = 0
                    
                    if (categories.isEmpty()) {
                        _isLoading.value = false
                        return@addOnSuccessListener
                    }
                    
                    categories.forEach { sportName ->
                        var participantCount = 0
                        var coachCount = 0
                        var teamCount = 0
                        var eventCount = 0
                        
                        // Count participants with this sport preference
                        db.collection("users")
                            .whereEqualTo("role", "participant")
                            .whereArrayContains("preferences", sportName)
                            .get()
                            .addOnSuccessListener { participantsSnapshot ->
                                participantCount = participantsSnapshot.size()
                                
                                // Count coaches with this expertise
                                db.collection("users")
                                    .whereEqualTo("role", "coach")
                                    .whereArrayContains("expertise", sportName)
                                    .get()
                                    .addOnSuccessListener { coachesSnapshot ->
                                        coachCount = coachesSnapshot.size()
                                        
                                        // Count teams for this sport
                                        db.collection("teams")
                                            .whereEqualTo("sport", sportName)
                                            .get()
                                            .addOnSuccessListener { teamsSnapshot ->
                                                teamCount = teamsSnapshot.size()
                                                
                                                // Count events for this sport
                                                db.collection("events")
                                                    .whereEqualTo("category", sportName)
                                                    .get()
                                                    .addOnSuccessListener { eventsSnapshot ->
                                                        eventCount = eventsSnapshot.size()
                                                        
                                                        // Add statistics for this sport
                                                        statisticsList.add(
                                                            SportStatistics(
                                                                sportName = sportName,
                                                                participantCount = participantCount,
                                                                coachCount = coachCount,
                                                                teamCount = teamCount,
                                                                eventCount = eventCount
                                                            )
                                                        )
                                                        
                                                        categoriesProcessed++
                                                        
                                                        // If all categories processed, update the state
                                                        if (categoriesProcessed == categories.size) {
                                                            _sportStatistics.value = statisticsList
                                                            calculateTotals(statisticsList)
                                                            loadUserRoleCounts()
                                                        }
                                                    }
                                            }
                                    }
                            }
                    }
                }
                .addOnFailureListener {
                    _isLoading.value = false
                }
        }
    }
    
    private fun loadUserRoleCounts() {
        db.collection("users").get()
            .addOnSuccessListener { usersSnapshot ->
                val roleCounts = mutableMapOf<String, Int>()
                
                usersSnapshot.documents.forEach { document ->
                    val role = document.getString("role") ?: "unknown"
                    roleCounts[role] = (roleCounts[role] ?: 0) + 1
                }
                
                _userRoleCounts.value = roleCounts.map { (role, count) ->
                    UserRoleCount(role, count)
                }
                
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
    
    private fun calculateTotals(statistics: List<SportStatistics>) {
        _totalParticipants.value = statistics.sumOf { it.participantCount }
        _totalCoaches.value = statistics.sumOf { it.coachCount }
        _totalEvents.value = statistics.sumOf { it.eventCount }
        _totalTeams.value = statistics.sumOf { it.teamCount }
    }
}