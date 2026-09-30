package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Slate950,
    primaryContainer = Slate850,
    onPrimaryContainer = Emerald400,
    secondary = Indigo400,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Indigo100,
    tertiary = Carmine400,
    onTertiary = Slate950,
    tertiaryContainer = Slate850,
    onTertiaryContainer = Carmine100,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = PureWhite,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald600,
    secondary = Indigo600,
    onSecondary = PureWhite,
    secondaryContainer = Indigo50,
    onSecondaryContainer = Indigo600,
    tertiary = Carmine600,
    onTertiary = PureWhite,
    tertiaryContainer = Carmine50,
    onTertiaryContainer = Carmine600,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default false to preserve sleek custom Slate/Emerald/Indigo branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
