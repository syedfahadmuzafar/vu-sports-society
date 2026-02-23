package com.vusports.bc220200768.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleEventViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    val eventName = MutableStateFlow("")
    val venue = MutableStateFlow("")
    val timing = MutableStateFlow("")
    val date = MutableStateFlow("")
    val equipment = MutableStateFlow("")
    val staffRequired = MutableStateFlow("")
    val maxParticipants = MutableStateFlow("")
    val logistics = MutableStateFlow("")
    val category = MutableStateFlow("")
    
    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()
    
    private val _assignedCoaches = MutableStateFlow<List<String>>(emptyList())
    val assignedCoaches: StateFlow<List<String>> = _assignedCoaches.asStateFlow()
    
    private val _events = MutableStateFlow<List<EventData>>(emptyList())
    val events: StateFlow<List<EventData>> = _events.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    init {
        loadEvents()
        loadAvailableCategories()
    }
    
    private fun loadAvailableCategories() {
        _isLoading.value = true
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.documents.mapNotNull { it.getString("name") }
                _availableCategories.value = categories
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _snackbarMessage.value = "Failed to load categories: ${e.message}"
                _isLoading.value = false
                // Fallback to default categories if loading fails
                _availableCategories.value = listOf("Cricket", "Football", "Basketball", "Tennis", "Swimming")
            }
    }
    
    fun onCategorySelected(newCategory: String) {
        category.value = newCategory
        // When category changes, find coaches with matching expertise
        findCoachesForCategory(newCategory)
    }
    
    private fun findCoachesForCategory(category: String) {
        _isLoading.value = true
        val lowerCaseCategory = category.lowercase()
        
        firestore.collection("users")
            .whereEqualTo("role", "coach")
            .get()
            .addOnSuccessListener { snapshot ->
                val coachEmails = snapshot.documents.mapNotNull { doc ->
                    val email = doc.id
                    // Get coach expertise and handle different data types
                    val expertise = try {
                        // Try to get as string first
                        (doc.getString("expertise") ?: "").lowercase()
                    } catch (e: Exception) {
                        try {
                            // If not a string, try to get as list
                            val expertiseList = doc.get("expertise") as? List<*>
                            expertiseList?.joinToString(",") { it.toString().lowercase() } ?: ""
                        } catch (e2: Exception) {
                            // If all else fails, use empty string
                            ""
                        }
                    }
                    
                    // Only include coaches whose expertise matches the selected category
                    if (expertise.contains(lowerCaseCategory)) {
                        email
                    } else {
                        null
                    }
                }
                _assignedCoaches.value = coachEmails
                _isLoading.value = false
            }
            .addOnFailureListener {
                _snackbarMessage.value = "❌ Failed to find coaches: ${it.message}"
                _isLoading.value = false
            }
    }
    
    fun loadEvents() {
        _isLoading.value = true
        firestore.collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                val eventsList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("eventName") ?: return@mapNotNull null
                    val venue = doc.getString("venue") ?: "-"
                    val timing = doc.getString("timing") ?: "-"
                    val date = doc.getString("date") ?: "-"
                    val equipment = doc.getString("equipment") ?: "-"
                    val staffRequired = doc.getString("staffRequired") ?: "-"
                    val maxParticipants = doc.getString("maxParticipants") ?: "-"
                    val logistics = doc.getString("logistics") ?: "-"
                    val category = doc.getString("category") ?: "-"
                    val createdByRole = doc.getString("createdByRole") ?: ""
                    val approvalStatus = doc.getString("approvalStatus") ?: "approved"
                    
                    // Get assigned coaches
                    val assignedCoaches = try {
                        (doc.get("assignedCoaches") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    EventData(
                        id = id,
                        name = name,
                        venue = venue,
                        timing = timing,
                        date = date,
                        equipment = equipment,
                        staffRequired = staffRequired,
                        maxParticipants = maxParticipants,
                        logistics = logistics,
                        category = category,
                        assignedCoaches = assignedCoaches,
                        createdByRole = createdByRole,
                        approvalStatus = approvalStatus
                    )
                }
                _events.value = eventsList
                _isLoading.value = false
            }
            .addOnFailureListener {
                _snackbarMessage.value = "❌ Failed to load events: ${it.message}"
                _isLoading.value = false
            }
    }

    fun scheduleEvent() {
        if (eventName.value.isBlank() || venue.value.isBlank() || timing.value.isBlank() || date.value.isBlank() || category.value.isBlank()) {
            _snackbarMessage.value = "⚠️ Event name, venue, timing, date, and category are required"
            return
        }

        val eventData = mapOf(
            "eventName" to eventName.value,
            "venue" to venue.value,
            "timing" to timing.value,
            "date" to date.value,
            "equipment" to equipment.value,
            "staffRequired" to staffRequired.value,
            "maxParticipants" to maxParticipants.value,
            "logistics" to logistics.value,
            "category" to category.value.lowercase(), // Store category in lowercase for consistency
            "assignedCoaches" to _assignedCoaches.value,
            "timestamp" to System.currentTimeMillis(),
            "createdByRole" to "admin",
            "approvalStatus" to "approved",
            "coachOrganized" to false,
            "organizingCoach" to "",
            "teamLineup" to emptyMap<String, List<String>>(),
            "matchResults" to emptyMap<String, String>(),
            "resultsApproved" to false
        )

        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestore.collection("events")
                    .add(eventData)
                    .addOnSuccessListener {
                        _snackbarMessage.value = "✅ Event scheduled successfully"
                        clearFields()
                        loadEvents() // Refresh the events list
                    }
                    .addOnFailureListener {
                        _snackbarMessage.value = "❌ Failed to schedule event"
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _snackbarMessage.value = "❌ ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun deleteEvent(eventId: String) {
        _isLoading.value = true
        firestore.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                _snackbarMessage.value = "✅ Event deleted successfully"
                loadEvents() // Refresh the events list
            }
            .addOnFailureListener {
                _snackbarMessage.value = "❌ Failed to delete event: ${it.message}"
                _isLoading.value = false
            }
    }

    private fun clearFields() {
        eventName.value = ""
        venue.value = ""
        timing.value = ""
        date.value = ""
        equipment.value = ""
        staffRequired.value = ""
        maxParticipants.value = ""
        logistics.value = ""
        category.value = ""
        _assignedCoaches.value = emptyList()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
    
    fun approveCoachEvent(eventId: String) {
        _isLoading.value = true
        firestore.collection("events").document(eventId)
            .get()
            .addOnSuccessListener { doc ->
                val invitedParticipants = doc.get("invitedParticipants") as? List<String> ?: emptyList()
                firestore.collection("events").document(eventId)
                    .update("approvalStatus", "approved")
                    .addOnSuccessListener {
                        invitedParticipants.forEach { email ->
                            val registrationData = mapOf(
                                "eventId" to eventId,
                                "user" to email,
                                "timestamp" to System.currentTimeMillis()
                            )
                            firestore.collection("event_registrations").add(registrationData)
                        }
                        _snackbarMessage.value = "✅ Coach event approved"
                        loadEvents()
                    }
                    .addOnFailureListener {
                        _snackbarMessage.value = "❌ Failed to approve event: ${it.message}"
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener {
                _snackbarMessage.value = "❌ Failed to load event for approval: ${it.message}"
                _isLoading.value = false
            }
    }
    
    data class EventData(
        val id: String = "",
        val name: String = "",
        val venue: String = "",
        val timing: String = "",
        val date: String = "",
        val equipment: String = "",
        val staffRequired: String = "",
        val maxParticipants: String = "",
        val logistics: String = "",
        val category: String = "",
        val assignedCoaches: List<String> = emptyList(),
        val createdByRole: String = "",
        val approvalStatus: String = "approved",
        val coachOrganized: Boolean = false,
        val organizingCoach: String = "",
        val teamLineup: Map<String, List<String>> = emptyMap(),
        val matchResults: Map<String, String> = emptyMap(),
        val resultsApproved: Boolean = false
    )
}
