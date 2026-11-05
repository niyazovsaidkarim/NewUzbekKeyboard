package uz.ozbekkeyboard.layout

import uz.ozbekkeyboard.data.UzbekLetters

data class Key(
    val primaryText: String,
    val shiftedText: String = primaryText.uppercase(),
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f,
    val iconResId: Int = 0,
    val popupVariants: List<String> = emptyList()
) {
    val bounds: KeyBounds = KeyBounds()
    var isPressed: Boolean = false

    fun getDisplayText(isShifted: Boolean): String {
        return when (type) {
            KeyType.CHARACTER -> if (isShifted) shiftedText else primaryText
            KeyType.SPACE -> "Özbekcha"
            KeyType.SWITCH_SYMBOLS -> primaryText
            KeyType.SWITCH_PAGE -> primaryText
            KeyType.ENTER -> primaryText
            KeyType.COMMA -> ","
            KeyType.PERIOD -> "."
            KeyType.NUMBER -> primaryText
            else -> primaryText
        }
    }

    fun getOutputText(isShifted: Boolean): String {
        return when (type) {
            KeyType.CHARACTER -> if (isShifted) shiftedText else primaryText
            KeyType.SPACE -> " "
            KeyType.COMMA -> ","
            KeyType.PERIOD -> "."
            KeyType.NUMBER -> primaryText
            else -> primaryText
        }
    }

    fun getVariants(isShifted: Boolean): List<String> {
        if (popupVariants.isNotEmpty()) return popupVariants
        return UzbekLetters.getVariants(primaryText, isShifted)
    }

    fun hasVariants(): Boolean {
        return popupVariants.isNotEmpty() || UzbekLetters.hasVariants(primaryText)
    }
}
