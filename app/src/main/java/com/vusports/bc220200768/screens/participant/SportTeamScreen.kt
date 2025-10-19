package com.vusports.bc220200768.screens.participant

import android.provider.CalendarContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.vusports.bc220200768.viewmodels.participant.ParticipantInfo
import com.vusports.bc220200768.viewmodels.participant.SportTeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportTeamScreen(
    navController: NavController,
    viewModel: SportTeamViewModel = viewModel()
) {
    val approvedSports by viewModel.approvedSports.collectAsState()
    val selectedSport by viewModel.selectedSport.collectAsState()
    val teamName by viewModel.teamName.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val selectedParticipants by viewModel.selectedParticipants.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Team by Sport", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh logic */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (approvedSports.isEmpty()) {
                    EmptyStateMessage("You don't have any approved sports preferences yet.")
                } else {
                    // Step 1: Select Sport
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Step 1: Select Sport",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF00BFA6)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp)
                            ) {
                                items(approvedSports) { sport ->
                                    SportItem(
                                        sport = sport,
                                        isSelected = sport == selectedSport,
                                        onClick = { viewModel.selectSport(sport) }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Step 2: Team Name (only if sport is selected)
                    if (selectedSport != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Step 2: Team Name",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF00BFA6)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = teamName,
                                    onValueChange = { viewModel.setTeamName(it) },
                                    label = { Text("Enter Team Name", color = Color.Black) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedBorderColor = Color(0xFF00BFA6),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Step 3: Select Participants (only if sport is selected)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Step 3: Select Team Members",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF00BFA6)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (loading) {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFF00BFA6))
                                    }
                                } else if (participants.isEmpty()) {
                                    Text(
                                        "No other participants found with approved ${selectedSport} preference.",
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        "Available ${selectedSport} Players:",
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                    ) {
                                        items(participants) { participant ->
                                            ParticipantItem(
                                                participant = participant,
                                                isSelected = selectedParticipants.contains(participant.email),
                                                onToggleSelection = { viewModel.toggleParticipantSelection(participant.email) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Create Team Button
                        Button(
                            onClick = { viewModel.createTeam() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = teamName.isNotBlank() && selectedSport != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00BFA6),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Create ${selectedSport} Team", fontSize = 16.sp)
                        }
                    }
                }
                
                if (feedback.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = feedback,
                        color = if (feedback.contains("successfully")) Color(0xFF00BFA6) else Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00BFA6)
                )
            }
        }
    }
}

@Composable
fun SportItem(sport: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF00BFA6) else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SportsSoccer,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF00BFA6) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = sport,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF00BFA6) else Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF00BFA6)
            )
        }
    }
}

@Composable
fun ParticipantItem(participant: ParticipantInfo, isSelected: Boolean, onToggleSelection: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF00BFA6) else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onToggleSelection() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image
        Image(
            painter = rememberAsyncImagePainter(participant.image.ifEmpty {
                "https://firebasestorage.googleapis.com/v0/b/YOUR_BUCKET/o/default_profile.png?alt=media"
            }),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(participant.name, fontWeight = FontWeight.Medium)
            Text(participant.email, fontSize = 12.sp, color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFA6))
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}