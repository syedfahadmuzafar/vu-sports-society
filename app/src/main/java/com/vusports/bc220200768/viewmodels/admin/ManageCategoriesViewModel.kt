// com.vusports.bc220200768.viewmodel.admin.ManageCategoriesViewModel.kt
package com.vusports.bc220200768.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.components.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageCategoriesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _organizers = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // name to email
    val organizers: StateFlow<List<Pair<String, String>>> = _organizers

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    
    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName: StateFlow<String> = _newCategoryName
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadData() {
        _loading.value = true
        viewModelScope.launch {
            db.collection("categories").get().addOnSuccessListener { result ->
                _categories.value = result.documents.map {
                    Category(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        organizerEmail = it.getString("organizerEmail") ?: "",
                        organizerName = it.getString("organizerName") ?: ""
                    )
                }
                _loading.value = false
            }.addOnFailureListener {
                _message.value = "Failed to load categories: ${it.message}"
                _loading.value = false
            }

            // Only fetch approved coaches for organizer selection
            db.collection("users")
                .whereEqualTo("role", "coach")
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener { result ->
                    _organizers.value = result.documents.map {
                        val name = it.getString("name") ?: it.id
                        name to it.id
                    }
                }
        }
    }

    fun assignOrganizer(
        categoryId: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        db.collection("categories").document(categoryId)
            .update(mapOf("organizerEmail" to email, "organizerName" to name))
            .addOnSuccessListener {
                _categories.update { list ->
                    list.map {
                        if (it.id == categoryId) it.copy(organizerEmail = email, organizerName = name) else it
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { onError() }
    }
    
    fun updateNewCategoryName(name: String) {
        _newCategoryName.value = name
    }
    
    fun addCategory(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_newCategoryName.value.isBlank()) {
            onError("Category name cannot be empty")
            return
        }
        
        // Check if category already exists
        if (_categories.value.any { it.name.equals(_newCategoryName.value, ignoreCase = true) }) {
            onError("Category already exists")
            return
        }
        
        _loading.value = true
        val newCategory = hashMapOf(
            "name" to _newCategoryName.value.lowercase(),
            "organizerEmail" to "",
            "organizerName" to ""
        )
        
        db.collection("categories")
            .add(newCategory)
            .addOnSuccessListener { documentRef ->
                val addedCategory = Category(
                    id = documentRef.id,
                    name = _newCategoryName.value,
                    organizerEmail = "",
                    organizerName = ""
                )
                
                _categories.update { currentList ->
                    currentList + addedCategory
                }
                
                _newCategoryName.value = ""
                _loading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                _loading.value = false
                onError("Failed to add category: ${it.message}")
            }
    }
    
    fun deleteCategory(categoryId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _loading.value = true
        
        db.collection("categories").document(categoryId)
            .delete()
            .addOnSuccessListener {
                _categories.update { currentList ->
                    currentList.filter { it.id != categoryId }
                }
                _loading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                _loading.value = false
                onError("Failed to delete category: ${it.message}")
            }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}
