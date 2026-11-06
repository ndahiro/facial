package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyFaceRequest(
  @Json(name = "feature_vector") val featureVector: List<Float> = emptyList(),
  @Json(name = "verification_mode") val verificationMode: String = "1:N",
  @Json(name = "claimed_user_id") val claimedUserId: String? = null,
  @Json(name = "threshold") val threshold: Float = 0.75f,
  @Json(name = "device_id") val deviceId: String = "ANDROID-SCANNER-01",
  @Json(name = "location_gate") val locationGate: String = "Terminal 1 Gate A",
  @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
)

@JsonClass(generateAdapter = true)
data class VerifyFaceResponse(
  @Json(name = "is_match") val isMatch: Boolean,
  @Json(name = "confidence") val confidence: Float,
  @Json(name = "matched_user_id") val matchedUserId: String? = null,
  @Json(name = "name") val name: String? = null,
  @Json(name = "employee_id") val employeeId: String? = null,
  @Json(name = "department") val department: String? = null,
  @Json(name = "access_level") val accessLevel: String? = null,
  @Json(name = "status") val status: String,
  @Json(name = "audit_signature") val auditSignature: String = "",
  @Json(name = "latency_ms") val latencyMs: Long = 0,
  @Json(name = "server_timestamp") val serverTimestamp: Long = System.currentTimeMillis(),
  @Json(name = "message") val message: String = "",
)

@JsonClass(generateAdapter = true)
data class EnrollFaceRequest(
  @Json(name = "name") val name: String,
  @Json(name = "employee_id") val employeeId: String,
  @Json(name = "department") val department: String,
  @Json(name = "role") val role: String,
  @Json(name = "access_level") val accessLevel: String,
  @Json(name = "feature_vector") val featureVector: List<Float>,
  @Json(name = "enrolled_at") val enrolledAt: Long = System.currentTimeMillis(),
)

@JsonClass(generateAdapter = true)
data class EnrollFaceResponse(
  @Json(name = "success") val success: Boolean,
  @Json(name = "enrolled_user_id") val enrolledUserId: String,
  @Json(name = "enrolled_at") val enrolledAt: Long,
  @Json(name = "message") val message: String,
)

@JsonClass(generateAdapter = true)
data class EnrolledUserDto(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "employee_id") val employeeId: String,
  @Json(name = "department") val department: String,
  @Json(name = "role") val role: String,
  @Json(name = "access_level") val accessLevel: String,
  @Json(name = "is_active") val isActive: Boolean,
  @Json(name = "enrolled_at") val enrolledAt: Long,
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
  @Json(name = "status") val status: String,
  @Json(name = "engine") val engine: String,
  @Json(name = "database_status") val databaseStatus: String,
  @Json(name = "enrolled_faces_count") val enrolledFacesCount: Int,
  @Json(name = "server_time") val serverTime: Long,
)
