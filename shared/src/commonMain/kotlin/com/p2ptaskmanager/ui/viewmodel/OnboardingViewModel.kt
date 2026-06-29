package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.Group
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 0,
    val displayName: String = "",
    val firstGroupName: String = "My Tasks",
    val isDone: Boolean = false,
    val isOnboardingRequired: Boolean = true
)

class OnboardingViewModel(
    private val prefs: PrefsRepository,
    private val groupRepo: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val done = prefs.isOnboardingComplete()
            _uiState.value = _uiState.value.copy(isOnboardingRequired = !done, isDone = done)
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun onGroupNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(firstGroupName = name)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(step = _uiState.value.step + 1)
    }

    fun finish() {
        viewModelScope.launch {
            val name = _uiState.value.displayName.trim().ifEmpty { "Me" }
            prefs.setDisplayName(name)
            val peerId = prefs.getPeerId()
            val nowMs = currentTimeMillis()
            val group = Group(
                id = generateId(),
                name = _uiState.value.firstGroupName.trim().ifEmpty { "My Tasks" },
                creatorPeerId = peerId,
                createdAt = nowMs,
                inviteCode = generateId()
            )
            groupRepo.createGroup(group)
            prefs.setOnboardingComplete(true)
            _uiState.value = _uiState.value.copy(isDone = true, isOnboardingRequired = false)
        }
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(16) { repeat(16) { append(chars.random()) } }
    }
}
