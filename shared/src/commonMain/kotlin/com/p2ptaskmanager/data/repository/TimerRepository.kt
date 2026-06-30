package com.p2ptaskmanager.data.repository

import com.p2ptaskmanager.data.model.TimerState
import com.p2ptaskmanager.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TimerRepository(private val db: AppDatabase) {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _activeTaskId = MutableStateFlow<String?>(null)
    val activeTimerTaskId: StateFlow<String?> = _activeTaskId.asStateFlow()

    suspend fun startTimer(taskId: String, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val existing = db.habitQueries.getActiveTimeEntry(taskId).executeAsOneOrNull()
        if (existing != null) {
            _timerState.value = TimerState.Running(taskId = taskId, startAt = existing.startAt, entryId = existing.id)
            _activeTaskId.value = taskId
            return@withContext
        }
        db.habitQueries.insertTimeEntry(
            taskId = taskId, startAt = nowMs, endAt = null, noteText = null, peerId = peerId
        )
        val entry = db.habitQueries.getActiveTimeEntry(taskId).executeAsOneOrNull()
        _timerState.value = TimerState.Running(taskId = taskId, startAt = nowMs, entryId = entry?.id ?: 0L)
        _activeTaskId.value = taskId
    }

    suspend fun pauseTimer(taskId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val state = _timerState.value as? TimerState.Running ?: return@withContext
        val elapsed = nowMs - state.startAt
        db.habitQueries.updateTimeEntryEnd(endAt = nowMs, id = state.entryId)
        _timerState.value = TimerState.Paused(taskId = taskId, accumulatedMs = elapsed)
        _activeTaskId.value = taskId
    }

    suspend fun resumeTimer(taskId: String, peerId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        val state = _timerState.value as? TimerState.Paused ?: return@withContext
        db.habitQueries.insertTimeEntry(
            taskId = taskId, startAt = nowMs, endAt = null, noteText = null, peerId = peerId
        )
        val entry = db.habitQueries.getActiveTimeEntry(taskId).executeAsOneOrNull()
        _timerState.value = TimerState.Running(taskId = taskId, startAt = nowMs, entryId = entry?.id ?: 0L)
        _activeTaskId.value = taskId
    }

    suspend fun stopTimer(taskId: String, nowMs: Long) = withContext(Dispatchers.IO) {
        when (val state = _timerState.value) {
            is TimerState.Running -> db.habitQueries.updateTimeEntryEnd(endAt = nowMs, id = state.entryId)
            else -> {}
        }
        _timerState.value = TimerState.Idle
        _activeTaskId.value = null
    }

    suspend fun getTotalMinutes(taskId: String): Double = withContext(Dispatchers.IO) {
        ((db.habitQueries.getTotalActualMinutes(taskId).executeAsOneOrNull() as? Long) ?: 0L) / 60000.0
    }

    fun isRunning(taskId: String): Boolean =
        (_timerState.value as? TimerState.Running)?.taskId == taskId

    fun isPaused(taskId: String): Boolean =
        (_timerState.value as? TimerState.Paused)?.taskId == taskId

    fun currentElapsedMs(nowMs: Long): Long = when (val state = _timerState.value) {
        is TimerState.Running -> nowMs - state.startAt
        is TimerState.Paused -> state.accumulatedMs
        TimerState.Idle -> 0L
    }
}
