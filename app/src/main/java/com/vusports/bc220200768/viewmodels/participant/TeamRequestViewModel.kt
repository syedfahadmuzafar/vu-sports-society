package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.models.Team
import com.vusports.bc220200768.models.TeamMember
import com.vusports.bc220200768.models.TeamRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeamRequestViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userEmail = auth.currentUser?.email

    private val _pendingRequests = MutableStateFlow<List<TeamRequest>>(emptyList())
    val pendingRequests: StateFlow<List<TeamRequest>> = _pendingRequests

    private val _myTeams = MutableStateFlow<List<Team>>(emptyList())
    val myTeams: StateFlow<List<Team>> = _myTeams

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadPendingRequests()
        loadMyTeams()
    }

    fun loadPendingRequests() {
        if (userEmail == null) return
        
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = db.collection("team_requests")
                    .whereEqualTo("requesterId", userEmail)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()
                
                _pendingRequests.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TeamRequest::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                _message.value = "Error loading requests: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMyTeams() {
        if (userEmail == null) return
        
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = db.collection("teams")
                    .whereArrayContains("memberEmails", userEmail)
                    .get()
                    .await()
                
                _myTeams.value = snapshot.documents.mapNotNull { doc ->
                    val team = doc.toObject(Team::class.java)?.copy(id = doc.id)
                    team
                }
            } catch (e: Exception) {
                _message.value = "Error loading teams: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun respondToTeamRequest(request: TeamRequest, approve: Boolean, reason: String = "") {
        if (userEmail == null) return
        
        viewModelScope.launch {
            _loading.value = true
            try {
                // Update the team request status
                val status = if (approve) "approved" else "rejected"
                val updates = mutableMapOf<String, Any>(
                    "status" to status
                )
                
                if (!approve && reason.isNotBlank()) {
                    updates["rejectionReason"] = reason
                }
                
                db.collection("team_requests")
                    .document(request.id)
                    .update(updates)
                    .await()
                
                // If approved, update the team member status
                if (approve) {
                    db.collection("teams")
                        .document(request.teamId)
                        .get()
                        .await()
                        .let { teamDoc ->
                            val members = teamDoc.get("members") as? List<Map<String, Any>> ?: emptyList()
                            val updatedMembers = members.map { member ->
                                if ((member["email"] as? String) == userEmail) {
                                    mapOf(
                                        "email" to member["email"],
                                        "name" to member["name"],
                                        "status" to "approved",
                                        "isTeamLeader" to member["isTeamLeader"],
                                        "timestamp" to member["timestamp"]
                                    )
                                } else {
                                    member
                                }
                            }
                            
                            db.collection("teams")
                                .document(request.teamId)
                                .update("members", updatedMembers)
                                .await()
                        }
                }
                
                _message.value = if (approve) "Request approved" else "Request rejected"
                loadPendingRequests() // Refresh the list
            } catch (e: Exception) {
                _message.value = "Error updating request: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}