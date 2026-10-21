package com.example.yungasdistribuidora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TurquoiseAccent,
    onPrimary = PrimaryNavy,
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = White,
    secondary = CorporateGreen,
    onSecondary = White,
    secondaryContainer = SecondaryBlue,
    onSecondaryContainer = White,
    tertiary = TurquoiseAccent,
    onTertiary = PrimaryNavy,
    background = PrimaryNavy,
    onBackground = White,
    surface = CardBackground,
    onSurface = White,
    surfaceVariant = SecondaryBlue,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed
)

@Composable
fun YungasDistribuidoraTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
