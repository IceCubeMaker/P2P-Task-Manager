package com.p2ptaskmanager.domain

import kotlinx.datetime.*

/**
 * Natural language date parser. Handles common phrases like:
 * "today", "tomorrow", "next Monday", "in 2 weeks", "next week", "this weekend",
 * "in 3 days", "Friday at 3pm", "next month"
 */
class DateNLParser {

    data class ParsedDate(
        val instant: Long, // epoch millis
        val displayText: String
    )

    fun parse(input: String, nowMs: Long = currentTimeMillis()): ParsedDate? {
        val now = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val text = input.trim().lowercase()

        // Quick date expressions
        val result: LocalDate? = when {
            text.contains("today") -> today
            text.contains("tomorrow") -> today.plus(1, DateTimeUnit.DAY)
            text.contains("this weekend") || text.contains("weekend") ->
                today.nextWeekday(DayOfWeek.SATURDAY)
            text.contains("next week") -> today.plus(7, DateTimeUnit.DAY)
            text.contains("next month") -> today.plus(1, DateTimeUnit.MONTH)
            text.contains("in (\\d+) day".toRegex()) -> {
                val n = Regex("in (\\d+) day").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                today.plus(n, DateTimeUnit.DAY)
            }
            text.contains("in (\\d+) week".toRegex()) -> {
                val n = Regex("in (\\d+) week").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                today.plus(n * 7, DateTimeUnit.DAY)
            }
            text.contains("monday") || text.contains("mon") -> today.nextWeekday(DayOfWeek.MONDAY)
            text.contains("tuesday") || text.contains("tue") -> today.nextWeekday(DayOfWeek.TUESDAY)
            text.contains("wednesday") || text.contains("wed") -> today.nextWeekday(DayOfWeek.WEDNESDAY)
            text.contains("thursday") || text.contains("thu") -> today.nextWeekday(DayOfWeek.THURSDAY)
            text.contains("friday") || text.contains("fri") -> today.nextWeekday(DayOfWeek.FRIDAY)
            text.contains("saturday") || text.contains("sat") -> today.nextWeekday(DayOfWeek.SATURDAY)
            text.contains("sunday") || text.contains("sun") -> today.nextWeekday(DayOfWeek.SUNDAY)
            else -> null
        }

        if (result == null) return null

        // Extract time if present
        val hour = extractHour(text)
        val dateTime = LocalDateTime(result, LocalTime(hour ?: 9, 0))
        val epochMs = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        val display = when (result) {
            today -> if (hour != null) "Today at ${formatHour(hour)}" else "Today"
            today.plus(1, DateTimeUnit.DAY) -> if (hour != null) "Tomorrow at ${formatHour(hour)}" else "Tomorrow"
            else -> "${result.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${result.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${result.dayOfMonth}"
        }

        return ParsedDate(epochMs, display)
    }

    private fun extractHour(text: String): Int? {
        val match = Regex("(\\d{1,2})\\s*(am|pm|:00)?").find(text) ?: return null
        val h = match.groupValues[1].toIntOrNull() ?: return null
        val ampm = match.groupValues[2]
        return when {
            ampm == "pm" && h < 12 -> h + 12
            ampm == "am" && h == 12 -> 0
            else -> h
        }
    }

    private fun formatHour(hour: Int): String {
        val ampm = if (hour < 12) "AM" else "PM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        return "$h:00 $ampm"
    }

    private fun LocalDate.nextWeekday(target: DayOfWeek): LocalDate {
        var date = this.plus(1, DateTimeUnit.DAY)
        while (date.dayOfWeek != target) {
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return date
    }
}
