# Local development

## Requirements

- Android Studio with Android SDK Platform 35 and JDK 17 or newer (Android Studio's bundled JDK is recommended).
- Git.
- A device or emulator running Android 8.0 (API 26) or newer.

## First setup on any computer

1. Clone the repository with `git clone https://github.com/tig-staking/natalia.git` (or run `git pull` in an existing checkout).
2. Open the repository root in Android Studio and allow Gradle sync to finish.
3. Install Android SDK Platform 35 if Android Studio asks.
4. Run the `app` configuration on an emulator or connected phone.

The online street map uses MapLibre and the public OpenFreeMap style; no Google Maps key or machine-specific map configuration is required. The map needs internet access. Core game progress and check-in rules remain local.

## Private release signing

Debug builds need no signing setup. A release APK must use the same private keystore on every computer so it can update the installed app without removing its local progress.

1. Create a keystore outside the repository, for example with PowerShell: `keytool -genkeypair -v -keystore "$env:USERPROFILE\.android\natalia-release.jks" -alias natalia -keyalg RSA -keysize 4096 -validity 10000`. Keytool prompts for the passwords; do not put them in the command.
2. Back up the keystore and passwords securely. A lost or replaced signing key prevents future in-place updates.
3. Set these user environment variables before starting Android Studio or Gradle: `NATALIA_RELEASE_STORE_FILE` (absolute keystore path, use `/` separators on Windows), `NATALIA_RELEASE_STORE_PASSWORD`, `NATALIA_RELEASE_KEY_ALIAS`, and `NATALIA_RELEASE_KEY_PASSWORD`. Keep the keystore and passwords outside Git and do not send them in chat.
4. Build with `./gradlew :app:assembleRelease` (Windows: `gradlew.bat :app:assembleRelease`). The build stops with a clear error if signing values or the keystore are missing.

The phone currently has a debug-signed build. Android will not install a release-signed APK over it. Before switching signatures, export or otherwise preserve any progress that should survive the one-time reinstall; keep updating the current debug install from the same computer in the meantime.

## Build and install

From the repository root, use Android Studio's Build menu or run `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). The checked-in wrapper selects the project's Gradle version; Android Studio's bundled JDK is recommended. The debug APK is written under `app/build/outputs/apk/debug/` and can be installed using Android Studio or `adb install -r <path-to-apk>`.

## Working across computers

Before starting, run `git pull`. After finishing a coherent change, commit and push it. Device-specific SDK paths belong only in the ignored `local.properties`; generated Gradle and build files stay untracked. Do not copy secrets into the repository.
