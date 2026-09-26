# Testing

Run the local game rule tests and Android debug build from the repository root:

- macOS/Linux: `./gradlew testDebugUnitTest assembleDebug`
- Windows: `gradlew.bat testDebugUnitTest assembleDebug`

The Compose instrumentation smoke test launches `MainActivity`, opens the Sagrada Família place screen, and checks that the GPS check-in action is shown. Run it on a connected device or running emulator with `./gradlew connectedDebugAndroidTest`. A Gradle Managed Device named `pixel2api35` is also configured for local use with `./gradlew pixel2api35DebugAndroidTest`.

GitHub Actions runs unit tests, compiles Android instrumentation test sources, builds a debug APK, and uploads that APK. Hosted runners tried so far could not boot the configured emulator, so CI does not claim the smoke test executed. Run the instrumentation test from Android Studio or on a local emulator/device.

The unit tests cover reward accounting, star redemption, level thresholds, GPS proximity and accuracy, City Pack parsing, ordered game stages, and persisted progress. The emulator smoke test does not verify a real GPS fix; physical-device location behavior still needs a field test.

Instrumentation coverage also checks the Android Keystore-backed parent PIN: it is not stored as plain text, five failed attempts trigger lockout, and the valid PIN works again after the lockout period.

The Compose reward approval test provisions a 100-star balance, configures the parent PIN in the UI, requests the ice-cream reward, approves it as a parent, and verifies that the ledger records a single 100-star debit.

The full-stage Compose test goes from onboarding through debug discovery, quest, Spanish word, a wrong and a correct quiz answer, and passport. It recreates the Activity and verifies that the earned badge and progress are still present. The managed Pixel 2 API 35 suite currently passes 4/4; physical GPS and a complete process-death check still need a phone.

For local emulator testing, install Android SDK Platform 35 and use Android Studio's bundled JDK (JDK 17 or newer). See [Local development](LOCAL_DEVELOPMENT.md).
