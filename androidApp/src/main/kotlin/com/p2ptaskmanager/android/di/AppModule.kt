package com.p2ptaskmanager.android.di

import com.p2ptaskmanager.data.db.DatabaseDriverFactory
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.PrefsRepositoryImpl
import com.p2ptaskmanager.data.sync.MqttAndroidTransport
import com.p2ptaskmanager.data.sync.NearbyTransport
import com.p2ptaskmanager.data.sync.P2PTransport
import com.p2ptaskmanager.db.AppDatabase
import com.p2ptaskmanager.di.sharedModule
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<AppDatabase> {
        runBlocking {
            AppDatabase(DatabaseDriverFactory(androidContext()).createDriver())
        }
    }
    single<PrefsRepository> { PrefsRepositoryImpl(androidContext()) }
    single<P2PTransport> {
        val prefs = get<PrefsRepository>()
        val peerId = runBlocking { prefs.getPeerId() }
        NearbyTransport(androidContext(), peerId)
    }
}

val allModules = listOf(androidModule, sharedModule)
