# Yasmin AI v1
Chat (Gemini) + Yasmin personality + permission-based tools.

Commands (test): `youtube lofi music`, `open Chrome`

## Setup
1. `cp local.properties.example local.properties` and fill sdk.dir + GEMINI_API_KEY
2. `gradle wrapper` (once), then `./gradlew assembleDebug`
APK: app/build/outputs/apk/debug/app-debug.apk

Termux note: Android SDK's aapt2 often fails on ARM Termux.
If it does, build via GitHub Actions or Android Studio, or set android.aapt2FromMavenOverride to a Termux-compatible aapt2.
