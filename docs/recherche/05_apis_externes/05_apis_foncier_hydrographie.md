# APIs foncières et hydrographiques — Cadastre, DVF, BD Topage

**Domaine** : docs/recherche/05_apis_externes/
**Date de recherche** : 2026-07-02
**Agent** : apis-foncier-hydrographie

Ce document approfondit et teste réellement les APIs listées dans `RESEARCH_OPPORTUNITIES.md`
§1.1 (Cadastre, DVF — déjà intégrés) et §1.2/§1.3 (BD Topage, API Données Foncières Cerema — à
intégrer). Conformément à la méthodologie, chaque API est testée par requête HTTP réelle et la
réponse obtenue est documentée. Aucune donnée inventée.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| API Géoplateforme IGN — géocodage reverse cadastral | API REST | officielle (IGN) | https://data.geopf.fr/geocodage/reverse | 2026 (service en ligne) |
| API Données Foncières Cerema — DVF+ open-data | API REST/GeoJSON | officielle (Cerema/DGALN) | https://apidf-preprod.cerema.fr/ | 2026 (service en ligne) |
| BD Topage® — WFS Sandre | API OGC WFS 2.0 | officielle (Sandre/OFB/IGN) | https://services.sandre.eaufrance.fr/geo/topage | Millésime 2025 (publié 2025) |
| Documentation API Données Foncières (data.gouv) | Doc | officielle (Etalab/Cerema) | https://www.data.gouv.fr/dataservices/api-donnees-foncieres | 2026 |
| Documentation DVF+ open-data (Cerema) | Doc | officielle (Cerema) | https://datafoncier.cerema.fr/donnees/autres-donnees-foncieres/dvfplus-open-data | 2026 |
| Package R `apifoncier` (client officiel Cerema) | Doc/code | officielle (Cerema) | https://github.com/CEREMA/apifoncier | 2026 |
| Métadonnées BD Topage® (Sandre) | Doc | officielle (Sandre/OFB) | https://www.sandre.eaufrance.fr/atlas/atlas/api/records/7fa4c224-fe38-4e2c-846d-dcc2fa7ef73e | Édition 2024/2025 |

---

## 2. Tests réels d'API

Point de test géographique : commune de La Bourgonce (88, Vosges), lat=48.29, lon=6.78 — zone
forestière de l'est de la France, code INSEE 88068.

### 2.1 API Cadastre IGN/DGFiP (déjà intégrée — vérification de l'endpoint)

**Endpoint utilisé dans le code** :
`LocalisationResolverService.kt` ligne 143 :
```
https://data.geopf.fr/geocodage/reverse?lon=$lon&lat=$lat&index=parcel&limit=1
```

**Requête test** :
```
GET https://data.geopf.fr/geocodage/reverse?lon=6.78&lat=48.29&index=parcel&limit=1
```

**Réponse réelle obtenue (HTTP 200 OK)** :
```json
{
  "type": "FeatureCollection",
  "features": [{
    "type": "Feature",
    "geometry": {"type": "Point", "coordinates": [6.780638936186654, 48.289557733626225]},
    "properties": {
      "id": "880680000B0705",
      "departmentcode": "88",
      "municipalitycode": "068",
      "oldmunicipalitycode": "000",
      "districtcode": "000",
      "section": "0B",
      "sheet": "02",
      "number": "0705",
      "city": "La Bourgonce",
      "distance": 68,
      "score": 0.9932,
      "_type": "parcel"
    }
  }]
}
```

**Analyse de conformité avec le code Kotlin** : le code (`LocalisationResolverService.kt` lignes
142-150) lit les champs `codeinsee`, `commune`, `section`, `numero`, `contenance`, `nature`. Or la
réponse réelle renvoie `departmentcode`/`municipalitycode` (pas `codeinsee`), `city` (pas
`commune`), `section`, `number` (pas `numero`), et **aucun champ `contenance` ni `nature`** n'est
présent dans la réponse du géocodage reverse. → **[À VÉRIFIER MANUELLEMENT]** : les champs
`contenanceCadastraleHa` et `natureCadastraleCode` extraits par le code seront donc toujours
`null`/vides avec cet endpoint ; la contenance et la nature ne sont pas fournies par le service de
géocodage reverse — il faudrait interroger le WFS `CADASTRALPARCELS.PARCELLAIRE_EXPRESS` ou l'API
Cadastre Etalab pour obtenir ces attributs.

| Caractéristique | Valeur |
|-----------------|--------|
| Clé API requise | Non |
| Code HTTP | 200 OK |
| Format | GeoJSON |
| Quota | Pas de quota documenté explicitement (usage raisonnable) |
| Licence | Licence ouverte Etalab 2.0 (données cadastrales DGFiP/IGN) |
| Coût | Gratuit |

### 2.2 DVF Cerema — endpoint utilisé dans le code (test = ÉCHEC)

**Endpoint utilisé dans le code** :
`StationDataAggregator.kt` lignes 234-235 :
```
https://apidf-preprod.cerema.fr/dvf_opendata/geomutations/
    ?lat=$lat&lon=$lon&rayon=1000&nature_culture_code=B
```

**Requête test** :
```
GET https://apidf-preprod.cerema.fr/dvf_opendata/geomutations/?lat=48.29&lon=6.78&rayon=1000&nature_culture_code=B
```

**Réponse réelle obtenue** :
```
HTTP 403 Forbidden
```

**Diagnostic** : l'endpoint `apidf-preprod.cerema.fr` est bien le bon hôte (la racine répond 200 et
affiche « API Données Foncières — Documentation technique »), mais **les paramètres
`lat`/`lon`/`rayon`/`nature_culture_code` ne sont pas des paramètres valides** de la ressource
`dvf_opendata/geomutations/`. Le serveur renvoie 403 (et non 400) sur les requêtes mal formées.
→ **Bug critique confirmé** : l'intégration DVF actuelle de GeoSylva est non fonctionnelle — la
requête ne retournera jamais de résultat en production. Voir §2.3 pour l'usage correct.

### 2.3 API Données Foncières Cerema — DVF+ open-data (usage correct)

**Endpoints corrects** (documentés via le client officiel `apifoncier` et la data.gouv) :
- `GET /dvf_opendata/geomutations/?code_insee=...` — mutations géolocalisées (GeoJSON)
- `GET /dvf_opendata/mutations/?code_insee=...` — mutations en liste (JSON paginé)

**Requête test 1 (geomutations, foncier nu)** :
```
GET https://apidf-preprod.cerema.fr/dvf_opendata/geomutations/?code_insee=88068&codtypbien=2&anneemut_min=2020
```

**Réponse réelle (HTTP 200 OK)** — `count: 112`, GeoJSON avec géométries MultiPolygon. Extrait de
la 1ère feature :
```json
{
  "id": 7182253, "type": "Feature",
  "geometry": {"type": "MultiPolygon", "coordinates": [[[6.841098,48.30492], ...]]},
  "properties": {
    "idmutinvar": "269d8befdfe1edf339000c1635673c0c",
    "datemut": "2020-06-04", "anneemut": 2020, "coddep": "88",
    "libnatmut": "Vente", "vefa": false,
    "valeurfonc": "36000.00",
    "l_codinsee": ["88068"],
    "nbpar": 3, "l_idpar": ["880680000C1270","880680000C1272","880680000C1274"],
    "sterr": "1844.00", "sbati": "0.00",
    "codtypbien": "2313", "libtypbien": "TERRAIN DE TYPE TERRE ET PRE"
  }
}
```

**Requête test 2 (mutations, tous types, 2022+)** :
```
GET https://apidf-preprod.cerema.fr/dvf_opendata/mutations/?code_insee=88068&anneemut_min=2022
```

**Réponse réelle (HTTP 200 OK)** — `count: 108`, JSON paginé. Exemples de mutations forestières
détectées dans la commune test :
- `idmutation 10407731` : `codtypbien=232`, `libtypbien="TERRAIN FORESTIER"`, `valeurfonc=340.00`,
  `sterr=2250.00` → **0,15 €/m²** (parcelle 880680000C0842)
- `idmutation 10667985` : `codtypbien=232`, `libtypbien="TERRAIN FORESTIER"`, `valeurfonc=607.50`,
  `sterr=1215.00` → **0,50 €/m²** (parcelles 880680000A0735/A0737)

**Codes de type de bien pertinents pour GeoSylva** (issus des réponses réelles) :
| `codtypbien` | `libtypbien` | Pertinence forestière |
|--------------|--------------|----------------------|
| `232` | TERRAIN FORESTIER | **Cible principale** — ventes de parcelles forestières |
| `2313` | TERRAIN DE TYPE TERRE ET PRE | Foncier rural non forestier (comparables) |
| `20` | TERRAIN NON BATIS INDETERMINE | À filtrer (type non qualifié) |
| `2` | (groupe foncier nu) | Filtre large tous terrains nus |
| `111` | UNE MAISON | À exclure (bâti) |

| Caractéristique | Valeur |
|-----------------|--------|
| Clé API requise | Non pour DVF+ open-data ; **oui (compte) pour DV3F** (accès restreint) |
| Code HTTP | 200 OK (avec `code_insee`) |
| Format | GeoJSON (`geomutations`) / JSON paginé (`mutations`) |
| Pagination | Oui (`page` param, `next`/`previous` dans la réponse) |
| Quota | Non documenté explicitement (usage raisonnable) |
| Licence | DVF+ open-data = Licence Ouverte Etalab 2.0 (données DVF DGFiP) |
| Coût | Gratuit (DVF+) ; DV3F sur demande d'accès |
| Paramètres de filtre clés | `code_insee`, `codtypbien`, `anneemut_min`/`anneemut_max`, `valeurfonc_min`/`max`, `sterr_min`/`max`, `in_bbox` |

**Note** : la recherche par coordonnée GPS n'est pas disponible via `lat/lon/rayon`. L'API accepte
`in_bbox` (emprise rectangulaire `minx,miny,maxx,maxy`) ou `code_insee`. GeoSylva doit donc
d'abord résoudre le code INSEE de la commune (déjà disponible via le cadastre §2.1 ou GeoAPI) puis
interroger DVF par `code_insee`.

### 2.4 BD Topage® (Sandre) — WFS

**Endpoint** : `https://services.sandre.eaufrance.fr/geo/topage` (service WFS 2.0.0 OGC)

**Requête test 1 (GetCapabilities)** :
```
GET https://services.sandre.eaufrance.fr/geo/topage?SERVICE=WFS&REQUEST=GetCapabilities&VERSION=2.0.0
```

**Réponse réelle (HTTP 200 OK)** — XML Capabilities valide. FeatureTypes disponibles :
`sa:CoursEau`, `sa:TronconHydrographique`, `sa:PlanEau`, `sa:NoeudHydrographique`,
`sa:SurfaceElementaire`, `sa:BassinHydrographique`, `sa:BassinVersantTopographique`,
`sa:LimiteTerreMer` — déclinés par contexte géographique (`_FXX` métropole, `_MTQ`, `_MYT`,
`REU`, `GUF`). CRS supportés : EPSG:4326, EPSG:2154 (Lambert-93), EPSG:3857, etc. Formats de
sortie : GML 3.2/3.1/2.1, **GeoJSON**, Shapefile, CSV, GeoPackage.

**Requête test 2 (GetFeature — cours d'eau, GeoJSON)** :
```
GET https://services.sandre.eaufrance.fr/geo/topage?SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0&TYPENAMES=sa:CoursEau&COUNT=2&SRSNAME=urn:ogc:def:crs:EPSG::4326&OUTPUTFORMAT=application/json;%20subtype=geojson
```

**Réponse réelle (HTTP 200 OK)** — GeoJSON valide avec 2 features. Extrait :
```json
{
  "type": "FeatureCollection", "name": "CoursEau",
  "features": [{
    "type": "Feature",
    "properties": {
      "gid": 103887,
      "CdOH": "05C0000002215483140",
      "TopoOH": "Ruisseau de Crabarie",
      "SourceNomOH": "IGN",
      "DateMajOH": "2024-09-20T06:17:19.300Z",
      "StatutOH": "Validé",
      "ProjCoordOH": "WGS84G"
    },
    "geometry": {"type": "LineString", "coordinates": [[1.418548,42.953505], ...]}
  }, {
    "type": "Feature",
    "properties": {
      "gid": 103888, "TopoOH": "Ruisseau de Laurels", "StatutOH": "Validé", ...
    },
    "geometry": {"type": "LineString", "coordinates": [[1.397974,42.958760], ...]}
  }]
}
```

**Attributs exploitables pour GeoSylva** : `TopoOH` (nom du cours d'eau), `CdOH` (identifiant
Sandre), `StatutOH` (Validé/…), `DateMajOH`, géométrie `LineString` métrique (précision BD TOPO®).
Pour les tronçons hydrographiques (`sa:TronconHydrographique`), des attributs supplémentaires
(topologie, sens d'écoulement) sont disponibles — à tester par GetFeature ciblé.

| Caractéristique | Valeur |
|-----------------|--------|
| Clé API requise | Non |
| Code HTTP | 200 OK (Capabilities + GetFeature) |
| Format | GML 3.2 par défaut ; **GeoJSON** via `OUTPUTFORMAT=application/json; subtype=geojson` |
| Quota | Non documenté (service public INSPIRE, usage raisonnable) |
| Licence | Licence Ouverte 2.0 (https://www.etalab.gouv.fr/wp-content/uploads/2017/04/ETALAB-Licence-Ouverte-v2.0.pdf) |
| Coût | Gratuit |
| Précision | Grande échelle métrique (issu BD TOPO® + BD CARTHAGE®) |
| Millésime | 2025 (métropole), MAJ annuelle |
| Téléchargement bulk | Shapefile/GeoJSON/GeoPackage sur l'atlas-catalogue Sandre |

---

## 3. Comparatif / analyse critique

| Critère | Cadastre IGN (reverse) | DVF+ Cerema | BD Topage WFS |
|---------|------------------------|-------------|---------------|
| Statut intégration GeoSylva | Intégré (endpoint OK) | Intégré mais **BROKEN** (403) | Non intégré |
| Clé requise | Non | Non (DVF+) | Non |
| Licence | LO 2.0 | LO 2.0 | LO 2.0 |
| Précision géom. | Point (parcelle) | MultiPolygon (parcelle) | LineString métrique |
| Granularité requête | lat/lon | `code_insee` ou `in_bbox` | bbox / filtre spatial WFS |
| Couverture | France entière | France entière (hors Alsace-Moselle historique) | France entière + DROM |
| Fiabilité | officielle | officielle | officielle |

**Point critique DVF** : l'intégration actuelle (`StationDataAggregator.kt`) suppose une
recherche par rayon géographique (`lat/lon/rayon`) avec filtre `nature_culture_code=B`, qui
n'existe pas dans l'API réelle. L'API DVF+ fonctionne par **commune (code INSEE)** ou **bbox**, et
filtre par **`codtypbien`** (code de type de bien DVF+, ex. `232` = terrain forestier) — pas par
`nature_culture_code` (qui est un code DVF brut, non exposé par cette API). Les noms de champs
diffèrent aussi : l'API renvoie `valeurfonc`/`sterr` (sans underscore), alors que le code Kotlin
lit `valeur_fonciere`/`surface_terrain` (lignes 252-253) → même si la requête aboutissait, le
parsing échouerait silencieusement (renvoie `null`).

---

## 4. Recommandation pour GeoSylva

### 4.1 Correction urgente — DVF (priorité HAUTE, bug bloquant)

**Fichier concerné** : `app/src/main/java/com/forestry/counter/domain/location/StationDataAggregator.kt`
(méthodes `fetchDvfData` lignes 233-242 et `parseDvfResponse` lignes 244-265).

1. **Remplacer l'URL** : passer de `?lat=...&lon=...&rayon=1000&nature_culture_code=B` à
   `?code_insee={insee}&codtypbien=232&anneemut_min={annee-5}` (terrain forestier sur 5 ans).
   Le `code_insee` est déjà résolu par `LocalisationResolverService` (champ `codeInseeCommune`).
2. **Corriger le parsing** : lire `valeurfonc` (et non `valeur_fonciere`) et `sterr` (et non
   `surface_terrain`) ; calculer le prix €/m² = `valeurfonc / sterr`.
3. **Filtrer `codtypbien=232`** (TERRAIN FORESTIER) pour les comparables forestiers ; conserver
   `codtypbien=2313` (terre et pré) comme comparables ruraux secondaires.
4. **Gérer la pagination** : la réponse contient `next`/`previous` — pour une commune rurale
   forestière le volume reste faible (<200 mutations/5 ans), mais prévoir un fetch de la 2ᵉ page
   si `next` est non-null.
5. **Alternative bbox** : si le code INSEE est absent, utiliser `in_bbox={lon-0.05},{lat-0.05},
   {lon+0.05},{lat+0.05}` (≈ ±5 km) — testé conceptuellement d'après la doc `apifoncier`.

### 4.2 Vérification Cadastre (priorité MOYENNE)

**Fichier concerné** : `LocalisationResolverService.kt` lignes 52-59.

Les champs `contenance` et `nature` lus par le code ne sont **pas** renvoyés par l'endpoint de
géocodage reverse (confirmé par test réel). Pour obtenir la contenance (surface cadastrale en m²)
et la nature de parcelle, il faut compléter par une requête WFS GetFeature sur
`CADASTRALPARCELS.PARCELLAIRE_EXPRESS:parcelle` filtrée par `id=880680000B0705` (l'`id` renvoyé
par le reverse). À défaut, la `surfaceCadastraleHa` restera `null` en production — impact :
diagnostic stationnel privé de la surface officielle.

### 4.3 Intégration BD Topage (priorité MOYENNE — ripisylves et contraintes hydriques)

**Fichiers concernés** : nouvelle méthode dans `StationDataAggregator.kt` ou service dédié
(`HydrographieService`) ; couche WMS déjà déclarée dans `WmsLayerManager.kt` ligne 70-76
(`preset_ign_hydro` — couche IGN générique, à compléter par BD Topage pour la précision métrique).

1. **Détection ripisylve** : GetFeature WFS `sa:TronconHydrographique` avec filtre spatial
   `BBOX(geom, {lon-0.005},{lat-0.005},{lon+0.005},{lat+0.005})` → si un tronçon intersecte un
   buffer de 5-10 m autour de la parcelle, marquer `presenceRipisylve=true` et stocker le
   `TopoOH` (nom du cours d'eau). Cas d'usage : inventaires ripisylves, contraintes L.124-2 Code
   forestier (bande de ripisylve).
2. **Contraintes hydriques** : `sa:BassinVersantTopographique` permet d'identifier le bassin
   versant de la parcelle → utile pour le diagnostic stationnel (zones humides, risque
   d'engorgement).
3. **Format** : privilégier `OUTPUTFORMAT=application/json; subtype=geojson` (léger, parsable
   avec `JSONObject` comme déjà fait pour les autres APIs) plutôt que GML.
4. **Cache offline** : pour usage terrain hors connexion, télécharger les tronçons
   hydrographiques du département (GeoPackage bulk Sandre) au moment de l'initialisation de la
   zone de travail.

### 4.4 Cas d'usage GeoSylva couverts

| Cas d'usage | API | Statut |
|-------------|-----|--------|
| Limites de propriété (parcelle, section, numéro) | Cadastre reverse | OK (endpoint) / à compléter (contenance/nature) |
| Transactions foncières comparables (forestier) | DVF+ `codtypbien=232` | **À corriger** (bug 403) |
| Transactions rurales comparables (terre/pré) | DVF+ `codtypbien=2313` | À corriger (même bug) |
| Ripisylves (détection cours d'eau) | BD Topage WFS `sa:CoursEau`/`sa:TronconHydrographique` | À intégrer |
| Contraintes hydriques (bassin versant, zone humide) | BD Topage WFS `sa:BassinVersantTopographique` | À intégrer |

---

## 5. Limites et points à vérifier manuellement

1. **[À VÉRIFIER MANUELLEMENT]** — Le test du endpoint DVF par `lat/lon` a renvoyé 403 ; il
   n'existe pas de documentation officielle listant explicitement les paramètres rejetés. La
   conclusion (paramètres invalides) est déduite du contraste 403 (lat/lon) vs 200 (code_insee)
   sur le même hôte/chemin. Confirmer auprès de la doc Swagger
   (`apidf-preprod.cerema.fr/swagger/`, SPA non lisible par fetch automatique) que `lat/lon/rayon`
   ne sont effectivement pas supportés sur `dvf_opendata/geomutations/`.
2. **[À VÉRIFIER MANUELLEMENT]** — Les champs `contenance` et `nature` absents de la réponse de
   géocodage reverse : confirmer qu'il n'existe pas de paramètre optionnel (ex. `index=parcel` +
   un flag `attributes=full`) qui les exposerait. À défaut, planifier une requête WFS
   complémentaire sur `CADASTRALPARCELS.PARCELLAIRE_EXPRESS`.
3. **Couverture DVF** : les données DVF ne couvrent pas l'Alsace-Moselle (régime foncier
   spécifique, livres fonciers et non cadastre) pour les millésimes anciens — vérifier la
   couverture actuelle (le Cerema a étendu la géolocalisation, mais des trous subsistent).
4. **Quotas** : aucun quota explicite documenté pour les 3 APIs (IGN géocodage, DVF+, BD Topage
   WFS). Ce sont des services publics INSPIRE/open-data soumis à « usage raisonnable » — à
   surveiller en production (risque de throttling si l'app fait des requêtes massives au
   démarrage de l'agrégation stationnelle pour de nombreuses parcelles).
5. **BD Topage GetFeature par bbox** : le test réel a utilisé `COUNT=2` sans filtre spatial
   (récupère les 2 premiers cours d'eau de France entière). Un test par `BBOX` sur la zone de La
   Bourgonce n'a pas été effectué faute de syntaxe WFS 2.0 `BBOX` validée par fetch — à tester
   manuellement avec un client WFS (QGIS ou `urllib`) avant implémentation Kotlin.
6. **DV3F (accès restreint)** : non testé — nécessite un compte Cerema. DV3F enrichit DVF avec
   les Fichiers Fonciers (vendeurs/acheteurs, évolution construction) ; pertinent pour une
   analyse foncière avancée mais probablement hors périmètre GeoSylva (RGPD : données à caractère
   personnel indirectement dérivables). À évaluer en phase 2 si besoin.
