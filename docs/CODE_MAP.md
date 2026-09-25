# Code map

Use this page to find the right file before changing behavior. For project status, current blockers, and the next steps, see [WORKLOG.md](../WORKLOG.md). The requirements live in [PROJECT_SPEC.md](../PROJECT_SPEC.md).

## App flow

```text
MainActivity (Compose screens and user actions)
        ├── CityPackRepository → citypacks/barcelona.json
        ├── GameEngine → GameProgressRepository → Preferences DataStore
        └── FusedLocationProvider → accurate foreground GPS fix
```

- `app/src/main/java/com/tigstaking/natalia/MainActivity.kt` — Compose UI and wiring for Home, place check-in, quest, Spanish word/TTS, quiz, passport, child reward requests, and the PIN-gated parent approval dialog. Parent access locks when the app goes to the background. Debug-only Developer Mode entry point is here; it persists/removes the test POI, advances game stages, adjusts test progress, resets progress, and shows GPS diagnostics. Screen or user-action behavior starts here.
- `app/src/main/java/com/tigstaking/natalia/security/ParentPinStore.kt` — Device-local parent PIN setup and verification using Android Keystore HMAC plus a short persisted lockout. Inspect this for PIN setup, failed-attempt handling, or device restore/reinstall behavior.
- `app/src/main/java/com/tigstaking/natalia/game/CityPack.kt` — City Pack data classes, JSON parsing, and validation rules. Change this when the content schema or its validation changes.
- `app/src/main/java/com/tigstaking/natalia/game/CityPackRepository.kt` — Loads the packaged Barcelona City Pack from Android assets.
- `app/src/main/assets/citypacks/barcelona.json` — Sagrada Família content: coordinates, radius, copy, quest, quiz, rewards, badge, and avatar pose. Content-only changes belong here.
- `app/src/main/java/com/tigstaking/natalia/game/GameEngine.kt` — Player-facing game rules and ordered stage flow: location check-in, discovery, quest, word, quiz, and badge. Change this when a player action should be allowed, rejected, or rewarded differently.
- `app/src/main/java/com/tigstaking/natalia/game/GameProgress.kt` — Pure progress models and calculations: XP, stars, reward ledger, levels, pending parent reward requests, per-POI completion reset with payout idempotency retained, and GPS accuracy/geofence classification. Start here for arithmetic or proximity bugs.
- `app/src/main/java/com/tigstaking/natalia/game/GameProgressRepository.kt` — Preferences DataStore persistence, event idempotency, reward request and approval state, ledger serialization, reward redemption, full reset, per-POI reset, and the device-local test POI configuration. Start here for lost or duplicated progress.
- `app/src/main/java/com/tigstaking/natalia/game/location/LocationProvider.kt` — Android permission and location-services checks plus fresh Fused Location Provider reading. Start here for permission, GPS availability, cancellation, or fix-accuracy issues.
- `app/src/main/java/com/tigstaking/natalia/game/speech/SpanishSpeechController.kt` — Android TextToSpeech lifecycle, `es-ES` availability, playback, and unavailable handling. Start here for voice/language/audio problems.

## Tests

- `app/src/test/java/com/tigstaking/natalia/game/GameRulesTest.kt` — XP, stars, levels, quiz selection, proximity/accuracy, and reward request/reservation rules.
- `app/src/test/java/com/tigstaking/natalia/game/CityPackTest.kt` — City Pack JSON parse and schema/content validation.
- `app/src/test/java/com/tigstaking/natalia/game/GameEngineTest.kt` — Ordered Sagrada flow, locked stages, and one-time payouts.
- `app/src/test/java/com/tigstaking/natalia/game/GameProgressRepositoryTest.kt` — Persistence after repository recreation, reward approval, ledger, redemption, and reset.
- `app/src/androidTest/java/com/tigstaking/natalia/HomeSmokeTest.kt` — Launches the app on an Android emulator, opens the Sagrada Família screen, and verifies the GPS check-in control.

Add or update the test next to the behavior being changed. GitHub Actions runs unit tests, compiles the instrumentation test sources, and builds the debug APK. It does not boot the emulator in CI yet.

## Build and CI

- `app/build.gradle.kts` — Android app SDK levels, JVM target, managed emulator, and app/test dependencies.
- `build.gradle.kts` — Android Gradle Plugin and Kotlin plugin versions.
- `gradle/wrapper/gradle-wrapper.properties` — Gradle distribution used on every computer and in CI.
- `.github/workflows/android.yml` — JDK/Android SDK setup, unit tests, instrumentation test compilation, debug APK build, and APK artifact upload.

## Where to look for common problems

| Problem | First files to inspect |
| --- | --- |
| A screen does not open or a button does nothing | `MainActivity.kt`, then the related `GameEngine.kt` method |
| XP/stars are wrong or paid twice | `GameProgress.kt`, `GameProgressRepository.kt`, `GameEngineTest.kt` |
| Progress disappears after restart | `GameProgressRepository.kt`, `GameProgressRepositoryTest.kt` |
| A POI or quiz is wrong | `barcelona.json`, `CityPack.kt`, `CityPackTest.kt` |
| Place unlocks from too far away or GPS is inconclusive | `GameProgress.kt`, `GameEngine.kt`, `LocationProvider.kt`, `GameRulesTest.kt` |
| GPS permission, provider status, or missing fresh fix | `LocationProvider.kt`, GPS diagnostics in `MainActivity.kt`, Android permission settings |
| Gradle fails before compiling app code | `gradle-wrapper.properties`, root `build.gradle.kts`, `app/build.gradle.kts`, then the Actions log |
| App launch, navigation, or Compose semantics fail on emulator | `MainActivity.kt`, `HomeSmokeTest.kt`, then the Actions instrumentation logs |
| Parent reward approval or star reservation is wrong | `GameProgress.kt`, `GameProgressRepository.kt`, `GameProgressRepositoryTest.kt`, `MainActivity.kt` |
| Per-POI reset removes unrelated state or allows repeated payouts | `GameProgress.kt`, `GameProgressRepository.kt`, `GameEngine.kt`, `MainActivity.kt` |
| Parent PIN setup, verification, or lockout is wrong | `ParentPinStore.kt`, then the parent dialog in `MainActivity.kt` |

When behavior crosses layers, update the relevant tests and this map if file ownership changes. Do not put secrets or computer-specific paths in this document.
