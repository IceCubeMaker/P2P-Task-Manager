package com.p2ptaskmanager.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.p2ptaskmanager.data.model.BujoState
import com.p2ptaskmanager.data.model.HabitCompletion
import com.p2ptaskmanager.data.model.ScoredTask
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.sync.VectorClock
import com.p2ptaskmanager.db.AppDatabase
import com.p2ptaskmanager.domain.HabitStrengthCalculator
import com.p2ptaskmanager.domain.PriorityCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class TaskRepository(private val db: AppDatabase) {

    private val queries = db.taskQueries
    private val relQueries = db.relationsQueries

    fun observeActiveTasks(groupIds: List<String>): Flow<List<Task>> =
        queries.getActiveByGroups(groupIds)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toTask() } }

    fun observeCompletedTasks(groupIds: List<String>): Flow<List<Task>> =
        queries.getCompletedByGroups(groupIds)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toTask() } }

    suspend fun getTask(id: String): Task? = withContext(Dispatchers.IO) {
        queries.getById(id).executeAsOneOrNull()?.toTask()
    }

    suspend fun getTasksByGroup(groupId: String): List<Task> = withContext(Dispatchers.IO) {
        queries.getByGroup(groupId).executeAsList().map { it.toTask() }
    }

    suspend fun getTasksByIds(ids: List<String>): List<Task> = withContext(Dispatchers.IO) {
        queries.getByIds(ids).executeAsList().map { it.toTask() }
    }

    suspend fun createTask(task: Task) = withContext(Dispatchers.IO) {
        queries.upsertTask(task.toDbRow())
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        queries.upsertTask(task.toDbRow())
    }

    suspend fun completeTask(id: String, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val task = queries.getById(id).executeAsOneOrNull() ?: return@withContext
        val vc = VectorClock.fromJson(task.vectorClock).increment(peerId)
        queries.markCompleted(
            completedAt = nowMs,
            peerId = peerId,
            updatedAt = nowMs,
            vectorClock = vc.toJson(),
            id = id
        )
        db.habitQueries.insertHabitCompletion(
            taskId = id,
            completedAt = nowMs,
            peerId = peerId
        )
    }

    suspend fun updateBujoState(id: String, state: BujoState, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val task = queries.getById(id).executeAsOneOrNull() ?: return@withContext
        val vc = VectorClock.fromJson(task.vectorClock).increment(peerId)
        queries.updateBujoState(
            state = state.name,
            updatedAt = nowMs,
            vectorClock = vc.toJson(),
            id = id
        )
        if (state == BujoState.COMPLETED) {
            queries.markCompleted(
                completedAt = nowMs,
                peerId = peerId,
                updatedAt = nowMs,
                vectorClock = vc.toJson(),
                id = id
            )
            db.habitQueries.insertHabitCompletion(
                taskId = id,
                completedAt = nowMs,
                peerId = peerId
            )
        }
    }

    suspend fun softDeleteTask(id: String, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val task = queries.getById(id).executeAsOneOrNull() ?: return@withContext
        val vc = VectorClock.fromJson(task.vectorClock).increment(peerId)
        queries.softDelete(updatedAt = nowMs, vectorClock = vc.toJson(), id = id)
    }

    suspend fun updateSortOrder(id: String, sortOrder: Long) = withContext(Dispatchers.IO) {
        queries.updateSortOrder(sortOrder = sortOrder, id = id)
    }

    suspend fun searchTasks(query: String): List<Task> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        queries.searchTasks(query.trim()).executeAsList().map { it.toTask() }
    }

    suspend fun getManifest(groupId: String): Map<String, String> = withContext(Dispatchers.IO) {
        queries.getManifest(groupId).executeAsList()
            .associate { it.id to it.vectorClock }
    }

    suspend fun upsertFromSync(task: Task) = withContext(Dispatchers.IO) {
        queries.upsertTask(task.toDbRow())
    }

    suspend fun addSubtask(childId: String, parentId: String) = withContext(Dispatchers.IO) {
        // BFS to collect all ancestors of childId; cycle if parentId is among them
        val ancestors = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(childId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val parents = relQueries.getDirectParents(current).executeAsList()
            for (p in parents) { if (ancestors.add(p)) queue.add(p) }
        }
        if (parentId in ancestors) {
            throw IllegalArgumentException("Cycle detected: parentId $parentId is already an ancestor of $childId")
        }
        relQueries.insertParent(childId = childId, parentId = parentId)
    }

    suspend fun getSubtaskTree(rootId: String): List<Task> = withContext(Dispatchers.IO) {
        // BFS replaces WITH RECURSIVE (SQLDelight 2.0.x crashes on recursive CTEs)
        val result = mutableListOf<Task>()
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(rootId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (!visited.add(current)) continue
            val children = relQueries.getDirectChildren(current).executeAsList().map { it.toTask() }
            result.addAll(children)
            children.forEach { queue.add(it.id) }
        }
        result
    }

    suspend fun addDependency(taskId: String, dependsOnTaskId: String) = withContext(Dispatchers.IO) {
        relQueries.insertDependency(taskId = taskId, dependsOnTaskId = dependsOnTaskId)
    }

    suspend fun getDependencies(taskId: String): List<Task> = withContext(Dispatchers.IO) {
        val depIds = relQueries.getDependenciesOf(taskId).executeAsList()
        if (depIds.isEmpty()) return@withContext emptyList()
        queries.getByIds(depIds).executeAsList().map { it.toTask() }
    }

    suspend fun addTag(taskId: String, tag: String) = withContext(Dispatchers.IO) {
        relQueries.insertTag(taskId = taskId, tag = tag)
    }

    suspend fun getTagsForTask(taskId: String): List<String> = withContext(Dispatchers.IO) {
        relQueries.getTagsForTask(taskId).executeAsList()
    }

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val allGroups = db.groupQueries.getAllGroups().executeAsList()
        val groupIds = allGroups.map { it.id }
        val tasks = if (groupIds.isEmpty()) emptyList()
        else queries.getByGroups(groupIds).executeAsList().map { it.toTask() }
        Json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(Task.serializer()),
            tasks
        )
    }

    suspend fun bulkComplete(ids: List<String>, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        ids.forEach { completeTask(it, peerId, nowMs) }
    }

    suspend fun bulkDelete(ids: List<String>, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        ids.forEach { softDeleteTask(it, peerId, nowMs) }
    }

    suspend fun getScoredTasks(groupIds: List<String>, nowMs: Long): List<ScoredTask> = withContext(Dispatchers.IO) {
        val tasks = queries.getActiveByGroups(groupIds).executeAsList().map { it.toTask() }
        val calc = PriorityCalculator(HabitStrengthCalculator())
        tasks.map { task ->
            val blockingCount = queries.getBlockingCount(task.id).executeAsOne().toInt()
            val unmetDepsCount = relQueries.getDependenciesOf(task.id).executeAsList().size
            val directChildCount = relQueries.getDirectChildCount(task.id).executeAsOne().toInt()
            val completedChildCount = relQueries.getCompletedChildCount(task.id).executeAsOne().toInt()
            val tags = relQueries.getTagsForTask(task.id).executeAsList()
            val rawCompletions = db.habitQueries.getCompletionsForTask(task.id).executeAsList()
            val habitCompletions = rawCompletions.map {
                HabitCompletion(id = it.id, taskId = it.taskId, completedAt = it.completedAt, peerId = it.peerId)
            }
            val input = PriorityCalculator.Input(
                task = task,
                myPeerId = "",
                blockingCount = blockingCount,
                unmetDepsCount = unmetDepsCount,
                directChildCount = directChildCount,
                completedChildCount = completedChildCount,
                habitCompletions = habitCompletions,
                nowMs = nowMs
            )
            val score = calc.calculate(input)
            val habitStrength = if (task.isRepeating && habitCompletions.isNotEmpty()) {
                HabitStrengthCalculator().calculate(habitCompletions, 1, task.createdAt, nowMs)
            } else 0f
            ScoredTask(
                task = task,
                score = score,
                tags = tags,
                blockingCount = blockingCount,
                directChildCount = directChildCount,
                completedChildCount = completedChildCount,
                habitStrength = habitStrength,
                unmetDepsCount = unmetDepsCount
            )
        }.sortedByDescending { it.score }
    }
}

private fun com.p2ptaskmanager.db.Tasks.toTask() = Task(
    id = id,
    groupId = groupId,
    creatorPeerId = creatorPeerId,
    assignedPeerId = assignedPeerId,
    title = title,
    description = description,
    dueDate = dueDate,
    userImportance = userImportance.toFloat(),
    estimatedMinutes = estimatedMinutes?.toInt(),
    reminderOffsetMinutes = reminderOffsetMinutes?.toInt(),
    recurrenceRuleJson = recurrenceRuleJson,
    colorLabel = colorLabel?.toInt(),
    manualSortOrder = manualSortOrder,
    bujoState = runCatching { BujoState.valueOf(bujoState) }.getOrDefault(BujoState.OPEN),
    isCompleted = isCompleted != 0L,
    completedAt = completedAt,
    completedByPeerId = completedByPeerId,
    isRepeating = isRepeating != 0L,
    createdAt = createdAt,
    updatedAt = updatedAt,
    vectorClock = vectorClock,
    isDeleted = isDeleted != 0L
)


private fun Task.toDbRow() = com.p2ptaskmanager.db.Tasks(
    id = id,
    groupId = groupId,
    creatorPeerId = creatorPeerId,
    assignedPeerId = assignedPeerId,
    title = title,
    description = description,
    dueDate = dueDate,
    userImportance = userImportance.toDouble(),
    estimatedMinutes = estimatedMinutes?.toLong(),
    reminderOffsetMinutes = reminderOffsetMinutes?.toLong(),
    recurrenceRuleJson = recurrenceRuleJson,
    colorLabel = colorLabel?.toLong(),
    manualSortOrder = manualSortOrder,
    bujoState = bujoState.name,
    isCompleted = if (isCompleted) 1L else 0L,
    completedAt = completedAt,
    completedByPeerId = completedByPeerId,
    isRepeating = if (isRepeating) 1L else 0L,
    createdAt = createdAt,
    updatedAt = updatedAt,
    vectorClock = vectorClock,
    isDeleted = if (isDeleted) 1L else 0L
)
