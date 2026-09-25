# Testing

Run the local game rule tests and Android debug build from the repository root:

- macOS/Linux: `./gradlew testDebugUnitTest assembleDebug`
- Windows: `gradlew.bat testDebugUnitTest assembleDebug`

The Android smoke test launches `MainActivity` on a managed Pixel 2 emulator (API 35), opens the Sagrada Família place screen, and checks that the GPS check-in action is shown. GitHub Actions runs this test as `pixel2api35DebugAndroidTest` alongside the unit tests and debug APK build.

The unit tests cover reward accounting, star redemption, level thresholds, GPS proximity and accuracy, City Pack parsing, ordered game stages, and persisted progress. The emulator smoke test does not verify a real GPS fix; physical-device location behavior still needs a field test.

For local emulator testing, install Android SDK Platform 35 and create an API 35 Google Play system image compatible with the managed device. Use Android Studio's bundled JDK (JDK 17 or newer). See [Local development](LOCAL_DEVELOPMENT.md).
