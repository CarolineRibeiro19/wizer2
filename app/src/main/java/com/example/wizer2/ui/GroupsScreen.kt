package com.example.wizer2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wizer2.model.Group
import com.example.wizer2.repository.GroupService
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    navController: NavController,
    groupService: GroupService,
    userId: String = "mock-user-id-123"
) {
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }
    var groupCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadGroups() {
        scope.launch {
            loading = true
            try {
                groups = groupService.getGroupsForUser(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Grupos que você participa", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar em Grupo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("groupDetail/${group.id}/${group.name}")
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(group.name, style = MaterialTheme.typography.titleMedium)
                            Text("Matéria: ${group.subjectName ?: "Desconhecida"}")
                            Text("Professor ID: ${group.professorId}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Entrar em Grupo") },
            text = {
                Column {
                    Text("Insira o código do grupo:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupCode,
                        onValueChange = { groupCode = it },
                        placeholder = { Text("Ex: GRP123") }
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val success = groupService.joinGroupWithCode(userId, groupCode.trim())
                        if (success) {
                            errorMessage = null
                            showDialog = false
                            groupCode = ""
                            loadGroups()
                        } else {
                            errorMessage = "Código inválido ou erro ao entrar no grupo."
                        }
                    }
                }) {
                    Text("Entrar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    groupCode = ""
                    errorMessage = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
