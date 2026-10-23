package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable

/**
 * What a new hunter practises instead of exams (DECISIONS.md, P11): each skill draws on chapters of the hunter-course
 * and PAL banks, written `scope:chapter`. The grouping is a first pass, to check against the questions.
 */
@Serializable
enum class FieldSkill(val chapters: Set<String>) {
    SHOT_PLACEMENT(setOf("federal:m08", "hunter-common:l07")),
    AFTER_SHOT(setOf("hunter-common:l08", "hunter-common:l09", "AB:ab-biggame")),
    SAFE_HANDLING(setOf("federal:intro", "federal:m04", "federal:m07", "hunter-common:l05")),
    RULES(
        setOf(
            "QC:l01", "QC:l03",
            "AB:ab-prohib", "AB:ab-seasons", "AB:ab-lic", "AB:ab-access",
            "hunter-common:l04", "hunter-common:l12", "federal:m11",
        ),
    ),
    SURVIVAL(setOf("hunter-common:l10", "hunter-common:l11")),
    ;

    fun covers(q: Question): Boolean = "${q.scope}:${q.chapter}" in chapters

    companion object {
        /** The skills a hunter practises: safe firearm handling only for those who hunt with a firearm. */
        fun forWeapons(weapons: Set<Weapon>): List<FieldSkill> =
            entries.filter { it != SAFE_HANDLING || weapons.isEmpty() || Weapon.FIREARM in weapons }
    }
}

/** A skill's state on the Field skills screen. */
data class SkillSummary(
    val skill: FieldSkill,
    val questionCount: Int,
    /** Share of the skill's questions answered right in the last 3 months (0..1). */
    val readiness: Float,
    /** Right answers older than 3 months, which count as unanswered again. */
    val expired: Int,
) {
    val mastered: Boolean get() = questionCount > 0 && readiness >= 1f

    /** Was mastered and faded: everything was right, some answers have expired. */
    val faded: Boolean get() = !mastered && expired > 0 && readiness + expired.toFloat() / questionCount >= 1f
}

/** The questions of each skill, from the banks of the user's provinces (plus the common and federal ones). */
fun Content.skillPools(skills: List<FieldSkill>, provinces: Set<Province>): Map<FieldSkill, List<Question>> {
    val scopes = setOf("federal", "hunter-common") + provinces.map { it.name }
    val all = scopes.flatMap(::questionsInScope)
    return skills.associateWith { skill -> all.filter(skill::covers).distinctBy { it.id } }
}
