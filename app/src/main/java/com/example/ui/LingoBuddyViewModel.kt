package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.data.AiEnglishRepository
import com.example.data.PrefsRepository
import com.example.data.UserPrefsSnapshot
import com.example.model.ChatMessage
import com.example.model.GradeCurriculum
import com.example.model.GradeProfile
import com.example.model.QuizOptionLetter
import com.example.model.QuizQuestion
import com.example.model.QuizUiState
import com.example.model.VoiceAiUiState
import com.google.firebase.ai.Chat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "LingoBuddyViewModel"

class LingoBuddyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = PrefsRepository(application)
    private val aiRepository = AiEnglishRepository()
    val speechManager = SpeechManager(application)

    val prefsState: StateFlow<UserPrefsSnapshot?> = prefsRepository.prefsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val activeProfile: StateFlow<GradeProfile> = prefsRepository.prefsFlow
        .map { GradeCurriculum.getProfile(it.selectedGrade ?: 3) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GradeCurriculum.getProfile(3)
        )

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _confettiTrigger = MutableStateFlow(0)
    val confettiTrigger: StateFlow<Int> = _confettiTrigger.asStateFlow()

    // Voice Chat State
    @Volatile
    private var activeChat: Chat? = null
    @Volatile
    private var chatGrade: Int? = null
    private var chatInitJob: Job? = null
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _voiceAiState = MutableStateFlow<VoiceAiUiState>(VoiceAiUiState.Idle)
    val voiceAiState: StateFlow<VoiceAiUiState> = _voiceAiState.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(true)
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    // Quiz State + Background Prefetch Cache
    private val _quizState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()
    private val recentQuizQuestions = mutableListOf<String>()
    private var quizLoadedForGrade: Int? = null

    private var prefetchedGrade: Int? = null
    private var prefetchedQuestions: List<QuizQuestion>? = null
    private var prefetchJob: Job? = null

    fun selectGrade(grade: Int) {
        viewModelScope.launch {
            prefsRepository.setSelectedGrade(grade)
            val profile = GradeCurriculum.getProfile(grade)
            if (chatGrade != grade) {
                activeChat = null
                chatGrade = null
            }
            if (quizLoadedForGrade != grade) {
                quizLoadedForGrade = null
                prefetchedQuestions = null
                prefetchedGrade = null
                prefetchNextQuizBatch(profile, initialDelayMs = 600L)
            }
        }
    }

    fun warmUpAudioAndQuiz() {
        speechManager.warmUpTts()
        val profile = activeProfile.value
        if (prefetchedGrade != profile.grade && prefetchedQuestions == null && prefetchJob?.isActive != true) {
            prefetchNextQuizBatch(profile, initialDelayMs = 600L)
        }
    }

    fun toggleDarkTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            prefsRepository.setDarkTheme(!currentlyDark)
        }
    }

    fun toggleAutoSpeak() {
        val next = !_autoSpeakEnabled.value
        _autoSpeakEnabled.value = next
        if (!next) {
            speechManager.stopSpeaking()
        }
    }

    fun ensureChatInitialized() {
        val profile = activeProfile.value
        if (_chatMessages.value.isEmpty() || chatGrade != profile.grade) {
            resetVoiceChat(profile, speakGreeting = true)
        }
    }

    fun resetVoiceChat(
        profile: GradeProfile = activeProfile.value,
        speakGreeting: Boolean = true
    ) {
        speechManager.stopSpeaking()
        chatGrade = profile.grade
        activeChat = null
        _voiceAiState.value = VoiceAiUiState.Idle

        val greeting = when {
            profile.grade <= 2 -> "Hi! I am Buddy! What is your name?"
            profile.grade <= 4 -> "Hello! I am Buddy, your English friend! How are you today?"
            profile.grade <= 6 -> "Hey there! I'm Buddy, your AI English partner. What topic would you like to chat about today?"
            else -> "Hi! I'm Buddy, your English speaking partner. Ready to practice Grade ${profile.grade} English together? How was your day?"
        }
        val msgId = UUID.randomUUID().toString()
        // Immediate 0ms UI update
        _chatMessages.value = listOf(
            ChatMessage(
                id = msgId,
                isFromUser = false,
                text = greeting
            )
        )

        // Initialize Firebase AI Chat off the main thread AFTER navigation animation settles
        chatInitJob?.cancel()
        chatInitJob = viewModelScope.launch {
            activeChat = withContext(Dispatchers.IO) {
                aiRepository.startVoiceChat(profile)
            }
            if (speakGreeting && _autoSpeakEnabled.value) {
                delay(240L)
                if (_autoSpeakEnabled.value && chatGrade == profile.grade) {
                    speechManager.speak(greeting, profile.ttsSpeechRate, msgId)
                }
            }
        }
    }

    fun sendVoiceOrTextMessage(rawText: String) {
        val userText = rawText.trim()
        if (userText.isBlank() || _voiceAiState.value is VoiceAiUiState.Generating) return

        val profile = activeProfile.value
        speechManager.stopSpeaking()
        speechManager.clearSpeechError()

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = userText
        )
        val aiMsgId = UUID.randomUUID().toString()
        val aiPlaceholder = ChatMessage(
            id = aiMsgId,
            isFromUser = false,
            text = "",
            isStreaming = true
        )
        _chatMessages.value = _chatMessages.value + userMsg + aiPlaceholder
        _voiceAiState.value = VoiceAiUiState.Generating

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val chat = activeChat ?: withContext(Dispatchers.IO) {
                    aiRepository.startVoiceChat(profile).also {
                        activeChat = it
                        chatGrade = profile.grade
                    }
                }
                withTimeout(45_000L) {
                    withContext(Dispatchers.IO) {
                        aiRepository.sendChatMessageStream(chat, userText).collect { chunk ->
                            accumulated.append(chunk)
                            val partial = accumulated.toString()
                            _chatMessages.value = _chatMessages.value.map { msg ->
                                if (msg.id == aiMsgId) msg.copy(text = partial, isStreaming = true) else msg
                            }
                        }
                    }
                }
                val finalText = accumulated.toString().trim()
                if (finalText.isBlank()) {
                    _chatMessages.value = _chatMessages.value.filterNot { it.id == aiMsgId }
                    _voiceAiState.value = VoiceAiUiState.Error("Yapay zeka boş yanıt döndürdü. Lütfen tekrar dene.")
                } else {
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(text = finalText, isStreaming = false) else msg
                    }
                    _voiceAiState.value = VoiceAiUiState.Idle
                    if (_autoSpeakEnabled.value) {
                        speechManager.speak(finalText, profile.ttsSpeechRate, aiMsgId)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Voice chat AI timed out", e)
                _chatMessages.value = _chatMessages.value.filterNot { it.id == aiMsgId }
                _voiceAiState.value = VoiceAiUiState.Error("Yanıt süresi doldu. Bağlantını kontrol edip tekrar dene.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Voice chat AI failed", e)
                _chatMessages.value = _chatMessages.value.filterNot { it.id == aiMsgId }
                _voiceAiState.value = VoiceAiUiState.Error(
                    e.localizedMessage ?: "Yapay zeka yanıtı alınamadı. Lütfen tekrar dene."
                )
            }
        }
    }

    fun startMicrophoneListening() {
        speechManager.startListening { spokenText ->
            sendVoiceOrTextMessage(spokenText)
        }
    }

    fun toggleSpeakMessage(messageId: String, text: String) {
        if (speechManager.isSpeaking.value && speechManager.speakingMessageId.value == messageId) {
            speechManager.stopSpeaking()
        } else {
            speechManager.speak(text, activeProfile.value.ttsSpeechRate, messageId)
        }
    }

    fun speakWordOrPrompt(text: String) {
        speechManager.speak(text, activeProfile.value.ttsSpeechRate)
    }

    fun ensureQuizLoaded() {
        val profile = activeProfile.value
        if (quizLoadedForGrade != profile.grade || _quizState.value is QuizUiState.Error) {
            loadNewQuizRound()
        }
    }

    fun loadNewQuizRound() {
        val profile = activeProfile.value
        quizLoadedForGrade = profile.grade

        val cached = prefetchedQuestions
        if (cached != null && prefetchedGrade == profile.grade) {
            prefetchedQuestions = null
            prefetchedGrade = null
            recordRecentQuestions(cached)
            _quizState.value = QuizUiState.Ready(questions = cached)
            prefetchNextQuizBatch(profile, initialDelayMs = 300L)
            return
        }

        _quizState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                prefetchJob?.let { job ->
                    if (job.isActive && prefetchedGrade == profile.grade) {
                        job.join()
                        val joinedCache = prefetchedQuestions
                        if (joinedCache != null && prefetchedGrade == profile.grade) {
                            prefetchedQuestions = null
                            prefetchedGrade = null
                            recordRecentQuestions(joinedCache)
                            _quizState.value = QuizUiState.Ready(questions = joinedCache)
                            prefetchNextQuizBatch(profile, initialDelayMs = 300L)
                            return@launch
                        }
                    }
                }

                val questions = withContext(Dispatchers.IO) {
                    aiRepository.generateQuizBatch(profile, recentQuizQuestions)
                }
                recordRecentQuestions(questions)
                _quizState.value = QuizUiState.Ready(questions = questions)
                prefetchNextQuizBatch(profile, initialDelayMs = 300L)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Quiz generation timed out", e)
                _quizState.value = QuizUiState.Error("Sorular hazırlanırken zaman aşımı oluştu. Tekrar deneyebilirsin.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Quiz generation failed", e)
                _quizState.value = QuizUiState.Error(
                    e.localizedMessage ?: "Sorular oluşturulurken bir hata oluştu."
                )
            }
        }
    }

    private fun prefetchNextQuizBatch(profile: GradeProfile, initialDelayMs: Long = 0L) {
        if (prefetchJob?.isActive == true && prefetchedGrade == profile.grade) return
        prefetchJob?.cancel()
        prefetchedGrade = profile.grade
        prefetchJob = viewModelScope.launch {
            try {
                if (initialDelayMs > 0L) delay(initialDelayMs)
                val batch = withContext(Dispatchers.IO) {
                    aiRepository.generateQuizBatch(profile, recentQuizQuestions)
                }
                if (activeProfile.value.grade == profile.grade) {
                    prefetchedQuestions = batch
                    prefetchedGrade = profile.grade
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Background quiz prefetch failed silently", e)
            }
        }
    }

    private fun recordRecentQuestions(questions: List<QuizQuestion>) {
        recentQuizQuestions.addAll(questions.map { it.question })
        if (recentQuizQuestions.size > 20) {
            recentQuizQuestions.subList(0, recentQuizQuestions.size - 20).clear()
        }
    }

    fun selectQuizOption(option: QuizOptionLetter) {
        val current = _quizState.value as? QuizUiState.Ready ?: return
        if (current.selectedOption != null || current.roundCompleted) return

        val isCorrect = option == current.currentQuestion.correctOption
        val updatedCorrectCount = if (isCorrect) current.correctCountInRound + 1 else current.correctCountInRound

        _quizState.value = current.copy(
            selectedOption = option,
            correctCountInRound = updatedCorrectCount
        )

        if (isCorrect) {
            val nextStreak = _currentStreak.value + 1
            _currentStreak.value = nextStreak
            _confettiTrigger.value += 1
            val pointsEarned = 10 + (nextStreak - 1) * 2
            viewModelScope.launch {
                val snapshot = prefsRepository.prefsFlow.first()
                prefsRepository.updateScoreAndBestStreak(
                    totalScore = snapshot.totalScore + pointsEarned,
                    bestStreak = nextStreak
                )
            }
        } else {
            _currentStreak.value = 0
        }
    }

    fun nextQuizQuestion() {
        val current = _quizState.value as? QuizUiState.Ready ?: return
        if (current.currentIndex + 1 < current.questions.size) {
            _quizState.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedOption = null
            )
        } else {
            _quizState.value = current.copy(roundCompleted = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
