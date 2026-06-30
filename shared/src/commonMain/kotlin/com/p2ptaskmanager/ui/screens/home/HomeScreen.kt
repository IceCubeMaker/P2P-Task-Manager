package com.p2ptaskmanager.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.data.model.BujoState
import com.p2ptaskmanager.data.model.ScoredTask
import com.p2ptaskmanager.data.model.Task
import com.p2ptaskmanager.data.model.TimerState
import com.p2ptaskmanager.ui.components.BottomNavBar
import com.p2ptaskmanager.ui.components.BulletSymbol
import com.p2ptaskmanager.ui.components.BujoProgressDots
import com.p2ptaskmanager.ui.components.ColorLabelStripe
import com.p2ptaskmanager.ui.components.DateSpreadHeader
import com.p2ptaskmanager.ui.components.ImportanceStars
import com.p2ptaskmanager.ui.components.JournalDivider
import com.p2ptaskmanager.ui.components.NavDestination
import com.p2ptaskmanager.ui.components.SyncStatusDot
import com.p2ptaskmanager.ui.components.UndoEffect
import com.p2ptaskmanager.ui.components.formatDateHeader
import com.p2ptaskmanager.ui.viewmodel.HomeUiState
import com.p2ptaskmanager.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onTaskClick: (String) -> Unit,
    onCreateTask: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    vm: HomeViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()
    val timerState by vm.timerState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchActive by remember { mutableStateOf(false) }

    UndoEffect(
        message = state.undoMessage,
        snackbarHostState = snackbarHost,
        onUndo = vm::undo,
        onDismiss = vm::dismissUndo
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks", fontWeight = FontWeight.Bold) },
                actions = {
                    SyncStatusDot(isSynced = true, hasPending = false, isOffline = false,
                        modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavBar(current = NavDestination.HOME, onNavigate = onNavigate)
        },
        floatingActionButton = {
            if (!state.isBulkMode) {
                FloatingActionButton(onClick = onCreateTask) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            DockedSearchBar(
                query = state.searchQuery,
                onQueryChange = vm::onSearch,
                onSearch = vm::onSearch,
                active = searchActive,
                onActiveChange = { searchActive = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search tasks...") }
            ) {
                if (state.searchResults.isNotEmpty()) {
                    LazyColumn {
                        items(state.searchResults) { task ->
                            SimpleTaskRow(task = task, onClick = { onTaskClick(task.id); searchActive = false })
                        }
                    }
                } else if (state.searchQuery.isNotBlank()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No results", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Active", Modifier.padding(12.dp)) }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Completed", Modifier.padding(12.dp)) }
            }

            if (selectedTab == 0) {
                ActiveTasksTab(
                    state = state,
                    timerState = timerState,
                    onTaskClick = onTaskClick,
                    onBulletTap = vm::toggleBujoState,
                    onLongPress = vm::toggleSelection,
                    onStartTimer = vm::startTimer,
                    onPauseTimer = vm::pauseTimer,
                    onStopTimer = vm::stopTimer
                )
            } else {
                CompletedTasksTab(state = state, onTaskClick = onTaskClick)
            }

            if (state.isBulkMode) {
                BulkActionBar(
                    count = state.selectedTaskIds.size,
                    onComplete = vm::bulkComplete,
                    onDelete = vm::bulkDelete,
                    onCancel = vm::clearSelection
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveTasksTab(
    state: HomeUiState,
    timerState: TimerState,
    onTaskClick: (String) -> Unit,
    onBulletTap: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onStartTimer: (String) -> Unit,
    onPauseTimer: (String) -> Unit,
    onStopTimer: (String) -> Unit
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    if (state.scoredTasks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◆", fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("No tasks yet", style = MaterialTheme.typography.titleMedium)
                Text("Tap + to add your first task", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val grouped = state.scoredTasks.groupBy { formatDateHeader(it.task.dueDate) }
    LazyColumn {
        grouped.forEach { (header, tasks) ->
            stickyHeader {
                DateSpreadHeader(label = header, modifier = Modifier.background(MaterialTheme.colorScheme.surface))
            }
            items(tasks, key = { it.task.id }) { scored ->
                val isSelected = scored.task.id in state.selectedTaskIds
                TaskRow(
                    scored = scored,
                    isSelected = isSelected,
                    timerState = timerState,
                    onTap = { onTaskClick(scored.task.id) },
                    onLongPress = { onLongPress(scored.task.id) },
                    onBulletTap = { onBulletTap(scored.task.id) },
                    onStartTimer = { onStartTimer(scored.task.id) },
                    onPauseTimer = { onPauseTimer(scored.task.id) }
                )
                JournalDivider()
            }
        }
    }
}

@Composable
private fun CompletedTasksTab(state: HomeUiState, onTaskClick: (String) -> Unit) {
    if (state.completedTasks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No completed tasks", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn {
        items(state.completedTasks, key = { it.id }) { task ->
            SimpleTaskRow(task = task, onClick = { onTaskClick(task.id) })
            JournalDivider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    scored: ScoredTask,
    isSelected: Boolean,
    timerState: TimerState,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onBulletTap: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit
) {
    val task = scored.task
    val isTimerRunning = timerState is TimerState.Running && (timerState as TimerState.Running).taskId == task.id

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorLabelStripe(colorLabel = task.colorLabel)
        Spacer(Modifier.width(8.dp))
        BulletSymbol(state = task.bujoState, onTap = onBulletTap,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                ImportanceStars(importance = task.userImportance)
                if (task.estimatedMinutes != null) {
                    Spacer(Modifier.width(8.dp))
                    Text("${task.estimatedMinutes}m", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isTimerRunning) {
                    Spacer(Modifier.width(8.dp))
                    Text("●", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                }
            }
        }
        if (task.estimatedMinutes != null) {
            IconButton(onClick = if (isTimerRunning) onPauseTimer else onStartTimer,
                modifier = Modifier.size(32.dp)) {
                Text(if (isTimerRunning) "⏸" else "▶", fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SimpleTaskRow(task: Task, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (task.isCompleted) "×" else "•",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BulkActionBar(count: Int, onComplete: () -> Unit, onDelete: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$count selected", fontWeight = FontWeight.SemiBold)
        Row {
            IconButton(onClick = onComplete) { Text("✓", fontSize = 20.sp) }
            IconButton(onClick = onDelete) { Text("×", fontSize = 20.sp) }
            IconButton(onClick = onCancel) { Text("✕", fontSize = 20.sp) }
        }
    }
}

// Needed for combinedClickable import
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickable(onClick: () -> Unit) = this.combinedClickable(onClick = onClick)
