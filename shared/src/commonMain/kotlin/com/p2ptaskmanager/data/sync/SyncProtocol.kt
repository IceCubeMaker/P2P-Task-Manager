package com.p2ptaskmanager.data.sync

import com.p2ptaskmanager.data.model.Task

object SyncProtocol {

    fun shouldAcceptIncoming(local: Task?, incoming: Task): Boolean {
        if (local == null) return true
        if (local.isDeleted && !incoming.isDeleted) return false
        return true
    }

    fun merge(local: Task, incoming: Task): Task {
        val localClock = VectorClock.fromJson(local.vectorClock)
        val incomingClock = VectorClock.fromJson(incoming.vectorClock)
        val relation = localClock.compareTo(incomingClock)

        return when (relation) {
            ClockRelation.AFTER -> local
            ClockRelation.BEFORE -> applyCompletionOr(local, incoming.copy(
                vectorClock = incomingClock.merge(localClock).toJson()
            ))
            ClockRelation.EQUAL -> local
            ClockRelation.CONCURRENT -> resolveConflict(local, incoming)
        }
    }

    private fun resolveConflict(local: Task, incoming: Task): Task {
        val mergedClock = VectorClock.fromJson(local.vectorClock)
            .merge(VectorClock.fromJson(incoming.vectorClock))
        val editWinner = if (local.updatedAt >= incoming.updatedAt) local else incoming
        return applyCompletionOr(editWinner.copy(vectorClock = mergedClock.toJson()), incoming)
    }

    private fun applyCompletionOr(base: Task, other: Task): Task {
        if (!base.isCompleted && other.isCompleted) {
            return base.copy(
                isCompleted = true,
                completedAt = other.completedAt,
                completedByPeerId = other.completedByPeerId,
                bujoState = com.p2ptaskmanager.data.model.BujoState.COMPLETED
            )
        }
        return base
    }

    fun computeNeeds(theirManifest: SyncManifest, ourEntries: List<ManifestEntry>): List<String> {
        val ourMap = ourEntries.associate { it.id to VectorClock.fromJson(it.vectorClock) }
        return theirManifest.entries.filter { entry ->
            val ours = ourMap[entry.id]
            ours == null || ours.compareTo(VectorClock.fromJson(entry.vectorClock)) == ClockRelation.BEFORE
        }.map { it.id }
    }

    fun computeWhatTheyNeed(theirManifest: SyncManifest, ourEntries: List<ManifestEntry>): List<String> {
        val theirMap = theirManifest.entries.associate { it.id to VectorClock.fromJson(it.vectorClock) }
        return ourEntries.filter { entry ->
            val theirs = theirMap[entry.id]
            theirs == null || VectorClock.fromJson(entry.vectorClock).compareTo(theirs) == ClockRelation.AFTER
        }.map { it.id }
    }
}
