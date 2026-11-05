package uz.ozbekkeyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.ime.KeyboardActionListener
import uz.ozbekkeyboard.ime.KeyboardMode
import uz.ozbekkeyboard.ime.ShiftState
import uz.ozbekkeyboard.layout.Key
import uz.ozbekkeyboard.layout.KeyboardLayout
import uz.ozbekkeyboard.layout.KeyType
import uz.ozbekkeyboard.settings.KeyboardSettings
import uz.ozbekkeyboard.theme.KeyboardTheme
import uz.ozbekkeyboard.theme.ThemeManager
import kotlin.math.abs
import kotlin.math.roundToInt

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var actionListener: KeyboardActionListener? = null
    var settings: KeyboardSettings = KeyboardSettings(context)
    var currentTheme: KeyboardTheme = ThemeManager.BLACK_YELLOW
    var currentMode: KeyboardMode = KeyboardMode.ALPHABET
    var shiftState: ShiftState = ShiftState.OFF
    var enterActionIconRes: Int = R.drawable.ic_enter_return

    private var rows: List<List<Key>> = emptyList()

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Touch and Gesture Tracking
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeKey: Key? = null
    private var touchDownTime: Long = 0
    private var touchDownX: Float = 0f
    private var touchDownY: Float = 0f

    // Trackpad 2.0 cursor state
    private var isTrackpadActive: Boolean = false
    private var hasSpaceMoved: Boolean = false
    private var lastTrackpadX: Float = 0f
    private var trackpadAccumulatedDx: Float = 0f
    private val trackpadThresholdPx: Float
        get() = settings.trackpadSensitivity.thresholdDp * resources.displayMetrics.density

    // Popup State
    private var isPopupActive: Boolean = false
    private var activePopupVariants: List<String> = emptyList()
    private var selectedPopupIndex: Int = -1
    private val popupBounds = RectF()
    private val variantBounds = mutableListOf<RectF>()
    private val variantBoundsPool = List(10) { RectF() }

    // Backspace continuous repeat
    private var isBackspaceRepeating: Boolean = false
    private val backspaceRepeatIntervalMs: Long = 50L
    private val backspaceInitialDelayMs: Long = 400L

    // Shift double tap tracking
    private var lastShiftTapTime: Long = 0L

    // Drawable caches
    private val iconCache = mutableMapOf<Int, Drawable>()
    private var audioManager: AudioManager? = null

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        reloadLayout()
    }

    private fun playSound(keyType: KeyType) {
        if (settings.soundOnPress) {
            val fx = when (keyType) {
                KeyType.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                KeyType.BACKSPACE -> AudioManager.FX_KEYPRESS_DELETE
                KeyType.ENTER -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(fx)
        }
    }

    fun resetTouchState() {
        cancelLongPressTimers()
        activeKey?.isPressed = false
        activeKey = null
        isTrackpadActive = false
        hasSpaceMoved = false
        isPopupActive = false
        isBackspaceRepeating = false
        invalidate()
    }

    fun reloadLayout() {
        rows = KeyboardLayout.getLayoutForMode(currentMode, settings.showNumberRow)
        if (width > 0 && height > 0) {
            calculateKeyBounds(width, height)
        }
        requestLayout()
        invalidate()
    }

    fun setKeyboardMode(mode: KeyboardMode) {
        if (currentMode != mode) {
            currentMode = mode
            if (mode != KeyboardMode.ALPHABET) {
                shiftState = ShiftState.OFF
            }
            cancelLongPressTimers()
            isPopupActive = false
            isTrackpadActive = false
            hasSpaceMoved = false
            activeKey = null
            reloadLayout()
        }
    }

    fun getRows(): List<List<Key>> = rows

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density

        // Row height adjusted by user settings and screen orientation
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val baseRowHeight = if (isLandscape) 38f * density else 54f * density
        val rowHeight = baseRowHeight * settings.keyboardHeight.factor
        val rowCount = rows.size
        val verticalPadding = (if (isLandscape) 4f * density else 8f * density).roundToInt()

        val desiredHeight = (rowCount * rowHeight + verticalPadding * 2).roundToInt()
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = right - left
        val h = bottom - top
        if (w > 0 && h > 0) {
            calculateKeyBounds(w, h)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            calculateKeyBounds(w, h)
        }
    }

    fun calculateKeyBounds(width: Int, height: Int) {
        val density = resources.displayMetrics.density
        KeyboardLayout.calculateBounds(rows, width.toFloat(), height.toFloat(), density)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Defensive check: ensure key bounds are computed before rendering
        if (rows.isNotEmpty() && width > 0 && height > 0) {
            val firstKey = rows.firstOrNull()?.firstOrNull()
            if (firstKey == null || firstKey.bounds.isEmpty) {
                calculateKeyBounds(width, height)
            }
        }

        // 1. Draw keyboard background
        backgroundPaint.color = currentTheme.keyboardBackground
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // 2. Draw keys
        val cornerRadius = currentTheme.keyCornerRadiusDp * resources.displayMetrics.density
        val density = resources.displayMetrics.density

        for (row in rows) {
            for (key in row) {
                drawKey(canvas, key, cornerRadius, density)
            }
        }

        // 3. Draw Long-Press Popup Overlay if active
        if (isPopupActive && activeKey != null) {
            drawPopupOverlay(canvas, cornerRadius, density)
        }
    }

    private fun drawKey(canvas: Canvas, key: Key, cornerRadius: Float, density: Float) {
        // Determine Key background color
        val keyBgColor = when {
            key.isPressed -> currentTheme.keyBackgroundPressed
            key.type == KeyType.SPACE && isTrackpadActive -> currentTheme.trackpadHighlight
            key.type == KeyType.NUMBER -> currentTheme.keyBackgroundNumber
            key.type == KeyType.CHARACTER || key.type == KeyType.COMMA || key.type == KeyType.PERIOD -> currentTheme.keyBackgroundAlphabet
            else -> currentTheme.keyBackgroundAction
        }

        keyPaint.color = keyBgColor
        canvas.drawRoundRect(key.bounds.left, key.bounds.top, key.bounds.right, key.bounds.bottom, cornerRadius, cornerRadius, keyPaint)

        // Subtle bottom border/shadow for Samsung style depth
        keyShadowPaint.color = Color.argb(25, 0, 0, 0)
        canvas.drawRoundRect(
            key.bounds.left,
            key.bounds.bottom - 2f * density,
            key.bounds.right,
            key.bounds.bottom,
            cornerRadius,
            cornerRadius,
            keyShadowPaint
        )

        // Determine Text and Icon colors
        val isActionKey = key.type == KeyType.SHIFT || key.type == KeyType.BACKSPACE ||
                key.type == KeyType.ENTER || key.type == KeyType.SWITCH_SYMBOLS ||
                key.type == KeyType.SWITCH_PAGE || key.type == KeyType.GLOBE

        val textColor = if (isActionKey) currentTheme.keyTextAction else currentTheme.keyTextPrimary
        val iconTint = if (isActionKey) currentTheme.keyIconActionTint else currentTheme.keyIconTint

        // Check if key has an icon
        val iconRes = when (key.type) {
            KeyType.SHIFT -> if (shiftState == ShiftState.CAPS_LOCK) R.drawable.ic_shift_caps else R.drawable.ic_shift
            KeyType.BACKSPACE -> R.drawable.ic_backspace
            KeyType.ENTER -> enterActionIconRes
            KeyType.GLOBE -> R.drawable.ic_globe
            else -> key.iconResId
        }

        if (iconRes != 0) {
            drawKeyIcon(canvas, key, iconRes, iconTint, density)
        } else {
            drawKeyText(canvas, key, textColor, density)
        }

        // Draw hint for keys with special variants (e.g. o, g, s, c)
        if (key.type == KeyType.CHARACTER && key.hasVariants() && currentMode == KeyboardMode.ALPHABET) {
            val variant = key.getVariants(shiftState.isShifted()).firstOrNull()
            if (variant != null && (key.primaryText in listOf("o", "g", "s", "c"))) {
                subTextPaint.color = currentTheme.keyTextSecondary
                subTextPaint.textSize = 10f * density
                canvas.drawText(
                    variant,
                    key.bounds.right - 8f * density,
                    key.bounds.top + 12f * density,
                    subTextPaint
                )
            }
        }
    }

    private fun drawKeyText(canvas: Canvas, key: Key, textColor: Int, density: Float) {
        val isShifted = shiftState.isShifted()
        val text = key.getDisplayText(isShifted)

        textPaint.color = textColor
        textPaint.textSize = when (key.type) {
            KeyType.SPACE -> 14f * density
            KeyType.SWITCH_SYMBOLS, KeyType.SWITCH_PAGE -> 14f * density
            KeyType.NUMBER -> 19f * density
            else -> 19f * density
        }

        val fontMetrics = textPaint.fontMetrics
        val centerY = key.bounds.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(text, key.bounds.centerX(), centerY, textPaint)
    }

    private fun drawKeyIcon(canvas: Canvas, key: Key, iconResId: Int, tintColor: Int, density: Float) {
        val drawable = iconCache.getOrPut(iconResId) {
            ContextCompat.getDrawable(context, iconResId)!!.mutate()
        }

        val iconSize = (22f * density).roundToInt()
        val left = (key.bounds.centerX() - iconSize / 2f).roundToInt()
        val top = (key.bounds.centerY() - iconSize / 2f).roundToInt()
        val right = left + iconSize
        val bottom = top + iconSize

        drawable.setBounds(left, top, right, bottom)
        DrawableCompat.setTint(drawable, tintColor)
        drawable.draw(canvas)
    }

    private fun drawPopupOverlay(canvas: Canvas, cornerRadius: Float, density: Float) {
        if (activePopupVariants.isEmpty() || activeKey == null) return

        val key = activeKey!!
        val variantCount = activePopupVariants.size
        val variantWidth = 44f * density
        val popupHeight = 52f * density
        val popupWidth = variantWidth * variantCount + 14f * density

        var popupLeft = key.bounds.centerX() - popupWidth / 2f
        if (popupLeft < 6f * density) popupLeft = 6f * density
        if (popupLeft + popupWidth > width - 6f * density) {
            popupLeft = width - 6f * density - popupWidth
        }

        // Avoid clipping: ensure popup sits cleanly above the key without going offscreen
        val desiredTop = key.bounds.top - popupHeight - 6f * density
        val minAllowedTop = -42f * density // Clamped to KeyboardRootView toolbar space
        val popupTop = if (desiredTop < minAllowedTop) minAllowedTop else desiredTop
        val popupRight = popupLeft + popupWidth
        val popupBottom = popupTop + popupHeight

        popupBounds.set(popupLeft, popupTop, popupRight, popupBottom)

        // Draw shadow/elevation for popup
        keyShadowPaint.color = Color.argb(110, 0, 0, 0)
        canvas.drawRoundRect(
            popupBounds.left - 2f,
            popupBounds.top - 2f,
            popupBounds.right + 2f,
            popupBounds.bottom + 4f,
            cornerRadius * 1.5f,
            cornerRadius * 1.5f,
            keyShadowPaint
        )

        // Draw popup container
        popupBgPaint.color = currentTheme.popupBackground
        canvas.drawRoundRect(popupBounds, cornerRadius * 1.5f, cornerRadius * 1.5f, popupBgPaint)

        variantBounds.clear()
        var currentX = popupLeft + 7f * density

        for (i in activePopupVariants.indices) {
            val variant = activePopupVariants[i]
            val itemBounds = if (i < variantBoundsPool.size) {
                variantBoundsPool[i].apply {
                    set(currentX, popupTop + 4f * density, currentX + variantWidth, popupBottom - 4f * density)
                }
            } else {
                RectF(currentX, popupTop + 4f * density, currentX + variantWidth, popupBottom - 4f * density)
            }
            variantBounds.add(itemBounds)

            if (i == selectedPopupIndex) {
                popupSelectedPaint.color = currentTheme.popupKeySelected
                canvas.drawRoundRect(itemBounds, cornerRadius, cornerRadius, popupSelectedPaint)
            }

            popupTextPaint.color = if (i == selectedPopupIndex) Color.BLACK else currentTheme.popupText
            popupTextPaint.textSize = 20f * density
            val fm = popupTextPaint.fontMetrics
            val centerY = itemBounds.centerY() - (fm.ascent + fm.descent) / 2
            canvas.drawText(variant, itemBounds.centerX(), centerY, popupTextPaint)

            currentX += variantWidth
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Defensive check before hit testing
        if (rows.isNotEmpty() && width > 0 && height > 0) {
            val firstKey = rows.firstOrNull()?.firstOrNull()
            if (firstKey == null || firstKey.bounds.isEmpty) {
                calculateKeyBounds(width, height)
            }
        }

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = x
                touchDownY = y
                touchDownTime = SystemClock.uptimeMillis()
                hasSpaceMoved = false
                val key = findKeyAt(x, y)
                activeKey = key

                if (key != null) {
                    key.isPressed = true
                    performHaptic()
                    playSound(key.type)
                    announceKey(key)
                    invalidate()

                    when (key.type) {
                        KeyType.BACKSPACE -> {
                            actionListener?.onBackspaceTap()
                            startBackspaceRepeat()
                        }
                        KeyType.SPACE -> {
                            if (settings.spaceCursorControl) {
                                startSpaceTrackpadLongPress()
                            }
                        }
                        KeyType.CHARACTER -> {
                            if (settings.longPressPopup && key.hasVariants()) {
                                startLongPressPopup(key)
                            }
                        }
                        else -> {}
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTrackpadActive) {
                    handleTrackpadMove(x)
                    return true
                }

                if (isPopupActive) {
                    handlePopupMove(x, y)
                    return true
                }

                // Check for early space trackpad drag activation
                if (activeKey?.type == KeyType.SPACE && settings.spaceCursorControl) {
                    val dx = abs(x - touchDownX)
                    if (dx > 10f * resources.displayMetrics.density) {
                        hasSpaceMoved = true
                        cancelLongPressTimers()
                        isTrackpadActive = true
                        lastTrackpadX = x
                        trackpadAccumulatedDx = 0f
                        performHaptic()
                        invalidate()
                        return true
                    }
                }

                // If finger moves significantly away from initial key, cancel popup timer
                if (activeKey != null) {
                    val dist = Math.hypot((x - touchDownX).toDouble(), (y - touchDownY).toDouble())
                    if (dist > 30f * resources.displayMetrics.density) {
                        cancelLongPressTimers()
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cancelLongPressTimers()

                if (isTrackpadActive || (activeKey?.type == KeyType.SPACE && hasSpaceMoved)) {
                    isTrackpadActive = false
                    hasSpaceMoved = false
                    actionListener?.onSpaceTrackpadEnd()
                    activeKey?.isPressed = false
                    activeKey = null
                    invalidate()
                    return true
                }

                if (isPopupActive) {
                    isPopupActive = false
                    if (selectedPopupIndex in activePopupVariants.indices) {
                        val chosen = activePopupVariants[selectedPopupIndex]
                        actionListener?.onPopupVariantSelected(chosen)
                        performHaptic()
                        playSound(KeyType.CHARACTER)
                        announceText(chosen)
                    }
                    activeKey?.isPressed = false
                    activeKey = null
                    selectedPopupIndex = -1
                    invalidate()
                    return true
                }

                // Normal tap release
                val releasedKey = activeKey
                if (releasedKey != null && releasedKey.isPressed) {
                    releasedKey.isPressed = false
                    val upKey = findKeyAt(x, y)
                    if (upKey == releasedKey) {
                        if (releasedKey.type != KeyType.SPACE || !hasSpaceMoved) {
                            handleKeyRelease(releasedKey)
                        }
                    }
                }

                hasSpaceMoved = false
                activeKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleKeyRelease(key: Key) {
        when (key.type) {
            KeyType.CHARACTER, KeyType.NUMBER -> {
                actionListener?.onKey(key)
            }
            KeyType.SPACE -> {
                actionListener?.onKey(key)
            }
            KeyType.COMMA, KeyType.PERIOD -> {
                actionListener?.onKey(key)
            }
            KeyType.SHIFT -> {
                handleShiftTap()
            }
            KeyType.ENTER -> {
                actionListener?.onEnterTap()
            }
            KeyType.SWITCH_SYMBOLS -> {
                actionListener?.onSwitchSymbolsTap()
            }
            KeyType.SWITCH_PAGE -> {
                actionListener?.onSwitchPageTap()
            }
            KeyType.GLOBE -> {
                actionListener?.onGlobeTap()
            }
            KeyType.BACKSPACE -> {
                // Single tap already fired on ACTION_DOWN, repeat stopped
            }
        }
    }

    private fun handleShiftTap() {
        val now = SystemClock.uptimeMillis()
        if (now - lastShiftTapTime < 350L && shiftState == ShiftState.SHIFTED) {
            shiftState = ShiftState.CAPS_LOCK
        } else {
            shiftState = when (shiftState) {
                ShiftState.OFF -> ShiftState.SHIFTED
                ShiftState.SHIFTED -> ShiftState.OFF
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
        }
        lastShiftTapTime = now
        actionListener?.onShiftTap()
        invalidate()
    }

    // Long Press Popup Logic
    private val popupRunnable = Runnable {
        val key = activeKey ?: return@Runnable
        if (!key.isPressed) return@Runnable

        val variants = key.getVariants(shiftState.isShifted())
        if (variants.isNotEmpty()) {
            isPopupActive = true
            activePopupVariants = variants
            selectedPopupIndex = 0 // default highlight first variant (e.g. ö)
            performHaptic()
            invalidate()
        }
    }

    private fun startLongPressPopup(key: Key) {
        mainHandler.postDelayed(popupRunnable, settings.longPressDurationMs.toLong())
    }

    private fun handlePopupMove(x: Float, y: Float) {
        val density = resources.displayMetrics.density
        val key = activeKey
        if (key != null && y > key.bounds.bottom + 25f * density) {
            // Dragged too far below the key to cancel
            if (selectedPopupIndex != -1) {
                selectedPopupIndex = -1
                invalidate()
            }
            return
        }

        var foundIndex = -1
        for (i in variantBounds.indices) {
            val bounds = variantBounds[i]
            // Expanded hit test for comfortable sliding
            if (x >= bounds.left - 10 && x <= bounds.right + 10) {
                foundIndex = i
                break
            }
        }

        if (foundIndex != selectedPopupIndex) {
            selectedPopupIndex = foundIndex
            invalidate()
        }
    }

    // Space Trackpad Logic
    private val trackpadRunnable = Runnable {
        val key = activeKey ?: return@Runnable
        if (key.type == KeyType.SPACE && key.isPressed) {
            isTrackpadActive = true
            lastTrackpadX = touchDownX
            trackpadAccumulatedDx = 0f
            performHaptic()
            invalidate()
        }
    }

    private fun startSpaceTrackpadLongPress() {
        mainHandler.postDelayed(trackpadRunnable, settings.longPressDurationMs.toLong())
    }

    private fun handleTrackpadMove(x: Float) {
        val dx = x - lastTrackpadX
        lastTrackpadX = x
        trackpadAccumulatedDx += dx

        val threshold = trackpadThresholdPx
        if (abs(trackpadAccumulatedDx) >= threshold) {
            val steps = (trackpadAccumulatedDx / threshold).toInt()
            if (steps != 0) {
                actionListener?.onSpaceTrackpadMove(steps)
                trackpadAccumulatedDx -= steps * threshold
                performHaptic()
            }
        }
    }

    // Backspace continuous repeat
    private val backspaceRepeatRunnable = object : Runnable {
        override fun run() {
            if (activeKey?.type == KeyType.BACKSPACE && activeKey?.isPressed == true) {
                isBackspaceRepeating = true
                actionListener?.onBackspaceRepeat()
                performHaptic()
                playSound(KeyType.BACKSPACE)
                mainHandler.postDelayed(this, backspaceRepeatIntervalMs)
            }
        }
    }

    private fun startBackspaceRepeat() {
        mainHandler.postDelayed(backspaceRepeatRunnable, backspaceInitialDelayMs)
    }

    private fun cancelLongPressTimers() {
        mainHandler.removeCallbacks(popupRunnable)
        mainHandler.removeCallbacks(trackpadRunnable)
        mainHandler.removeCallbacks(backspaceRepeatRunnable)
    }

    private fun findKeyAt(x: Float, y: Float): Key? {
        for (row in rows) {
            for (key in row) {
                if (key.bounds.contains(x, y)) {
                    return key
                }
            }
        }
        return null
    }

    private fun performHaptic() {
        if (settings.vibrateOnPress) {
            performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    private fun announceKey(key: Key) {
        try {
            val isShifted = shiftState.isShifted()
            val desc = when (key.type) {
                KeyType.CHARACTER -> key.getDisplayText(isShifted)
                KeyType.NUMBER -> key.primaryText
                KeyType.SPACE -> "Probel"
                KeyType.BACKSPACE -> "Öçiriş"
                KeyType.ENTER -> "Kiritiş"
                KeyType.SHIFT -> when (shiftState) {
                    ShiftState.OFF -> "Katta harf"
                    ShiftState.SHIFTED -> "Bosh harf yoqilgan"
                    ShiftState.CAPS_LOCK -> "Caps lock"
                }
                KeyType.SWITCH_SYMBOLS -> "Belgilar"
                KeyType.SWITCH_PAGE -> "Keyingi sahifa"
                KeyType.GLOBE -> "Tilni almaştiriş"
                KeyType.COMMA -> "Vergul"
                KeyType.PERIOD -> "Nuqta"
            }
            announceForAccessibility(desc)
        } catch (_: Exception) {}
    }

    private fun announceText(text: String) {
        try {
            announceForAccessibility(text)
        } catch (_: Exception) {}
    }
}
