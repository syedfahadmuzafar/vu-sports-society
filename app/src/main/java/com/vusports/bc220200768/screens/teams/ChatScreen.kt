package com.vusports.bc220200768.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.vusports.bc220200768.components.DashboardScaffold
import com.vusports.bc220200768.viewmodels.chat.ChatViewModel

data class ChatMessage(
    val message: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatType: String,
    id: String,
    currentUserEmail: String,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val senderName by viewModel.currentUserName.collectAsState()
    var message by remember { mutableStateOf("") }

    // Init ViewModel once
    LaunchedEffect(Unit) {
        viewModel.init(currentUserEmail, chatType, id)
    }

    val appGreen = Color(0xFF00BFA6)
    val lightBg = Color(0xFFF6F6F6)

    DashboardScaffold(title = "Chat - $chatType", navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(lightBg)
                .padding(padding)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔹 Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    val isUser = msg.senderEmail == currentUserEmail
                    val alignment = if (isUser) Arrangement.End else Arrangement.Start
                    val bubbleColor = if (isUser) appGreen else Color.White
                    val textColor = if (isUser) Color.White else Color.Black
                    val bubbleShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = alignment
                    ) {
                        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                            Text(
                                msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Box(
                                modifier = Modifier
                                    .clip(bubbleShape)
                                    .background(bubbleColor)
                                    .padding(12.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(msg.message, color = textColor)
                            }
                        }
                    }
                }
            }

            // 🔸 Input Bar
            Surface(
                shadowElevation = 6.dp,
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (message.isNotBlank()) {
                                viewModel.sendMessage(message, currentUserEmail)
                                message = ""
                            }
                        },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(44.dp)
                            .background(appGreen, shape = RoundedCornerShape(22.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
