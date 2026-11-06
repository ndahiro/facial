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

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberNavy900,
    primaryContainer = CyberNavy700,
    onPrimaryContainer = CyberCyan,
    secondary = ElectricIndigoLight,
    onSecondary = Color.White,
    secondaryContainer = CyberNavy600,
    onSecondaryContainer = ElectricIndigoLight,
    tertiary = EmeraldPass,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldPassBg,
    onTertiaryContainer = EmeraldPass,
    error = CrimsonFail,
    onError = Color.White,
    errorContainer = CrimsonFailBg,
    onErrorContainer = CrimsonFail,
    background = CyberNavy900,
    onBackground = SlateTextPrimary,
    surface = CyberNavy800,
    onSurface = SlateTextPrimary,
    surfaceVariant = CyberNavy700,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    outlineVariant = CyberNavy600,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyberCyanDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = CyberNavy900,
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = ElectricIndigo,
    tertiary = EmeraldPass,
    onTertiary = Color.White,
    error = CrimsonFail,
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
  )

@Composable
fun FaceVerifyTheme(
  darkTheme: Boolean = true, // Default to sleek cybersecurity dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
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
    content = content,
  )
}
