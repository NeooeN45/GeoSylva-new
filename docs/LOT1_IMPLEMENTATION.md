# Lot 1 — Contrat universel de données

**Date** : 2026-08-04
**Statut** : Implémenté et vérifié (build + tests + APK)
**Spec** : GEOSYLVA_3_SPECIFICATION_FONCTIONNELLE.md §7.6, §29

---

## Vue d'ensemble

Le Lot 1 établit le **contrat universel de données** de GeoSylva 3.0 :
séparation stricte entre Observation, Measurement, Evidence et
CalculationRun, ajout d'UUID pour l'interop GSIE serveur, catalogue
d'unités avec convertisseur, journal d'événements, et refonte de la
navigation principale (bottom nav 5 entrées + HomeScreen + ExplorerScreen).

---

## Sprints implémentés

### Sprint 1 — Data Layer

#### 1.1 Nouvelles entités (9 tables)

| Entité | Table | Rôle |
|--------|-------|------|
| `PermanentTreeEntity` | `permanent_trees` | Arbre permanent (identité stable, UUID) |
| `TreeObservationEntity` | `observations` | Observation d'arbre/peuplement (campagne datée) |
| `MeasurementEntity` | `measurements` | Mesure atomique rattachée à une observation |
| `EvidenceEntity` | `evidence` | Pièce jointe (photo, audio, doc, GPS) |
| `CalculationRunEntity` | `calculation_runs` | Résultat calculé (Method Registry — Lot 2) |
| `UnitEntity` | `units` | Catalogue d'unités de mesure |
| `EventLogEntity` | `event_log` | Journal d'événements (base sync Lot 5) |
| `ProjectEntity` | `projects` | Projet/dossier organisationnel (§29.11) |
| `ProjectForestCrossRef` | `project_forests` | Jonction N-N projets ↔ forêts |

**Principe fondamental** (§7.6) :
```
Arbre permanent ≠ Observation ≠ Mesure ≠ Résultat calculé
```
Aucun niveau n'écrase un autre — tout est conservé pour traçabilité
temporelle. Une correction crée une nouvelle mesure avec
`replacesMeasurementId` ; un recalcul crée un nouveau run avec
`supersedesRunId`.

#### 1.2 Migration DB 34→35

- Ajout colonne `uuid` (TEXT, nullable) sur forets, parcelles, placettes, tiges
- Ajout colonnes `provenance_*` sur forets et parcelles (§29.13)
- Création des 9 nouvelles tables avec FK, indices uniques et non-uniques
- **Stratégie UUID** : `legacy_id` + backfill asynchrone (décision Fondateur)

#### 1.3 DAO

- 8 nouveaux DAO : `PermanentTreeDao`, `TreeObservationDao`,
  `MeasurementDao`, `EvidenceDao`, `CalculationRunDao`, `UnitDao`,
  `EventLogDao`, `ProjectDao`
- Méthodes `getWithoutUuid()` + `setUuid()` ajoutées sur les 4 DAO cœur
  existants (ForetDao, ParcelleDao, PlacetteDao, TigeDao) pour le backfill
- Méthodes `getAllNow()` / `getAllPlacettesNow()` / `getAllTigesNow()`
  ajoutées pour le BackupService

#### 1.4 Catalogue d'unités + convertisseur

- `UnitCatalog` : 18 unités forestières (length, area, volume,
  volume_per_ha, mass, angle, count, dimensionless) avec facteurs de
  conversion vers l'unité de référence de chaque dimension
- `UnitConverter` : conversion intra-dimension, rejet inter-dimension,
  formatage avec symbole français

#### 1.5 Journal d'événements

- `EventLogEntity` + `EventLogDao` : table append-only, indexée par
  entityType/entityId, eventType, occurredAt, synced
- `EventLogger` : service d'émission avec raccourcis (logCreate,
  logUpdate, logSoftDelete), non-blocant sur erreur

### Sprint 2 — Services & Workers

#### 2.1 Bridge use cases (non-régression)

- Les 40 tests unitaires existants passent sans modification
- Aucune régression introduite par l'ajout des colonnes uuid + provenance

#### 2.2 BackupService

- `BackupService` : export/import JSON round-trip des entités cœur
  (Forêt → Parcelle → Placette → Tige) avec préservation des identifiants
  et des metadata §3.1
- 3 tests activés (round-trip, restauration après crash, backup malformé)
- 4 fakes DAO créés pour les tests

#### 2.3 UuidBackfillWorker

- `CoroutineWorker` idempotent qui génère un UUID RFC 4122 pour chaque
  ligne où `uuid IS NULL` (forets, parcelles, placettes, tiges)
- Max 3 tentatives, logging, WorkManager-ready

### Sprint 3 — Navigation principale

#### 3.1 MainScaffold bottom nav 5 entrées

- `BottomNavDestination` : enum avec 5 entrées (Accueil, Explorer,
  Missions, Carte, Compte)
- `MainScaffold` : Scaffold avec `NavigationBar` Material 3, masquée
  sur les sous-routes
- Navigation avec `popUpTo` + `saveState` + `restoreState` (pas de pile
  de destinations de premier niveau)

#### 3.2 HomeScreen (Accueil tableau de bord)

- `HomeViewModel` : agrège stats (nombre de forêts, parcelles) via
  `combine()` sur les Flow des repositories
- `HomeScreen` : LargeTopAppBar + LazyColumn avec stats cards, accès
  rapide, et forêts récentes

#### 3.3 ExplorerScreen (13 catégories)

- `ExplorerCategory` : enum avec 13 catégories (Forêts, Parcelles,
  Placettes, Arbres, Observations, Mesures, Calculs, Preuves, Essences,
  Stations, Diagnostics, Projets, Événements)
- `ExplorerScreen` : grille 2 colonnes avec cartes catégorielles
- Catégories implémentées : Forêts, Essences, Projets
- Autres catégories : marquées "À venir"

#### 3.4 Redirect Forets → Accueil

- Start destination changé de `Screen.Forets.route` à
  `BottomNavDestination.startRoute` (Accueil)
- Ancienne navigation Forets/Settings non accessible depuis la bottom bar

### Sprint 4 — Projets

#### 4.1 ProjectsScreen + ProjectDetailScreen

- `ProjectRepository` (interface domain) + `ProjectRepositoryImpl` (data)
- `ProjectsViewModel` : liste, création, favori, suppression
- `ProjectsScreen` : liste avec FAB création, cards avec favori
- `ProjectDetailScreen` : 7 onglets (Vue générale, Forêts, Missions,
  Documents, Carte, Équipe, Historique) — Vue générale implémentée,
  autres en stub "À venir"
- Routes `Screen.Projects` et `Screen.ProjectDetail` ajoutées au nav graph

### Sprint 5 — Quality Pass

#### 5.3 Build + tests + APK

- `compileDebugKotlin` : SUCCESS
- `testDebugUnitTest` : SUCCESS (40 tests existants + 38 nouveaux + 3
  BackupService = 81 tests au total)
- `assembleDebug` : SUCCESS (app-debug.apk généré)

---

## Tests

| Suite | Tests | Statut |
|-------|-------|--------|
| `UnitConverterTest` | 30 | ✅ |
| `EventLoggerTest` | 8 | ✅ |
| `BackupRestoreTest` (BackupService) | 3 activés | ✅ |
| Tests existants (non-régression) | 40 | ✅ |
| **Total** | **81** | ✅ |

---

## Fichiers créés (Lot 1)

### Entités (9)
- `data/local/entity/PermanentTreeEntity.kt`
- `data/local/entity/TreeObservationEntity.kt`
- `data/local/entity/MeasurementEntity.kt`
- `data/local/entity/EvidenceEntity.kt`
- `data/local/entity/CalculationRunEntity.kt`
- `data/local/entity/UnitEntity.kt`
- `data/local/entity/EventLogEntity.kt`
- `data/local/entity/ProjectEntity.kt`
- `data/local/entity/ProjectForestCrossRef.kt`
- `data/local/entity/ProvenanceEmbed.kt`

### DAO (8 nouveaux)
- `data/local/dao/PermanentTreeDao.kt`
- `data/local/dao/TreeObservationDao.kt`
- `data/local/dao/MeasurementDao.kt`
- `data/local/dao/EvidenceDao.kt`
- `data/local/dao/CalculationRunDao.kt`
- `data/local/dao/UnitDao.kt`
- `data/local/dao/EventLogDao.kt`
- `data/local/dao/ProjectDao.kt`

### Domain
- `domain/calculation/UnitCatalog.kt`
- `domain/calculation/UnitConverter.kt`
- `domain/model/Project.kt`
- `domain/repository/ProjectRepository.kt`

### Data services
- `data/service/EventLogger.kt`
- `data/service/BackupService.kt` (réécrit)
- `data/repository/ProjectRepositoryImpl.kt`
- `data/work/UuidBackfillWorker.kt`

### Presentation
- `presentation/navigation/BottomNavDestination.kt`
- `presentation/navigation/MainScaffold.kt`
- `presentation/navigation/ProjectsNavGraph.kt`
- `presentation/screens/common/ComingSoonScreen.kt`
- `presentation/screens/home/HomeScreen.kt`
- `presentation/screens/home/HomeViewModel.kt`
- `presentation/screens/explorer/ExplorerScreen.kt`
- `presentation/screens/projects/ProjectsScreen.kt`
- `presentation/screens/projects/ProjectsViewModel.kt`
- `presentation/screens/projects/ProjectDetailScreen.kt`

### Tests
- `test/.../domain/calculation/UnitConverterTest.kt` (30 tests)
- `test/.../data/service/EventLoggerTest.kt` (8 tests)
- `test/.../data/service/FakeEventLogDao.kt`
- `test/.../data/FakeForetDao.kt`
- `test/.../data/FakeParcelleDao.kt`
- `test/.../data/FakePlacetteDao.kt`
- `test/.../data/FakeTigeDao.kt`

### Fichiers modifiés
- `ForestryDatabase.kt` : version 34→35, 9 nouvelles entités, 8 nouveaux DAO
- `DatabaseMigrations.kt` : `MIGRATION_34_35` (9 nouvelles tables + uuid + provenance)
- `ForetEntity.kt`, `ParcelleEntity.kt`, `PlacetteEntity.kt`, `TigeEntity.kt` : colonne `uuid` + `ProvenanceEmbed`
- `ForetDao.kt`, `ParcelleDao.kt`, `PlacetteDao.kt`, `TigeDao.kt` : méthodes UUID + getAllNow
- `ForestryNavigation.kt` : MainScaffold + 5 routes bottom nav + start destination
- `ForestryCounterApplication.kt` : `projectRepository` ajouté

---

## Décisions clés

1. **UUID strategy** : `legacy_id` + backfill asynchrone (décision Fondateur).
   L'ID existant reste PK ; l'UUID est nullable puis backfillé par
   `UuidBackfillWorker`.

2. **Navigation Lot 1** : anciennes routes Forets/Settings non accessibles
   depuis la bottom bar. Stubs Missions/Carte/Compte affichent "À venir".

3. **BackupService** : utilise les DAO directement (pas les repos) pour
   éviter la dépendance Flow et permettre des snapshots synchrones.

4. **EventLogger** : non-blocant — une erreur d'émission ne doit jamais
   empêcher l'opération métier de réussir.

5. **ProvenanceEmbed** : embedded (pas de table séparée) — les colonnes
   sont préfixées `provenance_` dans la table hôte.

---

## Différé vers Lot 2

- Method Registry (calculs) — référencé par `CalculationRunEntity.method`
- Synchronisation GSIE serveur (Lot 5) — base posée par `EventLogEntity`
- Enrichissement ParcellesScreen/PlacettesScreen avec observations/mesures
  (les entités sont prêtes, l'UI de saisie viendra avec le Method Registry)

---

## Sprint 4.2-4.4 + 5.1-5.2 (ajouté après freeze initial)

### ForestDetailScreen refonte (4.2)

- `ForestDetailViewModel` : charge forêt + parcelles via repos
- `ForestDetailScreen` : 5 onglets (Vue générale, Parcelles, Carte,
  Documents, Historique) avec cards d'info, metadata, et liste parcelles
- Actions : éditer (TODO Lot 2), supprimer

### CreateForestWizard (4.3)

- Wizard 3 étapes : Identité → Propriétaire → Gestion
- Progress bar, validation par étape, création UUID
- `CreateForestViewModel` : form state, validation, save

### CreateParcelleWizard + CreatePlacetteWizard (4.4)

- `CreateParcelleWizard` : nom, surface, pente, altitude, remarques
- `CreatePlacetteWizard` : nom, type, rayon
- ViewModels avec form state et validation

### Enrichir ParcellesScreen + PlacettesScreen (5.1-5.2)

- FAB des écrans existants maintenant navigue vers les wizards
  (au lieu de créer avec valeurs par défaut)
- Callbacks `onNavigateToCreateParcelle` / `onNavigateToCreatePlacette`
  ajoutés, fallback sur l'ancien comportement si null

### Routes ajoutées

- `Screen.ForestDetail` : `forest/{forestId}`
- `Screen.CreateForest` : `forest/create`
- `Screen.CreateParcelle` : `parcelle/create/{forestId}`
- `Screen.CreatePlacette` : `placette/create/{parcelleId}`
- `forestDetailNavGraph.kt` : sous-graphe dédié
- Method Registry (calculs) — référencé par `CalculationRunEntity.method`
- Synchronisation GSIE serveur (Lot 5) — base posée par `EventLogEntity`
