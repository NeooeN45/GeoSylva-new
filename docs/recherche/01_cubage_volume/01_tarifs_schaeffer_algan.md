# Tarifs de cubage Schaeffer et Algan — vérification, formules et guide d'usage

**Domaine** : docs/recherche/01_cubage_volume/
**Date de recherche** : 2026-07-03
**Agent** : sous-agent recherche dendrométrie (vérification tarifs Schaeffer/Algan)

> Ce document **complète et approfondit** les sections 1.1 et 1.2 de
> `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (déjà produites). Il ne les duplique pas : il
> apporte les formules exactes sourcées, un tableau de coefficients reconstitué, un guide de
> décision d'usage, et une comparaison chiffrée avec `TarifData.kt`.

---

## Table des matières

1. [Sources identifiées](#1-sources-identifiées)
2. [Données détaillées](#2-données-détaillées)
   - 2.1 [Historique et filiation Algan → Schaeffer](#21-historique-et-filiation-algan--schaeffer)
   - 2.2 [Formule générale des tarifs Schaeffer/Algan à une entrée](#22-formule-générale-des-tarifs-schaefferalgan-à-une-entrée)
   - 2.3 [Tableau reconstitué des tarifs à une entrée (rapides)](#23-tableau-reconstitué-des-tarifs-à-une-entrée-rapides)
   - 2.4 [Tarifs Schaeffer à deux entrées](#24-tarifs-schaeffer-à-deux-entrées)
   - 2.5 [Tarif Algan « historique » (formules dédiées, hors numérotation)](#25-tarif-algan-historique-formules-dédiées-hors-numérotation)
3. [Comparatif / analyse critique](#3-comparatif--analyse-critique)
4. [Guide de décision — quel tarif utiliser dans quelle situation](#4-guide-de-décision--quel-tarif-utiliser-dans-quelle-situation)
5. [Écarts détectés vs TarifData.kt](#5-écarts-détectés-vs-tarifdatakt)
6. [Recommandation pour GeoSylva](#6-recommandation-pour-geosylva)
7. [Limites et points à vérifier manuellement](#7-limites-et-points-à-vérifier-manuellement)

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| M. Bruciamacchie, package R `PPtools` (AgroParisTech/ENGREF Nancy, École nationale du génie rural, des eaux et des forêts) | **scientifique** — auteur universitaire, outil utilisé en formation forestière française, code source public | Élevée pour la formule mathématique (code exécutable, cohérent avec la littérature) ; le dépôt lui-même est un outil pédagogique, pas une publication éditée avec comité de lecture | `https://raw.githubusercontent.com/Bruciamacchie/PPtools/master/R/TarifSch.R` et `TarifSch2.R` | Consulté 2026-07-03 (code non daté explicitement, package actif) |
| J.-Y. Massenet, cours de dendrométrie, Lycée forestier de Mesnières | **commerciale/tierce** (support pédagogique d'enseignant, pas une publication officielle ONF/IGN) mais recoupe fidèlement les valeurs numériques trouvées chez Bruciamacchie | Moyenne — support de cours, mais cohérent avec sources scientifiques | `http://jymassenet-foret.fr/cours/dendrometrie/coursdendrometrieppt/versionspdfdespptdendro/dendrometriechap6ppt.pdf` et `.../Coursdendrometriepdf/DENDRO4-2009.pdf` | Cours daté 2007-2012 (réédition d'un contenu historique 1949/1958) |
| G. Fombonnat / M. Bruciamacchie et al., *« Il y a 100 ans : la naissance des tarifs Algan »*, Revue forestière française | **scientifique** (revue à comité de lecture, hébergée sur le portail I-Revues/HAL) | Élevée | `https://doi.org/10.4267/2042/26629` | Article historique (RFF) |
| Auteur non identifié précisément (probablement J. Bouchon / ENGREF), *« Tarifs de cubage à une et deux entrées »*, HAL | **scientifique** | Élevée | `https://hal.science/hal-03382015v1/document` | Document non daté explicitement (fac-similé, ~1949-1958) |
| Vallet, Dhôte, Le Moguédec, Ravart, Pignard (2006), *« Development of total aboveground volume equations for seven important forest tree species in France »*, Forest Ecology and Management 229 (1-3), 98-110 | **scientifique** (revue à comité de lecture) | Élevée | `https://hal.inrae.fr/hal-02664812` | 2006 |
| Zimmer (fabricant matériel forestier), blog *« Dendrométrie »* | **commerciale/tierce** | Moyenne — vulgarisation, mais recoupe les usages ONF/CNPF classiques | `https://www.zimmersa.com/blog-forestier/dendrometrie-la-science-des-mesures-des-arbres-et-des-peuplements-forestiers-n236` | Publié 2022-07-22 |
| ONF, *« Quelles différences entre futaie régulière et futaie irrégulière »* | **officielle** (ONF) | Élevée pour le contexte sylvicole (régulier/irrégulier), pas de coefficients chiffrés | `https://www.onf.fr/onf/+/1167::la-futaie-reguliere-et-irreguliere.html` | Consulté 2026-07-03 |
| `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` §1.1/1.2 (recherche précédente interne au projet) | interne | — | — | 2026-07-01 |

**Lacune assumée** : les tables ONF/CTBA officielles complètes (édition papier 1949, rééditions successives) et les tables Algan/ENGREF originales de 1958 ne sont **pas disponibles librement en ligne sous forme de PDF texte exploitable** — les documents HAL trouvés sont des fac-similés scannés dont l'extraction automatique de texte échoue partiellement (contenu tronqué/illisible via `webfetch`, y compris via OCR déjà appliqué par HAL). Les valeurs numériques présentées ci-dessous proviennent donc principalement du **code source exécutable et documenté** du package R `PPtools` (fiable car formule + constantes explicites, vérifiable par calcul), recoupé avec les extraits de texte OCR obtenus par recherche web (snippets Google/HAL). **Aucune donnée n'est inventée** : tout chiffre est soit issu directement d'une source, soit explicitement marqué `[À VÉRIFIER MANUELLEMENT]` comme calcul dérivé.

---

## 2. Données détaillées

### 2.1 Historique et filiation Algan → Schaeffer

Fait vérifié (source : *« Il y a 100 ans : la naissance des tarifs Algan »*, RFF, DOI 10.4267/2042/26629) :

- Les **tarifs Algan** (Henri Algan, ingénieur des Eaux et Forêts, ~1900-1920) sont les premiers tarifs de cubage français à une entrée largement diffusés, construits **empiriquement** (sans formule mathématique explicite à l'origine) sous forme de tableaux, avec une **hypothèse implicite** que « un arbre double son volume quand son diamètre passe de 20 à 25 cm, ou le décuple quand son diamètre passe de 30 à 80 cm ».
- En **1949**, Léon Schaeffer publie dans le premier numéro de la *Revue forestière française* l'article « Tarifs rapides et tarifs lents », dans lequel il explicite : *« Mon intention n'est pas de détrôner les tarifs Algan (...) mieux vaut maintenant les appuyer sur une formule mathématique »*. Les **tarifs Schaeffer « rapides »** sont donc explicitement un **lissage mathématique des tarifs Algan**, pas une méthode indépendante. Schaeffer crée en complément des **tarifs « lents »** pour les peuplements réguliers homogènes où les tarifs rapides/Algan progressent trop vite d'une classe de diamètre à l'autre.
- Le manuel *Dendrométrie* (Pardé & Bouchon, 1ʳᵉ éd. 1961, 2ᵉ éd. 1988, ENGREF) et le *Manuel pratique d'aménagement* de l'ONF (1964) consacrent des développements aux tarifs Algan/Schaeffer rapides.

**Conséquence méthodologique importante** [FAIT VÉRIFIÉ] : Algan et Schaeffer-rapide ne sont pas deux familles de coefficients indépendantes par essence — ce sont **deux formalisations d'une même famille de tarifs numérotés génériques** (toutes essences confondues), la différence Algan/Schaeffer étant historique (tableau empirique vs formule) plutôt que botanique. Cela contredit l'idée d'un jeu de coefficients Algan *spécifique par essence* tel qu'implémenté dans `TarifData.alganCoefs` (cf. §5).

### 2.2 Formule générale des tarifs Schaeffer/Algan à une entrée

Source : code R `TarifSch.R`, package `PPtools` (M. Bruciamacchie), recoupé avec l'extrait OCR du cours Massenet donnant la même structure `V = M·D·(D-5)·(D-10)/1400` pour les tarifs rapides.

Le package distingue **4 familles** de tarifs à une entrée, chacune paramétrée par un seul numéro de tarif `num` et le diamètre `D` (cm, à 1,30 m) — le volume `V` est en m³ :

| Famille | Nom usuel | Formule | Commentaire |
|---|---|---|---|
| `SchR` | Schaeffer **rapide** (= lissage des tarifs Algan) | `V = (5/70000) × (8+num) × (D-5) × (D-10)` | Racines à D=5 et D=10 cm |
| `SchI` | Schaeffer **intermédiaire** | `V = (5/80000) × (8+num) × (D-2,5) × (D-7,5)` | Racines à D=2,5 et D=7,5 cm |
| `SchL` | Schaeffer **lent** | `V = (5/90000) × (8+num) × D × (D-5)` | Racine à D=0 et D=5 cm |
| `SchTL` | Schaeffer **très lent** | `V = (5/101250) × (8+num) × D²` | Fonction puissance pure (D²), pas de racine — plus « stable » d'une classe de diamètre à l'autre |

**Propriété de calage commune aux 4 familles** [FAIT VÉRIFIÉ, dérivé par calcul direct de la formule] : à D = 45 cm (diamètre étalon historique d'Algan/Schaeffer), on a toujours `V(45) = (8+num)/10` m³, quelle que soit la famille. Ainsi le numéro de tarif `num` correspond directement au volume (en décimètres cubes ×100, arrondi) de l'arbre-étalon de 45 cm de diamètre :

| num | V(45 cm) en m³ |
|---|---|
| 1 | 0,9 |
| 2 | 1,0 |
| 3 | 1,1 |
| ... | ... |
| 9 | 1,7 |
| 10 | 1,8 |
| ... | ... |
| 16 | 2,4 |
| ... | ... |
| 20 | 2,8 |

Ce tableau `num → V(45)` **recoupe exactement** l'extrait OCR trouvé sur `hal.science/hal-03382015v1` : *« n° 1 2 3 4 5 6 7 8 9 10 → k 0,9 1,0 1,1 1,2 1,3 1,4 1,5 1,6 1,7 1,8 »* et *« n° 11...20 → k 1,9 2,0 2,1 2,2 2,3 2,4 2,5 2,6 2,7 2,8 »*. Cette double confirmation indépendante (code source + fac-similé historique) rend la formule `SchR` **fiable avec un haut degré de confiance**.

### 2.3 Tableau reconstitué des tarifs à une entrée (rapides, SchR)

Coefficients obtenus en développant `V = (5/70000)×(8+num)×(D-5)×(D-10)` sous la forme polynomiale `V = a₀ + a₁·D + a₂·D²` (D en cm, V en m³) — **calcul dérivé, [À VÉRIFIER MANUELLEMENT]** (dérivation algébrique directe, non trouvée telle quelle dans une source, mais mathématiquement équivalente à la formule sourcée en 2.2) :

Avec `K = (8+num)/14000` :
`V = K·D² − 15K·D + 50K`

| Tarif num | K = (8+num)/14000 | a₂ (coef. D²) | a₁ (coef. D) | a₀ (constante) | V(D=30cm) | V(D=45cm) |
|---|---|---|---|---|---|---|
| 1 | 0,0006429 | 0,0006429 | −0,009643 | 0,032143 | 0,354 | 0,900 |
| 5 | 0,0009286 | 0,0009286 | −0,013929 | 0,046429 | 0,511 | 1,300 |
| 9 | 0,0012143 | 0,0012143 | −0,018214 | 0,060714 | 0,668 | 1,700 |
| 12 | 0,0014286 | 0,0014286 | −0,021429 | 0,071429 | 0,786 | 2,000 |
| 16 | 0,0017143 | 0,0017143 | −0,025714 | 0,085714 | 0,943 | 2,400 |

*(table limitée à 5 lignes représentatives pour lisibilité ; formule générale ci-dessus permet de reconstituer les 16 (voire 20+) tarifs).*

### 2.4 Tarifs Schaeffer à deux entrées

Source : code R `TarifSch2.R` (M. Bruciamacchie, même dépôt) — **13 numéros** de tarifs, pas 8 comme indiqué dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.1 (voir §5 « écarts »).

Formule (D en cm, hauteur découpe `h` en m, V en m³) :

```
V = π/40000 × (a − b·h − k·π·D)² / 10000 × D² × h
```

Table des coefficients (num, a, b, k) — **retranscrite intégralement telle que trouvée dans le code source** :

| num | a | b | k |
|---|---|---|---|
| 1 | 100 | 1,1 | 0 |
| 2 | 97 | 1,0 | 0 |
| 3 | 98 | 1,5 | 0 |
| 4.1 | 100 | 1,8 | 0 |
| 4.2 | 86,8 | 0,7 | 0 |
| 5.1 | 96 | 1,8 | 0 |
| 5.2 | 82,8 | 0,7 | 0 |
| 6 | 93 | 0,5 | 0,03 |
| 7 | 92 | 0,5 | 0,05 |
| 8.1 | 93 | 1,6 | 0 |
| 8.2 | 82 | 0,5 | 0 |
| 9 | 80 | 0,25 | 0 |
| 10 | 92 | 1,5 | 0 |

Cette paramétrisation `a, b, k` est structurellement **différente** de la forme `V = a + b×C²×H` documentée en interne dans `TarifData.kt` (cf. §5). Il s'agit très probablement de deux **conventions d'écriture différentes de la même famille historique de tarifs** — la forme `(a − b·h − k·π·D)²` module en réalité un « diamètre à la découpe » réduit, ce qui redonne au développement un polynôme en D² et D²·h, compatible avec la forme classique ONF `V = a + b·C²·h` **après développement algébrique** [À VÉRIFIER MANUELLEMENT — l'équivalence exacte terme à terme n'a pas pu être établie faute d'accès à une table numérique croisée officielle].

### 2.5 Tarif Algan « historique » (formules dédiées, hors numérotation)

Source : cours Massenet, `DENDRO4-2009.pdf` (OCR) — formules **spécifiques par contexte d'usage**, pas par essence :

| Usage | Formule Algan | Variables |
|---|---|---|
| Cubage des réserves de taillis-sous-futaie (grumes 4-12 m), formule **Algan-Monnin** | `v = d²·h/2` (avec correction ±1/10 par m au-delà/en-deçà de 8 m) | d = diamètre à 1,3 m (m), h = hauteur à la découpe (m) |
| Grumes > 12 m | `v = 0,4 × d² × (h − 5,2)` | idem, correction ±4 %/m autour de 20 m |
| Feuillus de futaie pleine (formule d'Auvergne, apparentée) | `v = 0,55 × d² × h` | — |
| Sapin/épicéa (bois d'œuvre, découpe 15 cm résineux) | deux variantes Algan, dont `v = 0,4 × d² × H` (volume total) | d, H en m |

**Constat clé** [FAIT VÉRIFIÉ] : ces formules Algan « historiques » sont des **formules de troncs de cône génériques** (facteur constant × d² × h, sans exposants ajustés par essence). Elles n'ont **aucun rapport structurel** avec la formule `V = a × D^b × H^c` (avec exposants b≈2,0-2,2 et c≈0,8-1,0 variables par essence) implémentée dans `TarifData.alganCoefs`. Cette dernière ressemble bien davantage aux équations allométriques nationales par essence publiées par **Vallet et al. (2006)** dans *Forest Ecology and Management* (« Development of total aboveground volume equations for seven important forest tree species in France »), qui utilisent précisément une forme en puissance `V = a·D^b·H^c` ajustée par essence sur les données IFN. Voir §5.

---

## 3. Comparatif / analyse critique

| Critère | Schaeffer 1 entrée (rapide/lent) | Schaeffer 2 entrées | Algan « historique » (formules dédiées) |
|---|---|---|---|
| Variables requises | D₁₃₀ uniquement | D₁₃₀ + hauteur | d + h (souvent hauteur à la découpe) |
| Précision | Moindre (pas d'info sur la forme réelle de l'arbre) | Meilleure (intègre la variabilité de forme via H) | Variable, dépend fortement du contexte d'usage (formule différente par essence/situation) |
| Essence-spécifique ? | Non — numéro générique, calé sur un échantillon local | Non — numéro générique | Non plus — mais formule différenciée par **usage** (réserves TSF, bois d'œuvre résineux, feuillus pleine futaie) plutôt que par essence stricto sensu |
| Peuplement cible historique | Peuplements homogènes réguliers (tarif lent) ou hétérogènes/fertilité inégale (tarif rapide = Algan lissé) | Peuplements où la hauteur est mesurée systématiquement (inventaires IFN, expertises) | Taillis-sous-futaie, réserves, résineux de futaie régulière |
| Usage aujourd'hui | Encore utilisé pour cubages rapides sur le terrain (choix d'un numéro par calibration locale sur quelques arbres-échantillons) | Utilisé par l'IGN/IFN (tarifs à 2 entrées, cf. `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.3) et pour les expertises de précision | Historique — remplacé en pratique par les tarifs Schaeffer rapides (mathématiquement équivalents) ou par les équations nationales (Vallet et al. 2006) pour un usage essence-spécifique moderne |

---

## 4. Guide de décision — quel tarif utiliser dans quelle situation

Ce guide synthétise les éléments trouvés (littérature dendrométrique française, ONF, cours de dendrométrie) — les recommandations générales sont bien établies dans la littérature classique ; les correspondances précises « situation → numéro de tarif » restent, elles, du ressort d'un calibrage local (cf. `TarifFindSch` du package `PPtools`, qui calcule statistiquement le numéro de tarif le mieux ajusté à un échantillon d'arbres réel) — **ceci n'est pas une donnée figée mais une méthode empirique**, à ne pas coder en dur comme une vérité universelle.

| Situation concrète | Tarif recommandé | Pourquoi |
|---|---|---|
| Mesure de terrain rapide, un seul relevé par arbre (D₁₃₀), peuplement hétérogène ou de fertilité variable, pas de temps/matériel pour mesurer la hauteur | **Schaeffer/Algan 1 entrée, tarif « rapide »** | Formule conçue précisément pour ce cas (lissage d'Algan) ; tolère une hétérogénéité de forme plus grande |
| Peuplement régulier, monospécifique, homogène en fertilité, avec un historique de gestion connu (ex. futaie régulière ONF calibrée) | **Schaeffer 1 entrée, tarif « lent » (ou très lent)** | Progression plus douce du volume par classe de diamètre, adaptée à une population homogène où le risque de sur-estimation des gros bois (défaut des tarifs rapides en peuplement homogène) est réel |
| Hauteur disponible pour chaque arbre (dendromètre, relascope, LiDAR terrain), besoin de précision supérieure, inventaire d'aménagement | **Schaeffer 2 entrées (ou tarif IFN 2 entrées)** | Intègre directement la variabilité de forme via H, réduit l'erreur par rapport à un tarif 1 entrée générique |
| Taillis-sous-futaie, réserves feuillues (chêne notamment), cubage de grumes en forêt irrégulière | **Formule Algan historique dédiée** (Algan-Monnin pour 4-12 m, Algan pour >12 m) | Formule spécifiquement calée sur ce type de structure ; usage documenté dans les cours de dendrométrie français et cité par l'ONF pour la gestion des TSF |
| Estimation de volume par essence à l'échelle nationale, avec base de données de calibration (D, H) disponible pour l'essence visée | **Équations allométriques nationales par essence (type Vallet et al. 2006), pas Algan** | Ce sont ces équations puissance `a·D^b·H^c` par essence, pas les tarifs Algan/Schaeffer génériques, qui sont adaptées à une segmentation par essence — cf. §5 |
| Usage ONF/IFN de référence officielle, inventaire national | **Tarifs IFN à 1 ou 2 entrées** (cf. `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.3) | Norme utilisée par l'inventaire forestier national, calibrée sur de très larges échantillons |
| Expert privé, estimation de valeur foncière ponctuelle sur peuplement mixte/irrégulier | **Schaeffer rapide ou tarif Algan historique selon le contexte**, avec calibration locale si possible (mesure de quelques arbres-témoins) | Approche traditionnelle des experts forestiers français, documentée depuis Algan (1900s) jusqu'aux cours actuels |

**Point clé non trouvé en accès libre** [LACUNE ASSUMÉE] : aucune règle officielle numérique publiée en accès libre ne dit explicitement « pour telle essence + telle station, utiliser exactement le tarif Schaeffer n°X ». Le choix du numéro se fait historiquement par **calibration empirique locale** (mesure de quelques arbres-échantillons du peuplement réel, ajustement statistique — cf. fonction `TarifFindSch` de Bruciamacchie qui automatise cette calibration à partir des données IFN). Toute affirmation d'une correspondance essence→numéro fixe doit être marquée `[À VÉRIFIER MANUELLEMENT]`.

---

## 5. Écarts détectés vs TarifData.kt

Fichier analysé : `app/src/main/java/com/forestry/counter/domain/calculation/tarifs/TarifData.kt`

### 5.1 Écart n°1 — Formule Schaeffer 1 entrée : forme mathématique différente de la source classique

- **TarifData.kt, lignes 14 et 45-62** : documente et implémente `V1E : V = a + b × C²` (C = circonférence en m), avec des coefficients `a` négatifs et `b` croissants de 0,3979 (tarif 1) à 4,0368 (tarif 16).
- **Source vérifiée (§2.2-2.3)** : la formule historique Schaeffer rapide est `V = (5/70000)×(8+num)×(D-5)×(D-10)`, un **polynôme complet en D** (terme constant + terme linéaire + terme quadratique), pas une simple fonction quadratique de la circonférence sans terme linéaire.
- **Test numérique** [calcul effectué par l'agent, À VÉRIFIER MANUELLEMENT] : pour un arbre D=30 cm (C≈0,9425 m), la formule sourcée SchR avec num=9 donne V ≈ 0,607 m³, alors que `TarifData.schaefferOneEntry` tarif n°9 (ligne 54 : a=-0,0996, b=1,7666) donne V = -0,0996 + 1,7666×0,9425² ≈ **1,470 m³** — soit un écart de **+142 %**. Pour l'arbre étalon D=45 cm, la formule sourcée donne V(45)=(8+9)/10=1,7 m³ par construction, alors que le tarif n°9 de `TarifData.kt` donne V = -0,0996 + 1,7666×1,4137² ≈ 2,532 m³ (écart +49 %). Ces écarts sont trop importants pour être de simples imprécisions d'arrondi et suggèrent soit (a) une convention de numérotation différente entre la source Bruciamacchie et celle utilisée pour calibrer `TarifData.kt`, soit (b) une confusion d'unité, soit (c) une source différente non identifiée pour ces coefficients. **Ce point nécessite une vérification manuelle croisée avec une table ONF/CTBA papier originale**, qui n'a pas pu être obtenue en accès libre dans le cadre de cette recherche.

### 5.2 Écart n°2 — Nombre de tarifs Schaeffer à 2 entrées

- **`docs/REFERENTIELS_FORESTIERS_EXTERNES.md` §1.1** et **`TarifData.kt` lignes 64-79** : indiquent/implémentent **8 tarifs** Schaeffer à 2 entrées (numérotés 1 à 8).
- **Source vérifiée (§2.4, code `TarifSch2.R`)** : la table de référence trouvée comporte **13 entrées** numérotées `1, 2, 3, 4.1, 4.2, 5.1, 5.2, 6, 7, 8.1, 8.2, 9, 10` (certains numéros ont une sous-variante `.1`/`.2`). Le nombre « 8 » présent dans la documentation interne du projet n'est donc **pas confirmé** par cette source — soit il existe une autre convention à 8 tarifs (non trouvée en accès libre), soit la documentation interne sous-estime le nombre de variantes existantes. **[À VÉRIFIER MANUELLEMENT]** auprès d'une table ONF officielle.

### 5.3 Écart n°3 (le plus important) — Les coefficients "Algan" de TarifData.kt ne correspondent probablement pas aux tarifs Algan historiques

- **`TarifData.kt` lignes 82-161 (`alganCoefs`)** : implémente un jeu de coefficients **par essence** (CH_SESSILE, HETRE_COMMUN, PIN_SYLVESTRE, etc.), sous la forme `V = a × D^b × H^c`, avec des exposants `b` variant entre ~1,88 et ~2,22 et `c` entre ~0,81 et ~1,01 selon l'essence.
- **Constat de cette recherche (§2.1 et §2.5)** : les tarifs Algan historiques (1900s-1958) sont (a) soit des **tableaux empiriques génériques non essence-spécifiques**, formalisés ensuite par Schaeffer sous forme `V=K·(D-5)(D-10)/1400` (aucun exposant b/c ajustable), (b) soit des **formules dédiées par usage** (taillis-sous-futaie, résineux de futaie régulière) du type `v = constante × d² × h` (exposants fixes = 2 et 1, jamais ajustés par essence). **Aucune source trouvée dans cette recherche ne présente de tarif Algan avec des coefficients `a, b, c` variables par essence comme dans `TarifData.kt`.**
- **Hypothèse la plus probable** [À VÉRIFIER MANUELLEMENT] : les coefficients codés sous le nom « Algan » dans `TarifData.kt` sont en réalité — ou s'inspirent — des **équations nationales de volume par essence** publiées par **Vallet, Dhôte, Le Moguédec, Ravart & Pignard (2006)**, *Forest Ecology and Management* 229, qui utilisent exactement la forme `V = a·D^b·H^c` calibrée par essence sur les données IFN (7 essences dans la publication princeps de 2006, étendue ensuite à un plus grand nombre d'essences dans des travaux ultérieurs de l'INRAE/IGN, notamment le projet EMERGE déjà cité en §1.4 de `REFERENTIELS_FORESTIERS_EXTERNES.md`). Si cette hypothèse est confirmée, il faudrait :
  1. **Renommer** la source citée dans les commentaires `TarifData.kt` (lignes 17-20) : ne pas attribuer à « Algan (1958) » un modèle qui semble provenir des équations nationales de volume 2006+ ;
  2. **Vérifier valeur par valeur** chaque coefficient `(a, b, c)` par essence contre la publication Vallet et al. 2006 (accessible via HAL : `https://hal.inrae.fr/hal-02664812`) et ses extensions (le projet EMERGE mentionné dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.4 est probablement la bonne piste pour les essences non couvertes par la publication de 2006 initiale, qui n'en couvrait que 7).
- Ce n'est **pas nécessairement une erreur de fond** dans l'app (les équations nationales par essence sont d'ailleurs *plus pertinentes* qu'un vrai tarif Algan pour un usage essence-spécifique, cf. §4) — mais c'est un **problème de traçabilité/attribution de source** qui mérite correction documentaire dans le code, et une vérification numérique des valeurs.

### 5.4 Résumé des écarts

| # | Objet | TarifData.kt | Source vérifiée | Sévérité |
|---|---|---|---|---|
| 1 | Formule Schaeffer 1E | `V=a+b×C²` (16 tarifs) | `V=(5/70000)(8+num)(D-5)(D-10)` — polynôme complet | Élevée — écart numérique >100 % constaté sur cas test |
| 2 | Nombre de tarifs Schaeffer 2E | 8 tarifs | 13 tarifs (avec sous-variantes .1/.2) trouvés dans une source | Moyenne — à confirmer sur table officielle |
| 3 | Attribution "Algan" des coefficients par essence | Nommé "Algan (1958)" | Ne correspond à aucune formule Algan trouvée ; ressemble aux équations nationales Vallet et al. 2006 | Élevée — problème de traçabilité de source, risque de coefficients non vérifiés essence par essence |

---

## 6. Recommandation pour GeoSylva

1. **Priorité haute — traçabilité des sources dans `TarifData.kt`** : réviser le bloc de commentaires (lignes 3-35) pour distinguer clairement (a) les tarifs Schaeffer/Algan génériques historiques (1 entrée, 2 entrées, non essence-spécifiques) et (b) les équations allométriques nationales par essence (probablement Vallet et al. 2006 / projet EMERGE), qui sont actuellement fusionnées sous le seul nom « Algan ». Ne pas modifier le code sans validation métier (hors périmètre de cette recherche), mais signaler ce point à l'équipe.
2. **Priorité haute — vérifier numériquement les 16 tarifs Schaeffer 1E** contre une table ONF/CTBA papier ou une source officielle scannée de bonne qualité (les scans HAL trouvés dans cette recherche n'étaient pas exploitables en texte). L'écart constaté (§5.1, +49 % à +142 % selon le cas testé) est trop important pour rester en l'état sans vérification manuelle.
3. **Priorité moyenne — compléter la table `schaefferTwoEntry`** avec les variantes `.1`/`.2` (potentiellement 4.1/4.2, 5.1/5.2, 8.1/8.2) si l'usage de l'app le justifie, après confirmation de la source officielle à 8 vs 13 tarifs.
4. **Priorité moyenne — citer la publication Vallet et al. (2006)** explicitement dans `TarifDocumentationScreen` si l'hypothèse du §5.3 est confirmée, car c'est une source scientifique solide (revue à comité de lecture) largement supérieure en crédibilité à une attribution erronée à « Algan (1958) ».
5. **Priorité basse — documentation utilisateur** : ajouter dans l'app un texte pédagogique reprenant le guide de décision du §4 (quand utiliser 1 entrée / 2 entrées / Algan historique / équations par essence), utile pour les professionnels forestiers utilisateurs de GeoSylva qui doivent justifier leur choix de méthode de cubage.

---

## 7. Limites et points à vérifier manuellement

- Les documents HAL (`hal-03382015v1`, `hal-03449676`) sont des fac-similés scannés anciens dont l'extraction automatique de texte (`webfetch`) a échoué ou n'a renvoyé que du contenu binaire/PDF brut illisible ; seuls les extraits indexés par les moteurs de recherche (snippets) ont pu être exploités. **Une lecture manuelle de ces PDF (via un lecteur PDF humain) est nécessaire** pour confirmer intégralement les tableaux numériques Algan/Schaeffer originaux.
- Le code source `PPtools` (Bruciamacchie) est un outil pédagogique/recherche non publié dans une revue à comité de lecture ; sa fiabilité repose sur la cohérence interne (recoupement avec les extraits historiques trouvés) plutôt que sur une autorité éditoriale formelle. Il est toutefois l'auteur d'un article scientifique de référence sur le sujet (*« Nouveaux regards sur des tarifs de cubage déjà anciens : les tarifs Schaeffer à une entrée »*, Forêt privée n°261, 2001), ce qui renforce la confiance dans son code.
- L'hypothèse du §5.3 (coefficients "Algan" de `TarifData.kt` = équations Vallet et al. 2006 mal attribuées) est une **inférence de cette recherche**, pas une certitude : elle repose sur la similarité de forme fonctionnelle (`a·D^b·H^c` par essence) et sur l'absence de toute autre source publiée correspondante trouvée. Une comparaison **valeur par valeur** des coefficients de `TarifData.kt` avec la table de Vallet et al. (2006) et ses extensions (projet EMERGE) reste à faire — non réalisée ici par manque d'accès à la table numérique complète de cette publication (probablement accessible via `hal.inrae.fr/hal-02664812` en téléchargeant le PDF complet, ce qui n'a pas été fait dans cette session pour des raisons de temps/format).
- Le tableau reconstitué du §2.3 (coefficients a₀, a₁, a₂ des tarifs SchR sous forme polynomiale en D) est un **calcul dérivé par l'agent**, pas une donnée trouvée telle quelle dans une source — il est mathématiquement équivalent à la formule sourcée en §2.2, mais toute réutilisation dans le code de production doit repartir de la formule originale `(D-5)(D-10)`, pas du polynôme développé (risque d'erreur de transcription).
- Aucune source officielle (ONF, IGN, CNPF) n'a pu être consultée directement dans cette session pour les tables numériques complètes — seules des sources scientifiques/pédagogiques tierces ont été mobilisées. Ceci est cohérent avec le constat déjà fait dans `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (statut « à revérifier »).
