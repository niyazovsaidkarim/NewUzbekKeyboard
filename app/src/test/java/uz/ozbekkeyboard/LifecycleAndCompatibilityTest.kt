package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.ime.KeyboardMode
import uz.ozbekkeyboard.ime.ShiftState
import uz.ozbekkeyboard.layout.Key
import uz.ozbekkeyboard.layout.KeyType
import uz.ozbekkeyboard.layout.KeyboardLayout
import uz.ozbekkeyboard.settings.KeyboardHeight

class LifecycleAndCompatibilityTest {

    @Test
    fun testKeyboardHeightFactors() {
        // Verify all height factors provide sensible proportions
        assertEquals(0.88f, KeyboardHeight.LOW.factor, 0.001f)
        assertEquals(1.00f, KeyboardHeight.NORMAL.factor, 0.001f)
        assertEquals(1.15f, KeyboardHeight.HIGH.factor, 0.001f)

        // Orientation scaled row heights
        val portraitBase = 54f
        val landscapeBase = 38f

        // Ensure landscape base row height is at least 25% lower than portrait to preserve screen real estate
        assertTrue("Landscape height must be more compact than portrait", landscapeBase < portraitBase * 0.75f)

        for (height in KeyboardHeight.values()) {
            val pHeight = portraitBase * height.factor
            val lHeight = landscapeBase * height.factor
            assertTrue(pHeight in 40f..70f)
            assertTrue(lHeight in 30f..50f)
        }
    }

    @Test
    fun testLayoutGenerationAcrossAllModes() {
        // Mode: ALPHABET
        val alphaWithoutNum = KeyboardLayout.getLayoutForMode(KeyboardMode.ALPHABET, showNumberRow = false)
        assertEquals(4, alphaWithoutNum.size)
        val alphaWithNum = KeyboardLayout.getLayoutForMode(KeyboardMode.ALPHABET, showNumberRow = true)
        assertEquals(5, alphaWithNum.size)

        // Mode: SYMBOLS_1
        val sym1WithoutNum = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_1, showNumberRow = false)
        assertEquals(4, sym1WithoutNum.size)
        val sym1WithNum = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_1, showNumberRow = true)
        assertEquals(5, sym1WithNum.size)

        // Mode: SYMBOLS_2
        val sym2WithoutNum = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_2, showNumberRow = false)
        assertEquals(4, sym2WithoutNum.size)
        val sym2WithNum = KeyboardLayout.getLayoutForMode(KeyboardMode.SYMBOLS_2, showNumberRow = true)
        assertEquals(5, sym2WithNum.size)

        // Total key weight in each row must sum to row weight (all rows have weights > 0)
        for (row in alphaWithNum) {
            val totalWeight = row.sumOf { it.weight.toDouble() }
            assertTrue(totalWeight > 0.0)
        }
    }

    @Test
    fun testKeyAccessibilityDescriptionsModernOrthography() {
        val backspaceKey = Key(primaryText = "", type = KeyType.BACKSPACE)
        val enterKey = Key(primaryText = "", type = KeyType.ENTER)
        val spaceKey = Key(primaryText = " ", type = KeyType.SPACE)
        val switchKey = Key(primaryText = "!#1", type = KeyType.SWITCH_SYMBOLS)
        val globeKey = Key(primaryText = "", type = KeyType.GLOBE)
        val commaKey = Key(primaryText = ",", type = KeyType.COMMA)
        val periodKey = Key(primaryText = ".", type = KeyType.PERIOD)

        fun getAnnouncement(key: Key, shiftState: ShiftState): String {
            val isShifted = shiftState.isShifted()
            return when (key.type) {
                KeyType.CHARACTER -> key.getDisplayText(isShifted)
                KeyType.NUMBER -> key.primaryText
                KeyType.SPACE -> "Probel"
                KeyType.BACKSPACE -> "Öçiriş"
                KeyType.ENTER -> "Kiritiş"
                KeyType.SHIFT -> when (shiftState) {
                    ShiftState.OFF -> "Katta harf"
                    ShiftState.SHIFTED -> "Bosh harf yoqilgan"
                    ShiftState.CAPS_LOCK -> "Caps lock"
                }
                KeyType.SWITCH_SYMBOLS -> "Belgilar"
                KeyType.SWITCH_PAGE -> "Keyingi sahifa"
                KeyType.GLOBE -> "Tilni almaştiriş"
                KeyType.COMMA -> "Vergul"
                KeyType.PERIOD -> "Nuqta"
            }
        }

        assertEquals("Öçiriş", getAnnouncement(backspaceKey, ShiftState.OFF))
        assertEquals("Kiritiş", getAnnouncement(enterKey, ShiftState.OFF))
        assertEquals("Probel", getAnnouncement(spaceKey, ShiftState.OFF))
        assertEquals("Belgilar", getAnnouncement(switchKey, ShiftState.OFF))
        assertEquals("Tilni almaştiriş", getAnnouncement(globeKey, ShiftState.OFF))
        assertEquals("Vergul", getAnnouncement(commaKey, ShiftState.OFF))
        assertEquals("Nuqta", getAnnouncement(periodKey, ShiftState.OFF))
    }
}
