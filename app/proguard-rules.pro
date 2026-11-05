# ProGuard / R8 Rules for Özbek Keyboard (uz.ozbekkeyboard)

# 1. Keep InputMethodService entry point and public callbacks
-keep public class uz.ozbekkeyboard.ime.OzbekInputMethodService {
    public *;
}

# 2. Keep Activities declared in AndroidManifest.xml
-keep public class uz.ozbekkeyboard.ui.OnboardingActivity {
    public *;
}
-keep public class uz.ozbekkeyboard.ui.SettingsActivity {
    public *;
}

# 3. Keep Custom UI Components instantiated programmatically or referenced in layouts
-keep class uz.ozbekkeyboard.ui.KeyboardView { *; }
-keep class uz.ozbekkeyboard.ui.KeyboardRootView { *; }
-keep class uz.ozbekkeyboard.ui.ToolbarView { *; }
-keep class uz.ozbekkeyboard.ui.EmojiPanelView { *; }
-keep class uz.ozbekkeyboard.ui.ClipboardPanelView { *; }
-keep class uz.ozbekkeyboard.ui.NoticePanelView { *; }

# 4. Keep Data Models and Preferences Enums
-keepclassmembers enum uz.ozbekkeyboard.settings.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers enum uz.ozbekkeyboard.layout.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers enum uz.ozbekkeyboard.ime.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 5. Preserve line numbers for safe debuggability and crash traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
