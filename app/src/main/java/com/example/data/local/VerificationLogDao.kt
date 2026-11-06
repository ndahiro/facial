package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationLogDao {
  @Query("SELECT * FROM verification_logs ORDER BY timestamp DESC")
  fun getAllLogsFlow(): Flow<List<VerificationLogEntity>>

  @Query("SELECT * FROM verification_logs ORDER BY timestamp DESC LIMIT :limit")
  suspend fun getRecentLogs(limit: Int = 50): List<VerificationLogEntity>

  @Query("SELECT * FROM verification_logs WHERE status = :status ORDER BY timestamp DESC")
  fun getLogsByStatusFlow(status: String): Flow<List<VerificationLogEntity>>

  @Query("SELECT COUNT(*) FROM verification_logs")
  fun getTotalLogsCountFlow(): Flow<Int>

  @Query("SELECT COUNT(*) FROM verification_logs WHERE status = 'VERIFIED'")
  fun getVerifiedCountFlow(): Flow<Int>

  @Query("SELECT COUNT(*) FROM verification_logs WHERE status != 'VERIFIED'")
  fun getDeniedCountFlow(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: VerificationLogEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLogs(logs: List<VerificationLogEntity>)

  @Query("DELETE FROM verification_logs WHERE id = :logId")
  suspend fun deleteLog(logId: Long)

  @Query("DELETE FROM verification_logs")
  suspend fun clearAllLogs()
}
