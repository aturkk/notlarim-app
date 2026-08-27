package com.applenotes.ai.core.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.applenotes.ai.core.theme.*
import java.io.File
import java.io.FileOutputStream

data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false,
    val points: List<Offset> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleDrawingDialog(
    onDismiss: () -> Unit,
    onSaveDrawing: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val paths = remember { mutableStateListOf<DrawPath>() }
    val undonePaths = remember { mutableStateListOf<DrawPath>() }

    var currentColor by remember { mutableStateOf(if (isDark) Color.White else Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }
    var isHighlighter by remember { mutableStateOf(false) }

    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<MutableList<Offset>>(mutableListOf()) }

    val colors = listOf(
        if (isDark) Color.White else Color.Black,
        AppleYellow,
        iOSRed,
        iOSBlue,
        iOSGreen,
        iOSPurple,
        iOSOrange
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) iOSBackgroundDark else iOSBackgroundLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Vazgeç", color = textSecondary(isDark))
                    }

                    Text(
                        text = "Çizim Tuvali",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val filePath = saveBitmapToFile(context, paths, isDark)
                                onSaveDrawing(filePath)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ekle", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

                // Drawing Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                        .pointerInput(isEraser, currentColor, currentStrokeWidth, isHighlighter) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = newPath
                                    currentPoints = mutableListOf(offset)
                                    undonePaths.clear()
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    currentPoints.add(change.position)
                                },
                                onDragEnd = {
                                    currentPath?.let { p ->
                                        val activeColor = if (isEraser) {
                                            if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
                                        } else if (isHighlighter) {
                                            currentColor.copy(alpha = 0.35f)
                                        } else {
                                            currentColor
                                        }
                                        val stroke = if (isEraser) 30f else if (isHighlighter) 24f else currentStrokeWidth
                                        paths.add(DrawPath(p, activeColor, stroke, isHighlighter, currentPoints.toList()))
                                    }
                                    currentPath = null
                                    currentPoints = mutableListOf()
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { drawPath ->
                            drawPath(
                                path = drawPath.path,
                                color = drawPath.color,
                                style = Stroke(
                                    width = drawPath.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        currentPath?.let { p ->
                            val activeColor = if (isEraser) {
                                if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
                            } else if (isHighlighter) {
                                currentColor.copy(alpha = 0.35f)
                            } else {
                                currentColor
                            }
                            val stroke = if (isEraser) 30f else if (isHighlighter) 24f else currentStrokeWidth
                            drawPath(
                                path = p,
                                color = activeColor,
                                style = Stroke(
                                    width = stroke,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Apple-style Tool Palette Bar
                Surface(
                    color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Tool Switchers & Undo/Redo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ToolIconButton(
                                    icon = Icons.Default.Edit,
                                    label = "Kalem",
                                    isSelected = !isEraser && !isHighlighter,
                                    onClick = { isEraser = false; isHighlighter = false }
                                )
                                ToolIconButton(
                                    icon = Icons.Default.Brush,
                                    label = "Fosforlu",
                                    isSelected = isHighlighter,
                                    onClick = { isEraser = false; isHighlighter = true }
                                )
                                ToolIconButton(
                                    icon = Icons.Default.AutoFixHigh,
                                    label = "Silgi",
                                    isSelected = isEraser,
                                    onClick = { isEraser = true; isHighlighter = false }
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        if (paths.isNotEmpty()) {
                                            undonePaths.add(paths.removeLast())
                                        }
                                    },
                                    enabled = paths.isNotEmpty()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Geri Al")
                                }
                                IconButton(
                                    onClick = {
                                        if (undonePaths.isNotEmpty()) {
                                            paths.add(undonePaths.removeLast())
                                        }
                                    },
                                    enabled = undonePaths.isNotEmpty()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "İleri Al")
                                }
                                IconButton(
                                    onClick = {
                                        paths.clear()
                                        undonePaths.clear()
                                    },
                                    enabled = paths.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Tümünü Temizle", tint = iOSRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Palette
                        if (!isEraser) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                colors.forEach { color ->
                                    val isSelected = currentColor == color
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) AppleYellow else Color.Gray.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .clickable { currentColor = color }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppleYellow,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun textSecondary(isDark: Boolean) = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

private fun saveBitmapToFile(context: android.content.Context, paths: List<DrawPath>, isDark: Boolean): String {
    val width = 1080
    val height = 1440
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val bgColor = if (isDark) iOSCardBackgroundDark.toArgb() else iOSCardBackgroundLight.toArgb()
    canvas.drawColor(bgColor)

    paths.forEach { drawPath ->
        val paint = AndroidPaint().apply {
            color = drawPath.color.toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = drawPath.strokeWidth * 1.5f
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            isAntiAlias = true
        }

        val androidPath = android.graphics.Path()
        val points = drawPath.points
        if (points.isNotEmpty()) {
            androidPath.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                androidPath.lineTo(points[i].x, points[i].y)
            }
            canvas.drawPath(androidPath, paint)
        }
    }

    val dir = File(context.filesDir, "drawings")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "drawing_.png")
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    fos.flush()
    fos.close()

    return file.absolutePath
}