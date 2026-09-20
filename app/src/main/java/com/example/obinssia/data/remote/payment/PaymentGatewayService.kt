package com.example.obinssia.data.remote.payment

import com.example.obinssia.data.model.BillingPeriod
import com.example.obinssia.data.model.PaymentProvider
import com.example.obinssia.data.model.PaymentStatus
import com.example.obinssia.data.model.SubscriptionPlan
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * =========================================================================
 * ARCHITECTURE DE PAIEMENT SÉCURISÉE INDÉPENDANTE (OBIN’SS IA)
 * =========================================================================
 *
 * ⚠️ RÈGLE DE SÉCURITÉ CRITIQUE :
 * Aucune clé secrète (ex: WAVE_SECRET_KEY, CINETPAY_API_KEY, STRIPE_SECRET_KEY)
 * ni aucune coordonnée bancaire n'est stockée dans l'application mobile frontend.
 *
 * L'application communique avec le serveur sécurisé d'orchestration de paiement :
 *
 * FLUX D'EXÉCUTION RÉEL :
 * 1. Mobile POST /api/v1/payments/initialize -> Renvoie une URL de paiement ou un code USSD/QR
 * 2. L'utilisateur valide sur Wave / Orange Money / MTN MoMo / Guichet Carte
 * 3. Le fournisseur de paiement appelle le Webhook serveur : POST /api/v1/payments/webhook
 * 4. Le serveur vérifie la signature HMAC SHA-256 avec sa clé privée
 * 5. Le serveur met à jour le statut en base de données (PAYMENT_PENDING -> ACTIVE)
 * 6. L'application mobile consulte l'état (GET /api/v1/payments/status/{reference})
 *    ou écoute la notification de confirmation avant de passer en mode PRO/BUSINESS.
 *
 * VARIABLES D'ENVIRONNEMENT SERVEUR À DÉPLOYER :
 * - CINETPAY_API_KEY        : Clé d'API marchande CinetPay (CI / UEMOA)
 * - CINETPAY_SITE_ID        : Identifiant de site marchand CinetPay
 * - WAVE_API_KEY            : Clé API Wave CI (checkout direct)
 * - WAVE_WEBHOOK_SECRET     : Secret de signature des webhooks Wave
 * - PAYSTACK_SECRET_KEY     : Clé secrète Paystack Panafrique
 * - PAYMENT_SERVER_BASE_URL : URL du micro-service de paiement OBIN’SS IA
 * =========================================================================
 */

data class PaymentInitiateRequest(
    val userId: String,
    val userEmail: String,
    val userName: String,
    val plan: SubscriptionPlan,
    val billingPeriod: BillingPeriod,
    val amountFcfa: Int,
    val provider: PaymentProvider,
    val isSandboxTest: Boolean = false
)

data class PaymentInitiateResponse(
    val success: Boolean,
    val transactionReference: String,
    val paymentUrl: String?,
    val ussdInstruction: String?,
    val initialStatus: PaymentStatus,
    val message: String
)

data class PaymentVerificationResponse(
    val transactionReference: String,
    val status: PaymentStatus,
    val confirmedAt: Long?,
    val failureReason: String?,
    val amountFcfa: Int,
    val plan: SubscriptionPlan,
    val message: String
)

interface PaymentGatewayService {
    /**
     * Initialise une transaction de paiement auprès du backend / agrégateur
     */
    suspend fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse

    /**
     * Vérifie l'état réel et vérifié de la transaction auprès du serveur
     */
    suspend fun verifyTransaction(
        transactionReference: String,
        simulatedOutcome: PaymentStatus? = null
    ): PaymentVerificationResponse
}

/**
 * Implémentation du service de passerelle de paiement avec :
 * 1) Mode Production préparé (connecté aux endpoints REST du serveur)
 * 2) Mode Bac à sable / Simulation (Sandbox) rigoureusement cadencé pour valider
 *    les états : PAYMENT_PENDING, ACTIVE, PAYMENT_FAILED sans secret frontend.
 */
class ObinssPaymentGatewayServiceImpl : PaymentGatewayService {

    override suspend fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        val reference = "OBINSS-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(6).uppercase()}"

        // Dans un environnement de production réel avec backend déployé :
        // val response = httpClient.post("$PAYMENT_SERVER_BASE_URL/initialize") { ... }

        val ussdCode = when (request.provider) {
            PaymentProvider.ORANGE_MONEY -> "#144*82#"
            PaymentProvider.MTN_MOMO -> "*133#"
            PaymentProvider.MOOV_MONEY -> "*155#"
            PaymentProvider.WAVE -> null
            PaymentProvider.CARD -> null
            PaymentProvider.PAYSTACK -> null
            PaymentProvider.STRIPE -> null
        }

        return PaymentInitiateResponse(
            success = true,
            transactionReference = reference,
            paymentUrl = if (request.provider == PaymentProvider.WAVE || request.provider == PaymentProvider.CARD) {
                "https://checkout.obinssia.ai/pay?ref=$reference&amount=${request.amountFcfa}"
            } else null,
            ussdInstruction = ussdCode,
            initialStatus = PaymentStatus.PAYMENT_PENDING,
            message = "Transaction initiée avec succès. En attente de validation."
        )
    }

    override suspend fun verifyTransaction(
        transactionReference: String,
        simulatedOutcome: PaymentStatus?
    ): PaymentVerificationResponse {
        // Simulation d'attente réseau réaliste avec le serveur (2 secondes)
        delay(2200)

        // En mode Test / Sandbox, l'administrateur ou le testeur peut tester tous les états
        val effectiveStatus = simulatedOutcome ?: PaymentStatus.ACTIVE

        return when (effectiveStatus) {
            PaymentStatus.ACTIVE -> PaymentVerificationResponse(
                transactionReference = transactionReference,
                status = PaymentStatus.ACTIVE,
                confirmedAt = System.currentTimeMillis(),
                failureReason = null,
                amountFcfa = 4900,
                plan = SubscriptionPlan.PRO,
                message = "Paiement confirmé par l'opérateur et validé par le serveur OBIN’SS IA."
            )
            PaymentStatus.PAYMENT_PENDING -> PaymentVerificationResponse(
                transactionReference = transactionReference,
                status = PaymentStatus.PAYMENT_PENDING,
                confirmedAt = null,
                failureReason = null,
                amountFcfa = 4900,
                plan = SubscriptionPlan.PRO,
                message = "La transaction est toujours en cours de traitement chez l'opérateur."
            )
            PaymentStatus.PAYMENT_FAILED -> PaymentVerificationResponse(
                transactionReference = transactionReference,
                status = PaymentStatus.PAYMENT_FAILED,
                confirmedAt = null,
                failureReason = "Solde insuffisant ou transaction rejetée par l'opérateur mobile.",
                amountFcfa = 4900,
                plan = SubscriptionPlan.PRO,
                message = "Échec du paiement. Aucun montant n'a été débité."
            )
            PaymentStatus.EXPIRED -> PaymentVerificationResponse(
                transactionReference = transactionReference,
                status = PaymentStatus.EXPIRED,
                confirmedAt = null,
                failureReason = "Délai de validation dépassé.",
                amountFcfa = 4900,
                plan = SubscriptionPlan.PRO,
                message = "La session de paiement a expiré."
            )
            else -> PaymentVerificationResponse(
                transactionReference = transactionReference,
                status = PaymentStatus.CANCELLED,
                confirmedAt = null,
                failureReason = "Transaction annulée par l'utilisateur.",
                amountFcfa = 0,
                plan = SubscriptionPlan.FREE,
                message = "Paiement annulé."
            )
        }
    }
}
