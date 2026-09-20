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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary

data class OnboardingGoal(
    val id: String,
    val title: String,
    val icon: String,
    val description: String
)

val defaultGoals = listOf(
    OnboardingGoal("etudier", "Étudier", "📚", "Comprendre des cours, fiches & examens"),
    OnboardingGoal("creer", "Créer", "✍️", "Scripts TikTok, histoires & visuels"),
    OnboardingGoal("travailler", "Travailler", "💻", "Coder, rédiger des emails & documents"),
    OnboardingGoal("business", "Développer mon business", "💼", "Stratégie, vente, SWOT & PME"),
    OnboardingGoal("discuter", "Discuter", "💬", "Échanger librement avec l'IA")
)

@Composable
fun OnboardingDialog(
    onGoalSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ObinssRedSubtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(ObinssRed, Color(0xFF8B0000)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "O",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Bienvenue sur OBIN’SS IA 👋",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "« Que souhaitez-vous accomplir aujourd'hui ? »",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = ObinssGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    defaultGoals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                .clickable {
                                    onGoalSelected(goal.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("goal_${goal.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = goal.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = goal.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = goal.description,
                                    fontSize = 11.sp,
                                    color = ObinssTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
