package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometrics.sdk.FaceBiometricEngine
import com.example.biometrics.sdk.LivenessChallenge
import com.example.biometrics.sdk.LivenessChallengeType
import com.example.biometrics.sdk.LivenessEvaluationResult
import com.example.biometrics.sdk.MatchResult
import com.example.data.api.SecureVerificationRepository
import com.example.data.local.AppDatabase
import com.example.data.model.EnrolledUser
import com.example.data.model.VerificationLog
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LivenessPhase {
  IDLE,
  PROMPTING_ACTION,
  ACTION_DETECTED,
  SPOOF_ATTACK_DETECTED,
  ACTION_TIMED_OUT,
}

data class VerificationUiState(
  val isVerifying: Boolean = false,
  val lastResult: MatchResult? = null,
  val lastLog: VerificationLog? = null,
  val verificationMode: String = "1:N Identification",
  val matchThreshold: Float = 0.75f,
  val selectedClaimedUser: EnrolledUser? = null,
  val isCameraActive: Boolean = true,
  val isFlashEnabled: Boolean = false,
  val useFrontCamera: Boolean = true,
  val continuousScanning: Boolean = false,
  val selectedProbeName: String? = null,
  val pendingProbeSeed: String? = null,
  val apiUrl: String = "https://api.faceplugin.internal/v1",
  val apiToken: String = "sk_sec_78291048",
  val useRemoteApi: Boolean = false,
  val locationGate: String = "Gate Alpha - Main Vault",
  val deviceId: String = "SECURE-SCANNER-04",
  val logSearchQuery: String = "",
  val logFilterStatus: VerificationStatus? = null,
  val enrolledUsers: List<EnrolledUser> = emptyList(),
  val verificationLogs: List<VerificationLog> = emptyList(),
  val totalLogsCount: Int = 0,
  val verifiedCount: Int = 0,
  val deniedCount: Int = 0,
  val selectedLogForDetails: VerificationLog? = null,
  val showEnrollDialog: Boolean = false,
  val infoMessage: String? = null,
  val antiSpoofingEnabled: Boolean = true,
  val activeLivenessChallenge: LivenessChallenge? = null,
  val livenessPhase: LivenessPhase = LivenessPhase.IDLE,
  val livenessSecondsRemaining: Int = 6,
  val livenessProgress: Float = 0f,
  val selectedChallengePreference: LivenessChallengeType? = null, // null = random
  val soundAndHapticsEnabled: Boolean = true,
  val currentFps: Int = 30,
)

class VerificationViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application, viewModelScope)
  private val repository = SecureVerificationRepository(application, database)
  private var livenessCountdownJob: Job? = null

  private val _uiState = MutableStateFlow(VerificationUiState())
  val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      repository.enrolledUsersFlow.collectLatest { users ->
        _uiState.update { it.copy(enrolledUsers = users) }
      }
    }

    viewModelScope.launch {
      repository.allLogsFlow.collectLatest { logs ->
        _uiState.update { it.copy(verificationLogs = logs) }
      }
    }

    viewModelScope.launch {
      repository.totalLogsCountFlow.collectLatest { count ->
        _uiState.update { it.copy(totalLogsCount = count) }
      }
    }

    viewModelScope.launch {
      repository.verifiedCountFlow.collectLatest { count ->
        _uiState.update { it.copy(verifiedCount = count) }
      }
    }

    viewModelScope.launch {
      repository.deniedCountFlow.collectLatest { count ->
        _uiState.update { it.copy(deniedCount = count) }
      }
    }
  }

  /**
   * Initiate identity verification with active liveness challenge anti-spoofing
   */
  fun triggerVerification(bitmap: Bitmap? = null, probeFeatureSeed: String? = null) {
    if (_uiState.value.isVerifying) return

    val state = _uiState.value
    if (state.antiSpoofingEnabled) {
      // Pick active challenge
      val challenge = if (state.selectedChallengePreference != null) {
        LivenessChallenge(type = state.selectedChallengePreference)
      } else {
        FaceBiometricEngine.generateRandomChallenge()
      }

      _uiState.update {
        it.copy(
          activeLivenessChallenge = challenge,
          livenessPhase = LivenessPhase.PROMPTING_ACTION,
          livenessSecondsRemaining = 6,
          livenessProgress = 0f,
          pendingProbeSeed = probeFeatureSeed,
          lastResult = null,
          lastLog = null,
        )
      }

      startLivenessTimer()
    } else {
      // Direct verification without active liveness
      executeBiometricVerification(
        bitmap = bitmap,
        probeFeatureSeed = probeFeatureSeed,
        livenessPassed = true,
        livenessStatus = LivenessEvaluationResult.PASSED,
        challengeType = LivenessChallengeType.BLINK,
      )
    }
  }

  private fun startLivenessTimer() {
    livenessCountdownJob?.cancel()
    livenessCountdownJob = viewModelScope.launch {
      val totalSeconds = 6
      for (sec in totalSeconds downTo 1) {
        _uiState.update {
          it.copy(
            livenessSecondsRemaining = sec,
            livenessProgress = (totalSeconds - sec) / totalSeconds.toFloat(),
          )
        }
        delay(1000)
      }

      // If still prompting, mark as timeout
      if (_uiState.value.livenessPhase == LivenessPhase.PROMPTING_ACTION) {
        _uiState.update { it.copy(livenessPhase = LivenessPhase.ACTION_TIMED_OUT) }
        delay(600)
        val challenge = _uiState.value.activeLivenessChallenge ?: FaceBiometricEngine.generateRandomChallenge()
        executeBiometricVerification(
          probeFeatureSeed = _uiState.value.pendingProbeSeed,
          livenessPassed = false,
          livenessStatus = LivenessEvaluationResult.FAILED_TIMEOUT,
          challengeType = challenge.type,
        )
      }
    }
  }

  /**
   * User or sensor confirms the active action (e.g. eye blink or head turn) was performed
   */
  fun completeLivenessAction(action: LivenessChallengeType) {
    livenessCountdownJob?.cancel()
    val challenge = _uiState.value.activeLivenessChallenge ?: LivenessChallenge(action)

    val isCorrectAction = challenge.type == action
    if (isCorrectAction) {
      _uiState.update {
        it.copy(
          livenessPhase = LivenessPhase.ACTION_DETECTED,
          livenessProgress = 1.0f,
        )
      }

      viewModelScope.launch {
        delay(500) // Brief pause to show checkmark
        executeBiometricVerification(
          probeFeatureSeed = _uiState.value.pendingProbeSeed,
          livenessPassed = true,
          livenessStatus = LivenessEvaluationResult.PASSED,
          challengeType = challenge.type,
        )
      }
    } else {
      _uiState.update { it.copy(livenessPhase = LivenessPhase.SPOOF_ATTACK_DETECTED) }
      viewModelScope.launch {
        delay(600)
        executeBiometricVerification(
          probeFeatureSeed = _uiState.value.pendingProbeSeed,
          livenessPassed = false,
          livenessStatus = LivenessEvaluationResult.FAILED_WRONG_ACTION,
          challengeType = challenge.type,
        )
      }
    }
  }

  /**
   * Simulates a 2D static printed photo or video screen replay attack
   * to test and demonstrate anti-spoof rejection!
   */
  fun simulateStaticPhotoSpoof() {
    livenessCountdownJob?.cancel()
    val challenge = _uiState.value.activeLivenessChallenge ?: LivenessChallenge(LivenessChallengeType.BLINK)

    _uiState.update {
      it.copy(
        livenessPhase = LivenessPhase.SPOOF_ATTACK_DETECTED,
        livenessProgress = 0f,
      )
    }

    viewModelScope.launch {
      delay(700)
      executeBiometricVerification(
        probeFeatureSeed = _uiState.value.pendingProbeSeed,
        livenessPassed = false,
        livenessStatus = LivenessEvaluationResult.FAILED_SPOOF_STATIC,
        challengeType = challenge.type,
      )
    }
  }

  fun cancelLivenessChallenge() {
    livenessCountdownJob?.cancel()
    _uiState.update {
      it.copy(
        activeLivenessChallenge = null,
        livenessPhase = LivenessPhase.IDLE,
        pendingProbeSeed = null,
      )
    }
  }

  private fun executeBiometricVerification(
    bitmap: Bitmap? = null,
    probeFeatureSeed: String? = null,
    livenessPassed: Boolean,
    livenessStatus: LivenessEvaluationResult,
    challengeType: LivenessChallengeType,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isVerifying = true, infoMessage = null) }

      val state = _uiState.value
      val features = when {
        bitmap != null -> FaceBiometricEngine.extractFaceFeatures(bitmap)
        probeFeatureSeed != null -> {
          val matchedEnrolled = state.enrolledUsers.find { it.id == probeFeatureSeed || it.name == probeFeatureSeed }
          if (matchedEnrolled != null && matchedEnrolled.featureVector.isNotEmpty()) {
            matchedEnrolled.featureVector.map { it + ((1..10).random() - 5) * 0.0015f }
          } else {
            FaceBiometricEngine.generateSyntheticFeatures(probeFeatureSeed)
          }
        }
        else -> {
          FaceBiometricEngine.generateSyntheticFeatures("Probe-${System.currentTimeMillis() % 1000}")
        }
      }

      val (result, log) = repository.performVerification(
        featureVector = features,
        mode = state.verificationMode,
        claimedUserId = state.selectedClaimedUser?.id,
        threshold = state.matchThreshold,
        apiUrl = state.apiUrl,
        apiToken = state.apiToken,
        deviceId = state.deviceId,
        locationGate = state.locationGate,
        useDirectRemoteApi = state.useRemoteApi,
        livenessPassed = livenessPassed,
        livenessStatus = livenessStatus,
        challengeType = challengeType,
      )

      _uiState.update {
        it.copy(
          isVerifying = false,
          lastResult = result,
          lastLog = log,
          livenessPhase = LivenessPhase.IDLE,
          activeLivenessChallenge = null,
          pendingProbeSeed = null,
        )
      }
    }
  }

  fun testWithEnrolledProbe(user: EnrolledUser) {
    _uiState.update { it.copy(selectedProbeName = user.name) }
    triggerVerification(probeFeatureSeed = user.id)
  }

  fun testWithUnknownFace() {
    _uiState.update { it.copy(selectedProbeName = "Unknown Subject (Unregistered)") }
    triggerVerification(probeFeatureSeed = "UNKNOWN_INTRUDER_${System.currentTimeMillis()}")
  }

  fun setVerificationMode(mode: String) {
    _uiState.update { it.copy(verificationMode = mode) }
  }

  fun setMatchThreshold(threshold: Float) {
    _uiState.update { it.copy(matchThreshold = threshold) }
  }

  fun setSelectedClaimedUser(user: EnrolledUser?) {
    _uiState.update { it.copy(selectedClaimedUser = user) }
  }

  fun toggleCameraFacing() {
    _uiState.update { it.copy(useFrontCamera = !it.useFrontCamera) }
  }

  fun toggleFlash() {
    _uiState.update { it.copy(isFlashEnabled = !it.isFlashEnabled) }
  }

  fun toggleContinuousScanning() {
    _uiState.update { it.copy(continuousScanning = !it.continuousScanning) }
  }

  fun setLogSearchQuery(query: String) {
    _uiState.update { it.copy(logSearchQuery = query) }
  }

  fun setLogFilterStatus(status: VerificationStatus?) {
    _uiState.update { it.copy(logFilterStatus = status) }
  }

  fun selectLogForDetails(log: VerificationLog?) {
    _uiState.update { it.copy(selectedLogForDetails = log) }
  }

  fun dismissResultBadge() {
    _uiState.update { it.copy(lastResult = null, lastLog = null) }
  }

  fun setShowEnrollDialog(show: Boolean) {
    _uiState.update { it.copy(showEnrollDialog = show) }
  }

  fun enrollUser(
    name: String,
    employeeId: String,
    department: String,
    role: String,
    accessLevel: String,
    bitmap: Bitmap? = null,
  ) {
    viewModelScope.launch {
      val enrolled = repository.enrollUser(
        name = name,
        employeeId = employeeId,
        department = department,
        role = role,
        accessLevel = accessLevel,
        bitmap = bitmap,
      )
      _uiState.update {
        it.copy(
          showEnrollDialog = false,
          infoMessage = "Enrolled ${enrolled.name} with 128-d biometric template.",
        )
      }
    }
  }

  fun deleteUser(userId: String) {
    viewModelScope.launch {
      repository.deleteUser(userId)
      _uiState.update { it.copy(infoMessage = "Identity removed from database.") }
    }
  }

  fun toggleUserActive(user: EnrolledUser) {
    viewModelScope.launch {
      repository.toggleUserActive(user)
    }
  }

  fun clearAllLogs() {
    viewModelScope.launch {
      repository.clearLogs()
      _uiState.update { it.copy(infoMessage = "Verification event audit trail cleared.") }
    }
  }

  fun deleteLog(logId: Long) {
    viewModelScope.launch {
      repository.deleteLog(logId)
    }
  }

  fun updateApiSettings(
    apiUrl: String,
    apiToken: String,
    useRemote: Boolean,
    gate: String,
    deviceId: String,
  ) {
    _uiState.update {
      it.copy(
        apiUrl = apiUrl,
        apiToken = apiToken,
        useRemoteApi = useRemote,
        locationGate = gate,
        deviceId = deviceId,
        infoMessage = "API and terminal configuration updated.",
      )
    }
  }

  fun toggleAntiSpoofing() {
    _uiState.update { it.copy(antiSpoofingEnabled = !it.antiSpoofingEnabled) }
  }

  fun setLivenessChallengePreference(type: LivenessChallengeType?) {
    _uiState.update { it.copy(selectedChallengePreference = type) }
  }

  fun toggleSoundAndHaptics() {
    _uiState.update { it.copy(soundAndHapticsEnabled = !it.soundAndHapticsEnabled) }
  }

  fun clearInfoMessage() {
    _uiState.update { it.copy(infoMessage = null) }
  }
}
