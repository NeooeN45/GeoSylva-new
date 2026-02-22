# Changelog — GeoSylva

All notable changes to this project will be documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com).

## [1.3.0] — 2026-02-22

### Changed
- **GPS capture behavior** — immediate trigger on tap (before snackbar), persists even if user leaves the screen using GlobalScope
- **GPS profile simplification** — removed FAST/STANDARD/PRECISE modes and precision threshold slider, replaced with single optimal profile (6 readings, max 20m, timeout 15s)
- **GPS point reuse** — when a stem is deleted and re-added with same essence and diameter class, the last GPS point is automatically reused
- **Map precision visualization** — colored precision circles around GPS points: ≤3m (excellent) ≤6m (good) ≤12m (moderate) >12m (poor)  no GPS (gray)

## [1.2.0] — 2026-02-20

### Added
- **GPS averaging with outlier rejection** — multi-reading averaging using MAD (Median Absolute Deviation) to reject outlier readings. 3 configurable profiles: Fast (3 readings), Standard (5), Precise (8).
- **GPS precision threshold** — configurable per-stem rejection of imprecise GPS points (3–40 m slider in Settings).
- **GPS periodic monitoring** — background signal quality check with snackbar warnings when accuracy degrades.
- **Offline tile manager** — download map tiles for any region and zoom levels for full offline cartography. Progress tracking with UI overlay.
- **12 map layers** with tile URL templates for offline downloading (OSM, IGN, satellite, cadastre, forests, topo…).
- **Offline local map style** — MapLibre style generated from locally cached tiles.
- **Map GPS reliability filter** — toggle to show only stems below a configurable precision threshold on the map.
- **Shapefile export** (SHP/SHX/DBF/PRJ) — pure Java ESRI Shapefile writer, zero external dependencies. Exports geolocated stems with full attribute table.
- **Dashboard screen** — visual summary with Compose Canvas charts: donut (species distribution), bar chart (diameter classes), horizontal bars (basal area by species), and 4 summary mini-cards.
- **Harvest simulation** — enter N/ha and G/ha before harvest to compute removal rates and residual stand metrics. Persisted per scope.
- **Sanity checker** — 30+ automated data consistency checks at tree-level and aggregate-level (diameter/height bounds, H/D ratio, V/G ratio, N/ha extremes, duplicate detection, surface coherence…).
- **SanityWarningsCard** — UI card displaying coherence warnings with severity icons (error/warning/info).
- **HarvestSimulationCard** — UI card showing removal rates and residual stand.
- **Essence enrichment** — 9 new fields per species: `densiteBois`, `qualiteTypique`, `typeCoupePreferee`, `usageBois`, `vitesseCroissance`, `hauteurMaxM`, `diametreMaxCm`, `toleranceOmbre`, `remarques`.
- **95+ canonical essences** — expanded species database with full forestry metadata.
- **Essence info card** — species properties displayed in PlacetteDetailScreen actions dialog.
- **Contextual tip cards** — dismissible help cards on Parcelles, Placettes, Essences, Map, and Martelage screens.
- **Onboarding revamp** — 7 pages (was 4) with bullet points, accent-colored gradient icons, page counter, scale/alpha animations, and scrollable content. New pages: GPS, Map, Export.
- **Height prompt snooze** — option to dismiss height reminders for 1h, 4h, or 24h. Persisted in preferences.
- **Map stem rendering overhaul** — GeoJSON source with clustering (CircleLayer + SymbolLayer), tap-to-inspect stems, dynamic color/size by essence and diameter.
- **SanityCheckerTest** — unit tests for data coherence validation.
- **QgisExportHelperTest** — 11 tests for GeoJSON, CSV-XY, and WKT export.
- **Price system overhaul** — quality-adjusted pricing (A=×2.5, B=×1.5, C=×1.0, D=×0.4) with per-essence coefficients.
- **Product breakdown UI** — expandable cards in MartelageScreen showing volume ventilation (BO/BI/BCh/PATE) with quality-adjusted pricing per essence.
- **Enriched price defaults** — ~100 entries with essence×product×diameter ranges (e.g., Douglas BO 25-34cm = 80€, 35-44cm = 95€, 45+ = 120€).
- **Documentation refresh** — `docs/methodes_calcul_volume.md` rewritten to match actual TarifCalculator implementation (7 methods + price system).

### Improved
- **Partial volume results** — synthesis shows partial volumes with completeness percentage instead of zeroing when some heights are missing.
- **VolumeCard** always visible with partial-result label when completeness < 100%.
- **ClassDistributionCard** simplified — removed unused `volumeAvailable` and `animationsEnabled` parameters.
- **Martelage CSV export** includes `volume_completeness_pct` and harvest simulation data.
- **OnboardingScreen** — animations with spring physics, radial gradient icons, vertical scroll support.
- **Settings screen** — new GPS section with capture mode dropdown, precision slider, map reliability toggle.
- **ProGuard/R8** rules optimized for release builds (minification + resource shrinking).
- **`.gitignore` hardened** — excludes build logs, keystores, archives, SIG data, OS files.
- **Missing-heights wording** changed from "disabled" to "partial" throughout the app.

### Fixed
- Unescaped apostrophe in French strings causing resource compilation error.
- Volume card was hidden when heights incomplete — now always visible with completeness indicator.
- Missing-height warning correctly tied to actual missing essences, not global flag.

### Removed
- Tracked build artifacts from repository: `buildlog.txt`, `kotlinlog.txt`, `local.properties`, `logo app.png`, `keystore/upload-certificate.pem`.
- Scratch reference code: `martelage_calcul/` directory.
- Unused animation imports from `MartelageSummaryCards.kt`.

### Security
- Created `SECURITY.md` with responsible disclosure policy.
- Removed `upload-certificate.pem` from version control.
- Added price sync (OkHttp) disclosure to `PRIVACY_POLICY.md`.

## [1.1.0] — 2026-02-17

### Added
- **FGH tariff method** for volume calculation (V = F × G × H).
- **PDF synthesis export** — professional A4 PDF report with dendrometry, valorization, and per-essence table.
- **GeoJSON & CSV-XY export** for QGIS integration with Lambert 93 coordinates.
- **PeuplementAvantCoupe calculator** — "stand before harvest" Excel-equivalent computation for softwoods.
- **Wood quality grading** (ABCD) with quality distribution in martelage summary.
- **Privacy Policy** (`PRIVACY_POLICY.md`) for Play Store compliance.
- Automated local backup via WorkManager.

## [1.0.0] — 2025-12-01

### Added
- Initial release with tree counting, diameter measurement, multi-tariff volume calculation.
- MapLibre map with satellite, topographic, cadastral layers.
- GPS single-shot location with precision averaging.
- CSV/XLSX/JSON import and export.
- Shapefile overlay support.
- Multi-language support (French/English).
- Dark/light theme with customizable font size.
- Onboarding wizard.
