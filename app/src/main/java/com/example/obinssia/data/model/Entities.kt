package com.example.obinssia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val language: String = "fr",
    val isDarkMode: Boolean = true,
    val plan: String = "FREE", // "FREE" or "PRO"
    val role: String = "USER", // "USER" or "ADMIN"
    val selectedFirstGoal: String = "",
    val isBlocked: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val title: String,
    val mode: String = AiMode.RAPIDE.id,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // "user" or "assistant" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Int = 0, // 0: none, 1: liked, -1: disliked
    val modelUsed: String = "gemini-3.5-flash",
    val attachedFileName: String? = null,
    val attachedFileType: String? = null
)

@Entity(tableName = "documents")
data class SavedDocumentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val contentPreview: String,
    val analysisSummary: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_usage")
data class UserUsageEntity(
    @PrimaryKey
    val userId: String,
    val dailyMessagesCount: Int = 0,
    val monthlyMessagesCount: Int = 0,
    val filesAnalyzedCount: Int = 0,
    val toolsUsedCount: Int = 0,
    val lastActiveDate: String = "" // YYYY-MM-DD
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val plan: String = "FREE", // FREE, PRO, BUSINESS
    val billingCycle: String = "MONTHLY", // MONTHLY, YEARLY
    val status: String = "ACTIVE", // FREE, PAYMENT_PENDING, ACTIVE, EXPIRED, CANCELLED, PAYMENT_FAILED
    val startDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val paymentMethod: String = "Wave",
    val amountFcfa: Int = 0,
    val transactionReference: String = ""
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userEmail: String,
    val reference: String,
    val plan: String, // PRO, BUSINESS
    val billingPeriod: String, // MONTHLY, YEARLY
    val amountFcfa: Int,
    val paymentMethod: String, // Wave, Orange Money, MTN MoMo, Moov Money, Carte Bancaire
    val status: String, // PAYMENT_PENDING, ACTIVE, PAYMENT_FAILED, CANCELLED, EXPIRED
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val failureReason: String? = null,
    val isSandboxTest: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String, // userId or "ALL"
    val title: String,
    val message: String,
    val type: String = "INFO", // WELCOME, USAGE_LIMIT, FEATURE, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "current_user",
    val name: String = "Utilisateur OBIN’SS",
    val email: String = "user@obinssia.ai",
    val avatarUrl: String = "",
    val plan: String = "FREE", // FREE, PRO, BUSINESS
    val language: String = "fr", // "fr" or "en"
    val isDarkMode: Boolean = true,
    val speechSpeed: Float = 1.0f,
    val tokensUsedToday: Int = 420,
    val messagesSentCount: Int = 12,
    val documentsAnalyzedCount: Int = 3,
    val selectedFirstGoal: String = ""
)
