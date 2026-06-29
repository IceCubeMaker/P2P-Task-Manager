package com.p2ptaskmanager.data.sync

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

private const val SERVICE_ID = "com.p2ptaskmanager.nearby"
private const val CHUNK_SIZE = 20

class NearbyTransport(private val context: Context, private val localPeerId: String) : P2PTransport {

    private val client = Nearby.getConnectionsClient(context)

    private val _discovered = MutableStateFlow<List<String>>(emptyList())
    override val discoveredPeers: StateFlow<List<String>> = _discovered.asStateFlow()

    private val _connected = MutableStateFlow<List<String>>(emptyList())
    override val connectedPeerIds: StateFlow<List<String>> = _connected.asStateFlow()

    private val _incoming = MutableSharedFlow<Pair<String, SyncPayload>>(extraBufferCapacity = 64)
    override val incomingPayloads: SharedFlow<Pair<String, SyncPayload>> = _incoming.asSharedFlow()

    private val endpointIdToName = mutableMapOf<String, String>()

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
            endpointIdToName[endpointId] = info.endpointName
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                _connected.update { it + endpointId }
            }
        }
        override fun onDisconnected(endpointId: String) {
            _connected.update { it - endpointId }
            endpointIdToName.remove(endpointId)
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            runCatching {
                val p = Json.decodeFromString(SyncPayload.serializer(), bytes.decodeToString())
                _incoming.tryEmit(endpointId to p)
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            _discovered.update { if (endpointId !in it) it + endpointId else it }
        }
        override fun onEndpointLost(endpointId: String) {
            _discovered.update { it - endpointId }
        }
    }

    override suspend fun startDiscovery() {
        client.startAdvertising(
            localPeerId, SERVICE_ID, connectionCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
        client.startDiscovery(
            SERVICE_ID, discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
    }

    override suspend fun stopDiscovery() {
        client.stopAdvertising()
        client.stopDiscovery()
    }

    override suspend fun connectTo(peerId: String) {
        client.requestConnection(localPeerId, peerId, connectionCallback)
    }

    override suspend fun disconnect(peerId: String) {
        client.disconnectFromEndpoint(peerId)
        _connected.update { it - peerId }
    }

    override suspend fun sendPayload(toPeerId: String, payload: SyncPayload) {
        val json = Json.encodeToString(SyncPayload.serializer(), payload)
        client.sendPayload(toPeerId, Payload.fromBytes(json.encodeToByteArray()))
    }

    override suspend fun sendManifest(toPeerId: String, manifest: SyncManifest) {
        val json = Json.encodeToString(SyncManifest.serializer(), manifest)
        client.sendPayload(toPeerId, Payload.fromBytes(json.encodeToByteArray()))
    }

    override suspend fun broadcastPresence(groupId: String) {
        val peers = _connected.value
        val payload = SyncPayload(
            handshake = SyncHandshake(peerId = localPeerId, displayName = localPeerId, groupIds = listOf(groupId))
        )
        peers.forEach { sendPayload(it, payload) }
    }
}
