package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
  object Scanner : Screen("scanner", "Scanner", Icons.Default.CameraAlt)
  object Logs : Screen("logs", "Audit Logs", Icons.Default.ListAlt)
  object Database : Screen("database", "Database", Icons.Default.People)
  object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
