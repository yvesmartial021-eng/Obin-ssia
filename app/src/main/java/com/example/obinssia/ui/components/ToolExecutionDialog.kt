package com.example.obinssia.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.data.model.AiTool
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ToolExecutionDialog(
    tool: AiTool,
    repository: ObinssRepository,
    onOpenInChat: (prompt: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val fieldValues = remember {
        mutableStateMapOf<String, String>().apply {
            tool.fields.forEach { field ->
                put(field.id, field.defaultValue)
            }
        }
    }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = tool.icon, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tool.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tool.category.title,
                                fontSize = 11.sp,
                                color = ObinssGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = ObinssTextSecondary
                        )
                    }
                }

                Text(
                    text = tool.description,
                    fontSize = 12.sp,
                    color = ObinssTextSecondary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Dynamic Input fields
                tool.fields.forEach { field ->
                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        Text(
                            text = field.label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = fieldValues[field.id] ?: "",
                            onValueChange = { fieldValues[field.id] = it },
                            placeholder = { Text(field.placeholder, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = if (field.isMultiLine) 3 else 1,
                            maxLines = if (field.isMultiLine) 6 else 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObinssRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Generate Button
                Button(
                    onClick = {
                        val prompt = tool.promptTemplate(fieldValues)
                        isGenerating = true
                        scope.launch {
                            try {
                                val response = repository.sendMessage(
                                    conversationId = repository.createConversation(tool.title, AiMode.RAPIDE),
                                    userPrompt = prompt,
                                    mode = AiMode.RAPIDE
                                )
                                generatedResult = response
                            } catch (e: Exception) {
                                generatedResult = "Erreur de génération : ${e.localizedMessage}"
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("tool_execute_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OBIN’SS IA réfléchit…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Générer avec OBIN’SS IA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Output container
                generatedResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, ObinssGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ Résultat généré",
                                    color = ObinssGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("OBINSS_RESULT", result))
                                            Toast.makeText(context, "Résultat copié !", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = ObinssTextSecondary, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val prompt = tool.promptTemplate(fieldValues)
                                            onOpenInChat(prompt)
                                            onDismiss()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Ouvrir dans le chat", tint = ObinssRedLight, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            MarkdownRenderer(text = result, isAssistant = true)
                        }
                    }
                }
            }
        }
    }
}
