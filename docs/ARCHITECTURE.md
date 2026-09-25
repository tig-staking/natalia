# Architecture

The Android app lives in `app/`. Compose screens are presentation only; game rules live under `app/src/main/java/com/tigstaking/natalia/game/` and city content lives in `app/src/main/assets/citypacks/`.

## Current engine foundation

- `CityPack` parses versioned city/place data, starting with one Barcelona place.
- `GameProgress` applies XP/star events only once and rejects XP spending or negative star balances.
- `GameProgressRepository` stores progress locally with Preferences DataStore and records an append-only reward ledger.
- `GameEngine` awards discovery, quest, Spanish word and quiz rewards from each place's reward plan; wrong quiz answers can be retried without changing progress.
- Quest types are data-driven: observation and say-phrase use a player confirmation, multiple-choice requires the configured correct answer, and parent-check requires an explicit PIN-gated parent confirmation.
- `Proximity` calculates straight-line distance in meters and checks a place radius.
- `FusedLocationProvider` retrieves a fresh, one-shot foreground fix; `GameEngine.checkIn` only records discovery while that fix is inside the place radius.
- `PlayerLevel` maps XP to the four levels in the product spec.

Progress is local to this installation and contains no location history. Discovery, quest, quiz and reward event IDs must be stable so duplicate callbacks cannot pay out twice.

## Cross-machine source of truth

GitHub `main` is the shared source of truth. Commit source and documentation; keep SDK paths, API keys, signing keys, and generated outputs local. Use the Android Studio bundled JDK to reduce differences between machines.
