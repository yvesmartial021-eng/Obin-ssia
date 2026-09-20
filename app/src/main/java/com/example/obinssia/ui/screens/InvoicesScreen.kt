package com.example.obinssia.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.PaymentStatus
import com.example.obinssia.data.model.PaymentTransactionEntity
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssSurface
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Section "Mes paiements / Factures"
 * Historique complet des transactions, reçus et factures d'abonnement certifiées.
 */
@Composable
fun InvoicesScreen(
    repository: ObinssRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val transactions by repository.getTransactionsForCurrentUser().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState(initial = null)

    var selectedTransactionForInvoice by remember { mutableStateOf<PaymentTransactionEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = when (selectedFilter) {
        "ACTIVE" -> transactions.filter { it.status == PaymentStatus.ACTIVE.code }
        "PENDING" -> transactions.filter { it.status == PaymentStatus.PAYMENT_PENDING.code }
        "FAILED" -> transactions.filter { it.status == PaymentStatus.PAYMENT_FAILED.code }
        else -> transactions
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObinssDeepBlack)
            .padding(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_invoices_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Mes Paiements & Factures",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = ObinssGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObinssSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3040)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL DES TRANSACTIONS", fontSize = 11.sp, color = ObinssGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${transactions.size} transaction${if (transactions.size > 1) "s" else ""}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    val totalSpent = transactions.filter { it.status == PaymentStatus.ACTIVE.code }.sumOf { it.amountFcfa }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DÉPENSES VALIDÉES", fontSize = 11.sp, color = ObinssTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalSpent FCFA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObinssSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab(
                    title = "Toutes (${transactions.size})",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" }
                )
                FilterTab(
                    title = "Validées",
                    isSelected = selectedFilter == "ACTIVE",
                    onClick = { selectedFilter = "ACTIVE" }
                )
                FilterTab(
                    title = "En attente",
                    isSelected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = "PENDING" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = ObinssTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune transaction trouvée",
                            fontSize = 14.sp,
                            color = ObinssTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vos reçus et factures d'abonnement apparaîtront ici.",
                            fontSize = 12.sp,
                            color = ObinssTextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.reference }) { tx ->
                TransactionCard(
                    transaction = tx,
                    onViewInvoice = { selectedTransactionForInvoice = tx }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Invoice Receipt Dialog
    selectedTransactionForInvoice?.let { tx ->
        InvoiceReceiptDialog(
            transaction = tx,
            userName = currentUser?.name ?: "Client OBIN’SS",
            userEmail = currentUser?.email ?: tx.userEmail,
            onDismiss = { selectedTransactionForInvoice = null },
            onShare = {
                val shareText = """
                    📄 FACTURE ACQUITTÉE - OBIN’SS IA
                    Réf : ${tx.reference}
                    Date : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.createdAt))}
                    Formule : OBIN’SS IA ${tx.plan} (${if (tx.billingPeriod == "YEARLY") "Annuel" else "Mensuel"})
                    Montant TTC : ${tx.amountFcfa} FCFA
                    Moyen : ${tx.paymentMethod}
                    Statut : Validé & Conforme
                    Émetteur : OBIN’SS IA Technologies (Abidjan)
                """.trimIndent()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Partager la facture"))
            }
        )
    }
}

@Composable
private fun FilterTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ObinssRed else ObinssSurface)
            .border(1.dp, if (isSelected) ObinssRed else Color(0xFF2A2E3D), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else ObinssTextSecondary
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: PaymentTransactionEntity,
    onViewInvoice: () -> Unit
) {
    val isSuccess = transaction.status == PaymentStatus.ACTIVE.code
    val isPending = transaction.status == PaymentStatus.PAYMENT_PENDING.code
    val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.FRENCH)

    Card(
        colors = CardDefaults.cardColors(containerColor = ObinssSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242838)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSuccess -> ObinssSuccess.copy(alpha = 0.15f)
                                    isPending -> ObinssGold.copy(alpha = 0.15f)
                                    else -> ObinssRed.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isSuccess -> Icons.Default.CheckCircle
                                isPending -> Icons.Default.HourglassTop
                                else -> Icons.Default.Close
                            },
                            contentDescription = null,
                            tint = when {
                                isSuccess -> ObinssSuccess
                                isPending -> ObinssGold
                                else -> ObinssRedLight
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "OBIN’SS IA ${transaction.plan}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${transaction.paymentMethod} • ${dateFormat.format(Date(transaction.createdAt))}",
                            fontSize = 11.sp,
                            color = ObinssTextSecondary
                        )
                    }
                }

                Text(
                    text = "${transaction.amountFcfa} FCFA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSuccess) ObinssSuccess else Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFF1E2230))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Réf : ${transaction.reference}",
                    fontSize = 10.sp,
                    color = ObinssTextMuted,
                    maxLines = 1
                )

                if (isSuccess) {
                    Text(
                        text = "Voir la facture ›",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObinssGold,
                        modifier = Modifier
                            .clickable(onClick = onViewInvoice)
                            .padding(4.dp)
                            .testTag("btn_view_invoice_${transaction.reference}")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isPending) ObinssGold.copy(alpha = 0.15f) else ObinssRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isPending) "En attente" else "Échoué",
                            fontSize = 10.sp,
                            color = if (isPending) ObinssGold else ObinssRedLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Facture détaillée et certifiée
 */
@Composable
fun InvoiceReceiptDialog(
    transaction: PaymentTransactionEntity,
    userName: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRENCH)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObinssSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObinssRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("O", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FACTURE ACQUITTÉE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = ObinssSuccess,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Enterprise Header
                Text(
                    text = "OBIN’SS IA TECHNOLOGIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObinssGold
                )
                Text(
                    text = "Plateforme d'Intelligence Artificielle • Abidjan, Côte d'Ivoire\nSIRET : CI-ABJ-2026-B-10928 • support@obinssia.ai",
                    fontSize = 10.sp,
                    color = ObinssTextMuted,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF2A2E3D))
                Spacer(modifier = Modifier.height(12.dp))

                // Metadata
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("FACTURE N°", fontSize = 10.sp, color = ObinssTextMuted)
                        Text("FAC-${transaction.reference.takeLast(8)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DATE D'ÉMISSION", fontSize = 10.sp, color = ObinssTextMuted)
                        Text(dateFormat.format(Date(transaction.createdAt)), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObinssDeepBlack)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("FACTURÉ À :", fontSize = 10.sp, color = ObinssGold, fontWeight = FontWeight.Bold)
                        Text(userName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(userEmail, fontSize = 11.sp, color = ObinssTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Items breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Prestation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObinssTextSecondary)
                    Text("Montant", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObinssTextSecondary)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Abonnement OBIN’SS IA ${transaction.plan}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            text = if (transaction.billingPeriod == "YEARLY") "Formule annuelle (accès 365 jours)" else "Formule mensuelle (accès 30 jours)",
                            fontSize = 10.sp,
                            color = ObinssTextMuted
                        )
                        Text("Règlement : ${transaction.paymentMethod}", fontSize = 10.sp, color = ObinssTextMuted)
                    }
                    Text("${transaction.amountFcfa} FCFA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF2A2E3D))
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total HT :", fontSize = 11.sp, color = ObinssTextSecondary)
                    Text("${transaction.amountFcfa} FCFA", fontSize = 11.sp, color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TVA (0% Export Services) :", fontSize = 11.sp, color = ObinssTextSecondary)
                    Text("0 FCFA", fontSize = 11.sp, color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL TTC PAYÉ :", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ObinssGold)
                    Text("${transaction.amountFcfa} FCFA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ObinssSuccess)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Certified Stamp Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObinssSuccess.copy(alpha = 0.1f))
                        .border(1.dp, ObinssSuccess.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ PAIEMENT ACQUITTÉ & VALIDÉ PAR OPÉRATEUR",
                        color = ObinssSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = ObinssGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Partager", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Fermer", color = Color.White, fontSize = 12.sp)
            }
        }
    )
}
