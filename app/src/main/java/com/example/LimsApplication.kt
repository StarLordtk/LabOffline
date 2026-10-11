package com.example

import android.app.Application
import com.example.data.database.LimsDatabase
import com.example.data.repository.LimsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class LimsApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { LimsDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { LimsRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Database will initialize on first access
    }
}
