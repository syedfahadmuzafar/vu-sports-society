package com.vusports.bc220200768.viewmodels.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vusports.bc220200768.components.FirebaseUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParticipantProfileViewModel : ViewModel() {
    private val db = FirebaseUtil.firestore
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _sportsPreference = MutableStateFlow("")
    val sportsPreference: StateFlow<String> = _sportsPreference

    private val _achievements = MutableStateFlow("")
    val achievements: StateFlow<String> = _achievements

    private val _pastParticipation = MutableStateFlow("")
    val pastParticipation: StateFlow<String> = _pastParticipation

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadProfile() {
        if (userEmail == null) return
        viewModelScope.launch {
            db.collection("participants").document(userEmail).get()
                .addOnSuccessListener { doc ->
                    _sportsPreference.value = doc.getString("sportsPreference") ?: ""
                    _achievements.value = doc.getString("achievements") ?: ""
                    _pastParticipation.value = doc.getString("pastParticipation") ?: ""
                }
        }
    }

    fun updateSportsPreference(value: String) {
        _sportsPreference.value = value
    }

    fun updateAchievements(value: String) {
        _achievements.value = value
    }

    fun updatePastParticipation(value: String) {
        _pastParticipation.value = value
    }

    fun saveProfile(onResult: (Boolean) -> Unit) {
        if (userEmail == null) return
        _loading.value = true

        val data = mapOf(
            "sportsPreference" to _sportsPreference.value,
            "achievements" to _achievements.value,
            "pastParticipation" to _pastParticipation.value,
            "email" to userEmail
        )

        db.collection("participants").document(userEmail).set(data)
            .addOnSuccessListener { onResult(true); _loading.value = false }
            .addOnFailureListener { onResult(false); _loading.value = false }
    }
}
