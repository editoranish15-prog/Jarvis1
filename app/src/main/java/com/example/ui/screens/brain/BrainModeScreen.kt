package com.example.ui.screens.brain

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MemoryItemEntity
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
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoidBlack

@Composable
fun BrainModeScreen(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val diagnostic by viewModel.diagnostic.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Brain Mode Banner
        HudGlassCard(
            title = "Neural Brain Center",
            statusTag = settings.aiModel.uppercase(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GEMINI INTELLIGENCE NEXUS",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanBright
                    )
                    Text(
                        text = "Configured for ${settings.responseStyle} synthesis style.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. AI Diagnostics & Latency Benchmark
        HudGlassCard(
            title = "Neural Diagnostics",
            statusTag = if (diagnostic.isSuccess) "Operational" else "Awaiting Check",
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = diagnostic.message,
                style = MaterialTheme.typography.bodySmall,
                color = if (diagnostic.isSuccess) QuantumTeal else TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.runDiagnosticPing() },
                enabled = !isTesting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .testTag("run_diagnostic_button")
                    .fillMaxWidth()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TESTING NEURAL UPLINK...", style = MaterialTheme.typography.labelSmall)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BENCHMARK GEMINI LATENCY", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. AI Model Selection
        HudGlassCard(
            title = "Active Model Architecture",
            statusTag = "Multi-Engine",
            modifier = Modifier.fillMaxWidth()
        ) {
            val supportedModels = listOf(
                Triple("gemini-3.8-flash", "Gemini 3.8 Flash (Default)", "Frontier versatility, speed, and analytical power."),
                Triple("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "Ultra-fast low-latency assistant for high-frequency queries."),
                Triple("gemini-4-argon", "Gemini 4 Argon", "Deep reasoning engine for complex STEM problem-solving.")
            )

            supportedModels.forEach { (modelId, displayName, desc) ->
                val isSelected = settings.aiModel == modelId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanGlow.copy(alpha = 0.15f) else SurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CyanBright else HologramBorder.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateModel(modelId) }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) CyanBright else TextPrimary
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = CyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Response Synthesis Style Presets
        HudGlassCard(
            title = "Synthesis Mode",
            statusTag = settings.responseStyle,
            modifier = Modifier.fillMaxWidth()
        ) {
            val styles = listOf(
                "QUICK" to "Concise, rapid bullets & fast direct facts.",
                "BALANCED" to "Sophisticated, articulate standard assistant tone.",
                "DETAILED" to "Exhaustive deep-dive explanations & technical breakdowns."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                styles.forEach { (styleKey, desc) ->
                    val isSelected = settings.responseStyle == styleKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricBlue.copy(alpha = 0.3f) else SurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanBright else HologramBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateResponseStyle(styleKey) }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = styleKey,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) CyanBright else TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Memory Management & Personal Context
        HudGlassCard(
            title = "Persistent AI Memory",
            statusTag = "${memories.size} Facts Stored",
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Enable Memory In Conversations",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "JARVIS will recall stored user preferences.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = settings.enableMemoryContext,
                    onCheckedChange = { viewModel.toggleMemoryContext(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidBlack,
                        checkedTrackColor = CyanGlow,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = VoidBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_memory_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD MEMORY FACT", style = MaterialTheme.typography.labelSmall)
                }

                if (memories.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.clearAllMemories() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCrimson.copy(alpha = 0.2f),
                            contentColor = AlertCrimson
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("clear_memories_button")
                    ) {
                        Text("CLEAR ALL", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (showAddDialog) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CyanGlow, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("RECORD NEW MEMORY FACT", style = MaterialTheme.typography.labelSmall, color = CyanBright)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            placeholder = { Text("Key (e.g. Favorite Topic, Project Code)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = HologramBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            placeholder = { Text("Value (e.g. Quantum Physics, Mark 85)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = HologramBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showAddDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                            ) {
                                Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                        viewModel.addMemory(newKey, newValue)
                                        newKey = ""
                                        newValue = ""
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = VoidBlack)
                            ) {
                                Text("SAVE FACT", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Memory items list
            memories.forEach { mem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, HologramBorder.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = mem.key, style = MaterialTheme.typography.labelMedium, color = CyanBright)
                            Text(text = mem.value, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        IconButton(
                            onClick = { viewModel.deleteMemory(mem) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Memory",
                                tint = AlertCrimson.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
