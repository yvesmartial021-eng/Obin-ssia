package com.example.obinssia.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.obinssia.data.model.AiMode
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

@Composable
fun FileImportDialog(
    repository: ObinssRepository,
    onDocumentAnalyzed: (prompt: String, docName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFileName by remember { mutableStateOf("Rapport_Strategique_2026.pdf") }
    var selectedFileType by remember { mutableStateOf("application/pdf") }
    var selectedFileSize by remember { mutableStateOf("2.1 Mo") }
    var customQuestion by remember { mutableStateOf("Résume et extrait les 5 points clés de ce document.") }
    var isAnalyzing by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Document_Importe.pdf"
            selectedFileName = name
            selectedFileType = "document/*"
            selectedFileSize = "1.8 Mo"
            Toast.makeText(context, "Fichier sélectionné : $name", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = ObinssRedLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Importation & Analyse",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = ObinssTextSecondary)
                    }
                }

                Text(
                    text = "Glissez-déposez ou sélectionnez vos PDF, Word, TXT, images ou codes.",
                    fontSize = 12.sp,
                    color = ObinssTextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Drag and drop / upload box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, ObinssRedSubtleBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            try {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                selectedFileName = "Nouveau_Document_Scanne.pdf"
                            }
                        }
                        .padding(20.dp)
                        .testTag("file_picker_dropzone"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = ObinssRedLight,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedFileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$selectedFileSize • Cliquez pour changer de fichier",
                            fontSize = 11.sp,
                            color = ObinssGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick tasks pills
                Text(
                    text = "Actions rapides :",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Résumer" to "Résume ce document de façon synthétique.",
                        "Expliquer" to "Explique les concepts clés de ce document simplement.",
                        "Extraire" to "Extrait toutes les données chiffrées et recommandations."
                    ).forEach { (label, prompt) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF20232E))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .clickable { customQuestion = prompt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ObinssGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = { customQuestion = it },
                    label = { Text("Votre consigne d'analyse") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ObinssRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isAnalyzing = true
                        scope.launch {
                            try {
                                repository.saveDocument(
                                    fileName = selectedFileName,
                                    fileType = selectedFileType,
                                    fileSize = selectedFileSize,
                                    preview = "Analyse demandée : $customQuestion",
                                    summary = "Document analysé par OBIN’SS IA"
                                )
                                onDocumentAnalyzed(customQuestion, selectedFileName)
                                onDismiss()
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    },
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("analyze_document_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyse en cours...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lancer l'analyse avec OBIN’SS IA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
