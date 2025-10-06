package com.vusports.bc220200768.screens.participant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vusports.bc220200768.viewmodels.participant.JoinedEvent
import com.vusports.bc220200768.viewmodels.participant.JoinedEventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinedEventsScreen(
    viewModel: JoinedEventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val upcoming by viewModel.upcomingEvents.collectAsState()
    val past by viewModel.pastEvents.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    val themeColor = Color(0xFF00BFA6)

    Scaffold(
        containerColor = Color(0xFFF6F6F6),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Your Joined Events")
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color(0xFFF6F6F6)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (upcoming.isEmpty() && past.isEmpty()) {
                Text("No events joined yet.", color = Color.Gray)
            } else {
                if (upcoming.isNotEmpty()) {
                    Text(
                        "🟢 Upcoming Events",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EventList(
                        events = upcoming,
                        themeColor = themeColor,
                        onLeave = { eventId ->
                            viewModel.leaveEvent(
                                eventId,
                                onSuccess = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }

                if (past.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "🔴 Past Events",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EventList(events = past, themeColor = themeColor, showLeaveButton = false)
                }
            }
        }
    }
}

@Composable
fun EventList(
    events: List<JoinedEvent>,
    themeColor: Color,
    showLeaveButton: Boolean = true,
    onLeave: (String) -> Unit = {}
) {
    events.forEach { event ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Event: ${event.name}", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Text("Venue: ${event.venue}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Text("Time: ${event.timing}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)

                if (showLeaveButton) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        onClick = { onLeave(event.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave Event", color = Color.White)
                    }
                }
            }
        }
    }
}
