package com.vusports.bc220200768.screens

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.vusports.bc220200768.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.storage.storageMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userImage by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var userSports by remember { mutableStateOf(listOf<String>()) }
    var approvedSports by remember { mutableStateOf(listOf<String>()) }
    var rejectedSports by remember { mutableStateOf(listOf<String>()) }
    var userAchievements by remember { mutableStateOf("") }
    var teamManagement by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var sportsOptions by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf("") }
    var fieldValue by remember { mutableStateOf("") }
    var showPreferencesDialog by remember { mutableStateOf(false) }


    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                val uid = auth.currentUser?.uid ?: return@rememberLauncherForActivityResult
                val imageRef = storage.reference.child("profileImages/$uid.jpg")

                uri?.let {
                    scope.launch {
                        try {
                            // Show loading indicator
                            isLoading = true
                            
                            // Upload image to Firebase Storage with explicit content type
                            val uploadTask = imageRef.putFile(
                                it, 
                                storageMetadata { contentType = "image/jpeg" }
                            )
                            uploadTask.await()
                            
                            // Wait for the upload to complete before getting download URL
                            val downloadUrl = imageRef.downloadUrl.await().toString()

                            // Update user document in Firestore
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                // First try to find by email
                                val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
                                val docId = snapshot.documents.firstOrNull()?.id
                                    ?: currentUser.uid // Fallback to UID if email search fails

                                db.collection("users").document(docId).update("image", downloadUrl)
                                    .await()
                                
                                // Update local state
                                userImage = downloadUrl
                                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileScreen", "Profile picture upload error: ${e.message}", e)
                            Toast.makeText(context, "Failed to update profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        try {
            val categoriesSnapshot = db.collection("categories").get().await()
            sportsOptions = categoriesSnapshot.documents.mapNotNull { it.getString("name") }

            if (sportsOptions.isEmpty()) {
                sportsOptions = listOf("Football", "Cricket", "Basketball", "Volleyball", "Tennis")
            }
        } catch (e: Exception) {
            sportsOptions = listOf("Football", "Cricket", "Basketball", "Volleyball", "Tennis")
        }

        val currentUser = auth.currentUser ?: return@LaunchedEffect
        userEmail = currentUser.email ?: "No Email"
        val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
        val doc = snapshot.documents.firstOrNull()
        doc?.let {
            userName = it.getString("name") ?: "No Name"
            userImage = it.getString("image") ?: ""
            userRole = it.getString("role") ?: "user"
            userSports = if (userRole == "coach") {
                it.get("expertise") as? List<String> ?: emptyList()
            } else {
                it.get("preferences") as? List<String> ?: emptyList()
            }

            if (userRole == "participant") {
                approvedSports = it.get("approved_preferences") as? List<String> ?: emptyList()
                rejectedSports = it.get("rejected_preferences") as? List<String> ?: emptyList()
            }
            userAchievements = it.getString("achievements") ?: ""
            teamManagement = it.getString("teamManagement") ?: ""
            availability = it.getString("availability") ?: ""
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF6F6F6))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00BFA6))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = rememberAsyncImagePainter(userImage.ifEmpty {
                            R.drawable.default_user
                        }),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                launcher.launch(intent)
                            },
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00BFA6))
                            .padding(4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(userEmail, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Sports Section
        SectionCard(if (userRole == "coach") "Sports Expertise" else "Preferred Sports") {
            if (userSports.isEmpty()) {
                Text("No sports selected.", color = Color.Gray)
            } else {
                Column {
                    userSports.forEach { sport ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("• $sport", modifier = Modifier.padding(vertical = 2.dp))
                            if (userRole == "participant") {
                                Spacer(Modifier.width(4.dp))
                                when {
                                    approvedSports.contains(sport) -> {
                                        Icon(Icons.Default.CheckCircle, "Approved", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Approved", color = Color(0xFF4CAF50), fontSize = 12.sp)
                                    }
                                    rejectedSports.contains(sport) -> {
                                        Icon(Icons.Default.Cancel, "Rejected", tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Rejected", color = Color(0xFFF44336), fontSize = 12.sp)
                                    }
                                    else -> Text("(Pending)", color = Color(0xFFFFA000), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        SectionCard("Achievements") {
            Text(userAchievements.ifEmpty { "No achievements yet." }, color = Color.Gray)
        }

        if (userRole == "coach") {
            SectionCard("Team Management Details") {
                Text(teamManagement.ifEmpty { "Not added yet." }, color = Color.Gray)
            }

            SectionCard("Availability") {
                Text(availability.ifEmpty { "Not specified." }, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Settings
        SettingItem("Name", userName, Icons.Default.Person) {
            fieldToEdit = "Name"
            fieldValue = userName
            showEditDialog = true
        }

        SettingItem("Email", userEmail, Icons.Default.Email) {
            fieldToEdit = "Email"
            fieldValue = userEmail
            showEditDialog = true
        }

        SettingItem(
            if (userRole == "coach") "Sports Expertise" else "Preferred Sports",
            userSports.joinToString(), Icons.Default.Sports
        ) { showPreferencesDialog = true }

        if (userRole == "coach") {
            SettingItem("Team Management", teamManagement, Icons.Default.Group) {
                fieldToEdit = "Team Management"
                fieldValue = teamManagement
                showEditDialog = true
            }

            SettingItem("Availability", availability, Icons.Default.AccessTime) {
                fieldToEdit = "Availability"
                fieldValue = availability
                showEditDialog = true
            }
        }

        SettingItem("Reset Password", "Send email", Icons.Default.LockReset) {
            auth.sendPasswordResetEmail(userEmail)
                .addOnSuccessListener {
                    Toast.makeText(context, "Reset link sent", Toast.LENGTH_SHORT).show()
                }
        }

        SettingItem("Logout", "", Icons.Default.Logout) {
            auth.signOut()
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Sports Preference / Expertise Dialog
    if (showPreferencesDialog) {
        var selectedSports by remember { mutableStateOf(userSports.toList()) }
        var errorMessage by remember { mutableStateOf("") }
        var existingCoachSports by remember { mutableStateOf<List<String>>(emptyList()) }
        var originalSports by remember { mutableStateOf(userSports.toList()) }
        var sportsWithCoaches by remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(showPreferencesDialog) {
            try {
                val documents = db.collection("users")
                    .whereEqualTo("role", "coach")
                    .whereEqualTo("status", "approved")
                    .get().await()

                val coachSports = mutableListOf<String>()
                for (document in documents) {
                    val email = document.getString("email")
                    val expertise = document.get("expertise") as? List<*>
                    expertise?.forEach { sport ->
                        if (sport is String) {
                            coachSports.add(sport.lowercase())
                        }
                    }
                }
                existingCoachSports = coachSports
                sportsWithCoaches = coachSports.distinct()
            } catch (_: Exception) { }
        }

        AlertDialog(
            onDismissRequest = { showPreferencesDialog = false },
            title = { Text("Edit ${if (userRole == "coach") "Sports Expertise" else "Preferred Sports"}") },
            text = {
                Column {
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                    }

                    sportsOptions.forEach { sport ->
                        val isSelected = selectedSports.contains(sport)
                        val hasCoach = sportsWithCoaches.contains(sport.lowercase())
                        val isDisabled = when {
                            userRole == "coach" && existingCoachSports.contains(sport.lowercase()) && !isSelected -> true
                            userRole == "coach" && selectedSports.size >= 1 && !isSelected -> false
                            userRole == "participant" && selectedSports.size >= 2 && !isSelected -> true
                            userRole == "participant" && !hasCoach -> true
                            else -> false
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                enabled = !isDisabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        when (userRole) {
                                            "coach" -> {
                                                if (existingCoachSports.contains(sport.lowercase()) && !selectedSports.contains(sport)) {
                                                    errorMessage = "This sport already has a coach assigned"
                                                } else if (selectedSports.isNotEmpty() && !selectedSports.contains(sport)) {
                                                    errorMessage = "Unselect your current sport before choosing another"
                                                } else {
                                                    errorMessage = ""
                                                    selectedSports = listOf(sport)
                                                }
                                            }

                                            "participant" -> {
                                                if (selectedSports.size >= 2 && !selectedSports.contains(sport)) {
                                                    errorMessage = "You can only select up to 2 sports"
                                                } else {
                                                    errorMessage = ""
                                                    selectedSports = selectedSports + sport
                                                }
                                            }
                                        }
                                    } else {
                                        errorMessage = ""
                                        selectedSports = selectedSports - sport
                                    }
                                }
                            )

                            Text(
                                text = sport,
                                color = if (isDisabled) Color.Gray else Color.Black
                            )

                            if (userRole == "coach" && existingCoachSports.contains(sport.lowercase()) && !isSelected) {
                                Spacer(Modifier.width(6.dp))
                                Text("(Has coach)", color = Color.Gray, fontSize = 12.sp)
                            }

                            if (userRole == "participant" && !hasCoach) {
                                Spacer(Modifier.width(6.dp))
                                Text("(No coach)", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
                            val docId = snapshot.documents.firstOrNull()?.id
                            docId?.let {
                                if (userRole == "coach") {
                                    db.collection("users").document(it).update("expertise", selectedSports).await()
                                    userSports = selectedSports
                                } else {
                                    val updates = mapOf(
                                        "preferences" to selectedSports,
                                        "status" to "pending_coach"
                                    )
                                    db.collection("users").document(it).update(updates).await()
                                    userSports = selectedSports
                                }
                                showPreferencesDialog = false
                                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = errorMessage.isEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreferencesDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SettingItem(title: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Medium)
                    if (value.isNotEmpty()) {
                        Text(value, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
