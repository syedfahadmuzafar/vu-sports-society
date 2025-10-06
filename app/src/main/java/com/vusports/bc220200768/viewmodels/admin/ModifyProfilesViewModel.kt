package com.vusports.bc220200768.viewmodels.admin

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.screens.admin.EditableUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ModifyProfilesViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _userList = MutableStateFlow<List<EditableUser>>(emptyList())
    val userList: StateFlow<List<EditableUser>> = _userList

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun loadUsers() {
        _loading.value = true
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.map { doc ->
                    EditableUser(
                        email = doc.id,
                        name = doc.getString("name") ?: doc.id,
                        role = doc.getString("role") ?: "participant",
                        approved = doc.getBoolean("approved") ?: false
                    )
                }
                _userList.value = users
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    fun updateUser(index: Int, updatedUser: EditableUser) {
        val newList = _userList.value.toMutableList()
        newList[index] = updatedUser
        _userList.value = newList
    }

    fun saveUser(user: EditableUser, onResult: (Boolean) -> Unit) {
        // First, get the current user document to check existing data
        db.collection("users").document(user.email).get()
            .addOnSuccessListener { document ->
                // Prepare update data
                val updateData = mutableMapOf<String, Any>(
                    "name" to user.name,
                    "role" to user.role,
                    "approved" to user.approved
                )
                
                // Set the status field based on approval and role
                if (user.approved) {
                    updateData["status"] = "approved"
                } else {
                    // If not approved, set appropriate pending status based on role
                    when (user.role) {
                        "coach" -> updateData["status"] = "pending_admin"
                        "participant" -> updateData["status"] = "pending_coach"
                        else -> updateData["status"] = "pending"
                    }
                }
                
                // Update the document with all fields
                db.collection("users").document(user.email)
                    .update(updateData)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }
}
