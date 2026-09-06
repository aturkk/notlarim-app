package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import kotlin.math.*

private data class GraphNode(
    val note: Note,
    val initialX: Float,
    val initialY: Float,
    val radius: Float = 28f
)

private data class GraphEdge(
    val sourceId: Long,
    val targetId: Long,
    val isDirectLink: Boolean // direct [[...]] link or shared tag
)

@Composable
fun GraphViewDialog(
    notes: List<Note>,
    onDismiss: () -> Unit,
    onNoteClick: (Long) -> Unit
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textMeasurer = rememberTextMeasurer()

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Build Nodes and Edges
    val (nodes, edges) = remember(notes) {
        val nodeCount = notes.size
        val radius = max(240f, nodeCount * 36f)
        val center = Offset(600f, 600f)

        val nodeList = notes.mapIndexed { index, note ->
            val angle = 2 * PI * index / max(1, nodeCount)
            val jitter = ((note.id % 5) - 2) * 35f
            val x = center.x + (radius + jitter) * cos(angle).toFloat()
            val y = center.y + (radius + jitter) * sin(angle).toFloat()
            GraphNode(note = note, initialX = x, initialY = y)
        }

        val nodeMap = notes.associateBy { it.id }
        val titleMap = notes.associateBy { it.title.trim().lowercase() }

        val edgeList = mutableListOf<GraphEdge>()

        // 1. Direct [[...]] Backlinks
        val linkRegex = Regex("\\[\\[(.*?)\\]\\]")
        notes.forEach { note ->
            val matches = linkRegex.findAll(note.content)
            for (m in matches) {
                val targetTitle = m.groupValues[1].trim().lowercase()
                val targetNote = titleMap[targetTitle]
                if (targetNote != null && targetNote.id != note.id) {
                    edgeList.add(GraphEdge(sourceId = note.id, targetId = targetNote.id, isDirectLink = true))
                }
            }
        }

        // 2. Shared tags connections
        for (i in 0 until notes.size) {
            for (j in i + 1 until notes.size) {
                val n1 = notes[i]
                val n2 = notes[j]
                val sharedTags = n1.tags.intersect(n2.tags.toSet())
                if (sharedTags.isNotEmpty()) {
                    edgeList.add(GraphEdge(sourceId = n1.id, targetId = n2.id, isDirectLink = false))
                }
            }
        }

        nodeList to edgeList
    }

    val nodePositionMap = remember(nodes) {
        nodes.associate { it.note.id to Offset(it.initialX, it.initialY) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🕸️ Bilgi Ağı Görünümü (Graph)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${notes.size} Not · ${edges.size} Bağlantı",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            // Interactive Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 3.5f)
                            panOffset += pan
                        }
                    }
                    .pointerInput(nodes, scale, panOffset) {
                        detectTapGestures { tapOffset ->
                            // Find tapped node
                            val transformedTap = (tapOffset - panOffset) / scale
                            val tappedNode = nodes.find { node ->
                                val pos = Offset(node.initialX, node.initialY)
                                (pos - transformedTap).getDistance() <= (node.radius + 18f)
                            }
                            selectedNote = tappedNode?.note
                        }
                    }
            ) {
                val edgeColorDirect = AppleYellow
                val edgeColorTag = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                val nodeColor = AppleYellow
                val nodeTextPaint = TextStyle(
                    color = if (isDark) Color.White else Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                // Draw Edges
                edges.forEach { edge ->
                    val p1 = nodePositionMap[edge.sourceId]
                    val p2 = nodePositionMap[edge.targetId]
                    if (p1 != null && p2 != null) {
                        val screenP1 = (p1 * scale) + panOffset
                        val screenP2 = (p2 * scale) + panOffset
                        val isHighlighted = selectedNote?.id == edge.sourceId || selectedNote?.id == edge.targetId

                        drawLine(
                            color = if (isHighlighted) AppleYellow else if (edge.isDirectLink) edgeColorDirect.copy(alpha = 0.6f) else edgeColorTag,
                            start = screenP1,
                            end = screenP2,
                            strokeWidth = if (isHighlighted) 3.5f * scale else if (edge.isDirectLink) 2f * scale else 1f * scale
                        )
                    }
                }

                // Draw Nodes
                nodes.forEach { node ->
                    val pos = Offset(node.initialX, node.initialY)
                    val screenPos = (pos * scale) + panOffset
                    val isSelected = selectedNote?.id == node.note.id
                    val nodeRadius = if (isSelected) (node.radius + 8f) * scale else node.radius * scale

                    // Outer glow / circle
                    drawCircle(
                        color = if (isSelected) AppleYellow.copy(alpha = 0.35f) else AppleYellow.copy(alpha = 0.12f),
                        radius = nodeRadius + (6f * scale),
                        center = screenPos
                    )
                    drawCircle(
                        color = if (isSelected) AppleYellow else AppleYellowDark,
                        radius = nodeRadius,
                        center = screenPos
                    )
                    drawCircle(
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.White,
                        radius = nodeRadius,
                        center = screenPos,
                        style = Stroke(width = if (isSelected) 2.5f * scale else 1.5f * scale)
                    )

                    // Draw Node label (Title)
                    val displayLabel = (node.note.icon?.let { "$it " } ?: "") +
                            node.note.title.ifBlank { "Not #${node.note.id}" }.take(14)
                    val textLayout = textMeasurer.measure(
                        text = displayLabel,
                        style = nodeTextPaint.copy(fontSize = (10f * scale).coerceIn(8f, 15f).sp)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = displayLabel,
                        topLeft = Offset(
                            screenPos.x - (textLayout.size.width / 2f),
                            screenPos.y + nodeRadius + 4f
                        ),
                        style = nodeTextPaint.copy(fontSize = (10f * scale).coerceIn(8f, 15f).sp)
                    )
                }
            }

            // Bottom Selected Note Card
            selectedNote?.let { note ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                note.icon?.let { icon ->
                                    Text(text = icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = note.title.ifBlank { "Başlıksız Not" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            if (note.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.tags.joinToString(" ") { "#$it" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleYellowDark,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onNoteClick(note.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aç", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
