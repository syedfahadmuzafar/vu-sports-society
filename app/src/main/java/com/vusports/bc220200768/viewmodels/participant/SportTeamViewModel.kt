package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ParticipantInfo(
    val email: String,
    val name: String,
    val image: String = ""
)

class SportTeamViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userEmail = auth.currentUser?.email

    private val _approvedSports = MutableStateFlow<List<String>>(emptyList())
    val approvedSports: StateFlow<List<String>> = _approvedSports

    private val _selectedSport = MutableStateFlow<String?>(null)
    val selectedSport: StateFlow<String?> = _selectedSport

    private val _teamName = MutableStateFlow("")
    val teamName: StateFlow<String> = _teamName

    private val _participants = MutableStateFlow<List<ParticipantInfo>>(emptyList())
    val participants: StateFlow<List<ParticipantInfo>> = _participants

    private val _selectedParticipants = MutableStateFlow<List<String>>(emptyList())
    val selectedParticipants: StateFlow<List<String>> = _selectedParticipants

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _feedback = MutableStateFlow("")
    val feedback: StateFlow<String> = _feedback

    init {
        loadApprovedSports()
    }

    private fun loadApprovedSports() {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (userEmail != null) {
                    val snapshot = db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .await()
                    
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {
                        val approvedPrefs = doc.get("approved_preferences") as? List<String> ?: emptyList()
                        _approvedSports.value = approvedPrefs
                    }
                }
            } catch (e: Exception) {
                _feedback.value = "Error loading approved sports: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectSport(sport: String) {
        _selectedSport.value = sport
        loadParticipantsForSport(sport)
    }

    private fun loadParticipantsForSport(sport: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("role", "participant")
                    .whereArrayContains("approved_preferences", sport)
                    .get()
                    .await()
                
                val participantsList = snapshot.documents.mapNotNull { doc ->
                    val email = doc.getString("email") ?: return@mapNotNull null
                    if (email == userEmail) return@mapNotNull null // Skip current user
                    
                    ParticipantInfo(
                        email = email,
                        name = doc.getString("name") ?: "Unknown",
                        image = doc.getString("image") ?: ""
                    )
                }
                
                _participants.value = participantsList
            } catch (e: Exception) {
                _feedback.value = "Error loading participants: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setTeamName(name: String) {
        _teamName.value = name
    }

    fun toggleParticipantSelection(email: String) {
        val currentList = _selectedParticipants.value.toMutableList()
        if (currentList.contains(email)) {
            currentList.remove(email)
        } else {
            currentList.add(email)
        }
        _selectedParticipants.value = currentList
    }

    fun createTeam() {
        val name = _teamName.value.trim()
        val sport = _selectedSport.value
        
        if (name.isEmpty() || userEmail == null) {
            _feedback.value = "Please enter a valid team name."
            return
        }
        
        if (sport == null) {
            _feedback.value = "Please select a sport."
            return
        }

        _loading.value = true

        // Create a unique team ID using sport and name
        val teamId = "${sport}_${name.replace(" ", "_")}"

        // Check if team already exists
        db.collection("teams").document(teamId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _feedback.value = "A team with this name already exists for $sport."
                    _loading.value = false
                } else {
                    // Create team with sport-specific information
                    val selectedMembers = _selectedParticipants.value.toMutableList()
                    
                    // Create team members list with approval status
                    val teamMembers = mutableListOf<Map<String, Any>>()
                    
                    // Add creator as team leader (auto-approved)
                    teamMembers.add(mapOf(
                        "email" to userEmail,
                        "name" to (name ?: "Team Leader"),
                        "status" to "approved",
                        "isTeamLeader" to true,
                        "timestamp" to System.currentTimeMillis()
                    ))
                    
                    // Add selected members (pending approval)
                    selectedMembers.forEach { email ->
                        // Find participant name from participants list
                        val participant = _participants.value.find { it.email == email }
                        teamMembers.add(mapOf(
                            "email" to email,
                            "name" to (participant?.name ?: "Team Member"),
                            "status" to "pending",
                            "isTeamLeader" to false,
                            "timestamp" to System.currentTimeMillis()
                        ))
                        
                        // Create team request for each member
                        db.collection("team_requests").add(mapOf(
                            "teamId" to teamId,
                            "teamName" to name,
                            "sport" to sport,
                            "requesterId" to email,
                            "requesterName" to (participant?.name ?: "Team Member"),
                            "status" to "pending",
                            "timestamp" to System.currentTimeMillis()
                        ))
                    }
                    
                    // Store all member emails for easy querying
                    val memberEmails = mutableListOf(userEmail)
                    memberEmails.addAll(selectedMembers)
                    
                    db.collection("teams").document(teamId)
                        .set(
                            mapOf(
                                "teamName" to name,
                                "sport" to sport,
                                "members" to teamMembers,
                                "memberEmails" to memberEmails,
                                "creator" to userEmail,
                                "status" to "pending", // Teams need admin approval
                                "coachApproved" to false,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            _feedback.value = "Team '$name' for $sport created successfully! Waiting for member and coach approval."
                            _loading.value = false
                            // Reset selections
                            _teamName.value = ""
                            _selectedParticipants.value = emptyList()
                        }
                        .addOnFailureListener {
                            _feedback.value = "Failed to create team: ${it.message}"
                            _loading.value = false
                        }
                }
            }
            .addOnFailureListener {
                _feedback.value = "Error processing request: ${it.message}"
                _loading.value = false
            }
    }
}