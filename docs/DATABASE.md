# Database and persistence

Kronoza stores app data locally using Room and AndroidX DataStore Preferences.

## Room database

The Room database is named `kronoza_db` and currently uses schema version 3. It contains:

| Table | Contents |
| --- | --- |
| `events` | Event dates/times, recurrence and series data, categories, notes, attendees, and checklist items |
| `tasks` | Due dates/times, priority, category, completion, reminders, and subtasks |
| `notifications` | In-app reminder/notification history and related event or invite metadata |
| `reminders` | Scheduled reminder target, due time, time zone, and delivery state |

Indexes support event date/series lookups, task due-date/completion lookups, and reminder target lookups. Kotlin value types such as recurrence options and nested checklist/subtask data are converted for Room by `KronozaConverters`.

The non-destructive `MIGRATION_2_3` adds event recurrence and update fields, task update timestamps, and indexes. Database schema changes should use explicit Room migrations and retain existing user records.

## Preferences

`PreferencesRepository` stores appearance, calendar defaults, notification options, onboarding state, and optional profile values in the DataStore file `kronoza_prefs`. Profile avatar content is stored in app-managed files; Android's configured backup/transfer rules determine whether selected app data is included in system-managed transfers.

## JSON backup and restore

The user can export or choose a JSON backup using Android's Storage Access Framework. The current backup codec writes schema version 2 and accepts supported versions 1 through 2. Imports are limited to 10 MiB, parser nesting is limited to 32 levels, and the payload is checked with SHA-256 before restore.

Restore supports:

- **Merge / skip duplicates:** retain existing records and add non-duplicate imported records.
- **Replace all:** replace the app's existing database records with the backup contents.

Backup restore is performed through the database layer; users should still keep their own safe copies of exported files. The checksum detects accidental or unauthorised payload changes but is not encryption or authentication.
