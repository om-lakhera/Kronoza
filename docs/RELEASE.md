# Release guide

## Versioning and artifacts

The current app version is `1.0.0` (`versionCode` 1). Release APKs and bundles are minified with R8 and use the ProGuard rules in `app/proguard-rules.pro`.

Build from the repository root:

```bash
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

On Windows, use `.\gradlew.bat` in place of `./gradlew`. Outputs are written under:

- `app/build/outputs/apk/release/`
- `app/build/outputs/bundle/release/`

No release is published and no APK is uploaded by the repository's CI workflow.

## Optional signing

Release signing credentials are read from environment variables, never from committed files:

| Variable | Purpose |
| --- | --- |
| `KEYSTORE_PATH` | Path to the private upload keystore |
| `STORE_PASSWORD` | Keystore password |
| `KEY_PASSWORD` | Password for the `upload` key alias |

Set all three values together to sign a release. If none are set, Gradle produces an unsigned release artifact. A partial set is a configuration error. Keep the keystore and credentials in a secure location outside the repository; for automation, use the CI provider's protected secrets and provide the keystore securely at job runtime.

For example, set the values in your local shell and build:

```bash
export KEYSTORE_PATH=/secure/path/to/upload-key.jks
export STORE_PASSWORD='your-store-password'
export KEY_PASSWORD='your-key-password'
./gradlew :app:assembleRelease :app:bundleRelease
```

PowerShell:

```powershell
$env:KEYSTORE_PATH = 'C:\secure\upload-key.jks'
$env:STORE_PASSWORD = 'your-store-password'
$env:KEY_PASSWORD = 'your-key-password'
.\gradlew.bat :app:assembleRelease :app:bundleRelease
```

Never commit signing keys, passwords, signed APKs, or AABs. Verify signed APKs with Android SDK Build Tools `apksigner` before distribution. Store Play App Signing and release-channel procedures outside this source repository.
