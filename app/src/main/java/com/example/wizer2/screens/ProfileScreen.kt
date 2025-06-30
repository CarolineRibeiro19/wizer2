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
                errorMessage = "Erro ao carregar usuário"
            }
            loading = false
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar nome de usuário") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Novo nome de usuário") }
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
                            errorMessage = "Erro ao atualizar nome"
                        }
                    }
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Seu Perfil", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Usuário: $username", style = MaterialTheme.typography.bodyLarge)
                Text("Email: $email", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
                    newUsername = TextFieldValue(username)
                    showEditDialog = true
                }) {
                    Text("Editar nome de usuário")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            userService.signOut()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}



