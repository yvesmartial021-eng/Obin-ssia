package com.example.obinssia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obinssia.data.model.AiTool
import com.example.obinssia.data.model.AiToolsCatalog
import com.example.obinssia.data.model.ToolCategory
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary

@Composable
fun ToolsScreen(
    onSelectTool: (AiTool) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val filteredTools = remember(searchQuery, selectedCategory) {
        AiToolsCatalog.allTools.filter { tool ->
            val matchesCategory = selectedCategory == null || tool.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    tool.title.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true) ||
                    tool.category.title.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Outils Spécialisés IA",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Générateurs intégrés avec formulaires guidés",
                    fontSize = 12.sp,
                    color = ObinssTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObinssRed.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${filteredTools.size} outils",
                    color = ObinssRedLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher un générateur (TikTok, email, code…)", fontSize = 12.sp, color = ObinssTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ObinssGold) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tools_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ObinssRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pills
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All pill
            CategoryPill(
                title = "Tous les outils",
                isSelected = selectedCategory == null,
                onClick = { selectedCategory = null }
            )

            ToolCategory.entries.forEach { cat ->
                CategoryPill(
                    title = cat.title,
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tools List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = { onSelectTool(tool) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ObinssRed else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) ObinssGold else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else ObinssTextSecondary
        )
    }
}

@Composable
fun ToolCard(
    tool: AiTool,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, ObinssRedSubtleBorder.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("tool_card_${tool.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = tool.icon, fontSize = 28.sp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tool.description,
                    fontSize = 12.sp,
                    color = ObinssTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.category.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObinssGold
                    )
                    Text(
                        text = " • ${tool.fields.size} champs configurables",
                        fontSize = 10.sp,
                        color = ObinssTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObinssRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ouvrir",
                    tint = ObinssRedLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
