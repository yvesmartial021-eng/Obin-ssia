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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.obinssia.data.model.ConversationEntity
import com.example.obinssia.data.model.SavedDocumentEntity
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    repository: ObinssRepository,
    onSelectConversation: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val conversations by repository.getAllConversations().collectAsState(initial = emptyList())
    val documents by repository.getAllDocuments().collectAsState(initial = emptyList())

    var conversationToRename by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val filteredDocuments = remember(documents, searchQuery) {
        if (searchQuery.isBlank()) documents
        else documents.filter { it.fileName.contains(searchQuery, ignoreCase = true) || it.contentPreview.contains(searchQuery, ignoreCase = true) }
    }

    // Rename Dialog
    conversationToRename?.let { conv ->
        Dialog(onDismissRequest = { conversationToRename = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Renommer la conversation", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = renameTitleInput,
                        onValueChange = { renameTitleInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { conversationToRename = null }) {
                            Text("Annuler", color = ObinssTextSecondary)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.renameConversation(conv.id, renameTitleInput)
                                    conversationToRename = null
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

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Vider tout l'historique ?", fontWeight = FontWeight.Bold) },
            text = { Text("Cette action supprimera définitivement toutes vos conversations avec OBIN’SS IA.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.clearAllConversations()
                            showClearAllConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
                ) {
                    Text("Oui, tout supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historique & Fichiers",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Retrouvez vos échanges passés et documents sauvegardés",
                    fontSize = 12.sp,
                    color = ObinssTextSecondary
                )
            }

            if (selectedTab == 0 && conversations.isNotEmpty()) {
                IconButton(
                    onClick = { showClearAllConfirm = true },
                    modifier = Modifier.testTag("history_clear_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Vider l'historique",
                        tint = Color(0xFFFF5252).copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher dans vos historiques…", fontSize = 12.sp, color = ObinssTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ObinssGold) },
            modifier = Modifier.fillMaxWidth().testTag("history_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ObinssRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ObinssRed
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Conversations (${conversations.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) ObinssRedLight else ObinssTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Documents (${documents.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) ObinssRedLight else ObinssTextSecondary
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            if (filteredConversations.isEmpty()) {
                EmptyStateView("Aucune conversation trouvée", "Vos nouveaux chats s'afficheront ici automatiquement.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            onClick = { onSelectConversation(conv.id) },
                            onRename = {
                                renameTitleInput = conv.title
                                conversationToRename = conv
                            },
                            onTogglePin = {
                                scope.launch { repository.togglePin(conv.id, !conv.isPinned) }
                            },
                            onDelete = {
                                scope.launch { repository.deleteConversation(conv.id) }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        } else {
            if (filteredDocuments.isEmpty()) {
                EmptyStateView("Aucun document analysé", "Glissez ou importez un PDF ou fichier texte pour le voir ici.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDocuments, key = { it.id }) { doc ->
                        DocumentItem(
                            doc = doc,
                            onClick = { /* Preview or resume */ },
                            onDelete = {
                                scope.launch { repository.deleteDocument(doc) }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(conversation.updatedAt) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH)
        sdf.format(Date(conversation.updatedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (conversation.isPinned) ObinssGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                tint = if (conversation.isPinned) ObinssGold else ObinssRedLight,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (conversation.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.PushPin, contentDescription = "Épinglé", tint = ObinssGold, modifier = Modifier.size(13.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • Mode ${conversation.mode.replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    color = ObinssTextSecondary
                )
            }

            Row {
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Renommer",
                        tint = ObinssGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Épingler",
                        tint = if (conversation.isPinned) ObinssGold else ObinssTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    doc: SavedDocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(doc.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        sdf.format(Date(doc.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.fileName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = doc.contentPreview,
                    fontSize = 11.sp,
                    color = ObinssTextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${doc.fileSize} • $dateStr",
                    fontSize = 10.sp,
                    color = ObinssTextMuted
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer le document",
                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📁", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ObinssTextMuted,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
