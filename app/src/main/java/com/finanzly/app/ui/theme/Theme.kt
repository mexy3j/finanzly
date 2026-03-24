package com.finanzly.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary          = Indigo400,
    onPrimary        = Slate900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo50,
    secondary        = Emerald400,
    onSecondary      = Slate900,
    tertiary         = Crimson400,
    onTertiary       = Slate900,
    background       = SurfaceDark,
    surface          = CardDark,
    onBackground     = Slate50,
    onSurface        = Slate100,
    outline          = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary          = Indigo600,
    onPrimary        = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo700,
    secondary        = Emerald600,
    onSecondary      = Color.White,
    tertiary         = Crimson600,
    onTertiary       = Color.White,
    background       = SurfaceLight,
    surface          = CardLight,
    onBackground     = Slate800,
    onSurface        = Slate700,
    outline          = Slate200
)

import androidx.compose.ui.graphics.Color
val Color.Companion.White get() = Color(0xFFFFFFFF)

@Composable
fun FinanzlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinanzlyTypography,
        content = content
    )
}
