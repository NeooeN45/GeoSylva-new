# Marché carbone forestier français (Label Bas-Carbone) et faisabilité GeoSylva
**Domaine** : docs/recherche/02_marche_prix/
**Date de recherche** : 2026-07-05 (recherche web)
**Agent** : recherche marché/prix — carbone forestier

## 0. Périmètre et non-duplication

Ce document couvre le **marché** de la valorisation carbone forestière (méthodes, prix,
plateformes). La **physique** de la conversion volume → carbone (formule
`C = VIFN × DEN × FEB × FER × CAR`, FER = 1.30 conifères / 1.28 feuillus, CAR = 47.5 %) est déjà
traitée et sourcée dans
`docs/recherche/01_cubage_volume/03_coefficients_forme_biomasse.md` (§2.3, IGN/CARBOFOR 2004,
mise à jour méthodo IGN mars 2021) — **ne pas la redupliquer ici**, s'y référer directement.
`RESEARCH_OPPORTUNITIES.md` (l.70-71, 109-110) mentionne l'ADEME Impact CO2/Base Carbone et la
lib LERFoB "CAT (Carbon Accounting)" comme pistes non explorées ; ce document les traite en
partie (Base Carbone) et écarte CAT comme pour LERFoB Forest Tools (blocage AWT, cf. doc
biomasse). `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` ne contient aucune mention carbone à ce
jour — une entrée y est recommandée en §4.

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| Ministère Transition écologique — Label Bas-Carbone, page "Présentation des méthodes" | officielle | officielle (gouv.fr) | https://label-bas-carbone.ecologie.gouv.fr/presentation-des-methodes-du-label-bas-carbone | maj 06.10.2025 |
| Ministère — méthode Boisement | officielle | officielle | https://label-bas-carbone.ecologie.gouv.fr/la-methode-boisement | V3, décision du 04.09.2025 |
| Ministère — méthode Balivage | officielle | officielle | https://label-bas-carbone.ecologie.gouv.fr/la-methode-balivage | maj 27.03.2025 |
| Ministère — méthode Reconstitution de peuplements forestiers dégradés | officielle | officielle | https://label-bas-carbone.ecologie.gouv.fr/la-methode-reconstitution-de-peuplements-forestiers-degrades | — |
| Ministère — méthode Gestion Forestière à Stock Continu (GFSC) | officielle | officielle | https://label-bas-carbone.ecologie.gouv.fr/methode-gestion-forestiere-stock-continu | — |
| Ministère — "Financer un projet" (fourchette de prix officielle) | officielle | officielle | https://label-bas-carbone.ecologie.gouv.fr/financer-un-projet | — |
| InfoCC / Geres — "État des lieux de la contribution carbone vue de France", édition 2025 | tierce (étude sectorielle) | scientifique/tierce sérieuse (co-financée acteurs du secteur, méthodologie déclarée) | https://www.geres.eu/s-informer/decryptages/infocc-10-ans-pour-eclairer-un-marche-carbone-en-pleine-transformation/ | édition 2025 (données 2024) |
| Les Echos, citant I4CE (Institut de l'économie pour le climat) | scientifique/presse | scientifique (I4CE = think tank reconnu) | https://www.lesechos.fr/politique-societe/societe/transition-ecologique-le-label-bas-carbone-un-outil-de-financement-a-ameliorer-2168264 | 2025 (bilan au 31.03.2025) |
| ResoilAg — "La contribution carbone en France en 2022" | commerciale/tierce | commerciale (mais cite InfoCC comme source primaire) | https://www.resoilag.com/blog/la-contribution-carbone-en-france | 2023 (données 2022) |
| ResoilAg — "Quel est le prix d'un crédit carbone du Label bas-carbone ?" | commerciale/tierce | commerciale | https://www.resoilag.com/blog/quel-est-le-prix-dun-credit-carbone-du-label-bas-carbone | 31.01.2024 |
| EcoTree — page produit "Crédits carbone Label Bas-Carbone – Forêt" | commerciale | commerciale | https://ecotree.green/entreprises/credits-carbone/label-bas-carbone-foret | consulté 2026-07-05 |
| Sylv'O2 (coopérative CFBL/Forêt d'Ici/Unisylva) — page Label bas carbone | commerciale | commerciale | https://www.sylvo2.fr/le-label-bas-carbone.html | consulté 2026-07-05 |
| Fransylva Services — "Label bas-carbone forestier" | commerciale/tierce (syndicat propriétaires) | commerciale (mais organisme professionnel officiel) | https://www.fransylvaservices.fr/services-label-bas-carbone | consulté 2026-07-05 |
| ADEME — Base Carbone®, jeu de données complet (data.gouv.fr) | officielle | officielle (ADEME) | https://www.data.gouv.fr/datasets/base-carbone-complete-de-lademe-en-francais-v17-0 | v17.0 |
| ADEME — Base Carbone, doc "Bois et articles en bois" | officielle | officielle (ADEME) | https://prod-basecarbonesolo.ademe-dri.fr/documentation/UPLOAD_DOC_FR/bois_et_articles_en_bois.htm | consulté 2026-07-05, non daté précisément dans l'extrait — **[À VÉRIFIER MANUELLEMENT]** |
| CNPF — "Forêt et Carbone" | officielle | officielle (CNPF) | https://www.cnpf.fr/nos-actions-nos-outils/nos-actions/foret-et-carbone | consulté 2026-07-05 |

## 2. Données détaillées

### 2.1. Les 4 méthodes forestières du Label Bas-Carbone (LBC)

Le LBC est un dispositif **officiel**, créé par décret n°2018-1043 du 28.11.2018, piloté par la
DGEC (Direction Générale de l'Énergie et du Climat) du ministère de la Transition écologique. Il
n'y a **pas** de "méthode Boisement + méthode Balivage" isolées : ce sont 2 des **4 méthodes
forestières** actuellement approuvées, toutes rédigées par le **CNPF** :

| Méthode | Objet | Durée projet | Essences | Surface min | Version en vigueur |
|---|---|---|---|---|---|
| **Boisement** | Plantation sur terrain non boisé depuis ≥10 ans | 30 ans (vérif./délivrance des crédits tous les 5 ans) | Selon arrêtés MFR (matériel forestier de reproduction) du ministère de l'Agriculture | 0,5 ha | V3, 04.09.2025 |
| **Reconstitution de peuplements forestiers dégradés** | Reboisement après dégâts lourds (incendie, tempête, crise sanitaire type scolytes) | — | — | — | — |
| **Balivage** (conversion taillis → futaie sur souches) | Prolongation de la révolution d'un taillis feuillu (10-30 ans) au-delà de l'âge d'exploitabilité, sur stations de bonne fertilité | 30 ans | **Feuillus uniquement** ; résineux, taillis courte/très courte rotation et taillis-sous-futaie exclus | — | maj 27.03.2025 |
| **Gestion Forestière à Stock Continu (GFSC)** | Augmentation du stock de carbone sur pied par une sylviculture qui maintient un couvert continu (futaie irrégulière) | — | — | — | — |

**Critères d'éligibilité communs (méthode Boisement, détaillés)** :
- Maîtrise foncière du terrain pour ≥ 30 ans (propriétaire de préférence) ;
- Terrain non boisé durant les 10 ans précédant la notification (accrus/broussailles tolérés si
  volume bois fort estimé ≤ 15 m³/ha, à justifier par photo/orthophoto) ;
- Document de gestion durable requis ;
- Surface minimale 0,5 ha (parcelle ou groupe de parcelles attenantes) ;
- Examen au cas par cas par l'autorité environnementale obligatoire au-delà de 0,5 ha (art.
  R.122-3 s. code de l'environnement) — arrêté préfectoral à fournir avant dépôt du dossier LBC ;
- Essences conformes aux arrêtés MFR ;
- Aucuns travaux commencés avant la notification du projet ne sont éligibles.

**Type de crédits** : les crédits Boisement sont **ex-ante** (anticipés, avant réalisation
effective de la séquestration), contrairement à des crédits ex-post. Ils subissent des **rabais**
(mécanisme de sécurisation du LBC) : rabais général de 10 % (risques généraux), rabais de 40 % si
le calcul BASI (Biomasse Aérienne Sèche Instantanée ?) n'est pas réalisé, rabais additionnel 0-15 %
selon d'autres critères — **[À VÉRIFIER MANUELLEMENT]** le détail exact des rabais n'a pas pu être
extrait intégralement de la page source (contenu tronqué lors de la recherche), se référer au PDF
officiel de la méthode (https://label-bas-carbone.ecologie.gouv.fr/sites/default/files/2025-07/M%C3%A9thode%20boisement%20V2.pdf).

**Pièces justificatives pour un dossier Balivage** (exhaustif d'après la source officielle) :
preuve de propriété, état actuel des parcelles, **classe de fertilité et densité de tiges
d'avenir**, table(s) de production, tableur de calcul des co-bénéfices, calcul de la VAN (si
demandé), preuve du risque incendie (sous conditions), tableur de calcul des réductions
d'émissions. Ces pièces recoupent directement des données qu'un inventaire GeoSylva produit déjà
(cf. §4).

### 2.2. Comptabilisation des tonnes de CO2 (principe général LBC forêt)

Le calcul certifié suit systématiquement le principe :

> Crédits carbone = (Séquestration du **scénario projet**) − (Séquestration du **scénario de
> référence**, i.e. la poursuite de l'usage antérieur du terrain : agriculture, enfrichement
> spontané, ou maintien en taillis simple pour Balivage)

Pour Boisement V3 (révision de septembre 2025), le niveau de référence d'accroissement naturel
d'un enfrichement spontané a été **revu à la baisse** par rapport à la V2 précédente en raison
d'une incertitude scientifique documentée, dans l'attente des résultats de l'étude COMFOR
(ONF/CNPF/INRAE/AgroParisTech) — signal que **ce paramètre de référence évolue régulièrement**
et n'est pas un chiffre figé exploitable comme constante applicative.

Le calcul de la séquestration du scénario projet lui-même s'appuie, pour la partie physique
(volume bois → carbone), sur la même méthodologie CARBOFOR/IGN que documentée dans
`01_cubage_volume/03_coefficients_forme_biomasse.md` — le LBC forestier n'a **pas** de formule
carbone alternative, il encadre le **calcul différentiel** (projet − référence) et les **rabais
de risque**, pas la physique de la biomasse elle-même.

### 2.3. Prix du crédit carbone forestier français (marché volontaire)

| Source | Année des données | Prix moyen LBC (tous secteurs) | Prix moyen LBC forêt spécifiquement | Fourchette |
|---|---|---|---|---|
| InfoCC état des lieux 2023 (donnée 2022) | 2022 | 33 €/tCO2e (LBC, tous secteurs) | non isolé | — |
| ResoilAg (citant InfoCC) | 2021→2022 | 31,78 € → 32,93 € (+4 %) | non isolé | — |
| InfoCC état des lieux 2025 (donnée 2024) | 2024 | ~31 €/tCO2e (LBC ≈ moitié de la valeur totale du marché malgré volumes limités) ; marché volontaire France tous standards confondus : 9,9 €/tCO2e moyen | non isolé dans l'extrait obtenu | — |
| I4CE (via Les Echos, 2025) | bilan au 31.03.2025 | 35 €/tCO2e moyen LBC (vs 8 €/t à l'international tous standards) | **20 à 70 €/tCO2e pour la forêt** (vs 40-60 €/t pour l'agriculture) | 20-70 €/t |
| Ministère (page officielle "Financer un projet") | — | 35 €/tCO2e moyen, tous secteurs LBC confondus | non isolé | **8 à 125 €/tCO2e** |

**Synthèse [À VÉRIFIER MANUELLEMENT pour affinage]** : le prix moyen d'un crédit carbone LBC
**forestier** se situe dans une fourchette large de **20 à 70 €/tonne de CO2**, avec une moyenne
toutes méthodes LBC confondues (forêt + agriculture + autres) autour de **31-35 €/tCO2e** en
2024-2025 (sources concordantes : InfoCC 2025, I4CE 2025, Ministère). C'est **3 à 4 fois plus
élevé** que les standards internationaux type Verra/Gold Standard (5-8 €/t en moyenne), la
robustesse méthodologique et la localisation France étant valorisées par les acheteurs.
Le ministère insiste explicitement : **il n'existe pas de prix de marché "coté"** — chaque
transaction se négocie de gré à gré selon coût réel du projet, co-bénéfices, localisation, part
d'autofinancement du porteur de projet et volonté de payer du financeur. Aucune donnée LBC ne
distingue précisément Boisement vs Balivage vs GFSC vs Reconstitution dans les sources
consultées — **[À VÉRIFIER MANUELLEMENT]** si une ventilation existe dans le registre officiel
(https://registre.label-bas-carbone.ecologie.gouv.fr/).

### 2.4. Registres et plateformes de commercialisation

| Acteur | Nature | Rôle | Source |
|---|---|---|---|
| **Registre officiel LBC** | officiel, public, gratuit | Liste tous les projets labellisés, porteurs, mandataires, financeurs ; les réductions d'émissions **ne sont ni transférables ni échangeables** une fois inscrites (traçabilité, pas de double-comptage) | https://registre.label-bas-carbone.ecologie.gouv.fr/ |
| **EcoTree** | commerciale (plateforme B2B) | Développe des projets LBC (Boisement, Restauration de peuplements dégradés), gère la paperasse LBC pour le compte des financeurs, propose des ORE (Obligations Réelles Environnementales) en complément pour renforcer la permanence | https://ecotree.green |
| **Sylv'O2** (coopérative issue de CFBL, Forêt d'Ici, Unisylva) | commerciale (coopérative forestière) | Concepteur de projet, intermédiaire financier, mandataire de certification ; propose Reboisement, Boisement, **Balivage** LBC ; couverture ~2/3 du territoire français | https://www.sylvo2.fr |
| **Fransylva Services** | professionnelle (service du syndicat de propriétaires forestiers Fransylva) | Accompagnement propriétaires : étude d'éligibilité, ingénierie carbone, labellisation DREAL, recherche de financeurs ; publie un bulletin de veille semestriel carbone pour France Bois Forêt depuis 2023 — réservé aux adhérents Fransylva | https://www.fransylvaservices.fr |
| **France Carbon Agri (FCAA)** | coopérative agricole | Périmètre **agricole** (élevage, grandes cultures), pas directement forestier — mentionné par l'utilisateur mais **hors périmètre forêt** ; à ne pas confondre avec les acteurs forestiers ci-dessus | https://www.france-carbon-agri.fr |

**Rôle systématique des intermédiaires** : dans la quasi-totalité des cas, le propriétaire
forestier (porteur de projet) ne vend pas directement ses crédits ; il passe par un **mandataire
de certification** (coopérative forestière, société type EcoTree) qui prend une commission,
réduisant la part reversée au propriétaire (mécanisme documenté par ResoilAg, non chiffré
précisément dans les sources trouvées — **[À VÉRIFIER MANUELLEMENT]**).

### 2.5. ADEME Base Carbone® — facteurs forestiers

La **Base Carbone®** est la base de données officielle de facteurs d'émission de l'ADEME
(distincte de son outil grand public "Impact CO2"), utilisée pour les Bilans GES réglementaires
(BEGES) et volontaires des entreprises. Elle contient une catégorie **"Bois et articles en bois"**
(https://prod-basecarbonesolo.ademe-dri.fr/documentation/UPLOAD_DOC_FR/bois_et_articles_en_bois.htm)
avec des facteurs d'émission liés à la **transformation et à l'usage** du bois (énergie, matériaux,
fin de vie), **pas** des facteurs de séquestration par essence/pratique sylvicole en forêt sur
pied — cette dernière relève de la méthodologie IGN/CARBOFOR déjà documentée en
`03_coefficients_forme_biomasse.md`. **[À VÉRIFIER MANUELLEMENT]** : l'accès au détail précis des
facteurs "Bois" nécessiterait de consulter le jeu de données complet (data.gouv.fr, v17.0,
licence ouverte) et de filtrer sur la catégorie bois — non fait ici par manque de temps, mais le
jeu de données est **en accès libre, licence ouverte**, donc exploitable sans blocage juridique
si un besoin précis apparaît (ex. facteur d'émission du transport de bois, du sciage, etc., pour
un futur module "bilan carbone de la filière" plus large que la simple séquestration forestière).

## 3. Comparatif / analyse critique

- **LBC vs standards internationaux (Verra, Gold Standard, MDP)** : le LBC est jugé plus robuste
  scientifiquement (mesurabilité, vérifiabilité, co-bénéfices quantifiés) mais génère des **prix
  4 à 7 fois plus élevés** — cohérent avec un marché de niche français plutôt qu'un marché de masse.
  Pour un propriétaire forestier français, le LBC est la voie de valorisation carbone la plus
  pertinente (registre public, DGEC, pas de risque de "crédit fantôme" documenté sur Verra).
- **Boisement/Reconstitution vs Balivage/GFSC** : les deux premières méthodes créent de nouveaux
  peuplements (changement d'usage des sols), les deux dernières valorisent un **changement de
  pratique sylvicole** sur une forêt existante — c'est **Balivage** et **GFSC** qui sont les plus
  directement pertinentes pour un propriétaire forestier *déjà* équipé de GeoSylva qui vient
  d'inventorier un peuplement existant (taillis ou futaie), alors que Boisement suppose un terrain
  agricole/en friche, situation hors du cœur de cible "inventaire forestier" de l'app.
- **Incertitude méthodologique documentée** : le fait que le ministère lui-même ait dû revoir à la
  baisse le niveau de référence d'accroissement naturel en V3 (2025) montre que **les paramètres
  de calcul évoluent** et ne sont pas stabilisés scientifiquement — toute estimation "maison" dans
  GeoSylva devra être présentée comme un **ordre de grandeur indicatif**, jamais comme un montant
  contractuel ou certifiable, sous peine de créer une attente commerciale non tenable pour
  l'utilisateur.
- **Absence de prix de marché coté** : contrairement au bois d'œuvre (cf.
  `01_cubage_volume/05_normes_qualite_bois.md`, prix FBF), il n'existe **aucun indice de prix
  carbone officiel et régulier** consultable en API ou en open data pour la forêt française — la
  fourchette 20-70 €/tCO2e (I4CE) est la donnée la plus fiable trouvée mais reste une estimation
  d'expert de think tank, pas un cours de marché.

## 4. Faisabilité d'une fonctionnalité "estimation carbone" dans GeoSylva

### 4.1. Données déjà disponibles dans l'app (à confirmer par lecture du code Kotlin, non fait ici — hors périmètre recherche pure)

D'après la logique métier déjà documentée dans les autres fiches de recherche (biomasse/cubage) et
la nature d'un inventaire dendrométrique standard, GeoSylva dispose vraisemblablement déjà de :
- **Essence** (code espèce aligné `CanonicalEssences.kt` d'après `_METHODOLOGIE.md`) ;
- **Surface de la parcelle** (module cartographie / GPS) ;
- **Volume bois fort par essence** (via tarifs Schaeffer/Algan/IFN, cf.
  `01_cubage_volume/01_tarifs_schaeffer_algan.md` et `02_tarifs_ifn_emerge.md`) ;
- Potentiellement l'**âge du peuplement** ou une estimation via les tables de production
  Décourt/Pardé (`01_cubage_volume/04_tables_production_croissance.md`), avec les limites déjà
  documentées (indice de station approximatif, Hdom/Hm confondus) ;
- La **formule physique volume → carbone** `C = VIFN × DEN × FEB × FER × CAR` est déjà identifiée
  et sourcée (§2.3 du doc biomasse) — c'est la brique de calcul manquante la plus simple à
  implémenter, puisque son seul intrant technique est le volume déjà calculé par GeoSylva et des
  constantes par essence (DEN = infradensité, FEB/FER = facteurs d'expansion) qu'il reste à
  compiler exhaustivement par essence (actuellement seules les moyennes conifères/feuillus sont
  vérifiées, pas le détail par essence — **lacune à combler**, cf. limites du doc biomasse).

### 4.2. Données manquantes pour une estimation carbone **crédible** (au sens LBC)

- **Scénario de référence** : impossible à déterminer par un inventaire ponctuel seul (il faut
  savoir si la parcelle était boisée depuis >10 ans, son historique d'usage, son mode de gestion
  antérieur) — **donnée non capturable par un simple relevé terrain**, nécessite une déclaration
  utilisateur ou une consultation de données historiques (ex. IGN BD Forêt multi-dates, orthophotos
  successives — piste à explorer dans `05_apis_externes/` si pertinent).
  - Note factuelle : la comptabilisation carbone se fait par différence, ce n'est **jamais** le
    volume/carbone absolu d'une parcelle qui donne un nombre de crédits, contrairement à ce qu'un
    utilisateur pourrait intuitivement attendre d'une "estimation carbone" simple.
- **Classe de fertilité et table de production adaptée** : requise explicitement pour un dossier
  Balivage (document 3 et 4 listés en §2.1) — GeoSylva a une approximation de l'indice de station
  mais celle-ci est documentée comme imprécise (âge ignoré) dans
  `04_tables_production_croissance.md`.
- **Durée de projet / trajectoire à 30 ans** : un inventaire fournit un instantané ; le LBC raisonne
  sur une trajectoire de 30 ans avec vérifications à échéances régulières — GeoSylva n'a pas
  aujourd'hui de module de simulation de croissance sur cet horizon (les tables Décourt/Pardé
  donnent des points statiques par classe d'âge, pas une simulation dynamique type CAPSIS/Fagacées,
  déjà signalé comme absent en open-source mobile dans `04_tables_production_croissance.md`).
- **Rabais réglementaires** (10 % général, jusqu'à 40 % sans BASI, etc.) : logique administrative
  du LBC, pas une donnée dendrométrique — nécessiterait juste une implémentation de règles, mais le
  détail exact des rabais n'a pas pu être extrait intégralement dans cette recherche (cf. §2.1,
  **[À VÉRIFIER MANUELLEMENT]** sur le PDF officiel de la méthode).
- **Prix €/tCO2e à appliquer** : aucune valeur de marché "officielle" unique n'existe (négociation
  gré à gré) — toute estimation monétaire de GeoSylva devrait afficher une **fourchette** (ex.
  20-70 €/t) et non un chiffre unique, avec un avertissement explicite de non-contractualité.

### 4.3. Recommandation de faisabilité

Une fonctionnalité **"ordre de grandeur du potentiel carbone"** (non contractuelle, non
certifiante) est **réalisable à court terme** avec les données déjà présentes dans l'app :
essence + volume bois fort + surface → tonnes de carbone/CO2 stockées sur pied (formule CARBOFOR),
puis affichage d'une fourchette de valorisation potentielle en euros (20-70 €/tCO2e) **si** un
projet LBC de type Balivage ou GFSC était monté sur la parcelle — avec un message clair indiquant
que ceci est une estimation indicative, que la comptabilisation réelle nécessite un différentiel
par rapport à un scénario de référence, une classe de fertilité vérifiée, et l'accompagnement d'un
mandataire agréé (CNPF, coopérative type Sylv'O2, ou Fransylva Services pour les adhérents).

Une fonctionnalité **"dossier LBC exportable"** (générant les documents 2A/3/4/5/8 requis pour un
dépôt Balivage) est **non réalisable à court terme** : elle nécessiterait un module de simulation
de croissance à 30 ans et une gestion administrative des rabais/co-bénéfices qui dépasse le
périmètre dendrométrique actuel de l'app — à envisager seulement en version avancée, après
consolidation des tables de production et de l'indice de station (cf. limites déjà notées dans
`01_cubage_volume/04_tables_production_croissance.md`).

## 5. Recommandation pour GeoSylva

- **Priorité moyenne, effort faible** : implémenter le calcul physique volume → CO2 par essence
  (formule CARBOFOR déjà sourcée) comme **indicateur informatif** dans la fiche parcelle, avec un
  simple facteur de conversion carbone → CO2 (masse molaire CO2/C ≈ 3.67) — donnée manquante à
  ajouter dans le futur module carbone : `C(kg) × 3.67 = CO2(kg)` — **[À VÉRIFIER MANUELLEMENT]**
  ce facteur est un fait de chimie standard, pas une donnée métier forestière, mais à citer
  explicitement dans le code/doc utilisateur.
- **Priorité moyenne, effort faible** : afficher, à côté de cet indicateur, un lien/texte
  pédagogique renvoyant vers le LBC officiel et les 4 méthodes forestières (Boisement,
  Reconstitution, Balivage, GFSC), sans promettre de chiffre de valorisation garanti — rôle
  d'orientation, pas de calcul financier engageant.
- **Priorité basse, effort élevé** : ne pas construire de simulateur de rabais/VAN LBC complet en
  l'état — dépendrait d'un module de croissance dynamique à 30 ans non disponible en open-source
  mobile (constat déjà fait dans `04_tables_production_croissance.md`).
- **Fichiers Kotlin concernés (à vérifier par lecture directe, non fait dans cette recherche)** :
  probablement le même calculateur que celui gérant biomasse/volume (`EnhancedForestryCalculator.kt`
  ou équivalent mentionné dans les autres fiches) — ajouter une fonction dédiée
  `computeCarbonEstimate()` plutôt que de mélanger avec le calcul de conformité ONF existant, pour
  respecter la séparation des responsabilités déjà recommandée dans les fiches précédentes.
- **Ajouter une entrée** dans `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (absente à ce jour) listant
  les méthodes LBC forestières et le registre officiel comme référentiel externe de valorisation,
  au même titre que les autres référentiels déjà cités (Schaeffer/Algan, IFN, NF EN 1316/1927).

## 6. Limites et points à vérifier manuellement

1. Le détail exact des **rabais** de la méthode Boisement (10 % général, 40 % sans BASI, 0-15 %
   variable) n'a pas pu être extrait intégralement — la page source a été tronquée pendant la
   recherche ; consulter directement le PDF officiel
   (label-bas-carbone.ecologie.gouv.fr/sites/default/files/2025-07/Méthode%20boisement%20V2.pdf)
   avant toute implémentation.
2. Aucune ventilation officielle des prix par méthode (Boisement vs Balivage vs GFSC vs
   Reconstitution) n'a été trouvée — la fourchette 20-70 €/tCO2e citée par I4CE est globale "forêt".
   Vérifier si le registre public LBC (registre.label-bas-carbone.ecologie.gouv.fr) permet un
   filtrage par méthode et par prix déclaré, projet par projet.
3. Les facteurs précis de la Base Carbone ADEME catégorie "Bois et articles en bois" n'ont pas été
   extraits en détail (accès au jeu de données complet non consulté ligne par ligne) — à faire si
   un besoin de facteurs d'émission de transformation/transport du bois se précise (hors
   séquestration sur pied, qui relève déjà de CARBOFOR/IGN).
4. La commission moyenne prélevée par les mandataires/intermédiaires (coopératives, EcoTree) sur le
   prix final reversé au propriétaire forestier n'a pas été chiffrée précisément dans les sources
   consultées (mentionnée qualitativement seulement par ResoilAg).
5. Aucune vérification terrain ni test d'API n'a été réalisé dans cette recherche (recherche
   documentaire pure) — contrairement à d'autres fiches de la vague 1 (ex. test EMERGE), il n'existe
   pas d'API publique du registre LBC identifiée à tester ; à explorer si besoin dans
   `05_apis_externes/`.
6. Le facteur de conversion carbone → CO2 (×3.67) cité en §5 est un fait chimique standard
   (masse molaire CO2 = 44 g/mol, carbone = 12 g/mol, ratio 44/12 ≈ 3.67) reformulé par l'agent —
   marqué `[À VÉRIFIER MANUELLEMENT]` par prudence méthodologique bien que ce ne soit pas une donnée
   forestière contestable.
