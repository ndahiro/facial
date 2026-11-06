package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.EnrolledDatabaseScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.FaceVerifyTheme
import com.example.viewmodel.VerificationViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: VerificationViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FaceVerifyTheme {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainApp(viewModel: VerificationViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentScreen by remember { mutableStateOf<Screen>(Screen.Scanner) }
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.infoMessage) {
    uiState.infoMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearInfoMessage()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = CyberNavy900,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .testTag("main_bottom_nav"),
        containerColor = CyberNavy800,
        tonalElevation = 8.dp,
      ) {
        val navItems = listOf(
          Screen.Scanner,
          Screen.Logs,
          Screen.Database,
          Screen.Settings,
        )

        navItems.forEach { screen ->
          val isSelected = currentScreen == screen
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentScreen = screen },
            icon = {
              when (screen) {
                Screen.Logs -> {
                  if (uiState.totalLogsCount > 0) {
                    BadgedBox(
                      badge = {
                        Badge(containerColor = CyberCyan, contentColor = CyberNavy900) {
                          Text("${uiState.totalLogsCount}")
                        }
                      },
                    ) {
                      Icon(screen.icon, contentDescription = screen.title)
                    }
                  } else {
                    Icon(screen.icon, contentDescription = screen.title)
                  }
                }
                Screen.Database -> {
                  if (uiState.enrolledUsers.isNotEmpty()) {
                    BadgedBox(
                      badge = {
                        Badge(containerColor = Color(0xFF6366F1), contentColor = Color.White) {
                          Text("${uiState.enrolledUsers.size}")
                        }
                      },
                    ) {
                      Icon(screen.icon, contentDescription = screen.title)
                    }
                  } else {
                    Icon(screen.icon, contentDescription = screen.title)
                  }
                }
                else -> Icon(screen.icon, contentDescription = screen.title)
              }
            },
            label = {
              Text(
                screen.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CyberNavy900,
              selectedTextColor = CyberCyan,
              indicatorColor = CyberCyan,
              unselectedIconColor = Color(0xFF94A3B8),
              unselectedTextColor = Color(0xFF94A3B8),
            ),
            modifier = Modifier.testTag("nav_item_${screen.route}"),
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(CyberNavy900),
    ) {
      when (currentScreen) {
        Screen.Scanner -> VerificationScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.fillMaxSize(),
        )
        Screen.Logs -> LogsScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.fillMaxSize(),
        )
        Screen.Database -> EnrolledDatabaseScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.fillMaxSize(),
        )
        Screen.Settings -> SettingsScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
