# APIs IGN — Carto Nature, Carto Urbanisme, BD Ortho, Corine Land Cover
**Domaine** : docs/recherche/05_apis_externes/
**Date de recherche** : 2026-07-02
**Agent** : apis-ign-carto-nature-urbanisme

## 0. Contexte et périmètre

Ce document teste **réellement** (requêtes live, codes HTTP, extraits de réponse) les 4 APIs IGN
identifiées mais non testées dans `RESEARCH_OPPORTUNITIES.md` §1.2 :
- API Carto Nature (Natura 2000, ZNIEFF, RNN, PNR) — détection espaces protégés sur une parcelle
- API Carto Urbanisme (PLU, POS, CC, servitudes) — contraintes d'urbanisme sur une parcelle
- BD Ortho (orthophotos 50cm) — photo-interprétation peuplements
- Corine Land Cover (occupation sols 44 classes) — interface forêt/agriculture/urbain

Point d'entrée unique : la **Géoplateforme** (`data.geopf.fr`), opérée par l'IGN depuis 2024
(remplace l'ancien `wxs.ign.fr` / `geoportail.ign.fr`). Les APIs REST complémentaires
(`apicarto.ign.fr`) sont testées séparément.

> **Constat majeur** : toutes les APIs testées ci-dessous sont accessibles **SANS clé API** pour les
> données ouvertes (endpoints publics). Une clé (compte gratuit `cartes.gouv.fr`) n'est requise que
> pour les données restreintes ou pour débloquer des quotas supérieurs. GeoSylva peut donc intégrer
> immédiatement ces 4 APIs sans démarche préalable.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| Géoplateforme — API WMTS | Officielle (IGN) | officielle | https://data.geopf.fr/wmts | CGU v15/10/2024 |
| Géoplateforme — API WMS-Raster | Officielle (IGN) | officielle | https://data.geopf.fr/wms-r | CGU v15/10/2024 |
| Géoplateforme — API WFS | Officielle (IGN) | officielle | https://data.geopf.fr/wfs | CGU v15/10/2024 |
| API Carto (REST, module Urbanisme) | Officielle (IGN) | officielle | https://apicarto.ign.fr/api | 2025 |
| CGU Géoplateforme | Officielle (IGN) | officielle | https://cartes.gouv.fr/cgu | v15/10/2024 |
| Limites d'usage (rate limiting) | Officielle (IGN) | officielle | https://ignf.github.io/cartes.gouv.fr-documentation/fr/guides-utilisateur/utiliser-les-services-de-la-geoplateforme/limites-d-usage/ | 19/03/2026 |
| Doc API Carto Urbanisme (PDF) | Officielle (IGN) | officielle | https://apicarto.ign.fr/api/doc/pdf/docUser_moduleUrbanisme.pdf | 2025 |
| Géoportail de l'Urbanisme (GPU) | Officielle (IGN/MTE) | officielle | https://www.geoportail-urbanisme.gouv.fr | 2025 |
| Corine Land Cover (métadonnées) | Officielle (MTECT/SDES) | officielle | https://cartes.gouv.fr/rechercher-une-donnee/dataset/MTECT_CORINE-LAND-COVER | CLC 2018 |
| Code GeoSylva — MapScreen.kt | Code projet | — | `app/.../presentation/screens/forestry/MapScreen.kt` | Lignes 879-998 |
| Code GeoSylva — WmsLayerManager.kt | Code projet | — | `app/.../domain/geo/WmsLayerManager.kt` | Lignes 28-83 |

---

## 2. Tests réels d'API

### 2.1 API Carto Nature — Natura 2000, ZNIEFF, Parcs, Réserves

**Objectif** : détecter les espaces protégés intersectant une parcelle / un point GPS.

#### 2.1.1 Endpoint WFS (données vectorielles, interrogation par BBOX/filtre)

- **URL** : `https://data.geopf.fr/wfs`
- **Méthode** : GET
- **Clé API** : **non requise** (endpoint public, données ouvertes)
- **Couches identifiées** (GetCapabilities, 5,1 Mo XML, HTTP 200) :

| Couche WFS | Type | Description |
|------------|------|-------------|
| `patrinat_sic:sic` | Natura 2000 — directive Habitats | Sites d'Importance Communautaire (SIC / future ZSC) |
| `patrinat_zps:zps` | Natura 2000 — directive Oiseaux | Zones de Protection Spéciale (ZPS) |
| `patrinat_znieff1:znieff1` | ZNIEFF de type 1 | Zones écologiquement sensibles (petits périmètres) |
| `patrinat_znieff2:znieff2` | ZNIEFF de type 2 | Grands ensembles naturels |
| `patrinat_pn:parc_national` | Parcs nationaux | Périmètres des parcs nationaux (cœur + aire d'adhésion) |
| `patrinat_pnr:pnr` | Parcs naturels régionaux | Périmètres PNR |
| `patrinat_rb:reserve_biologique` | Réserves biologiques | Réserves biologiques (ONF/RNF) |
| `patrinat_znieff1_mer:znieff1_mer` | ZNIEFF1 mer | Domaine maritime |
| `patrinat_znieff2_mer:znieff2_mer` | ZNIEFF2 mer | Domaine maritime |

> **Note** : les réserves naturelles nationales (RNN) et régionales (RNR) ne sont pas exposées
> directement comme couche WFS distincte sur la Géoplateforme au moment du test ; le MNHN/INPN
> propose un WFS séparé (cf. fiche `04_apis_biodiversite_inpn_gbif.md`). Les réserves biologiques
> (`patrinat_rb`) couvrent partiellement ce besoin.

**Requête test** (GetFeature, BBOX sur Fontainebleau, 2 features) :
```
GET https://data.geopf.fr/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0
    &TYPENAMES=patrinat_sic:sic&STARTINDEX=0&COUNT=2
    &BBOX=2.6,48.3,2.8,48.5,EPSG:4326
    &OUTPUTFORMAT=application/json
```

**Réponse réelle** :
- **HTTP 200** | 493 940 octets | `application/json;charset=UTF-8`
- Extrait (features retournées, BBOX Fontainebleau) :
```json
{
  "features": [
    {"properties": {"nom": "Massif de Fontainebleau", "code": null}},
    {"properties": {"nom": "Basse vallée du Loing", "code": null}}
  ]
}
```
> ✅ Résultat cohérent — le site Natura 2000 « Massif de Fontainebleau » (FR1100795, directive
> Habitats) intersecte bien la zone testée. Donnée réelle et exploitable.

**Requête test ZNIEFF2** (GetFeature, 2 features, sans BBOX) :
```
GET https://data.geopf.fr/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0
    &TYPENAMES=patrinat_znieff2:znieff2&STARTINDEX=0&COUNT=2
    &OUTPUTFORMAT=application/json
```
**Réponse réelle** : **HTTP 200** | 57 382 octets | `application/json;charset=UTF-8` (2 features
GeoJSON avec géométries MultiPolygon).

**Quota** : WFS = **30 requêtes/s par IP** (rate limiting depuis le 25/02/2025). Au-delà → HTTP 429
+ blocage 5 s (header `retry-after`).

**Licence** : Licence Ouverte 2.0 (Etalab) — données INPN/Patrinat (MNHN/OFB). Usage commercial
autorisé, attribution requise.

#### 2.1.2 Pattern d'intégration GeoSylva

La requête WFS par BBOX (ou par filtre spatial `INTERSECTS` sur la géométrie de la parcelle) permet
de répondre à la question « ma parcelle intersecte-t-elle un espace protégé ? » en une seule requête
par couche. Pour une parcelle ponctuelle, on peut aussi filtrer par `geom` GeoJSON (cf. API Carto
REST §2.2 pour l'urbanisme, même principe).

---

### 2.2 API Carto Urbanisme — PLU, POS, CC, Servitudes

**Objectif** : récupérer les contraintes d'urbanisme (zonage PLU, prescriptions, servitudes)
intersectant une parcelle.

#### 2.2.1 Endpoint REST API Carto (recommandé — le plus simple)

- **URL de base** : `https://apicarto.ign.fr/api/gpu/`
- **Méthode** : GET
- **Clé API** : **non requise** (endpoint public)
- **Format** : JSON/GeoJSON, projection WGS84 (lon, lat)
- **Filtrage** : par attribut (`?nom_attribut=valeur`) ou par intersection géométrique
  (`?geom={GeoJSON}`)

**Endpoints documentés** (doc PDF IGN) :

| Endpoint | Donnée |
|----------|--------|
| `/gpu/document` | Documents d'urbanisme (PLU, POS, PLUi, CC, PSMV) intersectant la géométrie |
| `/gpu/zone-urba` | Zonages des documents d'urbanisme (zones A/N/U/...) |
| `/gpu/secteur-cc` | Secteurs des cartes communales |
| `/gpu/prescription-pct` `/prescription-lin` `/prescription-surf` | Prescriptions ponctuelles/linéaires/surfaciques |
| `/gpu/info-pct` `/info-lin` `/info-surf` | Périmètres d'information ponctuels/linéaires/surfaciques |
| `/gpu/acte-sup` | Actes des servitudes d'utilité publique (SUP) |
| `/gpu/assiette-sup-p` `/assiette-sup-l` `/assiette-sup-s` | Assiettes des SUP |
| `/gpu/generateur-sup-p` `/generateur-sup-l` `/generateur-sup-s` | Générateurs des SUP |

**Requête test 1** — document d'urbanisme sur un point (Fontainebleau) :
```
GET https://apicarto.ign.fr/api/gpu/document
    ?geom={"type":"Point","coordinates":[2.7,48.4]}
```
**Réponse réelle** : **HTTP 200** | 17 251 octets | `application/json; charset=utf-8`
```json
{
  "type": "FeatureCollection",
  "features": [
    {"properties": {"du_type": "PLUi", "status": null, "grid": null}}
  ]
}
```
> ✅ La commune testée est couverte par un **PLUi** (Plan Local d'Urbanisme intercommunal) —
> information directement exploitable pour alerter l'utilisateur.

**Requête test 2** — zonage PLU sur le même point :
```
GET https://apicarto.ign.fr/api/gpu/zone-urba
    ?geom={"type":"Point","coordinates":[2.7,48.4]}
```
**Réponse réelle** : **HTTP 200** | 310 671 octets | `application/json; charset=utf-8`
```json
{
  "features": [
    {"properties": {"libelle": "N", "typezone": "N", "libelong": ""}}
  ]
}
```
> ✅ Zone **N** (naturelle / non constructible) retournée — cohérent pour un point en forêt
> domaniale de Fontainebleau. Le champ `typezone` suit la nomenclature nationale (A=agricole,
> N=naturelle, U=urbaine, AU=à urbaniser).

**Quota** : l'API Carto REST n'est pas listée dans le tableau de rate-limiting Géoplateforme
(https://ignf.github.io/.../limites-d-usage) — elle est gérée séparément. Pas de limite documentée
au moment du test, mais un fair-use implicite s'applique. [À VÉRIFIER MANUELLEMENT] : confirmer
l'absence de quota strict auprès de la doc API Carto en cas d'usage intensif.

**Licence** : Licence Ouverte 2.0 (Etalab) — données GPU. Usage commercial autorisé.

#### 2.2.2 Endpoint WFS équivalent (Géoplateforme)

Pour un accès bas-niveau (WFS standard), les mêmes données sont servies via `data.geopf.fr/wfs` :

| Couche WFS | Donnée |
|------------|--------|
| `wfs_du:doc_urba` | Documents d'urbanisme (PLU, POS, CC, PSMV) |
| `wfs_du:doc_urba_com` | Documents d'urbanisme communaux |
| `wfs_du:zone_urba` | Zonages des documents d'urbanisme |
| `wfs_sup:servitude` | Servitudes d'utilité publique |
| `wfs_sup:servitude_acte_sup` | Actes des SUP |
| `wfs_scot:doc_urba` | Schémas de Cohérence Territoriale (SCoT) |

**Quota WFS** : 30 requêtes/s par IP (cf. §2.1.1).

> **Recommandation** : préférer l'**API Carto REST** (`apicarto.ign.fr`) pour GeoSylva — plus
> simple (GeoJSON en entrée/sortie, filtrage par `geom`), moins verbeux que WFS, et conçue pour
> l'interrogation par géométrie de parcelle. Le WFS reste utile pour des exports massifs.

---

### 2.3 BD Ortho — Orthophotos 50cm

**Objectif** : photo-interprétation des peuplements (superposer une orthophotographie récente sur la
parcelle pour identifier la canopée, les coupes, les lisières).

> **Déjà intégrée dans GeoSylva** — cette couche est testée ici pour confirmer son fonctionnement
> sans clé et documenter les quotas.

#### 2.3.1 Endpoint WMTS (tuiles précalculées, recommandé pour la carte)

- **URL** : `https://data.geopf.fr/wmts`
- **Méthode** : GET
- **Clé API** : **non requise**
- **Couche** : `ORTHOIMAGERY.ORTHOPHOTOS`
- **Format** : `image/jpeg`
- **TileMatrixSet** : `PM` (Pseudo-Mercator EPSG:3857, niveaux 0-21)
- **Résolution** : 50 cm en France métropolitaine (jusqu'à zoom 20-21 selon zone)

**Requête test** (tuile Fontainebleau, zoom 16, x=33259 y=22670) :
```
GET https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0
    &STYLE=normal&FORMAT=image/jpeg&TILEMATRIXSET=PM
    &TILEMATRIX=16&TILEROW=22670&TILECOL=33259
    &LAYER=ORTHOIMAGERY.ORTHOPHOTOS
```
**Réponse réelle** : **HTTP 200** | 23 266 octets | `image/jpeg` ✅

**Quota** : WMTS = **PAS de rate limiting** (exception explicite dans la doc IGN — les tuiles WMTS
sont précalculées et mises en cache CDN, donc non limitées). C'est l'API idéale pour le
téléchargement hors-ligne massif de GeoSylva (`OfflineTileManager.kt`).

**Licence** : Licence Ouverte 2.0 (Etalab) — BD Ortho IGN. Attribution requise
(`"IGN Géoportail — Licence Ouverte 2.0 (Etalab)"`, déjà codé dans `MapScreen.kt:886`).

#### 2.3.2 Pattern GeoSylva existant (confirmé fonctionnel)

- `MapScreen.kt:880-883` — fonction `geopfLayer()` construit l'URL WMTS template `{z}/{x}/{y}` :
  ```kotlin
  private fun geopfLayer(layer: String, format: String = "image/png") =
      "https://data.geopf.fr/wmts?" +
      "SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&FORMAT=$format" +
      "&TILEMATRIXSET=PM&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&LAYER=$layer"
  ```
- `MapScreen.kt:932,936,965,975` — couche `ORTHO_IGN` déjà déclarée et utilisée comme fond de carte.
- `OfflineTileManager.kt:130-160` — téléchargement de tuiles avec retry/backoff, User-Agent
  conforme (`GeoSylva/2.3.0`), 6 téléchargements parallèles, max 6000 tuiles/lot.
- Aucune clé API n'est utilisée nulle part dans le code — confirmation que les endpoints publics
  fonctionnent sans authentification.

---

### 2.4 Corine Land Cover — Occupation des sols (44 classes)

**Objectif** : cartographier l'interface forêt / agriculture / urbain autour d'une parcelle pour le
diagnostic de station et le contexte paysager.

#### 2.4.1 Endpoint WMS-Raster (rendu cartographique)

- **URL** : `https://data.geopf.fr/wms-r/wms`
- **Méthode** : GET (GetMap)
- **Clé API** : **non requise**
- **Couche** : `LANDCOVER.CLC18_FR` (CLC 2018 France métropolitaine ; aussi `CLC00_FR`,
  `CLC06_FR`, `CLC12_FR` pour les millésimes historiques, et `_DOM` pour les DOM)
- **⚠️ Style** : `STYLES=` (paramètre **vide** requis — le style `normal` n'existe pas pour cette
  couche et renvoie HTTP 400 `Style normal is not available`)

**Requête test** (GetMap sur Fontainebleau) :
```
GET https://data.geopf.fr/wms-r/wms?SERVICE=WMS&REQUEST=GetMap&VERSION=1.3.0
    &LAYERS=LANDCOVER.CLC18_FR&STYLES=&FORMAT=image/png
    &CRS=EPSG:4326&BBOX=48.3,2.6,48.5,2.8&WIDTH=400&HEIGHT=400
```
**Réponse réelle** : **HTTP 200** | 123 397 octets | `image/png` ✅

> ⚠️ **Piège** : contrairement à BD Ortho/BD Forêt qui utilisent `STYLE=normal`, CLC exige
> `STYLES=` (vide). Omettre le paramètre renvoie HTTP 400 `STYLES query parameter missing`.
> [À VÉRIFIER MANUELLEMENT] : la légende des 44 classes (codes 311 "Forêts de feuillus", 312
> "Forêts de conifères", 313 "Forêts mélangées", 324 "Forêts et végétation arbustive en mutation"…)
> est disponible via `GetLegendGraphic` ou la doc SDES — à intégrer pour l'affichage utilisateur.

**Quota** : WMS-Raster = **40 requêtes/s par IP**.

#### 2.4.2 Endpoint WFS (données vectorielles — codes CLC exploitables programmatiquement)

- **URL** : `https://data.geopf.fr/wfs`
- **Couche** : `LANDCOVER.CLC18_FR:clc18_fr`
- **Clé API** : non requise

**Requête test** (GetFeature, BBOX Fontainebleau, 2 features) :
```
GET https://data.geopf.fr/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0
    &TYPENAMES=LANDCOVER.CLC18_FR:clc18_fr&STARTINDEX=0&COUNT=2
    &BBOX=2.6,48.3,2.8,48.5,EPSG:4326&OUTPUTFORMAT=application/json
```
**Réponse réelle** : **HTTP 200** | 5 090 894 octets | `application/json;charset=UTF-8`
```json
{
  "features": [
    {"properties": {"code_18": "523", "area": null, "perimeter": null}},
    {"properties": {"code_18": "211", "area": null, "perimeter": null}}
  ]
}
```
> ✅ Codes CLC retournés : **523** = « Surfaces principalement occupées par agriculture, avec
> espaces naturels importants », **211** = « Terres arables hors périmètres d'irrigation ».
> Donnée vectorielle réelle, exploitable pour classifier le contexte de la parcelle.
> ⚠️ La réponse est volumineuse (5 Mo pour 2 features avec géométries complexes) — utiliser
> `STARTINDEX/COUNT` et un BBOX serré en production, et ne pas récupérer les géométries si seul le
> code est nécessaire (filtrer les propriétés).

**Quota** : WFS = 30 requêtes/s par IP.

**Licence** : Licence Ouverte 2.0 (Etalab) — CLC produite par le SDES (MTECT) dans le cadre
Copernicus (Agence européenne pour l'environnement). Usage commercial autorisé.

---

### 2.5 Géoportail de l'IGN — Clé API et conditions d'obtention

#### 2.5.1 Accès sans clé (endpoints publics — confirmé par tests)

Tous les tests ci-dessus (§2.1 à §2.4) ont été réalisés **sans clé API** et ont retourné HTTP 200.
La Géoplateforme expose des **endpoints publics** pour les données ouvertes (Licence Ouverte 2.0) :
- WMTS (`data.geopf.fr/wmts`) — BD Ortho, BD Forêt, Plan IGN, Cadastre, RPG, MNT, CLC…
- WMS-R (`data.geopf.fr/wms-r`) — rendu raster de toutes les couches ouvertes
- WMS-V (`data.geopf.fr/wms-v`) — rendu vecteur (urbanisme, SUP)
- WFS (`data.geopf.fr/wfs`) — données vectorielles (Natura 2000, ZNIEFF, CLC, urbanisme…)
- API Carto REST (`apicarto.ign.fr/api`) — urbanisme, cadastre, AOC, codes postaux

> **Conclusion** : GeoSylva n'a **pas besoin de clé API** pour les 4 APIs testées. La demande de
> clé évoquée dans `RESEARCH_OPPORTUNITIES.md` §1.5 n'est nécessaire que pour :
> - les données **restreintes** (ex. BD Topo complète, Scan25 haute résolution, certaines couches
>   LiDAR) ;
> - les **quotas supérieurs** au fair-use par IP ;
> - l'**API de téléchargement** massif (10 req/s) pour récupérer des jeux complets.

#### 2.5.2 Obtention d'une clé (si besoin futur)

1. Créer un compte gratuit sur https://cartes.gouv.fr (statut particulier ou professionnel).
2. Espace développeur → créer une clé d'accès (`POST /api/users/me/keys`).
3. Ajouter des accès (permissions) aux offres souhaitées.
4. Utiliser la clé : paramètre `api_key` en query, ou header `X-Key` / `apikey`, ou auth Basic/Bearer.
5. Options : whitelist/blacklist IP, forcer le referer/user-agent.

#### 2.5.3 Conditions d'usage non commercial

Les CGU Géoplateforme (v15/10/2024) ne distinguent pas usage commercial / non commercial pour les
données ouvertes : **Licence Ouverte 2.0 (Etalab)** autorise tout usage (commercial inclus) sous
réserve d'attribution. L'offre « Essentielle » (gratuite) couvre l'accès aux endpoints publics.
L'offre « Premium » (payante) débloque des données/services restreints.

> Pour GeoSylva (app commerciale) : **aucune restriction** sur les 4 APIs testées — elles sont en
> Licence Ouverte 2.0 et accessibles sans clé. Attribution déjà en place dans le code
> (`ATTR_IGN = "IGN Géoportail — Licence Ouverte 2.0 (Etalab)"`, `MapScreen.kt:886`).

---

## 3. Comparatif / analyse critique

| Critère | API Carto Nature (WFS) | API Carto Urbanisme (REST) | BD Ortho (WMTS) | Corine Land Cover (WMS/WFS) |
|---------|------------------------|----------------------------|-----------------|-----------------------------|
| Clé API | Non | Non | Non | Non |
| Format | GeoJSON (WFS) | GeoJSON (REST) | JPEG/PNG (tuiles) | PNG (WMS) / GeoJSON (WFS) |
| Quota | 30 req/s | Non documenté (fair-use) | **Illimité** | 40 req/s (WMS) / 30 (WFS) |
| Précision | Polygones sites | Polygones zonages | 50 cm raster | 25 ha min (polygones) |
| Fraîcheur | Patrinat (à jour) | GPU (moissonnage ATOM) | < 3 ans (millésimes) | 2018 (CLC 2024 à venir) |
| Licence | LO 2.0 | LO 2.0 | LO 2.0 | LO 2.0 (SDES/Copernicus) |
| Intégration GeoSylva | Nouvelle | Nouvelle | **Déjà faite** | Nouvelle |
| Complexité intégration | Moyenne (WFS) | **Faible** (REST GeoJSON) | Nulle (existant) | Faible (WMS overlay) |

**Points d'attention** :
1. **CLC WMS** exige `STYLES=` vide (pas `normal`) — piège documenté §2.4.1.
2. **CLC WFS** retourne des réponses volumineuses (polygones 25 ha min) — filtrer BBOX serré +
   `COUNT` limité, et éviter de récupérer les géométries si seul le code est utile.
3. **Natura 2000** = 2 couches complémentaires (`sic` + `zps`) à interroger séparément (un site
   peut être SIC et/ou ZPS).
4. **API Carto REST** (urbanisme) non listée dans le rate-limiting Géoplateforme — quota incertain
   en cas d'usage intensif [À VÉRIFIER MANUELLEMENT].
5. **RNN/RNR** non exposées en WFS Géoplateforme → compléter par WFS INPN (cf.
   `04_apis_biodiversite_inpn_gbif.md`).

---

## 4. Recommandation pour GeoSylva

### 4.1 Intégrations prioritaires (ordre suggéré)

1. **API Carto Urbanisme REST** (`apicarto.ign.fr/api/gpu/`) — **priorité 1, effort faible**.
   - Cas d'usage : à la création/sélection d'une parcelle, interroger `/gpu/document` +
     `/gpu/zone-urba` avec la géométrie de la parcelle → afficher une bannière « PLU : zone N
     (non constructible) » ou « PLUi en cours ».
   - Fichier Kotlin cible : nouveau `ParcelleUrbanismeResolver.kt` dans `domain/location/` (même
     pattern que `LocalisationResolverService.kt` qui interroge déjà le WFS IGN/IFN pour la SER).
   - Requête type : `GET /api/gpu/zone-urba?geom={parcelleGeoJSON}` → parser `typezone` (A/N/U/AU).

2. **API Carto Nature WFS** (`data.geopf.fr/wfs`, couches `patrinat_*`) — **priorité 1, effort
   moyen**.
   - Cas d'usage : à la sélection d'une parcelle, interroger `patrinat_sic`, `patrinat_zps`,
     `patrinat_znieff1`, `patrinat_znieff2`, `patrinat_pn`, `patrinat_pnr` par BBOX ou
     `INTERSECTS(geom, parcelleWKT)` → alerter « ⚠️ Parcelle en ZNIEFF type 1 / Natura 2000 ».
   - Fichier Kotlin cible : nouveau `ProtectedAreaResolver.kt` dans `domain/location/`.
   - Attention : 6 requêtes WFS par parcelle (une par couche) — batch ou filtre spatial unique si
     possible. Quota WFS 30 req/s/IP largement suffisant pour un usage interactif (1 parcelle).

3. **Corine Land Cover WMS** (`LANDCOVER.CLC18_FR`) — **priorité 2, effort faible**.
   - Cas d'usage : overlay cartographique « occupation du sol » (contexte forêt/agri/urbain) +
     interrogation WFS `code_18` pour le diagnostic station (ex. détecter une lisière forêt/agri).
   - Fichier Kotlin cible : ajouter un preset dans `WmsLayerManager.kt` (cf. `PRESETS` ligne 28) :
     ```kotlin
     WmsLayerConfig(
         id = "preset_ign_clc",
         name = "IGN — Corine Land Cover 2018",
         url = "https://data.geopf.fr/wms-r/wms?SERVICE=WMS&REQUEST=GetMap&VERSION=1.3.0" +
               "&LAYERS=LANDCOVER.CLC18_FR&STYLES=&FORMAT=image/png&CRS=EPSG:3857" +
               "&BBOX={bbox}&WIDTH=256&HEIGHT=256",
         attribution = "IGN / SDES — Copernicus (LO 2.0)"
     )
     ```
   - ⚠️ Le preset WMS (GetMap dynamique) diffère du pattern WMTS (tuiles `{z}/{x}/{y}`) existant —
     nécessite une adaptation du `OfflineTileManager` ou un renderer WMS dédié.

4. **BD Ortho** — **déjà intégrée**, aucune action. Confirmer juste le bon millésime dans l'UI.

### 4.2 Architecture recommandée

- Suivre le pattern existant de `LocalisationResolverService.kt` (service `domain/location/` qui
  appelle les WFS/WMS IGN en arrière-plan, non bloquant, cache en DB Room).
- Réutiliser `SecureHttpClient` (validation de domaine) déjà utilisé pour les tuiles.
- Aucune clé API à gérer → pas de secret à stocker. Simplement un User-Agent conforme
  (`OfflineTileManager.kt:52`) et un respect des quotas (throttle à 25 req/s pour WFS, marge sous
  la limite 30).

### 4.3 Priorité

| API | Priorité | Effort | Valeur métier |
|-----|----------|--------|---------------|
| Carto Urbanisme (PLU) | P1 | Faible | Élevée (alerte réglementaire parcelle) |
| Carto Nature (Natura/ZNIEFF) | P1 | Moyenne | Élevée (obligation info sylviculteur) |
| Corine Land Cover | P2 | Faible | Moyenne (contexte paysager) |
| BD Ortho | P0 (déjà fait) | — | Élevée (photo-interprétation) |

---

## 5. Limites et points à vérifier manuellement

1. **[À VÉRIFIER MANUELLEMENT]** Quota exact de l'API Carto REST (`apicarto.ign.fr`) — non listé
   dans le tableau de rate-limiting Géoplateforme. Confirmer l'absence de limite stricte ou la
   valeur auprès de la documentation API Carto si usage intensif prévu (ex. batch de 1000 parcelles).
2. **[À VÉRIFIER MANUELLEMENT]** Légende des 44 classes CLC (codes 311/312/313/324…) — récupérer
   via `GetLegendGraphic` ou la doc SDES pour afficher la nomenclature dans l'UI utilisateur.
3. **RNN/RNR** : les réserves naturelles nationales/régionales ne sont pas en WFS Géoplateforme au
   moment du test. Compléter par le WFS INPN (cf. fiche `04_apis_biodiversite_inpn_gbif.md`) pour
   une couverture exhaustive des espaces protégés.
4. **Fraîcheur CLC** : CLC 2018 est le dernier millésime disponible sur la Géoplateforme au moment
   du test ; CLC 2024 est en production par le SDES/Copernicus (sortie attendue 2025-2026). Les
   couches `CLC00_FR` à `CLC18_FR` permettent le suivi temporel (changements d'occupation).
5. **Nomenclature `typezone` PLU** : les codes (A/N/U/AU) et sous-codes (Ah, Ns, AUb…) suivent la
   nomenclature nationale GPU mais peuvent varier selon les communes (libellés libres dans
   `libelong`). Prévoir un mapping code → libellé pédagogique dans GeoSylva.
6. **Géométrie d'entrée** : l'API Carto REST accepte une GeoJSON en `?geom=` (Point ou Polygon).
   Pour une parcelle cadastrale, récupérer d'abord le contour via l'API cadastre (déjà intégrée)
   puis l'envoyer à `/gpu/zone-urba` — vérifier la limite de taille de l'URL (GET) ; si contour
   trop verbeux, passer en POST (supporté par l'API Carto selon la doc).
7. **Tests réalisés le 2026-07-02** depuis une IP unique — les quotas (30/40 req/s) sont par IP ;
   en production, chaque utilisateur GeoSylva a sa propre IP mobile, donc pas de contention.
