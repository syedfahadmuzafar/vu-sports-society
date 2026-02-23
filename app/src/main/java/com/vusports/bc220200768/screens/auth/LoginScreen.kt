package com.vusports.bc220200768.screens.auth

import LoadingOverlay
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vusports.bc220200768.R
import com.vusports.bc220200768.components.FirebaseUtil

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    // Validation function
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validateInputs(): Boolean {
        var isValid = true
        
        // Email validation
        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = ""
        }
        
        // Password validation
        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else {
            passwordError = ""
        }
        
        return isValid
    }

    // 🔙 Back press confirmation
    BackHandler { showExitDialog = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "VU Logo",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(12.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome Back",
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF00BFA6)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign in to your account", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // 📩 Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it 
                    if (emailError.isNotEmpty()) {
                        // Clear error when user starts typing
                        if (it.isNotEmpty()) {
                            emailError = ""
                        }
                    }
                },
                label = { Text("Email", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),
                shape = RoundedCornerShape(10.dp),
                isError = emailError.isNotEmpty(),
                singleLine = true,
                supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    if (passwordError.isNotEmpty()) {
                        // Clear error when user starts typing
                        if (it.isNotEmpty()) {
                            passwordError = ""
                        }
                    }
                },
                label = { Text("Password", color = Color.Black) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),
                shape = RoundedCornerShape(10.dp),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔘 Login Button
            Button(
                onClick = {
                    if (validateInputs()) {
                        isLoading = true
                        FirebaseUtil.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                FirebaseUtil.firestore.collection("users").document(email.lowercase())
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        isLoading = false
                                        if (doc.exists()) {
                                            val role = doc.getString("role")
                                            val status = doc.getString("status") ?: "pending"

                                            when (status) {
                                                "approved" -> {
                                                    when (role) {
                                                        "admin" -> navController.navigate("adminDashboard")
                                                        "participant" -> navController.navigate("userDashboard")
                                                        "coach" -> navController.navigate("coachDashboard")
                                                        else -> Toast.makeText(context, "Unknown role.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                "partially_approved" -> {
                                                    // For participants with some approved preferences
                                                    if (role == "participant") {
                                                        // Get approved and rejected preferences
                                                        val approvedPreferences = doc.get("approved_preferences") as? List<*> ?: emptyList<String>()
                                                        val rejectedPreferences = doc.get("rejected_preferences") as? List<*> ?: emptyList<String>()
                                                        val preferences = doc.get("preferences") as? List<*> ?: emptyList<String>()
                                                        
                                                        // Check if at least one preference is approved
                                                        if (approvedPreferences.isNotEmpty()) {
                                                            // Show message about partially approved status
                                                            val approvedCount = approvedPreferences.size
                                                            val rejectedCount = rejectedPreferences.size
                                                            val totalCount = preferences.size
                                                            val pendingCount = totalCount - approvedCount - rejectedCount
                                                            
                                                            val message = if (rejectedCount > 0) {
                                                                "$approvedCount approved, $rejectedCount rejected, $pendingCount pending. You can only participate in approved sports."
                                                            } else {
                                                                "$approvedCount of $totalCount sports preferences approved. You can only participate in approved sports."
                                                            }
                                                            
                                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                            
                                                            // Navigate to dashboard
                                                            navController.navigate("userDashboard")
                                                        } else {
                                                            // No approved preferences
                                                            Toast.makeText(context, "None of your sports preferences have been approved yet.", Toast.LENGTH_LONG).show()
                                                            FirebaseUtil.auth.signOut()
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "Account status error. Contact support.", Toast.LENGTH_LONG).show()
                                                        FirebaseUtil.auth.signOut()
                                                    }
                                                }
                                                "pending_admin" -> {
                                                    Toast.makeText(context, "Your account is pending admin approval.", Toast.LENGTH_LONG).show()
                                                    FirebaseUtil.auth.signOut()
                                                }
                                                "pending_coach" -> {
                                                    Toast.makeText(context, "Your account is pending coach approval.", Toast.LENGTH_LONG).show()
                                                    FirebaseUtil.auth.signOut()
                                                }
                                                else -> {
                                                    Toast.makeText(context, "Account not active. Contact support.", Toast.LENGTH_LONG).show()
                                                    FirebaseUtil.auth.signOut()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "User profile not found.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Login failed: ${authTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
            ) {
                Text("Sign In", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    color = Color(0xFF00BFA6),
                    modifier = Modifier.clickable {
                        navController.navigate("forgot_password")
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Don't have an account? Register",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }

        // 🔄 Loading overlay
        if (isLoading) LoadingOverlay()

        // ❓ Exit App Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App?") },
                text = { Text("Are you sure you want to close the app?") },
                confirmButton = {
                    TextButton(onClick = { activity?.finish() }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("No") }
                }
            )
        }
    }
}
