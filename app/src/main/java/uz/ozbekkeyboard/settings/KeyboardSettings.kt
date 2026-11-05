package uz.ozbekkeyboard.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_settings")

enum class AppTheme(val id: String) {
    FOLLOW_SYSTEM("follow_system"),
    BLACK_YELLOW("black_yellow"),
    MINIMAL_LIGHT("minimal_light"),
    MINIMAL_DARK("minimal_dark");

    companion object {
        fun fromId(id: String?): AppTheme {
            return entries.firstOrNull { it.id == id } ?: BLACK_YELLOW
        }
    }
}

enum class KeyboardHeight(val id: String, val factor: Float) {
    LOW("low", 0.88f),
    NORMAL("normal", 1.0f),
    HIGH("high", 1.15f);

    companion object {
        fun fromId(id: String?): KeyboardHeight {
            return entries.firstOrNull { it.id == id } ?: NORMAL
        }
    }
}

class KeyboardSettings(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val KEY_KEYBOARD_HEIGHT = stringPreferencesKey("keyboard_height")
        val KEY_VIBRATE = booleanPreferencesKey("vibrate")
        val KEY_SOUND = booleanPreferencesKey("sound")
        val KEY_LONG_PRESS_POPUP = booleanPreferencesKey("long_press_popup")
        val KEY_LONG_PRESS_DURATION_MS = intPreferencesKey("long_press_duration_ms")
        val KEY_SPACE_CURSOR = booleanPreferencesKey("space_cursor")
        val KEY_AUTO_CAPITALIZATION = booleanPreferencesKey("auto_capitalization")
        val KEY_DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
        val KEY_KEY_PREVIEW = booleanPreferencesKey("key_preview")
        val KEY_WORD_SUGGESTIONS = booleanPreferencesKey("word_suggestions")
        val KEY_CLIPBOARD_HISTORY = booleanPreferencesKey("clipboard_history")
    }

    // Cached memory state for instantaneous access on the UI thread without blocking
    @Volatile var theme: AppTheme = AppTheme.BLACK_YELLOW
        private set
    @Volatile var showNumberRow: Boolean = true
        private set
    @Volatile var keyboardHeight: KeyboardHeight = KeyboardHeight.NORMAL
        private set
    @Volatile var vibrateOnPress: Boolean = true
        private set
    @Volatile var soundOnPress: Boolean = false
        private set
    @Volatile var longPressPopup: Boolean = true
        private set
    @Volatile var longPressDurationMs: Int = 400
        private set
    @Volatile var spaceCursorControl: Boolean = true
        private set
    @Volatile var autoCapitalization: Boolean = true
        private set
    @Volatile var doubleSpacePeriod: Boolean = true
        private set
    @Volatile var keyPreview: Boolean = false
        private set
    @Volatile var wordSuggestions: Boolean = true
        private set
    @Volatile var clipboardHistoryEnabled: Boolean = false
        private set

    init {
        scope.launch {
            try {
                loadInitialValues()
            } catch (_: Exception) {
                // Default fallbacks remain in effect
            }
        }
    }

    private suspend fun loadInitialValues() {
        val prefs = context.dataStore.data.first()
        theme = AppTheme.fromId(prefs[KEY_THEME])
        showNumberRow = prefs[KEY_SHOW_NUMBER_ROW] ?: true
        keyboardHeight = KeyboardHeight.fromId(prefs[KEY_KEYBOARD_HEIGHT])
        vibrateOnPress = prefs[KEY_VIBRATE] ?: true
        soundOnPress = prefs[KEY_SOUND] ?: false
        longPressPopup = prefs[KEY_LONG_PRESS_POPUP] ?: true
        longPressDurationMs = prefs[KEY_LONG_PRESS_DURATION_MS] ?: 400
        spaceCursorControl = prefs[KEY_SPACE_CURSOR] ?: true
        autoCapitalization = prefs[KEY_AUTO_CAPITALIZATION] ?: true
        doubleSpacePeriod = prefs[KEY_DOUBLE_SPACE_PERIOD] ?: true
        keyPreview = prefs[KEY_KEY_PREVIEW] ?: false
        wordSuggestions = prefs[KEY_WORD_SUGGESTIONS] ?: true
        clipboardHistoryEnabled = prefs[KEY_CLIPBOARD_HISTORY] ?: false
    }

    fun setTheme(newTheme: AppTheme) {
        theme = newTheme
        scope.launch {
            context.dataStore.edit { it[KEY_THEME] = newTheme.id }
        }
    }

    fun setShowNumberRow(show: Boolean) {
        showNumberRow = show
        scope.launch {
            context.dataStore.edit { it[KEY_SHOW_NUMBER_ROW] = show }
        }
    }

    fun setKeyboardHeight(height: KeyboardHeight) {
        keyboardHeight = height
        scope.launch {
            context.dataStore.edit { it[KEY_KEYBOARD_HEIGHT] = height.id }
        }
    }

    fun setVibrate(vibrate: Boolean) {
        vibrateOnPress = vibrate
        scope.launch {
            context.dataStore.edit { it[KEY_VIBRATE] = vibrate }
        }
    }

    fun setSound(sound: Boolean) {
        soundOnPress = sound
        scope.launch {
            context.dataStore.edit { it[KEY_SOUND] = sound }
        }
    }

    fun setLongPressPopup(enabled: Boolean) {
        longPressPopup = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_LONG_PRESS_POPUP] = enabled }
        }
    }

    fun setLongPressDurationMs(durationMs: Int) {
        longPressDurationMs = durationMs.coerceIn(250, 700)
        scope.launch {
            context.dataStore.edit { it[KEY_LONG_PRESS_DURATION_MS] = longPressDurationMs }
        }
    }

    fun setSpaceCursor(enabled: Boolean) {
        spaceCursorControl = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_SPACE_CURSOR] = enabled }
        }
    }

    fun setAutoCapitalization(enabled: Boolean) {
        autoCapitalization = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_AUTO_CAPITALIZATION] = enabled }
        }
    }

    fun setDoubleSpacePeriod(enabled: Boolean) {
        doubleSpacePeriod = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_DOUBLE_SPACE_PERIOD] = enabled }
        }
    }

    fun setKeyPreview(enabled: Boolean) {
        keyPreview = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_KEY_PREVIEW] = enabled }
        }
    }

    fun setWordSuggestions(enabled: Boolean) {
        wordSuggestions = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_WORD_SUGGESTIONS] = enabled }
        }
    }

    fun setClipboardHistory(enabled: Boolean) {
        clipboardHistoryEnabled = enabled
        scope.launch {
            context.dataStore.edit { it[KEY_CLIPBOARD_HISTORY] = enabled }
        }
    }

    fun resetToDefaults() {
        theme = AppTheme.BLACK_YELLOW
        showNumberRow = true
        keyboardHeight = KeyboardHeight.NORMAL
        vibrateOnPress = true
        soundOnPress = false
        longPressPopup = true
        longPressDurationMs = 400
        spaceCursorControl = true
        autoCapitalization = true
        doubleSpacePeriod = true
        keyPreview = false
        wordSuggestions = true
        clipboardHistoryEnabled = false

        scope.launch {
            context.dataStore.edit { it.clear() }
        }
    }
}
