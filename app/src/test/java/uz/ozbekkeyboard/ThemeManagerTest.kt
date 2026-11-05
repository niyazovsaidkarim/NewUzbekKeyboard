package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.theme.ThemeManager

class ThemeManagerTest {

    @Test
    fun testBlackYellowThemeColors() {
        val theme = ThemeManager.BLACK_YELLOW
        assertEquals("Black & Yellow", theme.name)
        // Background nearly black
        assertEquals(0xFF121212.toInt(), theme.keyboardBackground)
        // Alphabet keys bright yellow
        assertEquals(0xFFFFD500.toInt(), theme.keyBackgroundAlphabet)
        // Number keys warm orange
        assertEquals(0xFFFF8C00.toInt(), theme.keyBackgroundNumber)
        // Action keys white
        assertEquals(0xFFFFFFFF.toInt(), theme.keyBackgroundAction)
        // Text black
        assertEquals(0xFF000000.toInt(), theme.keyTextPrimary)
        // Toolbar dark gray
        assertEquals(0xFF1C1C1E.toInt(), theme.toolbarBackground)
    }

    @Test
    fun testMinimalThemesExist() {
        assertNotNull(ThemeManager.MINIMAL_LIGHT)
        assertNotNull(ThemeManager.MINIMAL_DARK)
        assertNotEquals(ThemeManager.MINIMAL_LIGHT.keyboardBackground, ThemeManager.MINIMAL_DARK.keyboardBackground)
    }
}
