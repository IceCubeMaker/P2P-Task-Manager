package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.Group
import com.p2ptaskmanager.data.model.GroupMember
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    val peerId: String = ""
)

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val isLoading: Boolean = true,
    val qrPayload: String? = null
)

class GroupsViewModel(
    private val groupRepo: GroupRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val peerId = prefs.getPeerId()
            _uiState.value = _uiState.value.copy(peerId = peerId)
            groupRepo.observeGroups().collectLatest { groups ->
                _uiState.value = _uiState.value.copy(groups = groups, isLoading = false)
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            val peerId = _uiState.value.peerId
            val nowMs = currentTimeMillis()
            val group = Group(
                id = generateId(),
                name = name.trim(),
                creatorPeerId = peerId,
                createdAt = nowMs,
                inviteCode = generateId()
            )
            groupRepo.createGroup(group)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch { groupRepo.deleteGroup(groupId) }
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(16) { repeat(16) { append(chars.random()) } }
    }
}

class GroupDetailViewModel(
    private val groupRepo: GroupRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun load(groupId: String) {
        viewModelScope.launch {
            val group = groupRepo.getGroup(groupId)
            _uiState.value = _uiState.value.copy(group = group, isLoading = false)
            groupRepo.observeMembers(groupId).collectLatest { members ->
                _uiState.value = _uiState.value.copy(members = members)
            }
        }
    }

    fun generateQrPayload(groupId: String) {
        viewModelScope.launch {
            val peerId = prefs.getPeerId()
            val displayName = prefs.getDisplayName()
            val broker = prefs.getMqttBroker()
            val group = groupRepo.getGroup(groupId) ?: return@launch
            val payload = kotlinx.serialization.json.Json.encodeToString(
                com.p2ptaskmanager.data.sync.QrPairingPayload.serializer(),
                com.p2ptaskmanager.data.sync.QrPairingPayload(
                    peerId = peerId,
                    displayName = displayName,
                    groupId = groupId,
                    groupName = group.name,
                    inviteCode = group.inviteCode,
                    mqttBroker = broker
                )
            )
            _uiState.value = _uiState.value.copy(qrPayload = payload)
        }
    }

    fun acceptPairingPayload(payloadJson: String) {
        viewModelScope.launch {
            runCatching {
                val payload = kotlinx.serialization.json.Json.decodeFromString(
                    com.p2ptaskmanager.data.sync.QrPairingPayload.serializer(), payloadJson
                )
                val group = groupRepo.getGroup(payload.groupId)
                if (group != null && group.inviteCode == payload.inviteCode) {
                    val member = com.p2ptaskmanager.data.model.GroupMember(
                        groupId = payload.groupId,
                        peerId = payload.peerId,
                        displayName = payload.displayName,
                        deviceName = "Remote",
                        joinedAt = currentTimeMillis()
                    )
                    groupRepo.upsertMember(member)
                }
            }
        }
    }
}
