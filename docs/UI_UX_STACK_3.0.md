# Stack UI/UX GeoSylva 3.0 — recherche et recommandations

> Document de recherche technique. Statut : `Draft`.
> Objectif : donner à GeoSylva 3.0 une identité visuelle moderne,
> lisible en plein soleil, utilisable avec des gants, et une base de
> composants réutilisables que les agents IA peuvent appliquer sans
> improviser le design à chaque écran.

---

## 0. Résumé exécutif

Trois constats issus de l'audit du code actuel
([libs.versions.toml](../gradle/libs.versions.toml),
[theme/](../app/src/main/java/com/forestry/counter/presentation/theme/)) :

1. **Le problème n°1 n'est pas une bibliothèque manquante, c'est la palette.**
   `Primary = #00E676` (vert néon) avec `OnPrimary = #000000` : c'est une
   couleur d'accent d'écran OLED, pas une couleur de marque. Elle sature,
   fatigue l'œil, et rend tout écran « démo de LLM ». Voir §2.
2. **`dynamicColor = true` par défaut annule toute identité de marque.**
   Sur Android 12+, la palette Material You du fond d'écran de
   l'utilisateur écrase entièrement le thème GeoSylva. C'est un choix
   valable pour une app système, pas pour un outil métier professionnel.
3. **La stack est saine mais figée à septembre 2024.** Compose BOM
   `2024.09.00`, Kotlin `1.9.23`. Tout Material 3 Expressive (les
   nouveaux composants, les ressorts de mouvement, les formes
   morphables) est hors de portée tant que le BOM n'est pas monté.

L'ordre d'attaque recommandé : **§2 palette → §1 montée de version →
§3 design tokens → §4 bibliothèques → §5 micro-interactions**. Faire §4
avant §2 revient à installer de beaux composants qui afficheront quand
même de mauvaises couleurs.

---

## 1. Socle : monter la stack Compose

| Élément | Actuel | Cible 3.0 | Pourquoi |
|---|---|---|---|
| Compose BOM | `2024.09.00` | BOM stable ≥ `2025.12.xx` (Compose 1.10 / Material3 1.4) | Débloque Material 3 Expressive, `LookaheadScope` stable, perfs de scroll |
| Kotlin | `1.9.23` | `2.0+` avec le plugin `compose-compiler` | Le compilateur Compose est intégré à Kotlin depuis 2.0 : `composeCompiler` disparaît du TOML |
| Material 3 | via BOM | `material3` + `material3-expressive` | Nouveaux composants (voir §4.1) |
| Adaptive | absent | `androidx.compose.material3.adaptive` | Tablette durcie en cabine / téléphone en forêt : même code, deux mises en page |

**Piège connu** : la montée Kotlin 2.x impose de basculer KSP et de
revalider Room + kotlinx-serialization. À traiter comme un chantier
isolé, avec build vert avant de toucher au design.

Référence : la release Compose de décembre '25 est la première stable
à embarquer Material 3 `1.4`
([Android Developers Blog](https://android-developers.googleblog.com/2025/12/whats-new-in-jetpack-compose-december.html),
[Compose Material3 releases](https://developer.android.com/jetpack/androidx/releases/compose-material3)).

---

## 2. Identité visuelle : la palette (priorité absolue)

### 2.1 Ce qui ne va pas aujourd'hui

- `#00E676` sur `#FFFFFF` : ratio de contraste ≈ **1.7:1**. Très
  au-dessous du minimum WCAG AA de 4.5:1 pour du texte. Utilisable en
  aplat de fond seulement, jamais en couleur de texte ou d'icône.
- Le duo néon vert + aqua (`#64FFDA`) est un vocabulaire « cyber /
  crypto », en contradiction directe avec le positionnement scientifique
  et forestier de GeoSylva.
- Les couleurs sémantiques du bas de [Color.kt](../app/src/main/java/com/forestry/counter/presentation/theme/Color.kt)
  (martelage, IBP, GPS) sont **excellentes** — sourcées, cohérentes,
  bien contrastées. Elles doivent survivre à la refonte. Le problème est
  uniquement la palette de marque au-dessus.

### 2.2 Palette proposée — « Forêt tempérée »

Logique : un vert **profond et désaturé** comme couleur de marque
(lisible, sérieux, tient en plein soleil), un ambre chaud comme accent
d'action, des neutres légèrement chauds plutôt que des gris purs.

| Rôle | Clair | Sombre | Usage |
|---|---|---|---|
| `primary` | `#2D5F3F` | `#8FD1A4` | Marque, boutons principaux, état actif |
| `onPrimary` | `#FFFFFF` | `#0B2417` | |
| `primaryContainer` | `#B8E6C5` | `#1B4430` | Chips actives, cartes en avant |
| `secondary` | `#4A6B58` | `#B1CCBB` | Éléments de support |
| `tertiary` / accent | `#B26A00` | `#FFB95C` | CTA unique par écran, alertes douces |
| `background` | `#FBFAF7` | `#12140F` | Blanc cassé chaud ≠ blanc pur |
| `surface` | `#F4F3EE` | `#1B1E18` | |
| `onSurface` | `#191C17` | `#E2E3DC` | Contraste ≥ 12:1 |
| `outline` | `#73796E` | `#8D9387` | Bordures visibles dans les **deux** thèmes |
| `error` | `#BA1A1A` | `#FFB4AB` | |

Contrôles à faire avant de figer : chaque couple `on*` / conteneur
vérifié à **4.5:1** (texte) et **3:1** (icônes, bordures), en clair
**et** en sombre, séparément.

### 2.3 Mode « plein soleil » — spécificité terrain

Un forestier lit son écran sous un couvert variable ou en plein champ.
Deux mesures peu coûteuses et à fort impact :

- Un troisième `ThemeMode.HIGH_CONTRAST` (en plus de `LIGHT`/`DARK`/`SYSTEM`)
  qui pousse `onSurface` vers le noir pur, épaissit les bordures à 2 dp
  et supprime les surfaces translucides.
- `WindowManager.LayoutParams.screenBrightness = 1f` optionnel sur les
  écrans de saisie terrain.

### 2.4 `dynamicColor`

Passer le défaut à **`false`**, et l'exposer comme option explicite
(« Suivre les couleurs du système ») dans les réglages. Les couleurs
sémantiques métier (martelage, IBP) ne doivent **jamais** être dérivées
de la palette dynamique.

---

## 3. Design tokens : le levier anti-« LLM sans goût »

C'est le point qui change tout pour du code généré par IA. Tant que les
écrans écrivent `padding(13.dp)`, `Color(0xFF3A8F5C)` ou
`RoundedCornerShape(11.dp)` à la main, chaque écran dérive. La parade :
un jeu de tokens fermé, et une règle « aucun littéral visuel dans un
écran ».

```kotlin
// presentation/theme/Tokens.kt
object Space {           // rythme 4 dp strict
    val xxs = 4.dp; val xs = 8.dp; val sm = 12.dp
    val md = 16.dp; val lg = 24.dp; val xl = 32.dp; val xxl = 48.dp
}
object Radius {
    val sm = 8.dp; val md = 12.dp; val lg = 20.dp; val full = 999.dp
}
object Elevation {
    val flat = 0.dp; val card = 1.dp; val raised = 3.dp; val overlay = 8.dp
}
object Motion {
    const val FAST = 150       // feedback tactile
    const val NORMAL = 250     // transition d'état
    const val SLOW = 400       // transition d'écran
}
object Touch {
    val min = 48.dp            // gants : viser 56.dp sur les écrans terrain
    val field = 56.dp
}
```

Trois règles à inscrire dans [CLAUDE.md](../CLAUDE.md) pour contraindre
les agents :

1. Aucune valeur `.dp` littérale hors de `Tokens.kt` — utiliser `Space.*`.
2. Aucun `Color(0xFF…)` hors de `Color.kt` — utiliser
   `MaterialTheme.colorScheme.*` ou un token sémantique.
3. Tout élément tactile ≥ `Touch.min` ; sur un écran de saisie terrain,
   `Touch.field`.

Une vérification `detekt` avec une règle `ForbiddenMethodCall` sur
`Color(Long)` en dehors du package `theme` rend la règle exécutoire
plutôt qu'incantatoire.

---

## 4. Bibliothèques — sélection retenue

### 4.1 Material 3 Expressive (AndroidX) — **à adopter**

Le plus gros gain de modernité, sans dépendance tierce. Apporté par la
montée de BOM du §1 : boutons à formes morphables, `ButtonGroup`,
`FloatingToolbar`, `LoadingIndicator` animé, indicateurs de progression
en vagues, et surtout un système de mouvement à ressorts (`MotionScheme`)
qui remplace les courbes de Bézier plates par des animations physiques.

C'est exactement le « jolis boutons + petites animations là où il en
faut » demandé, obtenu **sans** ajouter une seule bibliothèque tierce.
Statut : disponible depuis mai 2025, composants stabilisés
progressivement dans Material3 1.4/1.5
([m3.material.io](https://m3.material.io/develop/android/jetpack-compose)).

Usage recommandé dans GeoSylva :
- `ButtonGroup` pour la sélection de catégorie de martelage (Avenir /
  Réserve / Enlever / Dépérir / Biodiv) — meilleur que 5 chips.
- `FloatingToolbar` pour les actions carte (mesurer, ajouter arbre, GPS).
- `LoadingIndicator` pendant les calculs de cubage et l'export POI.

### 4.2 Haze — **remplace BlurView**

`com.github.Dimezis:BlurView` est une bibliothèque du système de Vues,
intégrée de force dans une UI Compose. [Haze](https://github.com/chrisbanes/haze)
est l'équivalent natif Compose, accéléré matériellement, avec repli
RenderScript pour les vieux Android depuis la 1.6.0, et un moteur
d'effets modulaire en 2.x ([Haze 2.0](https://chrisbanes.me/posts/haze-2.0/)).

À réserver aux **overlays de carte** (barre d'outils flottante au-dessus
de la carte MapLibre) — pas de glassmorphism décoratif : sur un écran
lu au soleil, le flou coûte du contraste.

### 4.3 Vico — **graphiques**

Pour l'histogramme de distribution des diamètres, la courbe de volume
par essence, la répartition du martelage.
[Vico](https://github.com/patrykandpatrick/vico) est extensible,
multiplateforme, et surtout **thémé Material 3 nativement** — les
graphiques héritent donc de la palette du §2 sans recolorisation
manuelle. KoalaPlot est l'alternative crédible mais moins alignée M3.

Règle métier associée : jamais de couleur seule pour distinguer une
série (voir la catégorie « Charts & Data » : `pattern-texture`,
`legend-visible`). Un forestier daltonien doit lire le même graphique.

### 4.4 MapLibre Compose — **à évaluer, pas à imposer**

Le TOML utilise `org.maplibre.gl:android-sdk:10.3.1`, le SDK Vue, donc
enveloppé dans un `AndroidView`. `org.maplibre.compose:maplibre-compose`
(v0.13.0) offre une API Compose déclarative
([maplibre.org/maplibre-compose](https://maplibre.org/maplibre-compose/)).

Réserve : version `0.x`, API non figée, et la carte est le cœur métier
de GeoSylva. Recommandation : **ne pas migrer en 3.0**. Isoler la carte
derrière une interface Compose maison, et réévaluer à la 1.0 de
maplibre-compose.

### 4.5 Autres, par ordre de valeur

| Bibliothèque | Usage | Verdict |
|---|---|---|
| `androidx.compose.material3.adaptive` | Tablette/téléphone, mode paysage | **Oui** — le paysage est le cas réel en cabine |
| Coil 3 (`io.coil-kt.coil3`) | Photos d'arbres, tuiles | **Oui** — migration depuis Coil 2.7 déjà présent, gain KMP et perfs |
| `accompanist-permissions` | GPS, caméra | **Oui** — sinon le code de permission pollue les écrans |
| Lottie Compose | Animations d'états vides / succès | **Avec parcimonie** — 2 ou 3 animations max, jamais en boucle infinie |
| `reorderable` (Calvin Liang) | Réordonner les placettes, les essences | **Oui** — drag & drop propre en Compose |
| `compose-shimmer` | Squelettes de chargement | **Oui** — remplace les spinners au-delà de 300 ms |
| Konfetti, particules, néon | — | **Non** — hors registre pour un outil métier |

Le reste (Hilt, Molecule, Voyager…) sort du périmètre UI/UX et ne doit
pas être introduit sous prétexte de refonte visuelle.

---

## 5. Micro-interactions : où en mettre, où ne pas en mettre

Le repère : **une animation doit exprimer une relation de cause à effet**,
jamais décorer. Budget : 1 à 2 éléments animés par écran.

| Interaction | Traitement | Durée |
|---|---|---|
| Appui sur bouton/carte | Échelle 0.97 + couche d'état M3 | `Motion.FAST` (150 ms) |
| Ajout d'un arbre au compteur | Le compteur monte en ressort, retour haptique `HapticFeedbackType.Confirm` | 250 ms |
| Changement d'écran | Slide directionnel : avant = depuis la droite, retour = vers la droite | `Motion.SLOW` |
| Ouverture fiche arbre depuis la carte | Transition d'élément partagé (`SharedTransitionLayout`) | 300 ms |
| Apparition de liste | Décalage de 30–40 ms par élément, sur les 8 premiers seulement | — |
| Sortie / fermeture | ~65 % de la durée d'entrée | — |
| Calcul de cubage > 300 ms | Squelette shimmer, pas de spinner bloquant | — |

Contraintes non négociables :
- Respecter `prefers-reduced-motion` — sous Android, lire
  `Settings.Global.ANIMATOR_DURATION_SCALE` et neutraliser les
  animations si à 0.
- Toute animation est **interruptible** : un appui pendant l'animation
  l'annule immédiatement.
- N'animer que `transform` / `alpha` — jamais la taille d'un conteneur,
  qui provoque un reflow.

---

## 6. Ergonomie terrain — ce que les guides génériques ne disent pas

Ce sont les points qui différencieront GeoSylva d'une app générique
bien dessinée.

1. **Cibles tactiles gantées** : le minimum Material de 48 dp est calculé
   pour un doigt nu. Sur les écrans de saisie utilisés en forêt, viser
   **56 dp**, avec 12 dp d'écart entre cibles.
2. **Zone du pouce** : les actions fréquentes (incrémenter, valider,
   catégorie de martelage) vont dans le tiers **bas** de l'écran. La
   navigation et la configuration en haut. Le geste courant se fait à
   une main, l'autre tenant un compas forestier.
3. **Actions destructrices** : supprimer une placette, réinitialiser un
   comptage — jamais adjacentes à une action fréquente, toujours avec
   un « Annuler » de 5 s plutôt qu'une boîte de confirmation qui
   ralentit le flux.
4. **États hors-ligne** : l'app est offline-first ([CLAUDE.md](../CLAUDE.md) §2).
   L'indicateur ne doit donc pas être une erreur rouge — c'est le mode
   nominal. Réserver le rouge à un vrai échec de synchronisation.
5. **Sauvegarde continue** : aucune saisie terrain ne doit être perdue
   par un retour accidentel. Brouillon auto en `DataStore`, et
   confirmation seulement si des données non enregistrées existent.
6. **Chiffres tabulaires** : diamètres, hauteurs, volumes en
   `FontFeatureSetting("tnum")` — sinon les colonnes tressautent à
   chaque incrément.
7. **Encoches et barre de gestes** : la barre d'action basse et la
   carte plein écran doivent respecter les `WindowInsets.safeDrawing`.

---

## 7. Plan d'exécution proposé

| Lot | Contenu | Dépend de | Impact visuel |
|---|---|---|---|
| L0 | Montée Kotlin 2.x + Compose BOM, build vert | — | Nul (préalable) |
| L1 | Palette §2 + `dynamicColor = false` + audit contraste | L0 | **Très fort** |
| L2 | `Tokens.kt` + règle detekt + purge des littéraux | L1 | Fort (cohérence) |
| L3 | Bibliothèque de composants : `GsButton`, `GsCard`, `GsField`, `GsCounter`, `GsEmptyState`, `GsSkeleton` | L2 | Fort |
| L4 | Material 3 Expressive sur les écrans clés (martelage, carte) | L3 | Fort |
| L5 | Haze (remplace BlurView), Vico, shimmer, reorderable | L3 | Moyen |
| L6 | Micro-interactions §5 + haptique | L4 | Moyen |
| L7 | Adaptive / paysage / tablette | L3 | Moyen |
| L8 | Mode plein soleil §2.3 | L1 | Fort en usage réel |

**L1 et L2 seuls règlent l'essentiel du problème énoncé.** Les lots
suivants sont de l'amélioration, pas du sauvetage.

---

## 8. Points à trancher par le Fondateur

1. La palette « Forêt tempérée » (§2.2) est une proposition. Faut-il
   l'aligner sur une charte Quintessences transverse (les autres apps
   clientes — Artemis, Ignis, Hydro — auront le même problème), ou
   GeoSylva porte-t-elle sa propre identité ?
2. Montée Kotlin 2.x : chantier réel avec risque de régression sur Room
   et la sérialisation. À planifier comme un lot isolé ou à repousser
   après la 3.0 ?
3. Typographie : la police système actuelle est un choix sûr. Une police
   de marque (par exemple un grotesque neutre pour les titres + chiffres
   tabulaires pour les données) est un gain d'identité, au prix de
   ~200 Ko d'APK.

---

## Sources

- [Material Design 3 for Jetpack Compose](https://m3.material.io/develop/android/jetpack-compose)
- [What's new in the Jetpack Compose December '25 release](https://android-developers.googleblog.com/2025/12/whats-new-in-jetpack-compose-december.html)
- [Compose Material 3 — release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Haze — chrisbanes/haze](https://github.com/chrisbanes/haze) et [Haze 2.0](https://chrisbanes.me/posts/haze-2.0/)
- [Vico — patrykandpatrick/vico](https://github.com/patrykandpatrick/vico)
- [MapLibre Compose](https://maplibre.org/maplibre-compose/) et [Maven Central](https://central.sonatype.com/artifact/org.maplibre.compose/maplibre-compose-android)
