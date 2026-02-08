# GéoSylva - Multi-Compteur Forestier

Une application Android professionnelle pour le comptage forestier avec support multi-compteurs, formules calculées, et import/export universel.

## 🌲 Fonctionnalités Principales

### Core
- ✅ Multi-compteurs avec groupes organisés
- ✅ Incrémentation/décrémentation avec haptique
- ✅ Valeurs cibles et suivi de progression
- ✅ Duplication rapide de compteurs
- ✅ Formules calculées avec moteur avancé
- ✅ Import/Export universel (CSV, XLSX, JSON, SQLite, ZIP)

### Interface
- ✅ Material 3 Design avec thème adaptatif
- ✅ Mode clair/sombre/système
- ✅ Couleurs d'accent personnalisables
- ✅ Tailles de police ajustables (accessibilité)
- ✅ Animations douces et micro-interactions
- ✅ Feedback haptique sur actions
- ✅ Contraste automatique du texte

### Calculs
- Opérateurs: `+`, `-`, `*`, `/`, `%`, `^`, `()`
- Fonctions: `sum()`, `avg()`, `min()`, `max()`, `count()`
- Filtres: `name:startsWith()`, `name:contains()`, `tag:`
- Conditions: `if(condition, true, false)`
- Variables personnalisées par groupe (ex: `PLOT_AREA`)
- Constantes: `PI`, `E`

### Import/Export
- **CSV**: auto-détection séparateur/encodage
- **Excel (.xlsx)**: lecture/écriture multi-feuilles
- **JSON**: format structuré avec métadonnées
- **SQLite**: import de bases externes
- **ZIP**: export groupé avec manifest

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Data Access Objects
│   │   └── ForestryDatabase # Room database
│   ├── preferences/         # DataStore preferences
│   ├── repository/          # Repository implementations
│   └── mapper/              # Entity ↔ Domain mappers
├── domain/
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   ├── calculator/          # Formula parser engine
│   └── usecase/             # Use cases (import/export)
└── presentation/
    ├── screens/             # UI screens (Compose)
    ├── navigation/          # Navigation graph
    └── theme/               # Material 3 theming
```

**Principes:**
- Clean Architecture (domain/data/presentation)
- MVVM pattern avec ViewModels
- Reactive (Kotlin Flow)
- Room pour persistence
- DataStore pour préférences

## 🚀 Technologies

- **Kotlin** + Coroutines
- **Jetpack Compose** - UI moderne
- **Material 3** - Design system
- **Room** - Base de données locale
- **DataStore** - Préférences
- **Navigation Compose** - Navigation
- **OpenCSV** - Parsing CSV
- **Apache POI** - Excel (XLSX)
- **kotlinx.serialization** - JSON
- **exp4j** - Évaluation d'expressions mathématiques

## 📋 Prérequis

- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 17
- Android SDK API 24+ (Android 7.0+)
- Gradle 8.2+

## 🛠️ Installation

1. Cloner le repository
```bash
git clone <repository-url>
cd "multi counter forestier"
```

2. Ouvrir dans Android Studio
```bash
# Ouvrir le projet dans Android Studio
# File → Open → Sélectionner le dossier du projet
```

3. Synchroniser Gradle
```bash
# Android Studio fera automatiquement la sync
# Ou: Tools → Android → Sync Project with Gradle Files
```

4. Lancer l'application
```bash
# Connecter un appareil Android ou démarrer un émulateur
# Run → Run 'app'
```

## 📦 Build Release

```bash
# Via ligne de commande
cd app
./gradlew assembleRelease

# APK généré dans:
# app/build/outputs/apk/release/app-release.apk
```

Pour un build signé (Play Store):
```bash
./gradlew bundleRelease
# AAB généré dans: app/build/outputs/bundle/release/
```

## 🔒 Sécurité & Confidentialité

- ✅ **Aucune publicité**
- ✅ **Aucun tracking / analytics**
- ✅ **Aucune permission réseau obligatoire**
- ✅ **Données 100% locales**
- ✅ **Code source auditable**
- ✅ **ProGuard/R8 activé** en release

## 📚 Utilisation

### Créer un Groupe
1. Écran d'accueil → Bouton `+`
2. Entrer le nom du groupe
3. Optionnel: choisir une couleur

### Ajouter des Compteurs
1. Ouvrir un groupe
2. Bouton `+` → Remplir les champs:
   - Nom
   - Pas (incrément)
   - Min/Max (limites)
   - Cible (objectif)

### Compter
- **Tap**: +1 (ou pas défini)
- **Long press**: Options (reset, dupliquer, supprimer)

### Formules
Exemples:
```javascript
// Total de tous les hêtres
sum(name:startsWith("Hêtre"))

// Densité par hectare
sum(*) * (10000 / PLOT_AREA)

// Condition
if(sum(tag:"Résineux") > 50, 1, 0)

// Moyenne
avg(name:contains("Chêne"))
```

### Import/Export
- Menu groupe → Import/Export
- Sélectionner format (CSV, XLSX, JSON, ZIP)
- Choisir mode (Remplacer/Fusionner/Ajouter)
- Mapper les colonnes si nécessaire

## 🧪 Tests

```bash
# Tests unitaires
./gradlew test

# Tests instrumentés
./gradlew connectedAndroidTest
```

## 🎨 Personnalisation

### Thème
Settings → Appearance → Theme (Clair/Sombre/Système)

### Accent
Settings → Appearance → Accent Color (Vert/Bleu/Orange...)

### Police
Settings → Appearance → Font Size (S/M/L)

## 🗺️ Roadmap

### Phase 1 ✅ (Actuel)
- [x] Core CRUD (groupes, compteurs)
- [x] UI Material 3
- [x] Thème personnalisable
- [x] Formules basiques
- [x] Import/Export (CSV, JSON, XLSX)

### Phase 2 (Prochaine)
- [ ] Templates forestiers (essence×classe)
- [ ] Mode terrain optimisé (gros boutons)
- [ ] Sauvegardes automatiques planifiées
- [ ] Synchronisation multi-appareils (optionnelle)
- [ ] Graphiques et statistiques
- [ ] Export PDF avec rapports

### Phase 3 (Future)
- [ ] Mode hors-ligne avancé
- [ ] Géolocalisation des comptages
- [ ] Photos par compteur
- [ ] Collaboration équipe
- [ ] API REST (optionnelle)

## 📄 Licence

Ce projet est **dual-licensed** (double licence).

### Licence Open Source
- **GNU Affero General Public License v3.0 (AGPL-3.0)**
- Libre pour usage personnel, éducatif et projets open-source compatibles.
- L'usage commercial est autorisé sous AGPL-3.0, à condition de divulguer le code source complet.

### Licence Commerciale
- Requise si vous souhaitez utiliser GeoSylva **sans respecter les obligations AGPL-3.0** (divulgation du code source).
- Concerne : intégration propriétaire, SaaS, services hébergés, usage interne sans divulgation.
- Contactez l'auteur pour les conditions de licence commerciale.

Voir [COMMERCIAL_LICENSE.md](COMMERCIAL_LICENSE.md) pour tous les détails.

## 👥 Contribution

Les contributions sont bienvenues! Merci de:
1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 🐛 Bugs & Support

Ouvrir une issue sur GitHub avec:
- Description du problème
- Étapes pour reproduire
- Version Android
- Screenshots si applicable

## 📧 Contact

Pour questions professionnelles: [Ajouter email/contact]

---

**Made with 🌲 for forestry professionals**
