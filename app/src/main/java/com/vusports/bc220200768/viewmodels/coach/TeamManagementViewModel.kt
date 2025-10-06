package com.vusports.bc220200768.viewmodel.coach

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.components.ParticipantProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TeamManagementViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _teamName = MutableStateFlow("")
    val teamName: StateFlow<String> = _teamName

    private val _schedule = MutableStateFlow("")
    val schedule: StateFlow<String> = _schedule

    private val _feedbackMessage = MutableStateFlow("")
    val feedbackMessage: StateFlow<String> = _feedbackMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _participants = MutableStateFlow<List<ParticipantProfile>>(emptyList())
    val participants: StateFlow<List<ParticipantProfile>> = _participants

    private val _selectedMembers = MutableStateFlow<Set<String>>(emptySet())
    val selectedMembers: StateFlow<Set<String>> = _selectedMembers

    private val _memberRoles = MutableStateFlow<Map<String, String>>(emptyMap())
    val memberRoles: StateFlow<Map<String, String>> = _memberRoles

    private val _pendingRequests = MutableStateFlow<List<ParticipantProfile>>(emptyList())
    val pendingRequests: StateFlow<List<ParticipantProfile>> = _pendingRequests

    fun onTeamNameChange(newName: String) {
        _teamName.value = newName
    }

    fun onScheduleChange(newSchedule: String) {
        _schedule.value = newSchedule
    }

    fun toggleMemberSelection(email: String) {
        _selectedMembers.value = _selectedMembers.value.toMutableSet().apply {
            if (contains(email)) remove(email) else add(email)
        }
    }

    fun assignRole(email: String, role: String) {
        _memberRoles.value = _memberRoles.value.toMutableMap().apply {
            put(email, role)
        }
    }

    fun loadParticipants() {
        _loading.value = true

        db.collection("users").whereEqualTo("role", "participant").get()
            .addOnSuccessListener { snapshot ->
                val all = snapshot.documents.mapNotNull { doc ->
                    val email = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val skills = doc.getString("skills") ?: ""
                    val requestedTeam = doc.getString("requestedTeam")
                    val status = doc.getString("status") ?: "none"

                    val profile = ParticipantProfile(name, email, skills)
                    if (status == "pending" && requestedTeam == _teamName.value) {
                        _pendingRequests.value += profile
                        null
                    } else {
                        profile
                    }
                }
                _participants.value = all
                _loading.value = false
            }
            .addOnFailureListener {
                _feedbackMessage.value = "Failed to load participants."
                _loading.value = false
            }
    }

    fun approveRequest(participant: ParticipantProfile) {
        db.collection("users").document(participant.email)
            .update("status", "approved", "joinedTeam", _teamName.value)
            .addOnSuccessListener {
                _participants.value += participant
                _pendingRequests.value = _pendingRequests.value.filterNot { it.email == participant.email }
            }
    }

    fun createTeam(onResult: (String) -> Unit) {
        if (_teamName.value.isBlank() || _selectedMembers.value.isEmpty()) {
            val message = "Please enter a team name and select members."
            _feedbackMessage.value = message
            onResult(message)
            return
        }

        _loading.value = true

        val teamData = mapOf(
            "teamName" to _teamName.value,
            "members" to _selectedMembers.value.toList(),
            "roles" to _memberRoles.value,
            "schedule" to _schedule.value,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("teams")
            .add(teamData)
            .addOnSuccessListener {
                val successMessage = "Team '${_teamName.value}' created!"
                _feedbackMessage.value = successMessage
                onResult(successMessage)
                _teamName.value = ""
                _selectedMembers.value = emptySet()
                _memberRoles.value = emptyMap()
            }
            .addOnFailureListener {
                val errorMessage = "Failed to create team."
                _feedbackMessage.value = errorMessage
                onResult(errorMessage)
            }
            .addOnCompleteListener {
                _loading.value = false
            }
    }
}
