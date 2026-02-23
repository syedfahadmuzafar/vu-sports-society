package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vusports.bc220200768.components.DashboardScaffold
import com.vusports.bc220200768.viewmodels.admin.ModifyProfilesViewModel

data class EditableUser(
    val email: String,
    var name: String,
    var role: String,
    var approved: Boolean
)

@Composable
fun ModifyProfilesScreen(
    navController: NavController,
    viewModel: ModifyProfilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val users by viewModel.userList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val appGreen = Color(0xFF00BFA6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
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
                // 🔹 Title
                Text(
                    "Modify Profiles",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = appGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 🔹 Subtitle
                Text(
                    "Approve & Edit User Profiles",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                when {
                    loading -> CircularProgressIndicator(color = appGreen)
                    users.isEmpty() -> Text("No users found.", color = Color.Black)
                    else -> {
                        users.forEachIndexed { index, user ->
                            EditableUserCard(
                                user = user,
                                onNameChange = { newName ->
                                    viewModel.updateUser(index, user.copy(name = newName))
                                },
                                onRoleChange = { newRole ->
                                    viewModel.updateUser(index, user.copy(role = newRole))
                                },
                                onApprovalChange = { isApproved ->
                                    viewModel.updateUser(index, user.copy(approved = isApproved))
                                },
                                onSave = {
                                    viewModel.saveUser(user) { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) "✅ Updated ${user.email}" else "❌ Failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
}

@Composable
fun EditableUserCard(
    user: EditableUser,
    onNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onApprovalChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User info
            Text(
                text = "${user.name} (${user.role})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Black),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Approval switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (user.approved) "Status: Approved" else "Status: Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (user.approved) Color(0xFF4CAF50) else Color(0xFFFFA000),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = user.approved,
                    onCheckedChange = onApprovalChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00BFA6)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name field
            OutlinedTextField(
                value = user.name,
                onValueChange = { onNameChange(it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BFA6),
                    cursorColor = Color(0xFF00BFA6)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role dropdown
            DropdownMenuRoleSelector(currentRole = user.role, onRoleChange = onRoleChange)

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
            ) {
                Text("Save", fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun DropdownMenuRoleSelector(
    currentRole: String,
    onRoleChange: (String) -> Unit
) {
    val roles = listOf("participant", "coach", "team_leader")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(currentRole, color = Color.Black)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}
