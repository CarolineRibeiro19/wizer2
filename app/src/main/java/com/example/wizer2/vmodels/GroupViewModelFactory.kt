package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.GroupMembersService
import com.example.wizer2.services.SubjectService
import com.example.wizer2.services.UserService

class GroupViewModelFactory(
    private val groupService: GroupService,
    private val groupMembersService: GroupMembersService,
    private val subjectService: SubjectService,
    private val userService: UserService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(
                groupService,
                groupMembersService,
                subjectService,
                userService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
