package com.p2ptaskmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val groupId: String,
    val creatorPeerId: String,
    val assignedPeerId: String? = null,
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,
    val userImportance: Float = 0.5f,
    val estimatedMinutes: Int? = null,
    val reminderOffsetMinutes: Int? = null,
    val recurrenceRuleJson: String? = null,
    val colorLabel: Int? = null,
    val manualSortOrder: Long = 0L,
    val bujoState: BujoState = BujoState.OPEN,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val completedByPeerId: String? = null,
    val isRepeating: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val vectorClock: String = "{}",
    val isDeleted: Boolean = false
)

@Serializable
enum class BujoState {
    OPEN,       // • bullet dot
    COMPLETED,  // × cross with strikethrough
    MIGRATED,   // > chevron right
    SCHEDULED,  // < chevron left
    NOTE,       // — dash
    EVENT       // ○ open circle
}

@Serializable
sealed class RecurrenceRule {
    @Serializable data object Daily : RecurrenceRule()
    @Serializable data class Weekly(val daysBitmask: Int) : RecurrenceRule() // bit 0=Mon..6=Sun
    @Serializable data class Monthly(val dayOfMonth: Int) : RecurrenceRule()
    @Serializable data class EveryNDays(val n: Int) : RecurrenceRule()
}

enum class TaskColorLabel(val hex: String) {
    CRIMSON("#8B1A1A"),
    INDIGO("#1A3A6B"),
    FOREST("#1A5C2E"),
    AMBER("#8B6914"),
    PLUM("#4A1A5C"),
    TEAL("#1A5C5A"),
    SIENNA("#8B4513"),
    SLATE("#2D3B4A");

    companion object {
        fun fromIndex(index: Int?) = index?.let { values().getOrNull(it) }
    }
}
