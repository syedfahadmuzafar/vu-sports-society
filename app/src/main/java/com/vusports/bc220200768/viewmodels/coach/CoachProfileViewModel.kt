// com.vusports.bc220200768.viewmodel.coach.CoachProfileViewModel.kt
package com.vusports.bc220200768.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoachProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _expertise = MutableStateFlow("")
    val expertise: StateFlow<String> = _expertise

    private val _availability = MutableStateFlow("")
    val availability: StateFlow<String> = _availability

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setExpertise(value: String) {
        _expertise.value = value
    }

    fun setAvailability(value: String) {
        _availability.value = value
    }

    fun updateProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onError("User not logged in")
            return
        }

        if (_expertise.value.isBlank() || _availability.value.isBlank()) {
            onError("Please fill all fields")
            return
        }

        _isLoading.value = true

        val data = mapOf(
            "expertise" to _expertise.value,
            "availability" to _availability.value
        )

        db.collection("users").document(user.uid)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
            .addOnCompleteListener { _isLoading.value = false }
    }
}
