package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HunterProfileTest {
    private val quebecer = UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC)

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
    fun `passing the real exam makes a new hunter and shows the welcome`() {
        val p = quebecer.withExamPassed("ICAF")
        assertEquals(HunterType.NEW_HUNTER, p.hunterType)
        assertEquals(setOf("ICAF"), p.passedExams)
        assertTrue(p.newHunterBanner)
        assertTrue(p.isHunter)
        assertEquals(listOf("ICAF"), p.myExamIds) // the exam stays available for refreshers
    }
}
