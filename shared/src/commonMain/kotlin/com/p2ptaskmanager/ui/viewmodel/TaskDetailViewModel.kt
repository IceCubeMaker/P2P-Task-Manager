package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.model.TimerState
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.data.repository.TimerRepository
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: Task? = null,
    val subtasks: List<Task> = emptyList(),
    val dependencies: List<Task> = emptyList(),
    val tags: List<String> = emptyList(),
    val habitStrength: Float = 0f,
    val currentStreak: Int = 0,
    val totalMinutes: Double = 0.0,
    val timerState: TimerState = TimerState.Idle,
    val elapsedMs: Long = 0L,
    val nowMs: Long = 0L,
    val peerId: String = "",
    val isLoading: Boolean = true
)

class TaskDetailViewModel(
    private val taskRepo: TaskRepository,
    private val timerRepo: TimerRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    fun load(taskId: String) {
        viewModelScope.launch {
            val peerId = prefs.getPeerId()
            val nowMs = currentTimeMillis()
            val task = taskRepo.getTask(taskId)
            val subtasks = taskRepo.getSubtaskTree(taskId)
            val deps = taskRepo.getDependencies(taskId)
            val tags = taskRepo.getTagsForTask(taskId)
            val totalMin = timerRepo.getTotalMinutes(taskId)
            _uiState.value = TaskDetailUiState(
                task = task,
                subtasks = subtasks,
                dependencies = deps,
                tags = tags,
                totalMinutes = totalMin,
                timerState = timerRepo.timerState.value,
                nowMs = nowMs,
                peerId = peerId,
                isLoading = false
            )
        }
        viewModelScope.launch {
            timerRepo.timerState.collect { ts ->
                val nowMs = currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    timerState = ts,
                    elapsedMs = timerRepo.currentElapsedMs(nowMs),
                    nowMs = nowMs
                )
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val nowMs = currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    elapsedMs = timerRepo.currentElapsedMs(nowMs),
                    nowMs = nowMs
                )
            }
        }
    }

    fun startTimer(taskId: String) {
        viewModelScope.launch {
            timerRepo.startTimer(taskId, _uiState.value.peerId, currentTimeMillis())
        }
    }

    fun pauseTimer(taskId: String) {
        viewModelScope.launch { timerRepo.pauseTimer(taskId, currentTimeMillis()) }
    }

    fun resumeTimer(taskId: String) {
        viewModelScope.launch {
            timerRepo.resumeTimer(taskId, _uiState.value.peerId, currentTimeMillis())
        }
    }

    fun stopTimer(taskId: String) {
        viewModelScope.launch {
            timerRepo.stopTimer(taskId, currentTimeMillis())
            val totalMin = timerRepo.getTotalMinutes(taskId)
            _uiState.value = _uiState.value.copy(totalMinutes = totalMin)
        }
    }

    fun addSubtask(parentId: String, childId: String) {
        viewModelScope.launch {
            taskRepo.addSubtask(childId, parentId)
            val subtasks = taskRepo.getSubtaskTree(parentId)
            _uiState.value = _uiState.value.copy(subtasks = subtasks)
        }
    }

    fun addTag(taskId: String, tag: String) {
        viewModelScope.launch {
            taskRepo.addTag(taskId, tag)
            val tags = taskRepo.getTagsForTask(taskId)
            _uiState.value = _uiState.value.copy(tags = tags)
        }
    }
}
