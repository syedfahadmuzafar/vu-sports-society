package com.vusports.bc220200768.screens.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vusports.bc220200768.viewmodel.coach.TeamManagementViewModel
import com.vusports.bc220200768.components.ParticipantProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    viewModel: TeamManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val teamName by viewModel.teamName.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val selectedMembers by viewModel.selectedMembers.collectAsState()
    val memberRoles by viewModel.memberRoles.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadParticipants()
    }

    val roleOptions = listOf("Player", "Captain", "Vice Captain", "Goalkeeper", "Batsman", "Bowler")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Team Management", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            Text(
                "Create Your Sports Team",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF00BFA6)),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = teamName,
                onValueChange = viewModel::onTeamNameChange,
                label = { Text("Team Name", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = schedule,
                onValueChange = viewModel::onScheduleChange,
                label = { Text("Practice/Match Schedule", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (pendingRequests.isNotEmpty()) {
                Text("Join Requests", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                pendingRequests.forEach { participant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👤 ${participant.name}", fontSize = 16.sp)
                            Text("✉️ ${participant.email}", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    viewModel.approveRequest(participant)
                                }) {
                                    Text("Approve", color = Color(0xFF00BFA6))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Select Participants", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            if (loading) {
                CircularProgressIndicator(color = Color(0xFF00BFA6))
            } else if (participants.isEmpty()) {
                Text("No participants found.", color = Color.Gray)
            } else {
                participants.forEach { participant ->
                    val isSelected = selectedMembers.contains(participant.email)
                    val currentRole = memberRoles[participant.email] ?: ""

                    Column {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .toggleable(
                                    value = isSelected,
                                    onValueChange = {
                                        viewModel.toggleMemberSelection(participant.email)
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE0F2F1) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("👤 ${participant.name}", fontSize = 16.sp)
                                Text("✉️ ${participant.email}", fontSize = 14.sp, color = Color.Gray)
                                if (participant.skills.isNotBlank()) {
                                    Text("🎯 Skills: ${participant.skills}", fontSize = 14.sp)
                                }

                                if (isSelected) {
                                    Spacer(Modifier.height(8.dp))
                                    DropdownMenuBox(
                                        label = "Assign Role",
                                        value = currentRole,
                                        options = roleOptions,
                                        onValueSelected = { selectedRole ->
                                            viewModel.assignRole(participant.email, selectedRole)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.createTeam { /* Optional callback */ }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA6),
                    contentColor = Color.White
                )
            ) {
                Text("Create Team", fontSize = 16.sp)
            }

            if (feedbackMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = feedbackMessage,
                    color = Color(0xFF00BFA6),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    label: String,
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
