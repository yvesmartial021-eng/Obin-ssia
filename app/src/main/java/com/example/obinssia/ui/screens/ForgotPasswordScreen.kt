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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.repository.ObinssRepository
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    repository: ObinssRepository,
    onNavigateBackToLogin: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun sendResetLink() {
        if (email.isBlank()) {
            errorMessage = "Veuillez renseigner votre adresse e-mail."
            return
        }
        isLoading = true
        errorMessage = null
        successMessage = null
        scope.launch {
            val result = repository.resetPassword(email)
            isLoading = false
            result.onSuccess { msg ->
                successMessage = msg
            }.onFailure { err ->
                errorMessage = err.message ?: "Une erreur est survenue."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Récupération de compte",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Saisissez votre e-mail pour réinitialiser l'accès à votre compte OBIN’SS IA.",
                    fontSize = 12.sp,
                    color = ObinssTextSecondary,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B151A))
                            .border(1.dp, ObinssRed, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFF8B8B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                successMessage?.let { success ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF132B1D))
                            .border(1.dp, ObinssSuccess, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ObinssSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = success,
                                color = Color(0xFFB4F8C8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Adresse e-mail du compte") },
                    placeholder = { Text("nom@exemple.ci") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ObinssGold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_password_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ObinssGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = { sendResetLink() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("forgot_password_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObinssRed),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Envoyer les instructions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Retour à la connexion",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObinssGold,
                        modifier = Modifier
                            .clickable { onNavigateBackToLogin() }
                            .testTag("forgot_password_back_btn")
                    )
                }
            }
        }
    }
}
