package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizer2.models.Group
import com.example.wizer2.models.GroupMembers
import com.example.wizer2.models.Subject
import com.example.wizer2.services.GroupMembersService
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.SubjectService
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

class GroupViewModel(
    private val groupService: GroupService,
    private val groupMembersService: GroupMembersService,
    private val subjectService: SubjectService
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

    val groups: StateFlow<List<Group>> = _professorGroups.asStateFlow()

    val groupsWithDetails: StateFlow<List<GroupWithDetails>> = combine(
        _professorGroups, // Use the _professorGroups as the primary source
        _groupMembersForProfessorGroups, // Use members relevant to these groups
        _subjects
    ) { profGroups, members, subjects ->
        profGroups.map { group ->
            val subject = subjects.find { it.id == group.subjectId }
            val groupMembers = members.filter { it.groupId == group.id } // Filter members by current group
            val averagePoints = if (groupMembers.isNotEmpty()) {
                groupMembers.map { it.points }.average()
            } else 0.0

            GroupWithDetails(
                group = group,
                subject = subject,
                members = groupMembers,
                totalMembers = groupMembers.size,
                averagePoints = averagePoints
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _currentUserId.collect { userId ->
                if (userId != null) {
                    loadAllData()
                }
            }
        }
    }


    fun setCurrentUserId(userId: String) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
        }
    }

    fun loadAllData() {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User ID not set. Cannot load data.", isLoading = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Step 1: Get groups by the professor ID
                val groupsResult = groupService.getGroupsByProfessor(userId)
                _professorGroups.value = groupsResult

                // Step 2: Get all subjects (typically static or less frequent changes)
                val subjectsResult = subjectService.getAllSubjects()
                _subjects.value = subjectsResult

                // Step 3: For each fetched group, get its members concurrently
                val membersForGroups = groupsResult.map { group ->
                    async { groupMembersService.getMembersByGroup(group.id) }
                }.awaitAll().flatten() // Await all and flatten the list of lists

                _groupMembersForProfessorGroups.value = membersForGroups

                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshGroups() {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User ID not set. Cannot refresh groups.")
            return
        }
        viewModelScope.launch {
            try {
                val groups = groupService.getGroupsByProfessor(userId)
                _professorGroups.value = groups
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // This will refresh members only for the groups currently held in _professorGroups
    fun refreshGroupMembers() {
        val currentGroups = _professorGroups.value
        if (currentGroups.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "No groups loaded to refresh members for.")
            return
        }

        viewModelScope.launch {
            try {
                val membersForGroups = currentGroups.map { group ->
                    async { groupMembersService.getMembersByGroup(group.id) }
                }.awaitAll().flatten()

                _groupMembersForProfessorGroups.value = membersForGroups
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun refreshSubjects() {
        viewModelScope.launch {
            try {
                val subjects = subjectService.getAllSubjects()
                _subjects.value = subjects
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // Utility functions for getting related data
    // These now operate on the _professorGroups and _groupMembersForProfessorGroups
    fun getGroupById(groupId: String): Group? {
        return _professorGroups.value.find { it.id == groupId }
    }

    fun getSubjectById(subjectId: String): Subject? {
        return _subjects.value.find { it.id == subjectId }
    }

    fun getMembersByGroupId(groupId: String): List<GroupMembers> {
        return _groupMembersForProfessorGroups.value.filter { it.groupId == groupId }
    }

    fun getGroupsBySubjectId(subjectId: String): List<Group> {
        return _professorGroups.value.filter { it.subjectId == subjectId }
    }

    fun getGroupsByProfessorId(professorId: String): List<Group> {
        // This function now assumes you're looking for groups specific to the *loaded* professor's data
        // If you need groups for an arbitrary professor, you'd need another service call.
        return _professorGroups.value.filter { it.professorId == professorId }
    }

    fun getMembersByUserId(userId: String): List<GroupMembers> {
        return _groupMembersForProfessorGroups.value.filter { it.userId == userId }
    }

    // Functions to add/update/delete data
    fun addGroup(group: Group) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User ID not set. Cannot add group.")
            return
        }
        viewModelScope.launch {
            try {
                // Ensure the new group is associated with the current professor
                val groupToAdd = group.copy(professorId = userId)
                val newGroup = groupService.createGroup(groupToAdd)
                _professorGroups.value += newGroup
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            try {
                val updatedGroup = groupService.updateGroup(group)
                _professorGroups.value = _professorGroups.value.map {
                    if (it.id == updatedGroup.id) updatedGroup else it
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                groupService.deleteGroup(groupId)
                _professorGroups.value = _professorGroups.value.filter { it.id != groupId }
                // Also remove associated members
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value.filter { it.groupId != groupId }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addGroupMember(groupMember: GroupMembers) {
        viewModelScope.launch {
            try {
                val newMember = groupMembersService.addMember(groupMember)
                _groupMembersForProfessorGroups.value += newMember
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateGroupMemberPoints(groupId: String, userId: String, points: Int) {
        viewModelScope.launch {
            try {
                val updatedMember = groupMembersService.updatePoints(groupId, userId, points)
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value.map { member ->
                    if (member.groupId == groupId && member.userId == userId) {
                        member.copy(points = points)
                    } else member
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeGroupMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupMembersService.removeMember(groupId, userId)
                _groupMembersForProfessorGroups.value = _groupMembersForProfessorGroups.value.filter {
                    !(it.groupId == groupId && it.userId == userId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// Extension function for easier data access
fun GroupViewModel.getGroupWithDetails(groupId: String): GroupWithDetails? {
    return groupsWithDetails.value.find { it.group.id == groupId }
}