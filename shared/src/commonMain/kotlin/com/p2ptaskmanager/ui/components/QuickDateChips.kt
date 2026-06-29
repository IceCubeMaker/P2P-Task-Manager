package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun QuickDateChips(
    selectedMs: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    val today = now.toLocalDateTime(tz).date
    val tomorrow = today.plus(DatePeriod(days = 1))
    val daysUntilSat = (DayOfWeek.SATURDAY.ordinal - today.dayOfWeek.ordinal + 7) % 7
    val thisWeekend = today.plus(DatePeriod(days = if (daysUntilSat == 0) 7 else daysUntilSat))
    val nextWeek = today.plus(DatePeriod(days = 7))

    fun dateToMs(date: kotlinx.datetime.LocalDate): Long {
        val ldt = kotlinx.datetime.LocalDateTime(date.year, date.month, date.dayOfMonth, 9, 0)
        return ldt.toInstant(tz).toEpochMilliseconds()
    }

    val todayMs = dateToMs(today)
    val tomorrowMs = dateToMs(tomorrow)
    val weekendMs = dateToMs(thisWeekend)
    val nextWeekMs = dateToMs(nextWeek)

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        FilterChip(
            selected = selectedMs == todayMs,
            onClick = { onSelected(if (selectedMs == todayMs) null else todayMs) },
            label = { Text("Today") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = selectedMs == tomorrowMs,
            onClick = { onSelected(if (selectedMs == tomorrowMs) null else tomorrowMs) },
            label = { Text("Tomorrow") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = selectedMs == weekendMs,
            onClick = { onSelected(if (selectedMs == weekendMs) null else weekendMs) },
            label = { Text("This Weekend") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = selectedMs == nextWeekMs,
            onClick = { onSelected(if (selectedMs == nextWeekMs) null else nextWeekMs) },
            label = { Text("Next Week") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = selectedMs == null,
            onClick = { onSelected(null) },
            label = { Text("No Date") }
        )
    }
}

private fun kotlinx.datetime.LocalDateTime.toInstant(tz: TimeZone): kotlinx.datetime.Instant =
    kotlinx.datetime.toInstant(tz)
