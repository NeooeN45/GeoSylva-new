# Alternatives mondiales/européennes au BDGSF INRAE pour la pédologie
**Domaine** : docs/recherche/04_sol_rhu/
**Date de recherche** : 2026-07-02
**Agent** : sol-rhu-alternatives

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| SoilGrids 2.0 (ISRIC) | Produit scientifique global | scientifique | https://isric.org/explore/soilgrids | 2021 (Poggio et al., SOIL 7:217-240) |
| SoilGrids REST API (ISRIC) | API REST | scientifique | https://rest.isric.org/soilgrids/v2.0/properties/query | v2.0, testé 2026-07-02 |
| SoilGrids FAQ / layers (ISRIC) | Documentation | scientifique | https://docs.isric.org/globaldata/soilgrids/SoilGrids_faqs_01.html | 2024+ |
| ESDB v2 Raster Library 1km (ESDAC/JRC) | Base de données européenne | officielle | https://esdac.jrc.ec.europa.eu/node/78 | 2024 (dérivé ESDB v2.0, 2004) |
| ESDB v2.0 vectorielle (ESDAC/JRC) | Base de données européenne | officielle | https://esdac.jrc.ec.europa.eu/content/european-soil-database-v20-vector-and-attribute-data | 2004 |
| LUCAS Topsoil (ESDAC/JRC) | Base de données européenne | officielle | https://esdac.jrc.ec.europa.eu/resource-type/european-soil-database-soil-properties | 2015 |
| HWSD v2.0/v2.01 (FAO/IIASA) | Base de données mondiale | officielle | https://www.fao.org/soils-portal/data-hub/soil-maps-and-databases/harmonized-world-soil-database-v20/en | 2023 (v2.0), v2.01 |
| BDGSF 1/1M (INRAE/GisSol) | Base de données française | officielle | https://gissol.hub.inrae.fr/donnees-et-outils/donnees/bdgsf | v3.2.8.0 (données 1998, publiée 2018) |
| BDGSF Dataverse (Recherche Data Gouv) | Téléchargement | officielle | https://entrepot.recherche.data.gouv.fr/dataverse/bdgsf | 2018-2021 |
| Poggio et al. 2021 (SoilGrids 2.0) | Article scientifique | scientifique | https://doi.org/10.5194/soil-7-217-2021 | 2021 |

## 2. Données détaillées

### 2.1 SoilGrids 2.0 (ISRIC)

- **Résolution spatiale** : 250 m (cellule raster). Couverture mondiale.
- **Profondeurs** : 6 intervalles standard GlobalSoilMap : 0-5, 5-15, 15-30, 30-60,
  60-100, 100-200 cm.
- **Variables disponibles** (propriétés de base, endpoint REST `properties/query`) :

  | Code | Description | Unités mappées | Facteur conv. | Unités conventionnelles |
  |------|-------------|----------------|---------------|--------------------------|
  | `bdod` | Densité apparente | cg/cm³ | ÷100 | kg/dm³ |
  | `cec` | CEC à pH 7 | mmol(c)/kg | ÷10 | cmol(c)/kg |
  | `cfvo` | Fragments grossiers | cm³/dm³ (‰) | ÷10 | % vol |
  | `clay` | Argile | g/kg | ÷10 | % |
  | `nitrogen` | Azote total | cg/kg | ÷100 | g/kg |
  | `ocd` | Densité de carbone organique | hg/m³ | ÷10 | kg/m³ |
  | `ocs` | Stock de carbone organique (0-30cm) | t/ha | ÷10 | kg/m² |
  | `soc` | Carbone organique du sol | dg/kg | ÷10 | g/kg |
  | `phh2o` | pH eau | pH×10 | ÷10 | - |
  | `sand` | Sable | g/kg | ÷10 | % |
  | `silt` | Limon | g/kg | ÷10 | % |
  | `wv0010` | Teneur en eau volumique à 10 kPa | 10⁻³ cm³/cm³ | ÷10 | % |
  | `wv0033` | Teneur en eau volumique à 33 kPa | 10⁻³ cm³/cm³ | ÷10 | % |
  | `wv1500` | Teneur en eau volumique à 1500 kPa | 10⁻³ cm³/cm³ | ÷10 | % |

- **AWC (Available Water Capacity)** : **NON disponible comme propriété de base** dans
  l'endpoint REST `properties/query`. C'est un **produit dérivé** (différence entre capacité
  au champ pF 2.0 et point de flétrissement pF 4.2), accessible via :
  - WCS (GeoTIFF on-demand) : couche dérivée `awc_fc_eq_pf_2_0` (StellaSpark/ISRIC),
  - Google Earth Engine (`ISRIC/SoilGrids250m/v2_0` + calcul à partir de `wv0033`/`wv1500`),
  - Téléchargement GeoTIFF direct sur soilgrids.org.
  - Calcul manuel possible côté app : `AWC ≈ wv0033 − wv1500` (capacité au champ ~33 kPa
    moins point de flétrissement ~1500 kPa), par profondeur, puis intégration sur la
    profondeur racinaire. **[À VÉRIFIER MANUELLEMENT]** : la convention pF (33 kPa vs 10 kPa
    pour la capacité au champ) varie selon les écoles — la BDGSF française et l'ESDB
    européen utilisent des classes, pas un calcul pF continu.

- **Modèle** : machine learning (random forest / regression kriging) sur ~240 000 profils
  WoSIS + >400 covariables environnementales (climat, relief, occupation du sol, NDVI).
  Quantifie l'incertitude (Q0.05, Q0.5, Q0.95, ratio inter-quantile).

- **Accès** :
  - **REST API** : `https://rest.isric.org/soilgrids/v2.0/properties/query?lon={lon}&lat={lat}&property={p}&depth={d}&value={v}` — **sans clé**, renvoie JSON GeoJSON Feature. Voir §3 test réel.
  - **WCS** : `https://maps.isric.org/mapserv?request=GetCoverage...` — téléchargement GeoTIFF par subset spatial, résolution 250m paramétrable.
  - **WMS** : visualisation uniquement.
  - **Google Earth Engine** : `ISRIC/SoilGrids250m/v2_0` (accès via GEE, nécessite compte Google).
  - **Téléchargement bulk** : GeoTIFF mondiaux sur soilgrids.org (volumineux, ~Go par variable).

- **Licence** : **CC-BY 4.0** (confirmé sur https://docs.isric.org/globaldata/soilgrids/).
  Usage commercial autorisé avec attribution.

### 2.2 ESDAC — European Soil Database v2 Raster Library (JRC)

- **Résolution spatiale** : **1 km × 1 km** (GeoTIFF, accès après inscription) ET
  **10 km × 10 km** (ESRI GRID, domaine public, sans inscription).
- **Couverture** : Europe (incluant France métropolitaine) + Eurasie.
- **Variables AWC** (PTRDB, classes discrètes — non continues) :

  | Code | Description |
  |------|-------------|
  | `AWC_TOP` | Available Water Capacity — horizon de surface |
  | `AWC_SUB` | Available Water Capacity — sous-sol |
  | `EAWC_TOP` | Easily Available Water Capacity — surface |
  | `EAWC_SUB` | Easily Available Water Capacity — sous-sol |

  Autres variables : texture (clay/silt/sand %), CEC, bulk density, profondeur,
  pierrosité, classes d'érosion, régime hydrique, etc. (73 attributs au total).

- **Origine** : ESDB v2.0 (2004), base vectorielle polygonale (SGDBE) + règles de
  pédotransfert (PTRDB). Les rasters 1km sont dérivés par « dominant value ».
- **Accès** : inscription via formulaire ESDAC (https://esdac.jrc.ec.europa.eu/node/78),
  puis téléchargement GeoTIFF. **Pas d'API REST** — téléchargement de fichiers uniquement.
- **Licence** : copyright conjoint JRC / European Soil Bureau Network. Usage libre pour
  recherche/policy ; conditions commerciales à vérifier. **[À VÉRIFIER MANUELLEMENT]**
  pour un usage en app commercial.
- **Complément LUCAS Topsoil (2015)** : propriétés physiques de surface (texture, AWC,
  bulk density) échantillonnées sur ~20 000 points européens par la Commission — plus
  récent mais surface uniquement (0-20 cm), pas de profondeur.

### 2.3 HWSD v2.0 / v2.01 (FAO/IIASA)

- **Résolution spatiale** : ~1 km (30 arc-secondes). Couverture mondiale.
- **Profondeurs** : 7 couches (amélioration vs v1.2 qui n'en avait que 2).
- **Variables** : morphologiques, chimiques et physiques (texture, CEC, bulk density,
  carbone organique, pH, base saturation, AWC dérivée) par Soil Mapping Unit (~30 000 SMU).
- **Accès** : téléchargement uniquement (raster GeoTIFF + base MS Access `.mdb` + viewer).
  **Pas d'API REST**. URL : https://data.apps.fao.org/catalog/iso/ff5c613c-75bb-46a9-a162-bc728059b465
- **Licence** : **CC BY-NC-SA 4.0** (NonCommercial, ShareAlike).
  ⚠️ **La clause NonCommercial est potentiellement incompatible** avec une app Android
  commerciale comme GeoSylva — à faire valider juridiquement avant toute intégration.

### 2.4 BDGSF INRAE/GisSol (rappel — source primaire française)

- **Échelle** : 1/1 000 000 (vectoriel — polygones SMU, non raster). Données 1998,
  version 3.2.8.0 publiée 2018.
- **Variables** : profondeur du sol (classes 30/50/80/100/120/150 cm),
  **Réserve Utile en eau (RU/AWC) en classes** (< 50 mm, 50-100, 100-150, 150-200, > 200 mm),
  texture, type de sol (FAO/WRB).
- **Accès** : téléchargement libre (shapefiles) sur
  https://entrepot.recherche.data.gouv.fr/dataverse/bdgsf (DOI 10.15454/BPN57S, 10.15454/JPB9RB).
  Package R `RADIS` (`get_bdgsf_from_dataverse`) pour automatiser.
- **Licence** : ouverte (Recherche Data Gouv, Licence Ouverte Etalab 2.0 implicite).
- **Statut** : source primaire française, validée par expertise pédologique nationale,
  intégrée à l'ESDB européen. **Référence pour la France**.

## 3. Comparatif / analyse critique

### 3.1 Tableau comparatif résolution / variables / licence

| Critère | BDGSF (INRAE) | SoilGrids 2.0 (ISRIC) | ESDB Raster (ESDAC) | HWSD v2.0 (FAO) |
|---------|---------------|------------------------|----------------------|------------------|
| **Résolution** | 1/1M vectoriel (~1 km, polygones) | 250 m raster | 1 km raster (+ 10 km) | ~1 km raster (30 arc-s) |
| **Type de valeurs** | Classes (discrètes) | Continues + incertitude | Classes (discrètes) | Continues par SMU |
| **Profondeurs** | Surface + profondeur (2) | 6 intervalles (0-200 cm) | Surface + sous-sol (2) | 7 couches |
| **AWC/RU** | Oui (classes FR, RU mm) | Dérivé (wv0033−wv1500) | Oui (AWC_TOP/SUB classes) | Oui (dérivé) |
| **Texture (clay/sand/silt)** | Classes | % continu (g/kg) | % (classes PTRDB) | % par SMU |
| **pH** | Non (via ESDB) | pH eau continu | Non direct | Oui |
| **CEC** | Non direct | Continu (mmol/kg) | Classes | Oui |
| **Bulk density** | Non | Continu (kg/dm³) | Classes | Oui |
| **Couverture France** | ✅ Optimisée FR | ✅ Globale (inclut FR) | ✅ Européenne | ✅ Globale |
| **Validation FR** | Expertise nationale INRAE | ML global (peu de points FR) | Inclus BDGSF | Inclut sources nationales |
| **Accès API REST** | ❌ (fichiers) | ✅ sans clé | ❌ (inscription + fichiers) | ❌ (fichiers) |
| **Licence** | Ouverte Etalab | CC-BY 4.0 | Copyright JRC (à vérifier) | **CC BY-NC-SA 4.0** ⚠️ |
| **Usage commercial** | ✅ | ✅ | ⚠️ À confirmer | ❌ Probablement non |
| **Mise à jour** | 1998 (v3.2.8.0) | 2021 | 2004 (rasters 2024) | 2023 (v2.0) |

### 3.2 Précision en France pour une granularité communale

- **SoilGrids 250m** offre la **meilleure résolution spatiale brute** (~250 m = ~6 ha/pixel).
  Une commune moyenne française (~15 km²) contient ~600 pixels SoilGrids vs ~15 pixels
  HWSD/ESDB 1km vs 1-5 polygones BDGSF. SoilGrids est donc le plus fin spatialement.
  **MAIS** : SoilGrids est un produit de **machine learning global** entraîné sur ~240 000
  profils mondiaux, dont une faible densité en France. Les prédictions peuvent manquer
  les spécificités pédologiques locales françaises (sols hydromorphes, rendosols,
  podzols forestiers) que l'expertise INRAE capture dans la BDGSF. L'incertitude
  (Q0.05–Q0.95) est fournie mais peut être élevée en zones peu échantillonnées.

- **BDGSF 1/1M** est **coarse spatialement** (polygones au 1/1M) mais **sémantiquement
  le plus pertinent pour la France** : nomenclature française, RU en classes calibrées
  par les pédologues INRAE, intégrée au système européen. Pour un diagnostic stationnel
  forestier (aptitude d'essence basée sur la RU), la **classe de RU BDGSF est la référence
  opérationnelle** même si elle ne distingue pas deux parcelles distantes de 500 m.

- **HWSD 1km** est le **plus grossier** et n'apporte rien de plus que SoilGrids pour la
  France (SoilGrids est 4× plus fin et inclut déjà les sources nationales). Sa licence
  NonCommercial le disqualifie pour GeoSylva.

- **ESDB 1km** est équivalent à la BDGSF (la BDGSF en est le volet français) — pas
  d'apport supplémentaire pour la France métropolitaine.

**Conclusion précision France communale** : Aucune source n'est parfaite.
- Pour la **RU/AWC** (critique pour le diagnostic stationnel forestier) : **BDGSF reste
  la référence** (classes validées FR), malgré sa coarseur spatiale.
- Pour les **propriétés continues** (pH, texture %, densité apparente, CEC) à granularité
  fine : **SoilGrids 250m est le meilleur complément**, à condition de croiser avec la
  classe BDGSF pour valider la cohérence pédologique.

### 3.3 Test d'accès réel à l'API SoilGrids REST (2026-07-02)

**Requête 1** — point urbain (Paris) :
```
GET https://rest.isric.org/soilgrids/v2.0/properties/query
    ?lon=2.35&lat=48.85&property=bdod&property=phh2o&depth=0-5cm&value=mean
```
- **Code HTTP** : 200 OK (JSON valide).
- **Temps de réponse** : `query_time_s: 0.70` (initial), puis `21.55` (sans filtre depth).
- **Réponse** (extrait) :
```json
{"type":"Feature","geometry":{"type":"Point","coordinates":[2.35,48.85]},
 "properties":{"layers":[
   {"name":"bdod","unit_measure":{"d_factor":100,"mapped_units":"cg/cm³","target_units":"kg/dm³"},
    "depths":[{"range":{"top_depth":0,"bottom_depth":5,"label":"0-5cm"},
               "values":{"mean":null,"Q0.05":null,"Q0.5":null,"Q0.95":null}}]},
   {"name":"phh2o","unit_measure":{"d_factor":10,"mapped_units":"pH*10"},
    "depths":[{"label":"0-5cm","values":{"mean":null,...}}]}
 ]}}
```
- ⚠️ **Toutes les valeurs `mean` sont `null`** — structure JSON correcte mais données vides.

**Requête 2** — point rural forestier (forêt de Fontainebleau, 48.4N / 2.7E), 5 propriétés,
  sans filtre depth (toutes profondeurs) :
```
GET https://rest.isric.org/soilgrids/v2.0/properties/query
    ?lon=2.7&lat=48.4&property=bdod&property=phh2o&property=clay&property=sand&property=cec
```
- **Code HTTP** : 200 OK.
- **Temps de réponse** : `query_time_s: 36.43` (long — le serveur travaille réellement).
- **Réponse** : structure GeoJSON complète, 6 intervalles de profondeur (0-5 à 100-200cm),
  mais **toutes valeurs `null`** (mean, Q0.05, Q0.5, Q0.95, uncertainty).

**Requête 3** — propriété inexistante `awc` :
- **Code HTTP** : 500 (erreur serveur) — confirme que `awc` n'est pas une propriété de base
  de l'endpoint `properties/query` (produit dérivé uniquement, cf. §2.1).

**Interprétation** :
1. L'endpoint REST **fonctionne** (réponses JSON structurées, codes 200, temps de calcul
   réel 0.7–36 s), **sans clé d'API**.
2. Les valeurs `null` sur les 2 points français testés suggèrent un **service de données
   partiellement dégradé** — la documentation ISRIC mentionne explicitement
   (https://docs.isric.org/globaldata/soilgrids/) : *« We will update as soon as the
   service is restored »* concernant les accès alternatifs. Le service de visualisation
   soilgrids.org reste opérationnel, mais l'API REST `properties/query` semble renvoyer
   des données vides actuellement.
3. **Pas de quota documenté** ni de clé requise — l'API est conçue pour un accès libre,
   mais la fiabilité actuelle est incertaine. **[À RETESTER MANUELLEMENT]** avant
   intégration : tester via `curl`/Postman (le proxy `webfetch` peut interférer) et
   vérifier si le problème persiste sur d'autres coordonnées (hors France).

## 4. Recommandation pour GeoSylva

### Combinaison optimale recommandée

**Source primaire FR : BDGSF (INRAE/GisSol)** — pour la Réserve Utile (RU/AWC) et la
profondeur de sol, en classes. C'est la référence pédologique française, licence ouverte,
validée par expertise nationale. À intégrer via téléchargement des shapefiles
(`bdgsf_classe_rfu.shp` pour RU, `bdgsf_classe_prof.shp` pour profondeur) prétraités en
backend, puis servis à l'app par code commune / point GPS (interpolation polygone).

**Complément haute résolution : SoilGrids 2.0 (ISRIC)** — pour les propriétés continues
absentes ou en classes dans la BDGSF : **pH eau, texture % (clay/sand/silt), densité
apparente, CEC, carbone organique**. Résolution 250m, licence CC-BY 4.0 (commercial OK),
API REST sans clé. À appeler côté app Android (point GPS) ou précalculer côté backend.

**Calcul de l'AWC/RU continu** : combiner `wv0033 − wv1500` de SoilGrids (capacité au
champ 33 kPa − point de flétrissement 1500 kPa) intégrée sur la profondeur racinaire,
puis **calibrer/valider contre la classe BDGSF**. Si écart > 1 classe, privilégier BDGSF
(expertise FR) et signaler l'incertitude à l'utilisateur.

**Exclure : HWSD v2.0** — licence CC BY-NC-SA 4.0 (NonCommercial) incompatible avec une
app commerciale, et aucune valeur ajoutée vs SoilGrids (plus grossier, pas d'API).

**Exclure (pour la France) : ESDB 1km ESDAC** — redondant avec la BDGSF (qui en est le
volet français), accès sur inscription sans API, licence commerciale incertaine. À
considérer uniquement si GeoSylva s'étend à d'autres pays européens.

### Fichiers Kotlin concernés (priorité)

- **Priorité 1 (diagnostic stationnel)** : la logique de RHU optimal par essence
  (fichiers `06_essences/`) et le calcul de bilan hydrique doivent consommer la **classe
  de RU BDGSF** comme entrée principale. Identifier le service/repository qui gère le sol
  (probablement un `SoilRepository` ou `StationDiagnosticService` à créer).
- **Priorité 2 (enrichissement)** : ajout d'un client SoilGrids REST
  (`SoilGridsApiService`, Retrofit) pour récupérer pH/texture/densité par point GPS —
  à condition que le test réel (§3.3) soit revalidé et que l'API soit stable.
- **Priorité 3 (backend)** : prétraitement Python des shapefiles BDGSF → table
  `soil_rhu_by_commune` (code INSEE → classe RU, profondeur) servie via l'API backend
  existante (cf. recommandation ERA5-Land vague 3).

## 5. Limites et points à vérifier manuellement

1. **[À RETESTER MANUELLEMENT]** L'API REST SoilGrids a renvoyé `null` sur 2 points
   français (Paris + Fontainebleau) le 2026-07-02 — retester via `curl` direct (sans
   proxy webfetch) et sur d'autres coordonnées pour distinguer : (a) dégradation
   temporaire du service ISRIC, (b) bug du proxy webfetch, (c) absence réelle de
   données sur ces points. La doc ISRIC évoque un service en cours de restauration.
2. **[À VÉRIFIER MANUELLEMENT]** Convention pF pour le calcul d'AWC : SoilGrids fournit
   wv0033 (33 kPa) et wv1500 (1500 kPa) ; la capacité au champ française est parfois
   définie à pF 2.0 (10 kPa) ou pF 2.5 (33 kPa) selon les écoles. Valider la formule
   `AWC = wv0033 − wv1500` contre un pédologue ou la BDGSF avant intégration.
3. **[À VÉRIFIER MANUELLEMENT]** Licence ESDAC/ESDB pour usage commercial app — le
   copyright conjoint JRC/European Soil Bureau Network n'est pas une licence standard
   type CC ; contacter ESDAC (esdac@jrc.ec.europa.eu) pour confirmation écrite si
   intégration envisagée (non recommandé pour la France, redondant avec BDGSF).
4. **[À VÉRIFIER JURIDIQUEMENT]** Licence HWSD v2.0 CC BY-NC-SA 4.0 — la clause
   « NonCommercial » exclut a priori une app Android payante/avec publicité. À faire
   valider par un juriste si jamais une variable HWSD s'avérait indispensable (peu
   probable vu les alternatives).
5. **Limites BDGSF** : données de 1998 (v3.2.8.0), échelle 1/1M — pas de mise à jour
   récente, précision limitée à l'échelle régionale/départementale, pas communale fine.
   La BDGSF ne distingue pas deux parcelles voisines ; pour une granularité parcellaire
   réelle, il faudrait un levé de sol terrain (hors scope API).
6. **SoilGrids en France** : densité de profils d'entraînement faible en France vs
   Pays-Bas/USA — l'incertitude (Q0.05–Q0.95) doit être affichée à l'utilisateur et ne
   pas être présentée comme une vérité terrain. Croiser systématiquement avec BDGSF.
