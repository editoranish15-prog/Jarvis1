package com.example.ui

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.JarvisDatabase
import com.example.data.model.SystemTelemetry
import com.example.data.model.VoiceState
import com.example.data.preferences.JarvisPreferences
import com.example.telemetry.SystemTelemetryManager
import com.example.ui.components.HudBackground
import com.example.ui.components.JarvisBottomNav
import com.example.ui.components.JarvisNavDestination
import com.example.ui.components.TopJarvisBar
import com.example.ui.screens.brain.BrainModeScreen
import com.example.ui.screens.brain.BrainViewModel
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.devicelink.DeviceLinkScreen
import com.example.ui.screens.devicelink.DeviceLinkViewModel
import com.example.ui.screens.history.ConversationHistoryScreen
import com.example.ui.screens.history.ConversationHistoryViewModel
import com.example.ui.screens.screenmode.ScreenModeCommandCenterScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tools.SmartToolsScreen
import com.example.ui.screens.tools.SmartToolsViewModel
import com.example.ui.theme.VoidBlack
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.launch

@Composable
fun JarvisApp(
    chatViewModel: ChatViewModel = viewModel(),
    brainViewModel: BrainViewModel = viewModel(),
    deviceLinkViewModel: DeviceLinkViewModel = viewModel(),
    smartToolsViewModel: SmartToolsViewModel = viewModel(),
    historyViewModel: ConversationHistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { JarvisPreferences(context) }
    val settings by preferences.settings.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(JarvisNavDestination.HOME) }
    var isInDedicatedScreenMode by remember { mutableStateOf(false) }

    // Telemetry Manager
    val telemetryManager = remember { SystemTelemetryManager(context) }
    var telemetry by remember { mutableStateOf(telemetryManager.getTelemetrySnapshot()) }

    LaunchedEffect(Unit) {
        telemetryManager.pollTelemetryFlow(2500L).collect { snapshot ->
            telemetry = snapshot
        }
    }

    var latestSpeechTranscript by remember { mutableStateOf("") }

    // Voice Manager
    lateinit var voiceManagerRef: JarvisVoiceManager
    val voiceManager = remember {
        JarvisVoiceManager(context) { transcript ->
            latestSpeechTranscript = transcript
            // If user speaks, automatically send to Gemini chat and speak the answer
            chatViewModel.sendMessage(transcript) { responseText ->
                if (settings.autoSpeakResponses) {
                    voiceManagerRef.speak(responseText)
                }
            }
        }.also { voiceManagerRef = it }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    // Keep voice manager pitch & rate in sync with settings
    LaunchedEffect(settings.voicePitch, settings.voiceRate) {
        voiceManager.pitch = settings.voicePitch
        voiceManager.speechRate = settings.voiceRate
    }

    var hasAnnouncedStartupGreeting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        if (!hasAnnouncedStartupGreeting && settings.autoSpeakResponses) {
            hasAnnouncedStartupGreeting = true
            val startupGreeting = com.example.data.model.JarvisGreetings.generateGreeting(settings.userHonorific)
            voiceManager.speak(startupGreeting)
        }
    }

    val voiceState by voiceManager.voiceState.collectAsStateWithLifecycle()
    val audioRms by voiceManager.audioRms.collectAsStateWithLifecycle()
    val isMuted by voiceManager.isMuted.collectAsStateWithLifecycle()

    // Microphone permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice command.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startListeningWithPermission() {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Back handling for sub-screens
    if (isInDedicatedScreenMode) {
        BackHandler {
            isInDedicatedScreenMode = false
        }
    } else if (currentDestination != JarvisNavDestination.HOME) {
        BackHandler {
            currentDestination = JarvisNavDestination.HOME
        }
    }

    if (isInDedicatedScreenMode) {
        // Fullscreen Dedicated 16:9 Screen Mode
        ScreenModeCommandCenterScreen(
            settings = settings,
            telemetry = telemetry,
            voiceState = voiceState,
            audioRms = audioRms,
            latestTranscript = latestSpeechTranscript,
            onToggleVoice = {
                if (voiceState == VoiceState.LISTENING) {
                    voiceManager.stopListening()
                } else {
                    startListeningWithPermission()
                }
            },
            onStopSpeaking = { voiceManager.stopSpeaking() },
            onUpdateEyeTheme = { newTheme ->
                preferences.updateSettings(settings.copy(eyeTheme = newTheme))
            },
            onExitScreenMode = {
                isInDedicatedScreenMode = false
            }
        )
    } else {
        HudBackground(
            showGrid = settings.hudGridEnabled,
            showScanlines = settings.scanlinesOverlay,
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                topBar = {
                    TopJarvisBar(
                        isMuted = isMuted,
                        modelName = settings.aiModel,
                        onToggleMute = { voiceManager.toggleMute() },
                        onEnterScreenMode = { isInDedicatedScreenMode = true }
                    )
                },
                bottomBar = {
                    JarvisBottomNav(
                        currentDestination = currentDestination,
                        onNavigate = { dest ->
                            if (dest == JarvisNavDestination.SCREEN_MODE) {
                                isInDedicatedScreenMode = true
                            } else {
                                currentDestination = dest
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        JarvisNavDestination.HOME -> DashboardScreen(
                            settings = settings,
                            telemetry = telemetry,
                            voiceState = voiceState,
                            audioRms = audioRms,
                            latestVoiceTranscript = latestSpeechTranscript,
                            onFaceClicked = {
                                if (voiceState == VoiceState.IDLE) {
                                    startListeningWithPermission()
                                } else {
                                    voiceManager.stopListening()
                                }
                            },
                            onToggleVoiceListening = {
                                if (voiceState == VoiceState.LISTENING) {
                                    voiceManager.stopListening()
                                } else {
                                    startListeningWithPermission()
                                }
                            },
                            onStopSpeaking = { voiceManager.stopSpeaking() },
                            onSpeakGreeting = { phrase -> voiceManager.speak(phrase) },
                            onNavigate = { dest ->
                                if (dest == JarvisNavDestination.SCREEN_MODE) {
                                    isInDedicatedScreenMode = true
                                } else {
                                    currentDestination = dest
                                }
                            }
                        )

                        JarvisNavDestination.CHAT -> ChatScreen(
                            viewModel = chatViewModel,
                            onSpeakText = { text ->
                                if (settings.autoSpeakResponses) {
                                    voiceManager.speak(text)
                                }
                            },
                            onStopSpeaking = { voiceManager.stopSpeaking() },
                            onStartVoiceInput = { startListeningWithPermission() },
                            speechInputText = latestSpeechTranscript
                        )

                        JarvisNavDestination.SCREEN_MODE -> {
                            // Handled via dedicated screen mode toggle above
                        }

                        JarvisNavDestination.BRAIN -> BrainModeScreen(
                            viewModel = brainViewModel
                        )

                        JarvisNavDestination.DEVICE_LINK -> DeviceLinkScreen(
                            viewModel = deviceLinkViewModel
                        )

                        JarvisNavDestination.TOOLS -> SmartToolsScreen(
                            viewModel = smartToolsViewModel
                        )

                        JarvisNavDestination.HISTORY -> ConversationHistoryScreen(
                            viewModel = historyViewModel,
                            onSelectSession = { sessionId ->
                                chatViewModel.loadSession(sessionId)
                                currentDestination = JarvisNavDestination.CHAT
                            }
                        )

                        JarvisNavDestination.SETTINGS -> SettingsScreen(
                            preferences = preferences,
                            settings = settings,
                            onTestVoice = { testPhrase ->
                                voiceManager.speak(testPhrase)
                            },
                            onPurgeAllData = {
                                scope.launch {
                                    val db = JarvisDatabase.getInstance(context)
                                    db.chatDao().clearAllSessions()
                                    db.memoryDao().clearAllMemories()
                                    db.noteDao().clearAllNotes()
                                    db.taskDao().clearAllTasks()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
