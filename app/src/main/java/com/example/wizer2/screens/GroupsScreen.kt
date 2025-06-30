package com.example.wizer2.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wizer2.models.Group
import com.example.wizer2.services.GroupService
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    navController: NavController,
    groupService: GroupService,
    userId: String
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
            Log.d("GroupsDebug", "ID do user loggado: $userId")
            try {
                Log.d("GroupsDebug", "Carregando grupos para $userId")
                groups = groupService.getGroupsForUser(userId)
                Log.d("GroupsDebug", "Recebido grupos: $groups")
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Erro ao carregar grupos."
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
        Text("Seus Grupos", style = MaterialTheme.typography.headlineSmall)
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
            if (groups.isEmpty()) {
                Text("Você ainda não está em nenhum grupo.")
            } else {
                LazyColumn {
                    items(groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate(
                                        "groupdetail/${group.id} "
                                    )
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(group.name, style = MaterialTheme.typography.titleMedium)
                            }
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
                    Text("Código do grupo:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupCode,
                        onValueChange = { groupCode = it },
                        placeholder = { Text("Ex: GRP123") }
                    )
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
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



