package com.example.data.model

data class EnrolledUser(
  val id: String,
  val name: String,
  val employeeId: String,
  val department: String,
  val role: String,
  val accessLevel: String, // e.g. "LEVEL_4_ALPHA", "LEVEL_3_SECURE", "LEVEL_2_STAFF"
  val photoDrawableName: String = "",
  val photoUri: String = "",
  val featureVector: List<Float> = emptyList(), // 128-dimensional embedding vector
  val enrolledAt: Long = System.currentTimeMillis(),
  val isActive: Boolean = true,
)
