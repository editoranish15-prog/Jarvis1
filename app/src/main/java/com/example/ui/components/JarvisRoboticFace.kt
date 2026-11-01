package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.VoiceState
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoloAmber
import com.example.ui.theme.QuantumTeal
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisRoboticFace(
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.IDLE,
    audioRms: Float = 0f,
    eyeTheme: String = "CYAN",
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val baseColor = when (eyeTheme) {
        "BLUE" -> ElectricBlue
        "AMBER" -> HoloAmber
        "GREEN" -> QuantumTeal
        "RED" -> AlertCrimson
        else -> CyanGlow
    }

    val glowColor = when (eyeTheme) {
        "BLUE" -> Color(0xFF82B1FF)
        "AMBER" -> Color(0xFFFFD54F)
        "GREEN" -> Color(0xFFB9F6CA)
        "RED" -> Color(0xFFFF8A80)
        else -> CyanBright
    }

    // Natural blink animation state
    var blinkScaleY by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            // Natural random interval between blinks: 3.5 to 6 seconds
            delay((3500..6000).random().toLong())
            if (voiceState != VoiceState.LISTENING) {
                // Blink down
                blinkScaleY = 0.08f
                delay(120)
                // Open up
                blinkScaleY = 1f
            }
        }
    }

    // Infinite transitions for rotating HUD reticles & breathing pulses
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_face")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticleRotation"
    )

    val thinkingScan by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinkingScan"
    )

    val listeningRipple by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listeningRipple"
    )

    // Animated eye height based on voice state and audio RMS
    val targetEyeOpen = when (voiceState) {
        VoiceState.LISTENING -> 1.25f
        VoiceState.THINKING -> 0.85f
        VoiceState.SPEAKING -> 1.0f + (audioRms * 0.45f).coerceIn(0f, 0.6f)
        VoiceState.MUTED -> 0.4f
        VoiceState.IDLE -> 1.0f
    } * blinkScaleY

    val animatedEyeOpen by animateFloatAsState(
        targetValue = targetEyeOpen,
        animationSpec = tween(100),
        label = "animatedEyeOpen"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val outerRadius = canvasWidth * 0.46f

            // 1. Outer Arc Reactor HUD rings
            drawCircle(
                color = baseColor.copy(alpha = 0.15f * pulseGlow),
                radius = outerRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer segmented notches
            rotate(reticleRotation, center) {
                val numNotches = 24
                for (i in 0 until numNotches) {
                    val angle = (i * (360f / numNotches)) * (Math.PI / 180.0)
                    val r1 = outerRadius - 4.dp.toPx()
                    val r2 = outerRadius + (if (i % 6 == 0) 6.dp.toPx() else 2.dp.toPx())
                    val p1 = Offset(
                        center.x + (r1 * cos(angle)).toFloat(),
                        center.y + (r1 * sin(angle)).toFloat()
                    )
                    val p2 = Offset(
                        center.x + (r2 * cos(angle)).toFloat(),
                        center.y + (r2 * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = if (i % 6 == 0) glowColor else baseColor.copy(alpha = 0.35f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (i % 6 == 0) 2.dp.toPx() else 1.dp.toPx()
                    )
                }
            }

            // Counter-rotating inner bracket ring
            rotate(-reticleRotation * 0.8f, center) {
                val arcRadius = outerRadius * 0.82f
                drawArc(
                    color = baseColor.copy(alpha = 0.5f),
                    startAngle = 15f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = baseColor.copy(alpha = 0.5f),
                    startAngle = 195f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 2. State-specific holographic waves
            if (voiceState == VoiceState.LISTENING) {
                // Expanding listening soundwave pulse
                drawCircle(
                    color = glowColor.copy(alpha = (1.3f - listeningRipple).coerceIn(0f, 0.6f)),
                    radius = outerRadius * listeningRipple,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = baseColor.copy(alpha = 0.25f),
                    radius = outerRadius * 0.95f,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // 3. Central Visor & Dual Expressive Robotic Eyes
            val eyeWidth = canvasWidth * 0.17f
            val maxEyeHeight = canvasHeight * 0.26f
            val currentEyeHeight = maxEyeHeight * animatedEyeOpen.coerceAtLeast(0.04f)
            val eyeSpacing = canvasWidth * 0.08f

            val leftEyeCenter = Offset(center.x - eyeWidth / 2f - eyeSpacing / 2f, center.y)
            val rightEyeCenter = Offset(center.x + eyeWidth / 2f + eyeSpacing / 2f, center.y)

            // Lateral offset during THINKING state to simulate scanning cognitive activity
            val eyeHorizontalScanOffset = if (voiceState == VoiceState.THINKING) {
                thinkingScan * (eyeWidth * 0.25f)
            } else 0f

            listOf(leftEyeCenter, rightEyeCenter).forEachIndexed { index, eyePos ->
                val adjustedEyePos = Offset(eyePos.x + eyeHorizontalScanOffset, eyePos.y)

                // Eye glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.55f * pulseGlow),
                            baseColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = adjustedEyePos,
                        radius = eyeWidth * 1.4f
                    ),
                    radius = eyeWidth * 1.4f,
                    center = adjustedEyePos
                )

                // Futuristic Robotic Eye Contours (Sci-fi angled hexagonal/chamfered pill)
                val eyeRect = RoundRect(
                    left = adjustedEyePos.x - eyeWidth / 2f,
                    top = adjustedEyePos.y - currentEyeHeight / 2f,
                    right = adjustedEyePos.x + eyeWidth / 2f,
                    bottom = adjustedEyePos.y + currentEyeHeight / 2f,
                    cornerRadius = CornerRadius(eyeWidth * 0.35f, eyeWidth * 0.35f)
                )

                val eyePath = Path().apply { addRoundRect(eyeRect) }

                // Outer eye bezel / frame
                drawPath(
                    path = eyePath,
                    color = baseColor.copy(alpha = 0.8f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Glowing inner core / Iris
                drawPath(
                    path = eyePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glowColor,
                            baseColor
                        ),
                        startY = adjustedEyePos.y - currentEyeHeight / 2f,
                        endY = adjustedEyePos.y + currentEyeHeight / 2f
                    )
                )

                // Center pupil aperture notch
                if (currentEyeHeight > 8.dp.toPx()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = (eyeWidth * 0.18f).coerceAtMost(currentEyeHeight * 0.3f),
                        center = adjustedEyePos
                    )
                }

                // Tech eyebrow accent brackets
                val browY = adjustedEyePos.y - maxEyeHeight * 0.72f
                val browStartX = adjustedEyePos.x - eyeWidth * 0.55f
                val browEndX = adjustedEyePos.x + eyeWidth * 0.55f
                drawLine(
                    color = glowColor.copy(alpha = 0.65f),
                    start = Offset(browStartX, browY),
                    end = Offset(browEndX, browY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Voice Waveform / Mouth Bar beneath the eyes
            val mouthY = center.y + outerRadius * 0.55f
            val mouthWidth = canvasWidth * 0.38f
            val numBars = 9
            val barSpacing = mouthWidth / (numBars - 1)

            for (i in 0 until numBars) {
                val x = center.x - mouthWidth / 2f + (i * barSpacing)
                val distFromCenter = Math.abs(i - (numBars - 1) / 2f) / ((numBars - 1) / 2f)
                val barFactor = (1.0f - distFromCenter * 0.4f)

                val barHeight = when (voiceState) {
                    VoiceState.SPEAKING -> (10.dp.toPx() + (audioRms * 28.dp.toPx() * barFactor)).coerceIn(4.dp.toPx(), 32.dp.toPx())
                    VoiceState.LISTENING -> (6.dp.toPx() + (sin(reticleRotation * 0.1f + i) * 6.dp.toPx())).coerceAtLeast(3.dp.toPx())
                    VoiceState.THINKING -> (4.dp.toPx() + (cos(thinkingScan * 4f + i) * 4.dp.toPx())).coerceAtLeast(2.dp.toPx())
                    else -> 3.dp.toPx()
                }

                drawLine(
                    color = if (voiceState == VoiceState.SPEAKING) glowColor else baseColor.copy(alpha = 0.5f),
                    start = Offset(x, mouthY - barHeight / 2f),
                    end = Offset(x, mouthY + barHeight / 2f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
