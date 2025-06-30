package com.example.wizer2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.GroupMembers
import com.example.wizer2.vmodels.GroupViewModel
import com.example.wizer2.vmodels.GroupWithDetails
import com.example.wizer2.vmodels.GroupMemberWithUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit,
    onEditGroup: () -> Unit = {},
    modifier: Modifier = Modifier
) {    val uiState by viewModel.uiState.collectAsState()
    val groupsWithDetails by viewModel.groupsWithDetails.collectAsState()
    val groupMembersWithUsers by viewModel.groupMembersWithUsers.collectAsState()

    val currentGroupWithDetails = groupsWithDetails.find { it.group.id == groupId }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAllData()
        viewModel.loadGroupMembersWithUsers(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentGroupWithDetails?.group?.name ?: "Group Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditGroup) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Group")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMemberDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { paddingValues ->
        if (currentGroupWithDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group Info Header
                item {
                    GroupInfoCard(groupWithDetails = currentGroupWithDetails)
                }

                // Group Statistics
                item {
                    GroupStatisticsCard(groupWithDetails = currentGroupWithDetails)
                }

                // Members Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Members (${currentGroupWithDetails.totalMembers})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Members List
                if (groupMembersWithUsers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {                                Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No members yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Add students to this group",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }                } else {
                    items(groupMembersWithUsers) { memberWithUser ->
                        MemberWithUserCard(
                            memberWithUser = memberWithUser,
                            onRemoveMember = {
                                viewModel.removeMemberFromGroup(groupId, memberWithUser.groupMember.userId)
                                viewModel.loadGroupMembersWithUsers(groupId) // Reload after removal
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddMemberDialog = false
                newMemberEmail = ""
            },
            title = { Text("Add Member") },
            text = {
                Column {
                    Text("Enter the student's email address:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(                    onClick = {
                    if (newMemberEmail.isNotBlank()) {
                        viewModel.addMemberToGroup(groupId, newMemberEmail)
                        showAddMemberDialog = false
                        newMemberEmail = ""
                        // Reload members after adding
                        viewModel.loadGroupMembersWithUsers(groupId)
                    }
                },
                    enabled = newMemberEmail.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddMemberDialog = false
                        newMemberEmail = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GroupInfoCard(
    groupWithDetails: GroupWithDetails,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = groupWithDetails.group.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            groupWithDetails.subject?.let { subject ->
                Text(
                    text = "Subject: ${subject.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            groupWithDetails.group.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupStatisticsCard(
    groupWithDetails: GroupWithDetails,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {                StatItem(
                icon = Icons.Default.Person,
                label = "Members",
                value = groupWithDetails.totalMembers.toString()
            )

                StatItem(
                    icon = Icons.Default.Star,
                    label = "Avg Points",
                    value = String.format("%.1f", groupWithDetails.averagePoints)
                )

                StatItem(
                    icon = Icons.Default.List,
                    label = "Quizzes",
                    value = "0" // TODO: Add quiz count when available
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemberWithUserCard(
    memberWithUser: GroupMemberWithUser,
    onRemoveMember: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memberWithUser.user?.username ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = memberWithUser.user?.email ?: "No email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${memberWithUser.groupMember.points} points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onRemoveMember
            ) {                Icon(
                Icons.Default.Delete,
                contentDescription = "Remove Member",
                tint = MaterialTheme.colorScheme.error
            )
            }
        }
    }
}
