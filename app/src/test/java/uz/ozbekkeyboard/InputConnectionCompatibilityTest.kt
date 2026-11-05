package uz.ozbekkeyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.ime.TextEditorHelper

class InputConnectionCompatibilityTest {

    @Test
    fun testIsNumericInput() {
        val numberInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        assertTrue(TextEditorHelper.isNumericInput(numberInfo))

        val phoneInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_PHONE
        }
        assertTrue(TextEditorHelper.isNumericInput(phoneInfo))

        val datetimeInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
        }
        assertTrue(TextEditorHelper.isNumericInput(datetimeInfo))

        val textInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse(TextEditorHelper.isNumericInput(textInfo))

        assertFalse(TextEditorHelper.isNumericInput(null))
    }

    @Test
    fun testIsEmailOrUrl() {
        val emailInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        assertTrue(TextEditorHelper.isEmailOrUrl(emailInfo))

        val webEmailInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        }
        assertTrue(TextEditorHelper.isEmailOrUrl(webEmailInfo))

        val urlInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }
        assertTrue(TextEditorHelper.isEmailOrUrl(urlInfo))

        val textInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse(TextEditorHelper.isEmailOrUrl(textInfo))

        assertFalse(TextEditorHelper.isEmailOrUrl(null))
    }

    @Test
    fun testIsPasswordField() {
        val passwordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertTrue(TextEditorHelper.isPasswordField(passwordInfo))

        val visiblePasswordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        assertTrue(TextEditorHelper.isPasswordField(visiblePasswordInfo))

        val webPasswordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        assertTrue(TextEditorHelper.isPasswordField(webPasswordInfo))

        val numberPasswordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        assertTrue(TextEditorHelper.isPasswordField(numberPasswordInfo))

        val normalText = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse(TextEditorHelper.isPasswordField(normalText))

        assertFalse(TextEditorHelper.isPasswordField(null))
    }

    @Test
    fun testGraphemeDeleteBoundaryConditions() {
        assertEquals(0, TextEditorHelper.calculateDeleteCount(""))
        assertEquals(1, TextEditorHelper.calculateDeleteCount("a"))
        assertEquals(1, TextEditorHelper.calculateDeleteCount("abc"))
        // Grapheme cluster with emoji surrogate pair (2 code units)
        assertEquals(2, TextEditorHelper.calculateDeleteCount("abc🎯"))
        // Regional indicator flag emoji (4 code units: \uD83C\uDDFA\uD83C\uDDFF)
        assertEquals(4, TextEditorHelper.calculateDeleteCount("Tashkent 🇺🇿"))
    }

    @Test
    fun testCursorCalculationBoundaries() {
        val str = "Salom"
        // No move
        assertEquals(2, TextEditorHelper.calculateNewCursorPosition(str, 2, 0))
        // Extreme negative clamped to 0
        assertEquals(0, TextEditorHelper.calculateNewCursorPosition(str, 2, -100))
        // Extreme positive clamped to length
        assertEquals(str.length, TextEditorHelper.calculateNewCursorPosition(str, 2, 100))
    }
}
