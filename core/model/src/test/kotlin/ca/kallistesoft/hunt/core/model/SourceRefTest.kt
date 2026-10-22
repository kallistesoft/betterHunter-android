package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SourceRefTest {
    @Test
    fun `a manual reference links to the PDF page`() {
        val ref = SourceRef.parse("cfsc_fr.pdf p.61", Lang.EN)!!
        assertEquals("CFSC student manual (RCMP, 2014), p. 61", ref.label)
        assertEquals(manuals.getValue("cfsc_fr.pdf").url + "#page=61", ref.url)
    }

    @Test
    fun `unknown manuals and other formats are ignored`() {
        assertNull(SourceRef.parse("other.pdf p.3", Lang.FR))
        assertNull(SourceRef.parse("", Lang.FR))
        assertNull(SourceRef.parse("cfsc_fr.pdf page 3", Lang.FR))
    }

    @Test
    fun `language follows the locale`() {
        assertEquals(Lang.FR, Lang.forLanguage("fr"))
        assertEquals(Lang.EN, Lang.forLanguage("en"))
        assertEquals(Lang.EN, Lang.forLanguage("de"))
    }
}
