package com.p2ptaskmanager.data.model

data class ScoredTask(
    val task: Task,
    val score: Float,
    val tags: List<String> = emptyList(),
    val directChildCount: Int = 0,
    val completedChildCount: Int = 0,
    val blockingCount: Int = 0,
    val unmetDepsCount: Int = 0,
    val habitStrength: Float = 0f,
    val currentStreak: Int = 0,
    val actualMinutes: Double = 0.0
) {
    val progressRatio: Float get() =
        if (directChildCount > 0) completedChildCount.toFloat() / directChildCount else 1f
}

data class TaskManifestEntry(val id: String, val vectorClock: String)
