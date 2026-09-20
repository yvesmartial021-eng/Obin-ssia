package com.example.obinssia.ui.components

import android.speech.SpeechRecognizer
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ObinssDeepBlack
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary

@Composable
fun VoiceDialog(
    isListening: Boolean,
    transcribedText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isRecognitionSupported = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    var currentText by remember { mutableStateOf(transcribedText) }

    LaunchedEffect(transcribedText) {
        if (transcribedText.isNotEmpty()) {
            currentText = transcribedText
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎙️ OBIN’SS IA Vocal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = ObinssTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRecognitionSupported) {
                    // Warning when recognition not supported on device
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF332014))
                            .border(1.dp, ObinssGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ObinssGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reconnaissance vocale non disponible sur cet appareil / navigateur.",
                            fontSize = 12.sp,
                            color = ObinssGold,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // Pulsating Wave Animation
                    VoiceWaveVisualizer(isListening = isListening)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isListening) "OBIN’SS IA écoute attentivement…" else "Touchez le micro pour dicter",
                        color = if (isListening) ObinssRedLight else ObinssTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Transcribed content editable box
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    placeholder = {
                        Text(
                            text = if (isRecognitionSupported) "Parlez au micro ou modifiez le texte ici…"
                            else "Saisissez votre message vocal ici…",
                            fontSize = 13.sp,
                            color = ObinssTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("voice_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ObinssRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecognitionSupported) {
                        // Mic toggle
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Brush.radialGradient(listOf(ObinssRedLight, ObinssRed))
                                    else Brush.radialGradient(listOf(Color(0xFF2E3240), Color(0xFF1E212B)))
                                )
                                .clickable {
                                    if (isListening) onStopListening() else onStartListening()
                                }
                                .testTag("voice_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Arrêter" else "Parler",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Send Button (allows sending or modifying before sending)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentText.isNotBlank()) ObinssGold
                                else ObinssGold.copy(alpha = 0.4f)
                            )
                            .clickable(enabled = currentText.isNotBlank()) {
                                onSendText(currentText)
                                onDismiss()
                            }
                            .testTag("voice_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Envoyer",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceWaveVisualizer(isListening: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulse2)
                    .clip(CircleShape)
                    .background(ObinssRed.copy(alpha = 0.15f))
            )
            // Inner glow ring
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .scale(pulse1)
                    .clip(CircleShape)
                    .background(ObinssRed.copy(alpha = 0.3f))
            )
        }

        // Center mic badge
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(ObinssRedLight, ObinssRed, Color(0xFF6B000B))
                    )
                )
                .border(2.dp, ObinssGold.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
