# APIs Biodiversité — INPN WFS (MNHN) & GBIF

**Domaine** : docs/recherche/05_apis_externes/
**Date de recherche** : 2026-07-02
**Agent** : biodiversite-inpn-gbif

> Cas d'usage GeoSylva ciblé : **vérifier la présence d'espèces protégées et/ou d'habitats
> Natura 2000 sur une parcelle avant coupe** (contrainte réglementaire — directive Habitats,
> arrêtés de protection de biotope, ZNIEFF). L'objectif est de lever une alerte automatique dans
> l'app lorsqu'une parcelle saisie intersecte un zonage de protection ou qu'une espèce protégée
> est référencée à proximité.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| INPN WFS — Géoservice INSPIRE (MNHN) | Officielle (État) | officielle | https://inpn-inspire.mnhn.fr/geoservices/ows | ⚠️ Indisponible (HTTP 403) — attaque cybernétique MNHN 2025, durée indéterminée |
| INPN WFS — Miroir Carmen Carto (MNHN) | Officielle (hébergement tiers) | officielle | http://ws.carmencarto.fr/WFS/119/fxx_inpn | Testé 2026-07-02, opérationnel |
| PatriNat — page temporaire téléchargement référentiels | Officielle (OFB/MNHN/CNRS/IRD) | officielle | https://www.patrinat.fr/fr/page-temporaire-de-telechargement-des-referentiels-de-donnees-lies-linpn-7353 | 2025 (post-attaque) |
| GBIF API — Occurrence / Species / Dataset | Scientifique/intergouvernemental | scientifique | https://api.gbif.org/v1/ | Testé 2026-07-02, opérationnel |
| GBIF Developer Documentation | Scientifique | scientifique | https://techdocs.gbif.org/en/openapi/ | [À VÉRIFIER MANUELLEMENT — page openapi 404 au moment du test] |
| API Carto Nature (IGN) — référence croisée | Officielle (IGN) | officielle | (testée par un autre agent de la vague) | Non re-testée ici — voir fiche dédiée `01_apis_ign_carto_nature.md` [à créer par l'agent dédié] |

---

## 2. Tests réels d'API

### 2.1 INPN WFS — endpoint principal MNHN (inpn-inspire.mnhn.fr)

**Statut : INDISPONIBLE au moment du test.**

> Contexte critique : une **attaque informatique sévère a affecté les serveurs du MNHN** ; les
> sites de l'INPN sont « inaccessibles pour une durée indéterminée » (source : PatriNat, page
> temporaire de téléchargement). Les référentiels (TAXREF v18, HABREF v07, BDC v18) sont
> provisoirement mis à disposition en téléchargement direct (ZIP), mais **les web-services WFS/WMS
> hébergés sur `inpn-inspire.mnhn.fr` ne répondent plus**.

#### Test 1 — GetCapabilities (endpoint geoservices)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://inpn-inspire.mnhn.fr/geoservices/ows?service=WFS&version=1.1.0&request=GetCapabilities` |
| Clé requise | Non (service ouvert INSPIRE) |
| Code HTTP | **403 Forbidden** |
| Réponse | `HTTP request failed with status: 403` — aucune capabilities retournée |

#### Test 2 — GetCapabilities (endpoint geoserver)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://inpn-inspire.mnhn.fr/geoserver/ows?service=WFS&acceptversions=2.0.0&request=GetCapabilities` |
| Clé requise | Non |
| Code HTTP | **403 Forbidden** |
| Réponse | `HTTP request failed with status: 403` |

**Conclusion** : l'endpoint MNHN principal est inutilisable à court terme. Les couches
`INPN_INSPIRE:DHFF_2013_HABITATS`, `INPN_INSPIRE:DHFF_2013_ESPECES`, `INPN_INSPIRE:DHFF_2018_HABITATS`
(répartition des habitats/espèces Natura 2000) — qui seraient les plus pertinentes pour GeoSylva —
**ne sont pas accessibles** via cet endpoint tant que le MNHN n'a pas restauré ses serveurs.

---

### 2.2 INPN WFS — miroir Carmen Carto (opérationnel)

L'INPN est également diffusé via un serveur WFS hébergé par **Carmen Carto** (infrastructure
géo mutualisée, opérateur technique pour le MNHN). Cet endpoint **fonctionne** et expose les
couches de **zonages d'espaces protégés** (mais pas les couches de répartition d'espèces).

#### Test 3 — GetCapabilities (endpoint carmencarto)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `http://ws.carmencarto.fr/WFS/119/fxx_inpn?service=WFS&version=1.1.0&request=GetCapabilities` |
| Clé requise | Non |
| Code HTTP | **200 OK** |
| Extrait réponse | XML capabilities retourné. Titre : `MNHN - INPN - Metropole infoFeatureAccessService OGC WFS 1.1.0`. Versions supportées : WFS 1.0.0 et 1.1.0. Output formats : `text/xml; subtype=gml/2.1.2`, `text/xml; subtype=gml/3.1.1`, `SHAPE`, `MIDMIF`, `KML`. Contact : `sig_spn@mnhn.fr`. Emprise métropole : `-5.58 40.92 / 10.75 51.44` (WGS84). |

**Couches (FeatureTypes) exposées** (pertinentes pour GeoSylva) :

| Nom de couche | Description | Pertinence GeoSylva |
|---------------|-------------|---------------------|
| `Sites_d_importance_communautaire_JOUE__ZSC_SIC_` | ZSC/SIC au JOUE (Natura 2000 Habitats) | ⭐⭐⭐ Direct |
| `Sites_d_importance_communautaire` | Sites d'importance communautaire (SIC) | ⭐⭐⭐ Direct |
| `Zones_de_protection_speciale` | ZPS (Natura 2000 Oiseaux) | ⭐⭐⭐ Direct |
| `ZICO` | Zones Importantes pour la Conservation des Oiseaux | ⭐⭐ |
| `Znieff1` / `Znieff2` | ZNIEFF de type 1 et 2 (terrestres) | ⭐⭐⭐ Direct |
| `Znieff1_mer` / `Znieff2_mer` | ZNIEFF marines | ⭐ (hors périmètre forestier) |
| `Parcs_nationaux` | Parcs nationaux | ⭐⭐ |
| `Reserves_naturelles_nationales` | RNN | ⭐⭐⭐ Direct |
| `Reserves_biologiques` | Réserves biologiques (ONF/RFN) | ⭐⭐⭐ Direct |
| `Reserves_de_la_biosphere` | Réserves de biosphère UNESCO | ⭐ |
| `Sites_Ramsar` | Zones humides Ramsar | ⭐ |
| `Reserves_nationales_de_chasse_et_faune_sauvage` | RNCFS | ⭐ |

> **Note importante** : ce miroir expose les **zonages d'espaces protégés** (polygones), mais
> **pas les couches de répartition d'espèces** (`DHFF_*_ESPECES`) ni les **habitats Natura 2000**
> (`DHFF_*_HABITATS`) qui sont sur l'endpoint MNHN actuellement indisponible. Pour GeoSylva, ce
> miroir permet donc de répondre à « ma parcelle intersecte-t-elle un zonage réglementaire ? »
> mais **pas** à « telle espèce protégée est-elle présente sur ma parcelle ? ».

#### Test 4 — GetFeature sans filtre (schéma / géométrie)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `http://ws.carmencarto.fr/WFS/119/fxx_inpn?service=WFS&version=1.1.0&request=GetFeature&typeName=Znieff1&maxFeatures=2&outputFormat=text/xml;%20subtype=gml/3.1.1` |
| Code HTTP | **200 OK** |
| Extrait réponse | GML retourné. Coordonnées en **Lambert 93 (EPSG:2154)** par défaut — ex. premier sommet `581285.685400 6258931.585700`. Géométries polygonales (ZNIEFF). CRS supportés : 2154, 4258, 4326, 3857, 32630-32, 275xx (NTF), 3942-3950 (Lambert coniques), 3034/3035. |

#### Test 5 — GetFeature avec filtre spatial BBOX (cas d'usage GeoSylva : parcelle)

Simulation d'une parcelle forestière dans les Vosges (lon 6.5–6.55, lat 48.0–48.05, WGS84).

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `http://ws.carmencarto.fr/WFS/119/fxx_inpn?service=WFS&version=1.1.0&request=GetFeature&typeName=Znieff1&maxFeatures=3&BBOX=48.0,6.5,48.05,6.55,urn:ogc:def:crs:EPSG::4326&outputFormat=text/xml;%20subtype=gml/3.1.1` |
| Code HTTP | **200 OK** |
| Extrait réponse | GML retourné avec géométries en Lambert 93 (~`965000, 6775000` = zone Vosges). Le filtre BBOX en WGS84 (lat,lon selon ordre d'axe EPSG:4326 en WFS 1.1.0) est **fonctionnel** — seules les ZNIEFF intersectant la bbox sont renvoyées. |

**Validation du cas d'usage** : le filtre spatial BBOX fonctionne. GeoSylva peut donc envoyer la
bbox de la parcelle (convertie en WGS84) et récupérer les zonages protégés intersectés.
L'intersection géométrique précise (pas seulement bbox) devra être calculée côté client avec JTS
(cf. recommandation §4).

#### Quotas / licence (INPN WFS carmencarto)

| Item | Valeur |
|------|--------|
| Clé API | Non requise |
| Quota | Non documenté explicitement [À VÉRIFIER MANUELLEMENT] — service INSPIRE public, usage raisonnable implicite |
| Licence | « No Conditions Apply » (catalogue open-data) / Licence Ouverte Etalab 2.0 pour les données INPN |
| Coût | Gratuit |
| Format | WFS 1.0.0 / 1.1.0, GML 2.1.2 / 3.1.1, KML, SHAPE, MIDMIF |

---

### 2.3 GBIF API — occurrences (testé, opérationnel)

GBIF (Global Biodiversity Information Facility) agrège des données d'occurrences d'espèces
mondiales, dont les données françaises issues de l'INPN/SINP et d'iNaturalist.

#### Test 6 — Recherche d'occurrences par espèce + pays

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://api.gbif.org/v1/occurrence/search?country=FR&scientificName=Quercus%20robur&limit=3` |
| Clé requise | Non |
| Code HTTP | **200 OK** |
| Extrait réponse | `{"offset":0,"limit":3,"endOfRecords":false,"count":324591,"results":[...]}` — **324 591 occurrences** de *Quercus robur* en France. Champs clés par occurrence : `scientificName`, `decimalLatitude/Longitude`, `eventDate`, `basisOfRecord` (HUMAN_OBSERVATION), `license` (varie : CC-BY-NC 4.0 pour iNaturalist), `iucnRedListCategory` ("LC"), `stateProvince`, `gadm` (niveaux administratifs). |

#### Test 7 — Recherche d'occurrences par emprise géographique (polygone)

Simulation d'une zone forestière (Alpes-de-Haute-Provence, lon 6.0–6.5, lat 44.0–44.5).

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://api.gbif.org/v1/occurrence/search?country=FR&geometry=POLYGON((6.0%2044.0,6.5%2044.0,6.5%2044.5,6.0%2044.5,6.0%2044.0))&limit=3&hasCoordinate=true` |
| Code HTTP | **200 OK** |
| Extrait réponse | `{"count":934150,...}` — **934 150 occurrences** dans ce rectangle. Le paramètre `geometry` accepte un WKT POLYGON (WGS84, lon lat). Permet de filtrer par emprise de parcelle. |

#### Test 8 — Occurrences d'une espèce protégée (Lynx lynx, France)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://api.gbif.org/v1/occurrence/search?country=FR&scientificName=Lynx%20lynx&limit=1` |
| Code HTTP | **200 OK** |
| Extrait réponse | `{"count":9107,...}` — **9 107 occurrences** de Lynx boréal en France. ⚠️ **Point critique** : le champ `informationWithheld` contient `"Coordinate uncertainty increased to 26935m to protect threatened taxon"` — **les coordonnées des espèces menacées sont volontairement dégradées** (flou de ~27 km). `coordinateUncertaintyInMeters: 26935.0`. |

> **Conséquence majeure pour GeoSylva** : GBIF ne permet **pas** de localiser précisément une
> espèce protégée sur une parcelle — les coordonnées sont floutées pour les taxons menacés. GBIF
> est donc utile pour confirmer la **présence régionale** d'une espèce protégée (échelle
> département/massif) mais **pas** pour une détection à l'échelle de la parcelle. Le zonage
> réglementaire (INPN WFS) reste la couche primaire pour l'alerte parcelle.

#### Test 9 — Recherche taxonomique (espèces, statut IUCN)

| Item | Valeur |
|------|--------|
| Méthode | GET |
| URL | `https://api.gbif.org/v1/species/search?country=FR&status=ACCEPTED&rank=SPECIES&q=Lynx&limit=3` |
| Code HTTP | **200 OK** |
| Extrait réponse | `{"count":1745,...}` — retourne les usages taxonomiques acceptés avec `taxonKey`, `scientificName`, `canonicalName`, `kingdom`...`species`, `vernacularNames` (noms vernaculaires), `threatStatuses` (statut menace — souvent vide côté checklist, l'IUCN est plutôt sur l'occurrence via `iucnRedListCategory`). |

#### Quotas / licence (GBIF API)

| Item | Valeur |
|------|--------|
| Clé API | Non requise pour usage anonyme ; recommandée pour usage intensif (inscription gratuite) |
| Quota | Pas de limite dure documentée pour l'accès anonyme [À VÉRIFIER MANUELLEMENT — page openapi techdocs.gbif.org a retourné 404 au moment du test]. Usage raisonnable implicite ; GBIF recommande un `User-Agent` identifiant et propose un téléchargement asynchrone (`/occurrence/download/request`) pour les gros volumes. |
| Licence API | CC-BY 4.0 (API/métadonnées) |
| Licence données | **Variable par occurrence** — champ `license` dans chaque enregistrement (souvent CC-BY-NC 4.0 pour iNaturalist, CC0 pour certains datasets). ⚠️ Vérifier la licence de chaque occurrence avant redistribution. |
| Coût | Gratuit |
| Format | JSON (REST), aussi Darwin Core Archive pour téléchargements |

---

## 3. Comparatif / analyse critique

| Critère | INPN WFS (carmencarto) | INPN WFS (MNHN) | GBIF API |
|---------|------------------------|-----------------|----------|
| Disponibilité | ✅ Opérationnel | ❌ 403 (attaque MNHN) | ✅ Opérationnel |
| Clé API | Non | Non | Non (recommandée si volume) |
| Granularité géo | Polygones (zonages) | Polygones + points (espèces/habitats) | Points (occurrences) |
| Espèces protégées | ❌ (zonages seulement) | ⚠️ (couche DHFF espèces, indispo) | ✅ (mais coords floutées pour taxons menacés) |
| Habitats Natura 2000 | ❌ | ⚠️ (couche DHFF habitats, indispo) | ❌ |
| Zonages réglementaires | ✅ (ZNIEFF, N2000, RNN, RB) | ✅ (théorique) | ❌ |
| Précision parcelle | ✅ (polygones exacts) | ✅ (théorique) | ❌ (flou 27 km pour espèces menacées) |
| Licence | Licence Ouverte Etalab 2.0 | Licence Ouverte Etalab 2.0 | CC-BY 4.0 API / variable par occurrence |
| Fraîcheur données | [À VÉRIFIER MANUELLEMENT] | — | Temps réel (crawl iNaturalist quotidien) |
| Couverture | France métropole | France + DOM-TOM | Mondial |

**Synthèse** :
- Pour l'alerte « parcelle intersecte un zonage protégé » → **INPN WFS carmencarto** (opérationnel,
  polygones précis, Lambert 93).
- Pour la répartition d'espèces/habitats Natura 2000 → endpoint MNHN **indisponible** ; à
  surveiller pour réactivation. Alternative provisoire : téléchargement ZIP des référentiels
  (TAXREF, BDC statuts de protection) sur la page temporaire PatriNat, à intégrer en base locale.
- Pour la présence régionale d'espèces (contexte biodiversité) → **GBIF** (mais flou spatial sur
  les taxons menacés → usage contextuel seulement, pas décisionnel à la parcelle).

---

## 4. Recommandation pour GeoSylva

### Intégration prioritaire — INPN WFS carmencarto (alerte zonage parcelle)

1. **Client WFS** : implémenter un client WFS 1.1.0 léger (ou réutiliser une lib GIS existante
   dans le projet) qui interroge `http://ws.carmencarto.fr/WFS/119/fxx_inpn` avec un
   `GetFeature` + `BBOX` sur les couches prioritaires :
   - `Znieff1`, `Znieff2` (ZNIEFF — inventaire, non contraignant mais signalétique)
   - `Sites_d_importance_communautaire_JOUE__ZSC_SIC_` + `Zones_de_protection_speciale` (Natura 2000 — contrainte forte)
   - `Reserves_naturelles_nationales`, `Reserves_biologiques` (contrainte forte)
2. **Workflow** : à la saisie/création d'une parcelle (GPS ou cadastre), convertir l'emprise en
   WGS84 (via Proj4J), envoyer une requête BBOX par couche, puis effectuer l'**intersection
   géométrique précise côté client** avec JTS (le BBOX WFS est un pré-filtre, pas une intersection
   exacte) pour confirmer que la parcelle intersecte réellement le polygone de zonage.
3. **UI** : afficher un badge/alerte « ⚠️ Zone réglementée » sur la fiche parcelle avec le type
   de zonage (ZNIEFF I, Natura 2000 ZSC, RNN, etc.) et un rappel des obligations (évaluation
   d'incidences Natura 2000 pour toute coupe > seuil).
4. **Cache/offline** : les zonages changent peu — envisager un téléchargement périodique (mensuel)
   des couches en GeoPackage local pour fonctionnement offline (le forestier est souvent hors
   réseau). Le projet prévoit déjà GeoPackage Android (cf. `RESEARCH_OPPORTUNITIES.md` §2.1).
5. **Fichiers Kotlin concernés** (à localiser par l'agent Ingénieur) : couche de données
   parcelle (`*Parcelle*Repository*`), couche API externe (`*ApiService*` / `*WfsClient*` à
   créer), ViewModel de fiche parcelle pour l'alerte. Priorité : **P1** (contrainte réglementaire
   forte, valeur métier élevée).

### Intégration secondaire — GBIF (contexte biodiversité)

6. **Usage contextuel** : lors de la consultation d'une parcelle, afficher les espèces observées
   dans un rayon large (ex. 10 km) via `GET /v1/occurrence/search?geometry=POLYGON(...)&limit=50`
   pour fournir un **contexte naturaliste** (richesse spécifique, espèces patrimoniales
   potentielles). **Ne pas présenter cela comme une localisation précise** (flou spatial sur
   taxons menacés).
7. **Filtrage espèces protégées** : croiser les `taxonKey` GBIF avec le référentiel TAXREF/BDC
   (téléchargeable en ZIP sur PatriNat) pour identifier les espèces à statut de protection
   national/européen. Le statut IUCN est disponible via `iucnRedListCategory` sur l'occurrence.
8. **Licence** : filtrer les occurrences par `license` pour n'afficher que celles compatibles
   (éviter CC-BY-NC si redistribution). Priorité : **P2** (valeur d'information, pas décisionnel).

### Surveillance endpoint MNHN

9. Mettre en place un check périodique de `https://inpn-inspire.mnhn.fr/geoservices/ows` (passage
   de 403 → 200) pour basculer vers l'endpoint officiel dès sa restauration, qui expose les
   couches `DHFF_*_HABITATS` et `DHFF_*_ESPECES` (habitats et espèces Natura 2000 — la donnée
   manquante du miroir carmencarto). Priorité : **P3** (suivi).

---

## 5. Limites et points à vérifier manuellement

1. **Indisponibilité MNHN** : l'endpoint `inpn-inspire.mnhn.fr` retourne 403 (test 2026-07-02)
   suite à l'attaque cybernétique sur le MNHN. **Date de restauration inconnue** — vérifier
   régulièrement l'état via https://www.patrinat.fr/ ou https://inpn.mnhn.fr/.
2. **Couches espèces/habitats non accessibles** : le miroir carmencarto n'expose que les zonages
   d'espaces protégés, pas les couches `DHFF_*_ESPECES` / `DHFF_*_HABITATS` (répartition des
   espèces et habitats Natura 2000). [À VÉRIFIER MANUELLEMENT : existe-t-il un autre miroir ou
   un téléchargement direct de ces couches ?]
3. **Quotas WFS carmencarto** : non documentés explicitement — vérifier auprès de
   `sig_spn@mnhn.fr` ou Carmen Carto si GeoSylva génère un volume élevé (ex. batch de toutes les
   parcelles d'un gestionnaire).
4. **Quotas GBIF** : la page `https://techdocs.gbif.org/en/openapi/` a retourné 404 au moment du
   test — les limites exactes (req/min, req/jour) pour l'accès anonyme restent à confirmer dans
   la documentation GBIF courante. [À VÉRIFIER MANUELLEMENT]
5. **Flou spatial GBIF sur taxons menacés** : confirmé par le test (Lynx lynx →
   `coordinateUncertaintyInMeters: 26935m`). GeoSylva ne doit **jamais** présenter une occurrence
   GBIF d'espèce protégée comme localisée sur une parcelle — risque juridique et éthique
   (anti-braconnage).
6. **Licence variable GBIF** : chaque occurrence porte sa propre `license` (CC-BY-NC 4.0 fréquent
   pour iNaturalist) — un filtrage licence est nécessaire avant tout affichage/redistribution.
7. **Fraîcheur des zonages carmencarto** : date de mise à jour des couches non lue dans le
   GetCapabilities (champ non extrait par le test) — [À VÉRIFIER MANUELLEMENT] pour confirmer
   que les ZNIEFF/Natura 2000 sont à jour (révisions ZNIEFF, nouveaux sites Natura 2000).
8. **API Carto Nature (IGN)** : non re-testée ici (testée par un autre agent de la vague) —
   référence croisée à intégrer dans la fiche dédiée. API Carto Nature peut être une alternative
   ou un complément pour les zonages Natura 2000/ZNIEFF via l'infrastructure IGN (clé API IGN
   requise).
