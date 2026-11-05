package uz.ozbekkeyboard.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import uz.ozbekkeyboard.clipboard.ClipboardHelper
import uz.ozbekkeyboard.ime.KeyboardActionListener
import uz.ozbekkeyboard.settings.KeyboardSettings
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager

class KeyboardRootView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val toolbarView: ToolbarView
    val keyboardView: KeyboardView
    private val contentFrame: FrameLayout

    val emojiPanelView: EmojiPanelView
    val clipboardPanelView: ClipboardPanelView
    val noticePanelView: NoticePanelView

    var actionListener: KeyboardActionListener? = null
        set(value) {
            field = value
            toolbarView.actionListener = value
            keyboardView.actionListener = value
        }

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        clipChildren = false
        clipToPadding = false

        // 1. Toolbar
        toolbarView = ToolbarView(context)
        addView(toolbarView)

        // 2. Content Frame
        contentFrame = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            clipChildren = false
            clipToPadding = false
        }

        keyboardView = KeyboardView(context)
        emojiPanelView = EmojiPanelView(context).apply { visibility = View.GONE }
        clipboardPanelView = ClipboardPanelView(context).apply { visibility = View.GONE }
        noticePanelView = NoticePanelView(context).apply { visibility = View.GONE }

        contentFrame.addView(keyboardView)
        contentFrame.addView(emojiPanelView)
        contentFrame.addView(clipboardPanelView)
        contentFrame.addView(noticePanelView)

        addView(contentFrame)

        // Subpanel back navigation listeners
        emojiPanelView.onBackToKeyboard = { showMainKeyboard() }
        clipboardPanelView.onBackToKeyboard = { showMainKeyboard() }
        noticePanelView.onBackToKeyboard = { showMainKeyboard() }

        // Window Insets listener for gesture bar and navigation bar padding
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val density = resources.displayMetrics.density
            val minBottom = (4f * density).toInt()
            val bottomPad = if (navInsets.bottom > 0) navInsets.bottom else minBottom
            setPadding(paddingLeft, paddingTop, paddingRight, bottomPad)
            insets
        }
    }

    fun isSubpanelShowing(): Boolean {
        return emojiPanelView.visibility == View.VISIBLE ||
                clipboardPanelView.visibility == View.VISIBLE ||
                noticePanelView.visibility == View.VISIBLE
    }

    fun showMainKeyboard() {
        keyboardView.visibility = View.VISIBLE
        emojiPanelView.visibility = View.GONE
        clipboardPanelView.visibility = View.GONE
        noticePanelView.visibility = View.GONE
        toolbarView.visibility = View.VISIBLE
    }

    fun showEmojiPanel(onEmojiSelect: (String) -> Unit, onBackspace: () -> Unit) {
        emojiPanelView.onEmojiSelected = onEmojiSelect
        emojiPanelView.onBackspace = onBackspace
        keyboardView.visibility = View.GONE
        clipboardPanelView.visibility = View.GONE
        noticePanelView.visibility = View.GONE
        emojiPanelView.visibility = View.VISIBLE
    }

    fun showClipboardPanel(helper: ClipboardHelper, onTextSelect: (String) -> Unit) {
        clipboardPanelView.loadHistory(helper)
        clipboardPanelView.onTextSelected = { text ->
            onTextSelect(text)
            showMainKeyboard()
        }
        keyboardView.visibility = View.GONE
        emojiPanelView.visibility = View.GONE
        noticePanelView.visibility = View.GONE
        clipboardPanelView.visibility = View.VISIBLE
    }

    fun showTranslationPanel() {
        noticePanelView.showTranslationNotice()
        keyboardView.visibility = View.GONE
        emojiPanelView.visibility = View.GONE
        clipboardPanelView.visibility = View.GONE
        noticePanelView.visibility = View.VISIBLE
    }

    fun showMorePanel(onToggleNumberRow: () -> Unit, onOpenSettings: () -> Unit) {
        noticePanelView.showMoreTools(onToggleNumberRow, onOpenSettings)
        keyboardView.visibility = View.GONE
        emojiPanelView.visibility = View.GONE
        clipboardPanelView.visibility = View.GONE
        noticePanelView.visibility = View.VISIBLE
    }

    fun applyTheme(theme: KeyboardTheme) {
        setBackgroundColor(theme.keyboardBackground)
        toolbarView.applyTheme(theme)
        keyboardView.currentTheme = theme
        keyboardView.invalidate()
        emojiPanelView.applyTheme(theme)
        clipboardPanelView.applyTheme(theme)
        noticePanelView.applyTheme(theme)
    }
}
