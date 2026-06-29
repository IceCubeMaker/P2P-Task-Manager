package com.p2ptaskmanager.domain

import com.p2ptaskmanager.data.model.HabitCompletion
import com.p2ptaskmanager.data.model.Task
import kotlin.math.log2
import kotlin.math.max

/**
 * Weighted Shortest Job First (WSJF) priority calculator.
 * Based on Don Reinertsen's "Principles of Product Development Flow".
 * Optimal under queueing theory: maximizes value delivered per unit time.
 *
 * WSJF = Cost of Delay / log₂(1 + estimatedHours)
 */
class PriorityCalculator(
    private val habitStrengthCalculator: HabitStrengthCalculator
) {

    data class Input(
        val task: Task,
        val myPeerId: String,
        val blockingCount: Int,
        val unmetDepsCount: Int,
        val directChildCount: Int,
        val completedChildCount: Int,
        val habitCompletions: List<HabitCompletion>,
        val contextScore: Float = 0f, // 0..1 from SmartContextEngine
        val nowMs: Long = currentTimeMillis()
    )

    fun calculate(input: Input): Float = with(input) {
        // Tasks blocked by unmet dependencies are deprioritized hard
        if (unmetDepsCount > 0) {
            return (task.userImportance * 0.1f).coerceIn(0f, 0.12f)
        }

        val cod = costOfDelay(input)
        val estimatedHours = estimatedHours(task)
        val jobSizeDivisor = log2(1.0 + estimatedHours).toFloat().coerceAtLeast(0.1f)

        return (cod / jobSizeDivisor).coerceIn(0f, 1f)
    }

    private fun costOfDelay(input: Input): Float = with(input) {
        // 1. Time Criticality (35%) — urgency from due date + planned duration
        val timeCriticality = task.dueDate?.let { due ->
            val estimatedMs = (task.estimatedMinutes ?: 30) * 60_000L
            val effectiveDue = due - estimatedMs // "effectively already late" if can't finish in time
            val msUntilDue = effectiveDue - nowMs
            val daysUntilDue = msUntilDue / 86_400_000.0
            when {
                daysUntilDue < 0 -> 1.0f
                daysUntilDue < 1 -> (1.0f - (daysUntilDue / 1.0).toFloat()).coerceIn(0f, 1f)
                daysUntilDue < 7 -> (1.0f - (daysUntilDue / 7.0).toFloat()).coerceIn(0f, 1f)
                daysUntilDue < 30 -> 0.1f
                else -> 0.05f
            }
        } ?: 0.1f

        // 2. User Importance (30%) — explicit 1-5 star rating
        val userImportance = task.userImportance.coerceIn(0f, 1f)

        // 3. Dependency Risk (20%) — how many tasks are blocked by this one
        val dependencyRisk = (blockingCount / 10f).coerceAtMost(1f)

        // 4. Habit Risk (15%) — low strength for repeating tasks = higher urgency
        val habitRisk = if (task.isRepeating) {
            val repeatDays = task.recurrenceRuleJson?.let { extractRepeatDays(it) } ?: 1
            val strength = habitStrengthCalculator.calculate(habitCompletions, repeatDays, task.createdAt, nowMs)
            1f - strength // low strength = high risk of losing the habit
        } else 0f

        val baseCod = timeCriticality * 0.35f +
                userImportance * 0.30f +
                dependencyRisk * 0.20f +
                habitRisk * 0.15f

        // Blend in smart context score (replaces some weighting when context available)
        return if (contextScore > 0f) {
            baseCod * 0.85f + contextScore * 0.15f
        } else {
            baseCod
        }
    }

    private fun estimatedHours(task: Task): Double {
        val minutes = task.estimatedMinutes ?: 30
        return minutes / 60.0
    }

    private fun extractRepeatDays(recurrenceJson: String): Int {
        // Simple extraction — EveryNDays uses the n field
        return when {
            recurrenceJson.contains("\"n\":") -> {
                val match = Regex("\"n\":(\\d+)").find(recurrenceJson)
                match?.groupValues?.get(1)?.toIntOrNull() ?: 1
            }
            recurrenceJson.contains("Daily") -> 1
            recurrenceJson.contains("Weekly") -> 7
            recurrenceJson.contains("Monthly") -> 30
            else -> 1
        }
    }
}
