package com.vusports.bc220200768.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.vusports.bc220200768.viewmodels.chat.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

// Format timestamp for display
fun formatChatGroupTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance()
    messageTime.time = date

    return when {
        // Today
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }

        // Yesterday
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Yesterday"
        }

        // Within a week
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) < 7 -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }

        // Older
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectorScreen(
    navController: NavController,
    currentUserEmail: String,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val chatGroups by viewModel.chatGroups.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    val whatsAppGreen = Color(0xFF128C7E)

    // Load chat groups
    LaunchedEffect(Unit) {
        viewModel.loadChatGroups(currentUserEmail)
    }

    Scaffold(
        topBar = { WhatsAppStyleTopBar() },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (chatGroups.isEmpty()) {
                // Show loading or empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = whatsAppGreen)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(chatGroups) { group ->
                        ChatGroupItem(
                            group = group,
                            onClick = {
                                navController.navigate("chat/${group.type}/global/$currentUserEmail")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppStyleTopBar() {
    val whatsAppGreen = Color(0xFF128C7E)

    TopAppBar(
        title = {
            Text(
                "VU Sports Chat",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = whatsAppGreen
        ),
        actions = {
            IconButton(onClick = { /* Add menu action */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun WhatsAppSearchBar() {
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Search...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color(0xFFF0F0F0),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        singleLine = true
    )
}

@Composable
fun ChatGroupItem(
    group: com.vusports.bc220200768.viewmodels.chat.ChatGroup,
    onClick: () -> Unit
) {
    val whatsAppGreen = Color(0xFF128C7E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Group",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }

        // Text content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = group.timestamp?.let { formatChatGroupTimestamp(it) } ?: "",
                    fontSize = 12.sp,
                    color = if (group.unreadCount > 0) whatsAppGreen else Color.Gray
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (group.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(whatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(start = 88.dp),
        color = Color(0xFFE0E0E0),
        thickness = 0.5.dp
    )
}
