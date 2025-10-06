package com.vusports.bc220200768.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var feedback by remember { mutableStateOf("") }
    var selectedParticipant by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf(listOf<String>()) }
    var expanded by remember { mutableStateOf(false) }

    // Load all participant emails from Firestore
    LaunchedEffect(Unit) {
        db.collection("users")
            .whereEqualTo("role", "participant")
            .get()
            .addOnSuccessListener { result ->
                participants = result.mapNotNull { it.getString("email") }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Give Feedback") },
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
                    Text("Select Participant", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Participant Dropdown
                    OutlinedTextField(
                        value = selectedParticipant,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Participant Email") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        participants.forEach { email ->
                            DropdownMenuItem(
                                text = { Text(email) },
                                onClick = {
                                    selectedParticipant = email
                                    expanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Feedback", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Write feedback here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedParticipant.isBlank() || feedback.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val feedbackData = mapOf(
                        "participantEmail" to selectedParticipant,
                        "feedback" to feedback,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("feedback")
                        .add(feedbackData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Feedback submitted!", Toast.LENGTH_SHORT).show()
                            feedback = ""
                            selectedParticipant = ""
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Feedback")
            }
        }
    }
}
