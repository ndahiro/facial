package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EnrolledUserDao {
  @Query("SELECT * FROM enrolled_users ORDER BY enrolledAt DESC")
  fun getAllUsersFlow(): Flow<List<EnrolledUserEntity>>

  @Query("SELECT * FROM enrolled_users WHERE isActive = 1")
  suspend fun getActiveUsers(): List<EnrolledUserEntity>

  @Query("SELECT * FROM enrolled_users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): EnrolledUserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: EnrolledUserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<EnrolledUserEntity>)

  @Update
  suspend fun updateUser(user: EnrolledUserEntity)

  @Query("DELETE FROM enrolled_users WHERE id = :userId")
  suspend fun deleteUser(userId: String)

  @Query("SELECT COUNT(*) FROM enrolled_users")
  suspend fun getUserCount(): Int
}
