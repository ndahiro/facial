package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.EnrolledUser
import com.example.ui.components.CameraScannerOverlay
import com.example.ui.components.VerificationResultBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy700
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.EmeraldPass
import com.example.viewmodel.VerificationUiState
import com.example.viewmodel.VerificationViewModel
import java.util.Locale

@Composable
fun VerificationScreen(
  viewModel: VerificationViewModel,
  uiState: VerificationUiState,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  var showThresholdSlider by remember { mutableStateOf(false) }
  var show1to1Dropdown by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize().background(CyberNavy900)) {
    // 1. Camera Preview or Fallback View
    if (hasCameraPermission) {
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
          }

          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
              it.surfaceProvider = previewView.surfaceProvider
            }

            val cameraSelector = if (uiState.useFrontCamera) {
              CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
              CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
              cameraProvider.unbindAll()
              val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
              )
              camera.cameraControl.enableTorch(uiState.isFlashEnabled)
            } catch (e: Exception) {
              // Ignore if preview binding fails in simulator
            }
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        update = { previewView ->
          val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
          cameraProviderFuture.addListener({
            try {
              val cameraProvider = cameraProviderFuture.get()
              val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
              }
              val cameraSelector = if (uiState.useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
              } else {
                CameraSelector.DEFAULT_BACK_CAMERA
              }
              cameraProvider.unbindAll()
              val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
              )
              camera.cameraControl.enableTorch(uiState.isFlashEnabled)
            } catch (_: Exception) {}
          }, ContextCompat.getMainExecutor(previewView.context))
        },
        modifier = Modifier.fillMaxSize().testTag("camera_preview_view"),
      )
    } else {
      // Permission prompt / simulation container
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.radialGradient(
              listOf(CyberNavy800, CyberNavy900),
            ),
          ),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp),
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(56.dp),
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Camera Access Required",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Grant camera permission for real-time facial biometric scanning, or test identities using the simulator probes below.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            modifier = Modifier.testTag("grant_camera_btn"),
          ) {
            Text("Grant Camera Permission", color = CyberNavy900, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 2. High-Tech Cyber Biometric Scanner HUD
    CameraScannerOverlay(
      isScanning = uiState.isVerifying,
      hasFaceInView = true,
      statusColor = if (uiState.lastResult?.isMatch == true) EmeraldPass else CyberCyan,
    )

    // 3. Top Floating Control Panel (Camera Flip, Flash, Mode, Threshold)
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 56.dp, start = 16.dp, end = 16.dp)
        .fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xDD070B16), RoundedCornerShape(16.dp))
          .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(16.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Mode Selector: 1:N Identification vs 1:1 Claim
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clickable {
                val nextMode = if (uiState.verificationMode == "1:N Identification") {
                  "1:1 Verification"
                } else {
                  "1:N Identification"
                }
                viewModel.setVerificationMode(nextMode)
              }
              .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("mode_toggle_chip"),
          ) {
            Text(
              text = if (uiState.verificationMode == "1:N Identification") "MODE: 1:N GALLERY" else "MODE: 1:1 VERIFY",
              color = CyberCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }

          if (uiState.verificationMode == "1:1 Verification") {
            Spacer(modifier = Modifier.width(6.dp))
            Box {
              Text(
                text = uiState.selectedClaimedUser?.name?.take(10) ?: "Select User",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                  .clickable { show1to1Dropdown = true }
                  .background(CyberNavy700, RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 4.dp),
              )
              DropdownMenu(
                expanded = show1to1Dropdown,
                onDismissRequest = { show1to1Dropdown = false },
              ) {
                uiState.enrolledUsers.forEach { user ->
                  DropdownMenuItem(
                    text = { Text(user.name) },
                    onClick = {
                      viewModel.setSelectedClaimedUser(user)
                      show1to1Dropdown = false
                    },
                  )
                }
              }
            }
          }
        }

        // Action Icons: Flash, Camera Flip, Threshold
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { viewModel.toggleFlash() },
            modifier = Modifier.size(36.dp).testTag("flash_toggle_btn"),
          ) {
            Icon(
              imageVector = if (uiState.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
              contentDescription = "Flash",
              tint = if (uiState.isFlashEnabled) Color(0xFFFBBF24) else Color(0xFF94A3B8),
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = { viewModel.toggleCameraFacing() },
            modifier = Modifier.size(36.dp).testTag("flip_camera_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.Cameraswitch,
              contentDescription = "Flip Camera",
              tint = CyberCyan,
              modifier = Modifier.size(20.dp),
            )
          }

          IconButton(
            onClick = { showThresholdSlider = !showThresholdSlider },
            modifier = Modifier.size(36.dp).testTag("threshold_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Threshold",
              tint = if (showThresholdSlider) CyberCyan else Color(0xFF94A3B8),
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      // Collapsible threshold slider
      AnimatedVisibility(visible = showThresholdSlider) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xEE070B16)),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300F0FF)),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Match Threshold Sensitivity", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text(
                "${(uiState.matchThreshold * 100).toInt()}%",
                color = CyberCyan,
                fontSize = 12.sp,
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
              modifier = Modifier.testTag("threshold_slider"),
            )
            Text(
              "Higher threshold increases strictness against false positives; 75% is recommended for enterprise access gates.",
              color = Color(0xFF94A3B8),
              fontSize = 10.sp,
            )
          }
        }
      }
    }

    // 4. Bottom Controls & Quick Probe Test Identities (for instant evaluation in emulator or real device)
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
        .fillMaxWidth(),
    ) {
      // Test Probe Quick-Selector: Let the user or tester click an identity to simulate face capture probe
      Text(
        text = "PROBE FACE & ANTI-SPOOF TEST BAR:",
        color = Color(0xFF94A3B8),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        uiState.enrolledUsers.take(4).forEach { user ->
          FilterChip(
            selected = false,
            onClick = { viewModel.testWithEnrolledProbe(user) },
            label = {
              Text(
                user.name.split(" ").firstOrNull() ?: user.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(14.dp),
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              containerColor = Color(0xCC0D1527),
              labelColor = Color.White,
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = false,
              borderColor = Color(0x3300F0FF),
            ),
            modifier = Modifier.testTag("probe_chip_${user.id}"),
          )
        }

        // Unknown test face
        FilterChip(
          selected = false,
          onClick = { viewModel.testWithUnknownFace() },
          label = { Text("Unknown Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(14.dp),
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xCC1A0B10),
            labelColor = Color(0xFFFCA5A5),
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = Color(0x44EF4444),
          ),
          modifier = Modifier.testTag("probe_chip_unknown"),
        )

        // Static photo spoof attack simulator
        FilterChip(
          selected = false,
          onClick = { viewModel.simulateStaticPhotoSpoof() },
          label = { Text("Test Photo Spoof", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(14.dp),
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xCC3B1117),
            labelColor = Color(0xFFFCA5A5),
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = Color(0x88EF4444),
          ),
          modifier = Modifier.testTag("simulate_static_spoof_chip"),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Main Live Verification Button
      Button(
        onClick = { viewModel.triggerVerification() },
        enabled = !uiState.isVerifying && uiState.livenessPhase == com.example.viewmodel.LivenessPhase.IDLE,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("trigger_verify_btn"),
        colors = ButtonDefaults.buttonColors(
          containerColor = CyberCyan,
          disabledContainerColor = CyberCyan.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
      ) {
        if (uiState.isVerifying) {
          CircularProgressIndicator(
            color = CyberNavy900,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "VERIFYING BIOMETRICS...",
            color = CyberNavy900,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
          )
        } else {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = CyberNavy900,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (uiState.antiSpoofingEnabled) "VERIFY WITH ACTIVE LIVENESS" else "SCAN & VERIFY IDENTITY",
            color = CyberNavy900,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
          )
        }
      }
    }

    // 5. Active Liveness Challenge HUD (Center modal prompt when active challenge is initiated)
    com.example.ui.components.LivenessChallengeHud(
      phase = uiState.livenessPhase,
      challenge = uiState.activeLivenessChallenge,
      secondsRemaining = uiState.livenessSecondsRemaining,
      progress = uiState.livenessProgress,
      onPerformAction = { action -> viewModel.completeLivenessAction(action) },
      onSimulateStaticSpoof = { viewModel.simulateStaticPhotoSpoof() },
      onCancel = { viewModel.cancelLivenessChallenge() },
      modifier = Modifier
        .align(Alignment.Center)
        .padding(16.dp),
    )

    // 6. Verification Result Modal / Card (Overlaid at center-bottom)
    VerificationResultBadge(
      log = uiState.lastLog,
      onDismiss = { viewModel.dismissResultBadge() },
      onEnrollNew = { viewModel.setShowEnrollDialog(true) },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 80.dp),
    )
  }
}
