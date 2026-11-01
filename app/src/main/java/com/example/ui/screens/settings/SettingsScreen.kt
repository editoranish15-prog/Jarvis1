package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.preferences.JarvisPreferences
import com.example.data.preferences.JarvisSettings
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoloAmber
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.QuantumTeal
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoidBlack

@Composable
fun SettingsScreen(
    preferences: JarvisPreferences,
    settings: JarvisSettings,
    onTestVoice: (String) -> Unit,
    onPurgeAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var honorificInput by remember(settings.userHonorific) { mutableStateOf(settings.userHonorific) }
    var nameInput by remember(settings.userName) { mutableStateOf(settings.userName) }
    var deviceNameInput by remember(settings.deviceName) { mutableStateOf(settings.deviceName) }
    var pitchVal by remember(settings.voicePitch) { mutableFloatStateOf(settings.voicePitch) }
    var rateVal by remember(settings.voiceRate) { mutableFloatStateOf(settings.voiceRate) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 1. Personality & Identity Settings
        HudGlassCard(title = "JARVIS PERSONALITY CONFIGURATION", modifier = Modifier.fillMaxWidth()) {
            Text("Preferred User Honorific:", style = MaterialTheme.typography.labelSmall, color = TextCyan)
            Spacer(modifier = Modifier.height(4.dp))
            val honorificOptions = listOf("Sir", "Ma'am", "Commander", "Chief")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                honorificOptions.forEach { opt ->
                    val isSel = honorificInput == opt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) CyanGlow.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isSel) CyanBright else HologramBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable {
                                honorificInput = opt
                                preferences.updateSettings(settings.copy(userHonorific = opt))
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(opt, style = MaterialTheme.typography.labelMedium, color = if (isSel) CyanBright else TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    preferences.updateSettings(settings.copy(userName = it))
                },
                label = { Text("Callsign / User Name", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = HologramBorder)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Voice & Audio Settings
        HudGlassCard(title = "VOICE SYNTHESIZER CALIBRATION", modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Auto-Speak AI Responses", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = settings.autoSpeakResponses,
                    onCheckedChange = { preferences.updateSettings(settings.copy(autoSpeakResponses = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = CyanGlow)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Voice Pitch (${String.format("%.2f", pitchVal)}x - Deep British Tone)", style = MaterialTheme.typography.labelSmall, color = TextCyan)
            Slider(
                value = pitchVal,
                onValueChange = {
                    pitchVal = it
                    preferences.updateSettings(settings.copy(voicePitch = it))
                },
                valueRange = 0.7f..1.3f,
                colors = SliderDefaults.colors(thumbColor = CyanGlow, activeTrackColor = CyanBright)
            )

            Text("Speech Rate (${String.format("%.2f", rateVal)}x - Controlled Pace)", style = MaterialTheme.typography.labelSmall, color = TextCyan)
            Slider(
                value = rateVal,
                onValueChange = {
                    rateVal = it
                    preferences.updateSettings(settings.copy(voiceRate = it))
                },
                valueRange = 0.7f..1.3f,
                colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onTestVoice("Voice synthesis calibrated and operational, ${settings.userHonorific}.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = VoidBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TEST VOICE SYNTHESIS", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Visual & HUD Overlays
        HudGlassCard(title = "HUD & VISOR DISPLAY", modifier = Modifier.fillMaxWidth()) {
            Text("Robotic Visor Theme:", style = MaterialTheme.typography.labelSmall, color = TextCyan)
            Spacer(modifier = Modifier.height(6.dp))

            val themes = listOf(
                "CYAN" to CyanGlow,
                "BLUE" to ElectricBlue,
                "AMBER" to HoloAmber,
                "GREEN" to QuantumTeal,
                "RED" to AlertCrimson
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                themes.forEach { (key, color) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (settings.eyeTheme == key) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { preferences.updateSettings(settings.copy(eyeTheme = key)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CRT Scanlines Overlay", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = settings.scanlinesOverlay,
                    onCheckedChange = { preferences.updateSettings(settings.copy(scanlinesOverlay = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = CyanGlow)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Holographic Grid Background", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = settings.hudGridEnabled,
                    onCheckedChange = { preferences.updateSettings(settings.copy(hudGridEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = CyanGlow)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Device Link Unit Settings
        HudGlassCard(title = "JARVIS DEVICE LINK SETTINGS", modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = deviceNameInput,
                onValueChange = {
                    deviceNameInput = it
                    preferences.updateSettings(settings.copy(deviceName = it))
                },
                label = { Text("Local Unit Callsign", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = HologramBorder)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Privacy & Data Control
        HudGlassCard(title = "SECURITY & DATA PURGE", modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "All chat history, tasks, notes, and AI memories are stored locally on your device in secure Room SQLite database.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    onPurgeAllData()
                    Toast.makeText(context, "All local archives purged", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson.copy(alpha = 0.25f), contentColor = AlertCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PURGE ALL CONVERSATIONS & MEMORIES", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. About JARVIS Specs
        HudGlassCard(title = "SYSTEM SPECIFICATIONS", modifier = Modifier.fillMaxWidth()) {
            Text("J.A.R.V.I.S. (Just A Rather Very Intelligent System)", style = MaterialTheme.typography.labelMedium, color = CyanBright)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Version: 1.0 (Mark LXXXV Architecture)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text("AI Brain: Google Gemini 3.8 Flash via Firebase AI Logic", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text("Device Link: Bluetooth Low Energy & RFCOMM Secure Stream", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text("UI Engine: Jetpack Compose Material Design 3", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
