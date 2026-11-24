package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GeminiDarkColorScheme = darkColorScheme(
    primary = GeminiDarkActiveAccent,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = GeminiDarkActiveContainer,
    onPrimaryContainer = GeminiDarkActiveAccent,
    secondary = GeminiPurple,
    onSecondary = Color(0xFF1F1233),
    secondaryContainer = GeminiDarkSurfaceHover,
    onSecondaryContainer = GeminiDarkOnSurface,
    tertiary = GeminiCoral,
    onTertiary = Color(0xFF3E1216),
    tertiaryContainer = GeminiDarkSurfaceHover,
    onTertiaryContainer = GeminiDarkOnSurface,
    background = GeminiDarkBackground,
    onBackground = GeminiDarkOnSurface,
    surface = GeminiDarkSurface,
    onSurface = GeminiDarkOnSurface,
    surfaceVariant = GeminiDarkSurfaceHover,
    onSurfaceVariant = GeminiDarkOnSurfaceSecondary,
    outline = GeminiDarkDivider,
    outlineVariant = GeminiDarkDivider,
    error = NegativeCoral
)

private val GeminiLightColorScheme = lightColorScheme(
    primary = GeminiLightActiveAccent,
    onPrimary = Color.White,
    primaryContainer = GeminiLightActiveContainer,
    onPrimaryContainer = GeminiLightActiveAccent,
    secondary = GeminiPurple,
    onSecondary = Color.White,
    secondaryContainer = GeminiLightSurfaceHover,
    onSecondaryContainer = GeminiLightOnSurface,
    tertiary = GeminiCoral,
    onTertiary = Color.White,
    tertiaryContainer = GeminiLightSurfaceHover,
    onTertiaryContainer = GeminiLightOnSurface,
    background = GeminiLightBackground,
    onBackground = GeminiLightOnSurface,
    surface = GeminiLightSurface,
    onSurface = GeminiLightOnSurface,
    surfaceVariant = GeminiLightSurfaceHover,
    onSurfaceVariant = GeminiLightOnSurfaceSecondary,
    outline = GeminiLightDivider,
    outlineVariant = GeminiLightDivider,
    error = Color(0xFFB3261E)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun NeetPulseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GeminiDarkColorScheme else GeminiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
