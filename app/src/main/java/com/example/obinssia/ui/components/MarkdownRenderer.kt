package com.example.obinssia.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObinssCardBg
import com.example.ui.theme.ObinssGold
import com.example.ui.theme.ObinssRed
import com.example.ui.theme.ObinssRedLight
import com.example.ui.theme.ObinssRedSubtleBorder
import com.example.ui.theme.ObinssSuccess
import com.example.ui.theme.ObinssTextMuted
import com.example.ui.theme.ObinssTextSecondary

@Composable
fun MarkdownRenderer(
    text: String,
    modifier: Modifier = Modifier,
    isAssistant: Boolean = true
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    val isLongText = text.length > 750

    Column(modifier = modifier) {
        val blocks = remember(text) { parseMarkdownBlocks(text) }

        val displayedBlocks = if (isLongText && !isExpanded) blocks.take(4) else blocks

        displayedBlocks.forEach { block ->
            when (block) {
                is Block.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code,
                        onCopy = {
                            copyToClipboard(context, block.code, "Code copié !")
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                is Block.TableBlock -> {
                    TableBlockView(block)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                is Block.HeaderBlock -> {
                    HeaderView(block.level, block.text)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                is Block.ListItemBlock -> {
                    ListItemView(block.prefix, block.text)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is Block.QuoteBlock -> {
                    QuoteView(block.text)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                is Block.ParagraphBlock -> {
                    ParagraphView(block.text, isAssistant)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        if (isLongText) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ObinssRedLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isExpanded) "Réduire la réponse" else "Développer la réponse complète...",
                    color = ObinssRedLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1117))
            .border(1.dp, ObinssRedSubtleBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        // Code header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase().ifEmpty { "CODE" },
                color = ObinssGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        onCopy()
                        copied = true
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copier le code",
                    tint = if (copied) ObinssSuccess else ObinssTextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (copied) "Copié" else "Copier",
                    color = if (copied) ObinssSuccess else ObinssTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Code content with horizontal scrolling
        val scrollState = rememberScrollState()
        Text(
            text = code.trimEnd(),
            color = Color(0xFFE6EDF3),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(12.dp)
        )
    }
}

@Composable
private fun TableBlockView(table: Block.TableBlock) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF12141A))
            .border(1.dp, ObinssRedSubtleBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        // Headers
        Row(
            modifier = Modifier
                .background(Color(0xFF1E212B))
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            table.headers.forEach { header ->
                Text(
                    text = header.trim(),
                    color = ObinssGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .width(140.dp)
                        .padding(horizontal = 6.dp)
                )
            }
        }

        // Rows
        table.rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .background(if (index % 2 == 0) Color(0xFF141720) else Color(0xFF0F1117))
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell.trim(),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .width(140.dp)
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderView(level: Int, text: String) {
    val (fontSize, color) = when (level) {
        1 -> 20.sp to Color.White
        2 -> 17.sp to Color.White
        3 -> 15.sp to ObinssGold
        else -> 14.sp to ObinssRedLight
    }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ListItemView(prefix: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = prefix,
            color = ObinssRedLight,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatInlineMarkdown(text),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun QuoteView(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ObinssCardBg.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .background(ObinssGold)
                .padding(vertical = 12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontStyle = FontStyle.Italic,
            color = ObinssTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ParagraphView(text: String, isAssistant: Boolean) {
    Text(
        text = formatInlineMarkdown(text),
        color = if (isAssistant) MaterialTheme.colorScheme.onBackground else Color.White,
        fontSize = 14.sp,
        lineHeight = 22.sp
    )
}

private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                append(text.substring(i + 2, end))
                pop()
                i = end + 2
                continue
            }
        }
        if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF262936),
                        color = Color(0xFFFFB3B8)
                    )
                )
                append(" " + text.substring(i + 1, end) + " ")
                pop()
                i = end + 1
                continue
            }
        }
        if (text.startsWith("*", i)) {
            val end = text.indexOf("*", i + 1)
            if (end != -1) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                append(text.substring(i + 1, end))
                pop()
                i = end + 1
                continue
            }
        }
        append(text[i])
        i++
    }
}

private sealed class Block {
    data class ParagraphBlock(val text: String) : Block()
    data class HeaderBlock(val level: Int, val text: String) : Block()
    data class CodeBlock(val language: String, val code: String) : Block()
    data class ListItemBlock(val prefix: String, val text: String) : Block()
    data class QuoteBlock(val text: String) : Block()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : Block()
}

private fun parseMarkdownBlocks(content: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val lines = content.lines()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]

        // Code block starts
        if (line.trim().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            index++
            while (index < lines.size && !lines[index].trim().startsWith("```")) {
                codeBuilder.append(lines[index]).append("\n")
                index++
            }
            blocks.add(Block.CodeBlock(lang, codeBuilder.toString()))
            index++
            continue
        }

        // Table block starts (| ... |)
        if (line.trim().startsWith("|") && index + 1 < lines.size && lines[index + 1].contains("---")) {
            val headers = line.split("|").filter { it.isNotBlank() }
            index += 2 // skip header and separator
            val rows = mutableListOf<List<String>>()
            while (index < lines.size && lines[index].trim().startsWith("|")) {
                val cells = lines[index].split("|").filter { it.isNotBlank() }
                if (cells.isNotEmpty()) rows.add(cells)
                index++
            }
            blocks.add(Block.TableBlock(headers, rows))
            continue
        }

        // Headers (#, ##, ###)
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val title = line.removePrefix("#".repeat(level)).trim()
            blocks.add(Block.HeaderBlock(level, title))
            index++
            continue
        }

        // List item
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val itemText = line.trim().substring(2)
            blocks.add(Block.ListItemBlock("•", itemText))
            index++
            continue
        }

        // Numbered list
        val numberedMatch = Regex("^(\\d+\\.)\\s*(.*)").find(line.trim())
        if (numberedMatch != null) {
            val num = numberedMatch.groupValues[1]
            val text = numberedMatch.groupValues[2]
            blocks.add(Block.ListItemBlock(num, text))
            index++
            continue
        }

        // Blockquote
        if (line.trim().startsWith(">")) {
            blocks.add(Block.QuoteBlock(line.trim().removePrefix(">").trim()))
            index++
            continue
        }

        // Empty line
        if (line.isBlank()) {
            index++
            continue
        }

        // Paragraph
        blocks.add(Block.ParagraphBlock(line))
        index++
    }

    return blocks
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("OBINSS_CODE", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
