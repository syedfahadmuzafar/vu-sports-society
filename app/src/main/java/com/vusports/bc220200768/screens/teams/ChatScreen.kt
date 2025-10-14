package com.vusports.bc220200768.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.vusports.bc220200768.components.DashboardScaffold
import com.vusports.bc220200768.viewmodels.chat.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val message: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

// Format timestamp to display time in WhatsApp style
fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

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

    // WhatsApp colors
    val whatsAppGreen = Color(0xFF128C7E)
    val whatsAppLightGreen = Color(0xFF25D366)
    val whatsAppBackground = Color(0xFFECE5DD)
    val userBubbleColor = Color(0xFFDCF8C6)
    val otherBubbleColor = Color.White

    DashboardScaffold(title = "Chat - $chatType", navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(whatsAppBackground)
                .padding(padding)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔹 Messages List - WhatsApp style
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    val isUser = msg.senderEmail == currentUserEmail
                    val alignment = if (isUser) Arrangement.End else Arrangement.Start
                    val bubbleColor = if (isUser) userBubbleColor else otherBubbleColor
                    val textColor = Color.Black
                    val bubbleShape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomEnd = if (isUser) 0.dp else 8.dp,
                        bottomStart = if (isUser) 8.dp else 0.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        horizontalArrangement = alignment
                    ) {
                        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                            Box(
                                modifier = Modifier
                                    .clip(bubbleShape)
                                    .background(bubbleColor)
                                    .padding(8.dp, 6.dp, 8.dp, 8.dp)
                                    .widthIn(max = 260.dp)
                            ) {
                                Column {
                                    if (!isUser) {
                                        Text(
                                            msg.senderName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = whatsAppGreen,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    
                                    Text(
                                        msg.message, 
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    // Time stamp in small text at the end
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            formatTimestamp(msg.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                        
                                        if (isUser) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Sent",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🔸 Input Bar - WhatsApp style
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    // Emoji button (just for UI, not functional)
                    IconButton(
                        onClick = { /* Not implemented */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Emoji",
                            tint = Color.Gray
                        )
                    }
                    
                    // Message input field
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("Type a message") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            focusedContainerColor = Color(0xFFF0F0F0),
                            disabledContainerColor = Color(0xFFF0F0F0),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    // Send button
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
                            .background(whatsAppLightGreen, shape = CircleShape)
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
