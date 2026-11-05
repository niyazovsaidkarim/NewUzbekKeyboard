package uz.ozbekkeyboard.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.clipboard.ClipboardHelper
import uz.ozbekkeyboard.settings.KeyboardSettings
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager

class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onTextSelected: ((String) -> Unit)? = null
    var onBackToKeyboard: (() -> Unit)? = null
    var currentTheme: KeyboardTheme = ThemeManager.BLACK_YELLOW

    private val headerLayout: LinearLayout
    private val recyclerView: RecyclerView
    private val emptyView: TextView
    private val settings = KeyboardSettings(context)

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density

        // Header: "Vaqtincha xotira", [Tozalash], [ABC]
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

        val titleView = TextView(context).apply {
            text = context.getString(R.string.toolbar_clipboard)
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        }
        headerLayout.addView(titleView)

        val btnClear = Button(context).apply {
            text = "Tozalaş"
            textSize = 13f
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                onClearClicked()
            }
        }
        headerLayout.addView(btnClear)
        addView(headerLayout)

        // Empty message
        emptyView = TextView(context).apply {
            text = context.getString(R.string.clipboard_empty)
            textSize = 14f
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (120f * density).toInt())
            visibility = View.GONE
        }
        addView(emptyView)

        // Recycler view
        recyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
            layoutManager = LinearLayoutManager(context)
        }
        addView(recyclerView)

        applyTheme(currentTheme)
    }

    private fun onClearClicked() {
        ClipboardHelper.clearSharedHistory()
        loadHistory()
    }

    fun loadHistory(helper: ClipboardHelper? = null) {
        val history = ClipboardHelper.getSharedHistory()
        if (history.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = ClipboardAdapter(history) { text ->
                onTextSelected?.invoke(text)
            }
        }
    }

    fun applyTheme(theme: KeyboardTheme) {
        currentTheme = theme
        setBackgroundColor(theme.keyboardBackground)
        headerLayout.setBackgroundColor(theme.toolbarBackground)
        emptyView.setTextColor(theme.keyTextSecondary)
    }

    private class ClipboardAdapter(
        private val items: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<ClipboardAdapter.ViewHolder>() {

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                textSize = 15f
                setTextColor(Color.WHITE)
                val density = parent.context.resources.displayMetrics.density
                setPadding((16f * density).toInt(), (12f * density).toInt(), (16f * density).toInt(), (12f * density).toInt())
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                maxLines = 2
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val text = items[position]
            holder.textView.text = text
            holder.textView.setOnClickListener { onItemClick(text) }
        }

        override fun getItemCount(): Int = items.size
    }
}
