package ca.kallistesoft.hunt.feature.quiz

import ca.kallistesoft.hunt.core.model.Question

/** Saved state of a run: each question's choice order ("2,0,3,1"; empty for the bank's order). */
internal fun List<Question>.savedChoiceOrders(): ArrayList<String> =
    mapTo(ArrayList(size)) { it.choiceOrder?.joinToString(",").orEmpty() }

/** The bank questions back in the order they were shown, from [savedChoiceOrders]. */
internal fun List<Question>.withSavedChoiceOrders(saved: List<String>?): List<Question> =
    if (saved == null || saved.size != size) {
        this
    } else {
        mapIndexed { i, q ->
            saved[i].takeIf { it.isNotEmpty() }?.split(',')?.mapNotNull(String::toIntOrNull)?.let(q::withChoiceOrder) ?: q
        }
    }
