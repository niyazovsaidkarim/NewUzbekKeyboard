package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.data.UzbekLetters
import uz.ozbekkeyboard.ime.TextEditorHelper
import uz.ozbekkeyboard.settings.TrackpadSensitivity

class GraphemeClusterTest {

    @Test
    fun testAsciiCursorNavigation() {
        val text = "Salom"
        // Move forward from 0
        assertEquals(1, TextEditorHelper.calculateNewCursorPosition(text, 0, 1))
        assertEquals(3, TextEditorHelper.calculateNewCursorPosition(text, 0, 3))
        assertEquals(5, TextEditorHelper.calculateNewCursorPosition(text, 0, 5))
        // Clamped at end
        assertEquals(5, TextEditorHelper.calculateNewCursorPosition(text, 0, 10))

        // Move backward from end
        assertEquals(4, TextEditorHelper.calculateNewCursorPosition(text, 5, -1))
        assertEquals(2, TextEditorHelper.calculateNewCursorPosition(text, 5, -3))
        assertEquals(0, TextEditorHelper.calculateNewCursorPosition(text, 5, -5))
        // Clamped at start
        assertEquals(0, TextEditorHelper.calculateNewCursorPosition(text, 5, -10))
    }

    @Test
    fun testEmojiGraphemeCursorNavigation() {
        // "Uz 🇺🇿 ok" -> "Uz " (3 chars) + flag emoji (4 chars: \uD83C\uDDFA\uD83C\uDDFF) + " ok" (3 chars) = 10 chars
        val text = "Uz 🇺🇿 ok"
        val flagStart = 3
        val flagEnd = 7

        // Moving +1 from before the flag must jump directly to flagEnd, NOT splitting the emoji!
        val nextPos = TextEditorHelper.calculateNewCursorPosition(text, flagStart, 1)
        assertEquals("Cursor must jump past entire flag emoji without splitting surrogates", flagEnd, nextPos)

        // Moving -1 from flagEnd must jump directly to flagStart
        val prevPos = TextEditorHelper.calculateNewCursorPosition(text, flagEnd, -1)
        assertEquals("Cursor must jump backward before flag emoji without splitting surrogates", flagStart, prevPos)
    }

    @Test
    fun testSurrogatePairEmojiCursorNavigation() {
        // "Salom 😊!" -> "Salom " (6) + "😊" (2 chars) + "!" (1)
        val text = "Salom 😊!"
        val emojiStart = 6
        val emojiEnd = 8

        val forward = TextEditorHelper.calculateNewCursorPosition(text, emojiStart, 1)
        assertEquals(emojiEnd, forward)

        val backward = TextEditorHelper.calculateNewCursorPosition(text, emojiEnd, -1)
        assertEquals(emojiStart, backward)
    }

    @Test
    fun testMultilineCursorNavigation() {
        val multiline = "Qator 1\nQator 2"
        val newlinePos = 7 // length of "Qator 1"

        // Cursor at newline moving forward
        val next = TextEditorHelper.calculateNewCursorPosition(multiline, newlinePos, 1)
        assertEquals(8, next) // lands on 'Q' of Qator 2
    }

    @Test
    fun testGraphemeDeleteCount() {
        // 1. Regular ASCII char
        assertEquals(1, TextEditorHelper.calculateDeleteCount("Salom"))

        // 2. Surrogate pair emoji
        assertEquals(2, TextEditorHelper.calculateDeleteCount("Salom 😊"))

        // 3. Multi-character flag emoji (4 code units)
        assertEquals(4, TextEditorHelper.calculateDeleteCount("Oʻzbekiston 🇺🇿"))

        // 4. Uzbek letter with modifier letter apostrophe
        // "Bogʻ" -> 'ʻ' is U+02BB (1 char). Single backspace deletes the modifier.
        assertEquals(1, TextEditorHelper.calculateDeleteCount("Bogʻ"))

        // 5. Empty string
        assertEquals(0, TextEditorHelper.calculateDeleteCount(""))
    }

    @Test
    fun testTrackpadSensitivityValues() {
        assertEquals(32f, TrackpadSensitivity.SLOW.thresholdDp, 0.01f)
        assertEquals(24f, TrackpadSensitivity.NORMAL.thresholdDp, 0.01f)
        assertEquals(16f, TrackpadSensitivity.FAST.thresholdDp, 0.01f)

        assertEquals(TrackpadSensitivity.NORMAL, TrackpadSensitivity.fromId("unknown"))
        assertEquals(TrackpadSensitivity.FAST, TrackpadSensitivity.fromId("fast"))
        assertEquals(TrackpadSensitivity.SLOW, TrackpadSensitivity.fromId("slow"))
    }

    @Test
    fun testUzbekLongPressPrimaryAndSecondaryVariants() {
        // Lowercase primary variants
        assertEquals("ö", UzbekLetters.getVariants("o", false).first())
        assertEquals("ğ", UzbekLetters.getVariants("g", false).first())
        assertEquals("ş", UzbekLetters.getVariants("s", false).first())
        assertEquals("ç", UzbekLetters.getVariants("c", false).first())

        // Uppercase primary variants
        assertEquals("Ö", UzbekLetters.getVariants("O", true).first())
        assertEquals("Ğ", UzbekLetters.getVariants("G", true).first())
        assertEquals("Ş", UzbekLetters.getVariants("S", true).first())
        assertEquals("Ç", UzbekLetters.getVariants("C", true).first())

        // Punctuation variants
        assertTrue(UzbekLetters.getVariants("'", false).contains(UzbekLetters.TUTUQ_STANDARD))
        assertTrue(UzbekLetters.getVariants("\"", false).contains("«"))
        assertTrue(UzbekLetters.getVariants("-", false).contains("—"))
        assertTrue(UzbekLetters.getVariants(".", false).contains("..."))
    }
}
