package com.p2ptaskmanager.data.db

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.p2ptaskmanager.db.AppDatabase
import kotlinx.coroutines.runBlocking

actual class DatabaseDriverFactory(private val context: Context) {
    actual suspend fun createDriver(): SqlDriver {
        val asyncSchema = AppDatabase.Schema
        val syncSchema = object : SqlSchema<QueryResult.Value<Unit>> {
            override val version: Long = asyncSchema.version
            override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
                runBlocking { asyncSchema.create(driver).await() }
                return QueryResult.Unit
            }
            override fun migrate(
                driver: SqlDriver,
                oldVersion: Long,
                newVersion: Long,
                vararg callbacks: app.cash.sqldelight.db.AfterVersion
            ): QueryResult.Value<Unit> {
                runBlocking { asyncSchema.migrate(driver, oldVersion, newVersion, *callbacks).await() }
                return QueryResult.Unit
            }
        }
        return AndroidSqliteDriver(syncSchema, context, "p2ptaskmanager.db")
    }
}
