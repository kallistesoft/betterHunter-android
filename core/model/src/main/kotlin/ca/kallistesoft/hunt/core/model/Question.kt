package ca.kallistesoft.hunt.core.model

import kotlin.random.Random

data class QuestionText(
    val q: String,
    val choices: List<String>,
    val explanation: String,
    /** Photo ID questions: where the animal lives, and a protected look-alike warning. */
    val habitat: String? = null,
    val confuse: String? = null,
)

/** Credit line required by the photo's free licence (Wikimedia Commons). */
data class ImageCredit(val author: String, val license: String, val page: String)

data class Question(
    val id: String,
    val scope: String,
    val chapter: String,
    val texts: Map<Lang, QuestionText>,
    val answerIndex: Int,
    val sourceRef: String = "",
    val reviewYear: Int? = null,
    /** Official page confirming the current rule (law, quebec.ca, rcmp.ca), if checked. */
    val sourceUrl: String? = null,
    /** Asset path of the photo for "What is it?" questions (e.g. `wildlife/abc.jpg`), with its licence credit. */
    val image: String? = null,
    val imageCredit: ImageCredit? = null,
    /** Width / height of the photo, so its space is reserved before it loads. */
    val imageAspect: Float? = null,
    /** The bank's index of each choice as shown ([withChoiceOrder]); null: the bank's order. */
    val choiceOrder: List<Int>? = null,
) {
    /** Sample questions are placeholders until reviewed questions are bundled. */
    val isSample: Boolean get() = id.startsWith("sample-")

    fun text(lang: Lang): QuestionText = texts[lang] ?: texts.getValue(Lang.FR)

    /**
     * The choices shown in [order] (bank indices), the same in every language; [answerIndex] follows the right
     * choice. No choice in the banks depends on its position ("all of the above"), P14.
     */
    fun withChoiceOrder(order: List<Int>): Question {
        val bank = inBankOrder()
        if (bank.texts.values.any { it.choices.indices.toList() != order.sorted() }) return this
        return bank.copy(
            texts = bank.texts.mapValues { (_, t) -> t.copy(choices = order.map(t.choices::get)) },
            answerIndex = order.indexOf(bank.answerIndex),
            choiceOrder = order,
        )
    }

    /** The choices in a random order (every quiz, P14). */
    fun withShuffledChoices(random: Random): Question =
        withChoiceOrder(inBankOrder().text(Lang.FR).choices.indices.shuffled(random))

    /** The question as in the bank: its choices in their original order. */
    fun inBankOrder(): Question {
        val order = choiceOrder ?: return this
        return copy(
            texts = texts.mapValues { (_, t) -> t.copy(choices = order.indices.map { t.choices[order.indexOf(it)] }) },
            answerIndex = order[answerIndex],
            choiceOrder = null,
        )
    }
}
