package com.p2ptaskmanager.domain

import com.p2ptaskmanager.data.model.HabitCompletion
import com.p2ptaskmanager.data.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Scores tasks based on contextual signals: time of day, weekday, weather, and tag affinity.
 * Learns from historical completion patterns to predict when a user tends to do what.
 */
class SmartContextEngine {

    data class WeatherContext(
        val weatherCode: Int,    // WMO weather code (0=clear, 51+=rain, 71+=snow)
        val temperatureCelsius: Double,
        val isDay: Boolean
    )

    // Tags that correlate with outdoor activity
    private val outdoorTags = setOf("exercise", "walk", "run", "jog", "outdoor", "garden", "bike", "sport")
    // Tags that correlate with focused work
    private val focusTags = setOf("work", "study", "code", "write", "read", "focus", "deep")
    // Tags that correlate with errands
    private val errandTags = setOf("shopping", "errand", "grocery", "buy", "store", "bank")

    /**
     * Returns a context score 0..1 for a task given current conditions.
     */
    fun score(
        task: Task,
        tags: List<String>,
        completionHistory: List<HabitCompletion>,
        weather: WeatherContext?,
        nowMs: Long = currentTimeMillis()
    ): Float {
        val localDt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val hourOfDay = localDt.hour
        val dayOfWeek = localDt.dayOfWeek

        var score = 0.5f // neutral baseline

        // Time-of-day affinity from historical completions
        val hourAffinity = computeHourAffinity(completionHistory, hourOfDay)
        score = score * 0.7f + hourAffinity * 0.3f

        // Weekday affinity
        val weekdayAffinity = computeWeekdayAffinity(completionHistory, dayOfWeek)
        score = score * 0.85f + weekdayAffinity * 0.15f

        // Weather modifiers (when weather data available)
        if (weather != null) {
            val weatherFactor = computeWeatherFactor(tags, weather)
            score = score * 0.85f + weatherFactor * 0.15f
        }

        // Workday vs weekend heuristic (if no history yet)
        if (completionHistory.size < 5) {
            val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            val tagSet = tags.map { it.lowercase() }.toSet()
            val isWorkTask = tagSet.intersect(focusTags).isNotEmpty()
            if (isWeekend && isWorkTask) score *= 0.7f
            if (!isWeekend && !isWorkTask) score *= 0.9f
        }

        // Energy level heuristic by time of day (cognitive tasks in morning/afternoon)
        val tagSet = tags.map { it.lowercase() }.toSet()
        val isFocusTask = tagSet.intersect(focusTags).isNotEmpty()
        val energyFactor = when {
            hourOfDay in 7..10 && isFocusTask -> 1.2f  // morning peak for focus tasks
            hourOfDay in 14..16 && isFocusTask -> 0.8f  // post-lunch dip
            hourOfDay in 18..21 && !isFocusTask -> 1.1f // evening for personal/errands
            hourOfDay < 7 || hourOfDay > 22 -> 0.6f     // late night / early morning penalty
            else -> 1.0f
        }
        score = (score * energyFactor).coerceIn(0f, 1f)

        return score
    }

    private fun computeHourAffinity(completions: List<HabitCompletion>, currentHour: Int): Float {
        if (completions.size < 3) return 0.5f
        val hourCounts = IntArray(24)
        completions.forEach { completion ->
            val hour = completion.completedAt.toLocalHour()
            hourCounts[hour]++
        }
        val total = completions.size.toFloat()
        val windowCount = ((-2)..(2)).sumOf { offset ->
            hourCounts[((currentHour + offset + 24) % 24)]
        }
        return (windowCount / total * 5f).coerceIn(0f, 1f)
    }

    private fun computeWeekdayAffinity(completions: List<HabitCompletion>, currentDay: DayOfWeek): Float {
        if (completions.size < 5) return 0.5f
        val dayCounts = IntArray(7)
        completions.forEach { completion ->
            val day = completion.completedAt.toDayOfWeekIndex()
            dayCounts[day]++
        }
        val total = completions.size.toFloat()
        val dayIndex = currentDay.ordinal
        return (dayCounts[dayIndex] / (total / 7f)).coerceIn(0f, 1f)
    }

    private fun computeWeatherFactor(tags: List<String>, weather: WeatherContext): Float {
        val tagSet = tags.map { it.lowercase() }.toSet()
        val isRaining = weather.weatherCode >= 51
        val isSnowing = weather.weatherCode >= 71
        val isClear = weather.weatherCode <= 3
        val isCold = weather.temperatureCelsius < 5
        val isHot = weather.temperatureCelsius > 32
        val isOutdoor = tagSet.intersect(outdoorTags).isNotEmpty()
        val isErrand = tagSet.intersect(errandTags).isNotEmpty()

        return when {
            isOutdoor && (isRaining || isSnowing) -> 0.2f  // bad weather for outdoor tasks
            isOutdoor && isClear && !isCold && !isHot -> 1.0f // perfect outdoor weather
            isErrand && isRaining -> 0.6f                   // slightly harder to run errands
            isOutdoor && isCold -> 0.5f                      // cold outdoor = less ideal
            else -> 0.7f
        }
    }
}

private fun Long.toLocalHour(): Int {
    return try {
        val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        dt.hour
    } catch (e: Exception) { 12 }
}

private fun Long.toDayOfWeekIndex(): Int {
    return try {
        val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        dt.dayOfWeek.ordinal
    } catch (e: Exception) { 0 }
}
