# Security policy

## Supported versions

Security fixes are intended for the latest released version. The current version recorded in the project is `1.0.0`.

## Reporting a vulnerability

Please do not publish sensitive vulnerability details in a public issue. Send a report to **Om Lakhera** at [omlakhera204@gmail.com](mailto:omlakhera204@gmail.com) with:

- The affected app version and Android version/device, where relevant.
- A concise description of the issue and its potential impact.
- Steps to reproduce or a minimal proof of concept that does not access another person's data.

Please allow time for review and remediation before public disclosure. Do not attach real calendar exports, personal data, credentials, or signing materials.

## Security notes

Kronoza stores its app records locally and does not request the Android internet permission. User-exported JSON backups are not encrypted; their SHA-256 checksum detects data changes but is not a security boundary or confidentiality mechanism. Android-managed backup and device-transfer behavior follows the app's declared rules and the user's platform settings.

Never submit signing keys, passwords, API credentials, or private user backups to the repository.
