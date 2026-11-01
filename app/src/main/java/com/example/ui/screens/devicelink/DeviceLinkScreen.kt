package com.example.ui.screens.devicelink

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceLinkMessage
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun DeviceLinkScreen(
    viewModel: DeviceLinkViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val status by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectedName by viewModel.connectedDeviceName.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val isBtSupported by viewModel.isBluetoothSupported.collectAsStateWithLifecycle()
    val isBtEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()

    var messageInput by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshDevices()
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // 1. Connection Status Card
        HudGlassCard(
            title = "JARVIS Device Link (Two-Phone)",
            statusTag = status.name,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (status) {
                        ConnectionStatus.CONNECTED -> QuantumTeal
                        ConnectionStatus.CONNECTING, ConnectionStatus.ADVERTISING -> HoloAmber
                        ConnectionStatus.SCANNING -> CyanGlow
                        ConnectionStatus.DISCONNECTED -> AlertCrimson
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (status == ConnectionStatus.CONNECTED) "LINK ACTIVE: ${connectedName ?: "Peer Unit"}" else "LINK STATUS: ${status.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanBright
                        )
                        Text(
                            text = "Unit ID: ${settings.deviceName} (${settings.deviceRole})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextCyan
                        )
                    }
                }

                if (status == ConnectionStatus.CONNECTED) {
                    Button(
                        onClick = { viewModel.disconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SEVER LINK", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Role Selector (PRIMARY vs SECONDARY)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isPrimary = settings.deviceRole == "PRIMARY"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPrimary) ElectricBlue.copy(alpha = 0.3f) else SurfaceElevated)
                    .border(1.dp, if (isPrimary) CyanBright else HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.setDeviceRole("PRIMARY") }
                    .padding(10.dp)
            ) {
                Column {
                    Text("PRIMARY (HOST)", style = MaterialTheme.typography.labelMedium, color = if (isPrimary) CyanBright else TextMuted)
                    Text("Listens for incoming secondary link", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            val isSecondary = settings.deviceRole == "SECONDARY"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSecondary) ElectricBlue.copy(alpha = 0.3f) else SurfaceElevated)
                    .border(1.dp, if (isSecondary) CyanBright else HologramBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.setDeviceRole("SECONDARY") }
                    .padding(10.dp)
            ) {
                Column {
                    Text("SECONDARY (CLIENT)", style = MaterialTheme.typography.labelMedium, color = if (isSecondary) CyanBright else TextMuted)
                    Text("Scans & connects to primary host", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Connection Setup Actions (Listen or Connect)
        if (status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.ADVERTISING) {
            HudGlassCard(
                title = if (settings.deviceRole == "PRIMARY") "Primary Beacon Transmitter" else "Available Bluetooth JARVIS Nodes",
                statusTag = "${pairedDevices.size} Known Units",
                modifier = Modifier.fillMaxWidth()
            ) {
                if (settings.deviceRole == "PRIMARY") {
                    Text(
                        text = "Activate primary beacon to allow secondary JARVIS device to link.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.startAdvertisingHost() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = VoidBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("primary_beacon_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (status == ConnectionStatus.ADVERTISING) "BEACON ACTIVE (LISTENING...)" else "START PRIMARY BEACON")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select paired JARVIS device to connect:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        IconButton(onClick = { viewModel.refreshDevices() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanBright, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (pairedDevices.isEmpty()) {
                        Text(
                            text = "No paired Bluetooth devices detected. Ensure Bluetooth is enabled and devices paired in Android settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HoloAmber
                        )
                    } else {
                        pairedDevices.forEach { dev ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, HologramBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.connectToDevice(dev) }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(dev.name ?: "Unknown Device", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                        Text(dev.address, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Icon(imageVector = Icons.Default.BluetoothConnected, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. Tactical Command Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { viewModel.sendTacticalCommand("PING") },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("PING PEER", style = MaterialTheme.typography.labelSmall, color = CyanBright)
            }
            Button(
                onClick = { viewModel.sendTacticalCommand("STATUS") },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("REQ STATUS", style = MaterialTheme.typography.labelSmall, color = TextCyan)
            }
            Button(
                onClick = { viewModel.sendTacticalCommand("ALERT") },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("DEF ALERT", style = MaterialTheme.typography.labelSmall, color = AlertCrimson)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Real Device Link Message Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark)
                .border(1.dp, HologramBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = CyanGlow.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No quantum packets exchanged yet.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            } else {
                val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(messages, key = { it.id }) { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (msg.isIncoming) SurfaceElevated else ElectricBlue.copy(alpha = 0.4f))
                                .border(1.dp, if (msg.type == "ALERT") AlertCrimson else HologramBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${msg.senderName} [${msg.type}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.type == "ALERT") AlertCrimson else CyanBright
                                    )
                                    Text(
                                        text = timeFormat.format(Date(msg.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Packet Transmission Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Transmit payload to peer phone...", color = TextMuted, style = MaterialTheme.typography.bodySmall) },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = HologramBorder,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                modifier = Modifier.weight(1f).testTag("device_link_input")
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        val text = messageInput
                        messageInput = ""
                        viewModel.sendLinkMessage(text)
                    }
                },
                modifier = Modifier
                    .testTag("device_link_send_button")
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Link Packet", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
