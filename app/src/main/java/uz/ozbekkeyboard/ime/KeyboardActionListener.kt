package uz.ozbekkeyboard.ime

import uz.ozbekkeyboard.layout.Key

interface KeyboardActionListener {
    fun onKey(key: Key)
    fun onKeyLongPress(key: Key)
    fun onPopupVariantSelected(variant: String)
    fun onSpaceTrackpadMove(steps: Int)
    fun onSpaceTrackpadEnd()
    fun onBackspaceTap()
    fun onBackspaceRepeat()
    fun onShiftTap()
    fun onEnterTap()
    fun onSwitchSymbolsTap()
    fun onSwitchPageTap()
    fun onGlobeTap()
    fun onSuggestionSelected(suggestion: String)
    fun onToolbarEmojiTap()
    fun onToolbarTranslateTap()
    fun onToolbarSettingsTap()
    fun onToolbarClipboardTap()
    fun onToolbarMoreTap()
}
