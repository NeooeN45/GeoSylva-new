# Privacy Policy — GeoSylva

**Last updated:** February 8, 2026

## Introduction

GeoSylva ("the App") is a forestry field management application for Android.
This privacy policy explains what data the App collects, how it is used, and how it is stored.

## Data Collection

### Data collected by the App

| Data type | Collected? | Purpose | Stored where? |
|---|---|---|---|
| GPS location | Yes (optional) | Position marking on map, tree/plot geolocation | Device only |
| Forestry data (trees, plots, parcels) | Yes | Core functionality | Device only |
| User preferences (theme, font size) | Yes | App customization | Device only |
| Import/export files (CSV, XLSX, JSON, ZIP) | Yes (user-initiated) | Data exchange | Device only |

### Data NOT collected

- No personal identification data (name, email, phone number)
- No account or login required
- No analytics or usage tracking
- No crash reporting to external services
- No advertising or ad identifiers
- No cookies or web tracking

## Data Storage

**All data is stored exclusively on the user's device.**

- Forestry data is stored in a local SQLite database (Room).
- User preferences are stored in local DataStore.
- Imported shapefiles and GeoJSON files are stored in local app storage.
- No data is transmitted to any server, cloud service, or third party.

## Network Usage

The App uses an internet connection **only** for the following purposes:

- **Map tile loading**: downloading map background images (satellite, topographic, cadastral) from public tile servers (IGN/GeoPortail, OpenStreetMap, MapLibre).
- **Price table sync** (optional, user-initiated): downloading wood market prices from a URL configured by the user in Settings. No user data is sent; the app only performs an HTTP GET request to fetch price data.

No personal or forestry data is sent over the network. Map tile requests contain only standard HTTP headers and geographic coordinates of the visible map area.

## Location Data

- Location access is **optional** and requires explicit user permission.
- GPS coordinates are used solely for positioning on the map and geolocating forestry measurements.
- Location data is stored **only on the device** and is never transmitted externally.
- The App uses single-shot location requests, not continuous background tracking.

## Third-Party Services

The App does not integrate any third-party analytics, advertising, or tracking services.

Map tiles are loaded from the following public services:
- IGN GeoPortail (data.geopf.fr) — French national geographic institute
- OpenStreetMap tile servers
- MapLibre demo tile server (for map fonts/glyphs)

These services may log standard HTTP access information (IP address, request time) according to their own privacy policies.

## Data Sharing

**GeoSylva does not share any user data with any third party.**

The user may choose to export their own data (CSV, XLSX, JSON, ZIP) using the Android share/save system. This is entirely user-initiated and user-controlled.

## Data Retention and Deletion

- All data remains on the device until the user deletes it.
- Uninstalling the App removes all locally stored data.
- The user can delete individual records, groups, or all data from within the App at any time.

## Children's Privacy

GeoSylva is a professional forestry tool. It is not directed at children under 13 and does not knowingly collect data from children.

## Changes to This Policy

This privacy policy may be updated from time to time. Changes will be reflected in the "Last updated" date above and in the App's repository.

## Contact

For questions about this privacy policy, please contact:

**Email**: [Contact the project owner]

---

*This privacy policy applies to the GeoSylva Android application.*
