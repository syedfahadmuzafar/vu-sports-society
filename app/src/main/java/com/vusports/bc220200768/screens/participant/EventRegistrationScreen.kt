package com.vusports.bc220200768.screens.participant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.components.FirebaseUtil
import com.vusports.bc220200768.viewmodels.participant.Event
import com.vusports.bc220200768.viewmodels.participant.EventRegistrationViewModel
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationScreen(
    viewModel: EventRegistrationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val availableEvents by viewModel.availableEvents.collectAsState()
    val joinedEvents by viewModel.joinedEvents.collectAsState()
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Available", "Joined")

    // Show toast for messages
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Events",
                        color = androidx.compose.ui.graphics.Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF00BFA6)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5))
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = androidx.compose.ui.graphics.Color(0xFF00BFA6),
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        selectedContentColor = androidx.compose.ui.graphics.Color.White,
                        unselectedContentColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF00BFA6))
                } else {
                    when (selectedTabIndex) {
                        0 -> AvailableEventsContent(availableEvents, viewModel, context)
                        1 -> JoinedEventsContent(joinedEvents, viewModel, context)
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableEventsContent(
    events: List<Event>,
    viewModel: EventRegistrationViewModel,
    context: android.content.Context
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No available events for you",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    } else {
        LazyColumn {
            items(events) { event ->
                EventCard(
                    event = event,
                    isJoined = false,
                    onJoin = { 
                        viewModel.registerEvent(event) { success, message ->
                            // Toast handled by LaunchedEffect
                        }
                    },
                    onReject = { reason ->
                        viewModel.rejectEvent(event, reason) { success, message ->
                            // Toast handled by LaunchedEffect
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun JoinedEventsContent(
    events: List<Event>,
    viewModel: EventRegistrationViewModel,
    context: android.content.Context
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "You haven't joined any events yet",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    } else {
        LazyColumn {
            items(events) { event ->
                EventCard(
                    event = event,
                    isJoined = true,
                    onJoin = { /* Not used for joined events */ },
                    onReject = { /* Not used for joined events */ },
                    onLeave = {
                        viewModel.leaveEvent(event) { success, message ->
                            // Toast handled by LaunchedEffect
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    isJoined: Boolean,
    onJoin: () -> Unit,
    onReject: (String) -> Unit,
    onLeave: (() -> Unit)? = null
) {
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "🏟️ ${event.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("📍 Venue: ${event.venue}")
            Text("🕒 Time: ${event.timing}")
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isJoined) {
                    // Leave button for joined events
                    Button(
                        onClick = { onLeave?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Leave Event")
                    }
                } else {
                    // Join button for available events
                    Button(
                        onClick = { onJoin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF00BFA6)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Join")
                    }
                    
                    // Reject button for available events
                    OutlinedButton(
                        onClick = { showRejectionDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color(0xFFE53935)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFE53935))
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
    
    // Rejection dialog
    if (showRejectionDialog) {
        AlertDialog(
            onDismissRequest = { showRejectionDialog = false },
            title = { Text("Reject Event") },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this event:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank()) {
                            onReject(rejectionReason)
                            showRejectionDialog = false
                            rejectionReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFE53935)
                    )
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
