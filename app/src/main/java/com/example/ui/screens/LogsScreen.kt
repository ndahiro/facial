package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.EmeraldPass
import com.example.viewmodel.VerificationUiState
import com.example.viewmodel.VerificationViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
  viewModel: VerificationViewModel,
  uiState: VerificationUiState,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val filteredLogs = remember(uiState.verificationLogs, uiState.logSearchQuery, uiState.logFilterStatus) {
    uiState.verificationLogs.filter { log ->
      val matchesQuery = uiState.logSearchQuery.isBlank() ||
        (log.personName?.contains(uiState.logSearchQuery, ignoreCase = true) == true) ||
        (log.personId?.contains(uiState.logSearchQuery, ignoreCase = true) == true) ||
        (log.department?.contains(uiState.logSearchQuery, ignoreCase = true) == true) ||
        (log.locationGate.contains(uiState.logSearchQuery, ignoreCase = true))

      val matchesStatus = uiState.logFilterStatus == null || log.status == uiState.logFilterStatus
      matchesQuery && matchesStatus
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "VERIFICATION AUDIT LOGS",
              color = CyberCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp,
            )
            Text(
              "Secure Event Trail & Timestamps",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
            )
          }
        },
        actions = {
          if (uiState.verificationLogs.isNotEmpty()) {
            IconButton(
              onClick = { showClearConfirmDialog = true },
              modifier = Modifier.testTag("clear_logs_btn"),
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear Logs",
                tint = Color(0xFF94A3B8),
              )
            }
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
        .padding(paddingValues),
    ) {
      // 1. Metric Overview Cards
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricCard(
          label = "TOTAL SCANS",
          value = "${uiState.totalLogsCount}",
          color = CyberCyan,
          modifier = Modifier.weight(1f),
        )
        MetricCard(
          label = "VERIFIED",
          value = "${uiState.verifiedCount}",
          color = EmeraldPass,
          modifier = Modifier.weight(1f),
        )
        MetricCard(
          label = "DENIED",
          value = "${uiState.deniedCount}",
          color = CrimsonFail,
          modifier = Modifier.weight(1f),
        )
      }

      // 2. Search Field
      OutlinedTextField(
        value = uiState.logSearchQuery,
        onValueChange = { viewModel.setLogSearchQuery(it) },
        placeholder = { Text("Search by name, ID, or gate...", color = Color(0xFF64748B), fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (uiState.logSearchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setLogSearchQuery("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = CyberNavy800,
          unfocusedContainerColor = CyberNavy800,
          focusedBorderColor = CyberCyan,
          unfocusedBorderColor = Color(0xFF1E293B),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("search_logs_input"),
      )

      // 3. Status Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = uiState.logFilterStatus == null,
          onClick = { viewModel.setLogFilterStatus(null) },
          label = { Text("All", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyberCyan,
            selectedLabelColor = CyberNavy900,
            containerColor = CyberNavy800,
            labelColor = Color.White,
          ),
          modifier = Modifier.testTag("filter_all_chip"),
        )
        FilterChip(
          selected = uiState.logFilterStatus == VerificationStatus.VERIFIED,
          onClick = { viewModel.setLogFilterStatus(VerificationStatus.VERIFIED) },
          label = { Text("Verified", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPass,
            selectedLabelColor = Color.White,
            containerColor = CyberNavy800,
            labelColor = Color.White,
          ),
          modifier = Modifier.testTag("filter_verified_chip"),
        )
        FilterChip(
          selected = uiState.logFilterStatus == VerificationStatus.UNKNOWN_FACE || uiState.logFilterStatus == VerificationStatus.ACCESS_DENIED,
          onClick = { viewModel.setLogFilterStatus(VerificationStatus.UNKNOWN_FACE) },
          label = { Text("Denied", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CrimsonFail,
            selectedLabelColor = Color.White,
            containerColor = CyberNavy800,
            labelColor = Color.White,
          ),
          modifier = Modifier.testTag("filter_denied_chip"),
        )
      }

      // 4. Log Events List
      if (filteredLogs.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              "No Verification Events Found",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Perform a face scan or adjust filters to view recorded verification events.",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize().testTag("logs_lazy_column"),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(filteredLogs, key = { it.id }) { log ->
            LogEventCard(
              log = log,
              onClick = { viewModel.selectLogForDetails(log) },
            )
          }
        }
      }
    }
  }

  // Cryptographic Audit Detail Modal Dialog
  uiState.selectedLogForDetails?.let { detailLog ->
    AuditDetailDialog(
      log = detailLog,
      onDismiss = { viewModel.selectLogForDetails(null) },
      onShare = {
        val shareText = """
          |=== BIOMETRIC VERIFICATION AUDIT RECORD ===
          |ID: #${detailLog.id}
          |Timestamp: ${detailLog.formattedTime}
          |Status: ${detailLog.status.name}
          |Candidate: ${detailLog.personName ?: "UNREGISTERED SUBJECT"} (${detailLog.personId ?: "N/A"})
          |Department: ${detailLog.department ?: "N/A"}
          |Clearance: ${detailLog.accessLevel ?: "NONE"}
          |Match Confidence: ${String.format(Locale.US, "%.1f", detailLog.confidenceScore * 100)}%
          |Threshold: ${String.format(Locale.US, "%.1f", detailLog.matchThreshold * 100)}%
          |Active Liveness: ${detailLog.livenessChallenge} (${detailLog.livenessResult})
          |Gate: ${detailLog.locationGate}
          |Mode: ${detailLog.verificationMode}
          |API Latency: ${detailLog.apiLatencyMs}ms
          |Audit Signature: ${detailLog.auditSignature}
          |===========================================
        """.trimMargin()

        val sendIntent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, shareText)
          type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Verification Audit Record")
        context.startActivity(shareIntent)
      },
    )
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear All Audit Logs?", color = Color.White) },
      text = { Text("This will permanently remove all recorded verification events from local storage.", color = Color(0xFF94A3B8)) },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllLogs()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonFail),
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel", color = Color.White)
        }
      },
      containerColor = CyberNavy800,
    )
  }
}

@Composable
private fun MetricCard(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = CyberNavy800),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        color = Color(0xFF94A3B8),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        color = color,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
      )
    }
  }
}

@Composable
private fun LogEventCard(
  log: VerificationLog,
  onClick: () -> Unit,
) {
  val isVerified = log.status == VerificationStatus.VERIFIED
  val statusColor = if (isVerified) EmeraldPass else CrimsonFail

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("log_card_${log.id}"),
    colors = CardDefaults.cardColors(containerColor = CyberNavy800),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
    ) {
      // Row 1: Status badge & Timestamp
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
              .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Text(
              text = if (isVerified) "VERIFIED" else log.status.name,
              color = statusColor,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${(log.confidenceScore * 100).toInt()}% Match",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        }

        // Formatted Timestamp with clock
        Text(
          text = log.formattedTime,
          color = Color(0xFF94A3B8),
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2: Subject info or Unknown face notice
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = log.personName ?: "Unknown Face (Unrecognized)",
            color = if (isVerified) Color.White else Color(0xFFFCA5A5),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${log.personId ?: "ID: NONE"} • ${log.department ?: "Visitor / Unknown"} • ${log.locationGate}",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
          )
        }

        Text(
          text = "${log.apiLatencyMs}ms",
          color = CyberCyan,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Micro Progress Bar for Confidence
      LinearProgressIndicator(
        progress = { log.confidenceScore.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = statusColor,
        trackColor = Color(0xFF1E293B),
      )
    }
  }
}

@Composable
private fun AuditDetailDialog(
  log: VerificationLog,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
) {
  val isVerified = log.status == VerificationStatus.VERIFIED
  val statusColor = if (isVerified) EmeraldPass else CrimsonFail

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          "AUDIT RECORD #${log.id}",
          color = CyberCyan,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AuditRow("Event Status", log.status.name, statusColor)
        AuditRow("Timestamp", log.formattedTime, Color.White)
        AuditRow("Subject", log.personName ?: "UNREGISTERED", Color.White)
        AuditRow("Employee ID", log.personId ?: "N/A", CyberCyan)
        AuditRow("Department", log.department ?: "N/A", Color(0xFF94A3B8))
        AuditRow("Access Clearance", log.accessLevel ?: "NONE", CyberCyan)
        AuditRow("Confidence Score", "${String.format(Locale.US, "%.2f", log.confidenceScore * 100)}%", statusColor)
        AuditRow("Threshold Required", "${String.format(Locale.US, "%.1f", log.matchThreshold * 100)}%", Color(0xFF94A3B8))
        AuditRow("Active Liveness", "${log.livenessChallenge} [${log.livenessResult}]", if (log.status == VerificationStatus.SPOOF_DETECTED) CrimsonFail else EmeraldPass)
        AuditRow("API Roundtrip Latency", "${log.apiLatencyMs} ms", Color.White)
        AuditRow("Checkpoint Gate", log.locationGate, Color(0xFF94A3B8))
        AuditRow("Audit Signature", log.auditSignature, CyberCyan)

        Spacer(modifier = Modifier.height(4.dp))
        Text("Verification Notes:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(log.notes, color = Color(0xFFCBD5E1), fontSize = 12.sp)
      }
    },
    confirmButton = {
      Button(
        onClick = onShare,
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
        modifier = Modifier.testTag("share_audit_btn"),
      ) {
        Icon(Icons.Default.Share, contentDescription = null, tint = CyberNavy900, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export Record", color = CyberNavy900, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = Color.White)
      }
    },
    containerColor = CyberNavy800,
    shape = RoundedCornerShape(20.dp),
  )
}

@Composable
private fun AuditRow(label: String, value: String, valueColor: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    Text(
      value,
      color = valueColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
    )
  }
}
