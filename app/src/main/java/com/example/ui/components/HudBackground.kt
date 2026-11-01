package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.HologramGrid

@Composable
fun HudBackground(
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    showScanlines: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (showGrid || showScanlines) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (showGrid) {
                    val gridSize = 40f
                    var x = 0f
                    while (x < width) {
                        drawLine(
                            color = HologramGrid,
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 0.8f
                        )
                        x += gridSize
                    }

                    var y = 0f
                    while (y < height) {
                        drawLine(
                            color = HologramGrid,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 0.8f
                        )
                        y += gridSize
                    }
                }

                if (showScanlines) {
                    val lineSpacing = 6f
                    var y = 0f
                    while (y < height) {
                        drawLine(
                            color = Color(0x0600F0FF),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                        y += lineSpacing
                    }
                }
            }
        }
        content()
    }
}
