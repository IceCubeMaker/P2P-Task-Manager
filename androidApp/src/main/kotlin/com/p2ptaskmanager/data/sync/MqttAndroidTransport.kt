package com.p2ptaskmanager.data.sync

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

private const val TOPIC_PREFIX = "p2ptask"

class MqttAndroidTransport(
    context: Context,
    private val brokerUrl: String,
    private val localPeerId: String
) : P2PTransport {

    private val client = MqttAndroidClient(context, brokerUrl, localPeerId)

    private val _discovered = MutableStateFlow<List<String>>(emptyList())
    override val discoveredPeers: StateFlow<List<String>> = _discovered.asStateFlow()

    private val _connected = MutableStateFlow<List<String>>(emptyList())
    override val connectedPeerIds: StateFlow<List<String>> = _connected.asStateFlow()

    private val _incoming = MutableSharedFlow<Pair<String, SyncPayload>>(extraBufferCapacity = 64)
    override val incomingPayloads: SharedFlow<Pair<String, SyncPayload>> = _incoming.asSharedFlow()

    init {
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {}
            override fun messageArrived(topic: String, message: MqttMessage) {
                val parts = topic.split("/")
                if (parts.size < 4) return
                val fromPeerId = parts[2]
                if (fromPeerId == localPeerId) return
                runCatching {
                    val payload = Json.decodeFromString(SyncPayload.serializer(), message.toString())
                    _incoming.tryEmit(fromPeerId to payload)
                    if (fromPeerId !in _discovered.value) {
                        _discovered.value = _discovered.value + fromPeerId
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }

    fun connect(groupId: String) {
        val opts = MqttConnectOptions().apply {
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
        }
        client.connect(opts, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                client.subscribe("$TOPIC_PREFIX/$groupId/+/$localPeerId", 1)
                client.subscribe("$TOPIC_PREFIX/$groupId/+/all", 1)
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
        })
    }

    override suspend fun startDiscovery() {}
    override suspend fun stopDiscovery() {
        runCatching { client.disconnect() }
    }

    override suspend fun connectTo(peerId: String) {}
    override suspend fun disconnect(peerId: String) {}

    override suspend fun sendPayload(toPeerId: String, payload: SyncPayload) {
        val topic = "$TOPIC_PREFIX/${payload.groupId}/$localPeerId/$toPeerId"
        val json = Json.encodeToString(SyncPayload.serializer(), payload)
        val msg = MqttMessage(json.encodeToByteArray()).apply { qos = 1 }
        runCatching { client.publish(topic, msg) }
    }

    override suspend fun sendManifest(toPeerId: String, manifest: SyncManifest) {
        val topic = "$TOPIC_PREFIX/${manifest.groupId}/$localPeerId/$toPeerId"
        val json = Json.encodeToString(SyncManifest.serializer(), manifest)
        val msg = MqttMessage(json.encodeToByteArray()).apply { qos = 1 }
        runCatching { client.publish(topic, msg) }
    }

    override suspend fun broadcastPresence(groupId: String) {
        val topic = "$TOPIC_PREFIX/$groupId/$localPeerId/all"
        val payload = SyncPayload(
            handshake = SyncHandshake(peerId = localPeerId, displayName = localPeerId, groupIds = listOf(groupId))
        )
        val json = Json.encodeToString(SyncPayload.serializer(), payload)
        val msg = MqttMessage(json.encodeToByteArray()).apply { qos = 1 }
        runCatching { client.publish(topic, msg) }
    }
}
