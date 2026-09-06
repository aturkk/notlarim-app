package com.applenotes.ai.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.core.sync.CloudSyncService
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CloudSyncDialog(
    prefs: SecurePreferences,
    repository: NoteRepository,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()

    val bgCard = if (isDark) iOSCardDark else iOSCardLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val syncService = remember { CloudSyncService(context) }

    var webDavUrl by remember { mutableStateOf(prefs.webDavServerUrl) }
    var webDavUser by remember { mutableStateOf(prefs.webDavUsername) }
    var webDavPass by remember { mutableStateOf(prefs.webDavPassword) }
    var showPassword by remember { mutableStateOf(false) }

    var isSyncing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var lastSyncTime by remember { mutableLongStateOf(prefs.lastCloudSyncTime) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kişisel Bulut",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = textSecondary)
                    }
                }

                Text(
                    text = "Nextcloud veya WebDAV sunucunuza bağlanarak notlarınızı kendi sunucunuzda güvenle saklayın ve geri yükleyin.",
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WebDAV Inputs
                OutlinedTextField(
                    value = webDavUrl,
                    onValueChange = { webDavUrl = it },
                    label = { Text("WebDAV Sunucu URL") },
                    placeholder = { Text("https://cloud.orneksunucu.com/remote.php/dav/files/kullanici/") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = webDavUser,
                    onValueChange = { webDavUser = it },
                    label = { Text("Kullanıcı Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = webDavPass,
                    onValueChange = { webDavPass = it },
                    label = { Text("Şifre veya Uygulama Parolası") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = textSecondary
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // WebDAV Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            prefs.webDavServerUrl = webDavUrl
                            prefs.webDavUsername = webDavUser
                            prefs.webDavPassword = webDavPass

                            scope.launch {
                                isSyncing = true
                                statusMessage = "WebDAV sunucusuna yükleniyor..."
                                val notes = repository.getAllNotes().first()
                                val res = syncService.uploadToWebDav(notes, webDavUrl, webDavUser, webDavPass)
                                isSyncing = false
                                res.onSuccess {
                                    prefs.lastCloudSyncTime = System.currentTimeMillis()
                                    lastSyncTime = prefs.lastCloudSyncTime
                                    statusMessage = "✅ Buluta başarıyla eşitlendi!"
                                }.onFailure { err ->
                                    statusMessage = "❌ Eşitleme hatası: ${err.localizedMessage}"
                                }
                            }
                        },
                        enabled = !isSyncing && webDavUrl.isNotBlank() && webDavUser.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Şimdi Yükle", fontSize = 13.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            prefs.webDavServerUrl = webDavUrl
                            prefs.webDavUsername = webDavUser
                            prefs.webDavPassword = webDavPass

                            scope.launch {
                                isSyncing = true
                                statusMessage = "Sunucudan indiriliyor..."
                                val res = syncService.restoreFromWebDav(webDavUrl, webDavUser, webDavPass, repository)
                                isSyncing = false
                                res.onSuccess { count ->
                                    statusMessage = "✅ $count not başarıyla geri yüklendi!"
                                }.onFailure { err ->
                                    statusMessage = "❌ İndirme hatası: ${err.localizedMessage}"
                                }
                            }
                        },
                        enabled = !isSyncing && webDavUrl.isNotBlank() && webDavUser.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Geri Yükle", fontSize = 13.sp)
                    }
                }

                // Status message & Progress
                if (isSyncing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = accentColor, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(statusMessage ?: "İşlem yapılıyor...", fontSize = 13.sp, color = textPrimary)
                    }
                } else if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = statusMessage ?: "",
                        fontSize = 13.sp,
                        color = if (statusMessage?.startsWith("✅") == true) accentColor else iOSRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (lastSyncTime > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    Text(
                        text = "Son Başarılı Eşitleme: ${sdf.format(Date(lastSyncTime))}",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tamam", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
