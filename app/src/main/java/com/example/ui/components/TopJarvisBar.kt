package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.QuantumTeal
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextCyan

@Composable
fun TopJarvisBar(
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    modelName: String = "gemini-3.8-flash",
    onToggleMute: () -> Unit = {},
    onEnterScreenMode: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(SurfaceGlass)
            .border(
                width = 1.dp,
                color = HologramBorder.copy(alpha = 0.4f)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Title and System Moniker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(QuantumTeal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "J.A.R.V.I.S.",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanBright
                    )
                    Text(
                        text = "CORE ONLINE • ${modelName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCyan
                    )
                }
            }

            // Right: Quick Controls (Mute, Screen Mode button)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Screen Mode quick button
                IconButton(
                    onClick = onEnterScreenMode,
                    modifier = Modifier
                        .testTag("top_bar_screen_mode_button")
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HologramBorder.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Screen Mode Command Center",
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mute toggle button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .testTag("top_bar_mute_button")
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMuted) AlertCrimson.copy(alpha = 0.25f) else HologramBorder.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Unmute JARVIS" else "Mute JARVIS",
                        tint = if (isMuted) AlertCrimson else CyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
