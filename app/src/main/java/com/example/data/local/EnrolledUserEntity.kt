package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EnrolledUser

@Entity(tableName = "enrolled_users")
data class EnrolledUserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val employeeId: String,
  val department: String,
  val role: String,
  val accessLevel: String,
  val photoDrawableName: String = "",
  val photoUri: String = "",
  val featureVectorString: String = "", // Comma-separated float values
  val enrolledAt: Long = System.currentTimeMillis(),
  val isActive: Boolean = true,
) {
  fun toModel(): EnrolledUser {
    val vector = if (featureVectorString.isBlank()) {
      emptyList()
    } else {
      featureVectorString.split(",").mapNotNull { it.toFloatOrNull() }
    }
    return EnrolledUser(
      id = id,
      name = name,
      employeeId = employeeId,
      department = department,
      role = role,
      accessLevel = accessLevel,
      photoDrawableName = photoDrawableName,
      photoUri = photoUri,
      featureVector = vector,
      enrolledAt = enrolledAt,
      isActive = isActive,
    )
  }

  companion object {
    fun fromModel(model: EnrolledUser): EnrolledUserEntity {
      return EnrolledUserEntity(
        id = model.id,
        name = model.name,
        employeeId = model.employeeId,
        department = model.department,
        role = model.role,
        accessLevel = model.accessLevel,
        photoDrawableName = model.photoDrawableName,
        photoUri = model.photoUri,
        featureVectorString = model.featureVector.joinToString(","),
        enrolledAt = model.enrolledAt,
        isActive = model.isActive,
      )
    }
  }
}
