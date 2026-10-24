package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** "What kind of hunter are you?" (onboarding, step 1). */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class HunterType {
    /** Preparing for the licence or the hunter course: the app is built around exam practice. */
    WORKING_ON_IT,

    /**
     * Licensed, whatever the number of seasons: today's questions, field skills, the season (P11). Progress saved
     * before the two hunter profiles were merged still says "NEW_HUNTER" or "EXPERIENCED": both read as this one.
     */
    @JsonNames("NEW_HUNTER", "EXPERIENCED")
    HUNTER,
    ;

    companion object {
        /** The saved name of a type, old names included; null when it isn't one. */
        fun fromName(name: String): HunterType? = when (name) {
            "NEW_HUNTER", "EXPERIENCED" -> HUNTER
            else -> entries.firstOrNull { it.name == name }
        }
    }
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
    QC(listOf("ICAF", "PAL")),
    AB(listOf("ABHE", "PAL")),

    /** Elsewhere in Canada: the PAL until more provinces have their course. */
    OTHER(listOf("PAL")),
}
