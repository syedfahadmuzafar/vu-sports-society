package com.vusports.bc220200768.screens.coach

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.vusports.bc220200768.viewmodel.coach.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: EventViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    coachEmail: String // Pass from logged in user
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val date by viewModel.date.collectAsState()
    val timing by viewModel.timing.collectAsState()
    val category by viewModel.category.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val feedback by viewModel.feedback.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCoachCategoriesAndParticipants()
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Create Event", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00BFA6))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Event Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF00BFA6)
                        )
                        if (category.isNotEmpty()) {
                            Text(
                                text = "Category: $category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Event Title", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Description", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.location.value = it },
                label = { Text("Venue", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = date,
                onValueChange = { viewModel.date.value = it },
                label = { Text("Date", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = timing,
                onValueChange = { viewModel.timing.value = it },
                label = { Text("Time", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray
                )
            )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Invite Participants",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )

                        if (loading) {
                            CircularProgressIndicator(color = Color(0xFF00BFA6))
                        } else {
                            participants.forEach { user ->
                                val isSelected = selected.contains(user.email)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.toggleParticipant(user.email) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFE0F2F1) else Color.White
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(user.name, color = Color.Black)
                                        Text(
                                            user.email,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.createEvent(coachEmail) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                        ) {
                            Text("Submit For Approval", color = Color.White)
                        }

                        if (feedback.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(feedback, color = Color(0xFF00BFA6))
                        }
                    }
                }
            }
        }
    }
}
