package ca.kallistesoft.hunt.core.testing

import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuestionText

/** Small content set: two training exams and the photo game, three questions each, and field skill questions; answer B. */
object TestContent {
    val pal = Exam("PAL", mapOf(Lang.FR to "Examen PPA", Lang.EN to "PAL exam"), listOf("federal"), questionCount = 3, minutes = 1)
    val icaf = Exam("ICAF", mapOf(Lang.FR to "ICAF", Lang.EN to "ICAF"), listOf("QC"), questionCount = 3)
    val photoGame = Exam(PHOTO_ID_EXAM, mapOf(Lang.FR to "Qu'est-ce que c'est?", Lang.EN to "What is it?"), listOf("wildlife-id"))

    fun question(id: String, scope: String, image: String? = null, chapter: String = "c") = Question(
        id = id,
        scope = scope,
        chapter = chapter,
        texts = mapOf(
            Lang.FR to QuestionText("$id ?", listOf("A", "B", "C", "D"), "Explication $id"),
            Lang.EN to QuestionText("$id?", listOf("A", "B", "C", "D"), "Explanation $id"),
        ),
        answerIndex = 1,
        image = image,
    )

    val content = Content(
        exams = listOf(pal, icaf, photoGame),
        questionsByScope = mapOf(
            "federal" to (1..3).map { question("federal-$it", "federal") },
            "QC" to (1..3).map { question("qc-$it", "QC") },
            // Field skills (new hunters): two questions per skill, except safe firearm handling.
            "hunter-common" to listOf("l07", "l08", "l04", "l10").flatMap { ch ->
                (1..2).map { question("hc-$ch-$it", "hunter-common", chapter = ch) }
            },
            "wildlife-id" to (1..3).map { question("id-$it", "wildlife-id", image = "wildlife/$it.jpg") },
        ),
    )
}
