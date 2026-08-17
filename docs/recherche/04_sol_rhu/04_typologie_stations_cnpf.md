# Typologie des stations forestières (CNPF/IGN) — diagnostic stationnel et automatisation

**Domaine** : docs/recherche/04_sol_rhu/
**Date de recherche** : 2026-07-02
**Agent** : sol-rhu / stations-cnpf

---

## Table des matières

1. [Sources identifiées](#1-sources-identifiées)
2. [Concepts : station, type de station, emboîtement GRECO > SER > station](#2-concepts--station-type-de-station-emboîtement-greco--ser--station)
3. [Catalogues et guides de stations régionaux (CNPF/IGN)](#3-catalogues-et-guides-de-stations-régionaux-cnpfign)
4. [Écogrammes : méthodologie et détermination de l'aptitude des essences](#4-écogrammes--méthodologie-et-détermination-de-laptitude-des-essences)
5. [Clé de détermination CNPF : observations terrain requises](#5-clé-de-détermination-cnpf--observations-terrain-requises)
6. [Pré-cartographie et cartographie prédictive des stations (SIG)](#6-pré-cartographie-et-cartographie-prédictive-des-stations-sig)
7. [Comparatif / analyse critique](#7-comparatif--analyse-critique)
8. [Faisabilité d'automatisation du diagnostic stationnel dans GeoSylva](#8-faisabilité-dautomatisation-du-diagnostic-stationnel-dans-geosylva)
9. [Recommandation pour GeoSylva](#9-recommandation-pour-geosylva)
10. [Limites et points à vérifier manuellement](#10-limites-et-points-à-vérifier-manuellement)

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| CNPF — « Les stations forestières » (page méthodo) | Officielle | Officielle CNPF | https://www.cnpf.fr/nos-actions-nos-outils/outils-et-techniques/les-stations-forestieres | Consultée 2026-07-02 |
| IGN — L'IF n°4, La typologie des stations forestières | Officielle | Officielle IGN | https://inventaire-forestier.ign.fr/IMG/pdf/L_IF_no04_typologie.pdf | s.d. (publication historique IFN) |
| IGN — Typologie des stations forestières, documents et études réalisés (TypoWeb) | Officielle | Officielle IGN | https://inventaire-forestier.ign.fr/IMG/pdf/TypoWeb_2008.pdf (et `TypoWeb_2007-2.pdf`) | 2008 |
| IGN — Fiches descriptives GRECO et SER | Officielle | Officielle IGN | https://inventaire-forestier.ign.fr/spip.php?article773= ; publication : https://inventaire-forestier.ign.fr/IMG/pdf/IF_SER_web.pdf | 2013 |
| CNPF Hauts-de-France/Normandie — Guide des stations forestières | Officielle | Officielle CNPF régional | https://hautsdefrance-normandie.cnpf.fr/guide-des-stations-forestieres-0 | Consultée 2026-07-02 |
| CNPF AURA — Guide Chablais / BEMC (exemples PDF) | Officielle | Officielle CNPF régional | https://auvergnerhonealpes.cnpf.fr/sites/socle/files/cnpf-old/402349_guide_chablais_1_1.pdf ; `402346_guide_bemc_1_1_1.pdf` | s.d. |
| Larrieu & Gonin (via HAL) — écogramme Flore forestière française | Scientifique | Scientifique (HAL, INRAE) | https://hal.science/hal-01470064v1/file/Larrieu_16386.pdf | 2016 (citations Rameau 1989/1993/2008) |
| Gaudin — Apport des bases de valeurs indicatrices (écogramme, groupes écologiques, logiciel Ecoflores) | Scientifique | Scientifique | http://sylvaingaudin.fr/PDF/Sesnr26-VI_et_GE.pdf | s.d. (Revue forestière) |
| Gembloux (ULiège) — Observer la végétation pour choisir une essence (écogramme NT/NH) | Pédagogique | Scientifique tierce (université) | https://www.gembloux.ulg.ac.be/gestion-des-ressources-forestieres/upload/Notes%20techniques/ntfg_09.pdf | s.d. |
| Fichier écologique des essences (Wallonie) — écogramme NT/NH | Officielle régionale | Officielle (SPW Wallonie) | https://www.fichierecologique.be/ ; https://geoportail.wallonie.be/catalogue/e281971d-776b-4793-99fd-ffc8e02d62d4.html | Consultée 2026-07-02 |
| Gégout & Piédallu — Cartographie prédictive des stations (massif vosgien) | Scientifique | Scientifique (HAL/INRAE) | https://hal.science/hal-03449798v1/file/037_060_GEGOUT.pdf ; https://hal.science/hal-00835906 | 2002 / repris ultérieurement |
| CRPF Normandie — Pré-cartographie des stations (WMS, 3 facteurs croisés) | Officielle | Officielle CNPF régional | https://hautsdefrance-normandie.cnpf.fr/pour-vous-aider-la-pre-cartographie-des-stations-forestieres-0 ; annexe : `la_cartographie_des_stations_forestieres_opt.pdf` | Consultée 2026-07-02 |
| Fransylva — diagnostic stationnel (sol, milieu, topographie, climat) | Tierce | Commerciale/tierce (coopérative) | https://www.fransylva.fr/preventions-risques-rechauffement-climatique.html | Consultée 2026-07-02 |

> **Note de lecture** : les PDF IGN (`TypoWeb_2008.pdf`, `L_IF_no04_typologie.pdf`) et Gembloux n'ont pas pu être
> extraits en texte par `webfetch` (binaires non convertis) — leur contenu a été reconstitué à partir des
> extraits renvoyés par le moteur de recherche et des pages HTML CNPF/HAL qui les citent. Les affirmations
> chiffrées précises (nombre exact de catalogues par région, numéros de pages) sont à confirmer par lecture
> manuelle des PDF `[À VÉRIFIER MANUELLEMENT]`.

---

## 2. Concepts : station, type de station, emboîtement GRECO > SER > station

### 2.1 Définitions (sources CNPF + IGN L'IF n°4)

- **Station forestière** : « une étendue de terrain sur laquelle les conditions physiques et biologiques
  (climat, propriétés du sol, composition floristique, etc.) sont homogènes » (IGN, L'IF n°4). Le CNPF
  ajoute le **relief/géologie** comme critère d'homogénéité. Une station est **réelle** (en forêt) ou
  **potentielle** (terrain susceptible d'être boisé). À une station donnée correspond, pour une essence
  et une sylviculture données, une **productivité comprise entre des limites connues**.
- **Type de station** : « regroupe les stations qui se ressemblent plus entre elles qu'elles ne ressemblent
  aux autres » (IGN) — c'est une **synthèse conceptuelle** construite par regroupement de stations observées
  sur le terrain. La **typologie** établit la classification des types existant sur un territoire.
- **Catalogue des stations** : document présentant, pour une **région forestière** donnée, l'inventaire des
  types de station avec leur description, leur clé de détermination et les préconisations sylvicoles.
- **Guide simplifié des stations** : version allégée du catalogue, destinée à un public plus large
  (propriétaires avertis), mettant l'accent sur les conseils de gestion et le choix d'essences.

### 2.2 Niveaux d'emboîtement

Le découpage écologique français (IGN/IFN) s'organise en niveaux emboîtés du plus large au plus fin :

```
GRECO  (Grandes Régions Écologiques)        11 GRECO + 1 GRECO d'alluvions = 12
   └─ SER  (Sylvoécorégions)                 86 SER + 5 SER d'alluvions récentes = 91
        └─ Région forestière (IFN historique) 309 régions regroupées dans les SER
             └─ Station  (terrain homogène)    unité réelle, quelques m² à plusieurs dizaines d'ha
                  └─ Type de station           unité conceptuelle (synthèse de stations)
```

- **GRECO** : 11 grandes régions écologiques (A-K) + 1 GRECO regroupant les 5 SER d'alluvions récentes
  (azonales, vallées des grands fleuves) = **12 GRECO** au total (source : IGN, `IF_SER_web.pdf`).
- **SER** : **91 sylvoécorégions** (86 + 5 d'alluvions), regroupant les 309 régions forestières IFN
  historiques avec redécoupage pour retrouver des limites climatiques/pédologiques.
- **Station / type de station** : niveau **infra-SER**, non national mais régional — chaque catalogue
  couvre une région forestière (ou un groupe de régions) à l'intérieur d'une SER.

> **Repère GeoSylva** : `GrecoRegion.kt` / `GrecoDetector.kt` couvrent déjà les 11 GRECO (A-K). La couche
> SER existe également (`SerRegion`/`SerDetector` à confirmer dans le code). La **station et le type de
> station sont les niveaux manquants** — c'est précisément l'objet de cette recherche.

### 2.3 Méthode phytoécologique (standard français)

La méthode « principalement employée pour décrire et classer les stations forestières en France » (CNPF)
est la **phytoécologie** : identifier les types de stations à partir du **caractère indicateur de la
végétation** qui y pousse. Les espèces aux exigences écologiques similaires forment des **groupes
écologiques** (ex. « espèces hygrophiles » = iris faux acore, laîche des marais, gaillet des marais,
lysimaque commune → station très engorgée → aulnaie marécageuse). Chaque typologie régionale précise ses
propres groupes écologiques.

Cette approche floristique doit **toujours être accompagnée d'observations pédologiques** (humus, sondage
à la tarière) — l'écogramme seul ne suffit pas (CNPF).

---

## 3. Catalogues et guides de stations régionaux (CNPF/IGN)

### 3.1 Existence et accès

- **Oui, les catalogues et guides existent en accès libre**, principalement sous deux formes :
  1. **Téléchargement libre sur le site IGN** (inventaire-forestier.ign.fr) — « les principaux documents
     (guides et catalogues), ainsi que la plupart des études régionales ou nationales de relations entre
     les stations et la production des essences sont disponibles en libre téléchargement » (CNPF).
  2. **Sites des délégations régionales CNPF** — les plus récents sont mis en ligne sur le site régional
     (ex. `hautsdefrance-normandie.cnpf.fr`, `auvergnerhonealpes.cnpf.fr`), les anciens diffusés en
     version papier.
- **Forme** : **PDF** (catalogues complets, guides simplifiés, fiches de terrain, clés de détermination).
  **Aucune base de données structurée ni API** n'a été identifiée — les catalogues sont des documents
  PDF narratifs avec clés dichotomiques textuelles et fiches descriptives.
- **Liste de référence** : le document IGN **« Typologie des stations forestières — Documents et études
  réalisés »** (`TypoWeb_2008.pdf` / `TypoWeb_2007-2.pdf`) est l'inventaire national des typologies
  disponibles par région administrative et région forestière `[À VÉRIFIER MANUELLEMENT — PDF non extrait]`.

### 3.2 Forme d'un catalogue (structure type, d'après IGN L'IF n°4)

Un catalogue comprend en principe **cinq parties** :
1. présentation générale de la région concernée ;
2. exposé de la méthode et des éléments diagnostiques (souvent des groupes écologiques d'espèces) ;
3. description des différents types de station, dans un ordre logique ;
4. **clé de détermination** des types ;
5. résultats synthétiques, annexes (dynamique de la végétation, habitats), conseils de mise en valeur.

### 3.3 Catalogues / guides identifiés (exemples, non exhaustif)

> La liste ci-dessous est **partielle** — elle résulte des recherches web et n'est pas une lecture
> exhaustive du `TypoWeb_2008.pdf`. À compléter manuellement par lecture du PDF IGN.

| Région / territoire | Document | URL / source | Accès |
|---|---|---|---|
| Normandie (fusion) | Guide unique des stations (synthèse anciens catalogues) | https://hautsdefrance-normandie.cnpf.fr/guide-des-stations-forestieres-en-normandie | Libre (CNPF régional) |
| Hauts-de-France (Artois, Ponthieu, Cambrésis, Santerre, St-Quentinois) | Guide des stations | https://hautsdefrance-normandie.cnpf.fr/sites/socle/files/cnpf-old/434229_guide_stations_forestieres_...pdf | Libre (PDF) |
| Hauts-de-France (Pays de Bray normand) | Catalogue des stations | https://hautsdefrance-normandie.cnpf.fr/sites/socle/files/cnpf-old/c_pays_bray_normand.pdf | Libre (PDF) |
| Auvergne-Rhône-Alpes (Chablais) | Guide des stations | https://auvergnerhonealpes.cnpf.fr/sites/socle/files/cnpf-old/402349_guide_chablais_1_1.pdf | Libre (PDF) |
| Auvergne-Rhône-Alpes (BEMC — Bas-Dauphiné ?) | Guide des stations | https://auvergnerhonealpes.cnpf.fr/sites/socle/files/cnpf-old/402346_guide_bemc_1_1_1.pdf | Libre (PDF) |
| Bourgogne (plateaux bourguignons) | Catalogue des types de stations (8 vol.) | https://side.developpement-durable.gouv.fr/PDLO/doc/SYRACUSE/155779/ | Libre (catalogue documentaire) |
| Massif Central (Parc Millevaches / Morvan) | Guide des stations et choix des essences (plateau Millevaches) | https://forets.parcdemillevaches.parcdumorvan.org/wp-content/uploads/2014/09/plateau-mille-vaches.pdf | Libre (PDF) |
| Massif vosgien | Cartographie prédictive des stations (Gégout & Piédallu) | https://hal.science/hal-00835906 | Libre (rapport scientifique) |
| GRECO A (Grand Ouest) | Clé de détermination et fiches des habitats forestiers | https://inventaire-forestier.ign.fr/IMG/pdf/clef_fiches_hab_greco_a_v2.pdf | Libre (PDF IGN) |

> **Lacune explicite (CNPF)** : « chaque document représente environ 2 à 3 ans de travail » — la couverture
> nationale est **inégale et incomplète** ; certaines régions n'ont pas de catalogue/guide récent. Les
> documents du siècle dernier ont une « disponibilité variable et souvent faible » (IGN, TypoWeb).

---

## 4. Écogrammes : méthodologie et détermination de l'aptitude des essences

### 4.1 Principe

L'**écogramme** est un diagramme à deux axes qui synthétise les exigences écologiques d'une essence (ou
d'une station) vis-à-vis des **deux principaux facteurs** de variation de la végétation, hors climat
(source : Flore forestière française, Rameau et al. 1989/1993/2008 ; confirmé par Larrieu & Gonin via HAL) :

- **Axe vertical — alimentation en eau (niveau hydrique)** : fonction de la **réserve utile maximale du
  sol**, de la pluviosité et des facteurs de compensation stationnels (confinement, circulation latérale).
- **Axe horizontal — alimentation minérale (niveau trophique / acidité)** : disponibilité en éléments
  minéraux (calcium, magnésium, potassium) dans la terre fine et recyclage des matières organiques.

> **Évolution du formalisme** (Larrieu & Gonin, HAL) : l'axe horizontal ne fait plus référence à
> l'**acidité** seule mais à la **fertilité minérale** (la corrélation acidité↔nutriments étant faible
> entre pH 4,5 et 6) ; quelques valeurs de pH significatives sont indiquées en correspondance. La
> relation stricte humus↔fertilité a été abandonnée (varie avec le macroclimat). Le **domaine calcaire**
> est séparé du non-calcaire par une **double barre verticale** (le CaCO₃ affecte la nutrition de certaines
> essences).

### 4.2 Deux aires par essence

Pour chaque essence, l'écogramme propose **deux aires** (Larrieu & Gonin) :
- **aire verte** : conditions **suffisantes pour assurer une production de bois rapide** (optimum de
  productivité) ;
- **aire jaune clair** : **amplitude écologique totale** de l'espèce (présence possible, productivité
  réduite ou sylviculture plus délicate).

> L'aptitude d'une essence à une station se détermine donc en **superposant la position de la station**
> (déduite de sa flore, cf. §5) **avec les aires verte/jaune de l'essence** sur l'écogramme. Pas de
> « seuil chiffré » universel : la méthode est **graphique et comparative**, fondée sur les groupes
> écologiques indicateurs plutôt que sur des valeurs numériques de pH/RU.

### 4.3 Outils dérivés

- **Logiciel Ecoflores** (Bartoli et al. 2000, cité par Gaudin) : positionne automatiquement un **relevé
  de végétation** sur l'écogramme à partir de la notion de groupe écologique — validation scientifique
  (Bruno & Bartoli 2001). Approche automatisable en principe.
- **Fichier écologique des essences** (SPW Wallonie, https://www.fichierecologique.be/) : implémente
  les niveaux trophique (NT) et hydrique (NH) dans un écogramme à double entrée, avec un niveau
  thermique (chaleur) comme troisième facteur. Outil opérationnel voisin de la démarche française.

### 4.4 Limites de l'écogramme

- **Deux axes seulement** (eau × minéraux) — le climat (chaleur, longueur de saison de végétation) est
  traité séparément (3 tomes de la Flore forestière : plaines/collines, méditerranéen, montagnes). Une
  classification anglaise ajoute un 3ᵉ axe climatique (cube au lieu de carré) — non standard en France.
- Ne capture pas la lumière, l'humidité atmosphérique, ni la dynamique de végétation.
- **Doit être complété par des observations pédologiques** (humus, tarière) — l'écogramme seul est
  insuffisant (CNPF).

---

## 5. Clé de détermination CNPF : observations terrain requises

### 5.1 Structure d'une clé

La reconnaissance des unités stationnelles sur le terrain se fait via une **fiche de relevés** et une
**clé de détermination** spécifiques à chaque guide, construites sur des **critères simples à observer**
(CNPF) :

1. **position topographique** (plateau, versant, bas de pente, fond de vallée, exposition) ;
2. **caractéristiques du sol** (texture, charge en éléments grossiers, drainage interne, profondeur,
   hydromorphie, calcaire actif, formes d'humus) — évaluées par **sondage à la tarière** ;
3. **flore** (végétation spontanée — espèces herbacées et ligneuses du sous-bois, rattachées aux groupes
   écologiques).

La clé renvoie à **une fiche par station** comportant :
- **partie 1 — diagnostic** : localisation, végétation typique, caractères essentiels du sol (confirmation) ;
- **partie 2 — potentialités** : préconisations sylvicoles et patrimoniales, essences adaptées.

### 5.2 Observations terrain requises (inventaire)

| Catégorie | Variable | Méthode d'observation | Automatisable dans GeoSylva ? |
|---|---|---|---|
| Topographie | Position (plateau/versant/fond), exposition, pente | Boussole + GPS + visuel | **Oui** (capteurs Android : GPS, compas, altimètre) |
| Sol | Texture (sable/limon/argile) | Sondage tarière + toucher | Non (saisie manuelle utilisateur) |
| Sol | Charge en éléments grossiers | Tarière, estimation visuelle | Non (saisie manuelle) |
| Sol | Profondeur explorée | Tarière | Non (saisie manuelle) |
| Sol | Hydromorphie (taches, horizon réductique) | Observation profil/tarière | Non (saisie manuelle, photo) |
| Sol | Calcaire actif (effervescence HCl) | Test HCl sur échantillon | Non (saisie manuelle) |
| Sol | Forme d'humus (mull, moder, mor, anmoor) | Observation litière | Non (saisie manuelle, photo) |
| Sol | pH | pH-mètre ou indicateur flore | Partiel (pH bio-indiqué par la flore) |
| Flore | Liste espèces présentes (herbacées + ligneuses) | Relevé phytosociologique | **Oui** (saisie assistée / reconnaissance IA future) |
| Flore | Abundance-dominance (coefficients Braun-Blanquet) | Estimation visuelle | Non (saisie manuelle) |
| Climat | Niveau thermique (plaine/montagne/médit.) | Cartographie / altitude | **Oui** (croisement GPS + SER/GRECO) |

### 5.3 Peut-on automatiser la clé CNPF dans une app ?

**Partiellement, oui** — mais avec une répartition nette entre ce qui est automatisable et ce qui reste
manuel :

- **Automatisable** : la **logique de la clé** (arbre de décision dichotomique) est codable ; la
  **position topographique** et le **contexte climatique** (GRECO/SER, altitude, exposition) sont
  déductibles du GPS + capteurs Android ; le **pH bio-indiqué** peut être estimé à partir de la flore
  saisie (cf. Ecoflores) ; la **pré-cartographie** (cf. §6) fournit un **pré-diagnostic géolocalisé**.
- **Non automatisable sans saisie humaine** : la **reconnaissance de la flore** (à ce jour, la
  reconnaissance automatique d'espèces herbacées forestières par photo n'est pas fiable à l'échelle
  requise), les **observations pédologiques** (texture, hydromorphie, humus, calcaire) qui nécessitent
  un sondage à la tarière et un œil expert.

> **Conclusion** : une app ne peut **pas se substituer** au sondage à la tarière et au relevé floristique
> expert. Elle peut en revanche **(a) fournir un pré-diagnostic géolocalisé** (pré-cartographie), **(b)
> guider l'utilisateur** dans la saisie structurée de ses observations, **(c) exécuter la clé** une fois
> les observations saisies, et **(d) afficher la fiche station + les essences adaptées**.

---

## 6. Pré-cartographie et cartographie prédictive des stations (SIG)

Deux approches complémentaires spatialisent les stations sans relevé de terrain exhaustif — **directement
pertinentes pour GeoSylva** (diagnostic par point GPS).

### 6.1 Pré-cartographie CRPF Normandie (CNPF)

- **Principe** : pré-zonage des stations potentielles par **croisement de 3 facteurs écologiques
  modélisés, spatialisés et reclassifiés** (annexe CNPF Normandie) :
  1. **Niveau trophique** = pH bio-indiqué des sols (obtenu par analyse de la végétation) ;
  2. **Réserve en eau maximale** des sols (estimée sur 90 cm de profondeur) ;
  3. **Niveau d'hydromorphie** = probabilité d'hydromorphie marquée à différentes profondeurs.
- **Méthode** : modèles statistiques prenant en compte **géologie, topographie, ancienneté de l'état
  boisé, effet des peuplements**. Croisement → **36 unités cartographiques de sols (UCS)**.
- **Diffusion** : **lien WMS** pour consulter directement la pré-cartographie sous SIG ; cartes
  géologiques BRGM 1/50 000 (couche « carte géologique vecteur harmonisé ») utilisées en entrée.
- **Limite** : « la pré-cartographie ne vaut pas carte définitive » — précision variable selon contextes
  géologiques ; outil de **pré-zonage** pour accélérer la cartographie de terrain, pas un substitut.

### 6.2 Cartographie prédictive (Gégout & Piédallu, massif vosgien — INRAE/ENGREF)

- **Principe** : modélisation de **3 facteurs écologiques** (richesse minérale, climat/bioclimat,
  réserve en eau) identifiés dans les typologies, puis **spatialisation** via des variables numériques
  (modèle AURELHY de Météo-France, géologie, etc.), **discrétisation en 4 à 6 classes**, et **croisement
  des 3 couches SIG** → chaque type de station = une combinaison originale de modalités.
- **Résolution** : information codée au **quart d'hectare** (≈ 50 × 50 m) — compatible avec une
  interrogation par point GPS.
- **Validation** : travail financé GIP ECOFOR + Région Lorraine, restitué 2002, repris/corrigé
  ultérieurement (détection d'erreurs de codage géologique, amélioration des équations).
- **Application** : potentialité de présence d'essences déduite de la carte des stations.

> **Pertinence GeoSylva** : cette approche **prédictive par point GPS** est exactement le cas d'usage
> visé — un utilisateur sur une parcelle obtient un **pré-diagnostic de station** sans relevé exhaustif.
  La limite est la **disponibilité des couches SIG** (modèles régionaux, pas national) et leur
  **licence d'accès** (à vérifier cas par cas — les travaux INRAE sont en HAL/open access pour la
  méthodologie, mais les **rasters produits** ne sont pas nécessairement librement téléchargeables).

---

## 7. Comparatif / analyse critique

| Approche | Granularité | Couverture | Accès data | Automatisation GeoSylva | Fiabilité |
|---|---|---|---|---|---|
| Catalogues/guides CNPF (PDF) | Région forestière (infra-SER) | Inégale, incomplète | Libre (PDF) | Clé codable, mais **saisie terrain manuelle obligatoire** | Référence (expert) |
| Écogramme Flore forestière | National (3 tomes) | France entière | Ouvrage (IDF, payant) | Graphique, superposition station↔essence | Référence scientifique |
| Pré-cartographie CRPF Normandie (WMS) | Parcelle (UCS) | Normandie uniquement | WMS libre (régional) | **Oui** — interrogation par point GPS | Pré-zonage (à confirmer terrain) |
| Carte prédictive Gégout (massif vosgien) | Quart d'hectare | Massif vosgien uniquement | Méthodo libre, rasters à vérifier | **Oui** — interrogation par point GPS | Validée scientifiquement |
| Fichier écologique essences (Wallonie) | Station (NT/NH) | Wallonie (transfrontalière) | Outil web libre | Inspirant (modèle NT/NH/thermique) | Officiel régional |

**Constat critique** : il **n'existe pas de pré-cartographie/carte prédictive nationale** des stations
  forestières librement accessible par point GPS. Les approches prédictives sont **régionales** (Normandie,
  Vosges) et leurs rasters ne sont pas systématiquement ouverts. La couverture en catalogues CNPF est
  **inégale** (certaines régions sans document récent). Un diagnostic stationnel **national** dans
  GeoSylva ne peut donc pas reposer sur une seule source — il faut une **approche hybride** (cf. §8).

---

## 8. Faisabilité d'automatisation du diagnostic stationnel dans GeoSylva

### 8.1 Cas d'usage cible

> À partir d'un **point GPS** + **observations terrain** (sol, flore), déterminer le **type de station**
> et les **essences adaptées**.

### 8.2 Données requises

| Donnée | Source | Disponibilité | Statut GeoSylva |
|---|---|---|---|
| GRECO/SER du point GPS | IGN (BD Forêt, couches SER) | Libre | **Déjà intégré** (`GrecoDetector`, SER) |
| Géologie (BRGM 1/50 000) | BRGM | Libre (WMS/WFS) | À intégrer |
| Pré-cartographie stations (régionale) | CNPF régional (WMS) | Libre mais **régional, partiel** | À intégrer par région |
| Catalogue/guide régional (clé + fiches) | CNPF/IGN (PDF) | Libre (PDF) | À **numériser** manuellement (clé → arbre de décision) |
| Écogramme essences (aires verte/jaune) | Flore forestière française (IDF) | Ouvrage payant | À extraire/saisir (cf. limites) |
| Groupes écologiques indicateurs | Catalogues régionaux / Flore forestière | Libre (PDF) / payant | À numériser |
| Observations terrain (sol, flore) | Utilisateur | — | UI de saisie à construire |

### 8.3 Limites

1. **Pas de pré-cartographie nationale ouverte** — couverture régionale fragmentée ; certaines régions
   sans aucune couche prédictive ni même catalogue récent.
2. **La flore est le facteur clé** or la **reconnaissance automatique d'espèces herbacées forestières
   par photo n'est pas fiable** aujourd'hui → saisie manuelle obligatoire (ou intégration future d'un
   modèle de vision par essence, hors de portée court terme).
3. **Le sondage à la tarière n'est pas automatisable** (texture, hydromorphie, humus, calcaire) —
   l'app ne peut que **guider la saisie**.
4. **Les écogrammes de la Flore forestière française sont dans un ouvrage payant** (IDF) — extraction
   des aires verte/jaune par essence à faire **manuellement** (droits d'auteur à respecter, cf. limites).
5. **Les clés CNPF sont régionales et hétérogènes** — pas de format standardisé national ; numériser
   chaque clé est un travail manuel par région.
6. **La précision prédictive est limitée** (pré-cartographie = pré-zonage, pas carte définitive) —
   l'app doit **afficher un pré-diagnostic à confirmer**, jamais un verdict.

### 8.4 Approche recommandée (par phases)

**Phase 1 — Pré-diagnostic géolocalisé (court terme, faisable)**
- À partir du point GPS : déterminer **GRECO + SER** (déjà intégré) → restreindre le catalogue
  régional applicable.
- Croiser avec **géologie BRGM** (WMS) + **altitude/exposition** (capteurs Android) + **données
  climatiques** déjà présentes (Open-Meteo / normales) → estimer un **pré-positionnement sur l'écogramme**
  (niveau hydrique approximatif via réserve en eau déduite de la géologie/pente/profondeur ; niveau
  trophique approximatif via géologie calcaire/non-calcaire).
- Afficher : « station probable : [type(s) candidat(s)] — à confirmer par relevé terrain ».

**Phase 2 — Saisie structurée des observations terrain (court-moyen terme)**
- UI de **fiche de relevés** (modèle CNPF) : position topographique (auto capteurs), exposition/pente
  (auto), texture/charge/profondeur/hydromorphie/calcaire/humus (saisie manuelle guidée), **liste
  d'espèces** (saisie manuelle avec autocomplétion sur une base floristique).
- Exécuter la **clé régionale numérisée** (arbre de décision) → type de station.

**Phase 3 — Recommandation d'essences (moyen terme)**
- Superposer la position station sur l'écogramme avec les **aires verte/jaune par essence** (table
  `EssenceEcogramAreas` à constituer) → aptitude (optimale / possible / déconseillée).
- Afficher les **préconisations sylvicoles** de la fiche station correspondante.

**Phase 4 — Intégration des pré-cartographies régionales ouvertes (moyen-long terme)**
- Connecter les **WMS régionaux** disponibles (Normandie, autres à recenser) pour affiner le
  pré-diagnostic Phase 1 dans les régions couvertes.
- À terme, envisager un **modèle prédictif national** léger (inspiré Gégout) si des rasters ouverts
  émergent — sinon rester sur l'approche hybride géologie+climat+flore.

### 8.5 Verdict de faisabilité

- **Court terme** : **faisable et à forte valeur** — un **pré-diagnostic géolocalisé** (Phase 1) +
  **fiche de relevés guidée** (Phase 2) + **recommandation d'essences** (Phase 3) est réalisable sans
  nouvelle donnée externe lourde, en s'appuyant sur GRECO/SER (existant) + géologie BRGM (ouvert) +
  écogrammes (à saisir manuellement pour les essences prioritaires).
- **Verrou principal** : la **numérisation manuelle des clés régionales** et des **écogrammes par
  essence** — travail éditorial, pas technique. Commencer par **2-3 régions pilotes** disposant d'un
  guide CNPF récent en PDF libre + les essences majeures de ces régions.
- **À ne pas promettre** : un diagnostic stationnel **automatique et fiable sans aucune saisie
  terrain** — la flore et le sol exigent l'œil d'un gestionnaire. L'app est un **assistant**, pas un
  remplaçant du diagnostic expert.

---

## 9. Recommandation pour GeoSylva

1. **Priorité 1 — Pré-diagnostic géolocalisé** : dans `StationDiagnosticScreen` (déjà cité dans
   `REFERENTIELS_FORESTIERS_EXTERNES.md` §4.2), ajouter une étape « station probable » calculée à partir
   de GRECO/SER (existant `GrecoDetector.kt`/`SerDetector`) + **géologie BRGM** (couche WMS à intégrer
   dans le service géospatial) + altitude/exposition (capteurs). Afficher explicitement « pré-diagnostic
   à confirmer par relevé terrain ».
2. **Priorité 2 — Fiche de relevés guidée** : créer une UI de saisie structurée (modèle fiche terrain
   CNPF) — position topographique auto, sol manuel, flore manuelle avec autocomplétion. Réutiliser le
   formalisme des groupes écologiques (à numériser par région pilote).
3. **Priorité 3 — Écogramme essences** : constituer une table `EssenceEcogramAreas` (aire verte / aire
   jaune, coordonnées normalisées niveau hydrique × niveau trophique) pour les essences majeures,
   extraite **manuellement** de la Flore forestière française (respecter droits IDF — usage interne /
   citation, pas de redistribution des figures). Superposer position station ↔ aires essence.
4. **Priorité 4 — Régions pilotes** : numériser les clés de **2-3 régions** avec guide CNPF récent en
   PDF libre (ex. Normandie, Hauts-de-France, AURA-Chablais) comme preuve de concept, avant extension.
5. **Priorité 5 — Pré-cartographies WMS régionales** : recenser les WMS ouverts (Normandie confirmé,
   autres à inventorier) et les brancher comme raffinement du pré-diagnostic là où disponibles.
6. **Citation obligatoire** : citer IGN (L'IF n°4, TypoWeb), CNPF (page stations), Flore forestière
   française (Rameau et al.), et Gégout & Piédallu dans la documentation in-app.

> Fichiers Kotlin concernés (connus / probables) : `StationDiagnosticScreen.kt`, `GrecoDetector.kt`,
> `GrecoRegion.kt`, `SerDetector`/`SerRegion` (à confirmer), service géospatial (intégration WMS BRGM),
> future `StationKeyEngine` / `EcogramEngine` / `EssenceEcogramAreas` (à créer).

---

## 10. Limites et points à vérifier manuellement

1. **PDF IGN non extraits** (`TypoWeb_2008.pdf`, `L_IF_no04_typologie.pdf`, `clef_fiches_hab_greco_a_v2.pdf`)
   — la **liste exhaustive des catalogues par région** et le **nombre exact de types par catalogue** n'ont
   pas pu être extraits automatiquement (binaires). `[À VÉRIFIER MANUELLEMENT]` — ouvrir les PDF et
   compiler la liste régionale complète.
2. **Droits d'auteur Flore forestière française** (IDF/IDF-Sylva) : les écogrammes et aires par essence
   sont dans un **ouvrage payant** — l'extraction systématique des figures pour redistribution dans une
   app commerciale soulève des questions de droits. Préférer une **re-numérisation** des positions
   (coordonnées normalisées NH/NT) plutôt que la copie des figures, et **citer** Rameau et al. Vérifier
   la politique de l'IDF/CNPF sur l'usage dérivé `[À VÉRIFIER MANUELLEMENT]`.
3. **Disponibilité des rasters prédictifs** (Gégout massif vosgien, pré-carto Normandie) : la
   **méthodologie** est en open access (HAL), mais les **couches SIG produites** ne sont pas
   nécessairement librement téléchargeables — vérifier licence et accès auprès des CRPF/INRAE
   `[À VÉRIFIER MANUELLEMENT]`.
4. **Licence WMS BRGM** (carte géologique 1/50 000 vecteur harmonisé) : conditions d'usage commercial
   à confirmer (BRGM open data, mais clauses d'usage API à vérifier) `[À VÉRIFIER MANUELLEMENT]`.
5. **Numéros de version / dates** des guides CNPF régionaux (Chablais, BEMC, Normandie) non extraits —
   à dater précisément avant citation in-app.
6. **Existence d'une couche SER dans GeoSylva** : la recherche assume qu'une détection SER existe au
   côté de `GrecoDetector` — à confirmer dans le code (`SerDetector`/`SerRegion` non lus dans cette
   session) `[À VÉRIFIER MANUELLEMENT]`.
7. **Logiciel Ecoflores** (Bartoli et al. 2000) : disponibilité, licence et code source non vérifiés —
   pourrait inspirer l'algorithme de positionnement flore→écogramme, mais à évaluer séparément.
8. **Aucune API nationale des stations** n'a été identifiée — toute intégration passe par la
   numérisation manuelle de PDF régionaux ou par des couches SIG régionales éparses. Confirmer l'absence
   d'un service national (IGN/CNPF) avant de conclure `[À VÉRIFIER MANUELLEMENT]`.
