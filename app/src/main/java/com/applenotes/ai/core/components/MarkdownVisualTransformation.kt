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
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Invisible zero-space style for hiding markdown syntax markers
        val hiddenSyntaxStyle = SpanStyle(
            color = Color.Transparent,
            fontSize = 0.001.sp,
            letterSpacing = (-0.5).sp
        )

        val annotated = buildAnnotatedString {
            append(raw)

            // ─── Headers: # H1, ## H2, ### H3 ──────────────────────────────
            val headerRegex = Regex("""(?m)^(#+)\s+(.*)$""")
            headerRegex.findAll(raw).forEach { match ->
                val hashSymbols = match.groups[1]
                val contentGroup = match.groups[2]

                if (hashSymbols != null && contentGroup != null) {
                    val level = hashSymbols.value.length
                    val headerSize = when (level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        else -> 17.sp
                    }
                    // Hide the leading '#' symbols and space
                    addStyle(hiddenSyntaxStyle, match.range.first, contentGroup.range.first)

                    // Style the header content
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = headerSize,
                            color = AppleYellow
                        ),
                        contentGroup.range.first,
                        contentGroup.range.last + 1
                    )
                }
            }

            // ─── Bold: **text** ──────────────────────────────────────────────
            val boldRegex = Regex("""\*\*(.+?)\*\*""")
            boldRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val innerStart = start + 2
                val innerEnd = end - 2

                if (innerStart < innerEnd) {
                    // Hide opening '**'
                    addStyle(hiddenSyntaxStyle, start, innerStart)
                    // Bold inner text
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), innerStart, innerEnd)
                    // Hide closing '**'
                    addStyle(hiddenSyntaxStyle, innerEnd, end)
                }
            }

            // ─── Italic: *text* (excluding bold **) ──────────────────────────
            val italicRegex = Regex("""(?<!\*)\*([^*]+?)\*(?!\*)""")
            italicRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val innerStart = start + 1
                val innerEnd = end - 1

                if (innerStart < innerEnd) {
                    // Hide opening '*'
                    addStyle(hiddenSyntaxStyle, start, innerStart)
                    // Italicize inner text
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), innerStart, innerEnd)
                    // Hide closing '*'
                    addStyle(hiddenSyntaxStyle, innerEnd, end)
                }
            }

            // ─── Strikethrough: ~~text~~ ─────────────────────────────────────
            val strikeRegex = Regex("""~~(.+?)~~""")
            strikeRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val innerStart = start + 2
                val innerEnd = end - 2

                if (innerStart < innerEnd) {
                    // Hide opening '~~'
                    addStyle(hiddenSyntaxStyle, start, innerStart)
                    // Strike inner text
                    addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), innerStart, innerEnd)
                    // Hide closing '~~'
                    addStyle(hiddenSyntaxStyle, innerEnd, end)
                }
            }

            // ─── Checklists: - [ ] or - [x] ──────────────────────────────────
            val checkRegex = Regex("""(?m)^-\s+\[([ xX])\]\s+(.*)$""")
            checkRegex.findAll(raw).forEach { match ->
                val isChecked = match.groupValues[1].equals("x", ignoreCase = true)
                val checkMarkerRange = match.groups[1]?.range
                val contentGroup = match.groups[2]

                // Style the marker
                if (checkMarkerRange != null) {
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) Color(0xFF34C759) else AppleYellow
                        ),
                        match.range.first,
                        (contentGroup?.range?.first ?: (match.range.last + 1))
                    )
                }

                if (contentGroup != null) {
                    if (isChecked) {
                        addStyle(
                            SpanStyle(
                                textDecoration = TextDecoration.LineThrough,
                                color = if (isDark) Color.Gray else Color.DarkGray
                            ),
                            contentGroup.range.first,
                            contentGroup.range.last + 1
                        )
                    } else {
                        addStyle(
                            SpanStyle(fontWeight = FontWeight.Normal),
                            contentGroup.range.first,
                            contentGroup.range.last + 1
                        )
                    }
                }
            }

            // ─── Inline Code: `code` ─────────────────────────────────────────
            val codeRegex = Regex("""`([^`]+)`""")
            codeRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val innerStart = start + 1
                val innerEnd = end - 1

                if (innerStart < innerEnd) {
                    // Hide opening '`'
                    addStyle(hiddenSyntaxStyle, start, innerStart)
                    // Monospace inner text with pill background
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                            color = if (isDark) Color(0xFFFF9F0A) else Color(0xFFD97706)
                        ),
                        innerStart,
                        innerEnd
                    )
                    // Hide closing '`'
                    addStyle(hiddenSyntaxStyle, innerEnd, end)
                }
            }

            // ─── Wiki Links: [[Note Title]] ──────────────────────────────────
            val wikiLinkRegex = Regex("""\[\[(.*?)\]\]""")
            wikiLinkRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val innerStart = start + 2
                val innerEnd = end - 2

                if (innerStart < innerEnd) {
                    // Hide opening '[['
                    addStyle(hiddenSyntaxStyle, start, innerStart)
                    // Highlight inner note title link with yellow underline
                    addStyle(
                        SpanStyle(
                            color = AppleYellow,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        ),
                        innerStart,
                        innerEnd
                    )
                    // Hide closing ']]'
                    addStyle(hiddenSyntaxStyle, innerEnd, end)
                }
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}