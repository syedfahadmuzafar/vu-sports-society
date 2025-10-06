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
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReportsScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var reports by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val eventsSnapshot = db.collection("events").get().await()
            val eventCategoryMap = eventsSnapshot.documents.associate {
                it.id to (it.getString("category") ?: "Unknown")
            }

            val registrationSnapshot = db.collection("event_registrations").get().await()
            val categoryCounts = mutableMapOf<String, MutableSet<String>>() // category -> unique users

            for (doc in registrationSnapshot.documents) {
                val eventId = doc.getString("eventId")
                val user = doc.getString("user")
                val category = eventCategoryMap[eventId] ?: "Unknown"

                if (user != null) {
                    categoryCounts.getOrPut(category) { mutableSetOf() }.add(user)
                }
            }

            reports = if (categoryCounts.isEmpty()) {
                listOf("No participation data available.")
            } else {
                categoryCounts.map { (category, users) ->
                    "$category: ${users.size} participant(s)"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading report", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Screen title
            Text(
                "📊 Participation Reports",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF00BFA6)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 🔹 Subtitle
            Text(
                "Summary by Category",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (loading) {
                CircularProgressIndicator()
            } else {
                reports.forEach { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = report,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
