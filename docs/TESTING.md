# Testing

Run the local game rule tests and Android debug build from the repository root:

- macOS/Linux: `./gradlew testDebugUnitTest assembleDebug`
- Windows: `gradlew.bat testDebugUnitTest assembleDebug`

The first unit tests cover one-time reward accounting, star redemption, level thresholds, and proximity calculations. City Pack parsing, persistence across process death, and device location flows still need dedicated tests as those features are built.

Use Android Studio's bundled JDK (JDK 17 or newer) and install Android SDK Platform 35 before building. See [Local development](LOCAL_DEVELOPMENT.md).
