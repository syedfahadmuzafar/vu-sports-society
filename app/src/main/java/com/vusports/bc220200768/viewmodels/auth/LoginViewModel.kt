package com.vusports.bc220200768.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onEmailChange(new: String) {
        _email.value = new
    }

    fun onPasswordChange(new: String) {
        _password.value = new
    }

    fun login(onSuccess: (String, Boolean) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email.value, password.value)
            .addOnSuccessListener {
                db.collection("users").document(email.value).get()
                    .addOnSuccessListener { doc ->
                        _isLoading.value = false
                        if (doc.exists()) {
                            val role = doc.getString("role") ?: "unknown"
                            val approved = doc.getBoolean("approved") ?: true
                            onSuccess(role, approved)
                        } else {
                            onError("User profile not found.")
                        }
                    }
            }
            .addOnFailureListener {
                _isLoading.value = false
                onError(it.localizedMessage ?: "Login failed.")
            }
    }
}
