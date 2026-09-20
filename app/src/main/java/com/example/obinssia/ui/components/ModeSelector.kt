package com.example.obinssia.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.AiMode
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextSecondary

@Composable
fun ModeSelector(
    selectedMode: AiMode,
    onModeSelected: (AiMode) -> Unit,
    modifier: Modifier = Modifier,
    showSubtitles: Boolean = false
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AiMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val accentColor = Color(mode.accentColorHex)

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                label = "mode_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) accentColor else ObinssRedSubtleBorder.copy(alpha = 0.2f),
                label = "mode_border"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("mode_${mode.id}")
            ) {
                if (showSubtitles) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mode.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.title,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = mode.subtitle,
                            color = ObinssTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = mode.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.title.replace(Regex("^[^a-zA-ZÀ-ÿ0-9]+\\s*"), ""),
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onBackground,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
