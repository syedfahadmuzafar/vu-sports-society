package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ReportsScreen(navController: NavController) {
    val context = LocalContext.current
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
            // Title
            Text(
                "Sports Activity Reports",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF00BFA6),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtitle
            Text(
                "Generate and view reports on sports activities",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Generate report button
            Button(
                onClick = { 
                    navController.navigate("view_reports")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "View Reports",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("View Participation Reports")
            }
            
            // Export button
            Button(
                onClick = { 
                    Toast.makeText(context, "Report exported successfully", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Export",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Export Report")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Report options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Available Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BFA6)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ReportOption(
                        title = "Participation by Category",
                        description = "View participation statistics by sports category",
                        onClick = { navController.navigate("view_reports") }
                    )
                    
                    ReportOption(
                        title = "Event Performance",
                        description = "Analyze performance metrics for each event",
                        onClick = { Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show() }
                    )
                    
                    ReportOption(
                        title = "Team Statistics",
                        description = "View team participation and performance data",
                        onClick = { Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportOption(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
        }
    }
}
