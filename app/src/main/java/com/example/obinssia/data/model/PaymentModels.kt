package com.example.obinssia.data.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Niveaux d'abonnement OBIN’SS IA
 */
enum class SubscriptionPlan(
    val code: String,
    val title: String,
    val badge: String,
    val description: String
) {
    FREE(
        code = "FREE",
        title = "OBIN’SS IA FREE",
        badge = "GRATUIT",
        description = "Découverte essentielle de l'intelligence artificielle"
    ),
    PRO(
        code = "PRO",
        title = "OBIN’SS IA PRO",
        badge = "POPULAIRE",
        description = "Puissance maximale pour créateurs, étudiants & professionnels"
    ),
    BUSINESS(
        code = "BUSINESS",
        title = "OBIN’SS IA BUSINESS",
        badge = "ENTREPRISE",
        description = "Solution complète pour équipes, PME et organisations"
    )
}

/**
 * Périodes de facturation
 */
enum class BillingPeriod(val code: String, val label: String) {
    MONTHLY("MONTHLY", "Mensuel"),
    YEARLY("YEARLY", "Annuel")
}

/**
 * États normalisés d'un paiement ou d'un abonnement
 */
enum class PaymentStatus(val code: String, val label: String) {
    FREE("FREE", "Gratuit"),
    PAYMENT_PENDING("PAYMENT_PENDING", "En attente"),
    ACTIVE("ACTIVE", "Actif"),
    EXPIRED("EXPIRED", "Expiré"),
    CANCELLED("CANCELLED", "Annulé"),
    PAYMENT_FAILED("PAYMENT_FAILED", "Échec")
}

/**
 * Moyens de paiement compatibles Côte d'Ivoire & International
 */
enum class PaymentProvider(
    val code: String,
    val displayName: String,
    val subtitle: String,
    val category: String // "MOBILE_MONEY", "CARD", "WALLET"
) {
    WAVE("WAVE", "Wave", "Paiement 1% instantané CI", "MOBILE_MONEY"),
    ORANGE_MONEY("ORANGE_MONEY", "Orange Money", "Côte d'Ivoire & Afrique", "MOBILE_MONEY"),
    MTN_MOMO("MTN_MOMO", "MTN MoMo", "MoMo Côte d'Ivoire", "MOBILE_MONEY"),
    MOOV_MONEY("MOOV_MONEY", "Moov Money", "Moov Africa", "MOBILE_MONEY"),
    CARD("CARD", "Carte Bancaire", "Visa / Mastercard sécurisé", "CARD"),
    PAYSTACK("PAYSTACK", "Paystack", "Paiement panafricain", "WALLET"),
    STRIPE("STRIPE", "Stripe", "Cartes internationales", "CARD")
}

/**
 * Configuration centralisée des prix, modifiable dynamiquement depuis l'administration
 */
data class PricingConfig(
    val proMonthlyPrice: Int = 4900,
    val proYearlyPrice: Int = 47000,
    val businessMonthlyPrice: Int = 19900,
    val businessYearlyPrice: Int = 191000,
    val yearlyDiscountPercent: Int = 20,
    val currencySymbol: String = "FCFA"
) {
    fun formatFcfa(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.FRENCH)
        return "${formatter.format(amount)} $currencySymbol"
    }

    fun getPrice(plan: SubscriptionPlan, period: BillingPeriod): Int {
        return when (plan) {
            SubscriptionPlan.FREE -> 0
            SubscriptionPlan.PRO -> if (period == BillingPeriod.MONTHLY) proMonthlyPrice else proYearlyPrice
            SubscriptionPlan.BUSINESS -> if (period == BillingPeriod.MONTHLY) businessMonthlyPrice else businessYearlyPrice
        }
    }

    fun getPriceFormatted(plan: SubscriptionPlan, period: BillingPeriod): String {
        val amount = getPrice(plan, period)
        if (amount == 0) return "0 $currencySymbol"
        val periodSuffix = if (period == BillingPeriod.MONTHLY) "/ mois" else "/ an"
        return "${formatFcfa(amount)} $periodSuffix"
    }
}

/**
 * Configuration centralisée des limites et quotas d'utilisation
 */
data class QuotaConfig(
    // FREE
    val freeDailyMessages: Int = 20,
    val freeMonthlyMessages: Int = 300,
    val freeMaxFiles: Int = 3,
    val freeMaxFileSizeMb: Int = 5,
    val freeAllowedAiModes: List<String> = listOf("rapide", "equilibre"),

    // PRO
    val proDailyMessages: Int = 500,
    val proMonthlyMessages: Int = 10000,
    val proMaxFiles: Int = 50,
    val proMaxFileSizeMb: Int = 50,
    val proAllowedAiModes: List<String> = listOf("rapide", "equilibre", "creatif", "expert", "code", "analyse"),

    // BUSINESS
    val businessDailyMessages: Int = 2500,
    val businessMonthlyMessages: Int = 50000,
    val businessMaxFiles: Int = 200,
    val businessMaxFileSizeMb: Int = 150,
    val businessAllowedAiModes: List<String> = listOf("rapide", "equilibre", "creatif", "expert", "code", "analyse")
) {
    fun getDailyLimit(plan: String): Int {
        return when (plan.uppercase()) {
            "PRO" -> proDailyMessages
            "BUSINESS" -> businessDailyMessages
            else -> freeDailyMessages
        }
    }

    fun getMonthlyLimit(plan: String): Int {
        return when (plan.uppercase()) {
            "PRO" -> proMonthlyMessages
            "BUSINESS" -> businessMonthlyMessages
            else -> freeMonthlyMessages
        }
    }

    fun getMaxFileSizeMb(plan: String): Int {
        return when (plan.uppercase()) {
            "PRO" -> proMaxFileSizeMb
            "BUSINESS" -> businessMaxFileSizeMb
            else -> freeMaxFileSizeMb
        }
    }

    fun isModeAllowed(plan: String, modeId: String): Boolean {
        return when (plan.uppercase()) {
            "PRO" -> proAllowedAiModes.contains(modeId.lowercase())
            "BUSINESS" -> businessAllowedAiModes.contains(modeId.lowercase())
            else -> freeAllowedAiModes.contains(modeId.lowercase())
        }
    }
}

/**
 * Résultat typé du contrôle serveur des quotas
 */
sealed class QuotaCheckResult {
    object Allowed : QuotaCheckResult()
    data class DailyLimitReached(val limit: Int, val plan: String) : QuotaCheckResult()
    data class MonthlyLimitReached(val limit: Int, val plan: String) : QuotaCheckResult()
    data class ModeLockedForPlan(val modeName: String, val requiredPlan: String = "PRO") : QuotaCheckResult()
    data class FileLimitExceeded(val maxFiles: Int) : QuotaCheckResult()
    data class FileSizeExceeded(val maxMb: Int) : QuotaCheckResult()
    data class FeatureLocked(val featureName: String, val requiredPlan: String = "PRO") : QuotaCheckResult()
    object UserBlocked : QuotaCheckResult()
    object SubscriptionExpired : QuotaCheckResult()
}

/**
 * Statistiques consolidées des abonnements et revenus pour l'administration
 */
data class PaymentStats(
    val totalRevenueFcfa: Int = 0,
    val activeSubscriptions: Int = 0,
    val expiredSubscriptions: Int = 0,
    val successfulPayments: Int = 0,
    val pendingPayments: Int = 0,
    val failedPayments: Int = 0,
    val totalUsers: Int = 0,
    val freeUsers: Int = 0,
    val proUsers: Int = 0,
    val businessUsers: Int = 0
)
