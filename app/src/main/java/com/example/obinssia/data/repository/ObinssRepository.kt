package com.example.obinssia.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.obinssia.data.local.ObinssDatabase
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.data.model.BillingPeriod
import com.example.obinssia.data.model.ConversationEntity
import com.example.obinssia.data.model.MessageEntity
import com.example.obinssia.data.model.NotificationEntity
import com.example.obinssia.data.model.PaymentProvider
import com.example.obinssia.data.model.PaymentStats
import com.example.obinssia.data.model.PaymentStatus
import com.example.obinssia.data.model.PaymentTransactionEntity
import com.example.obinssia.data.model.PricingConfig
import com.example.obinssia.data.model.QuotaCheckResult
import com.example.obinssia.data.model.QuotaConfig
import com.example.obinssia.data.model.SavedDocumentEntity
import com.example.obinssia.data.model.SubscriptionEntity
import com.example.obinssia.data.model.SubscriptionPlan
import com.example.obinssia.data.model.UserEntity
import com.example.obinssia.data.model.UserProfileEntity
import com.example.obinssia.data.model.UserUsageEntity
import com.example.obinssia.data.remote.GeminiApiClient
import com.example.obinssia.data.remote.payment.ObinssPaymentGatewayServiceImpl
import com.example.obinssia.data.remote.payment.PaymentGatewayService
import com.example.obinssia.data.remote.payment.PaymentInitiateRequest
import com.example.obinssia.data.remote.payment.PaymentInitiateResponse
import com.example.obinssia.data.remote.payment.PaymentVerificationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ObinssRepository(private val context: Context) {
    private val database = ObinssDatabase.getDatabase(context)
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val documentDao = database.documentDao()
    private val userProfileDao = database.userProfileDao()
    private val userDao = database.userDao()
    private val usageDao = database.usageDao()
    private val notificationDao = database.notificationDao()
    private val paymentDao = database.paymentDao()
    private val paymentGateway: PaymentGatewayService = ObinssPaymentGatewayServiceImpl()
    private val geminiClient = GeminiApiClient()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("obinss_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        const val FREE_DAILY_LIMIT = 20
        const val PRO_DAILY_LIMIT = 500
        const val BUSINESS_DAILY_LIMIT = 2500
        private const val KEY_USER_ID = "logged_in_user_id"
        val ADMIN_EMAIL = "yvesmartial021@gmail.com"
    }

    private val _currentUserId = MutableStateFlow(
        prefs.getString(KEY_USER_ID, "admin_user_yves") ?: "admin_user_yves"
    )
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Centralized dynamic Pricing state (configured from admin)
    private val _pricingConfig = MutableStateFlow(
        PricingConfig(
            proMonthlyPrice = prefs.getInt("cfg_pro_monthly", 4900),
            proYearlyPrice = prefs.getInt("cfg_pro_yearly", 47000),
            businessMonthlyPrice = prefs.getInt("cfg_biz_monthly", 19900),
            businessYearlyPrice = prefs.getInt("cfg_biz_yearly", 191000),
            yearlyDiscountPercent = prefs.getInt("cfg_yearly_discount", 20)
        )
    )
    val pricingConfig: StateFlow<PricingConfig> = _pricingConfig.asStateFlow()

    // Centralized dynamic Quotas state (configured from admin)
    private val _quotaConfig = MutableStateFlow(
        QuotaConfig(
            freeDailyMessages = prefs.getInt("cfg_free_daily_msg", 20),
            freeMonthlyMessages = prefs.getInt("cfg_free_monthly_msg", 300),
            freeMaxFiles = prefs.getInt("cfg_free_max_files", 3),
            freeMaxFileSizeMb = prefs.getInt("cfg_free_max_file_mb", 5)
        )
    )
    val quotaConfig: StateFlow<QuotaConfig> = _quotaConfig.asStateFlow()

    // Backward-compatible properties
    var freePrice: String = "0 FCFA / Gratuit"
    var proPrice: String
        get() = _pricingConfig.value.formatFcfa(_pricingConfig.value.proMonthlyPrice) + " / mois"
        set(value) {
            val digits = value.filter { it.isDigit() }
            val amount = digits.toIntOrNull() ?: 4900
            updatePricingConfig(_pricingConfig.value.copy(proMonthlyPrice = amount))
        }
    var businessPrice: String
        get() = _pricingConfig.value.formatFcfa(_pricingConfig.value.businessMonthlyPrice) + " / mois"
        set(value) {
            val digits = value.filter { it.isDigit() }
            val amount = digits.toIntOrNull() ?: 19900
            updatePricingConfig(_pricingConfig.value.copy(businessMonthlyPrice = amount))
        }

    var isVoiceFeatureEnabled = true
    var isDocAnalysisEnabled = true
    var isProToolsEnabled = true

    fun updatePricingConfig(newConfig: PricingConfig) {
        _pricingConfig.value = newConfig
        prefs.edit()
            .putInt("cfg_pro_monthly", newConfig.proMonthlyPrice)
            .putInt("cfg_pro_yearly", newConfig.proYearlyPrice)
            .putInt("cfg_biz_monthly", newConfig.businessMonthlyPrice)
            .putInt("cfg_biz_yearly", newConfig.businessYearlyPrice)
            .putInt("cfg_yearly_discount", newConfig.yearlyDiscountPercent)
            .apply()
    }

    fun updateQuotaConfig(newConfig: QuotaConfig) {
        _quotaConfig.value = newConfig
        prefs.edit()
            .putInt("cfg_free_daily_msg", newConfig.freeDailyMessages)
            .putInt("cfg_free_monthly_msg", newConfig.freeMonthlyMessages)
            .putInt("cfg_free_max_files", newConfig.freeMaxFiles)
            .putInt("cfg_free_max_file_mb", newConfig.freeMaxFileSizeMb)
            .apply()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private suspend fun seedInitialDataIfNeeded() {
        val today = getTodayDateString()

        // 1. Seed Admin User (Yves Martial)
        val admin = userDao.getUserByIdSync("admin_user_yves")
        if (admin == null) {
            userDao.insertUser(
                UserEntity(
                    id = "admin_user_yves",
                    name = "Yves Martial",
                    email = ADMIN_EMAIL,
                    passwordHash = "admin123",
                    avatarUrl = "",
                    createdAt = System.currentTimeMillis() - 86400000L * 15,
                    language = "fr",
                    isDarkMode = true,
                    plan = "PRO",
                    role = "ADMIN",
                    selectedFirstGoal = "Déployer OBIN’SS IA en Côte d'Ivoire et propulser l'écosystème",
                    isBlocked = false
                )
            )
            usageDao.insertOrUpdateUsage(
                UserUsageEntity(
                    userId = "admin_user_yves",
                    dailyMessagesCount = 4,
                    monthlyMessagesCount = 128,
                    filesAnalyzedCount = 5,
                    toolsUsedCount = 14,
                    lastActiveDate = today
                )
            )
        }

        // 2. Seed Sample Normal Free User (Marie Koné)
        val sampleUser = userDao.getUserByIdSync("user_marie_kone")
        if (sampleUser == null) {
            userDao.insertUser(
                UserEntity(
                    id = "user_marie_kone",
                    name = "Marie Koné",
                    email = "marie.kone@example.ci",
                    passwordHash = "user123",
                    avatarUrl = "",
                    createdAt = System.currentTimeMillis() - 86400000L * 3,
                    language = "fr",
                    isDarkMode = true,
                    plan = "FREE",
                    role = "USER",
                    selectedFirstGoal = "Préparer mes concours et synthétiser mes cours",
                    isBlocked = false
                )
            )
            usageDao.insertOrUpdateUsage(
                UserUsageEntity(
                    userId = "user_marie_kone",
                    dailyMessagesCount = 14,
                    monthlyMessagesCount = 42,
                    filesAnalyzedCount = 1,
                    toolsUsedCount = 6,
                    lastActiveDate = today
                )
            )
        }

        // 3. Seed UserProfileEntity for backward compatibility
        val existingProfile = userProfileDao.getUserProfileSync()
        if (existingProfile == null) {
            userProfileDao.insertProfile(
                UserProfileEntity(
                    id = "current_user",
                    name = "Yves Martial",
                    email = ADMIN_EMAIL,
                    avatarUrl = "",
                    plan = "PRO",
                    language = "fr",
                    isDarkMode = true,
                    tokensUsedToday = 1250,
                    messagesSentCount = 18,
                    documentsAnalyzedCount = 4
                )
            )
        }

        // 4. Seed welcome notifications
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_welcome",
                userId = "ALL",
                title = "Bienvenue sur 𝐎𝐁𝐈𝐍’𝐒𝐒 IA 🚀",
                message = "Explorez les modes Rapide, Expert, Étude et Code ainsi que l'analyse de documents.",
                type = "WELCOME",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_pro_info",
                userId = "ALL",
                title = "Passez au niveau supérieur ✨",
                message = "Découvrez OBIN’SS IA PRO pour une utilisation illimitée et des outils exclusifs.",
                type = "FEATURE",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )

        // 5. Seed conversation for admin if empty
        val existingConversations = conversationDao.getConversationsForUser("admin_user_yves").firstOrNull()
        if (existingConversations.isNullOrEmpty()) {
            val welcomeConvId = UUID.randomUUID().toString()
            val welcomeConv = ConversationEntity(
                id = welcomeConvId,
                userId = "admin_user_yves",
                title = "Bienvenue sur OBIN’SS IA",
                mode = AiMode.RAPIDE.id,
                createdAt = System.currentTimeMillis() - 3600000,
                updatedAt = System.currentTimeMillis() - 3600000,
                isPinned = true
            )
            conversationDao.insertConversation(welcomeConv)

            messageDao.insertMessage(
                MessageEntity(
                    conversationId = welcomeConvId,
                    role = "assistant",
                    content = "### Bienvenue sur 𝐎𝐁𝐈𝐍’𝐒𝐒 IA 🚀\n\n*Votre intelligence. Votre assistant. Votre avenir.*\n\nJe suis votre assistant IA polyvalent, conçu pour propulser vos études, créations de contenu, entreprises et projets de code.\n\nVous pouvez :\n- ⚡ Poser une question en langage naturel\n- 🔄 Basculer entre les modes (Rapide, Expert, Étude, Code, Créateur, Business)\n- 📎 Importer un document PDF ou texte pour analyse\n- 🎙️ Activer la voix pour dicter ou écouter les réponses.",
                    timestamp = System.currentTimeMillis() - 3600000,
                    modelUsed = "gemini-3.5-flash"
                )
            )

            documentDao.insertDocument(
                SavedDocumentEntity(
                    id = "doc_admin_sample",
                    userId = "admin_user_yves",
                    fileName = "Business_Plan_Synthese.pdf",
                    fileType = "application/pdf",
                    fileSize = "1.4 Mo",
                    contentPreview = "Synthèse exécutive du plan d'affaires 2026 : Stratégie de déploiement en Afrique de l'Ouest, prévisions financières et KPI...",
                    analysisSummary = "Document structuré en 5 parties : Résumé exécutif, Étude de marché, Plan marketing, Modèle de revenus et Équipe dirigeante."
                )
            )
        }

        // 6. Seed Subscriptions and Invoices for demo / audit
        val adminSub = paymentDao.getSubscriptionForUserSync("admin_user_yves")
        if (adminSub == null) {
            val now = System.currentTimeMillis()
            val thirtyDaysLater = now + 25L * 86400000L
            paymentDao.insertOrUpdateSubscription(
                SubscriptionEntity(
                    userId = "admin_user_yves",
                    plan = "PRO",
                    billingCycle = "MONTHLY",
                    status = "ACTIVE",
                    startDate = now - 5L * 86400000L,
                    expiryDate = thirtyDaysLater,
                    paymentMethod = "Wave",
                    amountFcfa = 4900,
                    transactionReference = "OBINSS-WAVE-8921"
                )
            )
            paymentDao.insertTransaction(
                PaymentTransactionEntity(
                    userId = "admin_user_yves",
                    userEmail = ADMIN_EMAIL,
                    reference = "OBINSS-WAVE-8921",
                    plan = "PRO",
                    billingPeriod = "MONTHLY",
                    amountFcfa = 4900,
                    paymentMethod = "Wave",
                    status = "ACTIVE",
                    createdAt = now - 5L * 86400000L,
                    confirmedAt = now - 5L * 86400000L,
                    isSandboxTest = false
                )
            )
        }
    }

    // ==========================================
    // AUTHENTICATION & USERS
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { uid ->
        userDao.getUserById(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUsage: Flow<UserUsageEntity?> = _currentUserId.flatMapLatest { uid ->
        usageDao.getUsageForUser(uid)
    }

    suspend fun register(name: String, email: String, password: String):Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) return Result.failure(Exception("Le nom ne peut pas être vide."))
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return Result.failure(Exception("Format d'e-mail invalide."))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins 4 caractères."))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("Un compte existe déjà avec cette adresse e-mail."))
        }

        val newId = "user_" + UUID.randomUUID().toString().take(8)
        val role = if (trimmedEmail == ADMIN_EMAIL) "ADMIN" else "USER"
        val newUser = UserEntity(
            id = newId,
            name = trimmedName,
            email = trimmedEmail,
            passwordHash = password,
            avatarUrl = "",
            createdAt = System.currentTimeMillis(),
            language = "fr",
            isDarkMode = true,
            plan = "FREE",
            role = role,
            selectedFirstGoal = "Découvrir OBIN’SS IA",
            isBlocked = false
        )

        userDao.insertUser(newUser)

        val today = getTodayDateString()
        usageDao.insertOrUpdateUsage(
            UserUsageEntity(
                userId = newId,
                dailyMessagesCount = 0,
                monthlyMessagesCount = 0,
                filesAnalyzedCount = 0,
                toolsUsedCount = 0,
                lastActiveDate = today
            )
        )

        // Seed welcome conversation for new user
        val convId = UUID.randomUUID().toString()
        conversationDao.insertConversation(
            ConversationEntity(
                id = convId,
                userId = newId,
                title = "Bienvenue ${trimmedName}",
                mode = AiMode.RAPIDE.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "assistant",
                content = "Bonjour **$trimmedName** ! 👋\nBienvenue dans votre espace personnel **OBIN’SS IA**.\nJe suis prêt à vous assister : posez-moi n'importe quelle question pour commencer !",
                timestamp = System.currentTimeMillis()
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = newId,
                title = "Bienvenue $trimmedName 🎉",
                message = "Votre compte a été créé avec succès. Vous bénéficiez de 20 messages gratuits par jour.",
                type = "WELCOME"
            )
        )

        // Log in immediately
        setLoggedInUser(newId)
        syncUserProfile(newUser)

        return Result.success(newUser)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("Aucun compte trouvé avec cet e-mail."))

        if (user.isBlocked) {
            return Result.failure(Exception("Ce compte a été suspendu. Veuillez contacter le support."))
        }

        if (user.passwordHash.isNotEmpty() && user.passwordHash != password) {
            return Result.failure(Exception("Mot de passe incorrect."))
        }

        setLoggedInUser(user.id)
        syncUserProfile(user)
        return Result.success(user)
    }

    suspend fun loginWithGoogle(email: String, name: String): UserEntity {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            setLoggedInUser(existing.id)
            syncUserProfile(existing)
            return existing
        }

        val newId = "user_g_" + UUID.randomUUID().toString().take(8)
        val role = if (trimmedEmail == ADMIN_EMAIL) "ADMIN" else "USER"
        val newUser = UserEntity(
            id = newId,
            name = name.ifBlank { "Utilisateur Google" },
            email = trimmedEmail,
            passwordHash = "",
            avatarUrl = "",
            createdAt = System.currentTimeMillis(),
            language = "fr",
            isDarkMode = true,
            plan = "FREE",
            role = role,
            selectedFirstGoal = "Découvrir OBIN’SS IA",
            isBlocked = false
        )
        userDao.insertUser(newUser)

        val today = getTodayDateString()
        usageDao.insertOrUpdateUsage(
            UserUsageEntity(
                userId = newId,
                dailyMessagesCount = 0,
                monthlyMessagesCount = 0,
                filesAnalyzedCount = 0,
                toolsUsedCount = 0,
                lastActiveDate = today
            )
        )

        // Seed welcome conversation
        val convId = UUID.randomUUID().toString()
        conversationDao.insertConversation(
            ConversationEntity(
                id = convId,
                userId = newId,
                title = "Bienvenue $name",
                mode = AiMode.RAPIDE.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "assistant",
                content = "Bonjour **$name** ! 👋\nVotre compte Google a été connecté avec succès à **OBIN’SS IA**.",
                timestamp = System.currentTimeMillis()
            )
        )

        setLoggedInUser(newId)
        syncUserProfile(newUser)
        return newUser
    }

    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
        // Switch to Marie Koné or empty
        _currentUserId.value = "user_marie_kone"
    }

    suspend fun resetPassword(email: String): Result<String> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("Aucun compte associé à cet e-mail n'a été trouvé."))

        // Simulate password reset link or temporary reset
        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Demande de réinitialisation",
                message = "Une demande de réinitialisation a été traitée pour $trimmedEmail.",
                type = "SYSTEM"
            )
        )
        return Result.success("Un lien de réinitialisation a été envoyé à $trimmedEmail.")
    }

    private fun setLoggedInUser(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    private suspend fun syncUserProfile(user: UserEntity) {
        userProfileDao.insertProfile(
            UserProfileEntity(
                id = "current_user",
                name = user.name,
                email = user.email,
                avatarUrl = user.avatarUrl,
                plan = user.plan,
                language = user.language,
                isDarkMode = user.isDarkMode,
                selectedFirstGoal = user.selectedFirstGoal
            )
        )
    }

    // ==========================================
    // USAGE & QUOTA ENGINE (SERVER SOURCE OF TRUTH)
    // ==========================================

    suspend fun checkAndResetDailyUsageIfNeeded(userId: String) {
        val today = getTodayDateString()
        val usage = usageDao.getUsageForUserSync(userId)
        if (usage == null) {
            usageDao.insertOrUpdateUsage(
                UserUsageEntity(
                    userId = userId,
                    dailyMessagesCount = 0,
                    monthlyMessagesCount = 0,
                    filesAnalyzedCount = 0,
                    toolsUsedCount = 0,
                    lastActiveDate = today
                )
            )
        } else if (usage.lastActiveDate != today) {
            usageDao.resetDailyCount(userId, today)
        }
    }

    /**
     * Vérifie la validité temporelle de l'abonnement et rétrograde en FREE si expiré
     */
    suspend fun checkSubscriptionStatus(userId: String): String {
        val now = System.currentTimeMillis()
        val sub = paymentDao.getSubscriptionForUserSync(userId)
        if (sub != null && sub.status == PaymentStatus.ACTIVE.code && sub.expiryDate != null && sub.expiryDate < now) {
            // L'abonnement a expiré -> Rétrogradation automatique vers FREE
            paymentDao.updateSubscriptionStatus(userId, PaymentStatus.EXPIRED.code)
            userDao.updateUserPlan(userId, SubscriptionPlan.FREE.code)
            if (userId == _currentUserId.value) {
                userProfileDao.updatePlan(SubscriptionPlan.FREE.code)
            }
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "Abonnement expiré",
                    message = "Votre abonnement ${sub.plan} est arrivé à échéance. Votre compte est repassé sur la formule Gratuite.",
                    type = "USAGE_LIMIT"
                )
            )
            return SubscriptionPlan.FREE.code
        }
        val user = userDao.getUserByIdSync(userId)
        return user?.plan ?: SubscriptionPlan.FREE.code
    }

    /**
     * Contrôle centralisé et strict avant toute action IA
     */
    suspend fun checkQuotaForAiAction(
        userId: String,
        mode: AiMode,
        attachedFileSizeMb: Int? = null,
        attachedFilesCount: Int = 0
    ): QuotaCheckResult {
        val user = userDao.getUserByIdSync(userId) ?: return QuotaCheckResult.UserBlocked
        if (user.isBlocked) return QuotaCheckResult.UserBlocked

        // 1. Vérification de la validité de l'abonnement
        val effectivePlan = checkSubscriptionStatus(userId)
        val quotas = _quotaConfig.value

        // 2. Contrôle de l'accès au Mode IA demandé
        if (!quotas.isModeAllowed(effectivePlan, mode.id)) {
            return QuotaCheckResult.ModeLockedForPlan(mode.title, "PRO")
        }

        // 3. Contrôle des fichiers joints
        if (attachedFilesCount > 0) {
            val maxFiles = when (effectivePlan) {
                "BUSINESS" -> quotas.businessMaxFiles
                "PRO" -> quotas.proMaxFiles
                else -> quotas.freeMaxFiles
            }
            if (attachedFilesCount > maxFiles) {
                return QuotaCheckResult.FileLimitExceeded(maxFiles)
            }
        }

        if (attachedFileSizeMb != null) {
            val maxMb = quotas.getMaxFileSizeMb(effectivePlan)
            if (attachedFileSizeMb > maxMb) {
                return QuotaCheckResult.FileSizeExceeded(maxMb)
            }
        }

        // 4. Contrôle des quotas de messages quotidiens et mensuels
        checkAndResetDailyUsageIfNeeded(userId)
        val usage = usageDao.getUsageForUserSync(userId)
        val dailyLimit = quotas.getDailyLimit(effectivePlan)
        val monthlyLimit = quotas.getMonthlyLimit(effectivePlan)

        if (usage != null) {
            if (usage.dailyMessagesCount >= dailyLimit) {
                return QuotaCheckResult.DailyLimitReached(dailyLimit, effectivePlan)
            }
            if (usage.monthlyMessagesCount >= monthlyLimit) {
                return QuotaCheckResult.MonthlyLimitReached(monthlyLimit, effectivePlan)
            }
        }

        return QuotaCheckResult.Allowed
    }

    suspend fun isUserQuotaExceeded(userId: String): Boolean {
        val effectivePlan = checkSubscriptionStatus(userId)
        if (effectivePlan == "PRO" || effectivePlan == "BUSINESS") return false

        checkAndResetDailyUsageIfNeeded(userId)
        val usage = usageDao.getUsageForUserSync(userId) ?: return false
        val dailyLimit = _quotaConfig.value.getDailyLimit(effectivePlan)
        return usage.dailyMessagesCount >= dailyLimit
    }

    suspend fun getRemainingFreeMessages(userId: String): Int {
        val effectivePlan = checkSubscriptionStatus(userId)
        val dailyLimit = _quotaConfig.value.getDailyLimit(effectivePlan)
        if (effectivePlan == "PRO" || effectivePlan == "BUSINESS") return dailyLimit

        checkAndResetDailyUsageIfNeeded(userId)
        val usage = usageDao.getUsageForUserSync(userId) ?: return dailyLimit
        val remaining = dailyLimit - usage.dailyMessagesCount
        return if (remaining > 0) remaining else 0
    }

    // ==========================================
    // CONVERSATIONS (USER-ISOLATED)
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllConversations(): Flow<List<ConversationEntity>> = _currentUserId.flatMapLatest { uid ->
        conversationDao.getConversationsForUser(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchConversations(query: String): Flow<List<ConversationEntity>> = _currentUserId.flatMapLatest { uid ->
        if (query.isBlank()) {
            conversationDao.getConversationsForUser(uid)
        } else {
            conversationDao.searchConversationsForUser(uid, query)
        }
    }

    suspend fun getConversationById(id: String): ConversationEntity? = conversationDao.getConversationById(id)

    suspend fun createConversation(title: String, mode: AiMode = AiMode.RAPIDE): String {
        val convId = UUID.randomUUID().toString()
        val uid = _currentUserId.value
        val conv = ConversationEntity(
            id = convId,
            userId = uid,
            title = title,
            mode = mode.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false
        )
        conversationDao.insertConversation(conv)
        return convId
    }

    suspend fun renameConversation(id: String, newTitle: String) {
        conversationDao.renameConversation(id, newTitle)
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        conversationDao.togglePin(id, isPinned)
    }

    suspend fun deleteConversation(id: String) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        val uid = _currentUserId.value
        val userConvs = conversationDao.getConversationsForUser(uid).firstOrNull() ?: emptyList()
        userConvs.forEach { conv ->
            messageDao.deleteMessagesForConversation(conv.id)
            conversationDao.deleteConversationById(conv.id)
        }
    }

    // ==========================================
    // MESSAGES & AI DISPATCH
    // ==========================================

    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(convId)

    suspend fun updateMessageReaction(messageId: String, isLiked: Int) {
        messageDao.updateReaction(messageId, isLiked)
    }

    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        mode: AiMode,
        attachedFileName: String? = null,
        attachedFileType: String? = null,
        attachedContext: String? = null
    ): String {
        val uid = _currentUserId.value

        // Check Quota with centralized server engine
        val quotaResult = checkQuotaForAiAction(
            userId = uid,
            mode = mode,
            attachedFileSizeMb = null,
            attachedFilesCount = if (attachedFileName != null) 1 else 0
        )

        if (quotaResult !is QuotaCheckResult.Allowed) {
            val limitMessage = when (quotaResult) {
                is QuotaCheckResult.DailyLimitReached ->
                    "⚠️ **Votre limite quotidienne de messages est atteinte (${quotaResult.limit} / ${quotaResult.limit}) pour la formule ${quotaResult.plan}.**\n\nRevenez demain pour de nouveaux crédits ou passez à **OBIN’SS IA PRO** pour échanger sans interruption avec vitesse prioritaire."
                is QuotaCheckResult.MonthlyLimitReached ->
                    "⚠️ **Votre quota mensuel de messages est atteint (${quotaResult.limit} / ${quotaResult.limit}).**\n\nPassez à une formule supérieure pour continuer."
                is QuotaCheckResult.ModeLockedForPlan ->
                    "🔒 **Le mode « ${quotaResult.modeName} » est réservé aux membres OBIN’SS IA ${quotaResult.requiredPlan}.**\n\nEn formule Gratuite, vous disposez des modes Rapide et Équilibré. Passez à PRO pour débloquer l'accès complet à tous les modes spécialisés."
                is QuotaCheckResult.FileLimitExceeded ->
                    "📎 **Nombre maximal de fichiers atteint (${quotaResult.maxFiles} fichiers max autorisés).**\n\nPassez à PRO pour analyser jusqu'à 50 documents."
                is QuotaCheckResult.FileSizeExceeded ->
                    "📎 **Fichier trop volumineux (${quotaResult.maxMb} Mo max autorisés pour votre formule).**"
                is QuotaCheckResult.SubscriptionExpired ->
                    "⚠️ **Votre abonnement a expiré.**\n\nVotre compte est repassé sur la formule Gratuite. Renouvelez votre abonnement pour continuer."
                is QuotaCheckResult.UserBlocked ->
                    "🚫 **Ce compte a été suspendu par l'administrateur.**"
                else ->
                    "⚠️ **Action non autorisée sur ce compte.**"
            }

            // Insert system notification
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = uid,
                    title = "Accès restreint",
                    message = "Une action a été bloquée par vos quotas actuels. Consultez les offres PRO.",
                    type = "USAGE_LIMIT"
                )
            )

            // Still record the user message, but respond with quota alert without calling Gemini
            val targetConvId = if (conversationId.isBlank()) createConversation("Limite de formule", mode) else conversationId
            messageDao.insertMessage(
                MessageEntity(
                    conversationId = targetConvId,
                    role = "user",
                    content = userPrompt,
                    timestamp = System.currentTimeMillis(),
                    attachedFileName = attachedFileName,
                    attachedFileType = attachedFileType
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    conversationId = targetConvId,
                    role = "assistant",
                    content = limitMessage,
                    timestamp = System.currentTimeMillis(),
                    modelUsed = "system"
                )
            )
            return limitMessage
        }

        // Ensure a valid conversation exists
        val targetConvId = if (conversationId.isBlank()) {
            createConversation("Nouvelle conversation", mode)
        } else {
            val existing = conversationDao.getConversationById(conversationId)
            if (existing == null) {
                createConversation("Nouvelle conversation", mode)
            } else {
                conversationId
            }
        }

        // 1. Insert user message
        val userMessage = MessageEntity(
            conversationId = targetConvId,
            role = "user",
            content = userPrompt,
            timestamp = System.currentTimeMillis(),
            attachedFileName = attachedFileName,
            attachedFileType = attachedFileType
        )
        messageDao.insertMessage(userMessage)

        // 2. Fetch history
        val historyList = messageDao.getMessagesList(targetConvId).map { it.role to it.content }

        // 3. Update usage counters
        usageDao.incrementMessageCount(uid)
        userProfileDao.incrementMessageAndTokens(tokens = (userPrompt.length / 3) + 120)

        // 4. Generate AI response
        val aiResponse = geminiClient.generateResponse(
            prompt = userPrompt,
            mode = mode,
            conversationHistory = historyList,
            attachedDocumentContext = attachedContext ?: attachedFileName
        )

        // 5. Insert assistant message
        val assistantMessage = MessageEntity(
            conversationId = targetConvId,
            role = "assistant",
            content = aiResponse,
            timestamp = System.currentTimeMillis(),
            modelUsed = "gemini-3.5-flash"
        )
        messageDao.insertMessage(assistantMessage)

        // 6. Update conversation title if default
        val conv = conversationDao.getConversationById(targetConvId)
        if (conv != null && (conv.title == "Nouvelle conversation" || conv.title == "New conversation")) {
            val summaryTitle = if (userPrompt.length > 32) userPrompt.take(30) + "…" else userPrompt
            conversationDao.renameConversation(targetConvId, summaryTitle)
        }

        return aiResponse
    }

    // ==========================================
    // DOCUMENTS (USER-ISOLATED)
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllDocuments(): Flow<List<SavedDocumentEntity>> = _currentUserId.flatMapLatest { uid ->
        documentDao.getDocumentsForUser(uid)
    }

    suspend fun saveDocument(fileName: String, fileType: String, fileSize: String, preview: String, summary: String?) {
        val uid = _currentUserId.value
        documentDao.insertDocument(
            SavedDocumentEntity(
                userId = uid,
                fileName = fileName,
                fileType = fileType,
                fileSize = fileSize,
                contentPreview = preview,
                analysisSummary = summary
            )
        )
        usageDao.incrementFilesAnalyzed(uid)
    }

    suspend fun deleteDocument(doc: SavedDocumentEntity) {
        documentDao.deleteDocument(doc)
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getNotifications(): Flow<List<NotificationEntity>> = _currentUserId.flatMapLatest { uid ->
        notificationDao.getNotificationsForUser(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUnreadNotificationsCount(): Flow<Int> = _currentUserId.flatMapLatest { uid ->
        notificationDao.getUnreadCount(uid)
    }

    suspend fun markNotificationAsRead(notifId: String) {
        notificationDao.markAsRead(notifId)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead(_currentUserId.value)
    }

    // ==========================================
    // USER PROFILE & SETTINGS
    // ==========================================

    fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun updatePlan(newPlan: String) {
        val uid = _currentUserId.value
        userDao.updateUserPlan(uid, newPlan)
        userProfileDao.updatePlan(newPlan)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = uid,
                title = "Abonnement $newPlan activé ✨",
                message = if (newPlan == "PRO") "Félicitations ! Vous avez accès à toutes les fonctionnalités illimitées d'OBIN’SS IA PRO." else "Vous êtes désormais sur la formule $newPlan.",
                type = "FEATURE"
            )
        )
    }

    suspend fun updateTheme(isDark: Boolean) {
        val uid = _currentUserId.value
        userDao.updateUserTheme(uid, isDark)
        userProfileDao.updateTheme(isDark)
    }

    suspend fun updateLanguage(lang: String) {
        val uid = _currentUserId.value
        userDao.updateUserLanguage(uid, lang)
        userProfileDao.updateLanguage(lang)
    }

    suspend fun updateFirstGoal(goal: String) {
        val uid = _currentUserId.value
        userDao.updateUserGoal(uid, goal)
        userProfileDao.updateFirstGoal(goal)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        val uid = _currentUserId.value
        val user = userDao.getUserByIdSync(uid)
        if (user != null) {
            userDao.updateUser(user.copy(name = profile.name, email = profile.email))
        }
        userProfileDao.updateProfile(profile)
    }

    suspend fun resetProfile() {
        val uid = _currentUserId.value
        val user = userDao.getUserByIdSync(uid)
        if (user != null) {
            userDao.updateUser(
                user.copy(
                    name = "Utilisateur OBIN’SS",
                    email = "user@obinssia.ai",
                    plan = "FREE",
                    language = "fr",
                    isDarkMode = true,
                    selectedFirstGoal = ""
                )
            )
        }
        userProfileDao.resetProfile()
    }

    // ==========================================
    // ADMIN DASHBOARD QUERIES
    // ==========================================

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getTotalUsersCount(): Flow<Int> = userDao.countTotalUsers()
    fun getProUsersCount(): Flow<Int> = userDao.countProUsers()
    fun getFreeUsersCount(): Flow<Int> = userDao.countFreeUsers()
    fun getTotalConversationsCount(): Flow<Int> = conversationDao.countTotalConversations()
    fun getTotalAiMessagesCount(): Flow<Int?> = usageDao.getTotalAiMessagesUsed()

    suspend fun adminUpdateUserPlan(userId: String, newPlan: String) {
        userDao.updateUserPlan(userId, newPlan)
    }

    suspend fun adminToggleBlockUser(userId: String, isBlocked: Boolean) {
        userDao.setUserBlocked(userId, isBlocked)
    }

    // ==========================================
    // SUBSCRIPTIONS & PAYMENT GATEWAY (PHASE 3)
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubscriptionForCurrentUser(): Flow<SubscriptionEntity?> = _currentUserId.flatMapLatest { uid ->
        paymentDao.getSubscriptionForUser(uid)
    }

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = paymentDao.getAllSubscriptions()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransactionsForCurrentUser(): Flow<List<PaymentTransactionEntity>> = _currentUserId.flatMapLatest { uid ->
        paymentDao.getTransactionsForUser(uid)
    }

    fun getAllTransactions(): Flow<List<PaymentTransactionEntity>> = paymentDao.getAllTransactions()

    fun getTransactionByReferenceFlow(reference: String): Flow<PaymentTransactionEntity?> =
        paymentDao.getTransactionByReferenceFlow(reference)

    suspend fun initiateSubscriptionPayment(
        plan: SubscriptionPlan,
        period: BillingPeriod,
        provider: PaymentProvider,
        isSandbox: Boolean
    ): Result<PaymentInitiateResponse> {
        val uid = _currentUserId.value
        val user = userDao.getUserByIdSync(uid)
            ?: return Result.failure(Exception("Utilisateur non connecté"))

        val amountFcfa = _pricingConfig.value.getPrice(plan, period)

        val request = PaymentInitiateRequest(
            userId = uid,
            userEmail = user.email,
            userName = user.name,
            plan = plan,
            billingPeriod = period,
            amountFcfa = amountFcfa,
            provider = provider,
            isSandboxTest = isSandbox
        )

        return try {
            val response = paymentGateway.initiatePayment(request)

            // Enregistrement de la transaction initiale en base (statut PAYMENT_PENDING)
            val transaction = PaymentTransactionEntity(
                userId = uid,
                userEmail = user.email,
                reference = response.transactionReference,
                plan = plan.code,
                billingPeriod = period.code,
                amountFcfa = amountFcfa,
                paymentMethod = provider.displayName,
                status = PaymentStatus.PAYMENT_PENDING.code,
                createdAt = System.currentTimeMillis(),
                confirmedAt = null,
                failureReason = null,
                isSandboxTest = isSandbox
            )
            paymentDao.insertTransaction(transaction)

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPaymentTransaction(
        reference: String,
        simulatedOutcome: PaymentStatus? = null
    ): Result<PaymentVerificationResponse> {
        val transaction = paymentDao.getTransactionByReference(reference)
            ?: return Result.failure(Exception("Transaction introuvable avec la référence $reference"))

        return try {
            val response = paymentGateway.verifyTransaction(reference, simulatedOutcome)
            val now = System.currentTimeMillis()

            when (response.status) {
                PaymentStatus.ACTIVE -> {
                    // Paiement validé par le serveur / webhook !
                    paymentDao.updateTransactionStatus(
                        reference = reference,
                        status = PaymentStatus.ACTIVE.code,
                        confirmedAt = now,
                        failureReason = null
                    )

                    val durationDays = if (transaction.billingPeriod == BillingPeriod.YEARLY.code) 365L else 30L
                    val expiryDate = now + (durationDays * 86400000L)

                    val sub = SubscriptionEntity(
                        userId = transaction.userId,
                        plan = transaction.plan,
                        billingCycle = transaction.billingPeriod,
                        status = PaymentStatus.ACTIVE.code,
                        startDate = now,
                        expiryDate = expiryDate,
                        paymentMethod = transaction.paymentMethod,
                        amountFcfa = transaction.amountFcfa,
                        transactionReference = reference
                    )
                    paymentDao.insertOrUpdateSubscription(sub)

                    // Mise à jour du plan de l'utilisateur
                    userDao.updateUserPlan(transaction.userId, transaction.plan)
                    if (transaction.userId == _currentUserId.value) {
                        userProfileDao.updatePlan(transaction.plan)
                    }

                    val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expiryDate))
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = transaction.userId,
                            title = "Abonnement ${transaction.plan} activé 🎉",
                            message = "Votre paiement de ${transaction.amountFcfa} FCFA via ${transaction.paymentMethod} a été validé. Formule active jusqu'au $dateFormatted.",
                            type = "FEATURE"
                        )
                    )
                }
                PaymentStatus.PAYMENT_FAILED -> {
                    paymentDao.updateTransactionStatus(
                        reference = reference,
                        status = PaymentStatus.PAYMENT_FAILED.code,
                        confirmedAt = null,
                        failureReason = response.failureReason ?: "Paiement refusé par l'opérateur"
                    )
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = transaction.userId,
                            title = "Échec du paiement ⚠️",
                            message = response.failureReason ?: "La transaction a échoué. Aucun montant n'a été débité.",
                            type = "USAGE_LIMIT"
                        )
                    )
                }
                PaymentStatus.EXPIRED -> {
                    paymentDao.updateTransactionStatus(
                        reference = reference,
                        status = PaymentStatus.EXPIRED.code,
                        confirmedAt = null,
                        failureReason = "Délai de paiement expiré"
                    )
                }
                PaymentStatus.PAYMENT_PENDING -> {
                    // Toujours en attente
                }
                else -> {
                    // Statut alternatif (ex: CANCELLED, FREE)
                }
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelSubscription(userId: String) {
        paymentDao.updateSubscriptionStatus(userId, PaymentStatus.EXPIRED.code)
        userDao.updateUserPlan(userId, SubscriptionPlan.FREE.code)
        if (userId == _currentUserId.value) {
            userProfileDao.updatePlan(SubscriptionPlan.FREE.code)
        }
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Abonnement résilié",
                message = "Votre abonnement a été interrompu. Vous êtes désormais sur la formule Gratuite.",
                type = "USAGE_LIMIT"
            )
        )
    }

    suspend fun adminSetUserSubscription(
        userId: String,
        plan: String,
        billingPeriod: String,
        status: String
    ) {
        val now = System.currentTimeMillis()
        val durationDays = if (billingPeriod == "YEARLY") 365L else 30L
        val expiry = if (status == "ACTIVE") now + (durationDays * 86400000L) else null

        val sub = SubscriptionEntity(
            userId = userId,
            plan = plan,
            billingCycle = billingPeriod,
            status = status,
            startDate = now,
            expiryDate = expiry,
            paymentMethod = "Attribution Admin",
            amountFcfa = if (plan == "PRO") 4900 else if (plan == "BUSINESS") 19900 else 0,
            transactionReference = "ADMIN-MANUAL-${System.currentTimeMillis().toString().takeLast(6)}"
        )
        paymentDao.insertOrUpdateSubscription(sub)
        userDao.updateUserPlan(userId, plan)
        if (userId == _currentUserId.value) {
            userProfileDao.updatePlan(plan)
        }
    }

    /**
     * Statistiques consolidées des paiements pour le Dashboard Admin
     */
    fun getPaymentStats(): Flow<PaymentStats> {
        return combine(
            paymentDao.getAllTransactions(),
            paymentDao.getAllSubscriptions(),
            userDao.getAllUsers()
        ) { transactions, subscriptions, users ->
            val totalRev = transactions
                .filter { it.status == PaymentStatus.ACTIVE.code }
                .sumOf { it.amountFcfa }
            val activeSubs = subscriptions.count { it.status == PaymentStatus.ACTIVE.code }
            val expiredSubs = subscriptions.count { it.status == PaymentStatus.EXPIRED.code }
            val successful = transactions.count { it.status == PaymentStatus.ACTIVE.code }
            val pending = transactions.count { it.status == PaymentStatus.PAYMENT_PENDING.code }
            val failed = transactions.count { it.status == PaymentStatus.PAYMENT_FAILED.code }

            val totalU = users.size
            val freeU = users.count { it.plan.uppercase() == "FREE" }
            val proU = users.count { it.plan.uppercase() == "PRO" }
            val bizU = users.count { it.plan.uppercase() == "BUSINESS" }

            PaymentStats(
                totalRevenueFcfa = totalRev,
                activeSubscriptions = activeSubs,
                expiredSubscriptions = expiredSubs,
                successfulPayments = successful,
                pendingPayments = pending,
                failedPayments = failed,
                totalUsers = totalU,
                freeUsers = freeU,
                proUsers = proU,
                businessUsers = bizU
            )
        }
    }
}
