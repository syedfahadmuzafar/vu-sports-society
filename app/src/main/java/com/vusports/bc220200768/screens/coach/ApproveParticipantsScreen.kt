package com.vusports.bc220200768.screens.coach

import LoadingOverlay
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveParticipantsScreen(navController: NavController) {
    var participants by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Coach expertise
    var coachExpertise by remember { mutableStateOf<List<String>>(emptyList()) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    // Fetch coach expertise
    LaunchedEffect(Unit) {
        if (currentUserEmail != null) {
            firestore.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {
                        val expertise = doc.get("expertise") as? List<*>
                        coachExpertise = expertise?.filterIsInstance<String>()?.map { it.lowercase() } ?: emptyList()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load coach expertise: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fetch participants pending approval
    LaunchedEffect(refreshTrigger, coachExpertise) {
        if (coachExpertise.isEmpty()) {
            // If coach expertise is not loaded yet, don't filter participants
            Log.d("ApproveParticipants", "Coach expertise is empty, waiting for it to load")
            participants = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        
        Log.d("ApproveParticipants", "Fetching participants with coach expertise: $coachExpertise")
        
        firestore.collection("users")
            .whereEqualTo("role", "participant")
            .whereIn("status", listOf("pending_coach", "partially_approved"))
            .get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                participants = docs.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val prefs = (data["preferences"] as? List<*>)?.filterIsInstance<String>()?.map { it.lowercase() }
                        ?: emptyList()
                    val approvedPrefs = (data["approved_preferences"] as? List<*>)?.filterIsInstance<String>()?.map { it.lowercase() }
                        ?: emptyList()
                    
                    // Find preferences that match coach expertise but haven't been approved yet
                    val unapprovedMatchingPrefs = prefs.filter { pref -> 
                        pref in coachExpertise && pref !in approvedPrefs
                    }
                    
                    // Only show participants with at least one matching unapproved preference
                    if (unapprovedMatchingPrefs.isNotEmpty()) {
                        Log.d("ApproveParticipants", "Found matching participant: ${data["name"]} with unapproved preferences: $unapprovedMatchingPrefs")
                        // Add matching preferences to the data for use in approval
                        val mutableData = data.toMutableMap()
                        mutableData["matching_preferences"] = unapprovedMatchingPrefs
                        mutableData
                    } else null
                }
                Log.d("ApproveParticipants", "Filtered ${participants.size} participants with unapproved matching preferences out of ${docs.size} total participants")
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pending Approvals", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF00BFA6)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                isLoading -> LoadingOverlay()

                participants.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No pending approvals for your sports", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { refreshTrigger++ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))) {
                        Text("Refresh")
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(participants) { participant ->
                        val email = participant["email"] as? String ?: ""
                        val name = participant["name"] as? String ?: "Unknown"
                        val phone = participant["phone"] as? String ?: "No phone"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(name, style = MaterialTheme.typography.titleMedium)
                                Text(email, style = MaterialTheme.typography.bodySmall)
                                Text(phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                
                                // Display matching sports preferences
                                val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                val matchingSports = matchingPrefs.filterIsInstance<String>()
                                
                                if (matchingSports.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Matching sports preferences:", 
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF00BFA6))
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.Start) {
                                        matchingSports.forEach { sport ->
                                            Card(
                                                modifier = Modifier.padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFFE0F7F5)
                                                )
                                            ) {
                                                Text(
                                                    text = sport,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF00796B)
                                                )
                                            }
                                            Spacer(Modifier.width(4.dp))
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    // Reject specific preferences button
                                    OutlinedButton(
                                        onClick = {
                                            isLoading = true
                                            val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                            val matchingSports = matchingPrefs.filterIsInstance<String>()
                                            
                                            firestore.collection("users")
                                                .whereEqualTo("email", email)
                                                .get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    if (querySnapshot.isEmpty) {
                                                        Toast.makeText(context, "Error: User document not found for $email", Toast.LENGTH_SHORT).show()
                                                        isLoading = false
                                                        return@addOnSuccessListener
                                                    }

                                                    val docId = querySnapshot.documents.first().id
                                                    val docRef = firestore.collection("users").document(docId)
                                                    
                                                    docRef.get()
                                                        .addOnSuccessListener { userDoc ->
                                                            if (!userDoc.exists()) {
                                                                Toast.makeText(context, "Error: User document not found", Toast.LENGTH_SHORT).show()
                                                                isLoading = false
                                                                return@addOnSuccessListener
                                                            }
                                                            
                                                            // Get existing rejected preferences or create empty list
                                                            val existingRejected = (userDoc.get("rejected_preferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                                            // Add new rejected preferences
                                                            val updatedRejected = (existingRejected + matchingSports).distinct()
                                                            
                                                            // Get all preferences to check if all are now rejected
                                                            val allPrefs = (userDoc.get("preferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                                            val approvedPrefs = (userDoc.get("approved_preferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                                            
                                                            // Determine status based on approved preferences
                                                            val status = if (approvedPrefs.isNotEmpty()) "partially_approved" else "pending_coach"
                                                            
                                                            docRef.update(
                                                                mapOf(
                                                                    "rejected_preferences" to updatedRejected,
                                                                    "status" to status
                                                                )
                                                            )
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Preferences rejected: ${matchingSports.joinToString()}", Toast.LENGTH_SHORT).show()
                                                                    refreshTrigger++
                                                                    isLoading = false
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                                    isLoading = false
                                                                }
                                                        }
                                                        .addOnFailureListener {
                                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                            isLoading = false
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                    isLoading = false
                                                }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) { 
                                        val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                        val matchingSports = matchingPrefs.filterIsInstance<String>()
                                        if (matchingSports.size == 1) {
                                            Text("Reject ${matchingSports.first()}")
                                        } else {
                                            Text("Reject ${matchingSports.size} sports")
                                        }
                                    }

                                    Spacer(Modifier.width(8.dp))

// Approve button
                                    Button(
                                        onClick = {
                                            isLoading = true
                                            val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                            val matchingSports = matchingPrefs.filterIsInstance<String>()

                                            firestore.collection("users")
                                                .whereEqualTo("email", email)
                                                .get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    if (querySnapshot.isEmpty) {
                                                        Toast.makeText(context, "Error: User document not found for $email", Toast.LENGTH_SHORT).show()
                                                        isLoading = false
                                                        return@addOnSuccessListener
                                                    }

                                                    val docId = querySnapshot.documents.first().id
                                                    val docRef = firestore.collection("users").document(docId)

                                                    docRef.get()
                                                        .addOnSuccessListener { userDoc ->
                                                            if (!userDoc.exists()) {
                                                                Toast.makeText(context, "Error: User document not found", Toast.LENGTH_SHORT).show()
                                                                isLoading = false
                                                                return@addOnSuccessListener
                                                            }

                                                            val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                                            val matchingSports = matchingPrefs.filterIsInstance<String>()

                                                            val already = (userDoc.get("approved_preferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                                            val updated = (already + matchingSports).distinct()

                                                            val allPrefs = (userDoc.get("preferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                                            val allApproved = allPrefs.all { it in updated }
                                                            val status = if (allApproved) "approved" else "partially_approved"

                                                            docRef.update(
                                                                mapOf(
                                                                    "status" to status,
                                                                    "approved" to true,
                                                                    "approved_preferences" to updated
                                                                )
                                                            )
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Participant approved for ${matchingSports.joinToString()}", Toast.LENGTH_SHORT).show()
                                                                    refreshTrigger++
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                                    isLoading = false
                                                                }
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                    isLoading = false
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                                    ) { 
                                        val matchingPrefs = participant["matching_preferences"] as? List<*> ?: emptyList<String>()
                                        val matchingSports = matchingPrefs.filterIsInstance<String>()
                                        if (matchingSports.size == 1) {
                                            Text("Approve ${matchingSports.first()}")
                                        } else {
                                            Text("Approve ${matchingSports.size} sports")
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
