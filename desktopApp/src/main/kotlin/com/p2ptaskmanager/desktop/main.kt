package com.p2ptaskmanager.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.p2ptaskmanager.data.db.DatabaseDriverFactory
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.PrefsRepositoryImpl
import com.p2ptaskmanager.data.sync.NoOpTransport
import com.p2ptaskmanager.data.sync.P2PTransport
import com.p2ptaskmanager.db.AppDatabase
import com.p2ptaskmanager.di.sharedModule
import com.p2ptaskmanager.ui.AppRoot
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    val db = runBlocking { AppDatabase(DatabaseDriverFactory().createDriver()) }

    val desktopModule = module {
        single<AppDatabase> { db }
        single<PrefsRepository> { PrefsRepositoryImpl() }
        single<P2PTransport> { NoOpTransport() }
    }

    startKoin {
        modules(desktopModule, sharedModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "P2P Task Manager"
        ) {
            AppRoot()
        }
    }
}
