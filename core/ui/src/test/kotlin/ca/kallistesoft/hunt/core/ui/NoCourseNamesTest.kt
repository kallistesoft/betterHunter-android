package ca.kallistesoft.hunt.core.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The app's strings never name a provincial hunter course or its publisher (DECISIONS.md, P13). */
class NoCourseNamesTest {
    private val names = listOf("ICAF", "ICAA", "ABHE", "AHEIA", "FédéCP", "FedeCP", "Fédération québécoise", "Conservation and Hunter Education")

    @Test
    fun `no string names a provincial course or its publisher`() {
        val files = listOf("values", "values-fr").map { File("src/main/res/$it/strings.xml") }
        for (file in files) {
            assertTrue("missing $file", file.exists())
            val text = file.readText()
            names.forEach { assertFalse("$file names \"$it\"", text.contains(it, ignoreCase = true)) }
        }
    }
}
