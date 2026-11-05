package uz.ozbekkeyboard.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager

class NoticePanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onBackToKeyboard: (() -> Unit)? = null
    var onActionClicked: (() -> Unit)? = null
    var currentTheme: KeyboardTheme = ThemeManager.BLACK_YELLOW

    private val headerLayout: LinearLayout
    private val contentLayout: LinearLayout
    private val titleView: TextView
    private val messageView: TextView
    private val actionButton: Button

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density

        // Header
        headerLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (44f * density).toInt())
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12f * density).toInt(), 0, (12f * density).toInt(), 0)
        }

        val btnAbc = Button(context).apply {
            text = "ABC"
            textSize = 14f
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { onBackToKeyboard?.invoke() }
        }
        headerLayout.addView(btnAbc)

        titleView = TextView(context).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        }
        headerLayout.addView(titleView)

        val spacer = View(context).apply {
            layoutParams = LayoutParams((50f * density).toInt(), 1)
        }
        headerLayout.addView(spacer)
        addView(headerLayout)

        // Body Content
        contentLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
            setPadding((24f * density).toInt(), (16f * density).toInt(), (24f * density).toInt(), (16f * density).toInt())
        }

        messageView = TextView(context).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        contentLayout.addView(messageView)

        actionButton = Button(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (16f * density).toInt()
            }
            visibility = View.GONE
            setOnClickListener { onActionClicked?.invoke() }
        }
        contentLayout.addView(actionButton)

        addView(contentLayout)
        applyTheme(currentTheme)
    }

    fun showTranslationNotice() {
        titleView.text = context.getString(R.string.translation_notice_title)
        messageView.text = context.getString(R.string.translation_notice_desc)
        actionButton.visibility = View.GONE
    }

    fun showMoreTools(onToggleNumberRow: () -> Unit, onOpenSettings: () -> Unit) {
        titleView.text = context.getString(R.string.toolbar_more)
        messageView.text = "Tezkor amallar va vositalar"
        actionButton.text = "Sozlamalarni ochish"
        actionButton.visibility = View.VISIBLE
        actionButton.setOnClickListener { onOpenSettings() }
    }

    fun applyTheme(theme: KeyboardTheme) {
        currentTheme = theme
        setBackgroundColor(theme.keyboardBackground)
        headerLayout.setBackgroundColor(theme.toolbarBackground)
        messageView.setTextColor(theme.keyTextSecondary)
        actionButton.setBackgroundColor(theme.popupKeySelected)
        actionButton.setTextColor(Color.BLACK)
    }
}
