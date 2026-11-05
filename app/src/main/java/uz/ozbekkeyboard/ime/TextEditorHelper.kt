package uz.ozbekkeyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

object TextEditorHelper {

    fun handleDelete(inputConnection: InputConnection?) {
        if (inputConnection == null) return

        // 1. Check if there is an active selection
        val selectedText = inputConnection.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            inputConnection.commitText("", 1)
            return
        }

        // 2. Inspect text before cursor to safely delete grapheme clusters & surrogate pairs
        val beforeCursor = inputConnection.getTextBeforeCursor(4, 0)
        if (beforeCursor.isNullOrEmpty()) {
            inputConnection.deleteSurroundingText(1, 0)
            return
        }

        val lastChar = beforeCursor.last()
        var deleteCount = 1

        if (Character.isLowSurrogate(lastChar) && beforeCursor.length >= 2) {
            val secondLast = beforeCursor[beforeCursor.length - 2]
            if (Character.isHighSurrogate(secondLast)) {
                deleteCount = 2
            }
        }

        // Check for variation selector (e.g. U+FE0F in emoji) or combining characters
        if (beforeCursor.length >= deleteCount + 1) {
            val prevChar = beforeCursor[beforeCursor.length - deleteCount - 1]
            if (Character.isSurrogate(prevChar) || Character.getType(prevChar) == Character.NON_SPACING_MARK.toInt()) {
                deleteCount++
                if (beforeCursor.length >= deleteCount + 1 && Character.isHighSurrogate(beforeCursor[beforeCursor.length - deleteCount])) {
                    deleteCount++
                }
            }
        }

        inputConnection.deleteSurroundingText(deleteCount, 0)
    }

    /**
     * Handles cursor movement along text horizontally or vertically.
     * steps: negative for left, positive for right
     */
    fun moveCursor(inputConnection: InputConnection?, steps: Int) {
        if (inputConnection == null || steps == 0) return

        try {
            val textBefore = inputConnection.getTextBeforeCursor(1000, 0) ?: ""
            val textAfter = inputConnection.getTextAfterCursor(1000, 0) ?: ""

            val currentPos = textBefore.length
            val totalLen = currentPos + textAfter.length

            var newPos = currentPos + steps
            if (newPos < 0) newPos = 0
            if (newPos > totalLen) newPos = totalLen

            // Ensure we don't land in the middle of a surrogate pair
            val fullText = textBefore.toString() + textAfter.toString()
            if (newPos in 1 until fullText.length) {
                if (Character.isSurrogate(fullText[newPos]) && Character.isLowSurrogate(fullText[newPos])) {
                    newPos = if (steps > 0) (newPos + 1).coerceAtMost(totalLen) else (newPos - 1).coerceAtLeast(0)
                }
            }

            inputConnection.setSelection(newPos, newPos)
        } catch (_: Exception) {
            // Fallback for custom or restrictive editors that don't allow relative cursor selection
        }
    }

    /**
     * Checks if auto-capitalization should trigger based on EditorInfo and text before cursor.
     */
    fun shouldAutoCapitalize(inputConnection: InputConnection?, editorInfo: EditorInfo?): Boolean {
        if (inputConnection == null || editorInfo == null) return false

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

        return false
    }

    /**
     * Checks whether double space should insert a period.
     */
    fun handleSpaceKey(inputConnection: InputConnection?, doubleSpacePeriodEnabled: Boolean): Boolean {
        if (inputConnection == null) return false

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
        return true
    }

    /**
     * Extracts the current word prefix being typed before cursor for suggestions.
     */
    fun extractCurrentWordPrefix(inputConnection: InputConnection?): String {
        if (inputConnection == null) return ""
        val textBefore = inputConnection.getTextBeforeCursor(32, 0) ?: return ""
        if (textBefore.isEmpty() || Character.isWhitespace(textBefore.last())) return ""

        var start = textBefore.length - 1
        while (start >= 0) {
            val c = textBefore[start]
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                break
            }
            start--
        }
        return textBefore.substring(start + 1)
    }

    private fun isPunctuation(c: Char): Boolean {
        return c in listOf(',', '.', '!', '?', ':', ';', '(', ')', '[', ']', '{', '}', '/', '\\', '"', '+', '=', '-')
    }

    fun replaceWordPrefix(inputConnection: InputConnection?, oldPrefix: String, newWord: String) {
        if (inputConnection == null) return
        inputConnection.deleteSurroundingText(oldPrefix.length, 0)
        inputConnection.commitText("$newWord ", 1)
    }

    fun isPasswordField(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) return false
        val inputType = editorInfo.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }
}
