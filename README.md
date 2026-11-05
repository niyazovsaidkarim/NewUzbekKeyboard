# Özbek Keyboard for Android 🇺🇿

[![Build Status](https://github.com/niyazovsaidkarim/NewUzbekKeyboard/actions/workflows/build.yml/badge.svg)](https://github.com/niyazovsaidkarim/NewUzbekKeyboard/actions/workflows/build.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin%202.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20(Zero%20Network)-success.svg)](SECURITY.md)

[**English**](#english) | [**O‘zbekcha**](#ozbekcha)

---

<a name="english"></a>
## English

### Overview

**Özbek Keyboard** (`uz.ozbekkeyboard`) is a fast, native, privacy-conscious Android Input Method Editor (IME) engineered specifically for the Uzbek language with modern Samsung One UI visual ergonomics.

Unlike web-based keyboards or simple text simulators, Özbek Keyboard is a true system-level IME built directly on Android's official `InputMethodService` and `InputConnection` frameworks. It can be set as your system default keyboard and used across Telegram, WhatsApp, Chrome, Instagram, SMS, and all other Android applications.

The keyboard functions **100% offline**: it declares **no internet permission**, contains zero telemetry SDKs, logs no keystrokes, and respects password field isolation.

---

### Key Features

1. **Dual Orthography Support (Official Standard & Proposed Reform):**
   - **Official Current Standard (1995 Law):** First-class, default support for the legally established Uzbek Latin alphabet, including the modifier letter apostrophe / tutuq belgisi (`ʼ` - U+02BC) and standard digraphs (`oʻ`, `gʻ`, `sh`, `ch`).
   - **Proposed Reform Characters (Draft Legislation):** Long-press popups provide forward-compatible, convenient access to the proposed diacritic characters (`o → ö`, `g → ğ`, `s → ş`, `c → ç`, and uppercase equivalents) from the legislative reform draft bills, without disrupting standard QWERTY typing.
2. **Space Trackpad 2.0:**
   - Long-press or horizontal drag on the Space key activates cursor control with haptic feedback.
   - **Unicode Grapheme Awareness:** Navigates smoothly across multiline text without splitting compound emojis (e.g. 🇺🇿, 👨‍👩‍👧‍👦) or surrogate pairs.
   - **Zero Accidental Spaces:** An intelligent gesture barrier guarantees no space characters are inserted when using or releasing the trackpad.
3. **Samsung One UI Design:**
   - Authentic Samsung-style Backspace with rounded key geometry and depth shadows.
   - Four refined themes: **Black & Yellow (High Contrast)**, **Minimal Light**, **Minimal Dark**, and **Follow System**.
   - Optional dedicated number row (`1`–`0`).
4. **Complete 2-Page Symbol Sets:**
   - **Page 1:** `+ × ÷ = / _ < > [ ] ! @ # £ % ^ & * ( ) [1/2] - ' " : ; , ? [Del]`
   - **Page 2:** `` ` ~ \ | { } € $ ₽ ₺ ° • ○ ● □ ■ ♡ ◇ [2/2] ☆ · « » ¡ ¿ § [Del] ``
   - Bottom row switcher toggles cleanly between `!#1` and `ABC` without layout disappearance.
5. **Privacy & Security by Design:**
   - **Zero Internet Permission:** `android.permission.INTERNET` is absent from `AndroidManifest.xml`.
   - **Password Protection:** Suggestions, autocorrect, and clipboard sync are automatically suppressed in sensitive fields.

---

### Installation & Setup

#### Option A: Install Pre-Built APK
1. Download the latest `OzbekKeyboard-v2.1.apk` from the [Releases](https://github.com/niyazovsaidkarim/NewUzbekKeyboard/releases) page.
2. Install the APK on your device:
   ```bash
   adb install -r OzbekKeyboard-v2.1.apk
   ```
3. Open the **Özbek Keyboard** app and follow the 3-step setup guide:
   - **Step 1:** Enable Özbek Keyboard in system settings.
   - **Step 2:** Select Özbek Keyboard as your default input method.
   - **Step 3:** Test typing in the test area.

> [!NOTE]
> On Android 13, 14, and 15, sideloaded applications may display a "Restricted Setting" prompt. To enable: Go to **Settings > Apps > Özbek Keyboard > tap three dots (top right) > Allow restricted settings**.

---

### Building from Source

#### Prerequisites
- JDK 17 or JDK 21
- Android SDK Platform 35 (Android 15)
- Android SDK Build-Tools 35.0.0
- Linux, macOS, or Windows

#### Build Instructions
```bash
# Clone the repository
git clone https://github.com/niyazovsaidkarim/NewUzbekKeyboard.git
cd NewUzbekKeyboard

# Run automated unit test suite (36+ tests)
./gradlew testDebugUnitTest

# Assemble installable debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

<a name="ozbekcha"></a>
## O‘zbekcha

### Ilova haqida

**Özbek Keyboard** (`uz.ozbekkeyboard`) — O‘zbek tili uchun maxsus ishlab chiqilgan, Samsung One UI dizayn tizimiga asoslangan, tezkor va 100% oflayn Android tizim klaviaturasi (IME).

Bu ilova Android tizimining rasmiy `InputMethodService` va `InputConnection` arxitekturasida yaratilgan to‘laqonli tizim klaviaturasidir. Uni Telegram, WhatsApp, Chrome, Instagram, SMS va barcha dasturlarda asosiy klaviatura sifatida ishlatish mumkin.

Klaviatura mutlaqo **oflayn** ishlaydi: **internetga ulanish ruxsati (INTERNET) mavjud emas**, hech qanday reklama, keylogger yoki tahlil kodlari kiritilmagan.

---

### Asosiy Imkoniyatlar

1. **Rasmiy Alifbo va Taklif Qilingan Islohot Belgilari:**
   - **Amaldagi rasmiy alifbo (1995-yilgi qonun):** O‘zbekiston Respublikasining amaldagi qonuniy lotin alifbosi, shu jumladan rasmiy tutuq belgisi (`ʼ` - U+02BC) va an’anaviy harf birikmalari (`oʻ`, `gʻ`, `sh`, `ch`) to‘liq va asosiy tarzda qo‘llab-quvvatlanadi.
   - **Taklif etilgan islohot harflari (qonun loyihasi):** Yangi muhokama qilinayotgan alifbo loyihasidagi maxsus belgilar (`o → ö`, `g → ğ`, `s → ş`, `c → ç` va ularning bosh harflari) tugmalarni uzoq bosish (long-press) orqali kiritish uchun qulay taqdim etilgan. Bu rasmiy alifboda yozuvchilarga ham, yangi loyihani sinovdan o‘tkazuvchilarga ham teng qulaylik yaratadi.
2. **Space (Bo‘sh joy) Trekpadi 2.0:**
   - Bo‘sh joy tugmasini uzoq bosish yoki gorizontal siljitish orqali kursor boshqariladi.
   - Matn ichidagi murakkab emojilar (masalan, 🇺🇿) va harf guruhlari buzilmasdan, yaxlit harakatlanadi.
   - Trekpaddan foydalanish vaqtida xatolik bilan ortiqcha bo‘sh joy belgisi yozilib qolmaydi.
3. **Samsung One UI Uslubidagi Dizayn:**
   - Samsung uslubidagi maxsus Backspace tugmasi.
   - To‘rtta zamonaviy mavzu: **Qora va sariq (Yuqori kontrast)**, **Minimal oq**, **Minimal qora** va **Tizimga moslashuvchan**.
   - Ixtiyoriy raqamlar qatori (`1`–`0`).
4. **2 Sahifali To‘liq Belgilar Paneli:**
   - Barcha matematik amallar, valyuta va tinish belgilari.
   - `!#1` va `ABC` o‘rtasida almashishda tugmalar yo‘qolib qolmaydi.

---

### O‘rnatish va Faollashtirish

1. Tayyor `OzbekKeyboard-v2.1.apk` faylini yuklab oling va o‘rnating.
2. **Özbek Keyboard** ilovasini oching:
   - **1-qadam:** Tizim sozlamalaridan klaviaturani yoqing.
   - **2-qadam:** Standart kiritish usuli sifatida Özbek Keyboardni tanlang.
   - **3-qadam:** Sinov maydonida yozib ko‘ring.

---

## Technical Specifications

- **Package ID:** `uz.ozbekkeyboard`
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 15 (API 35)
- **Programming Language:** Kotlin 2.0
- **UI Architecture:** Custom Canvas Engine (Zero deprecated `KeyboardView`)
- **Settings Architecture:** Jetpack DataStore Preferences

---

## Contributing & Security

- Contributions are welcome! Read our [Contributing Guidelines](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md).
- To report security concerns, consult our [Security Policy](SECURITY.md).

---

## License

Copyright 2026 Saidkarim Niyazov and Özbek Keyboard Contributors.

Licensed under the [Apache License, Version 2.0](LICENSE).
