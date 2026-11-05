package uz.ozbekkeyboard.theme

import android.content.Context
import android.content.res.Configuration
import uz.ozbekkeyboard.settings.AppTheme

data class KeyboardTheme(
    val name: String,
    val keyboardBackground: Int,
    val keyBackgroundAlphabet: Int,
    val keyBackgroundNumber: Int,
    val keyBackgroundAction: Int,
    val keyBackgroundPressed: Int,
    val keyTextPrimary: Int,
    val keyTextSecondary: Int,
    val keyTextAction: Int,
    val keyIconTint: Int,
    val keyIconActionTint: Int,
    val popupBackground: Int,
    val popupKeyBackground: Int,
    val popupKeySelected: Int,
    val popupText: Int,
    val toolbarBackground: Int,
    val toolbarIconTint: Int,
    val toolbarActiveTint: Int,
    val suggestionText: Int,
    val suggestionDivider: Int,
    val trackpadHighlight: Int,
    val keyCornerRadiusDp: Float = 6f
)

object ThemeManager {

    val BLACK_YELLOW = KeyboardTheme(
        name = "Black & Yellow",
        keyboardBackground = 0xFF121212.toInt(),
        keyBackgroundAlphabet = 0xFFFFD500.toInt(), // Bright yellow
        keyBackgroundNumber = 0xFFFF8C00.toInt(),   // Warm orange
        keyBackgroundAction = 0xFFFFFFFF.toInt(),   // White
        keyBackgroundPressed = 0xFFE0A800.toInt(),
        keyTextPrimary = 0xFF000000.toInt(),        // Black text
        keyTextSecondary = 0xFF333333.toInt(),
        keyTextAction = 0xFF000000.toInt(),         // Black text
        keyIconTint = 0xFF000000.toInt(),           // Black icons
        keyIconActionTint = 0xFF000000.toInt(),     // Black icons on white action keys
        popupBackground = 0xFF1C1C1E.toInt(),
        popupKeyBackground = 0xFF2C2C2E.toInt(),
        popupKeySelected = 0xFFFFD500.toInt(),
        popupText = 0xFFFFFFFF.toInt(),
        toolbarBackground = 0xFF1C1C1E.toInt(),
        toolbarIconTint = 0xFFFFFFFF.toInt(),
        toolbarActiveTint = 0xFFFFD500.toInt(),
        suggestionText = 0xFFFFD500.toInt(),
        suggestionDivider = 0xFF333333.toInt(),
        trackpadHighlight = 0xFFFFD500.toInt()
    )

    val MINIMAL_LIGHT = KeyboardTheme(
        name = "Minimal Light",
        keyboardBackground = 0xFFECEFF1.toInt(),
        keyBackgroundAlphabet = 0xFFFFFFFF.toInt(),
        keyBackgroundNumber = 0xFFF5F7FA.toInt(),
        keyBackgroundAction = 0xFFCFD8DC.toInt(),
        keyBackgroundPressed = 0xFFB0BEC5.toInt(),
        keyTextPrimary = 0xFF212121.toInt(),
        keyTextSecondary = 0xFF757575.toInt(),
        keyTextAction = 0xFF212121.toInt(),
        keyIconTint = 0xFF212121.toInt(),
        keyIconActionTint = 0xFF212121.toInt(),
        popupBackground = 0xFFFFFFFF.toInt(),
        popupKeyBackground = 0xFFECEFF1.toInt(),
        popupKeySelected = 0xFF2196F3.toInt(),
        popupText = 0xFF212121.toInt(),
        toolbarBackground = 0xFFDFE4E8.toInt(),
        toolbarIconTint = 0xFF37474F.toInt(),
        toolbarActiveTint = 0xFF1976D2.toInt(),
        suggestionText = 0xFF212121.toInt(),
        suggestionDivider = 0xFFB0BEC5.toInt(),
        trackpadHighlight = 0xFF1976D2.toInt()
    )

    val MINIMAL_DARK = KeyboardTheme(
        name = "Minimal Dark",
        keyboardBackground = 0xFF18191A.toInt(),
        keyBackgroundAlphabet = 0xFF3A3B3C.toInt(),
        keyBackgroundNumber = 0xFF303134.toInt(),
        keyBackgroundAction = 0xFF242526.toInt(),
        keyBackgroundPressed = 0xFF4E4F50.toInt(),
        keyTextPrimary = 0xFFE4E6EB.toInt(),
        keyTextSecondary = 0xFFB0B3B8.toInt(),
        keyTextAction = 0xFFE4E6EB.toInt(),
        keyIconTint = 0xFFE4E6EB.toInt(),
        keyIconActionTint = 0xFFE4E6EB.toInt(),
        popupBackground = 0xFF242526.toInt(),
        popupKeyBackground = 0xFF3A3B3C.toInt(),
        popupKeySelected = 0xFF007AFF.toInt(),
        popupText = 0xFFE4E6EB.toInt(),
        toolbarBackground = 0xFF242526.toInt(),
        toolbarIconTint = 0xFFE4E6EB.toInt(),
        toolbarActiveTint = 0xFF2D88FF.toInt(),
        suggestionText = 0xFFE4E6EB.toInt(),
        suggestionDivider = 0xFF3A3B3C.toInt(),
        trackpadHighlight = 0xFF2D88FF.toInt()
    )

    fun resolveTheme(context: Context, appTheme: AppTheme): KeyboardTheme {
        return when (appTheme) {
            AppTheme.BLACK_YELLOW -> BLACK_YELLOW
            AppTheme.MINIMAL_LIGHT -> MINIMAL_LIGHT
            AppTheme.MINIMAL_DARK -> MINIMAL_DARK
            AppTheme.FOLLOW_SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    MINIMAL_DARK
                } else {
                    MINIMAL_LIGHT
                }
            }
        }
    }
}
