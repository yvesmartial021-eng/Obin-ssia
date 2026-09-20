package com.example.obinssia.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.ui.components.BrandHeader
import com.example.obinssia.ui.components.ModeSelector
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary

data class HomeSuggestion(
    val id: String,
    val icon: String,
    val title: String,
    val prompt: String,
    val targetMode: AiMode
)

val defaultSuggestions = listOf(
    HomeSuggestion("1", "💼", "Créer un business plan", "Aide-moi à concevoir un business plan complet pour mon projet avec modèle économique et stratégie de lancement.", AiMode.BUSINESS),
    HomeSuggestion("2", "🎬", "Rédiger un script", "Rédige un script captivant pour une vidéo courte avec une accroche percutante et un appel à l'action.", AiMode.CREATEUR),
    HomeSuggestion("3", "📚", "Expliquer un concept", "Explique-moi ce concept pas à pas avec des exemples concrets et une méthode pédagogique simple.", AiMode.ETUDE),
    HomeSuggestion("4", "💻", "Corriger un code", "Analyse et corrige ce code pour éliminer les bugs, sécuriser la logique et optimiser les performances.", AiMode.CODE),
    HomeSuggestion("5", "✍️", "Écrire un texte", "Rédige un article professionnel et captivant sur l'intelligence artificielle.", AiMode.RAPIDE),
    HomeSuggestion("6", "📊", "Analyser un document", "Je souhaite analyser un document : résume les points clés et fais ressortir les recommandations.", AiMode.EXPERT)
)

@Composable
fun HomeScreen(
    selectedMode: AiMode,
    onModeSelected: (AiMode) -> Unit,
    onStartChat: (prompt: String, mode: AiMode) -> Unit,
    onOpenVoice: () -> Unit,
    onOpenFileImport: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAdmin: () -> Unit,
    userPlan: String = "FREE",
    selectedGoal: String = "",
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {}
) {
    var promptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        BrandHeader(
            userPlan = userPlan,
            unreadNotificationsCount = unreadNotificationsCount,
            onPremiumClick = onOpenPremium,
            onAdminClick = onOpenAdmin,
            onNotificationsClick = onNotificationsClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Central hero question
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Que puis-je faire pour vous aujourd'hui ?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Votre assistant d'intelligence artificielle tout-en-un.",
                        fontSize = 13.sp,
                        color = ObinssTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Mode Selector
            item {
                Text(
                    text = "Mode intelligent :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ObinssGold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                ModeSelector(
                    selectedMode = selectedMode,
                    onModeSelected = onModeSelected
                )
            }

            // Central input bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, ObinssRedSubtleBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenFileImport,
                            modifier = Modifier.testTag("home_attach_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Joindre un fichier",
                                tint = ObinssRedLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = "Demandez n'importe quoi à OBIN’SS IA…",
                                    fontSize = 13.sp,
                                    color = ObinssTextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_prompt_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (promptInput.isNotBlank()) {
                                        onStartChat(promptInput, selectedMode)
                                    }
                                }
                            )
                        )

                        IconButton(
                            onClick = onOpenVoice,
                            modifier = Modifier.testTag("home_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = ObinssGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (promptInput.isNotBlank()) ObinssRed else ObinssRed.copy(alpha = 0.5f))
                                .clickable(enabled = promptInput.isNotBlank()) {
                                    onStartChat(promptInput, selectedMode)
                                }
                                .testTag("home_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Envoyer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Suggestions header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggestions rapides",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (selectedGoal.isNotEmpty()) {
                        Text(
                            text = "Profil : ${selectedGoal.replaceFirstChar { it.uppercase() }}",
                            color = ObinssGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Suggestions cards (2 per row)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = defaultSuggestions.chunked(2)
                    chunked.forEach { rowSuggestions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowSuggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                        .clickable {
                                            onModeSelected(suggestion.targetMode)
                                            onStartChat(suggestion.prompt, suggestion.targetMode)
                                        }
                                        .padding(14.dp)
                                        .testTag("suggestion_${suggestion.id}")
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = suggestion.icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = suggestion.targetMode.title.substringBefore(" "),
                                                color = Color(suggestion.targetMode.accentColorHex),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = suggestion.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = suggestion.prompt,
                                            color = ObinssTextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                            if (rowSuggestions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
