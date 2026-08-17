# Méthode de calcul de la Réserve Utile (RU / RUM / RHU) en eau du sol

**Domaine** : docs/recherche/04_sol_rhu/
**Date de recherche** : 2026-07-03
**Agent** : sol-rhu

## Table des matières
1. [Définitions : RU, RUM, RUH, RFU, RDU](#1-définitions)
2. [Sources identifiées](#2-sources-identifiées)
3. [Formule de calcul](#3-formule-de-calcul)
4. [Tableau de référence texture → RU (Biljou/INRAE)](#4-tableau-de-référence-texture--ru)
5. [Méthode pratique des forestiers français (CNPF/ONF)](#5-méthode-pratique-des-forestiers-français)
6. [Lien avec le DHYa (Climessences)](#6-lien-avec-le-dhya-climessences)
7. [Comparatif / analyse critique](#7-comparatif--analyse-critique)
8. [Recommandation pour GeoSylva](#8-recommandation-pour-geosylva)
9. [Limites et points à vérifier manuellement](#9-limites-et-points-à-vérifier-manuellement)

---

## 1. Définitions

Les notions de réserve en eau du sol sont source de confusion fréquente. Voici la hiérarchie
précise des concepts, tels qu'utilisés en pédologie forestière française (INRAE, CNPF, GIS Sol) :

| Acronyme | Nom complet | Définition | Unité |
|----------|-------------|------------|-------|
| **RU** | Réserve Utile (Réservoir en eau Utile) | Quantité d'eau maximale que le sol peut contenir et restituer aux racines. Bornée par la capacité au champ (pF 2,5) et le point de flétrissement permanent (pF 4,2). | mm |
| **RUM** | Réserve Utile Maximale (Réservoir en eau Utilisable Maximal) | RU intégrée sur toute la profondeur de sol prospectable par les racines. C'est la **capacité maximale** du réservoir (taille du verre). | mm |
| **RUH / RUF** | Réserve Utile Humique / Profonde | Variante terminologique : la « réserve utile humique » désigne parfois la RU de l'horizon humifère de surface (horizon A), par opposition à la RU des horizons profonds. **Non standardisé** — usage variable selon catalogues. | mm |
| **RFU** | Réserve Facilement Utilisable | Fraction de la RU réellement accessible sans stress hydrique (confort hydrique). Varie de 1/3 (sable) à 2/3 (argile) de la RU. | mm |
| **RDU** | Réserve Difficilement Utilisable | Complément de la RFU dans la RU : eau extraite sous stress (réserve de survie). RFU + RDU = RU. | mm |

**Distinction clé RU vs RUM** : la RU est l'eau **réellement présente** à un instant t (variable
d'état dynamique), le RUM est la **capacité maximale** du réservoir sol (propriété statique du
sol). En pratique, les forestiers calculent le RUM pour caractériser une station ; Climessences
utilise le RUM comme paramètre d'entrée du bilan hydrique qui produit le DHYa.

> « La RUM représente la quantité maximale d'eau qu'un sol peut contenir. [...] On peut assimiler
> la RUM à la taille d'un verre, la RU étant la quantité d'eau réellement présente dans le verre. »
> — SILVAE AgroParisTech [Source 7]

---

## 2. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| GIS Sol / INRAE — « Le réservoir en eau utile des sols » | Officielle | Officielle | https://gissol.hub.inrae.fr/thematiques/sols-et-cycle-de-l-eau/le-reservoir-en-eau-utile-des-sols | 2023-11-30 |
| Biljou INRAE Nancy — « Réserve en eau du sol » (table pédotransfert Jamagne) | Scientifique | Officielle | https://appgeodb.nancy.inrae.fr/biljou/fr/fiche/reserve-en-eau-du-sol | 2024 (consulté 2026-07) |
| Arvalis — « Guide Réservoir en eau du sol utilisable » | Scientifique | Scientifique | https://sols-et-territoires.org/fileadmin/user_upload/archive/Produits_Reseau/documents_etudes/resultats_Axe2/Guide_ReservoirUtilisable_2022.pdf | Sept. 2022 |
| Al Majou et al. 2007 — CR Geoscience vol. 339 n°9, p.632-639 | Scientifique | Scientifique | https://comptes-rendus.academie-sciences.fr/geoscience/articles/10.1016/j.crte.2007.07.005/ | 2007 |
| Al Majou et al. 2008 — Soil Use & Management (FPT calée triangle européen 5 classes) | Scientifique | Scientifique | https://doi.org/10.1111/j.1475-2743.2008.00180.x | 2008 |
| Dobarco et al. 2021 — « Réservoir utile des sols de France métropolitaine » (carte 90 m) | Scientifique | Scientifique | https://doi.org/10.15454/9IRARJ | 2021 |
| Climessences — « Carte du Réservoir Utile Maximal » + indicateur DHYa | Officielle | Officielle (ONF/CNPF/Météo-France/AgroParisTech) | https://climessences.fr/modele-iks/donnees-climatiques/carte-du-reservoir-utile-maximal | V2 (2024) |
| CNPF — « Plaquette sols » (coefficients de texture) | Officielle | Officielle | https://www.cnpf.fr/sites/socle/files/2024-07/plaquette_sols_cnpf.pdf | 2024-07 |
| CNPF Normandie — Catalogue des types de station du Pays de Bray Normand | Officielle | Officielle | https://hautsdefrance-normandie.cnpf.fr/sites/socle/files/cnpf-old/c_pays_bray_normand.pdf | ~2010 |
| SILVAE AgroParisTech — « Réserve Utile Maximale des sols forestiers » | Scientifique | Scientifique | https://silvae.agroparistech.fr/home/?page_id=925 | 2011 (Piedallu et al.) |
| HAL INRAE hal-02808214 — RMQS / réservoir utile strate | Scientifique | Scientifique | https://hal.inrae.fr/hal-02808214/document | 2020 |
| Wikipédia — « Réserve utile en eau d'un sol » | Tertiaire | Tertiaire | https://fr.wikipedia.org/wiki/R%C3%A9serve_utile_en_eau_d'un_sol | 2025-08-15 |
| CTFC eforown — « Diagnostiquer les stations forestières » | Tertiaire | Commerciale/tierce | https://eforown.ctfc.cat/pdf/11_Diagnostiquer_les_stations_forestiere.pdf | ~2020 |

---

## 3. Formule de calcul

### 3.1 Formule fondamentale (par horizon)

La réserve utile d'un horizon se calcule à partir des humidités caractéristiques et de la profondeur.
Deux formulations équivalentes selon que l'on dispose des humidités **pondérales** (massiques, %) ou
**volumiques** (% ou mm/dm) :

**Forme volumique** (la plus directe) :
```
RU_horizon (mm) = (θcc − θpf) × épaisseur_horizon (dm)
```

**Forme pondérale** (requiert la densité apparente) :
```
RU_horizon (mm) = (wcc − wpf) × Da × épaisseur_horizon (dm)
```

où :
- **θcc** = humidité volumique à la capacité au champ (pF 2,5 ≈ -33 kPa), en cm³/cm³ ou mm/dm
- **θpf** = humidité volumique au point de flétrissement permanent (pF 4,2 ≈ -1580 kPa), en cm³/cm³
- **wcc, wpf** = humidités pondérales (% massique) équivalentes
- **Da** = densité apparente (masse volumique apparente, g/cm³ ou kg/dm³)
- **épaisseur** en décimètres (1 dm = 10 cm → ×10 pour passer en mm)

> Source : Arvalis Guide 2022 (Équations 1, 2, 3, 5) [Source 3] ; Wikipédia [Source 12].

### 3.2 Prise en compte de la pierrosité (éléments grossiers)

Pour un horizon **caillouteux** (éléments grossiers EG > 10 % en volume), la RU de la terre fine
est réduite par la fraction volumique occupée par les cailloux. La formule devient :

```
RU_horizon (mm) = (θcc − θpf) × épaisseur (dm) × (1 − EG/100)
```

> Source : Arvalis Guide 2022 (Équation pour horizon caillouteux) [Source 3] ; HAL INRAE
> hal-02808214 : `RUSTRATE = H × Réserve_en_eau × (1 − (EG/100))` [Source 11].

**Nuance** : les éléments grossiers retiennent eux-mêmes un peu d'eau (silex ~2 %, calcaires
>30 %), mais en pratique forestière on néglige cette rétention (conservatrice) sauf cas
particulier (Arvalis propose des valeurs de référence d'humidité utile des EG).

### 3.3 RUM total (somme des horizons)

```
RUM (mm) = Σ RU_horizon_i   (sur la profondeur prospectable par les racines, plafonnée à 1 m ou 2 m)
```

---

## 4. Tableau de référence texture → RU

### 4.1 Table de pédotransfert de référence (Biljou INRAE / triangle de Jamagne)

Cette table est la **référence opérationnelle** utilisée par le modèle de bilan hydrique forestier
Biljou© (INRAE UMR Silva, Nancy) et citée par la plaquette CNPF 2024. Elle donne, pour chaque
classe de texture du triangle de Jamagne (référence française), les humidités caractéristiques, la
densité apparente moyenne et le **réservoir utilisable en mm d'eau par cm de sol** (équivalent du
« coefficient U » des catalogues forestiers).

| Classe texture (Jamagne) | θcc % (pF 2,5) | θpf % (pF 4,2) | Eau utile (g/100g) | Da | **RU (mm/cm)** |
|--------------------------|:--------------:|:--------------:|:------------------:|:--:|:--------------:|
| S (Sableuse) | 8 | 3 | 5 | 1,35 | **0,70** |
| SL (Sablo-limoneuse) | 12 | 5 | 7 | 1,40 | **1,00** |
| SA (Sablo-argileuse) | 19 | 10 | 9 | 1,50 | **1,35** |
| LlS (Limon léger sableux) | 15 | 7 | 8 | 1,50 | **1,20** |
| LS (Limon sableux) | 19 | 9 | 10 | 1,45 | **1,45** |
| LmS (Limon moyen sableux) | 20 | 9 | 11 | 1,45 | **1,60** |
| LSA (Limon sablo-argileux) | 22 | 11 | 11 | 1,50 | **1,65** |
| LAS (Limon argilo-sableux) | 24 | 12 | 12 | 1,45 | **1,75** |
| Ll (Limon léger) | 17 | 8 | 9 | 1,45 | **1,30** |
| Lm (Limon moyen) | 23 | 10 | 13 | 1,35 | **1,75** |
| LA (Limon argileux) | 27 | 13 | 14 | 1,40 | **1,95** |
| AS (Argilo-sableuse) | 33 | 22 | 11 | 1,55 | **1,70** |
| A (Argileuse) | 37 | 25 | 12 | 1,45 | **1,75** |
| AL (Argilo-limoneuse) | 32 | 19 | 13 | 1,40 | **1,80** |
| A lourde (Argile lourde) | 29 | 18 | 11 | 1,50 | **1,65** |

> Source : Biljou INRAE Nancy [Source 2]. Table calée sur la base SOLHYDRO (Al Majou et al.).
> θcc = humidité pondérale à la capacité au champ (pF 2,5) ; θpf = humidité pondérale au point de
> flétrissement permanent (pF 4,2). La colonne « RU (mm/cm) » = (θcc − θpf) × Da / 10.
> Vérification : S → (8−3)×1,35/10 = 0,675 ≈ 0,70 ✓ ; LA → (27−13)×1,40/10 = 1,96 ≈ 1,95 ✓.

### 4.2 Valeurs moyennes simplifiées (GIS Sol / usage courant)

Pour une estimation rapide sans entrer dans le détail des 15 classes, GIS Sol/INRAE donne des
ordres de grandeur par grande famille de texture [Source 1] :

| Texture | RU (mm/cm de sol) |
|---------|:-----------------:|
| Sableuse | ~0,7 |
| Limoneuse / Limono-argileuse | ~1,3 à 2,0 |
| Argileuse | ~1,7 |
| Argilo-limoneuse | ~2,0 |

> Recoupé par Wikipédia [Source 12] : 0,9–1,2 mm/cm (sable), 1,3–1,6 (limon argileux),
> 1,8–2,0 (argileux/argilo-limoneux/argilo-sableux).

### 4.3 Conversion texture → RU en mm/m de sol

Pour un usage direct (profondeur en mètres), on multiplie la valeur mm/cm par 100 :

| Texture (regroupement) | RU (mm/m de sol) |
|------------------------|:----------------:|
| Sableuse (S) | 70 |
| Sablo-limoneuse (SL) | 100 |
| Limon sableux / Limon léger (LS, LlS, Ll) | 120–145 |
| Limon moyen (Lm) | 175 |
| Sablo-argileuse (SA) | 135 |
| Limon sablo-argileux (LSA, LmS) | 160–165 |
| Limon argileux (LA) | 195 |
| Limon argilo-sableux (LAS) | 175 |
| Argileuse (A) | 175 |
| Argilo-limoneuse (AL) | 180 |
| Argilo-sableuse (AS) | 170 |
| Argile lourde | 165 |

> [À VÉRIFIER MANUELLEMENT] : les regroupements ci-dessus sont une synthèse de l'auteur à partir de
> la table Biljou [Source 2] ; ils ne figurent pas tels quels dans une source unique.

---

## 5. Méthode pratique des forestiers français

### 5.1 Formule de terrain (catalogues CNPF / typologie des stations)

En pratique, les forestiers français (CRPF, ONF, catalogues de stations) calculent le RUM à partir
de la **texture**, de la **profondeur** et de la **pierrosité**, sans mesure laboratoire. La formule
opérationnelle, identique à celle de l'Arvalis/Biljou mais reformulée avec un « coefficient de
texture » (coef U = RU en mm/cm), est :

```
RUM_horizon (mm) = épaisseur (cm) × coef_U_texture × (1 − pierrosité%/100)

RUM_total (mm) = Σ RUM_horizon   (plafonné à la profondeur prospectable, max ~1 m en catalogue)
```

> Source : CTFC eforown [Source 13] — exemple chiffré : horizon 20 cm, texture LA, 0 % cailloux →
> 20 × 1,95 × 1,0 = 39 mm ; horizon 40 cm, texture A, 5 % cailloux → 40 × 1,75 × 0,95 = 66,5 mm
> (≈ 68,4 mm avec arrondi de coef). CNPF plaquette sols 2024 [Source 8] cite explicitement les
> « coefficients de réserve par classes de texture » et renvoie vers Biljou.

### 5.2 Classes de RUM utilisées en typologie (exemple Argonne / IGN)

L'IGN et les catalogues utilisent des classes ordinales de RUM pour caractériser les stations.
Exemple issu du catalogue Argonne [Source IGN, RFN-Argonne] :

| Classe RUM | Intervalle (mm) | Signification |
|------------|:---------------:|---------------|
| Très faible | < 60 | Station très sèche |
| Faible | 60 – 90 | Sèche |
| Moyenne | 90 – 150 | Moyennement approvisionnée |
| Forte | > 150 | Fraîche à humide |

> [À VÉRIFIER MANUELLEMENT] : les bornes exactes (60/90/150) sont spécifiques au catalogue Argonne
> et varient d'un catalogue à l'autre ; elles sont données ici à titre d'exemple.

### 5.3 Ordres de grandeur par type de sol (catalogue Pays de Bray Normand)

Le catalogue CNPF du Pays de Bray [Source 9] illustre les plages de RU selon profondeur + texture :

| Type de sol | Profondeur | RU typique (mm) |
|-------------|:----------:|:---------------:|
| Sableux épais | > 80 cm | 70 – 120 |
| Sableux moyen | 50 – 80 cm | 65 – 150 |
| Limoneux épais | > 80 cm | 160 – 190 |
| Limoneux moyen | 50 – 80 cm | 110 – 190 |
| Argileux (≥ 30 % argile) | épais | 150 – 195 |
| Sableux caillouteux (pierrosité 20–30 %) | — | 60 – 100 |

> Source : CNPF Pays de Bray Normand [Source 9]. Ces valeurs intègrent déjà texture + profondeur +
> pierrosité et sont cohérentes avec la formule §5.1 (ex. : limoneux épais 80 cm × ~2 mm/cm ≈ 160 mm).

---

## 6. Lien avec le DHYa (Climessences)

### 6.1 Rôle du RUM dans le calcul du DHYa

L'indicateur **DHYa** (Déficit Hydrique annuel, en mm) est l'un des 3 indicateurs du modèle IKS de
Climessences (avec TMIa et SDJa). Il caractérise le facteur limitant « manque d'eau ». Son calcul
est un **bilan hydrique mensuel cumulatif** P − ETP, dans lequel le RUM joue le rôle de **tampon** :

1. Pour chaque mois : bilan entre besoins (ETP, calculée via Turc à partir des T et du rayonnement)
   et précipitations reçues (P).
2. **Excès d'eau** → transfert dans la réserve utile, à concurrence du RUM (recharge).
3. **Manque d'eau** → prélèvement dans la réserve utile.
4. Si la réserve ne suffit pas à compenser le déficit → **déficit hydrique mensuel** (DHYmois).
5. Boucle sur 3 années successives (stabilisation de la condition initiale RU = RUM en janvier) ;
   DHYa = somme des DHYmois de la 3ᵉ année.

> Source : Climessences — Indicateur DHYa [Source 7]. Le RUM est donc un **paramètre d'entrée
> obligatoire** du DHYa : sans RUM fiable, pas de DHYa fiable.

### 6.2 Source du RUM dans Climessences v2

Climessences v2 utilise la carte de RUM à résolution 90 m produite par **Dobarco et al. (2021)**
[Source 6], agrégée à 1 km. Cette carte est calculée à partir :
- des données sol de la base nationale **DoneSol** (GIS Sol / INRAE), > 10 000 profils forestiers ;
- de la **fonction de pédotransfert d'Al Majou et al. (2008)** [Source 5], calée sur le triangle
  de texture européen à 5 classes, préconisée par Piedallu et al. (2018, *Forêt entreprise* n°242) ;
- de la profondeur de sol, de la charge en éléments grossiers (pierrosité) et de la profondeur
  d'enracinement.

Pour le reste de l'Europe, la base **ESDB** (Hiederer 2013, JRC) est utilisée avec la même FPT.

> Source : Climessences — Carte du Réservoir Utile Maximal [Source 7]. Le RUM moyen français
> calculé par Al Majou et al. (2008) est de **104 mm** (AWC moyen France).

### 6.3 Croisement avec la documentation climat GeoSylva

> **Note** : le fichier `docs/recherche/03_climat/05_indices_bioclimatiques_forestiers.md` mentionné
> dans la consigne **n'existe pas encore** (dossier `03_climat/` vide au 2026-07-03). La présente
> fiche documente donc le volet « RUM » qui devra être croisé, lorsque ce fichier sera rédigé, avec
> le calcul du DHYa côté climat. GeoSylva dispose déjà des normales climatiques embarquées
> (`precipMmAn`, `precipEteMm`, `etpMm`, `tempMoyC` dans `StationEnvironnementale.kt`) — il manque
> l'ETP mensuelle et la boucle de bilan hydrique pour reproduire le DHYa.

---

## 7. Comparatif / analyse critique

### 7.1 Trois approches concurrentes pour estimer le RUM

| Approche | Précision | Coût/données | Pertinence GeoSylva |
|----------|:---------:|:------------:|:-------------------:|
| **Mesure laboratoire** (courbes pF-humidité sur mottes non remaniées) | Excellente | Élevé (échantillonnage + labo) | Non (hors portée app terrain) |
| **FPT continue** (Al Majou 2008, van Genuchten) | Bonne | Texture granulométrique détaillée (% A/L/S + CO + Da) | Possible mais exigeante |
| **FPT en classes** (table Biljou / coef U par texture) | Moyenne (±34 mm) | Texture au toucher + profondeur + pierrosité | **Oui — cible GeoSylva** |

La précision de la carte SILVAE/Dobarco est R² = 0,35, erreur moyenne 34 mm pour un RUM variant de
0 à 150 mm [Source 10] — soit une incertitude relative ~25–35 %. Cette incertitude est **intrinsèque
à la méthode par classes** et acceptable pour du diagnostic stationnel (pas pour de l'agronomie
fine). GeoSylva, qui vise le diagnostic stationnel, doit accepter cette plage.

### 7.2 Triangle de Jamagne (15 classes) vs triangle européen (5 classes)

- **Biljou / catalogues forestiers français** : triangle de **Jamagne** (15 classes, riche).
- **Climessences / ESDB européen** : triangle **européen à 5 classes** (Coarse / Medium / Medium
  fine / Fine / Very fine).
- L'enum `TextureSol` de GeoSylva (7 valeurs : ARGILEUSE, LIMONEUSE, SABLEUSE, ARGILO_LIMONEUSE,
  ARGILO_SABLEUSE, LIMONO_SABLEUSE, GRAVELEUSE) est **intermédiaire** — plus grossier que Jamagne
  mais plus fin que le triangle européen. Un mapping devra être défini (cf. §8).

### 7.3 Profondeur d'enracinement : le point critique

La profondeur prospectable par les racines est la **première source d'imprécision** [Source 2]. En
forêt, en l'absence d'obstacle (dalle, nappe, forte pierrosité), les racines fines descendent
souvent à **≥ 2 m**, mais les catalogues forestiers plafonnent généralement à **1 m** (parfois 80
cm). GeoSylva devra : (a) plafonner à la profondeur de sol mesurée terrain (`soilProfondeurCm`),
(b) appliquer un plafond de sécurité (ex. 100 cm) cohérent avec les catalogues, (c) réduire à la
profondeur d'hydromorphie si `hydromorphieProfondeurCm` < profondeur de sol.

---

## 8. Recommandation pour GeoSylva

### 8.1 État actuel du code (audit)

L'app manipule **déjà** la notion de RUM, mais de façon **interpolée et non calculée** :

- **`EmbeddedSoilService.kt`** (domain/geo/) : ~110 points pédologiques embarqués avec une valeur
  RUM en mm **codée en dur** par point, interpolée par IDW (exposant 2, 6 voisins). Le RUM n'est
  **pas calculé** à partir de texture + profondeur + pierrosité — il est lu dans la table. La
  texture et le drainage sont interpolés par mode (vote majoritaire).
- **`StationEnvironnementale.kt`** (domain/model/) : expose `soilRumMm` (Double?), `soilRufMm`
  (Double? — la RFU), `soilProfondeurCm` (Int?), `soilTexture` (String?), `pierrositeClassePct`
  (String?), `rumClasseBdgsf` (String? — classe RUM de la BDGSF INRAE en WMS).
- **`StationObservation.kt`** (domain/model/station/) : saisie terrain avec `profondeurSolCm`,
  `texture` (enum `TextureSol` 7 valeurs), `pierrosite` (enum `Pierrosite` 5 valeurs),
  `hydromorphieProfondeurCm`, `humus`, `drainage`.
- **`StationDataAggregator.kt`** : alimente `soilRumMm` depuis `EmbeddedSoilService` (IDW).

**Lacune identifiée** : aucune fonction ne calcule le RUM à partir des saisies terrain
(texture + profondeur + pierrosité). L'app a tous les inputs mais pas le calcul.

### 8.2 Tableau de mapping TextureSol (GeoSylva) → coef U (mm/cm)

Mapping recommandé pour implémenter le calcul de RUM à partir de l'enum `TextureSol` existant.
Valeurs dérivées de la table Biljou [Source 2] en prenant la classe Jamagne la plus représentative
de chaque catégorie GeoSylva :

| `TextureSol` (GeoSylva) | Classe Jamagne retenue | **coef U (mm/cm)** | Justification |
|-------------------------|------------------------|:------------------:|---------------|
| `SABLEUSE` | S | **0,70** | Sable pur |
| `LIMONEUSE` | Lm (Limon moyen) | **1,75** | Limon typique français |
| `ARGILEUSE` | A | **1,75** | Argile typique |
| `ARGILO_LIMONEUSE` | AL | **1,80** | Meilleure RU (limon argileux) |
| `ARGILO_SABLEUSE` | AS | **1,70** | Argile sableuse |
| `LIMONO_SABLEUSE` | LS | **1,45** | Limon sableux |
| `GRAVELEUSE` | (cas spécial) | **0,70** | Sableuse par défaut (la pierrosité corrige) |
| `INCONNUE` | — | **1,40** | Médiane des 15 classes (valeur de repli) |

> [À VÉRIFIER MANUELLEMENT] : ce mapping est une proposition de l'auteur ; il n'existe pas de table
> de correspondance officielle entre l'enum GeoSylva (7 classes) et le triangle de Jamagne (15
> classes). La valeur `GRAVELEUSE` est délicate : elle traduit une forte pierrosité plus qu'une
> texture fine — il est préférable de la traiter via le coefficient de pierrosité (cf. §8.3) plutôt
> que via le coef U. À valider avec un pédologue forestier.

### 8.3 Algorithme proposé (pseudocode Kotlin)

```kotlin
// À implémenter dans un nouveau usecase domain/usecase/station/ComputeRumUseCase.kt
// ou méthode dans StationDiagnosticEngine.kt

fun computeRumMm(
    texture: TextureSol,
    profondeurSolCm: Int?,           // saisie terrain (StationObservation)
    pierrosite: Pierrosite,          // saisie terrain
    hydromorphieProfondeurCm: Int?   // réduit la profondeur prospectable
): Int {
    val coefU = when (texture) {
        SABLEUSE -> 0.70
        LIMONEUSE -> 1.75
        ARGILEUSE -> 1.75
        ARGILO_LIMONEUSE -> 1.80
        ARGILO_SABLEUSE -> 1.70
        LIMONO_SABLEUSE -> 1.45
        GRAVELEUSE -> 0.70
        INCONNUE -> 1.40
    }
    // Profondeur prospectable : min(sol, hydromorphie), plafonnée à 100 cm
    val profondeurBrute = profondeurSolCm ?: 60  // défaut conservateur
    val profondeurEff = minOf(profondeurBrute, hydromorphieProfondeurCm ?: profondeurBrute, 100)
    // Coefficient de pierrosité (fraction volumique de terre fine)
    val facteurTerreFine = when (pierrosite) {
        NULLE -> 1.00
        FAIBLE -> 0.95      // < 10 %
        MOYENNE -> 0.80     // 10–30 % (médiane 20 %)
        FORTE -> 0.55       // 30–60 % (médiane 45 %)
        TRES_FORTE -> 0.30  // > 60 % (médiane 70 %)
    }
    val rum = profondeurEff * coefU * facteurTerreFine
    return rum.toInt()
}

// RFU ≈ fraction de la RU (1/3 sable → 2/3 argile)
fun computeRfuMm(rumMm: Int, texture: TextureSol): Int {
    val fraction = when (texture) {
        SABLEUSE, GRAVELEUSE -> 0.33
        LIMONO_SABLEUSE, LIMONEUSE -> 0.50
        ARGILO_SABLEUSE, ARGILO_LIMONEUSE, ARGILEUSE -> 0.66
        INCONNUE -> 0.50
    }
    return (rumMm * fraction).toInt()
}
```

### 8.4 Priorité d'intégration

1. **Priorité HAUTE** : implémenter `ComputeRumUseCase` (calcul terrain texture + profondeur +
   pierrosité) — comble la lacune identifiée. À appeler dans `StationDiagnosticEngine` lorsque les
   saisies terrain sont présentes, en **surchargeant** la valeur IDW de `EmbeddedSoilService` (la
   saisie terrain est plus fiable que l'interpolation 25–50 km).
2. **Priorité MOYENNE** : exposer le RUM calculé dans `StationEnvironnementale.soilRumMm` et le
   RFU dans `soilRufMm` (champs déjà présents mais non alimentés par calcul).
3. **Priorité BASSE** : reproduire le calcul du DHYa (Climessences) côté GeoSylva — nécessite
   l'ETP mensuelle (non disponible actuellement, seulement `etpMm` annuel) et la boucle sur 3 ans.
   À différer jusqu'à la vague climat (cf. `03_climat/`).

### 8.5 Fichiers Kotlin concernés

- `app/src/main/java/com/forestry/counter/domain/geo/EmbeddedSoilService.kt` (RUM IDW actuel)
- `app/src/main/java/com/forestry/counter/domain/model/StationEnvironnementale.kt` (champs RU/RFU)
- `app/src/main/java/com/forestry/counter/domain/model/station/StationObservation.kt` (inputs terrain)
- `app/src/main/java/com/forestry/counter/domain/location/StationDataAggregator.kt` (assemblage)
- `app/src/main/java/com/forestry/counter/domain/usecase/station/StationDiagnosticEngine.kt` (moteur)
- **Nouveau** : `app/src/main/java/com/forestry/counter/domain/usecase/station/ComputeRumUseCase.kt`

---

## 9. Limites et points à vérifier manuellement

1. **[À VÉRIFIER MANUELLEMENT]** Le mapping `TextureSol` (7 classes GeoSylva) → coef U (table
   Biljou 15 classes Jamagne) du §8.2 est une proposition de l'auteur sans source officielle de
   correspondance. Faire valider par un pédologue forestier (CNPF/IGN) ou caler sur un catalogue
   régional de référence.
2. **[À VÉRIFIER MANUELLEMENT]** Les classes ordinales de RUM (très faible < 60, faible 60–90,
   moyenne 90–150, forte > 150 mm) du §5.2 sont spécifiques au catalogue Argonne et varient d'un
   catalogue à l'autre — ne pas coder en dur ces bornes au niveau national sans recoupement.
3. **Table Biljou non extraite d'une source PDF officielle** : la table §4.1 a été extraite de la
   page HTML Biljou (appgeodb.nancy.inrae.fr) et recoupée arithmétiquement (vérification
   (θcc−θpf)×Da/10 = RU mm/cm cohérente pour toutes les lignes). La source primaire (Baize & Jabiol,
   ou service de cartographie des sols de l'Aisne cité par le CNPF) n'a pas été consultée en PDF.
4. **Profondeur d'enracinement non standardisée** : les catalogues plafonnent à 80 cm, 100 cm ou
   2 m selon les sources. Le choix du plafond (100 cm proposé §8.3) est conservateur et aligné sur
   la pratique catalogue courante, mais sous-estime les sols profonds à enracinement > 1 m.
5. **FPT calée sur sols agricoles** : Biljou [Source 2] avertit que peu de FPT sont calées
   spécifiquement sur sols forestiers — l'usage de FPT agricoles peut induire un biais. La table
   Biljou est néanmoins la meilleure disponible pour la forêt française.
6. **Pierrosité : rétention d'eau des cailloux négligée** : le calcul proposé (§8.3) néglige la
   rétention d'eau des éléments grossiers (silex ~2 %, calcaires > 30 %). Conservatoire pour silex,
   potentiellement sous-estimant pour sols calcaires caillouteux. Arvalis [Source 3] fournit des
   valeurs de référence d'humidité utile des EG à intégrer si besoin de précision.
7. **Fichier `03_climat/05_indices_bioclimatiques_forestiers.md` absent** : la consigne demandait
   un croisement avec ce fichier, mais le dossier `03_climat/` est vide au 2026-07-03. Le lien
   DHYa/RUM est documenté §6 mais le croisement détaillé reste à faire quand le fichier climat
   existera.
8. **RUH / RUF non standardisés** : la consigne mentionnait RUH (réserve utile humique) et RUF
   (réserve utile profonde). Ces termes ne sont pas standardisés dans la littérature française
   (INRAE/CNPF utilisent quasi exclusivement RU/RUM/RFU/RDU). « RUH » semble désigner la RU de
   l'horizon humifère de surface dans certains catalogues, sans définition formelle retrouvée.
