package com.example.ui.screens.tools

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun SmartToolsScreen(
    viewModel: SmartToolsViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission is required for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                SmartToolTab.CALCULATOR to "Calculator",
                SmartToolTab.NOTES to "Notes",
                SmartToolTab.TASKS to "Tasks",
                SmartToolTab.REMINDERS to "Reminders",
                SmartToolTab.LAUNCHER to "Apps"
            )

            tabs.forEach { (tab, label) ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanGlow.copy(alpha = 0.2f) else SurfaceElevated)
                        .border(1.dp, if (isSelected) CyanBright else HologramBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.setTab(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) CyanBright else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            SmartToolTab.CALCULATOR -> CalculatorTab(viewModel)
            SmartToolTab.NOTES -> NotesTab(viewModel)
            SmartToolTab.TASKS -> TasksTab(viewModel)
            SmartToolTab.REMINDERS -> RemindersTab(viewModel) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            SmartToolTab.LAUNCHER -> AppLauncherTab(viewModel)
        }
    }
}

@Composable
private fun CalculatorTab(viewModel: SmartToolsViewModel) {
    val expression by viewModel.calcExpression.collectAsStateWithLifecycle()
    val result by viewModel.calcResult.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceDark)
                .border(1.dp, CyanGlow.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = expression.ifEmpty { "CALCULATOR READY" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "= $result",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CyanBright
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scientific Buttons Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("sqrt", "sin", "cos", "tan", "log").forEach { op ->
                CalcKey(text = op, isSpecial = true, modifier = Modifier.weight(1f)) {
                    viewModel.onCalcInput(op)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Keypad Grid
        val rows = listOf(
            listOf("C", "DEL", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "00", "=")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    CalcKey(
                        text = key,
                        isOp = key in listOf("÷", "×", "-", "+", "="),
                        isAction = key in listOf("C", "DEL"),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.onCalcInput(key)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    text: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,
    isAction: Boolean = false,
    isSpecial: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        text == "=" -> CyanGlow
        isAction -> AlertCrimson.copy(alpha = 0.25f)
        isOp -> ElectricBlue.copy(alpha = 0.3f)
        isSpecial -> SurfaceElevated
        else -> SurfaceDark
    }

    val textColor = when {
        text == "=" -> VoidBlack
        isAction -> AlertCrimson
        isOp -> CyanBright
        isSpecial -> TextCyan
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

@Composable
private fun NotesTab(viewModel: SmartToolsViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        HudGlassCard(title = "CREATE NOTE", modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                placeholder = { Text("Note Title...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = HologramBorder
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newContent,
                onValueChange = { newContent = it },
                placeholder = { Text("Note Content...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = HologramBorder
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (newTitle.isNotBlank() || newContent.isNotBlank()) {
                        viewModel.addNote(newTitle, newContent)
                        newTitle = ""
                        newContent = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = VoidBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("SAVE NOTE", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(notes, key = { it.id }) { note ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .border(1.dp, HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.labelMedium, color = CyanBright)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(note.content, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        IconButton(onClick = { viewModel.deleteNote(note) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertCrimson.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksTab(viewModel: SmartToolsViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Normal") }

    Column(modifier = Modifier.fillMaxSize()) {
        HudGlassCard(title = "TACTICAL TASK MANAGER", modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("New Objective...", color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = HologramBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle, selectedPriority)
                            newTaskTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tasks, key = { it.id }) { task ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (task.isCompleted) SurfaceDark.copy(alpha = 0.6f) else SurfaceDark)
                        .border(1.dp, if (task.isCompleted) HologramBorder.copy(alpha = 0.2f) else HologramBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleTask(task) }) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (task.isCompleted) QuantumTeal else TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.isCompleted) TextMuted else TextPrimary
                            )
                            Text(
                                text = "PRIORITY: ${task.priority.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCyan
                            )
                        }
                        IconButton(onClick = { viewModel.deleteTask(task) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertCrimson.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemindersTab(viewModel: SmartToolsViewModel, onRequestNotificationPermission: () -> Unit) {
    var reminderTitle by remember { mutableStateOf("") }
    var reminderMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        HudGlassCard(title = "JARVIS NOTIFICATION SYSTEM", modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Dispatch active alerts and scheduled notifications through Android system channels.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = reminderTitle,
                onValueChange = { reminderTitle = it },
                placeholder = { Text("Reminder Title (e.g. Flight Departure)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = HologramBorder)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = reminderMsg,
                onValueChange = { reminderMsg = it },
                placeholder = { Text("Details (e.g. Gate 4B, boarding in 20 minutes)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = HologramBorder)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onRequestNotificationPermission()
                    if (reminderTitle.isNotBlank()) {
                        val sent = viewModel.triggerNotificationReminder(reminderTitle, reminderMsg.ifBlank { "Priority tactical reminder." })
                        if (sent) {
                            Toast.makeText(context, "Notification dispatched to system tray", Toast.LENGTH_SHORT).show()
                            reminderTitle = ""
                            reminderMsg = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = VoidBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISPATCH NOTIFICATION REMINDER", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AppLauncherTab(viewModel: SmartToolsViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        HudGlassCard(title = "TACTICAL APP LAUNCHER", modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "JARVIS supported direct hardware intent bridges to Android native applications.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val apps = listOf(
                Triple("CAMERA", "Camera Optical Sensor", Icons.Default.CameraAlt),
                Triple("BROWSER", "Web Uplink (Browser)", Icons.Default.Language),
                Triple("MAPS", "Geospatial Navigation", Icons.Default.Map),
                Triple("DIALER", "Communications Phone", Icons.Default.Phone),
                Triple("SETTINGS", "Android System Settings", Icons.Default.Settings)
            )

            apps.forEach { (action, name, icon) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.launchApp(action) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = CyanBright, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = name, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "LAUNCH >", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
