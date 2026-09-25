# Local development

## Requirements

- Android Studio with Android SDK Platform 35 and a compatible JDK (Android Studio's bundled JDK is recommended).
- Git.
- A device or emulator running Android 8.0 (API 26) or newer.

## First setup on any computer

1. Clone the repository with `git clone https://github.com/tig-staking/natalia.git` (or run `git pull` in an existing checkout).
2. Open the repository root in Android Studio and allow Gradle sync to finish.
3. Install Android SDK Platform 35 if Android Studio asks.
4. Run the `app` configuration on an emulator or connected phone.

The initial app shell does not require a Maps key. When Google Maps is introduced, add the key to the ignored local `local.properties` file and follow the updated instructions here; never commit that file or put a key in source control.

## Build and install

From the repository root, use Android Studio's Build menu or run `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). The checked-in wrapper selects the project's Gradle version; Android Studio's bundled JDK is recommended. The debug APK is written under `app/build/outputs/apk/debug/` and can be installed using Android Studio or `adb install -r <path-to-apk>`.

## Working across computers

Before starting, run `git pull`. After finishing a coherent change, commit and push it. Device-specific SDK paths belong only in the ignored `local.properties`; generated Gradle and build files stay untracked. Do not copy secrets into the repository.
