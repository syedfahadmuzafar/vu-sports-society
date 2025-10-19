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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.components.FirebaseUtil
import com.vusports.bc220200768.viewmodels.participant.EventRegistrationViewModel
import kotlinx.coroutines.tasks.await

data class Event(
    val id: String = "",
    val name: String = "",
    val venue: String = "",
    val timing: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationScreen(
    viewModel: EventRegistrationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Available Events",
                        color = androidx.compose.ui.graphics.Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF00BFA6)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Register for Event",
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color(0xFF00BFA6)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF00BFA6))
                } else {
                    events.forEach { event ->
                        val alreadyRegistered = registeredEvents.contains(event.name)

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
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("📍 Venue: ${event.venue}")
                                Text("🕒 Time: ${event.timing}")
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.registerEvent(event) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    },
                                    enabled = !alreadyRegistered
                                ) {
                                    Text(if (alreadyRegistered) "Registered" else "Register")
                                }
                            }
                        }
                    }

                    if (registeredEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("🎉 Registered Events:", style = MaterialTheme.typography.titleMedium)
                        registeredEvents.forEach {
                            Text("• $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
