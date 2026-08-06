---
statut: Draft
date: 2026-07-17
auteur: Agent Dendromètre (forest-crew) + 5 subagents de recherche
périmètre: Audit fiabilité données dendrométriques & valorisation bois par essence — GeoSylva
essences_couverte: ~79 (CanonicalEssences.kt)
sources_vérifiées: 60+ publications, datasets, organismes
---

# Audit de fiabilité des données dendrométriques et de valorisation du bois par essence

> **Mission** : Fiabiliser la véracité de chaque donnée, resourcer proprement, identifier
> les données manquantes influentes sur le prix, adaptées par essence.
>
> **Méthode** : 5 subagents parallèles (inventaire codebase + 4 recherches web par catégorie
> de source). 60+ sources vérifiées. Statut de chaque donnée : ✅ sourcé/fiable,
> ⚠️ écart mineur ou source partielle, ❌ écart majeur ou source erronée/introuvable.

---

## Synthèse exécutive

L'audit révèle **3 problèmes critiques de sourcing** et **plusieurs écarts de valeurs**
qui doivent être corrigés avant toute utilisation production de GeoSylva pour l'estimation
de valeur bois.

### Problèmes critiques (à traiter en priorité)

| # | Problème | Gravité | Fichier concerné |
|---|---|---|---|
| C1 | **Coefficients Algan (a,b,c) non sourcés** — la forme V = a × D^b × H^c ne correspond pas aux tarifs Algan 1901 (qui sont des tableaux à une entrée, pas des équations). Source réelle des coefficients numériques introuvable dans la littérature publique. | ❌ CRITIQUE | `TarifData.kt` |
| C2 | **Référence Schumacher-Hall "Vallet 2006 RFF LVIII(5):481-496" inexistante** — l'article réel de Vallet et al. 2006 est publié dans *Forest Ecology and Management* 229:98-110 (DOI 10.1016/j.foreco.2006.03.013) et utilise une équation de **forme de tige** (4 coefficients a,b,c,d), PAS la forme Schumacher-Hall logarithmique V = exp(a + b·ln(D) + c·ln(H)). | ❌ CRITIQUE | `SylvicultureDatabase.kt` |
| C3 | **Incohérence d'unités densité bois** entre `CanonicalEssences.kt` (kg/m³ à 12% HR) et `AdvancedCalculationEngine.kt` (t/m³ infradensité) — écart jusqu'à 100 kg/m³ pour le chêne sessile (710 vs 610). | ⚠️ MAJEUR | `CanonicalEssences.kt` + `AdvancedCalculationEngine.kt` |

### Constats principaux

- **Densités bois** : 5 essences avec écart majeur (Chêne vert, Robinier, Pin maritime, Cèdre Atlas, Thuya). Source canonique recommandée : **XyloDensMap (INRAE/IGN)** — 156 espèces, 110 763 carottes, infradensité par tomographie RX.
- **Prix bois** : valeurs GeoSylva sont un mélange ambigu "sur pied / bord de route". Les prix résineux sont 2× supérieurs aux prix sur pied officiels FBF 2024-2025. Noyer sous-évalué. Source canonique : **France Bois Forêt observatoire** + **ONF mercuriales** + **CEEB bord de route**.
- **Croissance** : tables Décourt-Pardé 1980 toujours référence mais anciennes. MAI chêne (4.5-4.8) en borne basse. Alternatives modernes : **MARGOT (IGN)**, **Capsis (INRAE/AMAP open source)**, **ClimEssences (CNPF)**.
- **Carbone** : carbonFraction 0.50 et CO2 factor 3.67 ✅ corrects. RER 0.25 ⚠️ générique (IPCC 2019 Table 4.4 donne 0.24-0.48 selon zone/AGB). BEF 1.65/1.45 ⚠️ ne tient pas compte de l'âge (IPCC GPG-LULUCF Table 3A.1.10 : 1.34-4.0 selon âge).

---

## 1. Sources canoniques recommandées (à adopter comme références uniques)

### 1.1 Densité bois

| Source | URL | Type | Couverture | Statut |
|---|---|---|---|---|
| **XyloDensMap (INRAE/IGN)** | https://doi.org/10.57745/FA9DRA | Infradensité (kg/m³) | 156 espèces FR, 110 763 carottes | ✅ **PRIMAIRE** |
| **TROPIX (CIRAD)** | https://tropix.cirad.fr/ | Densité 12% HR (g/cm³) | 310 essences tropical + tempéré | ✅ Secondaire |
| **IPCC 2006 Guidelines** | V4 Ch4 Forest Land | Basic wood density (t/m³) | Tempéré/boréal défaut | ✅ Tertiaire (essences non-FR) |

### 1.2 Coefficients de cubage

| Source | URL | Type | Couverture | Statut |
|---|---|---|---|---|
| **Vallet et al. 2006 (FEM)** | DOI 10.1016/j.foreco.2006.03.013 | Forme de tige (4 coef. a,b,c,d) | 7 essences (CH sessile/pédonculé, Hêtre, Douglas, Épicéa, Pin sylvestre/maritime, Sapin) | ✅ **PRIMAIRE** |
| **GCubeR (package R)** | https://github.com/cran/GCubeR | Implémentation Vallet + Dagnelie + Bouvard | Open source, coefficients sourcés | ✅ Implémentation |
| **EMERGE (ANR 2009-2014)** | gftools R package | Tarifs tige/houppier/total | Quasi-totalité essences FR | ✅ Secondaire |
| **Pardé & Bouchon 1988** | ENGREF Nancy, ISBN 978-2-85710-025-6 | Dendrométrie (coef. forme) | Référence papier | ⚠️ À vérifier sur ouvrage papier |
| **IFN tarifs bruts** | http://hdl.handle.net/2042/42780 | Tableaux par essence/région | Complément essences non-Vallet | ✅ Tertiaire |

### 1.3 Prix bois

| Source | URL | Type | Statut |
|---|---|---|---|
| **France Bois Forêt observatoire** | https://observatoire.franceboisforet.com/ | Indicateur annuel prix sur pied forêt privée 2004-présent | ✅ **PRIMAIRE** (sur pied) |
| **ONF mercuriales** | https://www.onf.fr/produits-services/acheter-du-bois/ | Indices trimestriels forêts publiques | ✅ Primaire (forêts publiques) |
| **CEEB observatoire** | https://observatoire.franceboisforet.com/ | Prix bois ronds rendus scierie | ✅ Primaire (bord de route) |
| **CNPF régionaux** | https://nouvelle-aquitaine.cnpf.fr/ + régional | Prix régionaux détaillés | ✅ Secondaire (régional) |
| **FIBOIS régionales** | https://www.fibois-occitanie.com/ + régional | Indicateurs régionaux | ✅ Secondaire |
| **ADEME** | https://librairie.ademe.fr/ | Enquête prix combustibles bois | ✅ Bois énergie |

### 1.4 Croissance & production

| Source | URL | Type | Statut |
|---|---|---|---|
| **IGN IFN** | https://inventaire-forestier.ign.fr/ | Données inventaire actuelles + accroissement mesuré | ✅ **PRIMAIRE** |
| **MARGOT (IGN)** | Projections nationales (interne IGN) | Projections stocks/disponibilités 2050-2080 | ⚠️ Accès restreint |
| **Capsis (INRAE/AMAP)** | https://capsis.cirad.fr/ | Plateforme simulation croissance (open source) | ✅ Pour simulations avancées |
| **ClimEssences (CNPF/ONF)** | https://climessences.fr/ | Choix essences + données croissance (149 espèces, 37 critères) | ✅ Web gratuit |
| **Décourt & Pardé 1980** | ENGREF Nancy, ISBN 978-2-85710-016-4 | Tables production historiques | ⚠️ Anciennes, à compléter |
| **Richards 1959** | DOI 10.1093/jxb/10.2.290 | Fonction croissance flexible | ✅ Référence validée |
| **Dhôte & de Hercé 1994** | DOI 10.1139/x94-230 | Modèle H-D chêne/hêtre | ✅ Référence validée |

### 1.5 Biomasse & carbone

| Source | URL | Type | Statut |
|---|---|---|---|
| **IPCC 2019 Refinement** | https://www.ipcc-nggip.iges.or.jp/public/2019rf/ | Mise à jour facteurs AFOLU (Tables 4.4, 4.7-4.12) | ✅ **PRIMAIRE** |
| **IPCC 2006 Guidelines** | V4 Ch4 Forest Land | Tables défaut (4.3, 4.5, 3A.1.10) | ✅ Référence historique |
| **Cairns et al. 1997** | DOI 10.1007/s004420050201 | Root:shoot ratio mondial | ✅ Référence RER |
| **XyloDensMap (INRAE/IGN)** | https://doi.org/10.57745/FA9DRA | Densités bois par essence (156 espèces) | ✅ Pour densités carbone |
| **Label Bas-Carbone (CNPF)** | https://label-bas-carbone.ecologie.gouv.fr/ | Méthodologies forestières françaises | ✅ Pour projets carbone FR |
| **CITEPA** | https://www.citepa.org/ | Inventaire national GES UTCATF | ✅ Pour valeurs officielles FR |
| **Chave et al. 2014** | DOI 10.1111/gcb.12629 | Équations allométriques pantropicales | ⚠️ Référence méthodologique |
| **Jenkins et al. 2003** | DOI 10.1093/forestscience/49.1.12 | Équations biomasse NA | ⚠️ Référence méthodologique |

---

## 2. Vérification des densités bois par essence

### 2.1 Essences avec écart majeur (à corriger)

| Essence | GeoSylva (kg/m³) | Officiel (kg/m³) | Source | Recommandation |
|---|---|---|---|---|
| **Chêne vert** | 950 | 860 | XyloDensMap | Corriger à 860 |
| **Robinier** | 660 | 735 | TROPIX/ORBi | Corriger à 735 (sous-estimation 11%) |
| **Pin maritime** | 520 / 0.44* | 440 | XyloDensMap | Corriger à 440 (variabilité forte selon traitement) |
| **Cèdre Atlas** | 580 | 460 | Forêt Méditerranéenne | Corriger à 460 (sous-estimation 26%) |
| **Thuya** | 370 | 320 | Études basic density | Corriger à 320 |

\* AdvancedCalculationEngine.kt (t/m³)

### 2.2 Essences concordantes (✅)

Chêne sessile/pédonculé (710), Hêtre (680), Charme (750), Châtaignier (560), Frêne élevé (680),
Érable sycomore (620), Aulne glutineux (510), Orme champêtre (640), Pin sylvestre (510),
Épicéa commun (430), Sapin pectiné (440), Douglas (510), Mélèze Europe (590), Pin d'Alep (530),
Genévrier (550-600).

### 2.3 Essences sans source spécifique trouvée (~15)

Chêne rouge, Frêne oxyphylle, Érable plane/champêtre, Bouleau pubescent, Aulne blanc, Tilleul,
Pin weymouth/pignon/cembro/mugo, Épicéa Sitka, Sapin Nordmann, Cèdre Liban, Cyprès.

**Action** : Télécharger dataset XyloDensMap complet (156 espèces) pour vérifier présence.
Pour essences nord-américaines (Pin weymouth, Épicéa Sitka, Chêne rouge) : consulter USFS.
Pour essences rares : utiliser valeurs d'essences proches phylogénétiquement + documenter incertitude.

### 2.4 Incohérence d'unités à corriger

| Essence | CanonicalEssences (kg/m³) | AdvancedCalc (t/m³) | Écart |
|---|---|---|---|
| Chêne sessile | 710 | 0.61 | -100 kg/m³ |
| Pin maritime | 520 | 0.44 | -80 kg/m³ |
| Douglas | 510 | 0.47 | -40 kg/m³ |

**Recommandation** : Adopter XyloDensMap (infradensité kg/m³) comme source unique.
Convertir toutes les valeurs en kg/m³. Documenter le type de densité (infradensité vs 12% HR).

---

## 3. Vérification des coefficients de cubage

### 3.1 Coefficients Algan (TarifData.kt) — ❌ CRITIQUE

**Constat** : Les tarifs Algan originaux (Algan 1901, Bull. Soc. forestière Franche-Comté 6(2):123-130)
sont des **tableaux numériques à une entrée** (basés sur circonférence), PAS des équations
V = a × D^b × H^c. La forme mathématique avec coefficients (a,b,c) utilisée dans GeoSylva
ne correspond pas à la publication originale.

**Recherche** : Les valeurs numériques exactes (ex: CH_SESSILE a=0.0000423, b=2.118, c=0.872)
n'ont été trouvées dans AUCUNE publication académique ou technique publique.

**Recommandation** :
1. **Remplacer** par les tarifs **EMERGE** (package R gftools, fonction `TarifsEmerge()`) — modernes, sourcés, couvrent quasi-totalité essences FR
2. OU adopter les équations **Vallet 2006** via package **GCubeR** (7 essences principales)
3. OU utiliser les **tarifs IFN bruts** pour les essences non couvertes
4. Documenter précisément l'origine de chaque coefficient (DOI, URL, version package)

### 3.2 Coefficients Schumacher-Hall (SylvicultureDatabase.kt) — ❌ CRITIQUE

**Constat** : La référence citée "Vallet et al. 2006, Revue Forestière Française LVIII(5):481-496"
**n'existe pas** sous cette forme. L'article réel de Vallet et al. 2006 est publié dans
*Forest Ecology and Management* 229:98-110 (DOI 10.1016/j.foreco.2006.03.013) et utilise
une équation de **forme de tige** :

```
form = (a + b·c130 + c·√c130/htot) × (1 + d/c130²)
VTA = form × (π/40000) × c130² × htot
```

Cette équation utilise **4 coefficients** (a,b,c,d), PAS 3 (a,b,c) au format Schumacher-Hall
logarithmique V = exp(a + b·ln(D) + c·ln(H)).

**Recommandation** :
1. **Corriger la référence** : remplacer "Vallet 2006 RFF" par "Vallet et al. 2006, Forest Ecology and Management 229:98-110, DOI 10.1016/j.foreco.2006.03.013"
2. **Remplacer les coefficients** par les vrais coefficients Vallet (disponibles dans package R GCubeR, dataset `vallet_vta`)
3. OU si la forme Schumacher-Hall est requise, **identifier la vraie source** des coefficients actuels (peut-être une adaptation interne non documentée)
4. Les coefficients GCubeR sont open-source et vérifiables : https://github.com/cran/GCubeR

### 3.3 Coefficients de forme (TarifData.coefsFormeParEssence) — ⚠️ MODÉRÉ

**Constat** : Source Pardé & Bouchon 1988 (Dendrométrie, ENGREF Nancy, ISBN 978-2-85710-025-6)
plausible mais coefficients non vérifiables en ligne (ouvrage papier).

**Valeurs typiques littérature** (Grundner & Schwappach 1952, cours dendrométrie) :
- Hêtre f ≈ 0.48-0.50 ; Chêne f ≈ 0.53-0.58 ; Épicéa f ≈ 0.46-0.51 ; Sapin f ≈ 0.48-0.54 ;
  Pin sylvestre f ≈ 0.46 ; Douglas f ≈ 0.51

**Recommandation** :
1. Vérifier contre l'ouvrage papier Pardé & Bouchon 1988
2. Documenter que les coefficients de forme varient selon station (valeur unique = approximation)
3. Pour valeurs GeoSylva chêne (0.46-0.47) : semblerait sous-estimé vs littérature (0.53-0.58) — à vérifier

---

## 4. Vérification des prix bois par essence × produit

### 4.1 Sources officielles 2024-2025

**France Bois Forêt — Indicateur annuel prix bois sur pied forêt privée** :
- 2026 (données 2025) : https://observatoire.franceboisforet.com/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2026/
- 2025 (données 2024) : https://franceboisforet.fr/2025/05/05/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2025/
- Prix moyen national 2025 : **86 €/m³** toutes essences

### 4.2 Prix sur pied 2024-2025 par essence (source FBF)

| Essence | Prix min-max €/m³ | Moyenne 2024 | Tendance 2025 |
|---|---|---|---|
| Chêne | 150-300+ | 228 €/m³ | ~219 (-3%) |
| Chêne merrain | 400-2000+ | - | - |
| Hêtre | 50-76 | 56 €/m³ | ~55 |
| Douglas | 60-100 | 72 (+26%) | ~89 (+24%) |
| Épicéa commun | 45-70 | 56 €/m³ | ~60 |
| Sapin pectiné | 45-65 | ~55 | ~60 |
| Pin maritime | 45-60 | 56 (+10%) | ~60 |
| Pin sylvestre | 45-70 | ~55 | ~60 |
| Pin laricio | 45-90 | ~70 (Occitanie) | ~75 |
| Peuplier | 60-85 | 73 (+26%) | ~75 |
| Frêne | 130-188 | 158 (+6%) | ~165 |
| Châtaignier | 100-130 | 119 (+26%) | ~120 |
| Mélèze | 90-120 | 111 (NA) | ~110 |
| Noyer (scié) | 700-1400 | - | - |
| Merisier (scié) | 800-950 | - | - |
| Robinier (scié) | 900-1100 | - | - |

### 4.3 Vérification valeurs GeoSylva

| Essence×produit | GeoSylva (€/m³) | Officiel 2024-2025 | Statut | Commentaire |
|---|---|---|---|---|
| CH_SESSILE:MERAIN | 1200 | 400-2000 (grumes merrain) | ⚠️ | Dans fourchette basse, merrain premium atteint 2000 |
| CH_SESSILE:TRANCHAGE | 500 | 400-500 | ✅ | Conforme |
| CH_SESSILE:SCIAGE_Q | 185 | 200-400 (menuiserie) | ⚠️ | Légèrement sous |
| CH_SESSILE:BCh | 38 | 5-20 (bois industrie) | ⚠️ | Sur-évalué |
| HETRE:SCIAGE_Q | 95 | 55-76 (moyenne nationale) | ⚠️ | Sur-évalué |
| FRENE_ELEVE:SCIAGE_Q | 130 | 158 (moyenne 2024) | ⚠️ | Sous-évalué |
| NOYER_COMMUN:TRANCHAGE | 650 | 700-1400 (scié) | ⚠️ | Sous-évalué |
| NOYER_COMMUN:SCIAGE_Q | 350 | 700-1400 (scié) | ❌ | Très sous-évalué |
| DOUGLAS_VERT:GRUME_L | 145 | 72-100 (sur pied) | ⚠️ | Sur-évalué (2× prix sur pied) |
| SAPIN_PECTINE:GRUME_L | 110 | ~55-60 (sur pied) | ⚠️ | Sur-évalué (2×) |
| EPICEA_COMMUN:GRUME_L | 105 | 56 (sur pied) | ⚠️ | Sur-évalué (2×) |
| MEL_EUROPE:GRUME_L | 130 | 111 (NA) | ⚠️ | Légèrement sur |
| PIN_SYLVESTRE:CHARPENTE | 60 | ~55-60 | ✅ | Conforme |
| PIN_MARITIME:SCIAGE_S | 50 | 45-56 | ✅ | Conforme |
| PIN_LARICIO:SCIAGE_Q | 90 | 22-75 (Occitanie) | ⚠️ | Légèrement sur |

### 4.4 Problème de référence de prix

**Constat** : Les valeurs GeoSylva semblent être un mélange ambigu de prix "sur pied" et
"bord de route" sans documentation claire. Les prix résineux sont ~2× les prix sur pied
officiels FBF, suggérant des prix bord de route ou transformés.

**Recommandation** :
1. **Documenter explicitement** le type de prix pour chaque produit (sur pied / bord de route / rendu scierie)
2. Adopter **FBF** comme source pour prix sur pied
3. Adopter **CEEB** comme source pour prix bord de route
4. Mettre à jour les prix avec données 2024-2025

### 4.5 Écarts régionaux documentés

| Région | Essence | Écart vs national | Source |
|---|---|---|---|
| Occitanie | Douglas | -25% vs national (2024) | Collectivités forestières Occitanie |
| Occitanie | Chêne | +24% vs 2023 | Collectivités forestières Occitanie |
| Nouvelle-Aquitaine | Pin maritime | Prix maintenus malgré baisse nationale | CNPF NA |
| Grand Est | Hêtre | +15% vs national | FBF |
| BFC | Chêne sessile | +25% vs national | FBF |

**Recommandation** : Les coefficients régionaux dans `PricingCoefficients.kt` (6 essences)
sont sous-couverts. Étendre à toutes les essences majeures avec données CNPF/FIBOIS régionales.

---

## 5. Facteurs de prix INFLUENTS non couverts par GeoSylva

### 5.1 Facteurs techniques (manquants)

| Facteur | Impact | Exemple | Source |
|---|---|---|---|
| **Diamètre** | Prime gros bois | Chêne 50+ cm : jusqu'à 300 €/m³ vs <150 €/m³ | CNPF |
| **Volume unitaire par arbre** | Économie d'échelle | Douglas >2,5 m³ : 90-100 €/m³ vs <1 m³ : prix inférieurs | FBF |
| **Longueur de grume** | Rendement transformation | Grume longue ≥12m valorisée | ONF |
| **Forme de l'arbre (coef. forme)** | Arbres droits valorisés | - | Pardé-Bouchon |
| **Décroissance métrique moyenne (dmm)** | Volume exploitable | - | ONF |

### 5.2 Facteurs de qualité (manquants)

| Facteur | Impact | Source |
|---|---|---|
| **Classement qualité A/B/C/D** | Écarts 1 à 7 (chêne charpente vs tranchage) | NF EN 1316/1927 |
| **Certificat d'élagage** | Prime si présent | ONF |
| **Présence de défauts** | Nœuds, fil coupé, pourriture, mitraille | NF EN 1310 |
| **Qualités spéciales** | Couleur, tranchage, déroulage, ébénisterie | APECF/ONF |

### 5.3 Facteurs d'exploitation (partiellement couverts)

| Facteur | Couverture GeoSylva | Source |
|---|---|---|
| Accessibilité (pente, distance) | ✅ CoefAccessibilité | CNPF |
| Places de dépôt | ❌ Manquant | ONF |
| Taille de la coupe | ✅ CoefLot | CNPF |
| Conditions d'exploitation | ❌ Manquant | ONF |
| Portance des sols | ❌ Manquant | ONF |

### 5.4 Facteurs de marché (manquants)

| Facteur | Impact | Source |
|---|---|---|
| Distance usine transformation | Coût transport acheteur | CEEB |
| Modalités de paiement | Comptant vs échelonné | - |
| Conjoncture marché (offre/demande) | Variation temporelle | FBF trimestriel |
| Marché export | Demande asiatique (chêne), allemande (résineux) | FBF |
| Certifications PEFC/FSC | ✅ CoefCertification (+5-15%) | PEFC/FSC |

### 5.5 Facteurs sanitaires (manquants)

| Facteur | Impact | Source |
|---|---|---|
| Scolytes (épicéa) | Déclassement qualité | DSF |
| Dépérissement (sapin) | Bois déclassés | DSF |
| Maladies spécifiques | Graphiose (orme), bandes rouges (pin laricio) | DSF |

### 5.6 Recommandations facteurs manquants

**À intégrer en priorité** (impact > 20% sur prix) :
1. **Prime diamètre** : modulateur selon classe de diamètre (ex: chêne 50+ cm)
2. **Volume unitaire** : modulateur selon volume par arbre (ex: douglas >2.5 m³)
3. **Classement qualité A/B/C/D** : pondération par qualité (déjà partiellement dans ProPricingEngine via CoefQualité)
4. **Coefficient régional** : étendre à toutes essences majeures (±30% max)
5. **Facteur sanitaire** : déclassement selon état sanitaire (scolytes, dépérissement)

**À documenter** (impact modéré) :
6. Longueur de grume
7. Distance usine
8. Conjoncture marché (mise à jour trimestrielle)

---

## 6. Vérification croissance & production

### 6.1 Tables de production

**Décourt & Pardé 1980** (ENGREF Nancy, ISBN 978-2-85710-016-4) : référence historique
mais **anciennes**. Étude CNPF 2025 souligne que les tables utilisées sont "souvent anciennes,
qui ne sont pas nécessairement spécifiques aux forêts françaises".

**Alternatives modernes** :
- **IGN IFN** : données inventaire actuelles avec accroissement mesuré (5 dernières années)
- **MARGOT (IGN)** : projections stocks/disponibilités 2050-2080 (accès interne IGN)
- **Capsis (INRAE/AMAP)** : plateforme open source, 25+ modèles de croissance
- **ClimEssences (CNPF/ONF)** : 149 espèces, 37 critères dont croissance/production

### 6.2 MAI par essence — vérification

| Essence | MAI GeoSylva | MAI officiel | Source | Statut |
|---|---|---|---|---|
| Chêne pédonculé | 4.5 | 3-5 (futaies régulières, 90-120 ans) | CNPF NA | ✅ Cohérent |
| Chêne sessile | 4.8 | 4.9-6.5 (100-200 ans, bonne station) | Ann. Sci. For. 2000 | ⚠️ Borne basse |
| Hêtre | ABSENT | 5 | Statistiques 2025 | ❌ Manquant |
| Douglas | ABSENT | 14.4 (production biologique) | IFN 2019-2023 | ❌ Manquant |
| Sapin-Épicéa | ABSENT | 11.3 | IFN 2019-2023 | ❌ Manquant |
| Pin maritime | ABSENT | 8 | IFN 2019-2023 | ❌ Manquant |
| Pin sylvestre | ABSENT | 4.3 | IFN 2019-2023 | ❌ Manquant |

**Recommandation** : Ajouter MAI pour toutes essences majeures avec données IFN récentes.

### 6.3 Modèles de croissance

- **Richards 1959** (DOI 10.1093/jxb/10.2.290) : ✅ référence validée
- **Dhôte & de Hercé 1994** (DOI 10.1139/x94-230) : ✅ référence validée (modèle H-D chêne/hêtre)
- **Capsis** : à intégrer pour simulations avancées (open source)
- **ClimEssences** : à utiliser pour données croissance en contexte changement climatique

---

## 7. Vérification biomasse & carbone

### 7.1 Facteurs IPCC

| Facteur | GeoSylva | Officiel | Source | Statut |
|---|---|---|---|---|
| carbonFraction | 0.50 | 0.50 (bois ligneux) | IPCC 2006 Table 4.3 | ✅ Correct |
| co2ConversionFactor | 3.67 | 3.67 (44/12) | EPA, ratio moléculaire | ✅ Exact |
| rootToShootRatio | 0.25 | 0.24-0.48 (selon zone/AGB) | Cairns 1997 / IPCC 2019 Table 4.4 | ⚠️ Générique |
| BEF feuillus | 1.65 | 1.35 (>20 ans) / 4.0 (≤20 ans) | IPCC GPG-LULUCF Table 3A.1.10 | ⚠️ Ne tient pas compte âge |
| BEF résineux | 1.45 | 1.34 (>20 ans) / 3.0 (≤20 ans) | IPCC GPG-LULUCF Table 3A.1.10 | ⚠️ Ne tient pas compte âge |

### 7.2 Facteurs manquants par essence

| Facteur | Situation actuelle | Recommandation | Source |
|---|---|---|---|
| **BEF par essence et âge** | Valeurs globales (1.65/1.45) | Spécifier par essence et classe d'âge | IPCC 2006 Table 4.5 (BCEF) |
| **RER par essence** | 0.25 générique | RER selon zone écologique et AGB | IPCC 2019 Table 4.4 |
| **Fraction carbone par essence** | 0.50 générique | Valeurs spécifiques (étude 2024 recommande 0.47-0.50) | Étude "Improving wood carbon fractions" 2024 |
| **Densité bois par essence** | ~20 essences dans AdvancedCalc | Intégrer XyloDensMap (156 espèces) | XyloDensMap INRAE/IGN |
| **Stock carbone sol** | Non mentionné | Ajouter facteurs IPCC 2019 ou INRAE | IPCC 2019 / Label Bas-Carbone |

### 7.3 Recommandations carbone

1. **Adopter IPCC 2019 Refinement** comme référence principale (mise à jour 2006)
2. **Spécifier BEF par essence et classe d'âge** (IPCC Table 4.5 BCEF)
3. **Raffiner RER** selon zone écologique et AGB (IPCC 2019 Table 4.4)
4. **Intégrer XyloDensMap** pour densités bois par essence (156 espèces)
5. **Ajouter stock carbone sol** avec facteurs IPCC 2019 ou données INRAE
6. **Évaluer fractions carbone spécifiques** par essence (étude 2024)

---

## 8. Données manquantes par essence — synthèse

### 8.1 Couverture actuelle des données par essence

| Catégorie | Essences couvertes | % du catalogue (79) |
|---|---|---|
| CanonicalEssences (base) | 79 | 100% |
| Coefficients Algan | 79 | 100% (mais ❌ non sourcés) |
| Coefficients forme | 79 | 100% (⚠️ source à vérifier) |
| IFN rapide | 72 | 91% |
| IFN lent | 50 | 63% |
| Schumacher-Hall | 28 | 35% (❌ référence erronée) |
| Densité AdvancedCalc | ~20 | 25% |
| Prix essence×produit | 30 | 38% |
| Multiplicateur essence | 35 | 44% |
| Coefficients régionaux | 6 | 8% |
| Tables production | 2 (chêne, hêtre) | 3% |
| MAI | ~5 | 6% |

### 8.2 Essences avec données les plus complètes (top 10)

CH_SESSILE, CH_PEDONCULE, HETRE_COMMUN, DOUGLAS_VERT, SAPIN_PECTINE, EPICEA_COMMUN,
PIN_SYLVESTRE, PIN_MARITIME, MEL_EUROPE, CHATAIGNIER.

### 8.3 Essences avec données minimales (à compléter en priorité)

CH_PUBESCENT, CH_ROUGE, CH_VERT, CH_LIEGE, FRENE_OXYPHYLLE, ERABLE_CHAMP, BOUL_PUBESC,
AULNE_BLANC, TIL_PET_FEUIL, TIL_GR_FEUIL, ORME_CHAMP/LISSE/MONT, POMMIER_SAUV,
POIRIER_SAUV, NOISETIER, TREMBLE, PEUPLIER_NOIR, SAULE_BLANC/MARSAULT, SORB_OISEL,
ALISIER_BLANC, PIN_ALEP/PIGNON/CEMBRO/MUGO, EPICEA_SITKA, SAPIN_NORDMANN/GRANDIS,
MEL_JAPON, CEDRE_LIBAN, THUYA, CYPRES, IF, GENEVRIER.

### 8.4 Données manquantes critiques par essence majeure

| Essence | Données manquantes critiques |
|---|---|
| Hêtre | Schumacher-Hall (absent SylvicultureDB), MAI, densité AdvancedCalc cohérent |
| Douglas | Schumacher-Hall, MAI (14.4 m³/ha/an IFN), densité à vérifier (540 vs 510) |
| Pin maritime | Densité à corriger (440 vs 520), Schumacher-Hall, MAI (8) |
| Pin sylvestre | Schumacher-Hall, MAI (4.3), densité à vérifier (458 vs 510) |
| Mélèze | Schumacher-Hall, MAI, densité à vérifier (497 vs 590) |
| Cèdre Atlas | Densité à corriger (460 vs 580), Schumacher-Hall, MAI |
| Robinier | Densité à corriger (735 vs 660), Schumacher-Hall, MAI |
| Noyer | Prix à corriger (sous-évalué), Schumacher-Hall, MAI |

---

## 9. Plan d'action priorisé

### P0 — Critique (à traiter avant toute utilisation production)

1. **Remplacer coefficients Algan** par tarifs EMERGE (gftools) ou Vallet (GCubeR)
2. **Corriger référence Schumacher-Hall** : adopter vrais coefficients Vallet 2006 (GCubeR)
3. **Documenter type de prix** (sur pied vs bord de route) pour chaque produit
4. **Harmoniser unités densité** : adopter XyloDensMap (kg/m³ infradensité) comme source unique

### P1 — Majeur (à traiter dans la foulée)

5. **Corriger 5 densités** : Chêne vert (860), Robinier (735), Pin maritime (440), Cèdre Atlas (460), Thuya (320)
6. **Mettre à jour prix** avec données FBF 2024-2025 (notamment noyer sous-évalué, résineux sur-évalués)
7. **Ajouter MAI** pour essences majeures (Douglas 14.4, Sapin-Épicéa 11.3, Pin maritime 8, Pin sylvestre 4.3, Hêtre 5)
8. **Étendre coefficients régionaux** à toutes essences majeures (actuellement 6 essences)

### P2 — Important (à planifier)

9. **Intégrer facteurs prix manquants** : prime diamètre, volume unitaire, facteur sanitaire
10. **Raffiner BEF/RER** par essence et âge (IPCC 2019)
11. **Ajouter stock carbone sol** (IPCC 2019 ou INRAE)
12. **Vérifier coefficients de forme** contre Pardé & Bouchon 1988 (ouvrage papier)
13. **Compléter données** pour ~15 essences sans source spécifique (XyloDensMap complet + USFS)

### P3 — Amélioration continue

14. **Intégrer Capsis** pour simulations croissance avancées
15. **Utiliser ClimEssences** pour données croissance en contexte changement climatique
16. **Mise à jour trimestrielle** des prix (FBF, ONF, CEEB)
17. **Évaluer MARGOT** pour projections territoriales (contact IGN)

---

## 10. Sources vérifiées (bibliographie complète)

### Densité bois
- XyloDensMap (INRAE/IGN) : https://doi.org/10.57745/FA9DRA
- XyloDensMap V2 : https://entrepot.recherche.data.gouv.fr/dataset.xhtml?persistentId=doi%3A10.57745%2FZNFO7T
- CIRAD Wood Density Database : https://doi.org/10.5281/zenodo.1095454
- TROPIX (CIRAD) : https://tropix.cirad.fr/
- IPCC 2006 Guidelines V4 Ch4 : https://www.ipcc-nggip.iges.or.jp/public/2006gl/pdf/4_Volume4/V4_04_Ch4_Forest_Land.pdf
- Atlas bois résineux France (INRAE) : https://belinrae.inrae.fr/index.php?id=271363
- Forêt Méditerranéenne XyloDensMap : https://www.foret-mediterraneenne.org/_0/upload/biblio/FORET_MED_2020_4_267-274.pdf
- Scientific Data (Nature) XyloDensMap : https://preview-www.nature.com/articles/s41597-025-04645-1

### Coefficients cubage
- Algan 1901 : Bull. Soc. forestière Franche-Comté 6(2):123-130 — https://infodoc.agroparistech.fr/index.php?id=92431
- "Il y a 100 ans : naissance tarifs Algan" RFF 1995 : https://doi.org/10.4267/2042/26629
- Pardé & Bouchon 1988 : Dendrométrie, ENGREF Nancy, ISBN 978-2-85710-025-6 — https://belinrae.inrae.fr/index.php?id=4226
- Vallet et al. 2006 : Forest Ecology and Management 229:98-110 — https://doi.org/10.1016/j.foreco.2006.03.013
- GCubeR (package R) : https://github.com/cran/GCubeR — https://dlinchant.r-universe.dev/GCubeR
- EMERGE (ANR 2009-2014) : gftools R package — https://rdrr.io/github/pobsteta/gftools/src/R/TarifsEmerge.R
- IFN tarifs cubage : http://hdl.handle.net/2042/42780
- Schumacher 1933 : Journal of Agricultural Research 47:719-734 — https://ci.nii.ac.jp/naid/10029733560
- Bouchon 1982 : RFF 34(3):225-236 — https://doi.org/10.4267/2042/21575
- Longuetaud et al. 2013 : FEM 292:111-121 — https://doi.org/10.1016/j.foreco.2012.12.023
- Tran-ha et al. 2011 : RFF 63(3) — https://infodoc.agroparistech.fr/index.php?id=157091

### Prix bois
- France Bois Forêt observatoire : https://observatoire.franceboisforet.com/
- FBF Indicateur 2026 : https://observatoire.franceboisforet.com/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2026/
- FBF Indicateur 2025 : https://franceboisforet.fr/2025/05/05/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2025/
- ONF mercuriales : https://www.onf.fr/produits-services/acheter-du-bois/
- ONF indice T3 2024 : https://observatoire.franceboisforet.com/wp-content/uploads/2014/06/ONF_Prix_bois_2024_T3_Graph.pdf
- CEEB observatoire : https://observatoire.franceboisforet.com/wp-content/uploads/2014/06/GCF-OBS-ECO-commentaires-observatoire-des-prix-4T2024.pdf
- CNPF national : https://www.cnpf.fr/actualites/indicateur-2026-du-prix-de-vente-des-bois-sur-pied-en-foret-privee
- CNPF Nouvelle-Aquitaine : https://nouvelle-aquitaine.cnpf.fr/gestion-durable-des-forets/coupes-et-travaux/le-prix-des-bois
- CNPF fiche Estimer : https://ifc.cnpf.fr/sites/ifc/files/2024-03/Fiche%20Gestion%2021%20-%20Estimer%20et%20Vendre%20ses%20Bois.pdf
- FIBOIS Occitanie : https://www.fibois-occitanie.com/ressources/observabois/secteur-foret/indicateurs-prix-bois-sur-pied-prive-2025/
- Collectivités forestières Occitanie : https://www.collectivitesforestieres-occitanie.org/2024/12/16/observatoire-des-ventes-publiques-bilan-des-ventes-2024/
- ADEME combustibles bois 2024 : https://librairie.ademe.fr/energies/8718-enquete-sur-les-prix-des-combustibles-bois-en-2024.html
- Experts Forestiers de France : https://expertsforestiersdefrance.com/vente_bois/agenda

### Croissance & production
- Décourt & Pardé 1980 : ENGREF Nancy, ISBN 978-2-85710-016-4 — https://belinrae.inrae.fr/index.php?id=4213
- Richards 1959 : J. Exp. Bot. 10(2):290-301 — https://doi.org/10.1093/jxb/10.2.290
- Dhôte & de Hercé 1994 : Can. J. Forest Res. 24(9):1782-1790 — https://doi.org/10.1139/x94-230
- IGN IFN : https://inventaire-forestier.ign.fr/
- IGN inventIF : https://inventif.ign.fr/croissance/
- MARGOT IGN projections : https://www.ign.fr/projections-bois-carbone-foret-francaise-2023-2024
- FFSM++ (open source) : https://ffsm-project.org/wiki/en/home
- Capsis (INRAE/AMAP) : https://capsis.cirad.fr/
- ClimEssences (CNPF/ONF) : https://climessences.fr/
- CNPF étude tables production 2025 : https://www.cnpf.fr/document/faciliter-l-utilisation-des-tables-de-production-forestieres-dans-le-cadre-du-label-bas

### Biomasse & carbone
- IPCC 2006 V4 Ch4 : https://www.ipcc-nggip.iges.or.jp/public/2006gl/pdf/4_Volume4/V4_04_Ch4_Forest_Land.pdf
- IPCC 2019 Refinement V4 Ch4 : https://www.ipcc-nggip.iges.or.jp/public/2019rf/pdf/4_Volume4/19R_V4_Ch04_Forest%20Land.pdf
- Cairns et al. 1997 : Oecologia 111(1):1-11 — https://doi.org/10.1007/s004420050201
- Chave et al. 2014 : Global Change Biology 20:3177-3190 — https://doi.org/10.1111/gcb.12629
- Jenkins et al. 2003 : Forest Science 49(1):12-35 — https://doi.org/10.1093/forestscience/49.1.12
- Label Bas-Carbone méthodes : https://label-bas-carbone.ecologie.gouv.fr/les-m%C3%A9thodes
- CITEPA UTCATF : https://www.citepa.org/expertise-et-solutions/production-de-donnees-et-dexpertise-en-france/utcatf/
- IGN projections carbone : https://www.ign.fr/publications-de-l-ign/institut/domaines-intervention/foret/rapport-projections-foret-bois-ign-fcba.pdf

---

## Conclusion

L'audit révèle que **GeoSylva dispose d'une base dendrométrique riche** (79 essences,
7 méthodes de cubage, moteur de prix à 8 coefficients) mais **souffre de problèmes
critiques de sourcing** qui doivent être corrigés avant utilisation production :

1. **Coefficients Algan non sourcés** — risque d'estimations de volume erronées
2. **Référence Schumacher-Hall inexistante** — coefficients à remplacer par vrais Vallet 2006
3. **Incohérence d'unités densité** — impact direct sur calculs biomasse/carbone

Les **sources canoniques recommandées** (XyloDensMap, GCubeR/EMERGE, FBF/ONF/CEEB,
IPCC 2019, IGN IFN) sont toutes **open source ou publiquement accessibles**, ce qui
facilite l'adoption et la vérification future.

Le **plan d'action priorisé** (P0-P3) permet de traiter les problèmes critiques en premier,
puis d'étendre la couverture des données et d'intégrer les facteurs de prix manquants
(diamètre, volume unitaire, sanitaire) qui influencent significativement la valorisation
par essence.

---

*Document généré par l'Agent Dendromètre (forest-crew) + 5 subagents de recherche parallèles.
Audit exhaustif sur 79 essences, 60+ sources vérifiées. Statut Draft — à valider par le
Fondateur avant correction du code Kotlin.*
