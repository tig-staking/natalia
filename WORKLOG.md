# Work log and handoff

Updated: 2026-09-25

This file is the cross-computer handoff note. GitHub `main` is the source of truth. Read this file and `PROJECT_SPEC.md` before continuing work on another machine.

## Goal and working constraints

Build the MVP game engine for **Natalia na tropie** around one City Pack entry, Sagrada Família. The target is a working Android app that can be cloned, built, installed, played offline, tested on a phone, and resumed without losing progress.

Work on game behavior and reliability first. Leave visual polish and final character art until the functional flow is complete. Do not hardcode machine-specific paths or commit local secrets. Keep Barcelona content data-driven; do not add a large city catalogue yet.

## Current implementation

- Kotlin Android app with Jetpack Compose and Material 3.
- Barcelona City Pack loaded from `app/src/main/assets/citypacks/barcelona.json`; one Sagrada Família entry.
- Game rules cover discovery, quest, Spanish word, quiz, XP, stars, idempotent reward ledger, levels, badge markers, reward redemption, and reset.
- Preferences DataStore persists progress locally.
- Foreground one-shot Fused Location Provider checks coarse/fine permission, enabled location providers, and fresh location. No background location or route history.
- Check-in considers reported GPS accuracy. It confirms only when the whole accuracy circle fits inside the geofence; an ambiguous fix asks for another reading.
- Main Compose flow is now wired for Home → Place/GPS → Quest → Spanish word → Quiz → Passport. In debug builds, a Developer Mode shortcut can simulate discovery for Sagrada.
- GitHub Actions runs unit tests and builds a debug APK. Successful runs upload `natalia-debug-apk` for 14 days.

## Verification state

- Last verified green run before the latest screen-flow change: GitHub Actions run for commit `c9f661103547f3940150ea9413875c2525bc524f`; `testDebugUnitTest` and `assembleDebug` both completed successfully.
- CityPack parser tests now use a real JVM `org.json` implementation.
- DataStore persistence test verifies rewards, ledger, idempotent redemption, and reset across repository recreation.
- A newer CI run is checking commit `dfd48c83c7bf679149173141287c6a46275d964c` (playable Sagrada screen flow). Check its result before making further UI assumptions.
- CI builds an APK, but no emulator/instrumentation run has yet verified install, navigation, or runtime behavior.

## Known gaps to address

1. Check the latest CI result and fix all compile/test issues.
2. Add engine-level stage guards so quests, Spanish word, quiz, and badge cannot be completed out of order or before discovery. Add regression tests.
3. Finish a real Developer Mode: simulate/override location, unlock POI, complete stages, edit stars, force level, redeem reward, reset POI/all, create test POI at current location, and show GPS diagnostics.
4. Add parent approval and PIN flow. Never store a plain-text PIN. A reward request must wait for approval; successful redemption spends stars only.
5. Add onboarding, actual map/POI overview, location diagnostics, and recovery paths for permission denied, approximate location, disabled GPS, stale/unavailable fixes.
6. Add Spanish TextToSpeech using `es-ES`, handling unavailable TTS without crashing.
7. Add required celebration/avatar/level-up/badge animations after the functional game flow works.
8. Verify app install and full flow on an emulator; then ask the owner to field-test GPS and device behavior on a physical phone.
9. Keep documentation aligned with the real build and test commands. Continue to keep only Sagrada Família in the City Pack until the engine passes the MVP flow.

## Build and test

GitHub Actions command:

```sh
./gradlew --no-daemon testDebugUnitTest assembleDebug
```

For local setup on another computer, follow [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Use JDK 17 and Android SDK platform 35. Current Android build toolchain: Gradle 8.13, AGP 8.13.2, Kotlin 2.3.20.

## Progress estimate

Roughly 12% of the full MVP scope. Core data/progress/location rules exist and a first playable screen flow is being wired, but parent/developer tools, map, onboarding, rewards approval, audio, animations, and emulator/device verification remain. Re-estimate after each major verified milestone.
