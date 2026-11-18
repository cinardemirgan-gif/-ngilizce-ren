package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase.configureAppCheck
import com.example.ui.LingoBuddyViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.VoicePartnerScreen
import com.example.ui.theme.LingoBuddyTheme

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME = "home"
private const val ROUTE_VOICE_CHAT = "voice_chat"
private const val ROUTE_QUIZ = "quiz"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            LingoBuddyApp()
        }
    }
}

@Composable
fun LingoBuddyApp(
    viewModel: LingoBuddyViewModel = viewModel()
) {
    val prefsSnapshot by viewModel.prefsState.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val voiceAiState by viewModel.voiceAiState.collectAsStateWithLifecycle()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsStateWithLifecycle()

    val isListening by viewModel.speechManager.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by viewModel.speechManager.speakingMessageId.collectAsStateWithLifecycle()
    val partialSpeechText by viewModel.speechManager.partialText.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.speechManager.rmsLevel.collectAsStateWithLifecycle()
    val speechError by viewModel.speechManager.speechError.collectAsStateWithLifecycle()
    val isRecognitionAvailable by viewModel.speechManager.isRecognitionAvailable.collectAsStateWithLifecycle()

    val quizState by viewModel.quizState.collectAsStateWithLifecycle()

    val snapshot = prefsSnapshot
    val isDarkTheme = snapshot?.isDarkTheme ?: false

    LingoBuddyTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (snapshot == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                LaunchedEffect(snapshot.selectedGrade) {
                    if (snapshot.selectedGrade != null) {
                        viewModel.warmUpAudioAndQuiz()
                    }
                }

                val navController = rememberNavController()
                val startDestination = remember {
                    if (snapshot.selectedGrade == null) {
                        ROUTE_ONBOARDING
                    } else {
                        ROUTE_HOME
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / 6 },
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(180))
                    },
                    exitTransition = {
                        fadeOut(tween(140))
                    },
                    popEnterTransition = {
                        fadeIn(tween(160))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / 6 },
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(150))
                    }
                ) {
                    composable(ROUTE_ONBOARDING) {
                        OnboardingScreen(
                            initialGrade = snapshot.selectedGrade,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleDarkTheme(isDarkTheme) },
                            onGradeConfirmed = { grade ->
                                viewModel.selectGrade(grade)
                                navController.navigate(ROUTE_HOME) {
                                    popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(ROUTE_HOME) {
                        HomeScreen(
                            profile = activeProfile,
                            totalScore = snapshot.totalScore,
                            currentStreak = currentStreak,
                            bestStreak = snapshot.bestStreak,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleDarkTheme(isDarkTheme) },
                            onChangeGradeClick = {
                                navController.navigate(ROUTE_ONBOARDING) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenVoiceMode = {
                                navController.navigate(ROUTE_VOICE_CHAT) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenQuizMode = {
                                navController.navigate(ROUTE_QUIZ) {
                                    launchSingleTop = true
                                }
                            },
                            onSpeakWord = { word ->
                                viewModel.speakWordOrPrompt(word)
                            }
                        )
                    }

                    composable(ROUTE_VOICE_CHAT) {
                        LaunchedEffect(Unit) {
                            viewModel.ensureChatInitialized()
                        }
                        VoicePartnerScreen(
                            profile = activeProfile,
                            messages = chatMessages,
                            aiState = voiceAiState,
                            isListening = isListening,
                            isSpeaking = isSpeaking,
                            speakingMessageId = speakingMessageId,
                            partialSpeechText = partialSpeechText,
                            rmsLevel = rmsLevel,
                            speechError = speechError,
                            isRecognitionAvailable = isRecognitionAvailable,
                            autoSpeakEnabled = autoSpeakEnabled,
                            onBack = {
                                viewModel.speechManager.stopListening()
                                viewModel.speechManager.stopSpeaking()
                                navController.popBackStack()
                            },
                            onResetChat = { viewModel.resetVoiceChat() },
                            onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
                            onStartListening = { viewModel.startMicrophoneListening() },
                            onStopListening = { viewModel.speechManager.stopListening() },
                            onSendText = { text -> viewModel.sendVoiceOrTextMessage(text) },
                            onToggleSpeakMessage = { id, text ->
                                viewModel.toggleSpeakMessage(id, text)
                            },
                            onDismissSpeechError = { viewModel.speechManager.clearSpeechError() }
                        )
                    }

                    composable(ROUTE_QUIZ) {
                        LaunchedEffect(Unit) {
                            viewModel.ensureQuizLoaded()
                        }
                        QuizScreen(
                            profile = activeProfile,
                            quizState = quizState,
                            totalScore = snapshot.totalScore,
                            currentStreak = currentStreak,
                            confettiTrigger = confettiTrigger,
                            isDarkTheme = isDarkTheme,
                            onBack = {
                                viewModel.speechManager.stopSpeaking()
                                navController.popBackStack()
                            },
                            onRetryLoad = { viewModel.loadNewQuizRound() },
                            onSelectOption = { option -> viewModel.selectQuizOption(option) },
                            onNextQuestion = { viewModel.nextQuizQuestion() },
                            onSpeakQuestion = { text -> viewModel.speakWordOrPrompt(text) }
                        )
                    }
                }
            }
        }
    }
}
