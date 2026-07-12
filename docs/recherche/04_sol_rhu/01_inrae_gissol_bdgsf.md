# INRAE GisSol — BDGSF & cartes dérivées (RU, profondeur, types de sol)
**Domaine** : docs/recherche/04_sol_rhu/
**Date de recherche** : 2026-07-01
**Agent** : sol-gissol

> **Note de contexte** : `RESEARCH_OPPORTUNITIES.md` liste déjà « BDGSF INRAE » en §1.1
> (intégré) et « GéoSol INRAE » en §1.3 (à intégrer, priorité 2). Le présent document
> **n'duplique pas** ces mentions : il les **approfondit** avec un test d'accès réel aux
> services web et l'analyse des variables exploitables pour le diagnostic stationnel GeoSylva.
> « GéoSol INRAE » du §1.3 désigne ici le même écosystème GisSol (portail geodata.inrae.fr /
> gissol.fr) — il n'y a pas de produit distinct appelé « GéoSol » ; la piste à intégrer est
> l'**ensemble des cartes dérivées de la BDGSF** (RU, profondeur, texture, WRB) exposées en
> WMS/WFS sur `geodata.inrae.fr/geoserver`.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| GisSol — page BDGSF (présentation) | Officielle | officielle (INRAE/GisSol) | https://gissol.hub.inrae.fr/thematiques/diversite-et-proprietes-des-sols/diversite-des-sols-de-france/donnees/bdgsf | 2023-11-30 |
| BDGSF v3.2.8.0 — jeu téléchargeable | Officielle | officielle (INRA, Recherche Data Gouv) | https://doi.org/10.15454/BPN57S | 2018-10-10 (données 1998) |
| Carte Réserve Utile en eau (BDGSF) — téléchargement | Officielle | officielle (Le Bas, INRA) | https://doi.org/10.15454/JPB9RB | 2018-10-10 (V2) |
| Carte Profondeur du sol (BDGSF) — téléchargement | Officielle | officielle (Le Bas, INRAE) | https://doi.org/10.15454/7ZDND6 | 2021-07-09 (V1) |
| Service WMS/WFS GeoDataINRAE (workspace `inra_bdgsf`) | Officielle | officielle (INRAE) | https://geodata.inrae.fr/geoserver/inra_bdgsf/wms (et /wfs) | testé 2026-07-01 |
| Catalogue CSW GeoDataINRAE | Officielle | officielle (INRAE) | https://geodata.inrae.fr/geonetwork/srv/fre/csw | testé 2026-07-01 |
| GisSol — carte « Réserves en eau utile de France » | Officielle | officielle (GisSol/RESF) | https://www.gissol.fr/donnees/cartes/les-reserves-en-eau-utile-de-la-france-metropolitaine-1483 | s.d. |
| GisSol — Webservices (mode d'emploi) | Officielle | officielle (GisSol) | https://www.gissol.fr/?cat=31 | s.d. |
| Communiqué INRAE « nouvelle carte des sols accessible à tous » | Officielle | officielle (INRAE) | https://www.inrae.fr/actualites/nouvelle-carte-sols-france-accessible-tous | 2020-02-26 |
| BDGSF Dataverse (3 datasets, 11 fichiers) | Officielle | officielle (INRAE) | https://entrepot.recherche.data.gouv.fr/dataverse/bdgsf | 2021/2018 |
| GisSol — programme IGCS (inventaire 1:250 000, format DoneSol) | Officielle | officielle (GisSol) | https://www.gissol.fr/le-gis/programmes/inventaire-gestion-et-conservation-des-sols-igcs-67 | s.d. |
| GisSol — programme BDAT (pH, CEC, métaux — 31 paramètres) | Officielle | officielle (GisSol) | https://www.gissol.fr/le-gis/programmes/base-de-donnees-danalyses-des-terres-bdat-62 | s.d. |

**DoneSol** : non pas une base de données diffusée, mais le **format/schéma** de saisie
(normalisation AFNOR NF X31-560) utilisé par les programmes d'inventaire (IGCS, RRP). L'outil
de saisie en ligne est `DoneSol-web` (https://dw3.gissol.fr/login). Aucune API publique de
requêtage direct — l'accès aux données IGCS régionales se fait via les portails régionaux
(partenaires) et non un service national unifié.

---

## 2. Données détaillées

### 2.1 La BDGSF — nature et résolution

- **BDGSF** = Base de Données Géographique des Sols de France à l'échelle **1/1 000 000**.
  Partie française de la base européenne des sols (SGDBE). Version actuelle testée : **3.2.8.0**
  (données originelles 1998, republiées 2018).
- **Résolution spatiale** : 1:1 000 000 → maille cartographique de l'ordre du **kilomètre**.
  Les Unités Cartographiques de Sol (UCS / SMU) sont de très grande taille (ex. un polygone
  testé couvrait ~2 250 km²). **C'est une information paysage/région, pas parcellaire.**
- **Modèle à 2 niveaux** (précisé par la numérisation européenne) :
  - **UCS / SMU** (Soil Mapping Unit) : polygone cartographique, contenant plusieurs sols ;
  - **UTS / STU** (Soil Typological Unit) : type de sol décrit par ses propriétés (texture,
    profondeur, RU, pH…), rattaché à une UCS par des proportions.
- Couverture : **France métropolitaine exhaustive** — « seule couverture nationale exhaustive
  disponible » (Recherche Data Gouv), « bientôt remplacée par les bases au 1/250 000 ».
- Source : https://doi.org/10.15454/BPN57S

### 2.2 Carte de la Réserve Utile en eau (RU) — variable clé pour GeoSylva

Dérivée de la BDGSF (Le Bas 2018). **5 classes + non-sols** :

| `classe` | Réserve utile (mm d'eau) | Interprétation forestière |
|----------|--------------------------|---------------------------|
| 1 | < 50 mm | Très faible — sécheresse estivale marquée, essences xérophiles |
| 2 | 50 – 100 mm | Faible — essences peu exigeantes en eau |
| 3 | 100 – 150 mm | Moyenne — gamme large, feuillus sociaux |
| 4 | 150 – 200 mm | Élevée — essences à forte demande hydrique |
| 5 | ≥ 200 mm | Très élevée — sols limoneux profonds (Bassin parisien) |
| 9 | non-sols | Surfaces non pédologiques (urbain, eau) |

- Téléchargement (shapefile/GeoTIFF) : https://doi.org/10.15454/JPB9RB
- Service web : WMS + WFS (cf. §3 test réel).
- La carte GisSol indique : « relation forte avec la texture mais aussi la profondeur » ; fortes
  RU = sols limoneux profonds du Bassin parisien ; faibles RU = sols sableux (Landes, Vosges) ou
  peu épais (Causses, Provence).

### 2.3 Carte de la profondeur du sol — variable clé pour GeoSylva

Dérivée de la BDGSF (Le Bas 2021). Classes par attribut `pr-cl` :

| `pr-cl` | Profondeur |
|---------|------------|
| 30 | ≤ 30 cm |
| 50 | 30 < p ≤ 50 cm |
| 100 | 50 < p ≤ 100 cm |
| 150 | 100 < p ≤ 150 cm |
| 200 | ≥ 150 cm (profond) |

- Téléchargement : https://doi.org/10.15454/7ZDND6
- **Non exposée en WFS** sur le workspace `inra_bdgsf` (testé : `bdgsf_classe_profondeur`
  → `Feature type unknown`). Accès par **téléchargement seul** (shapefile).

### 2.4 Carte des sols dominants (WRB) — type de sol

- Couche WFS `inra_bdgsf:geometrie_bdgsf` (3516 polygones). Attributs : `wrbdom` (nom de sol
  dominant en classification WRB, ex. `NON_SOLS`, `LUVISOLS`…), `soil_id`, `smu` (unité
  cartographique). Permet d'identifier le **grand type de sol** (Luvisol, Podzol, Calcisol…)
  pour un point — utile pour la logique d'aptitude d'essence.
- Téléchargement : via le dataset BDGSF principal (doi 10.15454/BPN57S).

### 2.5 Texture, pH, matière organique — limites importantes

- **Texture** : GisSol produit une carte thématique « texture des sols » (issue BDGSF/RMQS),
  mais **pas de couche WFS/WMS dédiée testée** dans le workspace `inra_bdgsf`. La texture est
  portée par les **UTS** du dataset BDGSF complet (téléchargement) — pas par un service web
  simple à requêter par point.
- **pH** : **non disponible dans la BDGSF** à l'échelle 1:1M. Le pH est suivi par le programme
  **BDAT** (Base de Données d'Analyses des Terres) — 31 paramètres physico-chimiques (pH, CEC,
  carbone, N, P, K, Mg, métaux) — mais la BDAT est une base **par maille administrative /
  échantillons labo**, pas une couverture raster continue requêtable par point GPS. Accès par
  synthèses régionales / téléchargements agrégés.
- **Matière organique** : idem (BDAT / RMQS, pas de service point par point à 1:1M).
- **Référentiel Régional Pédologique (RRP)** : cartographies au **1:250 000** produites par le
  programme IGCS avec partenaires régionaux, au format DoneSol. **Plus précises que la BDGSF**
  mais **pas nationalement exhaustives** et **pas exposées via un service web unifié national** —
  accès dispersé chez les partenaires régionaux. `[À VÉRIFIER MANUELLEMENT]` : existence d'un
  WFS régional pour la zone d'intérêt de l'utilisateur GeoSylva.

### 2.6 Licence et conditions d'accès

- **Données téléchargeables** (Recherche Data Gouv / Dataverse) : sous
  **Licence Ouverte Etalab 2.0** (réutilisation libre, commerciale incluse, avec citation).
  Source : mentions Recherche Data Gouv (« Sauf mention contraire, tous les contenus de ce site
  sont sous licence etalab-2.0 »).
- **Service WMS/WFS GeoDataINRAE** : `Fees=NONE`, `AccessConstraints=NONE` (GetCapabilities).
  Aucune clé API requise (testé sans authentification). Licence exacte du flux non explicitée
  dans les capabilities — **présumer Licence Ouverte / CC-BY par héritage du dataset source**,
  `[À VÉRIFIER MANUELLEMENT]` auprès de GisSol pour un usage commercial en production.
- **Obligation de citation** : GisSol demande de citer le « rapport sur l'état des sols de
  France » (RESF) pour les cartes dérivées.

---

## 3. Test réel d'accès (effectué le 2026-07-01)

Tous les tests ci-dessous ont été exécutés via `curl` depuis le poste de recherche. Aucune clé
API, aucun token. Hôte résolu : `geodata.inrae.fr` (l'ancien `agroenvgeo.data.inra.fr` ne
résout plus — DNS obsolète, utiliser le domaine `inrae.fr`).

### 3.1 GetCapabilities WMS

```
GET https://geodata.inrae.fr/geoserver/inra_bdgsf/wms?service=WMS&request=GetCapabilities&version=1.3.0
```
- **HTTP 200**, 13 761 octets, `text/xml`.
- Service intitulé « Service WMS GeoDataINRAE ».
- CRS supportés : EPSG:2154 (Lambert 93), 3857, 4326, 27582 (Lambert II étendu, natif),
  3942–3950 (coniques France), 32630–32632 (UTM), etc.
- Couche nommée exposée : **`bdgsf_classe_ru`**.

### 3.2 GetCapabilities WFS + catalogue CSW

```
GET https://geodata.inrae.fr/geoserver/inra_bdgsf/wfs?service=WFS&request=GetCapabilities&version=1.0.0
GET https://geodata.inrae.fr/geonetwork/srv/fre/csw?service=CSW&request=GetCapabilities&version=2.0.2
```
- WFS : **HTTP 200**, 26 362 octets. Formats de sortie : JSON, GML2/GML3, SHAPE-ZIP, CSV,
  GeoPackage, KML, excel. `Fees=NONE`, `AccessConstraints=NONE`.
- Couche **annoncée** dans le FeatureTypeList : `inra_bdgsf:geometrie_bdgsf`
  (« BDGSF : Carte des sols dominants (WRB) »).
- CSW : **HTTP 200**, 14 336 octets — catalogue opérationnel (GetRecords / GetRecordById OK).

### 3.3 GetRecordById (métadonnées ISO 19115 de la carte RU)

```
GET https://geodata.inrae.fr/geonetwork/srv/fre/csw?service=CSW&request=GetRecordById
    &version=2.0.2&id=393d8106-4400-51cd-9767-e8bbef2f73a6
    &outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full
```
- **HTTP 200**, 29 623 octets. Extrait des liaisons (URLs de service) trouvées :
  - **WMS** : `https://geodata.inrae.fr/geoserver/inra_bdgsf/wms`
  - **WFS** : `https://geodata.inrae.fr/geoserver/inra_bdgsf/wfs?service=WFS&version=2.0.0
    &request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru&outputFormat=shape-zip`
  - **DOI téléchargement** : `https://doi.org/10.15454/JPB9RB`
  - **Rapport RESF** : `http://www.gissol.fr/rapports/Rapport_HD.pdf`

### 3.4 WFS GetFeature — échantillon (RU)

```
GET https://geodata.inrae.fr/geoserver/inra_bdgsf/wfs?service=WFS&version=2.0.0
    &request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru&count=3&outputFormat=application/json
```
- **HTTP 200**, 11 250 octets, `application/json`. GeoJSON valide.
- Schéma des propriétés : `area`, `perimeter`, `reserve_`, `reserve_id`, **`classe`** (1–5, 9).
- CRS natif renvoyé : `urn:ogc:def:crs:EPSG::27582` (Lambert II étendu).

### 3.5 WFS GetFeature — requête par point (cas d'usage GeoSylva)

**Test A — point en CRS natif (Lambert II étendu, EPSG:27582)** :
```
GET .../wfs?service=WFS&version=2.0.0&request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru
    &count=5&outputFormat=application/json
    &CQL_FILTER=INTERSECTS(geom,POINT(615000 2660000))
```
- **HTTP 200**, 8 341 octets, `numberMatched=1`, `numberReturned=1`.
- Résultat : `properties: { reserve_:3, reserve_id:2, classe:4 }` → **RU 150–200 mm**.
- ⚠️ Le filtre CQL `POINT(x y)` est interprété dans le **CRS natif de la géométrie (27582)**,
  pas dans le `srsName` de sortie. Un point fourni en Lambert 93 (2154) sans reprojection
  retourne 0 résultat (testé : `POINT(632000 6940000)` → `numberReturned=0`).

**Test B — BBOX en WGS84 (latitude/longitude, le plus simple pour un mobile GPS)** :
```
GET .../wfs?service=WFS&version=1.0.0&request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru
    &maxFeatures=5&outputFormat=application/json&srsName=EPSG:4326
    &BBOX=2.82,49.42,2.83,49.43,EPSG:4326
```
- **HTTP 200**, 38 516 octets. GeoJSON en WGS84 (coordonnées lon/lat). GeoServer
  **reprojette automatiquement** le BBOX WGS84 vers le CRS natif. ✅
- C'est la méthode recommandée pour GeoSylva : l'app reçoit un point GPS (WGS84), construit un
  micro-BBOX (±0.001° ≈ ±70 m) et récupère la classe RU du polygone contenant le point.

### 3.6 WFS GetFeature — sols dominants WRB

```
GET .../wfs?service=WFS&version=1.0.0&request=GetFeature&typeName=inra_bdgsf:geometrie_bdgsf
    &maxFeatures=2&outputFormat=application/json
```
- **HTTP 200**, 1 490 octets. `totalFeatures=3516`.
- Schéma : `area`, `perimeter`, `soil_`, `soil_id`, `smu`, **`wrbdom`** (ex. `NON_SOLS`).

### 3.7 Synthèse des accès confirmés

| Couche | WMS | WFS | Téléchargement | Attribut utile |
|--------|-----|-----|----------------|----------------|
| `bdgsf_classe_ru` | ✅ GetMap | ✅ GetFeature + CQL/BBOX | ✅ doi 10.15454/JPB9RB | `classe` (1–5,9) |
| `geometrie_bdgsf` (WRB) | — | ✅ GetFeature | ✅ doi 10.15454/BPN57S | `wrbdom`, `soil_id` |
| Profondeur | — | ❌ (non exposée) | ✅ doi 10.15454/7ZDND6 | `pr-cl` (30/50/100/150/200) |
| Texture / pH / MO | — | ❌ | partiel (UTS du dataset complet / BDAT) | — |

**Aucune clé API requise. Aucun quota visible.** Limites de taux non documentées — flux
apparemment non limité mais à usage raisonnable (ne pas scrapper la France entière par
boucle ; préférer un téléchargement bulk + index local pour usage intensif).

---

## 4. Variables exploitables pour le diagnostic stationnel GeoSylva

Cas d'usage cible : **pour un point GPS de parcelle, déterminer type de sol, RU, texture, pH
afin d'évaluer l'aptitude d'une essence**.

| Variable GeoSylva | Disponible BDGSF ? | Source/couche | Granularité réelle | Fiabilité parcellaire |
|-------------------|--------------------|---------------|--------------------|-----------------------|
| **Type de sol (WRB)** | ✅ Oui | WFS `geometrie_bdgsf`.`wrbdom` | 1:1M (UCS ~km) | **Faible** — indication régionale, pas parcellaire |
| **Réserve utile (mm)** | ✅ Oui (5 classes) | WFS `bdgsf_classe_ru`.`classe` | 1:1M | **Faible à modérée** — ordre de grandeur régional |
| **Profondeur (cm)** | ✅ Oui (5 classes) | Téléchargement only (`pr-cl`) | 1:1M | **Faible** |
| **Texture** | ⚠️ Partiel | UTS du dataset complet (pas WFS) | 1:1M | **Faible** — à extraire du shapefile téléchargé |
| **pH** | ❌ Non (BDGSF) | BDAT (synthèses régionales) | maille admin/échantillon | **Non requêtable par point GPS** |
| **Matière organique** | ❌ Non (BDGSF) | BDAT / RMQS (grille 16 km) | grille 16 km / échantillon | **Non requêtable par point GPS** |

### Logique d'intégration recommandée (par point GPS)

1. **RU** : WFS `bdgsf_classe_ru` + BBOX WGS84 (Test 3.5-B) → `classe` → intervalle mm.
   - Ex. `classe=4` → RU 150–200 mm → aptitude favorable pour chêne/hêtre/douglas
     (RHU optimal ~150–250 mm), défavorable pour essences xérophiles si classe 1–2.
2. **Type de sol WRB** : WFS `geometrie_bdgsf` + BBOX WGS84 → `wrbdom`.
   - Ex. `LUVISOLS` → sol lessivé, drainage moyen, potentiel feuillus nobles ;
     `PODZOLS` → sol acide pauvre, conifères (pin, épicéa) plutôt que feuillus exigeants.
3. **Profondeur** : à embarquer en local (télécharger le shapefile doi 10.15454/7ZDND6 une
   fois, indexer spatialement) — pas de WFS. Croiser avec RU pour qualifier la RU
   (RU élevée + profondeur ≥150 cm = réserve réellement disponible ; RU élevée mais sol
   peu profond = RU sur faible épaisseur, sécheresse rapide en surface).
4. **Texture / pH / MO** : **ne pas promettre** une valeur par point GPS à partir de GisSol.
   Signaler à l'utilisateur que ces variables nécessitent une analyse de sol terrain (ou les
   RRP 1:250 000 régionales si disponibles pour la zone).

### Correspondance avec les fiches essences (RHU, texture, pH optimaux)

Les fiches essences (`docs/recherche/06_essences/`, couche MOTEUR) attendent pour chaque
essence un **RHU optimal (mm)**, une **texture optimale** et un **pH optimal**. La BDGSF
alimente directement le **RHU** (classe → intervalle mm) et indirectement la **texture**
(via le type WRB → inférence texture dominante, ex. Luvisol → limono-argileux). Le **pH** ne
peut pas être servi par GisSol à l'échelle parcellaire → à traiter par saisie utilisateur
(analyse sol) ou par défaut régional grossier `[À VÉRIFIER MANUELLEMENT]`.

---

## 5. Comparatif / analyse critique

| Critère | BDGSF (1:1M) | RRP/IGCS (1:250 000) | RMQS (grille 16 km) | BDAT |
|---------|--------------|----------------------|---------------------|------|
| Couverture France | ✅ Exhaustive | ⚠️ Partielle (régionale) | ✅ Métropole | ✅ Métropole |
| Précision parcellaire | ❌ (région) | ⚠️ (commune/pays) | ❌ (échantillon) | ❌ (maille admin) |
| RU | ✅ (5 classes) | ✅ (plus fin) | ✅ (mesuré) | ❌ |
| Type de sol | ✅ (WRB) | ✅ (plus fin) | ✅ (profil) | ❌ |
| pH | ❌ | ✅ (parfois) | ✅ (mesuré) | ✅ (agrégé) |
| Accès web par point | ✅ WFS/WMS | ❌ dispersé | ❌ (téléchargement) | ❌ (synthèses) |
| Licence ouverte | ✅ Etalab 2.0 | variable | ✅ | ✅ |
| Intégration GeoSylva immédiate | ✅ **Oui** | Moyen terme | Long terme | Long terme |

**Verdict** : la BDGSF est le **seul produit national accessible par service web par point GPS
sans clé**, idéal pour un **premier niveau de diagnostic stationnel** dans GeoSylva. Sa
granularité (1:1M) impose de **présenter le résultat comme une indication régionale** et non
comme une vérité parcellaire — l'UI doit le dire explicitement pour ne pas surévaluer la
précision (enjeu déontologique pour un outil pro).

---

## 6. Recommandation pour GeoSylva

### Court terme (intégration directe, pas de clé)

1. **Embarquer la couche RU** via WFS `bdgsf_classe_ru` avec requête BBOX WGS84 (méthode
   Test 3.5-B). Côté Kotlin, un client WFS léger (OkHttp + parsing GeoJSON) suffit — réutiliser
   l'infrastructure existante d'appel aux APIs IGN (cf. `05_apis_externes/`).
   - Endpoint : `https://geodata.inrae.fr/geoserver/inra_bdgsf/wfs?service=WFS&version=1.0.0
     &request=GetFeature&typeName=inra_bdgsf:bdgsf_classe_ru&maxFeatures=1
     &outputFormat=application/json&srsName=EPSG:4326
     &BBOX={lon-0.001},{lat-0.001},{lon+0.001},{lat+0.001},EPSG:4326`
   - Mapper `classe` → intervalle RU (mm) → comparaison avec RHU optimal de l'essence
     (`CanonicalEssences` / fiches essences).
2. **Embarquer la couche WRB** via WFS `geometrie_bdgsf` (même mécanisme) → `wrbdom` affiché
   dans la fiche station (type de sol dominant).
3. **Télécharger une fois** le shapefile profondeur (doi 10.15454/7ZDND6) et l'indexer
   localement (Room + R-Tree / GeoPackage) pour éviter une dépendance online pour une couche
   non servie en WFS.
4. **UI** : afficher RU + type de sol + profondeur avec un bandeau « estimation régionale
   (BDGSF 1:1 000 000) — affiner par analyse de sol terrain ».

### Moyen terme

5. Évaluer la disponibilité d'un **RRP 1:250 000 régional** WFS pour les zones d'activité
   principales de l'utilisateur (contacter le partenaire régional IGCS) — précision
   nettement meilleure pour le diagnostic stationnel.
6. Compléter le **pH** via saisie utilisateur (analyse sol) + valeur par défaut régionale
   issue de la BDAT (synthèse régionale téléchargeable).

### Fichiers Kotlin concernés (à titre indicatif)

- Client API / repository sol : à créer (ex. `SoilRepository.kt`, `BdgsfApi.kt`) — pattern
  identique aux repositories IGN existants.
- Modèle stationnel : structure de données `StationDiagnostic` (RU mm, typeSol WRB,
  profondeur cm, texture?, pH?) à croiser avec les exigences essences.
- Fiches essences : `CanonicalEssences.kt` et `docs/recherche/06_essences/` (RHU optimal par
  essence) — la BDGSF fournit l'entrée « station », les fiches fournissent l'entrée « essence ».

### Priorité

**Haute** pour RU + type de sol (intégration immédiate, gratuite, sans clé, valeur métier
directe pour le diagnostic stationnel). **Moyenne** pour profondeur (téléchargement local).
**Basse / bloquée** pour pH et texture par point (hors GisSol à cette échelle).

---

## 7. Limites et points à vérifier manuellement

1. **Granularité 1:1 000 000** : la BDGSF donne une **indication régionale**, pas une
   caractérisation parcellaire. Ne jamais présenter la valeur comme exacte pour une parcelle
   donnée — risque de mauvais conseil d'essence si le sol local diffère du sol dominant de
   l'UCS (l'UCS peut contenir plusieurs UTS en proportions). `[À VÉRIFIER MANUELLEMENT]` :
   pondérer par les proportions d'UTS si on exploite le dataset complet.
2. **Ancienneté des données** : version 3.2.8.0 issue de données 1998 — la cartographie des
   sols évolue lentement mais certains aménagements (drainage, artificialisation) ne sont pas
   reflétés.
3. **CRS natif Lambert II étendu (EPSG:27582)** : piège classique — un filtre CQL `POINT()`
   doit être en 27582, pas en 2154. **Recommandation GeoSylva** : utiliser exclusivement le
   paramètre `BBOX=...,EPSG:4326` (WGS84) qui délègue la reprojection à GeoServer (Test 3.5-B
   validé). Éviter les filtres CQL spatiaux pour ne pas gérer la reprojection côté client.
4. **Profondeur non exposée en WFS** : accès par téléchargement shapefile uniquement →
   nécessite un index local (GeoPackage/Room) dans l'app.
5. **pH et matière organique non disponibles** à l'échelle par point via GisSol — ne pas
   implémenter de fausse promesse UI. La BDAT fournit des synthèses régionales agrégées, pas
   une valeur par coordonnée.
6. **Licence du flux WMS/WFS** : `Fees=NONE`/`AccessConstraints=NONE` confirmés dans les
   capabilities, données source en Licence Ouverte Etalab 2.0, mais la licence exacte du flux
   GeoServer n'est pas explicitement énoncée dans les capabilities. `[À VÉRIFIER
   MANUELLEMENT]` auprès de GisSol (contact) avant un usage commercial en production —
   présumé réutilisable sous citation du RESF.
7. **Hôte obsolète** : `agroenvgeo.data.inra.fr` (ancien domaine INRA) ne résout plus en DNS
   → utiliser **`geodata.inrae.fr`** (domaine INRAE post-fusion). Les anciennes fiches de
   métadonnées citent encore l'URL `.inra.fr`.
8. **Quotas / disponibilité** : aucun quota documenté, flux non authentifié. Pour un usage
   intensif (ex. préchargement France entière), **préférer le téléchargement bulk** (DOI) +
   index local plutôt que des milliers de requêtes WFS point par point (bonne pratique +
   robustesse offline).
9. **DoneSol** est un **format de saisie** (norme AFNOR NF X31-560), pas une base diffusée —
   ne pas chercher d'« API DoneSol » ; les données IGCS au format DoneSol sont accessibles
   via les partenaires régionaux, pas un service national unifié.
