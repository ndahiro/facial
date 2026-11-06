package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VerificationLog
import com.example.data.model.VerificationStatus

@Entity(tableName = "verification_logs")
data class VerificationLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val formattedTime: String,
  val status: String,
  val personName: String?,
  val personId: String?,
  val department: String?,
  val accessLevel: String?,
  val confidenceScore: Float,
  val matchThreshold: Float,
  val verificationMode: String,
  val apiLatencyMs: Long,
  val locationGate: String,
  val auditSignature: String,
  val matchedPhotoDrawable: String,
  val notes: String,
  val livenessChallenge: String = "Blink Detection",
  val livenessResult: String = "LIVENESS_PASSED",
) {
  fun toModel(): VerificationLog {
    val parsedStatus = try {
      VerificationStatus.valueOf(status)
    } catch (_: Exception) {
      VerificationStatus.UNKNOWN_FACE
    }
    return VerificationLog(
      id = id,
      timestamp = timestamp,
      formattedTime = formattedTime,
      status = parsedStatus,
      personName = personName,
      personId = personId,
      department = department,
      accessLevel = accessLevel,
      confidenceScore = confidenceScore,
      matchThreshold = matchThreshold,
      verificationMode = verificationMode,
      apiLatencyMs = apiLatencyMs,
      locationGate = locationGate,
      auditSignature = auditSignature,
      matchedPhotoDrawable = matchedPhotoDrawable,
      notes = notes,
      livenessChallenge = livenessChallenge,
      livenessResult = livenessResult,
    )
  }

  companion object {
    fun fromModel(model: VerificationLog): VerificationLogEntity {
      return VerificationLogEntity(
        id = model.id,
        timestamp = model.timestamp,
        formattedTime = model.formattedTime,
        status = model.status.name,
        personName = model.personName,
        personId = model.personId,
        department = model.department,
        accessLevel = model.accessLevel,
        confidenceScore = model.confidenceScore,
        matchThreshold = model.matchThreshold,
        verificationMode = model.verificationMode,
        apiLatencyMs = model.apiLatencyMs,
        locationGate = model.locationGate,
        auditSignature = model.auditSignature,
        matchedPhotoDrawable = model.matchedPhotoDrawable,
        notes = model.notes,
        livenessChallenge = model.livenessChallenge,
        livenessResult = model.livenessResult,
      )
    }
  }
}
