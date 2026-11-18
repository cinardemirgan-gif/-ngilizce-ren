package com.example.data

import com.example.model.GradeProfile
import com.example.model.QuizOptionLetter
import com.example.model.QuizQuestion
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

class AiEnglishRepository {

    private val quizResponseSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "questions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "question" to Schema.string(
                                description = "Short grade-appropriate English quiz question."
                            ),
                            "emoji" to Schema.string(
                                description = "One relevant emoji."
                            ),
                            "optionA" to Schema.string(description = "Choice A"),
                            "optionB" to Schema.string(description = "Choice B"),
                            "optionC" to Schema.string(description = "Choice C"),
                            "correctOption" to Schema.enumeration(
                                values = listOf("A", "B", "C"),
                                description = "Correct option: A, B, or C"
                            ),
                            "explanationTr" to Schema.string(
                                description = "1 short friendly Turkish explanation sentence."
                            )
                        )
                    ),
                    description = "Exactly 3 multiple-choice questions."
                )
            )
        )
    }

    // Ultra-fast Gemini 3.5 Flash-Lite model for low-latency quiz generation
    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.75f
                responseMimeType = "application/json"
                responseSchema = quizResponseSchema
            }
        )
    }

    fun startVoiceChat(profile: GradeProfile): Chat {
        val systemPrompt = buildString {
            appendLine("You are Buddy, a cheerful AI English teacher for a Turkish student in ${profile.labelTr} (${profile.cefrBadge}).")
            appendLine("Topics: ${profile.topicsDescriptionEn}.")
            appendLine("Grammar: ${profile.grammarDescriptionEn}.")
            if (profile.grade <= 4) {
                appendLine("Reply in 1-2 VERY short simple English sentences + a friendly question. Optional short Turkish hint in parentheses only if needed.")
            } else {
                appendLine("Reply in 2-3 clear conversational English sentences + an engaging follow-up question. Gently model corrections if needed.")
            }
            appendLine("Plain text only (no markdown/asterisks), as replies are spoken via TTS.")
        }

        val chatModel = Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content { text(systemPrompt) },
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )

        val initialGreeting = when {
            profile.grade <= 2 -> "Hi! I am Buddy! What is your name?"
            profile.grade <= 4 -> "Hello! I am Buddy, your English friend! How are you today?"
            profile.grade <= 6 -> "Hey there! I'm Buddy, your AI English partner. What topic would you like to chat about today?"
            else -> "Hi! I'm Buddy, your English speaking partner. Ready to practice Grade ${profile.grade} English together? How was your day?"
        }

        return chatModel.startChat(
            history = listOf(
                content("model") { text(initialGreeting) }
            )
        )
    }

    fun sendChatMessageStream(chat: Chat, message: String): Flow<String> {
        return chat.sendMessageStream(message).map { chunk ->
            chunk.text.orEmpty()
        }
    }

    suspend fun generateQuizBatch(
        profile: GradeProfile,
        avoidQuestions: List<String> = emptyList()
    ): List<QuizQuestion> {
        val avoidClause = if (avoidQuestions.isNotEmpty()) {
            "Avoid repeating: ${avoidQuestions.takeLast(6).joinToString(" | ")}."
        } else {
            ""
        }

        val prompt = """
            Create 3 short, fun English multiple-choice questions for a Turkish Grade ${profile.grade} student (${profile.cefrBadge}).
            Topics: ${profile.topicsDescriptionEn}.
            Grammar: ${profile.grammarDescriptionEn}.
            Rules:
            - Strictly 3 distinct options (optionA, optionB, optionC), 1 correct (A, B, or C).
            - Keep questions & options concise.
            - explanationTr: 1 short encouraging Turkish sentence.
            $avoidClause
        """.trimIndent()

        val response = withTimeout(45_000L) {
            quizModel.generateContent(prompt)
        }

        val jsonText = response.text
            ?: throw IllegalStateException("Yapay zeka boş yanıt döndürdü.")

        val root = JSONObject(jsonText)
        val array = root.getJSONArray("questions")
        val list = mutableListOf<QuizQuestion>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    question = item.getString("question").trim(),
                    emoji = item.optString("emoji", "✨").ifBlank { "✨" },
                    optionA = item.getString("optionA").trim(),
                    optionB = item.getString("optionB").trim(),
                    optionC = item.getString("optionC").trim(),
                    correctOption = QuizOptionLetter.fromString(item.getString("correctOption")),
                    explanationTr = item.getString("explanationTr").trim()
                )
            )
        }
        if (list.isEmpty()) {
            throw IllegalStateException("Soru listesi oluşturulamadı.")
        }
        return list
    }
}
