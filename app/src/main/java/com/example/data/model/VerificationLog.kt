package com.example.data.model

enum class VerificationStatus {
  VERIFIED,
  ACCESS_DENIED,
  UNKNOWN_FACE,
  SPOOF_DETECTED,
  API_ERROR
}

data class VerificationLog(
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String = "",
  val status: VerificationStatus,
  val personName: String? = null,
  val personId: String? = null,
  val department: String? = null,
  val accessLevel: String? = null,
  val confidenceScore: Float = 0.0f,
  val matchThreshold: Float = 0.75f,
  val verificationMode: String = "1:N Identification",
  val apiLatencyMs: Long = 0,
  val locationGate: String = "Checkpoint North-Gate",
  val auditSignature: String = "",
  val matchedPhotoDrawable: String = "",
  val notes: String = "",
  val livenessChallenge: String = "Blink Detection",
  val livenessResult: String = "LIVENESS_PASSED",
)
