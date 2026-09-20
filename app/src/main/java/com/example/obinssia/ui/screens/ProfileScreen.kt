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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    repository: ObinssRepository,
    onOpenPremium: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenAuth: () -> Unit,
    onViewInvoices: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val currentUsage by repository.currentUsage.collectAsState(initial = null)
    val profile by repository.getUserProfile().collectAsState(initial = null)
    val userSubscription by repository.getSubscriptionForCurrentUser().collectAsState(initial = null)
    val quotaConfig by repository.quotaConfig.collectAsState()

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    var isEditingGoal by remember { mutableStateOf(false) }
    var editedGoal by remember { mutableStateOf("") }

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    // Logout confirmation dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Déconnexion", fontWeight = FontWeight.Bold) },
            text = { Text("Souhaitez-vous vous déconnecter de votre compte OBIN’SS IA ? Vos conversations et données resteront sauvegardées en toute sécurité.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.logout()
                        showLogoutConfirm = false
                        Toast.makeText(context, "Vous êtes déconnecté.", Toast.LENGTH_SHORT).show()
                        onOpenAuth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed)
                ) {
                    Text("Se déconnecter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Goal edit dialog
    if (isEditingGoal) {
        Dialog(onDismissRequest = { isEditingGoal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Définir votre premier objectif", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedGoal,
                        onValueChange = { editedGoal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Créer et lancer mon projet avec OBIN’SS IA", fontSize = 12.sp) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { isEditingGoal = false }) {
                            Text("Annuler", color = ObinssTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.updateFirstGoal(editedGoal)
                                    isEditingGoal = false
                                    Toast.makeText(context, "Objectif mis à jour !", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ObinssRed)
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    val activeName = currentUser?.name ?: profile?.name ?: "Utilisateur OBIN’SS"
    val activeEmail = currentUser?.email ?: profile?.email ?: "user@obinssia.ai"
    val activePlan = currentUser?.plan ?: profile?.plan ?: "FREE"
    val activeRole = currentUser?.role ?: "USER"
    val createdAt = currentUser?.createdAt ?: System.currentTimeMillis()
    val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date(createdAt))

    val dailyCount = currentUsage?.dailyMessagesCount ?: 0
    val isPro = activePlan == "PRO"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Mon Compte & Profil",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Gérez votre identité, vos quotas et vos préférences",
                fontSize = 12.sp,
                color = ObinssTextSecondary
            )
        }

        // Profile Avatar and Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObinssRedSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(ObinssRed, Color(0xFF6A040F))))
                                .border(2.dp, if (isPro) ObinssGold else Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeName.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (isEditingName) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = editedName,
                                        onValueChange = { editedName = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                profile?.let {
                                                    repository.updateUserProfile(it.copy(name = editedName))
                                                }
                                                isEditingName = false
                                            }
                                        }
                                    ) {
                                        Text("✓", color = ObinssGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeRole == "ADMIN") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ObinssGold)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            editedName = activeName
                                            isEditingName = true
                                        },
                                        modifier = Modifier.size(24.dp).padding(start = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ObinssTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            Text(
                                text = activeEmail,
                                fontSize = 12.sp,
                                color = ObinssTextSecondary
                            )

                            Text(
                                text = "Membre depuis le $formattedDate",
                                fontSize = 10.sp,
                                color = ObinssTextMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Plan Badge
                            val planName = currentUser?.plan ?: profile?.plan ?: "FREE"
                            val isBusiness = planName.equals("BUSINESS", ignoreCase = true)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isBusiness) ObinssRed.copy(alpha = 0.2f) else if (isPro) ObinssGold.copy(alpha = 0.2f) else Color(0xFF2E3240))
                                        .clickable { onOpenPremium() }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isBusiness) "OBIN’SS IA BUSINESS 🏢" else if (isPro) "OBIN’SS IA PRO ⚡" else "Plan : GRATUIT (FREE) 🆓",
                                        color = if (isBusiness) ObinssRedLight else if (isPro) ObinssGold else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPro || isBusiness) "Gérer l'offre" else "Passer à PRO",
                                    fontSize = 11.sp,
                                    color = ObinssRedLight,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { onOpenPremium() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subscription & Quotas Card
        item {
            val planName = currentUser?.plan ?: profile?.plan ?: "FREE"
            val isBusiness = planName.equals("BUSINESS", ignoreCase = true)
            val isProOrBiz = isPro || isBusiness
            val dailyMax = if (isBusiness) 9999 else if (isPro) 500 else quotaConfig.freeDailyMessages
            val monthlyMax = if (isBusiness) 99999 else if (isPro) 10000 else quotaConfig.freeMonthlyMessages
            val monthlyCount = currentUsage?.monthlyMessagesCount ?: 0

            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_usage_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isProOrBiz) ObinssGold.copy(alpha = 0.3f) else ObinssRedSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⚡ Mon Abonnement & Quotas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isProOrBiz) ObinssGold else Color.White
                            )
                            userSubscription?.expiryDate?.let { exp ->
                                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp))
                                Text(
                                    text = "Valide jusqu'au $dateStr",
                                    fontSize = 10.sp,
                                    color = ObinssTextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isProOrBiz) ObinssSuccess.copy(alpha = 0.15f) else Color(0xFF262A38))
                                .border(1.dp, if (isProOrBiz) ObinssSuccess.copy(alpha = 0.4f) else Color(0xFF33384A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isProOrBiz) "ACTIF" else "GRATUIT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProOrBiz) ObinssSuccess else ObinssTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gauge 1: Daily Messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Messages aujourd'hui :", fontSize = 12.sp, color = ObinssTextSecondary)
                        Text(
                            text = if (isBusiness) "$dailyCount (Illimité)" else "$dailyCount / $dailyMax",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dailyCount >= dailyMax && !isProOrBiz) Color(0xFFFF5252) else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val dailyProgress = if (isBusiness) 0.05f else (dailyCount.toFloat() / dailyMax).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (dailyCount >= dailyMax && !isProOrBiz) Color(0xFFFF5252) else if (isProOrBiz) ObinssGold else ObinssRed,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gauge 2: Monthly Messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Consommation mensuelle :", fontSize = 12.sp, color = ObinssTextSecondary)
                        Text(
                            text = if (isBusiness) "$monthlyCount (Illimité)" else "$monthlyCount / $monthlyMax",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val monthlyProgress = if (isBusiness) 0.05f else (monthlyCount.toFloat() / monthlyMax).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { monthlyProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (isProOrBiz) ObinssGold else ObinssRed,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row (Changer d'offre & Mes factures)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenPremium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isProOrBiz) Color(0xFF282D3E) else ObinssRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(
                                text = if (isProOrBiz) "Modifier mon offre" else "Passer à PRO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onViewInvoices,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(
                                text = "Mes Factures",
                                fontSize = 12.sp,
                                color = ObinssGold
                            )
                        }
                    }
                }
            }
        }

        // First Goal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Premier objectif",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                editedGoal = currentUser?.selectedFirstGoal?.ifBlank { "Créer et lancer mon projet avec OBIN’SS IA" } ?: "Créer et lancer mon projet avec OBIN’SS IA"
                                isEditingGoal = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier l'objectif", tint = ObinssGold, modifier = Modifier.size(15.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentUser?.selectedFirstGoal.isNullOrBlank()) "Créer et lancer mon projet avec OBIN’SS IA" else currentUser!!.selectedFirstGoal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Metrics & Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Messages",
                    value = "${currentUsage?.monthlyMessagesCount ?: 18}",
                    subtitle = "échanges ce mois",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Documents",
                    value = "${currentUsage?.filesAnalyzedCount ?: 3}",
                    subtitle = "analysés",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Outils",
                    value = "${currentUsage?.toolsUsedCount ?: 5}",
                    subtitle = "utilisés",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // App Settings & Customization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "⚙️ Préférences du système",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dark / Light Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentUser?.isDarkMode != false) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = ObinssGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Thème de l'application", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = if (currentUser?.isDarkMode != false) "Mode Sombre (Dark)" else "Mode Clair (Light)",
                                    fontSize = 11.sp,
                                    color = ObinssTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = currentUser?.isDarkMode != false,
                            onCheckedChange = { isDark ->
                                scope.launch {
                                    repository.updateTheme(isDark)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObinssGold,
                                checkedTrackColor = ObinssRed,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("theme_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Langue de l'interface", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Français / Anglais", fontSize = 11.sp, color = ObinssTextSecondary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222634))
                                .clickable {
                                    scope.launch {
                                        val nextLang = if (currentUser?.language == "fr") "en" else "fr"
                                        repository.updateLanguage(nextLang)
                                        Toast.makeText(context, "Langue : ${nextLang.uppercase()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("language_switch_button")
                        ) {
                            Text(
                                text = (currentUser?.language ?: "fr").uppercase(),
                                color = ObinssGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speech speed
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ObinssRedLight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Vitesse de lecture vocale", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("${String.format("%.1f", speechRate)}x", color = ObinssGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = speechRate,
                            onValueChange = { speechRate = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = ObinssRed,
                                activeTrackColor = ObinssRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }

        // Actions & Administration
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Admin dashboard button only if Admin role
                if (activeRole == "ADMIN") {
                    Button(
                        onClick = onOpenAdmin,
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("profile_admin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ObinssGold.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tableau de bord Administrateur", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onOpenAuth,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("profile_auth_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObinssRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ObinssRedLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Changer de compte / Se connecter", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("profile_logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1517)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Déconnexion", color = Color(0xFFFF8080), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 11.sp, color = ObinssTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 19.sp, fontWeight = FontWeight.Black, color = ObinssGold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = ObinssTextMuted, maxLines = 1)
        }
    }
}
