# Synthèse comparative — APIs sol / pédologie / géologie pour GeoSylva

**Domaine** : docs/recherche/05_apis_externes/
**Date de recherche** : 2026-07-04
**Agent** : synthese-apis-sol

> **Note de contexte** : ce document est une **synthèse opérationnelle** qui consolide les
> recherches détaillées de la vague 4 (`docs/recherche/04_sol_rhu/`, 5 fichiers). Il **ne
> duplique pas** les tests d'API ni les tableaux de référence de chaque fichier source — il
> les croise en un **tableau comparatif unique** et un **schéma de chaîne de diagnostic
> stationnel** prêts à guider l'intégration Android. Pour le détail (requêtes curl, réponses
> JSON/GML, tables pédotransfert), se reporter aux 5 fichiers sources listés en §6.

---

## Table des matières

1. [Tableau comparatif unique des APIs sol/géologie](#1-tableau-comparatif-unique-des-apis-solgéologie)
2. [Schéma de chaîne de diagnostic stationnel](#2-schéma-de-chaîne-de-diagnostic-stationnel)
3. [Variables sol minimales à récupérer](#3-variables-sol-minimales-à-récupérer)
4. [Recommandation d'intégration Android pour GeoSylva](#4-recommandation-dintégration-android-pour-geosylva)
5. [Limites et points à vérifier manuellement](#5-limites-et-points-à-vérifier-manuellement)
6. [Sources — renvois vers les 5 fichiers de la vague 4](#6-sources--renvois-vers-les-5-fichiers-de-la-vague-4)

---

## 1. Tableau comparatif unique des APIs sol/géologie

> Sources consolidées : `04_sol_rhu/01_inrae_gissol_bdgsf.md` (BDGSF),
> `04_sol_rhu/02_alternatives_soilgrids_esdac_hwsd.md` (SoilGrids, ESDAC, HWSD),
> `04_sol_rhu/05_geologie_brgm_roche_mere.md` (BRGM). ESDAC/HWSD inclus pour exhaustivité
> comparative bien que non retenus pour intégration (cf. §4).

| API / Source | URL d'accès (endpoint principal) | Clé requise | Résolution spatiale | Variables clés disponibles | Licence | Faisabilité Android direct (live) | Cas d'usage GeoSylva | Priorité GeoSylva |
|---|---|:---:|---|---|---|:---:|---|:---:|
| **BDGSF — RU (INRAE/GisSol)** | WFS : `https://geodata.inrae.fr/geoserver/inra_bdgsf/wfs` (couche `inra_bdgsf:bdgsf_classe_ru`) ; DOI : https://doi.org/10.15454/JPB9RB | Non | 1/1 000 000 (polygones ~km) | **RU en 5 classes** (1:<50, 2:50-100, 3:100-150, 4:150-200, 5:≥200 mm, 9:non-sol) ; attribut `classe` | Licence Ouverte Etalab 2.0 (implicite) | ✅ Oui — WFS GetFeature + BBOX WGS84 (testé, GeoJSON, reprojection auto) | **Référence FR pour la RU** : alimente `rumClasseBdgsf` et le diagnostic hydrique stationnel | **P1** |
| **BDGSF — Sols dominants WRB (INRAE)** | WFS : `.../wfs` (couche `inra_bdgsf:geometrie_bdgsf`) ; DOI : https://doi.org/10.15454/BPN57S | Non | 1/1 000 000 (3516 polygones) | **Type de sol WRB** (`wrbdom` : Luvisol, Podzol, Calcisol…), `soil_id`, `smu` | Licence Ouverte Etalab 2.0 | ✅ Oui — WFS GetFeature + BBOX WGS84 (testé) | Identification du grand type de sol → inférence texture/drainage → aptitude essence | **P1** |
| **BDGSF — Profondeur (INRAE)** | Téléchargement only (DOI : https://doi.org/10.15454/7ZDND6) — **non exposée en WFS** | Non | 1/1 000 000 | Profondeur en 5 classes (`pr-cl` : 30/50/100/150/200 cm) | Licence Ouverte Etalab 2.0 | ❌ Pas en live — à embarquer (shapefile → GeoPackage local) | Qualifie la RU (RU élevée + profondeur ≥150 cm = réserve réellement disponible) | **P2** (embarqué) |
| **BDGSF — Texture/pH/MO** | UTS du dataset complet (téléchargement) / BDAT (synthèses régionales) | Non | 1/1M (texture) ; maille admin/échantillon (pH, MO) | Texture (UTS), pH/CEC/MO (BDAT — non requêtable par point GPS) | Licence Ouverte Etalab 2.0 | ❌ Pas de service web simple par point | Complément régional grossier — ne pas promettre une valeur parcellaire | **P3** (régional) |
| **SoilGrids 2.0 (ISRIC)** | REST : `https://rest.isric.org/soilgrids/v2.0/properties/query?lon={lon}&lat={lat}&property={p}&depth={d}&value=mean` | Non | 250 m raster | pH eau (`phh2o`), texture % (`clay`/`sand`/`silt`), densité apparente (`bdod`), CEC (`cec`), carbone organique (`soc`/`ocs`), eau volumique (`wv0033`/`wv1500`) ; 6 profondeurs (0-5 à 100-200 cm) ; AWC dérivé (`wv0033−wv1500`) | CC-BY 4.0 (commercial OK) | ⚠️ Oui en principe (REST sans clé) — **mais test 2026-07-02 a renvoyé `null` sur points FR** (service partiellement dégradé) `[À RETESTER MANUELLEMENT]` | **Complément haute résolution** : pH/texture/densité absents ou en classes dans BDGSF ; calibration AWC vs classe BDGSF | **P2** (sous condition de retest) |
| **ESDB Raster Library (ESDAC/JRC)** | Téléchargement GeoTIFF après inscription : https://esdac.jrc.ec.europa.eu/node/78 | Inscription requise | 1 km (+ 10 km domaine public) | AWC_TOP/SUB, EAWC_TOP/SUB (classes), texture %, CEC, bulk density (73 attributs PTRDB) | Copyright JRC/ESBN (commercial à vérifier) | ❌ Pas d'API REST — fichiers uniquement | Redondant avec BDGSF pour la France (la BDGSF en est le volet FR) — à considérer seulement si extension Europe | **Exclu (FR)** |
| **HWSD v2.0 (FAO/IIASA)** | Téléchargement uniquement (raster + base MS Access) : https://data.apps.fao.org/catalog/iso/ff5c613c-75bb-46a9-a162-bc728059b465 | Non | ~1 km (30 arc-s) | Texture, CEC, bulk density, SOC, pH, base saturation, AWC dérivée (7 couches, ~30 000 SMU) | **CC BY-NC-SA 4.0** ⚠️ NonCommercial | ❌ Pas d'API REST | Aucune valeur ajoutée vs SoilGrids (plus grossier) + **licence incompatible app commerciale** | **Exclu** |
| **BRGM — Lithologie 1/1M (WFS)** | WFS : `https://geoservices.brgm.fr/geologie?SERVICE=WFS&REQUEST=GetFeature&TYPENAME=LITHO_1M_SIMPLIFIEE&BBOX=...` | Non | 1/1 000 000 | **Roche mère / lithologie dominante** + catégorie (Sédimentaires/Magmatiques) | Licence Ouverte Etalab 2.0 | ✅ Oui — WFS GetFeature + BBOX WGS84 (testé, GML, 2 points FR validés) | **Roche mère** → inférence texture/pH/calcaire/profondeur attendus → aptitude essence (table `GeologyToSoilMapping`) | **P2** |
| **BRGM — Bd Charm-50 (1/50k)** | Téléchargement InfoTerre (vectoriel SHP) : https://infoterre.brgm.fr/page/telechargement-cartes-geologiques | Non | 1/50 000 (~50 m) | Lithologie fine (formations locales : ex. grès de Fontainebleau invisible au 1/1M) | Licence Ouverte Etalab 2.0 | ❌ Pas en live — à embarquer (GeoPackage local, téléchargement par région/dépt) | Diagnostic stationnel fin : capte les formations locales invisibles au 1/1M | **P3** (embarqué, volume à évaluer) |
| **BRGM — WMS (cartes géo.)** | WMS : `https://geoservices.brgm.fr/geologie` (couches `SCAN_H_GEOL50`, `SCAN_F_GEOL250`…) | Non | 50 m à 250 m (images) | Fond de carte géologique (visualisation) | Licence Ouverte Etalab 2.0 | ✅ Oui (GetMap) — **mais non queryable** (GetFeatureInfo rejeté, testé) | Affichage carto géologique en fond (pas d'extraction attributaire live) | **P3** (UI) |
| **CNPF — Catalogues/guides stations (PDF)** | Sites CNPF régionaux + IGN (TypoWeb) | N/A | Région forestière (infra-SER) | Types de station, clés dichotomiques, écogrammes, essences adaptées | Libre (PDF) — droits IDF pour écogrammes | ❌ Pas d'API — numérisation manuelle requise | Référence du diagnostic stationnel expert (clé + fiches + écogramme) | **P2** (numérisation régions pilotes) |
| **Pré-cartographie CNPF Normandie (WMS)** | WMS régional (URL à recenser) | À vérifier | Parcelle (UCS) | Pré-zonage stations (3 facteurs : trophique, RU, hydromorphie) | Libre (régional) | ✅ Oui (WMS) — interrogation par point GPS | Raffinement du pré-diagnostic en Normandie (couverture régionale fragmentée) | **P3** (régions couvertes) |

**Lecture rapide du tableau** :
- **Intégrables en live sur Android sans clé** : BDGSF (RU + WRB), BRGM Lithologie 1/1M, WMS
  BRGM (carto), SoilGrids (sous réserve de retest).
- **À embarquer en local (GeoPackage)** : BDGSF profondeur, Bd Charm-50 1/50k.
- **À exclure** : HWSD (licence NC), ESDB (redondant FR, pas d'API).
- **À numériser manuellement** : catalogues CNPF + écogrammes (travail éditorial, par régions
  pilotes).

---

## 2. Schéma de chaîne de diagnostic stationnel

Chaîne opérationnelle cible : à partir d'un **point GPS** (WGS84, lat/lon), enchaîner les
appels/services pour produire un **pré-diagnostic de station** + une **aptitude d'essence**,
avec confirmation terrain explicite.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 0 — INPUT UTILISATEUR                                                   │
│  Point GPS (lat, lon) + altitude/exposition/pente (capteurs Android)           │
│  + GRECO/SER (déjà intégré : GrecoDetector.kt / SerDetector)                   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 1 — BRGM (roche mère)                                                   │
│  WFS GET https://geoservices.brgm.fr/geologie?SERVICE=WFS&REQUEST=GetFeature   │
│         &TYPENAME=LITHO_1M_SIMPLIFIEE&BBOX={lon-0.01},{lat-0.01},{lon+0.01},    │
│         {lat+0.01},EPSG:4326&MAXFEATURES=3                                     │
│  → parsing GML → lithologie + catégorie                                         │
│  → table GeologyToSoilMapping : roche mère → texture/pH/calcaire/profondeur    │
│    attendus → premières essences candidates/à éviter                            │
│  ⚠ Précision 1/1M (indication régionale) — Bd Charm-50 locale si embarquée     │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 2 — BDGSF (RU + type de sol)                                            │
│  2a. WFS GET .../geoserver/inra_bdgsf/wfs?service=WFS&version=1.0.0            │
│         &request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru                │
│         &maxFeatures=5&outputFormat=application/json&srsName=EPSG:4326         │
│         &BBOX={lon-0.001},{lat-0.001},{lon+0.001},{lat+0.001},EPSG:4326        │
│     → GeoJSON → properties.classe (1-5, 9) → intervalle RU mm                  │
│                                                                                │
│  2b. WFS GET .../wfs?...&typeName=inra_bdgsf:geometrie_bdgsf&...               │
│     → GeoJSON → properties.wrbdom (Luvisol, Podzol, Calcisol…)                 │
│                                                                                │
│  2c. Profondeur : si shapefile embarqué (GeoPackage) → requête spatiale locale  │
│      → pr-cl (30/50/100/150/200 cm). Sinon : saisie terrain (StationObservation)│
│                                                                                │
│  → RU (classe BDGSF) + type WRB + profondeur → qualification hydrique          │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 3 — SoilGrids (complément pH / texture / densité)                       │
│  REST GET https://rest.isric.org/soilgrids/v2.0/properties/query               │
│         ?lon={lon}&lat={lat}                                                    │
│         &property=phh2o&property=clay&property=sand&property=silt              │
│         &property=bdod&property=cec&property=soc                                │
│  → JSON GeoJSON → valeurs par profondeur (mean, Q0.05, Q0.5, Q0.95)             │
│  → pH eau continu, texture % continue, densité apparente, CEC, SOC             │
│  → AWC dérivé : wv0033 − wv1500 intégré sur profondeur racinaire                │
│  ⚠ Service partiellement dégradé (null FR 2026-07-02) — RETESTER.              │
│  ⚠ Si écart AWC SoilGrids > 1 classe vs BDGSF → privilégier BDGSF (expertise FR)│
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 4 — Typologie station CNPF (pré-diagnostic)                             │
│  Croisement des sorties 1-3 + GRECO/SER + altitude/exposition :                │
│    - Restreindre le catalogue régional applicable (SER → guide CNPF)            │
│    - Position approximative sur l'écogramme (NH hydrique × NT trophique) :      │
│        NH ← RU (BDGSF) + profondeur + géologie (BRGM) + pente                   │
│        NT ← pH (SoilGrids) + roche mère (BRGM calcaire/non-calcaire) + WRB      │
│    - Exécuter la clé régionale numérisée (arbre de décision) si observations    │
│      terrain saisies (texture, hydromorphie, humus, calcaire, flore)            │
│  → « station probable : [type(s) candidat(s)] — à confirmer par relevé terrain »│
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 5 — Aptitude essence                                                    │
│  Superposition position station (NH × NT) ↔ aires verte/jaune par essence      │
│  (table EssenceEcogramAreas à constituer, extraite manuellement de la Flore     │
│  forestière française — respecter droits IDF) :                                 │
│    - aire verte = optimum de productivité → aptitude « optimale »               │
│    - aire jaune = amplitude totale → aptitude « possible »                      │
│    - hors aire → aptitude « déconseillée »                                      │
│  + croisement avec exigences stationnelles par essence (RHU optimal, texture,   │
│    pH optimal des fiches 06_essences/) et tableau roche mère → essences (BRGM)  │
│  → liste d'essences classées optimale / possible / déconseillée                 │
│  + préconisations sylvicoles de la fiche station correspondante                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ÉTAPE 6 — Affichage UI (StationDiagnosticScreen)                              │
│  « Pré-diagnostic stationnel (à confirmer par relevé terrain) :                │
│     Roche mère (BRGM 1/1M) : {lithologie}                                       │
│     Type de sol (BDGSF) : {WRB}                                                 │
│     RU (BDGSF) : classe {n} → {intervalle mm}                                   │
│     Profondeur : {classe ou saisie terrain} cm                                  │
│     pH (SoilGrids 250m) : {valeur} ± {incertitude}                              │
│     Texture (SoilGrids) : {clay/sand/silt %}                                    │
│     Station probable : {type(s)}                                                │
│     Essences : optimale(s) / possible(s) / déconseillée(s) »                    │
│  + crédits obligatoires : BRGM, INRAE/GisSol, ISRIC, CNPF/IGN                   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Points clés de la chaîne** :
- **Ordre BRGM → BDGSF → SoilGrids** : la roche mère (BRGM) donne le cadre interprétatif
  (pourquoi tel sol ?), la BDGSF donne la **référence FR validée** pour la RU (variable la
  plus discriminante pour l'aptitude hydrique), SoilGrids apporte les **propriétés
  continues** (pH, texture %) absentes de la BDGSF à l'échelle parcellaire.
- **Hiérarchie de confiance** : saisie terrain (tarière + flore) > BDGSF (expertise FR) >
  SoilGrids (ML global) > BRGM 1/1M (indication régionale). En cas de conflit RU, BDGSF
  prime sur SoilGrids.
- **Aucune étape ne se substitue au relevé terrain** : la chaîne produit un **pré-diagnostic
  à confirmer**, jamais un verdict (cf. `04_sol_rhu/04_typologie_stations_cnpf.md` §8.5).

---

## 3. Variables sol minimales à récupérer

Variables nécessaires et suffisantes pour alimenter (a) le **calcul de RHU/RUM** (cf.
`04_sol_rhu/03_methode_calcul_rhu.md`) et (b) le **diagnostic stationnel** (aptitude
essence). Chaque variable indique sa source prioritaire et son repli.

| Variable | Rôle dans le diagnostic | Source prioritaire | Source de repli | Saisie terrain (override) |
|---|---|---|---|---|
| **RU / RUM (mm)** | Variable hydrique discriminante n°1 ; alimente `soilRumMm`, le calcul de RHU et le DHYa (Climessences) | BDGSF `bdgsf_classe_ru` (classe → intervalle) | SoilGrids `wv0033−wv1500` intégré (calibrer vs BDGSF) | `ComputeRumUseCase` : texture + profondeur + pierrosité (coef U Biljou) |
| **Type de sol (WRB)** | Inférence texture/drainage/calcaire ; oriente le choix essence (Luvisol→feuillus nobles, Podzol→conifères) | BDGSF `geometrie_bdgsf`.`wrbdom` | — | `StationObservation` (observation profil) |
| **Profondeur (cm)** | Plafonne la RU réellement disponible ; distingue RU élevée sur sol profond vs superficiel | BDGSF profondeur (shapefile embarqué, `pr-cl`) | Saisie terrain | `StationObservation.profondeurSolCm` |
| **Texture (clay/sand/silt %)** | Coef U pour calcul RUM ; axe trophique écogramme ; calcaire actif indirect | SoilGrids `clay`/`sand`/`silt` (250m, continu) | BDGSF UTS (téléchargement, classes) | `StationObservation.texture` (enum `TextureSol` 7 classes) |
| **pH eau** | Axe trophique écogramme ; disponibilité nutriments ; calcaire actif indirect | SoilGrids `phh2o` (250m, continu) | BDAT (synthèse régionale, non par point) | `StationObservation` (pH-mètre ou bio-indication flore) |
| **Densité apparente (Da)** | Conversion humidité pondérale → volumique ; calcul RU précis | SoilGrids `bdod` (250m) | Table Biljou (Da par classe texture) | — |
| **Pierrosité (% vol)** | Réduit la RU de la terre fine (facteur 1−EG/100) | SoilGrids `cfvo` (250m) | — | `StationObservation.pierrosite` (enum 5 classes) |
| **Carbone organique (SOC)** | Fertilité minérale, humus, axe trophique | SoilGrids `soc`/`ocs` (250m) | BDAT (régional) | `StationObservation.humus` (forme : mull/moder/mor/anmoor) |
| **CEC** | Capacité d'échange cationique, fertilité minérale | SoilGrids `cec` (250m) | ESDB PTRDB (classes) | — |
| **Roche mère / lithologie** | Cadre interprétatif : texture/pH/calcaire/profondeur attendus ; explication du sol | BRGM `LITHO_1M_SIMPLIFIEE` (WFS live) | Bd Charm-50 1/50k (embarqué) | — |
| **Hydromorphie (profondeur cm)** | Réduit la profondeur prospectable ; exclut les essences craignant l'engorgement | — (pas de couche nationale ouverte par point) | Pré-carto CNPF régionale (si couverte) | `StationObservation.hydromorphieProfondeurCm` |
| **Calcaire actif** | Chlorose (chêne sessile, châtaignier, douglas, pin maritime/sylvestre) | — (pas de couche nationale par point) | Inférence roche mère (BRGM craie/calcaires durs) | `StationObservation` (test HCl effervescence) |
| **Flore (groupes écologiques)** | Position écogramme (NH × NT) ; clé CNPF ; pH bio-indiqué | — (saisie manuelle, reconnaissance auto non fiable) | — | `StationObservation` (liste espèces + Braun-Blanquet) |
| **Topographie (position/exposition/pente)** | Alimentation en eau latérale, confinement, niveau thermique | Capteurs Android (GPS, compas, altimètre) | — | Auto (capteurs) |

**Variables strictement minimales pour un pré-diagnostic sans saisie terrain** (étapes 1-3
de la chaîne) : **RU (BDGSF) + type WRB (BDGSF) + pH (SoilGrids) + texture % (SoilGrids) +
roche mère (BRGM)**. Les autres variables (profondeur, pierrosité, hydromorphie, calcaire,
flore) enrichissent le diagnostic mais exigent une saisie terrain pour fiabilité.

**Variables pour le calcul de RUM côté app** (cf. `04_sol_rhu/03_methode_calcul_rhu.md`
§8.3) : `texture` (TextureSol) + `profondeurSolCm` + `pierrosite` + `hydromorphieProfondeurCm`
→ `computeRumMm()` (coef U × profondeur effective × facteur terre fine). Ce calcul **surcharge**
la valeur IDW de `EmbeddedSoilService` quand la saisie terrain est présente.

---

## 4. Recommandation d'intégration Android pour GeoSylva

### 4.1 Stack d'intégration priorisée

| Priorité | Source | Mode d'intégration | Client Android | Justification |
|:---:|---|---|---|---|
| **P1** | BDGSF RU + WRB (INRAE) | **Live WFS** (BBOX WGS84, GeoJSON) | `BdgsfApiService` (Retrofit + Moshi GeoJSON) | Référence FR pour RU, sans clé, testé OK, licence ouverte |
| **P1** | BRGM Lithologie 1/1M | **Live WFS** (BBOX WGS84, GML) | `GeologyRepository` (Retrofit + parser GML léger) | Roche mère, sans clé, testé OK, licence ouverte |
| **P2** | BDGSF Profondeur | **Embarqué** (shapefile → GeoPackage, requête RTree locale) | `EmbeddedSoilDepthService` (cf. `EmbeddedSoilService.kt`) | Non exposé en WFS ; une seule ingestion |
| **P2** | SoilGrids 2.0 | **Live REST** (point GPS, JSON) — **sous retest** | `SoilGridsApiService` (Retrofit + Coroutines) | pH/texture/densité continus, CC-BY 4.0, mais service dégradé à retester |
| **P2** | Catalogues CNPF + écogrammes | **Numérisation manuelle** (clés → arbre de décision, aires essence → table) | `StationKeyEngine` / `EcogramEngine` / `EssenceEcogramAreas` | Verrou éditorial, par régions pilotes (Normandie, HdF, AURA-Chablais) |
| **P3** | Bd Charm-50 1/50k (BRGM) | **Embarqué** (GeoPackage par région/dépt, téléchargement à la demande) | `GeologyLocalRepository` | Précision fine, volume à évaluer |
| **P3** | WMS BRGM (carto géo.) | **Live WMS** (GetMap, fond de carte) | Service carto existant | Visualisation, non queryable |
| **P3** | Pré-carto CNPF régionale | **Live WMS** (régions couvertes) | Service carto | Raffinement régional, couverture fragmentée |
| **Exclu** | HWSD v2.0 | — | — | Licence CC BY-NC-SA (NonCommercial) incompatible app commerciale |
| **Exclu (FR)** | ESDB ESDAC | — | — | Redondant avec BDGSF (volet FR), pas d'API, licence commerciale incertaine |

### 4.2 Architecture cible (couches domain / data)

- **Domain** : `SoilRepository` (interface), `GeologyRepository` (interface),
  `ComputeRumUseCase`, `StationDiagnosticEngine` (orchestre la chaîne §2),
  `GeologyToSoilMapping` (table roche mère → sol → essences), `EssenceEcogramAreas`.
- **Data** : `BdgsfApiServiceImpl` (Retrofit WFS), `GeologyApiServiceImpl` (Retrofit WFS
  BRGM), `SoilGridsApiServiceImpl` (Retrofit REST), `EmbeddedSoilDepthService`
  (GeoPackage local).
- **Modèle** : `StationEnvironnementale` expose déjà `soilRumMm`, `soilRufMm`,
  `soilProfondeurCm`, `soilTexture`, `pierrositeClassePct`, `rumClasseBdgsf` — à alimenter
  par la chaîne. Ajouter `rocheMere` (String?), `solTypeWrb` (String?), `phEau` (Double?),
  `clayPct`/`sandPct`/`siltPct` (Double?).

### 4.3 Gestion des erreurs et de la fiabilité

- **Échec WFS BDGSF/BRGM** (réseau, serveur) : repli sur `EmbeddedSoilService` (IDW
  existant) + message « pré-diagnostic dégradé (données locales) ».
- **Échec SoilGrids** (null ou timeout) : ignorer pH/texture SoilGrids, garder BDGSF + BRGM
  + saisie terrain. Ne jamais bloquer le diagnostic sur SoilGrids.
- **Conflit RU** (SoilGrids AWC vs BDGSF classe) : si écart > 1 classe, **privilégier
  BDGSF** (expertise FR) et signaler l'incertitude à l'utilisateur.
- **Tous les pré-diagnostics** affichent explicitement « à confirmer par relevé terrain ».

---

## 5. Limites et points à vérifier manuellement

1. **[À RETESTER MANUELLEMENT]** SoilGrids REST API a renvoyé des valeurs `null` sur 2
   points français testés le 2026-07-02 (service partiellement dégradé côté ISRIC). Retester
   via `curl`/Postman (le proxy `webfetch` peut interférer) avant toute intégration ; vérifier
   d'autres coordonnées (hors France) pour isoler le problème.
2. **[À VÉRIFIER MANUELLEMENT]** Licence exacte du flux WMS/WFS GeoDataINRAE (BDGSF) : les
   `GetCapabilities` indiquent `Fees=NONE`/`AccessConstraints=NONE` mais la licence n'est
   pas explicitée dans le XML — présumer Licence Ouverte Etalab 2.0 par héritage du dataset
   source, à confirmer auprès de GisSol pour un usage commercial en production.
3. **[À VÉRIFIER MANUELLEMENT]** Précision 1/1M BRGM insuffisante pour diagnostic fin
   (test Fontainebleau → « Calcaires, marnes » au lieu du grès local). Évaluer le volume du
   GeoPackage Bd Charm-50 1/50k France entière et la faisabilité d'embarquement/téléchargement
   à la demande dans l'APK.
4. **[À VÉRIFIER MANUELLEMENT]** Mapping `TextureSol` (7 classes GeoSylva) → coef U (table
   Biljou 15 classes Jamagne) : proposition de l'auteur sans source officielle de
   correspondance (cf. `04_sol_rhu/03_methode_calcul_rhu.md` §8.2, §9.1). À valider par un
   pédologue forestier (CNPF/IGN).
5. **[À VÉRIFIER MANUELLEMENT]** Droits d'auteur Flore forestière française (IDF) pour
   l'extraction des aires écogramme par essence : préférer une re-numérisation des positions
   (coordonnées normalisées NH/NT) plutôt que la copie des figures ; vérifier la politique
   IDF/CNPF sur l'usage dérivé en app commerciale.
6. **[À VÉRIFIER MANUELLEMENT]** Disponibilité et licence des rasters prédictifs régionaux
   (Gégout massif vosgien, pré-carto Normandie) : la méthodologie est en open access (HAL),
   mais les couches SIG produites ne sont pas nécessairement librement téléchargeables.
7. **[À VÉRIFIER MANUELLEMENT]** Aucune API nationale des stations forestières identifiée —
   toute intégration passe par la numérisation manuelle de PDF régionaux CNPF/IGN ou des
   couches SIG régionales éparses. Confirmer l'absence d'un service national (IGN/CNPF).
8. **Profondeur d'enracinement non standardisée** : les catalogues plafonnent à 80 cm, 100 cm
   ou 2 m selon les sources. Le choix du plafond (100 cm proposé dans
   `04_sol_rhu/03_methode_calcul_rhu.md` §8.3) est conservateur mais sous-estime les sols
   profonds à enracinement > 1 m.
9. **Variables non couvertes par API live** : hydromorphie, calcaire actif, humus, flore —
   aucune couche nationale ouverte par point GPS ; exigent une saisie terrain guidée par
   l'app (fiche de relevés modèle CNPF).

---

## 6. Sources — renvois vers les 5 fichiers de la vague 4

> Ce document est une **synthèse**. Les tests d'API réels (requêtes curl, réponses
> JSON/GML, codes HTTP), les tables de référence détaillées (coef U par texture, classes
> RU, tableau roche mère → essences) et les listes de sources primaires se trouvent dans :

| # | Fichier source | Contenu détaillé référencé |
|---|---|---|
| 1 | `04_sol_rhu/01_inrae_gissol_bdgsf.md` | BDGSF (RU, profondeur, WRB), tests WMS/WFS/CSW réels, variables exploitables, licence |
| 2 | `04_sol_rhu/02_alternatives_soilgrids_esdac_hwsd.md` | SoilGrids 2.0 (REST, variables, test réel null), ESDB ESDAC, HWSD v2.0, comparatif résolution |
| 3 | `04_sol_rhu/03_methode_calcul_rhu.md` | Définitions RU/RUM/RFU/RDU, formules, table pédotransfert Biljou (15 classes), mapping TextureSol, algorithme `computeRumMm`, lien DHYa |
| 4 | `04_sol_rhu/04_typologie_stations_cnpf.md` | Stations/types, GRECO/SER, écogrammes (NH×NT), clés CNPF, pré-cartographie, faisabilité automatisation |
| 5 | `04_sol_rhu/05_geologie_brgm_roche_mere.md` | BRGM (WMS/WFS, Bd Charm-50), tests réels WFS `LITHO_1M_SIMPLIFIEE`, tableau roche mère → sol → essences |
