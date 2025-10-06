package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveTeamsEventsScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var pendingTeams by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (id, name)
    var pendingEventRegs by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) } // (docId, eventId, user)

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Fetch unapproved teams
            val teamSnapshot = db.collection("teams")
                .whereEqualTo("approved", false)
                .get()
                .await()
            pendingTeams = teamSnapshot.documents.map { it.id to (it.getString("teamName") ?: "Unnamed Team") }

            // Fetch unapproved event registrations
            val regSnapshot = db.collection("event_registrations")
                .whereEqualTo("approved", false)
                .get()
                .await()
            pendingEventRegs = regSnapshot.documents.map {
                Triple(it.id, it.getString("eventId") ?: "Unknown Event", it.getString("user") ?: "Unknown User")
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch requests", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Approve Teams / Events") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            // Section 1: Pending Teams
            Text("Pending Team Formations", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (pendingTeams.isEmpty()) {
                Text("No team approvals needed.")
            } else {
                pendingTeams.forEach { (teamId, teamName) ->
                    ApprovalCard(
                        name = teamName,
                        onApprove = {
                            db.collection("teams").document(teamId)
                                .update("approved", true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "$teamName approved", Toast.LENGTH_SHORT).show()
                                    pendingTeams = pendingTeams.filterNot { it.first == teamId }
                                }
                        },
                        onReject = {
                            db.collection("teams").document(teamId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "$teamName rejected & removed", Toast.LENGTH_SHORT).show()
                                    pendingTeams = pendingTeams.filterNot { it.first == teamId }
                                }
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Section 2: Event Registration Approvals
            Text("Pending Event Registrations", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (pendingEventRegs.isEmpty()) {
                Text("No event approvals needed.")
            } else {
                pendingEventRegs.forEach { (docId, eventId, userEmail) ->
                    ApprovalCard(
                        name = "Event: $eventId\nUser: $userEmail",
                        onApprove = {
                            db.collection("event_registrations").document(docId)
                                .update("approved", true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Approved registration", Toast.LENGTH_SHORT).show()
                                    pendingEventRegs = pendingEventRegs.filterNot { it.first == docId }
                                }
                        },
                        onReject = {
                            db.collection("event_registrations").document(docId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Rejected registration", Toast.LENGTH_SHORT).show()
                                    pendingEventRegs = pendingEventRegs.filterNot { it.first == docId }
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalCard(name: String, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Approve", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onReject
            ) {
                Text("Reject", fontSize = 14.sp)
            }
        }
    }
}
