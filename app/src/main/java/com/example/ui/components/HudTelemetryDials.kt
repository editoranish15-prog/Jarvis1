package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SystemTelemetry
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoloAmber
import com.example.ui.theme.QuantumTeal
import com.example.ui.theme.TextMuted

@Composable
fun CircularTelemetryDial(
    value: Float, // 0.0 to 1.0
    label: String,
    valueText: String,
    gaugeColor: Color = CyanGlow,
    size: Dp = 68.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 5.dp.toPx()
                val arcPadding = strokeWidth / 2f
                val arcSize = this.size.width - (arcPadding * 2)

                // Background track arc
                drawArc(
                    color = gaugeColor.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(arcPadding, arcPadding),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active telemetry sweep
                drawArc(
                    color = gaugeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * value.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(arcPadding, arcPadding),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = CyanBright
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun SystemTelemetryRow(
    telemetry: SystemTelemetry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val batteryPct = telemetry.batteryPercent / 100f
        val batteryColor = when {
            telemetry.batteryPercent > 40 -> QuantumTeal
            telemetry.batteryPercent > 15 -> HoloAmber
            else -> AlertCrimson
        }

        CircularTelemetryDial(
            value = batteryPct,
            label = if (telemetry.isCharging) "Power (Chg)" else "Battery",
            valueText = "${telemetry.batteryPercent}%",
            gaugeColor = batteryColor
        )

        val memoryUsedPct = if (telemetry.totalRamMb > 0) {
            ((telemetry.totalRamMb - telemetry.availableRamMb).toFloat() / telemetry.totalRamMb).coerceIn(0f, 1f)
        } else 0.5f

        CircularTelemetryDial(
            value = memoryUsedPct,
            label = "Memory RAM",
            valueText = "${(memoryUsedPct * 100).toInt()}%",
            gaugeColor = ElectricBlue
        )

        CircularTelemetryDial(
            value = 0.95f,
            label = "Core AI Link",
            valueText = "ACTIVE",
            gaugeColor = CyanGlow
        )
    }
}
