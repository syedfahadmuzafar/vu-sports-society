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

data class ChatGroup(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val lastMessage: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0
)

class ChatViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _currentUserName = MutableStateFlow("User")
    val currentUserName: StateFlow<String> = _currentUserName
    
    private val _currentUserRole = MutableStateFlow("")
    val currentUserRole: StateFlow<String> = _currentUserRole

    private val _chatGroups = MutableStateFlow<List<ChatGroup>>(emptyList())
    val chatGroups: StateFlow<List<ChatGroup>> = _chatGroups

    private lateinit var chatRefPath: String
    private var currentUserEmail: String = ""

    fun init(userEmail: String, chatType: String, id: String) {
        currentUserEmail = userEmail
        
        // Fetch user name and role
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()
                val doc = snapshot.documents.firstOrNull()
                val name = doc?.getString("name")
                val role = doc?.getString("role")
                if (!name.isNullOrBlank()) _currentUserName.value = name
                if (!role.isNullOrBlank()) _currentUserRole.value = role
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

        // Start listening to messages
        db.collection(chatRefPath)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val msgs = it.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                    }
                    _messages.value = msgs
                    
                    // Mark messages as read
                    markMessagesAsRead(chatType, id, userEmail)
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
    
    fun loadChatGroups(userEmail: String) {
        viewModelScope.launch {
            try {
                // Get user role
                val userSnapshot = db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()
                
                val userDoc = userSnapshot.documents.firstOrNull()
                val userRole = userDoc?.getString("role") ?: ""
                _currentUserRole.value = userRole
                
                android.util.Log.d("ChatViewModel", "Loading chat groups for user: $userEmail with role: $userRole")
                
                // Load available chat groups based on role
                val groups = mutableListOf<ChatGroup>()
                
                // Team chat is available for everyone
                val teamUnreadCount = getUnreadCount("team", "global", userEmail)
                groups.add(
                    ChatGroup(
                        id = "team_global",
                        name = "Team Chat",
                        type = "team",
                        lastMessage = getLastMessage("team", "global"),
                        timestamp = getLastMessageTimestamp("team", "global"),
                        unreadCount = teamUnreadCount
                    )
                )
                
                // Role-specific chats
                if (userRole == "participant") {
                    // Participants can chat with coaches
                    val coachUnreadCount = getUnreadCount("coach", "global", userEmail)
                    groups.add(
                        ChatGroup(
                            id = "coach_global",
                            name = "Coach Chat",
                            type = "coach",
                            lastMessage = getLastMessage("coach", "global"),
                            timestamp = getLastMessageTimestamp("coach", "global"),
                            unreadCount = coachUnreadCount
                        )
                    )
                }
                
                // Organizer chat is available for everyone
                val organizerUnreadCount = getUnreadCount("organizer", "global", userEmail)
                groups.add(
                    ChatGroup(
                        id = "organizer_global",
                        name = "Organizers",
                        type = "organizer",
                        lastMessage = getLastMessage("organizer", "global"),
                        timestamp = getLastMessageTimestamp("organizer", "global"),
                        unreadCount = organizerUnreadCount
                    )
                )
                
                android.util.Log.d("ChatViewModel", "Loaded ${groups.size} chat groups: ${groups.map { it.name }}")
                _chatGroups.value = groups
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error loading chat groups", e)
                // Initialize with default groups to ensure UI shows something
                val defaultGroups = mutableListOf(
                    ChatGroup(
                        id = "team_global",
                        name = "Team Chat",
                        type = "team",
                        lastMessage = "No messages yet",
                        timestamp = Timestamp.now(),
                        unreadCount = 0
                    ),
                    ChatGroup(
                        id = "organizer_global",
                        name = "Organizers",
                        type = "organizer",
                        lastMessage = "No messages yet",
                        timestamp = Timestamp.now(),
                        unreadCount = 0
                    )
                )
                
                // Add coach chat only for participants
                if (_currentUserRole.value == "participant") {
                    defaultGroups.add(
                        ChatGroup(
                            id = "coach_global",
                            name = "Coach Chat",
                            type = "coach",
                            lastMessage = "No messages yet",
                            timestamp = Timestamp.now(),
                            unreadCount = 0
                        )
                    )
                }
                
                _chatGroups.value = defaultGroups
            }
        }
    }
    
    private suspend fun getUnreadCount(chatType: String, id: String, userEmail: String): Int {
        val path = when (chatType) {
            "team" -> "teams/$id/chats"
            "coach" -> "coaches/$id/chats"
            "organizer" -> "organizers/$id/chats"
            else -> "teams/default/chats"
        }
        
        // Get read status for this user
        val readStatusDoc = db.collection("read_status")
            .document("${chatType}_${id}_$userEmail")
            .get()
            .await()
        
        val lastReadTimestamp = readStatusDoc.getTimestamp("lastRead") ?: Timestamp(0, 0)
        
        // Count messages after last read
        val unreadMessages = db.collection(path)
            .whereGreaterThan("timestamp", lastReadTimestamp)
            .whereNotEqualTo("senderEmail", userEmail) // Don't count user's own messages
            .get()
            .await()
        
        return unreadMessages.size()
    }
    
    private suspend fun getLastMessage(chatType: String, id: String): String {
        val path = when (chatType) {
            "team" -> "teams/$id/chats"
            "coach" -> "coaches/$id/chats"
            "organizer" -> "organizers/$id/chats"
            else -> "teams/default/chats"
        }
        
        val lastMessage = db.collection(path)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        
        return if (lastMessage.isEmpty) {
            "No messages yet"
        } else {
            lastMessage.documents[0].getString("message") ?: "New message"
        }
    }
    
    private suspend fun getLastMessageTimestamp(chatType: String, id: String): Timestamp {
        val path = when (chatType) {
            "team" -> "teams/$id/chats"
            "coach" -> "coaches/$id/chats"
            "organizer" -> "organizers/$id/chats"
            else -> "teams/default/chats"
        }
        
        val lastMessage = db.collection(path)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        
        return if (lastMessage.isEmpty) {
            Timestamp.now()
        } else {
            lastMessage.documents[0].getTimestamp("timestamp") ?: Timestamp.now()
        }
    }
    
    private fun markMessagesAsRead(chatType: String, id: String, userEmail: String) {
        // Update read status for this user
        val readStatusRef = db.collection("read_status")
            .document("${chatType}_${id}_$userEmail")
        
        readStatusRef.set(mapOf(
            "lastRead" to Timestamp.now(),
            "userEmail" to userEmail,
            "chatType" to chatType,
            "chatId" to id
        ))
    }
}
