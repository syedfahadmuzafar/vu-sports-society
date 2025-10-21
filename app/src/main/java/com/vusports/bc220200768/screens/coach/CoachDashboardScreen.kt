package com.vusports.bc220200768.screens.coach

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
import com.vusports.bc220200768.components.DashboardScaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vusports.bc220200768.viewmodels.GlobalNotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachDashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showLogoutDialog by remember { mutableStateOf(false) }
    var coachName by remember { mutableStateOf("Coach") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: "email@example.com"

    // Fetch Coach Name
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: ""
        if (email.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.documents.firstOrNull()?.getString("name") ?: "Coach"
                    coachName = name
                }
        }
    }

    val dashboardItems = listOf(
        DashboardItem("Edit Profile", "Update your profile", Icons.Default.Person) { navController.navigate("profile") },
        DashboardItem("Manage Teams", "Assign & view teams", Icons.Default.Groups) { navController.navigate("teamManagement") },
        DashboardItem("Approve Teams", "Review team requests", Icons.Default.CheckCircle) { navController.navigate("teamApproval") },
        DashboardItem("Organize Events", "Manage admin events", Icons.Default.EventAvailable) { navController.navigate("organize_events") },
        DashboardItem("Team Chat / Feedback", "Interact with team", Icons.Default.Chat) { navController.navigate("chat_selector/$email") },
        DashboardItem("Approve Participants", "Approve pending users", Icons.Default.CheckCircle) {
            navController.navigate("approveParticipants")
        },
        DashboardItem("Award Points", "Give points to participants", Icons.Default.Star) {
            navController.navigate("participant_points")
        },
        DashboardItem("Record Performance", "Track event performance", Icons.Default.Assessment) {
            navController.navigate("event_selector")
        },
        DashboardItem("Leaderboard", "Top performing teams", Icons.Default.Leaderboard) { navController.navigate("leaderboard") }
    )

    BackHandler {
        showLogoutDialog = true
    }

    val globalNotificationViewModel: GlobalNotificationViewModel = viewModel()

    DashboardScaffold(
        title = "Welcome, $coachName",
        navController = navController,
        onLogout = { showLogoutDialog = true },
        userRole = "coach",
        globalNotificationViewModel = globalNotificationViewModel,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF00BFA6)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                    label = { Text("Profile", color = Color.White) }
                )
            }
        }
    )
    { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
                .padding(16.dp)
            ) {
                items(dashboardItems) { item ->
                    CoachDashboardCard(item)
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Hey $coachName") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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

data class DashboardItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun CoachDashboardCard(item: DashboardItem) {
    Card(
        onClick = item.onClick,
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
            Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(32.dp), tint = Color(0xFF00BFA6))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                Text(item.description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
