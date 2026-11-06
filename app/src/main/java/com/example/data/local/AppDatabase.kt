package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
  entities = [EnrolledUserEntity::class, VerificationLogEntity::class],
  version = 2,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun enrolledUserDao(): EnrolledUserDao
  abstract fun verificationLogDao(): VerificationLogDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "face_verify_database",
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope,
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    private suspend fun populateInitialData(database: AppDatabase) {
      val userDao = database.enrolledUserDao()
      val logDao = database.verificationLogDao()

      // Seed 128-dimensional biometric vectors with distinct signature bases
      fun generateVector(seed: Float): String {
        return (0 until 128).map { i ->
          val angle = (i * 0.17f) + seed
          val raw = kotlin.math.sin(angle) * kotlin.math.cos(angle * 0.7f)
          String.format(Locale.US, "%.4f", raw)
        }.joinToString(",")
      }

      val initialUsers = listOf(
        EnrolledUserEntity(
          id = "USR-9021",
          name = "Dr. Elena Rostova",
          employeeId = "EMP-CYBER-88",
          department = "Biometric AI Research",
          role = "Principal Scientist",
          accessLevel = "LEVEL_4_ALPHA",
          photoDrawableName = "avatar_elena",
          featureVectorString = generateVector(1.23f),
          enrolledAt = System.currentTimeMillis() - 86400000L * 14,
          isActive = true,
        ),
        EnrolledUserEntity(
          id = "USR-7714",
          name = "Marcus Vance",
          employeeId = "EMP-SEC-01",
          department = "Corporate Security",
          role = "Chief Security Officer",
          accessLevel = "LEVEL_4_ALPHA",
          photoDrawableName = "avatar_marcus",
          featureVectorString = generateVector(2.84f),
          enrolledAt = System.currentTimeMillis() - 86400000L * 30,
          isActive = true,
        ),
        EnrolledUserEntity(
          id = "USR-6450",
          name = "Amina Chen",
          employeeId = "EMP-ENG-42",
          department = "Infrastructure Engineering",
          role = "Senior Systems Architect",
          accessLevel = "LEVEL_3_SECURE",
          photoDrawableName = "avatar_amina",
          featureVectorString = generateVector(4.56f),
          enrolledAt = System.currentTimeMillis() - 86400000L * 8,
          isActive = true,
        ),
        EnrolledUserEntity(
          id = "USR-3389",
          name = "Devon Brooks",
          employeeId = "EMP-OPS-19",
          department = "Facilities & Operations",
          role = "Operations Specialist",
          accessLevel = "LEVEL_2_STAFF",
          photoDrawableName = "avatar_devon",
          featureVectorString = generateVector(6.78f),
          enrolledAt = System.currentTimeMillis() - 86400000L * 3,
          isActive = true,
        ),
      )
      userDao.insertUsers(initialUsers)

      val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)
      val now = System.currentTimeMillis()

      val sampleLogs = listOf(
        VerificationLogEntity(
          timestamp = now - 1000L * 60 * 12,
          formattedTime = dateFormat.format(Date(now - 1000L * 60 * 12)),
          status = "VERIFIED",
          personName = "Dr. Elena Rostova",
          personId = "EMP-CYBER-88",
          department = "Biometric AI Research",
          accessLevel = "LEVEL_4_ALPHA",
          confidenceScore = 0.964f,
          matchThreshold = 0.75f,
          verificationMode = "1:N Identification",
          apiLatencyMs = 142,
          locationGate = "Main R&D Vault",
          auditSignature = "SHA256:7f4a...9b12",
          matchedPhotoDrawable = "avatar_elena",
          notes = "Biometric identity verified against central directory via API.",
        ),
        VerificationLogEntity(
          timestamp = now - 1000L * 60 * 45,
          formattedTime = dateFormat.format(Date(now - 1000L * 60 * 45)),
          status = "VERIFIED",
          personName = "Marcus Vance",
          personId = "EMP-SEC-01",
          department = "Corporate Security",
          accessLevel = "LEVEL_4_ALPHA",
          confidenceScore = 0.948f,
          matchThreshold = 0.75f,
          verificationMode = "1:N Identification",
          apiLatencyMs = 118,
          locationGate = "Executive Perimeter North",
          auditSignature = "SHA256:3e81...55c9",
          matchedPhotoDrawable = "avatar_marcus",
          notes = "Identity verified. Security bypass cleared.",
        ),
        VerificationLogEntity(
          timestamp = now - 1000L * 60 * 120,
          formattedTime = dateFormat.format(Date(now - 1000L * 60 * 120)),
          status = "UNKNOWN_FACE",
          personName = null,
          personId = null,
          department = null,
          accessLevel = null,
          confidenceScore = 0.412f,
          matchThreshold = 0.75f,
          verificationMode = "1:N Identification",
          apiLatencyMs = 186,
          locationGate = "Main Lobby Turnstile 02",
          auditSignature = "SHA256:0d2f...7e41",
          matchedPhotoDrawable = "",
          notes = "No matching biometric template in database (Confidence: 41.2% < 75.0%).",
        ),
        VerificationLogEntity(
          timestamp = now - 1000L * 60 * 240,
          formattedTime = dateFormat.format(Date(now - 1000L * 60 * 240)),
          status = "VERIFIED",
          personName = "Amina Chen",
          personId = "EMP-ENG-42",
          department = "Infrastructure Engineering",
          accessLevel = "LEVEL_3_SECURE",
          confidenceScore = 0.927f,
          matchThreshold = 0.75f,
          verificationMode = "1:N Identification",
          apiLatencyMs = 129,
          locationGate = "Server Room Tier-3",
          auditSignature = "SHA256:9a4b...22dd",
          matchedPhotoDrawable = "avatar_amina",
          notes = "Multi-factor facial biometric match verified.",
        ),
      )
      logDao.insertLogs(sampleLogs)
    }
  }
}
