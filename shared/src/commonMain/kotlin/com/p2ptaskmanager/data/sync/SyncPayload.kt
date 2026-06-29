package com.p2ptaskmanager.data.sync

import com.p2ptaskmanager.data.model.Group
import com.p2ptaskmanager.data.model.GroupMember
import com.p2ptaskmanager.data.model.HabitCompletion
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.model.TaskTimeEntry
import kotlinx.serialization.Serializable

@Serializable
data class SyncHandshake(
    val peerId: String,
    val displayName: String,
    val groupIds: List<String>
)

@Serializable
data class SyncManifest(
    val groupId: String,
    val entries: List<ManifestEntry>
)

@Serializable
data class ManifestEntry(val id: String, val vectorClock: String)

@Serializable
data class QrPairingPayload(
    val peerId: String,
    val displayName: String,
    val groupId: String,
    val groupName: String,
    val inviteCode: String,
    val mqttBroker: String
)

@Serializable
data class SyncPayload(
    val type: String = "SYNC",
    val groupId: String = "",
    val handshake: SyncHandshake? = null,
    val manifest: SyncManifest? = null,
    val requestedTaskIds: List<String> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val groups: List<Group> = emptyList(),
    val members: List<GroupMember> = emptyList(),
    val habitCompletions: List<HabitCompletion> = emptyList(),
    val timeEntries: List<TaskTimeEntry> = emptyList(),
    val chunkIndex: Int = 0,
    val totalChunks: Int = 1
)
