package com.p2ptaskmanager.ui.screens.plan

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.data.model.TodayPlanEntry
import com.p2ptaskmanager.ui.components.BottomNavBar
import com.p2ptaskmanager.ui.components.JournalDivider
import com.p2ptaskmanager.ui.components.NavDestination
import com.p2ptaskmanager.ui.viewmodel.TodayPlanViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPlanScreen(
    onTaskClick: (String) -> Unit,
    onNavigate: (NavDestination) -> Unit,
    vm: TodayPlanViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today — ${state.dateLabel}") }
            )
        },
        bottomBar = { BottomNavBar(current = NavDestination.TODAY, onNavigate = onNavigate) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Building your plan...")
            }
            return@Scaffold
        }
        if (state.planEntries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("◆ Focus", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("No tasks planned for today",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Add tasks with due dates to see them here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("◆ Focus", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    val totalMin = state.planEntries.filter { !it.isBreak }
                        .sumOf { it.estimatedMinutes ?: 0 }
                    Text("~${totalMin}m planned", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                JournalDivider()
            }
            itemsIndexed(state.planEntries, key = { _, e -> e.taskId }) { index, entry ->
                PlanEntryRow(
                    entry = entry,
                    onTaskClick = { if (!entry.isBreak) onTaskClick(entry.taskId) },
                    onRemove = { vm.removeEntry(entry.taskId) }
                )
                JournalDivider()
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PlanEntryRow(
    entry: TodayPlanEntry,
    onTaskClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (entry.isBreak) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DragHandle, contentDescription = "drag",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = if (entry.isBreak) "☕ Break (${entry.breakDurationMinutes ?: entry.estimatedMinutes ?: 15}m)" else entry.taskTitle ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (entry.isBreak) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (entry.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (entry.isBreak) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
            )
            if (!entry.isBreak) {
                Text("${entry.estimatedMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!entry.isBreak) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
