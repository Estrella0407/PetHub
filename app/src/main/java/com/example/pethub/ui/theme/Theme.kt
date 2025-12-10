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
    primary = DarkBrown,
    onPrimary = CreamBackground,
    secondary = NeutralBrown,
    onSecondary = CreamBackground,
    background = CreamBackground,
    onBackground = NeutralText,
    surface = CreamBackground,
    onSurface = NeutralText,
)

@Composable
fun PetHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // FORCE LIGHT COLORS:
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // If dynamic color is enabled, force the light version
            dynamicLightColorScheme(context)
        }
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PetHubTypography,
        content = content
    )
}