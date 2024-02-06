package com.biancamoosmann.student721042.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biancamoosmann.student721042.home.ViewModel.HomeViewModel


// Dark Theme Color Scheme
private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF8E24AA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF7B1FA2),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF42A5F5),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFC107),
    onTertiaryContainer = Color(0xFF000000),
    error = Color(0xFFE57373),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFEF5350),
    onErrorContainer = Color(0xFF000000),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF333333),
    onSurface = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFCCCCCC)
)

// Light Theme Color Scheme
private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF8E24AA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF7B1FA2),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF000000),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF42A5F5),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFC107),
    onTertiaryContainer = Color(0xFF000000),
    error = Color(0xFFE57373),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFEF5350),
    onErrorContainer = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF000000),
    inverseSurface = Color(0xFF000000),
    inverseOnSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF000000),
    outline = Color(0xFFCCCCCC)
)



@Composable
fun Student721042Theme(
    content: @Composable () -> Unit
) {
    val homeViewModel: HomeViewModel = viewModel()

    val isDarkMode by homeViewModel.isDarkMode.collectAsState()

    val darkColors = DarkColorPalette
    val lightColors = LightColorPalette

    // Choose Colorpalett based on DarkMode State
    val colors = if (isDarkMode) darkColors else lightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}


