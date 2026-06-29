package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.ScoredTask
import com.p2ptaskmanager.data.model.TodayPlanEntry
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.domain.DailyPlanner
import com.p2ptaskmanager.domain.SmartContextEngine
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TodayPlanUiState(
    val planEntries: List<TodayPlanEntry> = emptyList(),
    val isLoading: Boolean = true,
    val dateLabel: String = "Today"
)

class TodayPlanViewModel(
    private val taskRepo: TaskRepository,
    private val groupRepo: GroupRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayPlanUiState())
    val uiState: StateFlow<TodayPlanUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            val groups = groupRepo.getAllGroups()
            val groupIds = groups.map { it.id }
            if (groupIds.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            val nowMs = currentTimeMillis()
            val scored = taskRepo.getScoredTasks(groupIds, nowMs)
            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(tz)
            val dateStr = "${today.year}-${today.monthNumber.toString().padStart(2, '0')}-${today.dayOfMonth.toString().padStart(2, '0')}"
            val plan = DailyPlanner().generatePlan(date = dateStr, scoredTasks = scored).entries
            _uiState.value = TodayPlanUiState(
                planEntries = plan,
                isLoading = false,
                dateLabel = "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.dayOfMonth}"
            )
        }
    }

    fun moveEntry(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.planEntries.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _uiState.value = _uiState.value.copy(planEntries = current)
    }

    fun removeEntry(taskId: String) {
        val current = _uiState.value.planEntries.filterNot { it.taskId == taskId }
        _uiState.value = _uiState.value.copy(planEntries = current)
    }

    fun addTask(scoredTask: ScoredTask) {
        val existing = _uiState.value.planEntries.map { it.taskId }
        if (scoredTask.task.id in existing) return
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz)
        val dateStr = "${today.year}-${today.monthNumber.toString().padStart(2, '0')}-${today.dayOfMonth.toString().padStart(2, '0')}"
        val entry = TodayPlanEntry(
            id = 0,
            date = dateStr,
            taskId = scoredTask.task.id,
            sortOrder = _uiState.value.planEntries.size,
            taskTitle = scoredTask.task.title,
            estimatedMinutes = scoredTask.task.estimatedMinutes ?: 30,
            isBreak = false,
            isCompleted = scoredTask.task.isCompleted,
            addedManually = true
        )
        _uiState.value = _uiState.value.copy(
            planEntries = _uiState.value.planEntries + entry
        )
    }
}
