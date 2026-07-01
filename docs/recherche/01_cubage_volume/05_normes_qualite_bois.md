# Normes de classement qualitatif du bois rond (NF EN 1316 / NF EN 1927) et grilles de décote professionnelles

**Domaine** : docs/recherche/01_cubage_volume/
**Date de recherche** : 2026-07-02
**Agent** : Sous-agent recherche — qualité du bois / pricing

---

## 0. Contexte et objectif

Ce document approfondit la section « 2.5 Classification qualité (NF EN 1316) » de
`docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (action High #9 : *« Aligner la classification qualité
A/B/C/D sur NF EN 1316 »*) et répond à l'action associée dans `RESEARCH_OPPORTUNITIES.md`. L'objectif
est de vérifier si la classification qualité actuellement codée dans GeoSylva
(`WoodQualityGrade.kt`, `PriceCalculator.kt`, `WoodDefect.kt`, `ProPricingEngine.kt`) est conforme
aux normes européennes citées, et de comparer les multiplicateurs de prix (×2.50 / ×1.50 / ×1.00 /
×0.40 mentionnés dans le référentiel) avec les usages professionnels réels.

**Constat préalable important** : l'audit du code (étape 3 de cette recherche) montre que
l'implémentation GeoSylva est **plus riche que ce que suggérait la formulation initiale du
référentiel**. Il existe en réalité *deux* couches :
1. `WoodQualityGrade` (`app/.../domain/calculation/quality/WoodQualityGrade.kt`) : un multiplicateur
   **générique et unique** par grade A/B/C/D (×2.5 / ×1.5 / ×1.0 / ×0.4), utilisé comme repli
   (`DefaultProductPrices`, évaluation rapide `QualityAssessment`).
2. `PriceCalculator.qualityCoefficients` (`app/.../domain/calculation/PriceCalculator.kt`) : une table
   de coefficients **spécifiques par essence** (ex. chêne sessile A=2.80/D=0.55, Douglas A=1.55/D=0.50,
   noyer A=3.20/D=0.38), présentée comme la source de vérité utilisée par `ProPricingEngine`.
3. `WoodDefect` (`app/.../domain/calculation/pricing/WoodDefect.kt`) : un système de **défauts
   individuels cumulables** (courbure, nœuds, roulure, pourriture…) avec une dépréciation en % par
   défaut et par sévérité, plafonnée à -90 % au total (`MAX_TOTAL_DEPRECIATION`), qui s'applique **en
   plus** du coefficient de grade dans `ProPricingEngine.buildResult()`.

Cette architecture à deux niveaux (grade global + défauts cumulés) est cohérente avec la logique des
normes NF EN 1316/1927, qui distinguent elles-mêmes un *classement global par lettre* (issu de
plusieurs singularités combinées) et une *liste de singularités élémentaires mesurables* (nœuds,
fentes, courbure…) — cf. §2.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| AFNOR — NF EN 1316-1:2012 | Officielle (norme) | Officielle | https://norminfo.afnor.org/norme/NF%20EN%201316-1/bois-ronds-feuillus-classement-qualitatif-partie-1-chene-et-hetre/77351 | 2012 (1re éd. 1997) |
| AFNOR — boutique NF EN 1316-1 (table des matières) | Officielle (norme, accès payant) | Officielle | https://www.boutique.afnor.org/fr-fr/norme/nf-en-13161/bois-ronds-feuillus-classement-qualitatif-partie-1-chene-et-hetre/fa036620/3940 | 2012 |
| AFNOR — NF EN 1927-1:2008 (épicéas/sapins) | Officielle (norme) | Officielle | https://www.boutique.afnor.org/en-gb/standard/nf-en-19271/qualitative-classification-of-softwood-round-timber-part-1-spruces-and-firs/fa143589/31249 | 2008 |
| AFNOR — NF EN 1927-2:2008 (pins) | Officielle (norme) | Officielle | https://norminfo.afnor.org/norme/NF%20EN%201927-2/classement-qualitatif-des-bois-ronds-resineux-partie-2-pins/79832 | 2008 |
| Stdfr.org — fiches NF EN 1927-1, NF B53-672-1 | Commerciale/tierce (agrégateur normes) | Commerciale/tierce — recoupe AFNOR | https://stdfr.org/1492271016.html | 2008 |
| Bois de France (FNB) — « Classement des bois ronds » | Officielle (organisme filière, Fédération Nationale du Bois) | Officielle (fédération professionnelle nationale) | https://preferezlesboisdefrance.fr/sapprovisionner-en-sciages-et-bois-ronds/classement-des-bois-ronds/ | Non daté, consulté 2026-07-02 |
| ONF — « Trier les bois pour mieux les valoriser » (Grésigne) | Officielle (ONF) | Officielle | https://www.onf.fr/onf/recherche/+/23a::trier-les-bois-pour-mieux-les-valoriser.html | 2018-12-10 |
| Cabinet Taurë (experts/exploitants forestiers, Bourgogne) — « La qualité et le prix du chêne… Chapitre 2 » | Commerciale/tierce (entreprise forestière) | Commerciale/tierce, mais descriptif technique cohérent avec la norme | https://www.forestiere-taure.fr/actualites/la-qualite-et-le-prix-du-chene-en-fonction-de-ses-singularites-chapitre-2 | Non daté, consulté 2026-07-02 |
| Alliance Forêts Bois — « Estimation des bois sur pied » | Commerciale/tierce (coopérative forestière, 1re coopérative française) | Commerciale/tierce | https://www.allianceforetsbois.fr/proprietaires-forestiers/exploitation-achat-de-bois/estimation-bois-pied/ | Non daté, consulté 2026-07-02 |
| CTFC/eForOwn — « Estimer la qualité des bois sur pied ou abattus et la valeur d'une coupe » | Scientifique/pédagogique (fiche de formation forestière, programme européen) | Commerciale/tierce (guide pédagogique, non AFNOR) | https://eforown.ctfc.cat/pdf/34_Estimer_la_qualite_des_bois_sur_pied_ou_abattus_et_la_valeur_d_une_coupe.pdf | Non daté — **extraction texte échouée** (PDF image/vecteur), contenu résumé via le snippet du moteur de recherche uniquement, `[À VÉRIFIER MANUELLEMENT]` |
| CNPF/IFC — Fiche Gestion 21 « Estimer et vendre ses bois » | Officielle (CNPF) | Officielle | https://ifc.cnpf.fr/sites/ifc/files/2024-03/Fiche%20Gestion%2021%20-%20Estimer%20et%20Vendre%20ses%20Bois.pdf | 2024-03 (mise à jour) — **extraction texte échouée** (PDF non extractible par l'outil web), contenu partiellement recoupé via référentiel existant, `[À VÉRIFIER MANUELLEMENT]` |
| Fordaq — « Normes de classement des résineux scandinaves (Livre bleu) » | Commerciale/tierce (négoce bois) | Commerciale/tierce — norme scandinave, **pas française**, cité pour comparaison méthodologique uniquement | https://bois.fordaq.com/fordaq/html/quality_softwood_bluebook_Fr.htm | Non daté |
| Code GeoSylva — `WoodDefect.kt`, `WoodQualityGrade.kt`, `PriceCalculator.kt`, `ProPricingEngine.kt`, `PricingCoefficients.kt` | Interne (implémentation actuelle) | — | `app/src/main/java/com/forestry/counter/domain/calculation/...` | Version repo au 2026-07-02 |

**Limite majeure identifiée** : le **texte intégral** des normes NF EN 1316-1, NF EN 1927-1/2/3 et
NF EN 1310 est **payant** (AFNOR Boutique, plusieurs dizaines à ~150 € par norme) et n'a **pas** pu
être consulté dans cette recherche. Toutes les données ci-dessous sur les *critères précis de
classement par lettre* proviennent de sources secondaires qui **résument** la norme (Fédération
Nationale du Bois, ONF, exploitants forestiers), pas du texte normatif lui-même. C'est conforme à la
règle méthodologique n°4 (franc-parler sur les lacunes) : **aucun accès libre au détail exact des
seuils numériques (diamètre de nœuds en mm, longueur de fentes en % du billon, etc.) n'a été trouvé**
pour cette recherche.

---

## 2. Données détaillées

### 2.1 Architecture normative française/européenne du classement des bois ronds

Le classement des bois ronds français s'appuie sur une série de normes du comité **CEN/TC 175
« Bois ronds et bois sciés »**, avec une répartition par essence :

| Norme | Essences couvertes | Statut |
|---|---|---|
| NF EN 1316-1:2012 (indice de classement B53-671-1, remplace NF B 53-302) | Chêne (*Quercus petraea*, *Q. robur*), Hêtre (*Fagus sylvatica*) | Officielle, en vigueur |
| NF EN 1316-2 | Peuplier (mentionné par la Fédération Nationale du Bois — classes Po-A à Po-C) | Officielle, en vigueur `[À VÉRIFIER MANUELLEMENT]` (non retrouvée séparément, déduite du tableau FNB) |
| NF EN 1927-1:2008 (NF B53-672-1) | Épicéa, Sapin | Officielle, en vigueur, remplace NF B 53-300:1991 |
| NF EN 1927-2:2008 | Pin sylvestre, pin noir, pin maritime, pin radiata | Officielle, en vigueur |
| NF EN 1927-3 | Douglas, Mélèze (cité par le code GeoSylva et par du contenu tiers, non retrouvé en fiche indépendante lors de cette recherche) | `[À VÉRIFIER MANUELLEMENT]` — existence confirmée par recoupement mais fiche AFNOR dédiée non localisée |
| NF EN 1310 | Toutes essences (bois ronds et sciages) — **méthode de mesure des singularités** (nœuds, fentes, flache, courbure…), pas un classement en soi | Officielle, référencée par NF EN 1316/1927 |
| NF B 52-001-1:2018 | Résineux de structure — classement mécanique ST-I/C30, ST-II/C24, ST-III/C18 (déroge de l'aspect vers la résistance mécanique) | Officielle, distincte de NF EN 1316/1927 mais citée en complément dans le code GeoSylva |

**Fait vérifié** : la nomenclature normalisée à 2 caractères est confirmée par la Fédération
Nationale du Bois (source officielle filière) : 1er caractère = initiale latine de l'essence
(Q = chêne/*Quercus*, F = hêtre/*Fagus*, Po = peuplier, Fr = frêne, Ac = érable/*Acer*), 2e caractère
= lettre de qualité A à D.

| Chêne | Hêtre | Peuplier | Frêne | Érables | Qualité |
|---|---|---|---|---|---|
| Q-A | F-A | Po-A | Fr-A | Ac-A | Qualité exceptionnelle |
| Q-B | F-B | Po-B | Fr-B | Ac-B | Qualité courante |
| Q-C | F-C | — | Fr-C | Ac-C | Moindre qualité |
| Q-D | F-D | Po-C | Fr-D | Ac-D | Autre qualité (≥ 40 % du volume du billon doit rester utilisable) |

Source : Fédération Nationale du Bois, https://preferezlesboisdefrance.fr/sapprovisionner-en-sciages-et-bois-ronds/classement-des-bois-ronds/

**Point important pour GeoSylva** : cette source confirme que les classes **ne portent pas
exactement le même nom** selon l'essence (Q-A, F-A, Po-A…), même si elles sont toutes notées A à D en
2e caractère. GeoSylva utilise un code générique "A/B/C/D" indépendant de l'essence
(`WoodQualityGrade.code`), ce qui est une **simplification acceptable pour l'usage métier** (le grade
reste comparable en interne) mais ne doit pas être présenté comme « le code normatif exact » dans la
documentation utilisateur — il faudrait dire « équivalent au rang de qualité A à D des normes NF EN
1316/1927 », pas « code Q-A » etc.

### 2.2 Résineux — NF EN 1927 : structure du classement

D'après la Fédération Nationale du Bois (même page), le tableau réservé aux résineux — issu de la
norme NF EN 1927 — retient également 4 classes A à D, mais l'appellation d'espèce en 1er caractère
n'a pas été retrouvée pour les résineux dans les sources en accès libre (les fiches AFNOR listent
« épicéas et sapins » Partie 1, « pins » Partie 2, sans préfixe lettré visible dans les extraits
consultés). `[À VÉRIFIER MANUELLEMENT]` sur la boutique AFNOR (accès payant).

Le **classement dimensionnel** (indépendant du classement qualitatif, mais combiné à lui pour fixer
le prix) est standardisé ainsi (source FNB) :

| Classe | Diamètre médian (cm, sur écorce) |
|---|---|
| D0 | < 10 |
| D1a | 10–14 |
| D1b | 15–19 |
| D2a | 20–24 |
| D2b | 25–29 |
| D3a | 30–34 |
| D3b | 35–39 |
| D4 | 40–49 |
| D5 | 50–59 |
| D6 | ≥ 60 |

Et pour la longueur (résineux uniquement) :

| Classe | Longueur | Appellation |
|---|---|---|
| L1 | < 3 m | Bois court |
| L2 | 3–6 m | Bois mi-long |
| L3 | 6,5–13,5 m | Bois long |
| L4 | > 13,5 m | Bois très long |

**Recommandation immédiate** : GeoSylva ne modélise **pas encore** ce double classement
dimension × longueur normalisé (D0-D6, L1-L4) ; le module de pricing raisonne en diamètre continu
(`diamCm`) et en classes de produits (mérain, sciage, BI…), ce qui est fonctionnellement plus riche
mais non alignable directement sur la nomenclature de classe D/L. Ce n'est **pas un défaut** en soi
(l'app vise la valeur métier, pas la conformité de nommage normatif), mais cela signifie que
GeoSylva ne peut pas revendiquer une conformité formelle « classement selon NF EN 1927 D3b/L2 » — à
préciser dans toute documentation commerciale.

### 2.3 Familles de singularités prises en compte (NF EN 1310 et normes filles)

Toutes les sources consultées (FNB, ONF, Cabinet Taurë, code GeoSylva) convergent vers les mêmes
grandes familles de singularités, ce qui confirme que le catalogue `WoodDefect.kt` de GeoSylva est
**structurellement complet et conforme** à l'esprit de la norme :

| Famille normative (NF EN 1310) | Singularités citées par les sources | Équivalent dans `WoodDefect.kt` (`DefectCategory`) |
|---|---|---|
| Caractéristiques de structure | Aubier, largeur d'accroissement, couleur | Non modélisé explicitement (hors périmètre défaut/décote) |
| Singularités de structure | Nœuds (sains/noirs/sautants/adhérents), fil tors, cœur excentré, entre-écorce, lunure, poche de résine | `CROISSANCE` : NOEUDS_SAINS, NOEUDS_NOIRS, NOEUDS_SAUTANTS, NOEUDS_POURRIS, FIBRE_TORSE, GOURMANDS, BROUSSINS |
| Singularités de forme du tronc | Courbure, cannelure, bosse/excroissance, méplat, décroissance (conicité), fentes | `FORME` : COURBURE, OVALISATION, CONICITE, FOURCHE, CANNELURE, MEPLAT, EMPATTEMENT |
| Fentes/altérations fibreuses | Gélivure, gerces, fente de cœur, cœur étoilé, roulure | `FENTES` : GELIVURE, GERCES, FENTES_RETRAIT, FENTE_COEUR, COEUR_ETOILE, ROULURE |
| Altérations fongiques | Pourriture cubique/fibreuse, chancre, bleuissement | `BIOLOGIQUE` : POURRITURE_CUBIQUE, POURRITURE_FIBREUSE, ECHAUFFURE, BLEUISSEMENT, COEUR_ROUGE, COEUR_COLORE |
| Altérations entomologiques | Piqûres, gros trous (capricorne, etc.) | `BIOLOGIQUE` : PIQURES_INSECTES, GALERIES_CAPRICORNE |
| Autres dégradations | Blessures, corps étrangers, trous d'oiseaux | Partiellement couvert (blessure non listée explicitement d'après l'extrait lu, `[À VÉRIFIER MANUELLEMENT]` sur le reste du fichier, tronqué à la lecture) |

**Point qualitatif intéressant (Cabinet Taurë)** : certains défauts n'ont **aucune** influence sur la
valeur pour certains usages : blessure (aucune influence), méplat (peu d'influence), graisse (peu
d'influence sauf tranche/merrain), cœur excentré (pas d'influence sauf tranche/ébénisterie/merrain).
Ceci confirme que la dépréciation d'un défaut est **dépendante du produit visé**, pas seulement de
l'essence — une nuance que `WoodDefect.kt` ne semble pas encore capturer (le `depreciationRange` est
fixe par défaut, indépendant du produit cible `ForestProduct`). C'est une piste d'amélioration
(voir §4).

### 2.4 Grille de correspondance singularités → usage (Cabinet Taurë, confirmé conceptuellement par NF EN 1310/1316)

| Usage visé | Tolérance de singularités |
|---|---|
| Tranche (déroulage/tranchage haut de gamme) | Aucune singularité acceptée |
| Merrain (tonnellerie) | Aucune singularité sur le fût ; gélivures et roulures tolérées (le bois est fendu, pas scié) |
| Ébénisterie | Une petite singularité tolérée (petite roulure de cœur, picot) |
| Menuiserie | Très peu de singularités (petite courbure, 1-2 picots, petite roulure de cœur) |
| Parquet | Picots et petits nœuds sains tolérés, bois peut être courbe |
| Charpente | Picots et nœuds sains tolérés, roulures tolérées sur grosses pièces |
| Traverses (chemin de fer / paysagères) | Tous défauts tolérés hors pourriture |

Ce tableau est cohérent avec la logique de `ForestProduct.minQuality` dans `WoodQualityGrade.kt`
(MERAIN/TRANCHAGE/DEROULAGE → grade minimum A ; CHARPENTE/BARDAGE → grade minimum B ;
SCIAGE_STD/PIQUET/PALETTE → grade minimum C ; BOIS_INDUSTRIE/PATE/BCh/BE → grade minimum D). **La
structure conceptuelle de l'app est donc validée par cette source tierce.**

### 2.5 Ordres de grandeur de prix par qualité (marché réel, non normatif)

| Source | Donnée | Fiabilité |
|---|---|---|
| Cabinet Taurë (exploitant forestier) | Chêne sur pied : 50 à 450 €/m³ selon qualité ; record 2022 forêt de Tronçais : 2 500 €/m³ (chêne d'exception) | Commerciale/tierce, ordre de grandeur qualitatif uniquement |
| ONF (Grésigne, vente 2018) | Lot merrain vendu 397–422 €/m³ vs lots sciage classés séparément (prix non публиés dans la source) | Officielle, cas réel documenté mais base de comparaison partielle |
| France Bois Forêt (déjà cité dans le référentiel existant) | Chêne moyen toutes qualités confondues 2024 : ~228 €/m³ | Officielle (déjà dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.1) |

**Analyse critique** : en combinant Taurë (50–450 €/m³, jusqu'à 2 500 €/m³ pour l'exceptionnel) et FBF
(228 €/m³ moyenne toutes qualités), l'écart empirique **qualité A vs qualité D pour le chêne** est de
l'ordre de **×5 à ×9** dans les cas extrêmes documentés, ce qui est **cohérent** avec le coefficient
spécifique déjà codé dans `PriceCalculator.qualityCoefficients["CH_SESSILE"]` = A/D = 2.80/0.55 → ratio
≈ ×5.1, et nettement **supérieur** au ratio générique de `WoodQualityGrade` (A/D = 2.5/0.4 → ratio
×6.25, toutes essences confondues). Aucune source primaire chiffrée (NF EN 1316 elle-même) n'a pu
être consultée pour valider un ratio « officiel » : ces ratios restent des **estimations de marché**,
pas des ratios normatifs — la norme fixe des **classes**, pas des **coefficients de prix**. C'est un
point de méthodologie important à corriger dans la documentation existante de GeoSylva : dire
« aligner les multiplicateurs sur NF EN 1316 » est **imprécis**, car cette norme ne fixe aucun
coefficient économique. Le bon cadrage serait : « aligner les *classes de qualité* (nombre, ordre,
critères qualitatifs) sur NF EN 1316/1927, et calibrer les *coefficients de prix* sur les données de
marché (France Bois Forêt, CEEB, ONF, coopératives) ».

### 2.6 Le classement structurel mécanique (NF B 52-001-1) — une 3e dimension distincte

Le code `PriceCalculator.kt` cite en commentaire un écart « ST-I/C30 = 1.5–1.6× ST-II/C24 » issu de
NF B 52-001-1:2018. Cette norme classe les résineux de structure par **résistance mécanique** (classes
C18/C24/C30, indépendantes de NF EN 1316/1927 qui sont des classements **d'aspect/qualité visuelle**).
Il s'agit d'une confusion potentielle à signaler : NF EN 1927 (aspect, grumes rondes) et NF B 52-001-1
/ NF EN 1611-1 (aspect des sciages) / normes de classement mécanique (EN 338, EN 14081) sont des
référentiels **distincts et non directement convertibles** l'un dans l'autre sans étude spécifique.
Le code GeoSylva mélange ces sources dans un même commentaire de justification économique — ce n'est
pas une erreur de calcul (le coefficient reste un chiffre de marché), mais la **citation de source**
mériterait d'être clarifiée pour ne pas laisser croire que NF B 52-001-1 fixe un coefficient de prix
Douglas A/D (elle fixe des classes mécaniques C18/C24/C30 sur sciages, pas un coefficient économique
sur bois ronds).

### 2.7 Comparatif chiffré : multiplicateurs GeoSylva vs. ratios de marché identifiés

| Comparaison | Grade A | Grade B | Grade C (réf.) | Grade D | Ratio A/D |
|---|---|---|---|---|---|
| `WoodQualityGrade` (générique, toutes essences) | ×2.50 | ×1.50 | ×1.00 | ×0.40 | 6.25 |
| `PriceCalculator` — Chêne sessile | ×2.80 | ×1.80 | ×1.00 | ×0.55 | 5.09 |
| `PriceCalculator` — Douglas | ×1.55 | ×1.20 | ×1.00 | ×0.50 | 3.10 |
| `PriceCalculator` — Noyer commun | ×3.20 | ×2.00 | ×1.00 | ×0.38 | 8.42 |
| `PriceCalculator` — Wildcard "*" (essence non listée) | ×1.80 | ×1.30 | ×1.00 | ×0.55 | 3.27 |
| Marché chêne observé (Taurë, ordre de grandeur qualitatif, `[À VÉRIFIER MANUELLEMENT]`, non normatif) | jusqu'à 2 500 €/m³ (exceptionnel) | — | ~228 €/m³ (moyenne FBF 2024, toutes qualités) | ~50 €/m³ (bas de fourchette) | ≈ 5 à 11 (fourchette large, non comparable directement) |

**Conclusion du comparatif** : le multiplicateur **générique** `WoodQualityGrade` (×2.5/×1.5/×1.0/×0.4,
ratio 6.25) est **globalement dans l'ordre de grandeur** observé sur le marché pour une essence
« moyenne », mais il est **surestimé pour le Douglas et les résineux courants** (ratio marché ~3, vs
6.25 dans le fallback générique) et **plutôt sous-estimé pour les feuillus précieux exceptionnels**
(noyer, cormier — où des écarts de ×8 à ×10+ sont plausibles en cas de qualité exceptionnelle réelle).
Or `WoodQualityGrade` n'est utilisé qu'en **repli** (`fallbackPrice` dans `ProPricingEngine.kt`, ligne
166-171) — la table par essence de `PriceCalculator` est la source principale et elle est **déjà bien
plus fine et déjà cohérente avec les écarts de marché documentés**. Le risque réel n'est donc pas
dans le grade générique lui-même, mais dans le fait qu'il **s'applique par défaut aux essences non
listées dans `PriceCalculator.qualityCoefficients`** (wildcard "*" à ×1.80/×1.30/×1.00/×0.55, ratio
3.27) — ce qui crée une **divergence entre `WoodQualityGrade` (ratio 6.25) et le wildcard
`PriceCalculator` (ratio 3.27) pour la même essence non répertoriée**, selon le chemin de code utilisé
(`fallbackPrice` vs `getQualityCoefficient` avec `qualityCoefficients["*"]`). C'est une **incohérence
interne à corriger** (voir §4), indépendamment de la norme.

---

## 3. Comparatif / analyse critique

| Aspect | NF EN 1316-1 (chêne/hêtre) | NF EN 1927-1/2 (résineux) | GeoSylva actuel |
|---|---|---|---|
| Nombre de classes | 4 (A–D, notées Q-A…Q-D / F-A…F-D) | 4 (A–D, non-préfixées d'après les sources consultées) | 4 (A–D, `WoodQualityGrade` enum) |
| Objet du classement | Qualité d'aspect/singularités du billon, destination non connue a priori | Idem, adapté aux singularités propres aux résineux (poches de résine, gerçures gel spécifiques) | Grade qualité + système de défauts cumulés séparé (`WoodDefect`) |
| Coefficient de prix associé par la norme | **Aucun** — la norme ne fixe pas de prix, seulement des classes | **Aucun** | Coefficients de prix codés en dur, par essence (`PriceCalculator`) ou génériques (`WoodQualityGrade`) |
| Défauts pris en compte | Nœuds, roulure, gélivure, fente, courbure, cœur excentré/coloré, pourriture, singularités de structure (renvoi NF EN 1310) | Mêmes familles + spécificités résineux (poches de résine, fil tors) | Catalogue `WoodDefect` : 26+ défauts, avec plage de dépréciation par sévérité (MINEUR/MODERE/MAJEUR) et plafond cumulé -90 % |
| Méthode de mesure | NF EN 1310 (règles de mesure des singularités — diamètre nœud, longueur fente, etc.) | Idem | Non modélisé au niveau « mesure physique » — l'utilisateur saisit un jugement de sévérité qualitatif, pas une mesure en mm |
| Accès au détail exact des seuils | Payant (AFNOR Boutique) | Payant (AFNOR Boutique) | Sans objet |

**Analyse** : sur le plan **structurel**, GeoSylva est globalement **aligné en esprit** avec les
normes NF EN 1316/1927 (4 classes A-D, catalogue de singularités par catégorie, distinction
classement global / défauts élémentaires). L'écart principal n'est pas structurel mais porte sur
deux points :
1. **Absence de méthode de mesure quantifiée** (NF EN 1310) : l'app repose sur une évaluation
   qualitative de sévérité (MINEUR/MODERE/MAJEUR) plutôt que sur des seuils métriques (diamètre de
   nœud en mm rapporté au diamètre du billon, longueur de fente en % de la longueur du billon, etc.).
   C'est un choix ergonomique défendable pour un usage terrain rapide, mais cela signifie que
   GeoSylva ne peut pas revendiquer une **conformité stricte et vérifiable** à NF EN 1310/1316/1927 —
   seulement une **inspiration** de leur structure.
2. **Coefficients de prix non normatifs** : comme détaillé en §2.5-2.7, aucune norme ne fixe de
   coefficient économique — le calibrage doit se faire sur des données de marché (France Bois Forêt,
   ONF, CEEB, coopératives), pas sur le texte de la norme elle-même.

---

## 4. Recommandation pour GeoSylva

### Correction de la documentation/communication (priorité haute, faible effort)
- Dans `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` §2.5 et dans toute doc utilisateur, **reformuler**
  la recommandation « aligner A/B/C/D sur NF EN 1316 » en distinguant clairement : (a) le **nombre et
  la nature des classes** (déjà aligné), et (b) les **coefficients de prix** (à calibrer sur données
  de marché, pas sur la norme — la norme ne prescrit aucun prix).
- Ajouter dans `WoodQualityGrade.kt` et `PriceCalculator.kt` un commentaire explicite rappelant que
  NF EN 1316/1927 fixent des **classes qualitatives**, et que les multiplicateurs de prix sont des
  **estimations de marché documentées** (France Bois Forêt, CEEB, ONF, CNPF), pas des valeurs
  extraites du texte normatif — pour éviter toute allégation commerciale incorrecte (« conforme à la
  norme NF EN 1316 » serait trompeur pour la partie prix).

### Incohérence interne à corriger (priorité moyenne)
- Fichiers concernés : `app/src/main/java/com/forestry/counter/domain/calculation/quality/WoodQualityGrade.kt`
  et `app/src/main/java/com/forestry/counter/domain/calculation/PriceCalculator.kt`.
- Le ratio A/D du grade générique `WoodQualityGrade` (6.25) diverge du ratio A/D du wildcard
  `PriceCalculator.qualityCoefficients["*"]` (3.27) alors que les deux servent de repli pour une
  essence non spécifiquement listée, selon le chemin de code (`ProPricingEngine.fallbackPrice` vs
  `ProPricingEngine.buildResult` → `PriceCalculator.getQualityCoefficient`). **Recommandation** :
  harmoniser en faisant de `PriceCalculator.qualityCoefficients["*"]` la seule source de vérité pour
  le cas générique, et faire pointer `WoodQualityGrade.multiplier` vers ces mêmes valeurs (ou
  supprimer le multiplicateur générique redondant de `WoodQualityGrade` si `DefaultProductPrices`
  peut consommer directement `PriceCalculator`).

### Dépendance produit-défaut (priorité moyenne, amélioration future)
- Fichier concerné : `WoodDefect.kt`.
- D'après le Cabinet Taurë (§2.3), l'impact d'un défaut dépend du **produit visé** (ex. le cœur
  excentré n'a pas d'impact en charpente mais en a en tranche/merrain ; la « graisse » n'a d'impact
  qu'en tranche/merrain). Le modèle actuel applique un `depreciationRange` fixe indépendant du
  produit cible (`ForestProduct`). Piste d'évolution (non urgente) : moduler `WoodDefect.depreciation()`
  selon le `ForestProduct` visé, ou au minimum documenter cette limite dans l'UI (« la dépréciation
  affichée est une moyenne toutes destinations, l'impact réel dépend du produit final »).

### Vérifications manuelles à faire avant toute communication normative (priorité haute)
- Acquérir/consulter le texte intégral d'au moins NF EN 1316-1:2012 et NF EN 1927-1:2008 (AFNOR
  Boutique, payant) pour confirmer les seuils exacts par classe (taille de nœuds, longueur de
  fentes en % du billon, tolérance de courbure en cm/m) avant d'écrire "conforme NF EN 1316" dans
  toute documentation destinée aux utilisateurs professionnels ou à un usage commercial/juridique.
- Vérifier l'existence et le contenu exact de NF EN 1316-2 (peuplier) et NF EN 1927-3 (Douglas/mélèze)
  citées indirectement mais non confirmées par une fiche AFNOR dédiée dans cette recherche.
- Revalider les deux PDF non exploitables lors de cette recherche (CTFC/eForOwn, CNPF Fiche Gestion
  21) via une extraction OCR manuelle ou une lecture directe — ils contiennent probablement des
  détails utiles sur la méthode de détermination pratique de la valeur d'un lot (non récupérés ici).

### Priorité pour la roadmap produit
- **Ne pas re-prioriser en urgence** un chantier de refonte du système de qualité : l'architecture
  actuelle (`WoodQualityGrade` + `WoodDefect` + coefficients par essence) est déjà **conceptuellement
  solide et plus riche que le strict minimum normatif**. L'effort à prioriser est la **clarification
  des sources dans les commentaires/documentation** (risque de communication trompeuse) plutôt que
  la réécriture du moteur de calcul.

---

## 5. Limites et points à vérifier manuellement

1. **Texte intégral des normes non consulté** (accès payant AFNOR) : tous les seuils numériques
   précis (mm de nœud, % de fente, tolérance de courbure) restent non vérifiés à la source primaire.
2. Les deux PDF suivants n'ont pas pu être exploités par l'outil de lecture web (retour binaire
   illisible) : CTFC/eForOwn "Estimer la qualité des bois sur pied…" et CNPF Fiche Gestion 21
   "Estimer et vendre ses bois" (2024-03). Une relecture manuelle (téléchargement + lecteur PDF
   local, ou OCR) est recommandée pour extraire d'éventuelles grilles de décote chiffrées precises.
3. Les ordres de grandeur de prix (Cabinet Taurë : 50–450 €/m³, record 2 500 €/m³) sont des
   **témoignages qualitatifs d'un acteur commercial régional (Bourgogne)**, pas une série
   statistique — à ne pas utiliser comme donnée de calibrage nationale sans recoupement avec
   l'Observatoire France Bois Forêt (déjà cité dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.1) ou
   les indices Agreste (https://agreste.agriculture.gouv.fr/, PDF trouvé mais non exploité dans le
   détail lors de cette recherche — piste à explorer pour un futur enrichissement chiffré).
3bis. L'existence de NF EN 1316-2 (peuplier) et NF EN 1927-3 (Douglas/mélèze) est déduite par
   recoupement (mentions indirectes) et non confirmée par une fiche AFNOR consultée directement —
   marqué `[À VÉRIFIER MANUELLEMENT]` dans le texte.
4. Cette recherche n'a pas pu comparer les coefficients GeoSylva à une **grille de décote chiffrée
   officielle d'un expert forestier ou d'une coopérative** (type barème interne Alliance Forêts
   Bois/UNISYLVA) : ces grilles internes ne sont généralement pas publiées en accès libre. Seule la
   structure générale de la démarche d'estimation (Alliance Forêts Bois) a pu être documentée, pas
   les coefficients chiffrés eux-mêmes.
5. Le lien entre NF B 52-001-1 (classement mécanique des sciages structuraux, C18/C24/C30) et les
   coefficients de prix bois ronds cités dans `PriceCalculator.kt` reste une **extrapolation du
   code existant**, pas une donnée directement issue de la norme — signalé en §2.6, à clarifier dans
   les commentaires du code (hors périmètre de cette recherche qui ne modifie pas le code Kotlin).
