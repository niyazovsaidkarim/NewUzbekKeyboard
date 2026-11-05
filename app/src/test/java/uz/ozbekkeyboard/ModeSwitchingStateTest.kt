package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.ime.KeyboardMode
import uz.ozbekkeyboard.ime.ShiftState
import uz.ozbekkeyboard.layout.Key
import uz.ozbekkeyboard.layout.KeyboardLayout
import uz.ozbekkeyboard.layout.KeyType
import uz.ozbekkeyboard.theme.ThemeManager

class ModeSwitchingStateTest {

    private val screenWidth = 1080f
    private val screenHeight = 820f
    private val samsungDensity = 2.625f // Samsung Galaxy S/A series typical density

    @Test
    fun testTransitionAlphabetToSymbols1() {
        var currentMode = KeyboardMode.ALPHABET
        var shiftState = ShiftState.SHIFTED

        // User taps !#1 in bottom row
        val targetMode = if (currentMode == KeyboardMode.ALPHABET) {
            KeyboardMode.SYMBOLS_1
        } else {
            KeyboardMode.ALPHABET
        }
        if (targetMode != KeyboardMode.ALPHABET) {
            shiftState = ShiftState.OFF
        }
        currentMode = targetMode

        assertEquals(KeyboardMode.SYMBOLS_1, currentMode)
        assertEquals(ShiftState.OFF, shiftState)

        val rows = KeyboardLayout.getLayoutForMode(currentMode, showNumberRow = false)
        assertEquals(4, rows.size)

        // Verify page 1 row contents
        val r1 = rows[0].map { it.primaryText }
        assertEquals(listOf("+", "×", "÷", "=", "/", "_", "<", ">", "[", "]"), r1)

        val r2 = rows[1].map { it.primaryText }
        assertEquals(listOf("!", "@", "#", "£", "%", "^", "&", "*", "(", ")"), r2)

        val r3 = rows[2]
        assertEquals("1/2", r3.first().primaryText)
        assertEquals(KeyType.SWITCH_PAGE, r3.first().type)
        assertEquals(KeyType.BACKSPACE, r3.last().type)

        val r4 = rows[3]
        assertEquals("ABC", r4.first().primaryText)
        assertEquals(KeyType.SWITCH_SYMBOLS, r4.first().type)
    }

    @Test
    fun testTransitionSymbols1ToSymbols2() {
        var currentMode = KeyboardMode.SYMBOLS_1

        // User taps 1/2 in row 3
        currentMode = if (currentMode == KeyboardMode.SYMBOLS_1) {
            KeyboardMode.SYMBOLS_2
        } else {
            KeyboardMode.SYMBOLS_1
        }

        assertEquals(KeyboardMode.SYMBOLS_2, currentMode)

        val rows = KeyboardLayout.getLayoutForMode(currentMode, showNumberRow = false)
        assertEquals(4, rows.size)

        // Verify page 2 row contents
        val r1 = rows[0].map { it.primaryText }
        assertEquals(listOf("`", "~", "\\", "|", "{", "}", "€", "$", "₽", "₺"), r1)

        val r2 = rows[1].map { it.primaryText }
        assertEquals(listOf("°", "•", "○", "●", "□", "■", "♡", "◇"), r2)

        val r3 = rows[2]
        assertEquals("2/2", r3.first().primaryText)
        assertEquals(KeyType.SWITCH_PAGE, r3.first().type)
        val r3Middle = r3.subList(1, r3.size - 1).map { it.primaryText }
        assertEquals(listOf("☆", "·", "«", "»", "¡", "¿", "§"), r3Middle)
        assertEquals(KeyType.BACKSPACE, r3.last().type)

        val r4 = rows[3]
        assertEquals("ABC", r4.first().primaryText)
        assertEquals(KeyType.SWITCH_SYMBOLS, r4.first().type)
    }

    @Test
    fun testTransitionSymbols2ToSymbols1() {
        var currentMode = KeyboardMode.SYMBOLS_2

        // User taps 2/2 in row 3
        currentMode = if (currentMode == KeyboardMode.SYMBOLS_1) {
            KeyboardMode.SYMBOLS_2
        } else {
            KeyboardMode.SYMBOLS_1
        }

        assertEquals(KeyboardMode.SYMBOLS_1, currentMode)

        val rows = KeyboardLayout.getLayoutForMode(currentMode, showNumberRow = false)
        assertEquals("1/2", rows[2].first().primaryText)
    }

    @Test
    fun testTransitionSymbolsBackToAlphabet() {
        // From Symbols 1
        var mode1 = KeyboardMode.SYMBOLS_1
        val targetMode1 = if (mode1 == KeyboardMode.ALPHABET) KeyboardMode.SYMBOLS_1 else KeyboardMode.ALPHABET
        assertEquals(KeyboardMode.ALPHABET, targetMode1)

        val alphaRows1 = KeyboardLayout.getLayoutForMode(targetMode1, showNumberRow = false)
        assertEquals("q", alphaRows1[0][0].primaryText)
        assertEquals("!#1", alphaRows1[3][0].primaryText)

        // From Symbols 2
        var mode2 = KeyboardMode.SYMBOLS_2
        val targetMode2 = if (mode2 == KeyboardMode.ALPHABET) KeyboardMode.SYMBOLS_1 else KeyboardMode.ALPHABET
        assertEquals(KeyboardMode.ALPHABET, targetMode2)

        val alphaRows2 = KeyboardLayout.getLayoutForMode(targetMode2, showNumberRow = false)
        assertEquals("q", alphaRows2[0][0].primaryText)
        assertEquals("!#1", alphaRows2[3][0].primaryText)
    }

    @Test
    fun testBoundsCalculationForEveryMode() {
        val modes = listOf(KeyboardMode.ALPHABET, KeyboardMode.SYMBOLS_1, KeyboardMode.SYMBOLS_2)
        val numberRowOptions = listOf(true, false)

        for (mode in modes) {
            for (withNumberRow in numberRowOptions) {
                val rows = KeyboardLayout.getLayoutForMode(mode, withNumberRow)
                KeyboardLayout.calculateBounds(rows, screenWidth, screenHeight, samsungDensity)

                for ((rowIndex, row) in rows.withIndex()) {
                    for ((keyIndex, key) in row.withIndex()) {
                        assertFalse("Key ${key.primaryText} bounds must not be empty in mode $mode", key.bounds.isEmpty)
                        assertTrue("Key ${key.primaryText} width must be > 0", key.bounds.width > 0)
                        assertTrue("Key ${key.primaryText} height must be > 0", key.bounds.height > 0)
                        assertTrue("Key ${key.primaryText} left must be >= 0", key.bounds.left >= 0)
                        assertTrue("Key ${key.primaryText} right must be <= screenWidth", key.bounds.right <= screenWidth)
                        assertTrue("Key ${key.primaryText} top must be >= 0", key.bounds.top >= 0)
                        assertTrue("Key ${key.primaryText} bottom must be <= screenHeight", key.bounds.bottom <= screenHeight)

                        // Check hit testing reaches key center
                        assertTrue(
                            "Key ${key.primaryText} must contain its center (${key.bounds.centerX()}, ${key.bounds.centerY()})",
                            key.bounds.contains(key.bounds.centerX(), key.bounds.centerY())
                        )

                        // Check adjacent keys in the same row don't overlap
                        if (keyIndex > 0) {
                            val prevKey = row[keyIndex - 1]
                            assertTrue(
                                "Key ${key.primaryText} left (${key.bounds.left}) must be >= previous key right (${prevKey.bounds.right})",
                                key.bounds.left >= prevKey.bounds.right
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testRapidModeSwitching() {
        var mode = KeyboardMode.ALPHABET

        for (i in 0 until 100) {
            // Rapid cycle: Alphabet -> Sym1 -> Sym2 -> Sym1 -> Alphabet
            mode = KeyboardMode.SYMBOLS_1
            val sym1Rows = KeyboardLayout.getLayoutForMode(mode, false)
            KeyboardLayout.calculateBounds(sym1Rows, screenWidth, screenHeight, samsungDensity)
            assertFalse(sym1Rows.first().first().bounds.isEmpty)

            mode = KeyboardMode.SYMBOLS_2
            val sym2Rows = KeyboardLayout.getLayoutForMode(mode, false)
            KeyboardLayout.calculateBounds(sym2Rows, screenWidth, screenHeight, samsungDensity)
            assertFalse(sym2Rows.first().first().bounds.isEmpty)

            mode = KeyboardMode.SYMBOLS_1
            val sym1ReturnRows = KeyboardLayout.getLayoutForMode(mode, false)
            KeyboardLayout.calculateBounds(sym1ReturnRows, screenWidth, screenHeight, samsungDensity)
            assertFalse(sym1ReturnRows.first().first().bounds.isEmpty)

            mode = KeyboardMode.ALPHABET
            val alphaRows = KeyboardLayout.getLayoutForMode(mode, false)
            KeyboardLayout.calculateBounds(alphaRows, screenWidth, screenHeight, samsungDensity)
            assertFalse(alphaRows.first().first().bounds.isEmpty)
        }
    }

    @Test
    fun testNoMissingOrEmptyKeysInAnyMode() {
        for (mode in KeyboardMode.values()) {
            for (withNumberRow in listOf(true, false)) {
                val rows = KeyboardLayout.getLayoutForMode(mode, withNumberRow)
                assertTrue("Layout for $mode must have rows", rows.isNotEmpty())
                for (row in rows) {
                    assertTrue("Row must not be empty", row.isNotEmpty())
                    for (key in row) {
                        assertNotNull(key.primaryText)
                        assertTrue("Key primary text must not be blank", key.primaryText.isNotBlank())
                        assertNotNull(key.type)
                        assertTrue("Key weight must be > 0", key.weight > 0f)
                    }
                }
            }
        }
    }

    @Test
    fun testAllThemesLayoutCompatibility() {
        val themes = listOf(
            ThemeManager.BLACK_YELLOW,
            ThemeManager.MINIMAL_LIGHT,
            ThemeManager.MINIMAL_DARK
        )

        for (theme in themes) {
            assertNotNull(theme.name)
            assertTrue("Key corner radius must be positive", theme.keyCornerRadiusDp > 0f)

            // Verify colors are non-zero ARGB ints
            assertNotEquals(0, theme.keyboardBackground)
            assertNotEquals(0, theme.keyBackgroundAlphabet)
            assertNotEquals(0, theme.keyBackgroundAction)
            assertNotEquals(0, theme.keyTextPrimary)
        }
    }
}
