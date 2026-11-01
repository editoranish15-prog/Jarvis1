package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.JarvisGreetings
import com.example.data.model.SystemTelemetry
import com.example.data.model.VoiceState
import com.example.data.preferences.JarvisSettings
import com.example.ui.components.HudGlassCard
import com.example.ui.components.JarvisNavDestination
import com.example.ui.components.JarvisRoboticFace
import com.example.ui.components.SystemTelemetryRow
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoloAmber
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.QuantumTeal
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    settings: JarvisSettings,
    telemetry: SystemTelemetry,
    voiceState: VoiceState,
    audioRms: Float,
    latestVoiceTranscript: String,
    onFaceClicked: () -> Unit,
    onToggleVoiceListening: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSpeakGreeting: (String) -> Unit,
    onNavigate: (JarvisNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val greetingInfo = remember(settings.userHonorific) {
        com.example.data.model.JarvisGreetings.getGreetingInfo(settings.userHonorific)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Personalized Sci-Fi Greeting Card with Real-Time Protocol
        HudGlassCard(
            title = "Neural Status",
            statusTag = greetingInfo.salutation.uppercase(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${greetingInfo.currentTimeFormatted} • ${greetingInfo.timeRangeDescription.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanBright
                    )
                }

                IconButton(
                    onClick = { onSpeakGreeting(greetingInfo.fullGreeting) },
                    modifier = Modifier.size(28.dp).testTag("speak_greeting_button")
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.VolumeUp,
                        contentDescription = "Vocalize greeting",
                        tint = CyanBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = greetingInfo.fullGreeting,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Time-adjusted protocol active. Ready for voice or neural query, ${settings.userHonorific}.",
                style = MaterialTheme.typography.bodySmall,
                color = TextCyan
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Central Animated Robotic Face
        JarvisRoboticFace(
            voiceState = voiceState,
            audioRms = audioRms,
            eyeTheme = settings.eyeTheme,
            size = 210.dp,
            onClick = onFaceClicked,
            modifier = Modifier.testTag("jarvis_robotic_face")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Voice state pill indicator
        val voiceStateLabel = when (voiceState) {
            VoiceState.LISTENING -> "LISTENING TO COMMAND..."
            VoiceState.THINKING -> "PROCESSING NEURAL QUERY..."
            VoiceState.SPEAKING -> "TRANSMITTING VOCAL SYNTHESIS..."
            VoiceState.MUTED -> "MICROPHONE MUTED"
            VoiceState.IDLE -> "STANDBY • TAP CORE TO ACTIVATE"
        }
        val voiceStateColor = when (voiceState) {
            VoiceState.LISTENING -> CyanGlow
            VoiceState.THINKING -> HoloAmber
            VoiceState.SPEAKING -> QuantumTeal
            VoiceState.MUTED -> AlertCrimson
            VoiceState.IDLE -> TextMuted
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlass)
                .border(1.dp, voiceStateColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(voiceStateColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = voiceStateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = voiceStateColor
            )
        }

        if (latestVoiceTranscript.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$latestVoiceTranscript\"",
                style = MaterialTheme.typography.bodyMedium,
                color = CyanBright
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Voice Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onToggleVoiceListening,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (voiceState == VoiceState.LISTENING) AlertCrimson else ElectricBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .testTag("voice_listen_button")
                    .weight(1f)
            ) {
                Icon(
                    imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (voiceState == VoiceState.LISTENING) "STOP LISTENING" else "VOICE COMMAND",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (voiceState == VoiceState.SPEAKING) {
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onStopSpeaking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertCrimson.copy(alpha = 0.8f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("stop_speaking_button")
                ) {
                    Text("SILENCE", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Genuine System Telemetry
        HudGlassCard(
            title = "Hardware Telemetry",
            statusTag = telemetry.networkStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            SystemTelemetryRow(telemetry = telemetry)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "UPTIME: ${telemetry.systemUptime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "FREE RAM: ${telemetry.availableRamMb} MB / ${telemetry.totalRamMb} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Quick Access Tactical Grid
        Text(
            text = "TACTICAL INTERFACES",
            style = MaterialTheme.typography.labelLarge,
            color = CyanBright,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionTile(
                title = "AI CHAT",
                desc = "Multi-turn Gemini link",
                icon = Icons.Default.Chat,
                accentColor = CyanGlow,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(JarvisNavDestination.CHAT) }
            )
            QuickActionTile(
                title = "SCREEN MODE",
                desc = "16:9 Command Center",
                icon = Icons.Default.Tv,
                accentColor = ElectricBlue,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(JarvisNavDestination.SCREEN_MODE) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionTile(
                title = "BRAIN MODE",
                desc = "Intelligence & Memory",
                icon = Icons.Default.Psychology,
                accentColor = QuantumTeal,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(JarvisNavDestination.BRAIN) }
            )
            QuickActionTile(
                title = "DEVICE LINK",
                desc = "Two-Phone Bluetooth",
                icon = Icons.Default.Hub,
                accentColor = HoloAmber,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(JarvisNavDestination.DEVICE_LINK) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionTile(
                title = "SMART TOOLS",
                desc = "Calculator & Tasks",
                icon = Icons.Default.Build,
                accentColor = CyanBright,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(JarvisNavDestination.TOOLS) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, HologramBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
