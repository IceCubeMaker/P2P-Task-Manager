package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.ScoredTask
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.ui.components.next
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.data.repository.TimerRepository
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val scoredTasks: List<ScoredTask> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val groupIds: List<String> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Task> = emptyList(),
    val isSearching: Boolean = false,
    val selectedTaskIds: Set<String> = emptySet(),
    val isBulkMode: Boolean = false,
    val undoMessage: String? = null,
    val nowMs: Long = 0L,
    val peerId: String = ""
)

class HomeViewModel(
    private val taskRepo: TaskRepository,
    private val groupRepo: GroupRepository,
    private val timerRepo: TimerRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val timerState = timerRepo.timerState

    private var undoStack = ArrayDeque<() -> Unit>(20)
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val peerId = prefs.getPeerId()
            _uiState.value = _uiState.value.copy(peerId = peerId, nowMs = currentTimeMillis())
            loadGroups()
        }
        viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(nowMs = currentTimeMillis())
                delay(1000)
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepo.observeGroups().collectLatest { groups ->
                val groupIds = groups.map { it.id }
                _uiState.value = _uiState.value.copy(groupIds = groupIds, isLoading = false)
                if (groupIds.isNotEmpty()) loadTasks(groupIds)
            }
        }
    }

    private fun loadTasks(groupIds: List<String>) {
        viewModelScope.launch {
            val nowMs = currentTimeMillis()
            val scored = taskRepo.getScoredTasks(groupIds, nowMs)
            _uiState.value = _uiState.value.copy(scoredTasks = scored, isLoading = false)
        }
        viewModelScope.launch {
            taskRepo.observeCompletedTasks(groupIds).collectLatest { completed ->
                _uiState.value = _uiState.value.copy(completedTasks = completed)
            }
        }
    }

    fun refresh() {
        val groupIds = _uiState.value.groupIds
        if (groupIds.isNotEmpty()) loadTasks(groupIds)
    }

    fun onSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearching = query.isNotBlank())
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            val results = taskRepo.searchTasks(query)
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun toggleBujoState(taskId: String) {
        viewModelScope.launch {
            val peerId = _uiState.value.peerId
            val nowMs = currentTimeMillis()
            val task = taskRepo.getTask(taskId) ?: return@launch
            val nextState = task.bujoState.next()
            taskRepo.updateBujoState(taskId, nextState, peerId, nowMs)
            refresh()
            pushUndo { viewModelScope.launch { taskRepo.updateBujoState(taskId, task.bujoState, peerId, nowMs) } }
        }
    }

    fun toggleSelection(taskId: String) {
        val current = _uiState.value.selectedTaskIds
        val updated = if (taskId in current) current - taskId else current + taskId
        _uiState.value = _uiState.value.copy(
            selectedTaskIds = updated,
            isBulkMode = updated.isNotEmpty()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTaskIds = emptySet(), isBulkMode = false)
    }

    fun bulkComplete() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedTaskIds.toList()
            val peerId = _uiState.value.peerId
            taskRepo.bulkComplete(ids, peerId, currentTimeMillis())
            clearSelection()
            refresh()
        }
    }

    fun bulkDelete() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedTaskIds.toList()
            val peerId = _uiState.value.peerId
            taskRepo.bulkDelete(ids, peerId, currentTimeMillis())
            clearSelection()
            refresh()
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

    fun stopTimer(taskId: String) {
        viewModelScope.launch { timerRepo.stopTimer(taskId, currentTimeMillis()) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepo.getTask(taskId) ?: return@launch
            taskRepo.softDeleteTask(taskId, _uiState.value.peerId, currentTimeMillis())
            refresh()
            _uiState.value = _uiState.value.copy(undoMessage = "Task deleted")
            pushUndo {
                viewModelScope.launch {
                    taskRepo.updateTask(task.copy(isDeleted = false))
                    refresh()
                }
            }
        }
    }

    fun undo() {
        undoStack.removeLastOrNull()?.invoke()
        _uiState.value = _uiState.value.copy(undoMessage = null)
    }

    fun dismissUndo() {
        _uiState.value = _uiState.value.copy(undoMessage = null)
    }

    private fun pushUndo(action: () -> Unit) {
        if (undoStack.size >= 20) undoStack.removeFirst()
        undoStack.addLast(action)
    }
}

