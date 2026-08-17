# GeoSylva — Calcul de Volume Forestier : Nouvelle Génération

> **Document technique** — Architecture, algorithmes et IA pour le calcul de volume forestier sur mobile.
> **Auteur** : Équipe GeoSylva / Quintessences
> **Date** : 2026-07-17
> **Statut** : Draft — Phase de conception

---

## Sommaire

1. [Analyse du système existant](#1-analyse-du-système-existant)
2. [Vision nouvelle génération](#2-vision-nouvelle-génération)
3. [Méthodes de calcul de volume — classification exhaustive](#3-méthodes-de-calcul-de-volume--classification-exhaustive)
4. [Méthodes classiques — fondements et limites](#4-méthodes-classiques--fondements-et-limites)
5. [Méthodes 3D — LiDAR, photogrammétrie, NeRF, Gaussian Splatting](#5-méthodes-3d--lidar-photogrammétrie-nerf-gaussian-splatting)
6. [Méthodes IA — deep learning pour l'estimation de volume et biomasse](#6-méthodes-ia--deep-learning-pour-lestimation-de-volume-et-biomasse)
7. [Traduction en système algorithmique fonctionnel](#7-traduction-en-système-algorithmique-fonctionnel)
8. [Entraînement de modèles IA pour Android et iOS](#8-entraînement-de-modèles-ia-pour-android-et-ios)
9. [Architecture du nouveau moteur de volume GeoSylva](#9-architecture-du-nouveau-moteur-de-volume-geosylva)
10. [Intégration avec le multi-tier LLM](#10-intégration-avec-le-multi-tier-llm)
11. [Implémentation Kotlin — pseudocode](#11-implémentation-kotlin--pseudocode)
12. [Roadmap et migration](#12-roadmap-et-migration)
13. [Sources et références](#13-sources-et-références)
14. [Mesure de hauteur par capteurs mobiles — amélioration de la technologie existante](#14-mesure-de-hauteur-par-capteurs-mobiles--amélioration-de-la-technologie-existante)
15. [Détection automatique d'essence et qualité bois par IA](#15-détection-automatique-dessence-et-qualité-bois-par-ia)
16. [Martelage assisté par IA et projection de croissance interactive](#16-martelage-assisté-par-ia-et-projection-de-croissance-interactive)
17. [Certification carbone MRV, inventaire collaboratif et optimisation économique](#17-certification-carbone-mrv-inventaire-collaboratif-et-optimisation-économique)

---

## 1. Analyse du système existant

### 1.1 Architecture actuelle

GeoSylva dispose actuellement de **5 calculateurs** organisés en couches :

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                       │
│  MartelageScreen · PlacetteEvolution · TarifDocumentation    │
├─────────────────────────────────────────────────────────────┤
│                    COUCHE DOMAIN                             │
│  ┌─────────────────┐  ┌──────────────────┐                  │
│  │ ForestryCalculator │  │ ExpertForestryCalculator        │
│  │  (tarifs, coefs F) │  │  (Richards, Schumacher-Hall)    │
│  └────────┬────────┘  └────────┬─────────┘                  │
│           │                     │                            │
│  ┌────────▼──────────────────────▼─────────┐                 │
│  │    EnhancedForestryCalculator            │                 │
│  │    (combine base + expert + diagnostic)  │                 │
│  └─────────────────────────────────────────┘                 │
│  ┌─────────────────────────────────────────┐                 │
│  │    DecoupeCalculator                     │                 │
│  │    (ventilation par produit BO/BI/BCh)   │                 │
│  └─────────────────────────────────────────┘                 │
│  ┌─────────────────────────────────────────┐                 │
│  │    VolumeConversion                      │                 │
│  │    (stère ↔ m³)                          │                 │
│  └─────────────────────────────────────────┘                 │
├─────────────────────────────────────────────────────────────┤
│                    COUCHE DATA                               │
│  ┌─────────────────────────────────────────┐                 │
│  │    AdvancedCalculationEngine             │                 │
│  │    (biomasse IPCC, carbone, stats,       │                 │
│  │     corrélation, biodiversité, risque)   │                 │
│  └─────────────────────────────────────────┘                 │
├─────────────────────────────────────────────────────────────┤
│                    COUCHE PARAMÈTRES                         │
│  ParameterRepository (coefs volume, hauteurs, tarifs, prix)  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Méthodes de cubage actuelles

Le `TarifCalculator` (@`TarifCalculator.kt:22`) implémente **7 méthodes** :

| Méthode | Entrées | Principe | Précision |
|---|---|---|---|
| **Schaeffer 1 entrée** | D uniquement | Table de coefficients par numéro de tarif (1-16) | ±10-15% |
| **Schaeffer 2 entrées** | D + H | Table de coefficients par numéro de tarif (1-8) | ±8-12% |
| **Algan** | D + H | Équation polynomiale par essence (a₀ + a₁·D²·H + a₂·D·H + ...) | ±7-10% |
| **IFN rapide** | D uniquement | Tarifs nationaux IFN (36 tarifs) | ±10-15% |
| **IFN lent** | D + H | Tarifs nationaux IFN (8 tarifs) | ±8-10% |
| **FGH** | D + H + f | V = f × G × H (coefficient de forme × surface terrière × hauteur) | ±5-10% |
| **CoefForme** | D + H + f | V = (π/4) × D² × H × f | ±5-10% |

### 1.3 Modèles scientifiques existants

Le `ExpertForestryCalculator` (@`ExpertForestryCalculator.kt:42`) ajoute :

- **Tables de production Décourt & Pardé (1980)** — chêne (3 stations) et hêtre (2 stations), interpolation linéaire par âge
- **Modèle de croissance Richards (1959)** — D(t) = A / (1 + exp(−k·(t−t₀)))^b, paramètres par essence + classe de station
- **Volume Schumacher-Hall (1933)** — V = exp(a + b·ln(D) + c·ln(H)), paramètres par essence
- **Indice de station ONF** — proxy Hdom, classification I-VII
- **Surface terrière AFNOR NF B53-005** — G = Σ(π × (D/200)²)

### 1.4 Biomasse et carbone

Le `AdvancedCalculationEngine` (@`AdvancedCalculationEngine.kt:17`) applique la chaîne **IPCC 2006** :

```
Volume fût (m³) = a × D^b × H^c
  → × densité bois (t/m³) × 1000 = biomasse fût sec (kg)
    → × BEF (1.65 feuillus / 1.45 résineux) = biomasse aérienne (kg)
      → × (1 + RER 0.25) = biomasse totale (kg)
        → × 0.50 = carbone (kg)
          → × 3.67 = CO₂-équivalent (kg)
```

### 1.5 Limites du système actuel

| Limite | Impact | Cause racine |
|---|---|---|
| **Volume 1D (D + H uniquement)** | Pas de prise en compte de la forme réelle du tronc, des fourches, des courbures | Système basé sur des équations paramétriques par essence, pas sur la géométrie réelle |
| **Coefficients statiques** | Les coefs Algan/Schaeffer sont calibrés sur des peuplements des années 1970-1980 | Pas d'adaptation aux conditions stationnelles réelles, au climat actuel |
| **Pas de données 3D** | Impossible de détecter les défauts de forme, ventru, conicité variable | Aucun capteur 3D intégré (LiDAR, photogrammétrie) |
| **Biomasse IPCC générique** | BEF et RER fixes (1.65/1.45, 0.25) — ne varient pas par essence, âge, station | Valeurs par défaut IPCC, pas calibrées sur données françaises récentes |
| **Pas d'incertitude quantifiée** | L'utilisateur n'a pas d'intervalle de confiance sur les volumes | Système déterministe, pas bayésien |
| **Pas d'apprentissage** | Le système ne s'améliore pas avec les données terrain collectées | Pas de boucle de rétroaction, pas de ML |
| **Découpe rigide** | Ventilation BO/BI/BCh/PATE par règles statiques de diamètre | Pas de détection visuelle des défauts, pas de ML sur la qualité |
| **Pas de validation terrain intégrée** | L'utilisateur ne peut pas comparer ses volumes avec des arbres abattus mesurés | Pas de module de validation post-exploitation |

---

## 2. Vision nouvelle génération

### 2.1 Paradigme

Le système actuel est **paramétrique 1D** : il utilise des équations calibrées sur des mesures manuelles (D, H). La nouvelle génération est **hybride multi-niveaux** :

```
NIVEAU 0 — Algorithmique pure (T0-T1)
  D + H manuels → équations classiques améliorées (Algan, Schumacher-Hall)
  + incertitude bayésienne
  + calibration locale automatique

NIVEAU 1 — Photogrammétrie smartphone (T2-T3)
  Photos/vidéos → SfM → point cloud 3D → DBH, hauteur, forme du tronc
  + modèles allométriques 3D
  + détection défauts visuels

NIVEAU 2 — LiDAR smartphone (T3-T4)
  LiDAR intégré (iPhone Pro, iPad Pro) → point cloud dense
  → segmentation tronc/couronne → QSM (Quantitative Structure Model)
  → volume réel par intégration géométrique

NIVEAU 3 — IA hybride on-device (T4-T5)
  Point cloud + photos → réseau de neurones on-device
  → estimation volume + biomasse + qualité bois
  → incertitude quantifiée
  + apprentissage fédéré (amélioration continue)

NIVEAU 4 — Cloud + IA avancée (T5 + cloud fallback)
  Données 3D + satellite + inventaires → modèles transformer
  → projection croissance + optimisation sylvicole
  → calibration essence/station personnalisée
```

### 2.2 Objectifs quantifiés

| Métrique | Système actuel | Objectif nouvelle génération |
|---|---|---|
| Précision volume individuel | ±8-15% | **±3-5%** (Niveau 2+), **±1-3%** (Niveau 3+) |
| Précision biomasse | ±20-30% (IPCC générique) | **±5-10%** (modèles ML calibrés) |
| Incertitude fournie | Non | **Oui** — intervalle de confiance à 90% |
| Temps de mesure par arbre | 30-60s (D + H manuels) | **5-10s** (scan LiDAR/photo) |
| Détection défauts | Non | **Oui** — fourches, courbures, cavités |
| Calibration locale | Manuelle (paramètres) | **Automatique** (apprentissage sur données terrain) |
| Validation post-exploitation | Non | **Oui** — comparaison volume estimé vs réel |

### 2.3 Principes directeurs

1. **Dégradation gracieuse** — du scan 3D IA jusqu'aux équations classiques selon le device
2. **Incertitude explicite** — chaque estimation de volume inclut un intervalle de confiance
3. **Amélioration continue** — les données terrain alimentent un modèle qui s'affine
4. **Validation croisée** — les volumes estimés sont comparés aux volumes réels d'exploitation
5. **Multi-capteurs** — LiDAR, caméra, GPS, capteur inertiels combinés
6. **Offline-first** — toute l'inférence 3D et IA tourne on-device, le cloud est optionnel

---

## 3. Méthodes de calcul de volume — classification exhaustive

### 3.1 Taxonomie complète

```
CALCUL DE VOLUME FORESTIER
│
├── A. MÉTHODES CLASSIQUES (1D — diamètre + hauteur)
│   ├── A1. Tarifs de cubage (Schaeffer, IFN, Algan)
│   ├── A2. Équations allométriques (Schumacher-Hall, Volume = f(D,H))
│   ├── A3. Coefficient de forme (FGH, CoefForme)
│   ├── A4. Tables de production (Décourt & Pardé, ONF)
│   └── A5. Volume par classe de diamètre (tarifs locaux)
│
├── B. MÉTHODES GÉOMÉTRIQUES 3D (point cloud)
│   ├── B1. Intégration de profil (tranches perpendiculaires)
│   │   ├── B1a. Méthode des tranches (slices) — Smalian, Huber, Newton
│   │   ├── B1b. Intégration trapézoïdale du taper
│   │   └── B1c. Intégration de Simpson
│   ├── B2. QSM (Quantitative Structure Model)
│   │   ├── B2a. TreeQSM — cylindres ajustés sur le point cloud
│   │   ├── B2b. SimpleForest — segmentation + fitting géométrique
│   │   └── B2c. AdTree — reconstruction d'arbre topologique
│   ├── B3. Convex Hull / Alpha Shape
│   │   ├── B3a. Convex Hull du tronc — volume enveloppe convexe
│   │   └── B3b. Alpha Shape — enveloppe non-convexe (concavités)
│   ├── B4. Modèle de taper polynomial
│   │   ├── B4a. Kozak (1988) — équation variable-exponent
│   │   ├── B4b. Max & Burkhart (1976) — polynôme segmenté
│   │   └── B4c. Bi (2000) — taper trigonométrique
│   └── B5. Voxelisation
│       ├── B5a. Comptage de voxels occupés
│       └── B5b. Octree adaptatif
│
├── C. MÉTHODES PHOTOGRAMMÉTRIQUES
│   ├── C1. Structure from Motion (SfM)
│   │   ├── C1a. COLMAP / OpenMVS — pipeline complet
│   │   ├── C1b. ARCore / ARKit — SfM temps réel on-device
│   │   └── C1c. NeRFCapture (iOS) — NeRF on-device
│   ├── C2. NeRF (Neural Radiance Fields)
│   │   ├── C2a. NeRF classique — MLP 5D → (σ, rgb)
│   │   ├── C2b. Instant-NGP — NeRF accéléré (hash grid)
│   │   └── C2c. Mobile-NeRF — optimisé pour mobile
│   └── C3. 3D Gaussian Splatting (3DGS)
│       ├── C3a. ForestSplat — drone + 3DGS pour canopy
│       ├── C3b. TreeDGS — 3DGS aérien pour DBH
│       └── C3c. Mobile 3DGS — splatting on-device
│
├── D. MÉTHODES IA / DEEP LEARNING
│   ├── D1. Régression directe (point cloud → volume)
│   │   ├── D1a. PointNet++ — segmentation wood/leaf + régression
│   │   ├── D1b. DGCNN — Dynamic Graph CNN
│   │   ├── D1c. OctCNN — Octree-based CNN
│   │   └── D1d. Point Transformer — attention sur points 3D
│   ├── D2. Régression par projection 2D
│   │   ├── D2a. CoAtNet — projection 3D→2D + CNN
│   │   ├── D2b. ResNet V2 — transfer learning
│   │   └── D2c. MobileNetV2 — léger pour mobile
│   ├── D3. Régression multi-modal
│   │   ├── D3a. Photo + D + H → volume (MLP / XGBoost)
│   │   ├── D3b. Point cloud + photo + météo → biomasse
│   │   └── D3c. Multi-task (volume + biomasse + qualité)
│   ├── D4. Modèles bayésiens
│   │   ├── D4a. Gaussian Process — incertitude native
│   │   ├── D4b. Bayesian Neural Network — poids distribués
│   │   └── D4c. MC Dropout — approximation bayésienne
│   └── D5. Transfer learning & fine-tuning
│       ├── D5a. Pré-entraînement sur TLS → adaptation mobile
│       ├── D5b. Pré-entraînement sur forêts tropicales → tempérées
│       └── D5c. Domain adaptation (jour/nuit, saison)
│
└── E. MÉTHODES HYBRIDES
    ├── E1. Géométrique + IA (QSM + neural refinement)
    ├── E2. Classique + IA (Algan + correction ML)
    ├── E3. Multi-échelle (drone + sol + satellite)
    └── E4. Actif/itératif (scan + suggestion de meilleure position)
```

### 3.2 Matrice de sélection par contexte

| Contexte | Méthode recommandée | Précision attendue | Device requis |
|---|---|---|---|
| Inventaire rapide (pas de scan) | A2 (Schumacher-Hall) + D4a (GP) | ±8-12% | T0+ (manuel) |
| Inventaire avec photo | C1b (ARCore SfM) + B1a | ±5-8% | T2+ (caméra) |
| Inventaire LiDAR smartphone | B2a (TreeQSM) + B1b | ±3-5% | T3+ (LiDAR) |
| Arbre de valeur (cubage précis) | B2a + D1a (PointNet++) | ±1-3% | T4+ (LiDAR + NPU) |
| Peuplement entier (drone) | C3a (ForestSplat) + E3 | ±5-8% | T3+ + drone |
| Biomasse carbone | D1a + D4a (GP) | ±5-10% | T3+ |
| Validation post-exploitation | B1a (tranches) + E2 | ±1-2% | T2+ |

---

## 4. Méthodes classiques — fondements et limites

### 4.1 Équations de volume 1D — état de l'art

#### 4.1.1 Schumacher-Hall (1933)

```
V = exp(a + b·ln(D) + c·ln(H))
```

Paramètres actuels dans GeoSylva (@`ExpertForestryCalculator.kt:527-533`) :

| Essence | a | b | c |
|---|---|---|---|
| Chêne (QUPE) | -2.0 | 2.0 | 1.0 |
| Hêtre (FASY) | -2.2 | 2.1 | 0.95 |
| Sapin (ABAL) | -1.8 | 1.9 | 1.05 |
| Défaut | -2.0 | 2.0 | 1.0 |

**Limite** : ces coefficients sont des **valeurs approximatives** non calibrées sur données françaises récentes. Les vrais coefficients Schumacher-Hall par essence française proviennent de publications INRAE/IGN spécifiques.

#### 4.1.2 Modèle de taper de Kozak (1988)

Le modèle de taper à exposant variable permet de prédire le diamètre à **toute hauteur** du tronc :

```
d(h) = D × [ (1 - √(h/H)) / (1 - √(p)) ]^b₁ + b₂·(√(h/H) - p) + b₃·(√(h/H) - p)²
```

où :
- `d(h)` = diamètre à la hauteur h
- `D` = diamètre à 1.30m
- `H` = hauteur totale
- `p` = proportion de hauteur où l'inflexion se produit (~0.25-0.30)
- `b₁, b₂, b₃` = paramètres par essence

**Avantage** : permet de calculer le volume **par section** (bois fort, bois moyen, petit bois) et de simuler des découpes.

#### 4.1.3 Volume par intégration de profil

Si on dispose de **mesures de diamètre à plusieurs hauteurs** (d₁₃₀, d₃, d₅, d₇...), trois formules classiques :

**Formule de Smalian** (tranches parallèles) :
```
V_section = (A₁ + A₂) / 2 × L
```
où A₁, A₂ = sections aux deux extrémités, L = longueur de la section.

**Formule de Huber** (section médiane) :
```
V_section = A_médiane × L
```

**Formule de Newton** (3 sections) :
```
V_section = (A₁ + 4·A_médiane + A₂) / 6 × L
```

La formule de Newton est la plus précise (exacte pour un paraboloïde, un cône ou un néloïde) mais nécessite 3 mesures par section.

### 4.2 Limites fondamentales des méthodes 1D

| Problème | Description | Impact sur le volume |
|---|---|---|
| **Forme du tronc non mesurée** | Un chêne ventru et un chêne conique de même D et H ont des volumes différents | ±5-15% d'erreur |
| **Fourches non détectées** | Une fourche à 6m crée deux volumes distincts | ±10-30% d'erreur |
| **Courbure non mesurée** | Un tronc courbe a plus de volume qu'un tronc droit de même D et H | ±2-5% |
| **Galles et excroissances** | Non visibles dans D + H | ±3-8% |
| **Hauteur de souche** | La hauteur de coupe affecte le volume commercial | ±2-5% |
| **Découpe commerciale** | Le diamètre de découpe (7cm, 22cm) détermine le volume utile | Variable |

### 4.3 Améliorations algorithmiques possibles (sans 3D)

Même sans scan 3D, le système classique peut être amélioré :

1. **Calibration bayésienne locale** — ajuster les coefficients sur les données terrain collectées
2. **Modèle de taper intégré** — Kozak ou Max-Burkhart pour la ventilation par produit
3. **Incertitude quantifiée** — Gaussian Process sur les résidus d'équation
4. **Métadonnées stationnelles** — ajuster le coefficient de forme selon l'indice de station
5. **Correction climatique** — les arbres poussent différemment en 2026 qu'en 1980

---

## 5. Méthodes 3D — LiDAR, photogrammétrie, NeRF, Gaussian Splatting

### 5.1 Capteurs disponibles sur mobile (2025-2026)

| Capteur | Devices | Précision spatiale | Débit | Usage forestier |
|---|---|---|---|---|
| **LiDAR ToF (iPhone Pro/Pro Max)** | iPhone 12 Pro → 17 Pro, iPad Pro | 1-3 cm à 5m | 10-30 Hz | Scan tronc, DBH, forme |
| **LiDAR ToF (Android)** | Samsung S24 Ultra, Pixel 9 Pro | 2-5 cm à 5m | 10-20 Hz | Scan tronc (moins précis) |
| **ARCore (Android)** | Tous Android 7+ (API 24+) | 5-10 cm (SfM) | 30-60 Hz | SfM temps réel, DBH approximatif |
| **ARKit (iOS)** | Tous iOS 11+ | 1-5 cm (SfM + IMU) | 30-60 Hz | SfM temps réel, DBH |
| **Caméra standard** | Tous | N/A (2D) | 30-120 Hz | Photos pour SfM, NeRF, détection défauts |
| **IMU (gyro + accéléromètre)** | Tous | N/A | 100-1000 Hz | Stabilisation, estimation hauteur |
| **GPS / GNSS** | Tous | 3-10 m (1m RTK) | 1-10 Hz | Géolocalisation arbres |

### 5.2 Structure from Motion (SfM) — pipeline mobile

Le SfM reconstruit un point cloud 3D à partir de **photos multiples** d'un même objet sous angles différents.

```
PHOTOS (30-100 images)
  │
  ▼
[1] Détection de points d'intérêt (SIFT / SURF / ORB)
  │
  ▼
[2] Appariement inter-images (matching)
  │
  ▼
[3] Reconstruction sparse (SfM) — poses caméras + point cloud sparse
  │
  ▼
[4] Reconstruction dense (MVS) — point cloud dense
  │
  ▼
[5] Mesh + texture (optionnel)
  │
  ▼
POINT CLOUD 3D → extraction dendrométrique
```

**Implémentations mobiles** :
- **ARKit / ARCore** : SfM temps réel intégré (API native, pas de pipeline séparé)
- **COLMAP** : pipeline complet offline (desktop, mais possible via cloud)
- **OpenMVS** : reconstruction dense open source
- **Luma AI (iOS)** : NeRF on-device via app

**Performance forestière** (études récentes) :
- DBH : RMSE 1.52 cm (SfM smartphone, Cáceres 2026)
- Hauteur : RMSE 0.95 m (iPhone SfM, olive trees, 2024)
- Volume canopée : RMSE 25.85 m³ (iPhone SfM vs MLS)

### 5.3 NeRF (Neural Radiance Fields) pour la forêt

Le NeRF entraîne un **réseau de neurones** à représenter une scène 3D continue à partir d'images 2D :

```
F(x, y, z, θ, φ) → (σ, r, g, b)
```

où σ = densité volumétrique, (r,g,b) = couleur, (θ,φ) = direction de vue.

**Variantes pertinentes** :
- **Instant-NGP** : hash grid + MLP léger → entraînement en secondes/minutes
- **Mobile-NeRF** : optimisé pour rendu mobile (textures + mesh léger)
- **NeRFCapture (iOS)** : capture NeRF on-device via ARKit

**Performance forestière** :
- DBH : RMSE 1.60 cm (Gaussian Splatting, Cáceres 2026)
- Hauteur : RMSE 1.26 m (iPhone NeRF, olive trees, 2024)
- Volume canopée : RMSE 33.79 m³ (iPhone NeRF vs MLS)
- NeRF > SfM pour la qualité de reconstruction du tronc et de la canopée

**Limite** : le NeRF classique est **lent à entraîner** (heures). Instant-NGP réduit à minutes. Sur mobile, on utilise plutôt le **rendu** que l'entraînement.

### 5.4 3D Gaussian Splatting (3DGS) — la nouvelle frontière

Le 3DGS représente la scène comme un ensemble de **Gaussiennes 3D** (position, covariance, couleur, opacité) :

```
Scène = { G_i(μ_i, Σ_i, α_i, c_i) } pour i = 1..N
```

**Avantages sur NeRF pour la forêt** :
- Rendu **temps réel** sur mobile (rasterization, pas de ray marching)
- Entraînement **plus rapide** (minutes vs heures)
- Export en **point cloud** directement exploitable
- Qualité de reconstruction **supérieure** pour les structures fines (branches)

**Études forestières 2025** :
- **ForestSplat** : drone + 3DGS → canopy height map, MAE 0.17m vs LiDAR aéroporté, **100× moins cher**
- **TreeDGS** : 3DGS aérien pour DBH, RMSE 4.79 cm (supérieur au LiDAR aérien 7.66 cm)
- **Comparaison MLS vs SfM vs GS vs iPad-LiDAR** (Cáceres 2026) :
  - MLS : RMSE DBH 1.29 cm (référence)
  - SfM : RMSE DBH 1.52 cm
  - GS : RMSE DBH 1.60 cm
  - iPad-LiDAR : RMSE DBH 2.26 cm

**Conclusion** : le 3DGS depuis smartphone est **compétitif avec le LiDAR professionnel** pour le DBH, tout en étant accessible à tous.

### 5.5 LiDAR smartphone — exploitation directe

Les iPhones Pro et certains Android haut de gamme intègrent un LiDAR ToF :

**Pipeline LiDAR on-device** :
```
1. Capture ARKit/ARCore → depth map + pose caméra
2. Accumulation → point cloud dense (slam)
3. Segmentation sol / tronc / branches (RANSAC + clustering)
4. Extraction DBH (fit de cercle RANSAC à 1.30m)
5. Extraction hauteur (point le plus haut du tronc)
6. Extraction profil (diamètres à multiples hauteurs)
7. Intégration de profil → volume réel
```

**Performance** (étude Apóstolo et al. 2026, Pinus pinaster, validation destructive) :
- Volume tronc : MAPE 9.28%, biais +2.27%
- Hauteur : MAPE 4.04%, biais +0.13%
- DBH : MAPE 3.77%, biais +0.13%

**Étude Cakir et al. (2023)** — Huawei P30 Pro LiDAR, forêt complexe :
- DBH : RMSE 3.7 cm, R² = 0.97, 100% détection
- Temps réduit par **4.6×** vs mesure manuelle

### 5.6 QSM (Quantitative Structure Model)

Le QSM reconstruit l'**architecture complète** de l'arbre (tronc + branches) à partir d'un point cloud :

```
POINT CLOUD (TLS / MLS / SfM / LiDAR smartphone)
  │
  ▼
[1] Segmentation sol / végétation (CSF, RANSAC)
  │
  ▼
[2] Segmentation arbre individuel (DBSCAN, TLS2trees)
  │
  ▼
[3] Segmentation tronc / branches (classification sémantique)
  │
  ▼
[4] Fitting de cylindres / cônes par segment (TreeQSM, SimpleForest)
  │
  ▼
[5] Modèle topologique (graphe : tronc → branches → rameaux)
  │
  ▼
QSM = { segments cylindriques connectés }
  │
  ▼
Volume total = Σ(π × r_i² × h_i) pour chaque segment
Volume tronc = Σ(segments tronc)
Volume branches = Σ(segments branches)
Biomasse = Σ(volume_i × densité_essence(section))
```

**Logiciels open source** :
- **TreeQSM** (Raumonen et al., 2013) — MATLAB, référence scientifique
- **SimpleForest** (Hackenberg et al., 2015) — C++, open source
- **AdTree** (Du et al., 2019) — reconstruction topologique
- **TLS2trees** — segmentation d'arbres individuels

**Précision QSM** (validation destructive, Demol et al. 2021) :
- DBH : MAE 1.17 cm
- Hauteur : MAE 0.54 m
- Volume tronc : erreur < 5% (conditions optimales)

---

## 6. Méthodes IA — deep learning pour l'estimation de volume et biomasse

### 6.1 Régression directe : point cloud → volume

#### 6.1.1 PointNet++ (segmentation wood/leaf + régression)

**Architecture** :
```
Point cloud (N × 3)
  │
  ▼
Set Abstraction Layer 1 (sous-échantillonnage + grouping)
  │ → (N/4 × 64) features
  ▼
Set Abstraction Layer 2
  │ → (N/16 × 128) features
  ▼
Set Abstraction Layer 3
  │ → (N/64 × 256) features
  ▼
Feature Propagation (upsampling + skip links)
  │ → (N × 64) features par point
  ▼
MLP → segmentation par point (wood / leaf / ground)
  │
  ▼
Global Feature → MLP → régression (volume, biomasse)
```

**Performance** (CarbonScan-AI, 2026) :
- Wood IoU (synthetic) : 0.978
- Wood IoU (réel, zero-shot) : 0.18 → 0.42 (avec fine-tuning)
- DBH MAE : 1.17 cm, Hauteur MAE : 0.54 m

**Déploiement mobile** : TFLite / PyTorch Mobile, modèle quantifié INT8, 5-20 MB.

#### 6.1.2 CoAtNet (projection 3D→2D + CNN)

**Principe** : projeter le point cloud 3D en images 2D multi-vues, puis utiliser un CNN 2D (plus efficace sur mobile qu'un réseau 3D).

```
Point cloud 3D
  │
  ▼
Projection multi-vue (6 vues orthogonales)
  │ → 6 images depth + 6 images intensity
  ▼
CoAtNet (CNN + Transformer)
  │ → features par vue
  ▼
Fusion multi-vue → MLP → biomasse (kg)
```

**Performance** (Wei et al. 2025, forêt tempérée mixte) :
- R² = 0.73, erreur médiane 0.99%, MAPE 25.06%
- Supérieur à Random Forest et Point Transformer

**Avantage mobile** : les CNN 2D sont **très bien supportés** par TFLite, Core ML, NNAPI.

#### 6.1.3 DGCNN et OctCNN

- **DGCNN** (Dynamic Graph CNN) : construit un graphe k-NN dynamique entre points, applique des convolutions sur les arêtes. R² = 0.76 pour biomasse (Hell et al. 2023).
- **OctCNN** (Octree-based CNN) : structure hiérarchique octree, efficace pour grands point clouds. R² = 0.76, MAPE réduit de 20% vs Random Forest.

### 6.2 Régression multi-modal

#### 6.2.1 Photo + D + H → volume (MLP / XGBoost)

Quand on n'a pas de scan 3D, on peut enrichir les mesures manuelles avec des **features extraits d'une photo** :

```
ENTRÉES :
  - D (cm) — manuel
  - H (m) — manuel ou estimé photo
  - Features photo (CNN pré-entraîné) :
    · ratio hauteur/largeur du tronc
    · texture de l'écorce (essence)
    · présence de fourche (binaire)
    · courbure apparente (continu 0-1)
    · diamètre apparent à mi-hauteur (estimé)

MODÈLE :
  XGBoost ou MLP (64→32→16→1)
  Entrée : [D, H, f1, f2, f3, f4, f5, essence_id, station_id]

SORTIE :
  Volume (m³) + intervalle de confiance
```

**Avantage** : fonctionne sur **tous les devices** (T0+), améliore la précision de ±10-15% à ±5-8%.

#### 6.2.2 Multi-task : volume + biomasse + qualité

Un seul réseau prédit simultanément plusieurs cibles :

```
Backbone partagé (MobileNetV2 ou EfficientNet-Lite)
  │
  ├── Head volume → régression (m³)
  ├── Head biomasse → régression (kg)
  ├── Head qualité → classification (1-4)
  └── Head défauts → multi-label (fourche, cavité, courbe)

Loss = α·MSE(volume) + β·MSE(biomasse) + γ·CE(qualité) + δ·BCE(défauts)
```

### 6.3 Modèles bayésiens pour l'incertitude

#### 6.3.1 Gaussian Process (GP)

Le GP fournit nativement une **distribution prédictive** (mean + variance) :

```
f(x) ~ GP(m(x), k(x, x'))
  Volume prédit = μ(x*) ± 1.645·σ(x*)  (IC 90%)
```

**Avantage** : l'incertitude est **calibrée** — elle augmente quand on s'éloigne des données d'entraînement.

**Implémentation mobile** : GP approximé par **inducing points** (200-500 points), kernel RBF ou Matérn. Taille : 1-5 MB.

#### 6.3.2 MC Dropout (approximation bayésienne sur NN)

Au lieu d'un GP séparé, on peut utiliser un réseau de neurones avec **dropout actif à l'inférence** :

```
N passes forward avec dropout actif
  → N prédictions différentes
  → mean = prédiction, std = incertitude
```

**Coût** : N inférences (N=10-50), mais chaque inférence est rapide sur NPU/GPU.

### 6.4 Transfer learning pour la forêt française

#### 6.4.1 Pré-entraînement → adaptation

```
PHASE 1 — Pré-entraînement (cloud, off-device)
  Dataset : FOR-CE (IGN) + ForestGEO + TLS public datasets
  Modèle : PointNet++ ou CoAtNet
  Tâche : segmentation wood/leaf + régression volume
  → modèle pré-entraîné "forest foundation model"

PHASE 2 — Adaptation locale (on-device, federated)
  Données utilisateur : scans LiDAR + mesures manuelles
  Méthode : LoRA (Low-Rank Adaptation) ou fine-tuning dernières couches
  → modèle personnalisé par utilisateur / région

PHASE 3 — Apprentissage fédéré (cloud, agrégation)
  Updates locaux → serveur → agrégation (FedAvg)
  → modèle global amélioré redistribué
```

#### 6.4.2 Datasets d'entraînement disponibles

| Dataset | Type | Taille | Accès | Couverture |
|---|---|---|---|---|
| **FOR-CE** (IGN France) | Inventaire national | 100k+ placettes | Public (IGN) | France métropolitaine |
| **ForestGEO** | Inventaire global | 7M+ arbres | Public (Smithsonian) | Tropical + tempéré |
| **Demol et al. 2021** | TLS + destructive | 65 arbres | Public (Zenodo) | Belgique (feuillus) |
| **Wan et al. 2021** | TLS + labels wood/leaf | ~100 arbres | Public | Chine |
| **Hannah et al.** | TLS + MLS + ULS | Multi-sensor | Public | Europe |
| **ApostoloLDS** | HMLS + destructive | 45 arbres | Public (GitHub) | Portugal (Pinus pinaster) |
| **AI-Benchmark** | Photos + D + H | Variable | App | Global |

---

## 7. Traduction en système algorithmique fonctionnel

### 7.1 Pattern stratégique : Strategy + Pipeline

Le nouveau système utilise le pattern **Strategy** pour sélectionner dynamiquement la méthode de calcul selon les données disponibles et le device :

```kotlin
interface VolumeStrategy {
    fun estimate(inputs: VolumeInputs): VolumeEstimate
    val requiredData: Set<DataType>
    val requiredTier: DeviceTier
    val precisionClass: PrecisionClass
}

class VolumeEngine {
    private val strategies: List<VolumeStrategy>

    fun selectStrategy(inputs: VolumeInputs, caps: DeviceCapabilities): VolumeStrategy {
        return strategies
            .filter { it.requiredData.all { d -> inputs.has(d) } }
            .filter { caps.tier.ordinal >= it.requiredTier.ordinal }
            .maxByOrNull { it.precisionClass.ordinal }
            ?: throw IllegalStateException("Aucune stratégie viable")
    }
}
```

### 7.2 Types de données d'entrée

```kotlin
enum class DataType {
    DIAMETER_MANUAL,       // D à 1.30m mesuré manuellement
    HEIGHT_MANUAL,         // H mesurée manuellement
    HEIGHT_ESTIMATED,      // H estimée par modèle
    PHOTO_SINGLE,          // Une seule photo
    PHOTO_MULTI,           // Plusieurs photos (SfM possible)
    VIDEO_SCAN,            // Vidéo (SfM ou NeRF possible)
    LIDAR_SCAN,            // Scan LiDAR (point cloud)
    POINT_CLOUD_SFM,       // Point cloud reconstruit par SfM
    POINT_CLOUD_LIDAR,     // Point cloud LiDAR direct
    QSM_MODEL,             // Quantitative Structure Model
    ESSENCE_CODE,          // Code essence
    STATION_DATA,          // Données station (IS, sol, climat)
    AGE,                   // Âge du peuplement
    GPS_LOCATION,          // Position GPS
    PREVIOUS_INVENTORY,    // Inventaire précédent (même placette)
    HARVEST_DATA           // Données d'exploitation (validation)
}
```

### 7.3 Pipeline de calcul universel

```
ENTRÉES UTILISATEUR
  │
  ▼
[1] VALIDATION — complétude des données, détection d'erreurs
  │
  ▼
[2] SÉLECTION DE STRATÉGIE — selon données disponibles + device tier
  │
  ▼
[3] PRÉTRAITEMENT — normalisation, cleaning, feature extraction
  │
  ├── Si photo(s) → extraction features CNN (MobileNetV2)
  ├── Si point cloud → segmentation, RANSAC, QSM
  └── Si manuel → interpolation hauteur, calibration locale
  │
  ▼
[4] ESTIMATION — exécution de la stratégie sélectionnée
  │
  ├── Classique : équation (Algan, Schumacher-Hall, Kozak)
  ├── 3D : intégration de profil, QSM, voxel
  ├── IA : inférence réseau de neurones on-device
  └── Hybride : géométrique + correction IA
  │
  ▼
[5] INCERTITUDE — calcul de l'intervalle de confiance
  │
  ├── GP : σ native
  ├── MC Dropout : N passes → std
  ├── Analytique : propagation d'erreur sur les équations
  └── Empirique : historique des résidus par essence/station
  │
  ▼
[6] POST-TRAITEMENT — ventilation par produit, valorisation
  │
  ├── Découpe commerciale (Kozak taper → BO/BI/BCh/PATE)
  ├── Prix par produit et qualité
  └── Biomasse + carbone (IPCC amélioré ou ML)
  │
  ▼
[7] VALIDATION — comparaison avec données de référence
  │
  ├── Tables ONF (Décourt & Pardé)
  ├── Inventaires précédents (même placette)
  └── Données d'exploitation (si disponibles)
  │
  ▼
SORTIE : VolumeEstimate
  ├── volume_m3: Double
  ├── confidence_interval: Pair<Double, Double>  // IC 90%
  ├── method_used: VolumeStrategy
  ├── precision_class: PrecisionClass
  ├── breakdown_by_product: Map<String, Double>
  ├── biomass_kg: Double
  ├── carbon_kg: Double
  ├── quality_assessment: QualityAssessment
  └── validation_flags: List<ValidationFlag>
```

### 7.4 Calibration locale automatique

Le système apprend des **résidus passés** pour corriger les équations :

```
Pour chaque essence + station :
  1. Collecter les paires (volume_estimé, volume_réel)
     - volume_réel = données d'exploitation (grumes mesurées)
     - volume_estimé = volume prédit par l'équation
  2. Calculer le ratio moyen r = mean(volume_réel / volume_estimé)
  3. Ajuster : volume_corrigé = volume_estimé × r_local
  4. Incertitude de correction : σ_correction = std(ratios)
  5. Si n > 30 : utiliser une régression locale (D, H → correction)
  6. Si n > 100 : entraîner un petit MLP de correction
```

Cette calibration est **persistante** (Room database) et **synchronisée** entre devices (apprentissage fédéré).

---

## 8. Entraînement de modèles IA pour Android et iOS

### 8.1 Pipeline d'entraînement complet

```
PHASE 1 — Collecte de données (cloud)
  │
  ├── Datasets publics (FOR-CE, Demol, Wan, ForestGEO)
  ├── Données GeoSylva (scans utilisateurs anonymisés)
  └── Données de validation (exploitation forestière)
  │
  ▼
PHASE 2 — Préparation (cloud, Python/PyTorch)
  │
  ├── Nettoyage : outliers, doublons, erreurs de mesure
  ├── Normalisation : coordonnées centrées, échelle métrique
  ├── Augmentation :
  │   · Rotation 3D aléatoire
  │   · Bruit gaussien sur points (simulateur capteur)
  │   · Occlusion aléatoire (suppression de points)
  │   · Variation de densité (sous-échantillonnage)
  │   · Changement de saison (textures d'écorce)
  └── Split : train (70%) / val (20%) / test (10%)
  │
  ▼
PHASE 3 — Entraînement (cloud, GPU)
  │
  ├── Modèle backbone : PointNet++ ou CoAtNet
  ├── Loss : MSE (volume) + Dice (segmentation) + CE (qualité)
  ├── Optimiseur : AdamW, lr=1e-3, cosine schedule
  ├── Epochs : 100-300, early stopping (patience=20)
  └── Validation : R², RMSE, MAPE sur set de validation
  │
  ▼
PHASE 4 — Optimisation mobile (cloud)
  │
  ├── Quantification : FP32 → INT8 (TFLite converter)
  ├── Pruning : suppression des weights faibles (30-50%)
  ├── Distillation : modèle étudiant plus petit
  └── Conversion :
      · Android : TFLite (.tflite) ou PyTorch Mobile (.ptl)
      · iOS : Core ML (.mlmodel) ou TFLite
  │
  ▼
PHASE 5 — Déploiement OTA
  │
  ├── Modèle packagé avec métadonnées (version, essences, précision)
  ├── Téléchargement progressif (seulement si device tier compatible)
  ├── A/B testing (ancien vs nouveau modèle sur subset)
  └── Rollback automatique si dégradation
  │
  ▼
PHASE 6 — Apprentissage fédéré (optionnel, T4+)
  │
  ├── On-device fine-tuning sur données utilisateur
  ├── Agrégation FedAvg sur serveur
  ├── Redistribution du modèle global amélioré
  └── Privacy : données braves jamais envoyées, seulement les gradients
```

### 8.2 Modèles spécifiques par task

#### 8.2.1 Modèle DBH + Hauteur depuis point cloud

```
Architecture : PointNet++ (segmentation) + MLP (régression)
Input : point cloud (N=4096 points, xyz)
Output : DBH (cm), Hauteur (m), confiance

Entraînement :
  - Dataset : TLS + MLS + smartphone LiDAR
  - Loss : MSE(DBH) + MSE(Hauteur) + Dice(segmentation wood/leaf)
  - Augmentation : bruit capteur, occlusion, variation densité

Optimisation mobile :
  - INT8 quantization → 4 MB
  - Latence : 50-200ms sur NPU, 200-500ms sur GPU, 1-3s sur CPU
```

#### 8.2.2 Modèle volume + biomasse depuis photo

```
Architecture : MobileNetV2 (features) + MLP (régression)
Input : photo 224×224×3 + [D, H, essence_id]
Output : volume (m³), biomasse (kg), IC 90%

Entraînement :
  - Dataset : photos terrain + volumes mesurés (exploitation)
  - Transfer learning : ImageNet → forest-specific
  - Loss : Huber loss (robuste aux outliers)

Optimisation mobile :
  - INT8 → 2-3 MB
  - Latence : 20-50ms sur NPU, 50-100ms sur GPU
```

#### 8.2.3 Modèle qualité bois depuis photo

```
Architecture : EfficientNet-Lite0 (classification multi-label)
Input : photo tronc 224×224×3
Output : qualité (1-4), défauts (fourche, cavité, courbe, galle)

Entraînement :
  - Dataset : photos + annotations expertes (martelage)
  - Loss : CE(qualité) + BCE(défauts)
  - Augmentation : rotation, couleur, flou

Optimisation mobile :
  - INT8 → 1-2 MB
  - Latence : 15-30ms sur NPU
```

#### 8.2.4 Modèle de correction d'équation (calibration locale)

```
Architecture : XGBoost (100 arbres, max_depth=4)
Input : [D, H, essence_id, station_id, mois, altitude, pente]
Output : correction_factor (ratio réel/estimé)

Entraînement :
  - Dataset : paires (estimé, réel) collectées par l'utilisateur
  - On-device : XGBoost léger (1-5 MB)
  - Mise à jour : incrémentale (new data → retrain last 10 trees)

Avantage : pas de réseau de neurones, interprétable, rapide
```

### 8.3 Stack technique d'entraînement

| Composant | Technologie | Usage |
|---|---|---|
| **Entraînement** | PyTorch 2.x + CUDA | Cloud, GPU A100/H100 |
| **Dataset management** | HuggingFace Datasets + DVC | Versioning, splits |
| **Tracking** | Weights & Biases / MLflow | Métriques, hyperparams |
| **Quantification** | TFLite Converter / PyTorch Quantization | FP32 → INT8 |
| **Conversion iOS** | Core ML Tools (coremltools) | PyTorch → .mlmodel |
| **Conversion Android** | TFLite Converter | PyTorch → .tflite |
| **Federated** | Flower / PySyft | FedAvg, privacy |
| **Déploiement** | Firebase ML / Custom OTA | Distribution modèles |
| **A/B testing** | Firebase Remote Config | Comparaison modèles |

### 8.4 Inférence on-device — stack par plateforme

| Plateforme | Framework | Accélération | Modèles supportés |
|---|---|---|---|
| **Android T3+** | TFLite | GPU (OpenCL) / NNAPI (NPU) | .tflite (INT8, FP16) |
| **Android T4+** | TFLite + QNN delegate | Hexagon NPU direct | .tflite + QNN |
| **Android T5** | PyTorch Mobile / ExecuTorch | NPU + GPU | .ptl / .pte |
| **iOS T3+** | Core ML | Neural Engine + GPU | .mlmodel (quantized) |
| **iOS T4+** | Core ML + Metal Performance Shaders | Neural Engine v2 | .mlpackage |
| **Cross-platform** | ONNX Runtime Mobile | CPU / GPU / NPU | .onnx (quantized) |

### 8.5 Tailles et latences cibles

| Modèle | Taille (INT8) | Latence NPU | Latence GPU | Latence CPU |
|---|---|---|---|---|
| DBH + Hauteur (PointNet++) | 4-8 MB | 50-200ms | 200-500ms | 1-3s |
| Volume + Biomasse (MobileNetV2) | 2-3 MB | 20-50ms | 50-100ms | 200-500ms |
| Qualité bois (EfficientNet-Lite0) | 1-2 MB | 15-30ms | 30-60ms | 100-300ms |
| Correction locale (XGBoost) | 1-5 MB | N/A | N/A | 5-20ms |
| Gaussian Process (inducing 200) | 1-3 MB | N/A | N/A | 10-50ms |
| Segmentation wood/leaf (PointNet++) | 5-10 MB | 100-300ms | 300-800ms | 2-5s |

---

## 9. Architecture du nouveau moteur de volume GeoSylva

### 9.1 Vue d'ensemble

```
┌──────────────────────────────────────────────────────────────────────┐
│               GEOSYLVA VOLUME ENGINE — NEXT GEN                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │  Data Capture │  │  Data Fusion │  │  Strategy    │               │
│  │  Module       │  │  Module      │  │  Selector    │               │
│  │               │  │              │  │              │               │
│  │ · Manual D/H  │  │ · Merge      │  │ · Device     │               │
│  │ · Photo capture│ │   sensors    │  │   tier check │               │
│  │ · LiDAR scan  │  │ · Validate   │  │ · Data       │               │
│  │ · Video scan  │  │   completeness│  │   availability│              │
│  │ · GPS         │  │ · Error      │  │ · Precision  │               │
│  │               │  │   detection  │  │   preference │               │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         │                 │                 │                        │
│         └─────────────────┼─────────────────┘                        │
│                           │                                          │
│                           ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   VOLUME ENGINE CORE                          │   │
│  │                                                              │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐            │   │
│  │  │ Classical   │ │ Geometric3D │ │ AI Engine   │            │   │
│  │  │ Strategies  │ │ Strategies  │ │             │            │   │
│  │  │             │ │             │ │             │            │   │
│  │  │ · Algan     │ │ · Profile   │ │ · PointNet++│            │   │
│  │  │ · Schum-H   │ │   integr.   │ │ · CoAtNet   │            │   │
│  │  │ · Kozak     │ │ · QSM       │ │ · MobileNet │            │   │
│  │  │ · IFN       │ │ · Voxel     │ │ · GP        │            │   │
│  │  │ · FGH       │ │ · ConvexHull│ │ · MC Dropout│            │   │
│  │  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘            │   │
│  │         │               │               │                    │   │
│  │         └───────────────┼───────────────┘                    │   │
│  │                         ▼                                    │   │
│  │              ┌──────────────────┐                            │   │
│  │              │  Hybrid Combiner │                            │   │
│  │              │  (weighted avg,  │                            │   │
│  │              │   Bayesian fus.) │                            │   │
│  │              └────────┬─────────┘                            │   │
│  └───────────────────────┼─────────────────────────────────────┘   │
│                          │                                          │
│                          ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   POST-PROCESSING                             │   │
│  │                                                              │   │
│  │  · Uncertainty quantification (GP / MC / analytic)           │   │
│  │  · Product breakdown (Kozak taper → BO/BI/BCh/PATE)          │   │
│  │  · Biomass + Carbon (IPCC improved or ML)                    │   │
│  │  · Quality assessment (ML or rules)                          │   │
│  │  · Local calibration correction                              │   │
│  │  · Validation against reference tables                       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                          │                                          │
│                          ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   OUTPUT: VolumeEstimate                      │   │
│  │                                                              │   │
│  │  · volume_m3 + IC 90%                                       │   │
│  │  · method_used + precision_class                             │   │
│  │  · product_breakdown + value_eur                             │   │
│  │  · biomass_kg + carbon_kg + co2_kg                          │   │
│  │  · quality_assessment                                        │   │
│  │  · validation_flags                                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              LEARNING LOOP (T2+)                              │   │
│  │                                                              │   │
│  │  · Local calibration store (Room)                            │   │
│  │  · Residual history by essence/station                       │   │
│  │  · Federated learning (T4+, opt-in)                          │   │
│  │  · Harvest validation module                                 │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### 9.2 Hiérarchie des stratégies

Le moteur sélectionne **automatiquement** la meilleure stratégie selon un score de priorité :

| Priorité | Stratégie | Données requises | Tier min | Précision |
|---|---|---|---|---|
| 1 | **AIHybridStrategy** | point_cloud + photo | T4 | ±1-3% |
| 2 | **QSMStrategy** | point_cloud_lidar | T3 | ±2-5% |
| 3 | **ProfileIntegrationStrategy** | point_cloud_sfm | T2 | ±3-5% |
| 4 | **PhotoEnhancedStrategy** | photo + D + H | T2 | ±5-8% |
| 5 | **KozakTaperStrategy** | D + H + essence | T0 | ±5-10% |
| 6 | **SchumacherHallStrategy** | D + H + essence | T0 | ±7-10% |
| 7 | **AlganStrategy** | D + H + essence | T0 | ±7-10% |
| 8 | **IFNStrategy** | D + essence | T0 | ±10-15% |
| 9 | **FallbackStrategy** | D uniquement | T0 | ±15-20% |

**Règle** : le moteur utilise la stratégie de **plus haute priorité** dont toutes les données requises sont disponibles et dont le tier device est suffisant.

### 9.3 Fusion bayésienne multi-stratégies

Quand **plusieurs stratégies** sont disponibles, le moteur les combine par fusion bayésienne :

```
Soit N stratégies produisant (V_i, σ_i) pour i = 1..N

V_fusionné = Σ(V_i / σ_i²) / Σ(1 / σ_i²)
σ_fusionné = 1 / √(Σ(1 / σ_i²))
```

Cette fusion donne plus de poids aux stratégies avec faible incertitude. Si une stratégie 3D donne ±3% et une équation classique donne ±10%, la fusion est dominée par la 3D mais l'équation sert de **garde-fou**.

### 9.4 Module de validation post-exploitation

```
ARBRES ABATTUS
  │
  ▼
Mesure réelle du volume (grumes billonnées)
  │
  ▼
Comparaison avec volume estimé par GeoSylva
  │
  ▼
Calcul du ratio : réel / estimé
  │
  ▼
Stockage dans calibration store (essence, station, D, H, méthode, ratio)
  │
  ▼
Si n > 10 par essence+station :
  → calcul du biais moyen et correction automatique
  → mise à jour de l'incertitude empirique
  → suggestion de re-calibration des équations
```

---

## 10. Intégration avec le multi-tier LLM

### 10.1 Rôles du LLM dans le calcul de volume

| Tier LLM | Rôle dans le calcul de volume |
|---|---|
| **T0 (Legacy)** | Aucun — algorithmes purs |
| **T1 (Low)** | Aucun — algorithmes purs + calibration locale |
| **T2 (Mid)** | Explication des résultats, suggestions de mesure |
| **T3 (Mid+)** | Analyse d'anomalies, recommandations de méthode |
| **T4 (High)** | Interprétation 3D, diagnostic qualité, rapport automatique |
| **T5 (Ultra)** | Co-pilotage complet : suggestion de scan, analyse temps réel, optimisation |

### 10.2 Prompts intégrés

**T2+ — Explication de résultat** :
```
SYSTEM: Tu es un expert dendrométrique. Explique le résultat de volume
à un forestier. Sois concis, technique, en français.

USER: Volume estimé : 1.42 m³ (IC 90%: 1.28-1.56)
      Méthode : Kozak taper + correction locale
      Essence : Chêne sessile, D=42cm, H=25m
      Précision : ±8%
      Flags : hauteur estimée (non mesurée)
```

**T4+ — Analyse 3D** :
```
SYSTEM: Tu es un expert en scan 3D forestier. Analyse le QSM
reconstruit et identifie les anomalies.

USER: QSM : 47 segments, volume total 2.1 m³
      Anomalies détectées :
      - Fourche à 6.2m (deux branches de 15cm)
      - Courbure tronc : 3.2°
      - Cavité probable à 1.8m (densité de points anormale)
      Volume tronc principal : 1.8 m³
      Volume branches : 0.3 m³
```

**T5 — Co-pilotage scan** :
```
SYSTEM: Tu es un assistant de scan forestier en temps réel.
Guide l'utilisateur pour optimiser la capture.

USER: Scan en cours, 45% de couverture du tronc.
      Points accumulés : 12,400
      Zones manquantes : dos du tronc (sud)
      Device : iPhone 17 Pro (LiDAR + NPU)

ASSISTANT: Continue en contournant le tronc vers la droite.
           Tu as 45% de couverture. Vise 80%+ pour un QSM de
           qualité. Attention aux branches basses à 2m qui
           pourraient occlure la zone sud. ~15s restantes.
```

---

## 11. Implémentation Kotlin — pseudocode

### 11.1 Modèles de données

```kotlin
enum class PrecisionClass { LOW, MEDIUM, HIGH, ULTRA }

enum class VolumeMethod {
    ALGAN, SCHUMACHER_HALL, KOZAK_TAPER, IFN_RAPIDE, IFN_LENT,
    FGH, COEF_FORME, PROFILE_INTEGRATION, QSM, VOXEL,
    PHOTO_ENHANCED, AI_HYBRID, BAYESIAN_FUSION
}

data class VolumeInputs(
    val diameterCm: Double?,
    val heightM: Double?,
    val essenceCode: String?,
    val stationId: String?,
    val age: Int?,
    val photos: List<PhotoData>,
    val pointCloud: PointCloudData?,
    val qsmModel: QsmModel?,
    val gpsLocation: GpsLocation?,
    val previousInventory: PreviousInventoryData?,
    val harvestData: HarvestData?,
    val deviceCapabilities: DeviceCapabilities
) {
    fun has(dataType: DataType): Boolean = when (dataType) {
        DataType.DIAMETER_MANUAL -> diameterCm != null && diameterCm > 0
        DataType.HEIGHT_MANUAL -> heightM != null && heightM > 0
        DataType.PHOTO_SINGLE -> photos.isNotEmpty()
        DataType.PHOTO_MULTI -> photos.size >= 10
        DataType.VIDEO_SCAN -> false // géré séparément
        DataType.LIDAR_SCAN -> pointCloud?.source == PointCloudSource.LIDAR
        DataType.POINT_CLOUD_SFM -> pointCloud?.source == PointCloudSource.SFM
        DataType.POINT_CLOUD_LIDAR -> pointCloud?.source == PointCloudSource.LIDAR
        DataType.QSM_MODEL -> qsmModel != null
        DataType.ESSENCE_CODE -> essenceCode != null
        DataType.STATION_DATA -> stationId != null
        DataType.AGE -> age != null
        DataType.GPS_LOCATION -> gpsLocation != null
        DataType.PREVIOUS_INVENTORY -> previousInventory != null
        DataType.HARVEST_DATA -> harvestData != null
        DataType.HEIGHT_ESTIMATED -> heightM != null && heightM > 0
    }
}

data class VolumeEstimate(
    val volumeM3: Double,
    val confidenceInterval: Pair<Double, Double>,
    val methodUsed: VolumeMethod,
    val precisionClass: PrecisionClass,
    val productBreakdown: Map<String, Double>,
    val valueEur: Double?,
    val biomassKg: Double?,
    val carbonKg: Double?,
    val co2Kg: Double?,
    val qualityAssessment: QualityAssessment?,
    val validationFlags: List<ValidationFlag>,
    val metadata: VolumeMetadata
)

data class VolumeMetadata(
    val timestamp: Long,
    val deviceTier: DeviceTier,
    val strategiesEvaluated: List<VolumeMethod>,
    val strategiesUsed: List<VolumeMethod>,
    val fusionWeights: Map<VolumeMethod, Double>?,
    val calibrationApplied: Boolean,
    val calibrationSampleSize: Int,
    val processingTimeMs: Long
)

data class QualityAssessment(
    val qualityGrade: Int,           // 1-4
    val defects: List<WoodDefect>,
    val confidence: Double
)

data class WoodDefect(
    val type: DefectType,            // FORK, CAVITY, CURVE, GALL, CRACK
    val severity: Double,            // 0.0-1.0
    val positionM: Double?,          // hauteur sur le tronc
    val detectedBy: DefectDetectionMethod // MANUAL, PHOTO_AI, POINT_CLOUD
)

enum class ValidationFlag {
    HEIGHT_ESTIMATED_NOT_MEASURED,
    LOW_POINT_CLOUD_DENSITY,
    OCCLUDED_TRUNK_REGION,
    CALIBRATION_SAMPLE_TOO_SMALL,
    VOLUME_EXCEEDS_REFERENCE_TABLE,
    VOLUME_BELOW_REFERENCE_TABLE,
    BIOMASS_IPCC_GENERIC_NOT_CALIBRATED,
    QUALITY_ASSESSED_BY_AI_NOT_EXPERT
}
```

### 11.2 Interface de stratégie

```kotlin
interface VolumeStrategy {
    val method: VolumeMethod
    val requiredData: Set<DataType>
    val requiredTier: DeviceTier
    val precisionClass: PrecisionClass

    fun canHandle(inputs: VolumeInputs): Boolean {
        return requiredData.all { inputs.has(it) } &&
               inputs.deviceCapabilities.tier.ordinal >= requiredTier.ordinal
    }

    suspend fun estimate(inputs: VolumeInputs): StrategyResult
}

data class StrategyResult(
    val volumeM3: Double,
    val uncertaintySigma: Double,
    val method: VolumeMethod,
    val metadata: Map<String, Any>
)
```

### 11.3 Stratégies concrètes

```kotlin
class KozakTaperStrategy(
    private val parameterRepository: ParameterRepository,
    private val calibrationStore: CalibrationStore
) : VolumeStrategy {

    override val method = VolumeMethod.KOZAK_TAPER
    override val requiredData = setOf(
        DataType.DIAMETER_MANUAL, DataType.HEIGHT_MANUAL, DataType.ESSENCE_CODE
    )
    override val requiredTier = DeviceTier.T0_LEGACY
    override val precisionClass = PrecisionClass.MEDIUM

    override suspend fun estimate(inputs: VolumeInputs): StrategyResult {
        val d = inputs.diameterCm!!
        val h = inputs.heightM!!
        val essence = inputs.essenceCode!!

        val params = getKozakParameters(essence)
        val volumeRaw = integrateTaper(d, h, params)
        val correction = calibrationStore.getCorrection(essence, inputs.stationId, d, h)
        val volumeCorrected = volumeRaw * correction.factor
        val sigma = volumeCorrected * correction.uncertaintyPercent / 100.0

        return StrategyResult(
            volumeM3 = volumeCorrected,
            uncertaintySigma = sigma,
            method = method,
            metadata = mapOf(
                "kozak_params" to params,
                "calibration_factor" to correction.factor,
                "calibration_n" to correction.sampleSize,
                "raw_volume" to volumeRaw
            )
        )
    }

    private fun integrateTaper(d: Double, h: Double, params: KozakParams): Double {
        // Intégration trapézoïdale du taper de Kozak sur 100 sections
        val nSections = 100
        val dh = h / nSections
        var volume = 0.0
        for (i in 0 until nSections) {
            val h1 = i * dh
            val h2 = (i + 1) * dh
            val d1 = kozakDiameter(d, h, h1, params)
            val d2 = kozakDiameter(d, h, h2, params)
            val a1 = PI / 4.0 * (d1 / 100.0).pow(2)
            val a2 = PI / 4.0 * (d2 / 100.0).pow(2)
            volume += (a1 + a2) / 2.0 * dh
        }
        return volume
    }

    private fun kozakDiameter(d: Double, h: Double, hi: Double, p: KozakParams): Double {
        if (hi <= 0.0) return d
        if (hi >= h) return 0.0
        val z = hi / h
        val x = (1.0 - sqrt(z)) / (1.0 - sqrt(p.p))
        val exponent = p.b1 + p.b2 * (sqrt(z) - p.p) + p.b3 * (sqrt(z) - p.p).pow(2)
        return d * x.pow(exponent)
    }
}
```

### 11.4 Moteur principal

```kotlin
class VolumeEngine(
    private val strategies: List<VolumeStrategy>,
    private val deviceProfiler: DeviceCapabilityProfiler,
    private val calibrationStore: CalibrationStore,
    private val llmEngine: LlmEngineManager?
) {

    suspend fun estimate(inputs: VolumeInputs): VolumeEstimate {
        val startTime = System.currentTimeMillis()
        val caps = inputs.deviceCapabilities

        // 1. Sélectionner les stratégies viables
        val viable = strategies
            .filter { it.canHandle(inputs) }
            .sortedByDescending { it.precisionClass.ordinal }

        require(viable.isNotEmpty()) { "Aucune stratégie de volume viable" }

        // 2. Exécuter les top stratégies (max 3 pour performance)
        val topStrategies = viable.take(3)
        val results = topStrategies.map { strategy ->
            runCatching { strategy.estimate(inputs) }
                .getOrElse { null }
        }.filterNotNull()

        // 3. Fusion bayésienne
        val fusedVolume: Double
        val fusedSigma: Double
        val methodsUsed: List<VolumeMethod>
        val fusionWeights: Map<VolumeMethod, Double>?

        if (results.size == 1) {
            fusedVolume = results[0].volumeM3
            fusedSigma = results[0].uncertaintySigma
            methodsUsed = listOf(results[0].method)
            fusionWeights = null
        } else {
            val weights = results.associate { it.method to (1.0 / (it.uncertaintySigma.pow(2))) }
            val totalWeight = weights.values.sum()
            fusedVolume = results.sumOf { (it.volumeM3 / it.uncertaintySigma.pow(2)) } / totalWeight
            fusedSigma = 1.0 / sqrt(totalWeight)
            methodsUsed = results.map { it.method }
            fusionWeights = weights.mapValues { it.value / totalWeight }
        }

        // 4. Intervalle de confiance à 90%
        val ic90 = Pair(
            fusedVolume - 1.645 * fusedSigma,
            fusedVolume + 1.645 * fusedSigma
        )

        // 5. Post-traitement
        val productBreakdown = computeProductBreakdown(
            fusedVolume, inputs, fusedSigma
        )
        val valueEur = computeValue(productBreakdown, inputs)
        val biomass = computeBiomass(fusedVolume, inputs)
        val quality = assessQuality(inputs, caps)
        val flags = generateValidationFlags(inputs, results, fusedVolume)

        // 6. Métadonnées
        val metadata = VolumeMetadata(
            timestamp = System.currentTimeMillis(),
            deviceTier = caps.tier,
            strategiesEvaluated = viable.map { it.method },
            strategiesUsed = methodsUsed,
            fusionWeights = fusionWeights,
            calibrationApplied = calibrationStore.hasCalibration(
                inputs.essenceCode, inputs.stationId
            ),
            calibrationSampleSize = calibrationStore.sampleSize(
                inputs.essenceCode, inputs.stationId
            ),
            processingTimeMs = System.currentTimeMillis() - startTime
        )

        return VolumeEstimate(
            volumeM3 = fusedVolume,
            confidenceInterval = ic90,
            methodUsed = methodsUsed.first(),
            precisionClass = topStrategies.first().precisionClass,
            productBreakdown = productBreakdown,
            valueEur = valueEur,
            biomassKg = biomass?.totalKg,
            carbonKg = biomass?.carbonKg,
            co2Kg = biomass?.co2Kg,
            qualityAssessment = quality,
            validationFlags = flags,
            metadata = metadata
        )
    }
}
```

### 11.5 Module de calibration locale

```kotlin
class CalibrationStore(
    private val database: CalibrationDatabase
) {

    suspend fun getCorrection(
        essenceCode: String,
        stationId: String?,
        diameterCm: Double?,
        heightM: Double?
    ): CorrectionFactor {
        val records = database.getRecords(essenceCode, stationId)

        if (records.size < 5) {
            return CorrectionFactor(1.0, 12.0, records.size)
        }

        val ratios = records.map { it.realVolume / it.estimatedVolume }
        val meanRatio = ratios.average()
        val stdRatio = ratios.standardDeviation()
        val uncertainty = stdRatio / meanRatio * 100.0

        return CorrectionFactor(meanRatio, uncertainty, records.size)
    }

    suspend fun recordHarvest(
        essenceCode: String,
        stationId: String?,
        estimatedVolume: Double,
        realVolume: Double,
        method: VolumeMethod,
        diameterCm: Double,
        heightM: Double
    ) {
        database.insert(CalibrationRecord(
            essenceCode = essenceCode,
            stationId = stationId,
            estimatedVolume = estimatedVolume,
            realVolume = realVolume,
            ratio = realVolume / estimatedVolume,
            method = method,
            diameterCm = diameterCm,
            heightM = heightM,
            timestamp = System.currentTimeMillis()
        ))
    }

    suspend fun hasCalibration(essenceCode: String, stationId: String?): Boolean {
        return database.getRecordCount(essenceCode, stationId) >= 5
    }

    suspend fun sampleSize(essenceCode: String, stationId: String?): Int {
        return database.getRecordCount(essenceCode, stationId)
    }
}

data class CorrectionFactor(
    val factor: Double,
    val uncertaintyPercent: Double,
    val sampleSize: Int
)
```

---

## 12. Roadmap et migration

### 12.1 Phases de développement

| Phase | Durée | Contenu | Priorité |
|---|---|---|---|
| **Phase 1 — Refactor classical** | 2-3 semaines | Kozak taper, incertitude analytique, calibration store, validation post-exploitation | Haute |
| **Phase 2 — Photo enhanced** | 3-4 semaines | MobileNetV2 features photo, modèle correction XGBoost, qualité bois ML | Haute |
| **Phase 3 — SfM pipeline** | 4-6 semaines | ARCore/ARKit SfM, segmentation point cloud, RANSAC DBH, intégration de profil | Moyenne |
| **Phase 4 — LiDAR smartphone** | 4-6 semaines | Capture LiDAR, QSM léger, voxel counting, extraction forme tronc | Moyenne |
| **Phase 5 — AI on-device** | 6-8 semaines | PointNet++ TFLite, CoAtNet Core ML, GP incertitude, fusion bayésienne | Moyenne |
| **Phase 6 — Federated learning** | 4-6 semaines | Fine-tuning on-device, FedAvg, OTA model distribution | Basse |
| **Phase 7 — 3DGS / NeRF** | 8-12 semaines | Gaussian Splatting mobile, NeRF capture, rendu + extraction | Basse |

### 12.2 Stratégie de migration

```
ÉTAT ACTUEL (v1)
  ForestryCalculator + TarifCalculator + ExpertForestryCalculator
  + EnhancedForestryCalculator + AdvancedCalculationEngine
  │
  ▼
MIGRATION PROGRESSIVE (v2)
  │
  ├── Phase 1 : VolumeEngine remplace ForestryCalculator
  │   └── Stratégies classiques (Algan, Schumacher-Hall, Kozak) dans VolumeEngine
  │   └── Anciens calculateurs gardés en fallback (dépréciés)
  │
  ├── Phase 2 : PhotoEnhancedStrategy ajoutée au VolumeEngine
  │   └── Modèle MobileNetV2 pour features photo
  │   └── Calibration XGBoost locale
  │
  ├── Phase 3-4 : Stratégies 3D ajoutées
  │   └── ProfileIntegrationStrategy (SfM)
  │   └── QSMStrategy (LiDAR)
  │
  ├── Phase 5 : AIHybridStrategy ajoutée
  │   └── PointNet++ on-device
  │   └── Fusion bayésienne multi-stratégies
  │
  └── Phase 6-7 : Amélioration continue
      └── Apprentissage fédéré
      └── 3DGS / NeRF pour reconstruction avancée
```

### 12.3 Compatibilité ascendante

- Les **anciens calculateurs** restent disponibles comme stratégies fallback
- Les **paramètres existants** (coefs volume, hauteurs, tarifs) sont réutilisés
- Les **données existantes** (placettes, tiges) sont compatibles
- L'**UI existante** évolue progressivement (ajout bouton scan, affichage IC)

---

## 13. Sources et références

### 13.1 Méthodes classiques

- Pardé, J. & Bouchon, J. (1988). *Dendrométrie*. ENGREF Nancy, 2e édition.
- Décourt, N. & Pardé, J. (1980). *Tables de production pour les forêts françaises*. ENGREF Nancy.
- Schumacher, F.X. & Hall, F.S. (1933). *Logarithmic expression of timber-tree volume*. J. Agricultural Research, 47, 719-734.
- Kozak, A. (1988). *A variable-exponent taper equation*. Canadian Journal of Forest Research, 18(11), 1363-1368.
- Max, T.A. & Burkhart, H.E. (1976). *Segmented polynomial regression applied to taper equations*. Forest Science, 22(3), 283-289.
- Richards, F.J. (1959). *A flexible growth function for empirical use*. J. Experimental Botany, 10(29), 290-300.
- Dhôte, J.F. & de Hercé, F. (1994). *Un modèle hyperbolique pour l'évolution du diamètre dominant*. Ann. Sci. For., 51, 257-282.
- IPCC (2006). *Guidelines for National Greenhouse Gas Inventories — AFOLU*. Volume 4.
- Cairns, M.A. et al. (1997). *Root biomass allocation in the world's upland forests*. Oecologia, 111, 1-11.

### 13.2 Méthodes 3D et capteurs mobiles

- Apóstolo, P. et al. (2026). *Open-source handheld mobile LiDAR pipeline for tree detection, stem reconstruction and volume estimation*. GitHub: Apostolo6/ApostoloLDS. Validation destructive sur 45 Pinus pinaster.
- Cakir, M. et al. (2023). *Robust Single-Image Tree Diameter Estimation with Mobile Phones*. Remote Sensing, 15(3), 772. RMSE 3.7 cm, R²=0.97.
- Toma, M. et al. (2024). *Comparing 3D olive tree models: smartphone LiDAR, SfM and NeRF*. ISPRS Annals, X-3-2024, 61-2024.
- Wei, J. et al. (2025). *Individual trunk segmentation and DBH estimation using mobile LiDAR scanning*. Forests, 16(4), 582. 97.4% segmentation, 3.2 cm RMSE.
- Cabo, C. et al. (2026). *Active and passive 3D sensing for forest stem geometry: MLS, consumer LiDAR, SfM and Gaussian Splatting*. CUEX, 2. RMSE DBH: MLS 1.29cm, SfM 1.52cm, GS 1.60cm, iPad-LiDAR 2.26cm.
- Gonçalves, N. et al. (2023). *New technologies for expedited forest inventory using smartphone applications*. Forests, 14(8), 1553. Évaluation Katam, Arboreal, Trestima.
- Demol, M. et al. (2021). *Validation of forest QSM from TLS*. MAE DBH 1.17cm, hauteur 0.54m.

### 13.3 NeRF et Gaussian Splatting forestier

- Shaheen, B. et al. (2025). *ForestSplat: Scalable forestry mapping using 3D Gaussian Splatting*. Remote Sensing, 17(6), 993. MAE 0.17m vs LiDAR aéroporté, 100× moins cher.
- Li, J. et al. (2025). *TreeDGS: Aerial Gaussian Splatting for distant DBH measurement*. Remote Sensing, 18(6), 867. RMSE 4.79 cm.
- Kerbl, B. et al. (2023). *3D Gaussian Splatting for Real-Time Radiance Field Rendering*. ACM TOG, 42(4), 1-14.
- Mason, E. et al. (2025). *Democratizing 3D ecology: Mobile NeRF for scalable ecosystem mapping*. EcoEvoRxiv. NeRF mobile comparable à TLS en canopée ouverte.
- Huang, H. et al. (2024). *Evaluating point clouds of individual trees from NeRF*. Remote Sensing, 16(6), 967.

### 13.4 Deep learning pour la forêt

- Wei, J. et al. (2025). *Individual tree biomass estimation using single-scan TLS with CoAtNet*. J. Forestry, Springer. R²=0.73, MAPE 25.06%.
- Hell, M. et al. (2023). *Modelling tree biomass using direct and additive methods with point cloud deep learning*. Sensing and Geospatial Sciences, 9, 100110. DGCNN + OctCNN, R²=0.76.
- Qi, C.R. et al. (2017). *PointNet++: Deep Hierarchical Feature Learning on Point Sets in a Metric Space*. NeurIPS.
- Oehmcke, S. et al. (2024). *Deep learning based 3D point cloud regression for estimating forest biomass*. ACM SIGSPATIAL.
- CarbonScan-AI (2026). *AI carbon assessment from LiDAR/photogrammetry — PointNet++ wood-leaf segmentation*. GitHub: Remote55/carbonscan-ai.
- ForestSentry (2025). *Offline-first on-device leaf-health AI with TensorFlow Lite*. GitHub: aashir-athar/forest-sentry.

### 13.5 QSM et reconstruction d'arbres

- Raumonen, P. et al. (2013). *Fast automatic precision tree models from terrestrial laser scanner data*. Remote Sensing, 5(2), 491-514. TreeQSM.
- Hackenberg, J. et al. (2015). *SimpleTree — an efficient open source tool to build tree models from TLS clouds*. Forests, 6(11), 4245-4294.
- Du, S. et al. (2019). *AdTree: a quantitative structure model for single trees from LiDAR point clouds*. ISPRS Annals.
- PRBonn (2024). *Forest inventory pipeline — tree instance segmentation and DBH from mobile LiDAR*. GitHub: PRBonn/forest_inventory_pipeline. ICRA 2024.

### 13.6 Apprentissage fédéré et mobile ML

- McMahan, H.B. et al. (2017). *Communication-Efficient Learning of Deep Networks from Decentralized Data* (FedAvg). AISTATS.
- TensorFlow Lite (2025). *On-device ML for mobile*. https://www.tensorflow.org/lite
- Core ML (2025). *Machine learning on Apple platforms*. https://developer.apple.com/documentation/coreml
- PyTorch Mobile / ExecuTorch (2025). *On-device inference*. https://pytorch.org/executorch
- Flower (2025). *Federated learning framework*. https://flower.ai

---

## 14. Mesure de hauteur par capteurs mobiles — amélioration de la technologie existante

### 14.1 Système existant — analyse

GeoSylva dispose déjà d'un système de mesure de hauteur par clinométrie smartphone, implémenté dans 4 fichiers :

#### 14.1.1 Architecture actuelle

```
┌─────────────────────────────────────────────────────────────┐
│              TREE HEIGHT MEASURE — SYSTÈME ACTUEL            │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  TreeHeightMeasureDialog.kt (UI — 941 lignes)           │ │
│  │  Workflow : Waist → Distance → Angle Top → Angle Base   │ │
│  │  → Result                                                │ │
│  │  · Auto-capture après 1.5s de stabilité ≥ 82%           │ │
│  │  · Boussole + baromètre affichés                        │ │
│  │  · Bouton GPS distance (GpsDistanceMeasureDialog)       │ │
│  └────────────┬────────────────────────────────────────────┘ │
│               │                                               │
│  ┌────────────▼──────────┐  ┌──────────────────────┐        │
│  │ TreeHeightMeasureTool │  │  CompassManager       │        │
│  │  (181 lignes)          │  │  (120 lignes)         │        │
│  │                        │  │                       │        │
│  │ · detectCapability()   │  │ · bearingFlow()       │        │
│  │   NONE/BASIC/MEDIUM/   │  │   rotation vector ou  │        │
│  │   HIGH                  │  │   mag+accel           │        │
│  │ · pitchFlow()           │  │ · toCardinal()        │        │
│  │   rotation vector ou    │  └──────────────────────┘        │
│  │   accelerometer         │                                   │
│  │ · barometerAltitudeFlow│                                   │
│  │   TYPE_PRESSURE         │                                   │
│  │ · calculateHeight()     │                                   │
│  │   H = h_phone +         │                                   │
│  │   D×tan(α_top) −        │                                   │
│  │   D×tan(α_base)         │                                   │
│  └─────────────────────────┘                                   │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  HeightCameraAimOverlay.kt (363 lignes)                 │ │
│  │  · CameraX preview plein écran                          │ │
│  │  · Réticule + ligne horizon                             │ │
│  │  · Auto-capture + zoom manuel                           │ │
│  │  · HUD angle + stabilité                                │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

#### 14.1.2 Méthode mathématique actuelle

La formule utilisée (@`TreeHeightMeasureTool.kt:154`) est la **méthode des tangentes** :

```
H = h_phone + D × tan(α_top) + D × |tan(α_base)|

où :
  h_phone   = hauteur du téléphone au-dessus du sol (défaut 1.5m)
  D         = distance horizontale au pied de l'arbre (saisie manuelle ou GPS)
  α_top     = angle d'élévation vers la cime (capteur)
  α_base    = angle de déclinaison vers la base (capteur, optionnel)
```

Précision angulaire par capteur (@`TreeHeightMeasureTool.kt:169-174`) :
- HIGH (rotation vector) : ±0.5°
- MEDIUM (accel + gyro) : ±1.0°
- BASIC (accel seul) : ±2.0°
- NONE (manuel) : ±3.0°

#### 14.1.3 Limites identifiées

| # | Limite | Impact | Cause |
|---|---|---|---|
| 1 | **Distance GPS imprécise** | ±3-10m sur la distance → ±0.5-3m sur la hauteur | GPS civil sans RTK, pas de correction différentielle |
| 2 | **Distance manuelle non vérifiée** | L'utilisateur peut se tromper de plusieurs mètres | Pas de validation croisée, pas de télémétrie |
| 3 | **Pas de correction de pente terrain** | Sur terrain incliné, la distance horizontale ≠ distance mesurée | Formule assume terrain plat |
| 4 | **Pas de détection de penchage** | Un arbre penché a une hauteur verticale < hauteur oblique | Une seule mesure d'angle, pas de détection de direction de penchage |
| 5 | **Caméra = viseur uniquement** | Pas de computer vision pour détecter la cime/base automatiquement | CameraX preview sans analyse d'image |
| 6 | **Baromètre non exploité** | Affiché en info seulement, pas utilisé dans le calcul | Pas de calcul de différence d'altitude |
| 7 | **Pas de calibration de biais capteur** | Les capteurs ont un biais systématique (offset) non corrigé | Pas de protocole de calibration |
| 8 | **Pas de multi-position** | Une seule position de mesure → pas de triangulation | Pas de workflow multi-points |
| 9 | **Précision non contextualisée** | L'incertitude ne dépend que de l'angle, pas de la distance ou conditions | Formule d'erreur simpliste |
| 10 | **Pas de détection d'obstruction** | Si la cime est masquée par d'autres arbres, la mesure est fausse | Pas de CV pour vérifier la visibilité |
| 11 | **Pas de validation de cohérence** | Pas de comparaison avec hauteur théorique (modèle H-D par essence) | Pas de garde-fou |
| 12 | **Single-shot** | Pas de multi-mesures avec moyenne pondérée | Une seule capture par angle |

### 14.2 Vision améliorée — Height Engine 2.0

#### 14.2.1 Niveaux de sophistication

```
NIVEAU 0 — Clinomètre basique (actuel, T0+)
  Capteur d'angle + distance manuelle
  Formule tangente simple
  ±0.5-3m selon distance et capteur

NIVEAU 1 — Clinomètre amélioré (T0+)
  + Calibration de biais capteur
  + Correction de pente terrain
  + Multi-mesures avec moyenne pondérée
  + Validation de cohérence (modèle H-D)
  + Incertitude propagée complète
  ±0.3-1.5m

NIVEAU 2 — AR Distance (T2+)
  + ARCore/ARKit pour distance précise (±0.1-0.5m)
  + Détection automatique du pied d'arbre (CV)
  + Baromètre différentiel (altitude phone vs base)
  ±0.2-0.8m

NIVEAU 3 — CV-assisté (T3+)
  + Détection automatique cime/base (YOLO/segmentation)
  + Détection de penchage (multi-angle)
  + Détection d'obstruction (visibilité cime)
  + LiDAR distance (iPhone Pro, ±0.05m)
  ±0.1-0.5m

NIVEAU 4 — Multi-position triangulation (T3+)
  + 2+ positions de mesure (triangulation)
  + Fusion bayésienne multi-mesures
  + Modèle 3D léger de l'arbre
  + Correction de penchage complète
  ±0.05-0.3m

NIVEAU 5 — IA height estimation (T4+)
  + Réseau de neurones : photo + capteurs → hauteur + IC
  + Modèle entraîné sur données terrain
  + Détection essence automatique (pour validation H-D)
  ±0.02-0.2m
```

#### 14.2.2 Objectifs quantifiés

| Métrique | Système actuel | Objectif Height Engine 2.0 |
|---|---|---|
| Précision distance | ±3-10m (GPS) ou manuelle | **±0.1-0.5m** (AR/LiDAR) |
| Précision hauteur (à 20m) | ±0.5-3m | **±0.1-0.5m** (Niveau 3+) |
| Calibration capteur | Non | **Oui** — protocole auto |
| Correction pente | Non | **Oui** — accéléromètre |
| Détection penchage | Non | **Oui** — multi-position |
| Validation cohérence | Non | **Oui** — modèle H-D par essence |
| Multi-mesures | Non | **Oui** — moyenne bayésienne |
| Incertitude | Angle uniquement | **Complète** — distance + angle + pente |
| Détection cime/base | Manuelle | **Automatique** (CV, Niveau 3+) |

### 14.3 Améliorations algorithmiques détaillées

#### 14.3.1 Calibration de biais capteur

Les capteurs mobiles ont un **biais systématique** qui évolue avec la température et le temps. Un protocole de calibration rapide corrige ce biais :

```
PROTOCOLE DE CALIBRATION (30 secondes, une fois par session) :

1. L'utilisateur place le téléphone à plat sur un support horizontal
   (table, souche) pendant 5 secondes
   → mesure du biais de pitch (devrait être 0° à l'horizontal)
   → biais_pitch = moyenne des lectures sur 5s

2. L'utilisateur vise l'horizon (ligne de niveau lointaine)
   → validation : biais_pitch cohérent avec étape 1

3. L'utilisateur fait un demi-tour (180°) et vise à nouveau l'horizon
   → validation : pas de dérive du biais

4. Stockage : biais_pitch_offset, timestamp, température (si disponible)
   → appliqué à toutes les mesures de la session

CALIBRATION CONTINUE :
  - Pendant la mesure, si stabilityScore > 0.95 pendant > 3s
    et que l'utilisateur ne bouge pas, on peut détecter un drift
  - Si l'angle "repos" (tenu verticalement) change de > 0.5°
    entre sessions, suggérer une recalibration
```

```kotlin
data class SensorCalibration(
    val pitchOffsetDeg: Float,
    val calibrationTime: Long,
    val confidence: Float,        // 0-1, basé sur la qualité de la calibration
    val temperatureC: Float?      // si disponible, pour corriger le drift
)

object SensorCalibrator {
    fun calibratePitch(samples: List<Float>): SensorCalibration {
        // Filtrer les outliers (mediane + MAD)
        val sorted = samples.sorted()
        val median = sorted[samples.size / 2]
        val mad = sorted.map { abs(it - median) }.sorted()[samples.size / 2]
        val filtered = samples.filter { abs(it - median) < 3 * mad }
        val offset = filtered.average().toFloat()
        val std = filtered.standardDeviation()
        val confidence = (1f - (std / 2f).coerceIn(0f, 1f))
        return SensorCalibration(offset, System.currentTimeMillis(), confidence, null)
    }
}
```

#### 14.3.2 Correction de pente terrain

Sur terrain incliné, la distance mesurée au sol n'est pas la distance horizontale :

```
TERRAIN INCLINÉ :

  Soit β = angle de pente du terrain (mesuré par accéléromètre
  quand le téléphone est tenu parallèle au sol)

  Distance horizontale réelle :
    D_h = D_mesurée × cos(β)

  Hauteur corrigée :
    H = h_phone + D_h × tan(α_top) + D_h × |tan(α_base)|

  Erreur si non corrigé (pente 15°, D=20m) :
    D_h = 20 × cos(15°) = 19.32m (au lieu de 20m)
    ΔH = (20 - 19.32) × tan(45°) = 0.68m d'erreur
```

```kotlin
fun calculateHeightWithSlope(
    distanceM: Double,
    angleTopDeg: Double,
    angleBaseDeg: Double = 0.0,
    phoneHeightM: Double = 1.5,
    terrainSlopeDeg: Double = 0.0,     // NOUVEAU : pente du terrain
    capability: SensorCapability = SensorCapability.BASIC
): TreeHeightResult {
    val slopeRad = Math.toRadians(terrainSlopeDeg)
    val horizontalDistance = distanceM * cos(slopeRad)

    return TreeHeightMeasureTool.calculateHeight(
        distanceM = horizontalDistance,
        angleTopDeg = angleTopDeg,
        angleBaseDeg = angleBaseDeg,
        phoneHeightM = phoneHeightM,
        capability = capability
    )
}
```

#### 14.3.3 Mesure de la pente terrain par accéléromètre

L'accéléromètre peut mesurer la pente du terrain en plaçant le téléphone à plat sur le sol :

```
MESURE DE PENTE :

1. L'utilisateur pose le téléphone à plat sur le terrain
   (ou sur une planche / souche horizontale)

2. Lecture de l'accéléromètre :
   gravity = (gx, gy, gz)

3. Angle de pente :
   β = atan2(√(gx² + gy²), gz) - 90°
   (ou directement depuis le rotation vector si disponible)

4. Direction de pente (azimut) :
   direction = atan2(gx, gy)  → direction de la pente descendante

5. La pente est-elle significative ?
   Si |β| < 2° → terrain plat, pas de correction nécessaire
   Si |β| > 2° → appliquer la correction cos(β)
```

#### 14.3.4 ARCore/ARKit pour distance précise

ARCore (Android) et ARKit (iOS) fournissent une **estimation de profondeur** bien plus précise que le GPS :

```
ARCore Depth API (Android) :
  - Estimation de profondeur par SfM + IMU
  - Précision : ±0.05-0.5m à 5-20m
  - Disponible sur Android 7+ (API 24+) avec ARCore

ARKit (iOS) :
  - ARWorldTrackingConfiguration
  - Estimation de position relative précise
  - Précision : ±0.02-0.2m à 5-20m
  - Disponible sur iOS 11+

LiDAR (iPhone Pro, iPad Pro) :
  - ARWorldTrackingConfiguration avec sceneDepth
  - Depth map dense à 60Hz
  - Précision : ±0.01-0.05m à 5m
```

**Méthode AR pour distance arbre** :

```
PIPELINE AR DISTANCE :

1. L'utilisateur pointe le téléphone vers le pied de l'arbre
   → ARCore/ARKit détecte le plan vertical (tronc) ou le point de contact sol

2. Position de l'arbre dans le repère AR :
   arbre_pos_AR = hitTest(screenCenter, planeDetection)

3. Position du téléphone dans le repère AR :
   phone_pos_AR = ARSession.currentPose

4. Distance = ||arbre_pos_AR - phone_pos_AR||

5. Validation :
   - Si détection de plan vertical (tronc) → haute confiance
   - Si hitTest sur point cloud → confiance moyenne
   - Si pas de détection → fallback GPS ou manuel
```

```kotlin
// Pseudocode — intégration ARCore
class ArDistanceProvider(private val session: ArSession) {

    suspend fun measureDistanceToTree(): ArDistanceResult? {
        val frame = session.currentFrame ?: return null
        val cameraPose = frame.camera.pose

        // HitTest au centre de l'écran
        val hitResults = frame.hitTest(screenCenterX, screenCenterY)

        // Priorité 1 : plan vertical (tronc)
        val trunkHit = hitResults
            .filter { it.trackable is Plane && (it.trackable as Plane).type == Plane.Type.VERTICAL }
            .firstOrNull()

        // Priorité 2 : point cloud (Depth API)
        val pointHit = hitResults
            .filter { it.trackable is PointCloudPoint }
            .firstOrNull()

        val hitPose = (trunkHit ?: pointHit)?.hitPose ?: return null

        val distance = distanceBetween(cameraPose, hitPose)
        val confidence = when {
            trunkHit != null -> 0.95
            pointHit != null -> 0.75
            else -> 0.0
        }

        return ArDistanceResult(
            distanceM = distance,
            confidence = confidence,
            method = if (trunkHit != null) "PLANE" else "POINT_CLOUD"
        )
    }
}
```

#### 14.3.5 Détection automatique cime/base par computer vision

Au lieu de demander à l'utilisateur de viser manuellement, un modèle de détection d'objet peut identifier automatiquement la cime et la base de l'arbre dans le flux caméra :

```
MODÈLE YOLO LÉGER (TFLite, ~2 MB) :

Classes :
  - tree_top (sommet de la cime)
  - tree_base (pied du tronc)
  - trunk (tronc visible)

Inférence : 30-60 Hz sur NPU/GPU, 5-15 Hz sur CPU

Pipeline :
  1. Frame caméra → YOLO inference
  2. Détection tree_top → bounding box (x_top, y_top)
  3. Détection tree_base → bounding box (x_base, y_base)
  4. Calcul de l'angle vers le centre de chaque box :
     angle_top = pitchCapteur + (y_top - cy) × pixels_par_degré
     angle_base = pitchCapteur + (y_base - cy) × pixels_par_degré
  5. Si tree_top et tree_base détectés simultanément →
     hauteur = D × (tan(angle_top) - tan(angle_base))
     → pas besoin de viser manuellement !
```

**Alternative sans YOLO** — détection par couleur/texture :

```
Méthode plus légère (pas de modèle IA) :

1. Segmentation par couleur : le tronc a une couleur caractéristique
   (brun/gris) distincte du feuillage (vert) et du sol

2. Détection du point le plus haut du feuillage (transition vert → ciel)
   → cime

3. Détection du point le plus bas du tronc (transition tronc → sol)
   → base

4. Calcul des angles avec le capteur

Avantage : pas de modèle à entraîner, fonctionne sur tous les devices
Inconvénient : moins robuste (conditions lumineuses, saison)
```

#### 14.3.6 Détection de penchage par multi-position

Un arbre penché a une hauteur verticale inférieure à sa hauteur oblique. La méthode des tangentes mesure la hauteur **oblique** si on vise le long du tronc. Pour corriger :

```
MÉTHODE 2-POSITIONS :

Position 1 (face à l'arbre) :
  Mesurer : D₁, α_top₁, α_base₁
  H_oblique₁ = h_phone + D₁ × tan(α_top₁) + D₁ × |tan(α_base₁)|

Position 2 (à 90°, perpendiculaire) :
  Mesurer : D₂, α_top₂, α_base₂
  H_oblique₂ = h_phone + D₂ × tan(α_top₂) + D₂ × |tan(α_base₂)|

Si l'arbre est droit : H₁ ≈ H₂
Si l'arbre penche vers la position 1 : H₁ > H₂ (on voit la hauteur oblique)
Si l'arbre penche vers la position 2 : H₂ > H₁

Hauteur verticale réelle :
  H_verticale = min(H₁, H₂)

Angle de penchage :
  ΔH = |H₁ - H₂|
  angle_lean = atan(ΔH / (D₁ + D₂))  (approximation)

Direction de penchage :
  Si H₁ > H₂ → l'arbre penche vers la position 1 (face à l'observateur 1)
  La boussole donne l'azimut de chaque position → direction de penchage
```

#### 14.3.7 Baromètre différentiel

Le baromètre peut mesurer la **différence d'altitude** entre le téléphone et la base de l'arbre, ce qui affine l'estimation de `h_phone` :

```
MESURE BAROMÉTRIQUE DIFFÉRENTIELLE :

1. À la position de mesure (distance D de l'arbre) :
   alt_phone = SensorManager.getAltitude(hPa_phone)

2. L'utilisateur marche jusqu'au pied de l'arbre et pose le téléphone au sol :
   alt_base = SensorManager.getAltitude(hPa_base)

3. Différence d'altitude :
   Δh = alt_phone - alt_base
   h_phone_corrigé = Δh  (si le téléphone est à hauteur de poitrine à la position 1)

4. Précision barométrique :
   - Résolution : ±0.1-0.5m (capteur smartphone)
   - Dérive temporelle : ±0.5m / 10min (compensation par calibration)
   - Si mesure en < 2min → précision ±0.2-0.5m

AVANTAGE : pas besoin de connaître h_phone, mesuré automatiquement
```

#### 14.3.8 Multi-mesures avec fusion bayésienne

Au lieu d'une seule mesure, l'utilisateur peut répéter la mesure N fois. Chaque mesure fournit (H_i, σ_i) et la fusion bayésienne donne :

```
H_fusionné = Σ(H_i / σ_i²) / Σ(1 / σ_i²)
σ_fusionné = 1 / √(Σ(1 / σ_i²))

L'incertitude σ_i de chaque mesure dépend de :
  - σ_angle (capteur, calibration)
  - σ_distance (méthode : GPS ±3m, AR ±0.3m, LiDAR ±0.05m, manuel ±1m)
  - σ_pente (si terrain incliné)
  - stabilityScore (mesure instable → σ plus grand)

σ_i = √(σ_angle² × (∂H/∂α)² + σ_distance² × (∂H/∂D)² + σ_pente² × (∂H/∂β)²)

où :
  ∂H/∂α = D / cos²(α)   (sensibilité à l'erreur d'angle)
  ∂H/∂D = tan(α)         (sensibilité à l'erreur de distance)
  ∂H/∂β = -D × tan(α) × sin(β) / cos²(β)  (sensibilité à l'erreur de pente)
```

```kotlin
data class HeightMeasurement(
    val heightM: Double,
    val sigmaM: Double,
    val distanceM: Double,
    val angleTopDeg: Double,
    val angleBaseDeg: Double?,
    val terrainSlopeDeg: Double,
    val stabilityScore: Float,
    val method: HeightMethod,
    val timestamp: Long
)

object HeightFusion {

    fun fuse(measurements: List<HeightMeasurement>): FusedHeight {
        if (measurements.isEmpty()) return FusedHeight(0.0, Double.MAX_VALUE)
        if (measurements.size == 1) {
            return FusedHeight(measurements[0].heightM, measurements[0].sigmaM)
        }

        val weights = measurements.map { 1.0 / (it.sigmaM * it.sigmaM) }
        val totalWeight = weights.sum()
        val fusedHeight = measurements.zip(weights)
            .sumOf { (m, w) -> m.heightM * w } / totalWeight
        val fusedSigma = 1.0 / sqrt(totalWeight)

        // Détection d'outliers (chi²)
        val residuals = measurements.map { abs(it.heightM - fusedHeight) / it.sigmaM }
        val chiSquare = residuals.sumOf { it * it }
        val maxResidual = residuals.maxOrNull() ?: 0.0

        val hasOutlier = maxResidual > 3.0  // > 3σ
        val quality = if (hasOutlier) QualityFlag.SUSPECT_OUTLIER
                      else QualityFlag.GOOD

        return FusedHeight(fusedHeight, fusedSigma, quality, measurements.size)
    }
}
```

#### 14.3.9 Validation de cohérence par modèle H-D

Chaque essence a une relation hauteur-diamètre typique. Si la hauteur mesurée est incohérente avec le diamètre, une alerte est émise :

```
MODÈLES H-D PAR ESSENCE (source : IGN, INRAE) :

Chêne sessile : H = 1.2 × D^0.6  (D en cm, H en m)
Hêtre :         H = 1.5 × D^0.5
Sapin pectiné : H = 1.8 × D^0.55
Douglas :       H = 2.0 × D^0.55
Pin sylvestre : H = 1.6 × D^0.5

Intervalle de tolérance : ±30% (la hauteur varie avec la station, densité, âge)

VALIDATION :
  H_attendue = f(D, essence)
  Si |H_mesurée - H_attendue| / H_attendue > 0.30 :
    → Flag : HAUTEUR_INCOHERENTE
    → Suggestion : vérifier la distance ou l'identification d'essence

  Si |H_mesurée - H_attendue| / H_attendue > 0.50 :
    → Flag : HAUTEUR_SUSPECTE
    → Recommandation : refaire la mesure
```

#### 14.3.10 Incertitude propagée complète

L'incertitude actuelle (@`TreeHeightMeasureTool.kt:176`) ne dépend que de l'angle. La nouvelle formule propage **toutes les sources d'erreur** :

```kotlin
fun calculateHeightWithUncertainty(
    distanceM: Double,
    distanceSigmaM: Double,         // incertitude distance
    angleTopDeg: Double,
    angleTopSigmaDeg: Double,       // incertitude angle (capteur + calibration)
    angleBaseDeg: Double = 0.0,
    angleBaseSigmaDeg: Double = 0.0,
    phoneHeightM: Double = 1.5,
    phoneHeightSigmaM: Double = 0.05,
    terrainSlopeDeg: Double = 0.0,
    terrainSlopeSigmaDeg: Double = 0.0
): TreeHeightResult {

    val topRad = Math.toRadians(angleTopDeg)
    val baseRad = Math.toRadians(angleBaseDeg)
    val slopeRad = Math.toRadians(terrainSlopeDeg)

    // Distance horizontale corrigée
    val dH = distanceM * cos(slopeRad)
    val heightAbove = dH * tan(topRad)
    val heightBelow = if (angleBaseDeg < -0.5) dH * abs(tan(baseRad)) else 0.0
    val total = (phoneHeightM + heightAbove + heightBelow).coerceAtLeast(0.5)

    // Propagation d'erreur (dérivées partielles)
    val dH_dD = cos(slopeRad)  // ∂H/∂D
    val dH_dAlphaTop = dH / cos(topRad).pow(2)  // ∂H/∂α_top
    val dH_dAlphaBase = if (angleBaseDeg < -0.5) dH / cos(baseRad).pow(2) else 0.0
    val dH_dSlope = -distanceM * sin(slopeRad) * tan(topRad)  // ∂H/∂β
    val dH_dPhoneH = 1.0  // ∂H/∂h_phone

    val sigmaRad = Math.toRadians(angleTopSigmaDeg)
    val baseSigmaRad = Math.toRadians(angleBaseSigmaDeg)
    val slopeSigmaRad = Math.toRadians(terrainSlopeSigmaDeg)

    val variance = (
        (dH_dD * distanceSigmaM).pow(2) +
        (dH_dAlphaTop * sigmaRad).pow(2) +
        (dH_dAlphaBase * baseSigmaRad).pow(2) +
        (dH_dSlope * slopeSigmaRad).pow(2) +
        (dH_dPhoneH * phoneHeightSigmaM).pow(2)
    )
    val precisionM = sqrt(variance)

    return TreeHeightResult(total, precisionM, SensorCapability.HIGH)
}
```

### 14.4 Détection d'obstruction par CV

Un problème fréquent en forêt : la cime est masquée par d'autres arbres. Une détection d'obstruction évite des mesures fausses :

```
MÉTHODE — analyse de la frame caméra au moment de la capture :

1. Détection de contours (Canny edge) dans la zone de la cime
2. Analyse de la couleur : le feuillage est vert (HSV : H 30-90, S > 0.3)
3. Si la zone au-dessus du point visé contient :
   - Majorité de ciel (bleu/gris clair) → cime visible, OK
   - Majorité de feuillage vert → possible obstruction par autre arbre
   - Mixte → obstruction partielle

4. Score de visibilité : 0.0 (obstrué) → 1.0 (clair)

5. Si score < 0.5 :
   → Warning : "Cime potentiellement obstruée — rapprochez-vous ou changez d'angle"
   → La mesure est marquée avec un flag OBSTRUCTED_TOP

IMPLÉMENTATION LÉGÈRE (sans IA) :
  - Conversion RGB → HSV (native Android Color.space)
  - Histogramme de la zone supérieure de la frame
  - Ratio pixels_ciel / pixels_total
  - Seuil adaptatif selon conditions lumineuses
```

### 14.5 Architecture Height Engine 2.0

```
┌──────────────────────────────────────────────────────────────────────┐
│               GEOSYLVA HEIGHT ENGINE 2.0                              │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │ Sensor Module│  │ AR Distance  │  │ CV Module    │               │
│  │ (amélioré)   │  │ Module       │  │ (nouveau)    │               │
│  │              │  │              │  │              │               │
│  │ · Pitch flow │  │ · ARCore/    │  │ · Tree top   │               │
│  │   + calibrat │  │   ARKit hit  │  │   detection  │               │
│  │ · Barometer  │  │   test       │  │ · Tree base  │               │
│  │   diff.      │  │ · LiDAR depth│  │   detection  │               │
│  │ · Slope meas │  │ · Fallback   │  │ · Obstruction│               │
│  │ · Compass    │  │   GPS        │  │   detection  │               │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         │                 │                 │                        │
│         └─────────────────┼─────────────────┘                        │
│                           │                                          │
│                           ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              HEIGHT CALCULATOR CORE                           │   │
│  │                                                              │   │
│  │  · Tangent formula (avec correction pente)                   │   │
│  │  · Multi-position triangulation                              │   │
│  │  · Leaning tree correction                                   │   │
│  │  · Uncertainty propagation (complète)                        │   │
│  │  · Bayesian fusion (multi-mesures)                           │   │
│  │  · Coherence validation (modèle H-D)                         │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              OUTPUT: HeightEstimate                           │   │
│  │                                                              │   │
│  │  · heightM + IC 90%                                         │   │
│  │  · method_used (sensor, AR, LiDAR, CV, multi)                │   │
│  │  · quality_flags (obstructed, outlier, incoherent)           │   │
│  │  · lean_angle + direction (si multi-position)                │   │
│  │  · terrain_slope                                             │   │
│  │  · measurements_count                                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### 14.6 Workflow UI amélioré

#### 14.6.1 Workflow Niveau 1 (tous devices)

```
ÉTAPE 1 — Calibration (30s, une fois par session)
  "Posez le téléphone à plat sur une surface horizontale"
  → Mesure du biais capteur (5s)
  → "Calibration terminée : ±X.X°"

ÉTAPE 2 — Distance
  · Saisie manuelle OU
  · GPS distance (bouton existant) OU
  · AR distance (si ARCore/ARKit disponible)
  → Affichage de la méthode + incertitude

ÉTAPE 3 — Pente terrain (optionnel, 5s)
  "Posez le téléphone sur le terrain"
  → Mesure automatique de la pente
  → "Pente : X.X° (correction appliquée)"

ÉTAPE 4 — Visée cime (avec auto-détection si CV)
  · Si CV : rectangle vert sur la cime détectée
  · Auto-capture quand stable + cime détectée
  · Si pas de CV : viseur manuel (système actuel)

ÉTAPE 5 — Visée base (optionnel)
  · Même principe que cime

ÉTAPE 6 — Résultat
  · Hauteur ± précision (IC 90%)
  · Validation H-D (si essence + D connus)
  · Bouton "Nouvelle mesure" pour multi-mesures
  · Bouton "Mesure à 90°" pour détection penchage
```

#### 14.6.2 Workflow Niveau 3+ (LiDAR + CV)

```
SCAN UNIQUE (5 secondes) :

1. L'utilisateur pointe vers l'arbre
   → CV détecte tree_top + tree_base automatiquement
   → AR/LiDAR mesure la distance automatiquement
   → Capteur mesure l'angle automatiquement

2. Pas de visée manuelle — tout est automatique
   → L'utilisateur voit les rectangles de détection
   → L'utilisateur valide ou ajuste

3. Résultat instantané :
   → Hauteur + IC + flags
   → "Cime détectée à 28.3m, base à 0.2m, hauteur = 28.1m ± 0.3m"

4. Optionnel : 2e position pour triangulation
   → "Marchez 10m perpendiculairement et refaites un scan"
   → Fusion des 2 mesures
```

### 14.7 Modèles de données

```kotlin
enum class HeightMethod {
    MANUAL_ANGLE,           // angle manuel + distance manuelle
    SENSOR_TANGENT,         // capteur angle + distance manuelle/GPS
    AR_DISTANCE,            // ARCore/ARKit pour distance
    LIDAR_DISTANCE,         // LiDAR pour distance
    CV_ASSISTED,            // CV pour détection cime/base
    MULTI_POSITION,         // triangulation multi-position
    AI_PHOTO,               // IA depuis photo unique
    BAYESIAN_FUSION         // fusion de plusieurs méthodes
}

enum class QualityFlag {
    GOOD,
    OBSTRUCTED_TOP,
    OBSTRUCTED_BASE,
    SUSPECT_OUTLIER,
    INCOHERENT_HD,
    LOW_STABILITY,
    CALIBRATION_STALE,
    DISTANCE_UNVERIFIED
}

data class HeightEstimate(
    val heightM: Double,
    val confidenceInterval: Pair<Double, Double>,  // IC 90%
    val methodUsed: HeightMethod,
    val qualityFlags: List<QualityFlag>,
    val measurementCount: Int,
    val terrainSlopeDeg: Double,
    val leanAngleDeg: Double?,         // si multi-position
    val leanDirectionAzimuth: Double?, // si multi-position
    val distanceM: Double,
    val distanceMethod: String,        // "MANUAL", "GPS", "AR", "LIDAR"
    val distanceSigmaM: Double,
    val angleTopDeg: Double,
    val angleBaseDeg: Double?,
    val sensorCapability: SensorCapability,
    val calibrationApplied: Boolean,
    val cvDetectedTop: Boolean,
    val cvDetectedBase: Boolean,
    val obstructionScore: Float?,      // 0-1, si CV
    val hdValidation: HdValidation?,   // validation essence
    val processingTimeMs: Long
)

data class HdValidation(
    val essenceCode: String,
    val diameterCm: Double,
    val expectedHeightM: Double,
    val tolerancePercent: Double,
    val isCoherent: Boolean,
    val deviationPercent: Double
)
```

### 14.8 Intégration avec le moteur de volume

La hauteur mesurée par Height Engine 2.0 alimente directement le `VolumeEngine` (section 9) :

```kotlin
// Dans VolumeEngine, la hauteur peut venir de :
// 1. Saisie manuelle (précision ±1-3m)
// 2. Height Engine 2.0 Niveau 1 (±0.3-1.5m)
// 3. Height Engine 2.0 Niveau 3+ (±0.1-0.5m)
// 4. Point cloud 3D (±0.05-0.2m)

// L'incertitude sur H se propage au volume :
// Si V = f(D, H) → σ_V = √((∂V/∂D × σ_D)² + (∂V/∂H × σ_H)²)
// Une meilleure hauteur → un meilleur volume avec IC plus étroit
```

### 14.9 Roadmap Height Engine 2.0

| Phase | Durée | Contenu | Niveau |
|---|---|---|---|
| **HE-1** | 1-2 semaines | Calibration capteur, correction pente, incertitude propagée | Niveau 1 |
| **HE-2** | 2-3 semaines | Multi-mesures avec fusion bayésienne, validation H-D | Niveau 1+ |
| **HE-3** | 3-4 semaines | ARCore/ARKit distance, baromètre différentiel | Niveau 2 |
| **HE-4** | 4-6 semaines | CV détection cime/base, obstruction, LiDAR distance | Niveau 3 |
| **HE-5** | 3-4 semaines | Multi-position triangulation, penchage | Niveau 4 |
| **HE-6** | 6-8 semaines | Modèle IA hauteur depuis photo (MobileNetV2) | Niveau 5 |

### 14.10 Sources spécifiques à la mesure de hauteur

- Larjavaara, M. et al. (2013). *Comparison of tree height measurements from laser and clinometer*. Boreal Environment Research, 18, 353-362.
- Andersen, H.E. et al. (2006). *A comparison of forest canopy height derived from LiDAR and field measurements*. Scandinavian Journal of Forest Research, 21, 233-242.
- Brédif, M. et al. (2014). *ARCore Depth API — real-time depth estimation on mobile*. Google AR.
- Apple (2025). *ARKit — Scene Depth and LiDAR*. developer.apple.com.
- Anderson, H.E. et al. (2023). *Robust Single-Image Tree Diameter Estimation with Mobile Phones*. Remote Sensing, 15(3), 772.
- Vastaranta, M. et al. (2014). *Feasibility of smartphone-based laser scanning for forest inventory*. ISPRS Annals, II-5, 57-62.
- Bauwens, S. et al. (2016). *Forest inventory with mobile LiDAR — height estimation*. Forests, 7(7), 150.

---

## 15. Détection automatique d'essence et qualité bois par IA

### 15.1 Problématique

L'identification manuelle de l'essence est la **première source d'erreur** en inventaire forestier :
- Erreur d'identification → mauvais coefficients de volume, biomasse, croissance
- Confusion chêne sessile / chêne pédonculé / chêne pubescent
- Confusion sapin pectiné / épicéa / douglas en hiver (sans aiguilles visibles)
- Temps perdu à chercher le code essence dans une liste

L'évaluation qualité est actuellement **binaire et statique** : règles de diamètre pour BO/BI/BCh/PATE, sans analyse visuelle des défauts. Un arbre de 40cm avec une cavité majeure est classé BO alors qu'il devrait être déclassé.

### 15.2 Détection d'essence par photo — architecture

#### 15.2.1 Modèle de classification d'écorce

```
ARCHITECTURE : MobileNetV2 (transfer learning ImageNet → bark)

Input : photo tronc 224×224×3 (zone 1-2m de hauteur)
Output : probabilités par essence (softmax)

Classes (essences françaises principales, ~40) :
  Feuillus : QUPE, QUSE, QUPU, FASY, BEPE, FRAX, ACPS, TICO, COAV,
             PRAX, SOR, ULMI, POPE, POTR, SALI, CAST
  Résineux : ABAL, PISY, PINI, PCAB, EUSI, LARI, PSME, THPL
  Méditerranéens : PINP, PIPA, QUIL, QUSU, ARUN, CLES

Entraînement :
  Dataset : 50k+ photos d'écorce étiquetées (IGN, INRAE, crowdsourcing GeoSylva)
  Augmentation :
    · Rotation 90/180/270° (l'écorce n'a pas d'orientation privilégiée)
    · Variation luminosité ±30%
    · Flou gaussien (simulateur mise au point)
    · Bruit JPEG (simulateur compression)
    · Crop aléatoire (zone d'écorce variable)
  Loss : CrossEntropy + label smoothing (0.1)
  Optimiseur : AdamW, lr=1e-4, cosine schedule
  Epochs : 50, early stopping patience=10

Optimisation mobile :
  - INT8 quantization → 1.5-3 MB
  - Latence : 15-30ms NPU, 50-100ms GPU, 200-500ms CPU
  - Top-3 affiché (l'utilisateur choisit si top-1 < 80%)
```

#### 15.2.2 Modèle de classification feuillage

Complément au modèle écorce pour les feuillus en été :

```
ARCHITECTURE : EfficientNet-Lite0

Input : photo feuillage/couronne 224×224×3
Output : probabilités par essence (softmax)

Classes : mêmes ~40 essences

Particularités :
  - Photo prise depuis le sol vers la couronne
  - Feuillus en feuille : identification par forme de feuille
  - Résineux : identification par aiguilles (solitaires/par 2/par 5/courtes/longues)

Fusion écorce + feuillage :
  P(essence) = α × P_bark + (1-α) × P_leaf
  où α = confiance_bark / (confiance_bark + confiance_leaf)
```

#### 15.2.3 Pipeline de détection multi-signaux

```
PHOTO TRONC (écorce)
  │
  ▼
[BarkNet] → P_bark(essence) + confiance_bark
  │
  ├── Si confiance_bark > 90% → essence = argmax(P_bark)
  │
  ├── Si 60% < confiance_bark < 90% :
  │   └── Demander photo feuillage (si feuillus, saison)
  │       └── [LeafNet] → P_leaf(essence)
  │           └── Fusion → essence = argmax(α·P_bark + (1-α)·P_leaf)
  │
  └── Si confiance_bark < 60% :
      └── Demander photo feuillage + GPS (pour restreindre les essences possibles)
          └── Filtrage géographique (essences présentes dans la région)
              └── Fusion conditionnelle
```

#### 15.2.4 Filtrage géographique par GPS

Le GPS restreint les essences possibles selon la **zone biogéographique** :

```kotlin
data class SpeciesRange(
    val essenceCode: String,
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
    val altitudeRange: Pair<Int, Int>,  // m
    val biomes: List<Biome>             // atlantique, continental, méditerranéen, montagnard
)

object SpeciesGeoFilter {

    fun filterByLocation(
        probabilities: Map<String, Float>,
        latitude: Double,
        longitude: Double,
        altitude: Int
    ): Map<String, Float> {
        val plausible = SpeciesDatabase.getSpeciesAt(latitude, longitude, altitude)
        return probabilities
            .filterKeys { it in plausible }
            .let { filtered ->
                if (filtered.isEmpty()) probabilities  // fallback : pas de filtre
                else filtered
            }
    }
}
```

Exemple : un Pin d'Alep (PINP) identifié à 30% en Bretagne (altitude 50m) est éliminé au profit du Pin maritime (PINI) à 25%, car le Pin d'Alep n'existe pas en Bretagne.

### 15.3 Évaluation qualité bois par IA

#### 15.3.1 Modèle multi-task : défauts + grading

```
ARCHITECTURE : EfficientNet-Lite0 (backbone partagé)

Input : photo tronc complet 224×224×3 (ou 384×384 pour plus de détails)
Output :
  ├── Head classification : qualité (1-4) → CE
  ├── Head multi-label : défauts → BCE par défaut
  │   · FORK (fourche)
  │   · CAVITY (cavité/trou)
  │   · CRACK (fissure verticale)
  │   · CURVE (courbure/géométrie)
  │   · GALL (galle/tumeur)
  │   · DEAD_BRANCH (branche morte encastrée)
  │   · SPLIT (fente de gélivure)
  │   · INSECT (galeries d'insectes)
  │   · FUNGUS (champignon/pourriture)
  │   · BURL (loupes/broussins)
  └── Head regression : confiance globale → sigmoid

Loss = CE(qualité) + λ₁·ΣBCE(défaut_i) + λ₂·BCE(confiance)
  λ₁ = 0.5, λ₂ = 0.3

Entraînement :
  Dataset : photos + annotations expertes (martelage ONF/CRPF)
  10k+ images étiquetées (crowdsourcing + experts forestiers)
  Augmentation : rotation, couleur, flou, crop
  Validation : comparaison avec grading expert (Cohen's κ > 0.7)

Optimisation mobile :
  - INT8 → 2-4 MB
  - Latence : 20-40ms NPU, 80-150ms GPU
```

#### 15.3.2 Grading qualité ONF

Le système mappe les défauts détectés vers les **classes de qualité ONF** :

```
CLASSES QUALITÉ ONF (simplifié) :

QUALITÉ 1 — BO d'œuvre choix 1
  · Tronc droit, sans défaut visible
  · D ≥ 40cm (feuillus) / D ≥ 30cm (résineux)
  · Pas de fourche, cavité, fissure, courbure > 2°

QUALITÉ 2 — BO d'œuvre choix 2
  · Défauts mineurs tolérés (petite courbure, branches encastrées)
  · D ≥ 35cm (feuillus) / D ≥ 25cm (résineux)

QUALITÉ 3 — BO d'œuvre choix 3 / BIL
  · Défauts modérés (courbure < 5°, petites galles)
  · D ≥ 30cm (feuillus) / D ≥ 20cm (résineux)

QUALITÉ 4 — BCh / PATE / bois énergie
  · Défauts majeurs (cavité, grosse courbure, champignon)
  · D < 30cm ou qualité insuffisante pour BO

RÈGLES DE MAPPING :
  Si CAVITY détecté avec severity > 0.5 → qualité max 3
  Si FUNGUS détecté → qualité max 4 (bois énergie)
  Si FORK détecté sous 6m → qualité max 3
  Si CRACK détecté avec severity > 0.7 → qualité max 4
  Si CURVE severity > 0.6 → qualité max 3
  Si aucun défaut → qualité 1 ou 2 selon D
```

#### 15.3.3 Pseudocode Kotlin

```kotlin
data class QualityAssessment(
    val qualityGrade: Int,              // 1-4
    val defects: List<DetectedDefect>,
    val confidence: Float,              // 0-1
    val recommendedUse: WoodUse,        // BO1, BO2, BO3, BIL, BCh, PATE
    val onfCompatible: Boolean
)

data class DetectedDefect(
    val type: DefectType,
    val severity: Float,                // 0-1
    val boundingBox: RectF?,            // position dans l'image
    val confidence: Float
)

enum class WoodUse {
    BO_OEUVRE_1, BO_OEUVRE_2, BO_OEUVRE_3,
    BIL, BCh, PATE, BOIS_ENERGIE
}

class QualityAssessmentEngine(
    private val defectModel: TfliteModel,    // EfficientNet-Lite multi-task
    private val onfRules: OnfGradingRules
) {

    suspend fun assess(photo: Bitmap, diameterCm: Double, essenceCode: String): QualityAssessment {
        // 1. Inférence modèle
        val output = defectModel.infer(photo)

        val qualityProbs = output.qualitySoftmax   // [p1, p2, p3, p4]
        val defectProbs = output.defectSigmoids    // Map<DefectType, Float>
        val globalConfidence = output.confidenceSigmoid

        // 2. Extraction des défauts détectés (seuil > 0.5)
        val defects = defectProbs
            .filter { it.value > 0.5f }
            .map { (type, prob) ->
                DetectedDefect(type, prob, null, prob)
            }

        // 3. Grading ONF par règles
        val rawGrade = qualityProbs.indexOfMax() + 1
        val correctedGrade = onfRules.applyDefectConstraints(rawGrade, defects, diameterCm, essenceCode)

        // 4. Usage recommandé
        val recommendedUse = onfRules.gradeToUse(correctedGrade, diameterCm, essenceCode)

        return QualityAssessment(
            qualityGrade = correctedGrade,
            defects = defects,
            confidence = globalConfidence,
            recommendedUse = recommendedUse,
            onfCompatible = true
        )
    }
}

object OnfGradingRules {

    fun applyDefectConstraints(
        rawGrade: Int,
        defects: List<DetectedDefect>,
        diameterCm: Double,
        essenceCode: String
    ): Int {
        var grade = rawGrade

        for (defect in defects) {
            val maxGrade = when (defect.type) {
                DefectType.CAVITY -> if (defect.severity > 0.5f) 3 else 4
                DefectType.FUNGUS -> 4
                DefectType.FORK -> 3
                DefectType.CRACK -> if (defect.severity > 0.7f) 4 else 3
                DefectType.CURVE -> if (defect.severity > 0.6f) 3 else 2
                DefectType.GALL -> 3
                DefectType.INSECT -> 4
                else -> 4
            }
            grade = minOf(grade, maxGrade)
        }

        // Contrainte de diamètre minimum
        val minDForBO = if (isConifer(essenceCode)) 25.0 else 30.0
        if (diameterCm < minDForBO && grade <= 3) grade = 4

        return grade.coerceIn(1, 4)
    }

    fun gradeToUse(grade: Int, diameterCm: Double, essenceCode: String): WoodUse {
        return when (grade) {
            1 -> WoodUse.BO_OEUVRE_1
            2 -> WoodUse.BO_OEUVRE_2
            3 -> if (diameterCm >= 20) WoodUse.BO_OEUVRE_3 else WoodUse.BIL
            4 -> if (diameterCm >= 7) WoodUse.BCh else WoodUse.PATE
            else -> WoodUse.BOIS_ENERGIE
        }
    }
}
```

### 15.4 Workflow intégré dans GeoSylva

```
UTILISATEUR PHOTOGRAPHIE L'ARBRE
  │
  ▼
[1] BarkNet → essence (top-3)
  │  └── Si < 90% : demander photo feuillage → LeafNet → fusion
  │  └── Filtrage géographique GPS
  │
  ▼
[2] QualityAssessmentEngine → qualité + défauts
  │
  ▼
[3] Affichage résultat :
  "Essence : Chêne sessile (94%)
   Qualité : 2 (BO d'œuvre choix 2)
   Défauts : courbure légère (0.3), branche encastrée (0.6)
   Usage recommandé : BO choix 2
   Confiance : 87%"
  │
  ▼
[4] L'utilisateur confirme ou corrige
  │  └── Si correction : feedback → apprentissage (federated)
  │
  ▼
[5] Données envoyées au VolumeEngine :
  essenceCode + qualityGrade + defects → ventilation produit précise
```

### 15.5 Dataset et entraînement

| Dataset | Source | Taille | Usage |
|---|---|---|---|
| **BarkNet-FR** | Crowdsourcing GeoSylva + IGN | 50k+ photos écorce | Classification essence |
| **LeafNet-FR** | INRAE + crowdsourcing | 20k+ photos feuillage | Classification essence (feuillus) |
| **WoodDefect-FR** | Experts ONF/CRPF + crowdsourcing | 10k+ photos annotées | Détection défauts + grading |
| **ClimbID** | Dataset public (international) | 100k+ images tronc | Pre-training transfer |

**Stratégie d'annotation** :
- Phase 1 : experts forestiers annotent 2000 photos (gold standard)
- Phase 2 : crowdsourcing avec validation croisée (3 annotations par image, majorité)
- Phase 3 : active learning — le modèle suggère, l'expert valide

---

## 16. Martelage assisté par IA et projection de croissance interactive

### 16.1 Martelage assisté — vision

Le martelage (ou marquage) est l'acte sylvicole le plus engageant : le forestier décide **pour chaque arbre** s'il faut le garder, l'élaguer, ou le récolter. Cette décision dépend de :
- L'essence et sa valeur économique
- Le diamètre et la hauteur (croissance actuelle)
- La qualité du tronc (défauts)
- La station (fertilité, exposition)
- L'âge et le stade sylvicole
- Les objectifs du propriétaire (production, biodiversité, paysage, protection)
- La concurrence (arbres voisins)
- La réglementation (RTM, Natura 2000, LB)

### 16.2 Architecture du martelage assisté

```
┌──────────────────────────────────────────────────────────────────────┐
│               GEOSYLVA MARTHELAGE ASSISTANT                            │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │ Tree Data    │  │ Stand Context│  │ Owner Goals  │               │
│  │              │  │              │  │              │               │
│  │ · Essence    │  │ · Peuplement │  │ · Objectifs  │               │
│  │ · D, H       │  │   type       │  │   prioritaires│              │
│  │ · Qualité    │  │ · Densité    │  │ · Contraintes│               │
│  │ · Défauts    │  │ · G (m²/ha)  │  │   réglement. │               │
│  │ · Position   │  │ · IS, station│  │ · Budget     │               │
│  │ · GPS        │  │ · Concurrence│  │              │               │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         │                 │                 │                        │
│         └─────────────────┼─────────────────┘                        │
│                           │                                          │
│                           ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              DECISION ENGINE                                  │   │
│  │                                                              │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │   │
│  │  │ Rules Engine│  │ Growth Model│  │ LLM Reasoner│          │   │
│  │  │ (ONF, CRPF) │  │ (Richards)  │  │ (T4+)       │          │   │
│  │  │             │  │             │  │             │          │   │
│  │  │ · Seuils D  │  │ · Projection│  │ · Contexte  │          │   │
│  │  │ · Règles    │  │   croissance│  │ · Justification│       │   │
│  │  │   sylvicoles│  │ · AMA/ACA   │  │ · Alternatives│        │   │
│  │  │ · RTM, N2000│  │ · Âge tech. │  │ · Nuances    │          │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │   │
│  │         └────────────────┼────────────────┘                  │   │
│  │                          ▼                                    │   │
│  │              ┌──────────────────┐                            │   │
│  │              │  Decision Fusion │                            │   │
│  │              │  (weighted vote) │                            │   │
│  │              └────────┬─────────┘                            │   │
│  └───────────────────────┼─────────────────────────────────────┘   │
│                          │                                          │
│                          ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              OUTPUT: MartelageDecision                         │   │
│  │                                                              │   │
│  │  · action : GARDER / ELAGUER / RECOLTER / COUPE_SANITAIRE    │   │
│  │  · confidence : 0-1                                          │   │
│  │  · justification : texte explicatif (LLM)                    │   │
│  │  · alternatives : liste d'actions possibles + raisons        │   │
│  │  · projection : croissance si GARDER (D, H, V dans 5/10/20 ans)│  │
│  │  · impact_economique : valeur actuelle vs future             │   │
│  │  · impact_ecologique : biodiversité, carbone, structure      │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### 16.3 Règles sylvicoles encodées

#### 16.3.1 Futaie régulière feuillue (chêne/hêtre)

```kotlin
object SylvicultureRules {

    fun evaluate(
        tree: TreeData,
        stand: StandContext,
        goals: OwnerGoals
    ): MartelageDecision {

        val action = when {
            // Coupe sanitaire : arbre malade ou dangereux
            tree.hasFungus || tree.hasCavity -> Action.COUPER_SANITAIRE

            // Récolte : arbre mature (D ≥ diamètre d'exploitabilité)
            tree.diameterCm >= exploitabilityDiameter(tree.essence, stand.station) ->
                if (tree.qualityGrade <= 2) Action.RECOLTER
                else Action.GARDER  // qualité faible → attendre ou coupe sanitaire

            // Élagage : arbre jeune de qualité, potentiel BO
            tree.diameterCm in 15.0..25.0 &&
            tree.qualityGrade <= 2 &&
            tree.essence in listOf("QUSE", "QUPE", "FASY") -> Action.ELAGUER

            // Coupe d'amélioration : arbre concurrent d'un arbre d'avenir
            stand.hasCompetitor(tree) && tree.qualityGrade >= 3 ->
                Action.RECOLTER  // libérer l'arbre d'avenir

            // Par défaut : garder
            else -> Action.GARDER
        }

        return MartelageDecision(
            action = action,
            confidence = computeConfidence(tree, stand, goals),
            justification = explain(action, tree, stand),
            alternatives = generateAlternatives(tree, stand, goals)
        )
    }

    private fun exploitabilityDiameter(essence: String, station: Station): Double {
        return when (essence) {
            "QUSE", "QUPE" -> if (station.quality >= 2) 55.0 else 60.0
            "FASY" -> 50.0
            "FRAX" -> 45.0
            "ABAL" -> 45.0
            "PISY" -> 40.0
            "EUSI" -> 35.0
            else -> 40.0
        }
    }
}
```

#### 16.3.2 Intégration LLM pour le raisonnement

Le LLM (T4+) enrichit la décision rule-based avec du **contexte nuancé** :

```
SYSTEM: Tu es un expert sylvicole français (ONF/CRPF). Tu reçois
les données d'un arbre et le contexte du peuplement. Propose une
décision de martelage avec justification technique.

USER:
  Arbre : Chêne sessile, D=52cm, H=28m, qualité 2
  Station : IS 22 (classe II), plaine, altitude 180m
  Peuplement : Futaie régulière, 120 ans, G=28m²/ha, densité 220 t/ha
  Concurrence : 2 chênes voisins (D=38, D=45) à 4m
  Objectifs : Production BO + biodiversité + régénération naturelle
  Réglementation : Natura 2000 (pas de coupe rase > 0.5ha)

ASSISTANT:
  Décision : RECOLTER
  Confiance : 85%

  Justification :
  Ce chêne sessile de 52cm a atteint le diamètre d'exploitabilité
  (55cm en station de classe II, mais 52cm avec qualité 2 justifie
  la récolte anticipée pour valoriser le BO avant dépréciation).
  L'ACA (accroissement courant annuel) estimé est de 0.8cm/an,
  soit un gain de seulement 0.4 m³/an. La valeur actuelle (BO
  choix 2, ~250€) est optimale.

  Le peuplement est dense (G=28m²/ha) — la récolte libère de
  l'espace pour les 2 chênes voisins (38 et 45cm) qui sont des
  arbres d'avenir. La régénération naturelle sera favorisée par
  l'ouverture du couvert.

  Alternative : GARDER 5 ans de plus pour atteindre 56cm (BO
  choix 1 possible si la qualité se maintient), gain estimé
  ~100€ mais risque de dépréciation (coloration, fissuration).
```

### 16.4 Projection de croissance interactive

#### 16.4.1 Modèle de projection

La projection utilise le **modèle de Richards** déjà implémenté dans `ExpertForestryCalculator` (@`ExpertForestryCalculator.kt:42`), étendu avec visualisation :

```kotlin
data class GrowthProjection(
    val years: List<Int>,              // [0, 5, 10, 15, 20, 30]
    val diameterCm: List<Double>,      // projection D par année
    val heightM: List<Double>,         // projection H par année
    val volumeM3: List<Double>,        // projection V par année
    val volumePerProduct: List<Map<WoodUse, Double>>,  // ventilation par année
    val valueEur: List<Double>,        // valeur estimée par année
    val carbonKg: List<Double>,        // carbone stocké par année
    val optimalHarvestYear: Int,       // année de récolte optimale (NPV max)
    val npvEur: Double                 // valeur actuelle nette
)

class GrowthProjector(
    private val richardsModel: RichardsGrowthModel,
    private val volumeEngine: VolumeEngine,
    private val pricingEngine: PricingEngine
) {

    fun project(
        tree: TreeData,
        stand: StandContext,
        horizonYears: Int = 30
    ): GrowthProjection {
        val years = (0..horizonYears step 5).toList()
        val currentAge = estimateAge(tree, stand)

        val projections = years.map { year ->
            val futureAge = currentAge + year
            val d = richardsModel.projectDiameter(tree.essence, futureAge, stand.station)
            val h = richardsModel.projectHeight(tree.essence, futureAge, stand.station)
            val v = volumeEngine.estimateSync(tree.copy(diameterCm = d, heightM = h))
            val products = ventilateByProduct(v, d, tree.essence)
            val value = pricingEngine.value(products, tree.essence)
            val carbon = computeCarbon(v, tree.essence)
            ProjectionPoint(year, d, h, v.volumeM3, products, value, carbon)
        }

        // Année de récolte optimale : max NPV
        val npvByYear = projections.map { p ->
            p.year to p.value / (1.0 + DISCOUNT_RATE).pow(p.year)
        }
        val optimalYear = npvByYear.maxByOrNull { it.second }?.first ?: 0
        val npv = npvByYear.maxByOrNull { it.second }?.second ?: 0.0

        return GrowthProjection(
            years = projections.map { it.year },
            diameterCm = projections.map { it.d },
            heightM = projections.map { it.h },
            volumeM3 = projections.map { it.v },
            volumePerProduct = projections.map { it.products },
            valueEur = projections.map { it.value },
            carbonKg = projections.map { it.carbon },
            optimalHarvestYear = optimalYear,
            npvEur = npv
        )
    }
}
```

#### 16.4.2 Visualisation 3D de l'évolution

```
RENDERING 3D (Compose + OpenGL ES ou Sceneform) :

1. Arbre actuel :
   - Tronc : cylindre D × H (couleurs réalistes)
   - Couronne : forme essence-spécifique (ellipsoïde, conique)
   - Couleur feuillage : vert (saison)

2. Slider temporel (0 → 30 ans) :
   - Tronc grandit (D et H augmentent)
   - Couronne s'élargit
   - Couleur change en automne (jaune/orange)

3. Annotations :
   - D, H, V, valeur € affichés en overlay
   - Marqueur "Année optimale de récolte" sur le slider
   - Comparaison : valeur actuelle vs valeur future

4. Vue peuplement (optionnel) :
   - Tous les arbres de la placette en 3D
   - Animation de croissance collective
   - Mortalité simulée (arbres qui disparaissent)
   - Régénération (nouveaux arbres qui apparaissent)
```

#### 16.4.3 Simulation de scénarios sylvicoles

```
SCÉNARIO A — Aucune intervention (laisser faire)
  → Projection naturelle : croissance, mortalité, fermeture du couvert
  → Résultat : peuplement dense, D moyen stagnant, valeur faible

SCÉNARIO B — Éclaircie légère (20% des tiges)
  → Arbres prélevés : petits diamètres, qualité 3-4
  → Projection : arbres restants accélèrent (plus de lumière)
  → Résultat : D moyen augmente, valeur future plus élevée

SCÉNARIO C — Coupe de régénération (50% + ouverture)
  → Arbres prélevés : tous les matures + ouverture trouées
  → Projection : régénération naturelle, nouvelle cohorte
  → Résultat : renouvellement du peuplement

COMPARAISON AUTOMATIQUE :
  | Métrique | Scénario A | Scénario B | Scénario C |
  |---|---|---|---|
  | NPV (30 ans) | 8,500€/ha | 12,000€/ha | 15,000€/ha |
  | D moyen final | 42cm | 48cm | 35cm (nouvelle cohorte) |
  | Carbone stocké | 180 t/ha | 165 t/ha | 120 t/ha |
  | Biodiversité | Moyenne | Bonne | Excellente |
  | Risque tempête | Élevé | Moyen | Faible |
```

### 16.5 Workflow UI du martelage assisté

```
ÉCRAN MARTHELAGE (mode terrain) :

┌─────────────────────────────────────┐
│  Parcelle : Bois de la Chaume       │
│  42 arbres marqués / ~150 estimés   │
│  G actuelle : 26 m²/ha              │
├─────────────────────────────────────┤
│                                     │
│  [Photo arbre]  [Scan LiDAR]        │
│                                     │
│  Essence : Chêne sessile (94%)      │
│  D : 52cm    H : 28m ± 0.4m         │
│  Qualité : 2 (BO choix 2)           │
│  Volume : 2.8 m³ ± 0.15             │
│  Valeur : ~250€                     │
│                                     │
│  ┌─────────────────────────────────┐│
│  │  🤖 RECOMMANDATION IA           ││
│  │  Action : RÉCOLTER (85%)        ││
│  │                                 ││
│  │  "Chêne mature, qualité 2,      ││
│  │   valeur optimale. Libère 2     ││
│  │   arbres d'avenir (38, 45cm).   ││
│  │   Favorise régénération."       ││
│  │                                 ││
│  │  Alternatives :                 ││
│  │  · GARDER 5 ans (+100€, risque) ││
│  │  · GARDER 10 ans (NPV -50€)     ││
│  └─────────────────────────────────┘│
│                                     │
│  [Garder]  [Élaguer]  [Récolter]    │
│  [Sanitaire]  [Voir projection 3D]  │
│                                     │
│  GPS : 48.1234°N, 4.5678°E          │
│  Marquer : 🟢 (vert = récolter)     │
└─────────────────────────────────────┘
```

---

## 17. Certification carbone MRV, inventaire collaboratif et optimisation économique

### 17.1 Certification carbone — chaîne MRV automatisée

#### 17.1.1 Contexte

La **MRV** (Measurement, Reporting, Verification) est le processus de certification des crédits carbone. GeoSylva peut automatiser toute la chaîne :

```
MESURE (M) → RAPPORT (R) → VÉRIFICATION (V) → CERTIFICATION

M : Inventaire GeoSylva (D, H, essence, volume, biomasse, carbone)
R : Génération automatique de rapports conformes (Label Bas Carbone, VCS)
V : Audit traçable (chaque mesure a un GPS, timestamp, méthode, incertitude)
C : Soumission à l'organisme certificateur (OFAC, Verra, Gold Standard)
```

#### 17.1.2 Standards supportés

| Standard | Méthode | Marché | GeoSylva |
|---|---|---|---|
| **Label Bas Carbone (LBC)** | France, ministère MTE | Volontaire français | Support natif |
| **VCS (Verra)** | International | Volontaire international | Export de données |
| **Gold Standard** | International | Volontaire + co-bénéfices | Export de données |
| **CDM (Kyoto)** | International | Réglementé (pays Annexe 1) | Export de données |
| **PEFC / FSC** | Certification forestière | Gestion durable | Données de gestion |

#### 17.1.3 Calcul carbone amélioré

Le système actuel utilise des **valeurs IPCC par défaut** (BEF 1.65/1.45, RER 0.25). La nouvelle génération utilise des **équations allométriques calibrées par essence** :

```kotlin
data class CarbonCalculation(
    val treeVolumeM3: Double,
    val biomassStemKg: Double,
    val biomassBarkKg: Double,
    val biomassBranchesKg: Double,
    val biomassFoliageKg: Double,
    val biomassRootsKg: Double,
    val biomassTotalKg: Double,
    val carbonKg: Double,
    val co2EquivalentKg: Double,
    val method: CarbonMethod,
    val uncertainty: Double,           // ±% (IC 90%)
    val sourceEquations: List<String>  // références bibliographiques
)

class CarbonEngine {

    fun compute(
        volumeM3: Double,
        essenceCode: String,
        densityMethod: WoodDensityMethod = WoodDensityMethod.SPECIES_SPECIFIC
    ): CarbonCalculation {

        // 1. Densité du bois spécifique par essence (kg/m³)
        val woodDensity = WoodDensityDatabase.getDensity(essenceCode, densityMethod)

        // 2. Biomasse tige = volume × densité
        val biomassStem = volumeM3 * woodDensity

        // 3. BEF spécifique par essence (pas IPCC par défaut)
        val bef = BiomassExpansionFactorDatabase.getBEF(essenceCode)

        // 4. Biomasse aérienne = tige × BEF (décomposition par composante)
        val barkRatio = BarkRatioDatabase.get(essenceCode)        // ex: 0.12
        val branchRatio = BranchRatioDatabase.get(essenceCode)    // ex: 0.18
        val foliageRatio = FoliageRatioDatabase.get(essenceCode)  // ex: 0.03

        val biomassBark = biomassStem * barkRatio
        val biomassBranches = biomassStem * branchRatio
        val biomassFoliage = biomassStem * foliageRatio

        // 5. RER spécifique par essence (pas 0.25 par défaut)
        val rer = RootShootRatioDatabase.get(essenceCode)         // ex: 0.22-0.35
        val biomassRoots = biomassStem * (1 + barkRatio + branchRatio + foliageRatio) * rer

        // 6. Total
        val biomassTotal = biomassStem + biomassBark + biomassBranches +
                           biomassFoliage + biomassRoots

        // 7. Carbone = biomasse × fraction carbone (0.475 pour feuillus, 0.505 pour résineux)
        val carbonFraction = if (isConifer(essenceCode)) 0.505 else 0.475
        val carbon = biomassTotal * carbonFraction

        // 8. CO₂-équivalent
        val co2Eq = carbon * 3.67

        // 9. Incertitude (propagation)
        val densityUncertainty = WoodDensityDatabase.getUncertainty(essenceCode)
        val befUncertainty = BiomassExpansionFactorDatabase.getUncertainty(essenceCode)
        val totalUncertainty = sqrt(
            densityUncertainty.pow(2) + befUncertainty.pow(2) + 0.05.pow(2)
        )

        return CarbonCalculation(
            treeVolumeM3 = volumeM3,
            biomassStemKg = biomassStem,
            biomassBarkKg = biomassBark,
            biomassBranchesKg = biomassBranches,
            biomassFoliageKg = biomassFoliage,
            biomassRootsKg = biomassRoots,
            biomassTotalKg = biomassTotal,
            carbonKg = carbon,
            co2EquivalentKg = co2Eq,
            method = CarbonMethod.SPECIES_SPECIFIC_ALLOMETRIC,
            uncertainty = totalUncertainty * 100,
            sourceEquations = listOf(
                "IPCC 2006 GL AFOLU Vol.4 Ch.4",
                "Wirth et al. (2009) — densités essences françaises",
                "Vande Walle et al. (2005) — BEF feuillus tempérés"
            )
        )
    }
}
```

#### 17.1.4 Rapport MRV automatique

```kotlin
data class MrvReport(
    val projectId: String,
    val projectType: ProjectType,          // AFFORESTATION, REFORESTATION, IFM, AGROFORESTRY
    val standard: CarbonStandard,          // LBC, VCS, GOLD_STANDARD
    val period: Pair<LocalDate, LocalDate>,
    val plots: List<PlotSummary>,
    val totalCarbonTons: Double,
    val uncertaintyPercent: Double,
    val leakageTons: Double,               // fuites de carbone
    val permanenceRisk: Double,            // risque de non-permanence (%)
    val netCarbonCredits: Double,          // crédits nets après déduction
    val methodology: String,               // ex: "VM0037 v2.0"
    val evidenceChain: List<EvidenceItem>  // traçabilité
)

data class EvidenceItem(
    val treeId: String,
    val gpsLocation: GpsLocation,
    val measurementDate: Long,
    val method: String,                    // "KOZAK_TAPER", "QSM", "AI_HYBRID"
    val volumeM3: Double,
    val volumeUncertainty: Double,
    val carbonKg: Double,
    val photoHash: String?,                // hash de la photo pour preuve
    val operatorId: String
)
```

Le rapport est généré en **PDF + JSON structuré**, prêt pour soumission à l'organisme certificateur.

### 17.2 Réseau mesh LoRa natif GeoSylva — collaboration temps réel en forêt

#### 17.2.1 Pourquoi un mesh LoRa natif dans GeoSylva ?

Le contexte forestier rend les communications classiques (4G/5G, WiFi) **indisponibles ou peu fiables** :
- Couverture cellulaire absente en zone rurale et montagneuse
- WiFi limité à quelques dizaines de mètres
- Satellite (Starlink, Iridium) coûteux et encombrant

**La solution : un mesh LoRa 868MHz géré nativement par GeoSylva**, sans dépendre d'une app externe. GeoSylva communique **directement** avec les nœuds LoRa par BLE, gère ses propres canaux chiffrés, ses pages UI dédiées, synchronise automatiquement les martelages entre opérateurs, et route les données vers le serveur GSIE au bureau pour analyse par les moteurs PC.

| Caractéristique | LoRa 868MHz (Europe) | 4G forestier | WiFi |
|---|---|---|---|
| Portée en forêt | **1-5 km** (mesh étend) | 0-2 km (si couverture) | 30-50m |
| Portée en dégagé | **5-15 km** | variable | 100m |
| Débit | 250 bps - 21 kbps | 1-100 Mbps | 100-300 Mbps |
| Consommation | **~100mW (très faible)** | 1-3W | 1-2W |
| Autonomie batterie | **3-7 jours** (nœud mesh) | 4-8h (phone) | 4-8h |
| Coût hardware | **30-60€** par nœud | téléphone existant | téléphone existant |
| Infrastructure | **Aucune** (mesh P2P) | antennes relais | routeur |
| Fonctionnement | **Offline natif** | nécessite opérateur | nécessite AP |

**Avantages d'une intégration native vs app Meshtastic externe** :
- **UX unifiée** : l'opérateur ne quitte jamais GeoSylva — pas de bascule entre deux apps
- **Contrôle total** : GeoSylva gère les nœuds privés, les canaux, les clés, le routage
- **Sync automatique** : chaque martelage est diffusé aux autres opérateurs sans action manuelle
- **Route vers GSIE** : au retour au bureau, GeoSylva pousse les données vers le serveur GSIE pour analyse par les moteurs PC
- **Pas de dépendance externe** : pas de mise à jour Meshtastic à attendre, pas de breaking change

#### 17.2.2 Architecture matérielle

```
┌──────────────────────────────────────────────────────────────────────┐
│               GEOSYLVA MESH NATIF — RÉSEAU FORESTIER                   │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ Opérateur A      │  │ Opérateur B      │  │ Chef d'équipe    │   │
│  │                  │  │                  │  │                  │   │
│  │ ┌──────────────┐ │  │ ┌──────────────┐ │  │ ┌──────────────┐ │   │
│  │ │  Smartphone  │ │  │ │  Smartphone  │ │  │ │  Tablette    │ │   │
│  │ │  GeoSylva    │ │  │ │  GeoSylva    │ │  │ │  GeoSylva    │ │   │
│  │ │  (app native)│ │  │ │  (app native)│ │  │ │  (app native)│ │   │
│  │ └──────┬───────┘ │  │ └──────┬───────┘ │  │ └──────┬───────┘ │   │
│  │        │ BLE     │  │        │ BLE     │  │        │ BLE     │   │
│  │ ┌──────▼───────┐ │  │ ┌──────▼───────┐ │  │ ┌──────▼───────┐ │   │
│  │ │ Nœud LoRa   │ │  │ │ Nœud LoRa   │ │  │ │ Nœud LoRa   │ │   │
│  │ │ LilyGO      │ │  │ │ Heltec      │ │  │ │ LilyGO      │ │   │
│  │ │ T-Echo      │ │  │ │ LoRa 32     │ │  │ │ T-Echo      │ │   │
│  │ │ nRF52+SX1262│ │  │ │ ESP32+SX1262│ │  │ │ nRF52+SX1262│ │   │
│  │ │ GPS intégré │ │  │ │ GPS intégré │ │  │ │ GPS intégré │ │   │
│  │ └──────┬───────┘ │  │ └──────┬───────┘ │  │ └──────┬───────┘ │   │
│  └────────┼─────────┘  └────────┼─────────┘  └────────┼─────────┘   │
│           │                       │                       │            │
│           │    LoRa 868MHz        │    LoRa 868MHz        │            │
│           │◄─────────────────────►│◄─────────────────────►│            │
│           │    Mesh relay         │    Mesh relay         │            │
│           │                       │                       │            │
│           │          ┌────────────▼───────────┐           │            │
│           └──────────►│  Nœud relais (option) │◄──────────┘            │
│                      │  LilyGO T-Beam        │                        │
│                      │  Position élevée      │                        │
│                      │  (haut d'arbre, cime) │                        │
│                      │  Portée étendue       │                        │
│                      └──────────────────────┘                        │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  RETOUR AU BUREAU — Sync vers serveur GSIE                    │   │
│  │                                                              │   │
│  │  Smartphone GeoSylva                                        │   │
│  │  → WiFi / 4G / Ethernet                                     │   │
│  │  → POST /api/v1/sync/inventory                              │   │
│  │  → Upload données inventaire complet (JSON + photos)        │   │
│  │  → Serveur GSIE (FastAPI + PostgreSQL/PostGIS)              │   │
│  │  → Moteurs GSIE : ForestDynamics, Carbon, Volume, etc.     │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

#### 17.2.3 Hardware supporté

| Nœud | MCU | Radio | GPS | Autonomie | Prix | Usage |
|---|---|---|---|---|---|---|
| **LilyGO T-Echo** | nRF52840 | SX1262 | u-blox | **7-10 jours** | ~45€ | Recommandé (basse conso) |
| **Heltec LoRa 32 V3** | ESP32-S3 | SX1262 | — | 2-3 jours | ~25€ | Économique |
| **LilyGO T-Beam** | ESP32 | SX1276 | u-blox | 2-4 jours | ~35€ | Relais / gateway |
| **RAK WisBlock 4631** | nRF52840 | SX1262 | — | 5-7 jours | ~50€ | Compact, modulaire |

**Recommandation GeoSylva** : LilyGO T-Echo pour les opérateurs (nRF52 = ultra basse consommation, GPS intégré, écran e-ink pour lecture extérieure), T-Beam pour les nœuds relais.

#### 17.2.4 Gestion native des nœuds privés GeoSylva

GeoSylva gère ses propres nœuds LoRa comme des **périphériques privés**. L'app scanne les nœuds BLE, les associe, configure les canaux et les clés de chiffrement — le tout depuis l'interface GeoSylva, sans jamais ouvrir une app externe.

```kotlin
data class LoRaNode(
    val nodeId: UInt32,
    val name: String,                    // "T-Echo-Alice"
    val hardwareModel: HardwareModel,    // T_ECHO, HELTEC_V3, T_BEAM
    val batteryPct: Int,
    val firmwareVersion: String,
    val gpsFix: GpsFix?,
    val rssiDbm: Int,
    val snr: Float,
    val isPaired: Boolean,
    val channels: List<GeoSylvaChannel>
)

data class GeoSylvaChannel(
    val channelIndex: Int,               // 0-7
    val name: String,                    // "GEOSYLVA_DATA"
    val psk: ByteArray,                  // clé AES-128 (32 bytes hex)
    val isPrimary: Boolean,
    val hopLimit: Int,                   // 3-7
    val uplinkEnabled: Boolean,
    val downlinkEnabled: Boolean
)

class LoRaNodeManager(private val context: Context) {

    private val _nodes = MutableStateFlow<List<LoRaNode>>(emptyList())
    val nodes: StateFlow<List<LoRaNode>> = _nodes.asStateFlow()

    private val _connectedNode = MutableStateFlow<LoRaNode?>(null)
    val connectedNode: StateFlow<LoRaNode?> = _connectedNode.asStateFlow()

    /**
     * Scan BLE des nœuds LoRa à proximité.
     */
    suspend fun scanNodes(timeoutMs: Long = 10_000): List<LoRaNode> {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val results = scanner.scan(timeoutMs)

        return results.map { device ->
            LoRaNode(
                nodeId = extractNodeId(device),
                name = device.name ?: "Nœud ${device.address.takeLast(5)}",
                hardwareModel = detectHardwareModel(device),
                batteryPct = 0,
                firmwareVersion = "",
                gpsFix = null,
                rssiDbm = device.rssi,
                snr = 0f,
                isPaired = isAlreadyPaired(device),
                channels = emptyList()
            )
        }
    }

    /**
     * Appairage d'un nœud LoRa — configuration des canaux et clés.
     */
    suspend fun pairNode(node: LoRaNode, teamKey: ByteArray): Result<LoRaNode> {
        return try {
            val bleConnection = BleConnection.connect(node.nodeId)

            // Configuration des 3 canaux GeoSylva
            val channels = createDefaultChannels(teamKey)
            for (channel in channels) {
                bleConnection.configureChannel(channel)
            }

            // Configuration radio (868.1 MHz, SF9, BW 250kHz, 17 dBm)
            bleConnection.setRadioConfig(
                frequency = 868_100_000,
                spreadingFactor = 9,
                bandwidth = 250_000,
                txPower = 17
            )

            savePairedNode(node.copy(isPaired = true, channels = channels))
            _nodes.update { it + node }
            Result.success(node)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crée les 3 canaux GeoSylva avec la clé d'équipe.
     */
    private fun createDefaultChannels(teamKey: ByteArray): List<GeoSylvaChannel> {
        return listOf(
            GeoSylvaChannel(0, "GEOSYLVA_DATA", teamKey, true, 3, true, true),
            GeoSylvaChannel(1, "GEOSYLVA_PRESENCE", deriveChannelKey(teamKey, "presence"), false, 5, true, true),
            GeoSylvaChannel(2, "GEOSYLVA_CHAT", deriveChannelKey(teamKey, "chat"), false, 3, true, true)
        )
    }

    /**
     * Génération de la clé d'équipe (chef d'équipe) + partage QR code.
     */
    fun generateTeamKey(): ByteArray {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return key
    }

    fun encodeTeamKeyQr(key: ByteArray): String = "geosylva://mesh?key=${key.toHex()}"
    fun decodeTeamKeyQr(qr: String): ByteArray? {
        val prefix = "geosylva://mesh?key="
        if (!qr.startsWith(prefix)) return null
        return qr.removePrefix(prefix).hexToByteArray()
    }
}
```

#### 17.2.5 Protocole binaire et canaux chiffrés

GeoSylva utilise un **protocole binaire propriétaire** optimisé pour LoRa (237 bytes max par paquet). 3 canaux chiffrés AES-128 :

```
CANAL 0 — GEOSYLVA_DATA (chiffré AES-128, clé d'équipe)
  Messages : mesures d'arbres, marquages, sync inventaire
  Priorité : haute (hop limit 3)
  Taille max : 237 bytes par paquet LoRa

CANAL 1 — GEOSYLVA_PRESENCE (chiffré, clé dérivée)
  Messages : position GPS opérateurs, statut (actif/pause)
  Priorité : normale (hop limit 5)
  Fréquence : beacon toutes les 60s

CANAL 2 — GEOSYLVA_CHAT (chiffré, clé dérivée)
  Messages : chat texte entre opérateurs, annotations sur arbres
  Priorité : basse (hop limit 3)
```

```
FORMAT BINAIRE — Message Mesure Arbre (47 bytes) :

  [0]     msg_type      : uint8   = 0x01 (TREE_MEASUREMENT)
  [1-8]   tree_id       : uint64  = hash GPS + timestamp
  [9-12]  latitude      : int32   = lat × 10^7 (précision ~1cm)
  [13-16] longitude     : int32   = lon × 10^7
  [17-18] altitude_cm   : int16   = altitude en cm
  [19]    essence_code  : uint8   = code essence (table GeoSylva)
  [20-21] diameter_mm   : uint16  = diamètre en mm (0-65535 = 0-65m)
  [22-23] height_dm     : uint16  = hauteur en dm (0-6553m)
  [24]    quality_grade : uint8   = 1-4
  [25]    defects_mask  : uint8   = bitmap (10 défauts → 10 bits)
  [26-29] volume_cm3   : uint32  = volume en cm³
  [30-31] carbon_kg_x10: uint16  = carbone × 10 (0-6553 kg)
  [32]    action        : uint8   = 0=garder, 1=élaguer, 2=récolter, 3=sanitaire
  [33-36] operator_id   : uint32  = identifiant opérateur
  [37-44] timestamp     : uint64  = epoch millis
  [45]    confidence    : uint8   = 0-100 (%)
  [46]    checksum      : uint8   = XOR des bytes 0-45

Total : 47 bytes → 1 paquet LoRa (sous la limite 237 bytes)

FORMAT BINAIRE — Message Position (26 bytes) :

  [0]     msg_type      : uint8   = 0x02 (OPERATOR_POSITION)
  [1-4]   latitude      : int32   = lat × 10^7
  [5-8]   longitude     : int32   = lon × 10^7
  [9-10]  altitude_cm   : int16   = altitude en cm
  [11]    battery_pct   : uint8   = 0-100
  [12-15] operator_id   : uint32
  [16-23] timestamp     : uint64
  [24]    status        : uint8   = 0=actif, 1=pause, 2=retour
  [25]    checksum      : uint8

Total : 26 bytes → 1 paquet LoRa

FORMAT BINAIRE — Message Chat (max 195 bytes) :

  [0]     msg_type      : uint8   = 0x03 (CHAT_MESSAGE)
  [1-4]   operator_id   : uint32
  [5-12]  timestamp     : uint64
  [13]    msg_len       : uint8   = longueur texte (max 180)
  [14-193] text         : UTF-8   = message texte
  [194]   checksum      : uint8

Total : 195 bytes max → 1 paquet LoRa
```

```kotlin
object MeshProtocol {

    const val MSG_TREE_MEASUREMENT: Byte = 0x01
    const val MSG_OPERATOR_POSITION: Byte = 0x02
    const val MSG_CHAT: Byte = 0x03
    const val MSG_DOUBLE_MARK_ALERT: Byte = 0x04
    const val MSG_INVENTORY_SYNC_REQUEST: Byte = 0x05
    const val MSG_INVENTORY_SYNC_RESPONSE: Byte = 0x06
    const val MSG_GSIE_SYNC_BATCH: Byte = 0x07

    fun encodeTreeMeasurement(tree: TreeMeasurement): ByteArray {
        val buffer = ByteBuffer.allocate(47)
        buffer.put(MSG_TREE_MEASUREMENT)
        buffer.putLong(tree.id)
        buffer.putInt((tree.latitude * 10_000_000).toInt())
        buffer.putInt((tree.longitude * 10_000_000).toInt())
        buffer.putShort((tree.altitudeM * 100).toInt().toShort())
        buffer.put(EssenceTable.getCode(tree.essenceCode))
        buffer.putShort(tree.diameterMm.toShort())
        buffer.putShort(tree.heightDm.toShort())
        buffer.put(tree.qualityGrade.toByte())
        buffer.put(tree.defectsMask)
        buffer.putInt(tree.volumeCm3)
        buffer.putShort((tree.carbonKg * 10).toInt().toShort())
        buffer.put(tree.action.ordinal.toByte())
        buffer.putInt(tree.operatorId)
        buffer.putLong(tree.timestamp)
        buffer.put(tree.confidencePercent.toByte())
        buffer.put(checksum(buffer.array(), 0, 46))
        return buffer.array()
    }

    fun decodeTreeMeasurement(data: ByteArray): TreeMeasurement? {
        if (data.size < 47 || data[0] != MSG_TREE_MEASUREMENT) return null
        val buffer = ByteBuffer.wrap(data)
        if (data[46] != checksum(data, 0, 46)) return null

        return TreeMeasurement(
            id = buffer.getLong(1),
            latitude = buffer.getInt(9) / 10_000_000.0,
            longitude = buffer.getInt(13) / 10_000_000.0,
            altitudeM = buffer.getShort(17) / 100.0,
            essenceCode = EssenceTable.getCode(buffer.get(19).toInt()),
            diameterMm = buffer.getShort(20).toInt(),
            heightDm = buffer.getShort(22).toInt(),
            qualityGrade = buffer.get(24).toInt(),
            defectsMask = buffer.get(25),
            volumeCm3 = buffer.getInt(26),
            carbonKg = buffer.getShort(30).toInt() / 10.0,
            action = Action.values()[buffer.get(32).toInt()],
            operatorId = buffer.getInt(33),
            timestamp = buffer.getLong(37),
            confidencePercent = buffer.get(45).toInt()
        )
    }

    private fun checksum(data: ByteArray, start: Int, end: Int): Byte {
        var xor: Byte = 0
        for (i in start until end) {
            xor = (xor.toInt() xor data[i].toInt()).toByte()
        }
        return xor
    }
}
```

#### 17.2.6 Moteur mesh natif GeoSylva — `GeoSylvaMeshEngine`

GeoSylva intègre un **moteur mesh complet** qui gère la communication BLE avec le nœud LoRa, l'envoi/réception des messages, la synchronisation automatique des martelages, et la route vers le serveur GSIE.

```kotlin
class GeoSylvaMeshEngine(
    private val context: Context,
    private val localDatabase: InventoryDatabase,
    private val nodeManager: LoRaNodeManager,
    private val gsieApiClient: GsieApiClient
) {

    private val _meshState = MutableStateFlow(MeshState())
    val meshState: StateFlow<MeshState> = _meshState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<MeshMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MeshMessage> = _incomingMessages

    private val pendingMessages = mutableListOf<ByteArray>()
    private var bleConnection: BleConnection? = null

    /**
     * Connexion au nœud LoRa appairé via BLE.
     */
    suspend fun connect(nodeId: UInt32): Result<Unit> {
        return try {
            val node = nodeManager.getPairedNode(nodeId)
                ?: return Result.failure(IllegalArgumentException("Nœud non appairé"))

            bleConnection = BleConnection.connect(node.nodeId)
            bleConnection?.incomingPackets?.collect { packet ->
                handleIncomingPacket(packet)
            }

            _meshState.update { it.copy(isConnected = true) }
            flushPendingMessages()
            Result.success(Unit)
        } catch (e: Exception) {
            _meshState.update { it.copy(isConnected = false) }
            Result.failure(e)
        }
    }

    /**
     * Diffuse une mesure d'arbre sur le mesh — automatique après chaque martelage.
     */
    suspend fun broadcastTreeMeasurement(tree: TreeMeasurement): Result<Unit> {
        val encoded = MeshProtocol.encodeTreeMeasurement(tree)
        localDatabase.upsertTreeMeasurement(tree)

        return if (bleConnection?.isConnected() == true) {
            bleConnection?.send(channel = 0, data = encoded, hopLimit = 3)
            _meshState.update { it.copy(messagesSent = it.messagesSent + 1) }
            Result.success(Unit)
        } else {
            pendingMessages.add(encoded)
            Result.success(Unit)
        }
    }

    /**
     * Traite un paquet LoRa entrant.
     */
    private suspend fun handleIncomingPacket(packet: LoRaPacket) {
        when (packet.data[0]) {
            MeshProtocol.MSG_TREE_MEASUREMENT -> {
                val tree = MeshProtocol.decodeTreeMeasurement(packet.data)
                if (tree != null) {
                    localDatabase.upsertTreeMeasurement(tree)

                    // Détection double-marquage
                    val nearby = localDatabase.findTreesWithin(
                        tree.latitude, tree.longitude, radiusM = 3.0
                    ).filter { it.operatorId != tree.operatorId }

                    if (nearby.isNotEmpty()) {
                        val alert = MeshProtocol.encodeDoubleMarkAlert(
                            tree.id, nearby.first().id, nearby.first().distanceTo(tree)
                        )
                        bleConnection?.send(channel = 0, data = alert, hopLimit = 3)
                    }

                    _incomingMessages.emit(MeshMessage.TreeMeasurement(tree))
                    _meshState.update { it.copy(messagesReceived = it.messagesReceived + 1) }
                }
            }

            MeshProtocol.MSG_OPERATOR_POSITION -> {
                val pos = MeshProtocol.decodeOperatorPosition(packet.data)
                _meshState.update { it.copy(operators = it.operators + (pos.operatorId to pos)) }
                _incomingMessages.emit(MeshMessage.OperatorPosition(pos))
            }

            MeshProtocol.MSG_DOUBLE_MARK_ALERT -> {
                _incomingMessages.emit(
                    MeshMessage.DoubleMarkAlert(MeshProtocol.decodeDoubleMarkAlert(packet.data))
                )
            }

            MeshProtocol.MSG_CHAT -> {
                _incomingMessages.emit(
                    MeshMessage.Chat(MeshProtocol.decodeChatMessage(packet.data))
                )
            }

            MeshProtocol.MSG_INVENTORY_SYNC_REQUEST -> handleSyncRequest(packet)
        }
    }

    /**
     * Delta sync : envoie seulement les arbres que l'autre opérateur n'a pas.
     */
    private suspend fun handleSyncRequest(packet: LoRaPacket) {
        val requesterKnownIds = MeshProtocol.decodeSyncRequest(packet.data)
        val missingTrees = localDatabase.getAllTreeMeasurements()
            .filter { it.id !in requesterKnownIds }

        for (batch in missingTrees.chunked(5)) {  // 5 × 47 = 235 < 237
            val encoded = MeshProtocol.encodeSyncResponse(batch)
            bleConnection?.send(channel = 0, data = encoded, hopLimit = 3)
            delay(2000)
        }
    }

    /**
     * Envoie un message chat sur le mesh.
     */
    suspend fun sendChatMessage(operatorId: Int, text: String): Result<Unit> {
        val encoded = MeshProtocol.encodeChatMessage(operatorId, text)
        return if (bleConnection?.isConnected() == true) {
            bleConnection?.send(channel = 2, data = encoded, hopLimit = 3)
            Result.success(Unit)
        } else {
            pendingMessages.add(encoded)
            Result.success(Unit)
        }
    }

    /**
     * Beacon position GPS périodique.
     */
    suspend fun sendOperatorBeacon(
        operatorId: Int, latitude: Double, longitude: Double,
        altitudeM: Double, batteryPct: Int, status: OperatorStatus
    ) {
        val encoded = MeshProtocol.encodeOperatorPosition(
            operatorId, latitude, longitude, altitudeM, batteryPct, status
        )
        if (bleConnection?.isConnected() == true) {
            bleConnection?.send(channel = 1, data = encoded, hopLimit = 5)
        }
    }

    // ================================================================
    //  ROUTE VERS SERVEUR GSIE
    // ================================================================

    /**
     * Sync complète vers le serveur GSIE au retour au bureau.
     * Appelée automatiquement (détection WiFi/4G) ou manuellement.
     */
    suspend fun syncToGsieServer(parcelleId: Long): Result<GsieSyncResult> {
        return try {
            val inventory = localDatabase.getParcelleInventory(parcelleId)
            val photos = localDatabase.getParcellePhotos(parcelleId)

            val payload = GsieSyncPayload(
                parcelleId = parcelleId,
                syncTimestamp = System.currentTimeMillis(),
                operatorId = getCurrentOperatorId(),
                trees = inventory.map { it.toGsieTreeDto() },
                photos = photos.map { it.toGsiePhotoDto() },
                meshMetadata = _meshState.value.toGsieMeshMetadata()
            )

            val result = gsieApiClient.syncInventory(payload)
            localDatabase.markAsSynced(parcelleId, result.syncId)

            if (result.analysisResults != null) {
                localDatabase.saveAnalysisResults(parcelleId, result.analysisResults)
            }

            _meshState.update { it.copy(lastSyncGsie = System.currentTimeMillis()) }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun flushPendingMessages() {
        if (pendingMessages.isEmpty()) return
        val toFlush = pendingMessages.toList()
        pendingMessages.clear()
        for (msg in toFlush) {
            bleConnection?.send(channel = 0, data = msg, hopLimit = 3)
            delay(500)
        }
    }
}

data class MeshState(
    val isConnected: Boolean = false,
    val operators: Map<Int, OperatorPosition> = emptyMap(),
    val messagesSent: Int = 0,
    val messagesReceived: Int = 0,
    val rssiDbm: Int = 0,
    val snr: Float = 0f,
    val batteryNodePct: Int = 100,
    val channelUtilization: Float = 0f,
    val lastSyncGsie: Long? = null,
    val pendingSyncGsie: Boolean = false
)
```

#### 17.2.7 Synchronisation automatique des martelages

Chaque martelage est **automatiquement diffusé** aux autres opérateurs via le mesh LoRa. Aucune action manuelle n'est requise — l'opérateur martèle normalement dans GeoSylva, et le mesh engine s'occupe du reste.

```
FLUX AUTOMATIQUE :

  1. Opérateur A martèle un arbre dans GeoSylva
     → Enregistrement en base locale
     → GeoSylvaMeshEngine.broadcastTreeMeasurement() appelé automatiquement
     → Message binaire (47 bytes) → BLE → nœud LoRa → diffusion 868MHz

  2. Nœuds LoRa des autres opérateurs reçoivent le message
     → Relay au téléphone par BLE
     → GeoSylva décode et stocke en base locale
     → L'arbre apparaît sur la carte de B en temps réel
     → Si B s'approche du même arbre → alerte double-marquage

  3. Au retour au bureau (WiFi/4G détecté)
     → GeoSylva détecte la connexion réseau
     → syncToGsieServer() appelé automatiquement
     → Upload complet de la parcelle vers le serveur GSIE
     → Les moteurs GSIE analysent (volume, carbone, croissance)
     → Résultats retournés à GeoSylva
```

```kotlin
/**
 * Intercepte chaque martelage et le diffuse sur le mesh.
 */
class MartelageMeshObserver(private val meshEngine: GeoSylvaMeshEngine) {
    suspend fun onTreeMarked(tree: TreeMeasurement) {
        meshEngine.broadcastTreeMeasurement(tree)
    }
}

/**
 * Détecte le retour au bureau (WiFi/4G) et déclenche la sync GSIE.
 */
class GsieSyncTrigger(
    private val meshEngine: GeoSylvaMeshEngine,
    private val localDatabase: InventoryDatabase
) {
    fun startMonitoring(context: Context, scope: CoroutineScope) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        cm.networkCallback(onAvailable = { _ ->
            scope.launch {
                for (parcelle in localDatabase.getUnsyncedParcelles()) {
                    meshEngine.syncToGsieServer(parcelle.id)
                }
            }
        })
    }
}
```

#### 17.2.8 Route vers le serveur GSIE — analyse des moteurs PC

Au retour au bureau, GeoSylva pousse les données d'inventaire vers le **serveur GSIE** (FastAPI + PostgreSQL/PostGIS). Les moteurs GSIE traitent les données et retournent les résultats d'analyse.

```kotlin
class GsieApiClient(private val baseUrl: String) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun syncInventory(payload: GsieSyncPayload): GsieSyncResult {
        return httpClient.post("$baseUrl/api/v1/sync/inventory") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }
}

@Serializable
data class GsieSyncPayload(
    val parcelleId: Long,
    val syncTimestamp: Long,
    val operatorId: Int,
    val trees: List<GsieTreeDto>,
    val photos: List<GsiePhotoDto>,
    val meshMetadata: GsieMeshMetadata
)

@Serializable
data class GsieTreeDto(
    val treeId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val essenceCode: String,
    val diameterMm: Int,
    val heightDm: Int,
    val qualityGrade: Int,
    val defectsMask: Int,
    val volumeCm3: Long,
    val carbonKg: Double,
    val action: String,
    val operatorId: Int,
    val timestamp: Long,
    val confidencePercent: Int,
    val photoHashes: List<String>
)

@Serializable
data class GsieSyncResult(
    val syncId: String,
    val status: String,
    val treesReceived: Int,
    val photosReceived: Int,
    val analysisResults: GsieAnalysisResults?
)

@Serializable
data class GsieAnalysisResults(
    val parcelleVolumeM3: Double,
    val parcelleBiomassKg: Double,
    val parcelleCarbonKg: Double,
    val speciesDistribution: Map<String, Double>,
    val qualityDistribution: Map<Int, Int>,
    val growthProjection: GrowthProjectionDto?,
    val carbonReport: CarbonReportDto?,
    val anomalies: List<GsieAnomaly>
)
```

#### 17.2.9 API GSIE côté serveur — endpoint de sync

```python
# gsie_api/api/v1/sync.py

from fastapi import APIRouter, BackgroundTasks, Depends
from gsie_api.domain.sync import SyncService, SyncPayload, SyncResult
from gsie_api.infrastructure.database import get_session

router = APIRouter(prefix="/api/v1/sync", tags=["sync"])

@router.post("/inventory", response_model=SyncResult)
async def sync_inventory(
    payload: SyncPayload,
    background_tasks: BackgroundTasks,
    session = Depends(get_session)
):
    """
    Endpoint de synchronisation GeoSylva → GSIE.
    Reçoit les données d'inventaire, les stocke en PostgreSQL/PostGIS,
    et lance l'analyse des moteurs GSIE en arrière-plan.
    """
    sync_service = SyncService(session)
    sync_id = await sync_service.ingest_inventory(payload)

    background_tasks.add_task(
        sync_service.run_analysis,
        sync_id=sync_id,
        parcelle_id=payload.parcelle_id
    )

    return SyncResult(
        sync_id=sync_id,
        status="SUCCESS",
        trees_received=len(payload.trees),
        photos_received=len(payload.photos),
        analysis_results=None
    )

@router.get("/{sync_id}/results", response_model=AnalysisResults)
async def get_analysis_results(sync_id: str, session = Depends(get_session)):
    """Récupère les résultats d'analyse des moteurs GSIE."""
    return await SyncService(session).get_results(sync_id)
```

#### 17.2.10 Synchronisation multi-niveaux

```
NIVEAU 1 — Temps réel (LoRa mesh, < 5s)
  · Position GPS des opérateurs (beacon 60s)
  · Mesures d'arbres (envoi immédiat)
  · Alertes double-marquage (immédiat)
  · Chat texte (immédiat)

NIVEAU 2 — Différé (LoRa mesh, minutes)
  · Delta sync inventaire (à la demande)
  · Mises à jour de marquage (batch)

NIVEAU 3 — Gateway LoRa (si nœud gateway présent, heures)
  · Sync vers serveur GSIE via bridge LoRa→4G/Starlink
  · Backup des données
  · Mises à jour de l'application

NIVEAU 4 — Retour au bureau (WiFi/4G, automatique)
  · syncToGsieServer() — upload complet parcelle vers serveur GSIE
  · Upload photos (trop lourdes pour LoRa)
  · Moteurs GSIE : ForestDynamics, Carbon, Volume, Growth
  · Download modèles IA mis à jour + résultats d'analyse
```

#### 17.2.11 Gestion de la congestion LoRa

Le canal LoRa a une **capacité limitée**. En cas de forte activité (équipe de 10+ opérateurs), des stratégies de décongestion sont nécessaires :

```
STRATÉGIES :

1. Priorisation des messages :
   CRITIQUE  : alerte double-marquage, urgence sécurité
   HAUTE     : mesure d'arbre (nouveau)
   NORMALE   : delta sync, mise à jour marquage
   BASSE     : chat, beacon position

2. Agrégation temporelle :
   · Les beacons position sont envoyés toutes les 60s (pas en continu)
   · Les mises à jour de marquage sont batchées (toutes les 30s)
   · Le chat est envoyé en différé si canal saturé

3. Ajustement dynamique du débit LoRa :
   · Si channelUtilization > 70% :
     → Passage en SF12 (Spreading Factor 12) pour portée max, débit min
     → Réduction de la fréquence des beacons à 120s
   · Si channelUtilization < 30% :
     → Passage en SF7 pour débit max, portée plus courte
     → Fréquence des beacons à 30s

4. Compression adaptative :
   · Si canal saturé : envoyer seulement les champs critiques
     (tree_id, GPS, essence, D, action) = 25 bytes au lieu de 47
   · Les champs étendus (volume, carbone, défauts) sont envoyés
     en différé quand le canal est libre
```

#### 17.2.12 Détection de double-marquage sur mesh

```
SCÉNARIO TEMPS RÉEL :

  T=0s  : Opérateur A marque un chêne à 48.12345°N, 4.56789°E
          → Envoi MSG_TREE_MEASUREMENT sur canal LoRa
          → Tous les nœuds du mesh reçoivent le message en < 3s

  T=2s  : Opérateur B (à 20m) scanne le même chêne
          → Avant envoi, vérification locale :
            "Un arbre existe déjà à 1.5m de cette position"
            (reçu via le mesh il y a 2s)
          → Alerte UI : "⚠️ Arbre potentiellement déjà marqué par A"
          → L'opérateur B voit le marqueur de A sur sa carte

  T=3s  : Si l'opérateur B confirme quand même :
          → Envoi MSG_DOUBLE_MARK_ALERT sur le mesh
          → L'opérateur A reçoit l'alerte sur son téléphone
          → Le chef d'équipe reçoit aussi l'alerte

DÉTECTION LOCALE (sans attendre le mesh) :
  Chaque opérateur garde un cache local des arbres reçus via LoRa.
  Avant chaque nouvelle mesure, vérification du cache local
  → réponse immédiate (< 100ms), pas besoin d'attendre le réseau.
```

#### 17.2.13 Topologies de déploiement

```
TOPOLOGIE 1 — Petite équipe (2-4 opérateurs, < 50 ha)
  · Chaque opérateur a un nœud T-Echo
  · Mesh direct entre opérateurs (pas de relais)
  · Portée : 1-3 km en forêt
  · Sync : temps réel entre opérateurs
  · Gateway : non nécessaire

TOPOLOGIE 2 — Équipe moyenne (5-10 opérateurs, 50-200 ha)
  · Chaque opérateur a un nœud T-Echo
  · 1-2 nœuds relais T-Beam en position élevée
    (haut d'arbre, butte, mirador)
  · Portée : 3-8 km avec relais
  · Gateway : 1 nœud T-Beam + Raspberry Pi (optionnel)

TOPOLOGIE 3 — Grande équipe (10+ opérateurs, > 200 ha)
  · Chaque opérateur a un nœud T-Echo
  · 3-5 nœuds relais T-Beam répartis sur le massif
  · 1 gateway fixe (parking, local) avec 4G/Starlink
  · Portée : 5-15 km avec mesh multi-saut
  · Sync : temps réel local + différé vers cloud via gateway

TOPOLOGIE 4 — Solo (1 opérateur, zone isolée)
  · 1 nœud T-Echo (GPS + stockage local)
  · Pas de mesh (pas d'autres opérateurs)
  → Le nœud sert de GPS de secours et logger de données
  → Sync au retour au bureau (BLE → téléphone → cloud)
```

#### 17.2.14 Sécurité et chiffrement

```
SÉCURITÉ GEOSYLVA MESH NATIF :

1. Chiffrement canal (native GeoSylva) :
   · AES-128-CTR par canal, clé dérivée par canal
   · Clé d'équipe générée par le chef d'équipe, partagée par QR code
   · Clé rotative possible (régénération + redistribution QR)

2. Authentification opérateur :
   · Chaque opérateur a un ID unique (uint32)
   · L'ID est vérifié contre la liste d'équipe au niveau applicatif
   · Un opérateur non autorisé ne peut pas envoyer sur le canal chiffré

3. Intégrité des données :
   · Checksum XOR sur chaque message (détection corruption LoRa)
   · Hash SHA-256 sur les sync complètes (détection perte de paquets)
   · Numérotation séquentielle des messages (détection doublons/pertes)

4. Confidentialité :
   · Positions GPS opérateurs sur canal chiffré GEOSYLVA_PRESENCE
   · Données d'inventaire sur GEOSYLVA_DATA (chiffré)
   · Aucune donnée en clair sur le canal radio

5. Route vers GSIE :
   · Sync bureau sur HTTPS (TLS 1.3)
   · Authentification JWT (token opérateur)
   · Hash SHA-256 des photos pour intégrité
   · Données stockées chiffrées au repos sur PostgreSQL
```

#### 17.2.15 Pages UI dédiées dans GeoSylva

GeoSylva intègre **3 pages natives** pour la gestion du mesh, accessibles depuis le menu principal :

```
PAGE 1 — GESTION DES NŒUDS (Paramètres > Mesh LoRa)

┌─────────────────────────────────────────────────────┐
│  ◀  Gestion du mesh LoRa                             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  NŒUD CONNECTÉ                                │  │
│  │                                                │  │
│  │  📡 LilyGO T-Echo #42                          │  │
│  │  BLE : connecté ✓                              │  │
│  │  Batterie nœud : 87%                           │  │
│  │  Signal : -87 dBm  SNR : 7.2 dB               │  │
│  │  Canal : 868.1 MHz  SF9  BW250                 │  │
│  │  Occupation : 23%                              │  │
│  │  GPS nœud : 48.1234°N 4.5678°E                │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  NŒUDS APPAIRÉS                               │  │
│  │                                                │  │
│  │  ✓ T-Echo #42 (moi)      87% bat  connecté    │  │
│  │  ✓ T-Echo #17 (Bob)      78% bat  en ligne    │  │
│  │  ✓ T-Echo #55 (Claire)   45% bat  en ligne    │  │
│  │  ✗ T-Echo #09 (David)    --       hors portée │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  CLÉ D'ÉQUIPE                                 │  │
│  │                                                │  │
│  │  QR code : [████████████]                      │  │
│  │  Scanner QR pour rejoindre l'équipe            │  │
│  │  [Générer nouvelle clé]  [Scanner]             │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  [Scanner nœuds]  [Configurer radio]  [Test mesh]  │
└─────────────────────────────────────────────────────┘


PAGE 2 — CHAT ÉQUIPE (Onglet dédié dans GeoSylva)

┌─────────────────────────────────────────────────────┐
│  ◀  Chat équipe — Mesh LoRa                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  Alice (10:42)                                 │  │
│  │  > Zone nord-est terminée, je passe au sud     │  │
│  ├───────────────────────────────────────────────┤  │
│  │  Bob (10:44)                                   │  │
│  │  > OK, je finis la bande centrale              │  │
│  ├───────────────────────────────────────────────┤  │
│  │  Chef (10:45)                                  │  │
│  │  > Rappelez les D > 60cm pour vérification     │  │
│  ├───────────────────────────────────────────────┤  │
│  │  Alice (10:47)                                 │  │
│  │  > Vu, 3 chênes > 60cm dans ma zone            │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  [Écrire un message...]                    [➤] │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  📶 Mesh actif — 3 opérateurs en ligne              │
└─────────────────────────────────────────────────────┘


PAGE 3 — CARTE TEMPS RÉEL + SYNC GSIE

┌─────────────────────────────────────────────────────┐
│  ◀  Carte équipe — Mesh LoRa                         │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │                                                │  │
│  │   🌲🌲🌲🌳🌲🌲🌳🌲🌲🌲                          │  │
│  │   🌲🌲🌳🌲🌲🌲🌲🌲🌳🌲                          │  │
│  │        🟢A          🟢B                        │  │
│  │   🌲🌲🌲🌲🌲🌲🌲🌲🌲🌲                          │  │
│  │              🟡C                               │  │
│  │   🌳🌲🌲🌲🌲🌲🌲🌲🌲🌳                          │  │
│  │                                                │  │
│  │   🟢 = Alice    🟢 = Bob    🟡 = Claire        │  │
│  │   🔴 = arbre martelé (récolter)                │  │
│  │   🔵 = arbre martelé (garder)                  │  │
│  │   ⚠️ = double-marquage détecté                 │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Arbres martelés : 142/150                          │
│  · Alice : 52   Bob : 48   Claire : 42              │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  SYNC SERVEUR GSIE                             │  │
│  │                                                │  │
│  │  Statut : ⏳ En attente (pas de réseau)        │  │
│  │  Données : 142 arbres, 380 photos              │  │
│  │  Dernière sync : jamais                         │  │
│  │                                                │  │
│  │  [Sync manuelle]  (WiFi/4G requis)             │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

Ces 3 pages sont entièrement gérées par GeoSylva — **aucune app externe nécessaire**. L'opérateur reste dans GeoSylva du début à la fin :
- **Martelage** → diffusion mesh automatique
- **Chat** → page dédiée dans l'app
- **Carte équipe** → positions temps réel des autres opérateurs
- **Sync GSIE** → automatique au retour au bureau, ou manuelle via bouton

### 17.3 Optimisation économique

#### 17.3.1 Scheduler de récolte

```kotlin
data class HarvestSchedule(
    val trees: List<HarvestTree>,
    val totalVolumeM3: Double,
    val totalValueEur: Double,
    val totalCarbonTons: Double,
    val rotationLengthYears: Int,
    val npvEur: Double,
    val irr: Double,                      // taux de rendement interne
    val paybackPeriodYears: Int
)

data class HarvestTree(
    val treeId: String,
    val essenceCode: String,
    val diameterCm: Double,
    val volumeM3: Double,
    val qualityGrade: Int,
    val valueEur: Double,
    val harvestYear: Int,
    val reason: String                     // "Mature", "Sanitaire", "Concurrent", "..."
)

class EconomicOptimizer(
    private val growthProjector: GrowthProjector,
    private val pricingEngine: PricingEngine
) {

    fun optimizeHarvestSchedule(
        trees: List<TreeData>,
        stand: StandContext,
        horizonYears: Int = 30,
        constraints: EconomicConstraints
    ): HarvestSchedule {

        // 1. Projection de chaque arbre sur l'horizon
        val projections = trees.map { tree ->
            tree to growthProjector.project(tree, stand, horizonYears)
        }

        // 2. Pour chaque arbre, calculer le NPV par année de récolte
        val npvByTree = projections.map { (tree, proj) ->
            val npvByYear = proj.valueEur.mapIndexed { i, value ->
                val year = proj.years[i]
                year to value / (1.0 + constraints.discountRate).pow(year)
            }
            tree to npvByYear
        }

        // 3. Optimisation sous contraintes
        // Contraintes :
        //   - Volume max par année (capacité d'exploitation)
        //   - G minimale après coupe (stabilité du peuplement)
        //   - Pas de coupe rase > X ha (réglementation)
        //   - Budget minimal par année (trésorerie)

        val selected = selectOptimalHarvest(
            npvByTree, constraints, stand
        )

        // 4. Calcul des métriques globales
        val totalVolume = selected.sumOf { it.volumeM3 }
        val totalValue = selected.sumOf { it.valueEur }
        val totalCarbon = selected.sumOf { it.carbonKg } / 1000.0
        val npv = selected.sumOf { it.npvEur }

        return HarvestSchedule(
            trees = selected,
            totalVolumeM3 = totalVolume,
            totalValueEur = totalValue,
            totalCarbonTons = totalCarbon,
            rotationLengthYears = horizonYears,
            npvEur = npv,
            irr = computeIRR(selected),
            paybackPeriodYears = computePayback(selected)
        )
    }
}
```

#### 17.3.2 Simulation de scénarios économiques

```
INTERFACE — Simulateur économique :

┌─────────────────────────────────────────────────────┐
│  Simulateur de gestion forestière                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Horizon : [30 ans ▾]                               │
│  Taux d'actualisation : [3.0% ▾]                    │
│  Contraintes :                                      │
│  ☑ G minimale après coupe : 18 m²/ha                │
│  ☑ Pas de coupe rase > 0.5 ha                       │
│  ☐ Budget minimal annuel : ___ €                    │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  SCÉNARIO A — Sylviculture intensive           │  │
│  │  NPV : 18,500 €/ha    IRR : 4.2%               │  │
│  │  Récolte : 8 arbres/ha/an (moyenne)            │  │
│  │  Carbone : 145 t/ha stocké à 30 ans            │  │
│  │  [Voir détail]  [Comparer]                     │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  SCÉNARIO B — Sylviculture proche-nature       │  │
│  │  NPV : 12,000 €/ha    IRR : 3.1%               │  │
│  │  Récolte : 4 arbres/ha/an (moyenne)            │  │
│  │  Carbone : 180 t/ha stocké à 30 ans            │  │
│  │  [Voir détail]  [Comparer]                     │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  SCÉNARIO C — Conservation carbone             │  │
│  │  NPV : 6,500 €/ha     IRR : 1.8%               │  │
│  │  Récolte : 1 arbre/ha/an (sanitaire uniquement)│  │
│  │  Carbone : 220 t/ha stocké à 30 ans            │  │
│  │  Crédits carbone : +3,200 €/ha (LBC)           │  │
│  │  [Voir détail]  [Comparer]                     │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Recommandation IA : Scénario B (meilleur équilibre │
│  économique / écologique / risque)                   │
└─────────────────────────────────────────────────────┘
```

### 17.4 Roadmap consolidée — sections 15-17

| Phase | Durée | Contenu | Section |
|---|---|---|---|
| **IQ-1** | 3-4 semaines | BarkNet (classification essence par écorce) | 15.2 |
| **IQ-2** | 2-3 semaines | LeafNet + fusion + filtrage géographique | 15.2 |
| **IQ-3** | 4-6 semaines | Modèle défauts + grading ONF | 15.3 |
| **IQ-4** | 3-4 semaines | Règles sylvicoles encodées + decision engine | 16.2-16.3 |
| **IQ-5** | 2-3 semaines | Intégration LLM T4+ pour raisonnement martelage | 16.3.2 |
| **IQ-6** | 4-6 semaines | Projection de croissance + visualisation 3D | 16.4 |
| **IQ-7** | 3-4 semaines | Simulation scénarios sylvicoles | 16.4.3 |
| **IQ-8** | 3-4 semaines | CarbonEngine (équations spécifiques par essence) | 17.1.3 |
| **IQ-9** | 2-3 semaines | Rapport MRV automatique (LBC, VCS) | 17.1.4 |
| **IQ-10** | 2-3 semaines | Intégration Meshtastic BLE + protocole binaire LoRa | 17.2.5-17.2.6 |
| **IQ-11** | 3-4 semaines | Sync collaboratif mesh + dédoublonnage + congestion | 17.2.7-17.2.9 |
| **IQ-12** | 2-3 semaines | UI statut mesh + carte temps réel + chat opérateurs | 17.2.12 |
| **IQ-13** | 4-6 semaines | Optimiseur économique + simulateur scénarios | 17.3 |

### 17.5 Sources supplémentaires

- Wirth, C. et al. (2009). *Wood density and carbon fractions of temperate tree species*. DBFZ Leipzig.
- Vande Walle, I. et al. (2005). *Biomass expansion factors for temperate broadleaved forests*. Forest Ecology and Management, 213, 471-480.
- Cairns, M.A. et al. (1997). *Root biomass allocation in the world's upland forests*. Oecologia, 111, 1-11.
- Label Bas Carbone (2024). *Méthode forêts — version 3*. Ministère de la Transition Écologique.
- Verra (2025). *VM0037 — Methodology for Improved Forest Management*. verra.org.
- ONF (2023). *Guide de martelage — futaies feuillues françaises*. ONF Technical Guides.
- CRPF (2024). *Recommandations sylvicoles par région forestière*. crpf.fr.
- Dupouey, J.L. et al. (2010). *Équations de biomasse pour les principales essences forestières françaises*. INRAE.
- Meshtastic (2025). *Open-source mesh networking with LoRa*. https://meshtastic.org
- Meshtastic (2025). *Android API — service and BLE connection*. https://github.com/meshtastic/Meshtastic-Android
- Semtech (2024). *SX1262 — LoRa transceiver datasheet*. semtech.com
- LilyGO (2025). *T-Echo nRF52840 + SX1262 — product documentation*. lilygo.cn
- ARCEM (2024). *Réglementation LoRa 868MHz en Europe — ETSI EN 300 220*. arcep.fr

---

> **Note** : Ce document est un document de conception technique. Les implémentations concrètes suivront en Phase 4 (implémentation). Les modèles IA pré-entraînés seront développés et validés sur données françaises (FOR-CE, IGN) avant déploiement OTA.
