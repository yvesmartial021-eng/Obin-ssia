package com.example.obinssia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssSurface
import com.example.ui.theme.ObinssTextSecondary

/**
 * Modal affiché lorsqu'un utilisateur FREE tente d'accéder à une fonctionnalité PRO
 * ou dépasse ses quotas (limite de messages, fichiers, modes IA).
 */
@Composable
fun ProFeatureGateDialog(
    featureTitle: String,
    reasonDescription: String,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObinssSurface,
        titleContentColor = Color.White,
        textContentColor = ObinssTextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
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
        },
        title = {
            Text(
                text = "Fonctionnalité Pro Dédiée",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = featureTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ObinssRedLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reasonDescription,
                    fontSize = 13.sp,
                    color = ObinssTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Encadré avantages PRO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObinssDeepBlack)
                        .border(1.dp, Color(0xFF2A2E3D), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "AVEC OBIN’SS IA PRO :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObinssGold
                        )
                        ProAdvantageRow(text = "500 messages / jour & 10 000 / mois")
                        ProAdvantageRow(text = "Tous les 6 modes IA (Expert, Code, Analyse...)")
                        ProAdvantageRow(text = "Analyse documentaire illimitée (50 Mo)")
                        ProAdvantageRow(text = "Priorité serveur haute vitesse")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onUpgradeClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_pro_gate_upgrade")
            ) {
                Text(
                    text = "Passer à PRO (Dès 4 900 FCFA)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continuer en version Gratuite",
                    color = ObinssTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun ProAdvantageRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ObinssGold,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}
