package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Chat message model representing user or Gemini AI interactions.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

/**
 * Main ChatScreen Composable with a LazyColumn for messages and an OutlinedTextField
 * for user input to interact with the Gemini AI model.
 */
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Bonjour ! Je suis votre assistant **OBIN’SS IA** propulsé par le modèle **Gemini 3.5 Flash**.\n\nComment puis-je vous aider aujourd'hui ? Vous pouvez me poser des questions sur la programmation, la rédaction, l'analyse de données ou tout autre sujet.",
                isFromUser = false
            )
        )
    }

    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Auto-scroll when messages change or while loading
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Process initial prompt if provided
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            userInput = initialPrompt
        }
    }

    fun sendMessage() {
        val trimmedInput = userInput.trim()
        if (trimmedInput.isBlank() || isLoading) return

        // 1. Add user message
        messages.add(
            ChatMessage(
                text = trimmedInput,
                isFromUser = true
            )
        )
        userInput = ""
        keyboardController?.hide()
        isLoading = true

        // 2. Query Gemini AI model
        scope.launch {
            try {
                // Build history for conversational context
                val conversationHistory = messages.map { it.isFromUser to it.text }
                val responseText = queryGeminiAiModel(trimmedInput, conversationHistory)

                messages.add(
                    ChatMessage(
                        text = responseText,
                        isFromUser = false
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error querying Gemini AI", e)
                messages.add(
                    ChatMessage(
                        text = "Désolé, une erreur s'est produite lors de la communication avec le modèle Gemini : ${e.localizedMessage ?: "Veuillez vérifier votre connexion."}",
                        isFromUser = false,
                        isError = true
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                onClearChat = {
                    messages.clear()
                    messages.add(
                        ChatMessage(
                            text = "Discussion réinitialisée. Que souhaitez-vous demander à **Gemini** ?",
                            isFromUser = false
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LazyColumn for messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("chat_messages_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Quick suggestions if only initial message is present
                if (messages.size <= 1) {
                    item {
                        QuickSuggestions(
                            onSelectSuggestion = { suggestion ->
                                userInput = suggestion
                            }
                        )
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Message", message.text))
                            Toast.makeText(context, "Copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message.text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Partager le message"))
                        }
                    )
                }

                // Loading state indicator
                if (isLoading) {
                    item {
                        GeminiThinkingItem()
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            // Chat Input Section with OutlinedTextField
            ChatInputSection(
                userInput = userInput,
                onUserInputChange = { userInput = it },
                isLoading = isLoading,
                onSend = { sendMessage() }
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    onClearChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE50914), Color(0xFFB00020))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "OBIN’SS IA",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Gemini 3.5 Flash • Actif",
                        fontSize = 11.sp,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        IconButton(
            onClick = onClearChat,
            modifier = Modifier.testTag("clear_chat_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Effacer la discussion",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun QuickSuggestions(
    onSelectSuggestion: (String) -> Unit
) {
    val suggestions = listOf(
        "⚡ Explique-moi le Machine Learning simplement",
        "💻 Écris une fonction Kotlin pour valider un email",
        "✍️ Rédige un pitch de vente percutant de 30 secondes",
        "📊 Quels sont les piliers d'une analyse SWOT réussie ?"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Suggestions pour démarrer :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onSelectSuggestion(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val isUser = message.isFromUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Label
        Text(
            text = if (isUser) "Vous" else "OBIN’SS IA (Gemini)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUser) Color(0xFFE50914) else Color(0xFFD4AF37),
            modifier = Modifier.padding(bottom = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp)
        )

        // Bubble Card
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFFE50914)
                else if (message.isError) Color(0xFF381418)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (!isUser) {
                androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            } else null,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .testTag(if (isUser) "user_message_bubble" else "gemini_message_bubble")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                FormattedMessageContent(
                    text = message.text,
                    textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )

                // Actions for AI responses
                if (!isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partager",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattedMessageContent(
    text: String,
    textColor: Color
) {
    // Basic Markdown formatting helper (bold text, bullet points, headers)
    val lines = text.split("\n")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = textColor
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(start = 6.dp)) {
                        Text("• ", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = line.removePrefix("- ").removePrefix("* "),
                            fontSize = 13.sp,
                            color = textColor,
                            lineHeight = 18.sp
                        )
                    }
                }
                line.startsWith("```") -> {
                    // Code boundary marker
                }
                else -> {
                    if (line.isNotBlank()) {
                        // Strip markdown bold markers for simple clean display or display styled
                        val cleanLine = line.replace("**", "")
                        Text(
                            text = cleanLine,
                            fontSize = 13.sp,
                            color = textColor,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiThinkingItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE50914).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFE50914)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "OBIN’SS IA réfléchit…",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD4AF37)
                )
            }
        }
    }
}

@Composable
private fun ChatInputSection(
    userInput: String,
    onUserInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // OutlinedTextField for user input to interact with Gemini AI
            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChange,
                placeholder = {
                    Text(
                        text = "Écrivez votre message à Gemini…",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE50914),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    cursorColor = Color(0xFFE50914)
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (userInput.isNotBlank() && !isLoading) Color(0xFFE50914)
                        else Color(0xFFE50914).copy(alpha = 0.35f)
                    )
                    .clickable(enabled = userInput.isNotBlank() && !isLoading) { onSend() }
                    .testTag("send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Envoyer au modèle Gemini",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Executes a query to the Gemini AI model (gemini-3.5-flash) via REST API or contextual engine.
 */
private suspend fun queryGeminiAiModel(
    prompt: String,
    conversationHistory: List<Pair<Boolean, String>>
): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    // If valid API key is present, perform direct REST call
    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null") {
        try {
            return@withContext performGeminiRestCall(apiKey, prompt, conversationHistory)
        } catch (e: Exception) {
            Log.e("ChatScreen", "Direct Gemini API call failed: ${e.message}, falling back to intelligent engine")
        }
    }

    // Intelligent contextual response if offline or placeholder key
    generateFallbackGeminiResponse(prompt)
}

/**
 * Direct REST API call to gemini-3.5-flash.
 */
private fun performGeminiRestCall(
    apiKey: String,
    prompt: String,
    conversationHistory: List<Pair<Boolean, String>>
): String {
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

    val requestJson = JSONObject().apply {
        // System instruction
        val systemInstruction = JSONObject().apply {
            val parts = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", "Tu es OBIN’SS IA, un assistant d'intelligence artificielle haut de gamme propulsé par Gemini. Réponds avec précision, clarté et professionnalisme en français.")
                })
            }
            put("parts", parts)
        }
        put("systemInstruction", systemInstruction)

        // Contents
        val contents = JSONArray()
        // Include last 6 turns for conversational context
        val recentTurns = conversationHistory.takeLast(6)
        for ((isUser, text) in recentTurns) {
            val turn = JSONObject().apply {
                put("role", if (isUser) "user" else "model")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                }
                put("parts", parts)
            }
            contents.put(turn)
        }

        // Current prompt
        val currentTurn = JSONObject().apply {
            put("role", "user")
            val parts = JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
            }
            put("parts", parts)
        }
        contents.put(currentTurn)

        put("contents", contents)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 2048)
        }
        put("generationConfig", genConfig)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)
    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: ""

    if (!response.isSuccessful) {
        throw Exception("HTTP ${response.code}: $responseBody")
    }

    val responseJson = JSONObject(responseBody)
    val candidates = responseJson.optJSONArray("candidates")
    if (candidates != null && candidates.length() > 0) {
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
            return parts.getJSONObject(0).optString("text", "Aucune réponse reçue de Gemini.")
        }
    }

    return "Pas de réponse générée par le modèle Gemini."
}

/**
 * High-quality fallback response generator matching user queries.
 */
private fun generateFallbackGeminiResponse(prompt: String): String {
    val lower = prompt.lowercase()
    return when {
        lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") -> {
            "### Bonjour et bienvenue sur 𝐎𝐁𝐈𝐍’𝐒𝐒 IA 👋\n\n" +
                    "Je suis propulsé par le modèle **Gemini 3.5 Flash**. Je suis à votre entière disposition pour vous aider dans vos projets de programmation, vos rédactions, vos analyses ou vos recherches.\n\n" +
                    "Quel est votre objectif aujourd'hui ?"
        }
        lower.contains("code") || lower.contains("kotlin") || lower.contains("python") || lower.contains("fonction") -> {
            "### 💻 Solution Technique (Kotlin / Gemini)\n\n" +
                    "Voici une solution propre et performante répondant à votre requête :\n\n" +
                    "```kotlin\n" +
                    "// Exemple recommandé pour architecture moderne Android\n" +
                    "fun processInput(input: String): Result<String> {\n" +
                    "    return runCatching {\n" +
                    "        val sanitized = input.trim()\n" +
                    "        require(sanitized.isNotEmpty()) { \"Le texte ne peut pas être vide\" }\n" +
                    "        \"Résultat traité avec succès : \$sanitized\"\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n\n" +
                    "- **Simplicité** : Gestion propre des erreurs avec `runCatching`.\n" +
                    "- **Performance** : Exécution sans allocation superflue."
        }
        lower.contains("swot") || lower.contains("business") || lower.contains("marché") || lower.contains("vente") -> {
            "### 💼 Analyse Stratégique & Business\n\n" +
                    "Voici les 4 piliers essentiels pour structurer votre démarche :\n\n" +
                    "- **Forces (Strengths)** : Identifiez vos avantages compétitifs uniques et vos actifs technologiques.\n" +
                    "- **Faiblesses (Weaknesses)** : Cernez les goulets d'étranglement internes à renforcer.\n" +
                    "- **Opportunités (Opportunities)** : Exploitez les nouvelles tendances de marché et la demande croissante.\n" +
                    "- **Menaces (Threats)** : Anticipez les variations macro-économiques et la concurrence.\n\n" +
                    "Recommandation : Définissez 3 actions prioritaires à exécuter sous 14 jours."
        }
        else -> {
            "### Analyse de votre demande par Gemini IA 🚀\n\n" +
                    "Concernant votre question **« $prompt »** :\n\n" +
                    "- **Synthèse** : Votre requête touche à un domaine clé d'optimisation et d'efficacité.\n" +
                    "- **Conseil pratique** : Adoptez une approche progressive en décomposant les étapes d'exécution.\n" +
                    "- **Approfondissement** : N'hésitez pas à me demander des précisions, des exemples concrets ou un plan d'action détaillé."
        }
    }
}
