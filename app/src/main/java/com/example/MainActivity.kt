package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.data.model.AiTool
import com.example.obinssia.data.repository.ObinssRepository
import com.example.obinssia.service.SpeechService
import com.example.obinssia.ui.components.FileImportDialog
import com.example.obinssia.ui.components.NotificationsDialog
import com.example.obinssia.ui.components.OnboardingDialog
import com.example.obinssia.ui.components.ToolExecutionDialog
import com.example.obinssia.ui.components.VoiceDialog
import com.example.obinssia.ui.screens.AdminScreen
import com.example.obinssia.ui.screens.ChatScreen
import com.example.obinssia.ui.screens.ForgotPasswordScreen
import com.example.obinssia.ui.screens.HistoryScreen
import com.example.obinssia.ui.screens.HomeScreen
import com.example.obinssia.ui.screens.InvoicesScreen
import com.example.obinssia.ui.screens.LoginScreen
import com.example.obinssia.ui.screens.ProfileScreen
import com.example.obinssia.ui.screens.RegisterScreen
import com.example.obinssia.ui.screens.SubscriptionScreen
import com.example.obinssia.ui.screens.ToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSurface
import com.example.ui.theme.ObinssSurfaceVariant
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    CHAT("Chat", Icons.Default.ChatBubble),
    TOOLS("Outils", Icons.Default.AutoAwesome),
    HISTORY("Historique", Icons.Default.History),
    PROFILE("Profil", Icons.Default.Person),
    SUBSCRIPTION("Tarifs", Icons.Default.Diamond),
    ADMIN("Admin", Icons.Default.AutoAwesome),
    INVOICES("Factures", Icons.Default.Receipt),
    LOGIN("Connexion", Icons.Default.Lock),
    REGISTER("Inscription", Icons.Default.Person),
    FORGOT_PASSWORD("Récupération", Icons.Default.Lock)
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: ObinssRepository
    private lateinit var speechService: SpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ObinssRepository(applicationContext)
        speechService = SpeechService(applicationContext)

        setContent {
            val userProfile by repository.getUserProfile().collectAsState(initial = null)
            val isDarkMode = userProfile?.isDarkMode ?: true

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContainer(
                    repository = repository,
                    speechService = speechService
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService.release()
    }
}

@Composable
fun MainAppContainer(
    repository: ObinssRepository,
    speechService: SpeechService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val userProfile by repository.getUserProfile().collectAsState(initial = null)
    val conversations by repository.getAllConversations().collectAsState(initial = emptyList())
    val unreadNotificationsCount by repository.getUnreadNotificationsCount().collectAsState(initial = 0)

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var chatInitialPrompt by remember { mutableStateOf<String?>(null) }
    var selectedAiMode by remember { mutableStateOf(AiMode.RAPIDE) }

    // Sync active conversation when conversations load
    LaunchedEffect(conversations) {
        if (activeConversationId == null && conversations.isNotEmpty()) {
            activeConversationId = conversations.first().id
        }
    }

    // Dialog controllers
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showFileImportDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var selectedToolForExecution by remember { mutableStateOf<AiTool?>(null) }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceDialog = true
            speechService.startListening("fr-FR") { /* handled inside dialog */ }
        } else {
            Toast.makeText(context, "Permission micro requise pour la commande vocale.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openVoiceWithPermissionCheck() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showVoiceDialog = true
            speechService.startListening("fr-FR") {}
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun navigateToChatWithPrompt(prompt: String, mode: AiMode = selectedAiMode) {
        scope.launch {
            val convId = repository.createConversation("Nouvelle conversation", mode)
            activeConversationId = convId
            selectedAiMode = mode
            chatInitialPrompt = prompt
            currentScreen = AppScreen.CHAT
        }
    }

    // Modal Dialogs
    if (showOnboardingDialog) {
        OnboardingDialog(
            onGoalSelected = { goal ->
                scope.launch {
                    repository.updateFirstGoal(goal)
                }
            },
            onDismiss = { showOnboardingDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            repository = repository,
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showVoiceDialog) {
        val isListening by speechService.isListening.collectAsState()
        val transcribedText by speechService.transcribedText.collectAsState()

        VoiceDialog(
            isListening = isListening,
            transcribedText = transcribedText,
            onStartListening = { speechService.startListening("fr-FR") {} },
            onStopListening = { speechService.stopListening() },
            onSendText = { text ->
                navigateToChatWithPrompt(text)
            },
            onDismiss = {
                speechService.stopListening()
                showVoiceDialog = false
            }
        )
    }

    if (showFileImportDialog) {
        FileImportDialog(
            repository = repository,
            onDocumentAnalyzed = { prompt, docName ->
                navigateToChatWithPrompt(prompt)
            },
            onDismiss = { showFileImportDialog = false }
        )
    }

    selectedToolForExecution?.let { tool ->
        ToolExecutionDialog(
            tool = tool,
            repository = repository,
            onOpenInChat = { prompt ->
                navigateToChatWithPrompt(prompt)
            },
            onDismiss = { selectedToolForExecution = null }
        )
    }

    val isAuthOrAdminScreen = currentScreen in listOf(
        AppScreen.ADMIN,
        AppScreen.LOGIN,
        AppScreen.REGISTER,
        AppScreen.FORGOT_PASSWORD,
        AppScreen.INVOICES
    )

    // Multi-device Responsive layout
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= 600.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isAuthOrAdminScreen,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Brand Logo in drawer
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObinssRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("O", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "𝐎𝐁𝐈𝐍’𝐒𝐒 IA",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // New Chat drawer button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObinssRed.copy(alpha = 0.15f))
                                .border(1.dp, ObinssRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                        val convId = repository.createConversation("Nouvelle conversation", selectedAiMode)
                                        activeConversationId = convId
                                        chatInitialPrompt = null
                                        currentScreen = AppScreen.CHAT
                                    }
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Nouvelle conversation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Conversations récentes", fontSize = 11.sp, color = ObinssGold, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        // List recent conversations in drawer
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            conversations.take(6).forEach { conv ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            scope.launch {
                                                drawerState.close()
                                                activeConversationId = conv.id
                                                currentScreen = AppScreen.CHAT
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = ObinssTextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(conv.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, maxLines = 1)
                                }
                            }
                        }

                        // Drawer Footer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1E28))
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                        currentScreen = AppScreen.SUBSCRIPTION
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Plan ${currentUser?.plan ?: userProfile?.plan ?: "FREE"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Mettre à niveau (PRO)", color = ObinssGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Desktop / Tablet Navigation Rail
                if (isLargeScreen && !isAuthOrAdminScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(72.dp)
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObinssRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("O", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        listOf(
                            AppScreen.HOME,
                            AppScreen.CHAT,
                            AppScreen.TOOLS,
                            AppScreen.HISTORY,
                            AppScreen.PROFILE
                        ).forEach { screen ->
                            NavigationRailItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, fontSize = 10.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = ObinssRedLight,
                                    indicatorColor = ObinssRed,
                                    unselectedIconColor = ObinssTextSecondary,
                                    unselectedTextColor = ObinssTextSecondary
                                )
                            )
                        }
                    }
                }

                // Main Content Screen
                Scaffold(
                    modifier = Modifier.weight(1f),
                    bottomBar = {
                        if (!isLargeScreen && !isAuthOrAdminScreen) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                listOf(
                                    AppScreen.HOME,
                                    AppScreen.CHAT,
                                    AppScreen.TOOLS,
                                    AppScreen.HISTORY,
                                    AppScreen.PROFILE
                                ).forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = ObinssRedLight,
                                            indicatorColor = ObinssRed,
                                            unselectedIconColor = ObinssTextSecondary,
                                            unselectedTextColor = ObinssTextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                            when (screen) {
                                AppScreen.HOME -> HomeScreen(
                                    selectedMode = selectedAiMode,
                                    onModeSelected = { selectedAiMode = it },
                                    onStartChat = { prompt, mode ->
                                        navigateToChatWithPrompt(prompt, mode)
                                    },
                                    onOpenVoice = { openVoiceWithPermissionCheck() },
                                    onOpenFileImport = { showFileImportDialog = true },
                                    onOpenPremium = { currentScreen = AppScreen.SUBSCRIPTION },
                                    onOpenAdmin = { currentScreen = AppScreen.ADMIN },
                                    userPlan = currentUser?.plan ?: userProfile?.plan ?: "FREE",
                                    selectedGoal = currentUser?.selectedFirstGoal ?: userProfile?.selectedFirstGoal ?: "",
                                    unreadNotificationsCount = unreadNotificationsCount,
                                    onNotificationsClick = { showNotificationsDialog = true }
                                )

                                AppScreen.CHAT -> {
                                    val convId = activeConversationId ?: conversations.firstOrNull()?.id
                                    if (convId == null) {
                                        LaunchedEffect(Unit) {
                                            val newId = repository.createConversation("Nouvelle conversation", selectedAiMode)
                                            activeConversationId = newId
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            androidx.compose.material3.CircularProgressIndicator(
                                                color = ObinssRed
                                            )
                                        }
                                    } else {
                                        ChatScreen(
                                            conversationId = convId,
                                            repository = repository,
                                            speechService = speechService,
                                            onNewChat = {
                                                scope.launch {
                                                    val createdId = repository.createConversation("Nouvelle conversation", selectedAiMode)
                                                    activeConversationId = createdId
                                                    chatInitialPrompt = null
                                                }
                                            },
                                            onOpenVoice = { openVoiceWithPermissionCheck() },
                                            onOpenFileImport = { showFileImportDialog = true },
                                            initialPrompt = chatInitialPrompt
                                        )
                                    }
                                }

                                AppScreen.TOOLS -> ToolsScreen(
                                    onSelectTool = { tool ->
                                        selectedToolForExecution = tool
                                    }
                                )

                                AppScreen.HISTORY -> HistoryScreen(
                                    repository = repository,
                                    onSelectConversation = { convId ->
                                        activeConversationId = convId
                                        currentScreen = AppScreen.CHAT
                                    }
                                )

                                AppScreen.PROFILE -> ProfileScreen(
                                    repository = repository,
                                    onOpenPremium = { currentScreen = AppScreen.SUBSCRIPTION },
                                    onOpenAdmin = { currentScreen = AppScreen.ADMIN },
                                    onOpenAuth = { currentScreen = AppScreen.LOGIN },
                                    onViewInvoices = { currentScreen = AppScreen.INVOICES }
                                )

                                AppScreen.SUBSCRIPTION -> SubscriptionScreen(
                                    currentPlan = currentUser?.plan ?: userProfile?.plan ?: "FREE",
                                    repository = repository,
                                    onPlanChanged = { currentScreen = AppScreen.PROFILE },
                                    onViewInvoices = { currentScreen = AppScreen.INVOICES }
                                )

                                AppScreen.INVOICES -> InvoicesScreen(
                                    repository = repository,
                                    onBack = { currentScreen = AppScreen.PROFILE }
                                )

                                AppScreen.ADMIN -> AdminScreen(
                                    repository = repository,
                                    onBack = { currentScreen = AppScreen.HOME }
                                )

                                AppScreen.LOGIN -> LoginScreen(
                                    repository = repository,
                                    onLoginSuccess = { currentScreen = AppScreen.PROFILE },
                                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                                    onNavigateToForgotPassword = { currentScreen = AppScreen.FORGOT_PASSWORD },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )

                                AppScreen.REGISTER -> RegisterScreen(
                                    repository = repository,
                                    onRegisterSuccess = { currentScreen = AppScreen.PROFILE },
                                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                                    onBack = { currentScreen = AppScreen.LOGIN }
                                )

                                AppScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                                    repository = repository,
                                    onNavigateBackToLogin = { currentScreen = AppScreen.LOGIN },
                                    onBack = { currentScreen = AppScreen.LOGIN }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
