package com.applenotes.ai.presentation.ai_assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatBottomSheet(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    var inputQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Not Asistanı ile Sohbet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                    )
                }
            }

            HorizontalDivider(
                color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                thickness = 0.5.dp
            )

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bu not hakkında merak ettiğiniz her şeyi sorabilirsiniz.\nÖrn: \"Bu notun ana fikri nedir?\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                SuggestionChip(
                                    onClick = { onSendMessage("Bu notun ana fikrini ve en önemli noktalarını özetle.") },
                                    label = { Text("Özet Çıkar", fontSize = 12.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = { onSendMessage("Bu notu daha akıcı ve profesyonel hale nasıl getirebilirim?") },
                                    label = { Text("İyileştirme Önerisi", fontSize = 12.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight
                                    )
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    val isUser = msg.role == MessageRole.USER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) AppleYellow else if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight,
                            contentColor = if (isUser) Color.Black else if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AppleYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Yapay zeka yanıtlıyor...",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                            )
                        }
                    }
                }
            }

            // Input Bar (Always pinned at bottom with IME padding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            text = "Bir soru sorun...",
                            fontSize = 14.sp,
                            color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleYellow,
                        unfocusedBorderColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        focusedTextColor = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                        unfocusedTextColor = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                        cursorColor = AppleYellow,
                        focusedContainerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight,
                        unfocusedContainerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank() && !isLoading) {
                            onSendMessage(inputQuery)
                            inputQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (inputQuery.isNotBlank()) AppleYellow else Color.Gray.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = if (inputQuery.isNotBlank()) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
