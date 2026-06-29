package com.p2ptaskmanager.domain

import com.p2ptaskmanager.data.model.ScoredTask
import com.p2ptaskmanager.data.model.TodayPlanEntry

/**
 * Generates a daily plan from scored tasks, inserting breaks at scientifically
 * recommended intervals (Pomodoro-inspired: ~90 min work → 15-20 min break).
 *
 * Reference: Ultradian rhythm research (Peretz Lavie, Nathaniel Kleitman).
 * Also: Break timing from Anders Ericsson's deliberate practice research.
 */
class DailyPlanner {

    companion object {
        private const val WORK_BLOCK_MINUTES = 90
        private const val SHORT_BREAK_MINUTES = 15
        private const val LONG_BREAK_MINUTES = 30
        private const val LONG_BREAK_AFTER_BLOCKS = 3
        private const val MAX_DAILY_WORK_HOURS = 8
        private const val BREAK_TASK_ID_PREFIX = "break_"
    }

    data class DailyPlan(
        val entries: List<TodayPlanEntry>,
        val totalEstimatedMinutes: Int,
        val breakCount: Int
    )

    fun generatePlan(
        date: String,
        scoredTasks: List<ScoredTask>,
        existingPlan: List<TodayPlanEntry>? = null
    ): DailyPlan {
        val manuallyAdded = existingPlan?.filter { it.addedManually }?.map { it.taskId }?.toSet() ?: emptySet()

        // Take top tasks fitting within daily work budget
        val maxMinutes = MAX_DAILY_WORK_HOURS * 60
        var minutesAccumulated = 0
        val selectedTasks = mutableListOf<ScoredTask>()
        for (task in scoredTasks) {
            if (task.task.isCompleted) continue
            val estimated = task.task.estimatedMinutes ?: 25
            if (minutesAccumulated + estimated > maxMinutes) break
            selectedTasks.add(task)
            minutesAccumulated += estimated
        }

        // Also include manually added tasks (even if not in top scored)
        val selectedIds = selectedTasks.map { it.task.id }.toSet()
        val additionalManual = scoredTasks.filter { it.task.id in manuallyAdded && it.task.id !in selectedIds }
        val allSelected = (selectedTasks + additionalManual).sortedByDescending { it.score }

        // Insert breaks at 90-min work blocks
        val entries = mutableListOf<TodayPlanEntry>()
        var workMinutesSinceBreak = 0
        var workBlockCount = 0
        var breakCount = 0
        var sortOrder = 0

        for (scoredTask in allSelected) {
            val estimated = scoredTask.task.estimatedMinutes ?: 25
            workMinutesSinceBreak += estimated

            entries.add(
                TodayPlanEntry(
                    id = 0,
                    date = date,
                    taskId = scoredTask.task.id,
                    sortOrder = sortOrder++,
                    addedManually = scoredTask.task.id in manuallyAdded,
                    taskTitle = scoredTask.task.title,
                    estimatedMinutes = scoredTask.task.estimatedMinutes,
                    isCompleted = scoredTask.task.isCompleted,
                    userImportance = scoredTask.task.userImportance
                )
            )

            if (workMinutesSinceBreak >= WORK_BLOCK_MINUTES) {
                workBlockCount++
                breakCount++
                val isLongBreak = workBlockCount % LONG_BREAK_AFTER_BLOCKS == 0
                val breakDuration = if (isLongBreak) LONG_BREAK_MINUTES else SHORT_BREAK_MINUTES
                entries.add(
                    TodayPlanEntry(
                        id = 0,
                        date = date,
                        taskId = "${BREAK_TASK_ID_PREFIX}${breakCount}",
                        sortOrder = sortOrder++,
                        isBreak = true,
                        breakDurationMinutes = breakDuration,
                        taskTitle = if (isLongBreak) "Long break" else "Short break"
                    )
                )
                workMinutesSinceBreak = 0
            }
        }

        return DailyPlan(
            entries = entries,
            totalEstimatedMinutes = minutesAccumulated,
            breakCount = breakCount
        )
    }
}
