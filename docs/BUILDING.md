# Building from source

## Requirements

- JDK 17 or newer for Gradle
- Android SDK Platform 36 and Android Build Tools
- Android Studio with support for the configured Android Gradle Plugin, or a command-line Android SDK installation
- A Windows, Linux, or macOS host

The application source/target compatibility is Java 11; this does not change the JDK version required to run the build.

## Open in Android Studio

1. Clone or download the repository from its GitHub location.
2. Open the repository root (the directory containing `settings.gradle.kts`) in Android Studio.
3. Allow the Gradle Wrapper to download the configured Gradle distribution and sync dependencies.
4. Install SDK Platform 36 if prompted, then run the `app` configuration on a device or emulator.

Android Studio may create `local.properties` to record the local Android SDK path. It is machine-specific and must not be committed.

## Command-line build

Run commands from the repository root. The Wrapper pins the Gradle version, so a separate system Gradle installation is not needed.
On Linux and macOS, if the checkout does not preserve the executable bit, run `chmod +x gradlew` once.

### Windows PowerShell

```powershell
.\gradlew.bat assembleDebug
```

### Linux and macOS

```bash
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected device with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Other build variants

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

On Windows, use `.\gradlew.bat` instead of `./gradlew`. Release builds are minified and resource-shrunk. Without optional release-signing credentials they are not signed for distribution; see [Release](RELEASE.md).

## Offline builds

The first build needs access to Gradle distribution and Maven repositories unless the required artifacts are already cached. Subsequent builds can use Gradle's offline mode when all dependencies are available locally:

```bash
./gradlew --offline assembleDebug
```
