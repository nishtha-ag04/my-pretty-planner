package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ENTITIES
// ==========================================

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val username: String,
    val passwordPlain: String,
    val isAdmin: Boolean = false,
    val profilePic: String? = null,
    val state: String? = null,
    val gmail: String? = null,
    val isThemeDark: Boolean = false,
    val customThemeName: String = "Classic",
    val hydrationGoal: Int = 8,
    val hydrationProgress: String = "[]", // Stores progress like "[1,3]"
    val hydrationStreak: Int = 0,
    val hydrationLastDate: String = "", // e.g. "2026-06-12"
    val generatedSchedule: String? = null
)

@Entity(tableName = "calculations")
data class Calculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class CustomizedTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0,
    val category: String = "General",
    val reminderDate: String? = null, // YYYY-MM-DD
    val reminderTime: String? = null, // HH:MM
    val isRecurring: Boolean = false  // Daily recurrence configuration
)

@Entity(tableName = "storehouse_items")
data class StorehouseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // Note, Image, Video, Audio, PDF, Document
    val content: String, // Note text or simulated data base64/url
    val folderName: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0
)

@Entity(tableName = "usage_events")
data class UsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String, // Dashboard, Calculator, Tasks, Storehouse, Admin
    val durationMs: Long,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_configs")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val primaryColorHex: String = "#FF6B8B",     // Soft cute pink
    val secondaryColorHex: String = "#FF8E53",   // Peach pink
    val backgroundColorHex: String = "#FFF5F7",  // Warm creamy pink back
    val selectedFontName: String = "Playful",    // Playful, Sans, Serif, Mono
    val categoriesString: String = "Work,Personal,Study,Creative,Ideas",
    val welcomeMessage: String = "Welcome to Storehouse Studio! Have an amazing day! 🎉🌸",
    val channelLink: String = "https://t.me/storehouse_updates_demo",
    val announcementTitle: String = "Storehouse System Active! 💕",
    val announcementBody: String = "Welcome! Create folders, organize documents, track your metrics, and customize your theme inside this beautiful personal dashboard.",
    val visibleSectionsCsv: String = "Calculator,Tasks,Storehouse,Progress,Announcements,Support",
    val supportChatsJson: String = "[]", // JSON representation of mock admin support chats
    val contactEmail: String = "support@myprettyplanner.com",
    val telegramGroupLink: String = "https://t.me/my_pretty_planner_community",
    val supportHours: String = "Monday-Friday (10 AM to 6 PM)",
    val socialTwitterLink: String = "https://twitter.com/myprettyplanner",
    val websiteLink: String = "https://myprettyplanner.com",
    val broadcastMessage: String = "An administrative connection has broadcasted the database synchronization checks successfully. You can chat one-on-one with developer team below! 💕",
    
    // Admin Dynamic Authentication and Sound parameters
    val adminEmail: String = "mepro1267@gmail.com",
    val adminKey: String = "proop67",
    val welcomeMusicEnabled: Boolean = true,
    val welcomeMusicName: String = "Luxury Event Entrance (Preset)",
    val welcomeMusicUri: String = "preset_luxury",
    val badgeSoundName: String = "Cute Chime (Preset)",
    val badgeSoundUri: String = "preset_badge",
    val celebrationSoundName: String = "Success Fanfare (Preset)",
    val celebrationSoundUri: String = "preset_celebration",
    val notificationSoundName: String = "Ping Alert (Preset)",
    val notificationSoundUri: String = "preset_notification",
    val recoveryEmail: String = "",
    val recoveryCode: String = "",
    val isRecoverySetupCompleted: Boolean = false,
    val trustedDevicesJson: String = "[]",
    val badgeName: String = "Amazing Blossom 🌸",
    val badgeLogo: String = "💮",
    val badgeAppearance: String = "Pink Glow",
    val badgeImage: String? = null,
    val hydrationLogo: String = "🧊",
    val hydrationLogoImage: String? = null,
    val popupLogo: String = "🔔",
    val popupLogoImage: String? = null
)

@Entity(tableName = "connector_records")
data class ConnectorRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // Logged In, Updated Profile, etc.
    val state: String = "",
    val gmail: String = "",
    val lastActivity: String = ""
)

@Entity(tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val repeatAlarm: Boolean = false,
    val snoozeMinutes: Int = 5,
    val selectedTone: String = "Cute Tones", // Cute Tones, Relaxing Tones, Dramatic Loud Sounds
    val label: String = "Cute Alarm 🕰️",
    val isEnabled: Boolean = true
)

// ==========================================
// 2. DAOS
// ==========================================

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccount): Long

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountCount(): Int

    @Query("SELECT * FROM user_accounts ORDER BY id DESC")
    fun getAllAccountsFlow(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts")
    suspend fun getAllAccountsDirect(): List<UserAccount>
}

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<Calculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calc: Calculation)

    @Query("DELETE FROM calculations")
    suspend fun clearHistory()
}

@Dao
interface CustomizedTaskDao {
    @Query("SELECT * FROM tasks ORDER BY displayOrder ASC, id ASC")
    fun getAllTasks(): Flow<List<CustomizedTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CustomizedTask): Long

    @Delete
    suspend fun deleteTask(task: CustomizedTask)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Dao
interface StorehouseItemDao {
    @Query("SELECT * FROM storehouse_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<StorehouseItem>>

    @Query("SELECT * FROM storehouse_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchItems(query: String): Flow<List<StorehouseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StorehouseItem)

    @Delete
    suspend fun deleteItem(item: StorehouseItem)
}

@Dao
interface UsageEventDao {
    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<UsageEvent>>

    @Query("SELECT * FROM usage_events")
    suspend fun getAllEvents(): List<UsageEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UsageEvent)
}

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_configs WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AdminConfig?>

    @Query("SELECT * FROM admin_configs WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): AdminConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AdminConfig)
}

@Dao
interface ConnectorRecordDao {
    @Query("SELECT * FROM connector_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<ConnectorRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ConnectorRecord)
}

@Dao
interface AlarmItemDao {
    @Query("SELECT * FROM alarms ORDER BY id DESC")
    fun getAllAlarmsFlow(): Flow<List<AlarmItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmItem): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmItem)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmItem)
}

@Entity(tableName = "announcement_items")
data class AnnouncementItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AnnouncementItemDao {
    @Query("SELECT * FROM announcement_items ORDER BY timestamp DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(item: AnnouncementItem): Long

    @Query("DELETE FROM announcement_items")
    suspend fun clearAll()
}

// ==========================================
// 3. DATABASE
// ==========================================

@Database(
    entities = [
        UserAccount::class,
        Calculation::class,
        CustomizedTask::class,
        StorehouseItem::class,
        UsageEvent::class,
        AdminConfig::class,
        ConnectorRecord::class,
        AlarmItem::class,
        AnnouncementItem::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun calculationDao(): CalculationDao
    abstract fun customizedTaskDao(): CustomizedTaskDao
    abstract fun storehouseItemDao(): StorehouseItemDao
    abstract fun usageEventDao(): UsageEventDao
    abstract fun adminConfigDao(): AdminConfigDao
    abstract fun connectorRecordDao(): ConnectorRecordDao
    abstract fun alarmItemDao(): AlarmItemDao
    abstract fun announcementItemDao(): AnnouncementItemDao
}
