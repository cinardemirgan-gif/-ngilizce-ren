package com.example.model

enum class SchoolLevel(
    val titleTr: String,
    val subtitleTr: String,
    val emoji: String,
    val grades: List<Int>
) {
    PRIMARY(
        titleTr = "İlkokul",
        subtitleTr = "1. – 4. Sınıf • Eğlenceli kelimeler ve ilk cümleler",
        emoji = "🎒",
        grades = listOf(1, 2, 3, 4)
    ),
    MIDDLE(
        titleTr = "Ortaokul",
        subtitleTr = "5. – 8. Sınıf • Günlük diyaloglar, gramer ve pratik",
        emoji = "🏫",
        grades = listOf(5, 6, 7, 8)
    )
}

data class SampleWord(
    val english: String,
    val turkish: String,
    val emoji: String
)

data class GradeProfile(
    val grade: Int,
    val schoolLevel: SchoolLevel,
    val cefrBadge: String,
    val titleTr: String,
    val focusSummaryTr: String,
    val topicsDescriptionEn: String,
    val grammarDescriptionEn: String,
    val ttsSpeechRate: Float,
    val starterPrompts: List<String>,
    val sampleWords: List<SampleWord>
) {
    val labelTr: String get() = "${schoolLevel.titleTr} • $grade. Sınıf"
}

object GradeCurriculum {
    val profiles: Map<Int, GradeProfile> = listOf(
        GradeProfile(
            grade = 1,
            schoolLevel = SchoolLevel.PRIMARY,
            cefrBadge = "Başlangıç",
            titleTr = "1. Sınıf İngilizce",
            focusSummaryTr = "Selamlaşma, renkler, sayılar (1-10) ve sevimli hayvanlar",
            topicsDescriptionEn = "Greetings (Hello, Hi, Bye), colors, numbers 1-10, basic animals (cat, dog, bird, fish), classroom objects",
            grammarDescriptionEn = "Very short 2-4 word phrases only: 'Hello!', 'I am...', 'It is red.', 'Yes / No', 'What is your name?'",
            ttsSpeechRate = 0.84f,
            starterPrompts = listOf(
                "Hello! I am happy.",
                "What color is the sun?",
                "Let's count to five!"
            ),
            sampleWords = listOf(
                SampleWord("Apple", "Elma", "🍎"),
                SampleWord("Cat", "Kedi", "🐱"),
                SampleWord("Blue", "Mavi", "🔵"),
                SampleWord("Sun", "Güneş", "☀️")
            )
        ),
        GradeProfile(
            grade = 2,
            schoolLevel = SchoolLevel.PRIMARY,
            cefrBadge = "Temel • A0",
            titleTr = "2. Sınıf İngilizce",
            focusSummaryTr = "Meyveler, vücudumuz, oyuncaklar ve 'I like / I have' ifadeleri",
            topicsDescriptionEn = "Friends, body parts, pets, fruits, toys, numbers 1-20, simple feelings (happy, sad)",
            grammarDescriptionEn = "Simple present with 'I like...', 'I have...', 'It is a...', 'How old are you?'",
            ttsSpeechRate = 0.86f,
            starterPrompts = listOf(
                "I like apples and bananas!",
                "I have a cute cat.",
                "How are you today?"
            ),
            sampleWords = listOf(
                SampleWord("Banana", "Muz", "🍌"),
                SampleWord("Happy", "Mutlu", "😊"),
                SampleWord("Toy", "Oyuncak", "🧸"),
                SampleWord("Rabbit", "Tavşan", "🐰")
            )
        ),
        GradeProfile(
            grade = 3,
            schoolLevel = SchoolLevel.PRIMARY,
            cefrBadge = "Temel • A1-",
            titleTr = "3. Sınıf İngilizce",
            focusSummaryTr = "Aile, hava durumu, şehirler ve 'I can / I can't' yetenekleri",
            topicsDescriptionEn = "Family members, weather (sunny, rainy), feelings, toys & games, transportation, nature, abilities",
            grammarDescriptionEn = "'I can / I can't', 'There is / There are', 'It is sunny', 'Where is the...?', 'Do you like...?'",
            ttsSpeechRate = 0.88f,
            starterPrompts = listOf(
                "I can swim and jump!",
                "It is sunny today.",
                "Tell me about your family."
            ),
            sampleWords = listOf(
                SampleWord("Sunny", "Güneşli", "🌤️"),
                SampleWord("Bicycle", "Bisiklet", "🚲"),
                SampleWord("Family", "Aile", "👨‍👩‍👧"),
                SampleWord("Swim", "Yüzmek", "🏊")
            )
        ),
        GradeProfile(
            grade = 4,
            schoolLevel = SchoolLevel.PRIMARY,
            cefrBadge = "İlkokul • A1",
            titleTr = "4. Sınıf İngilizce",
            focusSummaryTr = "Günlük rutinler, saatler, meslekler, ülkeler ve yiyecekler",
            topicsDescriptionEn = "Classroom rules, nationalities, daily routines, free time activities, telling the time, jobs, clothes",
            grammarDescriptionEn = "Simple Present Tense ('I wake up at 7'), 'May I...?', 'What time is it?', 'She is a doctor', 'Would you like...?'",
            ttsSpeechRate = 0.90f,
            starterPrompts = listOf(
                "What time do you wake up?",
                "I want to be a pilot!",
                "What is your favorite food?"
            ),
            sampleWords = listOf(
                SampleWord("Morning", "Sabah", "🌅"),
                SampleWord("Teacher", "Öğretmen", "👩‍🏫"),
                SampleWord("Clock", "Saat", "⏰"),
                SampleWord("Jacket", "Ceket", "🧥")
            )
        ),
        GradeProfile(
            grade = 5,
            schoolLevel = SchoolLevel.MIDDLE,
            cefrBadge = "Ortaokul • A1",
            titleTr = "5. Sınıf İngilizce",
            focusSummaryTr = "Okul dersleri, hobiler, sağlık, kutlamalar ve hayvanat bahçesi",
            topicsDescriptionEn = "School subjects, town places & directions, games & hobbies, daily routines, health, movies, party/festivals, animal shelter",
            grammarDescriptionEn = "Simple Present, 'like + V-ing', 'must/mustn't' for rules, 'should' for health advice, Present Continuous ('is playing')",
            ttsSpeechRate = 0.94f,
            starterPrompts = listOf(
                "My favorite school subject is Science.",
                "What do you do in your free time?",
                "Let's talk about movies!"
            ),
            sampleWords = listOf(
                SampleWord("Library", "Kütüphane", "📚"),
                SampleWord("Science", "Fen Bilimleri", "🔬"),
                SampleWord("Festival", "Bayram / Şenlik", "🎉"),
                SampleWord("Healthy", "Sağlıklı", "🥗")
            )
        ),
        GradeProfile(
            grade = 6,
            schoolLevel = SchoolLevel.MIDDLE,
            cefrBadge = "Ortaokul • A1+",
            titleTr = "6. Sınıf İngilizce",
            focusSummaryTr = "Kahvaltı kültürü, şehir hayatı, tatiller, meslekler ve çevre",
            topicsDescriptionEn = "Life & routines, yummy breakfast, downtown, weather & emotions, at the fair, occupations, holidays, bookworms, saving the planet",
            grammarDescriptionEn = "Present Continuous vs Simple Present, Comparatives ('bigger than'), Simple Past Tense ('was/were', regular/irregular verbs), 'should'",
            ttsSpeechRate = 0.96f,
            starterPrompts = listOf(
                "How was your last summer holiday?",
                "Big cities are noisier than villages.",
                "How can we save the planet?"
            ),
            sampleWords = listOf(
                SampleWord("Recycle", "Geri dönüştürmek", "♻️"),
                SampleWord("Holiday", "Tatil", "🏖️"),
                SampleWord("Delicious", "Lezzetli", "🥐"),
                SampleWord("Curious", "Meraklı", "🔍")
            )
        ),
        GradeProfile(
            grade = 7,
            schoolLevel = SchoolLevel.MIDDLE,
            cefrBadge = "Ortaokul • A2",
            titleTr = "7. Sınıf İngilizce",
            focusSummaryTr = "Kişilik özellikleri, sporlar, biyografiler, vahşi yaşam ve gezegenler",
            topicsDescriptionEn = "Appearance & personality, sports, biographies, wild animals, television, celebrations, dreams/future, public buildings, environment, planets",
            grammarDescriptionEn = "Comparatives & Superlatives, Simple Past Tense, Future Tense ('will' predictions), 'used to' / modals ('must', 'have to'), frequency adverbs",
            ttsSpeechRate = 0.98f,
            starterPrompts = listOf(
                "What will the world look like in 2050?",
                "Tell me about an endangered wild animal.",
                "Which planet is the biggest in our solar system?"
            ),
            sampleWords = listOf(
                SampleWord("Generous", "Cömert", "🤝"),
                SampleWord("Orbit", "Yörünge", "🪐"),
                SampleWord("Wildlife", "Vahşi yaşam", "🦁"),
                SampleWord("Achievement", "Başarı", "🏆")
            )
        ),
        GradeProfile(
            grade = 8,
            schoolLevel = SchoolLevel.MIDDLE,
            cefrBadge = "LGS • A2+",
            titleTr = "8. Sınıf İngilizce",
            focusSummaryTr = "Arkadaşlık, gençlik yaşamı, mutfak, iletişim, internet, macera ve bilim",
            topicsDescriptionEn = "Friendship, teen life, in the kitchen, on the phone, the internet, adventures, tourism, chores, science, natural forces",
            grammarDescriptionEn = "Invitations/accepting/refusing ('Would you like to...?', 'I'd love to, but...'), Present Perfect ('Have you ever...?'), 'prefer / would rather', Passive Voice basics, Conditionals",
            ttsSpeechRate = 1.0f,
            starterPrompts = listOf(
                "Have you ever tried an extreme sport?",
                "How do you use the internet safely?",
                "Let's practice inviting a friend!"
            ),
            sampleWords = listOf(
                SampleWord("Reliable", "Güvenilir", "🛡️"),
                SampleWord("Invention", "İcat", "💡"),
                SampleWord("Adventure", "Macera", "🧗"),
                SampleWord("Destination", "Varış yeri", "🗺️")
            )
        )
    ).associateBy { it.grade }

    fun getProfile(grade: Int): GradeProfile = profiles[grade.coerceIn(1, 8)] ?: profiles.getValue(3)
}

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

sealed interface VoiceAiUiState {
    data object Idle : VoiceAiUiState
    data object Generating : VoiceAiUiState
    data class Error(val message: String) : VoiceAiUiState
}

enum class QuizOptionLetter(val label: String, val index: Int) {
    A("A", 0),
    B("B", 1),
    C("C", 2);

    companion object {
        fun fromString(value: String): QuizOptionLetter =
            when (value.trim().uppercase()) {
                "B" -> B
                "C" -> C
                else -> A
            }
    }
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val emoji: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val correctOption: QuizOptionLetter,
    val explanationTr: String
) {
    fun optionText(letter: QuizOptionLetter): String = when (letter) {
        QuizOptionLetter.A -> optionA
        QuizOptionLetter.B -> optionB
        QuizOptionLetter.C -> optionC
    }
}

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Ready(
        val questions: List<QuizQuestion>,
        val currentIndex: Int = 0,
        val selectedOption: QuizOptionLetter? = null,
        val correctCountInRound: Int = 0,
        val roundCompleted: Boolean = false
    ) : QuizUiState {
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
    }
    data class Error(val message: String) : QuizUiState
}
