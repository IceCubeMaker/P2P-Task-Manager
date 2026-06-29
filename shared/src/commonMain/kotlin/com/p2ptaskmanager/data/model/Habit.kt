package com.p2ptaskmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HabitCompletion(
    val id: Long = 0,
    val taskId: String,
    val completedAt: Long,
    val peerId: String
)

@Serializable
data class TaskTimeEntry(
    val id: Long = 0,
    val taskId: String,
    val startAt: Long,
    val endAt: Long? = null,
    val noteText: String? = null,
    val peerId: String
) {
    val durationMs: Long get() = if (endAt != null) endAt - startAt else 0L
    val durationMinutes: Double get() = durationMs / 60_000.0
}

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val taskId: String, val startAt: Long, val entryId: Long) : TimerState()
    data class Paused(val taskId: String, val accumulatedMs: Long) : TimerState()
}

data class TodayPlanEntry(
    val id: Long,
    val date: String,
    val taskId: String,
    val sortOrder: Int,
    val isBreak: Boolean = false,
    val breakDurationMinutes: Int? = null,
    val addedManually: Boolean = false,
    // Joined task fields
    val taskTitle: String? = null,
    val estimatedMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val userImportance: Float = 0.5f
)
