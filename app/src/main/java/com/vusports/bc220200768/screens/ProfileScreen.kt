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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                        imageRef.putFile(it).await()
                        val downloadUrl = imageRef.downloadUrl.await().toString()

                        val snapshot =
                            db.collection("users").whereEqualTo("email", userEmail).get().await()
                        val docId = snapshot.documents.firstOrNull()?.id

                        docId?.let {
                            db.collection("users").document(it).update("image", downloadUrl)
                                .addOnSuccessListener {
                                    userImage = downloadUrl
                                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                }
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
            
            // Get approved and rejected preferences for participants
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
                            "https://firebasestorage.googleapis.com/v0/b/YOUR_BUCKET/o/default_profile.png?alt=media"
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

        // Sports
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
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Approved",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Approved",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(start = 2.dp)
                                        )
                                    }
                                    rejectedSports.contains(sport) -> {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Rejected",
                                            tint = Color(0xFFF44336),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Rejected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFF44336),
                                            modifier = Modifier.padding(start = 2.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            "(Pending)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFFA000),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (userRole == "participant") {
                        Spacer(Modifier.height(8.dp))
                        if (userSports.any { !approvedSports.contains(it) && !rejectedSports.contains(it) }) {
                            Text(
                                "Pending preferences require coach approval",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (approvedSports.isNotEmpty()) {
                            Text(
                                "You can participate in approved sports",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        if (rejectedSports.isNotEmpty()) {
                            Text(
                                "You can't participate in rejected sports",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF44336)
                            )
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
        ) {
            showPreferencesDialog = true
        }

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

    // Edit Dialog
    if (showEditDialog) {
        var validationError by remember { mutableStateOf("") }

        fun isValidName(name: String): Boolean {
            return name.isNotEmpty() && name.all { it.isLetter() || it.isWhitespace() }
        }

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun validateField(): Boolean {
            when (fieldToEdit) {
                "Name" -> if (!isValidName(fieldValue)) {
                    validationError = "Name can only contain letters and spaces"
                    return false
                }
                "Email" -> if (!isValidEmail(fieldValue)) {
                    validationError = "Please enter a valid email address"
                    return false
                }
            }
            validationError = ""
            return true
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit $fieldToEdit") },
            text = {
                Column {
                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = {
                            fieldValue = it
                            if (validationError.isNotEmpty()) {
                                validateField()
                            }
                        },
                        label = { Text(fieldToEdit) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationError.isNotEmpty()
                    )

                    if (validationError.isNotEmpty()) {
                        Text(
                            text = validationError,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (validateField()) {
                            scope.launch {
                                val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
                                val docId = snapshot.documents.firstOrNull()?.id
                                docId?.let {
                                    val update = when (fieldToEdit) {
                                        "Name" -> {
                                            userName = fieldValue
                                            mapOf("name" to fieldValue)
                                        }
                                        "Email" -> {
                                            userEmail = fieldValue
                                            auth.currentUser?.updateEmail(fieldValue)?.await()
                                            mapOf("email" to fieldValue)
                                        }
                                        "Team Management" -> {
                                            teamManagement = fieldValue
                                            mapOf("teamManagement" to fieldValue)
                                        }
                                        "Availability" -> {
                                            availability = fieldValue
                                            mapOf("availability" to fieldValue)
                                        }
                                        else -> emptyMap()
                                    }
                                    db.collection("users").document(it).update(update).await()
                                    showEditDialog = false
                                    Toast.makeText(context, "$fieldToEdit updated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sports Expertise / Preferences dialog
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
                    .get()
                    .await()

                val sports = mutableListOf<String>()
                val coachSports = mutableListOf<String>()
                
                for (document in documents) {
                    val email = document.getString("email")
                    val expertise = document.get("expertise") as? List<*>
                    
                    expertise?.forEach { sport ->
                        if (sport is String) {
                            // Add to list of sports with coaches
                            coachSports.add(sport.lowercase())
                            
                            // For coach view, only add sports from other coaches
                            if (userRole == "coach" && email != userEmail) {
                                sports.add(sport.lowercase())
                            }
                        }
                    }
                }
                
                existingCoachSports = sports
                sportsWithCoaches = coachSports.distinct()
            } catch (_: Exception) {
            }
        }

        AlertDialog(
            onDismissRequest = { showPreferencesDialog = false },
            title = { Text("Edit ${if (userRole == "coach") "Sports Expertise" else "Preferred Sports"}") },
            text = {
                Column {
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    val validationText = when (userRole) {
                        "coach" -> "Coaches can select only one sport that doesn't already have a coach"
                        else -> "Participants can select up to 2 sports"
                    }
                    Text(
                        text = validationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    sportsOptions.forEach { sport ->
                        val isSelected = selectedSports.contains(sport)
                        val isApproved = userRole == "participant" && approvedSports.contains(sport)
                        val hasCoach = sportsWithCoaches.contains(sport.lowercase())
                        val isDisabled = when {
                            // For coaches: disable if another coach has this sport or coach already selected a different sport
                            userRole == "coach" && existingCoachSports.contains(sport.lowercase()) && !isSelected -> true
                            userRole == "coach" && selectedSports.isNotEmpty() && !isSelected -> true
                            // For participants: disable if no coach for this sport or already selected 2 sports
                            userRole == "participant" && !hasCoach -> true
                            userRole == "participant" && selectedSports.size >= 2 && !isSelected -> true
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
                                                if (existingCoachSports.contains(sport.lowercase())) {
                                                    errorMessage = "This sport already has a coach"
                                                } else if (selectedSports.isNotEmpty() && !selectedSports.contains(sport)) {
                                                    errorMessage = "Coaches can only select one sport"
                                                } else {
                                                    errorMessage = ""
                                                    if (!selectedSports.contains(sport)) {
                                                        selectedSports = selectedSports + sport
                                                    }
                                                }
                                            }
                                            else -> {
                                                if (selectedSports.size >= 2 && !selectedSports.contains(sport)) {
                                                    errorMessage = "Participants can only select up to 2 sports"
                                                } else {
                                                    errorMessage = ""
                                                    if (!selectedSports.contains(sport)) {
                                                        selectedSports = selectedSports + sport
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        errorMessage = ""
                                        selectedSports = selectedSports - sport
                                    }
                                },
                                
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                text = sport,
                                color = if (isDisabled || (userRole == "participant" && !hasCoach)) Color.Gray else Color.Black
                            )
                                
                                if (isApproved && isSelected) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Approved",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                if (userRole == "participant" && !hasCoach) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "(No coach available)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            if (userRole == "coach" && existingCoachSports.contains(sport.lowercase()) && !isSelected) {
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "(Has coach)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
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
                                    // For coaches, simply update expertise
                                    db.collection("users").document(it).update("expertise", selectedSports).await()
                                    userSports = selectedSports
                                } else {
                                    // For participants, handle preference changes
                                    val newPreferences = selectedSports.toSet()
                                    val originalPreferences = originalSports.toSet()
                                    
                                    // Keep approved preferences that are still selected
                                    val newApprovedPrefs = approvedSports.filter { sport -> newPreferences.contains(sport) }
                                    
                                    // Update status if preferences changed
                                    val statusUpdate = if (newPreferences != originalPreferences) {
                                        // If all new preferences are already approved, keep status as is
                                        if (newPreferences.all { pref -> approvedSports.contains(pref) }) {
                                            mapOf()
                                        } else {
                                            // Otherwise set to pending_coach
                                            mapOf("status" to "pending_coach")
                                        }
                                    } else {
                                        mapOf()
                                    }
                                    
                                    val updates = mapOf(
                                        "preferences" to selectedSports,
                                        "approved_preferences" to newApprovedPrefs
                                    ) + statusUpdate
                                    
                                    db.collection("users").document(it).update(updates).await()
                                    userSports = selectedSports
                                    approvedSports = newApprovedPrefs
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
                TextButton(onClick = { showPreferencesDialog = false }) {
                    Text("Cancel")
                }
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
