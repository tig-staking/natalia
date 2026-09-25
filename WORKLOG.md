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
- Check-in considers reported GPS accuracy. It confirms only when the whole accuracy circle fits inside the geofence; an ambiguous fix asks for another reading.
- GitHub Actions runs unit tests and builds a debug APK. Successful runs upload `natalia-debug-apk` for 14 days.
- [`docs/CODE_MAP.md`](docs/CODE_MAP.md) describes file ownership and where to look for common problems.

## Verification state

- Green GitHub Actions run for code commit `66095d1f95a0114ddaa588ea8cb17fb346639c24`: unit tests and debug APK build passed.
- The run uploaded debug APK artifact `natalia-debug-apk` (artifact id `10860897629`), available until 2026-10-09.
- Tests cover core rules, GPS accuracy boundaries, City Pack JSON parsing/validation, ordered game stages, idempotent payouts, DataStore persistence after repository recreation, reward redemption, ledger, and reset.
- GitHub Actions has not yet installed or exercised the app on an emulator. No on-device GPS test has been run.
- A docs-only commit is currently running CI; check the latest workflow status before the next code change.

## Known gaps to address

1. Add an emulator smoke/full-flow test and inspect app runtime behavior. Then ask the owner to field-test GPS and device behavior on a physical phone.
2. Finish a real Developer Mode: simulate/override location, unlock POI, complete stages, edit stars, force level, redeem reward, reset POI/all, create test POI at current location, and show GPS diagnostics. Current debug shortcut only simulates Sagrada discovery.
3. Add parent approval and PIN flow. Never store a plain-text PIN. A reward request must wait for approval; successful redemption spends stars only.
4. Add onboarding, actual map/POI overview, location diagnostics, and recovery paths for permission denied, approximate location, disabled GPS, stale/unavailable fixes.
5. Add Spanish TextToSpeech using `es-ES`, handling unavailable TTS without crashing.
6. Add required celebration/avatar/level-up/badge animations after the functional game flow works.
7. Keep documentation aligned with the real build and test commands. Continue to keep only Sagrada Família in the City Pack until the engine passes the MVP flow.

## Build and test

GitHub Actions command:

```sh
./gradlew --no-daemon testDebugUnitTest assembleDebug
```

For local setup on another computer, follow [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Use JDK 17 and Android SDK platform 35. Current Android build toolchain: Gradle 8.13, AGP 8.13.2, Kotlin 2.3.20.

## Progress estimate

Roughly 12% of the full MVP scope. Core data/progress/location rules and the first screen flow are in place, but emulator validation, full Developer/Parent modes, map, onboarding, rewards approval, audio, and animations remain. Re-estimate after each major verified milestone.
