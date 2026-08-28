package com.applenotes.ai.core.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.AppleYellow

class MarkdownVisualTransformation(
    private val isDark: Boolean
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val annotated = buildAnnotatedString {
            append(raw)

            // Bold: **text**
            val boldRegex = Regex("""\*\*(.*?)\*\*""")
            boldRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Italic: *text* (excluding **)
            val italicRegex = Regex("""(?<!\*)\*([^*]+?)\*(?!\*)""")
            italicRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Strikethrough: ~~text~~
            val strikeRegex = Regex("""~~(.*?)~~""")
            strikeRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(textDecoration = TextDecoration.LineThrough),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Headers: # Header or ## Header or ### Header
            val headerRegex = Regex("""(?m)^(#+)\s+(.*)$""")
            headerRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = AppleYellow,
                        fontSize = 18.sp
                    ),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Checklists: - [ ] or - [x]
            val checkRegex = Regex("""(?m)^-\s+\[([ xX])\]\s+(.*)$""")
            checkRegex.findAll(raw).forEach { match ->
                val isChecked = match.groupValues[1].equals("x", ignoreCase = true)
                if (isChecked) {
                    addStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = if (isDark) Color.Gray else Color.DarkGray
                        ),
                        match.range.first,
                        match.range.last + 1
                    )
                } else {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Medium),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // Inline Code: `code`
            val codeRegex = Regex("""`([^`]+)`""")
            codeRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        color = if (isDark) Color(0xFFFF9F0A) else Color(0xFFD97706)
                    ),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}