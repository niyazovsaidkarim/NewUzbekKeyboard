package uz.ozbekkeyboard

import android.content.ClipDescription
import android.os.PersistableBundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uz.ozbekkeyboard.clipboard.ClipboardHelper
import uz.ozbekkeyboard.ime.TextEditorHelper

class SecurityAndPrivacyTest {

    @Before
    fun setUp() {
        ClipboardHelper.clearSharedHistory()
    }

    @Test
    fun testSensitiveClipMetadataDetection() {
        // Null description must safely return false without exception
        assertFalse(ClipboardHelper.isClipMarkedSensitive(null))
    }

    @Test
    fun testCreditCardDetection() {
        // Valid Luhn test card (Visa)
        assertTrue("Visa card should be detected as sensitive",
            ClipboardHelper.isSensitiveContent("4111 1111 1111 1111"))
        assertTrue("Card without spaces should be detected",
            ClipboardHelper.isSensitiveContent("4111111111111111"))
        assertTrue("Card with hyphens should be detected",
            ClipboardHelper.isSensitiveContent("4111-1111-1111-1111"))

        // Uzcard test pattern (starts with 8600) with valid Luhn
        assertTrue("Uzcard format should be detected as sensitive",
            ClipboardHelper.isSensitiveContent("8600 0000 0000 0000"))

        // Humo test pattern (starts with 9860) with valid Luhn
        assertTrue("Humo format should be detected as sensitive",
            ClipboardHelper.isSensitiveContent("9860 0000 0000 0000"))
    }

    @Test
    fun testOtpAndVerificationCodeDetection() {
        // Standalone 6-digit SMS confirmation code
        assertTrue("Standalone 6-digit code must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("582910"))

        // Standalone 4-digit PIN
        assertTrue("Standalone 4-digit code must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("4921"))

        // SMS confirmation keywords
        assertTrue("Keyword 'kod' with digits must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("Tasdiqlash kodi: 948201"))
        assertTrue("Keyword 'SMS-kod' must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("SMS-kod: 481920"))
        assertTrue("Keyword 'parol' with digits must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("Bir martalik parol: 83921"))
        assertTrue("Keyword 'code' must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("Verification code: 829104"))
    }

    @Test
    fun testAuthTokenDetection() {
        // JWT Token
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        assertTrue("JWT tokens must be flagged sensitive", ClipboardHelper.isSensitiveContent(jwt))

        // Bearer token
        assertTrue("Bearer tokens must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))

        // GitHub token
        assertTrue("GitHub tokens must be flagged sensitive",
            ClipboardHelper.isSensitiveContent("ghp_1234567890abcdefghijklmnopqrstuvwxyz"))
    }

    @Test
    fun testNormalUzbekTextNotFlaggedAsSensitive() {
        assertFalse(ClipboardHelper.isSensitiveContent("Salom, qandaysiz?"))
        assertFalse(ClipboardHelper.isSensitiveContent("Bugun soat 15:30 da uchrashamiz."))
        assertFalse(ClipboardHelper.isSensitiveContent("Alisher Navoiy ko'chasi 45-uy"))
        assertFalse(ClipboardHelper.isSensitiveContent("Kitob narxi 45000 so'm"))
        assertFalse(ClipboardHelper.isSensitiveContent("Özbekiston mustaqil davlat"))
    }

    @Test
    fun testClipboardPurgeSynchronizedAcrossInstances() {
        // Verify shared cache starts empty
        assertTrue(ClipboardHelper.getSharedHistory().isEmpty())

        // Calling clearSharedHistory must leave cache empty
        ClipboardHelper.clearSharedHistory()
        assertTrue(ClipboardHelper.getSharedHistory().isEmpty())
    }

    @Test
    fun testIncognitoAndNoPersonalizedLearningDetection() {
        // 1. Field with IME_FLAG_NO_PERSONALIZED_LEARNING (Chrome Incognito, Firefox Private)
        val incognitoInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        assertTrue("Incognito / No-Personalized-Learning fields must be classified as sensitive",
            TextEditorHelper.isPasswordField(incognitoInfo))

        // 2. Standard password fields
        val passwordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertTrue("Text password must be classified as sensitive",
            TextEditorHelper.isPasswordField(passwordInfo))

        val numberPinInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        assertTrue("Numeric PIN must be classified as sensitive",
            TextEditorHelper.isPasswordField(numberPinInfo))

        // 3. Normal field without flags
        val normalInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        assertFalse("Normal text field must not be classified as sensitive",
            TextEditorHelper.isPasswordField(normalInfo))
    }
}
