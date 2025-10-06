package com.vusports.bc220200768.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vusports.bc220200768.viewmodel.admin.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val viewModel = remember { NotificationsViewModel() }

    val message = viewModel.message.collectAsState()
    val selectedRole = viewModel.selectedRole.collectAsState()
    val status = viewModel.status.collectAsState()
    val notifications = viewModel.notifications.collectAsState()

    var durationValue by remember { mutableStateOf("1") }
    var durationUnit by remember { mutableStateOf("hours") }

    // For edit dialog
    var editDialogOpen by remember { mutableStateOf(false) }
    var editMessage by remember { mutableStateOf("") }
    var editDuration by remember { mutableStateOf("60") }
    var editingId by remember { mutableStateOf<String?>(null) }

    fun calculateDurationInMinutes(): Int {
        val value = durationValue.toIntOrNull() ?: 1
        return when (durationUnit) {
            "minutes" -> value
            "hours" -> value * 60
            "days" -> value * 24 * 60
            else -> value * 60
        }
    }

    fun sendNotification() {
        if (message.value.isBlank()) return
        if (durationValue.toIntOrNull() == null || durationValue.toIntOrNull() ?: 0 <= 0) return

        viewModel.selectedDuration.value = calculateDurationInMinutes()
        viewModel.sendNotification()
    }

    Column( modifier = Modifier
        .fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 Title
                Text(
                    "📢 Send Notification",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BFA6)
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 🔹 Message Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Notification Message",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF00BFA6)
                        )
                        OutlinedTextField(
                            value = message.value,
                            onValueChange = { viewModel.message.value = it },
                            placeholder = { Text("Enter your announcement message...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BFA6),
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color(0xFFF9FFFD),
                                unfocusedContainerColor = Color(0xFFF9FFFD),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Audience Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Target Audience",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF00BFA6)
                        )

                        listOf(
                            "all" to "Everyone",
                            "participant" to "Participants Only",
                            "coach" to "Coaches Only"
                        ).forEach { (value, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedRole.value == value,
                                    onClick = { viewModel.selectedRole.value = value },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF00BFA6),
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Text(label, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Display Duration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )

                        OutlinedTextField(
                            value = durationValue,
                            onValueChange = { durationValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enter Time", color = Color.Black) },
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF00BFA6))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BFA6),
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color(0xFFF9FFFD),
                                unfocusedContainerColor = Color(0xFFF9FFFD),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("minutes", "hours", "days").forEach { unit ->
                                OutlinedButton(
                                    onClick = { durationUnit = unit },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (durationUnit == unit) Color(0xFF00BFA6).copy(alpha = 0.1f) else Color.Transparent,
                                        contentColor = if (durationUnit == unit) Color(0xFF00BFA6) else Color.Black
                                    )
                                ) {
                                    Text(
                                        text = unit.replaceFirstChar { it.uppercase() },
                                        fontSize = 13.sp,
                                        fontWeight = if (durationUnit == unit) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Send Button
                Button(
                    onClick = { sendNotification() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send Notification", fontSize = 16.sp, color = Color.White)
                }

                if (status.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        status.value,
                        color = if (status.value.contains("success", true)) Color(0xFF00C853) else Color.Red,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 🔹 Active Notifications
                Text(
                    "Active Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BFA6)
                    )
                )
                Spacer(Modifier.height(12.dp))

                LaunchedEffect(Unit) {
                    viewModel.loadNotifications()
                }

                notifications.value.forEach { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("📌 ${notif.message}", fontWeight = FontWeight.SemiBold)
                                    Text("Audience: ${notif.audience}", color = Color.Gray, fontSize = 13.sp)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        editingId = notif.id
                                        editMessage = notif.message
                                        editDuration = "60"
                                        editDialogOpen = true
                                    }
                                ) { Text("✏️ Edit", color = Color.Black) }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.cancelNotification(notif.id)
                                    }
                                ) { Text("Delete", color = Color.Red) }
                            }
                        }
                    }
                }
            }

            // 🔹 Edit Dialog
            if (editDialogOpen && editingId != null) {
                AlertDialog(
                    onDismissRequest = { editDialogOpen = false },
                    title = { Text("Edit Notification") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editMessage,
                                onValueChange = { editMessage = it },
                                label = { Text("Message") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editDuration,
                                onValueChange = { editDuration = it },
                                label = { Text("Duration (minutes)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val minutes = editDuration.toIntOrNull() ?: 60
                            viewModel.updateNotification(editingId!!, editMessage, minutes)
                            editDialogOpen = false
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { editDialogOpen = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
