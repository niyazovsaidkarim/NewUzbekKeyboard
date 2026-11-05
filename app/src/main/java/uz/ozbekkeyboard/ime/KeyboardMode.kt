package uz.ozbekkeyboard.ime

enum class ShiftState {
    OFF,
    SHIFTED,
    CAPS_LOCK;

    fun isShifted(): Boolean = this == SHIFTED || this == CAPS_LOCK
}

enum class KeyboardMode {
    ALPHABET,
    SYMBOLS_1,
    SYMBOLS_2
}
