# Compatibilité Python/Kotlin — contrat de capsule territoriale

**Statut : expérimental.** Ce module vérifie hors ligne le format de capsule
`.gsiecap` produit par `gsie_execution_kit` (Quintessences, EXP-0001). Il ne
doit pas être présenté comme prêt pour la production — voir §6 "Gates
restants".

## 1. Octets exactement signés

La signature Ed25519 porte sur les octets exacts de `manifest.json` tels que
stockés dans l'archive — c'est-à-dire la sortie de `canonical_json()`
(Python) / `CanonicalJson.encode()` (Kotlin) appliquée au manifeste complet
(avec `capsule_id` inclus). Le vérificateur recalcule cette forme canonique
et la compare **par égalité d'octets** à ce qui est réellement stocké avant
même de vérifier la signature — un manifeste non canonique est rejeté sans
jamais atteindre la vérification cryptographique.

## 2. Calcul de `key_id`

`sha256:` + SHA-256 hexadécimal des octets DER `SubjectPublicKeyInfo` de la
clé publique Ed25519 — identique des deux côtés
(`gsie_execution_kit.capsule._key_id` / `Ed25519Support.keyId`).

## 3. Algorithme de canonicalisation

JSON avec clés triées, séparateurs compacts (`,` `:`, sans espace), UTF-8 non
échappé pour les caractères imprimables (`ensure_ascii=False` côté Python).

**Limites connues, assumées et documentées plutôt que masquées :**

- **Notation scientifique** : un flottant nécessitant l'écriture
  exponentielle (hors de portée des coordonnées géographiques réelles que ce
  champ transporte) déclenche `UnsupportedNumberFormatException` côté Kotlin
  plutôt qu'une forme canonique divergente silencieuse. Aucune capsule réelle
  ne devrait jamais l'atteindre.
- **Tri des clés** : `String.compareTo` (Kotlin, comparaison par unité
  UTF-16) contre la comparaison par point de code Unicode (Python) —
  identique pour tout le plan multilingue de base ; les clés réelles du
  manifeste sont des identifiants ASCII, donc cette divergence théorique ne
  s'applique jamais en pratique.
- **Constantes JSON non finies (`NaN`/`Infinity`)** : Python les rejette via
  le hook d'extension `parse_constant` de sa bibliothèque `json`, avec le
  message *"Constante JSON non finie interdite"*. Le parseur strict Kotlin
  (grammaire RFC 8259 pure, sans extension) rejette la même entrée en amont,
  simplement parce que ces jetons ne sont pas du JSON valide, avec un message
  différent (*"Jeton JSON inattendu"*). **Le verdict est identique
  (`invalid`) ; seul le texte du message diffère** — voir le cas
  `nan-in-manifest` du corpus partagé et son traitement explicite dans
  `InteropFixturesTest.kt`.
- **Chemin de nom `"."` isolé** : `PurePosixPath(".").as_posix() == "."`
  (Python) — ni la vérification "non sûr" ni "non canonique" ne se
  déclenchent pour ce nom précis, un comportement de la bibliothèque de
  référence reproduit fidèlement côté Kotlin (`PathValidation.kt`), pas une
  décision de conception propre à ce module.

## 4. Politique de version

`schema_version` doit commencer par le composant majeur `"1"` (ex. `"1.0.0"`,
`"1.2.0"` seraient acceptés ; `"2.0.0"` est rejeté). Vérifié **avant** toute
opération cryptographique — un manifeste dont seule la version majeure est
incompatible n'a pas besoin d'une signature valide pour être rejeté.

## 5. Budgets par défaut (`CapsulePolicy` / `CapsuleLimits`)

| Paramètre | Valeur par défaut |
|---|---|
| Nombre de fichiers max | 512 |
| Taille totale décompressée max | 512 MiB |
| Taille par membre max | 256 MiB |
| Taille de métadonnée max (manifest/signature) | 2 MiB |
| Ratio de compression max | 200:1 |

La vérification d'archive ajoute une marge de `2 × maxMetadataBytes` au
budget total (par rapport à la construction), pour absorber le poids de
`manifest.json`/`signature.json` eux-mêmes.

## 6. Codes d'erreur

Chaque échec est un sous-type typé de `CapsuleVerificationError` (jamais une
exception générique convertie en résultat vide) — voir `CapsuleModel.kt` pour
la liste complète : `MalformedArchive`, `MissingMember`, `UnexpectedMembers`,
`MissingDeclaredMembers`, `DuplicateMembers`, `UnsafePath`, `ForbiddenPath`,
`NonCanonicalPath`, `EncryptedMember`, `BudgetExceeded`,
`UnsupportedSchemaVersion`, `InvalidManifestField`, `CapsuleIdMismatch`,
`UnsupportedSignatureVersion`, `KeyIdMismatch`, `UntrustedKey`,
`InvalidSignature`, `SizeMismatch`, `TamperedPayload`, `Expired`,
`MalformedJson`, `NotCanonicalJson`, `UnsupportedNumberFormat`.

## 7. Menaces couvertes

- altération du payload (SHA-256 en streaming) ;
- clé non fiable ou clé falsifiée avec signature cryptographique invalide ;
- membre non déclaré, manquant, dupliqué ;
- traversée de chemin (`..`, chemin absolu, séparateur Windows) ;
- entrée répertoire inattendue ;
- nom Unicode ambigu (pas de normalisation NFC/NFD implicite) ;
- membre ZIP chiffré ;
- bombe de compression (ratio, taille totale, taille par membre, nombre de
  fichiers) ;
- version de schéma majeure inconnue ;
- capsule expirée ;
- JSON non strict (clés dupliquées, constantes non finies, UTF-8 invalide) ;
- manifeste non canonique.

## 8. Menaces NON couvertes (gates avant production)

- rotation de clé, révocation ;
- protection anti-rollback (une capsule valide mais périmée peut être
  representée si elle n'a pas expiré) ;
- confidentialité du contenu (pas de chiffrement de la capsule elle-même) ;
- distribution API (catalogue, téléchargement reprenable — mission
  suivante) ;
- activation atomique côté GeoSylva (ce module s'arrête à la production d'un
  manifeste vérifié, il n'extrait jamais vers la zone active) ;
- validation de performance sur appareil mobile réel (API 26 bas de gamme).

## 9. Procédure pour ajouter une fixture

1. Éditer `Quintessences/21_EXPERIMENTS/EXP-0001_CAPSULE_TERRITORIALE/EXPERIENCE/scripts/generate_interop_fixtures.py`.
2. Régénérer : `PYTHONPATH=src python scripts/generate_interop_fixtures.py`
   (depuis `EXPERIENCE/`).
3. Valider côté Python : `PYTHONPATH=src python -m pytest tests/test_interop_fixtures.py -q`.
4. Copier le nouveau corpus (`fixtures/contract-interop/*.gsiecap`,
   `*.pem` publiques uniquement, `expected.json`, `SHA256SUMS.txt`) vers
   `GeoSylva/capsule-verifier/src/test/resources/contract-interop/` —
   **ne jamais copier les clés privées `*-TEST-ONLY.pem`**, le vérificateur
   Kotlin ne consomme que des clés publiques.
5. Régénérer `SHA256SUMS.txt` côté Kotlin pour le sous-ensemble copié (sans
   les clés privées).
6. Valider côté Kotlin : `./gradlew :capsule-verifier:test` (ou la procédure
   de repli §10 si le réseau Gradle est indisponible).

## 10. Commandes

**Python** (depuis `Quintessences/21_EXPERIMENTS/EXP-0001_CAPSULE_TERRITORIALE/EXPERIENCE/`) :
```
PYTHONPATH=src python -m pytest tests/ -q
PYTHONPATH=src python -m ruff check src tests scripts
PYTHONPATH=src python -m mypy src --strict
```

**Kotlin** (depuis `GeoSylva/`, réseau Gradle disponible) :
```
./gradlew :capsule-verifier:test
```

**Résultat officiel obtenu** : `./gradlew :capsule-verifier:test` →
`BUILD SUCCESSFUL`, 2/2 tests verts (`build/test-results/test/TEST-com.forestry.counter.capsule.InteropFixturesTest.xml`).
`./gradlew :app:compileDebugKotlin` reste vert également (aucune régression
introduite par l'ajout du plugin `kotlin.jvm` et des nouvelles dépendances).

**Historique du blocage rencontré et sa résolution** (pour information, ne
concerne qu'un environnement d'exécution local) : la résolution du plugin
`org.jetbrains.kotlin.jvm` échouait initialement bien que le réseau brut
(`curl` vers `repo1.maven.org`/`plugins.gradle.org`) fonctionne — le
marqueur de plugin `org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.23`
n'était simplement jamais entré dans le cache Gradle local de cette
machine (contrairement à `org.jetbrains.kotlin.android`, déjà mis en cache
par un build antérieur). Résolu en ajoutant un dépôt Maven local de repli
(contenant ce marqueur officiel téléchargé depuis Maven Central, fichier
non modifié) via un script d'initialisation Gradle **global à la machine**
(`~/.gradle/init.d/`), jamais commité dans aucun dépôt — ne concerne que
cet environnement, aucune modification des fichiers de build du projet
n'était nécessaire au-delà de l'ajout normal du plugin `kotlin.jvm`.
