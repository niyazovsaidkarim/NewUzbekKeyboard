package uz.ozbekkeyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import java.text.BreakIterator

object TextEditorHelper {

    fun handleDelete(inputConnection: InputConnection?) {
        if (inputConnection == null) return

        // 1. Check if there is an active selection
        val selectedText = inputConnection.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            inputConnection.commitText("", 1)
            return
        }

        // 2. Inspect text before cursor to safely delete full grapheme clusters
        val beforeCursor = inputConnection.getTextBeforeCursor(64, 0)
        if (beforeCursor.isNullOrEmpty()) {
            inputConnection.deleteSurroundingText(1, 0)
            return
        }

        val deleteCount = calculateDeleteCount(beforeCursor.toString())
        inputConnection.deleteSurroundingText(deleteCount, 0)
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
        val start = iterator.previous()
        return if (start != BreakIterator.DONE && end > start) {
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
                val next = iterator.following(targetPos)
                if (next == BreakIterator.DONE) {
                    targetPos = fullText.length
                    break
                }
                targetPos = next
            }
        } else {
            for (i in 0 until -steps) {
                val prev = iterator.preceding(targetPos)
                if (prev == BreakIterator.DONE) {
                    targetPos = 0
                    break
                }
                targetPos = prev
            }
        }
        return targetPos
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

        var i = textBefore.length - 1
        while (i >= 0 && !Character.isWhitespace(textBefore[i])) {
            val c = textBefore[i]
            if (c in listOf('.', ',', '!', '?', ';', ':', '(', ')', '[', ']', '"', '/')) {
                break
            }
            i--
        }

        return textBefore.substring(i + 1)
    }

    /**
     * Replaces the currently typed word prefix with the selected suggestion.
     */
    fun replaceWordPrefix(inputConnection: InputConnection?, prefix: String, suggestion: String) {
        if (inputConnection == null) return
        inputConnection.deleteSurroundingText(prefix.length, 0)
        inputConnection.commitText("$suggestion ", 1)
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
}
