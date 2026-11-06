package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnrolledUser
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy700
import com.example.ui.theme.CyberNavy800
import com.example.ui.theme.CyberNavy900
import com.example.ui.theme.EmeraldPass
import com.example.viewmodel.VerificationUiState
import com.example.viewmodel.VerificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrolledDatabaseScreen(
  viewModel: VerificationViewModel,
  uiState: VerificationUiState,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var userToDelete by remember { mutableStateOf<EnrolledUser?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "ENROLLED FACE DATABASE",
              color = CyberCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp,
            )
            Text(
              "${uiState.enrolledUsers.size} Registered Identities Active",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberNavy900),
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.setShowEnrollDialog(true) },
        containerColor = CyberCyan,
        contentColor = CyberNavy900,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("enroll_fab"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Enroll Face", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    },
    containerColor = CyberNavy900,
    modifier = modifier,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
      // Info Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .background(CyberNavy800, RoundedCornerShape(12.dp))
          .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp))
          .padding(12.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Face identities are indexed with 128-d L2-normalized vector embeddings matching the FacePlugin Open Source SDK standard.",
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
          )
        }
      }

      // Personnel List
      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("enrolled_users_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(uiState.enrolledUsers, key = { it.id }) { user ->
          EnrolledUserCard(
            user = user,
            onToggleActive = { viewModel.toggleUserActive(user) },
            onDelete = { userToDelete = user },
          )
        }
      }
    }
  }

  // Enroll Face Dialog
  if (uiState.showEnrollDialog) {
    EnrollFaceDialog(
      onDismiss = { viewModel.setShowEnrollDialog(false) },
      onEnroll = { name, empId, dept, role, accessLevel ->
        viewModel.enrollUser(name, empId, dept, role, accessLevel)
      },
    )
  }

  // Delete User Confirmation Dialog
  userToDelete?.let { targetUser ->
    AlertDialog(
      onDismissRequest = { userToDelete = null },
      title = { Text("Delete Identity?", color = Color.White) },
      text = {
        Text(
          "Are you sure you want to remove ${targetUser.name} (${targetUser.employeeId}) from the facial recognition database?",
          color = Color(0xFF94A3B8),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteUser(targetUser.id)
            userToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonFail),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { userToDelete = null }) {
          Text("Cancel", color = Color.White)
        }
      },
      containerColor = CyberNavy800,
    )
  }
}

@Composable
private fun EnrolledUserCard(
  user: EnrolledUser,
  onToggleActive: () -> Unit,
  onDelete: () -> Unit,
) {
  val context = LocalContext.current
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
  val enrolledDateStr = dateFormat.format(Date(user.enrolledAt))

  val drawableResId = if (user.photoDrawableName.isNotBlank()) {
    context.resources.getIdentifier(user.photoDrawableName, "drawable", context.packageName)
  } else 0

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("enrolled_card_${user.id}"),
    colors = CardDefaults.cardColors(containerColor = CyberNavy800),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (user.isActive) Color(0xFF1E293B) else Color(0x33EF4444),
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Avatar
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(CyberNavy700),
        contentAlignment = Alignment.Center,
      ) {
        if (drawableResId != 0) {
          Image(
            painter = painterResource(id = drawableResId),
            contentDescription = user.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        } else {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(28.dp),
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // User Info
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = user.name,
            color = if (user.isActive) Color.White else Color(0xFF94A3B8),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .background(
                if (user.isActive) EmeraldPass.copy(alpha = 0.15f) else Color(0x22EF4444),
                RoundedCornerShape(6.dp),
              )
              .padding(horizontal = 6.dp, vertical = 2.dp),
          ) {
            Text(
              text = if (user.isActive) "ACTIVE" else "DISABLED",
              color = if (user.isActive) EmeraldPass else CrimsonFail,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${user.employeeId} • ${user.department}",
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = user.accessLevel,
            color = CyberCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
          Text(
            text = "• Enrolled $enrolledDateStr",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
          )
        }
      }

      // Actions: Toggle / Delete
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp).testTag("delete_user_${user.id}"),
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete User",
          tint = Color(0xFF64748B),
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollFaceDialog(
  onDismiss: () -> Unit,
  onEnroll: (name: String, empId: String, dept: String, role: String, accessLevel: String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var employeeId by remember { mutableStateOf("EMP-NEW-${(100..999).random()}") }
  var department by remember { mutableStateOf("Engineering & Ops") }
  var role by remember { mutableStateOf("Staff Member") }
  var accessLevel by remember { mutableStateOf("LEVEL_3_SECURE") }

  val accessLevels = listOf("LEVEL_4_ALPHA", "LEVEL_3_SECURE", "LEVEL_2_STAFF", "LEVEL_1_GUEST")
  var accessDropdownExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("ENROLL NEW FACE", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          "Register a new biometric profile. The facial feature extractor will compute and store a 128-dimensional embedding vector.",
          color = Color(0xFF94A3B8),
          fontSize = 11.sp,
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name", color = Color(0xFF94A3B8)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = Color(0xFF334155),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
          ),
          modifier = Modifier.fillMaxWidth().testTag("enroll_name_input"),
        )

        OutlinedTextField(
          value = employeeId,
          onValueChange = { employeeId = it },
          label = { Text("Employee / Identity ID", color = Color(0xFF94A3B8)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = Color(0xFF334155),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
          ),
          modifier = Modifier.fillMaxWidth().testTag("enroll_empid_input"),
        )

        OutlinedTextField(
          value = department,
          onValueChange = { department = it },
          label = { Text("Department", color = Color(0xFF94A3B8)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = Color(0xFF334155),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
          ),
          modifier = Modifier.fillMaxWidth(),
        )

        // Access Level Dropdown
        ExposedDropdownMenuBox(
          expanded = accessDropdownExpanded,
          onExpandedChange = { accessDropdownExpanded = !accessDropdownExpanded },
        ) {
          OutlinedTextField(
            value = accessLevel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Clearance Tier", color = Color(0xFF94A3B8)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accessDropdownExpanded) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = CyberCyan,
              unfocusedTextColor = CyberCyan,
            ),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
          )
          ExposedDropdownMenu(
            expanded = accessDropdownExpanded,
            onDismissRequest = { accessDropdownExpanded = false },
          ) {
            accessLevels.forEach { level ->
              DropdownMenuItem(
                text = { Text(level) },
                onClick = {
                  accessLevel = level
                  accessDropdownExpanded = false
                },
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onEnroll(name, employeeId, department, role, accessLevel)
          }
        },
        enabled = name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
        modifier = Modifier.testTag("submit_enroll_btn"),
      ) {
        Text("Compute & Save Face", color = CyberNavy900, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color.White)
      }
    },
    containerColor = CyberNavy800,
    shape = RoundedCornerShape(20.dp),
  )
}
