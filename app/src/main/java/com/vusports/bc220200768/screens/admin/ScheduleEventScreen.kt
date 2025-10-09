package com.vusports.bc220200768.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vusports.bc220200768.viewmodel.admin.ScheduleEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEventScreen(
    viewModel: ScheduleEventViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val eventName by viewModel.eventName.collectAsState()
    val venue by viewModel.venue.collectAsState()
    val timing by viewModel.timing.collectAsState()
    val date by viewModel.date.collectAsState()
    val equipment by viewModel.equipment.collectAsState()
    val staffRequired by viewModel.staffRequired.collectAsState()
    val maxParticipants by viewModel.maxParticipants.collectAsState()
    val logistics by viewModel.logistics.collectAsState()
    val category by viewModel.category.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val assignedCoaches by viewModel.assignedCoaches.collectAsState()
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    // Show snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Schedule New Event", "Manage Events")

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Event Management",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            when (selectedTab) {
                0 -> {
                    // Schedule New Event Tab
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 🔹 Instruction
                            Text(
                                "Fill in the details below to schedule a new event.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // 🔹 Input Card
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
                                    // Event Details Section
                                    Text(
                                        "Event Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00BFA6)
                                    )
                                    
                                    OutlinedTextField(
                                        value = eventName,
                                        onValueChange = { viewModel.eventName.value = it },
                                        label = { Text("Event Name", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )
                                    
                                    // Category Dropdown
                                    var expanded by remember { mutableStateOf(false) }
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = category,
                                            onValueChange = { },
                                            label = { Text("Category", color = Color.Black) },
                                            modifier = Modifier.fillMaxWidth(),
                                            readOnly = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00BFA6),
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                cursorColor = Color.Black
                                            ),
                                            trailingIcon = {
                                                IconButton(onClick = { expanded = !expanded }) {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Filled.ArrowDropDown,
                                                        contentDescription = "Dropdown"
                                                    )
                                                }
                                            }
                                        )
                                        
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            availableCategories.forEach { categoryOption ->
                                                DropdownMenuItem(
                                                    text = { Text(categoryOption) },
                                                    onClick = {
                                                        viewModel.onCategorySelected(categoryOption)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Display assigned coaches if any
                                    if (assignedCoaches.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Text(
                                                "Assigned Coaches:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00BFA6)
                                            )
                                            
                                            assignedCoaches.forEach { coach ->
                                                Text(
                                                    "• $coach",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = date,
                                            onValueChange = { viewModel.date.value = it },
                                            label = { Text("Date", color = Color.Black) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00BFA6),
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                cursorColor = Color.Black
                                            )
                                        )
                                        
                                        OutlinedTextField(
                                            value = timing,
                                            onValueChange = { viewModel.timing.value = it },
                                            label = { Text("Time", color = Color.Black) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00BFA6),
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                cursorColor = Color.Black
                                            )
                                        )
                                    }

                                    OutlinedTextField(
                                        value = venue,
                                        onValueChange = { viewModel.venue.value = it },
                                        label = { Text("Venue", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )
                                    
                                    OutlinedTextField(
                                        value = maxParticipants,
                                        onValueChange = { viewModel.maxParticipants.value = it },
                                        label = { Text("Maximum Participants", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )
                                    
                                    // Logistics Section
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Logistics & Resources",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00BFA6)
                                    )
                                    
                                    OutlinedTextField(
                                        value = equipment,
                                        onValueChange = { viewModel.equipment.value = it },
                                        label = { Text("Equipment Needed", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )
                                    
                                    OutlinedTextField(
                                        value = staffRequired,
                                        onValueChange = { viewModel.staffRequired.value = it },
                                        label = { Text("Staff Required", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )

                                    OutlinedTextField(
                                        value = logistics,
                                        onValueChange = { viewModel.logistics.value = it },
                                        label = { Text("Additional Logistics Notes", color = Color.Black) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA6),
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        )
                                    )
                                }
                            }

                            // 🔹 Submit Button
                            Button(
                                onClick = { viewModel.scheduleEvent() },
                                modifier = Modifier
                                    .padding(top = 24.dp)
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00BFA6),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Schedule Event", fontSize = 16.sp)
                            }
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color(0xFF00BFA6)
                            )
                        }
                    }
                }
                1 -> {
                    // Manage Events Tab
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                    ) {
                        if (events.isEmpty() && !isLoading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No events scheduled yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                                
                                Button(
                                    onClick = { selectedTab = 0 },
                                    modifier = Modifier.padding(top = 16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00BFA6)
                                    )
                                ) {
                                    Text("Schedule New Event")
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(events) { event ->
                                    EventCard(
                                        event = event,
                                        onDelete = { viewModel.deleteEvent(event.id) }
                                    )
                                }
                            }
                        }
                        
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color(0xFF00BFA6)
                            )
                        }
                    }
                }
            }
        }

        // 🔹 Snackbar (overlay at bottom)
        SnackbarHost(
            hostState = snackbarHostState,

        )
    }
}

@Composable
fun EventCard(
    event: ScheduleEventViewModel.EventData,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFA6)
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Event",
                        tint = Color.Red
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Category",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = event.category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF00BFA6),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = event.date,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = event.timing,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Venue",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = event.venue,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (event.equipment.isNotBlank() && event.equipment != "-") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Equipment",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = event.equipment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (event.staffRequired.isNotBlank() && event.staffRequired != "-") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Staff Required",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = event.staffRequired,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (event.logistics.isNotBlank() && event.logistics != "-") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Logistics Notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = event.logistics,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
