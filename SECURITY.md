# Security Policy — GeoSylva

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.1.x   | ✅ Current |
| < 1.0   | ❌ Not supported |

## Data Security

### Local Storage
- All forestry data is stored in a local **SQLite** database (Room) on the device.
- User preferences are stored in **DataStore** (encrypted at OS level on Android 10+).
- No data is transmitted to external servers (except map tile requests).

### Export Security
- Exported files (CSV, XLSX, JSON, ZIP) are written to user-selected locations via the Android **Storage Access Framework** (SAF).
- The app does not store or cache exported files in shared storage.

### Network
- The **only** network usage is loading map tiles from public servers (IGN, OpenStreetMap).
- No user data is included in network requests.
- The app functions fully offline (except map tiles).

### Signing
- Release builds are signed with a private keystore.
- The keystore and its credentials are excluded from version control via `.gitignore`.

## Reporting a Vulnerability

If you discover a security vulnerability in GeoSylva, please report it responsibly:

1. **Do not** open a public issue.
2. Contact the project owner directly via email.
3. Include a description of the vulnerability and steps to reproduce.
4. Allow reasonable time for a fix before public disclosure.

## Best Practices for Contributors

- Never commit keystore files, API keys, or credentials.
- Never log sensitive user data (GPS coordinates in production logs, etc.).
- Use `ProGuard`/`R8` for release builds to obfuscate code.
- Follow the principle of least privilege for Android permissions.
