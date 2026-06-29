package com.p2ptaskmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val creatorPeerId: String,
    val createdAt: Long,
    val inviteCode: String,
    val color: Int = 0
)

@Serializable
data class GroupMember(
    val groupId: String,
    val peerId: String,
    val displayName: String,
    val deviceName: String,
    val avatarColor: Int = 0,
    val joinedAt: Long,
    val lastSeenAt: Long = 0L
)
