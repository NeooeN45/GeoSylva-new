# MASTER PLAN — GeoSylva

**Document de référence stratégique et opérationnel**
**Date de création** : 2026-06-29
**Dernière révision factuelle** : 2026-07-01 (re-audit du statut réel des items vs code)
**Statut** : Actif — remplace tous les documents de planification précédents
**Fondateur** : Camil (auto-entrepreneur, Poitiers, Nouvelle-Aquitaine)

> ⚠️ **Note méthodologique (2026-07-01)** : les audits du 2026-06-29 (§2.4, §3) ont été
> comparés au code réel après ~20 commits supplémentaires. **9 des 12 actions "Phase 0"
> étaient déjà résolues** au moment de cette révision (voir §3.2). Ce document reflète
> maintenant l'état vérifié, pas seulement l'état déclaré. Voir `.devin/AGENT_COORDINATION.md`
> pour le protocole qui doit éviter que ce plan se re-périme silencieusement.

---

## 0. LIRE CE FICHIER EN PREMIER

Ce fichier est la **source de vérité unique** pour :
- La vision produit et business
- Le plan d'exécution technique
- Le financement et les aides
- L'écosystème de partenariats
- Les standards de qualité et de test

**Ordre de lecture pour une nouvelle session IA** :
1. Ce fichier (`MASTER_PLAN.md`)
2. `AI_CONTEXT.md` (contexte technique du code)
3. `.devin/AGENT_COORDINATION.md` (protocole multi-agents — **obligatoire si tu coordonnes ou reçois le rapport d'un autre agent**)
4. `RESEARCH_OPPORTUNITIES.md` (opportunités techniques, IA, financement, hardware — 150+ entrées)
5. `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (référentiels professionnels/scientifiques externes : ONF, IGN, CNPF, France Bois Forêt, SAFER, AFNOR — 18 sources, 15 actions priorisées)
6. `AUDIT_FORESTIER_COMPLET.md` (audit vague 1 — DB, calculs, tarifs, foresterie — **daté du 2026-06-29, vérifier le statut réel avant d'agir, voir §3**)
7. `AUDIT_GLOBAL_GEOSYLVA.md` (audit vague 2 — sécurité, GIS, UI, i18n, RGPD, perf — **idem**)
8. `docs/RGPD_AUDIT_REPORT.md` (audit RGPD initial)
9. Le code

**Règle impérative pour toute IA qui met à jour ce document** : ne jamais marquer un item
"FAIT" sur la seule foi d'un rapport texte. Vérifier dans le code (grep/read) avant de
changer un statut. C'est exactement l'erreur qui a rendu ce plan obsolète 2 jours après
sa création.

---

## 1. VISION

### 1.1 Énoncé de vision

> GeoSylva est la **plateforme forestière française** qui remplace le carnet de terrain et Excel par un workflow numérique complet : de la saisie sur le terrain à l'analyse IA, du compas Bluetooth au satellite, du mobile au desktop.

### 1.2 Mission

Fournir aux forestiers français (experts indépendants, coopératives, ONF, CNPF, propriétaires privés) un outil de terrain professionnel, conforme aux standards scientifiques français, respectueux du RGPD, et propulsé par une IA souveraine.

### 1.3 Ambition long terme (3-5 ans)

Devenir le standard de facto pour la gestion forestière de terrain en France, avec :
- **GeoSylva Mobile** (Android + iOS) — saisie terrain
- **QGISIA+** (plugin QGIS desktop) — analyse IA et cartographie
- **Partenariat Pixstart** — monitoring satellite
- **IA forestière française** — Mistral 7B fine-tuné sur données ONF/CNPF
- **Partenariats ONF / CNPF / Mistral AI** — légitimité et distribution

---

## 2. ÉTAT ACTUEL (vérifié 2026-07-01)

### 2.1 Version

- **versionName** : 2.3.0
- **versionCode** : 9
- **DB version** : 32 (était 29 au 2026-06-29 — 3 migrations ajoutées depuis)
- **Kotlin** : 1.9.23 (toujours obsolète, cible 2.1.0 — Phase 1.14 non faite)
- **Compose BOM** : 2024.09.00
- **minSdk** : 26 (Android 8.0)
- **targetSdk / compileSdk** : 35
- **Build/tests vérifiés le 2026-07-01** : `compileDebugKotlin` OK, `testDebugUnitTest` OK — **467 tests, 0 échec, 0 erreur**

### 2.2 Architecture

- **Langage** : Kotlin (JVM 17)
- **UI** : Jetpack Compose + Material 3
- **Persistance** : Room (DB v29, 4 entités commentées — problème KSP)
- **Préférences** : DataStore (non chiffré)
- **Navigation** : Navigation Compose (sealed class Screen, 5 sous-graphes)
- **Network** : OkHttp + SecureHttpClient (cert pinning désactivé)
- **GIS** : Lambert-93 IGN NTG 71, WKT, Shapefile, GeoJSON, DEM SRTM
- **Exports** : CSV, XLSX (Apache POI), JSON, ZIP, Shapefile, GeoJSON, PDF
- **Maps** : MapLibre GL 10.3.1
- **Camera** : CameraX 1.3.3 (clinomètre numérique)
- **GPS** : FusedLocationProviderClient + LocationManager (legacy)

### 2.3 Couverture fonctionnelle

- Inventaire par essence et classe de diamètre (95+ essences)
- 7 méthodes de cubage (Schaeffer 1E/2E, Algan, IFN Rapide/Lent, FGH, coefficient forme)
- Martelage avec synthèse dendrométrique (G, Dg, Lorey, N/ha, V/ha)
- IBP CNPF officiel (10 critères, scoring 0/2/5, max 50 pts)
- Diagnostic station (floristique, gradient hydrique/trophique)
- Diagnostic ripisylve
- Cartographie interactive (12 couches, tuiles offline)
- Clinomètre numérique (capteurs téléphone)
- GPS de précision (moyennage MAD, inverse-variance)
- Export Shapefile/GeoJSON/CSV-XY/QGIS
- Diagnostic sylvicole avec recommandations
- Tables de prix éditables par essence/produit/classe
- Sauvegarde automatique WorkManager
- Onboarding 9 pages

### 2.4 Santé du code

**Scores initiaux (audit 2026-06-29)** vs **re-vérification factuelle (2026-07-01)** —
seuls les domaines avec preuve concrète dans le code ont été remontés, les autres restent
non re-vérifiés en détail (ne pas supposer qu'ils sont restés identiques, juste qu'on n'a
pas encore la preuve du contraire) :

| Domaine | Score 06-29 | Score 07-01 | Statut | Preuve du changement |
|---------|-------|-------|--------|--------|
| Calculs dendrométriques de base | 8.5/10 | non re-vérifié | Conforme | — |
| Système de tarification/prix | 4.5/10 | non re-vérifié | Risque financier | Voir travaux récents "feat(pricing)" — à ré-auditer |
| Intégrité base de données | 5/10 | non re-vérifié | Risque données | DB passée de v29→v32, migrations ajoutées — à ré-auditer |
| Logique forestière domainale | 6.5/10 | non re-vérifié | Approximatif | — |
| Traitement des données (mappers) | 5.5/10 | **amélioré** | En progrès | Refactor repositories→modèles domaine commité le 2026-07-01 (voir `.devin/AGENT_COORDINATION.md` §6) |
| Sécurité / chiffrement / réseau | 5/10 | **~7/10** | Nettement amélioré | SQLCipher actif (`ForestryDatabase.kt:151`), certificate pinning actif (`SecureHttpClient.kt:46-56`), FLAG_SECURE actif (`MainActivity.kt`), injection SQL GeoPackage corrigée (whitelist regex) |
| GIS / géomatique | 7.5/10 | non re-vérifié | Approximatif | `Lambert93.kt` dupliqué toujours présent (1.17 non fait) |
| Présentation / UI / Compose | 6.5/10 | **~7/10** | Amélioré | `collectAsState()` entièrement migré vers `collectAsStateWithLifecycle()` (166 occurrences, 0 restante) |
| Internationalisation (FR/EN) | 4/10 | **~5/10** | Toujours insuffisant | `plurals.xml` créé FR/EN ; mais 71 `SimpleDateFormat` et 53 `€` codés en dur toujours présents |
| Build / CI / Gradle | 6/10 | non re-vérifié | Obsolète | Toujours pas de `.github/workflows/` (CI/CD absent) |
| RGPD / privacy | 3/10 | **~6/10** | Nettement amélioré | `RECORD_OF_PROCESSING_ACTIVITIES.md` créé (7 traitements documentés), page consentement onboarding ajoutée, transferts hors UE documentés — **`PRIVACY_POLICY.md` reste à vérifier en détail (contenu non ré-audité)** |
| Performance / mémoire / batterie | 6.5/10 | non re-vérifié | Risques OOM | Coil toujours absent des dépendances (0.11 non fait) |
| Misc (FormulaParser, WorkManager, DataStore, a11y) | 5.5/10 | non re-vérifié | À corriger | FormulaParser sans limites, DataStore non chiffré, `Flow.first()` migré à 2/103 seulement |
| Couverture de tests | 35% | non re-vérifié | Insuffisant | 467 tests passent (0 échec) mais % de couverture non recalculé |

**Total issues identifiées (2026-06-29)** : 224 (40 CRITICAL, 58 HIGH, 80 MEDIUM, 46 LOW)
**Échantillon CRITICAL re-vérifié (2026-07-01)** : au moins 4/18 CRITICAL de la vague 2
confirmées résolues (S-C1 SQLCipher, S-C2 cert pinning, S-C3 injection SQL, S-H4 FLAG_SECURE).
Les 36 CRITICAL restantes (vague 1 + reste vague 2) n'ont pas été ré-auditées une par une —
ne pas supposer qu'elles sont résolues sans vérification.

Voir :
- `AUDIT_FORESTIER_COMPLET.md` — vague 1 (101 issues, statut 2026-06-29 non rafraîchi)
- `AUDIT_GLOBAL_GEOSYLVA.md` — vague 2 (123 issues, statut 2026-06-29 non rafraîchi)
- `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` — pour la fiabilité scientifique des calculs (cubage, IBP, GRECO)

---

## 3. PLAN D'EXÉCUTION TECHNIQUE

### 3.1 Standards de qualité

**Chaque correction ou feature doit** :
1. Passer `./gradlew testDebugUnitTest` avant et après
2. Passer `./gradlew lint` sans nouvelles erreurs
3. Passer `./gradlew assembleDebug` sans erreur
4. Être testée sur émulateur Android (API 26 + API 35)
5. Être testée sur Samsung S25 Ultra (appareil de référence)
6. Suivre les conventions de `CONTRIBUTING.md` et `global_rules.md`
7. Avoir un commit Conventional Commits (`type(scope): description`)
8. Ne pas introduire de dette technique supplémentaire

**Tests** :
- Tests unitaires : `./gradlew testDebugUnitTest`
- Tests instrumentés : `./gradlew connectedAndroidTest` (émulateur)
- Tests manuels : APK installé sur S25 Ultra, scénario métier complet
- Build release : `./gradlew assembleRelease` (vérifier ProGuard/R8)

### 3.2 Phase 0 — Blocages production (priorité absolue)

> **Objectif** : Rendre l'app déployable en production sans risque juridique ou sécurité.
> **Statut au 2026-07-01** : **9/12 FAIT**, 1 incertain, 2 non faits — voir colonne Statut.
> **Critère de sortie** : 0 CRITICAL restant, build release signé fonctionnel

| # | Action | Domaine | Issue | Effort | Statut 07-01 | Preuve |
|---|--------|---------|-------|--------|--------|--------|
| 0.1 | Activer `kotlin.incremental=true` dans gradle.properties | Build | B-C1 | 0.1j | ✅ FAIT | `gradle.properties:9` |
| 0.2 | Réactiver SQLCipher + clé Android Keystore + migration DB | Sécurité/RGPD | S-C1, R-C2 | 3j | ✅ FAIT | `ForestryDatabase.kt:151` (SupportFactory + DatabaseEncryptionService/Keystore) |
| 0.3 | Activer certificate pinning + corriger SecureTileService | Sécurité | S-C2, R-M4 | 1j | ✅ FAIT | `SecureHttpClient.kt:46-56` (4 domaines pinnés) |
| 0.4 | Valider tableName dans GeoImportParser (whitelist regex) | Sécurité | S-C3 | 0.5j | ✅ FAIT | `GeoImportParser.kt:447-453` |
| 0.5 | Ajouter FLAG_SECURE sur MainActivity | Sécurité | S-H4 | 0.5j | ✅ FAIT | `MainActivity.kt:35-39` |
| 0.6 | Réécrire PRIVACY_POLICY.md (26 PII, base légale, transferts) | RGPD | R-C1 | 1j | ⚠️ INCERTAIN | Le fichier existe mais son contenu n'a pas été relu ligne à ligne — **à vérifier avant de considérer conforme** |
| 0.7 | Ajouter page consentement RGPD dans onboarding | RGPD | R-C3 | 2j | ✅ FAIT | `OnboardingScreen.kt:67,174-177,371-387` (page + bouton Decline + dialog) |
| 0.8 | Créer RECORD_OF_PROCESSING_ACTIVITIES.md | RGPD | R-C4 | 1j | ✅ FAIT | Fichier créé, 7 traitements (T-01 à T-07) |
| 0.9 | Documenter ou supprimer transferts Esri/USA (SCC) | RGPD | R-C5 | 1j | ✅ FAIT | `RECORD_OF_PROCESSING_ACTIVITIES.md:46` |
| 0.10 | Remplacer collectAsState() par collectAsStateWithLifecycle() (50+) | Perf/UI | P-C1, U-H1 | 2j | ✅ FAIT | 0 occurrence `collectAsState()` restante, 166 `collectAsStateWithLifecycle` |
| 0.11 | Downsampling images + intégrer Coil | Perf/UI | P-C2, U-H5 | 2j | ❌ PAS FAIT | Coil absent de `libs.versions.toml`/dépendances |
| 0.12 | Corriger test SecureHttpClientTest (méthode inexistante) | Sécurité | S-H1 | 0.5j | ✅ FAIT | `SecureHttpClientTest.kt` réécrit, toutes méthodes testées existent |

**Reste à faire pour clore la Phase 0** : 0.6 (vérifier réellement `PRIVACY_POLICY.md`) et
0.11 (Coil + downsampling images).

### 3.3 Phase 1 — Corrections rapides high-impact

> **Objectif** : Qualité professionnelle (i18n propre, performance, UX).
> **Durée estimée** : 25 jours-homme
> **Statut au 2026-07-01** : **2/23 FAIT** (9%), 4 partiels, 4 incertains, 13 non faits — la
> Phase 0 a été priorisée à raison (sécurité/RGPD), mais la Phase 1 reste très largement à faire.
> **Critère de sortie** : 0 HIGH restant, i18n fonctionnel, pas de OOM

| # | Action | Domaine | Issue | Effort | Statut 07-01 |
|---|--------|---------|-------|--------|--------|
| 1.1 | Extraire 100+ chaînes FR codées en dur → strings.xml | i18n | I-C1 | 5j | ❌ PAS FAIT (53 `€` codés en dur restants) |
| 1.2 | Remplacer SimpleDateFormat(Locale.FRANCE) → DateFormat | i18n | I-C2 | 1j | ❌ PAS FAIT (71 occurrences `SimpleDateFormat`) |
| 1.3 | Remplacer € codé → NumberFormat.getCurrencyInstance() | i18n | I-C3 | 1j | ❌ PAS FAIT |
| 1.4 | Créer plurals.xml FR/EN | i18n | I-C4 | 1j | ✅ FAIT (`values/plurals.xml` + `values-fr/plurals.xml`, 4 plurals) |
| 1.5 | Compléter 22 chaînes manquantes en français | i18n | I-H1 | 0.5j | ⚠️ INCERTAIN — à revérifier avec `.devin/scratch/check_missing.py` |
| 1.6 | Ajouter key() aux LazyColumn (6 écrans) | UI/Perf | U-H4 | 1j | 🟡 PARTIEL (présent sur Parcelles/Placettes/IbpProjects/Martelage, absent ailleurs) |
| 1.7 | rememberSaveable pour formulaires (3 écrans) | UI | U-H2 | 1j | 🟡 PARTIEL (55 occurrences globales, pas vérifié sur les 3 écrans ciblés précisément) |
| 1.8 | contentDescription sur éléments interactifs | A11y | U-H3 | 2j | 🟡 PARTIEL (349 occurrences, ~100 encore à `null`) |
| 1.9 | Streaming exports (JsonWriter + CSV ligne par ligne) | Perf | P-C3 | 3j | 🟡 PARTIEL (CSV en ligne par ligne fait, export JSON pas encore en streaming `JsonWriter`) |
| 1.10 | Ajouter LIMIT/projection aux requêtes DAO | Perf | P-H3 | 2j | ⚠️ INCERTAIN — audit DAO non fait |
| 1.11 | Flow.first() → withTimeoutOrNull (30+ occurrences) | Perf | P-H2, U-H8 | 1j | ❌ PAS FAIT (2/103 migrés seulement) |
| 1.12 | BackupWorker : injecter DB + contraintes + idempotence | Misc | M-C2, M-H3, M-H4 | 1j | ❌ PAS FAIT (DB toujours créée en dur dans `doWork()`) |
| 1.13 | PriceSyncWorker : SecureHttpClient + timeout + backoff | Misc/Sécu | M-H5, M-H6, R-M3 | 1j | ❌ PAS FAIT (`OkHttpClient` standard, pas `SecureHttpClient`) |
| 1.14 | Mettre à jour build tools (AGP, Kotlin, KSP, Compose BOM) | Build | B-H1, B-H2 | 3j | 🟡 PARTIEL (Compose BOM à jour, AGP 8.2.2 et Kotlin 1.9.23 toujours obsolètes) |
| 1.15 | BlurView 2.0.5 → 3.2.0 | Build | B-H3 | 0.5j | ❌ PAS FAIT (version custom 2.0.6) |
| 1.16 | Créer workflow CI/CD release.yml | Build | B-H4 | 1j | ❌ PAS FAIT (aucun `.github/workflows/` — **CI/CD totalement absent, y compris tests/lint automatiques**) |
| 1.17 | Supprimer Lambert93.kt (unifier sur Lambert93Converter.kt) | GIS | G-M1 | 0.5j | ❌ PAS FAIT (duplication toujours présente) |
| 1.18 | Restaurer GeoPackageExporter (427 lignes, export OGC QGIS) | GIS/Export | APK-v2.1 | 3j | ❌ PAS FAIT |
| 1.19 | Restaurer AutecologyExpansion dans AutecologyStubs.kt | Domain | APK-v2.1 | 1j | ❌ PAS FAIT |
| 1.20 | Restaurer TappedDiagnosticInfo dans MapScreen | UI | APK-v2.1 | 1j | ❌ PAS FAIT |
| 1.21 | Restaurer ReferenceMode dans IbpDiagnosticScreen | IBP | APK-v2.1 | 0.5j | ❌ PAS FAIT |
| 1.22 | Évaluer EcologyFertilityTab vs DiagnosticMenu (restaurer ou confirmer remplacement) | UI | APK-v2.1 | 0.5j | ⚠️ INCERTAIN — fichiers non retrouvés, décision à formaliser |
| 1.23 | Évaluer CampaignData (historique campagnes martelage — restaurer si utile) | Domain | APK-v2.1 | 0.5j | ❌ PAS FAIT |

**Recommandation immédiate** : traiter en priorité 1.16 (CI/CD absent = aucun filet de
sécurité automatique sur les prochains commits des agents) et 1.1-1.3 (i18n, cohérent avec
le score encore faible 4→5/10).

### 3.4 Phase 2 — Consolidation

> **Objectif** : Standard "qualité ONF".
> **Durée estimée** : 30 jours-homme
> **Critère de sortie** : 0 HIGH/MEDIUM critique restant, pagination, tests 60%+

| # | Action | Domaine | Effort |
|---|--------|---------|--------|
| 2.1 | Implémenter Paging 3 pour grandes listes | Perf | 5j |
| 2.2 | Droit à l'effacement centralisé ("Effacer mes données") | RGPD | 2j |
| 2.3 | Politique de rétention + suppression automatique | RGPD | 3j |
| 2.4 | Documenter décision automatisée + avertissement UI | RGPD | 1j |
| 2.5 | Désigner DPO + coordonnées dans politique | RGPD | 0.5j |
| 2.6 | Version catalog libs.versions.toml | Build | 2j |
| 2.7 | Décommenter KSP Room args (schema export, incremental) | Build | 0.5j |
| 2.8 | Narrow ProGuard keep rules | Build | 1j |
| 2.9 | Jacoco coverage reporting | Build | 1j |
| 2.10 | Migrer Accompanist → Compose platform | Build | 2j |
| 2.11 | Timeout sur Flow GPS | Perf | 0.5j |
| 2.12 | Remplacer LocationManager par FusedLocationProviderClient (3 écrans) | Perf | 1j |
| 2.13 | Clustering marqueurs GPS sur carte | Perf | 3j |
| 2.14 | Détecteur root/debug | Sécurité | 1j |
| 2.15 | Auth biométrique optionnelle | Sécurité | 2j |
| 2.16 | Chiffrer DataStore (EncryptedSharedPreferences) | Sécurité | 1j |
| 2.17 | Sanitiser logs DEBUG (pas de coordonnées GPS) | Sécurité | 1j |
| 2.18 | ProGuard : supprimer Log.i/w/e en release | Sécurité | 0.1j |
| 2.19 | FormulaParser : limites longueur/complexité/timeout | Misc | 1j |
| 2.20 | Tests FormulaParser : edge cases | Misc | 1j |
| 2.21 | Extraire sous-fonctions DataInterpretationEngine | Misc | 1j |

### 3.5 Phase 3 — Excellence et écosystème

> **Objectif** : Au-delà du standard ONF, préparation de l'écosystème.
> **Durée estimée** : 25+ jours-homme

| # | Action | Domaine | Effort |
|---|--------|---------|--------|
| 3.1 | Algorithme géodésique pour surface polygone (Karney) | GIS | 2j |
| 3.2 | Intégrer PROJ.4 pour reprojection CRS généralisée | GIS | 3j |
| 3.3 | Interpolation DEM bilinéaire/bicubique | GIS | 2j |
| 3.4 | Étendre WktUtils (LINESTRING, POLYGON, SRID) | GIS | 2j |
| 3.5 | Audit accessibilité complet (touch targets, WCAG AA) | A11y | 3j |
| 3.6 | Couverture tests 35% → 60% (domain/business) | Tests | 10j |
| 3.7 | Lint rules pour détecter chaînes codées en dur | i18n | 1j |
| 3.8 | Profiling Android Profiler pour goulots résiduels | Perf | 2j |

### 3.6 Phase 4 — Extension écosystème (post-financement)

> **Objectif** : Transformer GeoSylva en plateforme.
> **Déclencheur** : financement obtenu (French Tech + France 2030 NA + CIR/JEI)

| # | Action | Domaine | Effort |
|---|--------|---------|--------|
| 4.1 | Sync cloud (Supabase/PostgreSQL+PostGIS) | Infra | 10j |
| 4.2 | Version web/desktop (Compose Multiplatform ou PWA) | Platform | 15j |
| 4.3 | Portage iOS (KMP ou Kotlin Multiplatform) | Platform | 20j |
| 4.4 | Intégration compas BLE (Masser/Codimex) | Hardware | 3j |
| 4.5 | Saisie vocale dendrométrique (Vosk FR offline) | IA/UX | 2j |
| 4.6 | IA forestière — API Mistral (assistant martelage) | IA | 5j |
| 4.7 | IA forestière — NVIDIA NIM self-hosted (Mistral 7B) | IA | 5j |
| 4.8 | IA forestière — fine-tuning sur données ONF/CNPF | IA | 10j |
| 4.9 | IA forestière — on-device (Qwen 2.5 3B / SmolLM3) | IA | 5j |
| 4.10 | Pipeline QGISIA+ × GeoSylva (terrain → desktop → terrain) | Écosystème | 10j |
| 4.11 | Partenariat Pixstart (satellite ↔ terrain, API sync) | Partenariat | 5j |
| 4.12 | Marketplace de tarifs (prix du bois par région, MAJ mensuelle) | Business | 5j |

---

## 4. FINANCEMENT ET AIDES

> **Détail exhaustif** : voir `RESEARCH_OPPORTUNITIES.md` §4 (35+ aides, 150+ entrées).
> **Potentiel total** : 350K€ - 1.2M€ sur 24 mois (après passage SASU/EURL) + ~600 000$ crédits cloud gratuits immédiats.

### 4.1 Crédits cloud gratuits (immédiat, auto-entrepreneur OK)

| Programme | Montant | Conditions | Statut |
|---|---|---|---|
| **NVIDIA Inception** | 100K$ AWS + 150K$ Nebius + NIM gratuit + 30% discount GPU | Startup IA, gratuit, pas d'equity | **Postuler maintenant** |
| **Microsoft for Startups** | 150 000$ Azure + support | Startup <5 ans, pas d'equity | **Postuler maintenant** |
| **Google for Startups Cloud** | 350 000$ GCP (AI startups) | Startup <5 ans | **Postuler maintenant** |
| **AWS Activate** | 200 000$ AWS | Startup <10 ans | **Postuler maintenant** |
| **HuggingFace Spaces** | Demo IA gratuite | Compte gratuit | Immédiat |
| **Total crédits cloud** | **~600 000$** | | |

### 4.2 Aides locales Poitiers / Nouvelle-Aquitaine

| Dispositif | Montant | Conditions | Échéance |
|---|---|---|---|
| **Neoloji Technopole** (Poitiers) | Accompagnement gratuit + pépinière 7€/m² | Projet innovant NA | Permanent |
| **France 2030 NA — Projets d'Avenir** | Jusqu'à 50% dépenses | EI/PME/ETI en NA | **30/09/2026** |
| **Région NA — Aide innovation start-up** | Subvention 45% dépenses | Start-up <5 ans NA | Permanent |
| **Région NA — Amorçage Start-Up** | Jusqu'à 3M€ | Start-up <5 ans, 50K€ fonds propres | Permanent |
| **ADEME Santé sols forestiers** | 150-250K€ | Collectifs acteurs forêt | Avril-Juillet 2026 |
| **POP Incub** (ADI NA) | Accompagnement gratuit | ESS/innovation sociale | 02/11/2025 |

### 4.3 Aides nationales

| Dispositif | Montant | Conditions | Échéance |
|---|---|---|---|
| **Bourse French Tech** | 30-50K€ | <1 an, innovation | Permanent |
| **Concours i-Lab** | Jusqu'à 600K€ | <2 ans, deep tech | **Février 2026** |
| **French Tech Seed** (OC) | 50-500K€ | <3 ans, après levée 25K€ | Permanent |
| **Prêt d'amorçage BPI** | Variable | PME <8 ans | Permanent |
| **French Tech Tremplin** | 15K€ + incubation | Conditions sociales | AAP 2026 |

### 4.4 Aides fiscales (après passage SASU/EURL)

| Dispositif | Avantage | Conditions |
|---|---|---|
| **CIR** | 30% dépenses R&D (20% PME depuis 02/2025, plafond 400K€/an) | Agrément CIR |
| **JEI** | Exonération charges + IS | <8 ans, R&D >20% charges (2025), PME |
| **CII** | 20% dépenses innovation (plafond 120K€) | PME, prototype/pilote |

### 4.5 Recommandation critique : passage en SASU/EURL

Le statut auto-entrepreneur bloque **80% des aides financières substantielles**. Le passage en SASU est fortement recommandé :
- **Coût** : ~200-500€ de formalités
- **Débloque** : Bourse French Tech, JEI, CIR/CII pleinement, aides régionales, Concours i-Lab, ADEME
- **Timing** : idéalement avant dépôt des premiers dossiers (mois 2)

### 4.6 Stratégie de financement séquencée

```
Semaine 1-2 : NVIDIA Inception + Microsoft + Google + AWS + HuggingFace
             → ~600 000$ crédits cloud gratuits
    ↓
Semaine 3   : Contact Neoloji Technopole (Poitiers)
    ↓
Semaine 4   : Préparation passage SASU/EURL
    ↓
Mois 2      : Bourse French Tech + Neoloji promo startups
    ↓
Mois 3-4    : France 2030 NA + Région NA aide innovation
    ↓
Mois 5-6    : CIR/JEI rescrit + ADEME sols forestiers (avril 2026)
    ↓
Mois 7-9    : Concours i-Lab (deadline février 2026)
    ↓
Mois 10-12  : Prêt d'amorçage BPI + Bordeaux Angels
    ↓
Mois 13-18  : French Tech Seed + Horizon Europe
```

---

## 5. ÉCOSYSTÈME ET PARTENARIATS

### 5.1 Écosystème produit

```
┌─────────────────────────────────────────────────────────────┐
│                    PLATEFORME GEOSYLVA                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  GeoSylva Mobile          QGISIA+ Desktop                    │
│  (Android + iOS)          (QGIS Plugin + IA)                 │
│  ├── Saisie terrain       ├── Analyse IA                     │
│  ├── Compas BLE           ├── Cartographie avancée           │
│  ├── Saisie vocale        ├── Détection couronnes            │
│  ├── GPS précision        ├── Rapport PSG                    │
│  ├── IBP CNPF             ├── Export vers GeoSylva           │
│  └── Export →             └── ← Import de GeoSylva           │
│                                                              │
│  Pixstart Satellite        IA Forestière                      │
│  ├── Monitoring continu   ├── Mistral 7B fine-tuné           │
│  ├── Alertes dégradation  ├── Assistant martelage            │
│  ├── Vue large échelle    ├── Diagnostic station auto        │
│  └── Calibration terrain  ├── Reconnaissance essence         │
│      ↑↓                    └── Prédiction accroissement       │
│      (API sync GeoSylva)                                     │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  PARTENAIRES : ONF · CNPF · Mistral AI · Masser · Codimex    │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Partenariats ciblés

| Partenaire | Type | Synergie | Approche |
|---|---|---|---|
| **Pixstart** (FR, satellite) | Technique | Satellite ↔ terrain, calibration | Contact direct, complémentarité évidente |
| **Masser** (FI, compas digital) | Hardware | Intégration BLE native | API Bluetooth documentée |
| **Codimex** (IT, compas digital) | Hardware | Intégration BLE native | App Android existe déjà |
| **CNPF** | Standards | Label "compatible CNPF" | Présenter IBP CNPF officiel implémenté |
| **ONF** | Distribution | Traction + légitimité | Via expert ONF consultant ou France 2030 |
| **Mistral AI** | IA | IA forestière française souveraine | Pitch "3M propriétaires + souveraineté" |
| **3Liz / Oslandia** | SIG | Intégration QGIS / PostGIS | Éditeurs SIG français |
| **Coopératives forestières NA** | Marché | Traction utilisateurs | Vente directe |

### 5.3 Technologies open source à exploiter

> **Détail exhaustif** : voir `RESEARCH_OPPORTUNITIES.md` §2 (40+ libraries) et §3 (30+ techno IA).

**IA / LLM** :
- Mistral 7B Instruct (Apache 2.0) — fine-tuning forêt française (QLoRA, GPU 6GB)
- Llama 3.1 8B — via NVIDIA NIM gratuit
- SmolLM3 3B (Apache 2.0) — on-device offline terrain (15 tok/s Samsung S22)
- Qwen 2.5 7B — multilingue compact
- Vosk FR (50MB) — saisie vocale offline
- Android SpeechRecognizer — saisie vocale native (Android 13+)

**Vision — identification essences** :
- PlantNet API (CIRAD/INRAE FR) — 35 000+ espèces
- PureForest (IGN/HuggingFace) — 135 569 patches, 13 essences françaises
- BarkVN-50 — 5 678 images écorce, 50 espèces
- ONNX Runtime Android — inference on-device
- TensorFlow Lite — classification mobile
- MediaPipe — vision temps réel caméra

**GIS / Android** :
- JTS Topology Suite (EPL-2.0) — géométrie vectorielle, WKT/WKB
- Proj4J (EPL-2.0) — transformations CRS (WGS84 ↔ L93 ↔ UTM)
- Spatial K (MapLibre, BSD-3) — GeoJSON, GPX, Turf.js en Kotlin
- GeoPackage Android (Public Domain) — export OGC GeoPackage
- MapLibre GL Android (BSD-2) — cartes vectorielles

**Foresterie / Dendrométrie** :
- LERFoB Forest Tools (LGPL-3.0) — calculs volume/biomasse/carbone français (Bouchon, FrenchCommercialVolume2020)
- CAT Carbon Accounting (LGPL-3.0) — bilan carbone forestier par compartiment

**Bluetooth / Hardware** :
- Kotlin BLE Library (Nordic, MIT) — wrapper coroutines BLE
- Blessed Kotlin (MIT) — BLE compact Android 9+

**Performance / Data** :
- Paging 3 — listes paginées (10k+ tiges)
- Coil — chargement images + cache
- SQLCipher 4.16.0 — chiffrement DB (CRITICAL Phase 0)

**Export** :
- PdfBox Android (Apache 2.0) — génération rapports PDF
- Android GPX Parser (Apache 2.0) — import/export GPX

**Infra cloud** :
- Supabase Kotlin (Apache 2.0) — backend sync cloud (Postgres + Auth + Realtime)
- PostgreSQL + PostGIS — stockage spatial cloud
- Ollama / vLLM / llama.cpp — serving LLM self-hosted
- MinIO — stockage photos S3

**GIS / QGIS desktop** :
- GeoAI plugin — détection arbres IA (DeepForest, SAM3)
- Netflora plugin — inventaire forestier drone + IA
- TreeEyed plugin — monitoring arbres IA
- DeepForest — modèle Python détection couronnes

**Apps forestières inspiration** :
- OpenForis Arena Mobile (FAO, MIT) — workflow collecte moderne
- Geopaparazzi (GPL-3.0) — cartes offline + formulaires terrain
- Forest Sentry (MIT) — IA on-device santé plantes
- GeoVision (MIT) — interface GIS Compose moderne

### 5.4 Hardware IoT forestier

> **Détail exhaustif** : voir `RESEARCH_OPPORTUNITIES.md` §5 (21 devices, plan d'intégration BLE).

**Compas Bluetooth (priorité #1)** :
- Codimex E-1 Caliper (Pologne, ~350€) — abordable, app FR native
- Masser BT Caliper (Finlande, ~1 300€) — référence qualité
- Haglöf Digitech BT (Suède, ~1 600€) — premium, app Haglof Link gratuite

**Hypsomètres laser** :
- Nikon Forestry Pro II (~500€) — manuel, pas de BLE
- TruPulse 200i (~1 200€) — BLE + Classic, doc protocole disponible
- Haglöf Vertex Laser Geo 2 (~3 500€) — tout-en-un laser+ultrason+GPS+compas

**GPS externe** :
- Emlid Reach RX2 (~1 300€) — RTK centimétrique
- Garmin GPSMAP 65s (~450€) — multi-fréquence

**Autres** :
- DJI Mavic 3 Multispectral (~4 800€) — drone NDVI forestier
- FLIR ONE (~400€) — caméra thermique smartphone (stress hydrique)
- Netatmo Weather Station (~180€) — station météo connectée (FR)

**Plan d'intégration** :
```
Mois 1-2 : Acheter Codimex E-1 (350€) + reverse engineering BLE
Mois 3-4 : MVP intégration BLE (Kotlin BLE Library Nordic)
Mois 5-6 : Extension Masser + Haglöf + documentation
Mois 7-8 : GPS externe NMEA 0183
Mois 9-12 : Hypsomètres BLE (TruPulse, Vertex Laser)
```

**ROI** : 30% gain de temps inventaire, 50% réduction erreurs, amortissement 6-12 mois.

---

## 6. BUSINESS MODEL

### 6.1 Modèle hybride (recommandé)

| Offre | Prix | Cible | Revenu |
|---|---|---|---|
| **Licence de base** | 200-400€ perpétuel | Expert indépendant, bureau d'étude | One-shot |
| **Abonnement premium** | 15-30€/mois | Coopérative, gestionnaire | Récurrent |
| **Marketplace tarifs** | 10€/mois | Tous | Récurrent |
| **Services** | Sur devis | Formation, intégration, custom | One-shot |

### 6.2 Projections (réalistes)

| Période | Utilisateurs | CA | Statut |
|---|---|---|---|
| An 1 (beta + lancement) | 50-100 payants | 20 000-40 000€ | Traction initiale |
| An 2 (sync cloud + premium) | 200-300 | 50 000-80 000€ récurrent | Croissance |
| An 3 (web + iOS + IA) | 500-1000 | 100 000-200 000€ récurrent | Scale |

### 6.3 Valorisation (si vente)

- Sans traction : 30 000-80 000€
- Avec 100 utilisateurs payants : 100 000-200 000€
- Avec 500+ utilisateurs + partenariats : 300 000-500 000€

---

## 7. STANDARDS DE DÉVELOPPEMENT

### 7.1 Workflow par correction/feature

1. **Lire** le fichier à modifier + comprendre le contexte
2. **Vérifier** les callers et le contrat
3. **Implémenter** la correction
4. **Tester** : `./gradlew testDebugUnitTest`
5. **Lint** : `./gradlew lint`
6. **Build** : `./gradlew assembleDebug`
7. **Émulateur** : tester sur API 26 + API 35
8. **S25 Ultra** : test manuel scénario métier
9. **Commit** : Conventional Commits (`type(scope): description`)
10. **Documenter** : mettre à jour CHANGELOG.md si feature/fix user-facing

### 7.2 Conventions de code

Voir `CONTRIBUTING.md` et `global_rules.md` :
- Fonctions : max 30 lignes, max 3 paramètres
- Complexité cyclomatique : max 5
- Pas de code commenté, pas de magic numbers
- Pas de `!!` en Kotlin
- Error handling : log avant propager, jamais swallow
- Tests : Arrange → Act → Assert, un assert logique par test
- Commits : `type(scope): description` (max 72 chars)

### 7.3 Tests

| Type | Commande | Fréquence |
|---|---|---|
| Unitaires | `./gradlew testDebugUnitTest` | Après chaque modification |
| Lint | `./gradlew lint` | Avant commit |
| Build debug | `./gradlew assembleDebug` | Avant commit |
| Build release | `./gradlew assembleRelease` | Avant merge main |
| Instrumentés | `./gradlew connectedAndroidTest` | Avant release |
| Manuel S25 Ultra | Installation APK + scénario | Avant release |

### 7.4 Appareil de référence

- **Samsung Galaxy S25 Ultra**
  - Android 15 (API 35)
  - Écran 6.9" QHD+ 144Hz
  - GPS: GPS, GLONASS, Beidou, Galileo, QZSS, NavIC
  - Capteurs: accéléromètre, gyroscope, magnétomètre
  - Caméra: 200MP + téléobjectif
- **Émulateurs** :
  - API 26 (minSdk) — compatibilité
  - API 35 (targetSdk) — courant

---

## 8. PITCHS

### 8.1 Pitch investisseur (BPI, business angels)

> Marché de 3 millions de propriétaires forestiers français sans outil mobile professionnel. GeoSylva est une app Android fonctionnelle couvrant le workflow complet : inventaire, martelage, IBP CNPF, cartographie, exports SIG. 50 utilisateurs payants en beta. Éligible ADEME et France 2030. Recherche 200k€ pour iOS + sync cloud + IA forestière.

### 8.2 Pitch mission (ADEME, ONF, CNPF)

> Outil de terrain pour la gestion forestière durable, conforme aux standards CNPF, IA française souveraine, objectif 100 000 forestiers équipés d'ici 5 ans. Réduire la fracture numérique en foresterie française. Préserver les forêts françaises par une gestion data-driven.

### 8.3 Pitch tech (NVIDIA, Microsoft, Mistral)

> IA forestière française fine-tunée sur données ONF/CNPF, hébergée sur Azure avec NVIDIA NIM, modèle Mistral 7B souverain. Cas d'usage : assistant martelage, diagnostic station, reconnaissance essence. 3M de propriétaires forestiers, marché underserved.

---

## 9. SUIVI ET MÉTRIQUES

### 9.1 KPIs techniques

| KPI | Cible | Actuel (06-29) | Actuel (07-01) |
|---|---|---|---|
| CRITICAL issues | 0 | 40 | ≥4 confirmées résolues (SQLCipher, cert pinning, injection SQL, FLAG_SECURE) sur 40 — 36 non re-vérifiées individuellement |
| HIGH issues | 0 | 58 | non re-vérifié individuellement |
| Tests unitaires | 0 échec | non mesuré | **467 tests, 0 échec, 0 erreur** (`testDebugUnitTest`) |
| Couverture tests | 60%+ | 35% | non recalculée (35% à confirmer) |
| Build time | <2min | ~5min (incremental=false) | `kotlin.incremental=true` actif — build ~6-7min observé (KSP + compile, à profiler) |
| OOM crashes | 0 | Risque élevé | Coil toujours absent — risque inchangé |
| i18n chaînes en dur (€, dates) | 0 | 100+ | 53 `€` + 71 `SimpleDateFormat` codés en dur toujours présents |
| RGPD conformité | Conforme | Non conforme | Nettement amélioré (registre traitements, consentement onboarding, transferts documentés) — `PRIVACY_POLICY.md` reste à vérifier en détail |
| CI/CD | Tests+lint+build auto | Absent | **Toujours absent** — aucun `.github/workflows/`, risque élevé de régression silencieuse pour un projet multi-agents |

### 9.2 KPIs business

| KPI | Cible An 1 | Cible An 2 |
|---|---|---|
| Utilisateurs payants | 50-100 | 200-300 |
| CA | 20-40k€ | 50-80k€ |
| NPS | >40 | >50 |
| Churn | <10% | <5% |

---

## 10. HISTORIQUE DES DÉCISIONS

| Date | Décision | Raison |
|---|---|---|
| 2026-06-29 | Audit complet vague 1 (5 sous-agents) | Évaluer intégrité DB, calculs, tarifs, foresterie |
| 2026-06-29 | Audit complet vague 2 (8 sous-agents) | Évaluer sécurité, GIS, UI, i18n, build, RGPD, perf, misc |
| 2026-06-29 | Suppression de 16 documents faux/périmés | Documents clamaient SQLCipher activé, 91% tests, etc. — tout faux |
| 2026-06-29 | Création MASTER_PLAN.md | Source de vérité unique pour vision + exécution |
| 2026-06-29 | Réécriture AI_CONTEXT.md | Refléter l'état réel du code (v2.3.0, DB v29) |
| 2026-06-29 | Analyse APK v2.1.0 vs code v2.3.0 | 6 classes perdues identifiées (GeoPackageExporter, AutecologyExpansion, EcologyFertilityTab, CampaignData, TappedDiagnosticInfo, ReferenceMode). Code v2.3.0 largement supérieur (+13 entités, +6 repos). Restauration ajoutée Phase 1.18-1.23. |
| 2026-06-29 | Recherche opportunités (5 sous-agents) | 150+ opportunités identifiées : 31 APIs FR (11 intégrées, 20 à intégrer), 40+ libraries OS, 30+ techno IA, 35+ aides financement (~600K$ cloud + 1.2M€ aides), 21 devices IoT. Synthèse dans `RESEARCH_OPPORTUNITIES.md`. |
| 2026-07-01 | Sécurisation de ~35 fichiers non commités (`feature/pro-pricing-engine`) | Refactor repositories→modèles domaine + polish UI, vérifiés build+467 tests OK avant commit (3 commits distincts). Aucune perte de travail. |
| 2026-07-01 | Re-audit factuel de la Phase 0/1 du plan vs code réel | 9/12 items Phase 0 étaient déjà FAITS (SQLCipher, cert pinning, FLAG_SECURE, RGPD onboarding/registre, collectAsStateWithLifecycle...) contrairement à ce que le plan indiquait. Phase 1 très largement non faite (2/23). Voir §2.4, §3.2, §3.3. |
| 2026-07-01 | Création de `.devin/AGENT_COORDINATION.md` | Protocole d'admission des rapports d'agents externes (checklist build/tests/architecture/sécurité/i18n avant tout merge), pour éviter que ce plan (ou le code) ne se re-périme silencieusement. |
| 2026-07-01 | Création de `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` + scaffold `docs/recherche/` | 18 sources officielles/scientifiques (ONF, IGN, CNPF, France Bois Forêt, SAFER, AFNOR NF EN 1316...) pour fiabiliser cubage/prix/IBP/GRECO, avec méthodologie de sourcing pour les recherches futures. |

---

## 11. DOCUMENTS DE RÉFÉRENCE

| Document | Rôle | Statut |
|---|---|---|
| `MASTER_PLAN.md` (ce fichier) | Vision + plan + écosystème | **Actif** (révisé 2026-07-01) |
| `AI_CONTEXT.md` | Contexte technique du code | Actif — **daté 2026-06-29, DB version à corriger (29→32)** |
| `.devin/AGENT_COORDINATION.md` | Protocole d'admission des rapports d'agents externes | **Actif** (créé 2026-07-01) |
| `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` | 18 sources officielles/scientifiques pour fiabiliser cubage/prix/IBP/GRECO | **Actif** (créé 2026-07-01) |
| `docs/recherche/` | Scaffold + méthodologie pour les futures recherches multi-agents sourcées | **Actif** (créé 2026-07-01, dossiers vides à peupler) |
| `RESEARCH_OPPORTUNITIES.md` | Opportunités techniques, IA, financement, hardware (150+ entrées) | Actif (créé 2026-06-29) |
| `AUDIT_FORESTIER_COMPLET.md` | Audit vague 1 (101 issues) | Référence — **statut des items non rafraîchi, voir §2.4/§3 pour les items déjà re-vérifiés** |
| `AUDIT_GLOBAL_GEOSYLVA.md` | Audit vague 2 (123 issues) | Référence — idem |
| `docs/RGPD_AUDIT_REPORT.md` | Audit RGPD initial | Référence |
| `docs/methodes_calcul_volume.md` | Référence technique cubage | Référence |
| `README.md` | Présentation publique | À mettre à jour |
| `CHANGELOG.md` | Historique des versions | Actif |
| `CONTRIBUTING.md` | Standards de contribution | Actif |
| `COMMERCIAL_LICENSE.md` | Licence dual AGPL/commerciale | Actif |
| `PRIVACY_POLICY.md` | Politique de confidentialité | **À vérifier en détail** (Phase 0.6 — fichier existe mais contenu non ré-audité) |

---

*Document maintenu par Camil, fondateur de GeoSylva, avec revue factuelle par IA (build/tests/grep avant toute mise à jour de statut).*
*Dernière mise à jour : 2026-07-01*
