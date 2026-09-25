# Testing

Run the local game rule tests and Android debug build from the repository root:

- macOS/Linux: `./gradlew testDebugUnitTest assembleDebug`
- Windows: `gradlew.bat testDebugUnitTest assembleDebug`

The Compose instrumentation smoke test launches `MainActivity`, opens the Sagrada Família place screen, and checks that the GPS check-in action is shown. Run it on a connected device or running emulator with `./gradlew connectedDebugAndroidTest`. A Gradle Managed Device named `pixel2api35` is also configured for local use with `./gradlew pixel2api35DebugAndroidTest`.

GitHub Actions runs unit tests, compiles Android instrumentation test sources, builds a debug APK, and uploads that APK. Hosted runners tried so far could not boot the configured emulator, so CI does not claim the smoke test executed. Run the instrumentation test from Android Studio or on a local emulator/device.

The unit tests cover reward accounting, star redemption, level thresholds, GPS proximity and accuracy, City Pack parsing, ordered game stages, and persisted progress. The emulator smoke test does not verify a real GPS fix; physical-device location behavior still needs a field test.

For local emulator testing, install Android SDK Platform 35 and use Android Studio's bundled JDK (JDK 17 or newer). See [Local development](LOCAL_DEVELOPMENT.md).
