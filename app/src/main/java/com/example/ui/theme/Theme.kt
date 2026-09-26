package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CinematicDarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF1F1400),
    primaryContainer = AmberContainer,
    onPrimaryContainer = OnAmberContainer,
    secondary = SaddleBronze,
    onSecondary = Color.White,
    secondaryContainer = BronzeContainer,
    onSecondaryContainer = OnBronzeContainer,
    tertiary = FrontierSlate,
    onTertiary = Color(0xFF0F1E24),
    tertiaryContainer = FrontierSlateDark,
    onTertiaryContainer = Color(0xFFCFDEE6),
    background = DarkObsidian,
    onBackground = TextPrimary,
    surface = DarkCharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun OldStoryVoiceTheme(
    content: @Composable () -> Unit
) {
    // We enforce the dark cinematic theme for authentic storytelling mood
    MaterialTheme(
        colorScheme = CinematicDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OldStoryVoiceTheme(content = content)
}
