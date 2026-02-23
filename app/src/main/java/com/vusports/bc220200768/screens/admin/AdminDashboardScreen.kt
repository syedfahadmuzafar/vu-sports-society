package com.vusports.bc220200768.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth

// 🟢 Define Admin Screens
sealed class AdminScreen(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : AdminScreen("admin_home", Icons.Default.Home, "Home")
    object Users : AdminScreen("manage_users", Icons.Default.People, "Users")
    object Profiles : AdminScreen("modify_profiles", Icons.Default.Edit, "Profiles")
    object Notify : AdminScreen("notifications", Icons.Default.Notifications, "Notify")
    object Schedule : AdminScreen("schedule_event", Icons.Default.Event, "Schedule")
    object Reports : AdminScreen("reports", Icons.Default.BarChart, "Reports")
    object ViewReports : AdminScreen("view_reports", Icons.Default.BarChart, "View Reports")
    object Categories : AdminScreen("manage_categories", Icons.Default.Category, "Categories")
    object ApproveTeams : AdminScreen("approve_teams_events", Icons.Default.CheckCircle, "Approve Teams")
}

val bottomScreens = listOf(
    AdminScreen.Home,
    AdminScreen.Users,
    AdminScreen.Categories
)

val allAdminCards = listOf(
    AdminScreen.Users,
    AdminScreen.Profiles,
    AdminScreen.Notify,
    AdminScreen.Schedule,
    AdminScreen.Reports,
    AdminScreen.ViewReports,
    AdminScreen.Categories,
    AdminScreen.ApproveTeams
)

private const val ADMIN_HOME_ROUTE = "admin_home"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navControl: NavController) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val selectedRoute = currentDestination?.destination?.route
    var showLogoutDialog by remember { mutableStateOf(false) }

    val appGreen = Color(0xFF00BFA6)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val hideTopBar = currentRoute != ADMIN_HOME_ROUTE

    BackHandler { showLogoutDialog = true }

    Scaffold(
        containerColor = Color(0xFFF6F6F6),
        topBar = {
            if (!hideTopBar) {
                CenterAlignedTopAppBar(
                    title = { Text("Admin Dashboard", color = Color.White) },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = appGreen)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = appGreen,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = Color.White
                            )
                        },
                        label = { Text(screen.label, color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFF008E7A)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // ✅ Apply Scaffold padding here like Participant
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AdminScreen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AdminScreen.Home.route) {
                    AdminHomeGrid(navController = navController)
                }
                composable(AdminScreen.Users.route) { ManageUsersScreen() }
                composable(AdminScreen.Profiles.route) { ModifyProfilesScreen(navController) }
                composable(AdminScreen.Notify.route) { NotificationsScreen(navController) }
                composable(AdminScreen.Schedule.route) { ScheduleEventScreen() }
                composable(AdminScreen.Reports.route) { ReportsScreen(navController) }
                composable(AdminScreen.ViewReports.route) { ViewReportsScreen() }
                composable(AdminScreen.Categories.route) { ManageCategoriesScreen() }
                composable(AdminScreen.ApproveTeams.route) { ApproveTeamsEventsScreen() }
            }
        }
    }

    // 🔐 Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navControl.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Yes")
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
fun AdminHomeGrid(navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp) // ✅ only inner spacing like participant
            .fillMaxSize()
    ) {
        items(allAdminCards) { screen ->
            Card(
                onClick = { navController.navigate(screen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        screen.icon,
                        contentDescription = screen.label,
                        tint = Color(0xFF00BFA6),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(screen.label, style = MaterialTheme.typography.titleSmall, color = Color.Black)
                }
            }
        }
    }
}
