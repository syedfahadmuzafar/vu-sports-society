package com.vusports.bc220200768.screens.coach

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vusports.bc220200768.viewmodels.coach.AdminEvent
import com.vusports.bc220200768.viewmodels.coach.OrganizeEventViewModel
import com.vusports.bc220200768.viewmodels.coach.Participant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeEventScreen(
    navController: NavController,
    viewModel: OrganizeEventViewModel = viewModel()
) {
    val context = LocalContext.current
    val availableEvents by viewModel.availableEvents.collectAsState()
    val organizedEvents by viewModel.organizedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val teamLineup by viewModel.teamLineup.collectAsState()
    val availableParticipants by viewModel.availableParticipants.collectAsState()
    val categoryRoles by viewModel.categoryRoles.collectAsState()
    
    var showResultsDialog by remember { mutableStateOf(false) }
    var currentResults by remember { mutableStateOf(mapOf<String, String>()) }
    
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }
    
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organize Events") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00BFA6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00BFA6)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (selectedEvent == null) {
                        // Show available events to organize
                        item {
                            Text(
                                "Available Admin Events",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00BFA6)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (availableEvents.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Text(
                                        "No available events to organize",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            items(availableEvents) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { viewModel.selectEvent(event) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Your Organized Events",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00BFA6)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (organizedEvents.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Text(
                                        "You haven't organized any events yet",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            items(organizedEvents) { event ->
                                OrganizedEventCard(
                                    event = event,
                                    onSubmitResults = {
                                        viewModel.selectEvent(event)
                                        showResultsDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        // Show team lineup organization UI
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Organize: ${selectedEvent?.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00BFA6)
                                    )
                                    Text("Date: ${selectedEvent?.date}")
                                    Text("Venue: ${selectedEvent?.venue}")
                                    Text("Category: ${selectedEvent?.category}")
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { viewModel.organizeEvent() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00BFA6)
                                        ),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Save Team Lineup")
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = { viewModel.selectEvent(null) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Gray
                                        ),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Show roles and participants
                        item {
                            Text(
                                "Assign Roles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (categoryRoles.isEmpty()) {
                            item {
                                Text(
                                    "No roles defined for this category",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            items(categoryRoles) { role ->
                                RoleCard(
                                    role = role,
                                    assignedParticipants = teamLineup[role] ?: emptyList(),
                                    availableParticipants = availableParticipants,
                                    onAssignParticipant = { participant ->
                                        viewModel.assignParticipantToRole(participant, role)
                                    },
                                    onRemoveParticipant = { participantEmail ->
                                        viewModel.removeParticipantFromRole(participantEmail, role)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showResultsDialog) {
        ResultsDialog(
            onDismiss = { showResultsDialog = false },
            onSubmit = { results ->
                viewModel.submitMatchResults(results)
                showResultsDialog = false
            }
        )
    }
}

@Composable
fun EventCard(event: AdminEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${event.date}")
            Text("Venue: ${event.venue}")
            Text("Category: ${event.category}")
        }
    }
}

@Composable
fun OrganizedEventCard(event: AdminEvent, onSubmitResults: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${event.date}")
            Text("Venue: ${event.venue}")
            Text("Category: ${event.category}")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onSubmitResults,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA6)
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Submit Results")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCard(
    role: String,
    assignedParticipants: List<String>,
    availableParticipants: List<Participant>,
    onAssignParticipant: (Participant) -> Unit,
    onRemoveParticipant: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = role,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show assigned participants
            if (assignedParticipants.isNotEmpty()) {
                Text(
                    "Assigned Participants:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                assignedParticipants.forEach { email ->
                    val participant = availableParticipants.find { it.email == email }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            participant?.name ?: email,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onRemoveParticipant(email) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
            
            // Dropdown to add participants
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = "Add participant",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00BFA6),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableParticipants.forEach { participant ->
                        // Only show participants not already assigned to this role
                        if (!assignedParticipants.contains(participant.email)) {
                            DropdownMenuItem(
                                text = { Text(participant.name) },
                                onClick = {
                                    onAssignParticipant(participant)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsDialog(
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit
) {
    var score by remember { mutableStateOf("") }
    var performance by remember { mutableStateOf("") }
    var highlights by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Match Results") },
        text = {
            Column {
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Final Score") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = performance,
                    onValueChange = { performance = it },
                    label = { Text("Team Performance") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = highlights,
                    onValueChange = { highlights = it },
                    label = { Text("Match Highlights") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val results = mapOf(
                        "score" to score,
                        "performance" to performance,
                        "highlights" to highlights
                    )
                    onSubmit(results)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}