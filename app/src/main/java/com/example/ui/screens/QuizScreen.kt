package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GradeProfile
import com.example.model.QuizOptionLetter
import com.example.model.QuizQuestion
import com.example.model.QuizUiState
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.ScoreAndStreakPills
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.EmeraldCorrectSoft
import com.example.ui.theme.RoseWrong
import com.example.ui.theme.RoseWrongSoft
import com.example.ui.theme.SkyTertiary
import com.example.ui.theme.TangerineSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun QuizScreen(
    profile: GradeProfile,
    quizState: QuizUiState,
    totalScore: Int,
    currentStreak: Int,
    confettiTrigger: Int,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onSelectOption: (QuizOptionLetter) -> Unit,
    onNextQuestion: () -> Unit,
    onSpeakQuestion: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("quiz_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_desc)
                            )
                        }
                        Column {
                            Text(
                                text = "Soru Yarışması",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.labelTr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    ScoreAndStreakPills(
                        score = totalScore,
                        streak = currentStreak
                    )
                }
            }

            // Content Body
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isWide = maxWidth >= 650.dp

                when (quizState) {
                    is QuizUiState.Loading -> {
                        QuizLoadingState(profile = profile)
                    }

                    is QuizUiState.Error -> {
                        QuizErrorState(
                            message = quizState.message,
                            onRetry = onRetryLoad
                        )
                    }

                    is QuizUiState.Ready -> {
                        if (quizState.roundCompleted) {
                            QuizRoundSummary(
                                profile = profile,
                                correctCount = quizState.correctCountInRound,
                                totalCount = quizState.questions.size,
                                currentStreak = currentStreak,
                                totalScore = totalScore,
                                onPlayAgain = onRetryLoad,
                                onBackHome = onBack
                            )
                        } else {
                            QuizActiveQuestionContent(
                                state = quizState,
                                isWide = isWide,
                                isDarkTheme = isDarkTheme,
                                onSelectOption = onSelectOption,
                                onNextQuestion = onNextQuestion,
                                onSpeakQuestion = onSpeakQuestion
                            )
                        }
                    }
                }
            }
        }

        // Celebratory Confetti Particle Layer
        ConfettiOverlay(triggerCount = confettiTrigger)
    }
}

@Composable
private fun QuizLoadingState(profile: GradeProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OwlMascotAvatar(size = 92.dp, isAnimated = false)
        Spacer(Modifier.height(18.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Buddy ${profile.grade}. sınıf için yepyeni sorular hazırlıyor...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "3 şıklı eğlenceli İngilizce testin birkaç saniye içinde hazır!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuizErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 460.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, RoseWrong.copy(alpha = 0.45f)),
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Sorular Yüklenemedi",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag("quiz_retry_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tekrar Dene")
                }
            }
        }
    }
}

@Composable
private fun QuizActiveQuestionContent(
    state: QuizUiState.Ready,
    isWide: Boolean,
    isDarkTheme: Boolean,
    onSelectOption: (QuizOptionLetter) -> Unit,
    onNextQuestion: () -> Unit,
    onSpeakQuestion: (String) -> Unit
) {
    val question = state.currentQuestion
    val totalCount = state.questions.size
    val progress = (state.currentIndex + 1).toFloat() / totalCount.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 880.dp)) {
            // Progress Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soru ${state.currentIndex + 1} / $totalCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Doğru: ${state.correctCountInRound}",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldCorrect
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    QuestionPromptCard(
                        question = question,
                        onSpeak = { onSpeakQuestion(question.question) },
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuizOptionsList(
                            question = question,
                            selectedOption = state.selectedOption,
                            isDarkTheme = isDarkTheme,
                            onSelectOption = onSelectOption
                        )
                    }
                }
            } else {
                QuestionPromptCard(
                    question = question,
                    onSpeak = { onSpeakQuestion(question.question) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuizOptionsList(
                        question = question,
                        selectedOption = state.selectedOption,
                        isDarkTheme = isDarkTheme,
                        onSelectOption = onSelectOption
                    )
                }
            }

            // Instant Feedback & Explanation + Next Button
            AnimatedVisibility(
                visible = state.selectedOption != null,
                enter = fadeIn() + expandVertically()
            ) {
                val isCorrect = state.selectedOption == question.correctOption
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isCorrect) {
                            EmeraldCorrect.copy(alpha = 0.14f)
                        } else {
                            RoseWrong.copy(alpha = 0.12f)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            if (isCorrect) EmeraldCorrect else RoseWrong
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isCorrect) {
                                    "🎉 Harika! Doğru Cevap!"
                                } else {
                                    "💡 Doğru Cevap: ${question.correctOption.label}) ${question.optionText(question.correctOption)}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCorrect) EmeraldCorrect else RoseWrong,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = question.explanationTr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = onNextQuestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("quiz_next_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (state.currentIndex + 1 < totalCount) "Sonraki Soru" else "Sonucu Gör 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuestionPromptCard(
    question: QuizQuestion,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = question.emoji,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("quiz_speak_question_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.speak_text_desc),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizOptionsList(
    question: QuizQuestion,
    selectedOption: QuizOptionLetter?,
    isDarkTheme: Boolean,
    onSelectOption: (QuizOptionLetter) -> Unit
) {
    QuizOptionLetter.entries.forEach { letter ->
        val isAnswered = selectedOption != null
        val isSelected = selectedOption == letter
        val isCorrectOption = question.correctOption == letter

        val scale by animateFloatAsState(
            targetValue = when {
                isAnswered && isCorrectOption -> 1.02f
                isSelected -> 0.99f
                else -> 1f
            },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "option_scale_${letter.label}"
        )

        val targetBg = when {
            !isAnswered -> MaterialTheme.colorScheme.surface
            isCorrectOption -> if (isDarkTheme) EmeraldCorrect.copy(alpha = 0.25f) else EmeraldCorrectSoft
            isSelected -> if (isDarkTheme) RoseWrong.copy(alpha = 0.25f) else RoseWrongSoft
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        }
        val bgColor by animateColorAsState(targetValue = targetBg, label = "option_bg_${letter.label}")

        val borderColor = when {
            !isAnswered -> MaterialTheme.colorScheme.outline
            isCorrectOption -> EmeraldCorrect
            isSelected -> RoseWrong
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        }

        val badgeColor = when {
            !isAnswered -> VioletPrimary
            isCorrectOption -> EmeraldCorrect
            isSelected -> RoseWrong
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            onClick = { if (!isAnswered) onSelectOption(letter) },
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .minimumInteractiveComponentSize()
                .testTag("quiz_option_${letter.label.lowercase()}"),
            shape = MaterialTheme.shapes.medium,
            color = bgColor,
            border = BorderStroke(
                width = if (isAnswered && (isCorrectOption || isSelected)) 2.dp else 1.dp,
                color = borderColor
            ),
            tonalElevation = if (!isAnswered) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text = question.optionText(letter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isAnswered) {
                    if (isCorrectOption) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Doğru",
                            tint = EmeraldCorrect
                        )
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Yanlış",
                            tint = RoseWrong
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizRoundSummary(
    profile: GradeProfile,
    correctCount: Int,
    totalCount: Int,
    currentStreak: Int,
    totalScore: Int,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            tonalElevation = 5.dp
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OwlMascotAvatar(size = 86.dp, isAnimated = false)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (correctCount >= 4) "Muhteşemsin! 🏆" else "Harika Çaba! 🌟",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${profile.titleTr} testinde $totalCount sorudan $correctCount doğru yaptın!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))
                ScoreAndStreakPills(score = totalScore, streak = currentStreak)
                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("quiz_play_again_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Yeni Sorular Üret (AI)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quiz_back_home_button"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Ana Menüye Dön",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
