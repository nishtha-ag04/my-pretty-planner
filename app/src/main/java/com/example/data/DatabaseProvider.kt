package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "storehouse_studio_database.db"
            )
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }

    // Seeds initial data if tables are empty
    fun seedInitialData(context: Context) {
        val db = getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Seed Admin Config
            val configDao = db.adminConfigDao()
            val existingConfig = configDao.getConfigDirect()
            if (existingConfig == null) {
                configDao.insertOrUpdateConfig(AdminConfig())
            }

            // 2. Seed User Accounts
            val accountDao = db.userAccountDao()
            if (accountDao.getAccountCount() == 0) {
                // Insert regular seed user
                accountDao.insertAccount(
                    UserAccount(
                        email = "user@example.com",
                        username = "CuteUser",
                        passwordPlain = "user123",
                        isAdmin = false
                    )
                )
                // Insert admin seed user
                accountDao.insertAccount(
                    UserAccount(
                        email = "mepro1267@gmail.com",
                        username = "AdminPro",
                        passwordPlain = "proop67",
                        isAdmin = true
                    )
                )
            }

            // 3. Seed Default Tasks
            val taskDao = db.customizedTaskDao()
            val currentTasks = taskDao.getAllTasks().first()
            if (currentTasks.isEmpty()) {
                taskDao.insertTask(CustomizedTask(title = "🌸 Set up my beautiful profile", isCompleted = true, displayOrder = 1, category = "Personal"))
                taskDao.insertTask(CustomizedTask(title = "📁 Create my first file folder", isCompleted = false, displayOrder = 2, category = "Creative"))
                taskDao.insertTask(CustomizedTask(title = "📊 View my study session analytics", isCompleted = false, displayOrder = 3, category = "Study"))
                taskDao.insertTask(CustomizedTask(title = "💖 Add notes inside Storehouse", isCompleted = false, displayOrder = 4, category = "Ideas"))
            }
        }
    }
}
