package com.example.pethub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = PeachPrimary,
    onPrimary = CreamBackground,
    secondary = TealSecondary,
    onSecondary = CreamBackground,
    background = CreamBackground,
    onBackground = NeutralText,
    surface = CreamBackground,
    onSurface = NeutralText
)

private val DarkColors = darkColorScheme(
    primary = PeachDark,
    onPrimary = CreamBackground,
    secondary = TealSecondary,
    background = NeutralText,
    onBackground = CreamBackground,
    surface = NeutralText,
    onSurface = CreamBackground
)

@Composable
fun PetHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PetHubTypography,
        content = content
    )
}