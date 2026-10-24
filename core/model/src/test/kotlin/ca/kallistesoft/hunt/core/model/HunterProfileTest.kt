package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HunterProfileTest {
    private val quebecer = UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC)

    @Test
    fun `saved type names read back, the old new and experienced hunters as hunters`() {
        assertEquals(HunterType.WORKING_ON_IT, HunterType.fromName("WORKING_ON_IT"))
        assertEquals(HunterType.HUNTER, HunterType.fromName("HUNTER"))
        assertEquals(HunterType.HUNTER, HunterType.fromName("NEW_HUNTER"))
        assertEquals(HunterType.HUNTER, HunterType.fromName("EXPERIENCED"))
        assertNull(HunterType.fromName("SOMETHING_ELSE"))
    }

    @Test
    fun `onboarding is done once the profile is saved`() {
        assertFalse(UserProgress().onboarded)
        assertTrue(quebecer.onboarded)
        assertFalse(quebecer.isHunter)
        assertEquals(setOf(Province.QC), quebecer.provinces)
    }

    @Test
    fun `the province sets the quizzes, hunter course first`() {
        assertEquals(listOf("ICAF"), quebecer.myExamIds)
        assertEquals(listOf("ABHE", "PAL"), quebecer.withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.AB).myExamIds)
        assertEquals(listOf("PAL"), quebecer.withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.OTHER).myExamIds)
    }

    @Test
    fun `other provinces add their quizzes after the home province's, each once`() {
        val p = quebecer.withProvince(Province.OTHER, true).withProvince(Province.AB, true)
        assertEquals(listOf("ICAF", "ABHE", "PAL"), p.myExamIds)
        assertEquals(listOf("ICAF", "PAL"), p.withProvince(Province.AB, false).myExamIds)
    }

    @Test
    fun `the last province can't be switched off`() {
        assertEquals(quebecer, quebecer.withProvince(Province.QC, false))
        val both = quebecer.withProvince(Province.AB, true)
        assertEquals(listOf("ABHE", "PAL"), both.withProvince(Province.QC, false).myExamIds)
    }

    @Test
    fun `passing the real exam makes a hunter and shows the welcome`() {
        val p = quebecer.withExamPassed("ICAF")
        assertEquals(HunterType.HUNTER, p.hunterType)
        assertEquals(setOf("ICAF"), p.passedExams)
        assertTrue(p.newHunterBanner)
        assertTrue(p.isHunter)
        assertEquals(listOf("ICAF"), p.myExamIds) // the exam stays available for refreshers
    }

    @Test
    fun `settings change the profile without losing the provinces added`() {
        val p = quebecer.withProvince(Province.OTHER, true).withHomeProvince(Province.AB)
        assertEquals(Province.AB, p.homeProvince)
        assertEquals(listOf("ABHE", "PAL", "ICAF"), p.myExamIds)
        assertEquals(setOf(Weapon.BOW), p.withWeapons(setOf(Weapon.BOW)).weapons)
        assertEquals(p, p.withWeapons(emptySet())) // at least one weapon
    }

    @Test
    fun `going back to working on it drops the hunter welcome`() {
        val hunter = quebecer.withExamPassed("ICAF")
        assertTrue(hunter.withHunterType(HunterType.HUNTER).newHunterBanner)
        val back = hunter.withHunterType(HunterType.WORKING_ON_IT)
        assertFalse(back.isHunter)
        assertFalse(back.newHunterBanner)
        assertEquals(setOf("ICAF"), back.passedExams)
    }

    @Test
    fun `the daily goal is one of the offered values`() {
        assertEquals(UserProgress.DEFAULT_DAILY_GOAL, quebecer.dailyGoal)
        assertEquals(30, quebecer.withDailyGoal(30).dailyGoal)
        assertEquals(quebecer, quebecer.withDailyGoal(7))
    }
}
