package com.example.wizer2.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Grupos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (groups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Você ainda não está em nenhum grupo.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(groups) { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("groupdetail/${group.id.trim()}")
                                    },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
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
                        OutlinedTextField(
                            value = groupCode,
                            onValueChange = { groupCode = it },
                            label = { Text("Código do grupo") },
                            placeholder = { Text("Ex: GRP123") },
                            modifier = Modifier.fillMaxWidth()
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
}
