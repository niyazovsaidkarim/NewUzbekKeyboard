# Android Device Test Checklist — Özbek Keyboard

**Package:** `uz.ozbekkeyboard`  
**Version:** 1.0.0 (Debug / Release)  
**Target Environment:** Physical Android devices and official Android emulators across Uzbekistan  

---

## 1. Pre-Test Installation & Setup

1. **Build / Obtain APK:**
   - Command: `./gradlew assembleDebug`
   - APK Location: `app/build/outputs/apk/debug/app-debug.apk`
2. **Install via ADB or Direct Sideload:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. **Launch Onboarding Application:**
   - Tap app icon `Özbek Keyboard` on device home screen.
   - **Step 1:** Tap «1. Sozlamalarda yoqiş» -> Enable the keyboard in system IME list.
     *(If testing on Android 13/14/15 sideload, verify the Restricted Settings prompt guidance).*
   - **Step 2:** Tap «2. Klaviaturani tanlaş» -> Select `Özbek Keyboard` in the system input picker.
   - **Step 3:** Verify the interactive test typing area activates with green checkmark badge.

---

## 2. Core Functional Test Cases

| Test ID | Area / Feature | Test Procedure | Expected Result | Pass / Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | **Alphabet Layout** | Open any text input (e.g. Telegram). Inspect QWERTY layout. | All 26 Latin keys, comma, period, space, enter, shift, backspace, and symbol switch are visible and properly aligned. | [ ] |
| **TC-02** | **Mode Switch: Alphabet to Symbols** | Tap `!#1` key. | Mode transitions cleanly to `SYMBOLS_1`. No blank space, no disappearing keys, toolbar remains anchored. | [ ] |
| **TC-03** | **Mode Switch: Page 1 to Page 2** | In symbol mode, tap `1/2` or `2/2` switch key. | Secondary symbols (math, brackets, currency symbols including `so'm` / `UZS` / `$`, `%`, etc.) display correctly. | [ ] |
| **TC-04** | **Mode Switch: Return to Alphabet** | Tap `ABC` from either symbol page. | Returns smoothly to the alphabet layout. | [ ] |
| **TC-05** | **Number Row Toggle** | Open More tools (⋮) in toolbar -> Toggle number row on/off. | 5th row appears/disappears smoothly. All keys rescale dynamically without clipping. | [ ] |
| **TC-06** | **Long-Press Popups (Uzbek Glyphs)** | Long-press `o`, `g`, `s`, `c` keys. | Popup overlay renders smoothly above key: `o` -> `ö`, `g` -> `ğ`, `s` -> `ş`, `c` -> `ç`. Sliding finger selects glyph; lifting inserts it. | [ ] |
| **TC-07** | **Shift & Caps Lock** | Tap Shift once; double-tap Shift. | Single tap uppercases keys for 1 character. Double tap enables persistent Caps Lock (filled arrow icon). | [ ] |
| **TC-08** | **Spacebar Trackpad** | Press and hold spacebar, then slide finger left and right. | Keyboard initiates cursor navigation; text cursor glides accurately across characters without lag. | [ ] |
| **TC-09** | **Continuous Backspace** | Type a sentence; press and hold Backspace. | Single character deleted initially, followed after 400ms by continuous smooth character deletion at 50ms intervals. | [ ] |
| **TC-10** | **Audio & Haptic Feedback** | Enable sound and vibration in Settings -> Type keys. | Audible click matches system keypress sound; gentle haptic vibration confirms keypress. | [ ] |
| **TC-11** | **Action Enter Key** | Focus Search field in Chrome; focus Send field in Telegram; focus Multiline note. | Enter key icon dynamically adapts (`Search`, `Send`, `Return`, `Next`). Tapping dispatches appropriate editor action. | [ ] |
| **TC-12** | **Numeric Auto-Switch** | Tap on a phone number or numeric PIN field (e.g., Payme, Click Up). | Keyboard automatically opens in `SYMBOLS_1` mode with numbers immediately accessible. | [ ] |

---

## 3. Real-World Application Matrix (Uzbekistan Ecosystem)

Test typing across these popular applications to verify IME compatibility:

| Application Category | Specific App Name | What to Verify | Status |
| :--- | :--- | :--- | :--- |
| **Messenger** | Telegram Messenger | Typing messages, emoji panel transition, sticker search, rapid typing. | [ ] |
| **Fintech / Banking** | Payme / Click Up / Anorbank | Numeric PIN inputs, card number entry, transfer notes, fallback deletion. | [ ] |
| **E-Commerce** | Uzum Market | Search bar input, address entry, promo code entry with mixed case and digits. | [ ] |
| **Browser** | Google Chrome / Yandex | URL bar address entry, web form autocomplete, search queries. | [ ] |
| **Social Media** | Instagram / TikTok | Comment section, Direct Message input, caption writing. | [ ] |
| **System** | Device Lock Screen / Settings | Direct Boot password/PIN unlock, searching in system settings. | [ ] |

---

## 4. Device Lifecycle, Orientation & Edge Cases

| Test ID | Scenario | Procedure | Expected Result | Pass / Fail |
| :--- | :--- | :--- | :--- | :--- |
| **EC-01** | **Screen Rotation (Landscape)** | Rotate device to landscape while keyboard is visible in Telegram. | Keyboard resizes to compact 38dp row height. Does NOT take over the screen in fullscreen extract mode. Underlying app remains visible. | [ ] |
| **EC-02** | **Screen Rotation (Portrait Return)** | Rotate back from landscape to portrait. | Keyboard smoothly returns to 54dp base height. Zero blank view or geometry calculation error. | [ ] |
| **EC-03** | **Gesture Navigation Pill** | Test on device with gesture navigation enabled (Android 10+). | Keyboard bottom edge is comfortably padded above the white gesture pill. No accidental home triggers. | [ ] |
| **EC-04** | **3-Button Navigation Bar** | Switch device to 3-button navigation (Back, Home, Recents). | Spacebar and bottom row keys do not overlap with system navigation buttons. | [ ] |
| **EC-05** | **App Switching & Dismissal** | Open keyboard -> Press Home or swipe up -> Re-open app. | Keyboard cleanly dismisses; touch state resets without dangling repeat timers or popup overlays. | [ ] |
| **EC-06** | **Subpanel Back Navigation** | Open Emoji panel or Clipboard panel -> Press system Back button. | Subpanel closes and returns to main keyboard view instead of immediately dismissing the entire IME. | [ ] |
| **EC-07** | **Password Field Isolation** | Tap password input field. | Suggestions in toolbar are completely disabled; clipboard sync is halted; no sensitive text retained. | [ ] |
| **EC-08** | **TalkBack Accessibility** | Enable TalkBack in Android Accessibility -> Touch keys. | Spoken feedback correctly pronounces modern Uzbek labels (*«Öçiriş», «Kiritiş», «Probel», «Belgilar», «Tilni almaştiriş»*). | [ ] |

---

## 5. Test Execution Sign-off

- **Tester Name:** _____________________
- **Device Model:** _____________________
- **Android OS Version:** _______________
- **Build Number / Patch:** _____________
- **Conclusion:** [ ] PASSED  /  [ ] DEFECTS IDENTIFIED (Attach logs)
