<div align="center">

# 🌲 GeoSylva

### Application Android professionnelle d'inventaire forestier et de martelage

[![Version](https://img.shields.io/badge/version-1.3.0-green?style=for-the-badge)](CHANGELOG.md)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-AGPL--3.0-blue?style=for-the-badge)](LICENSE)

**Conçue pour les forestiers, géomètres et gestionnaires de forêts.**
Fonctionne intégralement hors-ligne — idéal sur le terrain.

---

[Fonctionnalités](#-fonctionnalités) · [Captures d'écran](#-captures-décran) · [Installation](#-installation) · [Documentation](#-documentation) · [Licence](#-licence)

</div>

---

## 🎯 Pourquoi GeoSylva ?

GeoSylva remplace le carnet de terrain et les tableurs Excel par une application unique qui couvre **l'ensemble du workflow forestier** : de la saisie des tiges sur le terrain jusqu'au rapport PDF de synthèse dendrométrique, en passant par la cartographie, le calcul de volume et la simulation de martelage.

| Problème terrain | Solution GeoSylva |
|---|---|
| Saisie papier lente et sujette aux erreurs | Comptage par classe avec boutons +/−, GPS automatique |
| Calculs manuels fastidieux | 6 méthodes de cubage intégrées, calcul temps réel |
| Pas de visualisation sur place | Carte interactive avec 12 couches (IGN, satellite, cadastre…) |
| Export compliqué vers SIG | Export Shapefile, GeoJSON, CSV-XY en un clic |
| Pas de réseau en forêt | 100% hors-ligne, tuiles cartographiques téléchargeables |

---

## ✨ Fonctionnalités

### 📐 Inventaire & Dendrométrie

- **Saisie rapide** — comptage par essence et classe de diamètre avec boutons +/−
- **95+ essences** pré-configurées avec données forestières détaillées (densité, qualité, croissance, usage bois, tolérance ombre, dimensions max)
- **7 méthodes de cubage** : Schaeffer 1E/2E, Algan, IFN Rapide/Lent, FGH, Coefficient de forme
- **Classification produit automatique** — bois d'œuvre (BO), bois d'industrie (BI), bois de chauffage (BCh), déroulage, traverse, charpente…
- **Notation qualité bois** A/B/C/D avec défauts visuels

### 📍 GPS de précision

- **Capture immédiate au tap** — GPS déclenché instantanément lors de l'ajout d'une tige
- **Profil optimal unique** — 6 lectures (max 20m, timeout 15s) équilibre rapidité + précision
- **Réutilisation intelligente** — si une tige est supprimée puis re-ajoutée (même classe + essence), le dernier point GPS est réutilisé
- **Persistance hors-page** — la capture GPS continue en arrière-plan même si vous quittez l'écran
- **Visualisation de la précision** — cercles colorés sur la carte : 🟢 ≤3m (excellent) 🟡 ≤6m (bon) 🟠 ≤12m (modéré) 🔴 >12m (mauvais)
- **Moyennage multi-lectures** avec rejet d'outliers (MAD-based)
- **Monitoring périodique** de la qualité du signal GPS

### 🗺️ Cartographie interactive

- **12 couches cartographiques** : OSM, IGN, satellite, cadastre, forêts, topographique…
- **Affichage des tiges** sur la carte avec clustering et code couleur par essence
- **Tuiles hors-ligne** — téléchargez la zone de travail pour utilisation sans réseau
- **Import de shapefiles** pour superposer vos couches parcellaires
- **Filtre de fiabilité GPS** — n'affiche que les points sous un seuil de précision configurable

### 📊 Synthèse & Martelage

- **Tableau de bord visuel** — graphiques donut (répartition essences), barres (classes de diamètre), surface terrière par essence
- **Synthèse dendrométrique complète** — N/ha, G/ha, V/ha, hauteur dominante, diamètre moyen
- **Volume partiel intelligent** — affiche les résultats disponibles avec % de complétude au lieu de bloquer
- **Simulation de coupe** — taux de prélèvement N/ha et G/ha, peuplement résiduel
- **Garde-fous automatiques** — vérification de cohérence des données (30+ contrôles)
- **Tables de prix** éditables par essence, produit et classe de diamètre
- **Qualité bois A/B/C/D** avec multiplicateurs automatiques (A=×2.5, B=×1.5, C=×1.0, D=×0.4)
- **Ventilation par produit** — décomposition du volume par essence (BO/BI/BCh/PATE) avec valorisation détaillée

### 📤 Exports professionnels

- **PDF** — rapport A4 avec tableaux dendrométriques, valorisation par essence
- **Shapefile** (SHP/SHX/DBF/PRJ) — ESRI compatible pour QGIS / ArcGIS
- **GeoJSON** — avec coordonnées Lambert 93 pour intégration SIG
- **CSV / CSV-XY** — export tabulaire avec coordonnées géographiques
- **Excel (XLSX)** — multi-feuilles avec métadonnées

### 🛡️ Fiabilité terrain

- **100% hors-ligne** — aucune connexion requise pour toutes les fonctionnalités
- **Sauvegarde automatique** quotidienne via WorkManager
- **Rappel hauteurs avec snooze** — reportez les alertes de hauteurs manquantes (1h, 4h, 24h)
- **Tips contextuels** — aide intégrée sur chaque écran
- **Onboarding complet** — 7 écrans d'introduction interactifs

---

## 📸 Captures d'écran

> *À venir — captures des écrans principaux*

<!--
<div align="center">
<img src="docs/screenshots/dashboard.png" width="200" />
<img src="docs/screenshots/inventory.png" width="200" />
<img src="docs/screenshots/map.png" width="200" />
<img src="docs/screenshots/synthesis.png" width="200" />
</div>
-->

---

## 🏗️ Architecture

```
app/src/main/java/com/forestry/counter/
├── data/
│   ├── local/
│   │   ├── entity/              # Room entities (11 tables)
│   │   ├── dao/                 # Data Access Objects
│   │   ├── CanonicalEssences.kt # 95+ espèces pré-configurées
│   │   ├── DatabaseMigrations.kt# Migrations v1→v11
│   │   └── ForestryDatabase.kt  # Room database
│   ├── preferences/             # DataStore (GPS, affichage, tarifs…)
│   ├── repository/              # Implémentations Repository
│   ├── mapper/                  # Entity ↔ Domain mappers
│   └── work/                    # WorkManager (sauvegardes)
├── domain/
│   ├── model/                   # Modèles métier (Tige, Essence, Parcelle…)
│   ├── repository/              # Interfaces Repository
│   ├── calculation/
│   │   ├── ForestryCalculator.kt# Moteur dendrométrique principal
│   │   ├── SanityChecker.kt     # Garde-fous & cohérence
│   │   ├── tarifs/              # 6 méthodes de cubage
│   │   └── quality/             # Qualité bois & classification produit
│   ├── location/
│   │   ├── GpsAverager.kt       # Moyennage GPS + rejet outliers
│   │   └── OfflineTileManager.kt# Gestion tuiles hors-ligne
│   ├── geo/                     # Lambert 93, Shapefile parser
│   └── usecase/export/          # ShapefileExporter, ExportDataUseCase
└── presentation/
    ├── screens/
    │   ├── forestry/            # Inventaire, carte, martelage, dashboard
    │   ├── settings/            # Paramètres, éditeur de prix
    │   └── onboarding/          # Assistant d'accueil
    ├── components/              # Composants réutilisables
    ├── navigation/              # Navigation graph
    └── theme/                   # Material 3 theming
```

**Principes :**
- **Clean Architecture** — séparation stricte domain / data / presentation
- **Reactive** — Kotlin Flow du DAO jusqu'à l'UI Compose
- **Offline-first** — Room + DataStore, aucune dépendance réseau
- **Testable** — 11 fichiers de tests unitaires couvrant calculs, tarifs, export

---

## 🚀 Stack technique

| Catégorie | Technologies |
|---|---|
| **Langage** | Kotlin 1.9 + Coroutines + Flow |
| **UI** | Jetpack Compose + Material 3 |
| **Base de données** | Room (SQLite) — 11 tables, migrations automatiques |
| **Préférences** | DataStore Preferences |
| **Cartographie** | MapLibre GL Native 10.3 |
| **Géolocalisation** | Google Fused Location Provider |
| **Export** | Apache POI (XLSX), OpenCSV, Shapefile (pur Java) |
| **Sérialisation** | kotlinx.serialization |
| **Background** | WorkManager (sauvegardes planifiées) |
| **Build** | Gradle 8.2 + KSP + ProGuard/R8 |

---

## 📋 Prérequis

- Android Studio Ladybug (2024.2) ou supérieur
- JDK 17
- Android SDK API 26+ (Android 8.0 Oreo)
- Gradle 8.2+

## 🛠️ Installation

```bash
# 1. Cloner le repository
git clone https://github.com/NeooeN45/GeoSylva-new.git
cd GeoSylva

# 2. Ouvrir dans Android Studio
#    File → Open → Sélectionner le dossier GeoSylva

# 3. Gradle sync automatique, puis :
#    Run → Run 'app' (appareil ou émulateur)
```

## 📦 Build

```bash
# Debug
./gradlew assembleDebug

# Release (APK signé)
./gradlew assembleRelease
# → app/build/outputs/apk/release/

# Bundle Play Store (AAB)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/
```

## 🧪 Tests

```bash
# Tous les tests unitaires
./gradlew testDebugUnitTest

# Tests spécifiques
./gradlew testDebugUnitTest --tests "*.TarifCalculatorTest"
./gradlew testDebugUnitTest --tests "*.SanityCheckerTest"
./gradlew testDebugUnitTest --tests "*.ForestryCalculatorTest"
```

**Couverture des tests :**
- Calculs de volume (6 méthodes de cubage)
- Classification produit & qualité bois
- Garde-fous de cohérence (SanityChecker)
- Export GeoJSON / CSV-XY / WKT
- Conversion Lambert 93
- Parseur de formules

---

## 🔒 Sécurité & Confidentialité

- ✅ **Aucune publicité** — expérience 100% professionnelle
- ✅ **Aucun tracking / analytics** — aucune donnée collectée
- ✅ **Fonctionne hors-ligne** — aucune connexion requise
- ✅ **Données 100% locales** — stockées uniquement sur l'appareil
- ✅ **ProGuard/R8** — code obfusqué en release
- ✅ **Code source auditable** — open source sous AGPL-3.0

📄 [Politique de confidentialité](PRIVACY_POLICY.md) · 🔐 [Politique de sécurité](SECURITY.md)

---

## 📖 Documentation

| Document | Description |
|---|---|
| [CHANGELOG.md](CHANGELOG.md) | Historique des versions et modifications |
| [QUICK_START.md](QUICK_START.md) | Guide de démarrage rapide |
| [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) | Guide technique d'implémentation |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | Vue d'ensemble du projet |
| [PRIVACY_POLICY.md](PRIVACY_POLICY.md) | Politique de confidentialité |
| [SECURITY.md](SECURITY.md) | Politique de sécurité |
| [COMMERCIAL_LICENSE.md](COMMERCIAL_LICENSE.md) | Conditions de licence commerciale |

---

## 📄 Licence

Ce projet est sous **double licence**.

### Open Source
**GNU Affero General Public License v3.0 (AGPL-3.0)** — libre pour usage personnel, éducatif et projets open-source compatibles. L'usage commercial est autorisé sous AGPL-3.0 à condition de divulguer le code source complet.

### Commerciale
Requise pour une utilisation **sans les obligations AGPL-3.0** (intégration propriétaire, SaaS, services hébergés). Voir [COMMERCIAL_LICENSE.md](COMMERCIAL_LICENSE.md).

---

## 👥 Contribution

Les contributions sont bienvenues !

1. **Fork** le projet
2. Créez votre branche : `git checkout -b feature/ma-fonctionnalite`
3. Committez : `git commit -m 'Ajout de ma fonctionnalité'`
4. Pushez : `git push origin feature/ma-fonctionnalite`
5. Ouvrez une **Pull Request**

Merci de consulter le [SECURITY.md](SECURITY.md) pour le signalement de vulnérabilités.

---

## 🐛 Bugs & Support

Ouvrez une [issue](../../issues) avec :
- Description du problème
- Étapes pour reproduire
- Version Android & modèle d'appareil
- Captures d'écran si applicable

---

<div align="center">

**Made with 🌲 by forestry professionals, for forestry professionals.**

*GeoSylva — L'inventaire forestier, simplifié.*

</div>
