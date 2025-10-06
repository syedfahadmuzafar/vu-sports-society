package com.vusports.bc220200768.screens

import android.widget.Toast
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    var events by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(true) {
        val result = db.collection("events").get().await()
        events = result.documents.mapNotNull { doc ->
            doc.data?.plus("id" to doc.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upcoming Sports Events") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (events.isEmpty()) {
                Text("No events available.")
            } else {
                events.forEach { event ->
                    val eventName = event["eventName"] as? String ?: "Unnamed Event"
                    val venue = event["venue"] as? String ?: ""
                    val timing = event["timing"] as? String ?: ""
                    val eventId = event["id"] as String

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Event: $eventName", style = MaterialTheme.typography.titleSmall)
                            Text("Venue: $venue", style = MaterialTheme.typography.bodySmall)
                            Text("Time: $timing", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                if (userEmail == null) {
                                    Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                db.collection("event_registrations")
                                    .whereEqualTo("eventId", eventId)
                                    .whereEqualTo("user", userEmail)
                                    .get()
                                    .addOnSuccessListener { docs ->
                                        if (docs.isEmpty) {
                                            val registration = mapOf(
                                                "eventId" to eventId,
                                                "event" to eventName,
                                                "user" to userEmail,
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                            db.collection("event_registrations")
                                                .add(registration)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Joined $eventName", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to join event", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Already joined", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }) {
                                Text("Join")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Dashboard")
            }
        }
    }
}
