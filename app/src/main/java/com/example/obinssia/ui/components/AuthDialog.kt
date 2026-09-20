package com.example.obinssia.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary

@Composable
fun AuthDialog(
    onLoginSuccess: (name: String, email: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var usePhoneLogin by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRegisterMode) "Créer un compte" else "Connexion OBIN’SS IA",
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

                Text(
                    text = "Accédez à tous vos historiques, outils illimités et synchronisation.",
                    fontSize = 12.sp,
                    color = ObinssTextSecondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 16.dp)
                )

                // Social buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E212B))
                        .clickable {
                            onLoginSuccess("Utilisateur Google", "user.google@obinssia.ai")
                            onDismiss()
                        }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🌐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuer avec Google",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1877F2).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            onLoginSuccess("Utilisateur Facebook", "user.fb@obinssia.ai")
                            onDismiss()
                        }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📘", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuer avec Facebook",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    Text(
                        text = "  OU  ",
                        color = ObinssTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom complet") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObinssRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (usePhoneLogin) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Numéro de téléphone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ObinssGold) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObinssRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse e-mail") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ObinssGold) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObinssRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe sécurisé") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ObinssGold) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ObinssRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val displayName = if (name.isNotBlank()) name else if (email.isNotBlank()) email.substringBefore("@") else "Utilisateur OBIN'SS"
                        val finalEmail = if (email.isNotBlank()) email else "$phoneNumber@phone.obinssia.ai"
                        onLoginSuccess(displayName, finalEmail)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Créer mon compte" else "Se connecter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { usePhoneLogin = !usePhoneLogin }) {
                        Text(
                            text = if (usePhoneLogin) "Utiliser e-mail" else "Utiliser téléphone",
                            fontSize = 11.sp,
                            color = ObinssGold
                        )
                    }

                    TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                        Text(
                            text = if (isRegisterMode) "Déjà un compte ? Connexion" else "Nouveau ? S'inscrire",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
