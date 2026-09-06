package com.applenotes.ai.presentation.note_editor.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.applenotes.ai.core.theme.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.*
import com.applenotes.ai.core.components.AppleSegmentedControl
import com.applenotes.ai.core.components.SegmentItem

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownPreviewBottomSheet(
    title: String,
    content: String,
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight

    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val segmentItems = remember {
        listOf(
            SegmentItem("KaTeX & Şemalar", Icons.Default.AutoAwesome),
            SegmentItem("Katlanabilir Başlıklar", Icons.Default.ViewAgenda)
        )
    }

    val htmlContent = remember(title, content, isDark) {
        buildHtmlPreview(title, content, isDark)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Markdown Önizleme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AppleSegmentedControl(
                items = segmentItems,
                selectedIndex = selectedTab,
                onIndexSelected = { selectedTab = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(
                color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedTab == 0) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            setBackgroundColor(0) // Transparent
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL("https://applenotes.local", htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL("https://applenotes.local", htmlContent, "text/html", "UTF-8", null)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    CollapsibleMarkdownView(
                        markdown = if (title.isNotBlank()) "# $title\n\n$content" else content
                    )
                }
            }
        }
    }
}

private fun buildHtmlPreview(title: String, content: String, isDark: Boolean): String {
    val bgColor = if (isDark) "#1C1C1E" else "#FFFFFF"
    val textColor = if (isDark) "#FFFFFF" else "#000000"
    val secondaryColor = if (isDark) "#8E8E93" else "#6C6C70"
    val accentColor = "#E5A93C"
    val codeBg = if (isDark) "#2C2C2E" else "#F2F2F7"
    val mermaidTheme = if (isDark) "dark" else "default"

    // Escape backticks and backslashes for JS string template
    val safeRawContent = content
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")

    val safeTitle = title.replace("<", "&lt;").replace(">", "&gt;")

    return """
    <!DOCTYPE html>
    <html lang="tr">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
        
        <!-- KaTeX Math CSS & JS -->
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
        <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
        <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
        
        <!-- Marked (Markdown parser) -->
        <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
        
        <!-- Mermaid.js (Diagrams & Mindmaps) -->
        <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>

        <style>
            * { box-sizing: border-box; }
            body {
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                background-color: $bgColor;
                color: $textColor;
                padding: 12px 14px 40px 14px;
                line-height: 1.6;
                margin: 0;
            }
            h1.note-title {
                color: $accentColor;
                font-size: 1.6em;
                margin-top: 0;
                margin-bottom: 12px;
                font-weight: 700;
                border-bottom: 1px solid ${if (isDark) "#2C2C2E" else "#E5E5EA"};
                padding-bottom: 8px;
            }
            h1, h2, h3, h4 { color: $textColor; margin-top: 20px; margin-bottom: 8px; font-weight: 600; }
            h1 { font-size: 1.4em; }
            h2 { font-size: 1.25em; }
            h3 { font-size: 1.1em; }
            p { margin: 8px 0; }
            .collapsible-heading {
                cursor: pointer;
                user-select: none;
                display: flex;
                align-items: center;
                transition: opacity 0.2s ease;
            }
            .collapsible-heading:active { opacity: 0.7; }
            .toggle-icon {
                font-size: 0.65em;
                margin-right: 8px;
                color: $accentColor;
                display: inline-block;
                width: 14px;
            }
            code {
                background: $codeBg;
                color: $accentColor;
                padding: 2px 6px;
                border-radius: 4px;
                font-size: 0.9em;
                font-family: Menlo, Monaco, Consolas, monospace;
            }
            pre {
                background: $codeBg;
                padding: 12px;
                border-radius: 8px;
                overflow-x: auto;
            }
            pre code { padding: 0; background: none; color: inherit; }
            blockquote {
                border-left: 3px solid $accentColor;
                margin: 12px 0;
                padding-left: 12px;
                color: $secondaryColor;
                font-style: italic;
            }
            .mermaid {
                text-align: center;
                margin: 18px 0;
                background: ${if (isDark) "#252528" else "#F9F9FB"};
                padding: 14px;
                border-radius: 10px;
                overflow-x: auto;
            }
            ul, ol { padding-left: 20px; }
            li { margin: 4px 0; }
            input[type="checkbox"] { margin-right: 6px; }
            .katex-display { margin: 14px 0; overflow-x: auto; overflow-y: hidden; }
        </style>
    </head>
    <body>
        ${if (safeTitle.isNotBlank()) "<h1 class=\"note-title\">$safeTitle</h1>" else ""}
        <div id="render-target"></div>

        <script>
            document.addEventListener("DOMContentLoaded", function() {
                mermaid.initialize({ startOnLoad: false, theme: "$mermaidTheme" });

                const rawMarkdown = `$safeRawContent`;
                
                // Parse markdown
                marked.setOptions({ gfm: true, breaks: true });
                const parsedHtml = marked.parse(rawMarkdown);

                const target = document.getElementById("render-target");
                target.innerHTML = parsedHtml;

                // Collapsible Headings
                const headings = target.querySelectorAll("h1, h2, h3");
                headings.forEach(h => {
                    if (h.classList.contains("note-title")) return;
                    h.classList.add("collapsible-heading");
                    const iconSpan = document.createElement("span");
                    iconSpan.className = "toggle-icon";
                    iconSpan.textContent = "▼";
                    h.insertBefore(iconSpan, h.firstChild);

                    h.addEventListener("click", function() {
                        const icon = this.querySelector(".toggle-icon");
                        let next = this.nextElementSibling;
                        const myLevel = parseInt(this.tagName.substring(1));
                        const isCollapsed = this.classList.toggle("collapsed");
                        if (icon) icon.textContent = isCollapsed ? "▶" : "▼";
                        while (next) {
                            if (/^H[1-6]$/.test(next.tagName)) {
                                const nextLevel = parseInt(next.tagName.substring(1));
                                if (nextLevel <= myLevel) break;
                            }
                            next.style.display = isCollapsed ? "none" : "";
                            next = next.nextElementSibling;
                        }
                    });
                });

                // Transform ```mermaid blocks into <div class="mermaid">
                const codeBlocks = target.querySelectorAll("pre code.language-mermaid");
                codeBlocks.forEach(block => {
                    const pre = block.parentElement;
                    const div = document.createElement("div");
                    div.className = "mermaid";
                    div.textContent = block.textContent;
                    pre.parentElement.replaceChild(div, pre);
                });

                // Render KaTeX Math
                if (typeof renderMathInElement !== "undefined") {
                    renderMathInElement(target, {
                        delimiters: [
                            {left: '$$', right: '$$', display: true},
                            {left: '$', right: '$', display: false},
                            {left: '\\(', right: '\\)', display: false},
                            {left: '\\[', right: '\\]', display: true}
                        ],
                        throwOnError: false
                    });
                }

                // Render Mermaid Diagrams
                try {
                    mermaid.run({ querySelector: '.mermaid' });
                } catch(e) {
                    console.error("Mermaid render error: ", e);
                }
            });
        </script>
    </body>
    </html>
    """.trimIndent()
}
