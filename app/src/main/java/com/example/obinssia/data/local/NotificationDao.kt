package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.obinssia.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR userId = 'ALL') AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}
