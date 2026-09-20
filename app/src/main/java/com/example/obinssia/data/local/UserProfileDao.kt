package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.obinssia.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET plan = :newPlan WHERE id = 'current_user'")
    suspend fun updatePlan(newPlan: String)

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE id = 'current_user'")
    suspend fun updateTheme(isDark: Boolean)

    @Query("UPDATE user_profile SET language = :lang WHERE id = 'current_user'")
    suspend fun updateLanguage(lang: String)

    @Query("UPDATE user_profile SET selectedFirstGoal = :goal WHERE id = 'current_user'")
    suspend fun updateFirstGoal(goal: String)

    @Query("UPDATE user_profile SET messagesSentCount = messagesSentCount + 1, tokensUsedToday = tokensUsedToday + :tokens WHERE id = 'current_user'")
    suspend fun incrementMessageAndTokens(tokens: Int)

    @Query("UPDATE user_profile SET name = 'Utilisateur OBIN’SS', email = 'user@obinssia.ai', plan = 'FREE', language = 'fr', isDarkMode = 1, tokensUsedToday = 0, messagesSentCount = 0, documentsAnalyzedCount = 0, selectedFirstGoal = '' WHERE id = 'current_user'")
    suspend fun resetProfile()
}
