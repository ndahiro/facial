package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldPass

@Composable
fun CameraScannerOverlay(
  isScanning: Boolean,
  hasFaceInView: Boolean = true,
  statusColor: Color = CyberCyan,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val scanProgress by infiniteTransition.animateFloat(
    initialValue = 0.05f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "laser_pos",
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse_alpha",
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val totalWidth = maxWidth
    val totalHeight = maxHeight
    val reticleSize = (minOf(totalWidth, totalHeight) * 0.72f).coerceIn(240.dp, 360.dp)

    // Futuristic biometric corner reticles & laser line
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val reticlePx = reticleSize.toPx()
      val left = (w - reticlePx) / 2f
      val top = (h - reticlePx) / 2f
      val right = left + reticlePx
      val bottom = top + reticlePx
      val cornerLen = reticlePx * 0.22f

      val primaryColor = statusColor
      val strokeW = 4.dp.toPx()

      // Top-Left Corner
      val tlPath = Path().apply {
        moveTo(left, top + cornerLen)
        lineTo(left, top)
        lineTo(left + cornerLen, top)
      }
      drawPath(tlPath, primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

      // Top-Right Corner
      val trPath = Path().apply {
        moveTo(right - cornerLen, top)
        lineTo(right, top)
        lineTo(right, top + cornerLen)
      }
      drawPath(trPath, primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

      // Bottom-Left Corner
      val blPath = Path().apply {
        moveTo(left, bottom - cornerLen)
        lineTo(left, bottom)
        lineTo(left + cornerLen, bottom)
      }
      drawPath(blPath, primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

      // Bottom-Right Corner
      val brPath = Path().apply {
        moveTo(right - cornerLen, bottom)
        lineTo(right, bottom)
        lineTo(right, bottom - cornerLen)
      }
      drawPath(brPath, primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

      // Central target crosshair dots
      val cx = w / 2f
      val cy = h / 2f
      drawCircle(primaryColor.copy(alpha = 0.4f), radius = 3.dp.toPx(), center = Offset(cx, cy))
      drawCircle(primaryColor.copy(alpha = 0.25f), radius = 24.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.dp.toPx()))

      // Horizontal animated laser beam
      if (isScanning) {
        val laserY = top + (bottom - top) * scanProgress
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              primaryColor.copy(alpha = 0.0f),
              primaryColor.copy(alpha = pulseAlpha * 0.45f),
              primaryColor.copy(alpha = pulseAlpha),
              primaryColor.copy(alpha = pulseAlpha * 0.45f),
              primaryColor.copy(alpha = 0.0f),
            ),
            startY = laserY - 12.dp.toPx(),
            endY = laserY + 12.dp.toPx(),
          ),
          topLeft = Offset(left, laserY - 12.dp.toPx()),
          size = Size(reticlePx, 24.dp.toPx()),
        )

        // Sharp center laser ray
        drawLine(
          color = Color.White.copy(alpha = 0.9f),
          start = Offset(left, laserY),
          end = Offset(right, laserY),
          strokeWidth = 2.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }

    // Telemetry Top Bar inside Scanner HUD
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        .fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Biometric HUD Status Tag
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(Color(0xCC070B16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(
                if (isScanning) EmeraldPass else CyberCyan,
                CircleShape,
              ),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isScanning) "AI SCANNING ACTIVE" else "HUD READY",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        }

        // Security / Encryption Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(Color(0xCC070B16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x334F46E5), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(12.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "TLS 1.3 SECURE",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
          )
        }
      }
    }

    // Bottom Biometric Sensor Metrics
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        .background(Color(0xB3070B16), RoundedCornerShape(16.dp))
        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("LANDMARKS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("68-PTS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
      Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x33FFFFFF)))
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("EMBEDDING", color = Color(0xFF94A3B8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("128-DIM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
      Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x33FFFFFF)))
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("LIVENESS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("PASS 99%", color = EmeraldPass, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    }
  }
}
