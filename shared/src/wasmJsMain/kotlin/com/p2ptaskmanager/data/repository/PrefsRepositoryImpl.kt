package com.p2ptaskmanager.data.repository

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrefsRepositoryImpl : PrefsRepository {

    private val _nameFlow = MutableStateFlow("")
    private val _themeFlow = MutableStateFlow("system")

    private fun get(key: String, default: String = "") = localStorage.getItem(key) ?: default
    private fun set(key: String, value: String) = localStorage.setItem(key, value)

    override suspend fun getDisplayName(): String = get("display_name")
    override suspend fun setDisplayName(name: String) { set("display_name", name); _nameFlow.value = name }
    override suspend fun getPeerId(): String {
        val stored = get("peer_id")
        if (stored.isNotEmpty()) return stored
        val id = buildString(16) { repeat(16) { append(('a'..'f').random()) } }
        set("peer_id", id)
        return id
    }
    override suspend fun getMqttBroker(): String = get("mqtt_broker", "wss://broker.hivemq.com:8884/mqtt")
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
