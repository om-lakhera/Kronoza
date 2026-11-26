# Changelog

Notable user-facing changes are recorded here. Dates and release versions reflect the project files currently provided.

## [1.0.0] - 2026-11-24

See the [version 1.0.0 release notes](docs/releases/v1.0.0.md) for the full feature summary.

### Added

- Month, week, day, and agenda calendar views with recurring and multi-day event support.
- Event and task editing, task priorities/categories/subtasks, filtering, and search.
- Reminder scheduling with Android alarms, WorkManager support, and recovery after relevant system events.
- Productivity summaries computed from locally stored app records.
- Local preferences and optional profile/avatar settings.
- JSON backup and restore with merge/replace modes and SHA-256 integrity verification.

### Build and privacy

- Android app built with Kotlin and Jetpack Compose, with Room and DataStore persistence.
- App manifest contains reminder-related permissions and does not request internet access.
- Release variant uses R8 minification and resource shrinking.
