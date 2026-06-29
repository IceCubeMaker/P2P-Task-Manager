package com.p2ptaskmanager.domain

import com.p2ptaskmanager.data.model.HabitCompletion
import kotlin.math.exp
import kotlin.math.max

class HabitStrengthCalculator {

    /**
     * Calculates habit strength based on Phillippa Lally's 2010 research.
     * Automaticity follows a sigmoid curve, plateauing around 66 days for daily habits.
     *
     * Returns 0..1 where 1 = fully formed habit.
     */
    fun calculate(
        completions: List<HabitCompletion>,
        repeatIntervalDays: Int,
        taskCreatedAt: Long,
        nowMs: Long = currentTimeMillis()
    ): Float {
        if (completions.isEmpty()) return 0f
        val intervalMs = repeatIntervalDays * 86_400_000L
        val ageInIntervals = (nowMs - taskCreatedAt).toDouble() / intervalMs

        // Maturity: 1 - e^(-ageInIntervals/10) — reaches ~0.63 at 10 intervals, ~0.99 at 46+
        val maturity = 1.0 - exp(-ageInIntervals / 10.0)

        // Completion rate over last 10 intervals
        val windowMs = intervalMs * 10
        val windowStart = nowMs - windowMs
        val recentCount = completions.count { it.completedAt > windowStart }
        val completionRate = (recentCount / 10.0).coerceAtMost(1.0)

        // Recency: how recently did the user last complete this?
        val lastCompletedAt = completions.maxOf { it.completedAt }
        val daysSinceLast = (nowMs - lastCompletedAt).toDouble() / 86_400_000.0
        val recencyScore = exp(-daysSinceLast / repeatIntervalDays.toDouble())

        return (completionRate * 0.5 + recencyScore * 0.3 + maturity * 0.2)
            .toFloat().coerceIn(0f, 1f)
    }

    fun calculateStreak(completions: List<HabitCompletion>, repeatIntervalDays: Int, nowMs: Long = currentTimeMillis()): Int {
        if (completions.isEmpty()) return 0
        val sorted = completions.sortedByDescending { it.completedAt }
        val intervalMs = repeatIntervalDays * 86_400_000L
        val toleranceMs = intervalMs * 1.5 // allow 50% grace period

        var streak = 0
        var expectedBefore = nowMs
        for (completion in sorted) {
            if (expectedBefore - completion.completedAt <= toleranceMs) {
                streak++
                expectedBefore = completion.completedAt
            } else {
                break
            }
        }
        return streak
    }
}

expect fun currentTimeMillis(): Long
