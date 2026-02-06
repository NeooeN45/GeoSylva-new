# 🚀 Quick Start Guide - GéoSylva

## Installation Rapide

### 1. Ouvrir le Projet
```bash
# Dans Android Studio
File → Open → Sélectionner le dossier "multi counter forestier"
```

### 2. Synchroniser Gradle
Android Studio va automatiquement détecter et télécharger les dépendances.
Si ce n'est pas le cas:
```bash
File → Sync Project with Gradle Files
```

### 3. Configurer un Appareil
**Option A: Appareil Physique**
- Activer le mode développeur sur votre téléphone Android
- Activer le débogage USB
- Connecter via USB

**Option B: Émulateur**
- Tools → Device Manager → Create Device
- Choisir Pixel 6 ou similaire
- API Level 35 (Android 14) recommandé

### 4. Lancer l'Application
```bash
# Click sur le bouton Run (▶) ou
Run → Run 'app'
# Ou raccourci: Shift+F10
```

## Premiers Pas dans l'App

### Créer un Groupe
1. Écran d'accueil
2. Cliquer sur le bouton flottant `+`
3. Entrer "Parcelle A" comme nom
4. Cliquer sur "Create"

### Ajouter des Compteurs
1. Ouvrir le groupe "Parcelle A"
2. Cliquer sur `+`
3. Remplir:
   - Nom: "Hêtre 15-20"
   - Step: 1
   - Target: 50 (optionnel)
4. Cliquer sur "Create"
5. Répéter pour d'autres essences

### Compter
- **Tap simple** sur une carte → +1
- **Long press** → Menu d'options
  - Reset
  - Dupliquer
  - Supprimer

### Créer une Formule (Future)
```javascript
// Total Hêtres
sum(name:startsWith("Hêtre"))

// Densité/ha (avec variable PLOT_AREA=2000)
sum(*) * (10000 / PLOT_AREA)
```

## Structure du Projet

```
multi counter forestier/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/forestry/counter/
│   │   │   │   ├── data/           # Persistence
│   │   │   │   ├── domain/         # Logique métier
│   │   │   │   └── presentation/   # UI
│   │   │   ├── res/                # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # Tests
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Commandes Utiles

### Build
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (signé)
./gradlew assembleRelease

# Android App Bundle (Play Store)
./gradlew bundleRelease
```

### Tests
```bash
# Tests unitaires
./gradlew test

# Tests instrumentés
./gradlew connectedAndroidTest

# Test coverage
./gradlew testDebugUnitTestCoverage
```

### Clean
```bash
# Nettoyer les builds
./gradlew clean

# Nettoyer + rebuild
./gradlew clean build
```

## Troubleshooting

### Erreur de Sync Gradle
```bash
# Solution 1: Invalider le cache
File → Invalidate Caches → Invalidate and Restart

# Solution 2: Supprimer les caches manuellement
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Re-sync
./gradlew clean build
```

### Émulateur Lent
```bash
# Augmenter la RAM de l'émulateur
Tools → Device Manager → Edit → Advanced → RAM: 4096 MB

# Activer l'accélération matérielle
Settings → Emulated Performance → Graphics: Hardware
```

### Erreur de Build APK
```bash
# Vérifier la version Java
java -version  # Doit être JDK 17

# Vérifier les variables d'environnement
echo $JAVA_HOME
echo $ANDROID_HOME
```

### Import Gradle Échoue
```bash
# Vérifier la connexion internet (télécharge dépendances)
# Vérifier proxy si nécessaire

# Forcer le téléchargement
./gradlew build --refresh-dependencies
```

## Exemples de Code

### Créer un Compteur Programmatiquement
```kotlin
val counter = Counter(
    id = UUID.randomUUID().toString(),
    groupId = "group-id",
    name = "Chêne 20-25",
    value = 0.0,
    step = 1.0,
    targetValue = 100.0,
    tags = listOf("Feuillus", "Chêne")
)
counterRepository.insertCounter(counter)
```

### Évaluer une Formule
```kotlin
val result = formulaParser.evaluate(
    expression = "sum(name:startsWith('Hêtre'))",
    counters = allCounters,
    variables = mapOf("PLOT_AREA" to 2000.0)
)
```

### Exporter en JSON
```kotlin
val uri = // URI du fichier de destination
exportDataUseCase.exportToJson(uri)
```

## Configuration Recommandée

### Android Studio
- Version: Hedgehog (2023.1.1) ou supérieur
- Plugins:
  - Kotlin (installé par défaut)
  - Android Gradle Plugin
  - Jetpack Compose (installé par défaut)

### SDK
- Compile SDK: 35
- Min SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 14)

### Gradle
- Version: 8.2
- JVM: JDK 17

## Ressources

- [Documentation Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Material 3 Design](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## Support

**Problèmes?** Ouvrir une issue sur GitHub avec:
1. Description du problème
2. Logs d'erreur
3. Version Android Studio
4. Version Android du téléphone/émulateur

---

**Happy Coding! 🌲**
