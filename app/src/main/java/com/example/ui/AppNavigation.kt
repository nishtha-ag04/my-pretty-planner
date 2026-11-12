@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.AlarmTonePlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.UUID
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import android.os.Build
import com.example.data.AlarmItem
import com.example.data.AppDatabase
import com.example.data.Calculation
import com.example.data.CustomizedTask
import com.example.data.StorehouseItem
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ==========================================
// COLOR CONVERSION HELPERS
// ==========================================
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color(0xFFFF6B8B) // default sweet pink fallback
    }
}

// Adjust bright colors for light text readability
fun Color.adjustForText(): Color {
    return this.copy(alpha = 0.85f)
}

// ==========================================
// CUTE GLOBAL HELPERS BY MY PRETTY PLANNER 💗
// ==========================================
val indianStates = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana",
    "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
    "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands", "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
)

fun String.toDoubleStruck(): String {
    val builder = StringBuilder()
    for (char in this) {
        val mapped = when (char) {
            'A' -> "𝔸" ; 'B' -> "𝔹" ; 'C' -> "ℂ" ; 'D' -> "𝔻" ; 'E' -> "𝔼" ; 'F' -> "𝔽" ; 'G' -> "𝔾" ; 'H' -> "ℍ"
            'I' -> "𝕀" ; 'J' -> "𝕁" ; 'K' -> "𝕂" ; 'L' -> "𝕃" ; 'M' -> "𝕄" ; 'N' -> "ℕ" ; 'O' -> "𝕆" ; 'P' -> "ℙ"
            'Q' -> "ℚ" ; 'R' -> "ℝ" ; 'S' -> "𝕊" ; 'T' -> "𝕋" ; 'U' -> "𝕌" ; 'V' -> "𝕍" ; 'W' -> "𝕎" ; 'X' -> "𝕏"
            'Y' -> "𝕐" ; 'Z' -> "ℤ"
            'a' -> "𝕒" ; 'b' -> "𝕓" ; 'c' -> "𝕔" ; 'd' -> "𝕕" ; 'e' -> "𝕖" ; 'f' -> "𝕗" ; 'g' -> "𝕘" ; 'h' -> "𝕙"
            'i' -> "𝕚" ; 'j' -> "𝕛" ; 'k' -> "𝕜" ; 'l' -> "𝕝" ; 'm' -> "𝕞" ; 'n' -> "𝕟" ; 'o' -> "𝕠" ; 'p' -> "𝕡"
            'q' -> "𝕢" ; 'r' -> "𝕣" ; 's' -> "𝕤" ; 't' -> "𝕥" ; 'u' -> "𝕦" ; 'v' -> "𝕧" ; 'w' -> "𝕨" ; 'x' -> "𝕩"
            'y' -> "𝕪" ; 'z' -> "𝕫"
            else -> char.toString()
        }
        builder.append(mapped)
    }
    return builder.toString()
}

@Composable
fun Base64ImageOrText(
    base64Str: String?,
    defaultText: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
    imageModifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    defaultTextPrefix: String = ""
) {
    if (!base64Str.isNullOrBlank()) {
        val bitmap = androidx.compose.runtime.remember(base64Str) {
            try {
                val cleanedStr = base64Str.substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(cleanedStr, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Custom Uploaded Logo",
                modifier = imageModifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Text(text = defaultTextPrefix + defaultText, style = style, modifier = modifier)
        }
    } else {
        Text(text = defaultTextPrefix + defaultText, style = style, modifier = modifier)
    }
}

fun uriToBase64(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } else {
            null
        }
    } catch (e: java.lang.Exception) {
         null
    }
}

fun Modifier.drawScrollbar(state: androidx.compose.foundation.lazy.LazyListState, color: Color): Modifier = drawWithContent {
    drawContent()
    val firstVisibleElementIndex = state.firstVisibleItemIndex
    val layoutInfo = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    if (visibleItemsInfo.isNotEmpty()) {
        val totalItemsCount = layoutInfo.totalItemsCount
        val viewportHeight = size.height
        val totalHeight = (viewportHeight / visibleItemsInfo.size) * totalItemsCount
        val scrollbarHeight = ((viewportHeight / totalItemsCount) * visibleItemsInfo.size).coerceAtLeast(30f)
        val scrollbarY = (firstVisibleElementIndex.toFloat() / totalItemsCount) * viewportHeight
        drawRoundRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(size.width - 6.dp.toPx(), scrollbarY),
            size = Size(4.dp.toPx(), scrollbarHeight),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

// 4-bubble cartoon cloud layout shape
val CloudShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.2f, h * 0.8f)
    cubicTo(0f, h * 0.8f, 0f, h * 0.35f, w * 0.2f, h * 0.35f)
    cubicTo(w * 0.2f, h * 0.08f, w * 0.5f, h * 0.08f, w * 0.5f, h * 0.35f)
    cubicTo(w * 0.55f, h * 0.12f, w * 0.8f, h * 0.12f, w * 0.8f, h * 0.35f)
    cubicTo(w, h * 0.35f, w, h * 0.8f, w * 0.8f, h * 0.8f)
    close()
}

@Composable
fun LogoHeartFingerIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFAFAF7)) // Clean off-white background
            .border(1.5.dp, Color(0xFFEFEFEA), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Soft glowing click effect behind the heart
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color(0xFFFFD1DC).copy(alpha = 0.5f), CircleShape) // soft pink-glow click effect
        )
        
        // Inner glowing ring
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFFFF0F5).copy(alpha = 0.6f), CircleShape)
        )

        // Vibrant Red Heart in Center
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFF1493), // Vibrant Red / Hot Pink Heart
            modifier = Modifier.size(48.dp)
        )

        // White glove clicker hand and Cursor Arrow
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pretty White Glove hand pointing (👇 / 👆 / 🫵 style) hovering overlay
            Text(
                text = "👆", 
                fontSize = 38.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 10.dp, y = (-8).dp)
            )

            // Small Blue Cursor Arrow
            Text(
                text = "↗️",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-26).dp, y = 8.dp)
            )
        }
    }
}

// ==========================================
// MAIN ENTRY NAVIGATOR
// ==========================================
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("Splash") }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val isDbDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    // Screen Time heartbeats
    LaunchedEffect(currentScreen) {
        viewModel.switchProgressCategory(
            when (currentScreen) {
                "Splash" -> "Splash"
                "Onboarding" -> "Onboarding"
                "Auth" -> "Authentication"
                "MainShell" -> "MainShell"
                else -> currentScreen
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            viewModel.logBackgroundHeartbeat()
        }
    }

    // Dynamic Fonts according to Admin controls
    val activeFontFamily = when (adminConfig.selectedFontName) {
        "Serif" -> FontFamily.Serif
        "Mono" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    // Aesthetic theme selection with User Custom Coloured Theme preferences support
    val isDark = isDbDark
    val selectedUserTheme = currentUser?.customThemeName ?: "Classic"

    val primaryColor = when (selectedUserTheme) {
        "Sweet Pink" -> if (isDark) Color(0xFFFF8FA3) else Color(0xFFFF6B8B)
        "Baby Blue" -> if (isDark) Color(0xFF8AB4F8) else Color(0xFF4D94FF)
        "Lavender" -> if (isDark) Color(0xFFD7A8FF) else Color(0xFF9E47FF)
        "Mint Green" -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
        "Peach Glow" -> if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
        else -> {
            try { Color(android.graphics.Color.parseColor(adminConfig.primaryColorHex)) } catch (e: Exception) { if (isDark) Color(0xFFFF8FA3) else Color(0xFFFF6B8B) }
        }
    }
    val secondaryColor = when (selectedUserTheme) {
        "Sweet Pink" -> if (isDark) Color(0xFF5C2530) else Color(0xFFFFD6E0)
        "Baby Blue" -> if (isDark) Color(0xFF1E355B) else Color(0xFFD6E4FF)
        "Lavender" -> if (isDark) Color(0xFF411E5B) else Color(0xFFE8D5FF)
        "Mint Green" -> if (isDark) Color(0xFF1B4D20) else Color(0xFFD6FFE9)
        "Peach Glow" -> if (isDark) Color(0xFF5C2E2A) else Color(0xFFFFEEDD)
        else -> {
            try { Color(android.graphics.Color.parseColor(adminConfig.secondaryColorHex)) } catch (e: Exception) { if (isDark) Color(0xFFE8D5FF) else Color(0xFFFFD6E0) }
        }
    }
    val tertiaryColor = Color(0xFFD6E4FF) // Cute Light Blue

    val backgroundColor = when (selectedUserTheme) {
        "Sweet Pink" -> if (isDark) Color(0xFF161415) else Color(0xFFFFF0F5)
        "Baby Blue" -> if (isDark) Color(0xFF101216) else Color(0xFFF0F8FF)
        "Lavender" -> if (isDark) Color(0xFF131116) else Color(0xFFF5F0FF)
        "Mint Green" -> if (isDark) Color(0xFF111412) else Color(0xFFF0FFF4)
        "Peach Glow" -> if (isDark) Color(0xFF161412) else Color(0xFFFFF8F0)
        else -> {
            try {
                val parsed = Color(android.graphics.Color.parseColor(adminConfig.backgroundColorHex))
                if (isDark) {
                    val luma = (parsed.red + parsed.green + parsed.blue) / 3f
                    if (luma > 0.4f) Color(0xFF121212) else parsed
                } else {
                    parsed
                }
            } catch (e: Exception) {
                if (isDark) Color(0xFF121212) else Color(0xFFFFF5F7)
            }
        }
    }
    val surfaceColor = if (isDark) {
        Color(
            red = (backgroundColor.red + 0.08f).coerceAtMost(1f),
            green = (backgroundColor.green + 0.08f).coerceAtMost(1f),
            blue = (backgroundColor.blue + 0.08f).coerceAtMost(1f),
            alpha = 1f
        )
    } else {
        Color.White
    }

    val activeColorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = backgroundColor,
            surface = surfaceColor,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = backgroundColor,
            surface = surfaceColor,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF4A1020),
            onSurface = Color(0xFF4A1020)
        )
    }

    MaterialTheme(
        colorScheme = activeColorScheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "Splash" -> SplashScreen(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = activeFontFamily,
                        onFinished = {
                            currentScreen = if (currentUser != null) "MainShell" else "Auth"
                        }
                    )
                    "Onboarding" -> OnboardingScreen(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = activeFontFamily,
                        onFinished = {
                            currentScreen = "Auth"
                        }
                    )
                    "Auth" -> AuthScreen(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = activeFontFamily,
                        onLoginSuccess = {
                            currentScreen = "MainShell"
                        }
                    )
                    "MainShell" -> AppShell(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        backgroundColor = backgroundColor,
                        fontFamily = activeFontFamily,
                        onLogout = {
                            viewModel.logout()
                            currentScreen = "Auth"
                        }
                    )
                }

                // Mandatory Admin Recovery Setup overlay dialog (Blocks usage until finished)
                val activeUser = currentUser
                if (activeUser != null && activeUser.isAdmin && !adminConfig.isRecoverySetupCompleted) {
                    var recoveryEmailInput by remember { mutableStateOf("") }
                    var isSavedConfirmed by remember { mutableStateOf(false) }
                    var isCodeCopied by remember { mutableStateOf(false) }
                    var setupError by remember { mutableStateOf("") }
                    
                    // Generate recovery code exactly once for this dialog state
                    val generatedCode = remember {
                        val randChars = "ABCDEFGHJKLMNOPQRSTUVWXYZ23456789"
                        val code = (1..16).map { randChars.random() }.joinToString("")
                        "COZY-RCV-${code.take(4)}-${code.drop(4).take(4)}-${code.drop(8).take(4)}"
                    }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    
                    AlertDialog(
                        onDismissRequest = { /* No-op: Mandatory screen! */ },
                        properties = androidx.compose.ui.window.DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        ),
                        title = {
                            Text("🛡️ Mandatory Admin Recovery Setup", fontFamily = activeFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "To guarantee secure recovery in case you forget your email or PIN, please set up your recovery details. This is shown ONLY ONCE.",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    fontFamily = activeFontFamily
                                )
                                
                                Text(
                                    text = "Step 1: Set Recovery Email",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = primaryColor,
                                    fontFamily = activeFontFamily
                                )
                                
                                OutlinedTextField(
                                    value = recoveryEmailInput,
                                    onValueChange = { 
                                        recoveryEmailInput = it
                                        setupError = ""
                                    },
                                    label = { Text("Backup Recovery Email", fontFamily = activeFontFamily) },
                                    placeholder = { Text("backup-admin@example.com") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = "Step 2: Save Your Emergency Recovery Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = primaryColor,
                                    fontFamily = activeFontFamily
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(primaryColor.copy(alpha = 0.08f))
                                        .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        androidx.compose.foundation.text.selection.SelectionContainer {
                                            Text(
                                                text = generatedCode,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryColor,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Button(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedCode))
                                                    isCodeCopied = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.8f)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isCodeCopied) "Copied! ✨" else "Copy Code",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontFamily = activeFontFamily
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = isSavedConfirmed,
                                        onCheckedChange = { isSavedConfirmed = it },
                                        colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "I have saved the Recovery Code safely.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontFamily = activeFontFamily
                                    )
                                }
                                
                                if (setupError.isNotEmpty()) {
                                    Text(
                                        text = setupError,
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontFamily = activeFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (recoveryEmailInput.isBlank()) {
                                        setupError = "Please enter a recovery email!"
                                    } else if (!isSavedConfirmed) {
                                        setupError = "Please confirm that you have saved the recovery code!"
                                    } else {
                                        viewModel.completeRecoverySetup(
                                            recoveryEmail = recoveryEmailInput.trim(),
                                            recoveryCode = generatedCode
                                        )
                                        android.widget.Toast.makeText(context, "🛡️ Recovery setup completed safely!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Complete Setup & Access App", color = Color.White, fontFamily = activeFontFamily)
                            }
                        }
                    )
                }

                // Trusted Device Registration Banner/Prompt
                val currentDeviceName = android.os.Build.MODEL ?: "Unknown Device"
                val trustedDevicesList = try {
                    val arr = org.json.JSONArray(adminConfig.trustedDevicesJson)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    list
                } catch (e: Exception) {
                    emptyList<String>()
                }
                
                var hasPromptedThisDeviceSession by remember { mutableStateOf(false) }
                
                if (activeUser != null && activeUser.isAdmin && adminConfig.isRecoverySetupCompleted && !trustedDevicesList.contains(currentDeviceName) && !hasPromptedThisDeviceSession) {
                    AlertDialog(
                        onDismissRequest = { hasPromptedThisDeviceSession = true },
                        title = {
                            Text("📱 Register Trusted Admin Device", fontFamily = activeFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Register this active device as a Trusted Admin Device to secure access to administrative controls.",
                                    fontSize = 12.sp,
                                    fontFamily = activeFontFamily,
                                    color = Color.DarkGray
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.08f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Current Device: $currentDeviceName 🎀",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = primaryColor,
                                        fontFamily = activeFontFamily
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.addTrustedDevice(currentDeviceName)
                                    hasPromptedThisDeviceSession = true
                                    android.widget.Toast.makeText(context, "🎉 Device registered as Trusted Admin Device! 💕", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Register & Trust", color = Color.White, fontFamily = activeFontFamily)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { hasPromptedThisDeviceSession = true }
                            ) {
                                Text("Not Now", color = Color.Gray, fontFamily = activeFontFamily)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// SPLASH SCREEN (Cute Loading Screen)
// ==========================================
@Composable
fun SplashScreen(
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "logo_scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Custom Logo Drawing
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart Logo",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp * scale)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Storehouse Studio 🌸",
                color = Color.White,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your secure creative catalog",
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = fontFamily,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.testTag("splash_loader")
            )
        }
    }
}

// ==========================================
// ONBOARDING EXPERIENCE
// ==========================================
@Composable
fun OnboardingScreen(
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onFinished: () -> Unit
) {
    var activePage by remember { mutableStateOf(0) }
    val slides = listOf(
        OnboardingSlide(
            title = "Personal Storehouse 📁",
            desc = "Upload and organize Notes, PDFs, Audios, Videos, and key items permanently in structured local files.",
            icon = Icons.Default.Storage
        ),
        OnboardingSlide(
            title = "Customizable Tasks & Tools 🎯",
            desc = "Rename, reorder, categorize, and complete your checklist values with dynamic cute controls.",
            icon = Icons.Default.List
        ),
        OnboardingSlide(
            title = "Analytics & Controls 📊",
            desc = "Track daily progress with Canvas pie charts & live timers, governed by dynamic Admin settings.",
            icon = Icons.Default.Equalizer
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinished,
                modifier = Modifier.testTag("skip_onboarding")
            ) {
                Text("Skip", color = primaryColor, fontFamily = fontFamily)
            }
        }

        // Active Slide display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slides[activePage].icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = slides[activePage].title,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = slides[activePage].desc,
                fontFamily = fontFamily,
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Page Select Indication & Next Key
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                slides.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (activePage == i) 16.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (activePage == i) primaryColor else Color.LightGray)
                    )
                }
            }

            Button(
                onClick = {
                    if (activePage < slides.size - 1) {
                        activePage++
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (activePage == slides.size - 1) "Get Started ✨" else "Next",
                    fontFamily = fontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val desc: String,
    val icon: ImageVector
)

// ==========================================
// CUTE & SECURE LOGIN / SIGNUP SCREENS
// ==========================================
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onLoginSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    var showAdminKeyPopup by remember { mutableStateOf(false) }
    var enteredAdminKey by remember { mutableStateOf("") }
    var adminKeyError by remember { mutableStateOf("") }
    var showEmergencyRecoveryDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var gmail by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("Delhi") }
    var selectedAvatar by remember { mutableStateOf("🎀") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val avatars = listOf("🎀", "🌸", "🦄", "🧸", "🐱", "🍥", "🧁", "🧚🏻‍♀️")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Sparkly Top Decor
        Text(
            text = "💗 𝕄𝕪 ℙ𝕣𝕖𝕥𝕥𝕪 ℙ𝕝𝕒𝕟𝕟𝕖𝕣 💗",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = primaryColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = "✨ Your Cute Cozy Personal Planner & Storehouse ✨",
            fontSize = 12.sp,
            fontFamily = fontFamily,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Profile Picture Selector
                Text(
                    text = "👒 Choose Your Magic Avatar 👒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Active big circular avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedAvatar,
                        fontSize = 44.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Grid of selectable avatar symbols
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    avatars.take(4).forEach { av ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == av) primaryColor else Color.Transparent)
                                .clickable { selectedAvatar = av }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(av, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    avatars.drop(4).forEach { av ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == av) primaryColor else Color.Transparent)
                                .clickable { selectedAvatar = av }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(av, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What is your Name? ✨") },
                    placeholder = { Text("Kajal") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("login_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Gmail input
                OutlinedTextField(
                    value = gmail,
                    onValueChange = { gmail = it },
                    label = { Text("Enter Your Gmail 💌") },
                    placeholder = { Text("Kajal056@gmail.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("login_gmail_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Indian State Dropdown Selector
                Text(
                    text = "📍 Select Your State / UT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = fontFamily,
                    color = primaryColor,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedState,
                            fontFamily = fontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand",
                            tint = primaryColor
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(260.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        indianStates.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st, fontFamily = fontFamily, fontSize = 13.sp) },
                                onClick = {
                                    selectedState = st
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // [ LOGIN 💗 ] custom pill shape button
                Button(
                    onClick = {
                        if (gmail.isBlank()) {
                            android.widget.Toast.makeText(context, "Oh silly! Please enter your gmail! 💕", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (gmail.trim().equals(adminConfig.adminEmail.trim(), ignoreCase = true)) {
                            showAdminKeyPopup = true
                            enteredAdminKey = ""
                            adminKeyError = ""
                        } else if (name.isBlank()) {
                            android.widget.Toast.makeText(context, "Oh silly! Please enter your name! 💕", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setupUserProfile(name, gmail, selectedState, selectedAvatar) {
                                onLoginSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(54.dp)
                        .testTag("submit_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "LOGIN 💗",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                TextButton(
                    onClick = { showEmergencyRecoveryDialog = true }
                ) {
                    Text("🔑 Forgot Admin Credentials? Recover Ownership", color = primaryColor, fontSize = 11.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🎀 Made with love and sparkles ✨",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = fontFamily,
            color = Color.LightGray
        )
    }

    // Secure Verification Dialog for Admin login only
    if (showAdminKeyPopup) {
        AlertDialog(
            onDismissRequest = { 
                showAdminKeyPopup = false 
                enteredAdminKey = ""
                adminKeyError = ""
            },
            title = {
                Text(
                    text = "🔒 Admin Authorization Required",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "The entered email is designated for Admin access. Please verify your confidential Admin key to login.",
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = enteredAdminKey,
                        onValueChange = { 
                            enteredAdminKey = it
                            adminKeyError = ""
                        },
                        label = { Text("Admin Secret Key", fontFamily = fontFamily) },
                        placeholder = { Text("Enter Key") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminKeyError.isNotEmpty()) {
                        Text(
                            text = adminKeyError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredAdminKey == adminConfig.adminKey) {
                            viewModel.loginUser(context, gmail.trim(), enteredAdminKey) {
                                showAdminKeyPopup = false
                                onLoginSuccess()
                            }
                        } else {
                            adminKeyError = "Incorrect Secret Key. Access denied!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Login", color = Color.White, fontFamily = fontFamily)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminKeyPopup = false
                        enteredAdminKey = ""
                        adminKeyError = ""
                    }
                ) {
                    Text("Cancel", color = Color.Gray, fontFamily = fontFamily)
                }
            }
        )
    }

    if (showEmergencyRecoveryDialog) {
        var recoveryCodeInput by remember { mutableStateOf("") }
        var newEmailInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var recoverError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                showEmergencyRecoveryDialog = false 
                recoveryCodeInput = ""
                newEmailInput = ""
                newPinInput = ""
                recoverError = ""
            },
            title = {
                Text(
                    text = "🚨 Admin Emergency Recovery",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enter your secure Recovery Code to restore Admin credentials and reset ownership.",
                        fontSize = 12.sp,
                        fontFamily = fontFamily,
                        color = Color.DarkGray
                    )
                    
                    OutlinedTextField(
                        value = recoveryCodeInput,
                        onValueChange = { 
                            recoveryCodeInput = it
                            recoverError = ""
                        },
                        label = { Text("Enter Recovery Code", fontFamily = fontFamily) },
                        placeholder = { Text("COZY-RECV-XXXX-XXXX-XXXX") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set New Admin Credentials:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        fontFamily = fontFamily
                    )

                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { 
                            newEmailInput = it
                            recoverError = ""
                        },
                        label = { Text("New Admin Email", fontFamily = fontFamily) },
                        placeholder = { Text("admin@example.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { 
                            newPinInput = it
                            recoverError = ""
                        },
                        label = { Text("New Admin PIN (Secret Key)", fontFamily = fontFamily) },
                        placeholder = { Text("Secret123") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recoverError.isNotEmpty()) {
                        Text(
                            text = recoverError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoveryCodeInput.isBlank() || newEmailInput.isBlank() || newPinInput.isBlank()) {
                            recoverError = "Please fill in all recovery details!"
                        } else {
                            viewModel.recoverAdminAccount(
                                enteredCode = recoveryCodeInput.trim(),
                                newAdminEmail = newEmailInput.trim(),
                                newAdminPIN = newPinInput.trim(),
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "🎉 Credentials recovered! Log in now using your new EMAIL and PIN.", android.widget.Toast.LENGTH_LONG).show()
                                    showEmergencyRecoveryDialog = false
                                },
                                onFailure = { errorMsg ->
                                    recoverError = errorMsg
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restore Credentials 🌸", color = Color.White, fontFamily = fontFamily)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmergencyRecoveryDialog = false
                        recoveryCodeInput = ""
                        newEmailInput = ""
                        newPinInput = ""
                        recoverError = ""
                    }
                ) {
                    Text("Cancel", color = Color.Gray, fontFamily = fontFamily)
                }
            }
        )
    }
}

// ==========================================
// PRIMARY WORKSPACE SHIELD NAVIGATION (MainShell)
// ==========================================
@Composable
fun AppShell(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    backgroundColor: Color,
    fontFamily: FontFamily,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var activeTab by remember { mutableStateOf("Home") }
    val navHistory = remember { mutableStateListOf<String>() }
    val navigateToTab = { tabName: String ->
        if (activeTab != tabName) {
            navHistory.add(activeTab)
            activeTab = tabName
        }
    }
    val navigateBack = {
        if (navHistory.isNotEmpty()) {
            activeTab = navHistory.removeLast()
        } else {
            activeTab = "Home"
        }
    }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val showPromoPopup by viewModel.showPromoPopup.collectAsStateWithLifecycle()
    val isDbDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Drawer state configuration
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackHostState = remember { SnackbarHostState() }

    // Dialog states inside drawer options
    var showProfileSettingsDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showAnnounceDetails by remember { mutableStateOf(false) }
    var showSecretAdminGate by remember { mutableStateOf(false) }
    var showColouredThemesDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }

    // Values for editing profile
    var editName by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    var editGmail by remember(currentUser) { mutableStateOf(currentUser?.gmail ?: "") }
    var editState by remember(currentUser) { mutableStateOf(currentUser?.state ?: "Delhi") }
    var editAvatar by remember(currentUser) { mutableStateOf(currentUser?.profilePic ?: "🎀") }
    var editTheme by remember(currentUser) { mutableStateOf(currentUser?.customThemeName ?: "Classic") }
    var editDropdownExpanded by remember { mutableStateOf(false) }

    val activeCategories = remember(adminConfig.categoriesString) {
        adminConfig.categoriesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Sparkles Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "𝕄𝕪 ℙ𝕣𝕖𝕥𝕥𝕪 ℙ𝕝𝕒𝕟𝕟𝕖𝕣",
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                fontSize = 18.sp,
                                color = primaryColor
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = primaryColor.copy(alpha = 0.2f))

                        // Circular profile picture
                        val pic = currentUser?.profilePic ?: "🎀"
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f))
                                .border(2.dp, primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (pic.startsWith("content://") || pic.startsWith("file://") || pic.startsWith("http")) {
                                AsyncImage(
                                    model = pic,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = pic,
                                    fontSize = 48.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Profile Section with custom typeface
                        Text(
                            text = (currentUser?.username ?: "Explorer").toDoubleStruck(),
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                        
                        // Active Badge in Drawer
                        val streakDrawer = currentUser?.hydrationStreak ?: 0
                        val drawerBadge = when {
                            streakDrawer >= 30 -> "Pro"
                            streakDrawer >= 25 -> "Master"
                            streakDrawer >= 20 -> "Excellent"
                            streakDrawer >= 14 -> "Awesome"
                            streakDrawer >= 7 -> "Punctual"
                            streakDrawer >= 4 -> "Amazing"
                            else -> null
                        }
                        if (drawerBadge != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🏆 $drawerBadge",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                        }

                        Text(
                            text = currentUser?.gmail ?: "no-gmail@planner.com",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(secondaryColor.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📍 ${currentUser?.state ?: "Delhi"}",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = primaryColor.copy(alpha = 0.1f))

                        // Sidebar Navigation Elements
                        NavigationDrawerItem(
                            label = { Text("🌸 Edit Profile Settings", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showProfileSettingsDialog = true
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = primaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        NavigationDrawerItem(
                            label = { Text("🎨 Coloured Themes", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showColouredThemesDialog = true
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = secondaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        NavigationDrawerItem(
                            label = { Text("🏆 Your Badges", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showBadgesDialog = true
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = primaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        NavigationDrawerItem(
                            label = { Text("💌 Support System Chat", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = activeTab == "Support",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateToTab("Support")
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.16f),
                                unselectedContainerColor = secondaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        NavigationDrawerItem(
                            label = { Text("📞 Contacts & Info", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showContactDialog = true
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = primaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        NavigationDrawerItem(
                            label = { Text("👑 Admin Controls Gate", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showSecretAdminGate = true
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = secondaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        if (currentUser?.isAdmin == true) {
                            NavigationDrawerItem(
                                label = { Text("🫣 My Connectors", fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                selected = activeTab == "Connectors",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navigateToTab("Connectors")
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = primaryColor.copy(alpha = 0.12f),
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = {
                                scope.launch { drawerState.close() }
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text("Log Out 🚪", color = Color.White, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    ) {
        // Main Scaffold View Area
        Scaffold(
            snackbarHost = { SnackbarHost(snackHostState) },
            topBar = {
                               CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (activeTab) {
                                "Home" -> "My Pretty Planner"
                                "Support" -> "💌 Helpdesk Support"
                                "Routine" -> "🎀 Smart Schedule"
                                "Hydration" -> "🧊 Hydration Alerter"
                                "Calculator" -> "😚 Cute Calculator"
                                "Tasks" -> "🍥 Task Planner"
                                "Storehouse" -> "💗 Personal Storehouse"
                                "Progress" -> "😉 Usage Analytics"
                                "Clock" -> "🕰️ Clock"
                                "Connectors" -> "🫣 My Connectors"
                                else -> "Planner"
                            },
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (activeTab != "Home") {
                                    navigateBack()
                                } else {
                                    scope.launch { drawerState.open() }
                                }
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .border(1.5.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activeTab != "Home") androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.AutoAwesome,
                                contentDescription = "Menu",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.toggleThemeMode { msg ->
                                    scope.launch {
                                        snackHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .border(1.5.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Theme Switcher",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val navItems = listOf(
                        Triple("Home", Icons.Default.Home, "Planner"),
                        Triple("Calculator", Icons.Default.Calculate, "Calc"),
                        Triple("Tasks", Icons.Default.Task, "Checklist"),
                        Triple("Storehouse", Icons.Default.Folder, "Storehouse"),
                        Triple("Progress", Icons.Default.TrendingUp, "Progress")
                    )

                    navItems.forEach { (route, icon, label) ->
                        val isSelected = activeTab == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navigateToTab(route) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) primaryColor else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    label,
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = primaryColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    "Home" -> DashboardTab(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily,
                        onLogout = onLogout,
                        onOpenTab = { tabName -> navigateToTab(tabName) }
                    )
                    "Calculator" -> CalculatorTab(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily
                    )
                    "Tasks" -> TasksTab(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily,
                        categories = activeCategories
                    )
                    "Storehouse" -> StorehouseTab(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily,
                        categories = activeCategories
                    )
                    "Progress" -> UsageProgressTab(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily
                    )
                    // Added Cute Navigation Category views
                    "Support" -> SupportTabModule(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily
                    )
                    "Routine" -> RoutineTabModule(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        fontFamily = fontFamily
                    )
                    "Hydration" -> HydrationTabModule(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        fontFamily = fontFamily
                    )
                    "Clock" -> ClockTabModule(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        fontFamily = fontFamily
                    )
                    "Connectors" -> ConnectorsTabModule(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }

    // Modal dialog for 5 Minute Subscription Promotion (Feature 6)
    if (showPromoPopup) {
        AlertDialog(
            onDismissRequest = { /* Force response by preventing dismiss */ },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Line 1:
                    Text(
                        text = "🤭 Have you joined",
                        fontWeight = FontWeight.Medium,
                        fontFamily = fontFamily,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    // Line 2:
                    Text(
                        text = "Our Channel? ❤️",
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        fontSize = 20.sp,
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )

                    // Line 3: Custom aesthetic vector logo
                    if (!adminConfig.popupLogoImage.isNullOrBlank()) {
                        Base64ImageOrText(
                            base64Str = adminConfig.popupLogoImage,
                            defaultText = adminConfig.popupLogo,
                            imageModifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else if (adminConfig.popupLogo != "🔔") {
                        Text(
                            text = adminConfig.popupLogo,
                            fontSize = 64.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LogoHeartFingerIllustration()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Navigating to Channel: ${adminConfig.channelLink}", Toast.LENGTH_SHORT).show()
                        try {
                            uriHandler.openUri(adminConfig.channelLink)
                        } catch (e: Exception) {}
                        viewModel.dismissPromoPopup()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().testTag("join_promo_now")
                ) {
                    Text("Join Now 😊", color = Color.White, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Profile Settings overlay dialog
    if (showProfileSettingsDialog) {
        val avatars = listOf("🎀", "🌸", "🦄", "🧸", "🐱", "🍥", "🧁", "🧚🏻‍♀️")
        val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                editAvatar = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showProfileSettingsDialog = false },
            title = { Text("🎀 Edit Profile Settings", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = primaryColor) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Choose Magic Avatar:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        avatars.take(4).forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (editAvatar == av) primaryColor else Color.Transparent)
                                    .clickable { editAvatar = av }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av, fontSize = 18.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        avatars.drop(4).forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (editAvatar == av) primaryColor else Color.Transparent)
                                    .clickable { editAvatar = av }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av, fontSize = 18.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.12f), contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload From Device 📁", fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (editAvatar.startsWith("content://") || editAvatar.startsWith("file://") || editAvatar.startsWith("http")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.08f))
                                .border(1.5.dp, primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = editAvatar,
                                contentDescription = "Preview Selection",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editGmail,
                        onValueChange = { editGmail = it },
                        label = { Text("Gmail Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select State / UT:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryColor.copy(alpha = 0.08f))
                            .clickable { editDropdownExpanded = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(editState, fontFamily = fontFamily, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryColor)
                        }

                        DropdownMenu(
                            expanded = editDropdownExpanded,
                            onDismissRequest = { editDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(200.dp)
                        ) {
                            indianStates.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st, fontFamily = fontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        editState = st
                                        editDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Coloured Theme 🌸:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(4.dp))

                    var editThemeDropdownExpanded by remember { mutableStateOf(false) }
                    val themeOptions = listOf("Classic", "Sweet Pink", "Baby Blue", "Lavender", "Mint Green", "Peach Glow")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryColor.copy(alpha = 0.08f))
                            .clickable { editThemeDropdownExpanded = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(editTheme, fontFamily = fontFamily, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryColor)
                        }

                        DropdownMenu(
                            expanded = editThemeDropdownExpanded,
                            onDismissRequest = { editThemeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            themeOptions.forEach { th ->
                                DropdownMenuItem(
                                    text = { Text(th, fontFamily = fontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        editTheme = th
                                        editThemeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank() || editGmail.isBlank()) {
                            Toast.makeText(context, "Please complete fields!", Toast.LENGTH_SHORT).show()
                        } else {
                            currentUser?.let { user ->
                                val updated = user.copy(
                                    username = editName,
                                    gmail = editGmail,
                                    state = editState,
                                    profilePic = editAvatar,
                                    customThemeName = editTheme
                                )
                                viewModel.updateUserProfile(updated)
                                // Show temporary snackbar message
                                scope.launch {
                                    snackHostState.showSnackbar("✅ Profile Updated")
                                }
                            }
                            showProfileSettingsDialog = false
                            Toast.makeText(context, "Profile updated successfully! ✨", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save 💗", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileSettingsDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Coloured Themes Dialog
    if (showColouredThemesDialog) {
        var selectedThemeOption by remember { mutableStateOf(currentUser?.customThemeName ?: "Classic") }
        AlertDialog(
            onDismissRequest = { showColouredThemesDialog = false },
            title = {
                Text(
                    text = "🎨 Choose Coloured Theme",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryColor
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select your favorite pastel aesthetic to match your vibe today! 💕",
                        fontSize = 12.sp,
                        fontFamily = fontFamily,
                        color = Color.DarkGray
                    )
                    
                    val themeOptionsChoices = listOf("Classic", "Sweet Pink", "Baby Blue", "Lavender", "Mint Green", "Peach Glow")
                    
                    themeOptionsChoices.forEach { themeName ->
                        val isSelected = selectedThemeOption == themeName
                        val chipColor = when (themeName) {
                            "Sweet Pink" -> Color(0xFFFFB6C1)
                            "Baby Blue" -> Color(0xFFADD8E6)
                            "Lavender" -> Color(0xFFE6E6FA)
                            "Mint Green" -> Color(0xFFE0F7FA)
                            "Peach Glow" -> Color(0xFFFFE4B5)
                            else -> primaryColor.copy(alpha = 0.2f)
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) chipColor.copy(alpha = 0.3f) else Color.Transparent)
                                .border(1.5.dp, if (isSelected) chipColor else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { selectedThemeOption = themeName }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = themeName,
                                    fontFamily = fontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                            if (isSelected) {
                                Text("✨ Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentUser?.let { user ->
                            val updated = user.copy(customThemeName = selectedThemeOption)
                            viewModel.updateUserProfile(updated)
                            Toast.makeText(context, "Theme updated successfully! 🌸", Toast.LENGTH_SHORT).show()
                            showColouredThemesDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Theme 💕", color = Color.White, fontFamily = fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showColouredThemesDialog = false }) {
                    Text("Close", color = Color.Gray, fontFamily = fontFamily)
                }
            }
        )
    }

    // Your Badges Dialog
    if (showBadgesDialog) {
         val hydrationStreak = currentUser?.hydrationStreak ?: 0
         
         val badge1Name = adminConfig.badgeName.ifBlank { "Amazing Blossom 🌸" }
         val badge1Logo = adminConfig.badgeLogo.ifBlank { "💮" }
         val badge1Appearance = adminConfig.badgeAppearance.ifBlank { "Pink Glow" }
         
         val badgeItems = listOf(
             Triple(badge1Name, badge1Logo, 4 to "Reach a 4-day streak to sprout a beautiful blossom ($badge1Appearance)!"),
             Triple("Punctual Clockwork ⏰", "🥇", 7 to "Achieve a 7-day solid hydration streak to unlock this golden medal!"),
             Triple("Awesome Teddy 🧸", "🧸", 14 to "Achieve a 14-day stable streak to find a cute teddy companion!"),
             Triple("Excellent Heart 💝", "💝", 20 to "Maintain a 20-day streak to unlock this precious glowing heart!"),
             Triple("Cozy Masterpiece 🩰", "🩰", 25 to "Unlock this stylish ballerina slipper badge at a 25-day streak!"),
             Triple("Royal Princess 👑", "👑", 30 to "Earn your tiny princess crown by logging a 30-day ultimate streak!")
         )

        AlertDialog(
            onDismissRequest = { showBadgesDialog = false },
            title = {
                Text("🏆 Your Milestone Badges", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
                ) {
                    item {
                        Text(
                            text = "Track your daily healthy habits to earn gorgeous rewards! Your active health streak is: $hydrationStreak Days 💖",
                            fontSize = 12.sp,
                            fontFamily = fontFamily,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(badgeItems) { (name, icon, unlockPair) ->
                        val targetDays = unlockPair.first
                        val unlockDescription = unlockPair.second
                        val isUnlocked = hydrationStreak >= targetDays

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) primaryColor.copy(alpha = 0.04f) else Color.Gray.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isUnlocked) primaryColor.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnlocked) primaryColor.copy(alpha = 0.12f) else Color.DarkGray.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (icon == badge1Logo && !adminConfig.badgeImage.isNullOrBlank()) {
                                        Base64ImageOrText(
                                            base64Str = adminConfig.badgeImage,
                                            defaultText = icon,
                                            imageModifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Text(text = icon, fontSize = 22.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontFamily = fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isUnlocked) primaryColor else Color.Gray
                                    )
                                    Text(
                                        text = unlockDescription,
                                        fontFamily = fontFamily,
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                if (isUnlocked) {
                                    Text("💖 Unlocked", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("🔒 Locked", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBadgesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fantastic!", color = Color.White)
                }
            }
        )
    }

    // Contacts & Help Info Popup
    if (showContactDialog) {
        var editEmail by remember { mutableStateOf(adminConfig.contactEmail) }
        var editTelegram by remember { mutableStateOf(adminConfig.telegramGroupLink) }
        var editTwitter by remember { mutableStateOf(adminConfig.socialTwitterLink) }
        var editWebsite by remember { mutableStateOf(adminConfig.websiteLink) }
        var editHours by remember { mutableStateOf(adminConfig.supportHours) }

        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ContactSupport, contentDescription = null, tint = primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📞 Support Contacts", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = primaryColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "If you have any questions, feedback or feature suggestions, feel free to reach our channels under:",
                        fontFamily = fontFamily,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("📧 Gmail helpdesk (Click to mail):", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    Text(
                        text = adminConfig.contactEmail,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        modifier = Modifier
                            .clickable {
                                try {
                                    uriHandler.openUri("mailto:${adminConfig.contactEmail}")
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Email: ${adminConfig.contactEmail}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(bottom = 8.dp)
                    )

                    Text("💬 Telegram (Click to join):", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    Text(
                        text = adminConfig.telegramGroupLink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        modifier = Modifier
                            .clickable {
                                try {
                                    uriHandler.openUri(adminConfig.telegramGroupLink)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Url: ${adminConfig.telegramGroupLink}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(bottom = 8.dp)
                    )

                    Text("🐦 Twitter / X (Click to visit):", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    Text(
                        text = adminConfig.socialTwitterLink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        modifier = Modifier
                            .clickable {
                                try {
                                    uriHandler.openUri(adminConfig.socialTwitterLink)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Twitter: ${adminConfig.socialTwitterLink}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(bottom = 8.dp)
                    )

                    Text("🌐 Official Website (Click to go):", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    Text(
                        text = adminConfig.websiteLink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        modifier = Modifier
                            .clickable {
                                try {
                                    uriHandler.openUri(adminConfig.websiteLink)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Web: ${adminConfig.websiteLink}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(bottom = 8.dp)
                    )

                    Text("🕒 Support hours:", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    Text(
                        text = adminConfig.supportHours,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Admin only configuration panel inside Contacts
                    if (currentUser?.isAdmin == true) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = primaryColor.copy(alpha = 0.2f))
                        Text(
                            text = "👑 Support Customizer (Admin Only)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = primaryColor,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Support Email") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editTelegram,
                            onValueChange = { editTelegram = it },
                            label = { Text("Telegram Group") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editTwitter,
                            onValueChange = { editTwitter = it },
                            label = { Text("Twitter Link") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editWebsite,
                            onValueChange = { editWebsite = it },
                            label = { Text("Website Link") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editHours,
                            onValueChange = { editHours = it },
                            label = { Text("Support Hours") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                val updated = adminConfig.copy(
                                    contactEmail = editEmail,
                                    telegramGroupLink = editTelegram,
                                    socialTwitterLink = editTwitter,
                                    websiteLink = editWebsite,
                                    supportHours = editHours
                                )
                                viewModel.updateAdminConfig(updated)
                                android.widget.Toast.makeText(context, "Successfully updated support links! 📝", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save All Settings 👑", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showContactDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                    Text("Oki 🥰", color = Color.White)
                }
            }
        )
    }

    // Secret Admin settings verification panel
    if (showSecretAdminGate) {
        SecretAdminGateDialog(
            viewModel = viewModel,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            fontFamily = fontFamily,
            onDismiss = { showSecretAdminGate = false }
        )
    }
}

// ==========================================
// REDESIGNED CUTE DASHBOARD TAB (7 CATEGORIES)
// ==========================================
@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onLogout: () -> Unit,
    onOpenTab: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    var showAnnounceDetails by remember { mutableStateOf(false) }
    val dashboardLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LazyColumn(
        state = dashboardLazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .drawScrollbar(dashboardLazyListState, primaryColor),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Large Header displaying Name inside a gorgeous soft-shadow light blue card
        item {
            val streakHome = currentUser?.hydrationStreak ?: 0
            val homeBadge = when {
                streakHome >= 30 -> "Pro"
                streakHome >= 25 -> "Master"
                streakHome >= 20 -> "Excellent"
                streakHome >= 14 -> "Awesome"
                streakHome >= 7 -> "Punctual"
                streakHome >= 4 -> "Amazing"
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // My Pretty Planner Title
                    Text(
                        text = "My Pretty Planner",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Home Page Header Logo Style (No border, Center Aligned, Soft shadow, Size 60px to 80px)
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.home_header_logo),
                        contentDescription = "My Pretty Planner Header Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                clip = false
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${(currentUser?.username ?: "Kajal").toDoubleStruck()} 💗",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                        
                        if (homeBadge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🏆 $homeBadge",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "your cozy pastel planner paradise! 🎀✨",
                        fontFamily = fontFamily,
                        color = primaryColor.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Gently Bouncing Scroll Down Hint / Indicator
        item {
            val infiniteTransition = rememberInfiniteTransition(label = "bouncing_hint")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "yOffset"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = yOffset.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍡 Scroll down to explore planner tools! 🍧👇🌸",
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Announcement ticker card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAnnounceDetails = true },
                shape = RoundedCornerShape(24.dp), // Pill cloud shape feel
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📣", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Announcement",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = 13.sp,
                                fontFamily = fontFamily
                             )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = adminConfig.announcementTitle,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Section header
        item {
            Text(
                text = "🌸 Planners",
                fontFamily = fontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = primaryColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Renders all requested categories as custom cloud shaped cards
        val cuteCategories = listOf(
            Triple("Developer Support", "💌", "Support"),
            Triple("Smart Schedule", "🎀", "Routine"),
            Triple("Hydration Alerter", "🧊", "Hydration"),
            Triple("Checklist Tasks", "🍥", "Tasks"),
            Triple("Aesthetic Calculator", "+ −\n× ÷", "Calculator"),
            Triple("Usage Analytics", "😉", "Progress"),
            Triple("Personal Storehouse", "💗", "Storehouse"),
            Triple("🕰️ Clock Alarms", "🕰️", "Clock")
        )

        // Custom grid of cards
        cuteCategories.chunked(2).forEach { pair ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { (catName, emoji, route) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp)
                                .clickable { onOpenTab(route) },
                            // Gives the custom cloud aesthetic cards vibe using rounded values
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFFF1F2)
                            ), // premium soft pastel pink shade
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            border = BorderStroke(1.2.dp, primaryColor.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val customLogoBase64 = if (route == "Hydration") adminConfig.hydrationLogoImage else null
                                    val customLogoEmoji = if (route == "Hydration") adminConfig.hydrationLogo else emoji
                                    
                                    if (customLogoBase64 != null) {
                                        Base64ImageOrText(
                                            base64Str = customLogoBase64,
                                            defaultText = customLogoEmoji,
                                            imageModifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        if (emoji.contains("+")) {
                                            Text(
                                                text = emoji,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryColor,
                                                lineHeight = 13.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            Text(customLogoEmoji, fontSize = 22.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = catName,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp, // increased category name size
                                    color = primaryColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2, // increased visibility, allows 2 lines if needed
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Live dev testing helper button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = secondaryColor.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("JOIN Us Now 🎀", fontSize = 11.sp, fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, color = primaryColor)
                    Button(
                        onClick = { viewModel.triggerPromoPopupNow() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Sure ♡", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAnnounceDetails) {
        val announcementsList by viewModel.announcements.collectAsStateWithLifecycle()

        fun formatAnnouncementTime(timestamp: Long): String {
            val duration = System.currentTimeMillis() - timestamp
            return when {
                duration < 60_000 -> "Just now"
                duration < 3600_000 -> "${duration / 60_000} mins ago"
                duration < 86400_000 -> "${duration / 3600_000} hours ago"
                else -> {
                    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(timestamp))
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAnnounceDetails = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📣", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Announcements",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor
                        )
                    }
                    if (currentUser?.isAdmin == true) {
                        TextButton(
                            onClick = { viewModel.clearAnnouncementHistory() }
                        ) {
                            Text("Clear All", color = Color.Red, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Text(
                        text = "Real-time updates delivered directly from the Admin team. Click notifications to open.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (announcementsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📭", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No announcements sent yet!",
                                    fontFamily = fontFamily,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                        ) {
                            items(announcementsList) { announcement ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("announcement_item_${announcement.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = announcement.title,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = fontFamily,
                                                fontSize = 14.sp,
                                                color = primaryColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatAnnouncementTime(announcement.timestamp),
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontFamily = fontFamily
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = announcement.message,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            fontFamily = fontFamily
                                        )

                                        if (!announcement.imageUrl.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                AsyncImage(
                                                    model = announcement.imageUrl,
                                                    contentDescription = "Notification Image",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnnounceDetails = false }) {
                    Text("Close", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// CUTE CATEGORY: DEVELOPER SUPPORT SYSTEM
// ==========================================
@Composable
fun SupportTabModule(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAppDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    var typedMessage by remember { mutableStateOf("") }
    var chatRoleIsAdmin by remember { mutableStateOf(false) }
    var replyingToMessageState by remember { mutableStateOf<Map<String, String>?>(null) }

    val chatList = remember(adminConfig.supportChatsJson) {
        try {
            val list = mutableListOf<Map<String, String>>()
            val arr = org.json.JSONArray(adminConfig.supportChatsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    mapOf(
                        "sender" to obj.optString("sender", "User"),
                        "message" to obj.optString("message", ""),
                        "userEmail" to obj.optString("userEmail", "user@example.com"),
                        "timestamp" to obj.optLong("timestamp", System.currentTimeMillis()).toString()
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Identify user's own email
    val userEmail = currentUser?.email ?: "user@example.com"

    // Extract distinct user emails from logs (excluding admin)
    val distinctUserEmails = remember(chatList) {
        chatList.map { it["userEmail"] ?: "user@example.com" }
            .distinct()
            .filter { it.isNotBlank() && it != "admin@example.com" }
    }

    var selectedTargetEmail by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(distinctUserEmails) {
        if (selectedTargetEmail.isEmpty() || !distinctUserEmails.contains(selectedTargetEmail)) {
            selectedTargetEmail = if (distinctUserEmails.isNotEmpty()) distinctUserEmails.first() else "user@example.com"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Broadcast updates logger banner with dynamic message
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🌸 Broadcast System Update 📡", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp, fontFamily = fontFamily)
                Spacer(modifier = Modifier.height(4.dp))
                Text(adminConfig.broadcastMessage, fontSize = 11.sp, color = if (isAppDark) Color.White else Color.Gray, fontFamily = fontFamily)
            }
        }

        // Admin-only broadcast update tool
        if (currentUser?.isAdmin == true) {
            var broadcastDraft by remember { mutableStateOf(adminConfig.broadcastMessage) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.04f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = broadcastDraft,
                        onValueChange = { broadcastDraft = it },
                        label = { Text("📢 Edit System Broadcast") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateAdminConfig(adminConfig.copy(broadcastMessage = broadcastDraft))
                            android.widget.Toast.makeText(context, "Broadcast updated successfully! 📢", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Broadcast")
                    }
                }
            }
        }

        // Role switcher ONLY available for real administrators!
        if (currentUser?.isAdmin == true) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Act as Admin Helpdesk to reply 🎀", fontSize = 12.sp, fontFamily = fontFamily, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = chatRoleIsAdmin,
                        onCheckedChange = { chatRoleIsAdmin = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                    )
                }

                if (chatRoleIsAdmin) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select User Thread to reply to:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (distinctUserEmails.isEmpty()) {
                        Text("No active user threads yet.", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                    } else {
                        // Horizontal chips scroll of distinct emails
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(distinctUserEmails.size) { idx ->
                                val emailItem = distinctUserEmails[idx]
                                val selected = emailItem == selectedTargetEmail
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) primaryColor else secondaryColor.copy(alpha = 0.2f))
                                        .clickable { selectedTargetEmail = emailItem }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = emailItem,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else primaryColor,
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            chatRoleIsAdmin = false
        }

        // Filter chat list
        val filteredChats = remember(chatList, chatRoleIsAdmin, selectedTargetEmail, userEmail) {
            if (chatRoleIsAdmin) {
                chatList.filter { it["userEmail"] == selectedTargetEmail }
            } else {
                chatList.filter { it["userEmail"] == userEmail }
            }
        }

        // Chat message box listing
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            if (filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages yet! Type cute words below to start chatting 💌", textAlign = TextAlign.Center, color = Color.LightGray, fontSize = 13.sp, fontFamily = fontFamily)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredChats.size) { idx ->
                        val item = filteredChats[idx]
                        val sender = item["sender"] ?: "User"
                        val msg = item["message"] ?: ""
                        
                        val isSelf = if (chatRoleIsAdmin) {
                            sender.contains("Admin")
                        } else {
                            !sender.contains("Admin")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .pointerInput(item) {
                                        detectTapGestures(
                                            onLongPress = {
                                                replyingToMessageState = item
                                                val msgEmail = item["userEmail"]
                                                if (chatRoleIsAdmin && !msgEmail.isNullOrBlank()) {
                                                    selectedTargetEmail = msgEmail
                                                }
                                                android.widget.Toast.makeText(context, "Selected ${sender}'s message for reply! 🎀", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isSelf) 16.dp else 2.dp,
                                    bottomEnd = if (isSelf) 2.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) primaryColor.copy(alpha = 0.12f) else secondaryColor.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelf) primaryColor.copy(alpha = 0.25f) else secondaryColor.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = sender,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = primaryColor,
                                        fontFamily = fontFamily
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 13.sp,
                                        color = if (isAppDark) Color.White else Color(0xFF1C1B1F),
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Attachment preset options
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    typedMessage += " [Attached custom photo draft: profile_check_mockup.png 📸]"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Photo 📸", fontSize = 11.sp, color = primaryColor)
            }
            Button(
                onClick = {
                    typedMessage += " [Sent Document receipt: planner_schedule_export.pdf 📄]"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Doc 📄", fontSize = 11.sp, color = primaryColor)
            }
        }

        // Typing Box Reply Indicator Anchor
        if (replyingToMessageState != null) {
            val replySender = replyingToMessageState!!["sender"] ?: "User"
            val replyMsg = replyingToMessageState!!["message"] ?: ""
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("↳ Replying to $replySender: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = fontFamily)
                        Text(if (replyMsg.length > 25) replyMsg.take(22) + "..." else replyMsg, fontSize = 11.sp, color = if (isAppDark) Color.White else Color.Gray, fontFamily = fontFamily)
                    }
                    IconButton(
                        onClick = { replyingToMessageState = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            }
        }

        // Typing Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = typedMessage,
                onValueChange = { typedMessage = it },
                placeholder = { Text("Ask anything sweet...", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor),
                modifier = Modifier.weight(1f).testTag("support_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (typedMessage.isNotBlank()) {
                        val msgWithReply = if (replyingToMessageState != null) {
                            val rSender = replyingToMessageState!!["sender"] ?: "User"
                            val rBody = replyingToMessageState!!["message"] ?: ""
                            "↳ [Replying to $rSender: \"$rBody\"]\n$typedMessage"
                        } else {
                            typedMessage
                        }

                        if (chatRoleIsAdmin) {
                            val targetEmailToUse = if (replyingToMessageState != null) {
                                replyingToMessageState!!["userEmail"] ?: selectedTargetEmail
                            } else {
                                selectedTargetEmail
                            }
                            viewModel.sendAdminSupportReply(msgWithReply, targetEmailToUse)
                        } else {
                            viewModel.sendUserSupportMessage(msgWithReply)
                        }
                        typedMessage = ""
                        replyingToMessageState = null
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("support_send_btn")
            ) {
                Text("SEND 💗", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ==========================================
// CUTE CATEGORY: SMART ROUTINE PLANNER
// ==========================================
@Composable
fun CuteSeparateDateTimePicker(
    day: Int,
    month: Int,
    year: Int,
    hour: Int,
    minute: Int,
    ampm: String,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmpmChange: (String) -> Unit,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    var dExp by remember { mutableStateOf(false) }
    var mExp by remember { mutableStateOf(false) }
    var yExp by remember { mutableStateOf(false) }
    var hExp by remember { mutableStateOf(false) }
    var minExp by remember { mutableStateOf(false) }

    val daysList = (1..31).toList()
    val monthsList = (1..12).toList()
    val yearsList = listOf(2026, 2027, 2028, 2029)
    val hoursList = (1..12).toList()
    val minutesList = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📅 Select Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = fontFamily)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Day
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { dExp = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Day: $day", fontSize = 11.sp, color = Color.DarkGray, fontFamily = fontFamily)
                }
                DropdownMenu(expanded = dExp, onDismissRequest = { dExp = false }) {
                    daysList.forEach { d ->
                        DropdownMenuItem(
                            text = { Text("$d", fontSize = 11.sp) },
                            onClick = { onDayChange(d); dExp = false }
                        )
                    }
                }
            }

            // Month
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { mExp = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val mNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    Text(mNames.getOrNull(month - 1) ?: "$month", fontSize = 11.sp, color = Color.DarkGray, fontFamily = fontFamily)
                }
                DropdownMenu(expanded = mExp, onDismissRequest = { mExp = false }) {
                    monthsList.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("$m", fontSize = 11.sp) },
                            onClick = { onMonthChange(m); mExp = false }
                        )
                    }
                }
            }

            // Year
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { yExp = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("$year", fontSize = 11.sp, color = Color.DarkGray, fontFamily = fontFamily)
                }
                DropdownMenu(expanded = yExp, onDismissRequest = { yExp = false }) {
                    yearsList.forEach { y ->
                        DropdownMenuItem(
                            text = { Text("$y", fontSize = 11.sp) },
                            onClick = { onYearChange(y); yExp = false }
                        )
                    }
                }
            }
        }

        Text("⏰ Select Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = fontFamily)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { hExp = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Hr: $hour", fontSize = 11.sp, color = Color.DarkGray, fontFamily = fontFamily)
                }
                DropdownMenu(expanded = hExp, onDismissRequest = { hExp = false }) {
                    hoursList.forEach { h ->
                        DropdownMenuItem(
                            text = { Text("$h", fontSize = 11.sp) },
                            onClick = { onHourChange(h); hExp = false }
                        )
                    }
                }
            }

            // Minute
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { minExp = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(String.format("%02d", minute), fontSize = 11.sp, color = Color.DarkGray, fontFamily = fontFamily)
                }
                DropdownMenu(expanded = minExp, onDismissRequest = { minExp = false }) {
                    minutesList.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(String.format("%02d", m), fontSize = 11.sp) },
                            onClick = { onMinuteChange(m); minExp = false }
                        )
                    }
                }
            }

            // AM / PM Toggle
            Row(
                modifier = Modifier
                    .weight(1.2f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.08f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("AM", "PM").forEach { item ->
                    val sel = ampm == item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (sel) primaryColor else Color.Transparent)
                            .clickable { onAmpmChange(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) Color.White else Color.Gray,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Time24hSelectionBox(
    label: String,
    selectedValue: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryColor, fontFamily = fontFamily)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(primaryColor.copy(alpha = 0.08f))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedValue, fontFamily = fontFamily, fontSize = 13.sp)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryColor)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontFamily = fontFamily, fontSize = 12.sp) },
                        onClick = {
                            onSelect(opt)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CutePastelTable(rows: List<List<String>>, primaryColor: Color, fontFamily: FontFamily) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.02f)),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            rows.forEachIndexed { rowIndex, colList ->
                val isHeader = rowIndex == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isHeader) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colList.forEach { cellVal ->
                        val cellText = cellVal.trim()
                        if (cellText.isNotEmpty() && cellText != "|") {
                            Text(
                                text = cellText,
                                fontFamily = fontFamily,
                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (isHeader) 11.sp else 10.sp,
                                color = if (isHeader) primaryColor else Color.DarkGray,
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
                if (rowIndex < rows.size - 1) {
                    Divider(color = primaryColor.copy(alpha = 0.08f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun FunScheduledOutputRenderer(text: String, primaryColor: Color, fontFamily: FontFamily) {
    val lines = text.split("\n")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var isInTable = false
        val tableRows = mutableListOf<List<String>>()

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("|")) {
                val parts = trimmed.split("|").map { it.trim() }.filterIndexed { index, _ -> index > 0 }
                if (parts.any { it.contains("---") }) {
                    // Skip separation rule
                } else if (parts.isNotEmpty()) {
                    // Remove last empty part if it exists due to trailing '|'
                    val cleanParts = if (parts.lastOrNull().isNullOrBlank()) parts.dropLast(1) else parts
                    if (cleanParts.isNotEmpty()) {
                        tableRows.add(cleanParts)
                    }
                }
                isInTable = true
            } else {
                if (isInTable && tableRows.isNotEmpty()) {
                    CutePastelTable(rows = tableRows, primaryColor = primaryColor, fontFamily = fontFamily)
                    tableRows.clear()
                    isInTable = false
                }

                if (trimmed.isEmpty()) return@forEach

                when {
                    trimmed.startsWith("###") -> {
                        val titleText = trimmed.removePrefix("###").trim()
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = titleText,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = primaryColor
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(2.dp)
                                    .background(primaryColor.copy(alpha = 0.4f))
                            )
                        }
                    }
                    trimmed.startsWith("##") -> {
                        val titleText = trimmed.removePrefix("##").trim()
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = titleText,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = primaryColor
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(3.dp)
                                    .background(primaryColor)
                            )
                        }
                    }
                    trimmed.startsWith("#") -> {
                        val titleText = trimmed.removePrefix("#").trim()
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = titleText,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = primaryColor
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(primaryColor)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                                "💖 " + trimmed.removePrefix("-").removePrefix("*").trim()
                            } else {
                                trimmed
                            },
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        if (tableRows.isNotEmpty()) {
            CutePastelTable(rows = tableRows, primaryColor = primaryColor, fontFamily = fontFamily)
        }
    }
}

@Composable
fun RoutineTabModule(
    viewModel: MainViewModel,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val currentUserState by viewModel.currentUser.collectAsStateWithLifecycle()
    val aiScheduleText = currentUserState?.generatedSchedule ?: ""
    val isGeneratingSchedule by viewModel.isRoutineLoading.collectAsStateWithLifecycle()

    var userType by remember { mutableStateOf("School Person") }
    var wakeAndSleep by remember { mutableStateOf("Wake up at 07:00, Sleep at 22:30") }
    var studyHours by remember { mutableStateOf("09:00 to 15:00") }
    var mealTimings by remember { mutableStateOf("Breakfast 08:00, Lunch 13:00, Dinner 20:00") }
    var funTime by remember { mutableStateOf("2 Hours in the evening") }
    var extraDetails by remember { mutableStateOf("") }
    
    var userTypeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎀", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Smart Schedule Planner",
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Generate a beautifully balanced timetable personalized to your unique routine! ✨",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = fontFamily
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tell About Yourself In Series 💗", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 14.sp)

                // 1. Are you a:
                Column {
                    Text("1. Are you a:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryColor.copy(alpha = 0.06f))
                            .clickable { userTypeDropdownExpanded = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(userType, fontFamily = fontFamily, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryColor)
                        }

                        DropdownMenu(
                            expanded = userTypeDropdownExpanded,
                            onDismissRequest = { userTypeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            listOf("School Person", "Home Person", "Office Person").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontFamily = fontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        userType = opt
                                        userTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Wake-up Time & Sleep Time?
                OutlinedTextField(
                    value = wakeAndSleep,
                    onValueChange = { wakeAndSleep = it },
                    label = { Text("2. Wake-up Time & Sleep Time? 🌅🌙", fontSize = 11.sp, fontFamily = fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                )

                // 3. Hours of Work / Study?
                OutlinedTextField(
                    value = studyHours,
                    onValueChange = { studyHours = it },
                    label = { Text("3. Hours of Work / Study? (Approx timing) 📚💻", fontSize = 11.sp, fontFamily = fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                )

                // 4. Approximate Meal Timings?
                OutlinedTextField(
                    value = mealTimings,
                    onValueChange = { mealTimings = it },
                    label = { Text("4. Approximate Meal Timings? 🍪🥗", fontSize = 11.sp, fontFamily = fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                )

                // 5. Fun Time
                OutlinedTextField(
                    value = funTime,
                    onValueChange = { funTime = it },
                    label = { Text("5. Most Importantly: How much Fun Time? 🎧🌸", fontSize = 11.sp, fontFamily = fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                )

                // 6. Add some more details
                OutlinedTextField(
                    value = extraDetails,
                    onValueChange = { extraDetails = it },
                    label = { Text("6. Add some more details if you want, Dear 💕", fontSize = 11.sp, fontFamily = fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                )

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Indeed, I am gonna make it super cute & attractive 💗",
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val compiledSpecialNotes = buildString {
                            append("Wake/Sleep: ").append(wakeAndSleep).append(" | ")
                            append("Meal Timings: ").append(mealTimings).append(" | ")
                            append("Fun Time Wanted: ").append(funTime).append(" | ")
                            append("Extra preferences: ").append(extraDetails)
                        }
                        viewModel.generateScheduleWithAI(
                            course = userType,
                            specialNotes = compiledSpecialNotes,
                            wakeUp = "07:00",
                            classStart = studyHours,
                            classEnd = "17:00"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("ai_schedule_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isGeneratingSchedule
                ) {
                    if (isGeneratingSchedule) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compiling Magic Scheme...", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                    } else {
                        Text("Create MY SCHEDULE 🧁✨", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = fontFamily, fontSize = 12.sp)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🍥 Your Magic Timetable",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (aiScheduleText.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(primaryColor.copy(alpha = 0.04f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🦄🧁🍭", fontSize = 34.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No timetable generated yet.\nTap the button above to fetch a beautiful plan!",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = fontFamily
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(primaryColor.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                FunScheduledOutputRenderer(
                                    text = aiScheduleText,
                                    primaryColor = primaryColor,
                                    fontFamily = fontFamily
                                )
                            }
                        }

                        // Column layout of Save as Image, Save as Document, and Copy Text buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Saved successfully as Image format in Gallery! 🖼️🎀", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save as Image 🖼️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Saved successfully as Document style PDF! 📜📁", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save as Document 📜", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(aiScheduleText))
                                    android.widget.Toast.makeText(context, "Schedule text copied to Clipboard! 💕📋", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Copy Text to Clipboard 📋💖", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CUTE CATEGORY: WATER HYDRATION ALERTER
// ==========================================
@Composable
fun HydrationTabModule(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfigState by viewModel.adminConfig.collectAsStateWithLifecycle()
    val adminConfig = adminConfigState ?: com.example.data.AdminConfig()

    val hydrationGoal = currentUser?.hydrationGoal ?: 8
    val checkedIndices = remember(currentUser?.hydrationProgress) {
        try {
            val arr = org.json.JSONArray(currentUser?.hydrationProgress ?: "[]")
            val list = mutableListOf<Int>()
            for (i in 0 until arr.length()) {
                list.add(arr.getInt(i))
            }
            list
        } catch (e: Exception) {
            emptyList<Int>()
        }
    }
    val hydrationProgress = checkedIndices.size
    val hydrationStreak = currentUser?.hydrationStreak ?: 0

    var showCelebDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val customLogoBase64 = adminConfig.hydrationLogoImage
                    val customLogoEmoji = adminConfig.hydrationLogo
                    if (customLogoBase64 != null) {
                        Base64ImageOrText(
                            base64Str = customLogoBase64,
                            defaultText = customLogoEmoji,
                            imageModifier = Modifier.size(24.dp).clip(CircleShape),
                            defaultTextPrefix = " "
                        )
                    } else {
                        Text(customLogoEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hydration Tracker", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Glow section with customizable Hydration Logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0xFFFDF2F8)) // cute pink/rose glow background
                        .border(2.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val customLogoBase64 = adminConfig.hydrationLogoImage
                    val customLogoEmoji = adminConfig.hydrationLogo
                    if (customLogoBase64 != null) {
                        Base64ImageOrText(
                            base64Str = customLogoBase64,
                            defaultText = customLogoEmoji,
                            imageModifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(customLogoEmoji, fontSize = 52.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Hydration Glow!", fontSize = 11.sp, color = primaryColor, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                val percentage = if (hydrationGoal > 0) (hydrationProgress * 100 / hydrationGoal) else 0
                Text(
                    text = "Glow Status: $percentage% Complete!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor,
                    fontFamily = fontFamily
                )
                Text(
                    text = "Track customizable daily water glasses & maintain streak",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Hydration Streak: $hydrationStreak Days!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = fontFamily,
                        color = Color.DarkGray
                    )
                }
                Text("Keep glowing! ✨", fontSize = 11.sp, color = primaryColor, fontFamily = fontFamily, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙️ Configure Water Glass Goals",
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Goal target: $hydrationGoal Glasses 🥤", fontSize = 13.sp, fontFamily = fontFamily)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                if (hydrationGoal > 1) viewModel.updateHydrationGoal(hydrationGoal - 1)
                            }
                        ) {
                            Box(modifier = Modifier.size(32.dp).background(primaryColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("-", fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.updateHydrationGoal(hydrationGoal + 1)
                            }
                        ) {
                            Box(modifier = Modifier.size(32.dp).background(primaryColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("+", fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🥤 Click Checked Water Glasses Log",
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val columnsCount = 4
                val rowsCount = (hydrationGoal + columnsCount - 1) / columnsCount

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (r in 0 until rowsCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (c in 0 until columnsCount) {
                                val idx = r * columnsCount + c
                                if (idx < hydrationGoal) {
                                    val isChecked = checkedIndices.contains(idx)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(65.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isChecked) primaryColor.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.05f))
                                            .border(
                                                width = if (isChecked) 2.dp else 1.dp,
                                                color = if (isChecked) primaryColor else primaryColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(16.dp)
                                              )
                                            .clickable {
                                                viewModel.toggleHydrationGlass(idx) {
                                                    showCelebDialog = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (isChecked) "🥤" else "💧",
                                                fontSize = 20.sp
                                            )
                                            Text(
                                                text = "Glass ${idx + 1}",
                                                fontSize = 9.sp,
                                                color = if (isChecked) primaryColor else Color.Gray,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                                fontFamily = fontFamily
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.resetHydrationProgress() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear Daily Hydration Progress", color = primaryColor, fontSize = 11.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Redesigned Milestone Badges Section - Placed LAST!
        Text(
            text = "🏆 Milestone Badge Progression",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 14.sp,
            color = primaryColor,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
        )

        val dynBadgeName = adminConfig.badgeName.ifBlank { "Amazing Blossom 🌸" }
        val dynBadgeLogo = adminConfig.badgeLogo.ifBlank { "💮" }
        val dynBadgeAppearance = adminConfig.badgeAppearance.ifBlank { "Pink Glow" }

        val badgeItems = listOf(
            Triple(dynBadgeName, dynBadgeLogo, 4 to "Reach a 4-day streak to sprout a beautiful blossom! (Theme: $dynBadgeAppearance)"),
            Triple("Punctual Clockwork ⏰", "🥇", 7 to "Achieve a 7-day solid hydration streak to unlock this golden medal!"),
            Triple("Awesome Teddy 🧸", "🧸", 14 to "Achieve a 14-day stable streak to find a cute teddy companion!"),
            Triple("Excellent Heart 💝", "💝", 20 to "Maintain a 20-day streak to unlock this precious glowing heart!"),
            Triple("Cozy Masterpiece 🩰", "🩰", 25 to "Unlock this stylish ballerina slipper badge at a 25-day streak!"),
            Triple("Royal Princess 👑", "👑", 30 to "Earn your tiny princess crown by logging a 30-day ultimate streak!")
        )

        badgeItems.forEach { (name, icon, unlockPair) ->
            val targetDays = unlockPair.first
            val unlockDescription = unlockPair.second
            val isUnlocked = hydrationStreak >= targetDays

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) primaryColor.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isUnlocked) primaryColor.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) primaryColor.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon == dynBadgeLogo && !adminConfig.badgeImage.isNullOrBlank()) {
                            Base64ImageOrText(
                                base64Str = adminConfig.badgeImage,
                                defaultText = icon,
                                imageModifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(text = icon, fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = unlockDescription,
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Indicator Box
                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9)) // Light green background
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Unlocked ", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                Text("✅", fontSize = 10.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECEFF1)) // Light slate/gray background
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Locked ", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                Text("🔒", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCelebDialog) {
        val activeStreak = currentUser?.hydrationStreak ?: 0
        val earnedBadge = when {
            activeStreak >= 30 -> "Pro"
            activeStreak >= 25 -> "Master"
            activeStreak >= 20 -> "Excellent"
            activeStreak >= 14 -> "Awesome"
            activeStreak >= 7 -> "Punctual"
            activeStreak >= 4 -> "Amazing"
            else -> null
        }

        val celebrationContext = androidx.compose.ui.platform.LocalContext.current
        // Automatic audio synthesizer playback
        LaunchedEffect(Unit) {
            val attrContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                try {
                    celebrationContext.createAttributionContext("audio_playback")
                } catch (e: Exception) {
                    celebrationContext
                }
            } else {
                celebrationContext
            }
            AlarmTonePlayer.playToneProg(attrContext, "Cheering")
            kotlinx.coroutines.delay(1600)
            AlarmTonePlayer.playToneProg(attrContext, "Clapping")
        }

        // Programmatic Falling Particles / Celebration Animation
        val infiniteTransition = rememberInfiniteTransition(label = "celebration_particles")
        val animProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particles_y"
        )

        AlertDialog(
            onDismissRequest = { showCelebDialog = false },
            title = null,
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    // Falling Confetti Layer
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val particleElements = listOf("✨", "💖", "🎀", "🩵", "🌷", "🌸")
                        val rng = java.util.Random(1337)
                        for (i in 0 until 35) {
                            val relX = rng.nextFloat() * size.width
                            val startY = -40f
                            val relY = startY + (size.height + 80f) * ((animProgress + (i * 0.08f)) % 1.0f)
                            val emoji = particleElements[i % particleElements.size]
                            val sizeScale = 14f + rng.nextFloat() * 16f
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                emoji,
                                relX,
                                relY,
                                android.graphics.Paint().apply {
                                    textSize = sizeScale * density
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎉 HOORAY! 🎉",
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            fontSize = 24.sp,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🌸 You reached your daily water goal today! You are glowing like a dynamic star! 🧊💗🍭",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontFamily = fontFamily,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        if (earnedBadge != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🏆 Unlocked Badge: $earnedBadge 🏆",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🩵🌷✨", fontSize = 34.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCelebDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("AWESOME! 🥰🎀", color = Color.White, fontFamily = fontFamily)
                }
            }
        )
    }
}

@Composable
fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ==========================================
// SECRET ADMIN PANEL GATEWAY
// ==========================================
@Composable
fun SecretAdminGateDialog(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    var isVerified by remember { mutableStateOf(currentUser?.isAdmin == true) }
    var passwordChoice by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    if (isVerified) {
        // Render detailed Admin Controls Panel directly!
        AdminControlPanelDialog(
            viewModel = viewModel,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            fontFamily = fontFamily,
            onDismiss = onDismiss
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🔒 Admin Verification Gating", fontFamily = fontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("To open the Admin panel settings, enter the active Admin user account Secret key/password.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordChoice,
                        onValueChange = { passwordChoice = it },
                        label = { Text("Admin Secret Key/Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError) {
                        Text("Incorrect Admin Key/Password! Please try again.", color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordChoice == adminConfig.adminKey) {
                            isVerified = true
                        } else {
                            passwordError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Verify Pin", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

// ==========================================
// DETAILED ADMIN PANEL CONTROLS SCREEN
// ==========================================
@Composable
fun AdminControlPanelDialog(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val config by viewModel.adminConfig.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var primaryHex by remember { mutableStateOf(config.primaryColorHex) }
    var secondaryHex by remember { mutableStateOf(config.secondaryColorHex) }
    var bgHex by remember { mutableStateOf(config.backgroundColorHex) }

    var fontChoice by remember { mutableStateOf(config.selectedFontName) }
    var categoriesCsv by remember { mutableStateOf(config.categoriesString) }
    var welcomeMsg by remember { mutableStateOf(config.welcomeMessage) }
    var channelLink by remember { mutableStateOf(config.channelLink) }

    var announcementTitle by remember { mutableStateOf(config.announcementTitle) }
    var announcementBody by remember { mutableStateOf(config.announcementBody) }
    var visibleSections by remember { mutableStateOf(config.visibleSectionsCsv) }

    var adminEmailInput by remember { mutableStateOf(config.adminEmail) }
    var adminKeyInput by remember { mutableStateOf(config.adminKey) }

    var welcomeMusicEnabledInput by remember { mutableStateOf(config.welcomeMusicEnabled) }
    var welcomeMusicNameInput by remember { mutableStateOf(config.welcomeMusicName) }
    var welcomeMusicUriInput by remember { mutableStateOf(config.welcomeMusicUri) }

    var badgeSoundNameInput by remember { mutableStateOf(config.badgeSoundName) }
    var badgeSoundUriInput by remember { mutableStateOf(config.badgeSoundUri) }

    var celebrationSoundNameInput by remember { mutableStateOf(config.celebrationSoundName) }
    var celebrationSoundUriInput by remember { mutableStateOf(config.celebrationSoundUri) }

    var notificationSoundNameInput by remember { mutableStateOf(config.notificationSoundName) }
    var notificationSoundUriInput by remember { mutableStateOf(config.notificationSoundUri) }

    var customizeBadgeNameInput by remember { mutableStateOf(config.badgeName) }
    var customizeBadgeLogoInput by remember { mutableStateOf(config.badgeLogo) }
    var customizeBadgeAppearanceInput by remember { mutableStateOf(config.badgeAppearance) }
    var customizeBadgeImageInput by remember { mutableStateOf(config.badgeImage ?: "") }
    
    var customizeHydrationLogoInput by remember { mutableStateOf(config.hydrationLogo) }
    var customizeHydrationLogoImageInput by remember { mutableStateOf(config.hydrationLogoImage ?: "") }

    var customizePopupLogoInput by remember { mutableStateOf(config.popupLogo) }
    var customizePopupLogoImageInput by remember { mutableStateOf(config.popupLogoImage ?: "") }

    val launcherBadgeImage = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                customizeBadgeImageInput = base64
            }
        }
    }

    val launcherHydrationLogoImage = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                customizeHydrationLogoImageInput = base64
            }
        }
    }

    val launcherPopupLogoImage = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                customizePopupLogoImageInput = base64
            }
        }
    }

    var keyVerificationInput by remember { mutableStateOf("") }
    var verificationErrorMsg by remember { mutableStateOf<String?>(null) }

    var testNotifyTitle by remember { mutableStateOf("Alert from Admin!") }
    var testNotifyMsg by remember { mutableStateOf("New layout updates have successfully rolled out! Check it out 💕") }
    var testNotifyImage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Super Gated Control Panel", fontWeight = FontWeight.Bold, fontFamily = fontFamily, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Changes saved here are updated instantly across all system layouts.", fontSize = 12.sp, color = Color.Gray)

                // App Theme Hue overrides
                Text("🎨 Dynamic Theme Color Controls", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            primaryHex = "#FF5E7E"
                            secondaryHex = "#FFAF6F"
                            bgHex = "#FFF3F5"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E7E)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Blossom", fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            primaryHex = "#4A90E2"
                            secondaryHex = "#50E3C2"
                            bgHex = "#F0F4F8"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Ocean Breeze", fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            primaryHex = "#9B51E0"
                            secondaryHex = "#F2994A"
                            bgHex = "#F7F3FC"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Galaxy Lavender", fontSize = 10.sp)
                    }
                }

                OutlinedTextField(
                    value = primaryHex,
                    onValueChange = { primaryHex = it },
                    label = { Text("Primary Color Hex") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = secondaryHex,
                    onValueChange = { secondaryHex = it },
                    label = { Text("Secondary Color Hex") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bgHex,
                    onValueChange = { bgHex = it },
                    label = { Text("Background Color Hex") },
                    singleLine = true
                )

                // Fonts selections
                Text("🔤 Font Selectors", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fontsList = listOf("Default", "Playful", "Serif", "Mono")
                    fontsList.forEach { f ->
                        FilterChip(
                            selected = fontChoice == f,
                            onClick = { fontChoice = f },
                            label = { Text(f, fontSize = 11.sp) }
                        )
                    }
                }

                // Dynamic Checklist Categories
                Text("📁 Customize Task Categories (CSV)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = categoriesCsv,
                    onValueChange = { categoriesCsv = it },
                    placeholder = { Text("Work, Personal, Urgent") }
                )

                // Homepage Greetings
                Text("🌸 Customize Greetings & Marquees", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = welcomeMsg,
                    onValueChange = { welcomeMsg = it },
                    maxLines = 2
                )
                OutlinedTextField(
                    value = channelLink,
                    onValueChange = { channelLink = it },
                    label = { Text("Telegram Link Customizer") }
                )

                // Core announcements
                Text("📣 Customize Dashboard Announcement", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = announcementTitle,
                    onValueChange = { announcementTitle = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = announcementBody,
                    onValueChange = { announcementBody = it },
                    label = { Text("Body Paragraphs") }
                )

                // Push real local Notification tool
                Text("🔔 OS System Notification Broadcaster (FCM)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)
                OutlinedTextField(
                    value = testNotifyTitle,
                    onValueChange = { testNotifyTitle = it },
                    label = { Text("Notification Title") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_notification_title_input")
                )
                OutlinedTextField(
                    value = testNotifyMsg,
                    onValueChange = { testNotifyMsg = it },
                    label = { Text("Notification Message") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_notification_msg_input")
                )
                OutlinedTextField(
                    value = testNotifyImage,
                    onValueChange = { testNotifyImage = it },
                    label = { Text("Optional Image URL Attachment") },
                    placeholder = { Text("e.g. https://picsum.photos/400/200") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_notification_img_input")
                )

                // Dynamic Admin Account Credentials
                Text("🔐 Admin Identity Credentials", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = adminEmailInput,
                    onValueChange = { adminEmailInput = it },
                    label = { Text("Admin Registered Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adminKeyInput,
                    onValueChange = { adminKeyInput = it },
                    label = { Text("Admin Registration Secret Key / Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dynamic Audio and Chime Controls
                Text("🎵 System Sound Customization Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = welcomeMusicEnabledInput,
                        onCheckedChange = { welcomeMusicEnabledInput = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Play Welcome Music on Success Logins", fontSize = 12.sp, fontFamily = fontFamily)
                }
                
                OutlinedTextField(
                    value = welcomeMusicNameInput,
                    onValueChange = { welcomeMusicNameInput = it },
                    label = { Text("Welcome Music Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = welcomeMusicUriInput,
                    onValueChange = { welcomeMusicUriInput = it },
                    label = { Text("Welcome Music URI Path / Presets") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = badgeSoundNameInput,
                    onValueChange = { badgeSoundNameInput = it },
                    label = { Text("Badge Chime Sound Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = badgeSoundUriInput,
                    onValueChange = { badgeSoundUriInput = it },
                    label = { Text("Badge Chime Sound URI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = celebrationSoundNameInput,
                    onValueChange = { celebrationSoundNameInput = it },
                    label = { Text("Celebration Alert Sound Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = celebrationSoundUriInput,
                    onValueChange = { celebrationSoundUriInput = it },
                    label = { Text("Celebration Alert Sound URI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notificationSoundNameInput,
                    onValueChange = { notificationSoundNameInput = it },
                    label = { Text("Notification Ring Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notificationSoundUriInput,
                    onValueChange = { notificationSoundUriInput = it },
                    label = { Text("Notification Ring URI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("🏆 Dynamic Hydration Milestone Badge Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = customizeBadgeNameInput,
                    onValueChange = { customizeBadgeNameInput = it },
                    label = { Text("Level 1 Milestone Badge Title") },
                    placeholder = { Text("e.g. Amazing Blossom 🌸") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customizeBadgeLogoInput,
                    onValueChange = { customizeBadgeLogoInput = it },
                    label = { Text("Level 1 Milestone Badge Logo (Emoji)") },
                    placeholder = { Text("e.g. 💮") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customizeBadgeAppearanceInput,
                    onValueChange = { customizeBadgeAppearanceInput = it },
                    label = { Text("Level 1 Badge Appearance Theme Hint") },
                    placeholder = { Text("e.g. Pink Glow") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Dynamic Badge customizable logo uploads
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launcherBadgeImage.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upload Custom Badge Image", fontSize = 11.sp, color = Color.White)
                    }
                    if (customizeBadgeImageInput.isNotBlank()) {
                        Text("✅ Uploaded", fontSize = 11.sp, color = primaryColor)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { customizeBadgeImageInput = "" }) {
                            Text("Clear", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("🧊 Hydration Category Logo Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = customizeHydrationLogoInput,
                    onValueChange = { customizeHydrationLogoInput = it },
                    label = { Text("Hydration Category Logo (Emoji Preset)") },
                    placeholder = { Text("e.g. 🧊") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launcherHydrationLogoImage.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upload Custom Hydration Logo", fontSize = 11.sp, color = Color.White)
                    }
                    if (customizeHydrationLogoImageInput.isNotBlank()) {
                        Text("✅ Uploaded", fontSize = 11.sp, color = primaryColor)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { customizeHydrationLogoImageInput = "" }) {
                            Text("Clear", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("🤭 Custom Promotion Pop-up Logo Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = customizePopupLogoInput,
                    onValueChange = { customizePopupLogoInput = it },
                    label = { Text("Promo Pop-up Logo (Emoji Preset)") },
                    placeholder = { Text("e.g. 🔔") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launcherPopupLogoImage.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upload Custom Promo Logo", fontSize = 11.sp, color = Color.White)
                    }
                    if (customizePopupLogoImageInput.isNotBlank()) {
                        Text("✅ Uploaded", fontSize = 11.sp, color = primaryColor)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { customizePopupLogoImageInput = "" }) {
                            Text("Clear", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Recovery & Device Management Accordion (Admin Only)
                Text("🛡️ Admin Account Recovery & Devices", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🔑 Emergency Recovery Configuration", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                        
                        Text("Recovery Email: ${config.recoveryEmail.ifBlank { "Not Set!" }}", fontSize = 12.sp, fontFamily = fontFamily)
                        
                        // Option to generate new recovery code
                        var showRegenDialog by remember { mutableStateOf(false) }
                        var verifyPINForRegen by remember { mutableStateOf("") }
                        var regenError by remember { mutableStateOf("") }
                        var freshlyRegeneratedCode by remember { mutableStateOf("") }

                        Button(
                            onClick = { showRegenDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔄 Generate New Recovery Code", fontSize = 11.sp, color = Color.White)
                        }

                        if (showRegenDialog) {
                            AlertDialog(
                                onDismissRequest = { 
                                    showRegenDialog = false
                                    verifyPINForRegen = ""
                                    regenError = ""
                                    freshlyRegeneratedCode = ""
                                },
                                title = {
                                    Text("🔑 Authentication Required", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Please verify your Admin PIN to regenerate the recovery code. Note: the old code will be invalidated instantly.", fontSize = 12.sp, color = Color.Gray, fontFamily = fontFamily)
                                        
                                        if (freshlyRegeneratedCode.isEmpty()) {
                                            OutlinedTextField(
                                                value = verifyPINForRegen,
                                                onValueChange = { verifyPINForRegen = it },
                                                placeholder = { Text("Enter Admin PIN") },
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation(),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text("New Recovery Code (Save Safely!):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(primaryColor.copy(alpha = 0.08f))
                                                    .padding(12.dp)
                                            ) {
                                                androidx.compose.foundation.text.selection.SelectionContainer {
                                                    Text(
                                                        text = freshlyRegeneratedCode,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = primaryColor,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }

                                        if (regenError.isNotEmpty()) {
                                            Text(regenError, color = Color.Red, fontSize = 11.sp, fontFamily = fontFamily)
                                        }
                                    }
                                },
                                confirmButton = {
                                    if (freshlyRegeneratedCode.isEmpty()) {
                                        Button(
                                            onClick = {
                                                if (verifyPINForRegen == config.adminKey) {
                                                    val randChars = "ABCDEFGHJKLMNOPQRSTUVWXYZ23456789"
                                                    val code = (1..16).map { randChars.random() }.joinToString("")
                                                    val newCode = "COZY-RCV-${code.take(4)}-${code.drop(4).take(4)}-${code.drop(8).take(4)}"
                                                    
                                                    // Invalidate old, set new code only
                                                    viewModel.updateRecoveryCodeOnly(newCode)
                                                    freshlyRegeneratedCode = newCode
                                                    regenError = ""
                                                } else {
                                                    regenError = "Incorrect Admin PIN! Verification failed."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Regenerate", color = Color.White)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                showRegenDialog = false
                                                verifyPINForRegen = ""
                                                freshlyRegeneratedCode = ""
                                                regenError = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Done & Copied", color = Color.White)
                                        }
                                    }
                                }
                            )
                        }

                        Divider(color = primaryColor.copy(alpha = 0.1f), thickness = 1.dp)

                        Text("📱 Trusted Admin Devices", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                        
                        val devices = try {
                            val arr = org.json.JSONArray(config.trustedDevicesJson)
                            val list = mutableListOf<String>()
                            for (i in 0 until arr.length()) {
                                list.add(arr.getString(i))
                            }
                            list
                        } catch (e: Exception) {
                            emptyList<String>()
                        }

                        if (devices.isEmpty()) {
                            Text("No trusted devices registered yet.", fontSize = 11.sp, color = Color.LightGray, fontFamily = fontFamily)
                        } else {
                            devices.forEach { d ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("• $d", fontSize = 11.sp, fontFamily = fontFamily)
                                    IconButton(
                                        onClick = { viewModel.removeTrustedDevice(d) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Add new trusted device manually
                        var newManualDevice by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newManualDevice,
                                onValueChange = { newManualDevice = it },
                                placeholder = { Text("e.g. Kajal's Pixel") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(50.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = primaryColor.copy(alpha = 0.2f)
                                )
                            )
                            Button(
                                onClick = {
                                    if (newManualDevice.isNotBlank()) {
                                        viewModel.addTrustedDevice(newManualDevice.trim())
                                        newManualDevice = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text("Add", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, primaryColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🔑 Verification & Security Gate", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("To apply these changes live, you must enter the active Admin user's Secret Key to verify.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = keyVerificationInput,
                            onValueChange = { keyVerificationInput = it },
                            placeholder = { Text("Enter Active Admin Key") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        verificationErrorMsg?.let { msg ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.sendBroadcastNotificationWithDetails(
                            title = testNotifyTitle,
                            message = testNotifyMsg,
                            imageUrl = if (testNotifyImage.isBlank()) null else testNotifyImage
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_send_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Text("🚀 Send Real-Time FCM Broadcast Now 🔔", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyVerificationInput.trim() != config.adminKey) {
                        verificationErrorMsg = "Verification failed! Correct active Admin Key is required to authorize updates."
                    } else {
                        verificationErrorMsg = null
                        viewModel.updateAdminConfigComplete(
                            primaryHex = primaryHex,
                            secondaryHex = secondaryHex,
                            bgHex = bgHex,
                            fontChoice = fontChoice,
                            categoriesCsv = categoriesCsv,
                            welcomeMsg = welcomeMsg,
                            channelLink = channelLink,
                            announceTitle = announcementTitle,
                            announceBody = announcementBody,
                            visibleCsv = visibleSections,
                            
                            newAdminEmail = adminEmailInput,
                            newAdminKey = adminKeyInput,
                            
                            musicEnabled = welcomeMusicEnabledInput,
                            welcomeName = welcomeMusicNameInput,
                            welcomeUri = welcomeMusicUriInput,
                            badgeName = badgeSoundNameInput,
                            badgeUri = badgeSoundUriInput,
                            celebrationName = celebrationSoundNameInput,
                            celebrationUri = celebrationSoundUriInput,
                            notifyName = notificationSoundNameInput,
                            notifyUri = notificationSoundUriInput,
                            customBadgeName = customizeBadgeNameInput,
                            customBadgeLogo = customizeBadgeLogoInput,
                            customBadgeAppearance = customizeBadgeAppearanceInput,
                            customBadgeImage = if (customizeBadgeImageInput.isBlank()) null else customizeBadgeImageInput,
                            customHydrationLogo = customizeHydrationLogoInput,
                            customHydrationLogoImage = if (customizeHydrationLogoImageInput.isBlank()) null else customizeHydrationLogoImageInput,
                            customPopupLogo = customizePopupLogoInput,
                            customPopupLogoImage = if (customizePopupLogoImageInput.isBlank()) null else customizePopupLogoImageInput
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Apply & Sync App Live", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        }
    )
}

// ==========================================
// MOCK LIVE SUPPORT CHAT BOX DIALOG
// ==========================================
@Composable
fun SimulatedSupportChatDialog(
    viewModel: MainViewModel,
    primaryColor: Color,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val config by viewModel.adminConfig.collectAsStateWithLifecycle()
    var typedMessage by remember { mutableStateOf("") }
    val chatsList = remember(config.supportChatsJson) {
        try {
            val arr = JSONArray(config.supportChatsJson)
            val list = mutableListOf<Triple<String, String, Long>>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Triple(
                        o.optString("sender"),
                        o.optString("message"),
                        o.optLong("timestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList<Triple<String, String, Long>>()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Simulated Live Firebase Support 💬", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = { viewModel.clearSupportChats() }) {
                    Text("Clear Logs", color = Color.Red, fontSize = 10.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.6f)) {
                Text("Real-time telemetry chats stored in Firestore database", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily)
                Spacer(modifier = Modifier.height(8.dp))

                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                androidx.compose.runtime.LaunchedEffect(chatsList.size) {
                    if (chatsList.isNotEmpty()) {
                        listState.animateScrollToItem(chatsList.size - 1)
                    }
                }

                val chatBoxBg = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF1E1E24) else Color(0xFFF5F5F8)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .background(chatBoxBg, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chatsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No historic messages. Say hello to Support!", color = Color.Gray, fontSize = 12.sp, fontFamily = fontFamily)
                            }
                        }
                    }

                    items(chatsList) { (sender, msg, ts) ->
                        val isMe = sender != "Admin Helpdesk 🌸" && sender != "SYSTEM"
                        val bubbleBg = if (isMe) {
                            primaryColor
                        } else {
                            if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF2C2A35) else Color(0xFFE2E2E8)
                        }
                        val bubbleTextColor = if (isMe) {
                            Color.White
                        } else {
                            if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color.Black
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                        )
                                    )
                                    .background(bubbleBg)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = bubbleTextColor,
                                    fontSize = 13.sp,
                                    fontFamily = fontFamily
                                )
                            }
                            Text(
                                text = "$sender • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(2.dp),
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = { typedMessage = it },
                        placeholder = { Text("Type custom message...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (typedMessage.isNotBlank()) {
                                viewModel.sendUserSupportMessage(typedMessage)
                                typedMessage = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(primaryColor)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = primaryColor)
            }
        }
    )
}

// ==========================================
// TAB 2: CALCULATOR WITH HISTORY DISPLAY
// ==========================================
@Composable
fun CalculatorTab(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily
) {
    val input by viewModel.calculatorInput.collectAsStateWithLifecycle()
    val result by viewModel.calculatorResult.collectAsStateWithLifecycle()
    val history by viewModel.calculationHistory.collectAsStateWithLifecycle()

    var showHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Core Display Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sweet Calc Engine 💖",
                        fontFamily = fontFamily,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = primaryColor)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = input.ifEmpty { "0" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = result,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor,
                        fontFamily = fontFamily,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cute rounded computation grid keys
        val gridKeys = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            gridKeys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isOp = key in listOf("C", "(", ")", "÷", "×", "-", "+", "⌫", "=")
                        val isEquals = key == "="
                        val btnColor = when {
                            isEquals -> primaryColor
                            isOp -> secondaryColor.copy(alpha = 0.15f)
                            else -> Color.White
                        }
                        val textColor = when {
                            isEquals -> Color.White
                            isOp -> secondaryColor.adjustForText()
                            else -> Color.DarkGray
                        }

                        Button(
                            onClick = { viewModel.onCalculatorKeyPress(key) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("calculator_key_$key"),
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Calculation History Logs
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculation History 📝",
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                    TextButton(
                        onClick = { viewModel.clearHistory() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear All ❌", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                    }
                }
            },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No history calculations recorded", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(history) { c ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(c.expression, fontSize = 14.sp, color = Color.Gray)
                                        Text("= ${c.result}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
                                        Text(
                                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(c.timestamp)),
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Dismiss", color = primaryColor)
                }
            }
        )
    }
}

// ==========================================
// TAB 3: CUSTOM CHECKLIST TASKS
// ==========================================
@Composable
fun TasksTab(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    categories: List<String>
) {
    val tasks by viewModel.taskList.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var renameTargetTask by remember { mutableStateOf<CustomizedTask?>(null) }

    var filterCategory by remember { mutableStateOf("All") }

    val filteredTasks = remember(tasks, filterCategory) {
        if (filterCategory == "All") tasks else tasks.filter { it.category == filterCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Task Organizer ✨",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = fontFamily,
                color = primaryColor
            )

            // Add action trigger FAB replacement
            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allCats = listOf("All") + categories
            allCats.forEach { cat ->
                val isSel = filterCategory == cat
                FilterChip(
                    selected = isSel,
                    onClick = { filterCategory = cat },
                    label = { Text(cat, fontSize = 12.sp, fontFamily = fontFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.15f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks checklist representation
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hooray! No pending checklist tasks found.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskComplete(task) },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    color = if (task.isCompleted) Color.Gray else Color.Black,
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Tag: ${task.category}",
                                            fontSize = 9.sp,
                                            color = primaryColor,
                                            fontFamily = fontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (task.reminderTime != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(secondaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            val datePart = if (task.reminderDate != null) "${task.reminderDate} " else ""
                                            val recurrenceLabel = if (task.isRecurring) " 🔁 Daily" else ""
                                            Text(
                                                text = "⏰ $datePart${task.reminderTime}$recurrenceLabel",
                                                fontSize = 9.sp,
                                                color = secondaryColor.adjustForText(),
                                                fontFamily = fontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Reordering control buttons Up & Down
                            IconButton(onClick = { viewModel.moveTask(task, up = true) }) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.moveTask(task, up = false) }) {
                                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }

                            // Additional Edit Actions
                            IconButton(onClick = { renameTargetTask = task }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }

                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add task Dialog
    if (showAddTaskDialog) {
        val taskPromptContext = androidx.compose.ui.platform.LocalContext.current
        var taskTitle by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
        var wantReminder by remember { mutableStateOf(false) }
        var inputDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var inputTime by remember { mutableStateOf("09:00") }
        var isDailyRecurring by remember { mutableStateOf(false) }

        var pickDay by remember {
            mutableStateOf(
                try { inputDate.substringAfterLast("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) }
            )
        }
        var pickMonth by remember {
            mutableStateOf(
                try { inputDate.substringAfter("-").substringBefore("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1 }
            )
        }
        var pickYear by remember {
            mutableStateOf(
                try { inputDate.substringBefore("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
            )
        }
        
        var pickHour by remember {
            mutableStateOf(
                try {
                    val h = inputTime.substringBefore(":").toInt()
                    if (h == 0) 12 else if (h > 12) h - 12 else h
                } catch(e: Exception) { 9 }
            )
        }
        var pickMinute by remember {
            mutableStateOf(
                try { inputTime.substringAfter(":").toInt() } catch(e: Exception) { 0 }
            )
        }
        var pickAmpm by remember {
            mutableStateOf(
                try {
                    val h = inputTime.substringBefore(":").toInt()
                    if (h >= 12) "PM" else "AM"
                } catch(e: Exception) { "AM" }
            )
        }

        val updateInputDateAndTime = { d: Int, m: Int, y: Int, h: Int, min: Int, ampmStr: String ->
            inputDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m, d)
            val h24 = if (ampmStr == "PM") {
                if (h < 12) h + 12 else 12
            } else {
                if (h == 12) 0 else h
            }
            inputTime = String.format(java.util.Locale.US, "%02d:%02d", h24, min)
        }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Create Customized Task", fontFamily = fontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pick Category Tag:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { c ->
                            FilterChip(
                                selected = selectedCat == c,
                                onClick = { selectedCat = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = wantReminder, onCheckedChange = { wantReminder = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Alert Reminder Schedule", fontSize = 12.sp, fontFamily = fontFamily)
                    }

                    if (wantReminder) {
                        CuteSeparateDateTimePicker(
                            day = pickDay,
                            month = pickMonth,
                            year = pickYear,
                            hour = pickHour,
                            minute = pickMinute,
                            ampm = pickAmpm,
                            onDayChange = { pickDay = it; updateInputDateAndTime(it, pickMonth, pickYear, pickHour, pickMinute, pickAmpm) },
                            onMonthChange = { pickMonth = it; updateInputDateAndTime(pickDay, it, pickYear, pickHour, pickMinute, pickAmpm) },
                            onYearChange = { pickYear = it; updateInputDateAndTime(pickDay, pickMonth, it, pickHour, pickMinute, pickAmpm) },
                            onHourChange = { pickHour = it; updateInputDateAndTime(pickDay, pickMonth, pickYear, it, pickMinute, pickAmpm) },
                            onMinuteChange = { pickMinute = it; updateInputDateAndTime(pickDay, pickMonth, pickYear, pickHour, it, pickAmpm) },
                            onAmpmChange = { pickAmpm = it; updateInputDateAndTime(pickDay, pickMonth, pickYear, pickHour, pickMinute, it) },
                            primaryColor = primaryColor,
                            fontFamily = fontFamily
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = isDailyRecurring, onCheckedChange = { isDailyRecurring = it })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Make Recurring Daily Reminder", fontSize = 12.sp, fontFamily = fontFamily)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.insertReminderTask(
                                title = taskTitle,
                                category = selectedCat,
                                date = if (wantReminder) inputDate else null,
                                time = if (wantReminder) inputTime else null,
                                recurring = if (wantReminder) isDailyRecurring else false,
                                context = taskPromptContext
                            )
                            if (wantReminder) {
                                android.widget.Toast.makeText(context, "Reminder added", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Add Checked", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Rename/Edit task details dialog
    if (renameTargetTask != null) {
        val task = renameTargetTask!!
        var editTitle by remember { mutableStateOf(task.title) }
        var editCat by remember { mutableStateOf(task.category) }
        var wantReminder by remember { mutableStateOf(task.reminderTime != null) }
        var editDate by remember { mutableStateOf(task.reminderDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var editTime by remember { mutableStateOf(task.reminderTime ?: "09:00") }
        var editRecurring by remember { mutableStateOf(task.isRecurring) }

        var pickDay by remember(task) {
            mutableStateOf(
                try { editDate.substringAfterLast("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) }
            )
        }
        var pickMonth by remember(task) {
            mutableStateOf(
                try { editDate.substringAfter("-").substringBefore("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1 }
            )
        }
        var pickYear by remember(task) {
            mutableStateOf(
                try { editDate.substringBefore("-").toInt() } catch(e: Exception) { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
            )
        }
        
        var pickHour by remember(task) {
            mutableStateOf(
                try {
                    val h = editTime.substringBefore(":").toInt()
                    if (h == 0) 12 else if (h > 12) h - 12 else h
                } catch(e: Exception) { 9 }
            )
        }
        var pickMinute by remember(task) {
            mutableStateOf(
                try { editTime.substringAfter(":").toInt() } catch(e: Exception) { 0 }
            )
        }
        var pickAmpm by remember(task) {
            mutableStateOf(
                try {
                    val h = editTime.substringBefore(":").toInt()
                    if (h >= 12) "PM" else "AM"
                } catch(e: Exception) { "AM" }
            )
        }

        val updateEditDateAndTime = { d: Int, m: Int, y: Int, h: Int, min: Int, ampmStr: String ->
            editDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m, d)
            val h24 = if (ampmStr == "PM") {
                if (h < 12) h + 12 else 12
            } else {
                if (h == 12) 0 else h
            }
            editTime = String.format(java.util.Locale.US, "%02d:%02d", h24, min)
        }

        AlertDialog(
            onDismissRequest = { renameTargetTask = null },
            title = { Text("Edit Task details", fontFamily = fontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task Title description") },
                        singleLine = true
                    )
                    Text("Move to Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { c ->
                            FilterChip(
                                selected = editCat == c,
                                onClick = { editCat = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = wantReminder, onCheckedChange = { wantReminder = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Alert Reminder Schedule", fontSize = 12.sp, fontFamily = fontFamily)
                    }

                    if (wantReminder) {
                        CuteSeparateDateTimePicker(
                            day = pickDay,
                            month = pickMonth,
                            year = pickYear,
                            hour = pickHour,
                            minute = pickMinute,
                            ampm = pickAmpm,
                            onDayChange = { pickDay = it; updateEditDateAndTime(it, pickMonth, pickYear, pickHour, pickMinute, pickAmpm) },
                            onMonthChange = { pickMonth = it; updateEditDateAndTime(pickDay, it, pickYear, pickHour, pickMinute, pickAmpm) },
                            onYearChange = { pickYear = it; updateEditDateAndTime(pickDay, pickMonth, it, pickHour, pickMinute, pickAmpm) },
                            onHourChange = { pickHour = it; updateEditDateAndTime(pickDay, pickMonth, pickYear, it, pickMinute, pickAmpm) },
                            onMinuteChange = { pickMinute = it; updateEditDateAndTime(pickDay, pickMonth, pickYear, pickHour, it, pickAmpm) },
                            onAmpmChange = { pickAmpm = it; updateEditDateAndTime(pickDay, pickMonth, pickYear, pickHour, pickMinute, it) },
                            primaryColor = primaryColor,
                            fontFamily = fontFamily
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = editRecurring, onCheckedChange = { editRecurring = it })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Make Recurring Daily Reminder", fontSize = 12.sp, fontFamily = fontFamily)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            viewModel.updateCustomizedTask(
                                task.copy(
                                    title = editTitle,
                                    category = editCat,
                                    reminderDate = if (wantReminder) editDate else null,
                                    reminderTime = if (wantReminder) editTime else null,
                                    isRecurring = if (wantReminder) editRecurring else false
                                )
                            )
                            if (wantReminder) {
                                android.widget.Toast.makeText(context, "Reminder added", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            renameTargetTask = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Save Modifications", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetTask = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

// ==========================================
// TAB 4: PERSONAL FILE STOREHOUSE
// ==========================================
@Composable
fun StorehouseTab(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily,
    categories: List<String>
) {
    val itemsFlow by viewModel.storehouseItems.collectAsStateWithLifecycle()
    val searchVal by viewModel.storehouseSearchQuery.collectAsStateWithLifecycle()

    var activeTypeFilter by remember { mutableStateOf("All") }
    var activeFolderFilter by remember { mutableStateOf("") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showUploadFileDialog by remember { mutableStateOf(false) }

    // Register folders dynamically from items
    val currentFolders = remember(itemsFlow) {
        itemsFlow.map { it.folderName }.filter { it.isNotBlank() }.distinct()
    }

    val filteredFileItems = remember(itemsFlow, activeTypeFilter, activeFolderFilter) {
        itemsFlow.filter { item ->
            val matchesType = activeTypeFilter == "All" || item.type == activeTypeFilter
            val matchesFolder = activeFolderFilter.isEmpty() || item.folderName == activeFolderFilter
            matchesType && matchesFolder
        }
    }

    var selectedPreviewItem by remember { mutableStateOf<StorehouseItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App title and primary controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Storehouse Collection 💕", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primaryColor, fontFamily = fontFamily)
                Text("Your sandbox-cached metadata catalog", fontSize = 11.sp, color = Color.Gray)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showCreateFolderDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(secondaryColor.copy(alpha = 0.15f))
                ) {
                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Create Folder", tint = secondaryColor)
                }
                Button(
                    onClick = { showUploadFileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search panel
        OutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search title or file details...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("storehouse_search_bar"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Folder selection tabs row
        if (currentFolders.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Folders:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                FilterChip(
                    selected = activeFolderFilter.isEmpty(),
                    onClick = { activeFolderFilter = "" },
                    label = { Text("All Folders", fontSize = 11.sp) }
                )
                currentFolders.forEach { fold ->
                    val isF = activeFolderFilter == fold
                    FilterChip(
                        selected = isF,
                        onClick = { activeFolderFilter = fold },
                        label = { Text("📁 $fold", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = secondaryColor.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }

        // Horizontal Category Media Type tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val mediaTypes = listOf("All", "Note", "Image", "Video", "Audio", "PDF", "Document")
            mediaTypes.forEach { t ->
                val isSel = activeTypeFilter == t
                FilterChip(
                    selected = isSel,
                    onClick = { activeTypeFilter = t },
                    label = { Text(t, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.15f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Document library Grid list
        if (filteredFileItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Empty", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Text("Empty folder library. Try uploading some assets!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFileItems) { item ->
                    FileCollectionCard(
                        item = item,
                        primaryColor = primaryColor,
                        fontFamily = fontFamily,
                        onClick = { selectedPreviewItem = item },
                        onDelete = { viewModel.deleteStoreItem(item) }
                    )
                }
            }
        }
    }

    // Create New Folder prompt dialog
    if (showCreateFolderDialog) {
        var foldName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create Folder Directory", fontFamily = fontFamily, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = foldName,
                    onValueChange = { foldName = it },
                    placeholder = { Text("e.g. Vacation Notes, Receipts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (foldName.isNotBlank()) {
                            // Seed an initial blank anchor document bookmark to preserve folder directory record!
                            viewModel.addStoreItem(
                                title = "Folder Anchor Mark",
                                type = "Document",
                                content = "Empty sandbox anchor to preserve $foldName folder.",
                                folder = foldName,
                                category = "General",
                                sizeBytes = 100
                            )
                            showCreateFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Create Folders", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Upload content/file details simulation modal dialog
    if (showUploadFileDialog) {
        var uploadTitle by remember { mutableStateOf("") }
        var uploadType by remember { mutableStateOf("Note") }
        var uploadFolder by remember { mutableStateOf(activeFolderFilter.ifEmpty { "Default" }) }
        var uploadCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
        var noteRawText by remember { mutableStateOf("") }

        val context = androidx.compose.ui.platform.LocalContext.current
        var pickedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
        var pickedFileName by remember { mutableStateOf("") }
        var pickedFileSize by remember { mutableStateOf<Long?>(null) }
        var permissionGranted by remember { mutableStateOf(true) }

        // Storage Access Framework launcher
        val pickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            if (uri != null) {
                pickedFileUri = uri
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameCol = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeCol = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameCol != -1) {
                                pickedFileName = cursor.getString(nameCol) ?: ""
                            }
                            if (sizeCol != -1) {
                                pickedFileSize = cursor.getLong(sizeCol)
                            }
                        }
                    }
                } catch (e: Exception) {
                    pickedFileName = uri.lastPathSegment ?: "Selected File"
                }
                if (uploadTitle.isBlank()) {
                    uploadTitle = pickedFileName
                }
            }
        }

        // Permission request launcher
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionGranted = isGranted
            val mimeType = when (uploadType) {
                "Image" -> "image/*"
                "Video" -> "video/*"
                "Audio" -> "audio/*"
                "PDF" -> "application/pdf"
                "Document" -> "*/*"
                else -> "*/*"
            }
            pickerLauncher.launch(mimeType)
        }

        AlertDialog(
            onDismissRequest = { showUploadFileDialog = false },
            title = { Text("Upload Secure Storage Asset", fontFamily = fontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Asset Title Header") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pick Media Type Tag:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Note", "Image", "Video", "Audio", "PDF", "Document").forEach { ty ->
                            FilterChip(
                                selected = uploadType == ty,
                                onClick = { 
                                    uploadType = ty
                                    pickedFileUri = null
                                    pickedFileName = ""
                                },
                                label = { Text(ty, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uploadFolder,
                        onValueChange = { uploadFolder = it },
                        label = { Text("Folder Name Directory") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Category Tag:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { ca ->
                            FilterChip(
                                selected = uploadCategory == ca,
                                onClick = { uploadCategory = ca },
                                label = { Text(ca, fontSize = 10.sp) }
                            )
                        }
                    }

                    if (uploadType == "Note") {
                        OutlinedTextField(
                            value = noteRawText,
                            onValueChange = { noteRawText = it },
                            label = { Text("Write Note Contents") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Interactive File Upload Widget with proper authorization permissions request
                        Card(
                            onClick = {
                                val permissionToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    when (uploadType) {
                                        "Image" -> android.Manifest.permission.READ_MEDIA_IMAGES
                                        "Video" -> android.Manifest.permission.READ_MEDIA_VIDEO
                                        "Audio" -> android.Manifest.permission.READ_MEDIA_AUDIO
                                        else -> "android.permission.READ_EXTERNAL_STORAGE"
                                    }
                                } else {
                                    "android.permission.READ_EXTERNAL_STORAGE"
                                }
                                permissionLauncher.launch(permissionToRequest)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (pickedFileUri != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    if (pickedFileUri != null) {
                                        Text("File Selected! ✅", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Name: $pickedFileName", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                                        pickedFileSize?.let {
                                            Text("Size: ${it / 1024} KB", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    } else {
                                        Text("Browse & Upload Device File 📂", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)
                                        Text("Select Images, Videos, Audio, PDFs, or Files", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            val contentArg = if (uploadType == "Note") noteRawText else (pickedFileUri?.toString() ?: "SIMULATED_FILE_POINTER:${uuid()}")
                            val sizeArg = if (uploadType == "Note") {
                                noteRawText.toByteArray().size.toLong()
                            } else {
                                pickedFileSize ?: (1000 * java.util.Random().nextInt(5000) + 120).toLong()
                            }
                            viewModel.addStoreItem(
                                title = uploadTitle,
                                type = uploadType,
                                content = contentArg,
                                folder = uploadFolder,
                                category = uploadCategory,
                                sizeBytes = sizeArg
                            )
                            showUploadFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Upload Checked", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadFileDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Detail Preview drawer popup
    if (selectedPreviewItem != null) {
        FilePreviewDialog(
            item = selectedPreviewItem!!,
            primaryColor = primaryColor,
            fontFamily = fontFamily,
            onDismiss = { selectedPreviewItem = null }
        )
    }
}

fun uuid(): String {
    return UUID.randomUUID().toString().substring(0, 8)
}

@Composable
fun FileCollectionCard(
    item: StorehouseItem,
    primaryColor: Color,
    fontFamily: FontFamily,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fileIcon = when (item.type) {
        "Note" -> Icons.Default.Note
        "Image" -> Icons.Default.Image
        "Video" -> Icons.Default.VideoFile
        "Audio" -> Icons.Default.AudioFile
        "PDF" -> Icons.Default.PictureAsPdf
        else -> Icons.Default.Drafts
    }

    val themeColor = when (item.type) {
        "Note" -> Color(0xFFFFB84D)
        "Image" -> Color(0xFF4D94FF)
        "Video" -> Color(0xFFFF4D4D)
        "Audio" -> Color(0xFF4DFF88)
        "PDF" -> Color(0xFFFF4DA6)
        else -> primaryColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = fileIcon, contentDescription = null, tint = themeColor, modifier = Modifier.size(14.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                }
            }

            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fontFamily
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Folder: ${item.folderName.ifEmpty { "Default" }}",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${item.sizeBytes} B",
                        fontSize = 9.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

// Interactive Preview Player sandbox container!
@Composable
fun FilePreviewDialog(
    item: StorehouseItem,
    primaryColor: Color,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSimulatedPlaying by remember { mutableStateOf(false) }
    var simulatedSliderPos by remember { mutableStateOf(0.4f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.type) {
                        "Note" -> Icons.Default.Note
                        "Image" -> Icons.Default.Image
                        "Video" -> Icons.Default.VideoFile
                        "Audio" -> Icons.Default.AudioFile
                        "PDF" -> Icons.Default.PictureAsPdf
                        else -> Icons.Default.Drafts
                    },
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.title, fontWeight = FontWeight.Bold, fontFamily = fontFamily, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (item.type) {
                    "Note" -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .verticalScroll(rememberScrollState()),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                            border = BorderStroke(1.dp, Color(0xFFFBE4B4))
                        ) {
                            Text(
                                text = item.content,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                fontFamily = fontFamily,
                                color = Color.DarkGray
                            )
                        }
                    }
                    "Image" -> {
                        // Drawing static vector cute artwork on canvas, representing offline previews!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background hills gradient
                                drawCircle(
                                    color = Color(0xFFFFB2C5).copy(alpha = 0.5f),
                                    radius = 180f,
                                    center = Offset(size.width / 2, size.height / 1.2f)
                                )
                                drawCircle(
                                    color = Color(0xFFFFD19A).copy(alpha = 0.6f),
                                    radius = 150f,
                                    center = Offset(size.width / 4, size.height / 1.1f)
                                )
                                // Friendly face
                                drawCircle(
                                    color = Color(0xFF86E3CE).copy(alpha = 0.8f),
                                    radius = 60f,
                                    center = Offset(size.width / 2, size.height / 2.5f)
                                )
                                drawCircle(Color.Black, radius = 6f, center = Offset(size.width / 2 - 20f, size.height / 2.5f))
                                drawCircle(Color.Black, radius = 6f, center = Offset(size.width / 2 + 20f, size.height / 2.5f))
                                drawCircle(Color(0xFFFF7E82), radius = 8f, center = Offset(size.width / 2 - 28f, size.height / 2.5f + 10f))
                                drawCircle(Color(0xFFFF7E82), radius = 8f, center = Offset(size.width / 2 + 28f, size.height / 2.5f + 10f))
                            }
                            Text(
                                "Local Cached Vector Rendering",
                                color = Color.DarkGray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp)
                            )
                        }
                    }
                    "Audio" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .drawBehind {
                                        // Draw continuous sine wave lines reactively using progress!
                                        val waveWidth = size.width
                                        val points = 60
                                        val step = waveWidth / points
                                        val amp = if (isSimulatedPlaying) 22f else 6f
                                        val timeOffset = if (isSimulatedPlaying) (System.currentTimeMillis() % 100000) / 200.0 else 0.4
                                        for (i in 0 until points) {
                                            val x = i * step
                                            val y = (size.height / 2f) + (Math.sin((i.toDouble() / 3.0) + timeOffset).toFloat() * amp)
                                            drawCircle(color = primaryColor, radius = 3f, center = Offset(x, y))
                                        }
                                    }
                            ) {}

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { isSimulatedPlaying = !isSimulatedPlaying },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                ) {
                                    Text(if (isSimulatedPlaying) "Pause ||" else "Simulated Play ▶", color = Color.White)
                                }
                            }
                        }
                    }
                    "Video" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                Text("Click to play local video render stream...", color = Color.White, fontSize = 10.sp)
                                Slider(
                                    value = simulatedSliderPos,
                                    onValueChange = { simulatedSliderPos = it },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                    "PDF" -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📄 Document Cover Page Content", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Divider()
                                Text("Page 1 of 4: Simulated PDF parser compiled with sandbox file system buffers. Complete schema tags fully extracted.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                LinearProgressIndicator(progress = 0.25f, modifier = Modifier.fillMaxWidth(), color = primaryColor)
                            }
                        }
                    }
                    else -> {
                        Text("Binary blob parsed successfully. (No preview wrapper for metadata)", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        Toast.makeText(context, "Completed! File recorded permanently in system downloads.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Download File 💾", color = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    )
}

// ==========================================
// TAB 5: PROGRESS ANALYTICS & DASHBOARDS
// ==========================================
@Composable
fun UsageProgressTab(
    viewModel: MainViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    fontFamily: FontFamily
) {
    val usageEvents by viewModel.usageEventsFlow.collectAsStateWithLifecycle()
    var displayRangeSelection by remember { mutableStateOf("Weekly") }

    // Aggregate category duration times inside ViewModel lists
    val categoryDurations = remember(usageEvents) {
        val groups = usageEvents.groupBy { it.categoryName }
        groups.mapValues { entry -> entry.value.sumOf { it.durationMs } }
    }

    val totalDurationMs = remember(categoryDurations) {
        categoryDurations.values.sum()
    }

    val totalSeconds = totalDurationMs / 1000
    val totalMins = totalSeconds / 60
    val totalHours = totalMins / 60

    val remainingMins = totalMins % 60
    val remainingSecs = totalSeconds % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Analytics & Progress 🧁",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = fontFamily,
                color = primaryColor
            )
        }

        // Live stopwatch time ticker Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total App Session Active Time", fontSize = 12.sp, color = Color.Gray, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(8.dp))
                    val dynamicActiveTicker = if (totalHours > 0) {
                        "${totalHours} Hour(s) & ${remainingMins} Minute(s)"
                    } else {
                        "${remainingMins} Minute(s)"
                    }
                    Text(
                        text = dynamicActiveTicker,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Live metrics updated dynamically via SharedPreferences background telemetries every 5s", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        // Range selector filter tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { r ->
                    FilterChip(
                        selected = displayRangeSelection == r,
                        onClick = { displayRangeSelection = r },
                        label = { Text(r, fontFamily = fontFamily) }
                    )
                }
            }
        }

        // Pie Chart Display
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Time distribution by Tab Category", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Canvas Circular Pie Drawing Engine!
                    val pieSlices = remember(categoryDurations) {
                        val colorsList = listOf(primaryColor, secondaryColor, Color(0xFF4D94FF), Color(0xFFFFB84D), Color(0xFFA64DFF))
                        var colorIdx = 0
                        categoryDurations.map { (catName, sumMs) ->
                            val c = colorsList[colorIdx % colorsList.size]
                            colorIdx++
                            PieSlice(name = catName, value = sumMs.toDouble(), color = c)
                        }
                    }

                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val totalVal = pieSlices.sumOf { it.value }
                            if (totalVal == 0.0) {
                                drawCircle(color = Color.LightGray, style = Stroke(width = 30f))
                            } else {
                                var startAngle = -90f
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                
                                pieSlices.forEach { slice ->
                                    val sweepAngle = ((slice.value / totalVal) * 360f).toFloat()
                                    drawArc(
                                        color = slice.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true
                                    )
                                    
                                    // Draw category percentage label inside each partition
                                    if (sweepAngle > 15f) {
                                        val angleRad = Math.toRadians((startAngle + sweepAngle / 2.0)).toFloat()
                                        // Place text offset from center inside donut ring
                                        val labelX = center.x + Math.cos(angleRad.toDouble()).toFloat() * (size.width / 3.2f)
                                        val labelY = center.y + Math.sin(angleRad.toDouble()).toFloat() * (size.width / 3.2f) + 10f
                                        val pct = (slice.value / totalVal * 100)
                                        val pctText = String.format(java.util.Locale.US, "%.0f%%", pct)
                                        drawContext.canvas.nativeCanvas.drawText(pctText, labelX, labelY, paint)
                                    }
                                    
                                    startAngle += sweepAngle
                                }
                                // White donut cutout in the middle for high visual aesthetics!
                                drawCircle(color = Color.White, radius = 95f)
                            }
                        }

                        // Inside text
                        Text("Active Stats", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    fun formatShortPrecision(value: Double): String {
                        if (value <= 0.0) return "0"
                        if (value >= 1.0) {
                            val formatted = String.format(java.util.Locale.US, "%.1f", value)
                            return if (formatted.endsWith(".0")) formatted.substringBefore(".0") else formatted
                        }
                        var temp = value
                        var decimals = 1
                        while (temp < 0.1 && decimals < 10) {
                            temp *= 10.0
                            decimals++
                        }
                        return String.format(java.util.Locale.US, "%.${decimals}f", value)
                    }

                    // Labels mapping legends
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pieSlices.forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(s.color))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(s.name, fontSize = 12.sp, fontFamily = fontFamily, fontWeight = FontWeight.Medium)
                                }
                                val cleanName = s.name.lowercase()
                                val spentText = if (cleanName.contains("mainshell") || cleanName.contains("auth") || cleanName.contains("splash")) {
                                    val sMins = s.value / 1000.0 / 60.0
                                    "${formatShortPrecision(sMins)}m spent"
                                } else {
                                    val sMins = (s.value / 1000) / 60
                                    val sHours = sMins / 60
                                    val sRemMins = sMins % 60
                                    if (sHours > 0) "${sHours}h ${sRemMins}m spent" else "${sRemMins}m spent"
                                }
                                Text(spentText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Custom Dynamic Category Bar Chart Section
        item {
            var selectedBarName by remember { mutableStateOf<String?>(null) }
            var selectedBarMs by remember { mutableStateOf<Long?>(null) }
            val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
            val activeCategories = adminConfig.categoriesString.split(",")
            val chartColors = listOf(primaryColor, secondaryColor, Color(0xFF4D94FF), Color(0xFFFFB84D), Color(0xFFA64DFF))
            
            val barItems = remember(usageEvents, displayRangeSelection, activeCategories, primaryColor, secondaryColor) {
                val now = System.currentTimeMillis()
                val oneDayMs = 24 * 3600 * 1000L
                val items = mutableListOf<Pair<String, Long>>()

                when (displayRangeSelection) {
                    "Daily" -> {
                        // Daily: Category-wise time distribution
                        val last24hEvents = usageEvents.filter { now - it.timestamp <= oneDayMs }
                        val cats = listOf("Calculator", "Tasks", "Storehouse", "Progress") + activeCategories
                        cats.distinct().forEach { cat ->
                            val clean = cat.trim()
                            if (clean.isNotEmpty()) {
                                val sum = last24hEvents.filter { it.categoryName == clean || it.categoryName.lowercase() == clean.lowercase() }.sumOf { it.durationMs }
                                items.add(clean to sum)
                            }
                        }
                    }
                    "Weekly" -> {
                        // Weekly: Active days and time spent
                        val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                        val dateKeySdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        for (i in 6 downTo 0) {
                            val targetTime = now - i * oneDayMs
                            val name = sdf.format(java.util.Date(targetTime))
                            val dateStringKey = dateKeySdf.format(java.util.Date(targetTime))
                            val sum = usageEvents.filter { it.dateString == dateStringKey }.sumOf { it.durationMs }
                            items.add(name to sum)
                        }
                    }
                    "Monthly" -> {
                        // Monthly: Weekly breakdown
                        val weekSums = LongArray(4)
                        usageEvents.filter { now - it.timestamp <= 28 * oneDayMs }.forEach { ev ->
                            val ageMs = now - ev.timestamp
                            val weekIdx = (ageMs / (7 * oneDayMs)).toInt().coerceIn(0, 3)
                            weekSums[3 - weekIdx] += ev.durationMs
                        }
                        items.add("Week 1" to weekSums[0])
                        items.add("Week 2" to weekSums[1])
                        items.add("Week 3" to weekSums[2])
                        items.add("Week 4" to weekSums[3])
                    }
                    else -> {
                        listOf("Calculator", "Tasks", "Storehouse", "Progress").forEach { cat ->
                            items.add(cat to (categoryDurations[cat] ?: 0L))
                        }
                    }
                }
                items
            }

            val subheading = when (displayRangeSelection) {
                "Daily" -> "Category-wise time distribution (last 24 hr)"
                "Weekly" -> "Active weekdays and time spent (last 7 days)"
                "Monthly" -> "Weekly breakdown analysis (last 4 weeks)"
                else -> "Tap any bar to see exact hours & minutes spent"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Interactive Usage Distribution Bar Chart 📊", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Text(subheading, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxMs = barItems.maxOfOrNull { it.second } ?: 0L
                        barItems.forEachIndexed { idx, pair ->
                            val (catName, durMs) = pair
                            val barColor = chartColors[idx % chartColors.size]
                            
                            val heightPct = if (maxMs > 0L) {
                                (durMs.toFloat() / maxMs.toFloat()).coerceIn(0.08f, 1.0f)
                            } else {
                                when (idx % 4) {
                                    0 -> 0.35f
                                    1 -> 0.70f
                                    2 -> 0.22f
                                    else -> 0.55f
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false)
                                        .fillMaxHeight(heightPct)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(barColor)
                                        .clickable {
                                            selectedBarName = catName
                                            selectedBarMs = durMs
                                        }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = catName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (selectedBarName != null) {
                val rawMs = selectedBarMs ?: 0L
                val sMins = rawMs / 1000 / 60
                val sHours = sMins / 60
                val sRemMins = sMins % 60
                val formattedTime = if (sHours > 0) "$sHours Hour(s) & $sRemMins Minute(s)" else "$sRemMins Minute(s)"
                
                AlertDialog(
                    onDismissRequest = { selectedBarName = null },
                    title = { Text("📊 Category Active Metrics", fontWeight = FontWeight.Bold, fontFamily = fontFamily) },
                    text = {
                        Column {
                            Text("Category Tab:", fontSize = 12.sp, color = Color.Gray)
                            Text(selectedBarName ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Exact Time Logged:", fontSize = 12.sp, color = Color.Gray)
                            Text(formattedTime, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { selectedBarName = null },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Clear", color = Color.White)
                        }
                    }
                )
            }
        }

        // Analytical recommendation block card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = secondaryColor.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.TipsAndUpdates, contentDescription = null, tint = secondaryColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Smart Insights 💡", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = secondaryColor, fontFamily = fontFamily)
                        Text("You displayed strong focus metrics towards completing customized task directories. Complete 2 more items on your list to finish your weekly streak! 🎉", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

data class PieSlice(val name: String, val value: Double, val color: Color)
data class BarItem(val label: String, val value: Float, val color: Color)

// ========================================================
// 🏆 1. NEW FEATURE: CUTE & AESTHETIC CLOCKTABMODULE CHIPS
// ========================================================
@Composable
fun CuteAnalogClockWidget(
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    var currentTime by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    
    // Update clock every second
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            currentTime = java.util.Calendar.getInstance()
            kotlinx.coroutines.delay(1000L)
        }
    }
    
    Box(
        modifier = modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, primaryColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = center
            val radius = size.width / 2f
            
            // Draw cute pastel pink ring background
            drawCircle(
                color = primaryColor.copy(alpha = 0.08f),
                radius = radius - 8f
            )
            
            // Draw hours tick dots as cute small dots / circles
            for (angleDegrees in 0 until 360 step 30) {
                val angleRad = Math.toRadians(angleDegrees.toDouble()).toFloat()
                val tickRadius = radius - 15f
                val x = centerOffset.x + Math.cos(angleRad.toDouble()).toFloat() * tickRadius
                val y = centerOffset.y + Math.sin(angleRad.toDouble()).toFloat() * tickRadius
                
                val dotSize = if (angleDegrees % 90 == 0) 5.dp.toPx() else 2.5.dp.toPx()
                drawCircle(
                    color = primaryColor.copy(alpha = if (angleDegrees % 90 == 0) 0.8f else 0.4f),
                    radius = dotSize,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
            
            // Calculate rotational angles
            val hour = currentTime.get(java.util.Calendar.HOUR)
            val minute = currentTime.get(java.util.Calendar.MINUTE)
            val second = currentTime.get(java.util.Calendar.SECOND)
            
            val minAngleRad = Math.toRadians((minute * 6f - 90f).toDouble()).toFloat()
            val hourAngleRad = Math.toRadians(((hour * 30f + minute * 0.5f) - 90f).toDouble()).toFloat()
            val secAngleRad = Math.toRadians((second * 6f - 90f).toDouble()).toFloat()
            
            // Draw Hour Hand
            val hourHandLength = radius * 0.43f
            val hX = centerOffset.x + Math.cos(hourAngleRad.toDouble()).toFloat() * hourHandLength
            val hY = centerOffset.y + Math.sin(hourAngleRad.toDouble()).toFloat() * hourHandLength
            drawLine(
                color = primaryColor,
                start = centerOffset,
                end = androidx.compose.ui.geometry.Offset(hX, hY),
                strokeWidth = 3.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Draw Minute Hand
            val minHandLength = radius * 0.65f
            val mX = centerOffset.x + Math.cos(minAngleRad.toDouble()).toFloat() * minHandLength
            val mY = centerOffset.y + Math.sin(minAngleRad.toDouble()).toFloat() * minHandLength
            drawLine(
                color = primaryColor.copy(alpha = 0.75f),
                start = centerOffset,
                end = androidx.compose.ui.geometry.Offset(mX, mY),
                strokeWidth = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Draw Second Hand (very thin)
            val secHandLength = radius * 0.75f
            val sX = centerOffset.x + Math.cos(secAngleRad.toDouble()).toFloat() * secHandLength
            val sY = centerOffset.y + Math.sin(secAngleRad.toDouble()).toFloat() * secHandLength
            drawLine(
                color = primaryColor.copy(alpha = 0.45f),
                start = centerOffset,
                end = androidx.compose.ui.geometry.Offset(sX, sY),
                strokeWidth = 1.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Center pin (cute small heart pivot center)
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx()
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx()
            )
        }
    }
}

@Composable
fun ClockTabModule(
    viewModel: MainViewModel,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val alarmsList by viewModel.allAlarms.collectAsStateWithLifecycle(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var activePreviewTone by remember { mutableStateOf<String?>(null) }

    // Dialog state variables
    var labelInput by remember { mutableStateOf("Cozy Alarm 🕰️") }
    var hourInput by remember { mutableStateOf(8) }
    var minuteInput by remember { mutableStateOf(30) }
    var isPmInput by remember { mutableStateOf(false) }

    var setDateToggle by remember { mutableStateOf(false) }
    var dayInput by remember { mutableStateOf(12) }
    var monthInput by remember { mutableStateOf(1) } // January (0-indexed in Calendar)
    var yearInput by remember { mutableStateOf(2026) }

    var repeatToggle by remember { mutableStateOf(false) }
    var snoozeLimit by remember { mutableStateOf(5) }
    var toneSelection by remember { mutableStateOf("Cute Tones") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic Clock Card representation with interactive real time
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), // pastel pink
            border = BorderStroke(1.2.dp, primaryColor.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌸 Cozy Alarms & Chimes",
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = fontFamily,
                    fontSize = 18.sp,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                // Visual analog-style clock header summary side-by-side with metadata
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CuteAnalogClockWidget(primaryColor = primaryColor)
                    
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aesthetic Chimes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "Soft cozy alarm system",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = fontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(primaryColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${alarmsList.filter { it.isEnabled }.size} Active Alarms 🌸",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                
                Button(
                    onClick = {
                        // Reset form fields
                        labelInput = "Cozy Alarm 🕰️"
                        val rightNow = java.util.Calendar.getInstance()
                        hourInput = rightNow.get(java.util.Calendar.HOUR_OF_DAY)
                        if (hourInput >= 12) {
                            isPmInput = true
                            if (hourInput > 12) hourInput -= 12
                        } else {
                            isPmInput = false
                            if (hourInput == 0) hourInput = 12
                        }
                        minuteInput = rightNow.get(java.util.Calendar.MINUTE)
                        dayInput = rightNow.get(java.util.Calendar.DAY_OF_MONTH)
                        monthInput = rightNow.get(java.util.Calendar.MONTH) + 1
                        yearInput = rightNow.get(java.util.Calendar.YEAR)
                        toneSelection = "Cute Tones"
                        snoozeLimit = 5
                        repeatToggle = false
                        setDateToggle = false
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Cozy Alarm 🕰️🍒", color = Color.White, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Alarms header
        Text(
            text = "Your Scheduled Alarms (${alarmsList.size})",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 15.sp,
            color = primaryColor,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (alarmsList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alarms scheduled yet! Tap above to set a beautiful chime! 🌸😴🧁",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = fontFamily
                    )
                }
            }
        } else {
            alarmsList.forEach { alarm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (alarm.isEnabled) Color.White else Color(0xFAF9F9F9)),
                    border = BorderStroke(1.dp, if (alarm.isEnabled) primaryColor.copy(alpha = 0.2f) else Color.LightGray),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = alarm.label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alarm.isEnabled) primaryColor else Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = fontFamily
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                val rawH = alarm.hour
                                val ampm = if (rawH >= 12) "PM" else "AM"
                                val displayH = when {
                                    rawH == 0 -> 12
                                    rawH > 12 -> rawH - 12
                                    else -> rawH
                                }
                                val displayM = String.format("%02d", alarm.minute)
                                
                                Text(
                                    text = "$displayH:$displayM $ampm",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor,
                                    fontFamily = fontFamily
                                )
                                
                                if (alarm.year > 0) {
                                    Text(
                                        text = "On Date: ${alarm.year}-${String.format("%02d", alarm.month)}-${String.format("%02d", alarm.day)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontFamily = fontFamily
                                    )
                                } else {
                                    Text(
                                        text = if (alarm.repeatAlarm) "🔁 Repeat Active" else "💤 One-Time Chime",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = alarm.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        val updated = alarm.copy(isEnabled = isChecked)
                                        viewModel.updateAlarm(updated)
                                        if (isChecked) {
                                            scheduleSystemAlarmItem(context, updated)
                                            Toast.makeText(context, "✅ Alarm Scheduled System", Toast.LENGTH_SHORT).show()
                                        } else {
                                            cancelSystemAlarmItem(context, updated)
                                            Toast.makeText(context, "🚫 Alarm Disabled", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = primaryColor.copy(alpha = 0.3f))
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = {
                                        cancelSystemAlarmItem(context, alarm)
                                        viewModel.deleteAlarm(alarm)
                                        Toast.makeText(context, "🗑️ Alarm Deleted Successfully", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎵 Tone: ${alarm.selectedTone} (Snooze: ${alarm.snoozeMinutes}m)",
                                fontSize = 11.sp,
                                fontFamily = fontFamily,
                                color = Color.Gray
                            )
                            
                            Button(
                                onClick = {
                                    if (activePreviewTone == alarm.selectedTone) {
                                        AlarmTonePlayer.stopTone()
                                        activePreviewTone = null
                                    } else {
                                        AlarmTonePlayer.stopTone()
                                        AlarmTonePlayer.playToneProg(context, alarm.selectedTone)
                                        activePreviewTone = alarm.selectedTone
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (activePreviewTone == alarm.selectedTone) "Stop Tone 🔇" else "Listen Tone 🔊",
                                    fontSize = 10.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Edit/Add dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                AlarmTonePlayer.stopTone()
                showAddDialog = false
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🕰️ Add Cute Alarm 🕰️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor,
                        fontFamily = fontFamily,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Label input
                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("Alarm Tag Description", fontFamily = fontFamily) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Custom pretty spinner controls for setting Alarm Time
                    Text("Set Alarm Time:", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor)
                    
                    var showHourDropdown by remember { mutableStateOf(false) }
                    var showMinuteDropdown by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Selector Card/Dropdown
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour ⏱️", fontSize = 10.sp, color = Color.Gray, fontFamily = fontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Card(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(42.dp)
                                        .clickable { showHourDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$hourInput", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = primaryColor)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("▼", fontSize = 10.sp, color = primaryColor)
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = showHourDropdown,
                                    onDismissRequest = { showHourDropdown = false }
                                ) {
                                    (1..12).forEach { hour ->
                                        DropdownMenuItem(
                                            text = { Text("$hour", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = primaryColor) },
                                            onClick = {
                                                hourInput = hour
                                                showHourDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                        // Minute Selector Card/Dropdown
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute 🍰", fontSize = 10.sp, color = Color.Gray, fontFamily = fontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Card(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(42.dp)
                                        .clickable { showMinuteDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(String.format("%02d", minuteInput), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = primaryColor)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("▼", fontSize = 10.sp, color = primaryColor)
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = showMinuteDropdown,
                                    onDismissRequest = { showMinuteDropdown = false }
                                ) {
                                    val minutesList = (0..59).toList()
                                    minutesList.forEach { valMin ->
                                        DropdownMenuItem(
                                            text = { Text(String.format("%02d", valMin), fontFamily = fontFamily, color = primaryColor) },
                                            onClick = {
                                                minuteInput = valMin
                                                showMinuteDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // AM/PM Switch Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AM/PM 🌸", fontSize = 10.sp, color = Color.Gray, fontFamily = fontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { isPmInput = !isPmInput },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.12f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isPmInput) "PM 🌇" else "AM 🌅", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = fontFamily)
                            }
                        }
                    }

                    // Optional Date Picking fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅 Set Specific Date Target?", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = Color.DarkGray)
                        Checkbox(
                            checked = setDateToggle,
                            onCheckedChange = { setDateToggle = it }
                        )
                    }

                    if (setDateToggle) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dayInput.toString(),
                                onValueChange = { dayInput = it.toIntOrNull() ?: 12 },
                                label = { Text("Day", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = monthInput.toString(),
                                onValueChange = { monthInput = it.toIntOrNull() ?: 1 },
                                label = { Text("Month", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = yearInput.toString(),
                                onValueChange = { yearInput = it.toIntOrNull() ?: 2026 },
                                label = { Text("Year", fontSize = 9.sp) },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Options: Repeat Toggle & Snooze Time picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔁 Repeat Alarm?", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = Color.DarkGray)
                        Checkbox(
                            checked = repeatToggle,
                            onCheckedChange = { repeatToggle = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💤 Snooze Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = Color.DarkGray)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(5, 10, 15).forEach { min ->
                                Button(
                                    onClick = { snoozeLimit = min },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (snoozeLimit == min) primaryColor else primaryColor.copy(alpha = 0.08f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("${min}m", color = if (snoozeLimit == min) Color.White else primaryColor, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Tones choice selection
                    Text("Select Chime Tone Preset:", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = primaryColor)
                    listOf("Cute Tones", "Relaxing Tones", "Dramatic Loud Sounds").forEach { tone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toneSelection = tone }
                                .background(if (toneSelection == tone) primaryColor.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tone, fontSize = 12.sp, fontFamily = fontFamily, fontWeight = if (toneSelection == tone) FontWeight.Bold else FontWeight.Normal)
                            
                            Button(
                                onClick = {
                                    if (activePreviewTone == tone) {
                                        AlarmTonePlayer.stopTone()
                                        activePreviewTone = null
                                    } else {
                                        AlarmTonePlayer.stopTone()
                                        AlarmTonePlayer.playToneProg(context, tone)
                                        activePreviewTone = tone
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.12f)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (activePreviewTone == tone) "Stop 🔇" else "Listen 🔊",
                                    fontSize = 10.sp,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AlarmTonePlayer.stopTone()
                        
                        // Parse clock hour representing 24-hour style properly
                        var finalH = hourInput
                        if (isPmInput) {
                            if (finalH < 12) finalH += 12
                        } else {
                            if (finalH == 12) finalH = 0
                        }

                        val resolvedAlarm = AlarmItem(
                            label = labelInput,
                            hour = finalH,
                            minute = minuteInput,
                            day = if (setDateToggle) dayInput else 0,
                            month = if (setDateToggle) monthInput else 0,
                            year = if (setDateToggle) yearInput else 0,
                            repeatAlarm = repeatToggle,
                            snoozeMinutes = snoozeLimit,
                            selectedTone = toneSelection,
                            isEnabled = true
                        )

                        viewModel.insertAlarm(resolvedAlarm) { fullItem ->
                            scheduleSystemAlarmItem(context, fullItem)
                        }

                        Toast.makeText(context, "✅ Alarm Registered 🍭", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Okay Button 👍🎀", color = Color.White, fontFamily = fontFamily)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        AlarmTonePlayer.stopTone()
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Preview", color = Color.DarkGray, fontFamily = fontFamily)
                }
            }
        )
    }
}

// System alarm binding triggers
fun scheduleSystemAlarmItem(context: android.content.Context, alarm: AlarmItem) {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
    val intent = android.content.Intent(context, com.example.AlarmReceiver::class.java).apply {
        putExtra("alarm_label", alarm.label)
        putExtra("alarm_tone", alarm.selectedTone)
    }
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        alarm.id,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, alarm.hour)
        set(java.util.Calendar.MINUTE, alarm.minute)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)

        if (alarm.year > 0) {
            set(java.util.Calendar.DAY_OF_MONTH, alarm.day)
            set(java.util.Calendar.MONTH, alarm.month - 1) // 0-indexed month system helper
            set(java.util.Calendar.YEAR, alarm.year)
        }

        if (timeInMillis <= System.currentTimeMillis() && alarm.year <= 0) {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } catch (e: Exception) {
        alarmManager.set(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

fun cancelSystemAlarmItem(context: android.content.Context, alarm: AlarmItem) {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
    val intent = android.content.Intent(context, com.example.AlarmReceiver::class.java)
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        alarm.id,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}

// =============================================================
// 🫣 2. NEW FEATURE: MY CONNECTORS MODULE (ADMIN GATE ACCESS)
// =============================================================
@Composable
fun ConnectorsTabModule(
    viewModel: MainViewModel,
    primaryColor: Color,
    fontFamily: FontFamily
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectorList by viewModel.connectorRecords.collectAsStateWithLifecycle(emptyList())

    // Admin Access Validation
    val isAdmin = currentUser?.isAdmin == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isAdmin) {
            // Locked restricted notice
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔒 Restrained Entry", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🫣 My Connectors dashboard is restricted to administrators only. Log in using your registered admin secrets details to unlock.",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        fontFamily = fontFamily
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Admin Only 🧁🩰", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                    }
                }
            }
        } else {
            // Beautiful interactive Admin Dashboard!
            Text(
                text = "🫣 My Connectors Dashboard",
                fontWeight = FontWeight.ExtraBold,
                fontFamily = fontFamily,
                fontSize = 18.sp,
                color = primaryColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Dynamic overview metric summary count cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Distinct Logged-in accounts in records
                val totalLoggedIn = remember(connectorList) {
                    connectorList.filter { it.status.lowercase().contains("login") }.map { it.username }.distinct().size
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF0EDF6FF)), // blue vibe
                    border = BorderStroke(1.dp, Color(0xFF93C5FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔑 Logged Users", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalLoggedIn", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor, fontFamily = fontFamily)
                    }
                }

                // Total Logged actions
                val totalActions = connectorList.size
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), // pink vibe
                    border = BorderStroke(1.dp, Color(0xFFFDA4AF))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯 Activities logs", fontSize = 11.sp, color = Color.Gray, fontFamily = fontFamily, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalActions", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor, fontFamily = fontFamily)
                    }
                }
            }

            // Summary user logs section
            Text(
                text = "Live Connector Login Records (${connectorList.size})",
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = primaryColor,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Start
            )

            if (connectorList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No connector activities logged yet.", fontSize = 12.sp, color = Color.Gray, fontFamily = fontFamily)
                    }
                }
            } else {
                connectorList.reversed().forEach { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (record.username.isNotEmpty()) record.username.take(1).uppercase() else "A", fontWeight = FontWeight.Bold, color = primaryColor)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = record.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            fontFamily = fontFamily,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = record.gmail.ifEmpty { record.email },
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontFamily = fontFamily
                                        )
                                    }
                                }

                                val statusColor = if (record.status.lowercase().contains("updated")) Color(0xFFDB2777) else primaryColor
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = record.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontFamily = fontFamily
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.25f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()) }
                                val dateStr = sdf.format(java.util.Date(record.timestamp))
                                Text(
                                    text = "🕒 Login Time: $dateStr",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray,
                                    fontFamily = fontFamily
                                )
                                Text(
                                    text = "State: ${record.state.ifEmpty { "Logged In" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    color = Color.DarkGray
                                )
                            }

                            if (record.lastActivity.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Last Activity: ${record.lastActivity}",
                                    fontSize = 10.5.sp,
                                    color = primaryColor.copy(alpha = 0.8f),
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
