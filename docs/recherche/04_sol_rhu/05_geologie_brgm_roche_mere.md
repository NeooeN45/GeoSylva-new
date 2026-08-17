# Géologie BRGM & roche mère → sol → aptitude forestière

**Domaine** : docs/recherche/04_sol_rhu/
**Date de recherche** : 2026-07-03
**Agent** : geologie-brgm

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| BRGM — InfoTerre, Géoservices OGC (WMS/WFS) | officielle | officielle | https://infoterre.brgm.fr/page/geoservices-ogc | Consulté 2026-07-03 |
| BRGM — Téléchargement cartes géologiques (Bd Charm-50, 1/1M) | officielle | officielle | https://infoterre.brgm.fr/page/telechargement-cartes-geologiques | Science ouverte, depuis 2018/2019 |
| BRGM — Conditions d'utilisation des données | officielle | officielle | https://infoterre.brgm.fr/page/conditions-dutilisation-donnees | MàJ 2018-11-16 |
| BRGM — Notice WMS/WFS (PDF) | officielle | officielle | http://infoterre.brgm.fr/sites/default/files/upload/documents/brgm_notice_wms-wfs.pdf | s.d. |
| CNPF — Plaquette « Le sol forestier : élément clé pour le choix des essences » | tierce/pro. | commerciale/tierce | https://www.cnpf.fr/sites/socle/files/2024-07/plaquette_sols_cnpf.pdf | 2024 |
| Académie d'Agriculture — « Les sols forestiers » | scientifique | scientifique | http://academie-agriculture.fr/sites/default/files/publications/encyclopedie/les_sols_forestiers.pdf | s.d. |
| Gouvernement — « Les sols forestiers » (notre-environnement.gouv.fr) | officielle | officielle | https://www.notre-environnement.gouv.fr/themes/biodiversite/les-milieux-forestiers-ressources/article/les-sols-forestiers | 2015 (réf. IGN) |
| CNPF Bourgogne-Franche-Comté — Exigences stationnelles des essences | tierce/pro. | commerciale/tierce | https://bourgognefranchecomte.cnpf.fr/sites/socle/files/cnpf-old/srgs4_1_1.pdf | s.d. (SRGS) |
| CNPF IFC — Fiche essence chênes (sessile/pédonculé/pubescent) | tierce/pro. | commerciale/tierce | https://ifc.cnpf.fr/sites/ifc/files/2024-03/Fiche%20Essence%201%20-%20Che%CC%82ne%20Pe%CC%81doncule%CC%81%2C%20Sessile%20et%20Pubescent.pdf | 2024-03 |
| Parc du Morvan — Choix des essences (M.C. Deconninck) | tierce/pro. | commerciale/tierce | https://www.parcdumorvan.org/wp-content/uploads/2019/07/2-mc.deconninck-choix-essences.pdf | 2019 |
| HAL INRAE — Systèmes géopédologiques sur substratum carbonaté/gréseux (garrigues du Gard) | scientifique | scientifique | https://hal.inrae.fr/hal-02576132 | 2020 |
| GeoRezo — Forum « Connexion WMS BRGM » | tierce | commerciale/tierce | https://georezo.net/forum/viewtopic.php?id=56366 | s.d. |

## 2. Données détaillées

### 2.1 Le BRGM et la carte géologique de France

Le **BRGM** (Bureau de Recherches Géologiques et Minières) est le service géologique national français.
Il coordonne depuis 1968 le programme national de cartographie géologique de la France à différentes
échelles. Les produits clés pour GeoSylva :

| Produit | Échelle | Format | Accès | Précision spatiale |
|---------|---------|--------|-------|--------------------|
| Bd Million-Géol (carte litho. simplifiée) | 1/1 000 000 | Vecteur (SHP) / WFS | Téléchargement + WFS queryable | ~1 km (très coarse) |
| Bd Scan-Géol-250 | 1/250 000 | Image (WMS) | WMS (non queryable) | ~250 m |
| **Bd Charm-50** (carte géo. harmonisée vectorisée) | **1/50 000** | **Vecteur (SHP)** | **Téléchargement gratuit** | **~50 m (fine)** |
| Bd Scan-Géol-50 (image) | 1/50 000 | Image (WMS) | WMS (non queryable) | ~50 m |
| Banque du Sous-Sol (BSS) | Ponctuel | Forages/logs | WFS + téléchargement par dép. | Point (logs de forage) |

**Licence** : Licence Ouverte / Open Licence Etalab **Version 2.0** (confirmée sur la page
« Conditions d'utilisation des données », MàJ 2018-11-16). Libre, gratuite, sans restriction d'usage,
à condition de **citer la source (BRGM) et la date de dernière mise à jour**, et de ne pas altérer
le sens des données. URL licence :
https://www.etalab.gouv.fr/wp-content/uploads/2017/04/ETALAB-Licence-Ouverte-v2.0.pdf

### 2.2 Services d'accès (WMS / WFS / téléchargement)

Deux points d'accès OGC identifiés (confirmés par test réel, §3) :

- **`https://geoservices.brgm.fr/geologie`** — service OGC principal (WMS 1.1.1/1.3.0 + WFS 1.0.0).
  Couches WMS : `GEOLOGIE` (groupe), `SCAN_F_GEOL1M`, `SCAN_F_GEOL250`, `SCAN_D_GEOL50`,
  `SCAN_H_GEOL50` (harmonisée). **Aucune de ces couches WMS n'est queryable** (GetFeatureInfo
  renvoie « Requested layer(s) are not queryable » — testé §3).
- **`https://mapsref.brgm.fr/wxs/referentiel/geologie`** — service référentiel (couche
  `GEOL50_HARM`), **également non queryable** via GetFeatureInfo (testé §3).
- **Téléchargement direct** (Bd Charm-50 vectorielle 1/50k, Bd Million-Géol 1/1M) via formulaires
  InfoTerre — données vectorielles complètes avec attributs lithologiques.

**Conséquence technique majeure** : pour obtenir la **roche mère attributaire** d'un point GPS en
live (sans télécharger toute la Bd Charm-50), le seul service queryable est le **WFS sur la couche
`LITHO_1M_SIMPLIFIEE`** (carte lithologique simplifiée au 1/1M), testée et fonctionnelle §3. La
précision 1/1M est cependant trop coarse pour un diagnostic stationnel fin (cf. §5).

### 2.3 Lien roche mère → sol → aptitude forestière

La nature de la **roche mère** conditionne la pédogenèse et, donc, les caractéristiques du sol
(texture, pH, profondeur, réserve en eau, calcaire actif) qui déterminent l'aptitude des essences.
Le CNPF le résume explicitement (plaquette 2024, p. 10) :

> « La nature de la roche mère influence grandement la formation et les caractéristiques des sols. »

Principaux mécanismes (synthèse CNPF 2024 + Académie d'Agriculture + Gouvernement/IGN 2015) :

- **Texture** : l'altération de la roche mère fournit les éléments fins (argiles < 2 µm, limons
  2-50 µm, sables > 50 µm). Seuls argiles, limons et matière organique retiennent eau et nutriments.
- **pH** : en forêt, pH entre 3,5 (très acide) et 8,5 (très basique). Le pH gouverne la disponibilité
  des éléments minéraux (saturation H+/Al+++ en acide, Ca++ en basique → carences).
- **Calcaire actif** : libère des ions Ca++ en excès → chlorose (feuillus sensibles : chêne sessile,
  châtaignier, douglas, pin maritime/sylvestre).
- **Profondeur** : sous climat tempéré, de quelques cm à 1,5 m. Les sols peu épais sont moins
  favorables ; la plupart des essences de production exigent > 50-70 cm de sol prospectable.
- **Réserve utile en eau** : dépend de texture + profondeur + pierrosité. Sous 40 % de la RU max,
  un pin maritime adulte réduit sa photosynthèse/croissance.

### 2.4 Tableau roche mère → caractéristiques sol → essences adaptées

Tableau de synthèse croisant la typologie du CNPF (plaquette 2024, p. 10) et les exigences
stationnelles par essence (CNPF BFC, CNPF IFC, Parc du Morvan). Les libellés de roche mère
correspondent aux classes de la carte lithologique BRGM (1/1M et 1/50k).

| Roche mère (BRGM) | Texture sol dominante | pH | Profondeur / RU | Calcaire actif | Essences adaptées (optimum) | Essences à éviter |
|--------------------|-----------------------|-----|-----------------|----------------|-----------------------------|-------------------|
| **Granites & gneiss** | Sableuse, superficielle | Acide (4,5-5,5) | Faible à moyenne, filtrant | Non | Chêne sessile (sols filtrants acides), pin sylvestre, châtaignier, hêtre (si altitude/humidité), Douglas (si profondeur > 60 cm, pH acide), épicéa (montagne) | Chêne pédonculé (trop sec), frêne, feuillus calcicoles |
| **Schistes** | Argileuse en profondeur, limoneuse en surface | Acide à neutre | Moyenne, parfois hydromorphe | Non | Chêne sessile/pédonculé (selon alimentation eau), hêtre, frêne (si frais), épicéa, sapin (montagne) | Essences craignant l'hydromorphie si engorgement marqué |
| **Grès & sables** | Sableuse, perméable | Acide | Variable, faible RU si pur | Non | Pin sylvestre, pin maritime, chêne sessile, châtaignier | Chêne pédonculé (RU insuffisante si sableux pur), douglas si trop sec |
| **Craie** | Fine, perméable, argile de décarbonatation | Basique | Faible RU, risques chlorose | Oui (actif) | Chêne sessile (si sol profond sur craie fissurée), hêtre, alisier torminal, charme, pin sylvestre (si superficiel calcaire fissuré) | Chêne pédonculé (RU faible), châtaignier, douglas, pin maritime (chlorose) |
| **Calcaires durs** | Fine sur roche dure | Basique | Superficiel, enracinement contraint | Oui | Chêne pubescent, chêne vert (Sud), hêtre (si frais), alisier, charme, pin sylvestre | Châtaignier, douglas, pin maritime, chêne sessile si calcaire actif < 40 cm |
| **Marnes & argiles** | Argileuse, lourde | Neutre à basique | Profond mais compact, hydromorphe | Possible | Chêne pédonculé (sols frais profonds), frêne, aulne, charme, merisier (si bien drainé) | Douglas, épicéa (hydromorphie), châtaignier, pin sylvestre (compacité) |
| **Alluvions / limons** | Limoneuse, fertile | Neutre | Profond, bonne RU, frais | Variable | Chêne pédonculé (optimum sols profonds frais), frêne, merisier, aulne (hydromorphe), peuplier, noyer | Essences xérophiles, essences craignant l'engorgement selon drainage |
| **Basaltes & rhyolites** (roches magmatiques) | Sableuse à argileuse selon altération | Acide à neutre | Variable | Non | Chêne sessile, hêtre, sapin/épicéa (montagne), pin sylvestre | Selon altitude et profondeur — similaire au domaine granitique |

**Note chêne sessile vs pédonculé** (point critique pour GeoSylva, sources CNPF IFC 2024 + Parc du
Morvan + HAL) : le chêne sessile (*Quercus petraea*) est une essence « frugale » optimum sur **sols
filtrants, épais, légèrement acides** (limons sur granite/grès), résistante à la sécheresse ; le
chêne pédonculé (*Quercus robur*) exige des **sols profonds, riches, bien alimentés en eau toute
l'année** (argilo-limoneux frais, bas de versant, alluvions), tolère mieux le calcaire mais craint
les sécheresses estivales. Les dépérissements post-1976 et post-2003 sont largement liés à une
inadéquation essence-station (chêne pédonculé planté sur stations à chêne sessile). Les deux exigent
> 50-70 cm de profondeur pour produire du bois de qualité.

## 3. Test réel d'accès

### 3.1 WMS GetCapabilities (confirmé)

**Requête** :
```
GET https://infoterre.brgm.fr/geoserver/ows?service=WMS&request=GetCapabilities&version=1.3.0
```
**Résultat** : HTTP 200 — document XML GetCapabilities complet renvoyé (GeoServer MapServer,
titre « WMS GeoServer Web Map Service »). Confirme l'accessibilité du endpoint GeoServer.
Nombreux CRS supportés (EPSG:4326, 2154, 3857, etc.).

**Requête** (endpoint géologie dédié) :
```
GET https://geoservices.brgm.fr/geologie?service=WMS&request=GetCapabilities&version=1.3.0
```
**Résultat** : HTTP 200 — Capabilities complet. Couches identifiées : `GEOLOGIE`, `SCAN_F_GEOL1M`,
`SCAN_F_GEOL250`, `SCAN_D_GEOL50`, `SCAN_H_GEOL50`, `GEOLOGIE_OUTRE_MER`, etc. CRS supportés dont
EPSG:4326, 2154, 3857. Aucune clé API requise.

### 3.2 WMS GetFeatureInfo (échec — couches non queryables)

**Requête** :
```
GET https://geoservices.brgm.fr/geologie?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetFeatureInfo
  &LAYERS=SCAN_H_GEOL50&QUERY_LAYERS=SCAN_H_GEOL50&CRS=EPSG:4326
  &BBOX=48.39,2.69,48.41,2.71&WIDTH=101&HEIGHT=101&I=50&J=50
  &INFO_FORMAT=text/html&STYLES=
```
**Résultat** :
```
msWMSFeatureInfo(): WMS server error. Requested layer(s) are not queryable.
```
Idem sur `https://mapsref.brgm.fr/wxs/referentiel/geologie` couche `GEOL50_HARM` → même erreur.
**Conclusion** : les couches WMS géologiques BRGM ne supportent **pas** GetFeatureInfo. Impossible
de récupérer la roche mère attributaire d'un point via WMS seul.

### 3.3 WFS GetCapabilities (confirmé)

**Requête** :
```
GET https://geoservices.brgm.fr/geologie?SERVICE=WFS&REQUEST=GetCapabilities&VERSION=1.0.0
```
**Résultat** : HTTP 200 — WFS Capabilities complet. Feature types queryables identifiés, dont :
- **`LITHO_1M_SIMPLIFIEE`** — « Carte lithologique simplifiée au 1/1 000 000. Représente les roches
  dominantes du sous-sol en France. » ← **couche clé pour GeoSylva**
- `BSS_SEMIS_*`, `BSS_TOTAL_*` — semis de forages Banque du Sous-Sol
- `SCAN_H_GEOL50_PERIMETRE` — périmètres des cartes 1/50k harmonisées

### 3.4 WFS GetFeature — test réel sur 2 points forestiers (SUCCÈS)

**Test 1 — Forêt de Fontainebleau** (lat 48.40, lon 2.70 — grès de Fontainebleau attendu) :
```
GET https://geoservices.brgm.fr/geologie?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature
  &TYPENAME=LITHO_1M_SIMPLIFIEE&BBOX=2.69,48.39,2.71,48.41,EPSG:4326&MAXFEATURES=5
```
**Résultat** : HTTP 200 — GML renvoyé. Attributs extraits du feature :
- Lithologie : **« Calcaires, marnes et gypse »**
- Catégorie : **« Roches Sédimentaires »**

> ⚠ Le grès de Fontainebleau (formation locale) n'apparaît pas à l'échelle 1/1M : la carte
> simplifiée renvoie la lithologie dominante régionale (Bassin parisien = calcaires/marnes). Ceci
> illustre la **limite de précision du 1/1M** pour un diagnostic stationnel local (cf. §5).

**Test 2 — Morvan** (lat 47.05, lon 4.00 — socle granitique/volcanique attendu) :
```
GET https://geoservices.brgm.fr/geologie?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature
  &TYPENAME=LITHO_1M_SIMPLIFIEE&BBOX=3.99,47.04,4.01,47.06,EPSG:4326&MAXFEATURES=3
```
**Résultat** : HTTP 200 — GML renvoyé. Attributs extraits :
- Lithologie : **« Basaltes et rhyolites »**
- Catégorie : **« Roches Magmatiques »**

> Cohérent : le Morvan comporte d'importantes formations volcaniques (rhyolites/basaltes) à côté
> du granite, et la carte 1/1M renvoie la classe volcanique pour ce point.

**Synthèse du test réel** :
- Aucune clé API nécessaire, aucune authentification, aucune limite de quota apparente (usage
  raisonnable).
- Format de réponse : GML (text/xml). Attributs lithologiques exploitables par parsing XML/GML.
- Le WFS `LITHO_1M_SIMPLIFIEE` est **fonctionnellement intégrable** dans une app Android (requête
  HTTP GET simple, parsing GML léger).
- **Précision limitée au 1/1M** : pour un diagnostic stationnel fin, il faut télécharger la
  Bd Charm-50 (1/50k) et faire la requête attributaire localement (GeoPackage embarqué).

## 4. Recommandation pour GeoSylva

### Cas d'usage cible
À partir d'un **point GPS** (lat/lon), déterminer la **roche mère** via BRGM, en déduire une
**indication de sol** (texture/pH/profondeur attendus) et une **aptitude forestière** (essences
adaptées/à éviter) — couche complémentaire à BDGSF/SoilGrids (qui donnent le sol lui-même, pas la
roche mère).

### Approche recommandée (2 niveaux)

**Niveau 1 — Live, léger (court terme) : WFS `LITHO_1M_SIMPLIFIEE`**
- Requête HTTP GET sur `geoservices.brgm.fr/geologie` avec BBOX autour du point GPS.
- Parsing GML → extraction attribut « lithologie » + « catégorie ».
- Mapping lithologie BRGM → caractéristiques sol → essences (tableau §2.4, à coder en Kotlin dans
  une table de référence `GeologyToSoilMapping`).
- Affichage dans la fiche station : « Roche mère (BRGM 1/1M) : Granites → sol acide filtrant →
  optimum chêne sessile / pin sylvestre » avec mention explicite de la précision 1/1M.
- **Crédit obligatoire** : « Source : BRGM — Carte lithologique simplifiée 1/1M, [date d'accès] »
  (Licence Etalab 2.0).

**Niveau 2 — Embarqué, précis (moyen terme) : Bd Charm-50 (1/50k) en GeoPackage local**
- Télécharger la Bd Charm-50 vectorisée par département (ou France entière) depuis InfoTerre.
- Convertir en GeoPackage embarqué dans l'APK (ou téléchargement à la première utilisation).
- Requête spatiale locale (RTree) sur le point GPS → attribut lithologique fin 1/50k.
- Permet de capter les formations locales (ex. grès de Fontainebleau) invisibles au 1/1M.
- Volume à évaluer : la France entière 1/50k peut être lourde → envisager téléchargement par
  région/département à la demande.

### Fichiers Kotlin concernés (à créer/compléter)
- Nouveau : `GeologyRepository.kt` (data layer) — appels WFS BRGM + parsing GML.
- Nouveau : `GeologyToSoilMapping.kt` (domain layer) — table de correspondance roche mère → sol →
  essences (seed du tableau §2.4).
- Intégration dans le diagnostic stationnel existant (complémentaire à SoilGrids/BDGSF) — à relier
  au moteur de choix d'essences (`CanonicalEssences.kt` et fiches essences `06_essences/`).
- Priorité : **moyenne** — la roche mère est un indicateur indirect (le sol lui-même, donné par
  SoilGrids/BDGSF, est plus directement corrélé à l'aptitude). La géologie BRGM est surtout utile
  comme **couche d'interprétation/explication** (pourquoi tel sol ?) et en zone où les données
  pédologiques sont absentes.

## 5. Limites et points à vérifier manuellement

1. **Précision 1/1M insuffisante pour diagnostic fin** — confirmée par le test réel
   (Fontainebleau → « Calcaires, marnes » au lieu du grès local). La couche WFS queryable
   `LITHO_1M_SIMPLIFIEE` ne doit servir que d'**indication régionale** ; le diagnostic stationnel
   précis exige la Bd Charm-50 (1/50k) en local. [À VÉRIFIER MANUELLEMENT] : volume exact du
   GeoPackage 1/50k France entière et faisabilité d'embarquement/téléchargement à la demande dans
   l'APK.

2. **Aucun GetFeatureInfo WMS** — les couches WMS BRGM ne sont pas queryables (testé §3.2). Seul le
   WFS permet l'extraction attributaire live. Le WMS reste utile pour **affichage cartographique**
   (fond de carte géologique dans l'app), pas pour la requête par point.

3. **Mapping roche mère → sol → essences à affiner** — le tableau §2.4 est une synthèse
   pédagogique croisant CNPF (typologie roches) et fiches essences régionales (BFC, IFC, Morvan).
   Les correspondances sont **qualitatives** et simplifiées. [À VÉRIFIER MANUELLEMENT] : croiser
   avec les catalogues de stations régionaux (CSR) du CNPF/ONF et les SRGS par région pour un
   mapping quantitatif (pH, RU en mm, profondeur en cm) par type de station, plutôt que par roche
   mère seule. La roche mère n'est qu'un déterminant parmi d'autres (climat, topographie, hydromorphie).

4. **Classes lithologiques BRGM vs typologie CNPF** — les libellés exacts de la couche
   `LITHO_1M_SIMPLIFIEE` (ex. « Calcaires, marnes et gypse », « Basaltes et rhyolites ») et la liste
   complète des classes doivent être extraits du schéma GML / de la notice Bd Million-Géol pour
   bâtir la table `GeologyToSoilMapping` exhaustive. [À VÉRIFIER MANUELLEMENT] : télécharger la
   notice + le SHP 1/1M et lister toutes les valeurs distinctes de l'attribut lithologie.

5. **Pas de clé API mais pas de SLA garanti** — le service WFS BRGM est gratuit sans clé, mais le
   BRGM ne garantit pas la disponibilité (conditions d'utilisation §2.2 : « en l'état, sans
   garantie »). Pour un usage pro terrain hors-ligne, l'approche GeoPackage embarqué (Niveau 2)
   est plus robuste qu'un appel live.

6. **Complémentarité avec BDGSF/SoilGrids à formaliser** — la géologie (roche mère) explique le sol
   mais ne le remplace. GeoSylva doit présenter la géologie comme **couche explicative** et le sol
   (SoilGrids/BDGSF : texture, pH, profondeur mesurés/prédits) comme **couche décisionnelle** pour
   le choix d'essence. L'ordre de priorité dans le diagnostic stationnel reste à définir.
