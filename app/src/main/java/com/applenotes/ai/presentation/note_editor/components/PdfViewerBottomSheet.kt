package com.applenotes.ai.presentation.note_editor.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerBottomSheet(
    pdfFilePath: String,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val file = remember(pdfFilePath) { File(pdfFilePath) }

    LaunchedEffect(pdfFilePath, currentPageIndex) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    errorMessage = "PDF dosyası bulunamadı."
                    isLoading = false
                    return@withContext
                }
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount

                if (currentPageIndex in 0 until pageCount) {
                    val page = renderer.openPage(currentPageIndex)
                    // Render at 2x density for sharp quality
                    val width = (page.width * 2).coerceAtMost(2048)
                    val height = (page.height * 2).coerceAtMost(2048)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    currentBitmap = bitmap
                }
                renderer.close()
                pfd.close()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "PDF yüklenemedi: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = iOSRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = file.nameWithoutExtension.ifBlank { "PDF Belgesi" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1
                        )
                        if (pageCount > 0) {
                            Text(
                                text = "Sayfa ${currentPageIndex + 1} / $pageCount",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PDF Content Page
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = accentColor)
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Hata",
                        color = iOSRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else currentBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "PDF Sayfa",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Page Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                    enabled = currentPageIndex > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Önceki")
                }

                Text(
                    text = "${currentPageIndex + 1} / ${pageCount.coerceAtLeast(1)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )

                FilledTonalButton(
                    onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                    enabled = currentPageIndex < pageCount - 1,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sonraki")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                }
            }
        }
    }
}
