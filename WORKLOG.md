# Work log and handoff

Updated: 2026-09-26

## UI feedback follow-up — route map and learning content

- Responding to the user's review: place facts now have a clear `CIEKAWOSTKA` heading; the Spanish stage explicitly presents pronunciation, a phonetic hint for `torre`, and a prominent listen-and-repeat action.
- Improved the map's online street style to OpenFreeMap Liberty and added numbered coral map pins for real City Pack stops, plus an itinerary card and concise legend. After GeoJSON circles and MapLibre annotations proved invisible in emulator screenshots, pins were moved into a Compose overlay positioned through MapLibre's coordinate projection. A manual pan check confirms the marker follows its real location. GPS stays blue and is hidden when it overlaps the destination marker. A leftover “map position updated” toast was removed.
- Fixed completed-progress navigation: `Misje` used to point to the passport after the final stage, making the tab duplicate the passport. It now opens a Missions overview with direct links and visible summaries for the place fact, quest, Spanish word/pronunciation, and quiz. Individual learning screens remain reachable after completion.
- The Barcelona pack currently contains only Sagrada Família. The map intentionally does not invent other itinerary stops; the next content expansion should add real, reviewed City Pack entries with coordinates and learning material.
- Added City Pack validation coverage for the pronunciation hint. Final `testDebugUnitTest assembleDebug` passed after the Missions overview was added. On the API 35 emulator at 1080×1920, visually confirmed the numbered map marker, horizontal pan tracking, distinct Missions/Passport destinations, the fact and quest summaries, the Spanish word/pronunciation hint, and the dedicated listen-and-repeat screen. Emulator checks do not confirm TTS audio quality on a physical phone.
- Local visual-QA screenshots are temporary and must not be committed. Personal photos in `foty/` remain Git-ignored and are not included.

This file is the cross-computer handoff note. GitHub `main` is the source of truth. Read this file, [the code map](docs/CODE_MAP.md), and `PROJECT_SPEC.md` before continuing on another computer.

## Visual direction milestone

- Added `docs/DESIGN_SYSTEM.md` as the visual source of truth and `docs/CHARACTER_BRIEF.md` as the source of truth for Natalia's illustrated character. `AGENTS.md` now requires both documents before relevant UI/character work and explicitly keeps real reference photos out of Git.
- Audited the current UI: screens and flows are functional but live mostly in one `MainActivity.kt`, use default Material 3 styling, direct screen state, a single scrolling column, emoji character placeholders, and several hardcoded map/celebration colors. No production theme/token/component layer exists yet.
- Prepared three character concept previews from private local references and three matching UI comparison boards covering Home, Map/Place, Quest/Quiz, and Passport/Reward. The user selected UI B with restrained progression cues from C, specifically preferring B's simpler Barcelona passport and badge. Generated previews remain outside Git.
- Added `docs/UI_DIRECTIONS.md` with layouts, hierarchy, color/character/photo use, components, navigation, motion, strengths, limitations, age risk, Compose difficulty, mixable elements, and conflicts. The selected UI direction is documented; no production UI has been replaced.
- Historical note: the user rejected the generated 3D character direction. The current approved character direction is a hand-drawn 2D illustration with fine ink contours and soft painterly/cel shading, based on the latest Barcelona character preview supplied/approved by the user. Do not use the earlier 3D concept set as the character reference.
- Prepared a refreshed four-screen B+C UI concept board for Home, Map/Place, Quest/Quiz, and Barcelona Passport/Reward. Direction B remains the layout and storytelling base; selected C progress cues are compact; the passport stays simple with one destination stamp and one earned badge. The board is a preview only and remains outside Git. The user has not yet approved this UI board; production screens and UI assets remain unchanged.
- Updated `docs/CHARACTER_BRIEF.md`, `docs/DESIGN_SYSTEM.md`, and `docs/UI_DIRECTIONS.md` to reflect the current 2D character style and the B+C UI concept. Private source photos remain in the Git-ignored `foty/` directory; exploratory preview boards are not committed.

## Visual implementation milestone — first phone preview

- The user approved the hand-drawn/painterly Natalia illustration and the refreshed B+C UI concept. The selected direction is now reflected in the production Compose UI.
- Added `NataliaTheme.kt` with centralized palette, typography, and shape tokens; added persistent bottom navigation for Explore, Missions, Passport, and Rewards; redesigned Home with the approved Barcelona hero, a compact XP/level progress display, and an active mission card; restyled place, quest, word, quiz, passport, and reward content.
- Added generated, transparent character reaction art for thinking, discovery, victory, and ice-cream reward. These and the approved Home hero are stored under `app/src/main/res/drawable-nodpi/`. No files from `foty/` were added.
- Updated `docs/ANIMATIONS.md`, `docs/AVATAR_ASSETS.md`, `docs/CODE_MAP.md`, `docs/CHARACTER_BRIEF.md`, `docs/DESIGN_SYSTEM.md`, and `docs/UI_DIRECTIONS.md`.
- `./gradlew.bat --no-daemon assembleDebug` passed after setting process-local `ANDROID_HOME`/`ANDROID_SDK_ROOT` to the SDK installed on this machine. No tests or emulator/device checks were run in this visual pass. The debug APK is at `app/build/outputs/apk/debug/app-debug.apk` for the user's phone review.

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
- Home lets the player request the 100-star ice-cream reward; a parent can review, approve, or reject pending requests in a PIN-gated dialog.
- Parent PIN is configured on first use, checked as a keyed HMAC using a non-exportable Android Keystore key, and protected by a 30-second lockout after five failed attempts. The PIN itself is not persisted. The PIN credential is device-local; a new device or reinstall requires setup again. The parent dialog is intended to close and lock when the app goes to the background.
- Debug-only Developer Mode can advance one game stage at a time, add test stars, force the next level, spend test stars, reset one POI or all progress with confirmation, inspect permission/provider/fresh-fix GPS diagnostics, run inside/outside/low-accuracy simulated check-ins through `GameEngine.checkIn`, enter arbitrary validated coordinates/accuracy for a check-in simulation, and create a device-local test POI at the current GPS position that is restored after app restart. A single-POI reset clears completion markers but preserves the reward ledger/event IDs to prevent farming rewards by replay.
- Check-in errors for missing permission, disabled location services, and unavailable fresh location are shown as actionable Polish messages.
- The Spanish word stage can speak its word with the platform `es-ES` voice. The UI handles a missing engine/voice and releases TTS when the app screen is disposed.
- Foreground one-shot Fused Location Provider checks coarse/fine permission, enabled location providers, and a fresh location. No background location or route history.
- The mission map now uses MapLibre Native with the OpenFreeMap Liberty street style (OpenStreetMap data), numbered route stops, POI/geofence/GPS overlays, visible attribution, and a local schematic fallback when the style cannot load. It requires internet and does not yet download offline map packs. No Google Maps key is needed. See `docs/LOCATION.md` for provider terms and offline-pack constraints.
- A Compose instrumentation smoke test launches the app and opens the Sagrada Família check-in screen.
- GitHub Actions runs unit tests, compiles instrumentation test sources, builds a debug APK, and uploads that APK. Successful APK artifacts are retained for 14 days.
- [`docs/CODE_MAP.md`](docs/CODE_MAP.md) describes file ownership and where to look for common problems.

## Verification state

- Green GitHub Actions run for code commit `c3c8e73d868721675463903250f2acd7c4e6f92b`: unit tests, instrumentation test compilation, debug APK build, and artifact upload passed. Documentation-only commit `4897bd830a7a7759b5143b442d0e0989733bc516` also passed.
- Parent PIN and Compose approval UI commit `2ca9bd0702be2504cd7dc2ab7576204b519c8628` passed GitHub Actions run [36155555226](https://github.com/tig-staking/natalia/actions/runs/36155555226): unit tests, instrumentation source compilation, debug APK build, and artifact upload. APK artifact `10873641102` is available until 2026-10-09.
- Developer Mode stage controls commit `20e51519a681110ab3e480ac825fb29b7f5ae3ad` passed GitHub Actions run [36156150073](https://github.com/tig-staking/natalia/actions/runs/36156150073): unit tests, instrumentation source compilation, debug APK build, and artifact upload. APK artifact `10873961581` is available until 2026-10-09.
- The first CI run for GPS diagnostics (36160340582) caught a missing `Context` import. Follow-up commit `20fd9c3cbc1bbc009b93bd8bfcdd4d1916fb6603` fixed it and passed GitHub Actions run [36160595010](https://github.com/tig-staking/natalia/actions/runs/36160595010); APK artifact `10875501283` is available until 2026-10-09.
- CI passed for the parent PIN and Developer Mode changes. The emulator smoke test still only compiles in hosted CI; no runtime emulator or physical-device GPS test has been run.
- The run uploaded artifact `natalia-debug-apk` (id `10864448021`), available until 2026-10-09.
- Tests cover reward request reservation, no spending before approval, idempotent approval, cancellation, ledger entries, and persistence of pending requests after repository recreation.
- The emulator smoke test has compiled but has not run on a device. GitHub-hosted runs could not boot the configured emulator; CI avoids emulator startup while keeping the instrumentation compile check.
- No on-device GPS test has been run.
- Persistent test POI creation, restoration, and removal passed GitHub Actions run [36170862095](https://github.com/tig-staking/natalia/actions/runs/36170862095). The local config stores only one test point, not a location trail; full reset removes it.
- Per-POI reset commit `eb2051888fd0edca324609b9fc8a5af68a644cb0` passed GitHub Actions run [36168049429](https://github.com/tig-staking/natalia/actions/runs/36168049429); APK artifact `10879655186` is available until 2026-10-09.
- Spanish TTS commit `08a3853cc0f8a6e082f1a11332ad41256aa6ba1f` passed GitHub Actions run [36168758899](https://github.com/tig-staking/natalia/actions/runs/36168758899); APK artifact `10879981224` is available until 2026-10-09. No on-device TTS playback has been checked.
- Parent background-lock and PIN-field clearing commit `7124b3508709def5e2cd0cd282385d85ca3660d3` passed GitHub Actions run [36169271214](https://github.com/tig-staking/natalia/actions/runs/36169271214); APK artifact `10880106916` is available until 2026-10-09.
- Persistent test POI creation now requests location permission on demand and shows specific GPS failure messages. Its CI passed in [36171336678](https://github.com/tig-staking/natalia/actions/runs/36171336678). The developer shortcut also now uses the active place name, including for a test POI. Run [36182342440](https://github.com/tig-staking/natalia/actions/runs/36182342440) exposed an intermittent DataStore file-lock race in an existing repository recreation test; commit `97c94f3c4f16a68aa9dd1be0323c7ccc3509c633` waits for the old scope to shut down, and the full workflow passed in [36182697293](https://github.com/tig-staking/natalia/actions/runs/36182697293).

- Test POI creation now requests foreground location permission when needed and reports distinct permission, disabled-services, and unavailable-fix errors; this permission-recovery path awaits CI.
- Added a Home entry to an offline coordinate-scaled local map preview. It plots the active POI geofence and fresh foreground GPS position, reports distance and accuracy, and derives LOCKED / DISCOVERED / IN_PROGRESS / COMPLETED from saved progress; it uses no network tiles. Updated `docs/CODE_MAP.md` to describe the map boundary.
- Validation attempt on this computer: `./gradlew --no-daemon testDebugUnitTest compileDebugAndroidTestSources assembleDebug` could not start because neither `JAVA_HOME` nor a `java` executable is configured. `adb devices -l` is also unavailable because `adb` is not installed/on `PATH`. Runtime device checks remain unperformed.
- Installed Microsoft OpenJDK 17 and Android SDK command-line tools, Platform 35, Build Tools 35.0.0, and Platform Tools in the user profile; no machine-specific SDK path was written to the repository. With process-local `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT`, `gradlew.bat --no-daemon testDebugUnitTest compileDebugAndroidTestSources assembleDebug` passed. The new onboarding persistence test passed with the suite.
- Added first-run onboarding stored in DataStore and an offline local coordinate map preview that requests a fresh foreground fix, plots the POI and nearby user location, and shows distance and accuracy. Added the reusable short celebration banner for discovery, quest, word, quiz, and approved reward events. Added `docs/ANIMATIONS.md` and `docs/AVATAR_ASSETS.md`.
- GameEngine quest completion now enforces typed responses: multiple-choice quests only pay on the configured correct answer, and parent-check quests require the authenticated parent flow. The quest UI presents choices and routes parent confirmations through the existing PIN gate. Unit tests cover both gates and City Pack parsing of choice data.
- Level threshold crossings now change the celebration text to the new level title. Updated the Compose smoke test to handle onboarding and persisted progress across emulator runs.
- Aligned the live reward request and Developer Mode star-grant action with the specified 100-star ice-cream reward.
- Latest verification: `testDebugUnitTest compileDebugAndroidTestSources assembleDebug` passed after typed quest, animation, map, onboarding, and 100-star reward changes. Managed Pixel 2 API 35 emulator smoke test passed (1/1); this verifies app launch and place check-in UI, not actual GPS or the complete reward flow.
- Online street map integration milestone: added MapLibre Native 13.6.1, Android internet permission, OpenFreeMap Positron style, POI/geofence/GPS overlays, timeout fallback, and provider/license notes. Local build/unit tests/instrumentation compilation pass. The first dedicated map UI test lost its Compose hierarchy with the native map view, but manual emulator inspection subsequently confirmed the online style and street tiles load. That inspection found and fixed an opaque geofence fill; it is now translucent. Full local Pixel 2 API 35 instrumentation suite passes 4/4 on 2026-09-26.
- Manual API 35 emulator check-in: injected the Sagrada Família coordinates through the emulator location provider, granted foreground location, and tapped the normal `JESTEM NA MIEJSCU — SPRAWDŹ GPS` action. The app obtained a ±5 m fix, discovered the place, awarded the 10 XP / 10 stars discovery payout exactly once, and advanced to the quest screen. This validates the app's live Fused Location request and check-in UI on an emulator; it does not replace a physical-phone GPS test.
- User completed the phone checklist and confirmed the whole flow works: local test POI/GPS discovery, quest, Spanish speech, quiz retry, PIN-approved reward, and force-stop/relaunch. Location permission/error edge cases remain to be checked separately.
- Added Android Keystore instrumentation coverage for PIN plaintext storage and five-attempt lockout. The latest managed Pixel 2 API 35 run passed both instrumentation tests (2/2).
- Added a Compose parent reward test covering PIN setup, child request, parent approval, one-time 100-star debit, and ledger entry; its targeted managed-emulator run passed. Re-run the full instrumentation suite after this addition.
- Fixed onboarding restoration so a completed first-run flow returns to Home after process restart. Added animated XP/star counters and context-specific emoji reactions to the manga-burst celebration.
- Re-ran the complete managed Pixel 2 API 35 instrumentation suite after onboarding/reward UI changes: `pixel2api35DebugAndroidTest` passed. This includes the smoke flow, Android Keystore PIN lockout, and child request → parent approval → one-time star debit UI test.
- Map decision: keep game/check-in/progress fully offline and separate from the rendered map. MapLibre streams OpenFreeMap/OpenStreetMap street tiles online; manual emulator verification has confirmed streets render. No offline city pack is implemented. First find a provider/data source whose terms permit region downloads, then measure if a useful Barcelona pack fits the 200 MB budget before adding any download flow. Never bulk-download from OSM standard tiles or assume OpenFreeMap's public service permits automated pack collection.
- Combined successful quiz completion, one-time XP/stars, and badge unlock in one DataStore transaction. This closes the process-death window in which the quiz was complete but the badge had not yet been written. The ordered-flow unit test now checks the badge in the quiz result itself.
- GPS diagnostics now show the fresh latitude/longitude, distance to the active POI, accuracy, fix age, and whether the accuracy-aware check-in is inside, outside, or uncertain. Home exposes explicit Missions, Passport, and Rewards navigation/section labels; Passport shows discovery and badge status from persisted progress instead of displaying an unearned badge.
- Parent Mode now displays XP, stars, stars reserved for pending reward requests, and the five most recent star ledger entries. It can add or subtract 10 stars after PIN authentication. Adjustments are atomic, idempotent ledger entries; subtraction is rejected if it would use reserved stars. The dialog scrolls to keep controls reachable on smaller devices. The managed Pixel 2 API 35 suite passed 3/3 after the balance controls; the subsequent ledger display is a presentation-only change.
- Added a Compose full-stage emulator test: reset/onboarding, debug discovery, observation quest, Spanish word, wrong then correct quiz, badge/passport, Activity recreation, and restored passport. The managed Pixel 2 API 35 suite passed 4/4. The first attempt checked the passport before its async screen transition; the test now waits for that transition. This test does not replace physical GPS or a full process-death/phone installation check.

## Latest milestone — progress backup

- Added a versioned JSON backup for onboarding and all game progress, including XP, stars, event IDs, ledger, stage completions, badges, redemption history, and pending reward requests. Import validates balances and ledger consistency before one atomic DataStore update.
- Authenticated Parent Mode offers export and import. Import is size-limited, previews the destructive replacement, and requires the parent PIN again. PIN and device-local test coordinates are excluded; the destination test POI is preserved.
- Added codec round-trip, malformed-ledger, and repository import/preserve-device-POI tests. `testDebugUnitTest assembleDebug` passes. Phone transfer still needs verification before moving from debug to release signature. Do not create a keystore or switch the installed app yet.

## GPS resilience update

- Fused location now requests high accuracy only when precise permission is granted; approximate-only access uses balanced power and tells the player why check-in might need a better fix.
- Requests have a 15-second timeout. A pure policy rejects invalid coordinates, missing/non-finite accuracy, readings older than 30 seconds, and timestamps implausibly in the future. Unit coverage is added; device permission and approximate-only checks still need a physical phone.
- `testDebugUnitTest compileDebugAndroidTestSources assembleDebug` passes, and the full Pixel 2 API 35 `connectedDebugAndroidTest` suite passed 4/4 after this change.

## Emulator visual QA — full player flow

- Reviewed the installed debug build on the API 35 emulator at 1080×1920 / 420 dpi, including onboarding, Home, online map, place details, quest, Spanish word, quiz, completed passport/badge, and Rewards. The online map loaded, and the injected Sagrada location showed ±5 m accuracy. The complete player flow reached 50 XP / 50 stars and level 2; the 100-star reward was correctly disabled.
- Readability was good at the emulator's default display size. The fixed bottom navigation stays clear, screen changes now return to the top, and the passport/reward screens expose their primary information without clipped text. Physical-device GPS remains covered by the user's earlier successful phone check, not this emulator run.
- Polished the map after visual inspection: removed the persistent success toast, zoomed out one level to show more context, and reduced geofence fill opacity. Also improved status/navigation bar icon contrast, removed the irrelevant Home button from onboarding, compacted the map distance/accuracy summary, and tinted the selected bottom-navigation item with the approved coral accent.
- Build including these changes: `assembleDebug` passed. Emulator flow was manually driven; no test suite was run in this visual-only pass. Temporary screenshots/XML are local QA artifacts and must not be committed.

## Known gaps to address

1. The user confirmed the main phone flow: test POI check-in, quest, Spanish TTS, quiz retry, parent approval, and restart all worked. Still check approximate location, denied permission, disabled services, poor fixes, and leaving/re-entering a test POI.
2. Verify progress export/import on a physical phone before moving the current debug-signed install to a release-signed APK. Release signing support is in Gradle, but no private keystore has been created.
3. Replace emoji animation placeholders with reusable avatar, badge, level-up, and reward sequences; add reduced-motion handling and check small-screen layout.
4. Online maps meet the current need. Offline map packs are optional; only consider them after finding a source that allows region downloads and measuring a useful Barcelona pack against the 200 MB budget.
5. Add more Barcelona City Pack places after the tested engine remains stable. There is currently one sample place by design.

## Next milestones (ordered)

1. **Exercise location edge cases.** The user confirmed the main phone flow works. Still check approximate location, denied permission, disabled services, stale/poor fixes, and moving outside/back into a POI. Keep background geofencing and notifications optional because foreground check-in is the MVP requirement.
2. **Prepare a stable update path before wider installation.** Gradle now supports a private release keystore supplied through environment variables, and release packaging stops if signing data or the keystore is missing. Parent Mode now has versioned progress export/import with ledger validation and PIN re-authentication for destructive import. Verify this transfer on a physical phone, then generate and safely back up the keystore before the first release build.
3. **Improve the presentation layer.** Replace emoji placeholders with reusable avatar poses and distinct celebration, badge, level-up, and reward animations; support reduced motion and check small-screen layout. Keep game rewards independent of animation completion.
4. **Expand content after engine hardening.** Add Barcelona places through the City Pack schema. Online street maps are sufficient for the current use case; an offline pack is optional and needs a licensed data source plus a measured size at or below the 200 MB budget.

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

Roughly 35% of the full project; the core MVP flow has passed the phone checklist according to the user. The ordered engine, persistence, foreground GPS check-in, PIN-approved reward flow, diagnostics, test POIs, Spanish TTS, online street map, data-driven quests, and basic celebrations are implemented. Emulator instrumentation passes 4/4; the user confirms GPS, speech, quiz retry, reward approval, and restart on the phone. Remaining work: physical-phone verification of progress backup and GPS permission/error edge cases, a private release signing key, richer reusable animations and accessibility, optional licensed offline maps, more City Pack places, and final privacy/UX review. Debug APK assembly succeeds. Versioned progress backup/export is implemented and covered by unit tests; physical transfer still needs a phone check. Release signing configuration is present; no keystore or passwords are stored in the repository.
