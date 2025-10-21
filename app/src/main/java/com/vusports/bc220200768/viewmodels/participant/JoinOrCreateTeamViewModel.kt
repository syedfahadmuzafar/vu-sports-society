package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinOrCreateTeamViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    val teamName = MutableStateFlow("")
    private val _feedback = MutableStateFlow("")
    val feedback: StateFlow<String> = _feedback

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun setTeamName(name: String) {
        teamName.value = name
    }

    fun submitTeam() {
        val name = teamName.value.trim()
        if (name.isEmpty() || userEmail == null) {
            _feedback.value = "Please enter a valid team name."
            return
        }

        _loading.value = true

        db.collection("teams").document(name)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Join team - create a join request that needs approval
                    db.collection("team_join_requests").add(
                        mapOf(
                            "teamName" to name,
                            "requesterEmail" to userEmail,
                            "status" to "pending",
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                    .addOnSuccessListener {
                        _feedback.value = "Join request sent for team '$name'! Waiting for approval."
                        _loading.value = false
                    }
                    .addOnFailureListener {
                        _feedback.value = "Failed to send join request."
                        _loading.value = false
                    }
                } else {
                    // Create team with creator as team leader and pending coach approval
                    // Create team members list with approval status
                    val teamMembers = mutableListOf<Map<String, Any>>()
                    
                    // Add creator as team leader (auto-approved)
                    teamMembers.add(mapOf(
                        "email" to userEmail,
                        "status" to "approved",
                        "isTeamLeader" to true,
                        "timestamp" to System.currentTimeMillis()
                    ))
                    
                    db.collection("teams").document(name)
                        .set(
                            mapOf(
                                "name" to name,
                                "members" to teamMembers,
                                "memberEmails" to listOf(userEmail),
                                "creator" to userEmail,
                                "coachApproved" to false,
                                "status" to "pending", // Teams need coach approval
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            _feedback.value = "Team '$name' created successfully! You are assigned as team leader. Waiting for coach approval."
                            _loading.value = false
                        }
                        .addOnFailureListener {
                            _feedback.value = "Failed to create team."
                            _loading.value = false
                        }
                }
            }
            .addOnFailureListener {
                _feedback.value = "Error processing request."
                _loading.value = false
            }
    }
}
