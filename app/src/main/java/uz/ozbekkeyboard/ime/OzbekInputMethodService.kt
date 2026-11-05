package uz.ozbekkeyboard.ime

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.clipboard.ClipboardHelper
import uz.ozbekkeyboard.layout.Key
import uz.ozbekkeyboard.layout.KeyType
import uz.ozbekkeyboard.settings.KeyboardSettings
import uz.ozbekkeyboard.suggestions.SuggestionEngine
import uz.ozbekkeyboard.theme.ThemeManager
import uz.ozbekkeyboard.ui.KeyboardRootView
import uz.ozbekkeyboard.ui.SettingsActivity

class OzbekInputMethodService : InputMethodService(), KeyboardActionListener {

    private lateinit var settings: KeyboardSettings
    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var clipboardHelper: ClipboardHelper

    private var rootView: KeyboardRootView? = null
    private var currentEditorInfo: EditorInfo? = null

    override fun onCreate() {
        super.onCreate()
        settings = KeyboardSettings(this)
        suggestionEngine = SuggestionEngine()
        clipboardHelper = ClipboardHelper(this)
    }

    override fun onCreateInputView(): View {
        val root = KeyboardRootView(this)
        root.actionListener = this
        rootView = root
        applyCurrentSettings()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEditorInfo = info

        applyCurrentSettings()
        if (TextEditorHelper.isNumericInput(info)) {
            rootView?.keyboardView?.setKeyboardMode(KeyboardMode.SYMBOLS_1)
        }
        resolveEnterAction(info)
        resolveAutoCapitalization()
        updateSuggestions()

        // Sync clipboard if allowed
        val isSensitive = TextEditorHelper.isPasswordField(info)
        clipboardHelper.syncFromClipboard(settings.clipboardHistoryEnabled, isSensitive)
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Prevent IME from covering entire screen in landscape mode
        return false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        rootView?.keyboardView?.reloadLayout()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        rootView?.keyboardView?.resetTouchState()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0) {
            if (rootView?.isSubpanelShowing() == true) {
                rootView?.showMainKeyboard()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun applyCurrentSettings() {
        val root = rootView ?: return
        val theme = ThemeManager.resolveTheme(this, settings.theme)
        root.applyTheme(theme)
        root.keyboardView.settings = settings
        root.keyboardView.setKeyboardMode(KeyboardMode.ALPHABET)
        root.showMainKeyboard()
    }

    private fun resolveEnterAction(info: EditorInfo?) {
        val root = rootView ?: return
        if (info == null) {
            root.keyboardView.enterActionIconRes = R.drawable.ic_enter_return
            return
        }

        val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
        root.keyboardView.enterActionIconRes = when (action) {
            EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_enter_search
            EditorInfo.IME_ACTION_SEND -> R.drawable.ic_enter_send
            EditorInfo.IME_ACTION_DONE -> R.drawable.ic_enter_done
            EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_enter_next
            else -> R.drawable.ic_enter_return
        }
        root.keyboardView.invalidate()
    }

    private fun resolveAutoCapitalization() {
        val root = rootView ?: return
        if (settings.autoCapitalization && TextEditorHelper.shouldAutoCapitalize(currentInputConnection, currentEditorInfo)) {
            root.keyboardView.shiftState = ShiftState.SHIFTED
        } else {
            root.keyboardView.shiftState = ShiftState.OFF
        }
        root.keyboardView.invalidate()
    }

    private fun updateSuggestions() {
        val root = rootView ?: return
        if (!settings.wordSuggestions || TextEditorHelper.isPasswordField(currentEditorInfo)) {
            root.toolbarView.setSuggestions(emptyList())
            return
        }

        val prefix = TextEditorHelper.extractCurrentWordPrefix(currentInputConnection)
        if (prefix.length >= 2) {
            val suggestions = suggestionEngine.getSuggestions(prefix, 3)
            root.toolbarView.setSuggestions(suggestions)
        } else {
            root.toolbarView.setSuggestions(emptyList())
        }
    }

    // --- KeyboardActionListener implementation ---

    override fun onKey(key: Key) {
        val ic = currentInputConnection ?: return

        when (key.type) {
            KeyType.SPACE -> {
                TextEditorHelper.handleSpaceKey(ic, settings.doubleSpacePeriod)
            }
            KeyType.CHARACTER, KeyType.NUMBER, KeyType.COMMA, KeyType.PERIOD -> {
                val isShifted = rootView?.keyboardView?.shiftState?.isShifted() == true
                val text = key.getOutputText(isShifted)
                ic.commitText(text, 1)

                // Consume single-shift capitalization
                if (rootView?.keyboardView?.shiftState == ShiftState.SHIFTED) {
                    rootView?.keyboardView?.shiftState = ShiftState.OFF
                    rootView?.keyboardView?.invalidate()
                }
            }
            else -> {}
        }

        updateSuggestions()
        if (key.type == KeyType.SPACE || key.type == KeyType.PERIOD) {
            resolveAutoCapitalization()
        }
    }

    override fun onKeyLongPress(key: Key) {
        // Handled within KeyboardView for popup variant display
    }

    override fun onPopupVariantSelected(variant: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(variant, 1)

        // Consume single-shift
        if (rootView?.keyboardView?.shiftState == ShiftState.SHIFTED) {
            rootView?.keyboardView?.shiftState = ShiftState.OFF
            rootView?.keyboardView?.invalidate()
        }

        updateSuggestions()
    }

    override fun onSpaceTrackpadMove(steps: Int) {
        TextEditorHelper.moveCursor(currentInputConnection, steps)
    }

    override fun onSpaceTrackpadEnd() {
        // Trackpad navigation ended
    }

    override fun onBackspaceTap() {
        TextEditorHelper.handleDelete(currentInputConnection)
        updateSuggestions()
        resolveAutoCapitalization()
    }

    override fun onBackspaceRepeat() {
        TextEditorHelper.handleDelete(currentInputConnection)
    }

    override fun onShiftTap() {
        // Shift state already toggled in KeyboardView, re-render
        rootView?.keyboardView?.invalidate()
    }

    override fun onEnterTap() {
        val ic = currentInputConnection ?: return
        val info = currentEditorInfo

        if (info != null) {
            val hasNoEnterAction = (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
            if (!hasNoEnterAction && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(action)
                return
            }
        }

        // Default: send Enter key events
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    override fun onSwitchSymbolsTap() {
        val root = rootView ?: return
        val targetMode = if (root.keyboardView.currentMode == KeyboardMode.ALPHABET) {
            KeyboardMode.SYMBOLS_1
        } else {
            KeyboardMode.ALPHABET
        }
        root.keyboardView.setKeyboardMode(targetMode)
        if (targetMode == KeyboardMode.ALPHABET) {
            resolveAutoCapitalization()
        }
    }

    override fun onSwitchPageTap() {
        val root = rootView ?: return
        val targetMode = if (root.keyboardView.currentMode == KeyboardMode.SYMBOLS_1) {
            KeyboardMode.SYMBOLS_2
        } else {
            KeyboardMode.SYMBOLS_1
        }
        root.keyboardView.setKeyboardMode(targetMode)
    }

    override fun onGlobeTap() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        val token = window?.window?.attributes?.token

        var switched = false
        if (token != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                switched = imm.switchToNextInputMethod(token, false)
            } catch (_: Exception) {
                switched = false
            }
        }

        if (!switched) {
            imm.showInputMethodPicker()
        }
    }

    override fun onSuggestionSelected(suggestion: String) {
        val ic = currentInputConnection ?: return
        val prefix = TextEditorHelper.extractCurrentWordPrefix(ic)
        TextEditorHelper.replaceWordPrefix(ic, prefix, suggestion)
        updateSuggestions()
        resolveAutoCapitalization()
    }

    override fun onToolbarEmojiTap() {
        rootView?.showEmojiPanel(
            onEmojiSelect = { emoji ->
                currentInputConnection?.commitText(emoji, 1)
            },
            onBackspace = {
                onBackspaceTap()
            }
        )
    }

    override fun onToolbarTranslateTap() {
        rootView?.showTranslationPanel()
    }

    override fun onToolbarSettingsTap() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onToolbarClipboardTap() {
        val isSensitive = TextEditorHelper.isPasswordField(currentEditorInfo)
        clipboardHelper.syncFromClipboard(settings.clipboardHistoryEnabled, isSensitive)
        rootView?.showClipboardPanel(clipboardHelper) { text ->
            currentInputConnection?.commitText(text, 1)
        }
    }

    override fun onToolbarMoreTap() {
        rootView?.showMorePanel(
            onToggleNumberRow = {
                settings.setShowNumberRow(!settings.showNumberRow)
                rootView?.keyboardView?.reloadLayout()
                rootView?.showMainKeyboard()
            },
            onOpenSettings = {
                onToolbarSettingsTap()
            }
        )
    }
}
