package com.vusports.bc220200768.viewmodels.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val roles = listOf("Participant", "Coach")
    
    private val _sportsList = MutableStateFlow<List<String>>(emptyList())
    val sportsList: StateFlow<List<String>> = _sportsList

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _selectedRole = MutableStateFlow("Participant")
    val selectedRole: StateFlow<String> = _selectedRole

    private val _selectedSports = MutableStateFlow<List<String>>(emptyList())
    val selectedSports: StateFlow<List<String>> = _selectedSports
    
    private val _existingCoachSports = MutableStateFlow<List<String>>(emptyList())
    val existingCoachSports: StateFlow<List<String>> = _existingCoachSports

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Validation functions
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.all { it.isLetter() || it.isWhitespace() }
    }
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Input change handlers with validation
    fun onNameChange(new: String) { 
        // Only allow letters and spaces
        if (new.isEmpty() || new.all { it.isLetter() || it.isWhitespace() }) {
            _name.value = new 
        }
    }
    
    fun onEmailChange(new: String) { _email.value = new }
    fun onPasswordChange(new: String) { _password.value = new }
    fun onRoleChange(new: String) {
        _selectedRole.value = new
        _selectedSports.value = emptyList()
    }

    init {
        // Fetch existing coach sports and sports list
        fetchExistingCoachSports()
        fetchSportsList()
    }
    
    private fun fetchSportsList() {
        firestore.collection("categories").get()
            .addOnSuccessListener { documents ->
                val categoriesList = documents.mapNotNull { it.getString("name") }
                _sportsList.value = categoriesList
            }
            .addOnFailureListener {
                // Set a default list on failure
                _sportsList.value = listOf("Football", "Cricket", "Basketball", "Volleyball", "Tennis")
            }
    }
    
    private fun fetchExistingCoachSports() {
        firestore.collection("users")
            .whereEqualTo("role", "coach")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                val sports = mutableListOf<String>()
                for (document in documents) {
                    val expertise = document.get("expertise") as? List<*>
                    expertise?.forEach { sport ->
                        if (sport is String) {
                            sports.add(sport.lowercase())
                        }
                    }
                }
                _existingCoachSports.value = sports
            }
    }
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
    
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri
    
    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }
    
    fun toggleSport(sport: String) {
        if (_selectedSports.value.contains(sport)) {
            // Remove sport if already selected
            _selectedSports.value = _selectedSports.value - sport
            _errorMessage.value = "" // Clear any error message
            return
        }
        
        // Add sport based on role restrictions
        when (_selectedRole.value) {
            "Participant" -> {
                // For participants, limit to 2 selections
                if (_selectedSports.value.size >= 2) {
                    _errorMessage.value = "Participants can select up to 2 sports only"
                } else {
                    _selectedSports.value = _selectedSports.value + sport
                    _errorMessage.value = "" // Clear any error message
                }
            }
            "Coach" -> {
                // For coaches, limit to 1 selection and check if sport already has a coach
                if (_existingCoachSports.value.contains(sport.lowercase())) {
                    // Sport already has a coach
                    _errorMessage.value = "This sport already has a coach"
                } else if (_selectedSports.value.isNotEmpty()) {
                    // Coach can only select one sport
                    _errorMessage.value = "Coaches can select only 1 sport"
                } else {
                    _selectedSports.value = _selectedSports.value + sport
                    _errorMessage.value = "" // Clear any error message
                }
            }
            else -> {
                _selectedSports.value = _selectedSports.value + sport
                _errorMessage.value = "" // Clear any error message
            }
        }
    }
    
    // Check if a sport is disabled (for coaches only)
    fun isSportDisabled(sport: String): Boolean {
        return _selectedRole.value == "Coach" && _existingCoachSports.value.contains(sport.lowercase())
    }
    
    // Helper function to standardize sports names
    private fun standardizeSportName(sport: String): String {
        return sport.lowercase()
    }

    fun registerUser(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true

        val emailValue = email.value.trim().lowercase()
        val roleValue = selectedRole.value

        // 🔑 Status based on role
        val status = when (roleValue) {
            "Coach" -> "pending_admin"
            "Participant" -> "pending_coach"
            else -> "approved"
        }

        auth.createUserWithEmailAndPassword(emailValue, password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userData = mutableMapOf<String, Any>(
                        "uid" to userId,
                        "name" to name.value.trim(),
                        "email" to emailValue,
                        "role" to roleValue.lowercase(),
                        "status" to status
                    )

                    if (roleValue == "Coach") {
                        userData["expertise"] = selectedSports.value.map { standardizeSportName(it) }
                    } else {
                        userData["preferences"] = selectedSports.value.map { standardizeSportName(it) }
                        userData["sports"] = selectedSports.value.map { standardizeSportName(it) }
                    }
                    
                    // Handle profile image upload
                    val profileUri = profileImageUri.value
                    if (profileUri != null) {
                        uploadProfileImage(userId, profileUri, userData, emailValue, onSuccess, onError, status)
                    } else {
                        // No profile image to upload, just save user data
                        saveUserData(userData, emailValue, onSuccess, onError, status)
                    }
                } else {
                    _isLoading.value = false
                    onError("Registration failed: ${task.exception?.localizedMessage}")
                }
            }
    }
    
    private fun uploadProfileImage(
        userId: String,
        imageUri: Uri,
        userData: MutableMap<String, Any>,
        emailValue: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit,
        status: String
    ) {
        viewModelScope.launch {
            try {
                val imageRef = storage.reference.child("profileImages/$userId.jpg")
                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
                imageRef.putFile(imageUri, metadata).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                
                // Add image URL to user data
                userData["image"] = downloadUrl
                
                // Save user data with image URL
                saveUserData(userData, emailValue, onSuccess, onError, status)
            } catch (e: Exception) {
                _isLoading.value = false
                onError("Failed to upload profile image: ${e.message}")
            }
        }
    }
    
    private fun saveUserData(
        userData: Map<String, Any>,
        emailValue: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit,
        status: String
    ) {
        firestore.collection("users").document(emailValue)
            .set(userData)
            .addOnSuccessListener {
                _isLoading.value = false
                // Return true only if already approved
                onSuccess(status == "approved")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onError("Firestore Error: ${e.message}")
            }
    }
}
