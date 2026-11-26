# Architecture

Kronoza is a single-module Android application organized around a Compose UI, a ViewModel, and repository-backed data sources.

## Layers

```text
Compose UI and Navigation
        ↓ user actions / observable state
KronozaViewModel
        ↓
KronozaRepository
  ├── KronozaDatabase and KronozaDao (Room)
  ├── PreferencesRepository (DataStore)
  ├── KronozaReminderScheduler (Android alarms and WorkManager)
  └── KronozaBackupManager (JSON import/export)
```

- **UI:** `MainActivity` hosts the Jetpack Compose interface. Screens and shared components live under `ui/`; `KronozaRoutes` defines app navigation.
- **State and app operations:** `KronozaViewModel` exposes UI state and forwards user actions to the repository.
- **Persistence and integrations:** `KronozaRepository` coordinates Room-backed records, preferences, reminder updates, and backup operations.
- **Platform services:** Reminder receivers and workers restore or deliver reminders in response to scheduled work and system broadcasts.

## Data flow

Room is the source of truth for events, tasks, reminders, and notification history. DAO flows are collected by the repository/ViewModel and observed by Compose. User preferences, including theme and calendar options, are stored separately in DataStore.

Calendar date calculations and task filtering/sorting are implemented in utility code under `util/`. Productivity summaries are derived from the app's persisted records rather than from a remote service.

## Android identity and build

The Gradle namespace and Kotlin source packages are `com.omlakhera.kronoza`. The application ID remains `com.aistudio.cadence.mvrkpx` to preserve the v1.0.0 Android release identity; changing that ID would create a different app and prevent direct upgrades from existing installations.

The application targets SDK 36, supports SDK 24 and newer, and compiles Java source/target compatibility at version 11. Build plugin and library versions are defined in the version catalog.
