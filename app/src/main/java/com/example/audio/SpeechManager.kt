package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

private const val TAG = "SpeechManager"

class SpeechManager(context: Context) {

    private val appContext = context.applicationContext
    private val ttsExecutor = Executors.newSingleThreadExecutor()
    private val ttsDispatcher = ttsExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false
    @Volatile
    private var isTtsInitializing = false
    @Volatile
    private var pendingSpeakText: Triple<String, Float, String?>? = null

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private val _isRecognitionAvailable = MutableStateFlow(true)
    val isRecognitionAvailable: StateFlow<Boolean> = _isRecognitionAvailable.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            val available = try {
                SpeechRecognizer.isRecognitionAvailable(appContext)
            } catch (_: Exception) {
                false
            }
            _isRecognitionAvailable.value = available
        }
    }

    private fun ensureTtsInitialized() {
        if (isTtsReady || isTtsInitializing) return
        isTtsInitializing = true
        scope.launch(ttsDispatcher) {
            try {
                val instance = TextToSpeech(appContext) { status ->
                    isTtsInitializing = false
                    if (status == TextToSpeech.SUCCESS) {
                        scope.launch(ttsDispatcher) {
                            val result = tts?.setLanguage(Locale.US)
                            if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED
                            ) {
                                tts?.setLanguage(Locale.ENGLISH)
                            }
                            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    _isSpeaking.value = true
                                }

                                override fun onDone(utteranceId: String?) {
                                    _isSpeaking.value = false
                                    _speakingMessageId.value = null
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onError(utteranceId: String?) {
                                    _isSpeaking.value = false
                                    _speakingMessageId.value = null
                                }

                                override fun onError(utteranceId: String?, errorCode: Int) {
                                    _isSpeaking.value = false
                                    _speakingMessageId.value = null
                                }
                            })
                            isTtsReady = true
                            pendingSpeakText?.let { (text, rate, msgId) ->
                                pendingSpeakText = null
                                speakInternal(text, rate, msgId)
                            }
                        }
                    } else {
                        Log.w(TAG, "TextToSpeech init failed with status=$status")
                    }
                }
                tts = instance
            } catch (e: Exception) {
                isTtsInitializing = false
                Log.w(TAG, "TextToSpeech creation failed", e)
            }
        }
    }

    fun warmUpTts() {
        ensureTtsInitialized()
    }

    fun speak(text: String, speechRate: Float = 0.92f, messageId: String? = null) {
        val sanitized = sanitizeForTts(text)
        if (sanitized.isBlank()) return

        if (!isTtsReady) {
            pendingSpeakText = Triple(sanitized, speechRate, messageId)
            ensureTtsInitialized()
            return
        }

        scope.launch(ttsDispatcher) {
            speakInternal(sanitized, speechRate, messageId)
        }
    }

    private fun speakInternal(sanitized: String, speechRate: Float, messageId: String?) {
        try {
            tts?.setSpeechRate(speechRate.coerceIn(0.7f, 1.2f))
            _speakingMessageId.value = messageId
            _isSpeaking.value = true
            val utteranceId = messageId ?: UUID.randomUUID().toString()
            tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.w(TAG, "TTS speak failed", e)
            _isSpeaking.value = false
            _speakingMessageId.value = null
        }
    }

    fun stopSpeaking() {
        pendingSpeakText = null
        _isSpeaking.value = false
        _speakingMessageId.value = null
        scope.launch(ttsDispatcher) {
            try {
                tts?.stop()
            } catch (_: Exception) {
            }
        }
    }

    fun clearSpeechError() {
        _speechError.value = null
    }

    fun startListening(onFinalResult: (String) -> Unit) {
        if (!_isRecognitionAvailable.value) {
            _speechError.value = "Bu cihazda ses tanıma servisi bulunamadı. Aşağıdaki kutuya yazarak sohbet edebilirsin!"
            return
        }
        stopSpeaking()
        _speechError.value = null
        _partialText.value = ""

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        _partialText.value = ""
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "Sesini tam duyamadım. Tekrar dene veya yazarak gönder!"
                            SpeechRecognizer.ERROR_AUDIO ->
                                "Mikrofon ses kaydında bir sorun oluştu."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Mikrofon izni gerekiyor."
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Ses tanıma için internet bağlantısı gerekiyor."
                            else ->
                                "Ses tanıma şu an kullanılamıyor. Yazarak devam edebilirsin!"
                        }
                        if (error != SpeechRecognizer.ERROR_CLIENT) {
                            _speechError.value = message
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        _partialText.value = ""
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            onFinalResult(text)
                        } else {
                            _speechError.value = "Konuşma algılanamadı. Tekrar deneyebilirsin!"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            _partialText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _isListening.value = true
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SpeechRecognizer", e)
            _isListening.value = false
            _speechError.value = "Ses tanıma başlatılamadı. Metin kutusunu kullanabilirsin!"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    private fun sanitizeForTts(raw: String): String {
        return raw.replace(Regex("[*_`#]"), "").trim()
    }

    fun release() {
        scope.cancel()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing TTS", e)
        } finally {
            ttsDispatcher.close()
            ttsExecutor.shutdown()
        }
    }
}
