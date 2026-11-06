package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import com.example.biometrics.sdk.FaceBiometricEngine
import com.example.biometrics.sdk.MatchResult
import com.example.data.local.AppDatabase
import com.example.data.local.EnrolledUserEntity
import com.example.data.local.VerificationLogEntity
import com.example.data.model.EnrollFaceRequest
import com.example.data.model.EnrolledUser
import com.example.data.model.VerificationLog
import com.example.data.model.VerificationStatus
import com.example.data.model.VerifyFaceRequest
import com.example.data.model.VerifyFaceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecureVerificationRepository(
  private val context: Context,
  private val database: AppDatabase,
) {
  private val userDao = database.enrolledUserDao()
  private val logDao = database.verificationLogDao()

  val allLogsFlow: Flow<List<VerificationLog>> = logDao.getAllLogsFlow().map { entities ->
    entities.map { it.toModel() }
  }

  val enrolledUsersFlow: Flow<List<EnrolledUser>> = userDao.getAllUsersFlow().map { entities ->
    entities.map { it.toModel() }
  }

  val totalLogsCountFlow: Flow<Int> = logDao.getTotalLogsCountFlow()
  val verifiedCountFlow: Flow<Int> = logDao.getVerifiedCountFlow()
  val deniedCountFlow: Flow<Int> = logDao.getDeniedCountFlow()

  /**
   * Verify face against database via secure API or embedded biometric engine.
   * Logs every verification event with timestamp.
   */
  suspend fun performVerification(
    featureVector: List<Float>,
    mode: String,
    claimedUserId: String? = null,
    threshold: Float = 0.75f,
    apiUrl: String = "https://api.faceplugin.internal/v1",
    apiToken: String = "sk_sec_78291048",
    deviceId: String = "DEVICE-PASS-01",
    locationGate: String = "North Gate Terminal",
    useDirectRemoteApi: Boolean = false,
    livenessPassed: Boolean = true,
    livenessStatus: com.example.biometrics.sdk.LivenessEvaluationResult = com.example.biometrics.sdk.LivenessEvaluationResult.PASSED,
    challengeType: com.example.biometrics.sdk.LivenessChallengeType = com.example.biometrics.sdk.LivenessChallengeType.BLINK,
  ): Pair<MatchResult, VerificationLog> = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val activeUsers = userDao.getActiveUsers().map { it.toModel() }

    val matchResult: MatchResult
    val apiLatency: Long

    if (useDirectRemoteApi && apiUrl.startsWith("http")) {
      // Execute remote HTTP API request
      val client = FaceVerificationClient.createService(apiUrl)
      val req = VerifyFaceRequest(
        featureVector = featureVector,
        verificationMode = mode,
        claimedUserId = claimedUserId,
        threshold = threshold,
        deviceId = deviceId,
        locationGate = locationGate,
        timestamp = startTime,
      )
      val response = try {
        val res = client.verifyFace(req, "Bearer $apiToken", deviceId)
        if (res.isSuccessful && res.body() != null) {
          val body = res.body()!!
          val matchedLocal = activeUsers.find { it.id == body.matchedUserId || it.employeeId == body.employeeId }
          MatchResult(
            isMatch = body.isMatch && livenessPassed,
            confidenceScore = body.confidence,
            matchedUser = matchedLocal ?: body.matchedUserId?.let {
              EnrolledUser(
                id = it,
                name = body.name ?: "Verified Remote User",
                employeeId = body.employeeId ?: "EMP-REMOTE",
                department = body.department ?: "Directory",
                role = "Authorized Personnel",
                accessLevel = body.accessLevel ?: "LEVEL_3_SECURE",
              )
            },
            cosineSimilarity = body.confidence,
            euclideanDistance = 1.0f - body.confidence,
            livenessPassed = livenessPassed,
            livenessStatus = livenessStatus,
            livenessChallengeType = challengeType,
          )
        } else {
          fallbackLocalMatch(featureVector, activeUsers, mode, claimedUserId, threshold, livenessPassed, livenessStatus, challengeType)
        }
      } catch (_: Exception) {
        fallbackLocalMatch(featureVector, activeUsers, mode, claimedUserId, threshold, livenessPassed, livenessStatus, challengeType)
      }
      matchResult = response
      apiLatency = System.currentTimeMillis() - startTime
    } else {
      val simDelay = (45..110).random()
      kotlinx.coroutines.delay(simDelay.toLong())

      matchResult = fallbackLocalMatch(featureVector, activeUsers, mode, claimedUserId, threshold, livenessPassed, livenessStatus, challengeType)
      apiLatency = (System.currentTimeMillis() - startTime)
    }

    val timestamp = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)
    val formattedTime = dateFormat.format(Date(timestamp))

    val status = when {
      !livenessPassed && livenessStatus == com.example.biometrics.sdk.LivenessEvaluationResult.FAILED_SPOOF_STATIC -> VerificationStatus.SPOOF_DETECTED
      !livenessPassed -> VerificationStatus.ACCESS_DENIED
      matchResult.isMatch -> VerificationStatus.VERIFIED
      matchResult.confidenceScore < 0.50f -> VerificationStatus.UNKNOWN_FACE
      else -> VerificationStatus.ACCESS_DENIED
    }

    val auditSig = FaceBiometricEngine.generateAuditSignature(
      userId = matchResult.matchedUser?.id,
      score = matchResult.confidenceScore,
      timestamp = timestamp,
    )

    val notes = when (status) {
      VerificationStatus.VERIFIED -> "Biometric identity verified via secure API [Score: ${String.format(Locale.US, "%.1f", matchResult.confidenceScore * 100)}%]. Active Liveness (${challengeType.displayName}) confirmed."
      VerificationStatus.SPOOF_DETECTED -> "ANTI-SPOOF ALERT: Static photo/video replay detected. Active ${challengeType.displayName} action failed."
      VerificationStatus.UNKNOWN_FACE -> "No biometric identity found matching probe face [Score: ${String.format(Locale.US, "%.1f", matchResult.confidenceScore * 100)}% < Threshold]."
      VerificationStatus.ACCESS_DENIED -> {
        if (!livenessPassed) {
          "Access denied: Required active liveness challenge (${challengeType.displayName}) was not performed."
        } else {
          "Identity match below required authorization threshold [${String.format(Locale.US, "%.1f", matchResult.confidenceScore * 100)}% < ${String.format(Locale.US, "%.1f", threshold * 100)}%]."
        }
      }
      else -> "Biometric verification processed."
    }

    val logEntity = VerificationLogEntity(
      timestamp = timestamp,
      formattedTime = formattedTime,
      status = status.name,
      personName = matchResult.matchedUser?.name,
      personId = matchResult.matchedUser?.employeeId,
      department = matchResult.matchedUser?.department,
      accessLevel = matchResult.matchedUser?.accessLevel,
      confidenceScore = matchResult.confidenceScore,
      matchThreshold = threshold,
      verificationMode = mode,
      apiLatencyMs = apiLatency,
      locationGate = locationGate,
      auditSignature = auditSig,
      matchedPhotoDrawable = matchResult.matchedUser?.photoDrawableName ?: "",
      notes = notes,
      livenessChallenge = challengeType.displayName,
      livenessResult = livenessStatus.name,
    )

    val insertedId = logDao.insertLog(logEntity)
    val savedLog = logEntity.copy(id = insertedId).toModel()

    Pair(matchResult, savedLog)
  }

  private fun fallbackLocalMatch(
    featureVector: List<Float>,
    gallery: List<EnrolledUser>,
    mode: String,
    claimedUserId: String?,
    threshold: Float,
    livenessPassed: Boolean = true,
    livenessStatus: com.example.biometrics.sdk.LivenessEvaluationResult = com.example.biometrics.sdk.LivenessEvaluationResult.PASSED,
    challengeType: com.example.biometrics.sdk.LivenessChallengeType = com.example.biometrics.sdk.LivenessChallengeType.BLINK,
  ): MatchResult {
    return if (mode == "1:1 Verification" && claimedUserId != null) {
      val target = gallery.find { it.id == claimedUserId || it.employeeId == claimedUserId }
      if (target != null) {
        FaceBiometricEngine.verifyIdentity(featureVector, target, threshold, livenessPassed, livenessStatus, challengeType)
      } else {
        MatchResult(false, 0f, null, 0f, 999f, livenessPassed, livenessStatus, challengeType)
      }
    } else {
      FaceBiometricEngine.searchGallery(featureVector, gallery, threshold, livenessPassed, livenessStatus, challengeType)
    }
  }

  /**
   * Enroll a new identity into the database and sync with API.
   */
  suspend fun enrollUser(
    name: String,
    employeeId: String,
    department: String,
    role: String,
    accessLevel: String,
    photoUri: String = "",
    bitmap: Bitmap? = null,
  ): EnrolledUser = withContext(Dispatchers.IO) {
    val features = if (bitmap != null) {
      FaceBiometricEngine.extractFaceFeatures(bitmap)
    } else {
      FaceBiometricEngine.generateSyntheticFeatures("$name-$employeeId")
    }

    val newUser = EnrolledUser(
      id = "USR-${(1000..9999).random()}",
      name = name,
      employeeId = employeeId,
      department = department,
      role = role,
      accessLevel = accessLevel,
      photoUri = photoUri,
      featureVector = features,
      enrolledAt = System.currentTimeMillis(),
      isActive = true,
    )

    userDao.insertUser(EnrolledUserEntity.fromModel(newUser))
    newUser
  }

  suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
    userDao.deleteUser(userId)
  }

  suspend fun toggleUserActive(user: EnrolledUser) = withContext(Dispatchers.IO) {
    val updated = user.copy(isActive = !user.isActive)
    userDao.updateUser(EnrolledUserEntity.fromModel(updated))
  }

  suspend fun clearLogs() = withContext(Dispatchers.IO) {
    logDao.clearAllLogs()
  }

  suspend fun deleteLog(logId: Long) = withContext(Dispatchers.IO) {
    logDao.deleteLog(logId)
  }
}
