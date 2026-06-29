package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.data.sync.P2PTransport
import com.p2ptaskmanager.data.sync.SyncPayload
import com.p2ptaskmanager.data.sync.SyncProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class PeerSyncUiState(
    val discoveredPeers: List<String> = emptyList(),
    val connectedPeers: List<String> = emptyList(),
    val isDiscovering: Boolean = false,
    val lastSyncMs: Long = 0L,
    val mqttBroker: String = "",
    val isMqttConnected: Boolean = false,
    val pendingChanges: Int = 0
)

class PeerSyncViewModel(
    private val transport: P2PTransport,
    private val taskRepo: TaskRepository,
    private val groupRepo: GroupRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeerSyncUiState())
    val uiState: StateFlow<PeerSyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transport.discoveredPeers.collectLatest { peers ->
                _uiState.value = _uiState.value.copy(discoveredPeers = peers)
            }
        }
        viewModelScope.launch {
            transport.connectedPeerIds.collectLatest { peers ->
                _uiState.value = _uiState.value.copy(connectedPeers = peers)
            }
        }
        viewModelScope.launch {
            transport.incomingPayloads.collectLatest { (peerId, payload) ->
                handleIncoming(peerId, payload)
            }
        }
        viewModelScope.launch {
            val broker = prefs.getMqttBroker()
            _uiState.value = _uiState.value.copy(mqttBroker = broker)
        }
    }

    fun startDiscovery() {
        viewModelScope.launch {
            transport.startDiscovery()
            _uiState.value = _uiState.value.copy(isDiscovering = true)
        }
    }

    fun stopDiscovery() {
        viewModelScope.launch {
            transport.stopDiscovery()
            _uiState.value = _uiState.value.copy(isDiscovering = false)
        }
    }

    fun connectTo(peerId: String) {
        viewModelScope.launch { transport.connectTo(peerId) }
    }

    fun disconnect(peerId: String) {
        viewModelScope.launch { transport.disconnect(peerId) }
    }

    fun syncWithPeer(peerId: String, groupId: String) {
        viewModelScope.launch {
            val manifest = taskRepo.getManifest(groupId)
            val syncManifest = com.p2ptaskmanager.data.sync.SyncManifest(
                groupId = groupId,
                entries = manifest.entries.map {
                    com.p2ptaskmanager.data.sync.ManifestEntry(it.key, it.value)
                }
            )
            transport.sendManifest(peerId, syncManifest)
        }
    }

    private suspend fun handleIncoming(fromPeerId: String, payload: SyncPayload) {
        payload.tasks.forEach { incoming ->
            val existing = taskRepo.getTask(incoming.id)
            val merged = if (existing == null) incoming
            else SyncProtocol.merge(existing, incoming)
            if (merged != existing) {
                taskRepo.upsertFromSync(merged)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { transport.stopDiscovery() }
    }
}
