# Security Policy

## Our Security Commitment

Input Method Editors (keyboards) occupy a position of critical trust on mobile devices. Keyboards process sensitive personal communications, credentials, search queries, and payment details.

**Özbek Keyboard is built with a zero-trust, privacy-by-design architecture:**

1. **Zero Internet Access:**
   - The application does not declare the `android.permission.INTERNET` permission in `AndroidManifest.xml`.
   - The Android OS kernel strictly blocks any attempt to initiate network connections or transmit telemetry.
2. **No Keystroke Logging:**
   - Typed characters are routed directly to the target application via Android's `InputConnection`.
   - No keystrokes are recorded in memory buffers, written to logcat, or persisted to device storage.
3. **Sensitive Field Isolation:**
   - Password, PIN, and credit card fields (`TYPE_TEXT_VARIATION_PASSWORD`, `WEB_PASSWORD`, `NUMBER_PASSWORD`) automatically trigger Sensitive Mode:
     - Word suggestions are disabled.
     - Clipboard caching is bypassed.
     - Autocorrect is disabled.
4. **App-Private Storage:**
   - Settings and personal dictionary data are stored exclusively in app-private storage protected by Android sandbox boundaries and Linux UID isolation.

---

## Supported Versions

Only the latest release branch receives active security updates:

| Version | Supported          |
| :---    | :---               |
| 2.x     | :white_check_mark: |
| 1.x     | :x:                |

---

## Reporting a Vulnerability

If you discover a potential security flaw or unintended data retention vulnerability:

1. **Do not open a public GitHub issue.**
2. Send a confidential report to:
   - **Email:** `saidkarimniyazov@gmail.com`
3. Please include:
   - Steps to reproduce the issue.
   - Affected device model and Android OS version.
   - Expected vs observed behavior.

We will review reports within 48 hours and work with you to patch any confirmed issue before public disclosure.
