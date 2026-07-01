# Tarifs de cubage IFN (IGN) et projet EMERGE (INRAE/ONF/FCBA)

**Domaine** : docs/recherche/01_cubage_volume/
**Date de recherche** : 2026-07-02
**Agent** : sous-agent recherche « Tarifs IFN / EMERGE »

---

## 0. Cadrage et périmètre

Ce document complète (sans dupliquer) les sections **1.3 Tarifs IFN** et **1.4 Projet EMERGE**
de `docs/REFERENTIELS_FORESTIERS_EXTERNES.md`. Il se concentre sur :

1. Vérification/complément des formules et coefficients des « Tarifs rapides IFN » (36 tarifs,
   1 entrée D₁₃₀) et « Tarifs lents IFN » (8 tarifs, 2 entrées D₁₃₀+H) déjà implémentés dans
   `TarifData.ifnRapide` / `TarifData.ifnLent` (`app/src/main/java/com/forestry/counter/domain/
   calculation/tarifs/TarifData.kt`, lignes 208-269) et `TarifModels.kt` (lignes 130-166).
2. Approfondissement du projet EMERGE (modèles de cubage volume/biomasse, coefficients de
   forme, hauteur de décrochement) et **test réel** de l'accès aux données ouvertes annoncées.
3. Guide de décision : quand utiliser IFN Rapide vs IFN Lent vs Schaeffer/Algan (ces derniers
   déjà couverts par un autre agent — comparaison croisée uniquement).

**Constat liminaire important** : contrairement à Schaeffer/Algan (formules publiées et
stables depuis 1949/1958, largement reproduites dans la littérature pédagogique française),
**les coefficients numériques exacts des « 36 tarifs rapides » et « 8 tarifs lents » de l'IFN
ne sont pas publiés en libre accès sous forme de table chiffrée téléchargeable**. La
documentation IFN publique décrit la *méthode de construction* des tarifs (tarif brut,
ajustement pondéré) mais pas un tableau de coefficients (a₀, a₁, a₂) prêt à l'emploi comme
celui déjà présent dans `TarifData.kt`. Ce point est développé en section 3.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| Poncet L. (IFN), « Les tarifs de cubage de l'Inventaire forestier national », note technique | Officielle (IFN) | Officielle | http://hdl.handle.net/2042/42780 | Non daté précisément sur la page (probablement fin 1980s/1990s, republié via HAL/CTBA — **à vérifier**) |
| Batias A. (1958), « Construction d'un tarif de cubage approprié », *Revue Forestière Française* | Scientifique (historique) | Scientifique | https://hal.science/hal-03381976 (DOI 10.4267/2042/27382) | 1958, pp. 109-118 |
| IGN, « Ressource et disponibilité en bois d'œuvre... coniferes/feuillus » (études filière bois France) | Officielle (IGN) | Officielle | https://inventaire-forestier.ign.fr/IMG/pdf/etude_bo_france_restitution_coniferes_004_.pdf | Étude ADEME/IGN, restitution ~2019-2021 |
| Deleuze C. et al. (2014), « Estimation harmonisée du volume de tige à différentes découpes », *Rendez-vous Techniques de l'ONF* n°44 | Officielle/scientifique (ONF-RDI, LERFoB-INRA, IGN) | Officielle/scientifique | https://hal.science/hal-03016051 | 2014, pp. 33-42 |
| Deleuze C., présentation CAQSIS 4, « The french project EMERGE » | Scientifique (colloque CIRAD/CAPSIS) | Scientifique | https://capsis.cirad.fr/capsis/_media/documentation/reports/16_deleuzec_caqsis4-2014-emerge_v1.pdf | 9 avril 2014, Gembloux |
| ANR, fiche projet EMERGE (ANR-08-BIOE-0003) | Officielle (ANR) | Officielle | https://anr.fr/Projet-ANR-08-BIOE-0003 | Projet 2009-2013 |
| Notice de données EMERGE, geodata.inrae.fr (GeoNetwork) | Officielle (INRAE) | Officielle | https://geodata.inrae.fr/geonetwork/srv/api/records/27f18f57-b847-4ec3-909b-4067a5e1dc33 | Metadata datestamp : 2018-01-12 |
| Longuetaud F. et al., « Une base de données unique en France de cubages d'arbres individuels... » | Scientifique (Revue Forestière Française) | Scientifique | https://doi.org/10.4267/2042/45181 | — |
| Longuetaud F. et al. (2013), « Les coefficients d'expansion pour déduire différents volumes de branches... » | Scientifique (HAL-INRAE, Forest Ecology and Management) | Scientifique | https://hal.inrae.fr/hal-02648144 | 2013 (publication) |
| IGN, DataIFN (données brutes de terrain de l'inventaire forestier national) | Officielle (IGN) | Officielle | https://inventaire-forestier.ign.fr/dataifn/ | Campagnes annuelles depuis 2005, mise à jour continue |
| Package R `gftools` (pobsteta), fonctions `TarifFindSch` / `TarifIFNSER` | Commerciale/tierce (outil communautaire, non officiel) | Commerciale/tierce | https://rdrr.io/github/pobsteta/gftools/man/TarifFindSch.html | Non daté précisément |
| CNPF Grand Est, support de formation FOGEFOR (dendrométrie/tarifs) | Officielle (CNPF) | Officielle | https://grandest.cnpf.fr/sites/socle/files/cnpf-old/expos_c3_a9_20fogefor_23_09_2016_1.pdf | 23/09/2016 |
| IGN, BD Forêt v2/v3 et géoservices (contexte, pas des tarifs mais données ouvertes connexes) | Officielle (IGN) | Officielle | https://data.geopf.fr/telechargement/resource/BDFORET | Licence Ouverte Etalab 2.0 |

---

## 2. Données détaillées

### 2.1 Tarifs IFN — structure générale (vérifiée)

Confirmé par plusieurs sources indépendantes (note IFN, étude IGN bois d'œuvre, cours
dendrométrie jymassenet-foret.fr déjà cité par l'agent Schaeffer/Algan) :

- La méthode de référence de l'IFN pour cuber un arbre-échantillon combine circonférence
  à 1,30 m, hauteur totale, hauteur de découpe/décrochement et décroissance métrique.
- L'IFN construit ses tarifs à partir de sa propre base de données de terrain (mesures
  d'arbres-échantillons abattus ou cubés sur pied), pas à partir d'une formule théorique
  imposée a priori : *« le choix de la formule du tarif est totalement arbitraire »* — l'IFN
  ajuste plusieurs formes candidates sur les données observées et retient la meilleure
  (technique du « tarif brut » = tableau croisé diamètre×hauteur avec effectif, volume moyen
  centré, écart-type par case, puis ajustement pondéré par l'inverse du carré de l'écart-type).
  Source : http://hdl.handle.net/2042/42780. [Le résumé ci-dessus reformule la note IFN ;
  vérifier le texte intégral avant citation académique — accès au PDF complet non garanti,
  webfetch a renvoyé une erreur HTTP 500 lors du test du 2026-07-02]
- **Conséquence pratique** : il n'existe pas UN jeu unique et universel de « 36 tarifs
  rapides » et « 8 tarifs lents » valables pour toutes les essences et toutes les régions.
  Historiquement, l'appellation « tarifs rapides/lents » vient des **tarifs Schaeffer**
  (1949) : Algan avait construit une vingtaine de tarifs jugés trop rapides (volume croissant
  trop vite avec le diamètre) pour les peuplements réguliers ; Schaeffer a construit des
  tarifs alternatifs plus « lents » mieux adaptés (source : Batias 1958, hal-03381976, citant
  explicitement Schaeffer 1949). Le fait que l'IFN reprenne des séries numérotées « rapide »
  (1 entrée) / « lent » (2 entrées) dans sa propre nomenclature est cohérent avec cette
  tradition, mais **le lien exact entre la numérotation IFN 1-36/1-8 et les tarifs
  Algan/Schaeffer originaux n'a pas pu être confirmé par une source primaire accessible en
  ligne dans le cadre de cette recherche** `[À VÉRIFIER MANUELLEMENT]`.

### 2.2 Coefficients actuellement implémentés dans `TarifData.kt`

Rappel de ce qui existe dans le code (non trouvé identique dans une source primaire en
accès libre — voir limites) :

- **IFN Rapide** (`TarifData.ifnRapide`, 36 entrées) : `V(dm³) = a₀ + a₁·D + a₂·D²` avec D en
  cm. Coefficients strictement croissants et cohérents en progression géométrique d'un numéro
  à l'autre (ex. numéro 1 : a₀=-4.28, a₁=0.280, a₂=0.0340 ; numéro 36 : a₀=-270.30,
  a₁=41.380, a₂=7.9028). Cette progression régulière suggère une **famille de courbes
  générées par interpolation/extrapolation systématique** plutôt qu'un ajustement
  indépendant par essence sur données réelles IFN — pattern similaire à celui des tarifs
  Schaeffer 1E/2E (progression géométrique visible également dans
  `TarifData.schaefferOneEntry`). `[À VÉRIFIER MANUELLEMENT : origine exacte de cette série
  de 36 tarifs — possible reconstruction/estimation plutôt que copie d'une table officielle]`
- **IFN Lent** (`TarifData.ifnLent`, 8 entrées) : `V(dm³) = a₀ + a₁·D² + a₂·D²·H`. Même
  remarque sur la progression régulière des coefficients.
- Mapping essence → numéro de tarif recommandé : `TarifData.essenceToIfnRapideNumero` et
  `essenceToIfnLentNumero` (lignes 406+ et 501+ de `TarifData.kt`), utilisés par défaut dans
  `TarifCalculator.volumeIfnRapide` / `volumeIfnLent` (lignes 176-190 de `TarifCalculator.kt`)
  quand aucun numéro n'est fourni explicitement par l'utilisateur.

### 2.3 Calcul de comparaison chiffrée (test manuel, D=30 cm, H=20 m)

Calculs effectués manuellement à partir des formules et coefficients ci-dessus (résultats
`[calcul dérivé par l'IA, à revérifier]`) :

| Méthode | Tarif/numéro utilisé | Formule | Volume estimé (m³) |
|---|---|---|---|
| IFN Rapide n°12 | `essenceToIfnRapideNumero` (proche valeur médiane) | -27.05+2.518×30+0.3424×30² | ≈ 0.357 |
| IFN Lent n°1 | tarif le plus « rapide » de la série lente | -4.50+0.014×900+0.02032×900×20 | ≈ 0.374 |
| IFN Lent n°4 | tarif intermédiaire | -8.40+0.029×900+0.04408×900×20 | ≈ 0.811 |
| Algan Hêtre commun | `AlganCoefs(a=0.0000362,b=2.158,c=0.860)` | 0.0000362×30^2.158×20^0.86 | ≈ 0.733 |

**Observation** : pour un même arbre (D=30 cm, H=20 m), le volume varie de **0.357 à 0.811
m³** selon la méthode et le numéro de tarif choisi, soit un écart de plus du double. Ceci
illustre un point critique déjà identifié dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.3 :
**le choix du numéro de tarif est la variable la plus sensible du calcul**, bien plus que le
choix de la méthode elle-même (IFN vs Algan vs Schaeffer donnent des ordres de grandeur
comparables si le numéro/l'essence sont bien calés). Recommandation : fiabiliser en priorité
le **mapping essence → numéro** plutôt que de chercher une "meilleure" formule.

### 2.4 Guide de décision IFN Rapide vs IFN Lent vs Schaeffer/Algan

D'après les sources consultées (CNPF FOGEFOR 2016, cours dendrométrie jymassenet-foret.fr,
Batias 1958) :

- **Tarifs à 1 entrée (IFN Rapide, Schaeffer 1E)** : ne nécessitent que le diamètre/la
  circonférence à 1,30 m. Utilisables uniquement sur un **peuplement homogène** (même
  essence, même station, structure régulière) car ils supposent un rapport diamètre↔hauteur
  stable pour tous les arbres du peuplement — *« un tarif de cubage s'applique donc à un
  peuplement et non à un arbre seul »* (CNPF FOGEFOR 2016, p. citant Bouchon 1974). Usage
  typique : aménagement forestier, aménagiste de terrain avec moyens limités, martelage
  rapide, inventaires de gestion courante.
- **Tarifs à 2 entrées (IFN Lent, Schaeffer 2E, Algan)** : nécessitent diamètre **et**
  hauteur (mesurée ou estimée). Gain de précision net, en particulier pour les
  **peuplements irréguliers, mélangés ou jardinés**, où le rapport D/H varie fortement
  arbre à arbre (source confirmée par le sous-agent Schaeffer/Algan pour Algan ; même
  logique documentée pour Schaeffer 2E dans Batias 1958). Usage typique : cubage
  contractuel/commercial, expertise, martelage de précision sur peuplement irrégulier.
- **Signification exacte de « rapide »/« lent »** : deux interprétations circulent dans la
  littérature et ne sont pas totalement recoupées par une source primaire unique :
  1. Interprétation historique (Batias 1958, à propos des tarifs **Schaeffer**, tous à 1
     entrée à l'origine) : « rapide » = la courbe volume/diamètre monte plus vite (tarifs
     Algan jugés trop rapides pour futaies régulières) ; « lent » = courbe plus modérée,
     mieux ajustée aux peuplements âgés/jardinés.
  2. Interprétation opérationnelle courante (reprise dans plusieurs supports pédagogiques
     et cohérente avec la structure du code GeoSylva) : « rapide » = rapide à appliquer sur
     le terrain (1 seule mesure, D) ; « lent » = plus lent car il faut aussi mesurer la
     hauteur (2 mesures, D+H).
  Les deux interprétations coexistent selon les tarifs (Schaeffer historique = sens 1 ;
  usage IFN dans le code GeoSylva = sens 2, cohérent avec `entrees = 1` vs `entrees = 2`
  dans `TarifModels.kt`). `[À VÉRIFIER MANUELLEMENT auprès d'une source IFN primaire quelle
  interprétation est officiellement retenue par l'IFN lui-même]`.
- **Choix Algan vs IFN** (déjà noté par l'agent précédent : Algan par défaut dans l'app) :
  Algan reste la référence pour peuplements réguliers résineux (Douglas, Épicéa — objet
  historique des tarifs Algan) ; les tarifs IFN, construits sur l'échantillon national IFN
  (toutes essences, toutes régions), sont en théorie plus généralistes mais **moins
  spécifiques à une essence/région donnée** que des tarifs Algan calés localement.

### 2.5 Projet EMERGE — approfondissement

Confirmé par les sources ANR, HAL et GeoNetwork INRAE :

- **Nom complet** : « Élaboration de Modèles pour une Estimation Robuste et Générique du
  bois Énergie », projet ANR-08-BIOE-0003, coordonné par l'ONF, actif ~2009-2013 (dates de
  début/fin des données dans la notice GeoNetwork : décembre 2008 → juin 2013).
- **Partenaires confirmés** : ONF (coordinateur), INRA/INRAE (LERFoB, BEF), IRSTEA (ex-
  Cemagref), FCBA, CIRAD-AMAP (avec le laboratoire chinois LIAMA), IFN (intégré depuis à
  l'IGN), CNPF-IDF, CIRAD. Source : notice GeoNetwork INRAE + fiche ANR (concordance
  confirmée entre les deux sources).
- **Objectif scientifique** : construire des modèles **génériques** (valables pour
  plusieurs essences), **robustes** (indépendants de l'origine du peuplement, structure,
  fertilité, taille, sylviculture, date de mesure) et **cohérents entre grandeurs**
  (volume total, volume tige à différentes découpes, biomasse, minéralomasse, pouvoir
  calorifique — pour permettre d'estimer le bois-énergie du houppier en cohérence avec le
  bois d'œuvre déjà mesuré par les tarifs classiques).
- **Volumétrie des données** (source : présentation CAQSIS 4, Deleuze 2014, diapositive
  « Une forte complémentarité des données ») :
  - ~1 106 099 arbres IFN (mesures d'arbres sur pied, D+H)
  - ~118 505 tiges issues de bases R&D des partenaires (ONF, INRA, FCBA...)
  - 6 037 arbres mesurés pour la biomasse, 1 797 pour la minéralomasse
  - 220 arbres échantillonnés lors de 2 campagnes de terrain dédiées (2009-2010)
  - 20 983 mesures de menus-bois, 31 439 profils UR2PI (Congo — hors périmètre France
    métropolitaine, à noter pour ne pas confondre avec la ressource française)
- **Innovation méthodologique clé** (Deleuze et al. 2014, hal-03016051) : introduction de la
  **« hauteur de décrochement »** comme 3ᵉ variable d'entrée (en plus de D et H), une mesure
  propre au protocole de cubage IFN, pour modéliser le **profil de tige** (approximé par une
  distribution conique) et ainsi prédire un volume à n'importe quelle découpe (bois d'œuvre,
  bois d'industrie, bois total) à partir d'un modèle générique par essence — plutôt que
  d'avoir des tarifs séparés par type de découpe.
  - Concept de **coefficient de forme redéfini** intégrant cette 3ᵃ dimension, en
    complément du coefficient de forme classique `f` déjà utilisé dans
    `TarifData.coefsFormeParEssence`.
  - Modèle de **volume total aérien** ajusté par genre/essence (23 essences + regroupement
    feuillus/résineux en cas de données insuffisantes) — liste d'essences visibles sur la
    diapositive CAQSIS : *Acer campestre, Acer pseudoplatanus, Alnus sp., Betula pendula,
    Betula sp., Carpinus betulus, Fagus sylvatica, Fraxinus excelsior, Pinus halepensis,
    Populus tremula, Quercus ilex, Quercus pubescens, Quercus robur/petraea, Robinia
    pseudoacacia, Tilia cordata*, etc.
  - **Coefficients d'expansion** (Longuetaud et al. 2013, *Forest Ecology and Management*,
    repris sur HAL-INRAE hal-02648144) pour déduire différents volumes de branches à partir
    du volume de tige — approche présentée comme plus fine que les coefficients de passage
    fixes traditionnellement utilisés pour estimer le bois énergie de houppier.
- **Coordonnées Lambert 93** : confirmé dans la notice GeoNetwork
  (`Reference system identifier: Lambert 93 (SRID 2154)`), cohérent avec le standard
  géodésique français utilisé ailleurs dans GeoSylva pour les couches cartographiques.

### 2.6 Test réel d'accès aux données EMERGE (geodata.inrae.fr)

**Requête effectuée** : `GET https://geodata.inrae.fr/geonetwork/srv/api/records/27f18f57-b847-4ec3-909b-4067a5e1dc33`
(le 2026-07-02, via outil `webfetch`).

**Résultat obtenu** : code de réponse HTTP 200, page HTML de métadonnées GeoNetwork
(catalogue ISO 19115/19139) rendue avec succès. **Aucun fichier de données (CSV/JSON/
shapefile) n'est exposé par cette page** — c'est une notice de métadonnées, pas un endpoint
de téléchargement. Extraits significatifs de la réponse réelle :

```
Access constraints
  Restricted
Use constraints
  Restricted
Maintenance and update frequency
  Not planned
Status
  Completed
Begin date        Mon Dec 01 00:00:00 CET 2008
End date          Sat Jun 01 00:00:00 CEST 2013
Reference system identifier
  Lambert 93 (SRID 2154)
Digital transfer options
  OnLine resource: Protocol WWW:LINK-1.0-http--link, Linkage: (vide)
```

**Conclusion du test** : la base de données EMERGE **n'est pas en accès libre**. Les
mentions `Access constraints: Restricted` et `Use constraints: Restricted` indiquent
explicitement une restriction d'accès (probablement réservée aux partenaires du projet ou
sur demande auprès du point de contact INRA — Fleur Longuetaud, Frédéric Mothe, Laurent
Saint-André, Philippe Santenoise, Christine Deleuze). Le champ « OnLine resource » est
présent dans le schéma mais **sans lien de téléchargement renseigné** (`Linkage` vide dans
la réponse HTML observée). Il n'y a donc **aucun moyen confirmé, à ce jour, de télécharger
directement les coefficients EMERGE en CSV/JSON pour seeder une base Android**. Ceci diffère
fortement des données brutes de l'IFN elles-mêmes (voir 2.7), qui sont ouvertes.

`[Vérification manuelle recommandée]` : contacter directement l'INRAE/LERFoB (via la page
de contact de la notice, ou hal.inrae.fr) pour connaître les conditions d'accès à un dataset
EMERGE dérivé (modèles publiés dans les articles, ex. hal-03016051, contiennent parfois des
coefficients numériques dans le corps du texte/annexes du PDF — non extractibles par les
outils de cette recherche, cf. limites §5).

### 2.7 Alternative ouverte : DataIFN (données brutes IFN, hors EMERGE)

Indépendamment d'EMERGE, l'IGN publie ses données brutes de terrain de l'inventaire
forestier national sur **https://inventaire-forestier.ign.fr/dataifn/**, sous **Licence
Ouverte / Open Licence Etalab 2.0**, gratuitement, depuis les campagnes annuelles 2005 et
suivantes. Ce sont des données arbre-par-arbre (mesures brutes, pas des coefficients de
tarif pré-calculés), exploitables pour reconstruire/recalibrer des tarifs de cubage
spécifiques (comme le fait le package R `gftools` avec ses fonctions `TarifFindSch` /
`TarifIFNSER`, qui recherchent le numéro de tarif Schaeffer le mieux ajusté à partir des
données IFN autour d'un point ou d'un périmètre donné). C'est une piste plus prometteuse
que EMERGE pour un futur enrichissement basé sur données ouvertes, mais elle demande un
travail de traitement statistique (régression) qui dépasse la simple récupération d'un
fichier de coefficients — voir §4.

---

## 3. Comparatif / analyse critique

| Critère | Schaeffer 1E/2E | Algan | IFN Rapide (1 entrée) | IFN Lent (2 entrées) | EMERGE |
|---|---|---|---|---|---|
| Entrées | C (1E) / C+H (2E) | D+H | D | D+H | D, H, hauteur de décrochement (3 entrées) |
| Nb de tarifs/formules | 16 / 8 | 1 par essence | 36 | 8 | 1 modèle générique par essence/genre |
| Coefficients publiés en accès libre | Oui (littérature pédagogique stable depuis 1949) | Oui (référencé Pardé & Bouchon 1988) | **Non retrouvé sous forme de table chiffrée officielle en accès libre** | **Idem** | **Non — accès restreint confirmé (test 2.6)** |
| Adapté à | Peuplements réguliers (1E) / tous types (2E) | Résineux réguliers (historique), étendu aux feuillus | Peuplement homogène, mesure rapide | Peuplement irrégulier/mélangé, précision | Générique multi-essence, multi-découpe (BO/BI/biomasse) |
| Statut dans GeoSylva | Implémenté, jugé conforme par l'agent précédent | Implémenté, méthode par défaut | Implémenté mais origine des coefficients non tracée avec certitude | Idem | Non implémenté, données non accessibles |

**Point critique commun aux tarifs IFN dans le code actuel** : à la différence des tarifs
Schaeffer et Algan (sourcés dans des références bibliographiques précises et datées),
**aucune source primaire consultée dans cette recherche ne contient le tableau exact des
36+8 coefficients IFN présents dans `TarifData.kt`**. La progression très régulière
(quasi-géométrique) des coefficients d'un numéro à l'autre suggère une génération
algorithmique (probablement une interpolation exponentielle calée sur les tarifs 1 et 36,
à l'image de ce qui existe pour Schaeffer 1E/2E) plutôt qu'une reproduction fidèle d'une
table IFN officielle. Ce n'est pas nécessairement faux au sens mathématique (les tarifs
réels suivent souvent ce type de progression), mais **le niveau de confiance sur ces valeurs
doit être inférieur à celui accordé à Schaeffer/Algan** jusqu'à vérification croisée avec
une publication IFN primaire retrouvée en intégralité.

---

## 4. Recommandation pour GeoSylva

1. **Ne pas modifier `TarifData.kt` sans validation supplémentaire** — cette recherche n'a
   pas permis de confirmer ni d'infirmer avec certitude les 36+8 coefficients IFN actuels ;
   les remplacer sans une source primaire vérifiée serait aussi risqué que de les garder tels
   quels. Statut recommandé : **`VÉRIFIER` reste d'actualité**, mais avec un chemin d'action
   concret ci-dessous (au lieu d'un simple "à vérifier" générique).
2. **Piste d'action la plus fiable à moyen terme** : au lieu de chercher une table de
   coefficients IFN publiée (qui semble ne pas exister en accès libre sous cette forme),
   envisager de recalculer des tarifs par essence/région à partir des données brutes
   ouvertes **DataIFN** (https://inventaire-forestier.ign.fr/dataifn/, Licence Ouverte
   Etalab 2.0). C'est un travail de data science (régression sur échantillon IFN filtré par
   essence/GRECO) qui dépasse le cadre d'une recherche documentaire, mais c'est la seule
   voie identifiée donnant des coefficients à la fois **ouverts**, **traçables** et
   **spécifiques à la ressource française actuelle** — à évaluer comme item de roadmap
   (charge non négligeable : nécessite un pipeline offline, pas embarquable tel quel dans
   l'app Android).
3. **EMERGE : ne pas intégrer directement** — accès restreint confirmé (§2.6). Ne pas
   promettre d'intégration EMERGE dans une communication produit/commerciale avant d'avoir
   obtenu un accord d'accès explicite auprès de l'INRAE/LERFoB. Corriger, le cas échéant, la
   ligne « Recommandation : INTÉGRER » du §1.4 de `REFERENTIELS_FORESTIERS_EXTERNES.md` vers
   quelque chose comme « CONTACTER LES AUTEURS avant toute intégration — accès restreint
   confirmé ».
4. **Documentation utilisateur (`TarifDocumentationScreen` ou équivalent)** : ajouter une
   note pédagogique sur le choix rapide/lent (§2.4) — utile aux utilisateurs professionnels
   de l'app pour choisir la bonne méthode selon la régularité du peuplement, indépendamment
   de la fiabilité des coefficients eux-mêmes.
5. **Fiabiliser en priorité le mapping essence → numéro de tarif** (`essenceToIfnRapideNumero`
   / `essenceToIfnLentNumero`) plutôt que les coefficients bruts : la sensibilité du volume
   calculé au choix du numéro est très supérieure à la sensibilité à la méthode elle-même
   (voir §2.3, écart du simple au double observé sur un même arbre selon numéro choisi).
6. **Priorité** : Moyenne. Les tarifs IFN sont une option secondaire dans l'app (Algan est
   la méthode par défaut selon `REFERENTIELS_FORESTIERS_EXTERNES.md` §1.2) ; ne pas bloquer
   une fonctionnalité utilisateur sur ce point, mais éviter de communiquer les tarifs IFN
   comme "officiels IGN vérifiés" tant que la source primaire n'est pas retrouvée.

---

## 5. Limites et points à vérifier manuellement

- Le PDF de la note IFN de référence (http://hdl.handle.net/2042/42780) a renvoyé une erreur
  HTTP 500 lors de la tentative de récupération du texte intégral (2026-07-02) — seul
  l'extrait indexé par le moteur de recherche a pu être consulté. **À re-tenter manuellement
  ou via un accès direct au PDF hébergé par CTBA/AFOCEL.**
- Plusieurs PDF sources (CAQSIS 4, hal-02648144, foret-mediterraneenne.org) n'ont pu être
  extraits en texte lisible par l'outil de récupération web utilisé (retour de flux binaire
  brut) — le contenu cité dans ce document provient donc des **extraits indexés par le
  moteur de recherche** (snippets), pas d'une lecture intégrale du PDF. `[À VÉRIFIER
  MANUELLEMENT en téléchargeant et lisant les PDF directement]`.
- L'interprétation du sens exact de « rapide »/« lent » pour la nomenclature IFN (§2.4,
  point 2 des deux interprétations) reste une hypothèse de recoupement de plusieurs sources
  indirectes, pas une confirmation directe par un document IFN qui définirait explicitement
  ces deux termes pour ses propres tarifs (par opposition aux tarifs Schaeffer historiques,
  pour lesquels la définition est bien sourcée par Batias 1958).
- L'origine exacte des 36+8 coefficients dans `TarifData.kt` (créés par un développeur/agent
  précédent) n'a pas pu être tracée avec certitude ; il est possible qu'ils aient été
  générés par interpolation plutôt que copiés d'une source. Une revue de l'historique Git du
  fichier (`git log -p -- app/.../TarifData.kt`) permettrait de savoir si un commit
  antérieur cite une source précise — action recommandée pour l'équipe de développement,
  hors périmètre de cet agent de recherche documentaire.
- Les calculs de comparaison chiffrée du §2.3 sont des calculs manuels de vérification
  effectués par l'IA à partir des formules du code ; ils n'ont pas été recoupés avec une
  mesure de terrain réelle ni avec un tarif IFN officiel externe (faute d'accès à une telle
  table) — à considérer comme illustratif de la sensibilité du modèle, pas comme validation
  d'exactitude.
- Le statut d'accès EMERGE (« Restricted ») a été vérifié une seule fois (2026-07-02) ; les
  conditions d'accès peuvent évoluer (ex. dépôt ultérieur des données sur un entrepôt
  ouvert) — à re-vérifier périodiquement, notamment si un besoin produit concret émerge.
