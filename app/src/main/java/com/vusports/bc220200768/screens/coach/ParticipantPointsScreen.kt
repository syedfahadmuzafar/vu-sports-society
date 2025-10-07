package com.vusports.bc220200768.screens.coach

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import LoadingOverlay
import com.vusports.bc220200768.viewmodels.coach.ParticipantPointsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantPointsScreen(navController: NavController, viewModel: ParticipantPointsViewModel = viewModel()) {
    val participants by viewModel.participants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    
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
                title = { Text("Award Points", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadParticipants() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingOverlay()
            } else if (participants.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No approved participants found",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadParticipants() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                    ) {
                        Text("Refresh")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(participants) { participant ->
                        ParticipantPointCard(participant = participant, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantPointCard(
    participant: com.vusports.bc220200768.viewmodels.coach.ParticipantWithPoints,
    viewModel: ParticipantPointsViewModel
) {
    var pointsToAdd by remember { mutableStateOf("") }
    var showPointsDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(participant.name, style = MaterialTheme.typography.titleMedium)
                    Text(participant.email, style = MaterialTheme.typography.bodySmall)
                    Text("Current Points: ${participant.points}", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00BFA6))
                }
                
                Button(
                    onClick = { showPointsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Points")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Points")
                }
            }
        }
    }
    
    if (showPointsDialog) {
        AlertDialog(
            onDismissRequest = { showPointsDialog = false },
            title = { Text("Award Points to ${participant.name}") },
            text = {
                Column {
                    Text("Current Points: ${participant.points}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pointsToAdd,
                        onValueChange = { pointsToAdd = it.filter { char -> char.isDigit() } },
                        label = { Text("Points to Add") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val points = pointsToAdd.toIntOrNull() ?: 0
                        if (points > 0) {
                            viewModel.awardPoints(participant.email, participant.name, points)
                            showPointsDialog = false
                            pointsToAdd = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                ) {
                    Text("Award Points")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPointsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}