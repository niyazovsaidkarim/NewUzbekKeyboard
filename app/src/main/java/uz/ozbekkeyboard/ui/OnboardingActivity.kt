package uz.ozbekkeyboard.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import uz.ozbekkeyboard.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var cardStatusBanner: MaterialCardView
    private lateinit var tvStatusIcon: TextView
    private lateinit var tvStatusText: TextView

    private lateinit var cardStep1: MaterialCardView
    private lateinit var tvStep1Badge: TextView
    private lateinit var tvStep1Title: TextView
    private lateinit var btnEnableIme: Button
    private lateinit var tvRestrictedSettingsHelp: TextView

    private lateinit var cardStep2: MaterialCardView
    private lateinit var tvStep2Badge: TextView
    private lateinit var tvStep2Title: TextView
    private lateinit var btnSelectIme: Button
    private lateinit var btnSelectImeSamsungFallback: Button

    private lateinit var cardStep3: MaterialCardView
    private lateinit var tvStep3Badge: TextView
    private lateinit var tvStep3Title: TextView
    private lateinit var etTestInput: EditText

    private lateinit var btnOpenSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupListeners()
        updateStateUI()
    }

    override fun onResume() {
        super.onResume()
        updateStateUI()
    }

    private fun initViews() {
        cardStatusBanner = findViewById(R.id.cardStatusBanner)
        tvStatusIcon = findViewById(R.id.tvStatusIcon)
        tvStatusText = findViewById(R.id.tvStatusText)

        cardStep1 = findViewById(R.id.cardStep1)
        tvStep1Badge = findViewById(R.id.tvStep1Badge)
        tvStep1Title = findViewById(R.id.tvStep1Title)
        btnEnableIme = findViewById(R.id.btnEnableIme)
        tvRestrictedSettingsHelp = findViewById(R.id.tvRestrictedSettingsHelp)

        cardStep2 = findViewById(R.id.cardStep2)
        tvStep2Badge = findViewById(R.id.tvStep2Badge)
        tvStep2Title = findViewById(R.id.tvStep2Title)
        btnSelectIme = findViewById(R.id.btnSelectIme)
        btnSelectImeSamsungFallback = findViewById(R.id.btnSelectImeSamsungFallback)

        cardStep3 = findViewById(R.id.cardStep3)
        tvStep3Badge = findViewById(R.id.tvStep3Badge)
        tvStep3Title = findViewById(R.id.tvStep3Title)
        etTestInput = findViewById(R.id.etTestInput)

        btnOpenSettings = findViewById(R.id.btnOpenSettings)
    }

    private fun setupListeners() {
        // Step 1: Open system keyboard settings
        btnEnableIme.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // Help with Android 13/14/15 Restricted Settings
        tvRestrictedSettingsHelp.setOnClickListener {
            showRestrictedSettingsDialog()
        }

        // Step 2: Open input method picker
        btnSelectIme.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }

        // Step 2 Fallback: Open Samsung settings directly to choose default keyboard
        btnSelectImeSamsungFallback.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // Step 3: Interactive test typing area
        etTestInput.setOnClickListener {
            etTestInput.requestFocus()
        }

        // Open settings
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateStateUI() {
        val isEnabled = isImeEnabled(this)
        val isSelected = isImeSelected(this)

        val colorActiveYellow = Color.parseColor("#FFD500")
        val colorActiveOrange = Color.parseColor("#FF8C00")
        val colorSuccessGreen = Color.parseColor("#4CAF50")
        val colorInactive = Color.parseColor("#333333")
        val colorTextDim = Color.parseColor("#777777")
        val colorTextWhite = Color.parseColor("#FFFFFF")

        when {
            // STATE A: Installed but not enabled
            !isEnabled -> {
                cardStatusBanner.strokeColor = colorActiveYellow
                tvStatusIcon.text = "1️⃣"
                tvStatusText.text = "1-bosqich: «Özbek Keyboard»ni tizim sozlamalarida yoqing."

                // Step 1 Active
                cardStep1.alpha = 1.0f
                cardStep1.strokeColor = colorActiveYellow
                tvStep1Badge.text = "1"
                tvStep1Badge.setBackgroundColor(colorActiveYellow)
                tvStep1Badge.setTextColor(Color.BLACK)
                btnEnableIme.isEnabled = true
                btnEnableIme.visibility = View.VISIBLE
                tvRestrictedSettingsHelp.visibility = View.VISIBLE

                // Step 2 Inactive
                cardStep2.alpha = 0.45f
                cardStep2.strokeColor = colorInactive
                tvStep2Badge.text = "2"
                tvStep2Badge.setBackgroundColor(colorInactive)
                tvStep2Badge.setTextColor(colorTextDim)
                btnSelectIme.isEnabled = false
                btnSelectImeSamsungFallback.visibility = View.GONE

                // Step 3 Inactive
                cardStep3.alpha = 0.45f
                cardStep3.strokeColor = colorInactive
                tvStep3Badge.text = "3"
                tvStep3Badge.setBackgroundColor(colorInactive)
                tvStep3Badge.setTextColor(colorTextDim)
                etTestInput.isEnabled = false
            }

            // STATE B: Enabled but not selected as default
            isEnabled && !isSelected -> {
                cardStatusBanner.strokeColor = colorActiveOrange
                tvStatusIcon.text = "2️⃣"
                tvStatusText.text = "✓ 1-bosqich bajarildi! Endi «Özbek Keyboard»ni standart klaviatura qilib tanlang."

                // Step 1 Completed
                cardStep1.alpha = 0.85f
                cardStep1.strokeColor = colorSuccessGreen
                tvStep1Badge.text = "✓"
                tvStep1Badge.setBackgroundColor(colorSuccessGreen)
                tvStep1Badge.setTextColor(Color.WHITE)
                tvStep1Title.text = "1. Tizim sozlamalarida yoqildi (Bajarildi)"
                btnEnableIme.visibility = View.GONE
                tvRestrictedSettingsHelp.visibility = View.GONE

                // Step 2 Active
                cardStep2.alpha = 1.0f
                cardStep2.strokeColor = colorActiveOrange
                tvStep2Badge.text = "2"
                tvStep2Badge.setBackgroundColor(colorActiveOrange)
                tvStep2Badge.setTextColor(Color.BLACK)
                btnSelectIme.isEnabled = true
                btnSelectIme.visibility = View.VISIBLE
                btnSelectImeSamsungFallback.visibility = View.VISIBLE

                // Step 3 Inactive
                cardStep3.alpha = 0.5f
                cardStep3.strokeColor = colorInactive
                tvStep3Badge.text = "3"
                tvStep3Badge.setBackgroundColor(colorInactive)
                tvStep3Badge.setTextColor(colorTextDim)
                etTestInput.isEnabled = false
            }

            // STATE C: Fully enabled and selected as active default keyboard
            isEnabled && isSelected -> {
                cardStatusBanner.strokeColor = colorSuccessGreen
                tvStatusIcon.text = "🎉"
                tvStatusText.text = "Özbek Keyboard to‘liq faollashtirildi va tanlandi! Sinab ko‘rishingiz mumkin."

                // Step 1 Completed
                cardStep1.alpha = 0.8f
                cardStep1.strokeColor = colorSuccessGreen
                tvStep1Badge.text = "✓"
                tvStep1Badge.setBackgroundColor(colorSuccessGreen)
                tvStep1Badge.setTextColor(Color.WHITE)
                btnEnableIme.visibility = View.GONE
                tvRestrictedSettingsHelp.visibility = View.GONE

                // Step 2 Completed
                cardStep2.alpha = 0.8f
                cardStep2.strokeColor = colorSuccessGreen
                tvStep2Badge.text = "✓"
                tvStep2Badge.setBackgroundColor(colorSuccessGreen)
                tvStep2Badge.setTextColor(Color.WHITE)
                btnSelectIme.visibility = View.GONE
                btnSelectImeSamsungFallback.visibility = View.GONE

                // Step 3 Active!
                cardStep3.alpha = 1.0f
                cardStep3.strokeColor = colorSuccessGreen
                tvStep3Badge.text = "✓"
                tvStep3Badge.setBackgroundColor(colorSuccessGreen)
                tvStep3Badge.setTextColor(Color.WHITE)
                etTestInput.isEnabled = true
                etTestInput.requestFocus()
            }
        }
    }

    private fun showRestrictedSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Samsung va Android 13/14/15 cheklovi")
            .setMessage(
                "Agar klaviatura kaliti yoqilmasa yoki 'Cheklangan sozlama' (Restricted Setting) xabari chiqsa:\n\n" +
                "1. Quyidagi 'Ilova ma'lumotlarini ochish' tugmasini bosing.\n" +
                "2. Yuqori o‘ng burchakdagi 3 ta nuqtani (⋮) bosing.\n" +
                "3. «Cheklangan sozlamalarga ruxsat berish» (Allow restricted settings) bandini tanlang va PIN kodingizni kiriting.\n" +
                "4. Shundan so‘ng bu yerga qaytib, klaviaturani bemalol yoqishingiz mumkin."
            )
            .setPositiveButton("Ilova ma'lumotlarini ochish") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Yopish", null)
            .show()
    }

    companion object {
        fun isImeEnabled(context: Context): Boolean {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
            val enabledImes = imm.enabledInputMethodList
            return enabledImes.any { it.packageName == context.packageName }
        }

        fun isImeSelected(context: Context): Boolean {
            val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            return defaultIme != null && defaultIme.startsWith("${context.packageName}/")
        }
    }
}
