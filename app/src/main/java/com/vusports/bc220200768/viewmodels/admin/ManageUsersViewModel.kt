package com.vusports.bc220200768.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.screens.admin.UserItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageUsersViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserItem>>(emptyList())
    val users: StateFlow<List<UserItem>> = _users

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun fetchUsers() {
        _loading.value = true
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.documents.map {
                    val email = it.id
                    val role = it.getString("role") ?: "unknown"
                    val name = it.getString("name") ?: email
                    val blocked = it.getBoolean("blocked") ?: false
                    UserItem(name, role, email, blocked)
                }
                _users.value = userList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    fun toggleBlock(user: UserItem, onDone: (Boolean) -> Unit) {
        db.collection("users").document(user.email)
            .update("blocked", !user.blocked)
            .addOnSuccessListener {
                _users.value = _users.value.map {
                    if (it.email == user.email) it.copy(blocked = !user.blocked) else it
                }
                onDone(true)
            }
            .addOnFailureListener { onDone(false) }
    }

    fun deleteUser(user: UserItem, onDone: (Boolean) -> Unit) {
        db.collection("users").document(user.email)
            .delete()
            .addOnSuccessListener {
                _users.value = _users.value.filterNot { it.email == user.email }
                onDone(true)
            }
            .addOnFailureListener { onDone(false) }
    }
}
