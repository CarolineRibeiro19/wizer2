package com.example.wizer2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.wizer2.models.Role
import com.example.wizer2.models.User
import com.example.wizer2.services.UserService
import com.example.wizer2.vmodels.AuthState
import com.example.wizer2.vmodels.AuthViewModel
import com.example.wizer2.vmodels.AuthViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    onAuthSuccess: (User) -> Unit,
    modifier: Modifier = Modifier,
    userService: UserService,
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userService))
    val authState by authViewModel.authState.collectAsState()

    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }

    // Use LaunchedEffect to react to authState changes
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess((authState as AuthState.Success).user)
            authViewModel.resetAuthState() // Reset state after success to avoid re-triggering
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = if (isLogin) "Login" else "Register", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        if (!isLogin) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            DropdownMenuWithRoles(selectedRole = role, onRoleSelected = { role = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLogin) {
                    authViewModel.login(email, password)
                } else {
                    authViewModel.register(email, password, username, role)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading // Disable button during loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = if (isLogin) "Login" else "Register")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            isLogin = !isLogin
            authViewModel.resetAuthState()
        }) {
            Text(text = if (isLogin) "Don't have an account? Register" else "Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authState) {
            is AuthState.Error -> Text(text = (authState as AuthState.Error).message, color = Color.Red)
            // AuthState.Success is handled by LaunchedEffect
            else -> {}

        }
    }
}

@Composable
fun DropdownMenuWithRoles(selectedRole: Role, onRoleSelected: (Role) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Role: ${selectedRole.name}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Role.entries.forEach { role ->
                DropdownMenuItem(text = { Text(role.name) }, onClick = {
                    onRoleSelected(role)
                    expanded = false
                })
            }
        }
    }
}