package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.obinssia.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT COUNT(*) FROM users")
    fun countTotalUsers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE plan = 'PRO'")
    fun countProUsers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE plan = 'FREE'")
    fun countFreeUsers(): Flow<Int>

    @Query("UPDATE users SET plan = :newPlan WHERE id = :userId")
    suspend fun updateUserPlan(userId: String, newPlan: String)

    @Query("UPDATE users SET isDarkMode = :isDark WHERE id = :userId")
    suspend fun updateUserTheme(userId: String, isDark: Boolean)

    @Query("UPDATE users SET language = :lang WHERE id = :userId")
    suspend fun updateUserLanguage(userId: String, lang: String)

    @Query("UPDATE users SET selectedFirstGoal = :goal WHERE id = :userId")
    suspend fun updateUserGoal(userId: String, goal: String)

    @Query("UPDATE users SET isBlocked = :blocked WHERE id = :userId")
    suspend fun setUserBlocked(userId: String, blocked: Boolean)
}
