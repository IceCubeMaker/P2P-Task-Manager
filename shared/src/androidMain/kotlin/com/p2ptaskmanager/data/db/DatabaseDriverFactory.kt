package com.p2ptaskmanager.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.p2ptaskmanager.db.AppDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual suspend fun createDriver(): SqlDriver =
        AndroidSqliteDriver(AppDatabase.Schema, context, "p2ptaskmanager.db")
}
