package com.p2ptaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2ptaskmanager.data.model.BujoState
import com.p2ptaskmanager.data.model.Group
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.domain.DateNLParser
import com.p2ptaskmanager.domain.TimeEstimator
import com.p2ptaskmanager.domain.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

data class CreateEditUiState(
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val dueDateText: String = "",
    val userImportance: Float = 0.5f,
    val estimatedMinutes: Int? = null,
    val estimationSuggestion: Int? = null,
    val estimationConfidence: Float = 0f,
    val tags: List<String> = emptyList(),
    val newTagInput: String = "",
    val colorLabel: Int? = null,
    val isRepeating: Boolean = false,
    val recurrenceRuleJson: String? = null,
    val reminderOffsetMinutes: Int? = null,
    val selectedGroupId: String = "",
    val assignedPeerId: String? = null,
    val groups: List<Group> = emptyList(),
    val isSaving: Boolean = false,
    val savedId: String? = null,
    val nlDateHint: String = ""
)

class CreateEditTaskViewModel(
    private val taskRepo: TaskRepository,
    private val groupRepo: GroupRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditUiState())
    val uiState: StateFlow<CreateEditUiState> = _uiState.asStateFlow()

    fun init(taskId: String? = null, defaultGroupId: String? = null) {
        viewModelScope.launch {
            val groups = groupRepo.getAllGroups()
            val defaultGroup = defaultGroupId ?: groups.firstOrNull()?.id ?: ""
            _uiState.value = _uiState.value.copy(groups = groups, selectedGroupId = defaultGroup)
            if (taskId != null) loadExisting(taskId)
        }
    }

    private suspend fun loadExisting(taskId: String) {
        val task = taskRepo.getTask(taskId) ?: return
        val tags = taskRepo.getTagsForTask(taskId)
        _uiState.value = _uiState.value.copy(
            taskId = taskId,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            userImportance = task.userImportance,
            estimatedMinutes = task.estimatedMinutes,
            tags = tags,
            colorLabel = task.colorLabel,
            isRepeating = task.isRepeating,
            recurrenceRuleJson = task.recurrenceRuleJson,
            reminderOffsetMinutes = task.reminderOffsetMinutes,
            selectedGroupId = task.groupId,
            assignedPeerId = task.assignedPeerId
        )
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        val nlParsed = DateNLParser.parse(title)
        if (nlParsed != null) {
            val ms = nlParsed.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            _uiState.value = _uiState.value.copy(
                nlDateHint = "Date detected: ${nlParsed.date} ${if (nlParsed.hour != 0) "${nlParsed.hour}:00" else ""}",
                dueDate = ms
            )
        }
        if (title.length > 3) updateEstimation(title)
    }

    private fun updateEstimation(title: String) {
        viewModelScope.launch {
            val tags = _uiState.value.tags
            val allTasks = taskRepo.getTasksByGroup(_uiState.value.selectedGroupId)
            val completedWithTime = allTasks.filter { it.isCompleted && it.estimatedMinutes != null }
            if (completedWithTime.isEmpty()) return@launch
            val durations = completedWithTime.associate { task ->
                task.id to (task.estimatedMinutes!! * 60_000L)
            }
            val tagsByTask = completedWithTime.associate { task ->
                task.id to taskRepo.getTagsForTask(task.id)
            }
            val result = TimeEstimator().estimate(title, tags, durations, tagsByTask)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    estimationSuggestion = result.medianMinutes,
                    estimationConfidence = result.confidence
                )
            }
        }
    }

    fun onDescriptionChanged(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun onDueDateChanged(ms: Long?) { _uiState.value = _uiState.value.copy(dueDate = ms, nlDateHint = "") }
    fun onImportanceChanged(v: Float) { _uiState.value = _uiState.value.copy(userImportance = v) }
    fun onEstimatedMinutesChanged(m: Int?) { _uiState.value = _uiState.value.copy(estimatedMinutes = m) }
    fun acceptEstimationSuggestion() {
        _uiState.value = _uiState.value.copy(estimatedMinutes = _uiState.value.estimationSuggestion)
    }
    fun onGroupChanged(groupId: String) { _uiState.value = _uiState.value.copy(selectedGroupId = groupId) }
    fun onColorLabelChanged(label: Int?) { _uiState.value = _uiState.value.copy(colorLabel = label) }
    fun onRepeatingChanged(v: Boolean) { _uiState.value = _uiState.value.copy(isRepeating = v) }
    fun onRecurrenceChanged(json: String?) { _uiState.value = _uiState.value.copy(recurrenceRuleJson = json) }
    fun onReminderOffsetChanged(min: Int?) { _uiState.value = _uiState.value.copy(reminderOffsetMinutes = min) }
    fun onNewTagChanged(tag: String) { _uiState.value = _uiState.value.copy(newTagInput = tag) }
    fun addTag() {
        val tag = _uiState.value.newTagInput.trim().removePrefix("#")
        if (tag.isNotEmpty() && tag !in _uiState.value.tags) {
            _uiState.value = _uiState.value.copy(
                tags = _uiState.value.tags + tag,
                newTagInput = ""
            )
        }
    }
    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(tags = _uiState.value.tags - tag)
    }

    fun save() {
        val s = _uiState.value
        if (s.title.isBlank() || s.selectedGroupId.isEmpty()) return
        viewModelScope.launch {
            val peerId = prefs.getPeerId()
            val nowMs = currentTimeMillis()
            val id = s.taskId ?: generateId()
            val task = Task(
                id = id,
                groupId = s.selectedGroupId,
                creatorPeerId = peerId,
                assignedPeerId = s.assignedPeerId,
                title = s.title.trim(),
                description = s.description.trim(),
                dueDate = s.dueDate,
                userImportance = s.userImportance,
                estimatedMinutes = s.estimatedMinutes,
                reminderOffsetMinutes = s.reminderOffsetMinutes,
                recurrenceRuleJson = s.recurrenceRuleJson,
                colorLabel = s.colorLabel,
                bujoState = BujoState.OPEN,
                isRepeating = s.isRepeating,
                createdAt = if (s.taskId == null) nowMs else 0L,
                updatedAt = nowMs
            )
            if (s.taskId == null) taskRepo.createTask(task)
            else taskRepo.updateTask(task.copy(createdAt = taskRepo.getTask(id)?.createdAt ?: nowMs))
            s.tags.forEach { tag -> taskRepo.addTag(id, tag) }
            _uiState.value = _uiState.value.copy(savedId = id, isSaving = false)
        }
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(16) { repeat(16) { append(chars.random()) } }
    }
}
