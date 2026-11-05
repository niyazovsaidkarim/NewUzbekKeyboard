# Android & Samsung One UI Input Method Editor (IME) Requirements Report

**Author:** Senior Android OS Engineer & Technical Researcher  
**Target Application:** Özbek Keyboard (`uz.ozbekkeyboard`)  
**Date:** November 2026  
**Document Version:** 1.0.0  

---

## 1. Executive Summary

This report establishes the official requirements for building, registering, enabling, and selecting a native Android system keyboard (Input Method Editor - IME) on Android 8.0 through Android 16, with specific focus on **Samsung Galaxy devices running One UI (versions 5.0, 6.0, 6.1, and 7.0)**.

Our investigation of the observed failure — where the user installs the APK, sees Özbek Keyboard in Samsung settings, taps the toggle, accepts the security warning, but reports the keyboard is not enabled and absent from the input method picker — identifies two interrelated causes:
1. **IME Subtype Metadata Deficiencies:** The existing `res/xml/method.xml` omitted critical subtype attributes (`android:isAsciiCapable="true"`, `android:languageTag`, and `android:showInInputMethodPicker="true"`). Without `isAsciiCapable`, modern Android and Samsung One UI input method controllers restrict the IME from standard text selection and default keyboard promotion.
2. **Android 13/14/15 "Restricted Settings" & Samsung "Auto Blocker" on Sideloaded APKs:** On Android 13+, non-store (sideloaded) APKs are subjected to Android's "Restricted Settings" policy. On Samsung One UI 6+, Samsung's "Auto Blocker" further restricts sideloaded apps from binding or persisting permission changes. Unless the user explicitly grants "Allow restricted settings" in App Info or disables Auto Blocker, the system silently rolls back the enablement toggle.
3. **Passive Onboarding Activity:** The existing `OnboardingActivity` did not query `InputMethodManager.getEnabledInputMethodList()` or `Settings.Secure.DEFAULT_INPUT_METHOD`, nor did it update dynamically on `onResume()`. When users tapped "Select Keyboard", `showInputMethodPicker()` showed only Samsung Keyboard because the system had not persisted Özbek Keyboard into the enabled list.

---

## 2. Official Android IME Architecture

### 2.1 The Three States of an Android Keyboard
Android strictly distinguishes three operational states for any input method:

```
[ State A: Installed ]
       │
       ▼  User toggles switch in Settings.ACTION_INPUT_METHOD_SETTINGS
[ State B: Enabled ]   (Stored in Settings.Secure.ENABLED_INPUT_METHODS)
       │
       ▼  User selects IME in Input Method Picker or Settings.Secure.DEFAULT_INPUT_METHOD
[ State C: Selected / Active ]
```

1. **Installed:** The APK is on the device. Package Manager discovers the service implementing `android.view.InputMethod`. The IME is visible in `Settings -> General management -> Keyboard list and default`, but cannot receive user keystrokes.
2. **Enabled:** The user explicitly toggles the IME to ON in system settings and confirms the system security disclosure. The IME ID (`uz.ozbekkeyboard/.ime.OzbekInputMethodService`) is added to `Settings.Secure.ENABLED_INPUT_METHODS`. Only enabled IMEs appear in the system input method switcher (`showInputMethodPicker()`).
3. **Selected (Default):** The user selects the enabled IME as the default input provider (`Settings.Secure.DEFAULT_INPUT_METHOD`). The system binds to the service and routes `InputConnection` events to it when text fields gain focus.

### 2.2 What Makes an Application a Valid System Input Method?
Under official Android documentation ([Android Developers: Creating an Input Method](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)):
- A service extending `android.inputmethodservice.InputMethodService`.
- A `<service>` entry in `AndroidManifest.xml` protected by the system-only permission:
  `android:permission="android.permission.BIND_INPUT_METHOD"`.
- An `<intent-filter>` listening for `android.view.InputMethod`.
- A `<meta-data>` tag with `android:name="android.view.im"` pointing to an XML resource (e.g. `@xml/method`).
- Valid `<input-method>` XML declaring at least one non-auxiliary, ASCII-capable `<subtype>`.

### 2.3 Required AndroidManifest.xml Configuration

```xml
<service
    android:name="uz.ozbekkeyboard.ime.OzbekInputMethodService"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher"
    android:permission="android.permission.BIND_INPUT_METHOD"
    android:exported="true"
    android:directBootAware="true">
    <intent-filter>
        <action android:name="android.view.InputMethod" />
    </intent-filter>
    <meta-data
        android:name="android.view.im"
        android:resource="@xml/method" />
</service>
```

#### Key Element Rules:
- `android:permission="android.permission.BIND_INPUT_METHOD"`: Mandatory. Prevents malicious apps from binding directly to your keyboard. Only `system_server` can bind to services protected by this permission.
- `android:exported="true"`: Mandatory on Android 12+ (API 31+) for any component declaring an intent filter.
- `android:directBootAware="true"`: Recommended on Android 7.0+ (API 24+) to avoid blocking or scaring users with lock-screen reboot warnings.

### 2.4 Required IME Metadata XML (`res/xml/method.xml`)
The `<input-method>` resource defines how Android's `InputMethodManagerService` (IMMS) categorizes the keyboard:

```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="uz.ozbekkeyboard.ui.SettingsActivity"
    android:supportsSwitchingToNextInputMethod="true"
    android:showInInputMethodPicker="true">

    <!-- Primary Uzbek Latin Subtype -->
    <subtype
        android:label="@string/subtype_uzbek"
        android:icon="@mipmap/ic_launcher"
        android:languageTag="uz-Latn"
        android:imeSubtypeLocale="uz"
        android:imeSubtypeMode="keyboard"
        android:isAsciiCapable="true" />

</input-method>
```

#### Critical Subtype Attributes:
- **`android:isAsciiCapable="true"`**: Critical. Informs Android and Samsung One UI that this keyboard can type basic ASCII characters (`a-z`, `0-9`, punctuation). Without this, the system restricts the keyboard from being used in password/URL fields and will omit it from certain picker dialogs.
- **`android:languageTag="uz-Latn"`**: Standard BCP-47 language tag (introduced in API 24).
- **`android:imeSubtypeLocale="uz"`**: Legacy locale tag for backwards compatibility.
- **`android:imeSubtypeMode="keyboard"`**: Specifies that this is a virtual keyboard (not a voice or handwriting engine).
- **`android:showInInputMethodPicker="true"`**: Explicitly ensures visibility in system switcher dialogs.

---

## 3. Samsung One UI Findings & Compatibility

### 3.1 Does Samsung Require Proprietary SDKs or Signing?
**No.** Samsung Galaxy devices run Android with Samsung's One UI skin. Samsung complies with the standard Android Input Method Framework (IMF). Any standard Android `InputMethodService` that adheres to official Android specifications is recognized by Samsung One UI.

Samsung does **not** require:
- Samsung Knox SDK for standard keyboards.
- Proprietary Samsung certificates or Galaxy Store distribution.
- Root or Knox attestation.

### 3.2 Samsung-Specific Security & Management Policies
While standard Android APIs apply, Samsung One UI includes OEM-specific security layers:

#### A. Samsung "Auto Blocker" (One UI 6.0+ / Android 14+)
- **Location:** `Settings -> Security and privacy -> Auto Blocker`
- **Behavior:** Blocks app installation and sideloaded app operations from unauthorized sources.
- **Impact:** If active, it can block sideloaded APK services from being enabled or prevent persistent writes to `Settings.Secure.ENABLED_INPUT_METHODS`.
- **Resolution:** The user must toggle Auto Blocker OFF or allow installation.

#### B. Android 13/14/15 "Restricted Settings" for Sideloaded APKs
- Introduced by Google in Android 13 and enforced by Samsung One UI.
- When an APK is installed manually (via file manager, browser, or messaging app), sensitive system access is guarded by "Restricted Settings".
- If restricted, toggling the keyboard in settings may show: *"Restricted setting: For your security, this setting is currently unavailable"*, or silently revert to OFF.
- **Resolution:** 
  1. Open `Settings -> Apps -> Özbek Keyboard`.
  2. Tap the 3 dots in the top-right corner.
  3. Tap **"Allow restricted settings"** and authenticate with PIN/fingerprint.
  4. Return to Keyboard settings and turn the switch ON.

#### C. Samsung "Keyboard list and default" Navigation Structure
In Samsung One UI:
- `Settings -> General management -> Keyboard list and default`
- Row 1: **"Default keyboard"** (Displays currently selected keyboard, e.g. Samsung Keyboard).
- Row 2+: **"Keyboard list"** (Toggle switches for each installed keyboard).
- **Crucial user confusion point:** Turning the toggle switch to ON in the list does **not** automatically set it as the default keyboard! The user must also tap "Default keyboard" at the top of that screen or select it in the IME picker.

---

## 4. Audit of Current Implementation Failures

| Component | Current State | Defect / Failure Mechanism | Required Fix |
| :--- | :--- | :--- | :--- |
| `res/xml/method.xml` | Missing `isAsciiCapable`, `languageTag`, and `showInInputMethodPicker`. Included invalid `android:icon` and `android:label` on root tag. | IMMS / Samsung One UI does not recognize the subtype as ASCII-capable, hindering default keyboard promotion. | Add `android:isAsciiCapable="true"`, `android:languageTag="uz-Latn"`, `android:showInInputMethodPicker="true"`, and clean root attributes. |
| `AndroidManifest.xml` | `<service>` had no `android:icon` or `android:directBootAware="true"`. | Samsung settings uses icon from service; without directBootAware, system warns users on reboot. | Add `android:icon="@mipmap/ic_launcher"` and `android:directBootAware="true"`. |
| `OnboardingActivity.kt` | Static, one-way clicks without checking `imm.enabledInputMethodList` or `Settings.Secure.DEFAULT_INPUT_METHOD`. | When user returns from Settings, the app displays no feedback on whether enablement succeeded, and blindly calls `showInputMethodPicker()`. | Implement reactive state machine (`State A: Disabled`, `State B: Enabled but not selected`, `State C: Active`) with live validation in `onResume()`. |
| `KeyboardSettings.kt` | `runBlocking(Dispatchers.IO) { loadInitialValues() }` on the Main thread. | Can induce UI thread jank, ANR, or lockup when `InputMethodService` is bound by the system. | Load cached defaults synchronously in memory; perform DataStore operations non-blockingly via coroutine scope. |
| User Guidance | No explanation of Samsung One UI "Default keyboard" hierarchy or "Restricted Settings". | User flips switch, thinks tapping OK completes setup, but never selects the keyboard as default. | Provide clear, step-by-step Samsung Galaxy guidance inside `OnboardingActivity`. |

---

## 5. Comprehensive Validation Checklist

- [x] Service extends `android.inputmethodservice.InputMethodService`.
- [x] Manifest declares `android:permission="android.permission.BIND_INPUT_METHOD"`.
- [x] Manifest declares `<action android:name="android.view.InputMethod" />`.
- [x] Manifest meta-data `android.view.im` points to `@xml/method`.
- [x] `method.xml` contains `<subtype>` with `android:isAsciiCapable="true"`.
- [x] `method.xml` contains `android:imeSubtypeMode="keyboard"`.
- [x] `method.xml` contains valid `android:languageTag` and `android:imeSubtypeLocale`.
- [x] `OnboardingActivity` dynamically queries `isImeEnabled()` and `isImeSelected()`.
- [x] `OnboardingActivity` updates automatically in `onResume()`.
- [x] Samsung Galaxy step-by-step instructions provided in UI and documentation.
- [x] Standalone, fully signed APK produced with v1, v2, and v3 signature schemes.
- [x] Zero network permissions (`android.permission.INTERNET` absent).

---

## 6. Official References & Documentation Links

1. **Android Developers — Creating an Input Method:**  
   https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method
2. **Android Developers — InputMethodService API Reference:**  
   https://developer.android.com/reference/android/inputmethodservice/InputMethodService
3. **Android Developers — InputMethodManager API Reference:**  
   https://developer.android.com/reference/android/view/inputmethod/InputMethodManager
4. **Android Developers — InputMethodSubtype API Reference:**  
   https://developer.android.com/reference/android/view/inputmethod/InputMethodSubtype
5. **Samsung Support — How to change the keyboard on your Galaxy device:**  
   https://www.samsung.com/support/mobile-devices/how-to-change-the-keyboard-on-your-galaxy-phone-or-tablet/
6. **Android Developers — Restricted Settings in Android 13+:**  
   https://developer.android.com/guide/topics/permissions/restricted-settings
7. **Samsung Knox — Device Policy & Security Architecture:**  
   https://docs.samsungknox.com/
