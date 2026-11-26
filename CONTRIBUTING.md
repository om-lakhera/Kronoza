# Contributing

Thank you for considering a contribution to Kronoza. Focused bug fixes, usability improvements, and documentation corrections are welcome.

## Before you start

- Check existing issues and documentation to avoid duplicating work.
- For a large change or a new permission, storage format, or external service, open a discussion/issue before implementing it.
- Keep contributions aligned with the app's local-first behavior and avoid adding telemetry or permissions without a clear, reviewed need.

## Development

1. Fork the repository and clone your fork.
2. Open the repository root in Android Studio or use the Gradle Wrapper.
3. Build the debug variant:

   ```bash
   chmod +x gradlew
   ./gradlew assembleDebug
   ```

   On Windows, use `.\gradlew.bat assembleDebug`.

4. Create a focused branch and keep changes scoped to one concern.
5. Update documentation when changing app behavior, persistence, build configuration, or permissions.

## Pull requests

- Describe the user-visible change and reference related issues.
- Include the build command and result in the PR description.
- For UI changes, include screenshots captured from the running app and ensure they contain no personal data.
- Do not include generated APK/AAB files, local SDK paths, `.env` files, signing credentials, or keystores.

By submitting a contribution, you agree that it may be distributed under the project's [MIT License](LICENSE).
