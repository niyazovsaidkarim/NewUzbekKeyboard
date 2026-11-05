# Samsung Galaxy Device Diagnostic Guide for Özbek Keyboard

This guide provides exact commands and instructions to diagnose, verify, and debug keyboard enablement and selection on a Samsung Galaxy phone.

---

## 1. Quick ADB Diagnostic Commands

Connect your Samsung Galaxy phone to your PC via USB with **USB Debugging** enabled (`Settings -> Developer options -> USB debugging`).

### Check Connection
```bash
adb devices -l
```
*Expected: Device serial number followed by `device` (e.g. `RF8M... device product:e1sxxx model:SM-S928B`).*

---

## 2. Input Method Framework (IMF) Diagnostics

### A. List All Installed IMEs on the Device
```bash
adb shell ime list -a
```
*Check that `uz.ozbekkeyboard/.ime.OzbekInputMethodService` is listed.*  
Look for:
- `mId=uz.ozbekkeyboard/.ime.OzbekInputMethodService`
- `mSettingsActivity=uz.ozbekkeyboard.ui.SettingsActivity`
- `Subtypes:` Mode `keyboard`, Locale `uz-Latn`, `isAsciiCapable=true`.

### B. List Only Currently Enabled IMEs
```bash
adb shell ime list -s
```
*If `uz.ozbekkeyboard/.ime.OzbekInputMethodService` is NOT in this list, the keyboard has not been enabled in system settings.*

### C. Inspect System Secure Settings
```bash
adb shell settings get secure enabled_input_methods
```
*This prints the colon-separated list of enabled IMEs.*

```bash
adb shell settings get secure default_input_method
```
*This prints the currently active default keyboard (e.g. `com.sec.android.inputmethod/.SamsungKeypad` or `uz.ozbekkeyboard/.ime.OzbekInputMethodService`).*

### D. Full Input Method Service Dump
```bash
adb shell dumpsys input_method
```
*Look for `mCurMethodId`, `mCurFocusedWindow`, and `mCurClient`.*

---

## 3. Enable and Select via ADB (Direct Verification)

To verify the keyboard directly on a connected device via ADB without UI clicks:

### 1. Enable the Keyboard:
```bash
adb shell ime enable uz.ozbekkeyboard/.ime.OzbekInputMethodService
```
*Expected output: `Input method uz.ozbekkeyboard/.ime.OzbekInputMethodService: already enabled` or `now enabled`.*

### 2. Set as Default Keyboard:
```bash
adb shell ime set uz.ozbekkeyboard/.ime.OzbekInputMethodService
```
*Expected output: `Input method uz.ozbekkeyboard/.ime.OzbekInputMethodService selected`.*

---

## 4. Capturing Real-time Crash or Permission Logs

To monitor crashes, permission denials, or Restricted Settings blocks:

```bash
adb logcat -c
adb logcat | grep -iE "InputMethod|uz.ozbekkeyboard|RestrictedSettings|AutoBlocker|ActivityManager"
```

---

## 5. Samsung One UI Troubleshooting Checklist (Without ADB)

If you are using the phone directly without a PC:

1. **Check Samsung Auto Blocker:**
   - Go to `Settings -> Security and privacy -> Auto Blocker`.
   - Ensure it is **OFF** when installing or activating sideloaded apps.

2. **Check Android Restricted Settings (Android 13 / 14 / 15):**
   - Go to `Settings -> Apps -> Özbek Keyboard`.
   - Tap the 3 dots in the top-right corner.
   - Tap **"Allow restricted settings"** and confirm your PIN/biometrics.

3. **Enable in Samsung Settings:**
   - Go to `Settings -> General management -> Keyboard list and default`.
   - Toggle **Özbek Keyboard** to **ON**.
   - Tap **OK** on the warning dialog.

4. **Select as Default:**
   - On the same screen, tap **Default keyboard** at the top.
   - Select **Özbek Keyboard**.
