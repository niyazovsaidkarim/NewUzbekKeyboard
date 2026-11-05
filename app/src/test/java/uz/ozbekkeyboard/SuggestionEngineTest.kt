package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.suggestions.SuggestionEngine

class SuggestionEngineTest {

    private val engine = SuggestionEngine()

    @Test
    fun testSuggestionsForUzbekWords() {
        val suggestions = engine.getSuggestions("sal", 3)
        assertTrue("Suggestions for 'sal' should contain 'salom'", suggestions.contains("salom"))

        val rahmatSuggestions = engine.getSuggestions("rah", 3)
        assertTrue("Suggestions for 'rah' should contain 'rahmat'", rahmatSuggestions.contains("rahmat"))
    }

    @Test
    fun testCasePreservation() {
        val capitalized = engine.getSuggestions("Sal", 3)
        assertTrue("Capitalized prefix 'Sal' should return capitalized 'Salom'", capitalized.contains("Salom"))

        val allCaps = engine.getSuggestions("SAL", 3)
        assertTrue("Uppercase prefix 'SAL' should return uppercase 'SALOM'", allCaps.contains("SALOM"))
    }

    @Test
    fun testEmptyOrShortPrefix() {
        assertTrue(engine.getSuggestions("", 3).isEmpty())
        assertTrue(engine.getSuggestions("s", 3).isEmpty())
    }

    @Test
    fun testModernUzbekReformedWords() {
        val suggestions = engine.getSuggestions("özb", 3)
        assertTrue("Suggestions should support modern reform letters", suggestions.any { it.startsWith("özb") })
    }
}
