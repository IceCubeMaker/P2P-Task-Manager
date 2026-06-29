package com.p2ptaskmanager.data.sync

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class TransportType { NEARBY, LAN, MQTT, WEBSOCKET }

interface P2PTransport {
    val discoveredPeers: StateFlow<List<String>>
    val connectedPeerIds: StateFlow<List<String>>
    val incomingPayloads: SharedFlow<Pair<String, SyncPayload>>

    suspend fun startDiscovery()
    suspend fun stopDiscovery()
    suspend fun connectTo(peerId: String)
    suspend fun disconnect(peerId: String)
    suspend fun sendPayload(toPeerId: String, payload: SyncPayload)
    suspend fun sendManifest(toPeerId: String, manifest: SyncManifest)
    suspend fun broadcastPresence(groupId: String)
}
