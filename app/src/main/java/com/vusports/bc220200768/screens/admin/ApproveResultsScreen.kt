package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveResultsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    
    var pendingResults by remember { mutableStateOf<List<EventResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load pending results
    LaunchedEffect(Unit) {
        db.collection("events")
            .whereEqualTo("coachOrganized", true)
            .whereEqualTo("resultsApproved", false)
            .whereNotEqualTo("matchResults", null)
            .get()
            .addOnSuccessListener { documents ->
                val results = documents.mapNotNull { doc ->
                    val matchResults = doc.get("matchResults") as? Map<String, String> ?: return@mapNotNull null
                    
                    EventResult(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        date = doc.getString("date") ?: "",
                        category = doc.getString("category") ?: "",
                        organizingCoach = doc.getString("organizingCoach") ?: "",
                        score = matchResults["score"] ?: "",
                        performance = matchResults["performance"] ?: "",
                        highlights = matchResults["highlights"] ?: ""
                    )
                }
                pendingResults = results
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading results: ${it.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approve Match Results") },
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
            } else if (pendingResults.isEmpty()) {
                Text(
                    "No pending results to approve",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(pendingResults) { result ->
                        ResultCard(
                            result = result,
                            onApprove = {
                                db.collection("events").document(result.id)
                                    .update("resultsApproved", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Results approved", Toast.LENGTH_SHORT).show()
                                        pendingResults = pendingResults.filter { it.id != result.id }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error approving results: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onReject = {
                                db.collection("events").document(result.id)
                                    .update("matchResults", null)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Results rejected", Toast.LENGTH_SHORT).show()
                                        pendingResults = pendingResults.filter { it.id != result.id }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error rejecting results: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

data class EventResult(
    val id: String,
    val name: String,
    val date: String,
    val category: String,
    val organizingCoach: String,
    val score: String,
    val performance: String,
    val highlights: String
)

@Composable
fun ResultCard(
    result: EventResult,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
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
                text = result.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BFA6)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Date: ${result.date}")
            Text("Category: ${result.category}")
            Text("Coach: ${result.organizingCoach}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Match Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Score: ${result.score}")
            Text("Performance: ${result.performance}")
            Text("Highlights: ${result.highlights}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}