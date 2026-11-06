package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.EmeraldPass
import com.example.viewmodel.VerificationUiState
import com.example.viewmodel.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: VerificationViewModel,
  uiState: VerificationUiState,
  modifier: Modifier = Modifier,
) {
  var apiUrlInput by remember(uiState.apiUrl) { mutableStateOf(uiState.apiUrl) }
  var apiTokenInput by remember(uiState.apiToken) { mutableStateOf(uiState.apiToken) }
  var gateLocationInput by remember(uiState.locationGate) { mutableStateOf(uiState.locationGate) }
  var deviceIdInput by remember(uiState.deviceId) { mutableStateOf(uiState.deviceId) }
  var useRemoteApiToggle by remember(uiState.useRemoteApi) { mutableStateOf(uiState.useRemoteApi) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "SECURITY CONFIGURATION",
              color = CyberCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp,
            )
            Text(
              "API Endpoints & Biometric Parameters",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberNavy900),
      )
    },
    containerColor = CyberNavy900,
    modifier = modifier,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Secure API Service Settings
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavy800),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "SECURE API CONNECTION",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }

          Text(
            "Configure remote biometric verification server. When enabled, requests are signed and transmitted over TLS with Bearer authentication.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
          )

          // Remote Server vs Local Embedded Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Direct Remote Server Mode", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text(
                if (useRemoteApiToggle) "Routing to remote endpoint via Retrofit" else "Using local embedded biometric engine",
                color = if (useRemoteApiToggle) CyberCyan else Color(0xFF94A3B8),
                fontSize = 11.sp,
              )
            }
            Switch(
              checked = useRemoteApiToggle,
              onCheckedChange = { useRemoteApiToggle = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CyberNavy900,
                checkedTrackColor = CyberCyan,
              ),
              modifier = Modifier.testTag("remote_api_switch"),
            )
          }

          OutlinedTextField(
            value = apiUrlInput,
            onValueChange = { apiUrlInput = it },
            label = { Text("API Server Base URL", color = Color(0xFF94A3B8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().testTag("api_url_input"),
          )

          OutlinedTextField(
            value = apiTokenInput,
            onValueChange = { apiTokenInput = it },
            label = { Text("API Authorization Bearer Key", color = Color(0xFF94A3B8)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
          )
        }
      }

      // 2. Terminal & Gate Location
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavy800),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "TERMINAL IDENTIFIERS",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }

          OutlinedTextField(
            value = gateLocationInput,
            onValueChange = { gateLocationInput = it },
            label = { Text("Location Gate Name", color = Color(0xFF94A3B8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().testTag("gate_name_input"),
          )

          OutlinedTextField(
            value = deviceIdInput,
            onValueChange = { deviceIdInput = it },
            label = { Text("Terminal Scanner Device ID", color = Color(0xFF94A3B8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      // 3. Biometric Sensitivity & Liveness
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavy800),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "BIOMETRIC ENGINE SETTINGS",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }

          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Match Threshold Sensitivity", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text(
                "${(uiState.matchThreshold * 100).toInt()}%",
                color = CyberCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
              )
            }
            Slider(
              value = uiState.matchThreshold,
              onValueChange = { viewModel.setMatchThreshold(it) },
              valueRange = 0.50f..0.95f,
              steps = 8,
              colors = SliderDefaults.colors(
                thumbColor = CyberCyan,
                activeTrackColor = CyberCyan,
                inactiveTrackColor = Color(0xFF334155),
              ),
            )
          }

          // Anti-Spoofing toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Active Anti-Spoofing & Liveness", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("Requires user to blink or turn head to prevent 2D photo/video spoof attacks", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Switch(
              checked = uiState.antiSpoofingEnabled,
              onCheckedChange = { viewModel.toggleAntiSpoofing() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CyberNavy900,
                checkedTrackColor = EmeraldPass,
              ),
              modifier = Modifier.testTag("anti_spoof_switch"),
            )
          }

          if (uiState.antiSpoofingEnabled) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(12.dp),
            ) {
              Text(
                "CHALLENGE GESTURE PREFERENCE:",
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                val challengeOptions = listOf(
                  null to "Random (Recommended)",
                  com.example.biometrics.sdk.LivenessChallengeType.BLINK to "Blink Eyes",
                  com.example.biometrics.sdk.LivenessChallengeType.TURN_LEFT to "Turn Head Left",
                  com.example.biometrics.sdk.LivenessChallengeType.TURN_RIGHT to "Turn Head Right",
                  com.example.biometrics.sdk.LivenessChallengeType.SMILE to "Smile",
                )
                challengeOptions.forEach { (type, label) ->
                  androidx.compose.material3.FilterChip(
                    selected = uiState.selectedChallengePreference == type,
                    onClick = { viewModel.setLivenessChallengePreference(type) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                      selectedContainerColor = CyberCyan,
                      selectedLabelColor = CyberNavy900,
                      containerColor = CyberNavy800,
                      labelColor = Color.White,
                    ),
                  )
                }
              }
            }
          }

          // Haptics & Feedback toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Haptic & Sound Feedback", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("Vibrate device on access granted or denied", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Switch(
              checked = uiState.soundAndHapticsEnabled,
              onCheckedChange = { viewModel.toggleSoundAndHaptics() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CyberNavy900,
                checkedTrackColor = CyberCyan,
              ),
            )
          }
        }
      }

      // Save Settings Button
      Button(
        onClick = {
          viewModel.updateApiSettings(
            apiUrl = apiUrlInput,
            apiToken = apiTokenInput,
            useRemote = useRemoteApiToggle,
            gate = gateLocationInput,
            deviceId = deviceIdInput,
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_settings_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
        shape = RoundedCornerShape(14.dp),
      ) {
        Icon(Icons.Default.Save, contentDescription = null, tint = CyberNavy900, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save Configuration", color = CyberNavy900, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }

      // 4. SDK & Repository Architecture Reference
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224F46E5)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "FacePlugin Open Source SDK Architecture",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
            )
          }
          Text(
            text = "Engineered based on Faceplugin-ltd/Open-Source-Face-Recognition-SDK. Implements 128-dimensional facial embedding vector extraction, cosine similarity metrics, and zero-knowledge verification audit logging with Room & TLS Retrofit.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 16.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}
