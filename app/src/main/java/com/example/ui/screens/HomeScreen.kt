package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GradeProfile
import com.example.model.SampleWord
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.ScoreAndStreakPills
import com.example.ui.theme.SkyTertiary
import com.example.ui.theme.TangerineSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.VioletPrimaryDeep

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    profile: GradeProfile,
    totalScore: Int,
    currentStreak: Int,
    bestStreak: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onChangeGradeClick: () -> Unit,
    onOpenVoiceMode: () -> Unit,
    onOpenQuizMode: () -> Unit,
    onSpeakWord: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isWide = maxWidth >= 640.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 900.dp)) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onChangeGradeClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("change_grade_button"),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(profile.schoolLevel.emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = profile.labelTr,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Sınıf değiştir",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ScoreAndStreakPills(
                            score = totalScore,
                            streak = currentStreak
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = stringResource(R.string.toggle_theme_desc),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Hero Welcome Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(VioletPrimary, VioletPrimaryDeep, SkyTertiary)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OwlMascotAvatar(size = 76.dp, isAnimated = false)
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${profile.titleTr} • ${profile.cefrBadge}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Bugün nasıl pratik yapalım?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = profile.focusSummaryTr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                if (bestStreak > 0) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "🏆 En iyi seri rekorun: $bestStreak",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFFFE082)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "Öğrenme Modunu Seç",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))

                // Responsive 2-Mode Cards (side-by-side on tablets, stacked on phones)
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModeSelectionCard(
                            emoji = "🤖",
                            title = stringResource(R.string.home_voice_title),
                            subtitle = "Mikrofonla konuş veya yaz! Öğretmenin Buddy seviyene uygun sesli İngilizce yanıt versin.",
                            badgeText = "Sesli Sohbet (STT + TTS)",
                            gradientColors = listOf(VioletPrimary, SkyTertiary),
                            icon = Icons.Default.Mic,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_mode_card"),
                            onClick = onOpenVoiceMode
                        )
                        ModeSelectionCard(
                            emoji = "📝",
                            title = stringResource(R.string.home_quiz_title),
                            subtitle = "Yapay zekanın ${profile.grade}. sınıfa özel hazırladığı A, B, C şıklı sorularla puan ve seri topla!",
                            badgeText = "3 Şıklı Yarışma",
                            gradientColors = listOf(TangerineSecondary, Color(0xFFF59E0B)),
                            icon = Icons.Default.Quiz,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quiz_mode_card"),
                            onClick = onOpenQuizMode
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ModeSelectionCard(
                            emoji = "🤖",
                            title = stringResource(R.string.home_voice_title),
                            subtitle = "Mikrofonla konuş veya yaz! Öğretmenin Buddy seviyene uygun sesli İngilizce yanıt versin.",
                            badgeText = "Sesli Sohbet (STT + TTS)",
                            gradientColors = listOf(VioletPrimary, SkyTertiary),
                            icon = Icons.Default.Mic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_mode_card"),
                            onClick = onOpenVoiceMode
                        )
                        ModeSelectionCard(
                            emoji = "📝",
                            title = stringResource(R.string.home_quiz_title),
                            subtitle = "Yapay zekanın ${profile.grade}. sınıfa özel hazırladığı A, B, C şıklı sorularla puan ve seri topla!",
                            badgeText = "3 Şıklı Yarışma",
                            gradientColors = listOf(TangerineSecondary, Color(0xFFF59E0B)),
                            icon = Icons.Default.Quiz,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_mode_card"),
                            onClick = onOpenQuizMode
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Interactive Grade Vocabulary Quick Practice Strip
                Text(
                    text = "${profile.grade}. Sınıf Hızlı Telaffuz Kartları (Dinlemek için dokun)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    profile.sampleWords.forEach { word ->
                        SampleWordChip(
                            word = word,
                            onClick = { onSpeakWord(word.english) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ModeSelectionCard(
    emoji: String,
    title: String,
    subtitle: String,
    badgeText: String,
    gradientColors: List<Color>,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, gradientColors.first().copy(alpha = 0.35f)),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = gradientColors.first().copy(alpha = 0.14f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = gradientColors.first()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Başla",
                    style = MaterialTheme.typography.labelLarge,
                    color = gradientColors.first()
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = gradientColors.first(),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SampleWordChip(
    word: SampleWord,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("sample_word_${word.english.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(word.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = word.turkish,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = stringResource(R.string.speak_text_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
