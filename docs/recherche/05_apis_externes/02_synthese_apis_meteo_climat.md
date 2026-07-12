# Synthèse comparative — APIs météo/climat pour l'intégration Android GeoSylva

**Domaine** : docs/recherche/05_apis_externes/
**Date de recherche** : 2026-07-03
**Agent** : apis-externes-02-synthese-meteo

> Ce document consolide et synthétise les 5 fiches de la vague 3
> (`docs/recherche/03_climat/01` à `05`) du point de vue intégration Android.
> Il ne duplique pas le détail des tests/payloads (voir les fiches sources),
> mais produit une vue opérationnelle unique : tableau comparatif, architecture
> recommandée, variables minimales pour le diagnostic stationnel.

---

## 1. Sources consolidées

| API / Source | Type | Fiabilité | URL | Date/version | Fiche source |
|--------------|------|-----------|-----|--------------|--------------|
| Météo-France API officielle Open Data | Officielle | officielle | https://public-api.meteofrance.fr | Ouverture 01/01/2024 | `03_climat/01` |
| Météo-France API mobile (non documentée) | Tierce (reverse-eng.) | commerciale/tierce | https://webservice.meteofrance.com | App officielle MF | `03_climat/01` |
| DRIAS-2020 (Météo-France/CERFACS/IPSL) | Officielle | officielle | https://www.drias-climat.fr | 2020 (RCP/AR5) | `03_climat/02` |
| TRACC (doctrine adaptation FR) | Officielle | officielle | https://www.ecologie.gouv.fr | Giec France | `03_climat/02` |
| Copernicus ERA5 (CDS) | Officielle (UE) | officielle | https://cds.climate.copernicus.eu/datasets/reanalysis-era5-single-levels | DOI 10.24381/cds.adbb2d47 | `03_climat/03` |
| Copernicus ERA5-Land (CDS) | Officielle (UE) | officielle | https://cds.climate.copernicus.eu/datasets/reanalysis-era5-land | — | `03_climat/03` |
| Open-Meteo Forecast API | Tierce (open) | commerciale/tierce | https://api.open-meteo.com/v1/forecast | — | `03_climat/04` |
| Open-Meteo Archive API (proxy ERA5) | Tierce (open) | commerciale/tierce | https://archive-api.open-meteo.com | Données ERA5 | `03_climat/04` |
| Open-Meteo Climate API (CMIP6) | Tierce (open) | commerciale/tierce | https://climate-api.open-meteo.com | SSP/AR6 | `03_climat/04` |
| data.gouv.fr ETP Safran Hargreaves 8 km | Officielle | officielle | https://www.data.gouv.fr | Licence Ouverte 2.0 | `03_climat/05` |
| Climessences V2 (CNPF/INRAE) | Officielle | officielle | https://climessences.fr | V2 (pas d'API) | `03_climat/05` |
| INRAE BILJOU | Scientifique | scientifique | https://www.gip-ecofor.org/biljou | Accès restreint | `03_climat/05` |

---

## 2. Tableau comparatif unique des APIs météo/climat

| API | URL de base | Clé requise | Granularité spatiale | Pas de temps | Licence | Appel Android direct | Backend requis | Cas d'usage GeoSylva | Priorité d'intégration |
|-----|-------------|-------------|----------------------|--------------|---------|----------------------|----------------|----------------------|------------------------|
| **Open-Meteo Archive** | `archive-api.open-meteo.com` | ❌ Non | ~9 km (ERA5) / 31 km (ERA5) | Horaire → quotidien | Fair use (non-clé) | ✅ **Oui (déjà intégré)** | ❌ Non | Normales 30 ans, ET₀, soil moisture, série historique par point GPS | **P0** (déjà en place, à étendre) |
| **Open-Meteo Forecast** | `api.open-meteo.com/v1/forecast` | ❌ Non | ~9-31 km | Horaire → quotidien (7 j) | Fair use | ✅ Oui | ❌ Non | Prévisions terrain court terme (chantier, sécurité) | P1 |
| **Météo-France officielle** | `public-api.meteofrance.fr` | ✅ OAuth2 Bearer | Commune INSEE / station / département | Quotidien + horaire | Etalab-like (gratuit) | ⚠️ Via proxy backend (token non embarquable) | ✅ Oui (OAuth2) | Vigilance officielle FR, Météo des forêts (danger feux), obs temps réel | **P1** (apport unique : vigilance + feux) |
| **Météo-France mobile** | `webservice.meteofrance.com` | ⚠️ Token statique non officiel | Commune INSEE | 14 j daily + horaire | Non documentée | ✅ Oui (POC) | ❌ Non | POC vigilance + prévisions sans friction d'inscription | P1-POC (jamais production) |
| **ERA5 / ERA5-Land (CDS)** | `cds.climate.copernicus.eu` | ✅ CDS personnelle | 31 km (ERA5) / **9 km (ERA5-Land)** | Horaire | CC-BY 4.0 | ❌ **Non** (Python-only, asynchrone, GRIB/NetCDF, clé = faille) | ✅ **Obligatoire** (Python/FastAPI) | Normales 30 ans robustes, humidité sol 4 couches, série 1950→ | P2 (backend moyen terme) |
| **DRIAS-2020** | `www.drias-climat.fr` | ✅ Compte web (téléchargement) | SAFRAN 8 km (FR) | Journalier 1950-2100 | Accès libre (compte) | ❌ **Non** (NetCDF, pas d'API REST) | ✅ Oui (pré-traitement) | Projections RCP 2.6/4.5/8.5 par SER, bilan hydrique futur | P2 (deltas déjà embarqués, à re-sourcer) |
| **Open-Meteo Climate (CMIP6)** | `climate-api.open-meteo.com` | ❌ Non | ~25 km mondial | Quotidien | Fair use | ✅ Oui | ❌ Non | Projections SSP (AR6) par point GPS — alternative légère à DRIAS | P2 (si bascule AR6/SSP) |
| **TRACC** | (doctrine, pas d'API) | — | National | Horizons 2030/2050/2100 | Officielle | N/A (embarqué en dur) | ❌ Non | Communication utilisateur (« +2 °C à 2030 ») | P2 (vue UI dans `StationDiagnosticScreen`) |
| **data.gouv.fr ETP Safran** | `www.data.gouv.fr` | ❌ Non (téléchargement) | SAFRAN 8 km | Mensuel | Licence Ouverte 2.0 | ❌ Non (fichier bulk) | ✅ Oui (pré-traitement) | ETP Hargreaves mensuelle pour DHYa (alternative à Open-Meteo) | P2 (si backend) |
| **Climessences / BILJOU** | `climessences.fr` / `gip-ecofor.org/biljou` | ✅ Compte (login) | Par espèce / station | — | Pas d'API | ❌ Non (pas d'API) | ❌ Non | Seuils DHYa par essence (extraction manuelle) | P3 (extraction manuelle, pas d'intégration API) |

**Lecture rapide** :
- **Appel Android direct sans clé** : Open-Meteo (Archive/Forecast/Climate) — le seul viable en zero-config.
- **Appel Android via proxy backend** : Météo-France officielle (OAuth2), ERA5-Land (CDS), DRIAS (NetCDF), Safran data.gouv.fr.
- **Jamais en appel direct** : ERA5/ERA5-Land (clé CDS = faille de sécurité + async + GRIB).
- **Pas d'API du tout** : DRIAS (NetCDF + compte), Climessences (Drupal + login), BILJOU (accès restreint).

---

## 3. Schéma d'architecture recommandé

```
┌─────────────────────────────────────────────────────────────────────┐
│                    APP ANDIAN (Kotlin / OkHttp)                     │
│                                                                     │
│  ┌─────────────────────── APPELS DIRECTS (sans clé) ──────────────┐ │
│  │                                                               │ │
│  │  Open-Meteo Archive  ──► Normales 30 ans, ET₀, soil moisture  │ │
│  │  (models=era5/era5-land)    Série historique par point GPS    │ │
│  │                          (déjà dans ClimateContextService.kt) │ │
│  │                                                               │ │
│  │  Open-Meteo Forecast ──► Prévisions 7 j (sécurité chantier)   │ │
│  │                                                               │ │
│  │  Open-Meteo Climate  ──► Projections SSP par point (option)   │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌─────────────────── APPELS VIA BACKEND (proxy) ─────────────────┐ │
│  │                                                               │ │
│  │  Backend Python/FastAPI (clés côté serveur)                   │ │
│  │    ├─ Météo-France OAuth2 ──► Vigilance + Météo des forêts    │ │
│  │    │                          + obs station temps réel        │ │
│  │    ├─ ERA5-Land (cdsapi) ────► Normales 30 ans robustes,      │ │
│  │    │                          humidité sol 4 couches          │ │
│  │    ├─ DRIAS-2020 (NetCDF) ──► Deltas RCP par SER (pré-calcul) │ │
│  │    └─ Safran data.gouv.fr ──► ETP Hargreaves mensuelle 8 km   │ │
│  │                                                               │ │
│  │  → Sert JSON léger à l'app (point GPS / commune INSEE / SER)  │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌─────────────────── DONNÉES EMBARQUÉES (offline) ───────────────┐ │
│  │                                                               │ │
│  │  ProjectionClimatiqueSerData.kt ─► Deltas DRIAS par SER       │ │
│  │  NormalesClimatiques.kt ─────────► T/P annuels + JJA/DJF      │ │
│  │  BioClimaticRiskDatabase.kt ─────► Catalogue qualitatif       │ │
│  │  (à étoffer : 12 mois + seuils DHYa extraits manuellement)    │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.1 Quelles APIs appelées directement depuis l'app Android

| API | Rôle | Raison |
|-----|------|--------|
| **Open-Meteo Archive** (`models=era5`) | Normales climatiques + ET₀ + soil moisture par point GPS | Sans clé, déjà intégré, proxy ERA5 — meilleur compromis mobile |
| **Open-Meteo Forecast** | Prévisions 7 j terrain | Sans clé, latence <1 ms |
| **Open-Meteo Climate** (optionnel) | Projections SSP par point | Sans clé, alternative légère à DRIAS si bascule AR6 |

### 3.2 Quelles APIs nécessitent un backend

| API | Rôle du backend | Raison du backend |
|-----|-----------------|-------------------|
| **Météo-France officielle** | Proxy OAuth2 (token côté serveur, refresh) | Token non embarquable dans l'APK (faille + rotation impossible) |
| **ERA5 / ERA5-Land (CDS)** | Téléchargement cdsapi + pré-calcul climatologie communale | Python-only, async (job+polling), GRIB/NetCDF volumineux, clé CDS = faille |
| **DRIAS-2020** | Pré-traitement NetCDF → deltas par SER | Pas d'API REST, NetCDF, compte requis |
| **Safran data.gouv.fr** | Pré-traitement bulk → ETP Hargreaves mensuelle | Fichier bulk, pas d'API point-à-point |

### 3.3 Ordre d'intégration recommandé (séquence)

| Étape | API / action | Effort | Fichier Kotlin cible | Apport |
|-------|--------------|--------|----------------------|--------|
| **1. P0 — déjà fait, à étendre** | Open-Meteo Archive : ajouter `&models=era5`, ET₀/soil/vent, normale 30 ans (1991-2020) au lieu de 2023 figée, corriger index mois `(i*12)/n` | Faible | `ClimateContextService.kt` | Débloque variables forestières + climatologie robuste |
| **2. P1 — temps réel/alertes** | Météo-France : POC via API mobile (vigilance + Météo des forêts), puis migration proxy backend OAuth2 | Moyen | `ClimateContextService.kt` + nouvel écran vigilance/feux | Apport unique : vigilance officielle FR + danger feux (juin-sept) |
| **3. P1 — diagnostic stationnel** | Implémenter **DHYa Climessences** (Hargreaves + bilan P−ETP mensuel + tampon RUM + boucle 3 ans) en Kotlin natif offline | Moyen | Nouveau `BioclimaticIndicesEngine.kt`, `NormalesClimatiques.kt` (12 mois) | Standard professionnel FR de fait pour choix d'essence sous CC |
| **4. P2 — projections** | Re-sourcer `ProjectionClimatiqueSerData.kt` (RCP pas SSP, ou bascule Open-Meteo Climate SSP) + ajout vue TRACC | Faible-Moyen | `ProjectionClimatiqueSerData.kt`, `StationDiagnosticScreen.kt` | Cohérence scénarios + communication utilisateur claire |
| **5. P2 — backend ERA5-Land** | Backend Python/FastAPI : cdsapi + pré-calcul normales 30 ans + humidité sol 4 couches par commune | Élevé | Nouveau backend + client Kotlin | Source ultime, variables sol détaillées, série 1950→ |
| **6. P2 — backend DRIAS** | Compte DRIAS + téléchargement NetCDF + pré-calcul deltas par SER traçables | Élevé | Backend + `ProjectionClimatiqueSerData.kt` | Deltas sourcés point par point (remplace valeurs en dur) |
| **7. P3 — seuils essence** | Extraction manuelle compte Climessences → table `EssenceBioclimThresholds` (DHYa min/max par essence) | Manuel (pas code) | Nouvelle table seed | Calibrage du DHYa par essence |

**Note architecture** : à la date de recherche, l'existence d'un backend GeoSylva
n'est pas confirmée `[À VÉRIFIER MANUELLEMENT]` (cf. `03_climat/03` §5). Les étapes
P1 (Météo-France proxy) et P2 (ERA5-Land/DRIAS backend) sont conditionnées par la
mise en place de ce backend. En attendant, l'app peut fonctionner en mode
offline-first avec Open-Meteo + données embarquées (étapes 1, 3, 4, 7).

---

## 4. Variables météo/climat minimales pour le diagnostic stationnel

Le diagnostic stationnel GeoSylva repose sur 3 indices bioclimatiques
(cf. `03_climat/05`) : **DHYa** (priorité absolue), **De Martonne** (déjà présent),
et **bilan hydrique P−ETP**. Voici les variables météo/climat minimales à récupérer
pour alimenter chacun.

### 4.1 Variables requises par indice

| Indice | Variables météo requises | Pas de temps | Source recommandée | Statut GeoSylva |
|--------|--------------------------|--------------|--------------------|------------------|
| **De Martonne** `P/(T+10)` | P annuelle, Tmoy annuelle | Annuel | Open-Meteo Archive (normale 30 ans) | ✅ Déjà calculé (mais sur 2023, pas 30 ans — à corriger) |
| **Bilan P−ETP** | P mensuelle, ETP mensuelle | Mensuel (12 mois) | Open-Meteo Archive ET₀ + cumul mensuel, ou Safran Hargreaves 8 km | ❌ Non implémenté |
| **DHYa (Climessences)** | P mensuelle, ETP mensuelle (Hargreaves), RUM (sol) | Mensuel (12 mois × 3 ans) | Open-Meteo Archive (P, Tmoy/Tmax/Tmin) + Hargreaves calculé côté app + RUM de `04_sol_rhu/` | ❌ Non implémenté (P1 absolue) |
| **ETP Hargreaves** `0,0023·(Tmoy+17,8)·(Tmax−Tmin)^0,5·Ra` | Tmoy, Tmax, Tmin mensuelles + latitude | Mensuel | Open-Meteo Archive (`temperature_2m_mean/max/min`, `models=era5`) | ❌ Non implémenté (Turc présent mais non exploité) |

### 4.2 Liste consolidée des variables minimales à récupérer

| Variable | Unité | Pas de temps | Source prioritaire | Source alternative | Usage |
|----------|-------|--------------|--------------------|--------------------|-------|
| `temperature_2m_mean` | °C | Mensuel (12 mois) | Open-Meteo Archive (`models=era5`) | ERA5-Land backend | De Martonne, DHYa (Tmoy Hargreaves) |
| `temperature_2m_max` | °C | Mensuel | Open-Meteo Archive (`models=era5`) | ERA5-Land backend | DHYa (Tmax Hargreaves) |
| `temperature_2m_min` | °C | Mensuel | Open-Meteo Archive (`models=era5`) | ERA5-Land backend | DHYa (Tmin Hargreaves) |
| `precipitation_sum` | mm | Mensuel (12 mois) | Open-Meteo Archive (`models=era5`) | Safran data.gouv.fr | De Martonne, bilan P−ETP, DHYa |
| `et0_fao_evapotranspiration` | mm | Mensuel (cumul) | Open-Meteo Archive hourly (`models=era5`) | Safran Hargreaves 8 km | Bilan P−ETP (validation Hargreaves) |
| `soil_moisture` (0-7 cm et 7-28 cm) | m³/m³ | Mensuel moyen | Open-Meteo Archive (`models=era5-land`) | ERA5-Land backend (4 couches) | Contexte hydrique (complément DHYa) |
| **RUM** (réserve utile max du sol) | mm | Statique par station | `04_sol_rhu/` (BDGSF + calcul terrain) | SoilGrids 250 m | **Entrée obligatoire DHYa** (tampon bilan hydrique) |
| Rayonnement extraterrestre `Ra` | MJ/m²/j | Calculé (latitude + jour julien) | Formule Hargreaves (calcul local Kotlin) | — | ETP Hargreaves |

### 4.3 Lacunes data identifiées dans GeoSylva (à combler)

| Lacune | Fichier concerné | Action | Priorité |
|--------|------------------|--------|----------|
| `NormalesClimatiques.kt` n'a que T/P annuels + JJA/DJF (pas les 12 mois) | `NormalesClimatiques.kt` | Ajouter 12 mois Tmoy/Tmax/Tmin/P (Open-Meteo normale 30 ans ou Safran) | **P1** (bloquant pour DHYa) |
| De Martonne calculé sur 1 an ERA5 2023 (pas normale 30 ans) | `ClimateContextService.kt` l. 112 | Passer à moyenne 1991-2020 | P1 |
| ETP Turc stockée mais non exploitée dans un bilan hydrique | `NormalesClimatiques.kt` | Trancher Hargreaves (recommandé Climessences) vs Turc (à déprécier ou garder en complément) | P1 |
| Aucun calcul DHYa / bilan P−ETP / Wacussel / Emberger | Nouveau `BioclimaticIndicesEngine.kt` | Implémenter DHYa + Hargreaves en Kotlin natif offline | **P1 absolue** |
| Seuils DHYa par essence non disponibles (Climessences derrière login) | Nouvelle table `EssenceBioclimThresholds` | Extraction manuelle compte Climessences | P3 (manuel) |
| RUM non calculé à partir des saisies terrain (texture+profondeur+pierrosité) | `EmbeddedSoilService.kt` / nouveau `ComputeRumUseCase` | Voir `04_sol_rhu/03` — combler en priorité haute | P1 (entrée DHYa) |

---

## 5. Recommandation pour GeoSylva (synthèse opérationnelle)

1. **P0 — Étendre Open-Meteo (déjà intégré)** : ajouter `&models=era5` explicite,
   variables ET₀/soil/vent, remplacer 2023 figée par normale 30 ans (1991-2020),
   corriger l'index mois `(i*12)/n` dans `ClimateContextService.kt`. Aucun backend.
2. **P1 — Météo-France pour temps réel/alertes** : POC via API mobile (vigilance +
   Météo des forêts), puis migration proxy backend OAuth2. Apport unique vs
   Open-Meteo = vigilance officielle FR + danger feux (juin-sept).
3. **P1 absolue — DHYa Climessences en Kotlin natif offline** : bilan mensuel
   P−ETP + tampon RUM + boucle 3 ans + ETP Hargreaves. Standard professionnel FR
   de fait. Étoffer `NormalesClimatiques.kt` aux 12 mois. Combiner avec RUM de
   `04_sol_rhu/` (vague 4).
4. **P2 — Projections** : re-sourcer `ProjectionClimatiqueSerData.kt` (RCP pas SSP,
   ou bascule Open-Meteo Climate SSP) + ajouter vue TRACC dans
   `StationDiagnosticScreen.kt` pour une communication utilisateur claire.
5. **P2 — Backend ERA5-Land + DRIAS** : dès qu'un backend GeoSylva existera,
   pré-calculer normales 30 ans robustes (ERA5-Land 9 km, humidité sol 4 couches)
   et deltas DRIAS par SER traçables. En attendant, mode offline-first avec
   Open-Meteo + données embarquées suffit.
6. **P3 — Seuils essence** : extraction manuelle compte Climessences → table
   `EssenceBioclimThresholds` (DHYa min/max par essence). Ne pas crawler
   (pas d'API, login requis).

---

## 6. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Licence commerciale Open-Meteo et quota "fair use"
  exact (limite req/jour pour usage commercial) — cf. `03_climat/04` §5.
- `[À VÉRIFIER MANUELLEMENT]` Flow OAuth2 complet Météo-France officielle
  (inscription + échange token + refresh) non testé — cf. `03_climat/01` §5.
- `[À VÉRIFIER MANUELLEMENT]` Licence exacte ERA5-Land (CC-BY supposé par
  analogie avec ERA5) — cf. `03_climat/03` §5.
- `[À VÉRIFIER MANUELLEMENT]` **Existence d'un backend GeoSylva** — conditionne
  la faisabilité des étapes P1 (proxy Météo-France) et P2 (ERA5-Land/DRIAS).
  À ce jour, app semble client-side uniquement (à confirmer).
- `[À VÉRIFIER MANUELLEMENT]` Disponibilité d'une normale 30 ans via Open-Meteo
  Archive (nécessite 30 ans d'appels ou endpoint dédié ?) — cf. `03_climat/04` §5.
- `[À VÉRIFIER MANUELLEMENT]` Cohérence ET₀ Open-Meteo (ERA5) vs ETP Safran
  Météo-France (8 km) — à comparer sur un point test avant d'adopter.
- `[À VÉRIFIER MANUELLEMENT]` Seuils DHYa par essence derrière compte Climessences
  — extraction manuelle requise (login, export CSV par espèce, compilation).
- `[À VÉRIFIER MANUELLEMENT]` Formule exacte du tampon RUM dans DHYa (règle de
  report mois suivant, plafond RUM) — à confirmer sur documentation Climessences/CNPF.
- `[À VÉRIFIER MANUELLEMENT]` Origine point par point des deltas embarqués dans
  `ProjectionClimatiqueSerData.kt` (étiquetés SSP mais DRIAS-2020 livre du RCP) —
  à retracer et re-sourcer.
- `[À VÉRIFIER MANUELLEMENT]` Cohérence TRACC ↔ RCP : vérifier que les horizons
  TRACC (+2/+2,7/+4 °C) correspondent bien aux RCP 2.6/4.5/8.5 dans la doctrine.
