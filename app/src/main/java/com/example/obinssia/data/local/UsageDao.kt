package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.obinssia.data.model.UserUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Query("SELECT * FROM user_usage WHERE userId = :userId LIMIT 1")
    fun getUsageForUser(userId: String): Flow<UserUsageEntity?>

    @Query("SELECT * FROM user_usage WHERE userId = :userId LIMIT 1")
    suspend fun getUsageForUserSync(userId: String): UserUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usage: UserUsageEntity)

    @Query("UPDATE user_usage SET dailyMessagesCount = dailyMessagesCount + 1, monthlyMessagesCount = monthlyMessagesCount + 1 WHERE userId = :userId")
    suspend fun incrementMessageCount(userId: String)

    @Query("UPDATE user_usage SET filesAnalyzedCount = filesAnalyzedCount + 1 WHERE userId = :userId")
    suspend fun incrementFilesAnalyzed(userId: String)

    @Query("UPDATE user_usage SET toolsUsedCount = toolsUsedCount + 1 WHERE userId = :userId")
    suspend fun incrementToolsUsed(userId: String)

    @Query("UPDATE user_usage SET dailyMessagesCount = 0, lastActiveDate = :today WHERE userId = :userId")
    suspend fun resetDailyCount(userId: String, today: String)

    @Query("SELECT SUM(monthlyMessagesCount) FROM user_usage")
    fun getTotalAiMessagesUsed(): Flow<Int?>
}
