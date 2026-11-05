package uz.ozbekkeyboard.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

class ClipboardHelper(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    companion object {
        private const val MAX_HISTORY_ITEMS = 15
        private val sharedHistory = mutableListOf<String>()
        private val lock = Any()

        fun getSharedHistory(): List<String> {
            synchronized(lock) {
                return sharedHistory.toList()
            }
        }

        fun clearSharedHistory() {
            synchronized(lock) {
                sharedHistory.clear()
            }
        }

        /**
         * Checks if a clip's metadata indicates sensitive information.
         */
        fun isClipMarkedSensitive(description: ClipDescription?): Boolean {
            if (description == null) return false

            // Android 13+ (API 33) official flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true) {
                    return true
                }
            }

            // Fallback for custom or security app extras
            val extras = description.extras ?: return false
            return extras.getBoolean("android.content.extra.IS_SENSITIVE", false) ||
                    extras.getBoolean("is_sensitive", false) ||
                    extras.getBoolean("sensitive", false)
        }

        /**
         * Heuristic validation to detect sensitive content:
         * - Credit/debit card numbers (Luhn algorithm, Uzcard, Humo, Visa, etc.)
         * - One-Time Passwords (OTPs) and SMS verification codes
         * - High-entropy authentication tokens (JWT, API keys)
         */
        fun isSensitiveContent(text: String): Boolean {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return false

            // 1. Credit / Debit card check (13 to 19 digits)
            val digitsOnly = trimmed.filter { it.isDigit() }
            if (digitsOnly.length in 13..19 && (trimmed.contains(' ') || trimmed.contains('-') || trimmed == digitsOnly)) {
                val isCommonCardPrefix = digitsOnly.length == 16 && (
                        digitsOnly.startsWith("8600") || // Uzcard
                        digitsOnly.startsWith("9860") || // Humo
                        digitsOnly.startsWith("4") ||    // Visa
                        digitsOnly.startsWith("5")       // MasterCard
                )
                if (isCommonCardPrefix || isValidLuhn(digitsOnly)) {
                    return true
                }
            }

            // 2. Common OTP / Verification patterns
            // Standalone 4-8 digit numeric codes often copied from SMS
            if (digitsOnly.length in 4..8 && trimmed.length <= 10 && digitsOnly == trimmed.filterNot { it.isWhitespace() }) {
                return true
            }

            // Keyword-based OTP check: "kod: 1234", "parol: 1234", "code: 1234"
            val lower = trimmed.lowercase()
            if ((lower.contains("kod") || lower.contains("code") || lower.contains("parol") || lower.contains("otp")) &&
                digitsOnly.length in 4..8) {
                return true
            }

            // 3. JWT or Bearer tokens
            if (trimmed.startsWith("ey", ignoreCase = false) && trimmed.count { it == '.' } == 2 && trimmed.length > 50) {
                return true
            }
            if (lower.startsWith("bearer ") || lower.startsWith("ghp_") || lower.startsWith("xoxb-")) {
                return true
            }

            return false
        }

        private fun isValidLuhn(number: String): Boolean {
            var sum = 0
            var alternate = false
            for (i in number.length - 1 downTo 0) {
                var n = number[i] - '0'
                if (n !in 0..9) return false
                if (alternate) {
                    n *= 2
                    if (n > 9) n -= 9
                }
                sum += n
                alternate = !alternate
            }
            return sum % 10 == 0
        }
    }

    fun getHistory(): List<String> {
        return getSharedHistory()
    }

    fun syncFromClipboard(optInEnabled: Boolean, isSensitiveField: Boolean) {
        if (!optInEnabled || isSensitiveField) return

        try {
            val primaryClip = clipboardManager?.primaryClip ?: return
            if (primaryClip.itemCount == 0) return

            // 1. Guard against clips flagged sensitive by password managers or banking apps
            if (isClipMarkedSensitive(primaryClip.description)) {
                return
            }

            val text = primaryClip.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
            if (text.isNullOrEmpty()) return

            // 2. Guard against sensitive content (cards, OTPs, tokens)
            if (isSensitiveContent(text)) {
                return
            }

            synchronized(lock) {
                if (!sharedHistory.contains(text)) {
                    sharedHistory.add(0, text)
                    if (sharedHistory.size > MAX_HISTORY_ITEMS) {
                        sharedHistory.removeAt(sharedHistory.lastIndex)
                    }
                }
            }
        } catch (_: SecurityException) {
            // Android clipboard access restrictions (e.g. background access)
        } catch (_: Exception) {
            // Prevent any unexpected IPC or clipboard parsing crash
        }
    }

    fun clearHistory() {
        clearSharedHistory()
    }

    fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Özbek Keyboard", text)
        clipboardManager?.setPrimaryClip(clip)
    }
}
