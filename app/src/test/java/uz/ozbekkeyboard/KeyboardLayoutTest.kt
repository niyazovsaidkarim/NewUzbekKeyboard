package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.ime.KeyboardMode
import uz.ozbekkeyboard.layout.KeyboardLayout
import uz.ozbekkeyboard.layout.KeyType

class KeyboardLayoutTest {

    @Test
    fun testAlphabetLayoutWithNumberRow() {
        val rows = KeyboardLayout.getLayoutForMode(KeyboardMode.ALPHABET, showNumberRow = true)
        // 5 rows: Number row + 3 QWERTY rows + Bottom utility row
        assertEquals(5, rows.size)

        // Number row
        val numberRow = rows[0]
        assertEquals(10, numberRow.size)
        assertEquals("1", numberRow.first().primaryText)
        assertEquals("0", numberRow.last().primaryText)
        assertTrue(numberRow.all { it.type == KeyType.NUMBER })

        // Row 1: QWERTY
        val row1 = rows[1]
        assertEquals(10, row1.size)
        assertEquals("q", row1[0].primaryText)
        assertEquals("p", row1[9].primaryText)

        // Row 2: ASDFGHJKL
        val row2 = rows[2]
        assertEquals(9, row2.size)
        assertEquals("a", row2[0].primaryText)
        assertEquals("l", row2[8].primaryText)

        // Row 3: [Shift] ZXCVBNM [Backspace]
        val row3 = rows[3]
        assertEquals(9, row3.size)
        assertEquals(KeyType.SHIFT, row3.first().type)
        assertEquals(KeyType.BACKSPACE, row3.last().type)

        // Bottom row: [!#1] [Globe] [,] [SPACE] [.] [ENTER]
        val bottomRow = rows[4]
        assertEquals(6, bottomRow.size)
        assertEquals(KeyType.SWITCH_SYMBOLS, bottomRow[0].type)
        assertEquals("!#1", bottomRow[0].primaryText)
        assertEquals(KeyType.GLOBE, bottomRow[1].type)
        assertEquals(KeyType.COMMA, bottomRow[2].type)
        assertEquals(KeyType.SPACE, bottomRow[3].type)
        assertEquals("Özbekça", bottomRow[3].primaryText)
        assertTrue("Space key must be significantly wider than surrounding keys", bottomRow[3].weight >= 3.0f)
        assertEquals(KeyType.PERIOD, bottomRow[4].type)
        assertEquals(KeyType.ENTER, bottomRow[5].type)
    }

    @Test
    fun testAlphabetLayoutWithoutNumberRow() {
        val rows = KeyboardLayout.getLayoutForMode(KeyboardMode.ALPHABET, showNumberRow = false)
        // 4 rows: 3 QWERTY rows + Bottom utility row
        assertEquals(4, rows.size)
        assertEquals("q", rows[0][0].primaryText)
    }

    @Test
    fun testSymbolPage1Layout() {
        val rows = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_1, showNumberRow = false)
        assertEquals(4, rows.size)

        // Row 1: + × ÷ = / _ < > [ ]
        val row1Texts = rows[0].map { it.primaryText }
        assertEquals(listOf("+", "×", "÷", "=", "/", "_", "<", ">", "[", "]"), row1Texts)

        // Row 2: ! @ # £ % ^ & * ( )
        val row2Texts = rows[1].map { it.primaryText }
        assertEquals(listOf("!", "@", "#", "£", "%", "^", "&", "*", "(", ")"), row2Texts)

        // Row 3: [1/2] - ' " : ; , ? [Backspace]
        val row3 = rows[2]
        assertEquals(KeyType.SWITCH_PAGE, row3.first().type)
        assertEquals("1/2", row3.first().primaryText)
        val middleSymbols = row3.subList(1, row3.size - 1).map { it.primaryText }
        assertEquals(listOf("-", "'", "\"", ":", ";", ",", "?"), middleSymbols)
        assertEquals(KeyType.BACKSPACE, row3.last().type)

        // Bottom row in symbol mode: [ABC] [Globe] [,] [SPACE] [.] [ENTER]
        val bottomRow = rows[3]
        assertEquals("ABC", bottomRow[0].primaryText)
        assertEquals(KeyType.SWITCH_SYMBOLS, bottomRow[0].type)
    }

    @Test
    fun testSymbolPage2Layout() {
        val rows = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_2, showNumberRow = false)
        assertEquals(4, rows.size)

        // Row 1: ` ~ \ | { } € $ ₽ ₺
        val row1Texts = rows[0].map { it.primaryText }
        assertEquals(listOf("`", "~", "\\", "|", "{", "}", "€", "$", "₽", "₺"), row1Texts)

        // Row 2: ° • ○ ● □ ■ ♡ ◇
        val row2Texts = rows[1].map { it.primaryText }
        assertEquals(listOf("°", "•", "○", "●", "□", "■", "♡", "◇"), row2Texts)

        // Row 3: [2/2] ☆ · « » ¡ ¿ § [Backspace]
        val row3 = rows[2]
        assertEquals(KeyType.SWITCH_PAGE, row3.first().type)
        assertEquals("2/2", row3.first().primaryText)
        val middleSymbols = row3.subList(1, row3.size - 1).map { it.primaryText }
        assertEquals(listOf("☆", "·", "«", "»", "¡", "¿", "§"), middleSymbols)
        assertEquals(KeyType.BACKSPACE, row3.last().type)
    }

    @Test
    fun testRemovedLowPrioritySymbolsAreNotPresent() {
        val forbidden = listOf("₩", "¥", "¤", "♣", "♠")
        val page1 = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_1, showNumberRow = true)
        val page2 = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_2, showNumberRow = true)

        val allSymbols = (page1 + page2).flatMap { it }.map { it.primaryText }.toSet()

        for (sym in forbidden) {
            assertFalse("Symbol '$sym' must be removed from symbol layouts", allSymbols.contains(sym))
        }
    }
}
