package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizer2.models.Group
import com.example.wizer2.models.GroupMembers
import com.example.wizer2.models.Subject
import com.example.wizer2.services.GroupMembersService
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.SubjectService
import com.example.wizer2.services.UserService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class GroupWithDetails(
    val group: Group,
    val subject: Subject?,
    val members: List<GroupMembers>,
    val totalMembers: Int,
    val averagePoints: Double
)

data class GroupMemberWithUser(
    val groupMember: GroupMembers,
    val user: com.example.wizer2.models.User?
)

class GroupViewModel(
    private val groupService: GroupService,
    private val groupMembersService: GroupMembersService,
    private val subjectService: SubjectService,
    private val userService: UserService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _professorGroups = MutableStateFlow<List<Group>>(emptyList())

    private val _groupMembersForProfessorGroups = MutableStateFlow<List<GroupMembers>>(emptyList())
    val groupMembers: StateFlow<List<GroupMembers>> = _groupMembersForProfessorGroups.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _groupMembersWithUsers = MutableStateFlow<List<GroupMemberWithUser>>(emptyList())
    val groupMembersWithUsers: StateFlow<List<GroupMemberWithUser>> = _groupMembersWithUsers.asStateFlow()

    // Combined state for groups with their details
    val groupsWithDetails: StateFlow<List<GroupWithDetails>> = combine(
        _professorGroups,
        _subjects,
        _groupMembersForProfessorGroups
    ) { groups, subjects, allMembers ->
        groups.map { group ->
            val subject = subjects.find { it.id == group.subjectId }
            val members = allMembers.filter { it.groupId == group.id }
            val averagePoints = if (members.isNotEmpty()) {
                members.map { it.points }.average()
            } else 0.0

            GroupWithDetails(
                group = group,
                subject = subject,
                members = members,
                totalMembers = members.size,
                averagePoints = averagePoints
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
        loadAllData()
    }

    fun loadAllData() {
        val userId = _currentUserId.value
        if (userId != null) {
            loadProfessorGroups(userId)
            loadSubjects()
        }
    }

    private fun loadProfessorGroups(professorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val groups = groupService.getGroupsByProfessor(professorId)
                _professorGroups.value = groups

                // Load members for all groups
                val allMembers = mutableListOf<GroupMembers>()
                groups.forEach { group ->
                    val members = groupMembersService.getMembersByGroup(group.id)
                    allMembers.addAll(members)
                }
                _groupMembersForProfessorGroups.value = allMembers

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load groups"
                )
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                val subjects = subjectService.getAllSubjects()
                _subjects.value = subjects
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load subjects"
                )
            }
        }
    }

    fun loadGroupMembersWithUsers(groupId: String) {
        viewModelScope.launch {
            try {
                val members = groupMembersService.getMembersByGroup(groupId)
                val membersWithUsers = members.map { member ->
                    val user = userService.getUserById(member.userId)
                    GroupMemberWithUser(member, user)
                }
                _groupMembersWithUsers.value = membersWithUsers
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load group members"
                )
            }
        }
    }

    fun createGroup(name: String, subjectId: String, description: String?) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val newGroup = Group(
                    id = "", // Will be generated by Supabase
                    name = name,
                    subjectId = subjectId,
                    professorId = userId,
                    description = description
                )
                val createdGroup = groupService.createGroup(newGroup)
                _professorGroups.value += createdGroup
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create group"
                )
            }
        }
    }

    fun updateGroup(groupId: String, name: String, subjectId: String, description: String?) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val updatedGroup = Group(
                    id = groupId,
                    name = name,
                    subjectId = subjectId,
                    professorId = userId,
                    description = description
                )
                groupService.updateGroup(updatedGroup)

                // Update local state
                _professorGroups.value = _professorGroups.value.map { group ->
                    if (group.id == groupId) updatedGroup else group
                }

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update group"
                )
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                groupService.deleteGroup(groupId)

                // Update local state
                _professorGroups.value = _professorGroups.value.filter { it.id != groupId }
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value
                    .filter { it.groupId != groupId }

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete group"
                )
            }
        }
    }

    fun addMemberToGroup(groupId: String, userEmail: String) {
        viewModelScope.launch {
            try {
                // First, find the user by email
                val user = userService.getUserByEmail(userEmail)
                if (user == null) {
                    _uiState.value = _uiState.value.copy(error = "User with email $userEmail not found")
                    return@launch
                }

                // Check if user is already a member
                val existingMember = _groupMembersForProfessorGroups.value
                    .find { it.groupId == groupId && it.userId == user.id }
                if (existingMember != null) {
                    _uiState.value = _uiState.value.copy(error = "User is already a member of this group")
                    return@launch
                }

                val newMember = GroupMembers(
                    groupId = groupId,
                    userId = user.id,
                    points = 0
                )
                val createdMember = groupMembersService.addMember(newMember)
                _groupMembersForProfessorGroups.value += createdMember
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to add member")
            }
        }
    }

    fun removeMemberFromGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupMembersService.removeMember(groupId, userId)

                // Update local state
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value
                    .filter { !(it.groupId == groupId && it.userId == userId) }

                // Update members with users list
                _groupMembersWithUsers.value = _groupMembersWithUsers.value
                    .filter { !(it.groupMember.groupId == groupId && it.groupMember.userId == userId) }

                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to remove member")
            }
        }
    }

    fun updateMemberPoints(groupId: String, userId: String, points: Int) {
        viewModelScope.launch {
            try {
                groupMembersService.updatePoints(groupId, userId, points)

                // Update local state
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value.map { member ->
                    if (member.groupId == groupId && member.userId == userId) {
                        member.copy(points = points)
                    } else member
                }

                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update points")
            }
        }
    }

    fun getGroupById(groupId: String): Group? {
        return _professorGroups.value.find { it.id == groupId }
    }
}

// Extension function for easier data access
fun GroupViewModel.getGroupWithDetails(groupId: String): GroupWithDetails? {
    return groupsWithDetails.value.find { it.group.id == groupId }
}