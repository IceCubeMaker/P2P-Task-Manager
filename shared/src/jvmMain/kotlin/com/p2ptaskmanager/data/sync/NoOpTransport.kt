package com.p2ptaskmanager.data.sync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NoOpTransport : P2PTransport {
    override val discoveredPeers: StateFlow<List<String>> = MutableStateFlow(emptyList<String>()).asStateFlow()
    override val connectedPeerIds: StateFlow<List<String>> = MutableStateFlow(emptyList<String>()).asStateFlow()
    override val incomingPayloads: SharedFlow<Pair<String, SyncPayload>> = MutableSharedFlow<Pair<String, SyncPayload>>().asSharedFlow()
    override suspend fun startDiscovery() {}
    override suspend fun stopDiscovery() {}
    override suspend fun connectTo(peerId: String) {}
    override suspend fun disconnect(peerId: String) {}
    override suspend fun sendPayload(toPeerId: String, payload: SyncPayload) {}
    override suspend fun sendManifest(toPeerId: String, manifest: SyncManifest) {}
    override suspend fun broadcastPresence(groupId: String) {}
}
