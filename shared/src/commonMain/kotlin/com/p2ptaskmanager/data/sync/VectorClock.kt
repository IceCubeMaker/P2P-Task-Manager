package com.p2ptaskmanager.data.sync

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VectorClock(private val clock: MutableMap<String, Long> = mutableMapOf()) {

    fun increment(peerId: String): VectorClock {
        clock[peerId] = (clock[peerId] ?: 0L) + 1L
        return this
    }

    fun compareTo(other: VectorClock): ClockRelation {
        val allKeys = (clock.keys + other.clock.keys).toSet()
        var selfDominates = false
        var otherDominates = false
        for (key in allKeys) {
            val a = clock[key] ?: 0L
            val b = other.clock[key] ?: 0L
            if (a > b) selfDominates = true
            if (b > a) otherDominates = true
        }
        return when {
            selfDominates && otherDominates -> ClockRelation.CONCURRENT
            selfDominates -> ClockRelation.AFTER
            otherDominates -> ClockRelation.BEFORE
            else -> ClockRelation.EQUAL
        }
    }

    fun merge(other: VectorClock): VectorClock {
        val merged = clock.toMutableMap()
        for ((k, v) in other.clock) {
            merged[k] = maxOf(merged[k] ?: 0L, v)
        }
        return VectorClock(merged)
    }

    fun toJson(): String {
        val obj = buildJsonObject { clock.forEach { (k, v) -> put(k, v) } }
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): VectorClock {
            return try {
                val obj = Json.parseToJsonElement(json).jsonObject
                val map = obj.entries.associate { (k, v) -> k to v.jsonPrimitive.long }.toMutableMap()
                VectorClock(map)
            } catch (e: Exception) {
                VectorClock()
            }
        }

        fun initial(peerId: String) = VectorClock(mutableMapOf(peerId to 1L))
    }
}

enum class ClockRelation { BEFORE, AFTER, CONCURRENT, EQUAL }
