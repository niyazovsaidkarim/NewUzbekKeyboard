package uz.ozbekkeyboard.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.ime.KeyboardActionListener
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager

class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var actionListener: KeyboardActionListener? = null
    var currentTheme: KeyboardTheme = ThemeManager.BLACK_YELLOW

    private val toolbarContainer: LinearLayout
    private val suggestionsContainer: LinearLayout

    // Toolbar buttons
    private val btnEmoji: ImageButton
    private val btnTranslate: ImageButton
    private val btnSettings: ImageButton
    private val btnClipboard: ImageButton
    private val btnMore: ImageButton

    // Suggestion views
    private val suggestionViews = mutableListOf<TextView>()
    private val suggestionDividers = mutableListOf<View>()

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density
        val heightPx = (46f * density).toInt()

        // 1. Toolbar actions row
        toolbarContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightPx)
            setPadding((12f * density).toInt(), 0, (12f * density).toInt(), 0)
        }

        btnEmoji = createToolbarButton(R.drawable.ic_emoji, "Emoji") { actionListener?.onToolbarEmojiTap() }
        btnTranslate = createToolbarButton(R.drawable.ic_translate, "Translate") { actionListener?.onToolbarTranslateTap() }
        btnSettings = createToolbarButton(R.drawable.ic_settings, "Settings") { actionListener?.onToolbarSettingsTap() }
        btnClipboard = createToolbarButton(R.drawable.ic_clipboard, "Clipboard") { actionListener?.onToolbarClipboardTap() }
        btnMore = createToolbarButton(R.drawable.ic_more, "More") { actionListener?.onToolbarMoreTap() }

        toolbarContainer.addView(btnEmoji)
        toolbarContainer.addView(btnTranslate)
        toolbarContainer.addView(btnSettings)
        toolbarContainer.addView(btnClipboard)
        toolbarContainer.addView(btnMore)

        // 2. Suggestions row
        suggestionsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightPx)
            visibility = View.GONE
        }

        // Add 3 suggestion slots
        for (i in 0 until 3) {
            val tv = TextView(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f)
                gravity = Gravity.CENTER
                textSize = 15f
                isClickable = true
                isFocusable = true
                maxLines = 1
                setOnClickListener {
                    val word = text.toString()
                    if (word.isNotEmpty()) {
                        actionListener?.onSuggestionSelected(word)
                    }
                }
            }
            suggestionViews.add(tv)
            suggestionsContainer.addView(tv)

            if (i < 2) {
                val div = View(context).apply {
                    layoutParams = LayoutParams((1f * density).toInt(), (20f * density).toInt())
                }
                suggestionDividers.add(div)
                suggestionsContainer.addView(div)
            }
        }

        // Back to toolbar button on the right of suggestions
        val btnCollapseSuggestions = ImageButton(context).apply {
            layoutParams = LayoutParams((36f * density).toInt(), (36f * density).toInt())
            background = null
            setImageResource(R.drawable.ic_more)
            setOnClickListener {
                setSuggestions(emptyList())
            }
        }
        suggestionsContainer.addView(btnCollapseSuggestions)

        addView(toolbarContainer)
        addView(suggestionsContainer)

        applyTheme(currentTheme)
    }

    private fun createToolbarButton(iconResId: Int, contentDesc: String, onClick: () -> Unit): ImageButton {
        val density = resources.displayMetrics.density
        return ImageButton(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f)
            background = null
            contentDescription = contentDesc
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(iconResId)
            setOnClickListener { onClick() }
        }
    }

    fun applyTheme(theme: KeyboardTheme) {
        currentTheme = theme
        setBackgroundColor(theme.toolbarBackground)

        val tint = theme.toolbarIconTint
        tintButton(btnEmoji, tint)
        tintButton(btnTranslate, tint)
        tintButton(btnSettings, tint)
        tintButton(btnClipboard, tint)
        tintButton(btnMore, tint)

        for (tv in suggestionViews) {
            tv.setTextColor(theme.suggestionText)
        }
        for (div in suggestionDividers) {
            div.setBackgroundColor(theme.suggestionDivider)
        }
    }

    private fun tintButton(btn: ImageButton, tintColor: Int) {
        val d = btn.drawable?.mutate() ?: return
        DrawableCompat.setTint(d, tintColor)
        btn.setImageDrawable(d)
    }

    fun setSuggestions(suggestions: List<String>) {
        if (suggestions.isEmpty()) {
            suggestionsContainer.visibility = View.GONE
            toolbarContainer.visibility = View.VISIBLE
            return
        }

        toolbarContainer.visibility = View.GONE
        suggestionsContainer.visibility = View.VISIBLE

        for (i in suggestionViews.indices) {
            if (i < suggestions.size) {
                suggestionViews[i].text = suggestions[i]
                suggestionViews[i].visibility = View.VISIBLE
            } else {
                suggestionViews[i].text = ""
                suggestionViews[i].visibility = View.INVISIBLE
            }
        }
    }
}
