package com.example.ui.screens.screenmode

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.SystemTelemetry
import com.example.data.model.VoiceState
import com.example.data.preferences.JarvisSettings
import com.example.ui.components.CircularTelemetryDial
import com.example.ui.components.HudBackground
import com.example.ui.components.HudGlassCard
import com.example.ui.components.JarvisRoboticFace
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoloAmber
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.QuantumTeal
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScreenModeCommandCenterScreen(
    settings: JarvisSettings,
    telemetry: SystemTelemetry,
    voiceState: VoiceState,
    audioRms: Float,
    latestTranscript: String,
    onToggleVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onUpdateEyeTheme: (String) -> Unit,
    onExitScreenMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isLandscapeLocked by remember { mutableStateOf(false) }

    // Support hardware orientation toggle or automatic 16:9 container adaptation
    fun toggleOrientationLock() {
        val next = !isLandscapeLocked
        isLandscapeLocked = next
        activity?.requestedOrientation = if (next) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onExitScreenMode()
    }

    // Dynamic Millisecond Digital Clock
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentTimeString = dateFormat.format(Date())
            delay(500)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "screen_mode_glow")
    val hudPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hudPulse"
    )

    HudBackground(
        showGrid = settings.hudGridEnabled,
        showScanlines = settings.scanlinesOverlay,
        modifier = modifier.fillMaxSize().background(VoidBlack)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > maxHeight

            // If in portrait, center with a 16:9 command center aspect ratio or fill if landscape
            Box(
                modifier = if (isWideScreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .align(Alignment.Center)
                }
                    .background(DeepNavy.copy(alpha = 0.95f))
                    .border(2.dp, HologramBorder)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Top HUD Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceGlass)
                            .border(1.dp, HologramBorder.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(QuantumTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JARVIS TACTICAL COMMAND CENTER • 16:9 SCREEN MODE",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanBright
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$currentTimeString LOCAL",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanBright
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { toggleOrientationLock() },
                                modifier = Modifier
                                    .testTag("rotate_screen_mode_button")
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate Display",
                                    tint = CyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                    onExitScreenMode()
                                },
                                modifier = Modifier
                                    .testTag("exit_screen_mode_button")
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Screen Mode",
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Three-Column Tactical Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left Wing: Telemetry & Hardware Gauges
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceGlass)
                                .border(1.dp, HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SYSTEM TELEMETRY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanBright
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CircularTelemetryDial(
                                        value = telemetry.batteryPercent / 100f,
                                        label = "POWER",
                                        valueText = "${telemetry.batteryPercent}%",
                                        gaugeColor = QuantumTeal,
                                        size = 54.dp
                                    )
                                    CircularTelemetryDial(
                                        value = if (telemetry.totalRamMb > 0) ((telemetry.totalRamMb - telemetry.availableRamMb).toFloat() / telemetry.totalRamMb) else 0.4f,
                                        label = "MEMORY",
                                        valueText = "${telemetry.availableRamMb}M",
                                        gaugeColor = ElectricBlue,
                                        size = 54.dp
                                    )
                                }

                                Column {
                                    Text(
                                        text = "NET: ${telemetry.networkStatus}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextCyan
                                    )
                                    Text(
                                        text = "UPTIME: ${telemetry.systemUptime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "NEURAL LATENCY: 42ms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        // Center Wing: Giant Animated Robotic Face
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .border(1.dp, CyanGlow.copy(alpha = 0.5f * hudPulse), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                JarvisRoboticFace(
                                    voiceState = voiceState,
                                    audioRms = audioRms,
                                    eyeTheme = settings.eyeTheme,
                                    size = if (isWideScreen) 180.dp else 140.dp,
                                    onClick = onToggleVoice,
                                    modifier = Modifier.testTag("screen_mode_robotic_face")
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val statusText = when (voiceState) {
                                    VoiceState.LISTENING -> "LISTENING • AUDIO RECEPTIVE"
                                    VoiceState.THINKING -> "ANALYZING NEURAL QUERY"
                                    VoiceState.SPEAKING -> "VOCALIZING RESPONSE"
                                    VoiceState.MUTED -> "AUDIO MUTED"
                                    VoiceState.IDLE -> "CORE ARMED • READY"
                                }

                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanBright
                                )
                            }
                        }

                        // Right Wing: Tactical Controls & Color Theme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceGlass)
                                .border(1.dp, HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "AI CONTROLS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanBright
                                )

                                if (latestTranscript.isNotBlank()) {
                                    Text(
                                        text = "\"$latestTranscript\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        maxLines = 2
                                    )
                                } else {
                                    Text(
                                        text = "ENGINE: ${settings.aiModel.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextCyan
                                    )
                                }

                                // Quick Voice Activation Button
                                Button(
                                    onClick = onToggleVoice,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (voiceState == VoiceState.LISTENING) AlertCrimson else CyanGlow,
                                        contentColor = VoidBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .testTag("screen_mode_voice_toggle")
                                        .fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (voiceState == VoiceState.LISTENING) "STOP" else "VOICE LINK",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                // Eye Theme Color Selector
                                Column {
                                    Text(
                                        text = "EYE VISOR THEME",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val themes = listOf(
                                            "CYAN" to CyanGlow,
                                            "BLUE" to ElectricBlue,
                                            "AMBER" to HoloAmber,
                                            "GREEN" to QuantumTeal,
                                            "RED" to AlertCrimson
                                        )
                                        themes.forEach { (themeKey, color) ->
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        width = if (settings.eyeTheme == themeKey) 2.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { onUpdateEyeTheme(themeKey) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
