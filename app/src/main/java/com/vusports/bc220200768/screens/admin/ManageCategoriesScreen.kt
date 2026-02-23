package com.vusports.bc220200768.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vusports.bc220200768.components.Category
import com.vusports.bc220200768.viewmodel.admin.ManageCategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    viewModel: ManageCategoriesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val organizers by viewModel.organizers.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val newCategoryName by viewModel.newCategoryName.collectAsState()
    val newRoles by viewModel.newRoles.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val message by viewModel.message.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Show snackbar for messages
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Sports Categories",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF00BFA6),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Add new category section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Add New Sports Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BFA6)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { viewModel.updateNewCategoryName(it) },
                                label = { Text("Category Name") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00BFA6),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    viewModel.addCategory(
                                        onSuccess = {
                                            Toast.makeText(context, "Category added successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00BFA6)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Category"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Add Roles for this Category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentRole,
                                onValueChange = { viewModel.updateCurrentRole(it) },
                                label = { Text("Role Name") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00BFA6),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = { viewModel.addRole() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA6))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Role")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display added roles
                        if (newRoles.isNotEmpty()) {
                            Text(
                                "Added Roles:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 120.dp)
                                    .fillMaxWidth()
                            ) {
                                items(newRoles) { role ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            role,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeRole(role) }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove Role",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                            }
                        }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Manage Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFA6),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00BFA6))
                    }
                } else if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No categories available. Add your first category above.",
                            color = Color.Black,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categories) { category ->
                            CategoryCard(
                                category = category,
                                organizers = organizers,
                                onAssign = { name, email ->
                                    viewModel.assignOrganizer(
                                        category.id, name, email,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Organizer assigned",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = {
                                            Toast.makeText(
                                                context,
                                                "Failed to assign",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                onDelete = {
                                    categoryToDelete = category
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '${categoryToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { category ->
                            viewModel.deleteCategory(
                                category.id,
                                onSuccess = {
                                    Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                                    showDeleteConfirmDialog = false
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    showDeleteConfirmDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: Category,
    organizers: List<Pair<String, String>>,
    onAssign: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedName by remember { mutableStateOf(category.organizerName.ifBlank { "Assign Organizer" }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFA6)
                )

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Category"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Organizer: ${if (category.organizerName.isNotBlank()) category.organizerName else "Not Assigned"}",
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00BFA6))
                ) {
                    Text(selectedName)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    organizers.forEach { (name, email) ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.Black) },
                            onClick = {
                                selectedName = name
                                onAssign(name, email)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
