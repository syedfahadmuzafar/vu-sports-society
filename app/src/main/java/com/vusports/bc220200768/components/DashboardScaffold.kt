package com.vusports.bc220200768.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.vusports.bc220200768.R
import com.vusports.bc220200768.components.MarqueeText
import com.vusports.bc220200768.viewmodels.GlobalNotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScaffold(
    title: String,
    navController: NavController,
    onLogout: (() -> Unit)? = null, // optional custom logout handler
    userRole: String = "participant",
    globalNotificationViewModel: GlobalNotificationViewModel = viewModel(),
    showBackLogoutConfirmation: Boolean = false,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {}
) {
    val currentNotification by globalNotificationViewModel.currentNotification.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Back press → confirm logout
    if (showBackLogoutConfirmation) {
        BackHandler {
            showLogoutDialog = true
        }
    }

    // Rotate notifications every 10s
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000)
            globalNotificationViewModel.rotateNotification()
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // If custom logout provided → use it
                    onLogout?.invoke() ?: run {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // clear backstack
                        }
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

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(title, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF00BFA6)
                    )
                )


                // Notification Banner
                currentNotification?.let { notification ->

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MarqueeText(
                            text = "Admin Notification: ${notification.message}",
                            backgroundColor = Color(0xFF03118D),
                            color = Color.White
                        )
                    }
                }

            }
        },
        bottomBar = bottomBar,
        content = content
    )
}


