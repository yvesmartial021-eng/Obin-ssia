package com.example.obinssia.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.PaymentStatus
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran d'attente et de vérification sécurisée du paiement
 * Respecte strictement la règle : "Ne prétends pas qu'un paiement a réussi tant qu'un serveur
 * ou un webhook n'a pas confirmé réellement la transaction."
 */
@Composable
fun PaymentPendingScreen(
    transactionReference: String,
    repository: ObinssRepository,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val transaction by repository.getTransactionByReferenceFlow(transactionReference).collectAsState(initial = null)

    var isVerifying by remember { mutableStateOf(false) }
    var verificationCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pulsing animation for waiting state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Periodic check (simulating polling webhook status every 6 seconds)
    LaunchedEffect(transactionReference, transaction?.status) {
        if (transaction?.status == PaymentStatus.PAYMENT_PENDING.code) {
            delay(3500)
            if (transaction?.status == PaymentStatus.PAYMENT_PENDING.code) {
                repository.verifyPaymentTransaction(transactionReference)
                verificationCount++
            }
        }
    }

    val status = transaction?.status ?: PaymentStatus.PAYMENT_PENDING.code
    val isPending = status == PaymentStatus.PAYMENT_PENDING.code
    val isSuccess = status == PaymentStatus.ACTIVE.code
    val isFailed = status == PaymentStatus.PAYMENT_FAILED.code || status == PaymentStatus.EXPIRED.code

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObinssDeepBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_payment_pending_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                Text(
                    text = if (isSuccess) "Paiement Confirmé" else if (isFailed) "Échec du paiement" else "Vérification en cours",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (transaction?.isSandboxTest == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObinssGold.copy(alpha = 0.2f))
                            .border(1.dp, ObinssGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("SANDBOX", color = ObinssGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Central Animated Status Hero
        item {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (isPending) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSuccess -> ObinssSuccess.copy(alpha = 0.15f)
                            isFailed -> ObinssRed.copy(alpha = 0.15f)
                            else -> ObinssGold.copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isSuccess -> ObinssSuccess
                            isFailed -> ObinssRed
                            else -> ObinssGold
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Succès",
                            tint = ObinssSuccess,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    isFailed -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Échec",
                            tint = ObinssRed,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    else -> {
                        CircularProgressIndicator(
                            color = ObinssGold,
                            modifier = Modifier.size(52.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    isSuccess -> "Abonnement Activé avec Succès !"
                    isFailed -> "Le paiement n'a pas pu aboutir"
                    else -> "En attente de la confirmation serveur..."
                },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isSuccess -> ObinssSuccess
                    isFailed -> ObinssRedLight
                    else -> Color.White
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isSuccess -> "Votre compte a été surclassé vers la formule ${transaction?.plan ?: "PRO"}. Profitez de toutes les fonctionnalités illimitées."
                    isFailed -> transaction?.failureReason ?: "La transaction a été rejetée ou a expiré. Aucun montant n'a été prélevé."
                    else -> "Veuillez valider le paiement sur votre application ${transaction?.paymentMethod ?: "Mobile Money"}. Cette page se mettra à jour automatiquement dès confirmation par le serveur."
                },
                fontSize = 13.sp,
                color = ObinssTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Transaction Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3040)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RÉCAPITULATIF DE LA TRANSACTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObinssGold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(
                        label = "Référence unique",
                        value = transactionReference,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(transactionReference))
                            Toast.makeText(context, "Référence copiée", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = Color(0xFF222634), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Formule choisie",
                        value = "OBIN’SS IA ${transaction?.plan ?: "PRO"}"
                    )

                    HorizontalDivider(color = Color(0xFF222634), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Période",
                        value = if (transaction?.billingPeriod == "YEARLY") "Annuel (Facturation 1 an)" else "Mensuel (30 jours)"
                    )

                    HorizontalDivider(color = Color(0xFF222634), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Moyen de paiement",
                        value = transaction?.paymentMethod ?: "Mobile Money"
                    )

                    HorizontalDivider(color = Color(0xFF222634), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Montant total TTC",
                        value = "${transaction?.amountFcfa ?: 4900} FCFA",
                        isHighlight = true
                    )

                    HorizontalDivider(color = Color(0xFF222634), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Statut serveur",
                        value = when (status) {
                            PaymentStatus.ACTIVE.code -> "ACTIF (Validé)"
                            PaymentStatus.PAYMENT_FAILED.code -> "ÉCHEC"
                            PaymentStatus.EXPIRED.code -> "EXPIRÉ"
                            else -> "EN ATTENTE (Pending)"
                        },
                        statusColor = when (status) {
                            PaymentStatus.ACTIVE.code -> ObinssSuccess
                            PaymentStatus.PAYMENT_FAILED.code, PaymentStatus.EXPIRED.code -> ObinssRedLight
                            else -> ObinssGold
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Operator Instruction Note if Pending
        if (isPending) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191D28)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2E3D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ObinssGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Instructions de confirmation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val instructions = when (transaction?.paymentMethod?.lowercase()) {
                                "wave" -> "Ouvrez l'application Wave sur votre téléphone et validez la notification de transfert de ${transaction?.amountFcfa} FCFA."
                                "orange money" -> "Un prompt USSD Orange Money s'affiche sur votre téléphone. Composez votre code secret pour valider le débit."
                                "mtn momo" -> "Approuvez la demande de débit de ${transaction?.amountFcfa} FCFA dans votre application MTN MoMo ou tapez *133#."
                                "moov money" -> "Confirmez le débit Moov Money en composant votre code secret lors du prompt opérateur."
                                else -> "Pour des raisons de sécurité, nous attendons la confirmation finale de la passerelle de paiement."
                            }
                            Text(
                                text = instructions,
                                fontSize = 12.sp,
                                color = ObinssTextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isSuccess) {
                    Button(
                        onClick = onPaymentSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = ObinssSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_payment_access_pro")
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accéder à OBIN’SS IA PRO",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (isPending) {
                    Button(
                        onClick = {
                            isVerifying = true
                            scope.launch {
                                repository.verifyPaymentTransaction(transactionReference)
                                delay(600)
                                isVerifying = false
                            }
                        },
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_verify_payment_manual")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vérifier à nouveau le statut", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Vérifier plus tard / Retour", color = ObinssTextSecondary)
                    }
                } else {
                    // Failed
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Réessayer une autre méthode", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // SANDBOX SIMULATION TESTING PANEL
        // Permet de tester le flux réel en sandbox comme demandé par les directives
        if (isPending) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObinssGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PANNEAU DE TEST SANDBOX (DÉMONSTRATION)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObinssGold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Testez la réaction de l'application face aux différents retours webhook de la passerelle de paiement :",
                            fontSize = 12.sp,
                            color = ObinssTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.verifyPaymentTransaction(transactionReference, simulatedOutcome = PaymentStatus.ACTIVE)
                                        Toast.makeText(context, "Webhook: Paiement Validé !", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ObinssSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sandbox_simulate_success")
                            ) {
                                Text("Simuler Succès", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.verifyPaymentTransaction(transactionReference, simulatedOutcome = PaymentStatus.PAYMENT_FAILED)
                                        Toast.makeText(context, "Webhook: Paiement Rejeté", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ObinssRed.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sandbox_simulate_failure")
                            ) {
                                Text("Simuler Échec", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Security Notice
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = ObinssTextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Paiement chiffré SSL 256-bit • Aucun secret bancaire stocké sur l'appareil",
                    fontSize = 11.sp,
                    color = ObinssTextMuted,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    statusColor: Color? = null,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ObinssTextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = if (isHighlight) 14.sp else 12.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = statusColor ?: if (isHighlight) ObinssGold else Color.White
            )
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copier",
                        tint = ObinssGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
