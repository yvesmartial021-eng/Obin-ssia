package com.example.obinssia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.PaymentStats
import com.example.obinssia.data.model.PaymentStatus
import com.example.obinssia.data.model.PaymentTransactionEntity
import com.example.obinssia.data.model.PricingConfig
import com.example.obinssia.data.model.QuotaConfig
import com.example.obinssia.data.model.UserEntity
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssSurface
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    repository: ObinssRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState(initial = null)

    // Security Gate: Strict admin check
    val isAdmin = currentUser?.role == "ADMIN"

    if (!isAdmin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObinssDeepBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_access_denied_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B151A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Accès Restreint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cette section est strictement réservée aux administrateurs de la plateforme OBIN’SS IA.",
                        fontSize = 13.sp,
                        color = ObinssTextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Retour à l'espace membre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Tab state: 0 = Users & Overview, 1 = Finances & Subscriptions, 2 = Pricing & Quotas
    var selectedTab by remember { mutableStateOf(0) }

    // Dynamic metrics from Room database
    val totalUsers by repository.getTotalUsersCount().collectAsState(initial = 2)
    val proUsers by repository.getProUsersCount().collectAsState(initial = 1)
    val freeUsers by repository.getFreeUsersCount().collectAsState(initial = 1)
    val allUsersList by repository.getAllUsers().collectAsState(initial = emptyList())

    // Financial stats & transactions
    val paymentStats by repository.getPaymentStats().collectAsState(
        initial = PaymentStats(
            totalRevenueFcfa = 0,
            activeSubscriptions = 0,
            expiredSubscriptions = 0,
            successfulPayments = 0,
            pendingPayments = 0,
            failedPayments = 0,
            totalUsers = totalUsers,
            freeUsers = freeUsers,
            proUsers = proUsers,
            businessUsers = 0
        )
    )
    val allTransactions by repository.getAllTransactions().collectAsState(initial = emptyList())

    // Config states
    val pricingConfig by repository.pricingConfig.collectAsState()
    val quotaConfig by repository.quotaConfig.collectAsState()

    var proMonthlyPriceInput by remember(pricingConfig) { mutableStateOf(pricingConfig.proMonthlyPrice.toString()) }
    var proYearlyPriceInput by remember(pricingConfig) { mutableStateOf(pricingConfig.proYearlyPrice.toString()) }
    var bizMonthlyPriceInput by remember(pricingConfig) { mutableStateOf(pricingConfig.businessMonthlyPrice.toString()) }
    var bizYearlyPriceInput by remember(pricingConfig) { mutableStateOf(pricingConfig.businessYearlyPrice.toString()) }
    var discountInput by remember(pricingConfig) { mutableStateOf(pricingConfig.yearlyDiscountPercent.toString()) }

    var quotaDailyInput by remember(quotaConfig) { mutableStateOf(quotaConfig.freeDailyMessages.toString()) }
    var quotaMonthlyInput by remember(quotaConfig) { mutableStateOf(quotaConfig.freeMonthlyMessages.toString()) }
    var quotaMaxFilesInput by remember(quotaConfig) { mutableStateOf(quotaConfig.freeMaxFiles.toString()) }
    var quotaFileSizeMbInput by remember(quotaConfig) { mutableStateOf(quotaConfig.freeMaxFileSizeMb.toString()) }

    var isVoiceActive by remember { mutableStateOf(repository.isVoiceFeatureEnabled) }
    var isDocActive by remember { mutableStateOf(repository.isDocAnalysisEnabled) }
    var isProToolsActive by remember { mutableStateOf(repository.isProToolsEnabled) }

    // Manual Subscription Attribution Modal
    var showManualSubDialog by remember { mutableStateOf(false) }
    var selectedUserForSub by remember { mutableStateOf<UserEntity?>(null) }
    var selectedPlanForSub by remember { mutableStateOf("PRO") }
    var selectedPeriodForSub by remember { mutableStateOf("MONTHLY") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObinssDeepBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_admin_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Tableau de Bord Administrateur",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Supervision OBIN’SS IA en temps réel • ${currentUser?.name}",
                        fontSize = 11.sp,
                        color = ObinssGold
                    )
                }
            }
        }

        // Navigation Tabs (Users / Finances / Settings)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTabBadge(
                    title = "Utilisateurs",
                    icon = Icons.Default.People,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                AdminTabBadge(
                    title = "Finances & Abonnements",
                    icon = Icons.Default.Receipt,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1.3f)
                )
                AdminTabBadge(
                    title = "Tarifs & Quotas",
                    icon = Icons.Default.Tune,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // TAB 0: UTILISATEURS & SYSTÈME
        if (selectedTab == 0) {
            // Global KPIs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        icon = Icons.Default.People,
                        title = "Utilisateurs",
                        value = "$totalUsers",
                        change = "$freeUsers Free • $proUsers Pro",
                        color = ObinssGold,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        icon = Icons.Default.WorkspacePremium,
                        title = "Abonnés Actifs",
                        value = "${paymentStats.activeSubscriptions}",
                        change = "${paymentStats.proUsers} Pro • ${paymentStats.businessUsers} Biz",
                        color = ObinssSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // User Management Table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gestion des Comptes (${allUsersList.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Button(
                                onClick = {
                                    selectedUserForSub = allUsersList.firstOrNull()
                                    showManualSubDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ObinssGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Attribuer Abonnement", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allUsersList.forEach { user ->
                                UserManagementRow(
                                    user = user,
                                    onTogglePlan = {
                                        scope.launch {
                                            val newPlan = if (user.plan == "PRO") "FREE" else "PRO"
                                            repository.adminUpdateUserPlan(user.id, newPlan)
                                            Toast.makeText(context, "${user.name} basculé en $newPlan", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onToggleBlock = {
                                        scope.launch {
                                            val newBlocked = !user.isBlocked
                                            repository.adminToggleBlockUser(user.id, newBlocked)
                                            val status = if (newBlocked) "suspendu" else "réactivé"
                                            Toast.makeText(context, "${user.name} $status", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Feature Toggles & Moderation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fonctionnalités Système & Interrupteurs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureToggleRow(
                            title = "Interface Vocale (Dictée & TTS)",
                            checked = isVoiceActive,
                            onCheckedChange = {
                                isVoiceActive = it
                                repository.isVoiceFeatureEnabled = it
                            }
                        )

                        FeatureToggleRow(
                            title = "Analyse de Documents (PDF & Textes)",
                            checked = isDocActive,
                            onCheckedChange = {
                                isDocActive = it
                                repository.isDocAnalysisEnabled = it
                            }
                        )

                        FeatureToggleRow(
                            title = "Outils Spécialisés (12+ Modules)",
                            checked = isProToolsActive,
                            onCheckedChange = {
                                isProToolsActive = it
                                repository.isProToolsEnabled = it
                            }
                        )
                    }
                }
            }
        }

        // TAB 1: FINANCES & ABONNEMENTS
        if (selectedTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        icon = Icons.Default.AttachMoney,
                        title = "Revenu Total Validé",
                        value = "${paymentStats.totalRevenueFcfa} FCFA",
                        change = "${paymentStats.successfulPayments} paiements reçus",
                        color = ObinssSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        icon = Icons.Default.CardMembership,
                        title = "Transactions",
                        value = "${allTransactions.size}",
                        change = "${paymentStats.pendingPayments} en attente • ${paymentStats.failedPayments} échoués",
                        color = ObinssGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Manual Attribution Button Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObinssGold.copy(alpha = 0.15f))
                        .border(1.dp, ObinssGold, RoundedCornerShape(12.dp))
                        .clickable {
                            selectedUserForSub = allUsersList.firstOrNull()
                            showManualSubDialog = true
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Attribution Manuelle d'Abonnement", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Activer PRO ou BUSINESS pour un client (virement, chèque, cash)", fontSize = 11.sp, color = ObinssTextSecondary)
                            }
                        }
                        Text("Attribuer ›", color = ObinssGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Transactions Audit Log
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Journal des Transactions Récentes (${allTransactions.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (allTransactions.isEmpty()) {
                            Text("Aucune transaction enregistrée.", fontSize = 12.sp, color = ObinssTextMuted)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                allTransactions.take(15).forEach { tx ->
                                    val isSuccess = tx.status == PaymentStatus.ACTIVE.code
                                    val isPending = tx.status == PaymentStatus.PAYMENT_PENDING.code
                                    val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(tx.createdAt))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF151822))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "OBIN’SS ${tx.plan}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${tx.amountFcfa} FCFA",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (isSuccess) ObinssSuccess else ObinssGold
                                                    )
                                                }
                                                Text(
                                                    text = "${tx.userEmail} • ${tx.paymentMethod} • $dateStr",
                                                    fontSize = 10.sp,
                                                    color = ObinssTextMuted
                                                )
                                                Text(
                                                    text = "Réf: ${tx.reference}",
                                                    fontSize = 9.sp,
                                                    color = ObinssTextMuted
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when {
                                                            isSuccess -> ObinssSuccess.copy(alpha = 0.15f)
                                                            isPending -> ObinssGold.copy(alpha = 0.15f)
                                                            else -> ObinssRed.copy(alpha = 0.15f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = when {
                                                        isSuccess -> "ACTIF"
                                                        isPending -> "PENDING"
                                                        else -> "ÉCHEC"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isSuccess -> ObinssSuccess
                                                        isPending -> ObinssGold
                                                        else -> ObinssRedLight
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: TARIFS & QUOTAS CONFIGURABLES
        if (selectedTab == 2) {
            // Pricing Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grille Tarifaire Dynamique (FCFA)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proMonthlyPriceInput,
                                onValueChange = { proMonthlyPriceInput = it },
                                label = { Text("PRO Mensuel (FCFA)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssGold)
                            )
                            OutlinedTextField(
                                value = proYearlyPriceInput,
                                onValueChange = { proYearlyPriceInput = it },
                                label = { Text("PRO Annuel (FCFA)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssGold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bizMonthlyPriceInput,
                                onValueChange = { bizMonthlyPriceInput = it },
                                label = { Text("BUSINESS Mensuel (FCFA)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssGold)
                            )
                            OutlinedTextField(
                                value = bizYearlyPriceInput,
                                onValueChange = { bizYearlyPriceInput = it },
                                label = { Text("BUSINESS Annuel (FCFA)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssGold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Remise Annuelle (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssGold)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val proM = proMonthlyPriceInput.toIntOrNull() ?: 4900
                                val proY = proYearlyPriceInput.toIntOrNull() ?: 47000
                                val bizM = bizMonthlyPriceInput.toIntOrNull() ?: 19900
                                val bizY = bizYearlyPriceInput.toIntOrNull() ?: 191000
                                val disc = discountInput.toIntOrNull() ?: 20

                                repository.updatePricingConfig(
                                    PricingConfig(
                                        proMonthlyPrice = proM,
                                        proYearlyPrice = proY,
                                        businessMonthlyPrice = bizM,
                                        businessYearlyPrice = bizY,
                                        yearlyDiscountPercent = disc
                                    )
                                )
                                Toast.makeText(context, "Tarifs enregistrés avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObinssGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enregistrer les Tarifs", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            // Quota Limits Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = ObinssRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Limites & Quotas Formule FREE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quotaDailyInput,
                                onValueChange = { quotaDailyInput = it },
                                label = { Text("Messages / Jour") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssRed)
                            )
                            OutlinedTextField(
                                value = quotaMonthlyInput,
                                onValueChange = { quotaMonthlyInput = it },
                                label = { Text("Messages / Mois") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quotaMaxFilesInput,
                                onValueChange = { quotaMaxFilesInput = it },
                                label = { Text("Fichiers max") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssRed)
                            )
                            OutlinedTextField(
                                value = quotaFileSizeMbInput,
                                onValueChange = { quotaFileSizeMbInput = it },
                                label = { Text("Taille max (Mo)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ObinssRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val daily = quotaDailyInput.toIntOrNull() ?: 20
                                val monthly = quotaMonthlyInput.toIntOrNull() ?: 300
                                val files = quotaMaxFilesInput.toIntOrNull() ?: 3
                                val mb = quotaFileSizeMbInput.toIntOrNull() ?: 5

                                repository.updateQuotaConfig(
                                    QuotaConfig(
                                        freeDailyMessages = daily,
                                        freeMonthlyMessages = monthly,
                                        freeMaxFiles = files,
                                        freeMaxFileSizeMb = mb
                                    )
                                )
                                Toast.makeText(context, "Quotas FREE enregistrés avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enregistrer les Quotas", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Manual Subscription Assignment Dialog
    if (showManualSubDialog) {
        AlertDialog(
            onDismissRequest = { showManualSubDialog = false },
            containerColor = ObinssSurface,
            title = {
                Text("Attribution Manuelle d'Abonnement", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sélectionnez l'utilisateur et la formule à attribuer :", fontSize = 12.sp, color = ObinssTextSecondary)

                    // User selector
                    allUsersList.forEach { u ->
                        val isSelected = selectedUserForSub?.id == u.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ObinssGold.copy(alpha = 0.2f) else Color(0xFF181B26))
                                .border(1.dp, if (isSelected) ObinssGold else Color(0xFF2B2F40), RoundedCornerShape(8.dp))
                                .clickable { selectedUserForSub = u }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(u.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(u.email, fontSize = 10.sp, color = ObinssTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Formule à appliquer :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObinssGold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRO", "BUSINESS", "FREE").forEach { p ->
                            val isSel = selectedPlanForSub == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ObinssRed else Color(0xFF222634))
                                    .clickable { selectedPlanForSub = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Période :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObinssGold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MONTHLY" to "Mensuel", "YEARLY" to "Annuel").forEach { (code, label) ->
                            val isSel = selectedPeriodForSub == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ObinssGold else Color(0xFF222634))
                                    .clickable { selectedPeriodForSub = code }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUserForSub?.let { u ->
                            scope.launch {
                                repository.adminSetUserSubscription(
                                    userId = u.id,
                                    plan = selectedPlanForSub,
                                    billingPeriod = selectedPeriodForSub,
                                    status = if (selectedPlanForSub == "FREE") PaymentStatus.EXPIRED.code else PaymentStatus.ACTIVE.code
                                )
                                Toast.makeText(context, "Abonnement $selectedPlanForSub attribué à ${u.name}", Toast.LENGTH_SHORT).show()
                                showManualSubDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssSuccess)
                ) {
                    Text("Valider & Activer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showManualSubDialog = false }) {
                    Text("Annuler", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun AdminTabBadge(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ObinssRed else Color(0xFF181B26))
            .border(1.dp, if (isSelected) ObinssRed else Color(0xFF282C3D), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ObinssTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else ObinssTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UserManagementRow(
    user: UserEntity,
    onTogglePlan: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(user.createdAt))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161922))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (user.role == "ADMIN") ObinssGold.copy(alpha = 0.2f) else Color(0xFF2E3240)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            color = if (user.role == "ADMIN") ObinssGold else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            if (user.role == "ADMIN") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ObinssGold)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("ADMIN", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            }
                        }
                        Text(user.email, fontSize = 11.sp, color = ObinssTextSecondary)
                    }
                }

                // Plan badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (user.plan) {
                                "BUSINESS" -> ObinssRed.copy(alpha = 0.2f)
                                "PRO" -> ObinssGold.copy(alpha = 0.2f)
                                else -> Color(0xFF2E3240)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = user.plan,
                        color = when (user.plan) {
                            "BUSINESS" -> ObinssRedLight
                            "PRO" -> ObinssGold
                            else -> Color.LightGray
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Inscrit le $dateStr", fontSize = 10.sp, color = ObinssTextMuted)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Plan toggle button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF222634))
                            .clickable { onTogglePlan() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (user.plan == "PRO") "Basculer FREE" else "Passer PRO",
                            color = ObinssGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Block / Unblock button
                    if (user.role != "ADMIN") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (user.isBlocked) Color(0xFF1E3A24) else Color(0xFF3B151A))
                                .clickable { onToggleBlock() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (user.isBlocked) "Débloquer" else "Suspendre",
                                color = if (user.isBlocked) Color(0xFF81C784) else Color(0xFFFF8A80),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    change: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ObinssSurface)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 10.sp, color = ObinssTextSecondary, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = change, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun FeatureToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ObinssRed,
                uncheckedTrackColor = Color(0xFF2E3240)
            )
        )
    }
}
