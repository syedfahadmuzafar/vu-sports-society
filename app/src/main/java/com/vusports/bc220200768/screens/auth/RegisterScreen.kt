package com.vusportssociety.screens.auth

import LoadingOverlay
import android.net.Uri
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.vusports.bc220200768.R
import com.vusports.bc220200768.viewmodels.auth.RegisterViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = viewModel()
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val selectedSports by viewModel.selectedSports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    var expandedRole by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        profileImageUri = it
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF00BFA6)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔷 Profile Image Picker
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = if (profileImageUri != null)
                        rememberAsyncImagePainter(profileImageUri)
                    else
                        painterResource(id = R.drawable.default_user),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )

                // Camera Icon Overlay (Circular)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .shadow(2.dp, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Icon",
                        tint = Color(0xFF00BFA6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔷 TextFields with validation
            // Name field with validation
            var nameError by remember { mutableStateOf("") }
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isLetter() || char.isWhitespace() }) {
                        viewModel.onNameChange(it)
                        nameError = ""
                    } else {
                        nameError = "Name can only contain letters and spaces"
                    }
                },
                label = { Text("Name", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color(0xFF00BFA6),
                    errorBorderColor = Color.Red
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                isError = nameError.isNotEmpty()
            )
            if (nameError.isNotEmpty()) {
                Text(
                    text = nameError,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                )
            }
            
            // Email field with validation
            var emailError by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    viewModel.onEmailChange(it)
                    emailError = if (!viewModel.isValidEmail(it) && it.isNotEmpty()) {
                        "Please enter a valid email address"
                    } else {
                        ""
                    }
                },
                label = { Text("Email", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color(0xFF00BFA6),
                    errorBorderColor = Color.Red
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                isError = emailError.isNotEmpty()
            )
            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                )
            }
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BFA6),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color(0xFF00BFA6)
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.Black)
            )

            // 🔷 Role Dropdown
            Box {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Role", color = Color.Black) },
                    trailingIcon = {
                        IconButton(onClick = { expandedRole = !expandedRole }) {
                            Icon(
                                imageVector = if (expandedRole) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00BFA6),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color(0xFF00BFA6),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black)
                )

                DropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false }
                ) {
                    viewModel.roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                viewModel.onRoleChange(role)
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔷 Sports Selection
            Text(
                text = if (selectedRole == "Coach") "Select Sports Expertise" else "Select Sports Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Display error message if any
            val errorMessage by viewModel.errorMessage.collectAsState("")
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            val existingCoachSports by viewModel.existingCoachSports.collectAsState()
            val sportsList by viewModel.sportsList.collectAsState()
            
            sportsList.forEach { sport ->
                val isDisabled = viewModel.isSportDisabled(sport)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = sport, color = if (isDisabled) Color.Gray else Color.Black)
                        if (isDisabled) {
                            Text(
                                text = "Coach already assigned",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Use Button instead of Checkbox for better visual feedback
                    val isSelected = selectedSports.contains(sport)
                    Button(
                        onClick = { viewModel.toggleSport(sport) },
                        enabled = !isDisabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF00BFA6) else Color.LightGray,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .width(100.dp)
                    ) {
                        Text(
                            text = if (isSelected) "Selected" else "Select",
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔷 Register Button with validation
            Button(
                onClick = {
                    // Validate all fields
                    val nameValid = name.isNotEmpty() && name.all { char -> char.isLetter() || char.isWhitespace() }
                    val emailValid = viewModel.isValidEmail(email)
                    val passwordValid = password.isNotEmpty()
                    val roleValid = selectedRole.isNotEmpty()
                    
                    if (!nameValid) {
                        Toast.makeText(context, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (!emailValid) {
                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (!passwordValid) {
                        Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (!roleValid) {
                        Toast.makeText(context, "Please select a role", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (selectedSports.isEmpty()) {
                        Toast.makeText(context, "Please select at least one sport", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // All validations passed, proceed with registration
                    viewModel.registerUser(
                        onSuccess = { isApproved ->
                            if (isApproved) {
                                navController.navigate("userDashboard")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Coach registration submitted. Awaiting admin approval.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
            ) {
                Text("Register", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}