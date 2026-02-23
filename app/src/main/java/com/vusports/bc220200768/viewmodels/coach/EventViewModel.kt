package com.vusports.bc220200768.viewmodel.coach

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.vusports.bc220200768.components.ParticipantProfile

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val location = MutableStateFlow("")
    val date = MutableStateFlow("")
    val timing = MutableStateFlow("")
    val category = MutableStateFlow("")
    val loading = MutableStateFlow(false)
    val feedback = MutableStateFlow("")

    private val _participants = MutableStateFlow<List<ParticipantProfile>>(emptyList())
    val participants: StateFlow<List<ParticipantProfile>> = _participants

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected

    fun toggleParticipant(email: String) {
        _selected.value = _selected.value.toMutableSet().apply {
            if (contains(email)) remove(email) else add(email)
        }
    }

    fun loadCoachCategoriesAndParticipants() {
        loading.value = true
        val coachEmail = auth.currentUser?.email
        if (coachEmail == null) {
            feedback.value = "Coach not logged in."
            loading.value = false
            return
        }

        db.collection("users").document(coachEmail).get()
            .addOnSuccessListener { coachDoc ->
                val expertiseList = when (val exp = coachDoc.get("expertise")) {
                    is String -> if (exp.isNotBlank()) listOf(exp) else emptyList()
                    is List<*> -> exp.filterIsInstance<String>()
                    else -> emptyList()
                }

                val normalized = expertiseList.map { it.trim() }.filter { it.isNotEmpty() }
                val firstCategory = normalized.firstOrNull() ?: ""
                category.value = firstCategory

                if (firstCategory.isNotEmpty()) {
                    loadParticipantsForCategory(firstCategory)
                } else {
                    feedback.value = "No expertise category found for coach."
                    _participants.value = emptyList()
                    loading.value = false
                }
            }
            .addOnFailureListener {
                feedback.value = "Failed to load coach expertise."
                loading.value = false
            }
    }

    private fun loadParticipantsForCategory(selectedCategory: String) {
        db.collection("users").whereEqualTo("role", "participant").get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.id
                    val sports = when (val sportsField = doc.get("sports")) {
                        is String -> sportsField.lowercase()
                        is List<*> -> sportsField.filterIsInstance<String>().joinToString(",").lowercase()
                        else -> ""
                    }

                    if (sports.contains(selectedCategory.lowercase())) {
                        ParticipantProfile(name, email, sports)
                    } else null
                }
                _participants.value = list
                loading.value = false
            }
            .addOnFailureListener {
                feedback.value = "Failed to load participants."
                loading.value = false
            }
    }

    fun createEvent(coachEmail: String) {
        if (title.value.isBlank() || description.value.isBlank() || location.value.isBlank() || date.value.isBlank() || timing.value.isBlank()) {
            feedback.value = "All fields are required."
            return
        }
        if (category.value.isBlank()) {
            feedback.value = "No expertise category available."
            return
        }
        loading.value = true
        val data = mapOf(
            "eventName" to title.value,
            "description" to description.value,
            "location" to location.value,
            "venue" to location.value,
            "date" to date.value,
            "timing" to timing.value,
            "category" to category.value.lowercase(),
            "createdBy" to coachEmail,
            "createdByRole" to "coach",
            "approvalStatus" to "pending",
            "dateTime" to System.currentTimeMillis(),
            "invitedParticipants" to _selected.value.toList()
        )
        db.collection("events").add(data)
            .addOnSuccessListener {
                feedback.value = "Event submitted for admin approval."
                title.value = ""
                description.value = ""
                location.value = ""
                date.value = ""
                timing.value = ""
                _selected.value = emptySet()
            }
            .addOnFailureListener {
                feedback.value = "Failed to create event."
            }
            .addOnCompleteListener {
                loading.value = false
            }
    }
}
