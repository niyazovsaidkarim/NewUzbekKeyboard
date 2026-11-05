package uz.ozbekkeyboard.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardHelper(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val history = mutableListOf<String>()
    private val maxHistoryItems = 15

    fun getHistory(): List<String> {
        return history.toList()
    }

    fun syncFromClipboard(optInEnabled: Boolean, isSensitiveField: Boolean) {
        if (!optInEnabled || isSensitiveField) return

        try {
            val primaryClip = clipboardManager?.primaryClip ?: return
            if (primaryClip.itemCount > 0) {
                val text = primaryClip.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                if (!text.isNullOrEmpty() && !history.contains(text)) {
                    history.add(0, text)
                    if (history.size > maxHistoryItems) {
                        history.removeAt(history.lastIndex)
                    }
                }
            }
        } catch (_: SecurityException) {
            // Android clipboard access restrictions (e.g. background access)
        }
    }

    fun clearHistory() {
        history.clear()
    }

    fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Özbek Keyboard", text)
        clipboardManager?.setPrimaryClip(clip)
    }
}
