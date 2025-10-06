package com.vusports.bc220200768.viewmodels.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vusports.bc220200768.screens.teams.ChatMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _currentUserName = MutableStateFlow("User")
    val currentUserName: StateFlow<String> = _currentUserName

    private lateinit var chatRefPath: String

    fun init(currentUserEmail: String, chatType: String, id: String) {
        // Fetch user name
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("email", currentUserEmail)
                    .get()
                    .await()
                val name = snapshot.documents.firstOrNull()?.getString("name")
                if (!name.isNullOrBlank()) _currentUserName.value = name
            } catch (_: Exception) {
            }
        }

        // Setup reference
        chatRefPath = when (chatType) {
            "team" -> "teams/$id/chats"
            "coach" -> "coaches/$id/chats"
            "organizer" -> "organizers/$id/chats"
            else -> "teams/default/chats"
        }

        // Start listening
        db.collection(chatRefPath)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val msgs = it.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                    }
                    _messages.value = msgs
                }
            }
    }

    fun sendMessage(content: String, email: String) {
        val msg = ChatMessage(
            message = content,
            senderName = _currentUserName.value,
            senderEmail = email,
            timestamp = Timestamp.now()
        )
        db.collection(chatRefPath).add(msg)
    }
}
