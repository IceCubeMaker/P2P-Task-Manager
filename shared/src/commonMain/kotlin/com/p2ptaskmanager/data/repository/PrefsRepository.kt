package com.p2ptaskmanager.data.repository

import kotlinx.coroutines.flow.Flow

interface PrefsRepository {
    suspend fun getDisplayName(): String
    suspend fun setDisplayName(name: String)
    suspend fun getPeerId(): String
    suspend fun getMqttBroker(): String
    suspend fun setMqttBroker(broker: String)
    suspend fun getThemeMode(): String
    suspend fun setThemeMode(mode: String)
    suspend fun isOnboardingComplete(): Boolean
    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun getLastStreakDate(): String?
    suspend fun setLastStreakDate(date: String)
    suspend fun getCurrentStreak(): Int
    suspend fun setCurrentStreak(streak: Int)
    fun observeDisplayName(): Flow<String>
    fun observeThemeMode(): Flow<String>
}
