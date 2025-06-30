package com.example.wizer2.screens

import android.util.Log
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
import com.example.wizer2.models.User
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.UserService
import kotlinx.coroutines.launch

@Composable
fun GroupDetailScreen(
    groupId: String,
    groupService: GroupService,
    userService: UserService,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var group by remember { mutableStateOf<Group?>(null) }
    var professor by remember { mutableStateOf<User?>(null) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(groupId) {
        Log.d("TEST", "Carregando detalhes do grupo $groupId")
        scope.launch {
            try {
                group = groupService.getGroupById(groupId.trim())
                group?.let {
                    professor = userService.getUserById(it.professorId)
                    members = groupService.getGroupMembers(it.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        group?.let { g ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text("📘 ${g.name}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👨‍🏫 Professor", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Nome: ${professor?.username ?: "Desconhecido"}")
                        Text("Email: ${professor?.email ?: "Desconhecido"}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("👥 Participantes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (members.isEmpty()) {
                    Text("Nenhum membro encontrado.")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(members) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("• ${user.username}", style = MaterialTheme.typography.bodyLarge)
                                    Text("${user.email}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.navigate("exercises/${g.subjectId}") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🧠 Exercícios")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { /* navController.navigate("meetings/${g.id}") */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📅 Reuniões")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("❌ Grupo não encontrado.")
            }
        }
    }
}
