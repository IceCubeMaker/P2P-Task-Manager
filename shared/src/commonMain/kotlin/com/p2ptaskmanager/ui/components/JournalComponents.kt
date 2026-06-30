package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.ui.theme.GoldAccent
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Journal-style section date header: "JUNE 29 — SUNDAY" with full underline */
@Composable
fun DateSpreadHeader(label: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Thin ink-style separator between task rows. */
@Composable
fun JournalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 40.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

/** Progress as a row of filled/empty dots (journal-style). */
@Composable
fun BujoProgressDots(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    maxDots: Int = 10
) {
    val displayTotal = minOf(total, maxDots)
    val displayCompleted = minOf(completed, displayTotal)
    if (total == 0) return
    androidx.compose.foundation.layout.Row(modifier = modifier) {
        repeat(displayCompleted) {
            Text("●", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 1.dp))
        }
        repeat(displayTotal - displayCompleted) {
            Text("○", fontSize = 8.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 1.dp))
        }
        if (total > maxDots) {
            Text("+${total - maxDots}", fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

/** Sync status indicator dot in app bar. */
@Composable
fun SyncStatusDot(
    isSynced: Boolean,
    hasPending: Boolean,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, label) = when {
        isOffline -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) to "Offline"
        hasPending -> androidx.compose.ui.graphics.Color(0xFFFF9800) to "Pending"
        isSynced -> androidx.compose.ui.graphics.Color(0xFF4CAF50) to "Synced"
        else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) to "Unknown"
    }
    Text("●", color = color, fontSize = 10.sp, modifier = modifier)
}

fun formatDateHeader(epochMs: Long?): String {
    if (epochMs == null) return "No date"
    val tz = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(tz).date
    val today = Clock.System.now().toLocalDateTime(tz).date
    return when {
        date == today -> "Today — ${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
        date == today.plusDays(1) -> "Tomorrow — ${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
        date < today -> "Overdue"
        else -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth} — ${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
    }
}

private fun LocalDate.plusDays(days: Int): LocalDate = this.plus(days, DateTimeUnit.DAY)
