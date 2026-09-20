package com.example.obinssia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.BillingPeriod
import com.example.obinssia.data.model.PaymentProvider
import com.example.obinssia.data.model.SubscriptionPlan
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssSurface
import com.example.ui.theme.ObinssSurfaceVariant
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

/**
 * Écran officiel de Monétisation et Abonnements OBIN’SS IA
 * 3 Offres : FREE, PRO, BUSINESS
 * Prix configurables en direct par l'administration.
 * Support des moyens de paiement d'Afrique de l'Ouest (Wave, Orange Money, MTN, Moov) et Carte Bancaire.
 */
@Composable
fun SubscriptionScreen(
    currentPlan: String,
    repository: ObinssRepository,
    onPlanChanged: (String) -> Unit = {},
    onInitiatePayment: (transactionReference: String) -> Unit = {},
    onViewInvoices: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pricingConfig by repository.pricingConfig.collectAsState()
    val quotaConfig by repository.quotaConfig.collectAsState()
    val currentUserSubscription by repository.getSubscriptionForCurrentUser().collectAsState(initial = null)

    var selectedPeriod by remember { mutableStateOf(BillingPeriod.MONTHLY) }
    var selectedProvider by remember { mutableStateOf(PaymentProvider.WAVE) }
    var isSandboxMode by remember { mutableStateOf(true) } // Par défaut Sandbox pour test immédiat
    var isProcessingPayment by remember { mutableStateOf(false) }

    val effectiveCurrentPlan = currentUserSubscription?.plan ?: currentPlan.uppercase()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObinssDeepBlack)
    ) {
        val isTabletOrDesktop = maxWidth >= 768.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTabletOrDesktop) 32.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Header Top with Invoices shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObinssSurface)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PLAN ACTUEL : $effectiveCurrentPlan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (effectiveCurrentPlan == "PRO") ObinssGold else if (effectiveCurrentPlan == "BUSINESS") ObinssRedLight else ObinssTextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObinssSurface)
                            .border(1.dp, Color(0xFF2E3345), RoundedCornerShape(8.dp))
                            .clickable(onClick = onViewInvoices)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mes Factures", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header Title
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ObinssGold.copy(alpha = 0.15f))
                        .border(1.5.dp, ObinssGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = ObinssGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Choisissez votre formule 𝐎𝐁𝐈𝐍’𝐒𝐒 IA",
                    fontWeight = FontWeight.Black,
                    fontSize = if (isTabletOrDesktop) 28.sp else 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Débloquez la puissance maximale de l'Intelligence Artificielle professionnelle.\nSans engagement, résiliable à tout moment.",
                    fontSize = 13.sp,
                    color = ObinssTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.widthIn(max = 600.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Billing Period Switcher (Mensuel / Annuel -20%)
                BillingPeriodToggle(
                    selectedPeriod = selectedPeriod,
                    discountPercent = pricingConfig.yearlyDiscountPercent,
                    onSelectPeriod = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // The 3 Plan Cards (Responsive layout)
            if (isTabletOrDesktop) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Free Card
                        Box(modifier = Modifier.weight(1f)) {
                            PlanCard(
                                title = "FREE",
                                subtitle = "Découverte & Essentiel",
                                priceFormatted = "0 FCFA",
                                periodLabel = "/ toujours gratuit",
                                badge = null,
                                isPopular = false,
                                isCurrentPlan = effectiveCurrentPlan == "FREE",
                                features = listOf(
                                    "${quotaConfig.freeDailyMessages} messages / jour",
                                    "${quotaConfig.freeMonthlyMessages} messages / mois",
                                    "${quotaConfig.freeMaxFiles} fichiers max (${quotaConfig.freeMaxFileSizeMb} Mo/fichier)",
                                    "Modes Rapide & Équilibré",
                                    "Vitesse standard"
                                ),
                                lockedFeatures = listOf(
                                    "Modes Expert, Code & Étude",
                                    "Priorité serveur haute vitesse",
                                    "Analyse documentaire illimitée"
                                ),
                                buttonLabel = if (effectiveCurrentPlan == "FREE") "Votre formule actuelle" else "Rétrograder à Free",
                                buttonEnabled = effectiveCurrentPlan != "FREE",
                                isButtonPrimary = false,
                                onActionClick = {
                                    scope.launch {
                                        repository.updatePlan("FREE")
                                        onPlanChanged("FREE")
                                        Toast.makeText(context, "Vous êtes sur la formule Free", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        // Pro Card (Featured)
                        Box(modifier = Modifier.weight(1.08f)) {
                            PlanCard(
                                title = "PRO",
                                subtitle = "Pour professionnels & créateurs",
                                priceFormatted = pricingConfig.formatFcfa(
                                    if (selectedPeriod == BillingPeriod.YEARLY) pricingConfig.proYearlyPrice else pricingConfig.proMonthlyPrice
                                ),
                                periodLabel = if (selectedPeriod == BillingPeriod.YEARLY) " / an (-${pricingConfig.yearlyDiscountPercent}%)" else " / mois",
                                badge = "LE PLUS POPULAIRE",
                                isPopular = true,
                                isCurrentPlan = effectiveCurrentPlan == "PRO",
                                features = listOf(
                                    "500 messages / jour & 10 000 / mois",
                                    "50 fichiers (jusqu'à 50 Mo)",
                                    "Les 6 modes IA débloqués",
                                    "Mode Code & Expert pleine vitesse",
                                    "Analyse documentaire avancée",
                                    "Serveur prioritaire instantané"
                                ),
                                lockedFeatures = emptyList(),
                                buttonLabel = if (effectiveCurrentPlan == "PRO") "Formule active" else "Passer à PRO",
                                buttonEnabled = effectiveCurrentPlan != "PRO" && !isProcessingPayment,
                                isButtonPrimary = true,
                                onActionClick = {
                                    startPayment(
                                        plan = SubscriptionPlan.PRO,
                                        period = selectedPeriod,
                                        provider = selectedProvider,
                                        isSandbox = isSandboxMode,
                                        repository = repository,
                                        scope = scope,
                                        context = context,
                                        onProcessingChange = { isProcessingPayment = it },
                                        onSuccess = onInitiatePayment
                                    )
                                }
                            )
                        }

                        // Business Card
                        Box(modifier = Modifier.weight(1f)) {
                            PlanCard(
                                title = "BUSINESS",
                                subtitle = "Équipes, PME & Entreprises",
                                priceFormatted = pricingConfig.formatFcfa(
                                    if (selectedPeriod == BillingPeriod.YEARLY) pricingConfig.businessYearlyPrice else pricingConfig.businessMonthlyPrice
                                ),
                                periodLabel = if (selectedPeriod == BillingPeriod.YEARLY) " / an (-${pricingConfig.yearlyDiscountPercent}%)" else " / mois",
                                badge = "POUR LES ÉQUIPES",
                                isPopular = false,
                                isCurrentPlan = effectiveCurrentPlan == "BUSINESS",
                                features = listOf(
                                    "Messages et requêtes illimités",
                                    "Fichiers et documents illimités (150 Mo)",
                                    "Accès prioritaire absolu",
                                    "Multi-comptes & tableau d'équipe",
                                    "Support dédié VIP 24h/7j",
                                    "Facturation certifiée entreprise"
                                ),
                                lockedFeatures = emptyList(),
                                buttonLabel = if (effectiveCurrentPlan == "BUSINESS") "Formule active" else "Choisir Business",
                                buttonEnabled = effectiveCurrentPlan != "BUSINESS" && !isProcessingPayment,
                                isButtonPrimary = false,
                                onActionClick = {
                                    startPayment(
                                        plan = SubscriptionPlan.BUSINESS,
                                        period = selectedPeriod,
                                        provider = selectedProvider,
                                        isSandbox = isSandboxMode,
                                        repository = repository,
                                        scope = scope,
                                        context = context,
                                        onProcessingChange = { isProcessingPayment = it },
                                        onSuccess = onInitiatePayment
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // Mobile Stack
                item {
                    // Pro Card first on mobile because it's the recommended target
                    PlanCard(
                        title = "PRO",
                        subtitle = "Pour professionnels & créateurs",
                        priceFormatted = pricingConfig.formatFcfa(
                            if (selectedPeriod == BillingPeriod.YEARLY) pricingConfig.proYearlyPrice else pricingConfig.proMonthlyPrice
                        ),
                        periodLabel = if (selectedPeriod == BillingPeriod.YEARLY) " / an (-${pricingConfig.yearlyDiscountPercent}%)" else " / mois",
                        badge = "RECOMMANDÉ • LE PLUS POPULAIRE",
                        isPopular = true,
                        isCurrentPlan = effectiveCurrentPlan == "PRO",
                        features = listOf(
                            "500 messages / jour & 10 000 / mois",
                            "50 fichiers (jusqu'à 50 Mo par document)",
                            "Les 6 modes IA débloqués (Code, Expert...)",
                            "Analyse de documents & Outils complets",
                            "Serveur prioritaire & Vitesse maximale"
                        ),
                        lockedFeatures = emptyList(),
                        buttonLabel = if (effectiveCurrentPlan == "PRO") "Votre formule active" else "Passer à PRO",
                        buttonEnabled = effectiveCurrentPlan != "PRO" && !isProcessingPayment,
                        isButtonPrimary = true,
                        onActionClick = {
                            startPayment(
                                plan = SubscriptionPlan.PRO,
                                period = selectedPeriod,
                                provider = selectedProvider,
                                isSandbox = isSandboxMode,
                                repository = repository,
                                scope = scope,
                                context = context,
                                onProcessingChange = { isProcessingPayment = it },
                                onSuccess = onInitiatePayment
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PlanCard(
                        title = "BUSINESS",
                        subtitle = "Équipes, Cabinets & Entreprises",
                        priceFormatted = pricingConfig.formatFcfa(
                            if (selectedPeriod == BillingPeriod.YEARLY) pricingConfig.businessYearlyPrice else pricingConfig.businessMonthlyPrice
                        ),
                        periodLabel = if (selectedPeriod == BillingPeriod.YEARLY) " / an (-${pricingConfig.yearlyDiscountPercent}%)" else " / mois",
                        badge = "ENTREPRISES",
                        isPopular = false,
                        isCurrentPlan = effectiveCurrentPlan == "BUSINESS",
                        features = listOf(
                            "Messages et requêtes illimités",
                            "Documents illimités (jusqu'à 150 Mo)",
                            "Priorité absolue et débit réservé",
                            "Multi-utilisateurs & Facturation certifiée",
                            "Support dédié VIP 24h/7j"
                        ),
                        lockedFeatures = emptyList(),
                        buttonLabel = if (effectiveCurrentPlan == "BUSINESS") "Formule active" else "Choisir Business",
                        buttonEnabled = effectiveCurrentPlan != "BUSINESS" && !isProcessingPayment,
                        isButtonPrimary = false,
                        onActionClick = {
                            startPayment(
                                plan = SubscriptionPlan.BUSINESS,
                                period = selectedPeriod,
                                provider = selectedProvider,
                                isSandbox = isSandboxMode,
                                repository = repository,
                                scope = scope,
                                context = context,
                                onProcessingChange = { isProcessingPayment = it },
                                onSuccess = onInitiatePayment
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PlanCard(
                        title = "FREE",
                        subtitle = "Gratuit pour découvrir",
                        priceFormatted = "0 FCFA",
                        periodLabel = "/ sans limite de temps",
                        badge = null,
                        isPopular = false,
                        isCurrentPlan = effectiveCurrentPlan == "FREE",
                        features = listOf(
                            "${quotaConfig.freeDailyMessages} messages / jour",
                            "${quotaConfig.freeMonthlyMessages} messages / mois",
                            "${quotaConfig.freeMaxFiles} fichiers max (${quotaConfig.freeMaxFileSizeMb} Mo)",
                            "Modes Rapide & Équilibré",
                            "Vitesse standard"
                        ),
                        lockedFeatures = listOf(
                            "Modes Expert, Code & Étude",
                            "Accès serveur prioritaire",
                            "Analyse de gros documents"
                        ),
                        buttonLabel = if (effectiveCurrentPlan == "FREE") "Votre formule actuelle" else "Rétrograder à Free",
                        buttonEnabled = effectiveCurrentPlan != "FREE",
                        isButtonPrimary = false,
                        onActionClick = {
                            scope.launch {
                                repository.cancelSubscription(repository.currentUserId.value)
                                onPlanChanged("FREE")
                                Toast.makeText(context, "Vous êtes repassé sur la formule Free", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Payment Methods & Sandbox Section
            item {
                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282D3D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MOYENS DE PAIEMENT SÉCURISÉS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObinssGold,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ObinssSuccess, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chiffré SSL", fontSize = 10.sp, color = ObinssSuccess)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Payment Provider selector badges
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentProviderCard(
                                    provider = PaymentProvider.WAVE,
                                    isSelected = selectedProvider == PaymentProvider.WAVE,
                                    onClick = { selectedProvider = PaymentProvider.WAVE },
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentProviderCard(
                                    provider = PaymentProvider.ORANGE_MONEY,
                                    isSelected = selectedProvider == PaymentProvider.ORANGE_MONEY,
                                    onClick = { selectedProvider = PaymentProvider.ORANGE_MONEY },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentProviderCard(
                                    provider = PaymentProvider.MTN_MOMO,
                                    isSelected = selectedProvider == PaymentProvider.MTN_MOMO,
                                    onClick = { selectedProvider = PaymentProvider.MTN_MOMO },
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentProviderCard(
                                    provider = PaymentProvider.MOOV_MONEY,
                                    isSelected = selectedProvider == PaymentProvider.MOOV_MONEY,
                                    onClick = { selectedProvider = PaymentProvider.MOOV_MONEY },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            PaymentProviderCard(
                                provider = PaymentProvider.CARD,
                                isSelected = selectedProvider == PaymentProvider.CARD,
                                onClick = { selectedProvider = PaymentProvider.CARD },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF222634))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sandbox Toggle Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = ObinssGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Mode Sandbox (Test / Démo)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Permet de tester le flux réel et simuler les webhooks sans débit réel.",
                                        fontSize = 11.sp,
                                        color = ObinssTextMuted
                                    )
                                }
                            }
                            Switch(
                                checked = isSandboxMode,
                                onCheckedChange = { isSandboxMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObinssGold,
                                    checkedTrackColor = ObinssGold.copy(alpha = 0.3f),
                                    uncheckedThumbColor = ObinssTextSecondary,
                                    uncheckedTrackColor = ObinssSurfaceVariant
                                ),
                                modifier = Modifier.testTag("switch_sandbox_mode")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

private fun startPayment(
    plan: SubscriptionPlan,
    period: BillingPeriod,
    provider: PaymentProvider,
    isSandbox: Boolean,
    repository: ObinssRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    onProcessingChange: (Boolean) -> Unit,
    onSuccess: (transactionReference: String) -> Unit
) {
    onProcessingChange(true)
    scope.launch {
        val result = repository.initiateSubscriptionPayment(
            plan = plan,
            period = period,
            provider = provider,
            isSandbox = isSandbox
        )

        onProcessingChange(false)

        result.onSuccess { response ->
            Toast.makeText(context, "Transaction initialisée : ${response.transactionReference}", Toast.LENGTH_SHORT).show()
            onSuccess(response.transactionReference)
        }.onFailure { err ->
            Toast.makeText(context, "Erreur d'initialisation : ${err.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun BillingPeriodToggle(
    selectedPeriod: BillingPeriod,
    discountPercent: Int,
    onSelectPeriod: (BillingPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(ObinssSurface)
            .border(1.dp, Color(0xFF2E3345), RoundedCornerShape(30.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(if (selectedPeriod == BillingPeriod.MONTHLY) ObinssRed else Color.Transparent)
                .clickable { onSelectPeriod(BillingPeriod.MONTHLY) }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mensuel",
                fontSize = 13.sp,
                fontWeight = if (selectedPeriod == BillingPeriod.MONTHLY) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedPeriod == BillingPeriod.MONTHLY) Color.White else ObinssTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(if (selectedPeriod == BillingPeriod.YEARLY) ObinssRed else Color.Transparent)
                .clickable { onSelectPeriod(BillingPeriod.YEARLY) }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Annuel",
                    fontSize = 13.sp,
                    fontWeight = if (selectedPeriod == BillingPeriod.YEARLY) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedPeriod == BillingPeriod.YEARLY) Color.White else ObinssTextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObinssGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "-$discountPercent%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    priceFormatted: String,
    periodLabel: String,
    badge: String?,
    isPopular: Boolean,
    isCurrentPlan: Boolean,
    features: List<String>,
    lockedFeatures: List<String>,
    buttonLabel: String,
    buttonEnabled: Boolean,
    isButtonPrimary: Boolean,
    onActionClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPopular) Color(0xFF191D2B) else ObinssSurface
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPopular) 1.5.dp else 1.dp,
            color = if (isPopular) ObinssGold else Color(0xFF262B3A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPopular) ObinssGold else ObinssRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPopular) Color.Black else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OBIN’SS IA $title",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPopular) ObinssGold else Color.White
                    )

                    if (isCurrentPlan) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObinssSuccess.copy(alpha = 0.2f))
                                .border(1.dp, ObinssSuccess, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Actuel", fontSize = 10.sp, color = ObinssSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = ObinssTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = priceFormatted,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = periodLabel,
                        fontSize = 12.sp,
                        color = ObinssTextSecondary,
                        modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF222634))
                Spacer(modifier = Modifier.height(16.dp))

                // Included Features
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isPopular) ObinssGold else ObinssSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feat, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    // Locked Features
                    lockedFeatures.forEach { locked ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = ObinssTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(locked, fontSize = 12.sp, color = ObinssTextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = onActionClick,
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isPopular -> ObinssGold
                        isButtonPrimary -> ObinssRed
                        else -> Color(0xFF282D3D)
                    },
                    disabledContainerColor = Color(0xFF1F2330),
                    disabledContentColor = ObinssTextMuted
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_plan_action_${title.lowercase()}")
            ) {
                Text(
                    text = buttonLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = when {
                        !buttonEnabled -> ObinssTextMuted
                        isPopular -> Color.Black
                        else -> Color.White
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentProviderCard(
    provider: PaymentProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ObinssRed.copy(alpha = 0.15f) else Color(0xFF141722))
            .border(
                width = 1.dp,
                color = if (isSelected) ObinssGold else Color(0xFF282D3C),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (provider) {
                    PaymentProvider.CARD -> Icons.Default.CreditCard
                    else -> Icons.Default.Payment
                },
                contentDescription = null,
                tint = if (isSelected) ObinssGold else ObinssTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = provider.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else ObinssTextSecondary,
                maxLines = 1
            )
        }
    }
}
