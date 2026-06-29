package com.p2ptaskmanager.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.UUID

class PrefsRepositoryImpl(dataDir: String = System.getProperty("user.home") + "/.p2ptaskmanager") : PrefsRepository {

    private val prefsFile = File("$dataDir/prefs.json").also { it.parentFile?.mkdirs() }
    private val _nameFlow = MutableStateFlow("")
    private val _themeFlow = MutableStateFlow("system")

    private fun load(): MutableMap<String, String> {
        if (!prefsFile.exists()) return mutableMapOf()
        return runCatching {
            val obj = Json.decodeFromString(JsonObject.serializer(), prefsFile.readText())
            obj.entries.associate { (k, v) -> k to v.jsonPrimitive.contentOrNull.orEmpty() }.toMutableMap()
        }.getOrDefault(mutableMapOf())
    }

    private fun save(map: Map<String, String>) {
        val obj = JsonObject(map.mapValues { JsonPrimitive(it.value) })
        prefsFile.writeText(Json.encodeToString(JsonObject.serializer(), obj))
    }

    private fun get(key: String, default: String = ""): String = load()[key] ?: default
    private fun set(key: String, value: String) { val m = load(); m[key] = value; save(m) }

    override suspend fun getDisplayName(): String = get("display_name")
    override suspend fun setDisplayName(name: String) { set("display_name", name); _nameFlow.value = name }
    override suspend fun getPeerId(): String {
        val stored = get("peer_id")
        if (stored.isNotEmpty()) return stored
        val id = UUID.randomUUID().toString()
        set("peer_id", id)
        return id
    }
    override suspend fun getMqttBroker(): String = get("mqtt_broker", "tcp://broker.hivemq.com:1883")
    override suspend fun setMqttBroker(broker: String) = set("mqtt_broker", broker)
    override suspend fun getThemeMode(): String = get("theme_mode", "system")
    override suspend fun setThemeMode(mode: String) { set("theme_mode", mode); _themeFlow.value = mode }
    override suspend fun isOnboardingComplete(): Boolean = get("onboarding_complete") == "true"
    override suspend fun setOnboardingComplete(complete: Boolean) = set("onboarding_complete", complete.toString())
    override suspend fun getLastStreakDate(): String? = get("last_streak_date").takeIf { it.isNotEmpty() }
    override suspend fun setLastStreakDate(date: String) = set("last_streak_date", date)
    override suspend fun getCurrentStreak(): Int = get("current_streak").toIntOrNull() ?: 0
    override suspend fun setCurrentStreak(streak: Int) = set("current_streak", streak.toString())
    override fun observeDisplayName(): Flow<String> = _nameFlow.asStateFlow()
    override fun observeThemeMode(): Flow<String> = _themeFlow.asStateFlow()
}
