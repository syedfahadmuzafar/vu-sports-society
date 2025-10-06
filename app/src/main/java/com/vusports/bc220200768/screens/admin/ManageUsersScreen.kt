package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vusports.bc220200768.viewmodels.admin.ManageUsersViewModel

data class UserItem(
    val name: String,
    val role: String,
    val email: String,
    val blocked: Boolean
)

@Composable
fun ManageUsersScreen(viewModel: ManageUsersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    val appGreen = Color(0xFF00BFA6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Manage Users",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = appGreen
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Coaches & Participants",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                loading -> CircularProgressIndicator(color = appGreen)
                users.isEmpty() -> Text("No users found.", color = Color.Gray)
                else -> {
                    users.forEach { user ->
                        UserCard(
                            user = user,
                            onToggleBlock = {
                                viewModel.toggleBlock(user) { success ->
                                    Toast.makeText(context, if (success) "Updated" else "Failed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = {
                                viewModel.deleteUser(user) { success ->
                                    Toast.makeText(context, if (success) "Deleted" else "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UserCard(user: UserItem, onToggleBlock: () -> Unit, onDelete: () -> Unit) {
    val statusColor = if (user.blocked) Color.Red else Color(0xFF00BFA6)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${user.name} (${user.role})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (user.blocked) "Status: Blocked" else "Status: Active",
                        color = statusColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = onToggleBlock,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text(if (user.blocked) "Unblock" else "Block", fontSize = 13.sp, color = Color.Black)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}
