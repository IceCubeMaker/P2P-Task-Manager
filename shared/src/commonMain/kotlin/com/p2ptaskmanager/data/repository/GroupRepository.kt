package com.p2ptaskmanager.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.p2ptaskmanager.data.model.Group
import com.p2ptaskmanager.data.model.GroupMember
import com.p2ptaskmanager.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GroupRepository(private val db: AppDatabase) {

    private val q = db.groupQueries

    fun observeGroups(): Flow<List<Group>> =
        q.getAllGroups().asFlow().mapToList(Dispatchers.IO).map { rows -> rows.map { it.toGroup() } }

    suspend fun getAllGroups(): List<Group> = withContext(Dispatchers.IO) {
        q.getAllGroups().executeAsList().map { it.toGroup() }
    }

    suspend fun getGroup(id: String): Group? = withContext(Dispatchers.IO) {
        q.getGroupById(id).executeAsOneOrNull()?.toGroup()
    }

    suspend fun createGroup(group: Group) = withContext(Dispatchers.IO) {
        q.insertGroup(
            id = group.id,
            name = group.name,
            creatorPeerId = group.creatorPeerId,
            createdAt = group.createdAt,
            inviteCode = group.inviteCode,
            color = group.color.toLong()
        )
    }

    suspend fun updateGroupName(id: String, name: String) = withContext(Dispatchers.IO) {
        q.updateGroupName(name = name, id = id)
    }

    suspend fun deleteGroup(id: String) = withContext(Dispatchers.IO) {
        q.deleteGroup(id)
    }

    suspend fun getMembersOfGroup(groupId: String): List<GroupMember> = withContext(Dispatchers.IO) {
        q.getMembersForGroup(groupId).executeAsList().map { it.toMember() }
    }

    suspend fun upsertMember(member: GroupMember) = withContext(Dispatchers.IO) {
        q.insertMember(
            groupId = member.groupId,
            peerId = member.peerId,
            displayName = member.displayName,
            deviceName = member.deviceName,
            avatarColor = member.avatarColor.toLong(),
            joinedAt = member.joinedAt,
            lastSeenAt = member.lastSeenAt
        )
    }

    suspend fun getGroupIdsForPeer(peerId: String): List<String> = withContext(Dispatchers.IO) {
        q.getGroupsForPeer(peerId).executeAsList()
    }

    fun observeMembers(groupId: String): Flow<List<GroupMember>> =
        q.getMembersForGroup(groupId).asFlow().mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toMember() } }
}

// SELECT * FROM groups → SQLDelight generates the table class Groups
private fun com.p2ptaskmanager.db.Groups.toGroup() = Group(
    id = id,
    name = name,
    creatorPeerId = creatorPeerId,
    createdAt = createdAt,
    inviteCode = inviteCode,
    color = color.toInt()
)

// SELECT * FROM group_members → SQLDelight generates GroupMembers (PascalCase of table name)
private fun com.p2ptaskmanager.db.GroupMembers.toMember() = GroupMember(
    groupId = groupId,
    peerId = peerId,
    displayName = displayName,
    deviceName = deviceName,
    avatarColor = avatarColor.toInt(),
    joinedAt = joinedAt,
    lastSeenAt = lastSeenAt
)
