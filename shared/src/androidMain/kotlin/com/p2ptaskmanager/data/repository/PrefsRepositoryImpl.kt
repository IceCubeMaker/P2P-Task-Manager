package com.p2ptaskmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "p2ptaskmanager_prefs")

class PrefsRepositoryImpl(private val context: Context) : PrefsRepository {

    private val KEY_NAME = stringPreferencesKey("display_name")
    private val KEY_PEER_ID = stringPreferencesKey("peer_id")
    private val KEY_BROKER = stringPreferencesKey("mqtt_broker")
    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
    private val KEY_STREAK_DATE = stringPreferencesKey("last_streak_date")
    private val KEY_STREAK = intPreferencesKey("current_streak")

    override suspend fun getDisplayName(): String =
        context.dataStore.data.first()[KEY_NAME] ?: ""

    override suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[KEY_NAME] = name }
    }

    override suspend fun getPeerId(): String {
        val stored = context.dataStore.data.first()[KEY_PEER_ID]
        if (stored != null) return stored
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { it[KEY_PEER_ID] = newId }
        return newId
    }

    override suspend fun getMqttBroker(): String =
        context.dataStore.data.first()[KEY_BROKER] ?: "tcp://broker.hivemq.com:1883"

    override suspend fun setMqttBroker(broker: String) {
        context.dataStore.edit { it[KEY_BROKER] = broker }
    }

    override suspend fun getThemeMode(): String =
        context.dataStore.data.first()[KEY_THEME] ?: "system"

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    override suspend fun isOnboardingComplete(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING] ?: false

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = complete }
    }

    override suspend fun getLastStreakDate(): String? =
        context.dataStore.data.first()[KEY_STREAK_DATE]

    override suspend fun setLastStreakDate(date: String) {
        context.dataStore.edit { it[KEY_STREAK_DATE] = date }
    }

    override suspend fun getCurrentStreak(): Int =
        context.dataStore.data.first()[KEY_STREAK] ?: 0

    override suspend fun setCurrentStreak(streak: Int) {
        context.dataStore.edit { it[KEY_STREAK] = streak }
    }

    override fun observeDisplayName(): Flow<String> =
        context.dataStore.data.map { it[KEY_NAME] ?: "" }

    override fun observeThemeMode(): Flow<String> =
        context.dataStore.data.map { it[KEY_THEME] ?: "system" }
}
