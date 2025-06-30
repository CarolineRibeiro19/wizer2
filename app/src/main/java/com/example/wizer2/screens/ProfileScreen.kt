package com.example.wizer2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wizer2.services.UserService
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userService: UserService,
    onLogout: () -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            val user = userService.getCurrentUser()
            if (user != null) {
                username = user.username
                email = user.email
            } else {
                errorMessage = "Failed to load user data."
            }
            loading = false
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val updatedUser = userService.updateUsername(newUsername.text)
                        if (updatedUser != null) {
                            username = updatedUser.username
                            showEditDialog = false
                        } else {
                            errorMessage = "Error updating username"
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
            } else {
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👤 Username", style = MaterialTheme.typography.labelMedium)
                    Text(username, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("📧 Email", style = MaterialTheme.typography.labelMedium)
                    Text(email, style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            newUsername = TextFieldValue(username)
                            showEditDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Username")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                userService.signOut()
                                onLogout()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}
