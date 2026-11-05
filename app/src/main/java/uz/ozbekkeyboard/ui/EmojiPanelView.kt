package uz.ozbekkeyboard.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.data.EmojiCategory
import uz.ozbekkeyboard.data.EmojiData
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager

class EmojiPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onEmojiSelected: ((String) -> Unit)? = null
    var onBackToKeyboard: (() -> Unit)? = null
    var onBackspace: (() -> Unit)? = null
    var currentTheme: KeyboardTheme = ThemeManager.BLACK_YELLOW

    private val categoryTabsContainer: LinearLayout
    private val emojiRecyclerView: RecyclerView
    private val bottomBar: LinearLayout
    private var currentCategoryIndex = 0

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density

        // 1. Category Tabs
        val tabsScrollView = HorizontalScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (44f * density).toInt())
            isHorizontalScrollBarEnabled = false
        }
        categoryTabsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding((8f * density).toInt(), 0, (8f * density).toInt(), 0)
        }
        tabsScrollView.addView(categoryTabsContainer)
        addView(tabsScrollView)

        // 2. Emoji Grid
        emojiRecyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
            layoutManager = GridLayoutManager(context, 7)
        }
        addView(emojiRecyclerView)

        // 3. Bottom utility bar: [ABC] on left, [Backspace] on right
        bottomBar = LinearLayout(context).apply {
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
        bottomBar.addView(btnAbc)

        val spacer = View(context).apply {
            layoutParams = LayoutParams(0, 1, 1.0f)
        }
        bottomBar.addView(spacer)

        val btnDel = ImageButton(context).apply {
            layoutParams = LayoutParams((40f * density).toInt(), (40f * density).toInt())
            background = null
            setImageResource(R.drawable.ic_backspace)
            setOnClickListener { onBackspace?.invoke() }
        }
        bottomBar.addView(btnDel)

        addView(bottomBar)

        buildCategoryTabs()
        loadCategory(0)
        applyTheme(currentTheme)
    }

    private fun buildCategoryTabs() {
        categoryTabsContainer.removeAllViews()
        val density = resources.displayMetrics.density

        for (i in EmojiData.categories.indices) {
            val cat = EmojiData.categories[i]
            val tab = TextView(context).apply {
                text = cat.icon
                textSize = 20f
                gravity = Gravity.CENTER
                setPadding((12f * density).toInt(), 0, (12f * density).toInt(), 0)
                setOnClickListener {
                    loadCategory(i)
                }
            }
            categoryTabsContainer.addView(tab)
        }
    }

    private fun loadCategory(index: Int) {
        currentCategoryIndex = index
        val category = EmojiData.categories[index]
        emojiRecyclerView.adapter = EmojiAdapter(category.emojis) { emoji ->
            onEmojiSelected?.invoke(emoji)
        }
    }

    fun applyTheme(theme: KeyboardTheme) {
        currentTheme = theme
        setBackgroundColor(theme.keyboardBackground)
        categoryTabsContainer.setBackgroundColor(theme.toolbarBackground)
        bottomBar.setBackgroundColor(theme.toolbarBackground)
    }

    private class EmojiAdapter(
        private val emojis: List<String>,
        private val onEmojiClick: (String) -> Unit
    ) : RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                textSize = 26f
                gravity = Gravity.CENTER
                val size = (46f * parent.context.resources.displayMetrics.density).toInt()
                layoutParams = ViewGroup.LayoutParams(size, size)
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val emoji = emojis[position]
            holder.textView.text = emoji
            holder.textView.setOnClickListener {
                onEmojiClick(emoji)
            }
        }

        override fun getItemCount(): Int = emojis.size
    }
}
