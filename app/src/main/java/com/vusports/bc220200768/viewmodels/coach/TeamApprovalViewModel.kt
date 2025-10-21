package com.vusports.bc220200768.viewmodels.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.models.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeamApprovalViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coachEmail = auth.currentUser?.email
    
    private val _pendingTeams = MutableStateFlow<List<Team>>(emptyList())
    val pendingTeams: StateFlow<List<Team>> = _pendingTeams
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    
    init {
        loadPendingTeams()
    }
    
    fun loadPendingTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get coach expertise
                val coachEmail = auth.currentUser?.email ?: return@launch
                val coachDoc = db.collection("users").document(coachEmail).get().await()
                val expertise = when (val exp = coachDoc.get("expertise")) {
                    is String -> listOf(exp.lowercase())
                    is List<*> -> exp.filterIsInstance<String>().map { it.lowercase() }
                    else -> emptyList()
                }
                
                // Get all pending teams
                val snapshot = db.collection("teams")
                    .whereEqualTo("coachApproved", false)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()
                
                val teams = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: doc.getString("teamName") ?: return@mapNotNull null
                    val sport = doc.getString("sport") ?: ""
                    val category = doc.getString("category")?.lowercase() ?: sport.lowercase()
                    val creator = doc.getString("creator") ?: ""
                    val members = doc.get("members") as? List<Map<String, Any>> ?: emptyList()
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    
                    // Only include teams that match coach expertise
                    if (expertise.isEmpty() || expertise.contains(category)) {
                        Team(
                            id = id,
                            name = name,
                            sport = sport,
                            creator = creator,
                            members = members.map { member ->
                                com.vusports.bc220200768.models.TeamMember(
                                    email = member["email"] as? String ?: "",
                                    name = member["name"] as? String ?: "",
                                    status = member["status"] as? String ?: "pending",
                                    isTeamLeader = member["isTeamLeader"] as? Boolean ?: false,
                                    timestamp = member["timestamp"] as? Long ?: 0L
                                )
                            },
                            status = "pending",
                            coachApproved = false,
                            timestamp = timestamp
                        )
                    } else null
                }
                
                _pendingTeams.value = teams
                
                if (teams.isEmpty() && expertise.isNotEmpty()) {
                    _message.value = "No pending teams found matching your expertise: ${expertise.joinToString(", ")}"
                } else if (teams.isEmpty()) {
                    _message.value = "No pending teams found"
                }
            } catch (e: Exception) {
                _message.value = "Error loading teams: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun approveTeam(team: Team) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update team status
                db.collection("teams").document(team.id)
                    .update(
                        mapOf(
                            "coachApproved" to true,
                            "status" to "approved",
                            "coach" to coachEmail
                        )
                    )
                    .await()
                
                // Remove from pending list
                _pendingTeams.value = _pendingTeams.value.filter { it.id != team.id }
                _message.value = "Team '${team.name}' approved successfully!"
            } catch (e: Exception) {
                _message.value = "Error approving team: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun rejectTeam(team: Team, reason: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update team status
                val updates = mutableMapOf<String, Any>(
                    "status" to "rejected"
                )
                
                if (reason.isNotBlank()) {
                    updates["rejectionReason"] = reason
                }
                
                db.collection("teams").document(team.id)
                    .update(updates)
                    .await()
                
                // Remove from pending list
                _pendingTeams.value = _pendingTeams.value.filter { it.id != team.id }
                _message.value = "Team '${team.name}' rejected."
            } catch (e: Exception) {
                _message.value = "Error rejecting team: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}