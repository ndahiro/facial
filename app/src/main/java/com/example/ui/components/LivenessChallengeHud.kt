package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biometrics.sdk.LivenessChallenge
import com.example.biometrics.sdk.LivenessChallengeType
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.EmeraldPass
import com.example.viewmodel.LivenessPhase

@Composable
fun LivenessChallengeHud(
  phase: LivenessPhase,
  challenge: LivenessChallenge?,
  secondsRemaining: Int,
  progress: Float,
  onPerformAction: (LivenessChallengeType) -> Unit,
  onSimulateStaticSpoof: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_liveness")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "liveness_pulse",
  )

  AnimatedVisibility(
    visible = phase != LivenessPhase.IDLE,
    enter = fadeIn() + scaleIn(initialScale = 0.85f),
    exit = fadeOut() + scaleOut(targetScale = 0.85f),
    modifier = modifier,
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("liveness_challenge_card"),
      colors = CardDefaults.cardColors(containerColor = CyberNavy900.copy(alpha = 0.95f)),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(
        2.dp,
        when (phase) {
          LivenessPhase.ACTION_DETECTED -> EmeraldPass
          LivenessPhase.SPOOF_ATTACK_DETECTED -> CrimsonFail
          else -> CyberCyan
        },
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(
                  when (phase) {
                    LivenessPhase.ACTION_DETECTED -> EmeraldPass
                    LivenessPhase.SPOOF_ATTACK_DETECTED -> CrimsonFail
                    else -> CyberCyan
                  },
                  CircleShape,
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ACTIVE LIVENESS CHECK",
              color = CyberCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp,
            )
          }

          IconButton(
            onClick = onCancel,
            modifier = Modifier.size(28.dp).testTag("cancel_liveness_btn"),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (phase) {
          LivenessPhase.PROMPTING_ACTION -> {
            val challengeType = challenge?.type ?: LivenessChallengeType.BLINK
            val challengeIcon: ImageVector = when (challengeType) {
              LivenessChallengeType.BLINK -> Icons.Default.RemoveRedEye
              LivenessChallengeType.TURN_LEFT -> Icons.Default.TurnLeft
              LivenessChallengeType.TURN_RIGHT -> Icons.Default.TurnRight
              LivenessChallengeType.SMILE -> Icons.Default.SentimentSatisfied
            }

            // Animated Central Challenge Glyph with Circular Timer Progress
            Box(
              modifier = Modifier
                .size(100.dp)
                .scale(pulseScale),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(
                progress = { 1f - progress },
                modifier = Modifier.size(100.dp),
                color = CyberCyan,
                trackColor = Color(0xFF1E293B),
                strokeWidth = 4.dp,
              )
              Box(
                modifier = Modifier
                  .size(80.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0F172A))
                  .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = challengeIcon,
                  contentDescription = null,
                  tint = CyberCyan,
                  modifier = Modifier.size(40.dp),
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = challengeType.instruction.uppercase(),
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = challengeType.hint,
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action / Simulation Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              // Action confirmed trigger
              Button(
                onClick = { onPerformAction(challengeType) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1.3f)
                  .height(44.dp)
                  .testTag("confirm_liveness_action_btn"),
              ) {
                Text(
                  text = "I Did Action ($secondsRemaining s)",
                  color = CyberNavy900,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                )
              }

              // Test Spoof Attack button (Simulates holding a static photo or video replay)
              Button(
                onClick = onSimulateStaticSpoof,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonFail.copy(alpha = 0.6f)),
                modifier = Modifier
                  .weight(1.1f)
                  .height(44.dp)
                  .testTag("simulate_spoof_btn"),
              ) {
                Text(
                  text = "Test Static Spoof",
                  color = Color(0xFFFCA5A5),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }

          LivenessPhase.ACTION_DETECTED -> {
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(EmeraldPass.copy(alpha = 0.15f))
                .border(2.dp, EmeraldPass, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldPass,
                modifier = Modifier.size(44.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "LIVENESS ACTION CONFIRMED",
              color = EmeraldPass,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              text = "Natural human motion verified. Matching facial embeddings...",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
            )
          }

          LivenessPhase.SPOOF_ATTACK_DETECTED -> {
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(CrimsonFail.copy(alpha = 0.15f))
                .border(2.dp, CrimsonFail, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = CrimsonFail,
                modifier = Modifier.size(44.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "SPOOF ATTACK DETECTED",
              color = CrimsonFail,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              text = "Zero natural motion detected. Static photo or video replay rejected.",
              color = Color(0xFFFCA5A5),
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
            )
          }

          LivenessPhase.ACTION_TIMED_OUT -> {
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0x22F59E0B))
                .border(2.dp, Color(0xFFF59E0B), CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(44.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "LIVENESS TIMED OUT",
              color = Color(0xFFF59E0B),
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              text = "Required active gesture was not completed in time.",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
            )
          }

          else -> {}
        }
      }
    }
  }
}
