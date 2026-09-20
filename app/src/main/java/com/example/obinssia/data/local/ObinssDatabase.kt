package com.example.obinssia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.obinssia.data.model.ConversationEntity
import com.example.obinssia.data.model.MessageEntity
import com.example.obinssia.data.model.NotificationEntity
import com.example.obinssia.data.model.PaymentTransactionEntity
import com.example.obinssia.data.model.SavedDocumentEntity
import com.example.obinssia.data.model.SubscriptionEntity
import com.example.obinssia.data.model.UserEntity
import com.example.obinssia.data.model.UserProfileEntity
import com.example.obinssia.data.model.UserUsageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        SavedDocumentEntity::class,
        UserProfileEntity::class,
        UserEntity::class,
        UserUsageEntity::class,
        SubscriptionEntity::class,
        PaymentTransactionEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ObinssDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun documentDao(): DocumentDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userDao(): UserDao
    abstract fun usageDao(): UsageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: ObinssDatabase? = null

        fun getDatabase(context: Context): ObinssDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ObinssDatabase::class.java,
                    "obinss_ia_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
