package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

data class MessageChat(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            MessageChat(
                text = "Bonjour 👋 Je suis OBIN’SS IA. Comment puis-je vous aider ?",
                isUser = false
            )
        )
    }

    var userInput by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            userInput = initialPrompt
        }
    }

    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun sendMessage() {
        val question = userInput.trim()

        if (question.isBlank() || loading) return

        messages.add(
            MessageChat(
                text = question,
                isUser = true
            )
        )

        userInput = ""
        loading = true

        scope.launch {
            try {
                val history = messages
                    .filter { !it.isError }
                    .takeLast(20)

                val answer = GeminiApi.sendMessage(
                    context = context,
                    messages = history
                )

                messages.add(
                    MessageChat(
                        text = answer,
                        isUser = false
                    )
                )

            } catch (e: Exception) {

                messages.add(
                    MessageChat(
                        text = "Désolé, une erreur s'est produite lors de la communication avec l'IA.\n\n${e.message ?: "Erreur inconnue"}",
                        isUser = false,
                        isError = true
                    )
                )

            } finally {
                loading = false
            }
        }
    }

    fun clearChat() {
        messages.clear()

        messages.add(
            MessageChat(
                text = "Discussion réinitialisée. Que souhaitez-vous faire ?",
                isUser = false
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ChatInputBar(
                value = userInput,
                loading = loading,
                onValueChange = { userInput = it },
                onSend = { sendMessage() }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ChatTopBar(
                onClearChat = { clearChat() }
            )

            if (messages.size <= 1 && !loading) {
                QuickSuggestions(
                    onSuggestionSelected = {
                        userInput = it
                    }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = messages,
                    key = { it.id }
                ) { message ->

                    MessageBubble(
                        message = message,
                        context = context
                    )
                }

                if (loading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "OBIN’SS IA réfléchit…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = Color(0xFFE60076),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "OBIN’SS IA",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "OBIN’SS IA",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gemini • Actif",
                fontSize = 11.sp,
                color = Color(0xFF4DAF37)
            )
        }

        IconButton(
            onClick = onClearChat
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Effacer la discussion"
            )
        }
    }
}

@Composable
private fun QuickSuggestions(
    onSuggestionSelected: (String) -> Unit
) {
    val suggestions = listOf(
        "🤖 Explique-moi le Machine Learning simplement",
        "💻 Écris une fonction Kotlin pour valider un email",
        "✍️ Rédige un pitch de vente de 30 secondes",
        "📊 Quels sont les piliers d'une analyse SWOT réussie ?"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Text(
            text = "Suggestions pour démarrer :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        suggestions.forEach { suggestion ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onSuggestionSelected(suggestion)
                    }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text = suggestion,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageChat,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) {
            Alignment.End
        } else {
            Alignment.Start
        }
    ) {

        Text(
            text = if (message.isUser) "Vous" else "OBIN’SS IA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (message.isUser) {
                Color(0xFFE50914)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isError ->
                        Color(0xFF3F1818)

                    message.isUser ->
                        Color(0xFFE50914)

                    else ->
                        MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Text(
                    text = message.text,
                    color = if (message.isUser || message.isError) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 15.sp
                )

                if (!message.isUser && !message.isError) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(
                                        Context.CLIPBOARD_SERVICE
                                    ) as ClipboardManager

                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "OBIN’SS IA",
                                        message.text
                                    )
                                )

                                Toast.makeText(
                                    context,
                                    "Copié dans le presse-papier",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = {

                                val intent =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            message.text
                                        )
                                    }

                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Partager avec"
                                    )
                                )
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partager",
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    loading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = !loading,
            placeholder = {
                Text("Écrivez votre question…")
            },
            maxLines = 5,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors()
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !loading,
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = if (value.isNotBlank() && !loading) {
                        Color(0xFFE50914)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(16.dp)
                )
        ) {

            Icon(
                imageVector = if (loading) {
                    Icons.Default.Refresh
                } else {
                    Icons.Default.Send
                },
                contentDescription = "Envoyer",
                tint = Color.White
            )
        }
    }
}

object GeminiApi {

    private const val MODEL = "gemini-2.5-flash"

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    suspend fun sendMessage(
        context: Context,
        messages: List<MessageChat>
    ): String = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank()) {
            throw Exception(
                "Clé Gemini absente. Configure GEMINI_API_KEY dans le projet."
            )
        }

        val contents = JSONArray()

        messages
            .filter { !it.isError }
            .takeLast(20)
            .forEach { message ->

                val parts = JSONArray()

                parts.put(
                    JSONObject().apply {
                        put("text", message.text)
                    }
                )

                contents.put(
                    JSONObject().apply {
                        put(
                            "role",
                            if (message.isUser) "user" else "model"
                        )
                        put("parts", parts)
                    }
                )
            }

        val bodyJson = JSONObject().apply {
            put("contents", contents)
        }

        val mediaType =
            "application/json; charset=utf-8".toMediaType()

        val requestBody =
            bodyJson
                .toString()
                .toRequestBody(mediaType)

        val request =
            Request.Builder()
                .url(
                    "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"
                )
                .post(requestBody)
                .build()

        client.newCall(request).execute().use { response ->

            val responseText = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw Exception(
                    "Erreur Gemini ${response.code}: $responseText"
                )
            }

            val json = JSONObject(responseText)

            val candidates =
                json.optJSONArray("candidates")
                    ?: throw Exception(
                        "Aucune réponse de Gemini."
                    )

            if (candidates.length() == 0) {
                throw Exception(
                    "Gemini n'a retourné aucune réponse."
                )
            }

            val candidate =
                candidates.getJSONObject(0)

            val content =
                candidate.optJSONObject("content")
                    ?: throw Exception(
                        "Réponse Gemini invalide."
                    )

            val parts =
                content.optJSONArray("parts")
                    ?: throw Exception(
                        "Réponse Gemini vide."
                    )

            val result = StringBuilder()

            for (i in 0 until parts.length()) {

                val part = parts.optJSONObject(i)

                val text =
                    part?.optString("text").orEmpty()

                if (text.isNotBlank()) {
                    result.append(text)
                }
            }

            result.toString().trim().ifBlank {
                throw Exception(
                    "Gemini a retourné une réponse vide."
                )
            }
        }
    }
}
