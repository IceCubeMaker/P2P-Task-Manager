package com.p2ptaskmanager.domain

data class EstimationResult(
    val medianMinutes: Int,
    val confidence: Float,  // 0..1
    val sampleCount: Int
)

class TimeEstimator {

    private val stopWords = setOf("a", "an", "the", "and", "or", "to", "for", "of", "in", "on", "at", "my", "is", "it")

    /**
     * Given title tokens and tags for a new task, find similar completed tasks
     * and return the median actual duration.
     *
     * @param titleTokens Tokenized title of the new task
     * @param tags Tags of the new task
     * @param similarTaskDurations Map of taskId -> totalMs from DB query
     * @param tagsByTask Map of taskId -> List<String> for similarity scoring
     */
    fun estimate(
        title: String,
        tags: List<String>,
        similarTaskDurations: Map<String, Long>,
        tagsByTask: Map<String, List<String>>
    ): EstimationResult? {
        if (similarTaskDurations.isEmpty()) return null

        val newTokens = tokenize(title)
        val newTagSet = tags.map { it.lowercase() }.toSet()

        data class Candidate(val taskId: String, val similarity: Float, val durationMs: Long)

        val candidates = similarTaskDurations.entries.mapNotNull { (taskId, durationMs) ->
            if (durationMs <= 0) return@mapNotNull null
            val taskTags = (tagsByTask[taskId] ?: emptyList()).map { it.lowercase() }.toSet()
            val tagJaccard = if (newTagSet.isEmpty() && taskTags.isEmpty()) 0f
                else jaccardSimilarity(newTagSet, taskTags)
            val similarity = tagJaccard * 0.6f
            if (similarity > 0.2f) Candidate(taskId, similarity, durationMs) else null
        }.sortedByDescending { it.similarity }

        if (candidates.isEmpty()) return null

        val durations = candidates.map { it.durationMs / 60_000.0 }.sorted()
        val median = if (durations.size % 2 == 0) {
            ((durations[durations.size / 2 - 1] + durations[durations.size / 2]) / 2).toInt()
        } else {
            durations[durations.size / 2].toInt()
        }

        val confidence = (candidates.size / 5f).coerceAtMost(1f)

        return EstimationResult(
            medianMinutes = median.coerceAtLeast(1),
            confidence = confidence,
            sampleCount = candidates.size
        )
    }

    fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split(Regex("[\\s\\p{Punct}]+"))
            .filter { it.length > 2 && it !in stopWords }
            .toSet()

    private fun jaccardSimilarity(a: Set<String>, b: Set<String>): Float {
        if (a.isEmpty() && b.isEmpty()) return 1f
        val intersection = a.intersect(b).size.toFloat()
        val union = (a + b).size.toFloat()
        return if (union == 0f) 0f else intersection / union
    }
}
