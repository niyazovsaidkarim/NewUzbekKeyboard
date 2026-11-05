# Contributing to Özbek Keyboard

Thank you for your interest in contributing to **Özbek Keyboard**! We welcome contributions from developers, linguists, UI/UX designers, and translators to make this the best keyboard for the Uzbek language.

---

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## Architectural Principles & Strict Invariants

Before submitting code, please ensure your changes adhere to our core project invariants:

1. **Strict Offline Privacy:**
   - `android.permission.INTERNET` must NEVER be added to `AndroidManifest.xml`.
   - Core typing, suggestions, emoticons, and clipboard management must function 100% offline.
   - Keystrokes, personal dictionary entries, or user input must never be logged or transmitted.
2. **Linguistic Fidelity:**
   - Both the official 1995 Uzbek Latin orthography (`oʻ`, `gʻ`, `sh`, `ch`, `ʼ`) and the reformed Latin orthography (`ö`, `ğ`, `ş`, `ç`) must be fully supported.
   - Long-press popups must never produce duplicate characters upon release.
3. **Robust Rendering:**
   - Every layout mode transition (`ALPHABET` ↔ `SYMBOLS_1` ↔ `SYMBOLS_2`) must calculate key bounds immediately.
   - Keys must never render with empty geometry (0×0 pixels).
4. **Pure Unit Tests:**
   - Business logic, layout calculations, and suggestion engines must remain testable on standard JVMs without unmocked Android framework exceptions.

---

## Development Setup

### Prerequisites
- JDK 17 (or JDK 21)
- Android SDK Platform 35 (Android 15)
- Android SDK Build-Tools 35.0.0
- Gradle 8.13+ (included via Gradle Wrapper)

### Cloning and Building
```bash
git clone https://github.com/niyazovsaidkarim/NewUzbekKeyboard.git
cd NewUzbekKeyboard

# Build and run all automated unit tests
./gradlew testDebugUnitTest

# Assemble installable debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Submitting Pull Requests

1. **Fork the Repository:** Create your feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Write Unit Tests:** Ensure all new logic is covered with automated tests in `app/src/test/java/`.
3. **Verify Existing Tests:** Run `./gradlew testDebugUnitTest` and ensure all tests pass.
4. **Clean Code:** Adhere to official Kotlin style guidelines. Ensure no debug logs (`Log.d`, `println`) are left in production code.
5. **Commit Messages:** Use clear, conventional commit messages:
   - `feat(layout): add new symbol mappings`
   - `fix(trackpad): improve cursor boundary calculation`
   - `docs(readme): update installation instructions`
6. **Open a Pull Request:** Describe the problem solved and test results.
