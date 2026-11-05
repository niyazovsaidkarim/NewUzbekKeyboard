package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test

class ImeStateTest {

    @Test
    fun testDefaultImePackageMatching() {
        val packageName = "uz.ozbekkeyboard"

        // Short form
        val shortImeId = "uz.ozbekkeyboard/.ime.OzbekInputMethodService"
        assertTrue(shortImeId.startsWith("$packageName/"))

        // Full form
        val fullImeId = "uz.ozbekkeyboard/uz.ozbekkeyboard.ime.OzbekInputMethodService"
        assertTrue(fullImeId.startsWith("$packageName/"))

        // Other keyboard (e.g. Samsung Keyboard)
        val samsungImeId = "com.sec.android.inputmethod/.SamsungKeypad"
        assertFalse(samsungImeId.startsWith("$packageName/"))

        // Gboard
        val gboardImeId = "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME"
        assertFalse(gboardImeId.startsWith("$packageName/"))
    }
}
