package com.vusports.bc220200768.viewmodels.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    
    private val _categoryRoles = MutableStateFlow<List<String>>(emptyList())
    val categoryRoles: StateFlow<List<String>> = _categoryRoles.asStateFlow()
    
    init {
        loadEvents()
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
                                            AdminEvent(
                                                id = doc.id,
                                                name = doc.getString("name") ?: "",
                                                venue = doc.getString("venue") ?: "",
                                                timing = doc.getString("timing") ?: "",
                                                date = doc.getString("date") ?: "",
                                                category = doc.getString("category")?.lowercase() ?: "",
                                                coachOrganized = doc.getBoolean("coachOrganized") ?: false,
                                                organizingCoach = doc.getString("organizingCoach") ?: ""
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
    
    fun organizeEvent() {
        val event = _selectedEvent.value ?: return
        val coachEmail = auth.currentUser?.email ?: return
        
        _isLoading.value = true
        
        firestore.collection("events").document(event.id)
            .update(
                mapOf(
                    "coachOrganized" to true,
                    "organizingCoach" to coachEmail,
                    "teamLineup" to _teamLineup.value
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
    val organizingCoach: String = ""
)

data class Participant(
    val email: String = "",
    val name: String = "",
    val sports: String = ""
)