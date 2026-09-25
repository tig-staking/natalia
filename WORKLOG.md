# Work log and handoff

Updated: 2026-09-25

This file is the cross-computer handoff note. GitHub `main` is the source of truth. Read this file, [the code map](docs/CODE_MAP.md), and `PROJECT_SPEC.md` before continuing on another computer.

## Goal and working constraints

Build the MVP game engine for **Natalia na tropie** around one City Pack entry, Sagrada Família. The target is a working Android app that can be cloned, built, installed, played offline, tested on a phone, and resumed without losing progress.

Work on game behavior and reliability first. Leave visual polish and final character art until the functional flow is complete. Do not hardcode machine-specific paths or commit local secrets. Keep Barcelona content data-driven; do not add a large city catalogue yet.

## Current implementation

- Kotlin Android app with Jetpack Compose and Material 3.
- Barcelona City Pack loaded from `app/src/main/assets/citypacks/barcelona.json`; one Sagrada Família entry.
- Compose flow: Home → Place/GPS → Quest → Spanish word → Quiz → Passport. Debug builds expose a shortcut to simulate discovery.
- GameEngine requires stages in order: discover, quest, Spanish word, quiz, then badge.
- Progress rules include XP, stars, an idempotent reward ledger, levels, badge markers, reward redemption, and reset.
- Preferences DataStore persists progress locally.
- Reward redemption supports a persistent request/approval flow: requested stars are reserved without being spent, approval spends them once, cancellation releases the reservation, and duplicate actions are idempotent.
- Home lets the player request a 10-star reward; a parent can review, approve, or reject pending requests in a PIN-gated dialog.
- Parent PIN is configured on first use, checked as a keyed HMAC using a non-exportable Android Keystore key, and protected by a 30-second lockout after five failed attempts. The PIN itself is not persisted. The PIN credential is device-local; a new device or reinstall requires setup again.
- Debug-only Developer Mode can now advance exactly one game stage at a time and add 10 test stars. It uses the same ordered GameEngine methods as the player flow.
- Foreground one-shot Fused Location Provider checks coarse/fine permission, enabled location providers, and a fresh location. No background location or route history.
- A Compose instrumentation smoke test launches the app and opens the Sagrada Família check-in screen.
- GitHub Actions runs unit tests, compiles instrumentation test sources, builds a debug APK, and uploads that APK. Successful APK artifacts are retained for 14 days.
- [`docs/CODE_MAP.md`](docs/CODE_MAP.md) describes file ownership and where to look for common problems.

## Verification state

- Green GitHub Actions run for code commit `c3c8e73d868721675463903250f2acd7c4e6f92b`: unit tests, instrumentation test compilation, debug APK build, and artifact upload passed. Documentation-only commit `4897bd830a7a7759b5143b442d0e0989733bc516` also passed.
- Parent PIN and Compose approval UI commit `2ca9bd0702be2504cd7dc2ab7576204b519c8628` passed GitHub Actions run [36155555226](https://github.com/tig-staking/natalia/actions/runs/36155555226): unit tests, instrumentation source compilation, debug APK build, and artifact upload. APK artifact `10873641102` is available until 2026-10-09.
- Developer Mode stage controls were committed after that run; current engine UI revision still needs CI and Android device/emulator verification.
- The run uploaded artifact `natalia-debug-apk` (id `10864448021`), available until 2026-10-09.
- Tests cover reward request reservation, no spending before approval, idempotent approval, cancellation, ledger entries, and persistence of pending requests after repository recreation.
- The emulator smoke test has compiled but has not run on a device. GitHub-hosted runs could not boot the configured emulator; CI avoids emulator startup while keeping the instrumentation compile check.
- No on-device GPS test has been run.

## Known gaps to address

1. Run the Compose smoke test on an Android Studio emulator or connected Android device; then field-test GPS behavior on a physical phone.
2. Verify the new parent PIN and approval flow on a device. Add focused coverage for PIN lockout and approval UI. The credential is intentionally per-device; parent PIN setup must be repeated after installing on another device.
3. Finish Developer Mode: simulate/override location, force level, redeem reward, reset POI/all, create a test POI at current location, and show GPS diagnostics. Debug builds can currently simulate discovery, advance one ordered stage at a time, and add test stars.
4. Add onboarding, actual map/POI overview, location diagnostics, and recovery paths for permission denied, approximate location, disabled GPS, stale/unavailable fixes.
5. Add Spanish TextToSpeech using `es-ES`, handling unavailable TTS without crashing.
6. Add required celebration/avatar/level-up/badge animations after the functional game flow works.
7. Keep documentation aligned with the real build and test commands. Continue to keep only Sagrada Família in the City Pack until the engine passes the MVP flow.

## Build and test

GitHub Actions command:

```sh
./gradlew --no-daemon testDebugUnitTest compileDebugAndroidTestSources assembleDebug
```

To run the smoke test on a connected emulator/device:

```sh
./gradlew connectedDebugAndroidTest
```

For local setup on another computer, follow [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Use JDK 17 and Android SDK platform 35. Current Android build toolchain: Gradle 8.13, AGP 8.13.2, Kotlin 2.3.20.

## Progress estimate

Roughly 17% of the full MVP scope. The ordered engine, persistence, GPS rules, parent-gated reward accounting, device-local PIN-gated approval screen, and basic ordered Developer Mode controls are implemented. Parent PIN/UI passed CI; the newest Developer Mode change is awaiting CI/device verification. Runtime emulator validation, full Developer Mode, map, onboarding, audio, and animations remain. Re-estimate after each major verified milestone.
