package com.p2ptaskmanager.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.p2ptaskmanager.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class DatabaseDriverFactory(private val dataDir: String = System.getProperty("user.home") + "/.p2ptaskmanager") {
    actual suspend fun createDriver(): SqlDriver = withContext(Dispatchers.IO) {
        File(dataDir).mkdirs()
        val dbFile = "$dataDir/p2ptaskmanager.db"
        val isNew = !File(dbFile).exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbFile")
        if (isNew) {
            AppDatabase.Schema.create(driver).await()
        }
        driver
    }
}
