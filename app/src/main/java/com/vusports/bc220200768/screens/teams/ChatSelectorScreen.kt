package com.vusports.bc220200768.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChatSelectorScreen(navController: NavController, currentUserEmail: String) {
    var userRole by remember { mutableStateOf("") }

    // 🔄 Fetch user role from Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                userRole = doc?.getString("role") ?: ""
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select Chat",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF00BFA6)
        )

        ChatOptionCard("Chat with Team", onClick = {
            navController.navigate("chat/team/global/$currentUserEmail")
        })

        if (userRole == "coach") {
            ChatOptionCard("Chat with Participants", onClick = {
                navController.navigate("chat/coach/global/$currentUserEmail")
            })
        } else if (userRole == "participant") {
            ChatOptionCard("Chat with Coach", onClick = {
                navController.navigate("chat/coach/global/$currentUserEmail")
            })
        }

        ChatOptionCard("Chat with Organizers", onClick = {
            navController.navigate("chat/organizer/global/$currentUserEmail")
        })
    }
}

@Composable
fun ChatOptionCard(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = title,
                tint = Color(0xFF00BFA6)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color.Black
            )
        }
    }
}
