package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.obinssia.data.model.PaymentTransactionEntity
import com.example.obinssia.data.model.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    // ==========================================
    // TRANSACTIONS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: PaymentTransactionEntity)

    @Query("SELECT * FROM payment_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForUser(userId: String): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE reference = :reference LIMIT 1")
    suspend fun getTransactionByReference(reference: String): PaymentTransactionEntity?

    @Query("SELECT * FROM payment_transactions WHERE reference = :reference LIMIT 1")
    fun getTransactionByReferenceFlow(reference: String): Flow<PaymentTransactionEntity?>

    @Query("UPDATE payment_transactions SET status = :status, confirmedAt = :confirmedAt, failureReason = :failureReason WHERE reference = :reference")
    suspend fun updateTransactionStatus(reference: String, status: String, confirmedAt: Long?, failureReason: String? = null)

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE status = 'ACTIVE'")
    fun countSuccessfulPayments(): Flow<Int>

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE status = 'PAYMENT_FAILED'")
    fun countFailedPayments(): Flow<Int>

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE status = 'PAYMENT_PENDING'")
    fun countPendingPayments(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountFcfa), 0) FROM payment_transactions WHERE status = 'ACTIVE'")
    fun getTotalRevenueFcfa(): Flow<Int>

    // ==========================================
    // SUBSCRIPTIONS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubscription(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    fun getSubscriptionForUser(userId: String): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    suspend fun getSubscriptionForUserSync(userId: String): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions ORDER BY startDate DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND plan != 'FREE'")
    fun countActivePaidSubscriptions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE status = 'EXPIRED'")
    fun countExpiredSubscriptions(): Flow<Int>

    @Query("UPDATE subscriptions SET status = :status WHERE userId = :userId")
    suspend fun updateSubscriptionStatus(userId: String, status: String)

    @Query("UPDATE subscriptions SET plan = 'FREE', status = 'EXPIRED' WHERE expiryDate IS NOT NULL AND expiryDate < :currentTime AND status = 'ACTIVE'")
    suspend fun expireOldSubscriptions(currentTime: Long): Int
}
