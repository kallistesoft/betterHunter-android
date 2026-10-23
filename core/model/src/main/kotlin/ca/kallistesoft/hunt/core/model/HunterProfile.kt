package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable

/** "What kind of hunter are you?" (onboarding, step 1). */
@Serializable
enum class HunterType {
    /** Preparing for the licence or the hunter course: the app is built around exam practice. */
    WORKING_ON_IT,
    NEW_HUNTER,
    EXPERIENCED,
}

/** "What do you hunt with?" (onboarding, step 2; several allowed). */
@Serializable
enum class Weapon { FIREARM, BOW, CROSSBOW }

/** "Where do you hunt?" (onboarding, step 3). The province sets the quizzes (DECISIONS.md, P10). */
@Serializable
enum class Province(
    /** Exams this province adds, its hunter course first; the PAL is optional, so it comes last. */
    val examIds: List<String>,
) {
    QC(listOf("ICAF")),
    AB(listOf("ABHE", "PAL")),

    /** Elsewhere in Canada: the PAL until more provinces have their course. */
    OTHER(listOf("PAL")),
}
