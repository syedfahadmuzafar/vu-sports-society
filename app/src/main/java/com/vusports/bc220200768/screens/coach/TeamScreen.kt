package com.vusports.bc220200768.screens.coach

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TeamScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val email = currentUser?.email ?: return

    var availableTeams by remember { mutableStateOf(listOf<String>()) }
    var joinedTeam by remember { mutableStateOf<String?>(null) }
    var joinedTeamDetails by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(true) {
        db.collection("teams")
            .get()
            .addOnSuccessListener { result ->
                val teams = mutableListOf<String>()
                for (doc in result) {
                    val name = doc.getString("teamName") ?: continue
                    teams.add(name)

                    val members = doc.get("members") as? List<*> ?: emptyList<String>()
                    if (members.contains(email)) {
                        joinedTeam = name
                        joinedTeamDetails = doc.data
                    }
                }
                availableTeams = teams
            }
            .addOnFailureListener {
                Toast.makeText(navController.context, "Failed to load teams", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Teams", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        if (joinedTeam != null && joinedTeamDetails != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Joined Team: $joinedTeam", style = MaterialTheme.typography.titleMedium)

                    val roles = joinedTeamDetails?.get("roles") as? Map<*, *> ?: emptyMap<Any, Any>()
                    val schedule = joinedTeamDetails?.get("schedule") as? String ?: "No schedule set."

                    Spacer(Modifier.height(8.dp))
                    Text("Practice/Match Schedule:")
                    Text(schedule, color = MaterialTheme.colorScheme.primary)

                    Spacer(Modifier.height(8.dp))
                    Text("Team Roles:")
                    roles.forEach { (memberEmail, role) ->
                        Text("• $memberEmail ➝ ${role.toString()}")
                    }
                }
            }
        } else {
            Text("You haven't joined any team yet.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Available Teams:")

                availableTeams.forEach { team ->
                    Button(
                        onClick = {
                            // Optional: allow joining team
                            Toast.makeText(navController.context, "You can request to join $team", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("• $team")
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
