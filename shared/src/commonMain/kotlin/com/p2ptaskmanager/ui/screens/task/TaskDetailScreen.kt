package com.p2ptaskmanager.ui.screens.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.data.model.TimerState
import com.p2ptaskmanager.ui.components.BulletSymbol
import com.p2ptaskmanager.ui.components.ImportanceStars
import com.p2ptaskmanager.ui.components.JournalDivider
import com.p2ptaskmanager.ui.components.TimerCircle
import com.p2ptaskmanager.ui.components.formatDateHeader
import com.p2ptaskmanager.ui.viewmodel.TaskDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCreateSubtask: () -> Unit,
    vm: TaskDetailViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(taskId) { vm.load(taskId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) } }
            )
        }
    ) { padding ->
        val task = state.task
        if (task == null || state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BulletSymbol(state = task.bujoState, onTap = {})
                        Spacer(Modifier.width(12.dp))
                        Text(task.title, style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                    }
                    if (task.description.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(task.description, style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Due: ${formatDateHeader(task.dueDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    ImportanceStars(importance = task.userImportance)
                    if (state.tags.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row { state.tags.forEach { tag ->
                            Text("#$tag ", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }}
                    }
                }
                JournalDivider()
            }

            // Timer section
            item {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Timer", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    TimerCircle(
                        elapsedMs = state.elapsedMs,
                        estimatedMinutes = task.estimatedMinutes,
                        size = 120.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (state.timerState) {
                            TimerState.Idle -> {
                                Button(onClick = { vm.startTimer(taskId) }) { Text("Start") }
                            }
                            is TimerState.Running -> {
                                OutlinedButton(onClick = { vm.pauseTimer(taskId) }) { Text("Pause") }
                                Button(onClick = { vm.stopTimer(taskId) }) { Text("Stop") }
                            }
                            is TimerState.Paused -> {
                                Button(onClick = { vm.resumeTimer(taskId) }) { Text("Resume") }
                                OutlinedButton(onClick = { vm.stopTimer(taskId) }) { Text("Stop") }
                            }
                        }
                    }
                    if (state.totalMinutes > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text("Total: %.1f min".format(state.totalMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                JournalDivider()
            }

            // Subtasks
            if (state.subtasks.isNotEmpty()) {
                item {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Subtasks", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onCreateSubtask) { Icon(Icons.Default.Add, null) }
                    }
                }
                items(state.subtasks) { sub ->
                    Row(Modifier.padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("•", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(sub.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                item {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Subtasks", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onCreateSubtask) { Icon(Icons.Default.Add, null) }
                    }
                }
            }

            // Dependencies
            if (state.dependencies.isNotEmpty()) {
                item {
                    Column(Modifier.padding(16.dp)) {
                        Text("Depends on", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        state.dependencies.forEach { dep ->
                            Text("• ${dep.title}", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
