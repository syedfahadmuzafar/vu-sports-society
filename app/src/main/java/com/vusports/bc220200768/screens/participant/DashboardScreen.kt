package com.vusports.bc220200768.screens.participant

import LoadingOverlay
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vusports.bc220200768.components.DashboardScaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vusports.bc220200768.viewmodels.GlobalNotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var name by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: "email@example.com"
    currentUser?.email?.let { email ->
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    name = doc.getString("name") ?: "User"
                }
            }
    }
    var showLoading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val dashboardItems = listOf(
        Triple("Available Events", Icons.Default.EventAvailable, "Explore now"),
        Triple("Joined Events", Icons.Default.CheckCircle, "Your events"),
        Triple("Teams", Icons.Default.Groups, "View teams"),
        Triple("Create Team", Icons.Default.AddCircle, "By sport"),
        Triple("Chat", Icons.Default.Chat, "Talk to team"),
        Triple("Profile", Icons.Default.Person, "View info"),
        Triple("Performance", Icons.Default.Assessment, "View stats"),
        Triple("Leaderboard", Icons.Default.Leaderboard, "Top players")
    )

    BackHandler {
        showLogoutDialog = true
    }

    val globalNotificationViewModel: GlobalNotificationViewModel = viewModel()

    DashboardScaffold(
        title = "Welcome, $name",
        navController = navController,
        showBackLogoutConfirmation = true,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF00BFA6)) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        showLoading = true
                        scope.launch {
                            delay(600)
                            showLoading = false
                            navController.navigate("profile")
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                    label = { Text("Profile", color = Color.White) }
                )
            }
        }
    )
    { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                items(dashboardItems) { (label, icon, desc) ->
                    DashboardCard(label, icon, desc) {
                        when (label) {
                            "Available Events" -> navController.navigate("events")
                            "Joined Events" -> navController.navigate("joined_events")
                            "Teams" -> navController.navigate("teams")
                            "Create Team" -> navController.navigate("sport_team")
                            "Chat" -> navController.navigate("chat_selector/$email")
                            "Profile" -> {
                                showLoading = true
                                scope.launch {
                                    delay(600)
                                    showLoading = false
                                    navController.navigate("profile")
                                }
                            }
                            "Performance" -> navController.navigate("performance_history")
                            "Leaderboard" -> navController.navigate("leaderboard")
                        }
                    }
                }
            }

            if (showLoading) {
                LoadingOverlay()
            }
        }
    }

    // 🔐 Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Hey $name") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("userDashboard") { inclusive = true }
                    }
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DashboardCard(label: String, icon: ImageVector, desc: String = "", onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp), tint = Color(0xFF00BFA6))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                if (desc.isNotEmpty()) {
                    Text(desc, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
