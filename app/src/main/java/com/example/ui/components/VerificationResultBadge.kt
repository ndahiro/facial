package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VerificationLog
import com.example.data.model.VerificationStatus
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy700
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.EmeraldPass
import java.util.Locale

@Composable
fun VerificationResultBadge(
  log: VerificationLog?,
  onDismiss: () -> Unit,
  onEnrollNew: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  AnimatedVisibility(
    visible = log != null,
    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    modifier = modifier,
  ) {
    if (log == null) return@AnimatedVisibility

    val isGranted = log.status == VerificationStatus.VERIFIED
    val statusColor = if (isGranted) EmeraldPass else CrimsonFail
    val statusTitle = if (isGranted) "ACCESS GRANTED" else "ACCESS DENIED"
    val statusSubtitle = when (log.status) {
      VerificationStatus.VERIFIED -> "Biometric identity verified via secure API"
      VerificationStatus.UNKNOWN_FACE -> "No matching template in database"
      VerificationStatus.ACCESS_DENIED -> "Confidence below authorization threshold"
      VerificationStatus.SPOOF_DETECTED -> "Liveness check failed - Spoof risk detected"
      VerificationStatus.API_ERROR -> "Secure API verification error"
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("verification_result_card")
        .border(1.5.dp, statusColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
      colors = CardDefaults.cardColors(containerColor = CyberNavy800.copy(alpha = 0.96f)),
      shape = RoundedCornerShape(20.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        // Header Row: Status badge & close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = statusTitle,
                color = statusColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp,
              )
              Text(
                text = statusSubtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("dismiss_result_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Identity Details Section
        if (isGranted && log.personName != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(CyberNavy700, RoundedCornerShape(14.dp))
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Avatar
            val drawableResId = if (log.matchedPhotoDrawable.isNotBlank()) {
              context.resources.getIdentifier(log.matchedPhotoDrawable, "drawable", context.packageName)
            } else 0

            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B)),
              contentAlignment = Alignment.Center,
            ) {
              if (drawableResId != 0) {
                Image(
                  painter = painterResource(id = drawableResId),
                  contentDescription = log.personName,
                  modifier = Modifier.fillMaxWidth(),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Text(
                  text = log.personName.take(2).uppercase(),
                  color = CyberCyan,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = log.personName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "${log.personId ?: "ID-N/A"} • ${log.department ?: "Personnel"}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = log.accessLevel ?: "CLEARANCE ACTIVE",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Confidence score progress bar
        val scorePercent = (log.confidenceScore * 100).toInt()
        val thresholdPercent = (log.matchThreshold * 100).toInt()
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "MATCH SIMILARITY",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
          )
          Text(
            text = "$scorePercent% (Req: $thresholdPercent%)",
            color = if (scorePercent >= thresholdPercent) EmeraldPass else CrimsonFail,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
          progress = { log.confidenceScore.coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = statusColor,
          trackColor = Color(0xFF1E293B),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry Row: Latency, Gate, Timestamp
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "LATENCY: ${log.apiLatencyMs}ms",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
          )
          Text(
            text = log.formattedTime,
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Liveness & Anti-Spoofing Indicator
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "LIVENESS: ${log.livenessChallenge.uppercase()}",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
          )
          Text(
            text = if (log.status == VerificationStatus.SPOOF_DETECTED) "SPOOF REJECTED" else "ACTION CONFIRMED",
            color = if (log.status == VerificationStatus.SPOOF_DETECTED) CrimsonFail else EmeraldPass,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        }

        // Shortcut to enroll face if unknown
        if (!isGranted) {
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              onDismiss()
              onEnrollNew()
            },
            modifier = Modifier.fillMaxWidth().testTag("enroll_unknown_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.PersonAdd,
              contentDescription = null,
              tint = Color(0xFF070B16),
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Enroll Face into Database",
              color = Color(0xFF070B16),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
            )
          }
        }
      }
    }
  }
}
