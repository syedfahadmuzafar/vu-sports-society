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
                    // Join team
                    db.collection("teams").document(name)
                        .update("members", FieldValue.arrayUnion(userEmail))
                        .addOnSuccessListener {
                            _feedback.value = "Successfully joined team '$name'!"
                            _loading.value = false
                        }
                        .addOnFailureListener {
                            _feedback.value = "Failed to join team."
                            _loading.value = false
                        }
                } else {
                    // Create team
                    db.collection("teams").document(name)
                        .set(
                            mapOf(
                                "members" to listOf(userEmail),
                                "creator" to userEmail,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            _feedback.value = "Team '$name' created successfully!"
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
