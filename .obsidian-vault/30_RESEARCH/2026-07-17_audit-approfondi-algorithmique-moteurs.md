---
statut: Draft
date: 2026-07-17
auteur: Agent Dendromètre (forest-crew) + 4 subagents d'audit algorithmique
périmètre: Audit approfondi algorithmique — moteurs cubage, prix, qualité, biomasse, architecture
fichiers_audités: 12 fichiers Kotlin (ForestryCalculator, TarifCalculator, ProPricingEngine, WoodQualityGrade, AdvancedCalculationEngine, ExpertForestryCalculator, SylvicultureDatabase, TarifData, PricingCoefficients, etc.)
tests_analysés: 6 suites de tests (~3000 lignes)
---

# Audit approfondi algorithmique — Moteurs internes GeoSylva

> **Mission** : Pousser l'analyse des logiques moteurs algorithmiques qui calculent le
> prix final et le volume. Le moteur interne doit être **intrinsèquement fiable**,
> proposer **beaucoup de tarifs et manières de cuber**, et être **très bien revu**.
> Les prix seront inclus dans les packs payants de l'app, mais tout ce qui est
> dans le moteur interne doit être irréprochable.
>
> **Méthode** : 4 subagents parallèles d'audit algorithmique (cubage, prix,
> qualité/carbone, architecture). 12 fichiers Kotlin audités, 6 suites de tests
> analysées (~3000 lignes), comparaison avec méthodes professionnelles (EFF, ONF,
> CNPF, prix hédonistes INRA).

---

## Synthèse exécutive

L'audit approfondi révèle que **les moteurs internes de GeoSylva sont fonctionnels
mais présentent 5 faiblesses structurelles majeures** qui doivent être corrigées
pour atteindre une fiabilité intrinsèque :

### 5 problèmes structurels critiques

| # | Problème | Gravité | Impact |
|---|---|---|---|
| **S1** | **ForestryCalculator est un God object** (760 lignes, 7 responsabilités) — violation SRP, couplage fort à 7 sous-moteurs + ParameterRepository | ❌ CRITIQUE | Testabilité, maintenabilité |
| **S2** | **Pas de pattern Strategy** pour cubage/prix — ajout d'une méthode nécessite 5 fichiers à modifier (violation OCP) | ❌ CRITIQUE | Extensibilité |
| **S3** | **Composition multiplicative aveugle** dans ProPricingEngine (8 coef. multiplicatifs, amplitude 592×, pas de garde-fou) | ❌ CRITIQUE | Prix aberrants possibles |
| **S4** | **Facteurs de prix structurels absents** : diamètre (prime gros bois), volume unitaire, conjoncture marché | ❌ CRITIQUE | Sous-estimation systématique gros bois |
| **S5** | **Coefficients hardcodés** (pas de repository, pas de cache, pas de sync GSIE, pas de versioning) | ⚠️ MAJEUR | Offline-first, mise à jour |

### Constats principaux par moteur

- **Cubage** : 7 méthodes mais FGH/COEF_FORME redondants, IFN sans fallback (62 essences non couvertes), pas de validation du domaine (extrapolation silencieuse), méthodes modernes manquantes (Vallet 2006, Longuetaud 2013, EMERGE).
- **Prix** : formule multiplicative 8 coef. avec amplitude 592× (scénario catastrophe 1.61 €/m³, scénario favorable 954 €/m³), pas de prime diamètre/volume unitaire (facteurs structurels du marché réel), pas d'actualisation temporelle.
- **Qualité** : 4 grades A/B/C/D subjectifs (score 0-3 sur 4 critères), pas de mapping explicite NF EN 1316/1927, double système de multiplicateurs (WoodQualityGrade + PriceCalculator) avec ratio A/D incohérent (6.25 vs 3.27).
- **Biomasse/carbone** : double calcul de volume possible (TarifCalculator vs AdvancedCalculationEngine avec coefficients différents), BEF/RER génériques (pas par essence/âge), carbonFraction 0.50 ✅ et CO2 3.67 ✅ corrects.
- **Architecture** : pas d'injection Hilt, pas d'interfaces, pas de repository pour coefficients, migration GSIE canal 1 non préparée.

### Recommandation principale

**Refonte architecturale en 6 phases** (pattern Strategy étendu, repository de
coefficients avec cache offline + sync GSIE, injection Hilt, modulateurs bornés)
avec **migration incrémentale** (feature flags, tests de régression ancien/nouveau).

---

## 1. État des lieux — moteur de CUBAGE (TarifCalculator)

### 1.1 Les 7 méthodes actuelles

| Méthode | Formule | Entrées | Couverture essence | Tests | Recommandation |
|---|---|---|---|---|---|
| **SCHAEFFER_1E** | V = a + b×C² | D | 100% (générique "*") | ✅ 3 tests | ⚠️ MODIFIER — coef. (a,b) ne correspondent pas à Schaeffer 1949 (V = M×(D-14)²/(45-14)²) |
| **SCHAEFFER_2E** | V = a + b×C²×H | D, H | 100% (générique "*") | ✅ 3 tests | ⚠️ MODIFIER — même problème sourcing |
| **ALGAN** | V = a×D^b×H^c | D, H | 42% direct + 100% fallback famille | ✅ 9 tests | ✅ CONSERVER — meilleur compromis, documenter source |
| **IFN_RAPIDE** | V = (a₀+a₁×D+a₂×D²)/1000 | D, tarif | 38% direct, **pas de fallback** | ✅ 4 tests | ⚠️ MODIFIER — ajouter fallback essence inconnue |
| **IFN_LENT** | V = (a₀+a₁×D²+a₂×D²×H)/1000 | D, H, tarif | 38% direct, **pas de fallback** | ✅ 3 tests | ⚠️ MODIFIER — ajouter fallback essence inconnue |
| **FGH** | V = F×G×H | D, H, f | 28% direct + 100% fallback | ❌ 0 test | ❌ SUPPRIMER — redondant avec COEF_FORME (formule identique) |
| **COEF_FORME** | V = G×H×f | D, H, f | 28% direct + 100% fallback | ✅ 4 tests | ✅ CONSERVER — méthode classique simple |

### 1.2 Faiblesses algorithmiques transversales (cubage)

#### 1.2.1 Gestion des cas limites — INCOHÉRENTE

| Cas | Schaeffer 1E | Schaeffer 2E | Algan | IFN Rapide | IFN Lent | FGH | COEF_FORME |
|---|---|---|---|---|---|---|---|
| D=0 | ✅ null | ✅ null | ✅ 0.0 | ✅ null | ✅ null | ✅ null | ✅ null |
| H=0 | N/A | ✅ null | ✅ 0.0 | N/A | ✅ null | ✅ null | ✅ null |
| Essence absente | ✅ "*" | ✅ "*" | ✅ fallback famille | ❌ null | ❌ null | ✅ "*" | ✅ "*" |
| tarifNumero absent | ✅ défaut | ✅ défaut | N/A | ❌ null | ❌ null | N/A | N/A |

**Problème** : IFN Rapide/Lent retournent null pour essence inconnue au lieu de fallback. Incohérence de comportement entre méthodes.

#### 1.2.2 Comportement hors domaine — AUCUNE VALIDATION

- Aucune validation D min/max pour toutes les méthodes
- Aucune validation H min/max pour les méthodes 2 entrées
- Extrapolation silencieuse (pas de warning)
- Coefficients négatifs peuvent donner V < 0 (masqué par `coerceAtLeast(0.0)`)
- Risque d'overflow pour Algan (D^b avec b > 2, D > 200 cm) et IFN Lent (D²×H)

**Recommandation** : Bornes explicites (D ∈ [5, 200] cm, H ∈ [5, 60] m), retourner `Result<Volume, DomainError>` au lieu de `Double?`, logger les extrapolations.

#### 1.2.3 Précision numérique

- Algan : `Math.pow(D, b)` avec b > 2 peut overflow pour D > 200 cm
- Schaeffer 2E/IFN Lent : D²×H peut overflow pour grands arbres
- Pas de vérification de bornes avant calcul

#### 1.2.4 Cohérence inter-méthodes — AUCUN TEST

- Aucun test de cohérence entre méthodes
- FGH et COEF_FORME sont redondants (même formule)
- Schaeffer 1E vs IFN Rapide : même entrée D → volumes non comparés
- Schaeffer 2E vs Algan vs IFN Lent : même entrées (D, H) → volumes non comparés

### 1.3 Méthodes de cubage manquantes (catalogue exhaustif)

#### Tarifs à 1 entrée (D seul)

| Méthode | Formule | Source | Couverture | Priorité |
|---|---|---|---|---|
| **Algan 1901 (original)** | V = M×(D-14)²/(45-14)² | Algan 1901, Bull. Soc. forestière Franche-Comté 6(2):123-130 | 20 tarifs génériques | P1 |
| **Schaeffer 1949 (formule originale)** | V = M×(D-14)²/(45-14)² | Schaeffer 1949, RFF 1:7-13, DOI 10.4267/2042/27584 | 20 tarifs | P0 (correction) |

#### Tarifs à 2 entrées (D + H)

| Méthode | Formule | Source | Couverture | Priorité |
|---|---|---|---|---|
| **Schaeffer 2E (formule originale)** | V = M×(D-14)²×H/(45-14)²/H_ref | Schaeffer 1949 | 8 tarifs | P0 (correction) |
| **Vallet 2006 (forme de tige, 4 coef.)** | V = a+b×C²+c×C²×H+d×C²×H² | Vallet et al. 2006, FEM 229:98-110, DOI 10.1016/j.foreco.2006.03.013 | 7 essences majeures (4619 arbres) | P1 |

#### Tarifs à 3 entrées (D + H + coef. forme)

| Méthode | Formule | Source | Couverture | Priorité |
|---|---|---|---|---|
| **Pardé-Bouchon (f variable)** | V = G×H×f(D,H) | Pardé & Bouchon 1988, Dendrométrie ENGREF | Toutes essences | P2 |
| **Décroissance métrique** | V = Σ V_i par billon | Pardé & Bouchon 1988, ONF Guide cubage | Toutes essences | P2 |

#### Équations allométriques modernes

| Méthode | Formule | Source | Couverture | Priorité |
|---|---|---|---|---|
| **Longuetaud 2013 (VEF)** | VEF = a+b×D+c×H+d×D×H | Longuetaud et al. 2013, FEM 292:111-121, DOI 10.1016/j.foreco.2012.12.023 | 19 essences (8192 arbres) | P2 |
| **EMERGE** | V = a+b×C²+c×C²×H+d×C²×H²+e×C²×H²×IS | Deleuze et al. 2014, RDV Techniques ONF 44:22-32 (ANR-08-BIOE-003) | 58 essences FR (679 949 arbres) | P3 |
| **Chave 2014 (pantropical)** | AGB = 0.0673×(ρ×D²×H)^0.976 | Chave et al. 2014, GCB 20:3177-3190, DOI 10.1111/gcb.12629 | Tropicales | P3 (peu pertinent FR) |

#### Méthodes spécifiques

| Méthode | Formule | Source | Priorité |
|---|---|---|---|
| **Cubage grume par billons (ONF)** | V = Σ π/4×((d_base+d_sommet)/2/100)²×L | ONF Guide cubage | P2 |
| **Cubage abattage (façonné)** | V_façonné = V_grume - V_écorce - V_défauts | ONF Guide cubage | P2 |
| **Cubage LiDAR/photogrammétrie** | V = intégration nuage de points 3D | EMERGE LiDAR, voir VOLUME_CALCULATION_NEXT_GEN.md | P3 |
| **Cubage dendromètre (arbre debout)** | V = Σ π/4×(d_section/100)²×h_section | Pardé & Bouchon 1988 | P2 |

#### Conversions de volume manquantes

| Conversion | Formule | Source | Priorité |
|---|---|---|---|
| **Sur écorce / sous écorce** | V_sous = V_sur × (1 - coef_écorce) | Longuetaud 2013 | P1 |
| **Bois fort / bois petit** | V_fort = V_total × coef_bois_fort | ONF Guide cubage | P1 |
| **Commercial / total** | V_com = V_total × coef_commercial(essence, qualité) | ONF Guide cubage | P1 |
| **VEF (Longuetaud 2013)** | V_total = V_tige × VEF(D,H) | Longuetaud 2013 | P2 |

---

## 2. État des lieux — moteur de PRIX (ProPricingEngine)

### 2.1 Formule actuelle (décomposition complète)

```
Prix final = PrixRéférence(essence, produit, diam, région)
           × CoefficientQualité(essence, grade A/B/C/D)        [NF EN 1316/1927]
           × (1 - ΣDépréciationDéfauts)                          [NF EN 1310]
           × CoefficientRégional(région administrative)          [écarts FBF]
           × CoefficientAccessibilité(pente, distance)           [CNPF]
           × CoefficientSaison(mois)                             [CIBE]
           × CoefficientCertification(PEFC/FSC)                  [+5-15%]
           × CoefficientLot(volume)                              [économie d'échelle]
           × CoefficientPosition(sur pied / bord route / usine)  [+25-80%]
```

**Implémentation** : `val finalPrice = basePrice * breakdown.totalCoefficient` (produit de 8 facteurs).

### 2.2 Décomposition des 8 coefficients

| # | Coefficient | Plage | Source | Impact typique | Tests |
|---|---|---|---|---|---|
| 1 | Prix de base | Variable | PriceEntry / DefaultProductPrices | Référence | ✅ |
| 2 | Qualité | A: 1.55-3.20, B: 1.18-2.00, C: 1.0, D: 0.38-0.70 | NF EN 1316-1/1927, FBF 2025 | +55% à +220% | ✅ |
| 3 | Défauts | 0.0 à 0.90 (plafond) | NF EN 1310, NF EN 1309-3 | -0% à -90% | ✅ |
| 4 | Régional | 0.70 à 1.30 | FBF, Fibois régionaux | -30% à +30% | ✅ |
| 5 | Accessibilité | 0.75 à 1.0 | CNPF, Coforet | -0% à -25% | ❌ |
| 6 | Saison | 0.95 à 1.15 | CIBE, FBF | -5% à +15% | ❌ |
| 7 | Certification | 1.0 à 1.12 | PEFC/FSC France | +0% à +12% | ❌ |
| 8 | Taille de lot | 0.85 à 1.10 | CNPF "Estimer et vendre ses bois" | -15% à +10% | ✅ |
| 9 | Position | 1.0 à 1.65 | ONF, Magazine Bois | +0% à +65% | ✅ |

### 2.3 Analyse de la composition multiplicative — CRITIQUE

#### Scénario catastrophe (tous coef. défavorables)

```
Qualité D (chêne sessile)       × 0.38
Défauts max (plafond 90%)       × 0.10
Régional min (Douglas Occitanie) × 0.70
Accessibilité très difficile    × 0.75
Saison été                       × 0.95
Certification aucune             × 1.0
Lot < 50 m³                      × 0.85
Position sur pied                × 1.0
─────────────────────────────────────────
Total                             × 0.0161
```

**Prix de base 100 €/m³ → 1.61 €/m³** — aberrant pour du bois d'œuvre.

#### Scénario favorable (tous coef. favorables)

```
Qualité A (noyer)                × 3.20
Défauts aucun                    × 1.0
Régional max (Douglas Grand Est)  × 1.30
Accessibilité facile             × 1.0
Saison hiver                     × 1.15
Certification PEFC+FSC           × 1.12
Lot > 500 m³                     × 1.10
Position usine                   × 1.65
─────────────────────────────────────────
Total                             × 9.54
```

**Prix de base 100 €/m³ → 954 €/m³** — possible pour mérain noyer premium, excessif pour Douglas.

#### Amplitude totale : 592× entre scénarios extrêmes

**Problèmes** :
1. **Prix aberrants possibles** : < 5 €/m³ ou > 5000 €/m³
2. **Amplification des erreurs** : erreur 10% sur un coef. → 70% sur le final
3. **Pas de pondération** : qualité (3×) a même poids multiplicatif que saison (1.15×)
4. **Pas de garde-fou** : pas de plafond/plancher, pas de validation

### 2.4 Facteurs de prix manquants — CRITIQUE

#### 2.4.1 Diamètre (prime gros bois) — IMPACT CRITIQUE

**État actuel** : Diamètre utilisé uniquement pour lookup PriceEntry (bornes min/max), **aucun coefficient multiplicateur propre**.

**Réalité marché** (CNPF, FBF) :
- Chêne 50+ cm : jusqu'à 300 €/m³ vs <150 €/m³ pour 30-40 cm (écart 2×)
- Douglas > 2.5 m³ volume unitaire : 90-100 €/m³ vs 72 €/m³ moyen (+40%)
- Prime diamètre est **structurelle**, pas accessoire

**Conséquence** : GeoSylva sous-estime systématiquement les gros bois de qualité.

#### 2.4.2 Volume unitaire par arbre — IMPACT CRITIQUE

**État actuel** : **Pas du tout pris en compte**.

**Réalité marché** (FBF 2024) :
- Chêne : volume unitaire moyen 1.7 m³ → 228 €/m³
- Douglas : volume unitaire moyen 0.9 m³ → 72 €/m³
- Gros volumes unitaires (>2 m³) commandent une prime +20-40%

**Conséquence** : GeoSylva ne peut pas distinguer un arbre de 0.5 m³ d'un de 2.5 m³ à diamètre égal.

#### 2.4.3 Classement qualité — PARTIELLEMENT COUVERT

**État actuel** : 4 grades A/B/C/D, mais insuffisant pour essences premium.

**Réalité marché** (CNPF) :
- Chêne : sous-grades 1 à 7 pour tranchage (NF EN 1316-1)
- Alisier torminal, érable ondé : écart qualité 20× entre tranchage et sciage

**Conséquence** : GeoSylva lisse les écarts de qualité réels pour essences premium.

#### 2.4.4 Conjoncture marché — ABSENT

**État actuel** : Prix statiques, pas d'actualisation.

**Réalité marché** :
- Indices trimestriels FBF : variation ±20%
- Crises (COVID-19) : chute 20-30%
- Demande export (Chine) : +30% sur chêne tranchage

**Conséquence** : GeoSylva utilise des prix figés, non actualisés.

#### 2.4.5 Autres facteurs manquants

| Facteur | État | Impact |
|---|---|---|
| Longueur de grume | ABSENT | Moyen (prime grume longue ≥12m) |
| Facteur sanitaire peuplement | PARTIEL (WoodDefect arbre seul) | Moyen (scolytes, dépérissement) |
| Marché export | ABSENT | Moyen (prime Chine/USA) |
| Homogénéité lot | ABSENT | Moyen (facteur CNPF) |

### 2.5 Comparaison avec méthodes professionnelles

| Méthode | Approche | Écart principal GeoSylva |
|---|---|---|
| **EFF (Experts Forestiers)** | Ventes réelles groupées, indices trimestriels | ❌ Pas d'actualisation temporelle |
| **ONF (martelage)** | Adjudication pied à pied | ⚠️ Formule multiplicative vs adjudication |
| **CNPF (estimation)** | Volume unitaire prépondérant, homogénéité lot | ❌ Volume unitaire absent |
| **Prix hédonistes INRA** | Régression statistique (diamètre, qualité, structure) | ⚠️ Approche heuristique vs scientifique |

**Conclusion** : GeoSylva est une approximation raisonnable pour estimations rapides terrain, mais **s'écarte significativement** des méthodes professionnelles sur 2 points critiques : (1) absence de prime diamètre/volume unitaire, (2) absence d'actualisation temporelle.

### 2.6 Gestion des cas limites

| Cas | Comportement actuel | Problème |
|---|---|---|
| Essence sans prix | Fallback DefaultProductPrices | Prix potentiellement incohérent pour essences rares |
| Produit non défini | Fallback 50 €/m³ hardcodé | Valeur arbitraire non sourcée |
| Région non couverte | ×1.0 (moyenne nationale) | IDF, PAC ont prix significativement différents |
| Qualité non saisie | ×1.0 (grade C assumé) | Biaise le prix vers la moyenne |

### 2.7 Traçabilité et explicabilité — BON

**Avantages** :
- `PricingResult.summary()` fournit breakdown complet
- Chaque coefficient documenté avec sa source
- Warnings signalent les fallbacks
- Source de vérité unique (`buildResult()`) empêche divergence

**Limites** :
- Pas d'audit trail historique (qui a modifié les coef., quand)
- Pas de comparaison avec prix réels observés
- Pas d'explication des écarts vs marché

---

## 3. État des lieux — moteur QUALITÉ/PRODUITS

### 3.1 Grades A-D — analyse

| Grade | Multiplicateur | Description | Source déclarée |
|---|---|---|---|
| A | 2.5 | Excellente qualité (tranchage/mérain/ébénisterie) | APECF/ONF, NF EN 1316 |
| B | 1.5 | Bonne qualité (sciage qualité/charpente premium) | NF EN 1316 |
| C | 1.0 | Qualité moyenne (sciage courant/charpente) | NF EN 1316 |
| D | 0.4 | Qualité médiocre (bois industrie/chauffage) | NF EN 1316 |

**Critères de classification** (QualityAssessment) : score sur 12 points (4 critères × 3 niveaux)
- rectitude (0-3), branchage (0-3), etatSanitaire (0-3), defautsFut (0-3)
- Mapping : A≥10, B≥7, C≥4, D<4

**Faiblesses** :
1. **Critères subjectifs** : 4 critères qualitatifs 0-3 sans unité de mesure objective
2. **Pas de mapping explicite NF EN 1316/1927** : système A-D générique sans codes normalisés (Q-A/Q-B/Q-C/Q-D pour chêne, F-A/F-B/F-C/F-D pour feuillus)
3. **Multiplicateurs non sourcés** : 2.5/1.5/1.0/0.4 sont estimations marché, pas coefficients normatifs
4. **Incohérence ratio A/D** : WoodQualityGrade (6.25) vs PriceCalculator["*"] (3.27)
5. **Pas de prise en compte diamètre** : normes NF EN 1316/1927 lient qualité au diamètre minimum

### 3.2 Catalogue produits (16 ForestProduct)

| Code | Label | MinQuality | DiamMin | Feuillu | Résineux |
|---|---|---|---|---|---|
| MERAIN | Mérain (tonnellerie) | A | 55 | ✅ | ❌ |
| TRANCHAGE | Tranchage/placage | A | 45 | ✅ | ❌ |
| DEROULAGE | Déroulage (contreplaqué) | A | 40 | ✅ | ❌ |
| SCIAGE_Q | Sciage qualité/ébénisterie | B | 35 | ✅ | ✅ |
| GRUME_L | Grume longue (≥12m) | A | 35 | ❌ | ✅ |
| POTEAU | Poteau de ligne | A | 20 | ❌ | ✅ |
| CHARPENTE | Charpente/lamellé-collé | B | 25 | ❌ | ✅ |
| BARDAGE | Bardage/lambris | B | 20 | ✅ | ✅ |
| SCIAGE_S | Sciage standard/charpente | C | 25 | ✅ | ✅ |
| PIQUET | Piquet/clôture | C | 10 | ✅ | ✅ |
| TRAVERSE | Traverse chemin de fer | B | 30 | ✅ | ✅ |
| PALETTE | Palette/emballage | C | 20 | ✅ | ✅ |
| BI | Bois industrie/trituration | D | 10 | ✅ | ✅ |
| PATE | Pâte à papier | D | 7 | ✅ | ✅ |
| BCh | Bois de chauffage | D | 7 | ✅ | ❌ |
| BE | Bois énergie/plaquettes | D | 7 | ✅ | ✅ |

**Produits manquants** : Plot/plateau, Avivé, Lamellé-collé (BLC), Contreplaqué, Parquet, Menuiserie extérieure, Poteau télécom/électrique, Traverse SNCF (spécifications techniques).

**Faiblesses** :
1. **Catalogue incomplet** : 16 produits vs 30+ produits standard filière bois française
2. **Absence de référence normative** : codes inventés (MERAIN, TRANCHAGE) sans correspondance NF EN 1316-1/1927
3. **Pas de définition technique** : aucune spécification (longueur, largeur, épaisseur, tolérances, humidité)
4. **Mapping essence→produit heuristique** : ProductClassifier utilise règles basées sur diamètre + qualité, sans référence aux normes

### 3.3 DefaultProductPrices — structure et sources

**3 niveaux de prix** :
1. **Prix par défaut par produit** (16 entrées) : MERAIN=850€, TRANCHAGE=380€, SCIAGE_Q=135€, BE=16€
2. **Prix spécifique essence×produit** (128 entrées, 21 essences) : CH_SESSILE:MERAIN=1200€
3. **Multiplicateurs par essence** (43 essences) : CH_SESSILE=1.40, NOYER_COMMUN=2.50

**Formule** : `priceFor(product, essence, grade) = essenceProductPrices[essence:product] ?: defaults[product] × essenceMultipliers[essence] ?: defaults[product] × grade.multiplier`

**Faiblesses** :
1. **Source non documentée précisément** : "mercuriales ONF, FBF, DRAAF" sans référence spécifique
2. **Type de prix ambigu** : "bord de route" mentionné mais ONF = sur pied, FBF = bord de route
3. **Date de validité figée** : "France 2023-2024" sans mécanisme de mise à jour, sans champ `validUntil`
4. **Couverture partielle** : 21 essences sur 100+ avec prix spécifiques
5. **Incohérences potentielles** : multiplicateurs peuvent donner prix aberrants pour essences non listées

---

## 4. État des lieux — moteur BIOMASSE/CARBONE (AdvancedCalculationEngine)

### 4.1 volumeCoefficients (a par essence)

**Formule** : `V = a × D^b × H^c` avec b=2.0 et c=1.0 fixes pour toutes essences, a varie.

**Problème critique** : **Double calcul de volume possible** :
- TarifCalculator utilise TarifData.alganCoefs (a, b, c spécifiques par essence, ex: CH_SESSILE a=0.0000423, b=2.118, c=0.872)
- AdvancedCalculationEngine utilise coefficients simplifiés (b=2.0, c=1.0) **différents**
- Aucun mécanisme de synchronisation → volumes incohérents selon le moteur utilisé

**Source déclarée** : "équation de type Schumacher-Hall simplifiée" — **non sourcée précisément**

### 4.2 woodDensity (t/m³ par essence)

**Valeurs** : Chênes 0.61, Hêtre 0.68, Sapin/Épicéa 0.43, Douglas 0.47, Pin sylvestre 0.52, etc.

**Source déclarée** : "tables d'infradensité CIRAD/IGN" — **non précisée**

**Incohérence avec CanonicalEssences.kt** :
- CanonicalEssences : densiteBois en kg/m³ à 12% HR (CH_SESSILE=710, HETRE=680)
- AdvancedCalculationEngine : infradensité en t/m³ (CH_SESSILE=0.61, HETRE=0.68)
- Conversion : infradensité ≈ densité 12% HR × 0.85
- CH_SESSILE : 710 × 0.85 = 603.5 ≈ 0.61 ✅ cohérent
- HETRE : 680 × 0.85 = 578 ≠ 0.68 ❌ **incohérent** (Hêtre devrait être ~0.58, pas 0.68)

### 4.3 BEF (Biomass Expansion Factor)

**Valeurs** : Feuillus 1.65, Résineux 1.45

**Source déclarée** : "IPCC 2006, valeurs par défaut"

**Faiblesse** : Valeurs fixes sans variation par essence/âge. IPCC 2006 Table 4.5 fournit BCEF par zone écologique, type de forêt, âge. IPCC GPG-LULUCF Table 3A.1.10 : 1.34-4.0 selon âge.

### 4.4 RER (Root-to-Shoot Ratio)

**Valeur** : 0.25 (unique pour toutes essences)

**Source déclarée** : "Cairns et al. 1997"

**Faiblesse** : Valeur générique. IPCC 2019 Table 4.4 donne 0.24-0.48 selon zone/AGB.

### 4.5 carbonFraction et CO2 factor — VALIDÉS

- carbonFraction = 0.50 ✅ (IPCC 2006 Table 4.3, bois ligneux)
- co2ConversionFactor = 3.67 ✅ (44/12, ratio moléculaire)

### 4.6 Formule complète de calcul carbone

```
1. Volume fût (m³) = a × D^b × H^c
2. Biomasse fût sec (kg) = Volume × densité (t/m³) × 1000
3. Biomasse aérienne (kg) = biomasse fût × BEF
4. Biomasse totale (kg) = biomasse aérienne × (1 + RER)
5. Carbone (kg) = biomasse × carbonFraction (0.50)
6. CO2-équivalent (kg) = carbone × 3.67
```

**Cohérence dimensionnelle** : ✅ correcte (m³ × t/m³ = t, × 1000 = kg, × sans dimension = kg)

**Comparaison Label Bas-Carbone** : utilise IPCC 2006 + spécificités France (zones écologiques) + stock carbone sol. **GeoSylva n'implémente pas les spécificités France ni le stock sol.**

---

## 5. État des lieux — ARCHITECTURE

### 5.1 Cartographie de l'architecture actuelle

```
ForestryCalculator (760 lignes, God object)
├── ParameterRepository (infrastructure)
├── TarifCalculator (cubage, 7 méthodes)
│   ├── TarifData (624 lignes coefficients hardcodés)
│   └── EssenceAliases
├── ProPricingEngine (prix, 8 coef.)
│   ├── PriceCalculator (qualityCoefficients)
│   ├── WoodQualityGrade (DefaultProductPrices)
│   ├── RegionalCoefficients
│   └── WoodDefect
├── ProductClassifier
├── EssenceAliases
└── Sauvegarde configuration

ExpertForestryCalculator (625 lignes)
├── ForestryCalculator (dépendance)
├── ParameterRepository
└── SylvicultureDatabase (28 essences hardcodées)

EnhancedForestryCalculator
└── ExpertForestryCalculator → ForestryCalculator

AdvancedCalculationEngine (data layer, NON utilisé par domain)
└── Room Database
```

### 5.2 Analyse SOLID

| Principe | Statut | Détail |
|---|---|---|
| **S** (Single Responsibility) | ❌ VIOLATION | ForestryCalculator (760 lignes, 7 responsabilités), ExpertForestryCalculator (625 lignes), ProPricingEngine (303 lignes) |
| **O** (Open/Closed) | ❌ VIOLATION | Ajouter méthode cubage = 5 fichiers à modifier ; ajouter coef. prix = 4 fichiers |
| **L** (Liskov) | N/A | Pas d'héritage significatif (objects, pas classes) |
| **I** (Interface Segregation) | ❌ VIOLATION | ParameterRepository interface unique trop large ; PricingContext 22 champs |
| **D** (Dependency Inversion) | ⚠️ PARTIEL | Domain ne dépend pas de data ✅, mais calculateurs dépendent directement de ParameterRepository et coefficients hardcodés |

### 5.3 Couplage et cohésion

| Classe | Couplage | Cohésion |
|---|---|---|
| ForestryCalculator | **Très élevé** (7 sous-moteurs + ParameterRepository) | **Faible** (responsabilités hétérogènes) |
| ExpertForestryCalculator | **Élevé** (ForestryCalculator + ParameterRepository + SylvicultureDatabase) | **Faible** (calculs + tables + recommandations) |
| ProPricingEngine | Moyen (4 objects de données) | Moyenne |
| TarifCalculator | Moyen (TarifData 624 lignes) | Haute |

### 5.4 Testabilité

| Moteur | Tests | Couverture estimée | Difficulté |
|---|---|---|---|
| ForestryCalculator | ✅ 1059 lignes | ~70% | Difficile (ParameterRepository à mocker) |
| ExpertForestryCalculator | ✅ 474 lignes | ~60% | Difficile (ForestryCalculator à mocker) |
| TarifCalculator | ✅ 591 lignes | ~80% | Facile |
| ProPricingEngine | ✅ 205 lignes | ~75% | Moyenne (objects hardcodés) |
| WoodQualityGrade | ✅ | ~50% | Facile |
| AdvancedCalculationEngine | ✅ | ~40% | Moyenne |

**Couverture globale estimation** : ~65% sur module calculation

**Manques** :
- 0% cas limites (prix négatifs, aberrants, plafonds)
- 0% tests de cohérence inter-méthodes cubage
- 0% tests de cohérence cubage → biomasse
- 0% tests de conformité NF EN 1316/1927

### 5.5 Extensibilité — coût de modification

| Action | Fichiers à modifier | Complexité |
|---|---|---|
| Ajouter méthode de cubage | 5 (TarifMethod, TarifCalculator, TarifData, TarifModels, ForestryCalculator) | Élevée |
| Ajouter essence | 4 (TarifData, PriceCalculator, SylvicultureDatabase, EssenceAliases) | Moyenne |
| Ajouter produit | 3 (WoodQualityGrade, ProductClassifier, DefaultProductPrices) | Faible |
| Ajouter facteur de prix | 4 (PricingContext, PricingBreakdown, ProPricingEngine, PricingResult) | Élevée |

### 5.6 Offline-first et sync GSIE — NON PRÉPARÉ

- **Chargement coefficients** : hardcodés dans objects Kotlin (TarifData 624 lignes, PriceCalculator, SylvicultureDatabase, RegionalCoefficients)
- **Cache local** : inexistant (coefficients déjà en local mais pas de mécanisme de mise à jour)
- **Versioning** : inexistant (pas de champ version, pas de timestamp, pas de checksum)
- **Sync GSIE canal 1** : aucun code préparé (pas de repository, pas de Worker, pas d'API client)

---

## 6. Refonte proposée — moteur de CUBAGE

### 6.1 Architecture Strategy étendue (15-20 méthodes)

```kotlin
enum class TarifMethod(
    val code: String,
    val label: String,
    val description: String,
    val entrees: Int,
    val category: TarifCategory,
    val priority: TarifPriority
) {
    // ── TARIFS À 1 ENTRÉE ──
    SCHAEFFER_1E_1949(..., category = ONE_ENTRY, priority = HISTORICAL),
    ALGAN_1901(..., category = ONE_ENTRY, priority = HISTORICAL),
    IFN_RAPIDE(..., category = ONE_ENTRY, priority = STANDARD),

    // ── TARIFS À 2 ENTRÉES ──
    SCHAEFFER_2E_1949(..., category = TWO_ENTRY, priority = HISTORICAL),
    ALGAN(..., category = TWO_ENTRY, priority = STANDARD),
    VALLET_2006(..., category = TWO_ENTRY, priority = MODERN),

    // ── TARIFS À 3 ENTRÉES ──
    PARE_BOUCHON(..., category = THREE_ENTRY, priority = STANDARD),
    DECROISSANCE_METRIQUE(..., category = THREE_ENTRY, priority = ADVANCED),

    // ── ÉQUATIONS ALLOMÉTRIQUES ──
    LONGUETAUD_2013(..., category = ALLOMETRIC, priority = MODERN),
    EMERGE(..., category = ALLOMETRIC, priority = ADVANCED),
    CHAVE_2014(..., category = ALLOMETRIC, priority = EXPERIMENTAL),

    // ── MÉTHODES SPÉCIFIQUES ──
    BILLONS_ONF(..., category = SPECIFIC, priority = STANDARD),
    FACONNE(..., category = SPECIFIC, priority = STANDARD),
    LIDAR(..., category = SPECIFIC, priority = EXPERIMENTAL),
    DENDROMETRE(..., category = SPECIFIC, priority = STANDARD)
}

enum class TarifCategory { ONE_ENTRY, TWO_ENTRY, THREE_ENTRY, ALLOMETRIC, SPECIFIC }
enum class TarifPriority { HISTORICAL, STANDARD, MODERN, ADVANCED, EXPERIMENTAL }
```

### 6.2 Interface Strategy unifiée

```kotlin
interface VolumeCalculationStrategy {
    val method: TarifMethod

    fun calculate(params: VolumeCalculationParams): Result<Double, CalculationError>
    fun validate(params: VolumeCalculationParams): ValidationResult
    fun metadata(): MethodMetadata
}

data class VolumeCalculationParams(
    val essenceCode: String,
    val diamCm: Double,
    val hauteurM: Double?,
    val tarifNumero: Int? = null,
    val coefFormOverride: Double? = null,
    val additionalParams: Map<String, Double> = emptyMap()
)

sealed class CalculationError {
    data class DomainError(val message: String, val bounds: Bounds) : CalculationError()
    data class MissingDataError(val missing: String) : CalculationError()
    data class NumericalError(val message: String) : CalculationError()
    data class EssenceNotSupported(val essence: String) : CalculationError()
}

data class MethodMetadata(
    val source: String,
    val doi: String?,
    val year: Int,
    val essencesCovered: List<String>,
    val domain: Bounds,
    val precision: PrecisionInfo
)
```

### 6.3 Sélection automatique de la meilleure méthode

```kotlin
class TarifMethodSelector(private val methodRegistry: TarifMethodRegistry) {
    fun selectBestMethod(context: SelectionContext): TarifMethod {
        // 1. Override utilisateur
        context.userOverride?.let { return it }

        // 2. Données disponibles (D seul vs D+H)
        val candidates = if (context.hauteurM != null) {
            methodRegistry.methodsByCategory(TarifCategory.TWO_ENTRY)
        } else {
            methodRegistry.methodsByCategory(TarifCategory.ONE_ENTRY)
        }

        // 3. Couverture essence
        val essenceCovered = candidates.filter {
            it.metadata().essencesCovered.contains(context.essenceCode)
        }
        val bestByEssence = if (essenceCovered.isNotEmpty()) {
            essenceCovered.maxByOrNull { it.method.priority }
        } else {
            candidates.maxByOrNull { it.method.priority }
        }

        return bestByEssence?.method ?: fallbackMethod(context)
    }

    private fun fallbackMethod(context: SelectionContext): TarifMethod {
        return if (context.hauteurM != null) TarifMethod.ALGAN
        else TarifMethod.SCHAEFFER_1E_1949
    }
}
```

### 6.4 Cache des coefficients (offline-first)

```kotlin
class TarifCoefficientCache(
    private val context: Context,
    private val gson: Gson
) {
    private val cacheFile = File(context.filesDir, "tarif_coefficients_cache.json")

    fun loadCoefficients(): Map<String, Any> {
        return if (cacheFile.exists() && !isExpired()) {
            gson.fromJson(cacheFile.readText(), CoefficientCache::class.java).data
        } else {
            buildDefaultCache()  // Fallback hardcoded
        }
    }

    suspend fun updateFromKnowledgeEngine(engine: KnowledgeEngineClient) {
        // Sync GSIE canal 1 (uniquement en bonne condition réseau)
        val remoteCoefficients = engine.fetchTarifCoefficients()
        val cache = CoefficientCache(
            timestamp = System.currentTimeMillis(),
            version = "2026.07",
            data = remoteCoefficients
        )
        cacheFile.writeText(gson.toJson(cache))
    }

    private fun isExpired(): Boolean {
        val cache = gson.fromJson(cacheFile.readText(), CoefficientCache::class.java)
        val age = System.currentTimeMillis() - cache.timestamp
        return age > 30L * 24 * 60 * 60 * 1000  // 30 jours
    }
}
```

---

## 7. Refonte proposée — moteur de PRIX (intrinsèquement fiable)

### 7.1 Principes de conception

1. **Prix de base sourcés** : tables essence×produit×qualité×région (4D), pas multiplicateurs génériques
2. **Modulateurs bornés** : chaque modulateur a plage [-30%, +30%], avec pondération
3. **Plafond/plancher** : prix final dans [5, 5000] €/m³
4. **Validation** : tests de cohérence automatiques
5. **Explicabilité** : audit trail complet
6. **Mode prix sur pied vs bord de route** : sélection explicite
7. **Mise à jour temporelle** : indice FBF trimestriel
8. **Cache offline** : prix de base sync GSIE quand réseau

### 7.2 Nouvelle formule (modulateurs bornés, pas multiplicative aveugle)

```
Prix final = PrixBase(essence, produit, qualité, région, diamètre)
           × (1 + ΣModulateursBornés)        // Plage [-30%, +30%]
           × IndiceMarché(essence, année, trimestre)
           × Position(sur pied / bord route / usine)
```

Où :
- `PrixBase` : table 4D (essence×produit×qualité×région×diamètre)
- `ΣModulateursBornés` : somme pondérée de modulateurs bornés [-0.30, +0.30]
- `IndiceMarché` : indice trimestriel FBF (ex: 1.10 = +10%)
- `Position` : coefficient position (1.0, 1.32, 1.65)

### 7.3 Modulateurs bornés (avec pondération)

| Modulateur | Plage | Poids | Source |
|---|---|---|---|
| Diamètre | [-0.10, +0.40] | 1.0 | CNPF |
| Volume unitaire | [-0.10, +0.30] | 0.8 | FBF |
| Accessibilité | [-0.30, +0.00] | 0.6 | CNPF |
| Sanitaire | [-0.40, +0.10] | 0.7 | ONF |
| Longueur grume | [-0.00, +0.20] | 0.4 | Pratiques |
| Export | [+0.00, +0.30] | 0.5 | FBF |
| Certification | [+0.00, +0.15] | 0.3 | PEFC/FSC |

**Formule pondérée** :
```kotlin
val modulatorSum = (
    diameterModulator * 1.0 +
    volumeModulator * 0.8 +
    accessibilityModulator * 0.6 +
    sanitaryModulator * 0.7 +
    logLengthModulator * 0.4 +
    exportModulator * 0.5 +
    certificationModulator * 0.3
) / (1.0 + 0.8 + 0.6 + 0.7 + 0.4 + 0.5 + 0.3)  // Normalisation

val modulatorFactor = 1.0 + modulatorSum.coerceIn(-0.30, 0.30)
```

### 7.4 Plafond/plancher et validation

```kotlin
val finalPrice = (basePrice * modulatorFactor * marketIndex * positionCoef)
    .coerceIn(5.0, 5000.0)  // Plage raisonnable [5, 5000] €/m³
```

### 7.5 Architecture Kotlin (modulateurs bornés)

```kotlin
interface PriceModulator {
    fun calculate(context: PricingContext): Double  // Plage [-1.0, +1.0]
    val weight: Double
    val source: String
}

class DiameterModulator : PriceModulator {
    override val weight = 1.0
    override val source = "CNPF - Prime diamètre"
    override fun calculate(context: PricingContext): Double {
        val premium = DiameterPremium.premium(context.essenceCode, context.diamCm)
        return premium - 1.0  // Multiplicateur → delta
    }
}

class VolumeModulator : PriceModulator {
    override val weight = 0.8
    override val source = "FBF - Prime volume unitaire"
    override fun calculate(context: PricingContext): Double {
        val volume = context.volumeUnitaireM3 ?: 1.0
        val premium = VolumePremium.premium(context.essenceCode, volume)
        return premium - 1.0
    }
}

// ... SanitaryModulator, AccessibilityModulator, LogLengthModulator,
//     ExportModulator, CertificationModulator

data class ModulatorSet(
    val diameter: Double, val volume: Double, val accessibility: Double,
    val sanitary: Double, val logLength: Double, val export: Double,
    val certification: Double
) {
    val total: Double
        get() {
            val weights = listOf(1.0, 0.8, 0.6, 0.7, 0.4, 0.5, 0.3)
            val values = listOf(diameter, volume, accessibility, sanitary, logLength, export, certification)
            return values.zip(weights).sumOf { (v, w) -> v * w } / weights.sum()
        }
}
```

### 7.6 Validateur de prix

```kotlin
interface PriceValidator {
    fun validate(price: Double, context: PricingContext): ValidationResult
}

class RangePriceValidator : PriceValidator {
    override fun validate(price: Double, context: PricingContext): ValidationResult {
        val errors = mutableListOf<String>()
        if (price < 5.0) errors.add("Prix $price €/m³ < plancher 5 €/m³")
        if (price > 5000.0) errors.add("Prix $price €/m³ > plafond 5000 €/m³")
        return ValidationResult(errors.isEmpty(), errors, emptyList())
    }
}

class EssenceCoherenceValidator : PriceValidator {
    private val premiumEssences = setOf("CH_SESSILE", "CH_PEDONCULE", "NOYER_COMMUN", "CERISIER_MERIS")
    override fun validate(price: Double, context: PricingContext): ValidationResult {
        val warnings = mutableListOf<String>()
        if (context.essenceCode in premiumEssences && price < 50.0) {
            warnings.add("Prix anormalement bas pour essence premium ${context.essenceCode}")
        }
        return ValidationResult(true, emptyList(), warnings)
    }
}
```

### 7.7 Indice marché (actualisation temporelle)

```kotlin
interface MarketIndexProvider {
    fun getIndex(essence: String, year: Int, month: Int): Double
    fun getSource(context: PricingContext): String
}

class CachedMarketIndexProvider(
    private val localDataSource: LocalMarketIndexDataSource,
    private val remoteDataSource: RemoteMarketIndexDataSource?,  // GSIE
    private val cache: MarketIndexCache
) : MarketIndexProvider {
    override fun getIndex(essence: String, year: Int, month: Int): Double {
        val quarter = (month - 1) / 3 + 1
        val cacheKey = "$essence:$year:Q$quarter"
        cache.get(cacheKey)?.let { return it }
        localDataSource.getIndex(essence, year, quarter)?.let { cache.put(cacheKey, it); return it }
        remoteDataSource?.getIndex(essence, year, quarter)?.let {
            cache.put(cacheKey, it); localDataSource.save(essence, year, quarter, it); return it
        }
        return localDataSource.getNationalIndex(year, quarter) ?: 1.0
    }
}
```

---

## 8. Refonte proposée — moteur QUALITÉ/PRODUITS

### 8.1 Classification qualité normative (NF EN 1316/1927)

```kotlin
enum class WoodQualityGrade(
    val normCode: String,           // Code normalisé (Q-A, F-A, etc.)
    val normReference: String,     // NF EN 1316-1:2012 ou NF EN 1927-1:2008
    val description: String,
    val minDiameterCm: Int?,        // Diamètre minimum selon norme
    val maxDefectPct: Double?,      // Pourcentage max de défauts
    val multiplier: Double
) {
    // Codes NF EN 1316-1 (chêne/hêtre)
    OAK_QA("Q-A", "NF EN 1316-1:2012", "Qualité A chêne", 50, 5.0, 2.80),
    OAK_QB("Q-B", "NF EN 1316-1:2012", "Qualité B chêne", 35, 15.0, 1.80),
    OAK_QC("Q-C", "NF EN 1316-1:2012", "Qualité C chêne", 25, 30.0, 1.00),
    OAK_QD("Q-D", "NF EN 1316-1:2012", "Qualité D chêne", null, null, 0.55),

    // Codes NF EN 1927-1 (épicéa/sapin)
    SPRUCE_A("A", "NF EN 1927-1:2008", "Qualité A épicéa", 30, 5.0, 1.55),
    SPRUCE_B("B", "NF EN 1927-1:2008", "Qualité B épicéa", 25, 15.0, 1.20),
    // ...
}
```

### 8.2 Catalogue produits exhaustif (30+ produits)

```kotlin
enum class ForestProduct(
    val normCode: String?,          // Code normalisé si existe
    val normReference: String?,
    val category: ProductCategory,
    val minLengthM: Double?,
    val minDiameterCm: Int?,
    val typicalThicknessMm: Int?,
    val moistureClass: String?
) {
    // Feuillus (NF EN 1316-1)
    MERRAIN("MÉR", "NF EN 1316-1", PREMIUM, 2.0, 55, null, "12-15%"),
    TRANCHAGE("TR", "NF EN 1316-1", PREMIUM, 1.5, 45, null, "12-15%"),
    PLOT("PLOT", "NF EN 1316-1", INTERMEDIATE, 2.0, 35, null, "18-22%"),
    AVIVE("AVIV", "NF EN 1316-1", STANDARD, null, null, 27, "12-15%"),

    // Résineux (NF EN 1927)
    GRUME_LONGUE("GL", "NF EN 1927-1", PREMIUM, 12.0, 35, null, "18-22%"),
    POTEAU_LIGNE("PL", "NF EN 1927-1", PREMIUM, 6.0, 20, null, "18-22%"),

    // Industriels
    LAMELLE_COLLE("BLC", "NF B 52-001-1", STANDARD, null, null, null, "12-15%"),
    CONTREPLAQUE("CP", "EN 636", STANDARD, null, null, null, "8-12%"),
    // ... + parquet, menuiserie extérieure, poteau télécom, traverse SNCF
}
```

### 8.3 Prix de base sourcés (FBF/ONF/CEEB)

```kotlin
data class ProductPrice(
    val essenceCode: String,
    val productCode: String,
    val priceEurPerM3: Double,
    val priceType: PriceType,        // SUR_PIED, BORD_ROUTE, SCIE
    val region: FrenchRegion?,
    val source: PriceSource,         // ONF_T3_2024, FBF_OBS_2024, etc.
    val validFrom: LocalDate,
    val validUntil: LocalDate?,
    val qualityGrade: WoodQualityGrade?
)

enum class PriceSource {
    ONF_MERCURIALE_T3_2024,
    FBF_OBSERVATOIRE_2024,
    CEEB_ADJUDICATION_2024,
    DRAAF_REGIONALE_2024
}
```

---

## 9. Refonte proposée — moteur BIOMASSE/CARBONE

### 9.1 Suppression du double calcul de volume

**Action** : Supprimer `AdvancedCalculationEngine.volumeCoefficients` (formule simplifiée non sourcée). Utiliser uniquement `TarifData.alganCoefs` (sourcés) via wrapper :

```kotlin
fun getVolumeCoefficients(essence: String): AlganCoefs {
    return TarifData.alganCoefs.firstOrNull { it.essence.equals(essence, true) }
        ?: fallbackAlganCoefs(essence)
}
```

### 9.2 BEF par essence et âge (IPCC 2019)

```kotlin
data class BiomassExpansionFactor(
    val essenceCode: String,
    val ageClass: AgeClass,          // JEUNE, MATURE, VIEUX
    val bef: Double,
    val source: String              // IPCC 2019 Table 4.5
)

val befTable = mapOf(
    "CH_SESSILE" to mapOf(
        AgeClass.JEUNE to BiomassExpansionFactor("CH_SESSILE", AgeClass.JEUNE, 1.85, "IPCC 2019 Table 4.5"),
        AgeClass.MATURE to BiomassExpansionFactor("CH_SESSILE", AgeClass.MATURE, 1.65, "IPCC 2019 Table 4.5"),
        AgeClass.VIEUX to BiomassExpansionFactor("CH_SESSILE", AgeClass.VIEUX, 1.45, "IPCC 2019 Table 4.5")
    )
    // ...
)
```

### 9.3 RER par essence (IPCC 2019)

```kotlin
data class RootToShootRatio(
    val essenceCode: String,
    val ecologicalZone: EcologicalZone,  // GRECO
    val rer: Double,
    val source: String                   // IPCC 2019 Table 4.4
)
```

### 9.4 Densité XyloDensMap (156 espèces)

```kotlin
data class WoodDensity(
    val essenceCode: String,
    val basicDensityKgM3: Double,        // Infradensité
    val standardDeviation: Double,
    val sampleSize: Int,
    val source: String                  // XyloDensMap V2, 2022
)
// Import depuis dataset XyloDensMap (156 espèces, 110 000 arbres)
```

### 9.5 Stock carbone sol (IPCC 2019)

```kotlin
data class SoilCarbonStock(
    val ecologicalZone: EcologicalZone,
    val soilType: SoilType,
    val carbonStockTonnesPerHa: Double,
    val source: String                  // IPCC 2019
)
```

---

## 10. Refonte proposée — ARCHITECTURE

### 10.1 Structure des packages

```
domain/
├── model/              (Essence, Tree, Stand, Volume, Price, Carbon)
├── calculation/
│   ├── cubage/         (CubageStrategy interface + 15-20 impls)
│   ├── pricing/        (PricingEngine interface + ProPricingEngineV2)
│   ├── quality/        (QualityClassifier interface + impls)
│   ├── biomass/        (BiomassCalculator interface + IpccBiomassCalculator)
│   └── ForestryOrchestrator (facade simple)
├── repository/         (CoefficientRepository, EssenceRepository, PricingRepository)
└── service/            (CoefficientSyncService, OfflineCacheService)

data/
├── local/              (CanonicalEssences, CoefficientCache Room, CoefficientVersion)
├── remote/             (GsieApi Retrofit, GsieCoefficientDto, GsieSyncMapper)
└── repository/         (CoefficientRepositoryImpl, EssenceRepositoryImpl, PricingRepositoryImpl)
```

### 10.2 Interfaces clés

```kotlin
interface CubageStrategy {
    val method: TarifMethod
    fun computeVolume(essenceCode: String, diamCm: Double, hauteurM: Double?, params: CubageParams): Double?
}

interface PricingEngine {
    fun calculatePrice(context: PricingContext): PricingResult
    fun quickPrice(essenceCode: String, product: String, diamCm: Int): Double
}

interface QualityClassifier {
    fun classifyGrade(assessment: QualityAssessment): WoodQualityGrade
    fun classifyProduct(essenceCode: String, diamCm: Double, grade: WoodQualityGrade): ForestProduct
}

interface BiomassCalculator {
    fun calculateBiomass(tree: Tree): BiomassResult
    fun calculateCarbon(tree: Tree): CarbonResult
}

interface CoefficientRepository {
    suspend fun getCubageCoefficients(method: String, essenceCode: String): CubageCoefficients?
    suspend fun getPricingCoefficients(essenceCode: String): PricingCoefficients?
    suspend fun syncCoefficients(): SyncResult
    suspend fun getCoefficientVersion(): String
}
```

### 10.3 Orchestrateur facade simple

```kotlin
@Singleton
class ForestryOrchestrator @Inject constructor(
    private val cubageRegistry: CubageStrategyRegistry,
    private val pricingEngine: PricingEngine,
    private val qualityClassifier: QualityClassifier,
    private val biomassCalculator: BiomassCalculator,
    private val coefficientRepository: CoefficientRepository
) {
    suspend fun calculateTree(
        tree: Tree,
        methodCode: String,
        pricingContext: PricingContext
    ): TreeCalculationResult {
        val volume = cubageRegistry.computeVolume(methodCode, tree.essenceCode, tree.diamCm, tree.hauteurM, CubageParams())
        val grade = qualityClassifier.classifyGrade(tree.qualityAssessment)
        val product = qualityClassifier.classifyProduct(tree.essenceCode, tree.diamCm, grade)
        val price = pricingEngine.calculatePrice(pricingContext.copy(
            essenceCode = tree.essenceCode, product = product.code,
            diamCm = tree.diamCm.toInt(), qualityGrade = grade.code
        ))
        val biomass = biomassCalculator.calculateBiomass(tree)
        return TreeCalculationResult(volume, grade, product, price, biomass)
    }
}
```

### 10.4 Repository avec cache + sync GSIE

```kotlin
@Singleton
class CoefficientRepositoryImpl @Inject constructor(
    private val localCache: CoefficientCache,
    private val remoteApi: GsieApi,
    private val syncService: CoefficientSyncService
) : CoefficientRepository {

    override suspend fun getCubageCoefficients(method: String, essenceCode: String): CubageCoefficients? {
        // 1. Cache local d'abord
        localCache.getCubageCoefficients(method, essenceCode)?.let { return it }

        // 2. Si offline, fallback hardcoded
        if (!syncService.isOnline()) {
            return HardcodedCoefficients.getCubage(method, essenceCode)
        }

        // 3. Fetch remote (GSIE canal 1)
        return try {
            val dto = remoteApi.getCubageCoefficients(method, essenceCode)
            val coeffs = GsieSyncMapper.toCubageCoefficients(dto)
            localCache.saveCubageCoefficients(method, essenceCode, coeffs)
            coeffs
        } catch (e: Exception) {
            HardcodedCoefficients.getCubage(method, essenceCode)
        }
    }
}
```

---

## 11. Plan de migration incrémentale (6 phases)

### Phase 1 — Extraction interfaces (1-2 semaines)

**Objectif** : Créer interfaces sans modifier comportement existant.

1. Créer `CubageStrategy`, `PricingEngine`, `QualityClassifier`, `BiomassCalculator`, `CoefficientRepository`
2. Adapter implémentations existantes pour implémenter ces interfaces
3. Écrire tests de contrat (`CubageStrategyContractTest`, etc.)

**Tests à écrire** :
- Tests contract pour toutes implémentations
- Tests de non-régression (ancien comportement préservé)

### Phase 2 — Pattern Strategy cubage (2-3 semaines)

**Objectif** : Remplacer `when` dans TarifCalculator par registry.

1. Créer classes de stratégie (Schaeffer1EStrategy, AlganStrategy, etc.)
2. Créer `CubageStrategyRegistry`
3. Migrer TarifCalculator pour utiliser registry
4. Garder ancien code en commentaire (rollback)
5. Tests de régression (comparer ancien vs nouveau)

**Feature flag** :
```kotlin
class TarifCalculator(
    private val useStrategyPattern: Boolean = BuildConfig.ENABLE_STRATEGY_PATTERN
) {
    fun computeVolume(...): Double? {
        return if (useStrategyPattern) registry.computeVolume(...)
        else { /* ancien code avec when */ }
    }
}
```

### Phase 3 — Repository coefficients (2-3 semaines)

**Objectif** : Créer repository avec cache local.

1. Créer `CoefficientCache` (Room)
2. Créer `CoefficientRepositoryImpl`
3. Migrer TarifData pour utiliser repository
4. Implémenter fallback hardcoded
5. Tests de cache (hit, miss, expiration)

### Phase 4 — Injection Hilt (1-2 semaines)

**Objectif** : Remplacer constructeurs manuels par Hilt.

1. Ajouter dépendances Hilt
2. Créer modules Hilt
3. Injecter ForestryOrchestrator dans ViewModels
4. Tests d'injection

### Phase 5 — Sync GSIE canal 1 (3-4 semaines)

**Objectif** : Implémenter sync avec serveur GSIE.

1. Créer `GsieApi` (Retrofit)
2. Créer DTOs
3. Créer `CoefficientSyncService` (WorkManager)
4. Logique de sync (pull, merge, conflict resolution)
5. UI de configuration (URL GSIE, fréquence)
6. Tests de sync (réseau mocké)

### Phase 6 — Nettoyage (1 semaine)

**Objectif** : Supprimer code legacy.

1. Supprimer feature flags
2. Supprimer code commenté
3. Supprimer objects hardcodés (TarifData, etc.)
4. Mettre à jour documentation
5. Review finale

---

## 12. Plan de refonte priorisé (P0-P3)

### P0 — CRITIQUE (immédiat)

1. **Corriger formules Schaeffer 1949** : remplacer coef. (a,b) par formule originale V = M×(D-14)²/(45-14)²
2. **Corriger référence Schumacher-Hall** : adopter vrais coef. Vallet 2006 (GCubeR)
3. **Supprimer FGH** (redondant avec COEF_FORME)
4. **Ajouter validation domaine** : bornes D ∈ [5, 200] cm, H ∈ [5, 60] m, retourner Result<Volume, DomainError>
5. **Ajouter fallback IFN Rapide/Lent** pour essence inconnue
6. **Supprimer double calcul volume** : AdvancedCalculationEngine.volumeCoefficients → utiliser TarifData.alganCoefs
7. **Harmoniser unités densité** : adopter XyloDensMap (kg/m³ infradensité) comme source unique
8. **Documenter type de prix** (sur pied vs bord de route) pour chaque produit
9. **Ajouter garde-fou prix** : plafond/plancher [5, 5000] €/m³

### P1 — MAJEUR (à traiter dans la foulée)

10. **Ajouter prime diamètre** (modulateur borné CNPF)
11. **Ajouter prime volume unitaire** (modulateur borné FBF)
12. **Ajouter facteur sanitaire peuplement** (modulateur ONF)
13. **Ajouter indice marché trimestriel** (FBF)
14. **Ajouter équations Vallet 2006** (7 essences, 4 coef. forme de tige)
15. **Ajouter conversions volume** (écorce, bois fort, commercial)
16. **Corriger 5 densités** : Chêne vert (860), Robinier (735), Pin maritime (440), Cèdre Atlas (460), Thuya (320)
17. **Mettre à jour prix** avec données FBF 2024-2025 (notamment noyer sous-évalué, résineux sur-évalués)
18. **Étendre coefficients régionaux** à toutes essences majeures (actuellement 6 essences)
19. **Ajouter MAI** pour essences majeures (Douglas 14.4, Sapin-Épicéa 11.3, Pin maritime 8, Pin sylvestre 4.3, Hêtre 5)
20. **Adopter NF EN 1316/1927 explicitement** pour classification qualité (codes Q-A, F-A, etc.)

### P2 — IMPORTANT (à planifier)

21. **Refonte architecture** : pattern Strategy étendu, repository coefficients, injection Hilt
22. **Modulateurs bornés** (remplacer composition multiplicative aveugle)
23. **Ajouter Longuetaud 2013** (VEF, 19 essences)
24. **Cubage grume par billons** (ONF)
25. **Cubage dendromètre** (arbre debout)
26. **Raffiner BEF/RER** par essence et âge (IPCC 2019)
27. **Ajouter stock carbone sol** (IPCC 2019 ou INRAE)
28. **Vérifier coefficients de forme** contre Pardé & Bouchon 1988 (ouvrage papier)
29. **Catalogue produits exhaustif** (30+ produits : plot, avivé, BLC, contreplaqué, parquet, etc.)
30. **Prix sourcés dynamiques** (tables 4D essence×produit×qualité×région, sync trimestrielle)

### P3 — AMÉLIORATION CONTINUE

31. **Intégrer EMERGE** (58 essences, ANR-08-BIOE-003)
32. **Cubage LiDAR/photogrammétrie** (voir VOLUME_CALCULATION_NEXT_GEN.md)
33. **Intégrer Capsis** pour simulations croissance avancées
34. **Utiliser ClimEssences** pour données croissance contexte changement climatique
35. **Sync GSIE canal 1** (Knowledge Engine, cache offline, versioning)
36. **Évaluer MARGOT** pour projections territoriales (contact IGN)
37. **Méthodologie Label Bas-Carbone** compatible (spécificités France, stock sol)
38. **Sous-grades qualité premium** (chêne tranchage 1-7, alisier, érable ondé)

---

## 13. Risques et mitigations

### 13.1 Risques techniques

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| Régression de calcul | Élevée | Critique | Tests de régression ancien/nouveau, feature flags |
| Perte données coefficients | Moyenne | Critique | Backup avant migration, rollback possible |
| Performance dégradée | Moyenne | Moyenne | Benchmark avant/après, cache local |
| Sync GSIE échoue | Moyenne | Moyenne | Fallback hardcoded, mode offline garanti |
| Injection Hilt cassée | Faible | Critique | Tests d'injection, migration progressive |

### 13.2 Risques métier

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| Utilisateurs rejettent nouveaux calculs | Moyenne | Élevée | Mode parallèle (ancien/nouveau), A/B testing |
| Coefficients GSIE incorrects | Faible | Critique | Validation scientifique, checksum, rollback |
| Offline-first non respecté | Faible | Critique | Tests offline, garanties dans le code |

### 13.3 Stratégie de rollback

1. **Feature flags** : chaque phase désactivable individuellement
2. **Version APK** : garder ancienne version disponible
3. **Backup coefficients** : sauvegarder avant sync
4. **Tests de non-régression** : exécuter avant chaque release

### 13.4 Tests à écrire avant refactoring

1. **Tests de contrat** : pour chaque interface
2. **Tests de régression** : comparer résultats avant/après
3. **Tests d'intégration** : flow complet (orchestrateur)
4. **Tests offline** : vérifier mode hors-ligne
5. **Tests de sync** : mock réseau, vérifier cache
6. **Tests de cohérence inter-méthodes cubage** (manquants)
7. **Tests de cohérence cubage → biomasse** (manquants)
8. **Tests de conformité NF EN 1316/1927** (manquants)
9. **Tests de cas limites prix** (négatifs, aberrants, plafonds) (manquants)

---

## 14. Sources vérifiées (bibliographie scientifique)

### Cubage
- Schaeffer 1949 : RFF 1:7-13 — DOI 10.4267/2042/27584
- Algan 1901 : Bull. Soc. forestière Franche-Comté 6(2):123-130
- Pardé & Bouchon 1988 : Dendrométrie, ENGREF Nancy, ISBN 978-2-85710-025-6
- Vallet et al. 2006 : FEM 229:98-110 — DOI 10.1016/j.foreco.2006.03.013
- Longuetaud et al. 2013 : FEM 292:111-121 — DOI 10.1016/j.foreco.2012.12.023
- Deleuze et al. 2014 (EMERGE) : RDV Techniques ONF 44:22-32
- Chave et al. 2014 : GCB 20:3177-3190 — DOI 10.1111/gcb.12629
- GCubeR (package R) : https://github.com/cran/GCubeR
- EMERGE (gftools R) : https://rdrr.io/github/pobsteta/gftools/

### Prix
- France Bois Forêt observatoire : https://observatoire.franceboisforet.com/
- ONF mercuriales : https://www.onf.fr/produits-services/acheter-du-bois/
- CEEB observatoire bord de route
- CNPF "Estimer et vendre ses bois" (Fiche Gestion 21)
- NF EN 1316-1 (chêne/hêtre), NF EN 1927 (résineux), NF EN 1310 (défauts)

### Croissance
- Décourt & Pardé 1980 : ENGREF Nancy, ISBN 978-2-85710-016-4
- Richards 1959 : J. Exp. Bot. 10(2):290-301 — DOI 10.1093/jxb/10.2.290
- Dhôte & de Hercé 1994 : Can. J. Forest Res. 24(9):1782-1790 — DOI 10.1139/x94-230
- IGN IFN : https://inventaire-forestier.ign.fr/
- Capsis (INRAE/AMAP) : https://capsis.cirad.fr/
- ClimEssences (CNPF/ONF) : https://climessences.fr/

### Biomasse/carbone
- IPCC 2006 V4 Ch4 : https://www.ipcc-nggip.iges.or.jp/public/2006gl/pdf/4_Volume4/V4_04_Ch4_Forest_Land.pdf
- IPCC 2019 Refinement V4 Ch4 : https://www.ipcc-nggip.iges.or.jp/public/2019rf/pdf/4_Volume4/19R_V4_Ch04_Forest%20Land.pdf
- Cairns et al. 1997 : Oecologia 111(1):1-11 — DOI 10.1007/s004420050201
- XyloDensMap (INRAE/IGN) : DOI 10.57745/FA9DRA (156 espèces, 110 763 carottes)
- Label Bas-Carbone : https://label-bas-carbone.ecologie.gouv.fr/

---

## Conclusion

L'audit approfondi révèle que **les moteurs internes de GeoSylva sont fonctionnels
mais présentent 5 faiblesses structurelles majeures** :

1. **ForestryCalculator God object** (760 lignes, 7 responsabilités) — violation SRP
2. **Pas de pattern Strategy** — ajout méthode cubage = 5 fichiers à modifier (violation OCP)
3. **Composition multiplicative aveugle** dans ProPricingEngine (amplitude 592×, pas de garde-fou)
4. **Facteurs de prix structurels absents** (diamètre, volume unitaire, conjoncture marché)
5. **Coefficients hardcodés** (pas de repository, pas de cache, pas de sync GSIE, pas de versioning)

La **refonte proposée** introduit :
- **Pattern Strategy étendu** (15-20 méthodes de cubage, sélection automatique)
- **Modulateurs bornés** (remplacer composition multiplicative aveugle, plage [-30%, +30%])
- **Repository de coefficients** (cache offline + sync GSIE canal 1, versioning)
- **Injection Hilt** (testabilité, découplage)
- **Facade simple** (ForestryOrchestrator)
- **Validation automatique** (plage raisonnable [5, 5000] €/m³, cohérence essence/produit)
- **Audit trail complet** (explicabilité du prix final)

La **migration incrémentale en 6 phases** (avec feature flags, tests de régression
ancien/nouveau, rollback possible à chaque étape) minimise les risques et permet
de préserver le comportement existant pendant la transition.

Le **plan de refonte priorisé P0-P3** traite en priorité :
- **P0** : corrections critiques algorithmiques (Schaeffer, Schumacher-Hall, FGH, validation domaine, double calcul, garde-fou prix)
- **P1** : facteurs manquants (diamètre, volume unitaire, sanitaire, indice marché, Vallet 2006, densités, prix 2024-2025)
- **P2** : refonte architecture (Strategy, repository, Hilt, modulateurs bornés, Longuetaud 2013, BEF/RER par essence)
- **P3** : amélioration continue (EMERGE, LiDAR, Capsis, ClimEssences, sync GSIE, Label Bas-Carbone)

L'objectif est d'atteindre un **moteur intrinsèquement fiable** : sourcé, borné,
validé, explicable, extensible, offline-first, et préparé pour la sync GSIE.

---

*Document généré par l'Agent Dendromètre (forest-crew) + 4 subagents d'audit
algorithmique parallèles (cubage, prix, qualité/carbone, architecture). 12 fichiers
Kotlin audités, 6 suites de tests analysées (~3000 lignes), comparaison avec
méthodes professionnelles (EFF, ONF, CNPF, prix hédonistes INRA). Statut Draft —
à valider par le Fondateur avant refonte du code Kotlin.*
