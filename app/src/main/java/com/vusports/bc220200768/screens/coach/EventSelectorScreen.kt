package com.vusports.bc220200768.screens.coach

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import LoadingOverlay

data class CoachEvent(
    val id: String,
    val name: String,
    val venue: String,
    val timing: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelectorScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: ""
    
    var events by remember { mutableStateOf<List<CoachEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load events created by this coach
    LaunchedEffect(Unit) {
        loadCoachEvents(email, 
            onSuccess = { eventsList -> 
                events = eventsList
                isLoading = false
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Event", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isLoading = true
                        loadCoachEvents(email,
                            onSuccess = { eventsList -> 
                                events = eventsList
                                isLoading = false
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                        )
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingOverlay()
            } else if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No events found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("create_event") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                    ) {
                        Text("Create an Event")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            "Select an event to record performance",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    
                    items(events) { event ->
                        EventCard(event, navController)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: CoachEvent, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        onClick = { navController.navigate("event_performance/${event.id}") }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(event.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Venue: ${event.venue}", style = MaterialTheme.typography.bodyMedium)
            Text("Time: ${event.timing}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun loadCoachEvents(
    coachEmail: String,
    onSuccess: (List<CoachEvent>) -> Unit,
    onError: (String) -> Unit
) {
    if (coachEmail.isEmpty()) {
        onError("Not logged in")
        return
    }
    
    FirebaseFirestore.getInstance().collection("events")
        .whereEqualTo("createdBy", coachEmail)
        .get()
        .addOnSuccessListener { snapshot ->
            val eventsList = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("eventName") ?: doc.getString("title") ?: return@mapNotNull null
                val venue = doc.getString("venue") ?: doc.getString("location") ?: "-"
                val timing = doc.getString("timing") ?: "-"
                
                CoachEvent(id, name, venue, timing)
            }
            onSuccess(eventsList)
        }
        .addOnFailureListener {
            onError("Failed to load events: ${it.message}")
        }
}