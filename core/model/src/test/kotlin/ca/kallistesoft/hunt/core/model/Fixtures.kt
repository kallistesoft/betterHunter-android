package ca.kallistesoft.hunt.core.model

val testExam = Exam(
    id = "PAL",
    names = mapOf(Lang.FR to "Examen PAL", Lang.EN to "PAL exam"),
    scopes = listOf("federal"),
    questionCount = 3,
    passMark = 0.6,
)

fun testQuestion(id: String, answer: Int = 1) = Question(
    id = id,
    scope = "federal",
    chapter = "ch01",
    answerIndex = answer,
    sourceRef = "cfsc_fr.pdf p.61",
    texts = mapOf(
        Lang.FR to QuestionText("Question $id?", listOf("Un", "Deux", "Trois", "Quatre"), "Explication $id."),
        Lang.EN to QuestionText("Question $id (en)?", listOf("One", "Two", "Three", "Four"), "Explanation $id."),
    ),
)

val testPool = (1..5).map { testQuestion("q$it") }
