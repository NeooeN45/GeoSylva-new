# INDEX — Base de connaissances recherche GeoSylva

Voir `_METHODOLOGIE.md` pour les règles de production de ces documents.

## 1. Cubage / Volume (01_cubage_volume/)
_(à compléter par les agents de la vague 1)_

- **`01_cubage_volume/02_tarifs_ifn_emerge.md`** — Statut : brouillon (vérifié pour la partie
  « test d'accès EMERGE », non vérifié pour les coefficients IFN eux-mêmes).
  3 points clés :
  1. Les 36 tarifs IFN Rapide + 8 tarifs IFN Lent de `TarifData.kt` ne sont adossés à aucune
     source primaire retrouvée en accès libre (contrairement à Schaeffer/Algan) — leur
     progression quasi-géométrique suggère une génération algorithmique à vérifier.
  2. Test réel effectué le 2026-07-02 sur l'URL de données EMERGE
     (geodata.inrae.fr/.../27f18f57-...) : accès **Restricted/Restricted** confirmé (HTTP 200
     mais notice de métadonnées uniquement, pas de fichier téléchargeable) — ne pas intégrer
     EMERGE tel quel, contacter l'INRAE/LERFoB si besoin produit réel.
  3. Alternative ouverte identifiée : DataIFN (données brutes IFN, Licence Ouverte Etalab 2.0,
     https://inventaire-forestier.ign.fr/dataifn/) permettrait de recalculer des tarifs
     traçables, mais nécessite un travail de data science hors périmètre de cette recherche.

- **Fichier** : `01_cubage_volume/01_tarifs_schaeffer_algan.md`
  **Statut** : brouillon (sources scientifiques/pédagogiques tierces recoupées, tables ONF/IGN
  officielles papier non consultées — vérification manuelle requise)
  **3 points clés** :
  1. Formule sourcée et vérifiée des tarifs Schaeffer/Algan 1 entrée : `V=(5/70000)×(8+num)×(D-5)×(D-10)`
     (D en cm), avec 4 familles (rapide/intermédiaire/lent/très lent) — diffère structurellement de
     `V=a+b×C²` codé dans `TarifData.kt` (écart numérique constaté jusqu'à +142 % sur un cas test).
  2. Les tarifs Algan historiques ne sont **pas** essence-spécifiques ; les coefficients
     `TarifData.alganCoefs` (par essence, forme `a×D^b×H^c`) ressemblent plutôt aux équations
     nationales de volume par essence (Vallet et al. 2006) — attribution "Algan (1958)" à vérifier.
  3. Guide de décision Schaeffer 1E/2E/Algan selon peuplement régulier/irrégulier, disponibilité de
     la hauteur, et usage (terrain rapide vs inventaire/expertise de précision).

- **`03_coefficients_forme_biomasse.md`** — Statut : brouillon (vérifié partiellement — tableau
  chiffré Pardé & Bouchon 1988 non extrait faute d'accès au PDF exploitable, cf. limites).
  3 points clés :
  1. La bibliothèque **LERFoB Forest Tools** existe réellement (https://github.com/CWFC-CCFB/lerfobforesttools,
     LGPL-3.0) et sa classe `FrenchCommercialVolume2020Predictor` existe aussi (pas une hallucination) ;
     mais elle dépend lourdement de `javax.swing`/`java.awt` (via `repicea.gui`) et d'autres briques
     desktop (Access, jaxb, jfreechart) → **non intégrable directement dans l'APK Android** (blocage
     technique, pas seulement de licence).
  2. **LGPL-3.0 n'interdit pas l'usage commercial** (contrairement à GPL/AGPL) et n'oblige pas à
     ouvrir le code de GeoSylva ; le risque réel se limiterait à un fork/modification de la lib
     elle-même. Recommandation : **réimplémenter les formules en Kotlin natif** plutôt qu'importer
     le jar, ce qui évite à la fois le blocage AWT et l'ambiguïté LGPL/linkage statique mobile.
  3. Le modèle le plus récent réellement utilisé par les professionnels français pour
     volume/biomasse par essence est le **projet EMERGE** (Vallet et al. 2006, Deleuze et al. 2013),
     postérieur à Pardé & Bouchon 1988 ; pour le carbone, la référence officielle reste
     CARBOFOR (2004) + méthodologie IGN 2021 (FER = 1.30 conifères / 1.28 feuillus, carbone = 47.5 %
     de la biomasse sèche).

- **`04_tables_production_croissance.md`** — statut : brouillon (recherche documentaire, valeurs
  numériques des tables non revérifiées contre l'ouvrage source payant/indisponible en ligne).
  3 points clés :
  1. Le bug signalé « Hm au lieu de Hdom » dans `ExpertForestryCalculator.kt` est **partiellement
     infirmé** sur le calcul brut de Hdom (`computeHdom()` est correct : moyenne des 100 plus gros
     arbres/ha) mais **confirmé sous une autre forme** : le champ `ProductionData.hauteurMoyenne`
     contient en réalité des valeurs de Hdom (tables Décourt & Pardé) mais est comparé à une vraie
     Hm terrain dans `EnhancedForestryCalculator.kt` (ligne 86, `hauteurConforme`), faussant le
     diagnostic de conformité ONF affiché à l'utilisateur.
  2. L'indice de station (`calculateIndiceDeStation()`) reste une approximation IS ≈ Hdom courante
     (âge ignoré, `@Suppress("UNUSED_PARAMETER")`) — limite déjà documentée dans le code, pas un
     bug caché, mais son impact (sous-estimation potentielle de la fertilité) mérite d'être remonté
     dans l'UI utilisateur, pas seulement en commentaire Kotlin.
  3. Aucun modèle de croissance dynamique français (CAPSIS, Fagacées, SimCoP Douglas, Forêts-21)
     n'expose d'API publique ni de licence open-source claire permettant une intégration mobile
     directe à court terme — rester sur les tables statiques Décourt & Pardé, en les complétant si
     possible par les faisceaux de courbes Hdom~âge (Duplat & Tran-Ha 1997 chêne, Duplat et al.
     hêtre) pour un vrai indice de fertilité.

- **Fichier** : `01_cubage_volume/05_normes_qualite_bois.md`
  **Statut** : brouillon (texte intégral des normes NF EN 1316/1927 non consulté — accès payant AFNOR)
  **3 points clés** :
  1. NF EN 1316-1 (chêne/hêtre) et NF EN 1927-1/2 (résineux) fixent des **classes qualitatives A-D**
     (codes normalisés Q-A…Q-D, F-A…F-D selon essence), mais **aucune norme ne fixe de coefficient de
     prix** — les multiplicateurs ×2.5/×1.5/×1.0/×0.4 de GeoSylva sont des estimations de marché, pas
     des valeurs normatives ; à reformuler dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.5.
  2. L'architecture GeoSylva (`WoodQualityGrade` + `WoodDefect` + coefficients par essence dans
     `PriceCalculator`) est structurellement alignée avec l'esprit des normes (classes + catalogue de
     singularités NF EN 1310), mais une **incohérence de ratio A/D** existe entre le multiplicateur
     générique `WoodQualityGrade` (6.25) et le wildcard `PriceCalculator["*"]` (3.27) pour les
     essences non listées spécifiquement — à harmoniser.
  3. Comparé aux ordres de grandeur de marché documentés (chêne 50-450 €/m³, jusqu'à 2 500 €/m³
     d'exception ; FBF moyenne 2024 ~228 €/m³), le ratio A/D générique de GeoSylva est plausible pour
     une essence « moyenne » mais surestimé pour le Douglas/résineux courants (marché ≈×3 vs ×6.25
     générique) et potentiellement sous-estimé pour les feuillus précieux d'exception.

## 2. Marché / Prix (02_marche_prix/)
_(à compléter par les agents de la vague 2)_

- **`02_marche_prix/01_prix_national_fbf.md`** — Statut : brouillon (sources officielles FBF/CNPF/La
  Forestière recoupées ; PDF officiels illisibles en extraction automatique — tableau détaillé par
  essence pour 2025 non extrait, vérification manuelle requise).
  **3 points clés** :
  1. L'indicateur FBF (co-produit La Forestière/ASFFOR/EFF, panel = ventes groupées d'Experts
     Forestiers de France, ~60 ventes/an, ~3000 lots, €/m³ sur pied HT) donne un prix moyen toutes
     essences de **90 €/m³ en 2024** (+7 %) puis **86 €/m³ en 2025** (-4 %) ; les résineux atteignent
     en 2025 leur plus haut niveau depuis la création de l'indice (2004), avec 4 essences (douglas,
     épicéa commun, pin laricio, pin sylvestre) à leur record historique, tandis que les feuillus
     (chêne notamment) reculent.
  2. Écarts significatifs détectés entre les prix FBF 2024 par essence (chêne 228 €/m³, douglas
     89 €/m³, pin maritime 56 €/m³, peuplier 73 €/m³, châtaignier 119 €/m³, frêne 158 €/m³, hêtre
     56 €/m³) et les valeurs codées en dur dans `RegionalPricePresets.kt` (qualité C, sur pied) :
     sous-évaluation de -19 % (douglas) à -60 % (chêne), à l'exception du hêtre (+7 %, cohérent) —
     à recalibrer en priorité pour douglas/pin maritime/chêne.
  3. Le libellé `YEAR = 2025` dans `RegionalPricePresets.kt` semble confondre le numéro d'édition de
     l'indicateur FBF avec l'année réelle des prix constatés (l'« indicateur 2025 » porte sur les
     prix 2024) — à corriger ; la comparaison chiffrée reste en outre limitée par l'absence de
     ventilation qualité A/B/C/D connue du panel FBF (prix moyen probablement tiré vers le haut par
     des lots de qualité supérieure, non strictement comparable à une « qualité C »).

- **`02_marche_prix/03_onf_cooperatives_bois_energie.md`** — Statut : brouillon (sources
  officielles/tierces recoupées par recherche web + un test d'API réel réussi ; PDF ONF non lus
  en détail, cf. limites du document).
  3 points clés :
  1. **ventesdebois.onf.fr** est une Single Page Application (JS, hash-routing `/vel/#/...`) sans
     API/export CSV/JSON identifié ; un test réel a renvoyé HTTP 401 sur `robots.txt` et un
     contenu vide (`Loading...`) sur les pages — scraping **non recommandé** (faisabilité
     technique faible + risque légal/CGU non maîtrisé) ; l'indice ONF trimestriel reste
     accessible uniquement en **PDF** via l'Observatoire France Bois Forêt.
  2. **Les coopératives forestières** (UCFF/12 coopératives, Alliance Forêts Bois, UNISYLVA,
     Coforêt, Forestarn...) ne publient **aucun indicateur de prix chiffré public** — uniquement
     des services d'estimation individualisés ; à écarter comme source de données de pricing.
  3. **Découverte clé** : l'**ADEME publie un dataset ouvert avec une vraie API JSON sans clé**
     (`https://data.ademe.fr/data-fair/api/v1/datasets/prix-bois-domestique/lines`, licence
     Etalab, testée avec succès le 2026-07-02, série 2005-2024, prix bûches/granulés en €/MWh
     PCI) — c'est la meilleure source technique de ce document pour un futur module « bois de
     chauffage », à condition de sourcer un facteur de conversion €/MWh↔€/stère avant affichage.

- **`02_marche_prix/05_marche_carbone_forestier.md`** — Statut : brouillon (sources officielles LBC
  et étude sectorielle InfoCC/I4CE recoupées ; prix de marché non "cotés", détail des rabais LBC
  et facteurs ADEME Base Carbone bois non extraits intégralement — vérification manuelle requise).
  3 points clés :
  1. Le Label Bas-Carbone compte **4 méthodes forestières** (pas 2) : Boisement, Reconstitution de
     peuplements dégradés, **Balivage** (conversion taillis feuillu → futaie sur souches,
     10-30 ans, feuillus uniquement) et Gestion Forestière à Stock Continu (GFSC) — Balivage/GFSC
     sont les plus pertinentes pour un propriétaire *forêt existante* déjà inventoriée avec
     GeoSylva, contrairement à Boisement qui suppose un terrain non boisé depuis 10 ans.
  2. Prix du crédit carbone LBC forestier : **20 à 70 €/tCO2e** (I4CE, bilan 2025), moyenne LBC
     toutes méthodes confondues ≈31-35 €/tCO2e — **3 à 4× plus élevé** que les standards
     internationaux (Verra/Gold Standard ≈5-8 €/t) ; **aucun prix de marché "coté"**, tout se
     négocie de gré à gré (le ministère confirme une fourchette officielle de 8 à 125 €/tCO2e).
  3. La physique du calcul volume→carbone est déjà sourcée dans
     `01_cubage_volume/03_coefficients_forme_biomasse.md` (formule CARBOFOR/IGN) — le LBC n'ajoute
     pas de formule alternative, seulement un calcul **différentiel** (projet − scénario de
     référence) non capturable par un simple inventaire ponctuel GeoSylva. Une fonctionnalité
     "ordre de grandeur carbone" (non contractuelle) est faisable à court terme ; un vrai
     simulateur de dossier LBC ne l'est pas (nécessite simulation de croissance à 30 ans absente
     de l'app).

- **`02_marche_prix/04_valeur_fonciere_forestiere.md`** — Statut : brouillon (sources officielles
  SAFER/Cerema recoupées, moyenne SAFER contestée par un tiers non revérifiée officiellement).
  3 points clés :
  1. La SAFER publie chaque année (étude « Le prix des terres », gratuite, sans clé) un prix moyen
     national des forêts (**4 850 €/ha en 2024, +2,2 %**, 90 % des ventes entre 730 et 14 570 €/ha),
     mais uniquement à la granularité de **7 grandes régions forestières** — pas de commune/
     département comme pour les terres agricoles, et pas d'API/fichier structuré identifié.
  2. Cette moyenne SAFER est **activement contestée** par un expert forestier (Pierre Aussedat) qui
     calcule une moyenne brute à **13 585 €/ha** (montant total/surface totale, ×2,8 le chiffre
     officiel) — écart non expliqué en détail par la SAFER dans le document public consulté
     `[À VÉRIFIER MANUELLEMENT]` : tout affichage GeoSylva doit montrer une fourchette, jamais un
     chiffre unique. Aucun « indice Fransylva » chiffré et public n'a été trouvé (lacune confirmée).
  3. Le foncier forestier **est identifiable dans DVF géolocalisées** (open data gratuit,
     data.gouv.fr, Licence Ouverte 2.0) via le champ `nature_culture` / codes DGFiP B/BF/BM/BO/BP/
     BR/BS/BT (classe "Bois") — donnée brute exploitable à la parcelle, contrairement au modèle
     enrichi DVF+/DV3F qui reste réservé aux organismes à mission de service public (non accessible
     à GeoSylva en tant qu'entreprise privée).

- **`02_marche_prix/02_prix_regionaux.md`** — Statut : brouillon (recherche web multi-sources,
  aucun PDF officiel FBF/CNPF téléchargé et lu intégralement — vérification manuelle requise avant
  intégration produit).
  3 points clés :
  1. Correction factuelle : `RegionalPricePresets.kt` contient en réalité **13 presets organisés par
     GRECO** (NATIONAL + 12 zones écologiques IGN A-L), pas "7 presets régionaux" comme indiqué dans
     la commande initiale — le système de prix de l'app est structuré par région écologique, pas par
     région administrative.
  2. Sur les 13 régions administratives françaises, seules **Bretagne, Pays de la Loire (délégation
     CNPF fusionnée), Nouvelle-Aquitaine et Occitanie (via Fibois Observabois)** disposent d'un
     observatoire régional de prix du bois sur pied exploitable en accès libre ; le Grand Est a un
     statut incertain (observatoire annoncé "à venir" en 2018, non revérifié) ; **7 régions
     (Auvergne-Rhône-Alpes, PACA, Bourgogne-Franche-Comté, Centre-Val de Loire, Île-de-France,
     Normandie, Corse) n'ont aucun observatoire de prix régional identifié en accès libre.**
  3. Piège identifié : plusieurs "observatoires" Fibois régionaux trouvés (AURA, Bourgogne-Franche-
     Comté, Normandie, Île-de-France, Grand Est) ne portent PAS sur le prix du bois sur pied mais sur
     le bois énergie/bûche, le coût des machines forestières, ou la construction bois — à ne pas
     confondre lors d'une future intégration.

## 3. Climat (03_climat/)
_(à compléter par les agents de la vague 3)_

## 4. Sol / RHU (04_sol_rhu/)
_(à compléter par les agents de la vague 4)_

## 5. APIs externes (05_apis_externes/)
_(à compléter par les agents de la vague 5)_

## 6. Essences (06_essences/)
_(à compléter par les agents des vagues 6-8)_

## 7. Systèmes avancés (07_systemes_avances/)
_(à compléter par les agents de la vague 9)_
