# GéoSylva — AI Context / Documentation de référence (A → Z)

## 0) À lire en premier (pour une nouvelle session IA)
Ce fichier est la **source de vérité** pour comprendre le projet GeoSylva rapidement.

- Si tu es une IA et que tu reprends le projet :
  - Lis d’abord ce fichier.
  - Puis lis `README.md`.
  - Ensuite seulement, explore le code.

Objectifs prioritaires du projet (qualité “app commerciale”) :
- Correctness dendrométrique (calculs irréprochables).
- UI/UX pro (libellés, unités, décimales, erreurs, feedback).
- Internationalisation fiable (FR/EN) : **aucun texte anglais en mode FR**.
- Build stable + tests.

---

## 1) Présentation
GéoSylva est une application Android (Jetpack Compose) destinée au **comptage forestier** et à la **synthèse dendrométrique (martelage)**.

Deux grands blocs fonctionnels :
- **Compteurs** (groupes, compteurs, formules, calculatrice, import/export)
- **Foresterie** (projets/forêts, parcelles, placettes, essences, tiges, martelage, paramètres, prix)

---

## 2) Architecture & stack
- **Langage** : Kotlin (JVM 17)
- **UI** : Jetpack Compose + Material 3
- **Navigation** : Navigation Compose (`ForestryNavigation.kt`)
- **Persistance** : Room
- **Préférences** : DataStore (`UserPreferencesManager`)
- **Imports/Exports** : CSV / XLSX / JSON / ZIP

Organisation (Clean-ish) :
- `data/` : Room entities/dao, impl repositories, DataStore
- `domain/` : models, repository interfaces, usecases, calculs
- `presentation/` : écrans Compose, navigation, thème, utils

---

## 3) Points d’entrée
- Application : `app/src/main/java/com/forestry/counter/ForestryCounterApplication.kt`
- Activity : `app/src/main/java/com/forestry/counter/presentation/MainActivity.kt`
- Navigation : `app/src/main/java/com/forestry/counter/presentation/navigation/ForestryNavigation.kt`

---

## 4) Navigation (routes principales)
Définie dans `ForestryNavigation.kt`.

- Accueil (sélection projet) : `Screen.Forets` → `GroupsScreen`
- Parcelles : `Screen.Parcelles`
- Placettes : `Screen.Placettes`
- Placette détail (essences/tiges) : `Screen.PlacetteDetail`
- Diamètres par essence : `Screen.EssenceDiam`
- Martelage (synthèse) : `Screen.Martelage`
- Carte : `Screen.Map`

Compteurs :
- Détail projet (compteurs) : `Screen.GroupDetail` → `GroupScreen`
- Formules : `Screen.Formulas` → `FormulasScreen`
- Calculatrice : `Screen.Calculator` → `CalculatorScreen`

Paramètres :
- `Screen.Settings` → `SettingsScreen`

---

## 5) Données métier (modèles)
### 5.1 Compteurs
- `Group`
- `Counter`
- `Formula`

### 5.2 Foresterie
- `Parcelle`, `Placette`
- `Essence`
- `Tige` (tige/arbres mesurés)

### 5.3 Paramètres / prix
- Paramètres stockés via repository (JSON/Room selon impl) : barèmes, règles produits, sources
- **Prix** : `PriceEntry` (essence, produit, classe diam, €/m3)
- **Règles produit** : `ProductRule` (mapping essence+diam → produit)

---

## 6) Calculs dendrométriques (Martelage)
Fichier clé :
- `app/src/main/java/com/forestry/counter/domain/calculation/ForestryCalculator.kt`

Points importants :
- Agrégation par essence et classes de diamètre.
- Calculs : N, G (surface terrière), volumes, Dm/Hm, hauteur de Lorey, etc.
- **Coûts/recettes** : calculés via €/m3 appliqué sur volume.
- Robustesse : gestion des **alias de codes essence** + fallback prix (wildcard `*`).

UI Martelage :
- `MartelageScreen.kt` : affichage synthèse, détails, paramètres (surface d’échantillon, Ho, hauteurs), indicateur “prix manquants”.

---

## 7) Prix manquants (UX)
Quand le volume est calculé mais que certaines classes/essences n’ont pas de prix, un bandeau apparaît dans Martelage :
- détection : volume > 0 et `valueSumEur == null`
- message : volume non valorisé + %
- bouton : lien vers **Paramètres** (prix)

---

## 8) Internationalisation (i18n)
- Ressources :
  - `app/src/main/res/values/strings.xml` (EN)
  - `app/src/main/res/values-fr/strings.xml` (FR)

Règle :
- Dans les Composables : `stringResource(R.string.xxx)`
- Hors Composable : `context.getString(R.string.xxx)`

Objectif : **aucun texte anglais en mode FR**, y compris sur les écrans “foresterie”.

---

## 9) Animations / UX pro
- Transitions globales de navigation (fade/slide/scale) dans `ForestryNavigation.kt`.
- Micro-animations : `Crossfade`, `AnimatedVisibility`, `animateItemPlacement`, press-scale.
- Toutes les animations respectent le toggle `animationsEnabled` (si OFF → durée 0ms).

---

## 10) Versioning (auto build)
- `versionName` reste la version “produit” (ex: 1.1.0).
- À chaque compilation, un **Build ID** est injecté via `BuildConfig.BUILD_ID`.
- Affichage dans l’écran Paramètres → À propos : `versionName (BUILD_ID)`.

Fichier : `app/build.gradle.kts` (buildConfigField).

---

## 11) Tests
- Tests unitaires (ex : calculs ForestryCalculator) : `:app:testDebugUnitTest`

---

## 12) Journal des implémentations (résumé)
### Ajouts / améliorations (sessions récentes)
- Fix calcul recette/prix aligné aux volumes (alias essences + fallback wildcard).
- Correctifs parsing formules (gestion parenthèses imbriquées pour filtres).
- Refactor i18n Settings + Groups + ajouts strings FR/EN.
- Indicateur UI “prix manquants” dans Martelage + lien vers paramètres.
- Transitions globales navigation + micro-animations (Martelage, Parcelles, Placettes, PlacetteDetail, Groupes, GroupScreen, Formules, Calculator, Map).
- Build ID automatique affiché dans À propos.

### Changements / suppressions notables
- Remplacement de logique fragile de lookup prix par une logique robuste (alias/wildcard).
- Remplacement d’anciens textes en dur par des ressources (progressif).

---

## 13) TODO (à reprendre en priorité)
- Finaliser i18n : supprimer les dernières chaînes en dur et compléter `values-fr`.
- Renforcer datasets/tests dendrométriques si nécessaire.

---

## 14) Commandes utiles
- Build debug : `./gradlew :app:assembleDebug`
- Tests unitaires : `./gradlew :app:testDebugUnitTest`
