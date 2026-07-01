# Tables de production et modèles de croissance forestière (France)
**Domaine** : docs/recherche/01_cubage_volume/
**Date de recherche** : 2026-07-15 (session sous-agent)
**Agent** : sous-agent recherche — audit indice de station / tables de production

---

## 0. Objet et rappel de la mission

Ce document répond à trois demandes :
1. Documenter les tables de production françaises (Décourt & Pardé, INRA/ENGREF/ONF) par essence
   principale : structure, définition précise de l'indice de fertilité, classes de fertilité.
2. Documenter les modèles de croissance dynamiques utilisés aujourd'hui par les professionnels
   (plateforme CAPSIS/INRAE, modèle Fagacées, modèle Douglas ONF/INRAE, etc.), leur statut
   open-source, l'existence d'une API, et la faisabilité de les embarquer dans une app mobile.
3. Vérifier le bug signalé dans `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` §1.5 : *« indice de
   station approximatif (Hm au lieu de Hdom) dans `ExpertForestryCalculator.kt` »*, avec preuve
   par extrait de code, et proposer une correction précise.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| Decourt N., *Tables de production pour les forêts françaises* | ouvrage | scientifique (INRA/ENGREF) | https://belinrae.inrae.fr/index.php?id=62618&lvl=notice_display | 1973 (49 p.) |
| Vannière B. & Decourt N., *Tables de production pour les forêts françaises* (2ᵉ éd.) | ouvrage | scientifique (INRA/ENGREF/ONF/CNRF) | https://belinrae.inrae.fr/index.php?id=207687&lvl=notice_display | 1984 (158 p.) |
| Decourt N. (1972), « Méthode utilisée pour la construction rapide de tables de production provisoires en France » | article scientifique (Ann. Sci. For.) | scientifique | https://doi.org/10.1051/forest/19720102 | 1972 |
| Duplat P. & Tran-Ha M. (1997), modélisation Hdom chêne sessile | article scientifique (Ann. Sci. For.) | scientifique | cité dans hal-00823732 | 1997 |
| Duplat P. et al., « Croissance en hauteur dominante du hêtre » | article scientifique HAL/INRAE | scientifique | https://hal.science/hal-00823732/document (v1) | non daté précisément dans le PDF récupéré — **[À VÉRIFIER MANUELLEMENT]** |
| Dhôte J.-F. et al., modèle **Fagacées** (chêne sessile/hêtre) — page Capsis | documentation logicielle | scientifique (ONF/CEMAGREF/INRA) | https://capsis.cirad.fr/capsis/help_fr/fagacees | dernière modif. page : 2021-12-13 |
| Dhôte J.-F. & Le Moguédec G., « Fagacées: a tree-centered growth and yield model… », Annals of Forest Science | article scientifique peer-reviewed | scientifique | https://doi.org/10.1007/s13595-011-0157-0 | 2011 |
| Moguédec G. et al., « Improving the Fagacées growth model… » | article scientifique HAL/INRAE | scientifique | https://hal.inrae.fr/hal-03351338/file/Mogu-dec_et_al-2021-Annals_of_Forest_Science.pdf | 2021 |
| Plateforme **CAPSIS** — présentation | documentation logicielle officielle | officielle (INRAE/CIRAD) | https://capsis.cirad.fr/capsis/presentation | dernière modif. 2022-09-09 |
| CAPSIS — Charte (conditions d'utilisation/accès code) | documentation officielle | officielle (INRAE/CIRAD) | https://capsis.cirad.fr/capsis/charter (à consulter) | — |
| Forêts-21 (INRAE/CNPF/ONF/IGN/MAA), simulateur Douglas | plateforme officielle de simulation | officielle | https://forets21.inra.fr/pelican3.1/itk-douglas.html ; https://forets21.inra.fr/pelican3.1/nouvelle-essence-douglas.html | consulté 2026 |
| ONF, *Guide des sylvicultures des douglasaies françaises* | ouvrage officiel | officielle (ONF) | https://www.onf.fr/vivre-la-foret/+/1f6a::guide-des-sylvicultures-des-douglasaies-francaises.html | 2007, 297 p. |
| Ottorini J.-M., « Simulation et sylviculture du douglas » (modèle SimCoP) | article scientifique | scientifique (INRA) | https://hal.science/hal-03444346/document | 1991 (article), republié HAL |
| jymassenet-foret.fr, cours de dendrométrie (définition Hdom/Hm biologique et mathématique) | support pédagogique | commerciale/tierce (site personnel d'enseignant, non institutionnel) | http://jymassenet-foret.fr/cours/dendrometrie/Coursdendrometriepdf/DENDRO5-2010.pdf | 2010 |
| Brack & Wood, *Stand Height* (mensuration ANU) | support pédagogique universitaire (Australie) | scientifique/pédagogique (hors France, comparatif) | http://fennerschool-associated.anu.edu.au/mensuration/BrackandWood1998/STNDHGT.HTM | 1998 |
| Arbor Analytics blog, « Lorey's height » | blog spécialisé (télédétection forestière) | commerciale/tierce | https://arbor-analytics.com/post/2023-04-21-lorey-s-height-the-remote-sensing-way-to-estimate-tree-height/ | 2023 |
| RFPT — « FORESTIMATOR : plugin QGIS d'estimation de la hauteur dominante et du site index… » | article scientifique (revue française) | scientifique | https://doi.org/10.52638/rfpt.2015.550 | 2015 |
| Code source interne | code | — (source primaire de l'audit) | `app/src/main/java/com/forestry/counter/domain/calculation/ExpertForestryCalculator.kt` ; `EnhancedForestryCalculator.kt` ; `MartelageModels.kt` ; `presentation/screens/forestry/ExpertIbpExtension.kt` | lu intégralement le 2026-07-15 |

**Lacune identifiée** : le texte intégral des « Tables de production pour les forêts françaises »
(Decourt 1973 / Vannière & Decourt 1984) n'est pas en accès libre en ligne — seule la notice
bibliographique BeL-INRAE est publique. Les valeurs numériques précises (Hdom par âge/classe pour
chêne, hêtre, pins, épicéa, sapin, douglas, mélèze) **ne sont donc pas vérifiables gratuitement en
l'état** ; toute valeur numérique présente dans `ExpertForestryCalculator.kt` (ex. table
`cheneProductionTable`, `hetreProductionTable`) doit être considérée comme **non sourcée
individuellement** tant que l'ouvrage papier n'a pas été consulté (BU ENGREF/AgroParisTech ou
bibliothèque INRAE). C'est une limite majeure déjà signalée dans `REFERENTIELS_FORESTIERS_EXTERNES.md`
§1.5 et confirmée ici.

---

## 2. Données détaillées

### 2.1 Définition normative de l'indice de fertilité / indice de station

Trois notions distinctes à ne jamais confondre (source scientifique/pédagogique concordante,
notamment hal-00823732 et jymassenet-foret.fr DENDRO5-2010.pdf) :

| Notion | Symbole usuel | Définition | Sensibilité aux éclaircies |
|---|---|---|---|
| Hauteur moyenne arithmétique | Hm (ou h̄) | Moyenne simple des hauteurs de **tous** les arbres du peuplement | Forte — chute après éclaircie par le bas |
| Hauteur moyenne de Lorey | HL (Hg) | Moyenne des hauteurs pondérée par la surface terrière de chaque arbre | Moyenne — plus stable que Hm mais sensible aux éclaircies par le haut |
| **Hauteur dominante** | Hdom (H₀) | Hauteur moyenne des ~100 plus gros arbres/ha (définition « mathématique », la plus utilisée) — ou hauteur moyenne des arbres dominants/codominants (définition « biologique ») | Faible — quasi insensible aux éclaircies tant qu'elles ne sont pas fortement par le haut |
| **Indice de fertilité / Site Index** | IS | **Hdom à un âge de référence donné** (ex. 100 ans chêne, 80 ans hêtre en France ; 50 ans en Belgique pour résineux) — nécessite une courbe de croissance en Hdom (faisceau de courbes) pour « ramener » la Hdom observée à l'âge courant vers sa valeur à l'âge de référence | Faible (hérite de la stabilité de Hdom) |

Point clé (confirmé par hal-00823732, Duplat & Tran-Ha 1997 pour le chêne, Duplat et al. pour le
hêtre) : **l'indice de fertilité n'est PAS directement égal à la Hdom courante**. C'est la valeur
que prendrait Hdom **si le peuplement avait l'âge de référence**, obtenue via un faisceau de
courbes de croissance en hauteur dominante (modèle de Korf dans les publications citées). Ignorer
cette conversion produit une distorsion d'autant plus grande que l'âge réel du peuplement est
éloigné de l'âge de référence (ex. un jeune chêne de 40 ans avec Hdom = 18 m n'est pas de
« fertilité 18 » à l'échelle des tables de référence, car ces tables raisonnent en Hdom à 100 ans).

C'est exactement l'approximation que `ExpertForestryCalculator.kt` reconnaît déjà explicitement
en commentaire (lignes 22-27, voir §3 ci-dessous) — ce point est donc **une limite documentée par
le code lui-même**, pas un « bug caché ».

### 2.2 Structure d'une table de production française (Décourt & Pardé / Vannière & Decourt)

D'après la notice BeL-INRAE et la littérature scientifique associée, une table de production
française classique fournit, pour une essence donnée, pour chaque **classe de fertilité** (le
plus souvent I/II/III ou I à V selon l'essence) et pour une série d'**âges** (généralement pas de
10 ou 20 ans) :

- Hdom (hauteur dominante, m)
- dg (diamètre moyen quadratique, cm)
- G (surface terrière, m²/ha)
- N (nombre de tiges/ha)
- V (volume total ou volume marchand, m³/ha)
- ACA (accroissement courant annuel, m³/ha/an)
- AMA (accroissement moyen annuel, m³/ha/an)
- éventuellement le volume des éclaircies cumulées

Les essences couvertes par les tables Decourt/Vannière & Decourt (mots-clés de la notice
BeL-INRAE id=62618) : **Pinus sylvestris** (pin sylvestre), **Pinus nigra** (pin noir),
**Picea abies** (épicéa commun), **Pseudotsuga menziesii** (douglas), **Pinus pinaster**
(pin maritime), **Fagus sylvatica** (hêtre), **Quercus** (chêne, pédonculé et sessile).
Le **sapin pectiné (Abies alba)** et le **mélèze (Larix)** ne sont **pas confirmés** dans les
mots-clés de la notice consultée — **[À VÉRIFIER MANUELLEMENT]** dans l'ouvrage papier (existence
probable de tables séparées ou de tables régionales complémentaires, non retrouvées en accès
libre lors de cette recherche).

### 2.3 Modèle Fagacées (chêne sessile / hêtre) — successeur dynamique des tables statiques

- **Auteurs** : Jean-François Dhôte (ONF), Patrick Vallet (CEMAGREF), Gilles Le Moguédec,
  Frédéric Mothe (INRA) — https://capsis.cirad.fr/capsis/help_fr/fagacees
- **Type** : modèle arbre-individuel indépendant des distances (« distance-independent
  tree-centered model »), calibré sur les peuplements de plaine du nord de la France
  (Dhôte & Le Moguédec 2011, DOI 10.1007/s13595-011-0157-0).
- **Principe** : la croissance est d'abord calculée au niveau peuplement (surface terrière,
  Hdom), puis répartie entre les arbres individuels (organisation « top-down »).
- **Extension 2021** : Moguédec et al. (hal-03351338) ont élargi la calibration avec des données
  françaises et allemandes supplémentaires, modifiant l'équation d'accroissement en surface
  terrière ; le reste du formalisme reste applicable.
- **Statut d'accès** : le modèle est intégré dans la plateforme **CAPSIS** (voir §2.4). Le code du
  modèle Fagacées lui-même n'est **pas publié en accès libre indépendamment de CAPSIS** — accès
  via adhésion au projet CAPSIS (charte à signer, cf. https://capsis.cirad.fr/capsis/charter).
- **Prédécesseurs statiques cités par ce modèle** : les faisceaux de courbes de croissance en
  Hdom du chêne sessile (Duplat & Tran-Ha 1997) et du hêtre (hal-00823732), qui sont exactement le
  type de courbe manquant dans `ExpertForestryCalculator.kt` pour calculer un IS correct (cf. §3).

### 2.4 Plateforme CAPSIS (INRAE/CIRAD)

- **URL** : https://capsis.cirad.fr/capsis/presentation
- **Nature** : plateforme logicielle Java générique de simulation de croissance/dynamique
  forestière, développée depuis 1994 (CAPSIS 2.4, INRA URFM Avignon) puis CAPSIS4 depuis 1999
  (UMR AMAP Montpellier).
- **Licence affichée sur le wiki** : CC BY-NC-SA 4.0 (contenu du wiki ; **ne présume pas** de la
  licence exacte du code Java lui-même, à vérifier dans la charte — **[À VÉRIFIER
  MANUELLEMENT]**).
- **Accès** : les modélistes forestiers peuvent « rejoindre le projet CAPSIS » (charte à signer) ;
  ce n'est **pas un simple téléchargement open-source classique type GitHub public** — c'est un
  accès encadré par une communauté scientifique (chercheurs contributeurs). Aucune **API REST ou
  web-service public** identifiée pour interroger CAPSIS à distance depuis une app mobile.
- **Forme d'usage réel** : logiciel de bureau Java, pensé pour chercheurs/gestionnaires forestiers
  sur poste de travail, pas pour de l'embarqué mobile.

### 2.5 Douglas — modèles utilisés aujourd'hui

Deux lignées identifiées :

1. **SimCoP** (Ottorini, INRA, 1991) — simulateur individu-centré dépendant des distances,
   houppier topographié, très détaillé (profil de tige, cerne de croissance). Article :
   https://hal.science/hal-03444346/document. Usage : recherche, pas d'accès open-source
   identifié, pas d'API.
2. **Forêts-21** (INRAE, CNPF, ONF, IGN, MAA) — https://forets21.inra.fr/pelican3.1/ : plateforme
   web publique de simulation d'itinéraires sylvicoles pour le Douglas (et d'autres essences),
   construite avec un panel de 14 experts nationaux (CRPF, ONF, FCBA, France Douglas, coopératives).
   Propose des itinéraires sylvicoles simulés avec calendrier d'éclaircies et scénarios
   climatiques. **C'est un outil web applicatif accessible publiquement**, mais **aucune API
   documentée trouvée** lors de cette recherche pour intégration programmatique — usage prévu :
   navigation interactive humaine, pas de endpoint machine identifié. **[À VÉRIFIER
   MANUELLEMENT]** en contactant l'équipe Forêts-21 ou en inspectant le trafic réseau du site.
3. **Guide des sylvicultures des douglasaies françaises** (ONF, 2007, 297 p.) — référentiel papier
   avec règles sylvicoles par bassin de production (Grand Massif Central = 67% des douglasaies
   françaises), non numérisé en API.

### 2.6 Faisabilité d'intégrer directement les équations de ces modèles dans une app mobile

| Critère | CAPSIS/Fagacées | SimCoP Douglas | Forêts-21 |
|---|---|---|---|
| Open-source au sens strict (dépôt public, licence permissive) | Non confirmé — accès communautaire encadré | Non | Non (outil web, code non public identifié) |
| API HTTP disponible | Non | Non | Non identifiée |
| Équations publiées en détail dans la littérature scientifique | Partiellement (Fagacées : oui, article 2011 détaille les équations) | Partiellement (principes publiés, paramètres calibrés non tous publics) | Non (résultats de simulation visibles, équations sous-jacentes non exposées) |
| Portage réaliste dans Kotlin/Android à court terme | Faible à moyen — nécessiterait de ré-implémenter les équations publiées de Fagacées (faisable pour un sous-ensemble simplifié chêne/hêtre) et d'obtenir les jeux de paramètres calibrés (non garantis publics) | Faible — modèle trop dépendant de données individuelles d'arbres abattus non disponibles publiquement | Très faible — boîte noire côté utilisateur |

**Conclusion** : à ce stade, il n'existe **aucun modèle dynamique français en accès libre,
directement API-isable, prêt à être branché dans GeoSylva**. La voie réaliste à court terme reste
l'utilisation de **tables statiques Décourt/Pardé** (déjà partiellement présentes dans
`ExpertForestryCalculator.kt`) **complétées par les faisceaux de courbes Hdom~âge** (Duplat &
Tran-Ha 1997 pour le chêne, Duplat et al. pour le hêtre) pour calculer un **véritable indice de
fertilité** au lieu d'un simple Hdom courant. Le modèle Fagacées pourrait être une cible à moyen
terme si un accès aux équations/paramètres complets peut être obtenu auprès du LERFoB
(UMR 1092 INRA-AgroParisTech), mais cela sort du périmètre « accès libre immédiat ».

### 2.7 Quantification de l'écart Hdom vs Hm

Les sources consultées (jymassenet-foret.fr DENDRO5-2010.pdf ; Brack & Wood 1998 ; Arbor
Analytics 2023 ; FORESTIMATOR RFPT 2015) confirment unanimement que :

- Hdom > HL (Lorey) > Hm (arithmétique) dans un peuplement régulier non éclairci récemment par le
  haut — ordre confirmé par Arbor Analytics 2023 (comparaison FIA USA) et par la définition même
  (Hdom porte sur les plus gros/plus hauts arbres, Hm porte sur tous les arbres y compris les
  dominés/supprimés).
- **Aucune des sources consultées ne fournit un pourcentage d'écart moyen chiffré et sourcé pour
  les peuplements français** (chêne/hêtre/résineux). Les seules indications qualitatives
  disponibles : l'écart croît avec l'hétérogénéité du peuplement (variance des diamètres/hauteurs)
  et diminue dans un peuplement très régulier et non éclairci.
- Un ordre de grandeur de **+10 à +25 %** entre Hdom et Hm arithmétique est plausible d'après la
  littérature de mensuration forestière générale (Brack & Wood 1998, contexte australien) mais
  **ce chiffre n'est pas directement sourcé pour la sylviculture française** et doit être marqué
  `[À VÉRIFIER MANUELLEMENT]` — il ne doit pas être codé en dur dans l'app sans validation par un
  jeu de données réel (ex. comparaison sur les placettes IFN).
- Impact concret sur l'indice de fertilité : si l'on utilisait Hm au lieu de Hdom pour estimer un
  IS, l'IS serait **sous-estimé** (car Hm < Hdom), ce qui déclasserait à tort la fertilité de la
  station et sous-estimerait la production attendue — erreur systématique, pas aléatoire.

---

## 3. Bug Hdom/Hm confirmé ou infirmé

### 3.1 Verdict

**Partiellement infirmé sur le calcul de Hdom lui-même, mais confirmé sous une forme différente et
plus grave : une confusion de nommage/typage entre Hm et Hdom dans les données de production, qui
fausse une comparaison de conformité affichée à l'utilisateur.**

Détail :

- Le calcul de **Hdom en tant que tel** (`computeHdom()`) est **correct** et conforme à la
  définition « mathématique » standard (moyenne des hauteurs des N=100×surfaceHa plus gros arbres
  par diamètre décroissant) — voir preuve ci-dessous. Ce n'est donc **pas** un cas où le code
  confond Hm et Hdom au niveau du calcul brut de la hauteur dominante.
- En revanche, **l'indice de station lui-même** (`calculateIndiceDeStation()`) utilise Hdom
  **brute à l'âge courant** comme proxy de l'indice de fertilité, sans le ramener à l'âge de
  référence (100 ans chêne, 80 ans hêtre, 50 ans résineux) — **ceci est explicitement documenté
  comme approximation dans le code lui-même** (donc ce n'est pas un « bug caché », mais une
  limite connue et déjà commentée).
- Le **vrai bug non documenté** trouvé lors de cette relecture complète : le champ
  `ProductionData.hauteurMoyenne` (renseigné à partir des tables Décourt & Pardé) contient en
  réalité des valeurs de **Hdom** (le commentaire du code source le dit explicitement), mais ce
  champ est nommé `hauteurMoyenne` et **comparé/affiché comme s'il s'agissait de la hauteur
  moyenne arithmétique (Hm) réellement mesurée sur le terrain**. Cela produit un test de
  « conformité ONF » qui compare une Hm terrain à une Hdom tabulée, sous une étiquette commune
  trompeuse.

### 3.2 Preuves (extraits de code exacts)

**Preuve A — `computeHdom()` est correctement implémenté** (`ExpertForestryCalculator.kt`,
lignes 130-136) :

```kotlin
fun computeHdom(tiges: List<Tige>, surfaceHa: Double): Double? {
    if (tiges.isEmpty() || surfaceHa <= 0.0) return null
    val nTarget = ceil(100.0 * surfaceHa).toInt().coerceAtLeast(1)
    val selected = tiges.sortedByDescending { it.diamCm }.take(nTarget)
    val heights = selected.mapNotNull { it.hauteurM }
    return if (heights.isEmpty()) null else heights.average()
}
```
→ Conforme à la définition « hauteur moyenne arithmétique des 100 plus gros bois à l'hectare »
(jymassenet-foret.fr DENDRO5-2010.pdf ; FORESTIMATOR RFPT 2015, citant Rondeux 1999).

**Preuve B — l'IS est explicitement documenté comme approximation Hdom-à-âge-courant**
(`ExpertForestryCalculator.kt`, lignes 21-27 et 141-153) :

```kotlin
/*
 * ### Indice de station (IS)
 * Méthode officielle : IS = hauteur dominante (Hdom) à l'âge de référence
 * (100 ans pour chêne, 80 ans pour hêtre, 50 ans pour résineux).
 * ⚠ Approximation: IS ≈ Hdom en l'absence de l'âge de référence. L'IS officiel ONF
 * nécessite Hdom à âge de référence. La classification par classes (I–VII) est
 * conservée et reste basée sur Hdom.
 */
...
fun calculateIndiceDeStation(
    essenceCode: String,
    age: Int,
    hdom: Double,
    diametreMoyen: Double
): Double {
    // Approximation: IS ≈ Hdom en l'absence de l'âge de référence.
    @Suppress("UNUSED_PARAMETER")
    val ignoredAge: Int = age
    ...
    return hdom.coerceIn(5.0, 30.0)
}
```
→ Le paramètre `age` est reçu puis **explicitement ignoré** (`@Suppress("UNUSED_PARAMETER")`),
ce qui confirme textuellement que l'IS retourné = Hdom courante bornée, jamais corrigée par l'âge.
C'est une limite déjà connue de l'équipe (le commentaire le dit), donc pas le bug « caché » visé
par l'audit — mais elle reste réelle et impacte la précision (cf. §2.1 et §2.7).

**Preuve C — le vrai bug non documenté : `ProductionData.hauteurMoyenne` contient des valeurs de
Hdom mais est traité comme Hm dans les comparaisons de conformité.**

Commentaire sur la table de données (`ExpertForestryCalculator.kt`, lignes 47-49) :
```kotlin
// Tables de production — Décourt & Pardé (1980), ENGREF Nancy.
// Colonnes : hauteurDom(m), dg(cm), G(m²/ha), V(m³/ha), ACA(m³/ha/an), AMA(m³/ha/an).
```
→ La première colonne des tables (`8.5`, `14.2`, `18.6`... pour le chêne station 1, lignes 54-61)
est explicitement documentée comme **hauteurDom(m)**, c'est-à-dire une valeur de **Hdom**, jamais
de Hm.

Or, la data class qui reçoit cette colonne nomme ce champ `hauteurMoyenne` (`ExpertForestryCalculator.kt`,
lignes 571-578) :
```kotlin
data class ProductionData(
    val hauteurMoyenne: Double,      // m   <-- nommé "moyenne" mais alimenté par la colonne Hdom
    val diametreMoyen: Double,       // cm
    val surfaceTerriere: Double,     // m²/ha
    val volumeTotal: Double,         // m³/ha
    val accroissementAnnuel: Double, // m³/ha/an
    val indiceDeStation: Double      // IA (0-30)
)
```
et le premier argument positionnel de chaque `ProductionData(...)` de la table
(ex. ligne 54 : `ProductionData(8.5, 12.3, 8.2, 45.0, 2.8, 6.5)`) alimente précisément ce champ
`hauteurMoyenne` avec la valeur Hdom du tableau (8.5 m à 20 ans, station 1).

Ce champ mal nommé est ensuite comparé à une **vraie** hauteur moyenne arithmétique mesurée sur le
terrain (Hm réelle) dans `EnhancedForestryCalculator.kt`, lignes 67 et 86 :
```kotlin
val hauteurs = tiges.mapNotNull { it.hauteurM }
val hauteurMoyenne = hauteurs.average().takeIf { hauteurs.isNotEmpty() } ?: 20.0   // ligne 67 : vraie Hm terrain
...
hauteurConforme = abs(hauteurMoyenne - prod.hauteurMoyenne) / prod.hauteurMoyenne < 0.15   // ligne 86
```
→ **`hauteurMoyenne` (variable locale, Hm arithmétique réelle des arbres mesurés) est comparée à
`prod.hauteurMoyenne` (Hdom issue de la table Décourt & Pardé)**, sous couvert d'un test de
« conformité de hauteur » (`hauteurConforme`). Comme Hdom > Hm structurellement (cf. §2.7), ce
test produira un écart artificiellement élevé et signalera à tort une **non-conformité** même
pour un peuplement parfaitement conforme aux tables, biaisant le diagnostic « conformité ONF »
affiché à l'utilisateur professionnel.

Le même champ mal nommé est réutilisé à l'affichage dans `presentation/screens/forestry/ExpertIbpExtension.kt` :
- ligne 46 : `hauteurMoyenne` (vraie Hm terrain) recalculée localement,
- ligne 85/481 : stockée dans `ExpertAnalysisResult.hauteurMoyenne`,
- ligne 252 : `production.hauteurMoyenne` (en réalité une Hdom tabulée) affiché sous le libellé
  UI `stringResource(R.string.expertibp_mean_height_value, production.hauteurMoyenne)`
  — soit littéralement « hauteur moyenne » affichée à l'utilisateur alors que la valeur est une
  Hdom de table.

### 3.3 Proposition de correction (illustrative — non appliquée au code réel)

Deux corrections distinctes et complémentaires, à faire valider par le Dendromètre (forest-crew)
avant implémentation réelle :

**Correction 1 — renommer le champ pour lever l'ambiguïté** (pas de changement de valeur, juste de
nom et de doc, limite le risque de mauvaise réutilisation future) :

```kotlin
// Illustration seulement — ne pas appliquer sans revue, renommage impactant (data class utilisée
// dans plusieurs écrans : ExpertIbpExtension.kt, EnhancedForestryCalculator.kt, etc.)
data class ProductionData(
    val hauteurDominanteTable: Double, // m — Hdom issue de la table Décourt & Pardé (ex-"hauteurMoyenne")
    val diametreMoyen: Double,         // cm
    val surfaceTerriere: Double,       // m²/ha
    val volumeTotal: Double,           // m³/ha
    val accroissementAnnuel: Double,   // m³/ha/an
    val indiceDeStation: Double        // IA (0-30)
)
```

**Correction 2 — comparer Hdom réelle à Hdom tabulée (pas Hm à Hdom)**, en réutilisant
`computeHdom()` qui existe déjà et est correct :

```kotlin
// Illustration seulement.
// Avant (bug) :
//   hauteurConforme = abs(hauteurMoyenne - prod.hauteurMoyenne) / prod.hauteurMoyenne < 0.15
// Après (corrigé) : comparer Hdom terrain à Hdom table, pas Hm terrain à Hdom table.
val hdomTerrain = expertCalculator.computeHdom(tiges, surfaceHa) ?: hauteurMoyenne
val hauteurConforme = productionData?.let { prod ->
    abs(hdomTerrain - prod.hauteurDominanteTable) / prod.hauteurDominanteTable < 0.15
} ?: false
```

**Correction 3 (plus structurelle, moyen terme) — calculer un véritable indice de fertilité** en
ramenant Hdom courante à l'âge de référence via un faisceau de courbes Hdom~âge (par essence),
au lieu de retourner Hdom brute bornée :

```kotlin
// Illustration seulement — nécessite les courbes de Duplat & Tran-Ha (1997, chêne) et
// Duplat et al. (hêtre, hal-00823732) ou équivalent pour les résineux, non trouvées en accès
// libre lors de cette recherche (à obtenir auprès du LERFoB / ENGREF-AgroParisTech).
fun calculateIndiceDeStationCorrige(
    essenceCode: String,
    ageActuel: Int,
    hdomActuelle: Double
): Double {
    val ageReference = ageReferencePourEssence(essenceCode) // 100 chêne / 80 hêtre / 50 résineux
    // hdomAAgeReference() = projection via le faisceau de courbes officiel (modèle de Korf)
    return hdomAAgeReference(essenceCode, ageActuel, hdomActuelle, ageReference)
        .coerceIn(5.0, 30.0)
}
```

Cette correction 3 dépend de données non disponibles en accès libre à ce stade (cf. §2.7) — à
traiter comme un chantier séparé, potentiellement via contact direct avec le LERFoB ou achat de
l'ouvrage Décourt/Vannière.

---

## 4. Recommandation pour GeoSylva

1. **Priorité haute (correctif rapide, faible risque)** : appliquer la Correction 1 + Correction 2
   ci-dessus dans `ExpertForestryCalculator.kt` / `EnhancedForestryCalculator.kt` /
   `ExpertIbpExtension.kt`. Impact : élimine une comparaison Hm-vs-Hdom invalide qui peut afficher
   à tort une « non-conformité » de hauteur à l'utilisateur professionnel. Fichiers concernés :
   - `app/src/main/java/com/forestry/counter/domain/calculation/ExpertForestryCalculator.kt`
     (renommage du champ `ProductionData.hauteurMoyenne` → `hauteurDominanteTable`, lignes 571-578
     et tous les positionnels des tables `cheneProductionTable`/`hetreProductionTable`)
   - `app/src/main/java/com/forestry/counter/domain/calculation/EnhancedForestryCalculator.kt`
     (ligne 86 — comparaison `hauteurConforme`)
   - `app/src/main/java/com/forestry/counter/presentation/screens/forestry/ExpertIbpExtension.kt`
     (lignes 252, 481 — libellés d'affichage, à faire valider par le Dendromètre pour la traduction
     UI `expertibp_mean_height` → un libellé distinguant clairement Hdom/Hm)
2. **Priorité moyenne** : conserver l'avertissement déjà présent dans le code sur l'IS = Hdom
   brute (ce n'est pas un bug caché, mais il gagnerait à être remonté dans l'UI utilisateur
   (ex. bandeau « indice de station approximatif, non corrigé de l'âge »), pas seulement en
   commentaire Kotlin invisible pour l'utilisateur final.
3. **Priorité basse / chantier de fond** : envisager la Correction 3 (vrai indice de fertilité par
   courbes Hdom~âge) uniquement après acquisition des données sources manquantes (contact LERFoB
   ou achat des tables Decourt/Vannière). Ne pas coder de coefficients d'ajustement d'âge inventés
   sans source.
4. **Ne pas intégrer CAPSIS/Fagacées/SimCoP/Forêts-21 comme dépendance technique directe** dans
   GeoSylva à ce stade : aucun n'expose d'API mobile-friendly, et les modèles individus-centrés
   sont hors de portée d'un calcul embarqué léger sur smartphone. Les citer en revanche comme
   **sources bibliographiques** dans la documentation utilisateur/scientifique de l'app est
   pertinent et sans risque technique.
5. **Compléter les tables de production** : les tables `cheneProductionTable`/`hetreProductionTable`
   actuelles n'ont pas pu être vérifiées valeur par valeur contre l'ouvrage source (non
   disponible en accès libre) — recommander l'achat/consultation physique de Vannière & Decourt
   (1984, 158 p.) pour valider chaque cellule numérique avant toute communication officielle de
   ces chiffres comme « validés ONF/INRA ».

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` La date exacte de publication de hal-00823732 (article Duplat et al.
  sur la Hdom du hêtre) n'a pas pu être confirmée précisément à partir du PDF récupéré.
- `[À VÉRIFIER MANUELLEMENT]` La licence exacte du code source de la plateforme CAPSIS (le CC
  BY-NC-SA 4.0 constaté concerne le wiki de documentation, pas nécessairement le code Java lui-même) —
  consulter https://capsis.cirad.fr/capsis/charter avant toute décision d'intégration.
- `[À VÉRIFIER MANUELLEMENT]` L'existence ou non de tables de production Décourt/Pardé
  spécifiques au sapin pectiné et au mélèze n'a pas pu être confirmée dans les notices
  bibliographiques consultées (mots-clés listés : pin sylvestre, pin noir, épicéa, douglas, pin
  maritime, hêtre, chêne — sapin et mélèze absents des mots-clés mais possiblement couverts par
  d'autres publications complémentaires non retrouvées).
- `[À VÉRIFIER MANUELLEMENT]` Le pourcentage d'écart Hdom vs Hm cité en §2.7 (« +10 à +25 % »)
  est une estimation qualitative de littérature générale de mensuration (pas spécifique à la
  sylviculture française) — à remplacer par une mesure empirique sur données IFN/placettes
  GeoSylva réelles avant tout usage dans un calcul métier.
- `[À VÉRIFIER MANUELLEMENT]` Aucune API HTTP publique n'a été identifiée pour CAPSIS, Fagacées,
  SimCoP ou Forêts-21 lors de cette recherche documentaire — une vérification technique directe
  (test de requêtes réelles) n'a pas été effectuée dans le cadre de cette mission (recherche
  documentaire uniquement, pas de test d'API en conditions réelles).
- Les valeurs numériques des tables `cheneProductionTable`/`hetreProductionTable` dans
  `ExpertForestryCalculator.kt` n'ont pas pu être confrontées cellule par cellule à l'ouvrage
  source Decourt/Vannière (non disponible en accès libre) — seule la **structure** des tables
  (colonnes, ordre de grandeur, essences couvertes) a pu être confirmée par les sources externes.
