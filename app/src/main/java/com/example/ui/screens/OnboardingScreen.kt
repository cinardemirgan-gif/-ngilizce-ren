package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.GradeCurriculum
import com.example.model.SchoolLevel
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.theme.SkyTertiary
import com.example.ui.theme.TangerineSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun OnboardingScreen(
    initialGrade: Int?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onGradeConfirmed: (Int) -> Unit
) {
    val initialProfile = initialGrade?.let { GradeCurriculum.getProfile(it) }
    var selectedLevel by remember(initialGrade) {
        mutableStateOf(initialProfile?.schoolLevel)
    }
    var selectedGrade by remember(initialGrade) {
        mutableStateOf(initialGrade)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with theme switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 780.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "✨ LingoBuddy AI",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("onboarding_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = stringResource(R.string.toggle_theme_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Hero Mascot + Welcome Greeting
            Column(
                modifier = Modifier.widthIn(max = 760.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OwlMascotAvatar(
                    size = if (isWide) 104.dp else 88.dp,
                    isAnimated = false
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "İngilizce Macerana Hoş Geldin!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Seni tanıyalım ki yapay zeka öğretmenin Buddy tam seviyene göre konuşsun ve sorular hazırlasın.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 520.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Step 1 Question
                Text(
                    text = stringResource(R.string.onboarding_question),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))

                // Two large tactile school-level cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SchoolLevelCard(
                        level = SchoolLevel.PRIMARY,
                        selected = selectedLevel == SchoolLevel.PRIMARY,
                        accentColor = TangerineSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("school_primary_button"),
                        onClick = {
                            selectedLevel = SchoolLevel.PRIMARY
                            if (selectedGrade !in SchoolLevel.PRIMARY.grades) {
                                selectedGrade = null
                            }
                        }
                    )
                    SchoolLevelCard(
                        level = SchoolLevel.MIDDLE,
                        selected = selectedLevel == SchoolLevel.MIDDLE,
                        accentColor = VioletPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("school_middle_button"),
                        onClick = {
                            selectedLevel = SchoolLevel.MIDDLE
                            if (selectedGrade !in SchoolLevel.MIDDLE.grades) {
                                selectedGrade = null
                            }
                        }
                    )
                }

                // Step 2: Dynamic Grade Selection
                AnimatedVisibility(
                    visible = selectedLevel != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val level = selectedLevel ?: SchoolLevel.PRIMARY
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kaçıncı sınıfa gidiyorsun?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))

                        val grades = level.grades
                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                grades.forEach { grade ->
                                    GradeChipCard(
                                        grade = grade,
                                        selected = selectedGrade == grade,
                                        accentColor = if (level == SchoolLevel.PRIMARY) TangerineSecondary else VioletPrimary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("grade_${grade}_button"),
                                        onClick = { selectedGrade = grade }
                                    )
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    grades.take(2).forEach { grade ->
                                        GradeChipCard(
                                            grade = grade,
                                            selected = selectedGrade == grade,
                                            accentColor = if (level == SchoolLevel.PRIMARY) TangerineSecondary else VioletPrimary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("grade_${grade}_button"),
                                            onClick = { selectedGrade = grade }
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    grades.drop(2).forEach { grade ->
                                        GradeChipCard(
                                            grade = grade,
                                            selected = selectedGrade == grade,
                                            accentColor = if (level == SchoolLevel.PRIMARY) TangerineSecondary else VioletPrimary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("grade_${grade}_button"),
                                            onClick = { selectedGrade = grade }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Curriculum Preview + Start Button
                AnimatedVisibility(
                    visible = selectedGrade != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val grade = selectedGrade ?: 3
                    val profile = GradeCurriculum.getProfile(grade)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = profile.titleTr,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = SkyTertiary.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = profile.cefrBadge,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SkyTertiary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Odak Konular: ${profile.focusSummaryTr}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = { onGradeConfirmed(grade) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("start_learning_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Maceraya Başla!",
                                style = MaterialTheme.typography.titleLarge,
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
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SchoolLevelCard(
    level: SchoolLevel,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "school_card_scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .minimumInteractiveComponentSize(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(
            width = if (selected) 2.5.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline
        ),
        tonalElevation = if (selected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = level.emoji, style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = level.titleTr,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = level.subtitleTr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (selected) {
                Spacer(Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun GradeChipCard(
    grade: Int,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val profile = GradeCurriculum.getProfile(grade)
    Surface(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) accentColor else MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline
        ),
        tonalElevation = if (selected) 5.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$grade. Sınıf",
                style = MaterialTheme.typography.titleLarge,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = profile.cefrBadge,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
