package uz.ozbekkeyboard.ime

import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import java.text.BreakIterator

object TextEditorHelper {

    fun handleDelete(inputConnection: InputConnection?) {
        if (inputConnection == null) return

        try {
            // 1. Check if there is an active selection
            val selectedText = inputConnection.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                inputConnection.commitText("", 1)
                return
            }

            // 2. Inspect text before cursor to safely delete full grapheme clusters
            val beforeCursor = inputConnection.getTextBeforeCursor(64, 0)
            if (beforeCursor.isNullOrEmpty()) {
                sendFallbackDelete(inputConnection)
                return
            }

            val deleteCount = calculateDeleteCount(beforeCursor.toString())
            val deleted = inputConnection.deleteSurroundingText(deleteCount, 0)
            if (!deleted) {
                // Some custom editors or WebViews return false for deleteSurroundingText, fallback to keyevent
                sendFallbackDelete(inputConnection)
            }
        } catch (_: Exception) {
            // Robust fallback if IPC or custom InputConnection throws an exception
            sendFallbackDelete(inputConnection)
        }
    }

    private fun sendFallbackDelete(inputConnection: InputConnection?) {
        if (inputConnection == null) return
        try {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        } catch (_: Exception) {
            // Ignore if client app is completely unresponsive
        }
    }

    /**
     * Pure function calculating the number of UTF-16 code units belonging to the
     * last grapheme cluster before cursor.
     */
    fun calculateDeleteCount(textBeforeCursor: String): Int {
        if (textBeforeCursor.isEmpty()) return 0
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(textBeforeCursor)
        val end = iterator.last()
        var start = iterator.previous()
        return if (start != BreakIterator.DONE && end > start) {
            // Defensive check for Regional Indicator emoji flag pairs on JVMs lacking full UAX #29 RI pairing
            if (start > 0 && start < textBeforeCursor.length) {
                val cpAfter = Character.codePointAt(textBeforeCursor, start)
                val cpBefore = Character.codePointBefore(textBeforeCursor, start)
                if (isRegionalIndicator(cpAfter) && isRegionalIndicator(cpBefore)) {
                    val prev = iterator.previous()
                    if (prev != BreakIterator.DONE) {
                        start = prev
                    } else {
                        start = Character.offsetByCodePoints(textBeforeCursor, start, -1)
                    }
                }
            }
            end - start
        } else {
            1
        }
    }

    /**
     * Handles cursor movement along text horizontally or across lines.
     * steps: negative for left, positive for right
     */
    fun moveCursor(inputConnection: InputConnection?, steps: Int) {
        if (inputConnection == null || steps == 0) return

        try {
            val textBefore = inputConnection.getTextBeforeCursor(2048, 0) ?: ""
            val textAfter = inputConnection.getTextAfterCursor(2048, 0) ?: ""

            val currentPos = textBefore.length
            val fullText = textBefore.toString() + textAfter.toString()
            if (fullText.isEmpty()) return

            val targetPos = calculateNewCursorPosition(fullText, currentPos, steps)
            inputConnection.setSelection(targetPos, targetPos)
        } catch (_: Exception) {
            // Fallback for custom or restrictive editors that don't allow relative cursor selection
        }
    }

    /**
     * Pure function calculating the new cursor position after advancing or retreating
     * by `steps` Unicode grapheme clusters.
     */
    fun calculateNewCursorPosition(fullText: String, currentPos: Int, steps: Int): Int {
        if (fullText.isEmpty() || steps == 0) return currentPos
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(fullText)
        var targetPos = currentPos.coerceIn(0, fullText.length)

        if (steps > 0) {
            for (i in 0 until steps) {
                var next = iterator.following(targetPos)
                if (next == BreakIterator.DONE) {
                    targetPos = fullText.length
                    break
                }
                if (next > 0 && next < fullText.length) {
                    val cpBefore = Character.codePointBefore(fullText, next)
                    val cpAfter = Character.codePointAt(fullText, next)
                    if (isRegionalIndicator(cpBefore) && isRegionalIndicator(cpAfter)) {
                        val skip = iterator.following(next)
                        if (skip != BreakIterator.DONE) {
                            next = skip
                        }
                    }
                }
                targetPos = next
            }
        } else {
            for (i in 0 until -steps) {
                var prev = iterator.preceding(targetPos)
                if (prev == BreakIterator.DONE) {
                    targetPos = 0
                    break
                }
                if (prev > 0 && prev < fullText.length) {
                    val cpBefore = Character.codePointBefore(fullText, prev)
                    val cpAfter = Character.codePointAt(fullText, prev)
                    if (isRegionalIndicator(cpBefore) && isRegionalIndicator(cpAfter)) {
                        val skip = iterator.preceding(prev)
                        if (skip != BreakIterator.DONE) {
                            prev = skip
                        }
                    }
                }
                targetPos = prev
            }
        }
        return targetPos
    }

    private fun isRegionalIndicator(codePoint: Int): Boolean {
        return codePoint in 0x1F1E6..0x1F1FF
    }

    /**
     * Checks if auto-capitalization should trigger based on EditorInfo and text before cursor.
     */
    fun shouldAutoCapitalize(inputConnection: InputConnection?, editorInfo: EditorInfo?): Boolean {
        if (inputConnection == null || editorInfo == null) return false

        return try {
            val inputType = editorInfo.inputType
            if (inputType and InputType.TYPE_MASK_CLASS != InputType.TYPE_CLASS_TEXT) {
                return false
            }

            // Characters flag (e.g. state code, serial number)
            if (inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS != 0) {
                return true
            }

            val textBefore = inputConnection.getTextBeforeCursor(64, 0) ?: return true
            if (textBefore.isEmpty()) return true

            // Words flag (e.g. Person Name)
            if (inputType and InputType.TYPE_TEXT_FLAG_CAP_WORDS != 0) {
                return Character.isWhitespace(textBefore.last())
            }

            // Sentences flag
            if (inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0) {
                var i = textBefore.length - 1
                while (i >= 0 && Character.isWhitespace(textBefore[i])) {
                    i--
                }
                if (i < 0) return true // Only whitespaces or empty start of line
                val endChar = textBefore[i]
                return endChar == '.' || endChar == '?' || endChar == '!' || endChar == '\n'
            }

            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether double space should insert a period.
     */
    fun handleSpaceKey(inputConnection: InputConnection?, doubleSpacePeriodEnabled: Boolean): Boolean {
        if (inputConnection == null) return false

        return try {
            if (doubleSpacePeriodEnabled) {
                val textBefore = inputConnection.getTextBeforeCursor(2, 0)
                if (!textBefore.isNullOrEmpty() && textBefore.length >= 2) {
                    val lastChar = textBefore.last()
                    val prevChar = textBefore[textBefore.length - 2]
                    if (lastChar == ' ' && !Character.isWhitespace(prevChar) && prevChar != '.') {
                        inputConnection.deleteSurroundingText(1, 0)
                        inputConnection.commitText(". ", 1)
                        return true
                    }
                }
            }

            inputConnection.commitText(" ", 1)
            true
        } catch (_: Exception) {
            try {
                inputConnection.commitText(" ", 1)
            } catch (_: Exception) {}
            true
        }
    }

    /**
     * Extracts the current word prefix being typed before cursor for suggestions.
     */
    fun extractCurrentWordPrefix(inputConnection: InputConnection?): String {
        if (inputConnection == null) return ""
        return try {
            val textBefore = inputConnection.getTextBeforeCursor(32, 0) ?: return ""
            if (textBefore.isEmpty() || Character.isWhitespace(textBefore.last())) return ""

            var i = textBefore.length - 1
            while (i >= 0 && !Character.isWhitespace(textBefore[i])) {
                val c = textBefore[i]
                if (c in listOf('.', ',', '!', '?', ';', ':', '(', ')', '[', ']', '"', '/')) {
                    break
                }
                i--
            }

            textBefore.substring(i + 1)
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Replaces the currently typed word prefix with the selected suggestion.
     */
    fun replaceWordPrefix(inputConnection: InputConnection?, prefix: String, suggestion: String) {
        if (inputConnection == null) return
        try {
            inputConnection.deleteSurroundingText(prefix.length, 0)
            inputConnection.commitText("$suggestion ", 1)
        } catch (_: Exception) {
            try {
                inputConnection.commitText("$suggestion ", 1)
            } catch (_: Exception) {}
        }
    }

    /**
     * Checks if the current editor is a password or PIN field.
     */
    fun isPasswordField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                info.inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    /**
     * Checks if the field is strictly numeric, phone, or date/time.
     */
    fun isNumericInput(info: EditorInfo?): Boolean {
        if (info == null) return false
        val inputClass = info.inputType and InputType.TYPE_MASK_CLASS
        return inputClass == InputType.TYPE_CLASS_NUMBER ||
                inputClass == InputType.TYPE_CLASS_PHONE ||
                inputClass == InputType.TYPE_CLASS_DATETIME
    }

    /**
     * Checks if the field is specialized for email or URI.
     */
    fun isEmailOrUrl(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_URI
    }
}
