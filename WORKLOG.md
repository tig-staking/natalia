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
- Foreground one-shot Fused Location Provider checks coarse/fine permission, enabled location providers, and a fresh location. No background location or route history.
- A Compose instrumentation smoke test launches the app and opens the Sagrada Família check-in screen.
- GitHub Actions runs unit tests, compiles the instrumentation test APK, and builds a debug APK. Successful APK artifacts are retained for 14 days.
- [`docs/CODE_MAP.md`](docs/CODE_MAP.md) describes file ownership and where to look for common problems.

## Verification state

- Last successful full unit-test and debug-APK run: commit `8700c722b6a4892c4ba2701b4b8821092682c7ee`.
- Previous verified artifact `natalia-debug-apk` (id `10860897629`) expires 2026-10-09.
- The app, unit tests, and instrumentation test sources compile. The instrumentation test itself has not completed on an emulator.
- GitHub-hosted runs did not boot the emulator: Ubuntu could not start its x86 image with CPU acceleration, the macOS managed image exited at startup, and the direct software-emulator fallback timed out waiting for a device.
- CI is back to a reliable Ubuntu job that runs unit tests, compiles instrumentation tests, builds the debug APK, and uploads it. Run the smoke test on a local emulator/device before treating runtime navigation as verified.
- No on-device GPS test has been run.

## Known gaps to address

1. Run the Compose smoke test on an Android Studio emulator or a connected Android device; then field-test GPS behavior on a physical phone.
2. Finish a real Developer Mode: simulate/override location, unlock POI, complete stages, edit stars, force level, redeem reward, reset POI/all, create test POI at current location, and show GPS diagnostics. Current debug shortcut only simulates Sagrada discovery.
3. Add parent approval and PIN flow. Never store a plain-text PIN. A reward request must wait for approval; successful redemption spends stars only.
4. Add onboarding, actual map/POI overview, location diagnostics, and recovery paths for permission denied, approximate location, disabled GPS, stale/unavailable fixes.
5. Add Spanish TextToSpeech using `es-ES`, handling unavailable TTS without crashing.
6. Add required celebration/avatar/level-up/badge animations after the functional game flow works.
7. Keep documentation aligned with the real build and test commands. Continue to keep only Sagrada Família in the City Pack until the engine passes the MVP flow.

## Build and test

GitHub Actions command:

```sh
./gradlew --no-daemon testDebugUnitTest compileDebugAndroidTest assembleDebug
```

To run the smoke test on a connected emulator/device:

```sh
./gradlew connectedDebugAndroidTest
```

For local setup on another computer, follow [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Use JDK 17 and Android SDK platform 35. Current Android build toolchain: Gradle 8.13, AGP 8.13.2, Kotlin 2.3.20.

## Progress estimate

Roughly 12% of the full MVP scope. Core data/progress/location rules and the first screen flow are in place, but runtime emulator validation, full Developer/Parent modes, map, onboarding, rewards approval, audio, and animations remain. Re-estimate after each major verified milestone.
