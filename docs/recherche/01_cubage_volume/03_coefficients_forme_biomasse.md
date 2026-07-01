# Coefficients de forme, biomasse et LERFoB Forest Tools — vérification et faisabilité Android

**Domaine** : docs/recherche/01_cubage_volume/
**Date de recherche** : 2026-07-01
**Agent** : sous-agent recherche cubage/biomasse (vérification `1.6 Coefficients de forme` + `RESEARCH_OPPORTUNITIES.md` §2.2)

---

## 0. Résumé de la commande

Ce document répond à trois questions :
1. Les coefficients de forme `f` par essence dans `TarifData.kt` (attribués à Pardé & Bouchon 1988)
   sont-ils vérifiables/corrects ?
2. La bibliothèque « LERFoB Forest Tools » existe-t-elle réellement, avec quelle licence et quelle
   API (en particulier la classe `FrenchCommercialVolume2020` mentionnée dans `RESEARCH_OPPORTUNITIES.md`) ?
3. Est-elle intégrable dans une app Android Kotlin commerciale (dépendances, taille, AWT/Swing, LGPL) ?
   Existe-t-il un modèle plus récent (2020+) utilisé par les professionnels (ONF/CNPF) ?

**Conclusion courte** (détaillée en §4) : LERFoB Forest Tools **existe bel et bien**, mais c'est une
bibliothèque Java desktop (Swing/AWT, jaxb, drivers Access) **non intégrable en l'état sur Android**.
La classe `FrenchCommercialVolume2020Predictor` existe réellement (bonne nouvelle : ce n'est pas une
hallucination). La licence **LGPL-3.0 n'interdit pas l'usage commercial**, mais son usage pratique sur
Android (linkage statique dans un APK/AAB) pose une question de conformité qu'il est plus simple
d'éviter en **réimplémentant les formules publiées en Kotlin natif** plutôt qu'en importer le jar.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| Pardé J., Bouchon J., *Dendrométrie*, 2ᵉ éd., ENGREF, 1988 | scientifique (ouvrage de référence) | officielle/académique | citée dans hal-03390143 et infodoc.agroparistech.fr | 1988 |
| HAL — coefficient de forme (fiche/extrait) | scientifique | officielle (HAL/CCSD) | https://hal.science/hal-03390143/document | non daté précisément dans les métadonnées PDF (dépôt HAL) |
| Infodoc AgroParisTech — notice Bouchon 1988 | officielle | officielle (bibliothèque AgroParisTech) | https://infodoc.agroparistech.fr/index.php?lvl=author_see&id=26490 | 1988, ISBN 978-2-85710-025-6 |
| Projet EMERGE (INRAE/ONF/FCBA, Deleuze et al. 2013 ; Vallet et al. 2006) | scientifique/officielle | officielle (INRAE, ANR) | https://hal.inrae.fr/hal-00934771 ; https://www.researchgate.net/publication/257143322 | 2006 / 2013 |
| CWFC-CCFB/CAT (dépôt GitHub) | officielle (code source, ex-LERFoB AgroParisTech, repris par Service canadien des forêts) | officielle/scientifique | https://github.com/CWFC-CCFB/CAT | dernières release 2025 (voir Maven) |
| CWFC-CCFB/lerfobforesttools (dépôt source, javadoc) | officielle | officielle/scientifique | https://github.com/CWFC-CCFB/lerfobforesttools ; javadoc https://lerfobforesttools.sourceforge.io/lerfobforesttools/javadoc/overview-summary.html | dernière release Maven Central 14/08/2025 |
| Maven Central — `io.github.cwfc-ccfb:lerfobforesttools` / `:repicea` | officielle (dépôt de paquets) | officielle | https://mvnrepository.com/artifact/io.github.cwfc-ccfb | lerfobforesttools : dernière release 14/08/2025 ; repicea : 1.17.2 |
| SourceForge — wiki REpicea (dépendances, licence) | officielle (auteur du projet) | officielle | https://sourceforge.net/p/repiceasource/wiki/REpicea%20-%20main%20page/ | consulté 2026-07-01, non daté précisément côté page |
| Javadoc `FrenchCommercialVolume2020Predictor` | officielle (documentation du code) | officielle | https://lerfobforesttools.sourceforge.io/lerfobforesttools/javadoc/lerfob/predictor/volume/frenchcommercialvolume2020/FrenchCommercialVolume2020Predictor.html | package daté « update d'un modèle 2014 » — pas de date de publication scientifique associée trouvée dans le javadoc lui-même |
| IGN — méthodologie carbone forêt (facteurs d'expansion biomasse, CARBOFOR 2004) | officielle | officielle (IGN) | https://foret.ign.fr/api/upload/IGD2020_1.4_carbone_methodo_detail_regions_mars21.pdf | mars 2021 |
| Rapport « I.1.5 Coefficients d'expansion (BEF) racines/carbone » (ministère agriculture) | officielle | officielle | http://www.agriculture.gouv.fr/telecharger/81462 | non daté précisément dans l'extrait obtenu — **[À VÉRIFIER MANUELLEMENT]** |
| HAL — passage volume bois fort → carbone (formule C = VIFN×DEN×FEB×FER×CAR) | scientifique | officielle (HAL) | https://hal.science/hal-03443385v1/document | citée avec Bouchon et al. 1981, Cannell 1982, Vogt et al. 1996 |
| ADEME — méthodologie ALDO (forêts et haies) | officielle | officielle (ADEME) | https://aldo-documentation.territoiresentransitions.fr/aldo-documentation/flux/specificites-forets-et-haies | consulté 2026-07-01, non daté précisément dans la page |
| Stack Overflow — incompatibilité javax.swing/java.awt sur Android | technique | commerciale/tierce (communauté, mais faits techniques vérifiables via Android SDK docs) | https://stackoverflow.com/questions/28544821 ; https://stackoverflow.com/questions/25980118 | consulté 2026-07-01 |
| `TarifData.kt` (code GeoSylva actuel) | code interne | — | `app/src/main/java/com/forestry/counter/domain/calculation/tarifs/TarifData.kt` | commit courant |

**Point de méthode important** : je n'ai **pas pu extraire le tableau chiffré exact** de
`hal-03390143/document` (le PDF récupéré est un flux binaire compressé/scanné non exploitable par
l'outil de fetch utilisé). Les valeurs précises « f par essence et par âge » de Pardé & Bouchon 1988
n'ont donc **pas pu être vérifiées chiffre par chiffre** dans le cadre de cette recherche —
voir §5 « Limites ».

---

## 2. Données détaillées

### 2.1 Coefficients de forme dans le code GeoSylva actuel

`TarifData.kt` (lignes ~271-397) contient une table `coefsFormeParEssence` de **~90 entrées**
(essence → `f`), commentée comme provenant de « Pardé & Bouchon (1988), *Dendrométrie*, ENGREF »,
avec un usage dans `TarifCalculator.kt` :

```kotlin
// TarifCalculator.kt, lignes 192-202
private fun volumeCoefForme(essenceCode, diamCm, hauteurM, override): Double {
    val f = override ?: defaultCoefForme(essenceCode)
    val g = PI / 4.0 * (diamCm / 100.0).pow(2.0)
    return g * hauteurM * f   // V = G × H × f
}
```

Plage observée dans le code : `f` va de **0.39** (Douglas vert, séquoia) à **0.53** (buis,
genêts d'Espagne), avec un fallback générique `f = 0.45` (`essence = "*"`). Ces plages sont
**cohérentes avec l'ordre de grandeur classique** enseigné en dendrométrie française : les
résineux à fût cylindrique (Douglas, épicéa, sapin) ont un coefficient de forme plus faible
(0.39-0.44) que les feuillus à houppier large et défilement marqué (chênes, hêtre : 0.45-0.50),
et les arbustes/petites essences (buis, houx, prunellier) ont les valeurs les plus élevées
(0.50-0.53) car proches de formes cylindriques sur petite hauteur. C'est le sens attendu de la
littérature (Pardé & Bouchon, chap. sur le défilement et le coefficient de forme f = V/(G×H)).

**Ce que je peux confirmer** : l'ordre de grandeur et le sens des variations par essence/famille
sont plausibles et conformes à la doctrine classique française.
**Ce que je NE peux PAS confirmer** : que chaque valeur au centième (ex. `CH_SESSILE = 0.46` très
exactement, vs `0.455` ou `0.47`) correspond au chiffre publié dans l'édition 1988. Le document
source cité (`hal-03390143`) n'a pas pu être lu en texte exploitable. **[À VÉRIFIER MANUELLEMENT]**
— télécharger le PDF HAL et lire manuellement le tableau (ou consulter l'ouvrage papier ENGREF 1988,
ISBN 978-2-85710-025-6, disponible via la bibliothèque AgroParisTech/infodoc).

Point additionnel non traité dans le code actuel : Pardé & Bouchon indiquent que le coefficient de
forme **varie avec l'âge et le diamètre** (un jeune arbre défilé a un `f` différent d'un vieil arbre
à houppier développé) — la table de GeoSylva utilise une **valeur unique par essence** (approche
« simplifiée », standard pour les tarifs à une entrée). C'est un choix de simplification répandu
(les tarifs de cubage eux-mêmes intègrent déjà l'essentiel de cette variabilité via D et H), mais à
documenter explicitement comme approximation dans l'app.

### 2.2 LERFoB Forest Tools — existence, dépôt, licence, classes

**Existence confirmée.** LERFoB (Laboratoire d'Étude des Ressources Forêt-Bois, unité mixte
INRAE/AgroParisTech, https://hal.inrae.fr/LERFOB) a développé une bibliothèque Java de modèles de
croissance/volume/biomasse pour le contexte forestier français, aujourd'hui maintenue par
**Mathieu Fortin** (ex-LERFoB, désormais Service canadien des forêts / Canadian Forest Service,
CWFC-CCFB) sous l'organisation GitHub **CWFC-CCFB**.

- **Dépôt principal** : https://github.com/CWFC-CCFB/lerfobforesttools
- **Dépôt de l'outil carbone qui l'utilise** : https://github.com/CWFC-CCFB/CAT (Carbon Accounting
  Tool, ex-LERFoB-CAT)
- **Bibliothèque de base (dépendance)** : https://github.com/CWFC-CCFB/repicea (utilitaires
  génériques : maths, stats, GUI, I/O, sérialisation XML)
- **Publication Maven Central** : `io.github.cwfc-ccfb:lerfobforesttools` et
  `io.github.cwfc-ccfb:repicea` (https://mvnrepository.com/artifact/io.github.cwfc-ccfb) — dernières
  releases 2025.
- **Javadoc public** : https://lerfobforesttools.sourceforge.io/lerfobforesttools/javadoc/
- **Licence** : **LGPL-3.0** — confirmée sur le dépôt GitHub CAT (badge « License: LGPL v3 »,
  https://www.gnu.org/licenses/lgpl-3.0.html) et sur la page wiki REpicea (« The library is developed
  under a LGPL 3.0 license »).

**Classe `FrenchCommercialVolume2020` — existe réellement**, ce n'est pas une hallucination du
sous-agent précédent. Package :
`lerfob.predictor.volume.frenchcommercialvolume2020`, avec :
- `FrenchCommercialVolume2020Predictor` — classe principale, `predictTreeCommercialOverbarkVolumeDm3(tree)`.
  Prédit le **volume commercial bois fort tige, sur écorce, découpe fin bout 7 cm**, essence par
  essence. D'après le javadoc : *« This model is an update of a preliminary model fitted in 2014,
  which can be found in the frenchcommercialvolume2014 package »* — donc bien un modèle 2020
  (mise à jour d'un modèle 2014), avec incertitude/variance disponible (constructeur
  `isVariabilityEnabled`).
- `FrenchCommercialVolume2020Tree` (interface) et `FrenchCommercialVolume2020TreeImpl` (implémentation
  basique) — nécessitent DBH (cm), hauteur (m), essence (via un enum
  `FrenchCommercialVolume2020TreeSpecies`).
- Retourne 0 si DBH < 7.5 cm, -1 si hauteur manquante.

Autres classes pertinentes trouvées dans le package `lerfob.allometricrelationships` :
- `BouchonVolumeEquations` — implémente **les équations de volume de Bouchon pour chêne et hêtre**
  (donc directement liées à Pardé & Bouchon).
- `ValletTotalAboveGroundVolumeEquations` — modèle de **Vallet et al.** pour le volume aérien total
  (tronc + branches), issu du projet EMERGE (cf. §2.3).

**[À VÉRIFIER MANUELLEMENT]** : je n'ai pas trouvé, dans les extraits javadoc accessibles, la
référence bibliographique exacte (auteurs, revue, année) associée à `FrenchCommercialVolume2020`
au-delà de la mention « update d'un modèle 2014 ». À confirmer via le code source complet du dépôt
GitHub (commentaires Javadoc dans les fichiers `.java`) ou une recherche HAL dédiée avant citation
dans la documentation utilisateur GeoSylva.

### 2.3 Modèle plus récent que Pardé & Bouchon 1988 utilisé par les professionnels

**Oui — les praticiens français (IGN, ONF, CNPF) utilisent aujourd'hui des modèles postérieurs
à 1988**, notamment issus du **projet EMERGE** (ANR, 2010-2013, INRAE/ONF/FCBA) :
- Vallet et al. (2006) — modèles de volume aérien total par essence.
- Deleuze et al. (2013) — « Le projet EMERGE pour des tarifs cohérents de volumes et biomasses des
  essences forestières françaises métropolitaines » (https://www.researchgate.net/publication/257143322).

C'est cohérent avec la mention déjà présente dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.4
(projet EMERGE) — **ce document confirme et détaille** cette piste plutôt que de la dupliquer :
EMERGE est le successeur naturel/mise à jour scientifique de Pardé & Bouchon pour le volume ET la
biomasse, avec des données collectées plus récemment et une couverture élargie des essences.
Les classes `BouchonVolumeEquations` et `ValletTotalAboveGroundVolumeEquations` de LERFoB Forest
Tools sont précisément l'implémentation logicielle de ces deux générations de modèles.

Pour le carbone/biomasse spécifiquement, la méthode de référence utilisée par l'IGN et l'inventaire
national des émissions de GES (formule `C = VIFN × DEN × FEB × FER × CAR`) remonte au **projet
CARBOFOR (2004)** pour les facteurs d'expansion (branches FEB, racines FER) et les infradensités
par essence, avec un taux de carbone moyen de la biomasse fixé à **47.5 %**. Facteurs d'expansion
racinaires moyens rapportés : **1.30 pour les conifères, 1.28 pour les feuillus**
(source : rapport ministère de l'Agriculture, http://www.agriculture.gouv.fr/telecharger/81462,
§I.1.5 — **[À VÉRIFIER MANUELLEMENT]** la date exacte du rapport n'a pas pu être confirmée dans
l'extrait récupéré). Documentation méthodologique consolidée : IGN, mars 2021
(https://foret.ign.fr/api/upload/IGD2020_1.4_carbone_methodo_detail_regions_mars21.pdf).

Ces facteurs d'expansion sont **eux-mêmes reconnus comme une source d'erreur importante** par les
auteurs (variabilité forte selon essence/âge/région/sylviculture — cf. hal-03443385v1), donc à
présenter dans GeoSylva avec un avertissement d'incertitude plutôt que comme valeur exacte.

**Conclusion sur « plus récent que 1988 »** : oui, EMERGE (2006-2013) est la référence
scientifique la plus citée aujourd'hui pour le volume/biomasse par essence en France ; pour le
carbone spécifiquement, CARBOFOR (2004) + méthodologie IGN (2021, mise à jour méthodo, pas
nouveaux coefficients fondamentaux) restent la référence officielle utilisée dans l'inventaire
national GES. Je n'ai pas trouvé de modèle **plus récent que EMERGE** qui l'aurait remplacé pour le
volume/biomasse par essence (le modèle `FrenchCommercialVolume2020` de LERFoB Forest Tools est une
mise à jour d'un sous-modèle de *volume commercial*, pas un remplacement complet d'EMERGE).

### 2.4 Biomasse aérienne/racinaire — équations allométriques françaises

- Manuel FAO (allométrie/biomasse/tarifs, méthode générale, en français) :
  http://foris.fao.org/static/allometric/tarifs_fr_web_May23_light.pdf — référentiel méthodologique
  généraliste (pas spécifique France), utile pour la pédagogie de l'app mais pas pour des coefficients
  français directement exploitables.
- Formule française opérationnelle de conversion volume → carbone :
  `C = VIFN × DEN × FEB × FER × CAR` (cf. §2.3), où :
  - `DEN` = infradensité du bois par essence (masse anhydre / volume vert)
  - `FEB` = facteur d'expansion branches (aérien total / tige)
  - `FER` = facteur d'expansion racines (total ligneux / aérien total) — **1.30 conifères / 1.28 feuillus** en moyenne
  - `CAR` = taux de carbone moyen de la biomasse sèche — **47.5 %**
- Pas d'équivalent IPCC Tier 3 France identifié en accès libre et daté avec précision dans le cadre
  de cette recherche (le Tier 2 « facteurs par défaut génériques » IPCC existe mais la France utilise
  ses propres coefficients nationaux CARBOFOR/IGN, ce qui est en fait **plus proche d'un Tier 3
  national** que du Tier 2 générique IPCC). **[À VÉRIFIER MANUELLEMENT]** avant toute mention
  « Tier 2/3 IPCC » dans la documentation utilisateur — je n'ai pas trouvé de document officiel
  français qui classe explicitement sa méthode comme « Tier 2 » ou « Tier 3 » au sens du GIEC.

---

## 3. Comparatif / analyse critique — faisabilité technique Android de LERFoB Forest Tools

| Critère | LERFoB Forest Tools (`lerfobforesttools` + `repicea`) | Impact pour GeoSylva (Kotlin/Android) |
|---|---|---|
| Langage/plateforme | Java desktop (JVM standard, pas AGP/Android-aware) | Compatible Java→Kotlin en théorie, mais… |
| Dépendance `repicea.gui` | Utilise **`javax.swing` et `java.awt`** massivement (dialogs, composants GUI, `ActionListener`, `DropTargetDropEvent`…) | **Bloquant** : `javax.swing`/`java.awt` **n'existent pas dans le SDK Android** (confirmé : Stack Overflow, doc Android — pas de UI toolkit desktop sur Android). Si le module GUI est référencé (même indirectement via des classes partagées type `AbstractGenericEngine`), le bytecode contient des références non résolvables → risques de `NoClassDefFoundError` à l'exécution, et complications avec R8/D8 en mode `-dontwarn`/`keep`. |
| Autres dépendances transitives | `ucanaccess` (pilote MS Access, Apache-2.0), `json-io` (Apache-2.0), `jaxb` 2.3.2 (EDL) pour Java 11/13, `jfreechart` (LGPL), `batik` (Apache-2.0), `py4j` (BSD) | Beaucoup de dépendances **inutiles et lourdes pour un besoin purement numérique** (graphiques desktop, XML binding, base Access) — aucune n'a de sens sur mobile. |
| Taille | Bibliothèque complète orientée application desktop scientifique (CAT = outil GUI complet) ; le jar `lerfobforesttools` seul + `repicea` + transitives représentent plusieurs Mo, avec beaucoup de code mort du point de vue mobile | Gonflement inutile de l'APK/AAB, temps de build accru, risque d'échec de dexing (D8/R8) sur les classes AWT/Swing. |
| API réellement utile pour GeoSylva | `FrenchCommercialVolume2020Predictor`, `BouchonVolumeEquations`, `ValletTotalAboveGroundVolumeEquations` — chacune ne nécessite en théorie que des maths (DBH, hauteur, essence) | **Seule une fraction infime de la bibliothèque est utile.** Ces classes héritent cependant de `REpiceaPredictor` (package `repicea.simulation`), lui-même dépendant potentiellement d'autres briques `repicea.stats`/`repicea.math` — à vérifier si ce sous-arbre est réellement isolé de `repicea.gui`/AWT (probable en grande partie, mais non garanti sans compilation d'essai réelle). |
| Build system | Gradle (le projet est passé sous Gradle selon le wiki REpicea) | Compatible en théorie avec un projet Android Gradle, mais nécessite exclusions de dépendances (`exclude group/module`) pour retirer AWT/Swing/Access/jaxb, avec le risque que la classe cible ne compile plus sans elles. |
| Licence | **LGPL-3.0** | Voir §3.1 ci-dessous — n'interdit pas l'usage commercial, mais implications de conformité à respecter. |
| Maintenance | Activement maintenue (releases Maven 2025), mais désormais sous l'égide du Service canadien des forêts, contexte principal = modèles canadiens + module français hérité | Risque de dérive de priorités (le focus n'est plus la France) ; support/communauté restreints (recherche scientifique, pas grand public). |

**Verdict effort** : « Difficile », comme déjà indiqué dans `RESEARCH_OPPORTUNITIES.md` §2.2 — ce
document **confirme** cette évaluation et l'affine : la difficulté n'est pas seulement l'intégration
Gradle, c'est une **incompatibilité structurelle** (AWT/Swing) qui rend l'import direct du jar
risqué/non garanti sans un travail de reverse engineering du graphe de dépendances internes (voire
un fork qui retire les modules GUI, ce qui poserait à son tour des obligations LGPL de republication
du fork modifié).

### 3.1 Licence LGPL-3.0 — implications pour une app commerciale payante

Les faits (LGPL-3.0, texte officiel : https://www.gnu.org/licenses/lgpl-3.0.html) :

- **La LGPL n'est pas « virale » comme la GPL** : le fait de *lier* (dynamiquement ou statiquement)
  une bibliothèque LGPL à votre application **n'oblige pas à publier le code source de votre
  application**. GeoSylva pourrait rester propriétaire/payant.
- **Obligations qui s'appliquent quand même** :
  1. Fournir/rendre accessible le **code source de la bibliothèque LGPL elle-même** (y compris toute
     modification que vous y apporteriez) — trivial ici puisqu'elle est déjà publique sur GitHub, il
     suffit de la citer et d'en lier la source dans les mentions légales de l'app.
  2. **Permettre à l'utilisateur de remplacer/relier une version modifiée de la bibliothèque LGPL**
     (LGPLv3 §4d) — c'est la clause la plus délicate en mobile : un APK/AAB est compilé et signé de
     façon monolithique, l'utilisateur final ne peut pas « relinker » une DLL/jar comme sur desktop.
     En pratique, l'écosystème mobile (ex. builds LGPL de FFmpeg intégrés dans des apps commerciales)
     traite cette clause comme satisfaite si : (a) la dépendance reste un module/jar séparé et non
     fusionné/modifié dans le code source de l'app, (b) le nom, la version et un lien vers les
     sources de la bibliothèque LGPL sont visibles dans un écran de mentions légales/licences
     (« About » / écran crédits open source), et (c) l'éditeur s'engage à fournir sur demande les
     fichiers objets nécessaires à un relinkage. **Ce n'est cependant pas une garantie juridique
     absolue** — c'est une zone d'usage établi mais pas totalement clarifiée pour le cas spécifique
     du linkage statique dans un binaire mobile signé.
  3. **Ne pas retirer les avis de copyright/licence** de la bibliothèque.

- **Risque commercial concret pour GeoSylva** :
  - **Faible/nul** si LERFoB Forest Tools est utilisé *tel quel*, comme dépendance externe non
    modifiée, avec attribution correcte dans un écran de licences — cela n'imposerait pas d'ouvrir le
    code de GeoSylva ni ne remettrait en cause le modèle payant.
  - **Plus élevé** si l'équipe **fork/modifie** le code LERFoB pour le faire fonctionner sur Android
    (ex. retirer `repicea.gui`) : la LGPL impose alors de publier les sources de cette version
    modifiée (sous LGPL également) — ce qui est gérable (publier le fork sur GitHub) mais ajoute une
    charge de maintenance et une obligation de transparence sur les modifications.
  - Le risque n'est **pas** un risque de devoir open-sourcer *GeoSylva dans son ensemble* (contrairement
    à la GPL ou l'AGPL, correctement signalée à part comme « ⚠️ AGPL » pour Treetracker dans
    `RESEARCH_OPPORTUNITIES.md`) : LGPL est nettement plus permissive pour un usage commercial.

**Recommandation pragmatique** (voir aussi §4) : plutôt que d'importer le jar LGPL avec ses
incompatibilités AWT, **réimplémenter en Kotlin natif les formules mathématiques publiées** dans les
articles scientifiques associés (Bouchon, Vallet et al., Deleuze et al., et si trouvable la
publication académique derrière `FrenchCommercialVolume2020`). Une formule mathématique et des
coefficients publiés dans un article scientifique en accès ouvert **ne sont pas eux-mêmes couverts
par la licence LGPL du code** — c'est le *code* (l'implémentation Java) qui est sous LGPL, pas
l'équation ou les paramètres numériques publiés dans un article (protégés au mieux par le droit
d'auteur du texte de l'article, ce qui est un problème différent — **citer la source scientifique
reste la bonne pratique**, indépendamment du droit de licence logicielle). Cette approche élimine
à la fois le problème AWT/Swing et l'essentiel de l'ambiguïté LGPL/mobile, au prix d'un travail
manuel de retranscription et de vérification des coefficients (à faire avec rigueur, cf. §5).

---

## 4. Recommandation pour GeoSylva

1. **Ne pas intégrer le jar `lerfobforesttools`/`repicea` dans l'app Android.** Le rapport
   coût (dépendances AWT/Swing, taille, risque de build, ambiguïté LGPL en linkage statique mobile)
   / bénéfice (accès à 2-3 classes de calcul) est défavorable. Rejoint et confirme l'évaluation
   « Difficile » de `RESEARCH_OPPORTUNITIES.md` §2.2/§6.3.
2. **Réimplémenter nativement en Kotlin, dans `TarifCalculator.kt`/`TarifData.kt`**, les modèles
   suivants une fois leurs formules/coefficients confirmés à la source :
   - Modèle EMERGE / Vallet et al. 2006 / Deleuze et al. 2013 pour le volume aérien total et la
     biomasse par essence — piste prioritaire, potentiellement complémentaire ou correctif de
     `coefsFormeParEssence`.
   - Formule française `C = VIFN × DEN × FEB × FER × CAR` (IGN/CARBOFOR) pour toute fonctionnalité de
     GeoSylva touchant au **bilan carbone forestier** (ex. si le module IBP ou un futur module carbone
     en a besoin) — FEB par essence à collecter précisément, FER = 1.30 (conifères) / 1.28 (feuillus)
     déjà identifiés, CAR = 47.5 %.
   - Si l'équation de `FrenchCommercialVolume2020Predictor` (donnée mathématique, pas le code) peut
     être retrouvée dans une publication scientifique ouverte associée, elle constituerait une
     alternative de tarif de cubage plus récente que Schaeffer/Algan/IFN — **à rechercher
     spécifiquement avant intégration** (voir §5).
3. **Documenter dans l'app** (écran de documentation des tarifs) que le coefficient de forme est une
   **valeur moyenne par essence, non ajustée à l'âge/diamètre**, avec la source Pardé & Bouchon 1988
   citée telle qu'actuellement, en attendant une vérification chiffre par chiffre du tableau source.
4. **Fichiers Kotlin concernés** (aucune modification effectuée dans le cadre de cette recherche,
   seulement identifiés pour un futur ticket) :
   - `app/src/main/java/com/forestry/counter/domain/calculation/tarifs/TarifData.kt` (table
     `coefsFormeParEssence`, à revérifier/compléter avec EMERGE)
   - `app/src/main/java/com/forestry/counter/domain/calculation/tarifs/TarifCalculator.kt`
     (`volumeCoefForme`, `volumeFgh`, `defaultCoefForme`)
   - `app/src/main/java/com/forestry/counter/domain/calculation/ExpertForestryCalculator.kt` (aucun
     usage direct des coefficients de forme trouvé dans ce fichier lors de cette recherche — les
     paramètres qui y apparaissent, ex. `ChapmanRichardsParameters.b`, concernent des courbes de
     croissance en diamètre, pas le coefficient de forme volumétrique ; à noter si une doc antérieure
     affirmait le contraire)
5. **Priorité** : Moyenne — utile pour fiabiliser/affiner le calcul de volume existant (déjà
   fonctionnel via Schaeffer/Algan/IFN + coefficient de forme), mais pas bloquant. Prioriser d'abord
   la vérification manuelle du tableau Pardé & Bouchon (§5, action peu coûteuse) avant tout travail
   d'intégration EMERGE plus lourd.

---

## 5. Limites et points à vérifier manuellement

- **Tableau exact Pardé & Bouchon 1988** : non extrait (PDF HAL non exploitable par l'outil de
  fetch utilisé dans cette recherche). Actions possibles : (a) télécharger `hal-03390143` et l'ouvrir
  avec un lecteur PDF/OCR pour lire le tableau page par page, (b) consulter l'ouvrage papier via une
  bibliothèque universitaire (ISBN 978-2-85710-025-6), (c) contacter le CNPF/AgroParisTech pour
  confirmation. **Sans cette vérification, considérer les ~90 valeurs de `coefsFormeParEssence` comme
  plausibles mais non formellement confirmées au centième.**
- **Référence scientifique précise de `FrenchCommercialVolume2020Predictor`** : le javadoc mentionne
  seulement « update d'un modèle 2014 » sans citation d'article. À rechercher dans le code source
  complet (commentaires Javadoc des fichiers `.java` sur GitHub) ou dans les publications de
  Mathieu Fortin / LERFoB pour obtenir la référence bibliographique exacte avant citation publique
  dans GeoSylva.
- **Classification IPCC Tier 2 vs Tier 3 de la méthode française** : affirmation non confirmée par
  une source officielle qui l'explicite dans ces termes — à vérifier auprès de l'inventaire national
  GES français (CITEPA) ou de la documentation IPCC si cette classification est nécessaire pour la
  communication produit.
- **Date exacte du rapport ministériel** (`agriculture.gouv.fr/telecharger/81462`, coefficients
  FEB/FER) non confirmée dans l'extrait récupéré — à ouvrir directement pour dater précisément
  (probablement années 2010, projet lié à CARBOFOR/IGN, mais à confirmer).
- **Isolation réelle du sous-arbre de dépendances de `FrenchCommercialVolume2020Predictor` par
  rapport à `repicea.gui`/AWT** : évaluée ici par analyse de la documentation et de la structure des
  packages, **pas par une compilation Gradle réelle avec exclusion de dépendances**. Si l'équipe
  souhaite malgré tout tenter l'intégration du jar (plutôt que la réimplémentation recommandée), un
  test de compilation réel (Gradle `exclude module: 'repicea-gui'` ou équivalent, puis `./gradlew
  assembleDebug`) serait nécessaire pour confirmer/infirmer le blocage AWT — **non réalisé dans le
  cadre de cette recherche documentaire**.
- Aucune modification de code n'a été effectuée : ce document est **exclusivement documentaire**,
  conformément à la commande.
