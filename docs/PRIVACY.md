# Privacy

Kronoza is designed to work locally without an account or app network connection.

## Data and network access

- The app manifest does not declare `android.permission.INTERNET`.
- Events, tasks, reminder records, notification history, and preferences are persisted in local Room/DataStore storage.
- The app does not include Firebase or analytics dependencies in its active build configuration.
- Manual backups are created and opened at a location chosen by the user through Android's Storage Access Framework. Kronoza does not upload them.
- Avatar selection uses Android's Photo Picker rather than broad media-library access.

The app does not include account-based cloud synchronization. Android OS backup and device-transfer behavior is separate: the app allows backup and its XML rules include the Room database and DataStore preferences for cloud backup; device transfer also includes the profile avatar file. Platform and user backup settings apply.

## Manifest permissions

| Permission | Purpose |
| --- | --- |
| `POST_NOTIFICATIONS` | Show reminder notifications on Android versions that require notification permission |
| `SCHEDULE_EXACT_ALARM` | Schedule time-sensitive reminders when the system grants exact-alarm access |
| `RECEIVE_BOOT_COMPLETED` | Reconcile scheduled reminders after device startup |

The app also listens for package replacement, time-zone changes, and system clock changes to refresh reminder schedules. Exact-alarm access can be restricted by Android; the app includes WorkManager scheduling support.

## Backups

JSON backup files may contain the user's schedule, tasks, preferences, and related records. Keep exported files in a location appropriate to their sensitivity. Backups are not encrypted by the app; SHA-256 verification checks integrity but does not provide confidentiality.
