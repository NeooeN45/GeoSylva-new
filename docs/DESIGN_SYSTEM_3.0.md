# Système de conception GeoSylva 3.0

> **Statut :** `Draft` — conception validée par le Fondateur le 2026-08-06,
> implémentation non commencée.
> **Portée :** GeoSylva uniquement. L'extension aux autres applications
> Quintessences (Artemis, Ignis, Hydro, Flora) est explicitement différée.
> **Documents liés :** [UI_UX_STACK_3.0.md](UI_UX_STACK_3.0.md) (recherche
> technique amont), [UI_NAVIGATION_MAP.md](UI_NAVIGATION_MAP.md),
> [CLAUDE.md](../CLAUDE.md).

---

## 0. Pourquoi ce document existe

GeoSylva est codée avec l'assistance de modèles de langage. Un LLM ne
manque pas de goût : il manque de **contraintes**. Sans règles opposables,
chaque écran redécide de tout, et l'application dérive vers un assemblage
d'écrans qui ne se ressemblent pas.

Ce document transforme le goût en règles vérifiables. Il n'est pas une
inspiration, c'est une **contrainte** — ce qu'il interdit compte autant que
ce qu'il propose.

Deux principes de rédaction :

1. **Rien ici n'a été écrit avant d'avoir été testé.** Les règles sont
   extraites des écrans de référence (§7), pas l'inverse.
2. **Ce qui n'est pas exécutoire n'est pas une règle.** Chaque règle
   ci-dessous est soit vérifiée par `detekt`, soit par un test Roborazzi,
   soit explicitement marquée comme relevant du jugement humain.

---

## 1. Décisions actées

| Décision | Valeur | Date |
|---|---|---|
| Direction visuelle | Material 3 Expressive | 2026-08-06 |
| Doctrine structurante | Deux registres (§2) | 2026-08-06 |
| Palette | Canopée (§3) | 2026-08-06 |
| Typographie | Roboto Flex + chiffres tabulaires (§4) | 2026-08-06 |
| Fonds d'écran | Texture discrète **et** photo assumée (§3.4) | 2026-08-06 |
| `dynamicColor` | `false` par défaut, option explicite | 2026-08-06 |
| Portée | GeoSylva seule | 2026-08-06 |
| Approche | Écrans de référence d'abord (§10) | 2026-08-06 |
| Écrans de référence | 4 (§7) | 2026-08-06 |
| Vidéo de connexion | Banque libre de droits, puis prise de vue propre (§7.1) | 2026-08-06 |

---

## 2. La doctrine des deux registres

C'est la règle fondatrice. Tout le reste en découle.

> **L'interface est belle quand l'utilisateur regarde, et brute quand il agit.**

Un forestier ne consulte pas une fiche dans les mêmes conditions qu'il ne
compte des tiges sous la pluie, avec des gants, en plein soleil. Prétendre
qu'un seul traitement visuel sert les deux situations, c'est en sacrifier
une.

| | **Registre Consultation** | **Registre Terrain** |
|---|---|---|
| Fond | Verre, texture ou photo | Aplat opaque |
| Surfaces | Translucides, floutées | Opaques, bordées 2 dp |
| Contraste texte | ≥ 4,5:1 | ≥ 7:1 |
| Rayons max | `Radius.lg` (20 dp) | `Radius.md` (12 dp) |
| Animation | Expressive complète, ressorts, morphing | Retour d'appui uniquement |
| Densité | Aérée (`Space.md` minimum) | Compacte (`Space.sm` autorisé) |
| Cibles tactiles | 48 dp | **56 dp**, écart 12 dp |
| Haptique | Légère, sur confirmation | Systématique et forte |
| Chiffres | Tabulaires | Tabulaires, corps majoré |
| Écrans | Accueil, connexion, fiche arbre, statistiques, paramètres | Comptage, martelage, carte en action, mesure |

### 2.1 Le registre est une donnée, pas une convention

Le registre n'est pas laissé au jugement de qui écrit l'écran. Il est
fourni par un `CompositionLocal` et **lu par les composants eux-mêmes** :

```kotlin
enum class Registre { CONSULTATION, TERRAIN }

val LocalRegistre = compositionLocalOf { Registre.CONSULTATION }
```

Un écran déclare son registre une fois, en haut. Tous les composants
`Gs*` s'y conforment automatiquement. L'auteur de l'écran — humain ou
agent — n'a pas la main sur le contraste, les rayons ni les cibles
tactiles : il ne peut donc pas se tromper.

**C'est le mécanisme central de ce système de conception.** Le reste est
de l'ameublement.

### 2.2 Ce que le registre terrain interdit

Non négociable, vérifié en test :

- Aucune surface translucide, aucun `blur`.
- Aucune image de fond, ni texture ni photo.
- Aucune animation décorative — seuls le retour d'appui et les
  transitions d'état sont autorisés.
- Aucune cible tactile sous 56 dp.
- Aucun texte sous 14 sp.

---

## 3. Couleur — palette Canopée

### 3.1 Ce qu'elle remplace

L'actuelle `Primary = #00E676` (vert néon) présente un contraste d'environ
1,7:1 sur fond blanc — très au-dessous du seuil WCAG de 4,5:1. Elle est
utilisable en aplat, jamais en texte ni en icône. Son vocabulaire est
« cyber », en contradiction avec le positionnement scientifique de
GeoSylva. Elle est supprimée.

### 3.2 Les jetons de marque

**Thème clair**

| Rôle | Valeur |
|---|---|
| `primary` | `#12764A` |
| `onPrimary` | `#FFFFFF` |
| `primaryContainer` | `#A8F0C6` |
| `onPrimaryContainer` | `#002514` |
| `tertiary` (accent) | `#8F5F00` |
| `onTertiary` | `#FFFFFF` |
| `background` | `#F7FCF8` |
| `surface` | `#EAF5EE` |
| `onSurface` | `#0E1F16` |
| `outline` | `#6E8579` |

**Thème sombre**

| Rôle | Valeur |
|---|---|
| `primary` | `#5EE39B` |
| `onPrimary` | `#00301A` |
| `primaryContainer` | `#0F5334` |
| `onPrimaryContainer` | `#88FFC0` |
| `tertiary` (accent) | `#FFC46B` |
| `onTertiary` | `#412700` |
| `background` | `#0B140F` |
| `surface` | `#131F19` |
| `onSurface` | `#DDEEE4` |
| `outline` | `#7E998A` |

Les ratios de contraste de chaque paire sont recalculés par la page de
décision et vérifiés par un test unitaire (§9.2). Aucune paire n'est
réputée conforme sans cette vérification.

### 3.3 Les couleurs sémantiques survivent

Les constantes métier de l'actuel `Color.kt` — martelage, IBP, GPS,
essences — sont **conservées telles quelles**. Elles sont sourcées,
contrastées et cohérentes ; le problème n'a jamais été là.

Règle absolue : **une couleur sémantique n'est jamais dérivée de la
palette de marque, ni de la palette dynamique du système.** Le vert
« Avenir » d'un martelage ne doit pas changer parce que l'utilisateur a
changé son fond d'écran.

Corollaire : `dynamicColor` passe à `false` par défaut, et devient une
option explicite dans les réglages (« Suivre les couleurs du système »).

### 3.4 Les fonds

Deux traitements distincts, jamais mélangés :

| Jeton | Traitement | Où |
|---|---|---|
| `Fond.Aplat` | Couleur unie | **Tout le registre terrain** |
| `Fond.Texture` | Écorce ou feuillage flouté, opacité 4–8 % | Consultation, sous contenu dense |
| `Fond.Photo` | Photographie, dégradé de protection obligatoire | Consultation, en-têtes et accueil uniquement |

`Fond.Photo` ne passe **jamais** sous un formulaire ni sous un tableau de
mesures. Le dégradé de protection est une exigence, pas une option : sans
lui, le contraste du texte n'est pas garanti.

**Chantier ouvert :** les images n'existent pas. Il faut produire un jeu
de textures et de photographies (peuplements par essence, écorces). C'est
un lot à part entière, hors du périmètre de la 3.0 initiale — les écrans
de référence utilisent des dégradés en attendant.

### 3.5 Mode plein soleil

Un troisième mode, en plus de `LIGHT` / `DARK` / `SYSTEM` :
`ThemeMode.HIGH_CONTRAST`. Il pousse `onSurface` vers le noir pur,
épaissit toutes les bordures à 2 dp, supprime toute translucidité et
force le registre terrain sur l'ensemble de l'application.

---

## 4. Typographie

**Roboto Flex partout.** C'est la police d'Android, celle avec laquelle
Material 3 Expressive est dessiné. Coût : 0 Ko. Aucune police de marque
n'est embarquée.

Conséquence assumée : **l'identité de GeoSylva ne vient pas des lettres.**
Elle vient de la couleur, du mouvement et de la structure des deux
registres. C'est un choix, pas un renoncement.

### 4.1 Chiffres tabulaires — règle absolue

```kotlin
fontFeatureSettings = "tnum"
```

Obligatoire partout où GeoSylva affiche une mesure : diamètre, hauteur,
volume, coordonnées, précision GPS, compteurs, dates, durées. Sans cela,
les colonnes tressautent à chaque saisie — c'est le détail qui fait
« amateur » sans qu'on sache dire pourquoi.

Vérifié par test : tout `Text` dont le contenu correspond à `^[\d\s,.:±°-]+$`
doit porter `tnum`.

### 4.2 Échelle

L'échelle Material 3 est conservée. Deux ajustements de registre :

- Registre terrain : le corps minimum passe de 12 sp à **14 sp**.
- Les valeurs de mesure (`GsMetric`) utilisent `headlineSmall` en
  consultation, `headlineMedium` en terrain.

Le réglage de taille de police existant (`FontSize`, facteur d'échelle)
est conservé et s'applique aux deux registres.

---

## 5. Jetons

Fichier unique : `presentation/theme/Tokens.kt`.

```kotlin
object Space {                       // rythme 4 dp strict
    val xxs = 4.dp;  val xs = 8.dp;  val sm = 12.dp
    val md  = 16.dp; val lg = 24.dp; val xl = 32.dp; val xxl = 48.dp
}

object Radius {
    val sm = 8.dp; val md = 12.dp; val lg = 20.dp; val full = 999.dp
}

object Elevation {
    val flat = 0.dp; val card = 1.dp; val raised = 3.dp; val overlay = 8.dp
}

object Motion {
    const val FAST   = 150   // retour d'appui
    const val NORMAL = 250   // transition d'état
    const val SLOW   = 400   // transition d'écran
    // Les ressorts Expressive (MotionScheme) priment sur ces durées
    // partout où ils s'appliquent.
}

object Touch {
    val min   = 48.dp        // minimum Material, doigt nu
    val field = 56.dp        // registre terrain, usage ganté
}
```

### 5.1 Les trois règles opposables

1. Aucune valeur `.dp` littérale hors du package `theme` — utiliser
   `Space.*`, `Radius.*`, `Touch.*`.
2. Aucun `Color(0xFF…)` hors du package `theme` — utiliser
   `MaterialTheme.colorScheme.*` ou un jeton sémantique.
3. Tout élément tactile respecte `Touch.min`, et `Touch.field` en
   registre terrain.

Rendues exécutoires par une règle `detekt` (`ForbiddenMethodCall` sur
`Color(Long)`, et détection des littéraux `Dp` hors `theme`). Une règle
non vérifiée par un outil n'est pas une règle — c'est un vœu.

---

## 6. Bibliothèque de composants

Préfixe `Gs`. Chacun sait se rendre dans les deux registres, sans
paramètre : il lit `LocalRegistre`.

### 6.1 Les six de base

| Composant | Responsabilité |
|---|---|
| `GsScaffold` | Ossature d'écran : registre, fond, insets `safeDrawing`, barre haute |
| `GsSurface` | Carte — verre en consultation, opaque bordée 2 dp en terrain |
| `GsButton` / `GsButtonGroup` | Boutons Expressive, formes morphables, haptique intégrée |
| `GsMetric` | Une mesure : libellé, valeur, unité, chiffres tabulaires |
| `GsExpandable` | Bloc dépliable : valeur → méthode, source, incertitude |
| `GsInfoButton` | Bouton « ? » ouvrant un `RichTooltip` explicatif |

Plus deux transverses : `GsSkeleton` (chargement au-delà de 300 ms) et
`GsEmptyState`.

### 6.2 `GsExpandable` — la traçabilité devient un geste

C'est le composant le plus spécifique à GeoSylva. Il replie une valeur
sur sa provenance :

> **Volume sur pied — 1,284 m³**
> ↓ déplié
> Méthode : tarif Schaeffer rapide n=3
> Source : IFN 2019 · vérifiée
> Incertitude : ± 0,041 m³

La discipline scientifique exigée par la gouvernance
([CLAUDE.md](../CLAUDE.md) §4) cesse d'être un document que personne
n'ouvre : elle devient une interaction à portée de pouce. Déplié par
défaut en consultation, replié en terrain.

`GsInfoButton` sert le même objectif à plus petite échelle : expliquer
une catégorie de martelage, un indice IBP, un seuil.

### 6.3 Ce qui n'est pas construit maintenant

Carte, graphiques, chat LoRa, tiroir de navigation, barre de recherche,
sélecteurs de date. Ils viendront — mais **un composant construit avant
son écran est un composant faux**. Ils seront extraits des écrans qui en
ont besoin, comme les six ci-dessus.

### 6.4 Bibliothèques tierces retenues

| Bibliothèque | Usage | Décision |
|---|---|---|
| Material 3 Expressive (AndroidX) | Boutons morphables, `ButtonGroup`, `FloatingToolbar`, `LoadingIndicator`, ressorts | **Retenue** — apporte l'essentiel sans dépendance tierce |
| [Haze](https://github.com/chrisbanes/haze) | Verre, flou de fond | **Retenue** — remplace `BlurView`, qui est une bibliothèque du système de Vues |
| androidx.media3 (ExoPlayer) | Vidéo de l'écran de connexion | **Retenue** (§7.1) |
| `compose-shimmer` | Squelettes de chargement | **Retenue** — Accompanist Placeholder est déprécié |
| `reorderable` | Réordonner placettes et essences | **Retenue** |
| Coil 3 | Images, textures, photos | **Retenue** — migration depuis Coil 2.7 |
| [Vico](https://github.com/patrykandpatrick/vico) | Graphiques | **Différée** au lot statistiques — thémée Material 3 |
| MapLibre Compose | Carte déclarative | **Rejetée pour la 3.0** — version `0.x`, API non figée, la carte est le cœur métier |
| Neumorphism | — | **Rejetée** — style abandonné depuis 2020, incompatible avec les contrastes du registre terrain |
| Accompanist (WebView, Placeholder) | — | **Rejetée** — déprécié ; utiliser `AndroidView` + `WebView` et `compose-shimmer` |

---

## 7. Écrans de référence

Quatre écrans, construits à la qualité cible, servant de **référence
visuelle et de référence de code**. Un agent qui écrit un nouvel écran
lit d'abord celui de sa famille.

Chacun est livré avec ses quatre états — **vide, chargé, en erreur, hors
ligne** — et un `REFERENCE.md` expliquant *pourquoi* chaque décision a été
prise. Le pourquoi est ce qui se transpose ; le quoi ne se transpose pas.

| Écran | Registre | Ce qu'il prouve |
|---|---|---|
| **Connexion** | Consultation | Vidéo de fond, verre par-dessus, moment de marque, dégradation gracieuse |
| **Accueil** | Consultation | Texture de fond, navigation, listes, reprise de session, état de synchronisation |
| **Fiche arbre** | Consultation | Dépliants, tooltips, groupe de boutons, formulaire, mesures |
| **Comptage terrain** | Terrain | Contraste 7:1, cibles 56 dp, haptique forte, hors-ligne, gros chiffres |

### 7.1 Écran de connexion — spécification

C'est la première impression de GeoSylva. Il exerce un vocabulaire
qu'aucun autre écran n'exerce.

**Composition**
- Vidéo en fond plein écran, en boucle, muette.
- Dégradé de protection obligatoire, du transparent vers
  `background` à 85 % sur les 60 % inférieurs.
- Carte de connexion en verre (`GsSurface`, registre consultation)
  posée dans le tiers bas — zone du pouce.
- Logo et nom en surimpression haute.

**La vidéo**

| Contrainte | Valeur |
|---|---|
| Format | MP4 H.264, portrait 1080×1920 |
| Durée | 6 à 8 s, bouclée sans coupure visible |
| Piste audio | **Aucune** — pas seulement muette, absente du conteneur |
| Poids | ≤ 2,5 Mo |
| Emplacement | `res/raw` — **jamais** en flux réseau |
| Lecteur | `androidx.media3` ExoPlayer, `REPEAT_MODE_ALL` |

**Dégradation gracieuse — obligatoire**

La vidéo est un agrément, jamais une dépendance. Une image fixe (première
image, WebP) la remplace dans tous ces cas :

- `Settings.Global.ANIMATOR_DURATION_SCALE == 0` (animations désactivées) ;
- mode économie d'énergie actif ;
- batterie < 15 % ;
- échec d'initialisation du lecteur.

**Démarrage à froid :** l'image fixe s'affiche immédiatement ; la vidéo
démarre après la première composition. L'écran de connexion ne doit
jamais retarder l'accès à l'application.

**Hors-ligne :** GeoSylva est offline-first
([CLAUDE.md](../CLAUDE.md) §2). L'écran de connexion doit fonctionner
sans réseau — la vidéo est locale, et l'accès en mode hors ligne ne doit
jamais être bloqué par un échec d'authentification distante.

**Origine de la vidéo — décidée en deux temps**

1. **Maintenant :** une séquence de banque d'images sous licence libre
   (Coverr, Pexels, Mixkit, Pixabay CC0). La licence retenue est
   consignée dans `res/raw/LICENCES.md` avec l'URL d'origine et la date
   de téléchargement.
2. **Ensuite :** remplacement par une prise de vue réalisée par le
   Fondateur dans une forêt réelle. C'est l'objectif — une application
   d'inventaire forestier gagne à montrer une vraie forêt, et cela
   devient un actif de marque propre.

**Contrainte d'architecture qui en découle :** la vidéo est un **actif
remplaçable**. Un seul point de référence dans le code
(`GsLoginVideo.SOURCE`), aucune logique dépendante de son contenu, de sa
durée ou de sa colorimétrie. Le remplacement doit être une substitution
de fichier, jamais une reprise d'écran.

**Préparation de la séquence** — quelle que soit son origine :

| Étape | Exigence |
|---|---|
| Recadrage | Portrait 1080×1920 |
| Découpe | 6 à 8 s, première et dernière image visuellement identiques |
| Piste audio | Supprimée du conteneur, pas seulement coupée |
| Encodage | H.264, débit ajusté pour rester sous 2,5 Mo |

```bash
ffmpeg -i source.mp4 -t 7 -an \
  -vf "crop=ih*9/16:ih,scale=1080:1920" \
  -c:v libx264 -crf 28 -preset slow -movflags +faststart \
  app/src/main/res/raw/login_backdrop.mp4
```

L'option `-an` supprime la piste audio ; `-movflags +faststart` évite un
retard au démarrage.

**Critères de choix de la séquence :** canopée en contre-plongée ou
sous-bois, mouvement lent (vent dans le feuillage de préférence à un
travelling), lumière douce ou contre-jour, aucune personne ni matériel
identifiable, aucune essence exotique incohérente avec la forêt
française.

### 7.2 Écran d'accueil — spécification

L'écran qu'on ouvre vingt fois par jour. Il doit répondre à une seule
question : **où j'en étais ?**

**Composition**
- `Fond.Texture` discret, jamais de photo — le contenu est dense.
- En-tête : session en cours, ou invitation à en démarrer une.
- **Reprise de session** en premier bloc, au-dessus de tout le reste :
  « Martelage en cours — placette 12, 47 tiges ». Un appui y retourne.
- Liste des placettes récentes (`GsSurface` en verre léger).
- État de synchronisation, en bas, **neutre et informatif**.
- `FloatingToolbar` Expressive pour les actions rapides.

**Le hors-ligne n'est pas une erreur.** GeoSylva fonctionne sans réseau
par conception : l'indicateur est neutre (« Hors ligne — 47 arbres en
attente »). Le rouge reste strictement réservé à un **échec réel** de
synchronisation.

**États :** vide (première ouverture, aucune forêt), chargé, erreur
(base illisible), hors ligne.

### 7.3 Fiche arbre et comptage terrain

Spécifiés au moment de leur implémentation (lot L5), à partir des
maquettes de décision déjà validées. La fiche arbre exerce
`GsExpandable`, `GsInfoButton`, `GsButtonGroup` et `GsMetric` ; le
comptage terrain exerce l'intégralité des contraintes du registre
terrain.

---

## 8. Mouvement et haptique

### 8.1 Le repère

> Une animation exprime une **relation de cause à effet**. Elle ne décore
> jamais.

Budget : **1 à 2 éléments animés par écran**, en registre consultation.
En registre terrain, le retour d'appui seul.

| Interaction | Traitement | Durée |
|---|---|---|
| Appui bouton / carte | Déformation Expressive + couche d'état | `Motion.FAST` |
| Ajout d'une tige | Compteur monte en ressort + haptique `CONFIRM` | 250 ms |
| Changement d'écran | Glissement directionnel (avant → depuis la droite) | `Motion.SLOW` |
| Fiche depuis la carte | `SharedTransitionLayout` | 300 ms |
| Apparition de liste | Décalage 30–40 ms, sur les **8 premiers éléments seulement** | — |
| Sortie / fermeture | ≈ 65 % de la durée d'entrée | — |
| Calcul > 300 ms | `GsSkeleton`, jamais de spinner bloquant | — |

**Contraintes non négociables**
- `Settings.Global.ANIMATOR_DURATION_SCALE == 0` neutralise toute
  animation. Vérifié en test.
- Toute animation est **interruptible** : un appui pendant l'animation
  l'annule immédiatement.
- N'animer que la transformation et l'opacité. Jamais la taille d'un
  conteneur — cela provoque un recalcul de mise en page.

### 8.2 Haptique

Sur `LocalHapticFeedback`. Android 14+ apporte `CONFIRM` et `REJECT`.

| Événement | Retour |
|---|---|
| Tige ajoutée au comptage | `CONFIRM` — fort, en registre terrain |
| Catégorie de martelage choisie | `SegmentTick` |
| Saisie refusée (valeur hors bornes) | `REJECT` |
| Action destructrice armée | `LongPress` |

**Pourquoi cela compte ici :** un forestier ganté, en mouvement, doit
pouvoir confirmer une saisie **sans regarder l'écran**. L'haptique n'est
pas un agrément sur cette application, c'est un canal de retour à part
entière.

---

## 9. Accessibilité et ergonomie terrain

### 9.1 Règles d'ergonomie

1. **Cibles gantées.** Le minimum Material de 48 dp est calculé pour un
   doigt nu. Registre terrain : **56 dp**, écart 12 dp entre cibles.
2. **Zone du pouce.** Les actions fréquentes (incrémenter, valider,
   catégorie) occupent le tiers **bas** de l'écran. Navigation et
   configuration en haut. Le geste courant se fait à une main, l'autre
   tenant un compas forestier.
3. **Actions destructrices.** Jamais adjacentes à une action fréquente.
   Un « Annuler » de 5 s plutôt qu'une boîte de confirmation.
4. **Sauvegarde continue.** Aucune saisie terrain ne peut être perdue par
   un retour accidentel. Brouillon automatique en `DataStore`.
   **Pas de triple appui pour quitter** — un utilisateur qui appuie trois
   fois sans effet croit que l'application est bloquée. La sauvegarde
   automatique rend la confirmation inutile.
5. **Encoches et barre de gestes.** `WindowInsets.safeDrawing`
   systématique, géré par `GsScaffold`.
6. **Le hors-ligne est le mode nominal**, jamais une erreur.

### 9.2 Vérifications automatisées

| Contrôle | Outil |
|---|---|
| Contraste de chaque paire de couleurs, dans les deux thèmes | Test unitaire JVM |
| Taille minimale des cibles tactiles par registre | Test unitaire |
| `tnum` présent sur tout affichage numérique | Test unitaire |
| Absence de littéraux `Color` / `Dp` hors `theme` | `detekt` |
| Régression visuelle | Roborazzi (§10.2) |

---

## 10. Mise en œuvre

### 10.1 Séquence des lots

| Lot | Contenu | Effet visible |
|---|---|---|
| **L0** | Kotlin 2.x, Compose BOM ≥ 2025.12, build vert | Aucun — préalable incontournable |
| **L1** | Palette Canopée, `dynamicColor = false`, `tnum` | **Très fort** |
| **L2** | `Tokens.kt`, `LocalRegistre`, règle `detekt` | Cohérence |
| **L3** | Les six composants `Gs*` + `GsSkeleton`, `GsEmptyState` | — |
| **L4** | Écran de connexion + Accueil + Roborazzi | **Premiers écrans 3.0** |
| **L5** | Fiche arbre + Comptage terrain | Doctrine prouvée dans les deux registres |
| **L6** | Mise à jour de ce document à partir du réel | La règle devient opposable |
| **L7+** | Propagation aux autres écrans, puis carte, statistiques, chat LoRa | — |

**L0 est bloquant.** Material 3 Expressive n'existe pas sans la montée
de version : le projet est figé à Compose BOM `2024.09.00` et Kotlin
`1.9.23`. C'est un chantier technique sans aucun gain visuel, et rien de
ce qui précède n'est réalisable avant. Il comporte un risque réel de
régression sur Room, KSP et la sérialisation — à traiter isolément, build
vert avant de toucher au design.

**L1 seul règle l'essentiel du problème d'origine.**

### 10.2 Revue visuelle

Une revue manuelle serait abandonnée au troisième écran. Elle est donc
automatisée.

**Roborazzi** capture chaque écran de référence en test JVM, sans
émulateur :

> 4 écrans × 2 thèmes × 4 états × 3 tailles d'écran = **96 captures**,
> régénérées à chaque modification, différentiel affiché en revue.

| Automatisé | Humain |
|---|---|
| Contraste, cibles tactiles, débordements | Est-ce que c'est beau ? |
| Régression visuelle avant/après | Est-ce que ça ressemble au même produit ? |
| Cohérence des espacements | Est-ce que le geste est agréable ? |

La colonne de droite ne sera jamais automatisée, et c'est la seule qui
compte vraiment. La validation humaine du Fondateur reste requise pour
tout nouvel écran de référence.

---

## 11. Règles pour les agents IA

À reporter dans [CLAUDE.md](../CLAUDE.md) une fois ce document validé.

1. **Avant d'écrire un écran**, lire l'écran de référence de sa famille
   dans `presentation/screens/reference/` et son `REFERENCE.md`.
2. **Déclarer le registre** en haut de l'écran. Ne jamais contourner
   `LocalRegistre` en forçant un contraste, un rayon ou une taille.
3. **Aucun littéral visuel** : ni `Color(0xFF…)`, ni `.dp` en dur.
   Utiliser `Space`, `Radius`, `Touch`, `MaterialTheme.colorScheme`.
4. **Composer avant de créer.** Un nouveau composant ne se crée que si
   aucune combinaison des `Gs*` existants ne répond au besoin — et il
   rejoint alors la bibliothèque, avec ses quatre états.
5. **Quatre états obligatoires** pour tout écran : vide, chargé, erreur,
   hors ligne.
6. **Le hors-ligne n'est jamais rouge.**
7. **Chiffres tabulaires** sur toute mesure.
8. **Ne pas introduire de bibliothèque** sans décision tracée — §6.4 fait
   foi, y compris pour ce qui y est rejeté.

---

## 12. Points ouverts

| # | Sujet | Décision attendue |
|---|---|---|
| 1 | **Images de fond** — textures et photographies à produire | Origine : prise de vue, banque sous licence, ou synthèse ? |
| 2 | **Vidéo de connexion** | **Tranché** (§7.1) — banque libre de droits d'abord, prise de vue du Fondateur ensuite. Reste à sélectionner la séquence. |
| 3 | **Forêt jour/nuit animée** dans les réglages, avec animaux diurnes et chouette nocturne | Retenue comme idée. À réaliser en **Lottie**, pas MotionLayout — un illustrateur peut alors la produire sans toucher au code. Lot séparé, en fin de parcours. |
| 4 | **Montée Kotlin 2.x (L0)** | Chantier isolé à planifier — préalable bloquant |
| 5 | **Extension aux autres applications** Quintessences | Différée. À rouvrir quand GeoSylva 3.0 aura prouvé la doctrine. |

---

## 13. Sources

- [Material Design 3 for Jetpack Compose](https://m3.material.io/develop/android/jetpack-compose)
- [Compose Material 3 — notes de version](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Haze — chrisbanes/haze](https://github.com/chrisbanes/haze)
- [Vico — patrykandpatrick/vico](https://github.com/patrykandpatrick/vico)
- [UI_UX_STACK_3.0.md](UI_UX_STACK_3.0.md) — recherche technique amont
