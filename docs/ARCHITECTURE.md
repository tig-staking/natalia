# Architecture

The Android app lives in `app/`. The first increment establishes a Compose application shell and keeps machine-specific Android SDK configuration in the ignored `local.properties` file. Game rules and City Pack loading will be added as separate layers in subsequent increments, so screens do not own game state or hardcode city content.

## Cross-machine source of truth

GitHub `main` is the shared source of truth. Commit source and documentation; keep SDK paths, API keys, signing keys, and generated outputs local. Use the Android Studio bundled JDK to reduce differences between machines.
