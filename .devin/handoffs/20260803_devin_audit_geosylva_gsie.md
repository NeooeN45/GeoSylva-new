# Handoff — Audit GeoSylva ↔ GSIE Serveur (2026-08-03)

**Source** : Devin CLI (GLM-5.2 High)
**Destinataires** : ChatGPT 5.6 Luna (conception), Chef / Sous-chef / Fiabilisation (réalisation)
**Objet** : Cartographie complète de l'existant avant améliorations

---

## 1. Contexte

GeoSylva est l'app Android forestière de l'écosystème Quintessences, propulsée par le moteur GSIE. Phase 4 active. Aujourd'hui (03/08) : 5 DEC livrées (000044 à 000048) — identité multi-fournisseurs, cycle compte local, bordure Cloudflare, sync parcelles GeoSylva.

**Principe fondateur** (CLAUDE.md §2) : offline-first. Le serveur GSIE est un **amplificateur de capacité**, jamais une dépendance. Le cœur forestier doit fonctionner sans réseau.

**3 canaux réseau** (CLAUDE.md §3) :
1. GeoSylva ↔ GSIE serveur (Wi-Fi/4G stable) — amplification réflexion
2. Technicien ↔ technicien (Bluetooth) — partage terrain immédiat
3. Technicien ↔ technicien / GSIE (LoRa mesh) — portée longue, bas débit

---

## 2. Connexion GSIE Serveur — état actuel

### 2.1 Authentification ✅

**Stack** : Retrofit 2 + OkHttp 3 + kotlinx.serialization + SecureHttpClient (certificate pinning, SSRF protection, DNS durci)

**Endpoints Identity** (13 endpoints) :
- `GET /api/v1/auth/providers` — découverte fournisseurs
- `POST /api/v1/auth/register` — inscription locale
- `POST /api/v1/auth/login/password` — connexion locale (Argon2id)
- `POST /api/v1/auth/google/nonce` — nonce pour Google OIDC
- `POST /api/v1/auth/login/google` — connexion Google (Credential Manager)
- `POST /api/v1/auth/refresh` — refresh token rotatif
- `POST /api/v1/auth/logout` — déconnexion
- `GET /api/v1/auth/me` — profil courant
- `PATCH /api/v1/auth/me` — modification profil
- `POST /api/v1/auth/email/verification/request` + `/confirm` — vérification e-mail
- `POST /api/v1/auth/password/reset/request` + `/confirm` — récupération mot de passe
- `GET /health` + `GET /ready` — diagnostic développeur

**Fichiers clés** :
- `data/remote/identity/IdentityApiService.kt` (175 lignes) — interface Retrofit
- `data/remote/identity/IdentityApiFactory.kt` (38 lignes) — factory
- `data/remote/identity/EncryptedIdentitySessionStore.kt` (60 lignes) — stockage AES-256
- `data/remote/identity/JwtSessionDecoder.kt` (45 lignes) — décodage métadonnées JWT
- `data/remote/identity/GoogleCredentialClient.kt` (51 lignes) — Credential Manager
- `data/repository/IdentityRepositoryImpl.kt` (315 lignes) — logique auth + refresh 401
- `domain/model/IdentityModels.kt` (85 lignes) — modèles domaine

**Stockage tokens** : EncryptedSharedPreferences (AES256-GCM), fichier `quintessences_identity_session`. Pas de DataStore pour les tokens.

**Refresh 401** : un seul refresh, puis erreur permanente si le refresh échoue.

### 2.2 Synchronisation parcelles (push) ✅

**Endpoints Sync** (2 endpoints) :
- `PUT /api/v1/sync/geosylva/parcelles/{clientId}` — upsert
- `DELETE /api/v1/sync/geosylva/parcelles/{clientId}` — suppression

**Architecture file** :
- Table Room `parcel_sync_queue` (SQLCipher, migration 32→33)
- WorkManager `ParcelSyncWorker` (CoroutineWorker)
- Idempotence : `operationId` UUID par opération
- Version optimiste : `baseVersion` envoyé au serveur
- États : PENDING, SYNCING, SYNCED, CONFLICT, ERROR
- Retry exponentiel : 15s min → 1h max
- Batch size : 50 éléments
- Conflit 409 → état CONFLICT, `nextAttemptAt = Long.MAX_VALUE` (jamais relancé auto)
- Activation explicite par compte (EncryptedSharedPreferences)

**Fichiers clés** :
- `data/sync/ParcelSyncRepositoryImpl.kt` (359 lignes) — logique sync
- `data/sync/ParcelSyncContract.kt` (106 lignes) — classification erreurs HTTP
- `data/sync/ParcelSyncApiService.kt` (24 lignes) — interface Retrofit
- `data/sync/ParcelSyncApiFactory.kt` (30 lignes) — factory
- `data/sync/ParcelSyncActivationStore.kt` (38 lignes) — activation explicite
- `data/work/ParcelSyncWorker.kt` (33 lignes) — WorkManager
- `data/local/dao/ParcelSyncDao.kt` (117 lignes) — DAO
- `data/local/entity/ParcelSyncEntity.kt` (35 lignes) — entity

**Classification erreurs** (`ParcelSyncContract.kt`) :
- 401 → REFRESH_SESSION
- 409 → CONFLICT
- 408, 425, 429, 5xx → RETRY
- Autre → PERMANENT_ERROR

### 2.3 Configuration serveur

- `GSIE_API_BASE_URL` : via `local.properties` ou env var, défaut = chaîne vide
- Debug : accepte `http://localhost:8000`, `http://10.0.2.2:8000` (émulateur)
- Release : HTTPS publique obligatoire (vérifié par `SecureHttpClient.isSafeRemoteHttpsUrl`)
- `GOOGLE_WEB_CLIENT_ID` : via `local.properties` ou env var

### 2.4 Diagnostic développeur ✅

`presentation/screens/account/DeveloperOptionsScreen.kt` (342 lignes) :
- État connexion API (CONNECTED/DEGRADED/UNREACHABLE/NOT_CONFIGURED)
- Latence, version, environnement, dépendances
- Résumé sync parcelles (pending, syncing, synced, conflicts, errors)
- Bouton "Synchroniser toutes les parcelles"
- Infos build et device

### 2.5 Manques identifiés (connexion)

| Manque | Impact | Fichiers à toucher |
|---|---|---|
| **Pull serveur→mobile** | Sync unidirectionnelle seulement | `ParcelSyncApiService`, `ParcelSyncRepositoryImpl`, nouveau `PullWorker` |
| **Résolution conflits** | Conflits 409 détectés mais bloqués | Nouvel écran `ConflictResolutionScreen`, `ParcelSyncRepositoryImpl` |
| **Tombstones** | Suppressions locales déjà syncées non marquées | `ParcelSyncEntity`, `ParcelSyncDao` |
| **Tests E2E auth/sync** | Pas de mock serveur GSIE | Nouveau `MockGsieServer`, tests instrumentés |
| **Tests intégration IdentityRepository** | Login/refresh/logout non testés E2E | `app/src/test/` ou `androidTest/` |

---

## 3. Logique interne — état actuel

### 3.1 Architecture

Clean Architecture 3 couches : `domain/` (modèles, interfaces, use cases, calculateurs) → `data/` (Room, remote, repositories impl, sync) → `presentation/` (Compose, ViewModels, navigation).

**DI manuelle** — pas de Hilt. `ForestryCounterApplication.kt` (329 lignes) initialise 19 repositories + services + use cases + calculateurs à la main. Pattern Factory pour Identity et ParcelSync.

### 3.2 Base de données Room

- **Version** : 33 (migration 32→33 = table `parcel_sync_queue`)
- **Tables** : 32 (GroupEntity, CounterEntity, FormulaEntity, ParcelleEntity, PlacetteEntity, EssenceEntity, TigeEntity, IbpEvaluationEntity, ForetEntity, StationEntity, DiagnosticSylvicoleEntity, etc.)
- **Chiffrement** : SQLCipher v4.5.4, clé via Android Keystore (`DatabaseEncryptionService`)
- **Migrations** : MIGRATION_1_2 à MIGRATION_32_33, migrations externalisées (Migration15to27, 27to28, 28to29, 29to30, 30to31, 31to32)
- **Fichier** : `data/local/ForestryDatabase.kt`

### 3.3 Moteur de calcul forestier ✅

**Coefficients sourcés** (conforme ADR-007) :
- **Schaeffer 1 entrée** (16 tarifs) : V = a + b×C² — Schaeffer 1949, Annales ENS Forêts Nancy
- **Schaeffer 2 entrées** (8 tarifs) : V = a + b×C²×H — Schaeffer 1949, tables ONF
- **Algan** (40+ essences) : V = a×D^b×H^c — Algan 1958, ajustés Pardé & Bouchon 1988, IFN
- **IFN rapides** (36 tarifs) : V = a₀ + a₁×D + a₂×D² — Documentation technique IFN, IGN
- **Schumacher-Hall** (28 essences) : V = exp(a + b·ln(D) + c·ln(H)) — Vallet et al. 2006, Revue Forestière Française LVIII(5):481-496

**Fichiers** :
- `domain/calculation/ExpertForestryCalculator.kt` — Hdom, IS, Richards, Schumacher-Hall
- `domain/calculation/tarifs/TarifData.kt` (624 lignes) — tous les coefficients
- `domain/usecase/sylviculture/SylvicultureDatabase.kt` (672 lignes) — 28 essences + alias ABAL→ABBA

### 3.4 Use cases métier

| Module | Fichier principal | Lignes | État |
|---|---|---|---|
| **Brain** | `LocalBrainCore.kt` | 317 | ✅ orchestrateur offline, FTS flore, cache GPS |
| **Correlateur** | `CorrelationEngine.kt` | 547 | ✅ corrélation GPS/flore/station/ripisylve, score 0-1 |
| **Florist** | `FloristDatabase.kt` + extensions | — | ✅ Flora Gallica, Ellenberg, Landolt |
| **Fertility** | `FertilityClassifier.kt` | 200 | ✅ Hdom, Lorey, zone bioclimatique, confiance |
| **Autecology** | `AutecologyStubs.kt` | 456 | ⚠️ ~30 essences, fallback générique |
| **Station** | `StationDiagnosticEngine.kt` | 398 | ✅ GPS + dendro + pédologie + botanique |
| **Ripisylve** | `RipisylveScorer.kt` | 53 | ✅ continuité, largeur, strates, diversité |
| **Export** | `ExportDataUseCase.kt` + 8 exporters | — | ✅ CSV, XLSX, JSON, ZIP, Shapefile, GeoJSON, PDF, QGIS |

### 3.5 Écrans Compose

60+ écrans, 6 graphes de navigation (Diagnostic, ForestryFlow, Ibp, Onboarding, Settings, + Account).

Parcours utilisateur :
1. Onboarding → découverte
2. Login/Account → création compte (local + Google)
3. Dashboard → vue d'ensemble
4. Parcelles/Placettes → inventaire forestier
5. Martelage → cubage et martelage
6. Map → cartographie terrain (MapLibre GL, 12 couches)
7. Diagnostics (Station, Ripisylve, IBP) → diagnostics experts
8. Settings → configuration + diagnostic développeur

### 3.6 Tests

| Type | Fichiers | Tests | Couverture |
|---|---|---|---|
| Unitaires | 37 fichiers | ~450+ tests | ~35% |
| Instrumentés | 3 fichiers | ~15 tests | — |
| Migration Room | 1 fichier | 1 test | — |

**Tests auth/sync** (4 fichiers, 21 tests) :
- `JwtSessionDecoderTest.kt` (3 tests)
- `ParcelSyncMapperTest.kt` (1 test)
- `ParcelSyncPolicyTest.kt` (3 tests)
- `SecureHttpClientTest.kt` (14 tests)

### 3.7 Manques identifiés (logique interne)

| Manque | Impact | Priorité |
|---|---|---|
| **DI manuelle (329 lignes)** | Maintenance difficile, testabilité limitée | Moyenne |
| **Tests Room insuffisants** | 1 test migration, pas d'in-memory DB | Haute |
| **Tests UI faibles** | 3 tests instrumentés seulement | Haute |
| **AutecologyStubs incomplète** | ~30 essences, fallback générique | Moyenne |
| **Fichiers monolithiques** | MartelageScreen.kt >2000 lignes | Basse |
| **CorrelationEngine peu testé** | 547 lignes, logique complexe | Haute |
| **Pas de tests perf** | Risque lenteur gros datasets | Basse |

---

## 4. État Git — commits non poussés

### Repo parent (Quintessences)
**Branche** : `feat/schemas-de-domaine` — **5 commits ahead** :
- `b0c8402` feat(api): synchronisation parcelles GeoSylva
- `6121baa` feat(securite): bordure Cloudflare
- `278a74c` feat(api): cycle compte Quintessences
- `07659db` feat: doc + tests + datasets
- `215a251` feat(api): identité multi-fournisseurs

**PR #2** : https://github.com/NeooeN45/Quintessences/pull/2 (CI 11/11 verte, mergeable CLEAN)

### Repo GeoSylva
**Branche** : `fix/enterprise-reliability-2026-07-21` — **3 commits ahead** :
- `7788bba` feat(geosylva): synchronise parcelles avec GSIE
- `c6773b3` docs(securite): documente bordure Cloudflare
- `d7d684a` feat(geosylva): complète gestion compte Quintessences

**⚠️ Action requise** : ces commits ne sont pas poussés sur origin. Le Fondateur doit autoriser le push.

---

## 5. Points d'entrée pour les agents de réalisation

### Pour Luna (conception)
- Principe offline-first à respecter absolument (CLAUDE.md §2)
- 3 canaux réseau distincts, ne pas confondre (CLAUDE.md §3)
- Garde-fou scientifique ADR-007 : coefficients sourcés (CLAUDE.md §4)
- Ne pas dupliquer la science en dur — à terme via Knowledge Engine GSIE

### Pour Chef / Sous-chef (réalisation)
- DI manuelle dans `ForestryCounterApplication.kt` — ajouter nouveaux repositories ici
- DB Room v33 — nouvelle table = migration 33→34 + test migration
- SecureHttpClient partagé entre Identity et ParcelSync — réutiliser pour nouveaux endpoints
- Pattern Factory : `IdentityApiFactory`, `ParcelSyncApiFactory` — réutiliser pour nouveaux services API

### Pour Fiabilisation (tests)
- Pas de mock serveur GSIE — à créer pour tests E2E
- Tests Room : utiliser Robolectric ou in-memory database
- Tests Compose : `createAndroidComposeRule` pour parcours critiques
- Harnais de tests existant : 37 fichiers, ~450 tests, couverture ~35%

---

## 6. Métriques de référence

| Métrique | Valeur |
|---|---|
| Tables Room | 32 (v33) |
| Écrans Compose | 60+ |
| Use cases | 10 modules |
| Calculateurs | 4 (Forestry, Expert, PeuplementAvantCoupe, Price) |
| Tarifs cubage | 100+ (Schaeffer, Algan, IFN) |
| Essences Schumacher-Hall | 28 (Vallet 2006) |
| Tests unitaires | ~450+ |
| Tests instrumentés | 3 |
| Endpoints Identity | 13 |
| Endpoints Sync | 2 |
| Commits non poussés (parent) | 5 |
| Commits non poussés (GeoSylva) | 3 |

---

*Handoff généré par Devin CLI le 2026-08-03. Source : exploration subagents + AI_CONTEXT.md + MASTER_PLAN.md + CLAUDE.md.*
