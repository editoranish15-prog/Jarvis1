package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisDarkColorScheme = darkColorScheme(
    primary = PrimaryM3,
    onPrimary = OnPrimaryM3,
    primaryContainer = PrimaryContainerM3,
    onPrimaryContainer = OnPrimaryContainerM3,
    secondary = SecondaryM3,
    onSecondary = OnSecondaryM3,
    secondaryContainer = SecondaryContainerM3,
    onSecondaryContainer = OnSecondaryContainerM3,
    tertiary = TertiaryM3,
    onTertiary = OnTertiaryM3,
    tertiaryContainer = TertiaryContainerM3,
    onTertiaryContainer = OnTertiaryContainerM3,
    background = BackgroundM3,
    onBackground = OnBackgroundM3,
    surface = SurfaceM3,
    onSurface = OnSurfaceM3,
    surfaceVariant = SurfaceVariantM3,
    onSurfaceVariant = OnSurfaceVariantM3,
    outline = OutlineM3
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisDarkColorScheme,
        typography = JarvisTypography,
        content = content
    )
}
