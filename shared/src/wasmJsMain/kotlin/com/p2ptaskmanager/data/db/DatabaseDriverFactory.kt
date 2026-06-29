package com.p2ptaskmanager.data.db

import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.p2ptaskmanager.db.AppDatabase
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        val driver = WebWorkerDriver(Worker(js("new URL('./sqljs-worker.js', import.meta.url) + ''")))
        AppDatabase.Schema.create(driver).await()
        return driver
    }
}
