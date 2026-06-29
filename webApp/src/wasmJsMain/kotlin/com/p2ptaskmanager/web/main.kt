package com.p2ptaskmanager.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.p2ptaskmanager.data.db.DatabaseDriverFactory
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.data.repository.PrefsRepositoryImpl
import com.p2ptaskmanager.data.sync.NoOpTransport
import com.p2ptaskmanager.data.sync.P2PTransport
import com.p2ptaskmanager.db.AppDatabase
import com.p2ptaskmanager.di.sharedModule
import com.p2ptaskmanager.ui.AppRoot
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CoroutineScope(Dispatchers.Main).launch {
        val factory = DatabaseDriverFactory()
        val driver = factory.createDriver()
        val db = AppDatabase(driver)

        val webModule = module {
            single<AppDatabase> { db }
            single<PrefsRepository> { PrefsRepositoryImpl() }
            single<P2PTransport> { NoOpTransport() }
        }

        startKoin {
            modules(webModule, sharedModule)
        }

        val body = document.body ?: return@launch
        ComposeViewport(body) {
            AppRoot()
        }
    }
}
