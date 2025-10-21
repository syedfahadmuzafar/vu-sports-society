package com.vusports.bc220200768.viewmodels.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrganizeEventViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _availableEvents = MutableStateFlow<List<AdminEvent>>(emptyList())
    val availableEvents: StateFlow<List<AdminEvent>> = _availableEvents.asStateFlow()
    
    private val _organizedEvents = MutableStateFlow<List<AdminEvent>>(emptyList())
    val organizedEvents: StateFlow<List<AdminEvent>> = _organizedEvents.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    private val _selectedEvent = MutableStateFlow<AdminEvent?>(null)
    val selectedEvent: StateFlow<AdminEvent?> = _selectedEvent.asStateFlow()
    
    private val _teamLineup = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val teamLineup: StateFlow<Map<String, List<String>>> = _teamLineup.asStateFlow()
    
    private val _availableParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val availableParticipants: StateFlow<List<Participant>> = _availableParticipants.asStateFlow()
    
    private val _selectedParticipants = MutableStateFlow<List<String>>(emptyList())
    val selectedParticipants: StateFlow<List<String>> = _selectedParticipants
    
    private val _selectedTeams = MutableStateFlow<List<String>>(emptyList())
    val selectedTeams: StateFlow<List<String>> = _selectedTeams.asStateFlow()
    
    private val _availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val availableTeams: StateFlow<List<TeamInfo>> = _availableTeams.asStateFlow()
    
    private val _categoryRoles = MutableStateFlow<List<String>>(emptyList())
    val categoryRoles: StateFlow<List<String>> = _categoryRoles.asStateFlow()
    
    private val _teamMembers = MutableStateFlow<Map<String, List<TeamMember>>>(emptyMap())
    val teamMembers: StateFlow<Map<String, List<TeamMember>>> = _teamMembers.asStateFlow()
    
    private val _teamMemberRoles = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val teamMemberRoles: StateFlow<Map<String, Map<String, String>>> = _teamMemberRoles.asStateFlow()
    
    init {
        loadEvents()
        loadTeams()
    }
    
    fun loadEvents() {
        _isLoading.value = true
        val coachEmail = auth.currentUser?.email ?: return
        
        viewModelScope.launch {
            // Get coach expertise
            firestore.collection("users")
                .document(coachEmail)
                .get()
                .addOnSuccessListener { coachDoc ->
                    val expertise = when (val exp = coachDoc.get("expertise")) {
                        is String -> exp.lowercase()
                        is List<*> -> exp.filterIsInstance<String>().joinToString(",").lowercase()
                        else -> ""
                    }
                    
                    if (expertise.isNotEmpty()) {
                        // Find admin events matching coach expertise
                        firestore.collection("events")
                            .whereEqualTo("coachOrganized", false)
                            .get()
                            .addOnSuccessListener { eventDocs ->
                                val events = eventDocs.documents.mapNotNull { doc ->
                                    val category = doc.getString("category")?.lowercase() ?: ""
                                    val assignedCoaches = doc.get("assignedCoaches") as? List<String> ?: emptyList()
                                    
                                    // Include events where coach is assigned (regardless of category)
                                    if (assignedCoaches.contains(coachEmail)) {
                                        AdminEvent(
                                            id = doc.id,
                                            name = doc.getString("name") ?: "",
                                            venue = doc.getString("venue") ?: "",
                                            timing = doc.getString("timing") ?: "",
                                            date = doc.getString("date") ?: "",
                                            category = doc.getString("category") ?: "",
                                            coachOrganized = doc.getBoolean("coachOrganized") ?: false,
                                            organizingCoach = doc.getString("organizingCoach") ?: ""
                                        )
                                    } else null
                                }
                                _availableEvents.value = events
                                
                                // Load events already organized by this coach
                                firestore.collection("events")
                                    .whereEqualTo("organizingCoach", coachEmail)
                                    .whereEqualTo("coachOrganized", true)
                                    .get()
                                    .addOnSuccessListener { organizedDocs ->
                                        val organizedEvents = organizedDocs.documents.mapNotNull { doc ->
                                            val teams = doc.get("teams") as? List<String> ?: emptyList()
                                            val selectedParticipants = doc.get("selectedParticipants") as? List<String> ?: emptyList()
                                            
                                            AdminEvent(
                                                id = doc.id,
                                                name = doc.getString("name") ?: "",
                                                venue = doc.getString("venue") ?: "",
                                                timing = doc.getString("timing") ?: "",
                                                date = doc.getString("date") ?: "",
                                                category = doc.getString("category")?.lowercase() ?: "",
                                                coachOrganized = doc.getBoolean("coachOrganized") ?: false,
                                                organizingCoach = doc.getString("organizingCoach") ?: "",
                                                selectedParticipants = selectedParticipants,
                                                teams = teams
                                            )
                                        }
                                        _organizedEvents.value = organizedEvents
                                        _isLoading.value = false
                                    }
                                    .addOnFailureListener {
                                        _message.value = "Failed to load organized events: ${it.message}"
                                        _isLoading.value = false
                                    }
                            }
                            .addOnFailureListener {
                                _message.value = "Failed to load events: ${it.message}"
                                _isLoading.value = false
                            }
                    } else {
                        _message.value = "Coach expertise not found"
                        _isLoading.value = false
                    }
                }
                .addOnFailureListener {
                    _message.value = "Failed to load coach data: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun selectEvent(event: AdminEvent?) {
        _selectedEvent.value = event
        loadCategoryRoles(event?.category ?: "")
        loadParticipantsForCategory(event?.category ?: "")
    }
    
    private fun loadCategoryRoles(category: String) {
        firestore.collection("categories")
            .whereEqualTo("name", category)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val roles = result.documents[0].get("roles") as? List<String> ?: emptyList()
                    _categoryRoles.value = roles
                }
            }
    }
    
    private fun loadParticipantsForCategory(category: String) {
        firestore.collection("users")
            .whereEqualTo("role", "participant")
            .get()
            .addOnSuccessListener { result ->
                val participants = result.documents.mapNotNull { doc ->
                    val sports = when (val sportsField = doc.get("sports")) {
                        is String -> sportsField.lowercase()
                        is List<*> -> sportsField.filterIsInstance<String>().joinToString(",").lowercase()
                        else -> ""
                    }
                    
                    if (sports.contains(category.lowercase())) {
                        Participant(
                            email = doc.id,
                            name = doc.getString("name") ?: "",
                            sports = sports
                        )
                    } else null
                }
                _availableParticipants.value = participants
            }
    }
    
    fun toggleParticipantSelection(participant: Participant) {
        val currentList = _selectedParticipants.value.toMutableList()
        if (currentList.contains(participant.email)) {
            currentList.remove(participant.email)
        } else {
            currentList.add(participant.email)
        }
        _selectedParticipants.value = currentList
    }
    
    fun isParticipantSelected(email: String): Boolean {
        return _selectedParticipants.value.contains(email)
    }
    
    fun loadTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val coachEmail = auth.currentUser?.email ?: return@launch
                
                // Get coach expertise
                val coachDoc = firestore.collection("users").document(coachEmail).get().await()
                val expertise = when (val exp = coachDoc.get("expertise")) {
                    is String -> listOf(exp.lowercase())
                    is List<*> -> exp.filterIsInstance<String>().map { it.lowercase() }
                    else -> emptyList()
                }
                
                // Get the current event category for filtering
                val eventCategory = _selectedEvent.value?.category?.lowercase()
                
                // Get all teams where this coach is the creator or assigned coach
                val snapshot = firestore.collection("teams")
                    .get()
                    .await()
                
                val teamsList = snapshot.documents.mapNotNull { doc ->
                    val teamId = doc.id
                    val teamName = doc.getString("name") ?: doc.getString("teamName") ?: return@mapNotNull null
                    val teamCoach = doc.getString("coach") ?: ""
                    val teamCreator = doc.getString("creator") ?: ""
                    val coachApproved = doc.getBoolean("coachApproved") ?: false
                    val status = doc.getString("status") ?: ""
                    val teamCategory = doc.getString("category")?.lowercase() ?: doc.getString("sport")?.lowercase() ?: ""
                    
                    // Filter by category if event is selected
                    val categoryMatches = if (eventCategory != null && eventCategory.isNotEmpty()) {
                        // Strict matching when event is selected
                        teamCategory == eventCategory
                    } else {
                        // When no event selected or filtering by coach expertise
                        expertise.isEmpty() || expertise.contains(teamCategory)
                    }
                    
                    // Include team if:
                    // 1. Coach is the assigned coach, or
                    // 2. Coach is the creator, or
                    // 3. Team is approved by any coach
                    // AND team category matches event category or coach expertise
                    if ((teamCoach == coachEmail || 
                        teamCreator == coachEmail || 
                        (coachApproved && status == "approved")) && categoryMatches) {
                        TeamInfo(
                            id = teamId,
                            name = teamName,
                            category = teamCategory
                        )
                    } else null
                }
                
                _availableTeams.value = teamsList
                
                if (teamsList.isEmpty()) {
                    if (eventCategory != null && eventCategory.isNotEmpty()) {
                        _message.value = "No teams available for category: $eventCategory"
                    } else {
                        _message.value = "No teams available. Please create or approve teams first."
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error loading teams: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleTeamSelection(teamId: String) {
        val currentSelection = _selectedTeams.value.toMutableList()
        if (currentSelection.contains(teamId)) {
            currentSelection.remove(teamId)
        } else {
            currentSelection.add(teamId)
            // Load team members when a team is selected
            loadTeamMembers(teamId)
        }
        _selectedTeams.value = currentSelection
    }
    
    private fun loadTeamMembers(teamId: String) {
        viewModelScope.launch {
            try {
                val teamDoc = firestore.collection("teams").document(teamId).get().await()
                
                // Get members data
                val members = teamDoc.get("members") as? List<Map<String, Any>> ?: emptyList()
                val memberEmails = teamDoc.get("memberEmails") as? List<String> ?: emptyList()
                
                // Convert to TeamMember objects
                val teamMembersList = members.mapNotNull { memberMap ->
                    val email = memberMap["email"] as? String ?: return@mapNotNull null
                    val name = memberMap["name"] as? String ?: ""
                    val status = memberMap["status"] as? String ?: "pending"
                    
                    // Check if role is already assigned in the database
                    val existingRole = memberMap["role"] as? String ?: ""
                    
                    TeamMember(
                        email = email,
                        name = name,
                        status = status,
                        role = existingRole
                    )
                }
                
                // Update the team members state
                val currentTeamMembers = _teamMembers.value.toMutableMap()
                currentTeamMembers[teamId] = teamMembersList
                _teamMembers.value = currentTeamMembers
                
                // Initialize roles map for this team if not already present
                if (!_teamMemberRoles.value.containsKey(teamId)) {
                    val currentRoles = _teamMemberRoles.value.toMutableMap()
                    val memberRoles = teamMembersList
                        .filter { it.role.isNotEmpty() }
                        .associate { it.email to it.role }
                        .toMutableMap()
                    
                    currentRoles[teamId] = memberRoles
                    _teamMemberRoles.value = currentRoles
                }
                
            } catch (e: Exception) {
                _message.value = "Error loading team members: ${e.message}"
            }
        }
    }
    
    fun isTeamSelected(teamId: String): Boolean {
        return _selectedTeams.value.contains(teamId)
    }
    
    fun assignRoleToTeamMember(teamId: String, memberEmail: String, role: String) {
        // Update the role in our local state
        val currentRoles = _teamMemberRoles.value.toMutableMap()
        val teamRoles = currentRoles[teamId]?.toMutableMap() ?: mutableMapOf()
        teamRoles[memberEmail] = role
        currentRoles[teamId] = teamRoles
        _teamMemberRoles.value = currentRoles
        
        // Update the role in Firestore
        viewModelScope.launch {
            try {
                val teamDoc = firestore.collection("teams").document(teamId).get().await()
                val members = teamDoc.get("members") as? List<Map<String, Any>> ?: return@launch
                
                // Find the member and update their role
                val updatedMembers = members.map { memberMap ->
                    val email = memberMap["email"] as? String
                    if (email == memberEmail) {
                        // Update this member's role
                        memberMap.toMutableMap().apply { 
                            put("role", role)
                        }
                    } else {
                        memberMap
                    }
                }
                
                // Update the team document
                firestore.collection("teams").document(teamId)
                    .update("members", updatedMembers)
                    .addOnSuccessListener {
                        _message.value = "Role assigned successfully"
                    }
                    .addOnFailureListener { e ->
                        _message.value = "Failed to assign role: ${e.message}"
                    }
                
            } catch (e: Exception) {
                _message.value = "Error assigning role: ${e.message}"
            }
        }
    }
    
    fun organizeEvent() {
        val event = _selectedEvent.value ?: return
        val coachEmail = auth.currentUser?.email ?: return
        
        if (_selectedParticipants.value.isEmpty() && _selectedTeams.value.isEmpty()) {
            _message.value = "Please select at least one participant or team"
            return
        }
        
        _isLoading.value = true
        
        // Include team member roles in the event data
        val teamRolesData = _teamMemberRoles.value
        
        firestore.collection("events").document(event.id)
            .update(
                mapOf(
                    "coachOrganized" to true,
                    "organizingCoach" to coachEmail,
                    "teamLineup" to _teamLineup.value,
                    "selectedParticipants" to _selectedParticipants.value,
                    "teams" to _selectedTeams.value,
                    "teamMemberRoles" to teamRolesData
                )
            )
            .addOnSuccessListener {
                _message.value = "Event organized successfully"
                _isLoading.value = false
                loadEvents() // Refresh the lists
            }
            .addOnFailureListener {
                _message.value = "Failed to organize event: ${it.message}"
                _isLoading.value = false
            }
    }
    
    fun getTeamMemberRole(teamId: String, memberEmail: String): String {
        return _teamMemberRoles.value[teamId]?.get(memberEmail) ?: ""
    }
    
    fun hasAssignedRoles(teamId: String): Boolean {
        return _teamMemberRoles.value[teamId]?.isNotEmpty() == true
    }
    
    fun assignParticipantToRole(participant: Participant, role: String) {
        val currentLineup = _teamLineup.value.toMutableMap()
        val participantsForRole = currentLineup[role]?.toMutableList() ?: mutableListOf()
        
        // Add participant if not already assigned to this role
        if (!participantsForRole.contains(participant.email)) {
            participantsForRole.add(participant.email)
            currentLineup[role] = participantsForRole
            _teamLineup.value = currentLineup
        }
    }
    
    fun removeParticipantFromRole(participantEmail: String, role: String) {
        val currentLineup = _teamLineup.value.toMutableMap()
        val participantsForRole = currentLineup[role]?.toMutableList() ?: return
        
        participantsForRole.remove(participantEmail)
        
        if (participantsForRole.isEmpty()) {
            currentLineup.remove(role)
        } else {
            currentLineup[role] = participantsForRole
        }
        
        _teamLineup.value = currentLineup
    }
    
    fun submitMatchResults(results: Map<String, String>) {
        val event = _selectedEvent.value ?: return
        
        _isLoading.value = true
        
        firestore.collection("events").document(event.id)
            .update(
                mapOf(
                    "matchResults" to results,
                    "resultsApproved" to false
                )
            )
            .addOnSuccessListener {
                _message.value = "Match results submitted successfully"
                _isLoading.value = false
            }
            .addOnFailureListener {
                _message.value = "Failed to submit results: ${it.message}"
                _isLoading.value = false
            }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}

data class AdminEvent(
    val id: String = "",
    val name: String = "",
    val venue: String = "",
    val timing: String = "",
    val date: String = "",
    val category: String = "",
    val coachOrganized: Boolean = false,
    val organizingCoach: String = "",
    val selectedParticipants: List<String> = emptyList(),
    val teams: List<String> = emptyList()
)

data class Participant(
    val email: String = "",
    val name: String = "",
    val sports: String = ""
)

data class TeamInfo(
    val id: String = "",
    val name: String = "",
    val category: String = ""
)

data class TeamMember(
    val email: String = "",
    val name: String = "",
    val status: String = "",
    val role: String = ""
)