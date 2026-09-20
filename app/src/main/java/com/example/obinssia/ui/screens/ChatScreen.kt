package com.example.obinssia.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.data.model.ConversationEntity
import com.example.obinssia.data.model.MessageEntity
import com.example.obinssia.data.repository.ObinssRepository
import com.example.obinssia.service.SpeechService
import com.example.obinssia.ui.components.MarkdownRenderer
import com.example.obinssia.ui.components.ModeSelector
import com.example.ui.theme.ObinssCardBg
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    conversationId: String,
    repository: ObinssRepository,
    speechService: SpeechService,
    onNewChat: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenFileImport: () -> Unit,
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by repository.getMessagesForConversation(conversationId)
        .collectAsState(initial = emptyList())

    var conversation by remember { mutableStateOf<ConversationEntity?>(null) }
    var selectedMode by remember { mutableStateOf(AiMode.RAPIDE) }
    var inputPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitleInput by remember { mutableStateOf("") }

    var attachedFilePreview by remember { mutableStateOf<String?>(null) }

    // Load conversation metadata
    LaunchedEffect(conversationId) {
        val conv = repository.getConversationById(conversationId)
        conversation = conv
        if (conv != null) {
            selectedMode = AiMode.fromId(conv.mode)
            newTitleInput = conv.title
        }
    }

    // Auto-send initial prompt if provided (e.g. from Home or Tool runner)
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputPrompt = initialPrompt
        }
    }

    // Auto-scroll when messages change
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation?.title ?: "Chat OBIN’SS IA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Text(
                    text = "Mode : ${selectedMode.title}",
                    fontSize = 11.sp,
                    color = Color(selectedMode.accentColorHex),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // New Chat Button
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.testTag("chat_new_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nouveau chat",
                        tint = ObinssRedLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = ObinssTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Renommer la conversation", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ObinssGold) },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer la conversation", color = Color(0xFFFF5252)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    repository.deleteConversation(conversationId)
                                    onNewChat()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Mode Switcher strip
        ModeSelector(
            selectedMode = selectedMode,
            onModeSelected = { newMode ->
                selectedMode = newMode
            }
        )

        // Rename Dialog
        if (showRenameDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showRenameDialog = false }) {
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Renommer la conversation", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTitleInput,
                            onValueChange = { newTitleInput = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.material3.TextButton(onClick = { showRenameDialog = false }) {
                                Text("Annuler", color = ObinssTextSecondary)
                            }
                            androidx.compose.material3.Button(
                                onClick = {
                                    scope.launch {
                                        repository.renameConversation(conversationId, newTitleInput)
                                        conversation = repository.getConversationById(conversationId)
                                        showRenameDialog = false
                                    }
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ObinssRed)
                            ) {
                                Text("Enregistrer")
                            }
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OBINSS_MESSAGE", message.content))
                        Toast.makeText(context, "Réponse copiée !", Toast.LENGTH_SHORT).show()
                    },
                    onRegenerate = {
                        scope.launch {
                            isThinking = true
                            try {
                                repository.sendMessage(
                                    conversationId = conversationId,
                                    userPrompt = "Peux-tu régénérer et approfondir ta réponse précédente ?",
                                    mode = selectedMode
                                )
                            } finally {
                                isThinking = false
                            }
                        }
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "OBIN’SS IA :\n\n${message.content}\n\n— Généré par OBIN’SS IA (Votre intelligence. Votre assistant. Votre avenir.)")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Partager la réponse OBIN’SS IA"))
                    },
                    onLike = {
                        scope.launch {
                            val newStatus = if (message.isLiked == 1) 0 else 1
                            repository.updateMessageReaction(message.id, newStatus)
                        }
                    },
                    onDislike = {
                        scope.launch {
                            val newStatus = if (message.isLiked == -1) 0 else -1
                            repository.updateMessageReaction(message.id, newStatus)
                        }
                    },
                    onSpeak = {
                        speechService.speak(message.content, "fr")
                    }
                )
            }

            // "OBIN’SS IA réfléchit…" Animated Loading State
            if (isThinking) {
                item {
                    ThinkingIndicator()
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }

        // Attached File Preview Tag (if any)
        attachedFilePreview?.let { fileName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = ObinssRedLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = fileName, fontSize = 12.sp, color = ObinssGold, maxLines = 1)
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Retirer",
                    tint = ObinssTextSecondary,
                    modifier = Modifier.size(16.dp).clickable { attachedFilePreview = null }
                )
            }
        }

        // Chat Input Box
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
                // Attach button
                IconButton(
                    onClick = {
                        onOpenFileImport()
                        attachedFilePreview = "Document_Attache.pdf"
                    },
                    modifier = Modifier.size(36.dp).testTag("chat_attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attacher un document",
                        tint = ObinssRedLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text field
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = {
                        Text(
                            text = "Demandez à OBIN’SS IA…",
                            fontSize = 13.sp,
                            color = ObinssTextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ObinssRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputPrompt.isNotBlank() && !isThinking) {
                                val promptToSend = inputPrompt
                                val attached = attachedFilePreview
                                inputPrompt = ""
                                attachedFilePreview = null
                                isThinking = true

                                scope.launch {
                                    try {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            userPrompt = promptToSend,
                                            mode = selectedMode,
                                            attachedFileName = attached
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erreur : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isThinking = false
                                    }
                                }
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Mic button
                IconButton(
                    onClick = onOpenVoice,
                    modifier = Modifier.size(36.dp).testTag("chat_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Vocal",
                        tint = ObinssGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputPrompt.isNotBlank() && !isThinking) ObinssRed
                            else ObinssRed.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = inputPrompt.isNotBlank() && !isThinking) {
                            val promptToSend = inputPrompt
                            val attached = attachedFilePreview
                            inputPrompt = ""
                            attachedFilePreview = null
                            isThinking = true

                            scope.launch {
                                try {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        userPrompt = promptToSend,
                                        mode = selectedMode,
                                        attachedFileName = attached
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isThinking = false
                                }
                            }
                        }
                        .testTag("chat_send_button"),
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
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onShare: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObinssRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text("O", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "𝐎𝐁𝐈𝐍’𝐒𝐒 IA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ObinssRedLight
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 1f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) Color(0xFF221518)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    1.dp,
                    if (isUser) ObinssRed.copy(alpha = 0.4f)
                    else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                if (message.attachedFileName != null) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E241E))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(message.attachedFileName, fontSize = 11.sp, color = ObinssGold)
                    }
                }

                MarkdownRenderer(
                    text = message.content,
                    isAssistant = !isUser
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time display
                val timeFormatted = remember(message.timestamp) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.format(Date(message.timestamp))
                }
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = if (isUser) ObinssGold.copy(alpha = 0.7f) else ObinssTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Actions toolbar for Assistant messages
        if (!isUser) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = ObinssTextSecondary, modifier = Modifier.size(15.dp))
                }

                // Regenerate
                IconButton(onClick = onRegenerate, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Régénérer", tint = ObinssTextSecondary, modifier = Modifier.size(16.dp))
                }

                // Share
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Partager", tint = ObinssTextSecondary, modifier = Modifier.size(15.dp))
                }

                // Speak / TTS
                IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Écouter", tint = ObinssGold, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Like
                IconButton(onClick = onLike, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "J'aime",
                        tint = if (message.isLiked == 1) ObinssSuccess else ObinssTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Dislike
                IconButton(onClick = onDislike, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Je n'aime pas",
                        tint = if (message.isLiked == -1) ObinssRed else ObinssTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "spin_trans")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, ObinssRedSubtleBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation)
                .border(2.dp, ObinssRed, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "OBIN’SS IA réfléchit…",
            color = ObinssRedLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
