package uz.ozbekkeyboard.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.clipboard.ClipboardHelper
import uz.ozbekkeyboard.settings.AppTheme
import uz.ozbekkeyboard.settings.KeyboardHeight
import uz.ozbekkeyboard.settings.KeyboardSettings

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: KeyboardSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = KeyboardSettings(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        initAppearanceSettings()
        initTypingSettings()
        initClipboardSettings()
        initResetButton()
    }

    private fun initAppearanceSettings() {
        val rgTheme = findViewById<RadioGroup>(R.id.rgTheme)
        when (settings.theme) {
            AppTheme.BLACK_YELLOW -> findViewById<RadioButton>(R.id.rbThemeBlackYellow).isChecked = true
            AppTheme.FOLLOW_SYSTEM -> findViewById<RadioButton>(R.id.rbThemeSystem).isChecked = true
            AppTheme.MINIMAL_LIGHT -> findViewById<RadioButton>(R.id.rbThemeLight).isChecked = true
            AppTheme.MINIMAL_DARK -> findViewById<RadioButton>(R.id.rbThemeDark).isChecked = true
        }

        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.rbThemeBlackYellow -> AppTheme.BLACK_YELLOW
                R.id.rbThemeSystem -> AppTheme.FOLLOW_SYSTEM
                R.id.rbThemeLight -> AppTheme.MINIMAL_LIGHT
                R.id.rbThemeDark -> AppTheme.MINIMAL_DARK
                else -> AppTheme.BLACK_YELLOW
            }
            settings.setTheme(selectedTheme)
        }

        val switchNumberRow = findViewById<MaterialSwitch>(R.id.switchNumberRow)
        switchNumberRow.isChecked = settings.showNumberRow
        switchNumberRow.setOnCheckedChangeListener { _, isChecked ->
            settings.setShowNumberRow(isChecked)
        }

        val rgHeight = findViewById<RadioGroup>(R.id.rgHeight)
        when (settings.keyboardHeight) {
            KeyboardHeight.LOW -> findViewById<RadioButton>(R.id.rbHeightLow).isChecked = true
            KeyboardHeight.NORMAL -> findViewById<RadioButton>(R.id.rbHeightNormal).isChecked = true
            KeyboardHeight.HIGH -> findViewById<RadioButton>(R.id.rbHeightHigh).isChecked = true
        }

        rgHeight.setOnCheckedChangeListener { _, checkedId ->
            val selectedHeight = when (checkedId) {
                R.id.rbHeightLow -> KeyboardHeight.LOW
                R.id.rbHeightNormal -> KeyboardHeight.NORMAL
                R.id.rbHeightHigh -> KeyboardHeight.HIGH
                else -> KeyboardHeight.NORMAL
            }
            settings.setKeyboardHeight(selectedHeight)
        }
    }

    private fun initTypingSettings() {
        val switchVibration = findViewById<MaterialSwitch>(R.id.switchVibration)
        switchVibration.isChecked = settings.vibrateOnPress
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            settings.setVibrate(isChecked)
        }

        val switchSound = findViewById<MaterialSwitch>(R.id.switchSound)
        switchSound.isChecked = settings.soundOnPress
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            settings.setSound(isChecked)
        }

        val switchPopup = findViewById<MaterialSwitch>(R.id.switchPopup)
        switchPopup.isChecked = settings.longPressPopup
        switchPopup.setOnCheckedChangeListener { _, isChecked ->
            settings.setLongPressPopup(isChecked)
        }

        val switchSpaceCursor = findViewById<MaterialSwitch>(R.id.switchSpaceCursor)
        switchSpaceCursor.isChecked = settings.spaceCursorControl
        switchSpaceCursor.setOnCheckedChangeListener { _, isChecked ->
            settings.setSpaceCursor(isChecked)
        }

        val switchAutoCap = findViewById<MaterialSwitch>(R.id.switchAutoCap)
        switchAutoCap.isChecked = settings.autoCapitalization
        switchAutoCap.setOnCheckedChangeListener { _, isChecked ->
            settings.setAutoCapitalization(isChecked)
        }

        val switchDoubleSpace = findViewById<MaterialSwitch>(R.id.switchDoubleSpace)
        switchDoubleSpace.isChecked = settings.doubleSpacePeriod
        switchDoubleSpace.setOnCheckedChangeListener { _, isChecked ->
            settings.setDoubleSpacePeriod(isChecked)
        }

        val switchSuggestions = findViewById<MaterialSwitch>(R.id.switchSuggestions)
        switchSuggestions.isChecked = settings.wordSuggestions
        switchSuggestions.setOnCheckedChangeListener { _, isChecked ->
            settings.setWordSuggestions(isChecked)
        }
    }

    private fun initClipboardSettings() {
        val switchClipboardHistory = findViewById<MaterialSwitch>(R.id.switchClipboardHistory)
        switchClipboardHistory.isChecked = settings.clipboardHistoryEnabled
        switchClipboardHistory.setOnCheckedChangeListener { _, isChecked ->
            settings.setClipboardHistory(isChecked)
            if (!isChecked) {
                ClipboardHelper.clearSharedHistory()
            }
        }

        val btnClearClipboard = findViewById<MaterialButton>(R.id.btnClearClipboard)
        btnClearClipboard.setOnClickListener {
            ClipboardHelper.clearSharedHistory()
            Toast.makeText(this, R.string.clipboard_cleared, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initResetButton() {
        val btnReset = findViewById<MaterialButton>(R.id.btnReset)
        btnReset.setOnClickListener {
            settings.resetToDefaults()
            Toast.makeText(this, "Sozlamalar asl holatiga qaytarildi", Toast.LENGTH_SHORT).show()
            recreate()
        }
    }
}
