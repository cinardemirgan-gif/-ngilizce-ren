package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.ChatMessage
import com.example.model.GradeProfile
import com.example.model.VoiceAiUiState
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.theme.RoseWrong
import com.example.ui.theme.SkyTertiary
import com.example.ui.theme.TangerineSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.VioletPrimaryDeep

@Composable
fun VoicePartnerScreen(
    profile: GradeProfile,
    messages: List<ChatMessage>,
    aiState: VoiceAiUiState,
    isListening: Boolean,
    isSpeaking: Boolean,
    speakingMessageId: String?,
    partialSpeechText: String,
    rmsLevel: Float,
    speechError: String?,
    isRecognitionAvailable: Boolean,
    autoSpeakEnabled: Boolean,
    onBack: () -> Unit,
    onResetChat: () -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendText: (String) -> Unit,
    onToggleSpeakMessage: (String, String) -> Unit,
    onDismissSpeechError: () -> Unit
) {
    val context = LocalContext.current
    var inputText by rememberSaveable { mutableStateOf("") }
    var permissionDeniedNotice by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNotice = false
            onStartListening()
        } else {
            permissionDeniedNotice = true
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.size > 1) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val handleMicTap = {
        if (isListening) {
            onStopListening()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                permissionDeniedNotice = false
                onStartListening()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("voice_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc)
                        )
                    }

                    OwlMascotAvatar(
                        size = 44.dp,
                        isAnimated = isSpeaking || aiState is VoiceAiUiState.Generating
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Buddy Öğretmen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                isSpeaking -> "🔊 Konuşuyor..."
                                aiState is VoiceAiUiState.Generating -> "✨ Düşünüyor..."
                                isListening -> "🎙️ Seni dinliyor..."
                                else -> "${profile.labelTr} • ${profile.cefrBadge}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleAutoSpeak,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_auto_speak_button")
                    ) {
                        Icon(
                            imageVector = if (autoSpeakEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (autoSpeakEnabled) "Otomatik sesi kapat" else "Otomatik sesi aç",
                            tint = if (autoSpeakEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onResetChat,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reset_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yeni sohbet başlat"
                        )
                    }
                }
            }
        }

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    isThisSpeaking = isSpeaking && speakingMessageId == msg.id,
                    onToggleSpeak = { onToggleSpeakMessage(msg.id, msg.text) }
                )
            }
        }

        // Error or Permission Notices
        AnimatedVisibility(visible = aiState is VoiceAiUiState.Error) {
            val errorMsg = (aiState as? VoiceAiUiState.Error)?.message.orEmpty()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.small,
                color = RoseWrong.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, RoseWrong.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "⚠️ $errorMsg",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseWrong
                )
            }
        }

        AnimatedVisibility(visible = permissionDeniedNotice || speechError != null) {
            val infoText = if (permissionDeniedNotice) {
                "Mikrofon izni verilmedi. Aşağıdaki kutuya İngilizce yazarak Buddy ile konuşmaya devam edebilirsin!"
            } else {
                speechError.orEmpty()
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.small,
                color = TangerineSecondary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, TangerineSecondary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        permissionDeniedNotice = false
                        onDismissSpeechError()
                    }) {
                        Text("Tamam")
                    }
                }
            }
        }

        // Live Listening Indicator
        AnimatedVisibility(visible = isListening) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (partialSpeechText.isNotBlank()) "\"$partialSpeechText\"" else "Dinliyorum... İngilizce konuşabilirsin!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Starter Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 820.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profile.starterPrompts.forEach { prompt ->
                Surface(
                    onClick = {
                        if (aiState !is VoiceAiUiState.Generating) {
                            onSendText(prompt)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "💬 $prompt",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Lightweight Bottom Input Dock (zero OutlinedTextField composition lag)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = if (isRecognitionAvailable) {
                                    "İngilizce yaz veya mikrofona bas..."
                                } else {
                                    "İngilizce mesajını buraya yaz..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("voice_text_input"),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank() && aiState !is VoiceAiUiState.Generating) {
                                        onSendText(inputText)
                                        inputText = ""
                                    }
                                }
                            )
                        )
                    }

                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                if (aiState !is VoiceAiUiState.Generating) {
                                    onSendText(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("send_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.send_message_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                PulsingMicButton(
                    isListening = isListening,
                    isGenerating = aiState is VoiceAiUiState.Generating,
                    rmsLevel = rmsLevel,
                    onClick = { handleMicTap() }
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isThisSpeaking: Boolean,
    onToggleSpeak: () -> Unit
) {
    val isUser = message.isFromUser
    val bubbleShape = remember(isUser) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = if (isUser) 20.dp else 6.dp,
            bottomEnd = if (isUser) 6.dp else 20.dp
        )
    }
    val userBrush = remember {
        Brush.linearGradient(listOf(VioletPrimary, VioletPrimaryDeep))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            OwlMascotAvatar(
                size = 34.dp,
                isAnimated = false
            )
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = bubbleShape,
            color = if (isUser) Color.Transparent else MaterialTheme.colorScheme.surface,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = if (isUser) {
                    Modifier
                        .clip(bubbleShape)
                        .background(userBrush)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                } else {
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                }
            ) {
                Column {
                    if (message.text.isBlank() && message.isStreaming) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Buddy yazıyor...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isUser && !message.isStreaming && message.text.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = onToggleSpeak,
                            shape = RoundedCornerShape(50),
                            color = if (isThisSpeaking) {
                                TangerineSecondary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isThisSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.speak_text_desc),
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isThisSpeaking) TangerineSecondary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isThisSpeaking) "Durdur" else "Dinle",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isThisSpeaking) TangerineSecondary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingMicButton(
    isListening: Boolean,
    isGenerating: Boolean,
    rmsLevel: Float,
    onClick: () -> Unit
) {
    val pulse = if (isListening) {
        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
        val value by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.16f,
            animationSpec = infiniteRepeatable(
                animation = tween(520),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mic_scale"
        )
        value
    } else {
        1f
    }
    val dynamicScale = if (isListening) (pulse + rmsLevel * 0.14f) else 1f

    val bgBrush = remember(isListening, isGenerating) {
        Brush.linearGradient(
            colors = when {
                isListening -> listOf(RoseWrong, TangerineSecondary)
                isGenerating -> listOf(Color.Gray, Color.DarkGray)
                else -> listOf(VioletPrimary, SkyTertiary)
            }
        )
    }

    Box(contentAlignment = Alignment.Center) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .graphicsLayer {
                        val ringScale = dynamicScale * 1.15f
                        scaleX = ringScale
                        scaleY = ringScale
                    }
                    .clip(CircleShape)
                    .background(RoseWrong.copy(alpha = 0.24f))
            )
        }

        Surface(
            onClick = { if (!isGenerating) onClick() },
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = dynamicScale
                    scaleY = dynamicScale
                }
                .testTag("mic_listen_button"),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = stringResource(R.string.mic_button_desc),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
