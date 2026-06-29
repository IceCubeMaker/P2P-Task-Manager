package com.p2ptaskmanager.data.db

import com.p2ptaskmanager.db.AppDatabase

suspend fun createAppDatabase(factory: DatabaseDriverFactory): AppDatabase =
    AppDatabase(factory.createDriver())
