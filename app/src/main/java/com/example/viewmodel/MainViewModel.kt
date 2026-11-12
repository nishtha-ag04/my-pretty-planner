package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.AlarmReceiver
import com.example.AlarmTonePlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val accountDao = db.userAccountDao()
    private val calculationDao = db.calculationDao()
    private val taskDao = db.customizedTaskDao()
    private val storehouseItemDao = db.storehouseItemDao()
    private val usageEventDao = db.usageEventDao()
    private val adminConfigDao = db.adminConfigDao()
    private val connectorRecordDao = db.connectorRecordDao()
    private val alarmItemDao = db.alarmItemDao()
    private val announcementItemDao = db.announcementItemDao()

    // ==========================================
    // ALARMS & CONNECTORS LIVE FLOW REGISTER
    // ==========================================
    val allAlarms: StateFlow<List<AlarmItem>> = alarmItemDao.getAllAlarmsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val connectorRecords: StateFlow<List<ConnectorRecord>> = connectorRecordDao.getAllRecordsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val allUserAccounts: StateFlow<List<UserAccount>> = accountDao.getAllAccountsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val announcements: StateFlow<List<com.example.data.AnnouncementItem>> = announcementItemDao.getAllAnnouncementsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val allUsageEvents: StateFlow<List<UsageEvent>> = usageEventDao.getAllEventsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun logConnectorActivity(actionName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val record = ConnectorRecord(
                username = user.username,
                email = user.email,
                status = "Activity",
                state = user.state ?: "",
                gmail = user.gmail ?: "",
                lastActivity = actionName
            )
            connectorRecordDao.insertRecord(record)
        }
    }

    fun insertAlarm(alarm: AlarmItem, onScheduled: (AlarmItem) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = alarmItemDao.insertAlarm(alarm)
            val fullAlarm = alarm.copy(id = insertedId.toInt())
            viewModelScope.launch(Dispatchers.Main) {
                onScheduled(fullAlarm)
            }
        }
    }

    fun updateAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmItemDao.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmItemDao.deleteAlarm(alarm)
        }
    }

    fun updateAdminConfig(config: AdminConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            adminConfigDao.insertOrUpdateConfig(config)
        }
    }

    // ==========================================
    // AUTHENTICATION STATE (Firebase Simulator)
    // ==========================================
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // ==========================================
    // ADMIN CONFIG STATE (Live styles & controls)
    // ==========================================
    val adminConfig: StateFlow<AdminConfig> = adminConfigDao.getConfigFlow()
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AdminConfig()
        )

    fun updateThemeColors(primary: String, secondary: String, bg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                primaryColorHex = primary,
                secondaryColorHex = secondary,
                backgroundColorHex = bg
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateFont(fontName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(selectedFontName = fontName)
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateCategories(csv: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(categoriesString = csv)
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateHomeConfig(
        welcomeMsg: String,
        link: String,
        announcementTitle: String,
        announcementBody: String,
        visibleCsv: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                welcomeMessage = welcomeMsg,
                channelLink = link,
                announcementTitle = announcementTitle,
                announcementBody = announcementBody,
                visibleSectionsCsv = visibleCsv
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateAdminConfigComplete(
        primaryHex: String,
        secondaryHex: String,
        bgHex: String,
        fontChoice: String,
        categoriesCsv: String,
        welcomeMsg: String,
        channelLink: String,
        announceTitle: String,
        announceBody: String,
        visibleCsv: String,
        newAdminEmail: String,
        newAdminKey: String,
        musicEnabled: Boolean,
        welcomeName: String,
        welcomeUri: String,
        badgeName: String,
        badgeUri: String,
        celebrationName: String,
        celebrationUri: String,
        notifyName: String,
        notifyUri: String,
        customBadgeName: String = "",
        customBadgeLogo: String = "",
        customBadgeAppearance: String = "",
        customBadgeImage: String? = null,
        customHydrationLogo: String = "🧊",
        customHydrationLogoImage: String? = null,
        customPopupLogo: String = "🔔",
        customPopupLogoImage: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex,
                backgroundColorHex = bgHex,
                selectedFontName = fontChoice,
                categoriesString = categoriesCsv,
                welcomeMessage = welcomeMsg,
                channelLink = channelLink,
                announcementTitle = announceTitle,
                announcementBody = announceBody,
                visibleSectionsCsv = visibleCsv,
                adminEmail = newAdminEmail,
                adminKey = newAdminKey,
                welcomeMusicEnabled = musicEnabled,
                welcomeMusicName = welcomeName,
                welcomeMusicUri = welcomeUri,
                badgeSoundName = badgeName,
                badgeSoundUri = badgeUri,
                celebrationSoundName = celebrationName,
                celebrationSoundUri = celebrationUri,
                notificationSoundName = notifyName,
                notificationSoundUri = notifyUri,
                badgeName = customBadgeName,
                badgeLogo = customBadgeLogo,
                badgeAppearance = customBadgeAppearance,
                badgeImage = customBadgeImage,
                hydrationLogo = customHydrationLogo,
                hydrationLogoImage = customHydrationLogoImage,
                popupLogo = customPopupLogo,
                popupLogoImage = customPopupLogoImage
            )
            adminConfigDao.insertOrUpdateConfig(updated)
            
            // Sync current active Admin credentials in session live & invalidate others
            val allAccounts = accountDao.getAllAccountsDirect()
            var adminUserFound = false
            for (acc in allAccounts) {
                if (acc.email.trim().equals(newAdminEmail.trim(), ignoreCase = true)) {
                    val updatedAcc = acc.copy(isAdmin = true, passwordPlain = newAdminKey)
                    accountDao.insertAccount(updatedAcc)
                    adminUserFound = true
                    val activeUser = _currentUser.value
                    if (activeUser != null && activeUser.id == acc.id) {
                        _currentUser.value = updatedAcc
                    }
                } else if (acc.isAdmin) {
                    // Invalidate old admin privileges immediately
                    val deactivated = acc.copy(isAdmin = false)
                    accountDao.insertAccount(deactivated)
                    val activeUser = _currentUser.value
                    if (activeUser != null && activeUser.id == acc.id) {
                        _currentUser.value = deactivated
                    }
                }
            }
            if (!adminUserFound) {
                val newAdminAccount = UserAccount(
                    email = newAdminEmail,
                    username = "AdminPro",
                    passwordPlain = newAdminKey,
                    isAdmin = true
                )
                val newId = accountDao.insertAccount(newAdminAccount)
                val activeUser = _currentUser.value
                if (activeUser != null && activeUser.isAdmin) {
                    _currentUser.value = newAdminAccount.copy(id = newId.toInt())
                }
            }
        }
    }

    // ==========================================
    // CALCULATOR STATE (Feature 5)
    // ==========================================
    private val _calculatorInput = MutableStateFlow("")
    val calculatorInput: StateFlow<String> = _calculatorInput.asStateFlow()

    private val _calculatorResult = MutableStateFlow("")
    val calculatorResult: StateFlow<String> = _calculatorResult.asStateFlow()

    val calculationHistory: StateFlow<List<Calculation>> = calculationDao.getAllCalculations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onCalculatorKeyPress(key: String) {
        viewModelScope.launch {
            when (key) {
                "C" -> {
                    _calculatorInput.value = ""
                    _calculatorResult.value = ""
                }
                "⌫" -> {
                    val current = _calculatorInput.value
                    if (current.isNotEmpty()) {
                        _calculatorInput.value = current.substring(0, current.length - 1)
                    }
                }
                "=" -> {
                    val input = _calculatorInput.value
                    if (input.isNotEmpty()) {
                        val resultVal = evaluateExpression(input)
                        _calculatorResult.value = resultVal
                        // Save in database
                        calculationDao.insertCalculation(
                            Calculation(expression = input, result = resultVal)
                        )
                    }
                }
                "MC" -> {
                    calculatorMemory = 0.0
                    _calculatorResult.value = "Memory Cleared"
                }
                "MR" -> {
                    val formatted = if (calculatorMemory % 1.0 == 0.0) {
                        calculatorMemory.toLong().toString()
                    } else {
                        calculatorMemory.toString()
                    }
                    _calculatorInput.value += formatted
                }
                "M+" -> {
                    val input = _calculatorInput.value
                    val currentVal = if (input.isNotEmpty()) evaluateExpression(input).toDoubleOrNull() ?: 0.0 else 0.0
                    calculatorMemory += currentVal
                    _calculatorResult.value = "M+ added"
                }
                "M-" -> {
                    val input = _calculatorInput.value
                    val currentVal = if (input.isNotEmpty()) evaluateExpression(input).toDoubleOrNull() ?: 0.0 else 0.0
                    calculatorMemory -= currentVal
                    _calculatorResult.value = "M- subtracted"
                }
                else -> {
                    _calculatorInput.value += key
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            calculationDao.clearHistory()
        }
    }

    // Persistent Calculator memory state
    private var calculatorMemory: Double = 0.0

    // Safe mathematical expression calculator
    fun evaluateExpression(expr: String): String {
        return try {
            val cleaned = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt")
            val result = parseAndEvaluate(cleaned)
            if (result.isNaN() || result.isInfinite()) {
                "Error"
            } else if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format(Locale.US, "%.6f", result).trimEnd('0').trimEnd('.')
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun parseAndEvaluate(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected expression: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor // division
                    } else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, this.pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // scientific functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        "log" -> Math.log10(x)
                        "ln" -> Math.log(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor()) // power
                if (eat('%'.code)) x = x / 100.0 // percentage
                return x
            }
        }.parse()
    }

    // ==========================================
    // TASK CUSTOMIZER (Feature 7)
    // ==========================================
    val taskList: StateFlow<List<CustomizedTask>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertTask(title: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskList.value
            val nextOrder = if (list.isNotEmpty()) list.maxOf { it.displayOrder } + 1 else 0
            taskDao.insertTask(
                CustomizedTask(
                    title = title,
                    category = category,
                    displayOrder = nextOrder
                )
            )
        }
    }

    fun updateTask(task: CustomizedTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
        }
    }

    fun toggleTaskComplete(task: CustomizedTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun modifyTaskTitleAndCategory(task: CustomizedTask, title: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task.copy(title = title, category = category))
        }
    }

    fun deleteTask(task: CustomizedTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
        }
    }

    fun updateCustomizedTask(task: CustomizedTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
        }
    }

    fun moveTask(task: CustomizedTask, up: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskList.value.toMutableList()
            val index = list.indexOfFirst { it.id == task.id }
            if (index == -1) return@launch

            if (up && index > 0) {
                // Swap with the previous task
                val prev = list[index - 1]
                val updatedPrevOrder = task.displayOrder
                val updatedCurrentOrder = prev.displayOrder

                taskDao.insertTask(task.copy(displayOrder = updatedCurrentOrder))
                taskDao.insertTask(prev.copy(displayOrder = updatedPrevOrder))
            } else if (!up && index < list.size - 1) {
                // Swap with next task
                val next = list[index + 1]
                val updatedNextOrder = task.displayOrder
                val updatedCurrentOrder = next.displayOrder

                taskDao.insertTask(task.copy(displayOrder = updatedCurrentOrder))
                taskDao.insertTask(next.copy(displayOrder = updatedNextOrder))
            }
        }
    }

    // ==========================================
    // PERSONAL STOREHOUSE (Feature 8)
    // ==========================================
    private val _storehouseSearchQuery = MutableStateFlow("")
    val storehouseSearchQuery: StateFlow<String> = _storehouseSearchQuery.asStateFlow()

    val storehouseItems: StateFlow<List<StorehouseItem>> = _storehouseSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                storehouseItemDao.getAllItems()
            } else {
                storehouseItemDao.searchItems(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _storehouseSearchQuery.value = query
    }

    fun addStoreItem(title: String, type: String, content: String, folder: String, category: String, sizeBytes: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            storehouseItemDao.insertItem(
                StorehouseItem(
                    title = title,
                    type = type,
                    content = content,
                    folderName = folder,
                    category = category,
                    sizeBytes = sizeBytes
                )
            )
        }
    }

    fun deleteStoreItem(item: StorehouseItem) {
        viewModelScope.launch(Dispatchers.IO) {
            storehouseItemDao.deleteItem(item)
        }
    }

    // ==========================================
    // PROGRESS & SCREEN TIME TRACKER (Feature 6)
    // ==========================================
    private var lastSwitchTime = System.currentTimeMillis()
    private var currentCategory = "Dashboard"

    val usageEventsFlow: StateFlow<List<UsageEvent>> = usageEventDao.getAllEventsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchProgressCategory(newCategory: String) {
        val now = System.currentTimeMillis()
        val duration = now - lastSwitchTime
        if (duration > 500) { // log only if spent more than 0.5s
            logUsageEvent(currentCategory, duration)
        }
        lastSwitchTime = now
        currentCategory = newCategory
    }

    fun logUsageEvent(category: String, durationMs: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            usageEventDao.insertEvent(
                UsageEvent(
                    categoryName = category,
                    durationMs = durationMs,
                    dateString = dateStr
                )
            )
        }
    }

    // Helper to log periodically while app is open
    fun logBackgroundHeartbeat() {
        val now = System.currentTimeMillis()
        val duration = now - lastSwitchTime
        if (duration > 1000) {
            logUsageEvent(currentCategory, duration)
            lastSwitchTime = now
        }
    }

    // ==========================================
    // SYSTEM NOTIFICATIONS (Firebase Simulator)
    // ==========================================
    fun sendBroadcastNotification(title: String, message: String) {
        sendBroadcastNotificationWithDetails(title, message, null)
    }

    fun sendBroadcastNotificationWithDetails(title: String, message: String, imageUrl: String?) {
        val context = getApplication<Application>().applicationContext
        // Use our custom real Firebase Messaging Service dispatcher to trigger OS heads-up alert
        com.example.service.MyFirebaseMessagingService.sendPushNotification(context, title, message, imageUrl)

        // Persistent insertion into announcements table
        viewModelScope.launch(Dispatchers.IO) {
            val announcement = com.example.data.AnnouncementItem(
                title = title,
                message = message,
                imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl
            )
            announcementItemDao.insertAnnouncement(announcement)

            // Sync with current Admin announcement title/body and standard Simulated System Chats
            val config = adminConfig.value
            val array = try {
                JSONArray(config.supportChatsJson)
            } catch (e: Exception) {
                JSONArray()
            }

            val chatObj = JSONObject()
            chatObj.put("sender", "SYSTEM")
            chatObj.put("message", "Broadcast Announcement: $title - $message")
            chatObj.put("timestamp", System.currentTimeMillis())
            array.put(chatObj)

            val updated = config.copy(
                announcementTitle = title,
                announcementBody = message,
                supportChatsJson = array.toString()
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    // ==========================================
    // REAL CLOUD AUTH & FIRESTORE LOGINS (Local simulator wrapper)
    // ==========================================
    fun registerNewUser(email: String, username: String, pass: String, isAdmin: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _authError.value = null
            if (email.isBlank() || username.isBlank() || pass.isBlank()) {
                _authError.value = "All fields are required!"
                return@launch
            }

            // Secure guard: block regular registration using active admin email
            val activeConfig = adminConfigDao.getConfigDirect() ?: AdminConfig()
            if (email.trim().equals(activeConfig.adminEmail.trim(), ignoreCase = true)) {
                _authError.value = "This email is reserved for Admin authentication. Please sign in directly! 💕"
                return@launch
            }

            val existing = accountDao.getAccountByEmail(email)
            if (existing != null) {
                _authError.value = "An account with this email already exists!"
                return@launch
            }

            val newUser = UserAccount(email = email, username = username, passwordPlain = pass, isAdmin = isAdmin)
            accountDao.insertAccount(newUser)
            _currentUser.value = newUser
            _isDarkTheme.value = newUser.isThemeDark
            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    fun loginUser(context: android.content.Context, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _authError.value = null
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "Please fill in all details!"
                return@launch
            }

            val activeConfig = adminConfigDao.getConfigDirect() ?: AdminConfig()
            val masterAdminEmail = activeConfig.adminEmail
            val masterAdminKey = activeConfig.adminKey

            val resolvedAccount = if (email.trim().equals(masterAdminEmail.trim(), ignoreCase = true) && pass == masterAdminKey) {
                // Instantly sync/seed admin role to SQLite
                var account = accountDao.getAccountByEmail(masterAdminEmail)
                if (account == null) {
                    account = UserAccount(email = masterAdminEmail, username = "Admin", passwordPlain = masterAdminKey, isAdmin = true)
                    accountDao.insertAccount(account)
                } else if (!account.isAdmin || account.passwordPlain != masterAdminKey) {
                    account = account.copy(isAdmin = true, passwordPlain = masterAdminKey)
                    accountDao.insertAccount(account)
                }
                
                // ALSO invalidate any other accounts that have isAdmin = true but do not match the new email or key!
                val allAccounts = accountDao.getAllAccountsDirect()
                for (acc in allAccounts) {
                    if (!acc.email.trim().equals(masterAdminEmail.trim(), ignoreCase = true) && acc.isAdmin) {
                        accountDao.insertAccount(acc.copy(isAdmin = false))
                    }
                }
                
                account
            } else {
                val account = accountDao.getAccountByEmail(email)
                if (account == null) {
                    _authError.value = "Account not found! Sign up above."
                    return@launch
                }
                if (account.passwordPlain != pass) {
                    _authError.value = "Incorrect password! Try again."
                    return@launch
                }
                // Check if old credentials are tried
                if (account.isAdmin && (!email.trim().equals(masterAdminEmail.trim(), ignoreCase = true) || pass != masterAdminKey)) {
                    // Invalidate this account's admin status in database immediately!
                    accountDao.insertAccount(account.copy(isAdmin = false))
                    _authError.value = "This admin credential has been modified and is no longer valid. Enter new keys!"
                    return@launch
                }
                account
            }

            _currentUser.value = resolvedAccount
            _isDarkTheme.value = resolvedAccount.isThemeDark
            
            // Log connector activity
            try {
                connectorRecordDao.insertRecord(
                    ConnectorRecord(
                        username = resolvedAccount.username,
                        email = resolvedAccount.email,
                        status = "Logged In",
                        state = resolvedAccount.state ?: "Delhi",
                        gmail = resolvedAccount.gmail ?: resolvedAccount.email,
                        lastActivity = if (resolvedAccount.isAdmin) "Logged into App Shell as Admin" else "Logged into App Shell"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Play cute luxury welcome sound on login!
            if (activeConfig.welcomeMusicEnabled) {
                viewModelScope.launch(Dispatchers.Main) {
                    AlarmTonePlayer.playSound(context, activeConfig.welcomeMusicUri, "Luxury Entrance")
                }
            }

            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    fun completeRecoverySetup(recoveryEmail: String, recoveryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                recoveryEmail = recoveryEmail,
                recoveryCode = recoveryCode,
                isRecoverySetupCompleted = true
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateRecoveryCodeOnly(recoveryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                recoveryCode = recoveryCode
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun updateRecoveryEmailOnly(recoveryEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val updated = current.copy(
                recoveryEmail = recoveryEmail
            )
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun addTrustedDevice(deviceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val list = try {
                val array = org.json.JSONArray(current.trustedDevicesJson)
                val newList = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    newList.add(array.getString(i))
                }
                newList
            } catch (e: Exception) {
                mutableListOf()
            }
            if (!list.contains(deviceName)) {
                list.add(deviceName)
                val updatedJson = org.json.JSONArray(list).toString()
                val updated = current.copy(trustedDevicesJson = updatedJson)
                adminConfigDao.insertOrUpdateConfig(updated)
            }
        }
    }

    fun removeTrustedDevice(deviceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = adminConfig.value
            val list = try {
                val array = org.json.JSONArray(current.trustedDevicesJson)
                val newList = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    newList.add(array.getString(i))
                }
                newList
            } catch (e: Exception) {
                mutableListOf()
            }
            if (list.contains(deviceName)) {
                list.remove(deviceName)
                val updatedJson = org.json.JSONArray(list).toString()
                val updated = current.copy(trustedDevicesJson = updatedJson)
                adminConfigDao.insertOrUpdateConfig(updated)
            }
        }
    }

    fun recoverAdminAccount(enteredCode: String, newAdminEmail: String, newAdminPIN: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = adminConfig.value
            if (config.recoveryCode.isNotBlank() && config.recoveryCode.trim() == enteredCode.trim()) {
                // Code matches! Let's update AdminConfig with the new Email and PIN
                val updatedConfig = config.copy(
                    adminEmail = newAdminEmail,
                    adminKey = newAdminPIN
                )
                adminConfigDao.insertOrUpdateConfig(updatedConfig)
                
                // Also update UserAccount privileges or create Admin User account in DB
                val allAccounts = accountDao.getAllAccountsDirect()
                var adminUserFound = false
                for (acc in allAccounts) {
                    if (acc.email.trim().equals(newAdminEmail.trim(), ignoreCase = true)) {
                        val updatedAcc = acc.copy(isAdmin = true, passwordPlain = newAdminPIN)
                        accountDao.insertAccount(updatedAcc)
                        adminUserFound = true
                    } else if (acc.isAdmin) {
                        accountDao.insertAccount(acc.copy(isAdmin = false))
                    }
                }
                if (!adminUserFound) {
                    val newAdminAccount = UserAccount(
                        email = newAdminEmail,
                        username = "AdminPro",
                        passwordPlain = newAdminPIN,
                        isAdmin = true
                    )
                    accountDao.insertAccount(newAdminAccount)
                }
                
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure("Incorrect Recovery Code! Verification failed.")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // ==========================================
    // THEME STATE & LIGHT/DARK THEME SWITCHER
    // ==========================================
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleThemeMode(onMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value
            if (user != null) {
                val updatedDark = !user.isThemeDark
                val updated = user.copy(isThemeDark = updatedDark)
                accountDao.insertAccount(updated)
                _currentUser.value = updated
                _isDarkTheme.value = updatedDark
                viewModelScope.launch(Dispatchers.Main) {
                    onMessage(if (updatedDark) "Your theme changed to Dark Mode 🌙" else "Your theme changed to Light Mode ☀️")
                }
            } else {
                val updatedDark = !_isDarkTheme.value
                _isDarkTheme.value = updatedDark
                viewModelScope.launch(Dispatchers.Main) {
                    onMessage(if (updatedDark) "Your theme changed to Dark Mode 🌙" else "Your theme changed to Light Mode ☀️")
                }
            }
        }
    }

    // ==========================================
    // HYDRATION STATE & ACTION CONTROLLERS (Feature 3)
    // ==========================================
    fun toggleHydrationGlass(index: Int, onStreakCelebration: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val progressList = try {
                val arr = JSONArray(user.hydrationProgress)
                val list = mutableListOf<Int>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getInt(i))
                }
                list
            } catch (e: Exception) {
                mutableListOf<Int>()
            }

            if (progressList.contains(index)) {
                progressList.remove(index)
            } else {
                progressList.add(index)
            }

            val updatedProgress = JSONArray(progressList).toString()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            var newStreak = user.hydrationStreak
            if (progressList.size == user.hydrationGoal) {
                if (user.hydrationLastDate != todayStr) {
                    newStreak += 1
                    viewModelScope.launch(Dispatchers.Main) { onStreakCelebration() }
                }
            } else if (progressList.isEmpty() && user.hydrationStreak > 0 && user.hydrationLastDate == todayStr) {
                // If they undo all, decrement streak safely
                newStreak = max(0, newStreak - 1)
            }

            val updated = user.copy(
                hydrationProgress = updatedProgress,
                hydrationLastDate = todayStr,
                hydrationStreak = newStreak
            )
            accountDao.insertAccount(updated)
            _currentUser.value = updated
        }
    }

    fun resetHydrationProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(hydrationProgress = "[]")
            accountDao.insertAccount(updated)
            _currentUser.value = updated
        }
    }

    fun updateHydrationGoal(goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(hydrationGoal = max(1, goal))
            accountDao.insertAccount(updated)
            _currentUser.value = updated
        }
    }

    // ==========================================
    // SMART ROUTINE GENERATOR - GEMINI REST API (Feature 2)
    // ==========================================
    private val _isRoutineLoading = MutableStateFlow(false)
    val isRoutineLoading: StateFlow<Boolean> = _isRoutineLoading.asStateFlow()

    fun getLocalAestheticSchedule(
        course: String, specialNotes: String, wakeUp: String, classStart: String, classEnd: String
    ): String {
        val courseText = if (course.isBlank()) "Explorer studies" else course
        val notesText = if (specialNotes.isBlank()) "None" else specialNotes
        
        val wakeHour = try { wakeUp.substringBefore(":").toInt() } catch(e: Exception) { 7 }
        val startHour = try { classStart.substringBefore(":").toInt() } catch(e: Exception) { 9 }
        val endHour = try { classEnd.substringBefore(":").toInt() } catch(e: Exception) { 16 }
        
        val builder = java.lang.StringBuilder()
        builder.append("### 📅 Smart Schedule (Course: $courseText) ⏱️\n\n")
        builder.append("Here is your highly structured, hourly schedule chart customized perfectly for you! ✨🌷\n\n")
        builder.append("| Time Slot | Activity Description | Details & Emojis 🌸 |\n")
        builder.append("|---|---|---|\n")
        
        var current = wakeHour
        builder.append(String.format("| %02d:00 - %02d:00 | 🌅 Morning Breakfast | Fresh morning start, hygiene & hydration 🎀 |\n", current, current + 1))
        current++
        
        if (current < startHour) {
            builder.append(String.format("| %02d:00 - %02d:00 | 📚 Prep Study / Commute | Gathering study material & notes for $courseText 💮 |\n", current, startHour))
            current = startHour
        }
        
        if (startHour < endHour) {
            val midClass = (startHour + endHour) / 2
            builder.append(String.format("| %02d:00 - %02d:00 | 🎓 Classes Block (Part I) | Active lecture attendance & note taking 💗 |\n", startHour, midClass))
            builder.append(String.format("| %02d:00 - %02d:00 | 🍱 Lunch Break | Nourishing break to power up! 🍱 |\n", midClass, midClass + 1))
            builder.append(String.format("| %02d:00 - %02d:00 | 🎓 Classes Block (Part II) | Afternoon lectures & practical assignments 💻 |\n", midClass + 1, endHour))
            current = endHour
        }
        
        builder.append(String.format("| %02d:00 - %02d:00 | 🍵 Tea & Wind-down | Relaxation & stretching session 🧸 |\n", current, current + 1))
        current++
        
        builder.append(String.format("| %02d:00 - %02d:00 | 📝 Self Homework / Revise | Revision & coursework assignment tasks 📚 |\n", current, current + 3))
        current += 3
        
        builder.append(String.format("| %02d:00 - %02d:00 | 🍥 Dinner & Music | Relax, cozy visual arts or pastel songs 🍨 |\n", current, current + 1))
        current++
        
        builder.append(String.format("| %02d:00 - 23:00 | 🌙 Cozy Night Wind-Down | Skincare routine, reading novels, cozy planning 🧸 |\n", current))
        builder.append("| 23:00 - 07:00 | 💤 Restful Sleep | Sleeping nicely inside a warm cozy bed 🎀 |\n\n")
        
        builder.append("#### ⭐ Special Notes\n")
        builder.append("- Notes: $notesText\n")
        builder.append("- Tips: Work in focused 25-minute study sprints with sweet tea!\n")
        
        return builder.toString()
    }

    fun saveUserScheduleToDb(scheduleText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value
            if (user != null) {
                val updated = user.copy(generatedSchedule = scheduleText)
                accountDao.insertAccount(updated)
                _currentUser.value = updated
            }
        }
    }

    fun generateScheduleWithAI(
        course: String, specialNotes: String, wakeUp: String, classStart: String, classEnd: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRoutineLoading.value = true
            val apiKey = BuildConfig.GEMINI_API_KEY
            val fallbackText = getLocalAestheticSchedule(course, specialNotes, wakeUp, classStart, classEnd)

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                val user = _currentUser.value
                if (user != null) {
                    val updated = user.copy(generatedSchedule = fallbackText)
                    accountDao.insertAccount(updated)
                    _currentUser.value = updated
                }
                _isRoutineLoading.value = false
                return@launch
            }

            val prompt = """
                Arrange a cute, beautiful, and aesthetic girly timetable.
                User schedule details:
                - Course / Year: $course
                - Wake Up Time (24h format): $wakeUp
                - Classes Starting Time (24h format): $classStart
                - Classes Ending Time (24h format): $classEnd
                - Special Notes: $specialNotes
                
                Please generate:
                An Hourly Schedule Chart depicting 1-hour or 2-hour timeline slots starting from the Wake Up time ($wakeUp), including morning preparation, commuting/studying, actual classes ($classStart to $classEnd), lunch break, self-study, tea/wind-down, dinner, and sleeping.
                Use a beautiful markdown table format with emojis aggressively (hearts 💗, sparkles ✨, bows 🎀, and clouds ☁️).
                Do NOT output any conversational text, guidelines, introduction, meta-prompts or advice. Respond ONLY with direct markdown title "### 📅 Smart Schedule Chart" followed by the table.
            """.trimIndent()

            val client = OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val urlText = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val reqJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                           put(JSONObject().apply {
                               put("text", prompt)
                           })
                        })
                    })
                })
            }

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                reqJson.toString()
            )

            val request = Request.Builder()
                .url(urlText)
                .post(body)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val respJson = JSONObject(respStr)
                    val candidatesList = respJson.getJSONArray("candidates")
                    val firstCand = candidatesList.getJSONObject(0)
                    val contentObj = firstCand.getJSONObject("content")
                    val partsList = contentObj.getJSONArray("parts")
                    val generatedText = partsList.getJSONObject(0).getString("text")

                    val currentUserVal = _currentUser.value
                    if (currentUserVal != null) {
                        val updated = currentUserVal.copy(generatedSchedule = generatedText)
                        accountDao.insertAccount(updated)
                        _currentUser.value = updated
                    }
                } else {
                    // Fail gracefully to local aesthetic schedule on non-200 responses
                    val currentUserVal = _currentUser.value
                    if (currentUserVal != null) {
                        val updated = currentUserVal.copy(generatedSchedule = fallbackText)
                        accountDao.insertAccount(updated)
                        _currentUser.value = updated
                    }
                }
            } catch (e: Exception) {
                // Fail gracefully to local aesthetic schedule on network Exceptions
                val currentUserVal = _currentUser.value
                if (currentUserVal != null) {
                    val updated = currentUserVal.copy(generatedSchedule = fallbackText)
                    accountDao.insertAccount(updated)
                    _currentUser.value = updated
                }
            } finally {
                _isRoutineLoading.value = false
            }
        }
    }

    // ==========================================
    // REMINDERS WITH LOCAL ALERTER ENGINE (Feature 4)
    // ==========================================
    fun insertReminderTask(title: String, category: String, date: String?, time: String?, recurring: Boolean, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskList.value
            val nextOrder = if (list.isNotEmpty()) list.maxOf { it.displayOrder } + 1 else 0
            val newTask = CustomizedTask(
                title = title,
                category = category,
                displayOrder = nextOrder,
                reminderDate = date,
                reminderTime = time,
                isRecurring = recurring
            )
            val newId = taskDao.insertTask(newTask)

            // Dynamic Exact AlarmManager trigger!
            if (date != null && time != null) {
                scheduleExactTaskAlarm(context, newId.toInt(), title, date, time)
            }
        }
    }

    private fun scheduleExactTaskAlarm(context: android.content.Context, taskId: Int, title: String, dateStr: String, timeStr: String) {
        try {
            val calendar = java.util.Calendar.getInstance()
            val dateParts = dateStr.trim().split("-")
            val timeParts = timeStr.trim().split(":")
            if (dateParts.size == 3 && timeParts.size == 2) {
                calendar.apply {
                    set(java.util.Calendar.YEAR, dateParts[0].toInt())
                    set(java.util.Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(java.util.Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                    set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(java.util.Calendar.MINUTE, timeParts[1].toInt())
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                    val intent = android.content.Intent(context, com.example.AlarmReceiver::class.java).apply {
                        putExtra("is_task_reminder", true)
                        putExtra("task_id", taskId)
                        putExtra("task_name", title)
                    }
                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                        context,
                        100000 + taskId,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
                    )

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startReminderEngine() {
        viewModelScope.launch(Dispatchers.IO) {
            val triggeredReminders = mutableSetOf<String>()
            while (true) {
                delay(15000) // check every 15 seconds
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                val now = Date()
                val todayStr = sdfDate.format(now)
                val currTimeStr = sdfTime.format(now)

                val tasks = taskList.value
                for (task in tasks) {
                    if (!task.isCompleted && task.reminderTime != null) {
                        val uniqueKey = "${task.id}_${task.reminderDate}_${task.reminderTime}"
                        if (!triggeredReminders.contains(uniqueKey)) {
                            val dateMatches = task.reminderDate == null || task.reminderDate == todayStr
                            val timeMatches = task.reminderTime == currTimeStr
                            if (dateMatches && timeMatches) {
                                triggeredReminders.add(uniqueKey)
                                sendBroadcastNotification(
                                    "🎀 Time for: ${task.title}",
                                    "Your scheduled task reminder is active now! Don't forget to take a minute for yourself! 💅🏻✨"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 5-MIN CHANNEL PROMOTION REGISTRY
    // ==========================================
    private val _showPromoPopup = MutableStateFlow(false)
    val showPromoPopup: StateFlow<Boolean> = _showPromoPopup.asStateFlow()

    fun dismissPromoPopup() {
        _showPromoPopup.value = false
    }

    fun triggerPromoPopupNow() {
        _showPromoPopup.value = true
    }

    // ==========================================
    // USER PROFILE PERMANENCE SETUP
    // ==========================================
    fun setupUserProfile(username: String, gmail: String, state: String, profilePic: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = UserAccount(
                email = gmail,
                username = username,
                passwordPlain = "cutePassword123",
                isAdmin = false,
                profilePic = profilePic,
                state = state,
                gmail = gmail,
                isThemeDark = false
            )
            val insertId = accountDao.insertAccount(account)
            val registeredProfile = account.copy(id = insertId.toInt())
            _currentUser.value = registeredProfile
            _isDarkTheme.value = false
            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    fun updateUserProfile(user: UserAccount) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDao.insertAccount(user)
            _currentUser.value = user
            _isDarkTheme.value = user.isThemeDark

            // Log connector activity
            try {
                connectorRecordDao.insertRecord(
                    ConnectorRecord(
                        username = user.username,
                        email = user.email,
                        status = "Updated Profile",
                        state = user.state ?: "Delhi",
                        gmail = user.gmail ?: user.email,
                        lastActivity = "Modified display details & avatar"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // MOCK CHAT MESSAGES
    // ==========================================
    fun sendUserSupportMessage(userMsg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = adminConfig.value
            val array = try {
                JSONArray(config.supportChatsJson)
            } catch (e: Exception) {
                JSONArray()
            }

            val userObj = currentUser.value
            val userEmail = userObj?.email ?: "user@example.com"
            val userName = userObj?.username ?: "User"

            // User message
            val chatObj = JSONObject()
            chatObj.put("sender", userName)
            chatObj.put("userEmail", userEmail)
            chatObj.put("message", userMsg)
            chatObj.put("timestamp", System.currentTimeMillis())
            array.put(chatObj)

            // Auto simulated support replies
            val replyObj = JSONObject()
            replyObj.put("sender", "Admin Helpdesk 🌸")
            replyObj.put("userEmail", userEmail)
            replyObj.put("message", "Thank you, $userName! Your message was routed to the Support Desk. We'll assist you right away!")
            replyObj.put("timestamp", System.currentTimeMillis() + 1000)
            array.put(replyObj)

            val updated = config.copy(supportChatsJson = array.toString())
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun clearSupportChats() {
        viewModelScope.launch(Dispatchers.IO) {
            val config = adminConfig.value
            val updated = config.copy(supportChatsJson = "[]")
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    fun clearAnnouncementHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            announcementItemDao.clearAll()
        }
    }

    fun sendAdminSupportReply(replyMsg: String, targetEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = adminConfig.value
            val array = try {
                JSONArray(config.supportChatsJson)
            } catch (e: Exception) {
                JSONArray()
            }
            val chatObj = JSONObject()
            chatObj.put("sender", "Admin Helpdesk 🌸")
            chatObj.put("userEmail", targetEmail)
            chatObj.put("message", replyMsg)
            chatObj.put("timestamp", System.currentTimeMillis())
            array.put(chatObj)
            
            val updated = config.copy(supportChatsJson = array.toString())
            adminConfigDao.insertOrUpdateConfig(updated)
        }
    }

    init {
        // Trigger database seed
        DatabaseProvider.seedInitialData(application)
        startReminderEngine()
        viewModelScope.launch {
            delay(300000) // 5 minutes delay popup
            _showPromoPopup.value = true
        }
    }
}
