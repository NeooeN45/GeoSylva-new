# GEOSYLVA-003 — Spécification fonctionnelle et scientifique GeoSylva 3.0

| Champ | Valeur |
|---|---|
| Identifiant | GEOSYLVA-003 |
| Statut | Frozen — Spec produit figée pour implémentation |
| Version | 0.9.1 |
| Date | 2026-08-04 |
| Auteur | Quintessences — spécification issue du brainstorming Fondateur/Codex |
| Périmètre | Application mobile GeoSylva et ses échanges avec GSIE |

## 1. Résumé

Ce document transforme le brainstorming validé en liste de réalisation pour la
prochaine grande évolution de GeoSylva. Il complète `MASTER_PLAN.md` et les
audits existants ; il ne remplace pas les contrats techniques ni les décisions
architecturales à venir.

La priorité est double :

1. offrir une expérience de terrain rapide, claire, adaptative et agréable ;
2. faire de GeoSylva une véritable base de données forestière offline-first,
   traçable et scientifiquement défendable.

Une interface séduisante ne doit jamais masquer une donnée perdue, une formule
non sourcée ou une estimation présentée comme une mesure.

## 2. Périmètre fonctionnel cible

La navigation canonique est :

```text
Accueil → Dossier/Projet → Forêt → Parcelle → Placette
       → Martelage → Observations → Calculs et synthèses → Synchronisation GSIE
```

Le produit cible comprend :

- l’accueil avec projets, dossiers, recherche et synchronisation ;
- la création guidée de forêts, parcelles et placettes ;
- les observations dendrométriques, sanitaires, de qualité et de biodiversité ;
- le martelage classique, vocal ou hybride ;
- les calculs locaux explicables et l’analyse approfondie GSIE ;
- les cartes et packs de données utilisables hors connexion ;
- les échanges Bluetooth, QR code et Meshtastic ;
- la gestion de compte, des paramètres, de la confidentialité et du mode développeur.

## 3. Principes non négociables

> **Cadrage v0.7.0** : ce document est la **spécification produit
> maîtresse** de GeoSylva-003. Il contient la vision, l'UX, les exigences
> fonctionnelles et les décisions techniques de haut niveau. Les modèles
> précis, endpoints, tables et bibliothèques ne doivent **plus** être
> décidés ici — ils font l'objet de **RFC indépendantes** (voir §28
> Annexe — RFC à extraire). Les sections §14 (contrats détaillés) et §15
> (architecture IA) sont des **visions de cadrage**, pas des contrats
> définitifs.

### 3.1 Donnée et base locale

GeoSylva est un client de saisie et de consultation d’une base locale, pas une
collection d’écrans qui recalculent leur propre état. Toutes les écritures passent
par un domaine de données transactionnel et versionné. Chaque objet doit avoir :

- un identifiant global stable (UUID) ;
- ses **relations structurelles et contextuelles** (pas un `parentId`
  unique — une mission peut concerner plusieurs forêts, un peuplement
  plusieurs parcelles, une placette plusieurs rattachements) ;
- son workspace ;
- ses éventuels rattachements principaux pour la navigation (vue
  utilisateur, §29.3) ;
- une date de création, de modification et, si pertinent, de suppression logique ;
- l’auteur, l’appareil et la session de saisie ;
- la source, la méthode, l’unité, la précision et le niveau de confiance ;
- une version et un historique permettant l’annulation et la résolution de conflits.

Les migrations Room, l’export et la restauration doivent préserver ces champs.
Une synchronisation interrompue ne doit produire ni doublon silencieux ni perte de
donnée. Les opérations doivent être rejouables et idempotentes.

### 3.2 Observation contre calcul

Une valeur observée, une valeur dérivée, une valeur estimée et une valeur
proposée par un modèle sont quatre états distincts dans l’interface et dans la
base. Une valeur manquante ne doit jamais être remplacée silencieusement par une
valeur plausible.

### 3.3 Offline-first

La création, la consultation, la saisie du martelage et les calculs de base
doivent fonctionner sans réseau. Le réseau enrichit les packs et déclenche
l’analyse GSIE ; il ne doit pas être requis pour terminer une mesure de terrain.

### 3.4 Explicabilité et consentement

Toute recommandation indique les données utilisées, la version de méthode, les
hypothèses, les limites et la possibilité de correction par le technicien. Toute
action destructive, tout partage de donnée sensible et toute fusion de versions
exigent une confirmation explicite.

## 4. Expérience et identité visuelle

### 4.1 Démarrage et onboarding

- Icône : feuille verticale abstraite, simple, douce et identifiable sur fond clair.
- Animation de lancement : ondulation unique de bas en haut pendant environ une
  seconde, uniquement lors d’un vrai démarrage à froid.
- Premier lancement : didacticiel court mais complet, visuel, interactif et
  rejouable depuis l’aide ; un faux projet permet de tester le parcours sans
  créer de donnée réelle.
- Le didacticiel est révisé lors des évolutions majeures et peut proposer un
  parcours commun puis un parcours adapté au rôle de l’utilisateur.

### 4.2 Connexion

- Écran affiché à la première connexion ou lorsque l’utilisateur est déconnecté.
- Vidéo de drone de forêt française fournie dans un pack signé et bouclée
  localement ; aucun streaming obligatoire.
- Panneau inférieur animé avec connexion Google et compte GeoSylva classique,
  création de compte et récupération.
- Mode découverte clairement marqué, avec données fictives non synchronisables.
- Les comptes entreprise et partenaires restent signalés « en développement »
  tant que leur contrat d’identité et leurs droits ne sont pas définis.

> **Évolution cible (§20)** : l'état actuel (connexion Google directe + compte
> GeoSylva classique) est **transitoire**. L'architecture cible — Keycloak
> comme broker d'identité, OIDC PKCE S256, passkeys/WebAuthn, organisations et
> workspaces, UUID Quintessences immuable — est décrite en §20. La migration
> des comptes existants est spécifiée en §20.9. Les comptes entreprise
> passent de « en développement » à une architecture définie (§20.10). Voir
> aussi RFC-0002 (Global Identity and Workspaces).

### 4.3 Accueil et projets

- Grille par défaut, alternative liste ; taille des cartes réglable.
- Réorganisation par glisser-déposer et regroupement dans des dossiers simples
  (sans sous-dossiers dans la première version).
- Projets récents en premier, dossiers avant projets.
- Carte projet : nom, couleur personnalisée, état de synchronisation, alerte et,
  en grande taille, nombre de tiges ; les détails supplémentaires sont dans `⋮`.
- Barre supérieure : carte, documentation scientifique, recherche, réorganisation
  et échanges.
- Recherche tolérante aux fautes, tags, date, statut et commande vocale.

### 4.4 Cartographie

- Fond hybride satellite + IGN lisible, avec niveaux de texte réglables.
- Calques organisés : travail, forêt, parcellaire, références, IGN et GSIE.
- Mode économie : fond noir sous 20 % de batterie ou selon le réglage choisi.
- Les cartes hors ligne proviennent de packs versionnés ; la date, la licence,
  la source et le périmètre sont toujours affichables.

## 5. Création guidée des données

### 5.1 Création d’une forêt

Proposer les modes GPS, sélection institutionnelle, recherche, saisie manuelle et
import. Un questionnaire rapide est affiché à chaque étape ; le mode complet est
disponible pour le technicien. La synthèse finale permet d’ajouter des parcelles,
choisir les protocoles et ouvrir la carte sans imposer son affichage.

### 5.2 Création d’une parcelle

Le parcours principal est un menu interactif, pas l’ouverture automatique d’une
carte. À partir du GPS et des packs locaux, GeoSylva doit :

1. rechercher les forêts proches ;
2. proposer au maximum cinq parcelles pertinentes ;
3. afficher pour chaque candidate la source, la date, la forêt, la surface et le
   propriétaire uniquement si cette information est légalement disponible ;
4. permettre la combinaison de sources (ONF, CNPF, DDT, IGN, cadastre) sans
   fusionner leurs géométries sans règle explicite ;
5. permettre une vue carte facultative, le dessin, l’import ou le mode
   « parcelle non référencée » si aucune candidate ne convient ;
6. afficher un récapitulatif avant création et conserver la provenance du choix.

La surface officielle de la forêt, la somme des parcelles sélectionnées et la
surface mesurée par le technicien sont trois valeurs séparées.

### 5.3 Création d’une placette

Le questionnaire propose trois choix :

- « Martelage sur l’intégralité de la parcelle » ;
- « Placette de surface définie » ;
- « Protocole d’échantillonnage » fourni par un pack ou configuré par le technicien.

La surface de parcelle est préremplie mais reste modifiable. Une surface de
martelage supérieure à la parcelle déclenche immédiatement une erreur visuelle
et bloque la validation tant qu’elle n’est pas corrigée. Une surface réduite
affiche un avertissement et propose le mode placette personnalisé. La forme,
le rayon et la surface sont recalculés ensemble et validés avant enregistrement.

Après création transactionnelle, l’application ouvre la fiche de placette ; le
martelage ne commence qu’après une action explicite.

## 6. Observations, essences et martelage

### 6.1 Fiche essence et placette

La page actuelle des cartes par essence est conservée comme base. Les évolutions
portent sur la robustesse du modèle, l’accessibilité, la recherche tolérante, la
provenance des observations et la séparation nette entre observation et synthèse.
Les onglets Essences et Évolution restent la structure de référence.

### 6.2 Modes de prise de données

- Mode classique : saisie tactile et formulaires rapides.
- Mode vocal : entraînement local facultatif à la voix de l’utilisateur, commandes
  confirmables et suppression de l’audio brut après transcription si aucun accord
  spécifique n’est donné.
- Mode hybride : les commandes vocales sont proposées, mais chaque donnée
  sensible ou ambiguë peut être confirmée visuellement.
- Essence inconnue : création provisoire avec statut à identifier, jamais rejet
  silencieux ni assimilation automatique à une autre essence.

La pluie peut proposer un mode tactile protégé et de gros contrôles ; le technicien
peut forcer ou désactiver ce comportement. La luminosité adaptative, limitée à
l’application et à la batterie disponible, revient au réglage précédent à la fin.

### 6.3 Session de martelage

Le modèle doit persister une vraie session : début, pauses, reprise, fin validée,
utilisateurs, appareils, mode de saisie, météo, réseau, durée totale, durée
active et événements. La pause rend l’application réutilisable sans clore la
session. La fin est confirmée en deux étapes et produit un instantané immuable
de la saisie avant analyse.

Le questionnaire post-martelage propose un mode simple et un mode complet. Il est
adaptatif, corrigeable, validé par le technicien et versionné avec les résultats.
L’analyse locale immédiate et l’analyse GSIE approfondie sont affichées comme
deux niveaux distincts.

## 7. Doctrine scientifique des calculs

### 7.1 Règle d’or

Aucun calcul forestier ne doit être codé à partir d’une formule « plausible ».
Chaque méthode doit être analysée, sourcée, discutée avec son domaine de validité,
implémentée avec ses unités et vérifiée par des cas de référence. Une méthode non
validée est explicitement marquée expérimentale et ne peut alimenter une
recommandation opérationnelle sans consentement.

### 7.2 Fiche obligatoire d’une méthode

Chaque formule ou modèle possède une fiche versionnée contenant :

- nom, objectif et population concernée ;
- équation, variables, unités, conversions et arrondis ;
- source bibliographique ou protocole officiel, date et licence ;
- hypothèses, domaine de validité, biais connus et incertitude ;
- paramètres régionaux, stationnels, d’essence, tarif et sylvicoles ;
- traitement des valeurs manquantes, aberrantes et hors domaine ;
- jeux de tests de référence et résultat attendu ;
- version de code, pack de paramètres et date de validation.

### 7.3 Variables à intégrer

Les moteurs doivent pouvoir prendre en compte, selon la méthode validée :

- essence, diamètre, hauteur, forme et coefficient d’élancement ;
- densité et qualité du bois, défauts, produit et destination ;
- état sanitaire, vigueur, mortalité, dépérissement et arbres habitat ;
- pente, station, sol, climat, région, structure et historique de gestion ;
- tarif de cubage, facteur de forme, seuils de diamètre et unité monétaire ;
- protocole d’échantillonnage, surface réellement couverte et effort de mesure.

Le résultat doit exposer les contributions principales et l’incertitude ; une
valeur de prix n’est jamais déduite d’un volume sans tarif et date explicites.

### 7.4 Pathogènes et parcelles voisines

Un pathogène observé sur une essence est une observation localisée, datée,
qualifiée par son niveau de preuve et reliée à une source ou à une photo. Un
signal provenant d’une parcelle voisine est un contexte de risque géographique,
pas une preuve que la parcelle courante est contaminée.

Les moteurs doivent donc :

- distinguer observation, suspicion, confirmation et absence d’observation ;
- conserver l’espèce hôte, le pathogène, la sévérité, la surface touchée et la
  méthode d’identification ;
- intégrer la distance, la date et la qualité de la source pour un risque voisin ;
- ne modifier un calcul de volume, de qualité ou de valeur que si une règle
  versionnée le prévoit et l’affiche clairement ;
- demander confirmation humaine pour une identification ou une extrapolation
  sanitaire incertaine.

### 7.5 Système de qualité des données

Inspiré d'Open Foris Collect (licence MIT), GeoSylva gère plusieurs états
de qualité par donnée :

```text
Brouillon terrain
→ Saisie terminée
→ Contrôle automatique
→ À corriger
→ Validé par le technicien
→ Contrôlé par un responsable
→ Verrouillé
```

Chaque donnée reçoit un **niveau de qualité** :

| Niveau | Signification |
|---|---|
| `VALIDÉE` | Donnée cohérente et confirmée |
| `PROBABLE` | Donnée plausible, à confirmer |
| `INCOMPLÈTE` | Donnée manquante ou partielle |
| `INCOHÉRENTE` | Donnée en contradiction avec d'autres |
| `HORS_PLAGE` | Donnée en dehors du domaine de validité |
| `À_VÉRIFIER` | Donnée atypique nécessitant confirmation |

**Exemples de contrôles de cohérence** :

- Diamètre de 165 cm pour un charme : valeur possible, mais atypique.
- Hauteur de 6 m pour un douglas de 75 cm de diamètre : incohérence
  probable.
- Volume calculé avec une équation employée hors de sa plage de
  calibration.

Le système ne doit pas bloquer automatiquement une observation rare, mais
demander une confirmation.

### 7.6 Moteur de campagnes multiannuelles

Inspiré d'Open Foris Arena (licence MIT, PostgreSQL/PostGIS, intégration
RStudio), ce moteur gère les placettes permanentes sur plusieurs cycles :

```text
Campagne 2026
├── placette 001
├── placette 002
└── placette 003

Campagne 2031
├── nouvelle observation des mêmes arbres
├── recrues
├── arbres morts
├── arbres exploités
└── arbres introuvables
```

**Calculs possibles** : accroissement diamétrique, accroissement en surface
terrière, accroissement en volume, mortalité, recrutement, évolution
sanitaire, évolution de la composition, évolution du carbone, effet des
interventions.

**Distinction fondamentale des entités** :

```text
Arbre permanent
≠ Observation de l'arbre
≠ Mesure
≠ Résultat calculé
```

Un arbre permanent est identifié une fois (UUID). Chaque campagne produit
une nouvelle observation. Chaque observation contient des mesures. Chaque
mesure peut déclencher des résultats calculés. Aucun niveau n'écrase un
autre — tout est conservé pour traçabilité temporelle.

### 7.7 Architecture en moteurs spécialisés

Le noyau scientifique est organisé en 9 domaines, chacun contenant des
moteurs à responsabilité unique :

```text
domain/
├── taxonomy/          SpeciesResolver, SpeciesGroupResolver, TraitResolver
├── measurement/       MeasurementNormalizer, UnitConverter, MeasurementValidator
├── dendrometry/       BasalAreaEngine, DiameterEngine, HeightEngine,
│                      DensityEngine, SamplingStatisticsEngine
├── volume/            VolumeEquationRegistry, VolumeMethodResolver,
│                      VolumeCalculationEngine, VolumeUncertaintyEngine
├── assortment/        StemSegmentationEngine, ProductClassifier, YieldLossEngine
├── valuation/         PriceCatalogResolver, QualityAdjustmentEngine,
│                      HarvestCostEngine, TransportCostEngine, NetValueEngine
├── silviculture/      StandDiagnosisEngine, ThinningSimulationEngine,
│                      ScenarioComparisonEngine
├── rules/             RuleEngine, RuleRegistry, RuleEvaluator, ExplanationBuilder
└── audit/             ProvenanceRecorder, CalculationTrace, MethodVersionRegistry
```

**Propriétés de chaque moteur** :

- indépendant de Compose (pas de dépendance UI) ;
- indépendant de Room (pas de dépendance persistance) ;
- testable avec des objets Kotlin simples ;
- déterministe (même entrée → même sortie) ;
- versionné ;
- documenté ;
- utilisable par l'application, le serveur et éventuellement d'autres
  modules GSIE.

### 7.8 Moteur de règles déclaratives

Les règles métier évoluent plus vite que l'application. Il faut éviter les
chaînes de `if/else` dispersées dans les ViewModels. Les règles sont
**déclaratives, versionnées et testables**, inspirées de JSON Logic.

**Exemple de règle JSON** :

```json
{
  "ruleId": "WOOD_VALUE_OAK_A_50",
  "version": "2026.1",
  "conditions": {
    "all": [
      { "field": "speciesGroup", "operator": "equals", "value": "OAK" },
      { "field": "diameterCm", "operator": "greaterOrEqual", "value": 50 },
      { "field": "quality", "operator": "equals", "value": "A" }
    ]
  },
  "effects": [
    { "type": "PRICE_MULTIPLIER", "value": 2.5 }
  ]
}
```

**Décision d'implémentation** : développer un **moteur Kotlin dédié et
limité**, inspiré de JSON Logic, plutôt que d'intégrer directement un
moteur JavaScript. Avantages : fonctionnement hors ligne, typage fort,
performance, auditabilité, contrôle des opérateurs autorisés, tests
unitaires déterministes.

### 7.9 Chaîne de valorisation économique

Le chiffrage final est présenté comme une **chaîne transparente** :

```text
Volume brut
− pertes techniques
= volume commercialisable

Volume commercialisable
× répartition par produits
× prix unitaire
× coefficient de qualité
= valeur brute

Valeur brute
− exploitation
− débardage
− transport
− tri
− stockage
− frais commerciaux
− risques
= valeur nette estimée
```

**Exemple de résultat** (pas un simple nombre) :

```text
Valeur nette estimée : 18 450 €
Fourchette probable : 16 800 à 20 300 €
Confiance : moyenne

Principaux facteurs :
+ qualité élevée des grumes
− débardage long
− pente de 22 %
− faible volume de bois d'industrie
```

Le moteur expose les contributions principales et l'incertitude. La
**distance de débardage sur graphe** (§19.6) alimente les coûts
d'exploitation.

### 7.10 Versionnement des méthodes scientifiques

Le serveur GSIE publie les référentiels via un **Method Registry** :

```text
Method Registry
├── méthodes de cubage
├── équations
├── tarifs
├── coefficients
├── densités
├── facteurs carbone
├── règles sylvicoles
├── protocoles
└── domaines de validité
```

GeoSylva télécharge les versions validées. **Scénario de migration** :

```text
Méthode locale installée : VOLUME-OAK-FR-002@1.2.0
Nouvelle méthode serveur : VOLUME-OAK-FR-002@1.3.0
```

L'utilisateur peut : consulter les différences, conserver l'ancienne
méthode pour les dossiers existants, appliquer la nouvelle méthode aux
nouveaux calculs, recalculer un scénario sans écraser l'historique.

### 7.11 IA et moteurs déterministes

**L'IA peut** : proposer l'essence probable, transformer une dictée en
données structurées, détecter une incohérence, expliquer un résultat,
produire un compte rendu, suggérer les méthodes compatibles, identifier
des informations manquantes.

**L'IA ne doit pas** : inventer une équation de cubage, un coefficient,
une valeur économique, une règle réglementaire, ou un diagnostic présenté
comme certain.

**Principe fondamental** : le calcul final doit toujours venir de moteurs
déterministes et audités (§7.7). L'IA assiste, explique et structure ;
elle ne remplace pas.

## 8. Architecture de données et synchronisation

Les entités Projet, Forêt, Parcelle, Placette, Session de martelage, Essence,
Tige, Observation sanitaire, Observation de qualité, Événement et Résultat de
calcul sont séparées. Les résultats dérivés sont recalculables à partir des
observations et référencent la version exacte de la méthode.

Les synchronisations Bluetooth, QR code et Meshtastic utilisent une session
explicite, des paquets signés et chiffrés, une sélection de données approuvée,
une reprise après interruption et un journal de fusion. Une fusion ne remplace
jamais silencieusement une version : les deux versions et la décision du
technicien restent récupérables.

## 9. Packs de données et départements

L’utilisateur peut sélectionner plusieurs départements cliquables, choisir des
packs recommandés ou personnaliser la sélection. Chaque pack expose version,
date, source, licence, empreinte et signature. Les mises à jour sont proposées
en Wi-Fi, de préférence en charge, réversibles et sans perte de projet.

Les packs peuvent contenir cartes, parcellaire, protocoles, référentiels d’essences,
tarifs ou modèles de calcul. Un pack ne peut pas modifier une méthode validée
sans nouvelle version, tests et journal de migration.

## 10. Paramètres, sécurité et mode développeur

Les paramètres sont classés par compte, sécurité, application, terrain/martelage,
cartes, packs, synchronisation, confidentialité, aide et informations. La
recherche tolère les fautes et reste en français en première intention.

Le mode développeur s’active par huit appuis sur le numéro de version. Il affiche
état de la base, migrations, file de synchronisation, packs, API, GPS, batterie,
versions de méthodes et journaux non sensibles. Les diagnostics sensibles exigent
un second niveau de confirmation et ne sont jamais envoyés automatiquement.

### 10.1 Catégories de consentement des données

Le système distingue cinq catégories de données pour l'amélioration des
modèles et le partage :

| Catégorie | Description | Partage |
|---|---|---|
| **Données privées du client** | Données confidentielles du propriétaire | Jamais sans consentement explicite |
| **Données utilisables pour améliorer les modèles** | Données d'entraînement consenties | Anonymisées, avec consentement |
| **Données scientifiques validées** | Données vérifiées pour la recherche | Partage recherche avec traçabilité |
| **Données pédagogiques** | Données utilisables pour l'enseignement | Anonymisées, avec consentement |
| **Données anonymisées** | Données totalement anonymisées | Partage communautaire possible |

Cette granularité alimente la boucle GSIE (§18.9) : les données client
restent privées sauf consentement explicite et cadre défini.

## 11. Vérification, validation et critères d’acceptation

Avant d’accepter une fonctionnalité :

- tests unitaires des formules avec valeurs calculées indépendamment ;
- tests de propriétés (unités, monotonicité attendue, invariants) ;
- cas limites : absence de hauteur, diamètre nul, surface incohérente, GPS
  imprécis, pathogène incertain, doublon de synchronisation ;
- tests d’intégration Room/migrations/export/import ;
- tests de reprise offline, conflit multi-utilisateur et paquet partiel ;
- tests UI réels sur le parcours création → observation → martelage → export ;
- benchmark documenté sur appareil ancien et appareil récent ;
- revue scientifique et traçabilité de la source avant activation opérationnelle.

Les tests qui ne montent pas réellement l’écran ou qui se contentent de
commentaires ne constituent pas une preuve d’acceptation.

Critères de sortie GeoSylva 3.0 : aucune perte de donnée dans les scénarios
offline et de fusion, 100 % des méthodes opérationnelles sourcées et testées,
aucun résultat sans provenance, et parcours terrain complet démontré sur les
profils d’appareils supportés.

## 12. Roadmap GeoSylva 3.0

### 12.1 Architecture cible

GeoSylva 3.0 consolide trois axes d'intelligence qui restent distincts dans le
code et dans la base :

```text
┌─────────────────────────────────────────────────────────────┐
│  Téléphone (offline-first, cœur forestier autonome)         │
│  ├─ Calculs déterministes sourcés (tarifs, IBP, Shannon)     │
│  ├─ LLM on-device léger (profil T1, §15.2)                  │
│  │   → assistance vocale, explication, jamais calcul        │
│  └─ Cache local : packs, référentiels, connaissances GSIE   │
├─────────────────────────────────────────────────────────────┤
│  Canal 1 — GSIE Serveur (Wi-Fi/4G stable)                    │
│  ├─ Moteurs lourds : Correlation, Reasoning, Diagnostic,    │
│  │   Recommendation, Forest Dynamics, Simulation            │
│  ├─ LLM serveur (profil T3-SERVER, §15.2)                   │
│  │   → RAG scientifique, raisonnement profond              │
│  └─ Knowledge Engine : coefficients sourcés, autécologie    │
├─────────────────────────────────────────────────────────────┤
│  Canal 2 — Bluetooth (proximité immédiate)                   │
│  └─ Partage terrain instantané entre deux appareils          │
├─────────────────────────────────────────────────────────────┤
│  Canal 3 — LoRa mesh (portée longue, bas débit)              │
│  └─ Synchronisation équipe terrain, remontée GSIE PC        │
└─────────────────────────────────────────────────────────────┘
```

**Principes non négociables** (consolidés depuis CLAUDE.md, RFC-0003,
VISION_LLM_SPECIALISES, ADR-009) :

1. **Offline-first** — le cœur forestier fonctionne sans réseau. Les canaux
   1-3 sont des amplificateurs, jamais des dépendances.
2. **Le LLM appelle les moteurs, ne calcule jamais de mémoire** — un volume,
   une surface terrière, un indice de risque ou une recommandation sylvicole
   est toujours produit par un moteur déterministe sourcé, jamais deviné par
   le LLM (VISION_LLM_SPECIALISES §2.1, ADR-009).
3. **Observation contre calcul** — quatre états distincts (§3.2) : observé,
   dérivé, estimé, proposé par modèle. Le LLM ne transforme jamais
   silencieusement l'un en l'autre.
4. **Provenance obligatoire** — toute valeur expose sa source, sa méthode,
   son unité, son incertitude et son niveau de preuve (ADR-009, §7.2).
5. **Le forestier reste le décideur** (GSIE-CON-001) — toute recommandation
   est contournable, toute action destructive exige confirmation.

### 12.2 Cascade LLM multi-tier

La vision multi-tier (VOLUME_CALCULATION_NEXT_GEN §10, RESEARCH_OPPORTUNITIES
§3) est consolidée ici sans pseudocode ni implémentation — uniquement
l'architecture cible et les garanties.

| Tier | Profil cible | Rôle | Réseau | Objectif |
|---|---|---|---|---|
| **T1 — Mobile** | T1-MICRO ou T1-STANDARD (§15.2) | Assistance vocale, explication des calculs locaux, saisie contextuelle, identification essence | Aucun | P50 < 500 ms |
| **T2 — Edge** | T2-EDGE (§15.2) | RAG sur documentation forestière, raisonnement intermédiaire, cascade si T1 insuffisant | Wi-Fi local | P50 < 3 s |
| **T3 — Serveur** | T3-SERVER (§15.2) | Raisonnement profond via moteurs GSIE, diagnostic, recommandation, simulation | 4G/Wi-Fi | P50 < 10 s |

> Les noms de modèles précis seront choisis par la RFC renouvelable
> `RFC-IA-MODEL-SELECTION-YYYY-MM` au moment de l'implémentation.

**Règles de cascade** :

- T1 répond seul tant que la question reste dans le périmètre des calculs
  locaux et des connaissances disponibles localement. Aucune donnée n'est envoyée au serveur
  pour une question que le téléphone peut traiter.
- T1 délègue à T2/T3 uniquement pour le raisonnement profond (diagnostic
  stationnel, projection de croissance, recommandation sylvicole). La
  délégation est explicite et tracée dans la session de martelage.
- T3 appelle les moteurs GSIE (Correlation, Reasoning, Diagnostic,
  Recommendation, Forest Dynamics, Simulation) et renvoie une conclusion
  expliquée avec la chaîne d'inférence — jamais un verdict brut.
- Le LLM ne produit jamais une valeur numérique forestière de lui-même : il
  invoque un moteur et cite le résultat. Une sortie LLM non citée est un
  défaut bloquant (ADR-009).

**Adaptateurs LoRA spécialisés** (VISION_LLM_SPECIALISES §2) :

- `GeoSylva-Forest` — sylviculture, dendrométrie, autécologie (T2/T3)
- `GSIE-Research` — recherche, comparaison des preuves et citations (T3)
- Le modèle terrain T1 reste un modèle généraliste quantifié, sans LoRA
  spécialisée (contrainte mémoire et simplicité de mise à jour).

### 12.3 Connexion GSIE Serveur — moteurs et contrats

GeoSylva 3.0 ne réimplémente pas la science forestière côté serveur. Elle
délègue aux moteurs GSIE via le canal 1 et consomme leurs contrats
documentés (`GSIE/ENGINES/*/`, `ENGINE_INTERFACE_CONTRACTS.md`).

| Moteur GSIE | Rôle pour GeoSylva | Quand GeoSylva l'appelle | Statut |
|---|---|---|---|
| **Correlation** | Détecte corrélations statistiques sourcées entre observations terrain et données domaine | Diagnostic stationnel, analyse peuplement | Livré |
| **Reasoning** | Inférence explicite et auditable sur connaissances et corrélations | Toute conclusion nécessitant une chaîne d'inférence | Livré |
| **Diagnostic** | Synthèse diagnostic stationnel/sylvicole (contraintes, atouts, risques sourcés) | Bouton "Analyse GSIE approfondie" post-martelage | Draft (persistance livrée) |
| **Recommendation** | Propositions sylvicoles contournables avec alternatives | Suite à un diagnostic, sur demande explicite | Stub |
| **Forest Dynamics** | Projection de croissance avec incertitude | Martelage assisté, scénarios +5/+10/+30 ans | Livré |
| **Simulation** | Comparatif de scénarios d'évolution | Avant validation d'une coupe, comparatif sylvicole | Architecture |
| **Botanical** | Taxonomie, autécologie, versionnement (TAXREF, GBIF) | Résolution essence, autécologie, identification PlantNet | Livré |
| **Learning** | Calibration continue à partir des retours forestier | Retours sur recommandations, calibration coefficients | Architecture |

**Contrats d'interface GeoSylva → GSIE** (à spécifier par moteur dans une RFC
dédiée — voir §12.5) :

- Chaque appel expose : `requete_id`, `session_id`, `auteur` (UUID
  Quintessences), `device_id` (UUID d'installation), `entry_mode`
  (provenance de saisie), `transport` (moyen de transport), `version`
  des données envoyées.
- Chaque réponse expose : `resultat_id`, `moteur_version`, `source_reference`,
  `evidence_level`, `incertitude`, `chaîne_inference` (Reasoning/Diagnostic).
- Les réponses sont mises en cache local (SQLCipher) avec leur `version` pour
  rejouer hors ligne et comparer les évolutions.
- Un appel échoué ne bloque jamais le parcours terrain : le résultat est
  marqué `EN_ATTENTE_AMPLIFICATION` et l'utilisateur continue avec les
  calculs locaux.

**SDK Kotlin** — GeoSylva consomme l'API GSIE via un SDK Kotlin dédié
(`GSIE/SDK/`, actuellement non implémenté). En attendant, les endpoints
Identity et Sync parcelles sont consommés directement via Retrofit (pattern
Factory existant). Le SDK Kotlin est un livrable de la Phase 4 (§12.4).

### 12.4 Ordre de développement — 11 lots

> **Revue critique v0.7.0** : l'ordre précédent (P0-P7) mettait trop tôt
> en avant le LLM on-device, Bluetooth, QR, Meshtastic et la refonte
> visuelle, alors que les fondations (QPIS, identité, Mission Engine,
> moteur géospatial, TreeVision) étaient reléguées en vision long terme.
> Le Fondateur a demandé de **refondre l'ordre** pour construire les
> fondations avant la toiture. L'ancien ordre P0-P7 est conservé en
> référence en fin de section.

Chaque lot produit ses tests, preuves et décision de validation (DEC).
Un lot ne démarre que si le précédent est au minimum en Review.

> **Refonte UI/UX transversale (§29)** : la refonte UI/UX n'est pas une
> phase finale isolée. Elle accompagne chaque lot fonctionnel. La phase
> finale (Quality Pass) ne sert qu'à harmoniser et optimiser ce qui a
> déjà été refondu. Voir §29.35 pour le détail page par page.

| Lot | Objet | Livrables | Dépendances | Pages UI (§29) | RFC/DEC |
|---|---|---|---|---|---|
| **Lot 0 — Audit et sécurisation de l'existant** | Stabiliser la base avant d'ajouter | Erreurs scientifiques connues, migrations, sauvegarde/restauration, chiffrement, tests de non-régression, audit UI (§29.34) | — | Splash, états globaux, erreurs, provenance, navigation | DEC corrections audits |
| **Lot 1 — Contrat universel de données** | Fondation du modèle de données | UUID globaux, Observation, Measurement, Evidence, CalculationRun, provenance, unités, événements, distinction arbre/observation/mesure/résultat (§7.6) | Lot 0 | Accueil, Explorer, Fiche projet, Fiche forêt (refonte), Création forêt/parcelle/placette guidée, Parcelles/Placettes enrichis | RFC-0001 amorcé |
| **Lot 2 — Noyau scientifique forestier** | Cœur métier déterministe | Cubage, surface terrière, hauteur, agrégations par essence, Method Registry (§7.10), incertitude, comparaison de méthodes, valorisation (§7.9), architecture moteurs spécialisés (§7.7), règles déclaratives (§7.8) | Lot 1 | Fiche placette (Calculs, Essences enrichis), Centre scientifique, Dashboard enrichi | RFC-0001 |
| **Lot 3 — Mission et Protocol Engine minimal** | Un seul métier, trois protocoles | Métier initial : technicien forestier. Trois protocoles pilotes : inventaire, martelage, diagnostic sanitaire. **Pas douze interfaces métier dès la v1.** | Lot 2 | Liste missions, dashboard mission, Saisie martelage terrain, SynthèseMartelage, Chantier travaux, Documents gestion, Diagnostics (stationnel/ripisylve/IBP déplacés) | RFC-0005 |
| **Lot 4 — Identité et workspaces** | Authentification fédérée | Keycloak, Google, passkey, espace personnel, une organisation, droits simples (§20.13), politique hors ligne, migration 3 cas (§20.9) | Lot 1 | Connexion Quintessences (refonte Login), Sélection workspace, Compte (4 domaines, 16 destinations secondaires, refonte Settings/Account), Appareils | RFC-0002 |
| **Lot 5 — Synchronisation GSIE** | Sync serveur normale | Journal d'événements, push/pull, idempotence, conflits, audit, pièces jointes | Lot 4 | Centre synchronisation, Résolution conflits, Analyse GSIE (refonte SuperCorrelateur) | RFC-0003 |
| **Lot 6 — QPIS minimal** | Packs de base | Pack système, pack scientifique, pack départemental, manifeste, signature, installation atomique, stockage. **La mise à jour différentielle par blocs peut venir ensuite.** | Lot 5 | Gestionnaire QPIS (refonte PackManager) | RFC-0004 |
| **Lot 7 — Geo Engine** | Moteur cartographique | MapLibre, PMTiles, GeoPackage, opérations de base, interopérabilité QGIS/QField | Lot 6 | Carte principale (refonte complète), fiches cartographiques | RFC-0006 |
| **Lot 8 — TreeVision R&D** | Mesure assistée (expérimental) | Visée base/cime, diamètre semi-automatique, saisie au compas comme référence, banc de validation. **Pas encore de placette entièrement automatique.** Statut initial « à valider » (§18.10). | Lot 2 | TreeVision caméra, mesure, validation, résultat | RFC-0007 |
| **Lot 9 — IA locale et vocale** | LLM on-device (après les moteurs) | Le LLM vient **après** les moteurs qu'il doit expliquer et appeler. Profils T1-MICRO/STANDARD/T2-EDGE/T3-SERVER (§15.2), RFC renouvelable. | Lot 2, Lot 3 | Assistant, dictée, explications | RFC-IA-MODEL-SELECTION |
| **Lot 10 — Meshtastic et sync de proximité** | Canal mesh (après sync normale) | Bluetooth (canal 2), QR code team key, Meshtastic (canal 3). **Très intéressant, mais après la synchronisation GSIE normale.** | Lot 5 | (pas d'UI dédiée, intégration dans Mission) | RFC sync terrain |
| **Quality Pass final** | Harmonisation UI/UX | Accessibilité, performances, cohérence visuelle, états vides/erreurs/hors ligne sur toutes les pages | Tous | Toutes les pages | — |

**Lots parallélisables** : Lot 4 (identité) peut démarrer en parallèle de
Lot 2 (noyau scientifique) car il ne dépend que de Lot 1. Lot 8
(TreeVision R&D) peut démarrer en parallèle de Lot 3 dès que Lot 2 est
en Review. Lot 7 (Geo Engine) peut progresser en parallèle de Lot 5.

**Ancien ordre P0-P7 (référence, remplacé par les 11 lots ci-dessus)** :

| Phase | Livrables | Statut |
|---|---|---|
| P0 — Fondations | Corrections audits, contrat données, tests restauration | **Remplacé par Lot 0-1** |
| P1 — Création guidée | Forêt/parcelle/placette, questionnaires | **Intégré dans Lot 1-3** |
| P2 — Martelage persistant | Session martelage, modes classique/vocal/hybride | **Intégré dans Lot 3** |
| P3 — Moteurs scientifiques locaux | Fiches méthodes, qualité, pathogènes, incertitudes | **Remplacé par Lot 2** |
| P4 — Connexion GSIE Serveur | SDK Kotlin, contrats API moteurs, cache, conflits | **Remplacé par Lot 5** |
| P5 — LLM on-device et multi-tier | Modèle T1, RAG, cascade, assistant vocal | **Remplacé par Lot 9 (décalé)** |
| P6 — Synchronisation terrain | Bluetooth, QR, Meshtastic | **Remplacé par Lot 10 (décalé)** |
| P7 — Refonte visuelle | Onboarding, animation, optimisations | **Obsolète — la refonte UI est désormais transversale à tous les lots (§29.35)** |

### 12.5 Décisions et RFC à produire

| # | Document | Objet | Phase déclencheuse |
|---|---|---|---|
| 1 | DEC — corrections audits | Hdom, indice station, SQLCipher, certificate pinning, RGPD | Lot 0 |
| 2 | DEC — format session martelage | Structure session, événements, instantané immuable | Lot 3 |
| 3 | RFC — fiches méthodes versionnées | Format fiche méthode (§7.2), registre, versionnement | Lot 2 |
| 4 | RFC — contrats GeoSylva ↔ moteurs GSIE | Contrats d'interface par moteur, cache, résolution conflits, pull | Lot 5 |
| 5 | RFC — IA forestière on-device | Architecture multi-tier, choix modèles, RAG, cascade, garde-fous | Lot 9 |
| 6 | RFC — synchronisation terrain | Bluetooth, QR, Meshtastic, paquets signés, journal de fusion | Lot 10 |

### 12.6 Critères de sortie GeoSylva 3.0

Repris et étendus depuis §11 :

- Aucune perte de donnée dans les scénarios offline et de fusion.
- 100 % des méthodes opérationnelles sourcées et testées (§7).
- Aucun résultat sans provenance, unité et incertitude (ADR-009).
- Parcours terrain complet démontré sur les profils d'appareils supportés.
- Le cœur forestier fonctionne sans réseau (canal 1 absent).
- Toute sortie LLM cite le moteur ou la source qu'elle invoque (ADR-009).
- Le forestier peut contourner toute recommandation et tracer sa décision
  (GSIE-CON-001, GSIE-CON-004).

### 12.7 Sources consolidées

Cette roadmap consolide sans les réinventer les visions existantes :

- `VOLUME_CALCULATION_NEXT_GEN.md` §10 (multi-tier LLM), §16 (martelage IA)
- `RESEARCH_OPPORTUNITIES.md` §3 (stack IA séquencée, modèles on-device)
- `VISION_LLM_SPECIALISES_GSIE_CORE_2026-07-20.md` (adaptateurs LoRA, famille
  de modèles, principe "LLM appelle moteurs")
- `RFC-0003` (GSIE-Net, intelligence distribuée, sync orientée données)
- `RFC-0019` (gsie-ai-gateway, RAG scientifique)
- `RFC-0018` (identification botanique PlantNet)
- `GSIE/ENGINES/*/` (contrats d'interface des 14 moteurs)
- `GEO-001` à `GEO-004` (exigences fonctionnelles existantes)
- `MASTER_PLAN.md` (programme DENDRO-EXCELLENCE, promesse produit)

### 12.8 Vision long terme (Dev Pack)

> **Correction v0.9.1** : les lots 0 à 10 de §12.4 constituent le **plan
> d'exécution actuel**. La roadmap Dev Pack ci-dessous donne la **vision
> longue durée**. Les anciennes phases P0-P7 sont **archivées
> uniquement pour traçabilité** (§12.4, tableau de référence).

Le Dev Pack (`21_EXPERIMENTS/GEOSYLVA_DEV_PACK_2026-08-04/`,
`10_ROADMAP_IMPLEMENTATION.md`) propose une vision long terme en 10 phases
(0-9) qui s'étend au-delà de la roadmap GeoSylva 3.0 (§12.4, lots 0-10). Ces
phases couvrent la transformation de GeoSylva en poste de travail numérique
complet du technicien forestier.

| Phase Dev Pack | Objet | Correspondance GeoSylva 3.0 |
|---|---|---|
| **Phase 0** — Audit du dépôt | Cartographier modules, tables, calculs, dettes | P0 (partiel) |
| **Phase 1** — Fondations communes | UUID globaux, distinction entité/observation/mesure, registre de méthodes | P0-P3 |
| **Phase 2** — Refonte du moteur forestier | VolumeEquationDefinition, MethodResolver, incertitude, valorisation | P3 |
| **Phase 3** — Mission et Protocol Engine | Métiers, capabilities, protocoles déclaratifs, formulaires contextuels | §17 (nouveau) |
| **Phase 4** — Identité et organisations | Keycloak, Google, passkeys, OIDC PKCE, workspaces | §20 (nouveau) |
| **Phase 5** — Synchronisation GSIE | Event journal, API idempotente, résolution conflits, parité | P4-P6 |
| **Phase 6** — QPIS | Manifeste, catalogue, delta, rollback, packs départementaux | §16 (nouveau) |
| **Phase 7** — Geo Engine | PMTiles, GeoPackage, R-Tree, QGIS/QField, PostGIS, Martin | §19.4 (nouveau) |
| **Phase 8** — TreeVision prototype | Diamètre semi-auto, scan multi-angle, GNSS stabilisé, banc | §18 (nouveau) |
| **Phase 9** — Moteurs serveur | Télédétection, STAC, Orfeo ToolBox, LiDAR, IA régionale | P4 (canal 1) |

**Stratégie de livraison** (Dev Pack) : chaque phase suit RFC → ADR → tests
de contrat → implémentation par petits lots → migration → instrumentation →
documentation → validation terrain → rollback possible.

**Priorité immédiate** (Dev Pack) : commencer par l'audit du dépôt et le RFC
du moteur de cubage et de valorisation. Ne pas lancer simultanément
l'identité, les packs, le SIG et TreeVision sans fondations partagées.

Voir aussi : §16 (QPIS), §17 (Mission/Protocol Engine), §18 (TreeVision),
§19 (Métiers et architecture modulaire), §20 (Identité fédérée).

## 14. Connexion GSIE Serveur — contrats détaillés

> **Avertissement v0.9.1 — NON NORMATIF** : cette section contient des
> exemples de cadrage (endpoints, tables, structures JSON, délais
> WorkManager, enveloppes). Ces éléments sont **non normatifs** — ils
> seront définis précisément dans **RFC-0003** (Synchronization Protocol)
> et **RFC-0033** (contrats GeoSylva ↔ moteurs GSIE). Devin ne doit pas
> implémenter ces exemples avant validation des RFC correspondantes.

### 14.1 Principe

GeoSylva 3.0 ne réimplémente pas la science forestière côté serveur. Elle
délègue aux moteurs GSIE via le **canal 1** (Wi-Fi/4G stable) et consomme
leurs contrats documentés (`GSIE/ENGINES/*/`, `ENGINE_INTERFACE_CONTRACTS.md`).
La spécification détaillée des contrats d'interface fait l'objet de la
**RFC-0033** (§12.5, Lot 5). Cette section pose le cadre et les formats
communs ; la RFC détaillera les endpoints REST, les codes d'erreur et les
schémas JSON complets.

### 14.2 Enveloppe commune de requête

Toute requête GeoSylva → GSIE porte une enveloppe commune garantissant la
traçabilité (ADR-009, GSIE-CON-005) :

```text
GeoSylvaRequest = {
  requete_id     : UUID          — généré côté mobile, idempotence
  session_id     : UUID?         — session de martelage courante (§6.3, optionnel : pas obligatoire pour tous les appels GSIE)
  auteur         : UUID          — identifiant Quintessences (UUID, pas texte)
  device_id      : UUID          — UUID d'installation (pas Android ID hashé)
  source         : enum { manual, sync, gps, bluetooth, lora }
  entry_mode     : enum { manual, voice, photo, sensor }  — provenance de saisie
  transport      : enum { local, sync, bluetooth, lora }  — moyen de transport
  version        : entier        — version des données envoyées (optimistic locking)
  moteur_cible   : enum { correlation, reasoning, diagnostic,
                          recommendation, forest_dynamics, simulation,
                          botanical, learning }
  payload        : <MoteurSpecificRequest>
  cache_hint     : texte (optionnel) — clé de cache pour rejouer hors ligne
}
```

### 14.3 Enveloppe commune de réponse

Toute réponse GSIE → GeoSylva porte l'enveloppe commune suivante :

```text
GeoSylvaResponse = {
  resultat_id    : UUID          — généré côté serveur
  requete_origine: UUID          — reflète requete_id de la requête
  moteur_version: texte         — ex. « correlation-engine@1.3.0 »
  source_reference : SourceReference  — provenance de la sortie (ADR-009)
  evidence_level : enum { A, B, C, D, E, F }
  incertitude    : décimal (optionnel) — intervalle ou écart-type
  chaine_inference : liste de EtapeInference (optionnel — Reasoning/Diagnostic)
  date_calcul    : ISO 8601
  payload        : <MoteurSpecificResponse>
  cache_ttl      : entier (optionnel) — durée de validité du cache en secondes
}
```

### 14.4 Moteurs appelés et déclencheurs

| Moteur | Déclencheur GeoSylva | Entrée clé | Sortie clé | Statut |
|---|---|---|---|---|
| **Correlation** | Bouton « Analyser corrélations » sur une placette | `CorrelationRequest` (parametres, zone_etude) | `CorrelationMatrix` (coefficients, p_valeur, domaine_validite) | Livré |
| **Reasoning** | Question libre « quelles essences adaptées ? » | `ReasoningRequest` (contexte, question) | `InferenceResult` (conclusions, chaine_inference) | Livré |
| **Diagnostic** | Bouton « Analyse GSIE approfondie » post-martelage | `DiagnosticRequest` (station_id, conclusions) | `Diagnostic` (contraintes, atouts, risques, confiance) | Draft |
| **Recommendation** | Suite à un diagnostic validé | `RecommendationRequest` (diagnostic_id, objectif) | `RecommendationSet` (recommandations, alternatives) | Stub |
| **Forest Dynamics** | Martelage assisté, scénario +10/+30 ans | `DynamicsRequest` (etat_initial, horizon) | `DynamicsProjection` (trajectoires, incertitude) | Livré |
| **Simulation** | Comparatif sylvicole avant validation coupe | `ScenarioSimulation` (scenarios) | `SimulationResult` (comparatif) | Architecture |
| **Botanical** | Résolution essence, autécologie, identification PlantNet | `BotanicalQuery` (taxon, station) | `BotanicalData` (autécologie, synonymes) | Livré |
| **Learning** | Retour forestier sur recommandation (accepte/refuse) | `LearningSignal` (decision, contexte) | `LearningOutput` (calibration proposée) | Architecture |

### 14.5 Chaîne d'appel type — analyse GSIE approfondie

Le parcours « Analyse GSIE approfondie » post-martelage enchaîne les moteurs
sans intervention utilisateur entre chaque étape :

```text
1. Correlation Engine
   Entrée : observations terrain (placette) + données domaine (GIS, Climate,
            Pedology, Botanical) déjà en cache serveur
   Sortie : CorrelationMatrix → injectée dans Reasoning

2. Reasoning Engine
   Entrée : StationContexte (géographie, climat, sol, botanique, peuplement)
            + CorrelationMatrix
   Sortie : InferenceResult (conclusions + chaîne d'inférence)
            → injecté dans Diagnostic

3. Diagnostic Engine
   Entrée : conclusions du Reasoning + station_id + type_diagnostic
   Sortie : Diagnostic (contraintes, atouts, risques, confiance)
            → persisté côté serveur (diagnostic_id UUID5)
            → renvoyé à GeoSylva pour affichage

4. Recommendation Engine (sur action explicite forestier)
   Entrée : diagnostic_id + objectif_forestier + contraintes_forestier
   Sortie : RecommendationSet (recommandations + alternatives)
            → toutes contournables (GSIE-CON-001)

5. Simulation Engine (sur action explicite forestier)
   Entrée : scénarios de martelage (avant/après coupe)
   Sortie : SimulationResult (comparatif +5/+10/+30 ans)
```

Chaque étape est tracée dans la session de martelage (§6.3) avec son
`resultat_id`, sa `moteur_version` et son `evidence_level`. Le forestier
peut consulter la chaîne complète d'inférence à tout moment (GSIE-CON-004).

### 14.6 Cache local et mode hors ligne

Les réponses des moteurs sont mises en cache local (SQLCipher, table
`gsie_cache`) avec :

- `requete_id` (clé primaire)
- `moteur_cible`
- `payload` sérialisé (JSON chiffré)
- `moteur_version` (pour détecter une obsolescence)
- `date_calcul` + `cache_ttl` (pour expiration)
- `source_reference` + `evidence_level` (pour affichage provenance)

**Règles de cache** :

- Un résultat en cache est affiché avec un badge « amplification GSIE
  (version X, date Y) ». Si la `moteur_version` est antérieure à la
  dernière version connue du moteur, un badge « obsolète » est affiché et
  une ré-exécution est proposée.
- Un appel échoué (réseau indisponible) ne bloque jamais le parcours
  terrain. Le résultat est marqué `EN_ATTENTE_AMPLIFICATION` et
  l'utilisateur continue avec les calculs locaux déterministes.
- À retour du réseau, les requêtes `EN_ATTENTE_AMPLIFICATION` sont
  rejouées automatiquement (WorkManager, retry exponentiel 15s → 1h).
- Le forestier peut forcer une ré-exécution manuelle (mode développeur,
  §10) pour comparer un résultat en cache avec une nouvelle exécution.

### 14.7 Pull serveur → mobile et résolution de conflits

La synchronisation parcelles (DEC-000048) est actuellement unidirectionnelle
(push mobile → serveur). GeoSylva 3.0 ajoute le **pull serveur → mobile**
pour récupérer les diagnostics, recommandations et connaissances produites
côté serveur (par un autre technicien ou par une analyse différée).

**Contrat pull** (à spécifier dans RFC-0033) :

- `GET /api/v1/sync/geosylva/resources/{client_id}?since={timestamp}` —
  récupère les ressources modifiées depuis `timestamp`, paginé.
- Chaque ressource renvoyée porte son `version` (optimistic locking).
- Si la version locale est identique → ignorée.
- Si la version locale est plus récente → conflit 409, résolution
  explicite requise (écran `ConflictResolutionScreen`).

**Résolution de conflits** (écran dédié, Phase P4) :

- L'écran affiche côte à côte la version locale et la version serveur.
- Le forestier choisit : garder local, garder serveur, ou fusionner
  manuellement.
- La décision est tracée (auteur, date, choix, justification optionnelle).
- Aucune fusion automatique silencieuse — les deux versions et la
  décision restent récupérables (§8).

### 14.8 SDK Kotlin

GeoSylva consomme l'API GSIE via un **SDK Kotlin** dédié
(`GSIE/SDK/kotlin/`, actuellement non implémenté). Le SDK encapsule :

- L'authentification JWT (refresh automatique, pattern existant
  `IdentityRepositoryImpl`).
- Les contrats d'enveloppe commune (§14.2, §14.3).
- Le cache local avec expiration et détection d'obsolescence.
- La file d'attente `EN_ATTENTE_AMPLIFICATION` (WorkManager).
- La sérialisation kotlinx.serialization (alignée sur le métamodèle GSIE
  v6.2).

En attendant le SDK, les endpoints Identity et Sync parcelles sont
consommés directement via Retrofit (pattern Factory existant,
`IdentityApiFactory`, `ParcelSyncApiFactory`). Le SDK Kotlin est un
livrable de la Phase P4.

### 14.9 Garde-fous

- **ADR-009** : toute valeur retournée par un moteur expose sa
  `source_reference`, son `evidence_level` et est reconstructible. Une
  réponse sans `source_reference` est un défaut bloquant.
- **GSIE-CON-001** : toute recommandation est `contournable: vrai`. Le
  forestier peut refuser, modifier ou demander une alternative.
- **GSIE-CON-004** : toute conclusion expose sa `chaine_inference`. Le
  forestier peut consulter chaque étape (règle, source, prémisses).
- **Offline-first** : un appel GSIE échoué ne dégrade jamais le cœur
  forestier. Le résultat est `EN_ATTENTE_AMPLIFICATION`, pas une erreur
  bloquante.
- **Pas de LLM sans moteur** : le LLM serveur (T3, §15) invoque toujours
  un moteur pour produire une valeur numérique forestière. Une sortie LLM
  non citée est un défaut bloquant (ADR-009).

## 15. LLM on-device et multi-tier — architecture détaillée

> **Avertissement v0.9.1 — NON NORMATIF** : cette section contient une
> vision de cadrage (profils T1/T2/T3, cascade, RAG, LoRA). Les choix
> précis de modèles, formats de prompts, schémas RAG et stratégies de
> quantification seront définis dans **RFC-IA-MODEL-SELECTION-YYYY-MM**
> (renouvelable) et **RFC-0019** (gsie-ai-gateway). Devin ne doit pas
> implémenter ces exemples avant validation des RFC correspondantes.

### 15.1 Vision consolidée

Cette section consolide sans les réinventer les visions existantes :
`VOLUME_CALCULATION_NEXT_GEN.md` §10 (multi-tier LLM),
`RESEARCH_OPPORTUNITIES.md` §3 (stack IA séquencée),
`VISION_LLM_SPECIALISES_GSIE_CORE` (adaptateurs LoRA, famille de modèles),
`RFC-0019` (gsie-ai-gateway serveur). La spécification opérationnelle
détaillée (formats de prompts, schéma RAG, stratégie de quantification,
banc d'évaluation) fait l'objet de la **RFC-0034** (§12.5, Phase P5).

### 15.2 Architecture multi-tier

> **Avertissement** : les noms de modèles précis ne sont **pas** figés
> dans cette spécification. Les modèles IA évoluent rapidement et seront
> probablement obsolètes avant la phase d'implémentation. Cette section
> définit des **profils** (taille, rôle, contraintes), pas des choix
> définitifs. La sélection concrète fait l'objet d'une **RFC renouvelable**
> `RFC-IA-MODEL-SELECTION-YYYY-MM` qui comparera les modèles disponibles
> au moment réel du développement. Les latences ci-dessous sont des
> **objectifs à mesurer** (P50, P95, temps de premier token, tokens/s,
> RAM maximale, chauffe, consommation, qualité métier), pas des
> promesses.

```text
┌──────────────────────────────────────────────────────────────┐
│  Tier 1 — Mobile (on-device, offline)                        │
│  Profil    : T1-MICRO (0,3 à 1,5 B) ou T1-STANDARD (1,5 à 4 B)│
│  Runtime   : ONNX Runtime / llama.cpp Android                 │
│  Mémoire   : à valider par benchmark (un modèle 3B en INT4    │
│              représente ~1,5 GB de poids bruts seuls, avant   │
│              métadonnées, contexte et mémoire d'exécution)    │
│  Rôle      : assistance vocale, explication des calculs      │
│              locaux, saisie contextuelle, identification     │
│              essence (modèle à entraîner/adapter, §15.6)      │
│  Réseau    : aucun                                            │
│  Objectif  : P50 < 500 ms, P95 < 2 s (génération courte)     │
│  Garde-fou : n'invoque jamais un moteur GSIE (pas de réseau) │
│              → explique les résultats locaux, ne calcule pas │
├──────────────────────────────────────────────────────────────┤
│  Tier 2 — Edge (Wi-Fi local, Jetson / NIM dev)                │
│  Profil    : T2-EDGE (4 à 12 B, quantifié)                   │
│  Runtime   : vLLM ou NIM sur Jetson Orin / poste GSIE PC      │
│  Rôle      : RAG sur documentation forestière locale,        │
│              raisonnement intermédiaire, cascade si T1        │
│              insuffisant                                      │
│  Réseau    : Wi-Fi local (terrain, base vie)                 │
│  Objectif  : P50 < 3 s, P95 < 8 s                            │
│  Garde-fou : peut invoquer les moteurs GSIE locaux (cache)   │
│              → cite le moteur, ne calcule pas de mémoire      │
├──────────────────────────────────────────────────────────────┤
│  Tier 3 — Serveur (4G/Wi-Fi, gsie-ai-gateway RFC-0019)        │
│  Profil    : T3-SERVER (modèle choisi par benchmark)         │
│  Runtime   : vLLM sur GPU serveur (RFC-0019)                  │
│  Rôle      : raisonnement profond via moteurs GSIE,          │
│              diagnostic, recommandation, simulation,         │
│              RAG scientifique (pgvector, /ai/research)        │
│  Réseau    : 4G/Wi-Fi stable                                  │
│  Objectif  : P50 < 10 s, P95 < 30 s                          │
│  Garde-fou : invoque toujours un moteur pour toute valeur     │
│              numérique forestière (ADR-009)                  │
└──────────────────────────────────────────────────────────────┘
```

**Profils de référence** (les noms concrets seront choisis par la RFC
renouvelable au moment de l'implémentation) :

| Profil | Taille | Rôle | Contrainte |
|---|---|---|---|
| `T1-MICRO` | 0,3 à 1,5 B | Assistance vocale, saisie contextuelle | < 1 GB RAM |
| `T1-STANDARD` | 1,5 à 4 B | Explication calculs, identification essence | < 2 GB RAM |
| `T2-EDGE` | 4 à 12 B | RAG local, raisonnement intermédiaire | Jetson / PC |
| `T3-SERVER` | choisi par benchmark | Raisonnement profond, diagnostic | GPU serveur |

### 15.3 Règles de cascade

1. **T1 répond seul** tant que la question reste dans le périmètre des
   calculs locaux et des connaissances disponibles localement. Aucune donnée n'est
   envoyée au serveur pour une question que le téléphone peut traiter.
2. **T1 délègue à T2/T3** uniquement pour le raisonnement profond
   (diagnostic stationnel, projection de croissance, recommandation
   sylvicole). La délégation est **explicite et tracée** dans la session
   de martelage (§6.3) : `tier_source`, `prompt_envoye`, `moteur_invoque`.
3. **T3 appelle les moteurs GSIE** (Correlation, Reasoning, Diagnostic,
   Recommendation, Forest Dynamics, Simulation) et renvoie une conclusion
   **expliquée avec la chaîne d'inférence** — jamais un verdict brut.
4. **Le LLM ne produit jamais une valeur numérique forestière de
   lui-même** : il invoque un moteur et cite le résultat. Une sortie LLM
   non citée est un défaut bloquant (ADR-009).
5. **Le forestier voit la cascade** : l'UI affiche « réponse locale »,
   « réponse edge » ou « réponse serveur » avec le tier source et la
   latence. Aucune réponse n'est présentée sans indiquer son origine.

### 15.4 Adaptateurs LoRA spécialisés

D'après `VISION_LLM_SPECIALISES_GSIE_CORE` §2, les adaptateurs LoRA sont
spécialisés par application et partagent un modèle de base multilingue :

| Adaptateur | Application | Rôle | Tier |
|---|---|---|---|
| `GeoSylva-Forest` | GeoSylva | Sylviculture, dendrométrie, autécologie | T2/T3 |
| `GSIE-Research` | GSIE Core | Recherche, comparaison des preuves, citations | T3 |
| `Ignis-Operations` | Ignis | Analyse d'incidents, appels d'outils | T3 |
| `Hydro-Atmos` | Hydro | Explication de modèles et scénarios | T3 |
| `GSIE-Data` | GSIE Core | Extraction structurée depuis documents | T3 |

**Le modèle terrain T1 reste un modèle généraliste quantifié**, sans LoRA
spécialisée. Raison : contrainte mémoire (un adaptateur LoRA ajoute
~100-300 MB) et simplicité de mise à jour (un seul fichier modèle à
distribuer via packs de données, §9).

### 15.5 RAG scientifique

Le RAG (Retrieval-Augmented Generation) alimente T2 et T3 avec la
documentation forestière sourcée :

**Sources indexées** (RFC-0019, gsie-ai-gateway) :

- Documentation ONF (guides martelage, sylviculture)
- Documentation CNPF (IBP, gestion)
- Documentation INRAE (recherche forestière)
- Référentiels IGN (BD Forêt, tarifs)
- `GSIE/KNOWLEDGE/` (base de connaissances structurée)
- `GSIE/RESEARCH/` (travaux scientifiques)

**Infrastructure** :

- `pgvector` déjà activé dans PostgreSQL (migration 20260731_0024)
- Routes RFC-0019 : `/ai/embed` (indexation), `/ai/rerank` (re-ranking),
  `/ai/research` (RAG avec citations exactes)
- Garde-fou : PostgreSQL/PostGIS = vérité canonique, le LLM = assistant
  (RFC-0019 §48-54)

**RAG local T1** (offline) :

- Index compact embarqué (SQLite-vec ou FAISS mobile, < 200 MB)
- Sous-ensemble des documents les plus pertinents (guides martelage,
  autécologie essences locales)
- Mise à jour via packs de données (§9), pas en temps réel

### 15.6 Identification essence on-device

L'identification d'essence par photo (RFC-0018, GEO-004) suit deux volets :

**Volet en ligne** (adopté, RFC-0018) :

- Capture photo → file locale → serveur GSIE → Pl@ntNet → normalisation
  TAXREF → décision forestier
- Cycle : `SUGGESTION_IA` → `VALIDEE_UTILISATEUR` ou `REJETEE`
- Principe : identification = `EvidenceStatement` modélisé, jamais
  `observé` (RFC-0018)

**Volet hors ligne** (à l'étude, RFC-0018 § volet hors-ligne) :

- Modèle de reconnaissance à **entraîner ou adapter** à partir de jeux
  de données dont la licence, la couverture taxonomique et la qualité
  devront être **auditées**. Un jeu de données et un modèle Android
  opérationnel sont deux choses différentes — le modèle n'existe pas
  encore comme produit intégrable.
- **Étape obligatoire avant intégration** :
  1. Audit dataset (licence, couverture, qualité, biais)
  2. Audit licence (compatibilité commerciale/AGPL)
  3. Nettoyage taxonomique
  4. Découpage entraînement/validation/test
  5. Benchmark (précision, rappel, F1 par essence)
  6. Conversion mobile (TFLite/ONNX, quantification INT8)
  7. Validation terrain (essences, écorces, appareils, luminosités)
- Classification des essences françaises les plus courantes (~50 espèces
  en première tranche)
- Quantification INT8 pour Android
- Dégradation gracieuse : si le modèle on-device n'est pas confiant
  (score < seuil), proposer l'envoi à Pl@ntNet au retour du réseau
- L'identification on-device reste une `SUGGESTION_IA`, jamais une
  validation automatique

### 15.7 Assistant vocal terrain

L'assistant vocal (T1 on-device) couvre trois cas d'usage :

1. **Saisie vocale de mesures** — « diamètre 32, hauteur 18, essence
   chêne sessile » → transcription (Vosk FR offline) → remplissage
   automatique du formulaire de tige. Confirmation visuelle obligatoire
   avant validation (§6.2 mode hybride).
2. **Explication des calculs** — « pourquoi ce volume ? » → le LLM
   récupère le résultat du calcul local (tarif, coefficients) et
   l'explique en langage naturel avec la source. Ne recalcule pas.
3. **Question contextuelle** — « quelle essence adaptée ici ? » → le LLM
   consulte le cache local (autécologie Botanical Engine) et répond. Si
   le cache est vide ou insuffisant, propose de déléguer à T3 au retour
   du réseau.

**Garde-fous vocaux** :

- L'audio brut est supprimé après transcription si aucun accord spécifique
  n'est donné (§6.2, RGPD).
- Les commandes vocales sensibles (suppression, validation de session)
  exigent une confirmation visuelle explicite.
- Le mode vocal peut être forcé ou désactivé par le technicien (§6.2).

### 15.8 Distribution des modèles

Les modèles LLM et les index RAG sont distribués via les **packs de
données** (§9), pas via le Play Store :

- Pack « Assistant terrain FR » : modèle T1 (profil à définir par RFC
  renouvelable) + index RAG local + modèle d'identification essences.
  **La taille exacte sera mesurée après sélection du modèle** — un
  modèle 3B en INT4 représente ~1,5 GB de poids bruts seuls, avant
  métadonnées et contexte. L'objectif initial de ~500 MB est
  probablement irréaliste pour un modèle 3B et devra être réévalué.
- Pack « Documentation ONF/CNPF » : index RAG complémentaire (~200 MB)
- Chaque pack expose version, date, source, licence, empreinte et
  signature (§9)
- Mises à jour proposées en Wi-Fi, de préférence en charge, réversibles
  et sans perte de projet (§9)

### 15.9 Évaluation et garde-fous

- **Banc d'essai `GSIE-Eval-FR`** (RFC-0019) : tout modèle LLM doit
  passer le banc avant activation opérationnelle. Le banc teste la
  justesse des citations, le refus d'inventer, le respect du format
  d'enveloppe.
- **Tests contractuels ADR-009** : toute sortie LLM doit contenir une
  `source_reference` ou invoquer un moteur. Une sortie sans citation est
  un défaut bloquant détecté par tests statiques.
- **Mode expérimental** : une méthode IA non validée est marquée
  `experimental` et ne peut alimenter une recommandation opérationnelle
  sans consentement (§7.1).
- **Consentement RGPD** : l'assistant vocal exige un consentement
  explicite. L'audio brut est supprimé après transcription sauf accord
  spécifique (§6.2, `docs/RGPD_AUDIT_REPORT.md`).

### 15.10 Profils de modèles — synthèse

> Les noms de modèles précis (SmolLM3, Phi-3-mini, Mistral 7B,
> Phi-4-reasoning, etc.) ne sont **pas** des décisions définitives. Ils
> sont étudiés comme candidats potentiels mais seront obsolètes avant
> l'implémentation. La sélection concrète fait l'objet d'une **RFC
> renouvelable** `RFC-IA-MODEL-SELECTION-YYYY-MM` qui comparera les
> modèles disponibles au moment réel du développement.

| Profil | Taille | Tier | Cas d'usage | Statut |
|---|---|---|---|---|
| `T1-MICRO` | 0,3 à 1,5 B | T1 | Assistant terrain offline léger | Cible P5 (RFC renouvelable) |
| `T1-STANDARD` | 1,5 à 4 B | T1 | Assistant terrain offline | Cible P5 (RFC renouvelable) |
| `T2-EDGE` | 4 à 12 B | T2 | RAG edge, raisonnement intermédiaire | Cible P5 (RFC renouvelable) |
| `T3-SERVER` | choisi par benchmark | T3 | Raisonnement profond serveur | Différé (RFC-0031) |
| Modèle identification essence | à entraîner | T1 | Identification on-device | Étude (RFC-0018, audit dataset requis) |

**Métriques à mesurer** (pas des promesses) : P50, P95, temps de premier
token, tokens/s, RAM maximale, chauffe, consommation, qualité métier.

## 16. QPIS — Quintessences Pack Intelligence System

QPIS est le système de packs de données qui généralise et étend le mécanisme
déjà présent en §9. Le serveur GSIE collecte, normalise, contrôle, découpe,
compresse, signe et distribue les données sous forme de packs versionnés.
L'application ne contacte pas directement des dizaines d'API publiques : elle
consomme des packs préparés. Le gestionnaire local sélectionne les packs selon
le compte, l'abonnement, le workspace, le métier, la mission, le territoire,
l'appareil, la connexion, la batterie, le stockage et la fraîcheur des données.

### 16.1 Finalité

Le serveur prépare les packs (collecte → validation → normalisation →
reprojection → déduplication → enrichissement → découpage territorial →
indexation → compression → signature → publication → surveillance).
L'application les consomme : catalogue, téléchargement, vérification
d'intégrité, installation atomique, mise à jour différentielle, rollback.

### 16.2 Types de packs

| Type | Contenu | Exemples |
|---|---|---|
| **Système** | Taxonomie, unités, méthodes, équations, règles, classifications, protocoles de base, traductions, documentation | Référentiel TAXREF, unités dendrométriques |
| **Géographiques** | Hiérarchie France → région → département → territoire → forêt → mission | Découpage départemental |
| **Cartographiques** | PMTiles, MBTiles, orthophotos, fond topographique, cadastre, DFCI, relief, couches forestières, MNT | Orthophoto IGN départementale |
| **Scientifiques** | Tarifs de cubage, équations, allométrie, biomasse, carbone, station, santé, sylviculture, produits | Tarifs ONF, équations Vallet et al. |
| **Organisationnels** | Protocoles privés, tarifs internes, couches privées, nomenclatures, modèles de rapports, paramètres, missions | Protocole de martelage ONF |
| **IA** | Reconnaissance d'essences, TreeVision, voix, OCR, assistant local, modèle sanitaire | Modèle à entraîner (§15.6) |

> **Distinction critique — packs vs code exécutable** : un pack QPIS
> contient des **données, modèles, règles, protocoles, styles et
> ressources**. Il ne doit **jamais** injecter du code exécutable non
> signé. Trois concepts distincts :
>
> - **Entitlement** : autorise une fonction **déjà présente** dans
>   l'application (ex : « martelage pro » débloqué par abonnement).
> - **Feature module signé** : code livré par le **canal officiel** de
>   l'application (Play Feature Delivery ou équivalent), signé,
>   versionné.
> - **Pack QPIS** : données, modèles, règles, protocoles — jamais de
>   code Kotlin ou natif arbitraire.

### 16.3 Manifeste de pack

Chaque pack expose un manifeste contenant : ID, version sémantique, type,
taille compressée, espace installé, dépendances, compatibilité application,
niveau d'abonnement, territoire, date de publication, expiration, source,
licence, hash, signature, stratégie de mise à jour, criticité et politiques
de suppression.

### 16.4 États

| État | Signification |
|---|---|
| `REQUIRED` | Pack obligatoire, l'app ne fonctionne pas sans lui |
| `RECOMMENDED` | Fortement conseillé pour le métier/mission courant |
| `OPTIONAL` | Disponible au choix de l'utilisateur |
| `DEPRECATED` | Remplacé, maintenu temporairement |
| `REVOKED` | Retiré (sécurité, licence, obsolescence) |
| `ARCHIVED` | Conservé en lecture seule pour historique |

### 16.5 Téléchargement intelligent

Politique adaptative selon la taille et le contexte :

- petits correctifs : téléchargement mobile autorisé ;
- packs moyens : confirmation utilisateur ;
- gros packs : Wi-Fi recommandé ;
- LiDAR/orthophoto : Wi-Fi par défaut ;
- téléchargement différé si batterie faible ;
- préchargement avant mission (§17 Mission Engine) ;
- reprise sur coupure, vérification par blocs.

### 16.6 Storage Budget Manager

Priorité de conservation (du plus critique au moins critique) :

1. données non synchronisées ;
2. mission active ;
3. référentiels essentiels ;
4. cartes de mission ;
5. packs favoris ;
6. archives synchronisées ;
7. orthophotos ;
8. caches reproductibles.

Calcul avant installation : `espace_pack + espace_temporaire + espace_rollback
+ marge - espace_libéré`. Si le budget est insuffisant, l'utilisateur est
invité à libérer de l'espace selon la priorité.

### 16.7 Mise à jour différentielle

- découpage en blocs adressés par hash ;
- réutilisation des blocs identiques (seules les différences sont téléchargées) ;
- installation atomique (le pack n'est actif qu'une fois complet et vérifié) ;
- retour arrière (rollback) en cas d'échec ;
- collecte des anciennes versions après validation.

### 16.8 Lien avec l'existant GeoSylva

Le §9 « Packs de données et départements » actuel est un **sous-ensemble de
QPIS** : il couvre la sélection de départements, l'exposition version/date/
source/licence/empreinte/signature et les mises à jour en Wi-Fi. QPIS ajoute :
la typologie en 7 familles, les manifestes riches, les états de pack, le
téléchargement intelligent adaptatif, le Storage Budget Manager, la mise à
jour différentielle par blocs et l'installation atomique avec rollback.

**Dépendances** : RFC-0004 (QPIS Pack Format, RFC prioritaires du Dev
Pack `12_MODELES_RFC_ADR.md`). ADR : packs signés, Room/SQLCipher conservé
comme base locale métier.

### 16.9 Droits et abonnements

La résolution des droits de packs QPIS s'appuie sur le modèle d'abonnement
décrit en §20.4 (Subscription). Le serveur GSIE expose un
**EntitlementResolver** qui consomme l'abonnement utilisateur, les
appartenances organisationnelles (§20.1) et les politiques de l'organisation
pour déterminer :

- les packs accessibles (par type, territoire, niveau d'abonnement) ;
- les packs `REQUIRED` vs `RECOMMENDED` vs `OPTIONAL` selon le métier (§17.2) ;
- les restrictions d'expiration (délai de grâce hors ligne, §20.6) ;
- les packs organisationnels privés (protocoles, tarifs internes).

**Chaîne logique** :

```text
Subscription (§20.4) + Membership (§20.4) + Organization policy
        ↓
EntitlementResolver (serveur GSIE)
        ↓
Droits de packs QPIS → manifeste accessible au client
        ↓
Cache local GeoSylva (hors ligne, §20.6)
```

L'expiration d'un abonnement **ne supprime pas les données** (§20.6) — elle
peut bloquer le téléchargement de nouveaux packs premium et limiter les
traitements serveur étendus. Un délai de grâce hors ligne permet de continuer
à travailler sur le terrain après une expiration, jusqu'à la reconnexion.

**Dépendances** : RFC-0008 (Subscription and Entitlements). Lien avec §20.4
(Subscription), §20.6 (hors ligne), §17.3 (capabilities).

### 16.10 GSIE comme usine de packs

Le serveur GSIE opère une **chaîne de fabrication** des packs :

```text
Ingestion → validation → harmonisation → transformation
→ découpage territorial → simplification par niveau de zoom
→ génération d'index → compression → signature → publication
→ surveillance
```

Au lieu que GeoSylva appelle directement les services externes (IGN,
cadastre, INPN, BRGM, Copernicus, services météo, services DFCI), GSIE
appelle ces services côté serveur, respecte leurs quotas et leurs
licences, puis produit des packs territoriaux (ex :
`pack-vienne-forestier-2026-08`).

L'application ne contacte principalement que : `api.quintessences`,
`packs.quintessences`, `tiles.quintessences`, `sync.quintessences`.

### 16.11 Quintessences Pack Store commun

À terme, un **Quintessences Pack Store commun** est prévu, plutôt qu'un
stockage indépendant par application. Un pack peut être partagé entre
plusieurs apps :

- Pack taxonomique France → GeoSylva, Flora, Artemis
- Pack relief Vienne → GeoSylva, Ignis, Hydro, Terra
- Pack météorologique régional → GeoSylva, Ignis, Atmos

### 16.12 Intelligence locale de recommandation

GeoSylva propose des recommandations contextuelles de gestion de packs :

**Exemple 1 — Mise à jour recommandée** :

```text
Une mission est prévue demain dans la forêt de Chizé.
Le pack cartographique local n'est plus à jour.
Mise à jour disponible : 74 Mo.
Téléchargement recommandé ce soir en Wi-Fi.
```

**Exemple 2 — Gestion de l'espace** :

```text
Il reste 2,1 Go sur l'appareil.
Le pack LiDAR demande 4,8 Go.
Options proposées :
- version allégée de 850 Mo
- traitement uniquement sur le serveur
- suppression de deux anciennes orthophotographies
```

---

## 17. Mission Engine et Protocol Engine

Le Dev Pack introduit deux moteurs qui transforment GeoSylva d'une app
d'inventaire/martelage vers un poste de travail structuré par métier et par
mission. Le **Mission Engine** décrit une mission de terrain (objectif,
territoire, protocole, participants, packs nécessaires). Le **Protocol
Engine** décrit les formulaires et règles de collecte de façon déclarative et
versionnée, inspiré d'ODK Collect et Open Foris.

### 17.1 Trois dimensions

Ne pas confondre :

- **métier** : fonction habituelle de l'utilisateur (technicien, expert, etc.) ;
- **mission** : tâche actuelle assignée (inventaire, martelage, travaux) ;
- **contexte** : organisme, territoire, protocole, contraintes matérielles.

L'interface s'adapte à l'intersection de ces trois dimensions.

### 17.2 Métiers initiaux

> **Correction v0.9.0** : douze profils métier sont identifiés pour la
> **cible longue durée**. La **première implémentation complète** prend
> en charge le **technicien forestier** (métier 1). Les autres profils
> sont activés **progressivement** sur la même architecture de
> capabilities et de protocoles. Ne pas construire douze interfaces
> métier distinctes dès la v1.

12 profils métier sont identifiés pour la cible longue durée :

| # | Métier | Focus |
|---|---|---|
| 1 | Technicien forestier territorial | Gestion multi-usage, martelage, travaux |
| 2 | Gestionnaire privé | Propriété, rentabilité, simplifié |
| 3 | Expert forestier | Diagnostic, valorisation, expertise |
| 4 | Technicien travaux | Prescription, réception, suivi |
| 5 | Technicien exploitation | Coupe, débardage, transport |
| 6 | Technicien SIG | Couches, topologie, import/export |
| 7 | Technicien DFCI | Risque feu, accès, points d'eau |
| 8 | Chargé biodiversité | Habitats, espèces protégées |
| 9 | Propriétaire | Suivi simplifié de sa forêt |
| 10 | Étudiant | Apprentissage guidé, exercices |
| 11 | Formateur | Distribution de missions, correction |
| 12 | Administrateur | Configuration, sécurité, audit |

### 17.3 Capabilities

Les droits et l'interface sont basés sur des **capacités précises**
(capability-based), pas sur des rôles monolithiques. Exemples :

- `forest.inventory.read` / `forest.inventory.create` / `forest.inventory.validate`
- `forest.marking.execute`
- `forest.valuation.read` / `forest.valuation.modify`
- `forest.protocol.manage`
- `geo.layer.publish` / `geo.export.sensitive`
- `organization.members.manage`

Les capabilities sont résolues localement (cache hors ligne, §20.6) et
rafraîchies à la connexion. L'interface n'affiche que les actions permises.

### 17.4 Mission Engine

Une mission contient : type, objectif, responsable, participants, territoire,
parcelles, protocole, date, couches, matériel, formulaires, règles,
livrables, politique de synchronisation et packs nécessaires. La mission
précharge les packs QPIS requis (§16.5) et configure l'interface selon le
protocole et le métier.

### 17.5 Protocol Engine

Inspiré d'ODK et Open Foris, le protocole est **déclaratif, versionné et
signé**. Il décrit : sections, champs, types, unités, valeurs, obligations,
conditions, répétitions, calculs, contrôles, pièces jointes, géométrie,
règles de validation et rapport attendu. Le protocole est distribué par pack
QPIS (type système ou organisationnel).

### 17.6 Formulaires contextuels

Les champs s'affichent conditionnellement selon le contexte. Exemple : si
l'état sanitaire est « dépérissant », le formulaire affiche déficit foliaire,
branches mortes, symptômes, cause suspectée, photo et indice de confiance. Le
technicien ne voit que les champs nécessaires au moment utile — réduction de
la charge cognitive et de la saisie.

### 17.7 Workflows de validation

```text
Brouillon → Terminé → Contrôlé (auto) → À corriger → Validé
  → Contrôlé par responsable → Verrouillé
```

Chaque transition est tracée (auteur, date, décision). Le verrouillage
empêche toute modification ultérieure sans déverrouillage explicite et tracé.

### 17.8 Tableaux de bord par métier

Chaque métier dispose d'un tableau de bord adapté :

- **Territorial** : tournée, martelage, travaux, échéances, santé, documents, alertes.
- **Travaux** : prescription, entreprise, quantités, risques, avancement, non-conformités, réception, réserves.
- **Exploitation** : lot, produits, qualité, accès, débardage, dépôt, transport, estimation, réception réelle.
- **SIG** : projections, géométries, topologie, couches, relations, import, synchronisation, métadonnées.
- **Étudiant** : explications, protocoles guidés, exercices, comparaison manuel/automatique, contrôle pédagogique.
- **Formateur** : distribution de missions, récupération, correction, annotation, comparaison de groupes, export.

**Lien avec l'existant** : la spec v0.3.0 ne distingue pas les métiers —
l'interface est uniforme. Le Mission/Protocol Engine est **nouveau** et
représente un changement d'approche : l'app s'adapte au profil au lieu
d'exposer toutes les fonctions à tous les utilisateurs.

**Dépendances** : RFC-0005 (Protocol and Form Engine), RFC-0002 (Global
Identity and Workspaces). ADR : règles déclaratives hors UI.

### 17.9 Catalogue de protocoles

Les protocoles proviennent de quatre sources distinctes :

| Source | Description | Exemple |
|---|---|---|
| **Officiels** | Protocoles nationaux ou institutionnels validés | Protocole IGN inventaire forestier national |
| **Organisationnels** | Protocoles privés d'une organisation (pack QPIS type organisationnel) | Protocole de martelage ONF |
| **Pédagogiques** | Protocoles d'apprentissage pour étudiants et formateurs | Exercice d'inventaire lycée forestier |
| **Communautaires validés** | Protocoles soumis par la communauté, revus et validés | Protocole de suivi sanitaire associatif |

Chaque protocole du catalogue expose les métadonnées suivantes : auteur,
organisme, version, licence, territoire, date, compatibilité (version app
minimale), champs, règles, tests de validation et livrables attendus.

Le catalogue est consultable depuis l'app (recherche par métier, territoire,
type de mission) et les protocoles sont installés via QPIS (§16, type système
ou organisationnel). Un protocole ne peut être utilisé en mission qu'après
vérification de sa signature et de sa compatibilité avec la version de
l'application.

**Dépendances** : RFC-0005 (Protocol and Form Engine). Lien avec §16 (QPIS),
§17.5 (distribué par pack).

### 17.10 Exemple de protocole déclaratif (format YAML)

Inspiré d'ODK Collect (application Android open source pour relevés
complexes hors connexion), un protocole est défini dans un format
déclaratif :

```yaml
protocol:
  id: diagnostic_sanitaire_v1
  title: Diagnostic sanitaire

sections:
  - id: identification
    fields:
      - id: species
        type: taxon
        required: true
      - id: health_status
        type: choice
        values:
          - sain
          - affaibli
          - dépérissant
          - mort

  - id: decline
    visible_if:
      field: health_status
      in:
        - affaibli
        - dépérissant
    fields:
      - id: crown_deficit
        type: percentage
      - id: dead_branches
        type: percentage
      - id: symptom_photo
        type: photo
        required: true
```

Ce format permet de créer sans modifier le code : un inventaire
dendrométrique, un diagnostic sanitaire, une réception de plantation, un
relevé IBP, un contrôle DFCI, un suivi de régénération, un relevé de
dégâts de gibier, un protocole pédagogique.

---

## 18. TreeVision — mesure multimodale des arbres

TreeVision est le module de mesure assistée par capteurs qui combine caméra,
profondeur (AR Depth), IMU, GNSS, visées humaines et instruments forestiers
connectés pour produire une observation complète, traçable et assortie d'une
incertitude. Il s'agit de l'évolution du `docs/VOLUME_CALCULATION_NEXT_GEN.md`
vers une chaîne de mesure intégrée et non plus seulement un moteur de calcul.

### 18.1 Vision

TreeVision fusionne plusieurs sources capteurs pour estimer automatiquement
les paramètres dendrométriques d'un arbre, tout en conservant la mesure
instrumentale humaine comme référence et la correction humaine comme
garde-fou (human-in-the-loop).

### 18.2 Données estimées

- diamètre à 1,30 m ;
- hauteur totale et hauteur marchande ;
- inclinaison et rectitude ;
- diamètre à plusieurs hauteurs ;
- défauts visibles ;
- position ;
- volume ;
- qualité de mesure (indice de confiance).

### 18.3 Hiérarchie des sources

```text
Mesure instrumentale directe validée
  > instrument connecté (compas Bluetooth, télémètre)
  > vision multi-angle fiable
  > vision simple
  > estimation algorithmique
  > valeur par défaut
```

Le moteur ne fait **pas une moyenne naïve** : il fusionne selon l'incertitude
de chaque source et conserve toutes les valeurs sources pour traçabilité.

### 18.4 Workflow un arbre

16 étapes : vérification du matériel → viser la base → estimation du sol →
placement du plan 1,30 m → scan en arc → segmentation → ajustement
cercle/ellipse/cylindre → visée de la cime → zoom optique si nécessaire →
position → cohérence → incertitude → confirmation → création de
l'observation → cubage → synchronisation.

### 18.5 Correction humaine

Le technicien peut : modifier le diamètre, déplacer la ligne 1,30 m, corriger
les bords, sélectionner la bonne cime, saisir le compas, indiquer un
obstacle, refaire le scan. **La valeur automatique est conservée** avec la
correction et le motif — la correction n'écrase jamais la mesure initiale
(§3.2 observation contre calcul, principe human-in-the-loop du Dev Pack).

### 18.6 Position améliorée

Lorsque l'utilisateur est immobile : accumulation GNSS, moyenne pondérée,
rejet d'outliers, filtre de Kalman, stabilité IMU, estimation de dispersion.
La position de l'arbre est dérivée de la position du téléphone, de l'azimut
et de la distance. Deux points d'observation permettent une triangulation
relative. Toujours distinguer précision absolue (nationale) et précision
relative (locale à la placette).

### 18.7 Indice de confiance

Facteurs : distance, visibilité, lumière, mouvement, couverture angulaire,
profondeur, cohérence, GNSS, nombre de visées, présence d'obstacles, mesure
de référence. L'indice est affiché au technicien et conservé avec la mesure.

### 18.8 Banc de validation

Pour chaque arbre de référence : diamètre réel, circonférence, hauteur de
référence, position de référence, essence, écorce, pente, lumière, distance,
téléphone, vidéo, profondeur, résultat automatique, correction humaine. Le
banc permet de mesurer la précision du système sur des arbres connus.

### 18.9 Boucle GSIE

```text
Mesure automatique → correction humaine → sync consentie
  → analyse GSIE → amélioration du modèle → nouveau pack TreeVision
```

Les données client restent privées sauf consentement explicite et cadre
défini (consentement et amélioration des modèles, Dev Pack §09).

**Lien avec l'existant** : `VOLUME_CALCULATION_NEXT_GEN.md` décrit déjà la
vision LiDAR et photogrammétrie comme perspectives. TreeVision est
l'**intégration opérationnelle** de cette vision en module de mesure
multimodale. La §15.6 (identification essence on-device) est un sous-ensemble
des capacités TreeVision (volet reconnaissance, pas encore volet mesure).

**Dépendances** : RFC-0007 (TreeVision Measurement Pipeline). Phase 8 du Dev
Pack (prototype). ADR : human-in-the-loop, conservation des valeurs sources.

### 18.10 Modes de mesure

TreeVision propose quatre modes adaptés au contexte terrain :

| Mode | Usage | Statut initial | Vitesse |
|---|---|---|---|
| **Rapide** | Inventaire de reconnaissance, estimation préliminaire | Estimation indicative | Élevée |
| **Précis** | Inventaire assisté, martelage | Mesure assistée **à valider** | Modérée |
| **Calibration** | Étalonnage de l'appareil sur un arbre de référence connu | Production de références | Lente |
| **Placette semi-automatique** | Scan d'une placette entière avec détection automatique des tiges | **R&D expérimentale** | Variable |

> **Avertissement** : tant que TreeVision n'a pas été validé sur
> différentes essences, écorces, appareils, couverts, luminosités,
> terrains plats et pentus, feuillus et résineux, arbres isolés et
> peuplements denses, le mode **Précis** reste une mesure **assistée à
> valider**, pas une mesure professionnelle certifiée. Le mode
> **Placette** reste **expérimental**.

**Seuils de passage** (à valider empiriquement) :

```text
Prototype → expérimental → assistance opérationnelle → mesure professionnelle validée
```

Aucun mode ne peut être utilisé pour des inventaires officiels ou des
données contractuelles tant qu'il n'a pas atteint le statut **mesure
professionnelle validée**.

Le mode détermine le nombre de visées requises, le niveau de détail du
scan, la tolérance d'incertitude acceptée et les champs du formulaire de
saisie (§17.6 formulaires contextuels). Le technicien peut changer de mode
en cours de mission, mais le mode utilisé est **conservé avec chaque
mesure** pour traçabilité.

**Dépendances** : RFC-0007 (TreeVision Measurement Pipeline). Lien avec
§18.7 (indice de confiance), §17.6 (formulaires contextuels).

### 18.11 Philosophie : mesure multimodale coopérative

TreeVision propose une mesure **coopérative** : GeoSylva mesure
automatiquement ce qu'il peut, demande au technicien de viser ou mesurer
ce qui reste ambigu, accepte les instruments forestiers comme références,
détecte les incohérences, améliore la position par immobilisation et
triangulation, puis fusionne toutes les sources avec un niveau
d'incertitude explicite.

C'est bien plus solide qu'une promesse de « mesurer un arbre avec une
photo ». L'expertise du technicien et les capteurs du téléphone
**travaillent ensemble**, au lieu de se concurrencer.

### 18.12 Méthodes de mesure du diamètre

**Méthode A — Profondeur ARCore et largeur apparente** : GeoSylva détecte
les deux bords du tronc à 1,30 m, obtient leur profondeur et reconstruit
leur distance réelle.

```text
Bord gauche 3D → Bord droit 3D → Distance → Diamètre apparent
```

Sensibilités : irrégularités de l'écorce, branches, plantes devant le
tronc, erreurs de profondeur sur les contours, angle de prise de vue.

**Méthode B — Ajustement d'un cylindre (RANSAC)** : reconstruction de
plusieurs points du tronc autour de 1,30 m et ajustement mathématique
d'un cercle, ellipse ou cylindre vertical.

```text
Nuage de points → Suppression aberrants → Coupe 1,30 m → RANSAC
```

La méthode B est plus robuste que la A mais nécessite un scan multi-angle.

### 18.13 Modèle de confiance par mesure

Pour chaque mesure, le système enregistre :

- valeur retenue, valeur automatique, valeur manuelle ;
- méthode, incertitude, niveau de confiance ;
- motif de correction, preuves associées.

**Exemple 1 — Diamètre** :

```text
Diamètre retenu : 47,0 cm
Source : compas forestier
Estimation caméra : 46,4 cm
Écart : 0,6 cm
Confiance globale : très élevée
```

**Exemple 2 — Hauteur** :

```text
Hauteur retenue : 27,8 m
Source : visée base/cime
Détection automatique de cime : rejetée
Motif : chevauchement de houppiers
Confiance : moyenne
```

### 18.14 Contrôles de cohérence

Le système détecte les incohérences et demande confirmation **sans bloquer
automatiquement** :

- 18 cm de diamètre et 44 m de hauteur : combinaison très improbable.
- Diamètre passé de 42 à 67 cm en trois ans : erreur de mesure, mauvais
  arbre ou unité incorrecte probable.
- Largeur apparente du tronc varie de 18 % entre deux angles de prise de
  vue : incohérence géométrique.

### 18.15 Amélioration GNSS par immobilisation

> Les valeurs ci-dessous sont des **exemples pédagogiques**, pas des
> garanties. La précision réelle dépend de l'appareil, des conditions
> de réception et de la géométrie satellitaire.

Lorsque l'utilisateur reste immobile, GeoSylva accumule plusieurs
positions GNSS :

```text
Lecture 1 : ±5,2 m → Lecture 2 : ±4,7 m → ... → Lecture 20 : ±2,4 m
```

Méthodes : moyenne pondérée, rejet des aberrantes, médiane spatiale,
filtre de Kalman, analyse de stabilité, durée minimale, contrôle IMU.

**Interface** :

```text
Affinage de la position…
Précision initiale : ±5,8 m
Précision actuelle : ±2,6 m
Stabilité : 92 %
```

### 18.16 Analyse des constellations GNSS

GeoSylva exploite simultanément : GPS, Galileo, GLONASS, BeiDou,
QZSS/SBAS (selon appareil). Paramètres analysés : nombre de satellites,
élévation, azimut, rapport signal/bruit, fréquence, dispersion,
géométrie satellitaire. En France, Galileo est particulièrement
intéressant avec GPS sur les téléphones multifréquences.

### 18.17 Fusion de position (SpatialEvidence)

Structure de données de fusion :

```kotlin
data class SpatialEvidence(
    val source: SpatialSource,
    val coordinates: Coordinates,
    val horizontalUncertaintyM: Double,
    val timestamp: Instant,
    val confidence: Double
)
```

> **Avertissement** : les valeurs ci-dessous sont des **exemples
> pédagogiques**, pas des capacités garanties. Le moteur doit travailler
> avec des **matrices de covariance**, des **résidus**, la **qualité du
> signal**, une **calibration** et une **validation empirique**. Les
> poids des sources (GNSS, AR, triangulation, contrainte parcellaire)
> doivent être **calculés dynamiquement** ou appris sur des données de
> référence, jamais inscrits comme constantes métier.

**Exemple pédagogique de résultat** (les valeurs réelles seront mesurées) :

```text
Position fusionnée : ±1,9 m absolu, ±0,4 m relatif
Sources :
- Galileo/GPS : 45 %
- trajectoire AR : 30 %
- triangulation visuelle : 20 %
- contrainte parcellaire : 5 %
```

### 18.18 Capture de calibration

Pour améliorer l'algorithme, le mode calibration collecte : diamètre réel
au compas, hauteur instrumentale, distance réelle, photos multi-angles,
position RTK éventuelle, description des conditions.

---

## 19. Métiers, capabilities et adaptation contextuelle

Cette section synthétise le volet « adaptation contextuelle » du Dev Pack
(`06_METIERS_MISSIONS_PROTOCOLS.md`) et l'architecture écosystème
(`02_ARCHITECTURE_GSIE_GEOSYLVA.md`). Elle décrit comment GeoSylva s'insère
dans l'écosystème Quintessences en partageant des objets communs, en
s'adaptant au métier de l'utilisateur et en interagissant avec les autres
applications.

### 19.1 Objets communs Quintessences

Les applications de l'écosystème ne doivent pas dupliquer les objets
fondamentaux. Entités communes recommandées (20) :

```text
Identity, Organization, Workspace, Team, Project, Mission,
Location, Geometry, Territory, Property, ManagementUnit,
Taxon, Habitat, Observation, Measurement, Evidence,
Protocol, Method, Calculation, Document, Asset, Event
```

Les extensions spécialisées (peuplement, tige, martelage, etc.) sont
rattachées à ces objets communs. GeoSylva utilise `ManagementUnit` comme
unité de référence forestière, `Observation`/`Measurement`/`Evidence` pour
les saisies terrain, `Protocol`/`Method`/`Calculation` pour les calculs
(§7 doctrine scientifique).

### 19.1.1 Hiérarchie territoriale — 8 entités distinctes

> **Correction v0.7.0** : la navigation canonique (Projet → Forêt →
> Parcelle → Placette) et le modèle central (`ManagementUnit`) doivent
> être précisés. Une parcelle cadastrale, une parcelle forestière, un
> peuplement et une placette ne doivent **jamais** être fusionnés sous
> un même concept.

| Entité | Définition | Source |
|---|---|---|
| **Property** | Propriété foncière (ensemble de parcelles cadastrales appartenant à un même propriétaire) | Cadastre |
| **Forest** | Forêt (ensemble cohérent sur le plan écologique ou sylvicole, peut chevaucher plusieurs propriétés) | IGN BD Forêt |
| **CadastralParcel** | Parcelle cadastrale (unité juridique de propriété) | Cadastre |
| **ManagementUnit** | Unité de gestion (ensemble de parcelles forestières gérées ensemble sous un même document de gestion, §24) | PSG/RTG |
| **ForestParcel** | Parcelle forestière (subdivision de la management unit pour la gestion opérationnelle) | Gestionnaire |
| **Stand** | Peuplement (unité sylvicole homogène : essence dominante, structure, âge, station) | Diagnostic sylvicole (§22) |
| **SamplingUnit** | Unité d'échantillonnage (regroupe plusieurs placettes pour un protocole statistique) | Protocole (§17) |
| **Plot** | Placette (surface exacte inventoriée sur le terrain, circulaire ou rectangulaire) | Inventaire (§6) |

**Relations** — la base centrale est un **graphe relationnel
territorial**, pas un arbre rigide. Les entités utilisent des relations
**plusieurs-à-plusieurs** lorsque le métier l'exige :

```text
Property
└── possède → CadastralParcel

Forest
└── intersecte → CadastralParcel (tout ou partie)

ManagementUnit
├── couvre → tout ou partie de CadastralParcel
└── organise → ForestParcel

Stand
└── intersecte → une ou plusieurs ForestParcel

SamplingUnit
└── contient → Plot

Plot
└── observe → Stand / ForestParcel / ManagementUnit
```

> **Avertissement** : une forêt peut couvrir tout ou partie de plusieurs
> propriétés et parcelles cadastrales. Elle n'est donc pas nécessairement
> contenue dans une propriété, et une parcelle cadastrale n'est pas
> nécessairement contenue entièrement dans une forêt. De même, un
> peuplement peut chevaucher plusieurs parcelles forestières. Le modèle
> de données doit refléter ces relations N-N — seul le cadastre est
> strictement hiérarchique (Property → CadastralParcel).

### 19.2 Unité territoriale partagée

Une même unité de gestion peut être enrichie par plusieurs modules :

| Module | Apport sur l'unité de gestion |
|---|---|
| **GeoSylva** | Peuplements, inventaires, martelages, travaux |
| **Ignis** | Combustibilité, accès DFCI, points d'eau, scénarios |
| **Artemis** | Dégâts de gibier, passages, pression cynégétique |
| **Flora** | Taxons, habitats, espèces protégées |
| **Terra** | Sol, réserve utile, hydromorphie |
| **Atmos** | Climat, sécheresse, prévisions |
| **Hydro** | Ruissellement, cours d'eau, zones humides |

### 19.3 Deep links interapplications

Navigation entre apps Quintessences via schéma d'URI partagé :

- `quintessences://geosylva/management-unit/{uuid}`
- `quintessences://flora/taxon/{uuid}`
- `quintessences://ignis/risk-zone/{uuid}`
- `quintessences://artemis/observation/{uuid}`

### 19.4 Architecture modulaire recommandée

```text
platform/          — identity, authorization, subscription, packs, sync, audit
forest-core/       — taxonomy, measurement, dendrometry, volume, assortment,
                     valuation, silviculture, health, biodiversity
mission-engine/    — professions, capabilities, protocols, workflows, forms
geo-engine/        — rendering, geometry, offline, geopackage, pmtiles, qgis-interop
treevision/        — capture, detection, geometry, positioning, uncertainty
```

Cette modularité sépare les préoccupations transverses (platform), le cœur
forestier (forest-core), la logique mission/protocole (mission-engine), le
moteur géospatial (geo-engine) et la mesure assistée (treevision).

### 19.5 Moteurs locaux vs serveur vs hybrides

| Type | Moteurs | Rôle |
|---|---|---|
| **Locaux** | Surface terrière, statistiques dendrométriques, cubage courant, contrôles de cohérence, valorisation simple, simulation de prélèvement, rapport terrain | Fonctionnement hors ligne (§3.3) |
| **Serveur** | Télédétection, LiDAR, analyse nationale, comparaison grands volumes, modèles climatiques, IA lourde, génération massive de tuiles, agrégations organisationnelles | Capacité étendue (canal 1, §14) |
| **Hybrides** | Volume, biomasse, carbone, valorisation, règles de qualité, scénarios sylvicoles | Partagent définitions, paramètres et tests |

**Règle de parité** : un calcul effectué localement et le même calcul exécuté
sur le serveur avec la même méthode et les mêmes entrées doivent produire le
même résultat dans la tolérance définie. Les moteurs hybrides partagent les
mêmes définitions, paramètres et jeux de tests.

### 19.6 Distance de débardage sur graphe

La distance de débardage est calculée sur un **graphe de desserte**, pas en
distance euclidienne. Entrées : réseau, portance, pente, obstacles, périodes,
sens de circulation, place de dépôt. Sorties : itinéraire, distance,
difficulté, coût estimé, incertitude. Cette logique alimente le Valuation
Engine (§7) pour estimer les coûts d'exploitation.

**Lien avec l'existant** : la spec v0.3.0 décrit une app autonome sans
référence aux autres modules Quintessences. Cette section introduit la
**dimension écosystème** — objets partagés, deep links, architecture
modulaire et parité des calculs. La distance de débardage sur graphe est
nouvelle (la spec actuelle ne couvre que la valorisation par tarif, pas
l'accessibilité).

**Dépendances** : RFC-0006 (Geo Engine and QField Interoperability),
RFC-0009 (Scientific Method Registry), RFC-0010 (Data Provenance and
Evidence). ADR : UUID global, calculs hybrides avec parité.

### 19.7 Services techniques GSIE

Le serveur GSIE expose les services techniques suivants :
synchronisation, API, stockage, recherche, génération de rapports,
notifications, sauvegardes, audit, partage, administration.

### 19.8 Technologies open source étudiées

> **Avertissement** : ce tableau est une **étude préliminaire**. Chaque
> brique doit faire l'objet d'un **audit juridique** avant intégration,
> notamment pour les licences de données (distinctes des licences
> logicielles), les obligations de redistribution, la compatibilité
> AGPL/commerciale, et les conditions réelles de réutilisation des
> données institutionnelles. Les colonnes « licence logiciel » et
> « licence données » ci-dessous sont **à vérifier** (date de
> vérification à compléter).

| Technologie | Rôle | Licence logiciel | Licence données | Obligations redistribution | Compat AGPL/com. | Usage prévu | Décision juridique | Date vérification |
|---|---|---|---|---|---|---|---|---|
| **Open Foris Collect** | Définition d'inventaires, collecte structurée | MIT | — | Inclure licence | Oui | Inspiration workflow qualité | À auditer | À compléter |
| **Open Foris Arena** | Collecte forestière, campagnes multiannuelles | MIT | — | Inclure licence | Oui | Inspiration campagnes | À auditer | À compléter |
| **ODK Collect** | Relevés complexes hors connexion | Apache 2.0 | — | Inclure NOTICE | Oui | Inspiration protocoles déclaratifs | À auditer | À compléter |
| **QField** | Workflows QGIS sur Android | GPL | — | Source disponible | À étudier (GPL) | Interopérabilité | À auditer | À compléter |
| **SpatiaLite** | Extension géospatiale SQLite (OGC) | MPL | — | Inclure licence | À étudier | Étude (prudence wrappers Android) | À auditer | À compléter |
| **GeoPackage** | Format d'échange géospatial (OGC) | OGC | — | Standard ouvert | Oui | Format d'échange | À auditer | À compléter |
| **DuckDB Spatial** | Moteur analytique secondaire | MIT | — | Inclure licence | Oui | Étude (ne remplace pas Room) | À auditer | À compléter |
| **Orfeo ToolBox** | Télédétection serveur | Apache 2.0 | — | Inclure NOTICE | Oui | Phase 9 (télédétection) | À auditer | À compléter |
| **STAC** | Catalogue d'images satellites | Apache 2.0 | — | Inclure NOTICE | Oui | Catalogue serveur | À auditer | À compléter |
| **Martin** | Tuiles vectorielles PMTiles | MIT | — | Inclure licence | Oui | Serveur de tuiles | À auditer | À compléter |
| **pg_featureserv** | OGC API Features | MIT | — | Inclure licence | Oui | API Features | À auditer | À compléter |
| **JSON Logic** | Inspiration moteur de règles | MIT | — | Inclure licence | Oui | Inspiration (moteur Kotlin dédié) | À auditer | À compléter |
| **ZEN** | Alternative moteur de règles | **À vérifier** | — | **À vérifier** | **À vérifier** | Étude | **À auditer** | À compléter |
| **Meshtastic** | Canal mesh LoRa | MIT | — | Inclure licence | Oui | Canal Mesh (§19.9) | À auditer | À compléter |
| **Données IGN** | BD Forêt, orthophotos, cadastre | — | **À vérifier** (Licence Ouverte v2 ?) | **À vérifier** | **À vérifier** | Packs cartographiques | **À auditer** | À compléter |
| **Données INPN** | Biodiversité | — | **À vérifier** | **À vérifier** | **À vérifier** | Packs naturalistes | **À auditer** | À compléter |
| **Données BRGM** | Géologie | — | **À vérifier** | **À vérifier** | **À vérifier** | Packs pédologiques | **À auditer** | À compléter |
| **Données Copernicus** | Satellite | — | **À vérifier** | **À vérifier** | **À vérifier** | Packs télédétection | **À auditer** | À compléter |
| **Datasets IA (PureForest etc.)** | Entraînement reconnaissance | — | **À vérifier** | **À vérifier** | **À vérifier** | Modèle identification (§15.6) | **À auditer** | À compléter |

### 19.9 Meshtastic — canal Mesh détaillé

Pour des événements courts et prioritaires, GeoSylva utilise la couche
Mesh GSIE :

```text
GeoSylva → Meshtastic/LoRa → relais terrain → passerelle connectée → GSIE Server
```

**Données transmises** : position d'équipe, statut de sécurité, alerte
incendie, accident, besoin d'assistance, observation critique, progression
de mission, météo locale, petite télémétrie.

**Données non transmises** : orthophotographies, bases complètes, longues
vidéos, modèles d'intelligence artificielle.

### 19.10 Décision : moteur cartographique

GeoSylva utilise **MapLibre** comme moteur de rendu mobile rapide,
développe son propre moteur SIG forestier et devient nativement
compatible avec les workflows QGIS/QField, GeoPackage et PostGIS.

**Justification** : plus robuste qu'une dépendance à Mapbox, plus léger
qu'intégrer entièrement QGIS dans Android, et surtout beaucoup plus
adapté à la construction d'un produit forestier professionnel.

### 19.11 Décision : base de données spatiale

**Mise en garde sur SpatiaLite** : son intégration Android mérite une
étude technique prudente (certains wrappers Android sont anciens).

**Approche recommandée** :

- Room/SQLCipher pour les données métier ;
- géométries stockées en WKB ou GeoJSON normalisé ;
- index spatial R-Tree SQLite ;
- bibliothèque géométrique Kotlin/Java pour les opérations locales ;
- GeoPackage comme format d'échange professionnel ;
- PostGIS côté serveur.

Ne pas remplacer immédiatement Room par SpatiaLite sans prototype de
performances, de chiffrement, de migrations et de compatibilité Android.

---

## 20. Identité fédérée et organisations

Le Dev Pack introduit un modèle d'identité fédérée qui dépasse la connexion
Google + compte GeoSylva classique décrite en §4.2. L'objectif est d'avoir
**une seule identité interne Quintessences** utilisée par toutes les
applications, avec Google, les passkeys et les systèmes d'entreprise comme
moyens de connexion fédérés, pas comme des comptes indépendants.

### 20.1 Décision d'architecture

Une seule identité interne Quintessences. Google, passkeys/WebAuthn et
systèmes d'entreprise (Microsoft Entra ID, Google Workspace, Okta, Keycloak
tiers, SAML) sont des moyens fédérés. Le compte personnel reste unique ; les
entreprises, établissements et partenaires sont des organisations ou
workspaces. Un même utilisateur peut être propriétaire dans son entreprise,
intervenant dans un lycée, prestataire dans une collectivité et technicien
dans une organisation cliente.

### 20.2 Composants

- **Keycloak** (auto-hébergé) comme autorité centrale d'identité ;
- **PostgreSQL** pour la persistance ;
- **OpenID Connect** et **OAuth 2.0** ;
- **Authorization Code Flow avec PKCE S256** sur Android ;
- **passkeys/WebAuthn** comme méthode Quintessences principale ;
- **Google** comme fournisseur externe ;
- Microsoft Entra ID, Google Workspace, Okta, Keycloak tiers ou SAML pour
  les entreprises ;
- service d'autorisation métier GSIE séparé.

Keycloak est choisi pour les raisons suivantes : open source, sans coût par
utilisateur, compatible OIDC/OAuth 2.0/SAML, compatible Google comme
fournisseur externe, adapté aux applications Android/web/serveur, prise en
charge des passkeys et WebAuthn, gestion des organisations, rôles et
autorisations, et aucune dépendance durable à Firebase, Auth0 ou Microsoft.
Le coût se limite à l'hébergement, les sauvegardes, le nom de domaine et
éventuellement le service d'envoi des courriels. Pour un démarrage modeste,
Keycloak peut tourner avec PostgreSQL sur un petit serveur — la
responsabilité de maintenir les correctifs de sécurité et les sauvegardes
reste toutefois non négligeable.

### 20.2.1 Méthodes de connexion Quintessences

**Méthode principale** : passkey (WebAuthn). L'utilisateur se connecte avec
empreinte digitale, reconnaissance faciale sécurisée, code de verrouillage
de l'appareil ou clé physique de sécurité. La clé privée reste protégée sur
l'appareil ; le serveur ne conserve pas un secret réutilisable comme un mot
de passe.

**Méthodes secondaires** (récupération) : code temporaire par application
TOTP, seconde passkey, clé de sécurité physique, codes de récupération,
courriel de récupération vérifié.

**Mot de passe** : conservé comme solution de compatibilité pour certains
utilisateurs, mais **non privilégié** — la stratégie par défaut est
passwordless (passkey + récupération).

**Administrateurs** : minimum deux moyens d'authentification enregistrés
(passkey ou clé physique + TOTP ou seconde clé + codes de récupération hors
ligne). Voir §20.11 Sécurité administrative.

### 20.3 Identifiant interne

Chaque utilisateur possède un **UUID Quintessences immuable**. Ne jamais
utiliser comme clé principale : l'adresse électronique, le nom, le Google
`sub` ou l'identifiant Microsoft. Les identités externes sont liées à
l'identité Quintessences (table `ExternalIdentity`).

### 20.4 Modèle

```text
QuintessencesUser    — id UUID, status, createdAt
ExternalIdentity     — provider, providerSubject, verifiedEmail, linkedAt
Organization         — entité juridique (entreprise, établissement, partenaire)
Workspace            — espace de travail dans une organisation
Membership           — lien User ↔ Workspace avec rôle
Role                 — rôle général (admin, membre, gestionnaire)
Capability           — capacité précise (forest.inventory.create, etc.)
Device               — appareil enregistré (fingerprint, Keystore)
Session              — session authentifiée avec expiration
Subscription         — abonnement et droits associés
```

### 20.5 Flux Android

- client public (aucun secret dans l'APK) ;
- Authorization Code + PKCE (S256) ;
- navigateur système ou Custom Tab (pas de WebView embarquée) ;
- App Link vérifié pour le callback ;
- access token court, rotation des refresh tokens ;
- **jetons stockés dans un stockage chiffré** ; la clé de chiffrement non
  exportable est protégée par Android Keystore (Keystore stocke des clés
  cryptographiques, pas les jetons eux-mêmes) ;
- **identité d'appareil** : UUID d'installation + paire de clés générée
  dans Android Keystore + clé publique enregistrée côté GSIE (pas
  d'Android ID hashé, qui n'est pas une identité durable). L'identité
  technique de l'appareil devient cryptographiquement démontrable.
  Éventuellement, attestation d'intégrité séparée ;
- réauthentification pour les opérations sensibles.

**Interdictions** (sécurité Android) :

- ne pas intégrer le formulaire de connexion dans une WebView ;
- ne pas enregistrer de secret client dans l'APK (client public) ;
- ne pas transmettre le mot de passe à GeoSylva ;
- ne pas utiliser le flux implicite ;
- ne pas utiliser le flux « mot de passe direct » (Resource Owner Password
  Credentials) ;
- ne pas stocker les jetons en clair.

PKCE empêche qu'un code d'autorisation intercepté soit transformé en jetons
par une autre application. Keycloak permet d'imposer S256 pour le client
mobile.

Le flux Google : GeoSylva ouvre l'URL Keycloak dans le navigateur système →
l'utilisateur choisit Google → Google authentifie → Keycloak crée ou
retrouve l'identité Quintessences → GeoSylva reçoit des jetons Quintessences
(jamais un jeton Google utilisé directement contre GSIE).

### 20.6 Hors ligne

GeoSylva met en cache : identité minimale, workspace actif, capacités,
missions, droits essentiels et expiration de la politique hors ligne. La
durée hors ligne dépend de l'abonnement, de la sensibilité, de la politique
de l'organisation et du rôle. **L'expiration ne supprime pas les données** —
elle peut limiter la création de nouvelles opérations sensibles jusqu'à
reconnexion.

### 20.7 Séparation identité / autorisation métier

| Domaine | Géré par |
|---|---|
| Identité, sessions, fournisseurs, MFA, rôles généraux | **Keycloak** |
| Accès à une forêt, modification d'une mission, validation d'un inventaire, accès aux tarifs, export sensible, publication d'un pack | **GSIE** |

Cette séparation garantit que l'authentification (Keycloak) et l'autorisation
métier (GSIE) restent indépendantes et évolutives.

### 20.8 Liaison de comptes

**Ne jamais fusionner automatiquement** deux identités sur la seule base d'une
adresse électronique. Procédure : identité externe nouvelle détectée →
adresse déjà connue → demande de reconnexion avec un moyen déjà lié →
confirmation explicite → création de la liaison → journal d'audit.

**Lien avec l'existant** : la §4.2 décrit une connexion Google + compte
GeoSylva classique avec les comptes entreprise « en développement ». Cette
section formalise le modèle cible : Keycloak comme broker, OIDC PKCE, passkeys
comme méthode principale, organisations/workspaces, capabilities. Les comptes
entreprise passent de « en développement » à une architecture définie.

**Dépendances** : RFC-0002 (Global Identity and Workspaces), RFC-0008
(Subscription and Entitlements). ADR : Keycloak comme broker d'identité, UUID
global. La §4.2 est désormais amendée pour pointer vers cette section comme
architecture cible (transition §20.9).

### 20.9 Migration des comptes existants

Les utilisateurs GeoSylva actuels se connectent avec Google directement. La
migration vers Keycloak doit traiter **trois cas** selon l'état de l'identité
existante :

> **Avertissement** : l'UUID Quintessences doit être **généré
> indépendamment**, puis **associé** à Google. Il ne doit **pas** être
> dérivé de Google. Avant la migration, il faut vérifier que : le `sub`
Google est réellement stocké, son audience/client Google est la même, il
est récupérable, une migration serveur existe, et l'utilisateur peut être
rapproché sans fusion dangereuse.

**Cas 1 — `sub` Google existant et vérifié** : migration automatique
contrôlée. Keycloak reconnaît le `sub` Google déjà enregistré dans
`ExternalIdentity` → retrouve ou crée l'identité Quintessences (UUID
indépendant) → associe le `sub` Google à l'UUID → l'utilisateur est
connecté sans action supplémentaire.

**Cas 2 — Adresse vérifiée mais pas de `sub` exploitable** :
reconnexion Google + confirmation. L'utilisateur doit se reconnecter via
Google (à travers Keycloak) pour établir le lien. Une confirmation
explicite est demandée avant l'association.

**Cas 3 — Identité ambiguë** : liaison manuelle sécurisée. L'utilisateur
doit prouver la propriété du compte (email de confirmation, passkey
existante, ou validation administrative) avant que la liaison soit
effectuée. Aucune fusion automatique n'a lieu en cas de doute.

**Procédure post-migration** (quel que soit le cas) :

1. **Premier login post-migration** : l'utilisateur ouvre GeoSylva mise à
   jour → l'app redirige vers Keycloak au lieu de Google directement.
2. **Invitation passkey** : après migration, l'utilisateur est invité à
   enregistrer une passkey comme méthode principale (§20.2.1).
3. **Période de transition** : Google reste disponible comme fournisseur
   fédéré pendant toute la période de transition — l'authentification Google
   via Keycloak est transparente pour l'utilisateur.
4. **Fallback** : si l'utilisateur refuse la passkey, le mot de passe de
   compatibilité (§20.2.1) reste disponible.

Aucune donnée utilisateur n'est perdue : l'UUID Quintessences est généré
indépendamment, associé à l'identité Google, et toutes les données GeoSylva
(forêts, inventaires, martelages) sont associées via l'UUID.

### 20.10 Connexion entreprise

Le « compte entreprise » n'est pas un nouveau compte personnel — c'est une
**organisation** à laquelle l'utilisateur appartient (§20.1). Un même
utilisateur peut appartenir à plusieurs organisations avec des rôles
différents (propriétaire dans son entreprise, intervenant dans un lycée,
prestataire dans une collectivité).

**Petite entreprise sans SSO** : les utilisateurs se connectent avec Google,
une passkey Quintessences ou leur adresse professionnelle. Le responsable
les invite dans l'organisation.

**Grande entreprise avec système d'identité** : l'utilisateur choisit « Se
connecter avec mon organisation », saisit son adresse professionnelle
(`prenom.nom@entreprise.fr`). Le système détecte le domaine et redirige
automatiquement vers le fournisseur de l'entreprise (Microsoft Entra ID,
Google Workspace, Okta, Keycloak tiers, SAML). L'authentification et la MFA
de l'entreprise sont respectées. Keycloak agit comme courtier d'identité et
délivre ensuite une identité uniforme Quintessences.

Une organisation contient : ses membres, ses équipes, ses licences, ses
abonnements, ses données, ses packs, ses protocoles, ses rôles et ses
politiques de sécurité.

### 20.11 Sécurité administrative

Pour les administrateurs Quintessences :

- passkey ou clé physique de sécurité **obligatoire** ;
- second facteur de secours (TOTP ou seconde clé) ;
- codes de récupération conservés hors ligne ;
- durée de session **réduite** par rapport à un utilisateur standard ;
- journal de connexion (toutes les connexions administrateur tracées) ;
- révocation des appareils à distance ;
- validation renforcée pour les actions critiques (suppression de données,
  modification des droits, publication de packs).

### 20.12 Gestion des jetons

| Jeton | Durée | Rotation |
|---|---|---|
| Access token | 5 à 10 minutes | Court, à rotation fréquente |
| Refresh token | Plusieurs jours/semaines selon le risque | Rotation à chaque utilisation |
| Session normale | Jours ou semaines selon le risque | — |
| Session administrateur | Plus courte, réauthentification renforcée | — |

Les jetons sont stockés dans un **stockage chiffré** ; la clé de chifflement non exportable est protégée par Android Keystore. L'API
GSIE vérifie systématiquement : la signature, l'émetteur (`iss`), l'audience
(`aud`), l'expiration (`exp`), l'identifiant de session, les rôles ou
permissions, et l'organisation active.

### 20.13 Droits basés sur les capacités

Le système ne se limite pas à « administrateur » et « utilisateur ». Il
gère des **capacités précises** :

| Capacité | Description |
|---|---|
| `forest.inventory.read` | Lecture des inventaires |
| `forest.inventory.create` | Création d'inventaires |
| `forest.inventory.validate` | Validation d'inventaires |
| `forest.marking.execute` | Exécution du martelage |
| `forest.valuation.read` | Lecture des valorisations |
| `forest.valuation.modify` | Modification des valorisations |
| `forest.protocol.manage` | Gestion des protocoles |
| `geo.layer.publish` | Publication de couches |
| `geo.export.sensitive` | Export de données sensibles |
| `organization.members.manage` | Gestion des membres |

Un technicien peut réaliser une saisie sans forcément modifier le tarif
de cubage, changer les prix, supprimer une campagne, publier des données
ou accéder à toutes les propriétés. Cette granularité est indispensable
pour les organismes professionnels.

### 20.14 Alternatives d'identité rejetées

**Firebase Authentication** : très simple au départ, mais moins naturel
pour les organisations complexes, le SAML/OIDC par client, la souveraineté,
le hors-ligne professionnel et la maîtrise de l'ensemble Quintessences.
Pourrait convenir pour un prototype, mais pas comme fondation définitive.

**Auth0, Clerk et services similaires** : très rapides à intégrer, mais le
coût augmente avec les utilisateurs actifs, le SSO d'entreprise, les
organisations, le MFA et les connexions personnalisées. Pratiques mais
moins cohérents avec l'objectif de maîtrise, d'auto-hébergement et de
réduction des coûts.

### 20.15 SCIM — Provisionnement automatique

Le provisionnement automatique via SCIM (System for Cross-domain Identity
Management) sera adopté lorsque le support Keycloak et les besoins seront
suffisamment mûrs (Phase 3 — Entreprises). SCIM permet la synchronisation
automatique des utilisateurs depuis les systèmes d'entreprise (Microsoft
Entra ID, Google Workspace) vers Keycloak.

### 20.16 Déploiement progressif — 4 phases

| Phase | Objet | Fonctionnalités |
|---|---|---|
| **1 — Lancement économique** | Démarrage à coût minimal | Keycloak, PostgreSQL, Google OIDC, passkeys, compte Quintessences, espaces personnels et organisations, rôles simples, Authorization Code + PKCE |
| **2 — Professionnels** | Équipes professionnelles | Invitations, équipes, abonnement par organisation, MFA obligatoire pour les responsables, politique hors ligne, journal des connexions, révocation des appareils |
| **3 — Entreprises** | Intégration SSO d'entreprise | Microsoft Entra ID, Google Workspace, SAML/OIDC par organisation, détection du domaine, administration déléguée, provisionnement automatique, SCIM |
| **4 — Institutions sensibles** | Sécurité renforcée | Clés matérielles, authentification renforcée, appareil géré, restrictions d'export, audit complet, haute disponibilité du service d'identité |

### 20.17 Architecture d'identité recommandée

```text
auth.quintessences.fr
        │
        ▼
Keycloak
├── Google OIDC
├── Passkeys Quintessences
├── TOTP et récupération
├── Microsoft Entra ID
├── Google Workspace
├── SAML entreprise
└── OIDC entreprise
        │
        ▼
Identité Quintessences unique
├── espace personnel
├── organisations
├── équipes
├── rôles généraux
└── abonnements
        │
        ▼
GSIE Authorization Service
├── capacités métier
├── accès aux territoires
├── missions
├── packs
├── licences
└── politiques hors ligne
        │
        ▼
GeoSylva · Ignis · Artemis · Flora · Terra · Hydro · Atmos
```

**Justification du choix Keycloak** : la combinaison la plus efficace,
sécurisée et économique pour Quintessences. Elle permet de commencer à
faible coût sans enfermer GeoSylva dans une solution limitée, tout en
étant déjà compatible avec les futurs besoins des collectivités,
établissements forestiers et grandes organisations.

---

## 21. Diagnostic de station

GeoSylva doit couvrir le diagnostic de station forestière — un domaine
absent de la spec v0.3.0 mais essentiel au raisonnement sylvicole. Cette
section synthétise le domaine 2 de la conversation ChatGPT (Dev Pack).

### 21.1 Sous-thèmes à couvrir

- géologie ;
- pédologie ;
- profondeur et réserve utile du sol ;
- hydromorphie ;
- texture ;
- humus ;
- topographie ;
- exposition ;
- pente ;
- climat ;
- indices bioclimatiques ;
- végétation indicatrice ;
- habitats ;
- stations forestières ;
- contraintes d'exploitation ;
- sensibilité aux sécheresses et au tassement.

### 21.2 Synthèse automatique

L'application produit une synthèse automatique de la station, par exemple :

> Station à réserve utile moyenne, exposition sud-ouest, forte sensibilité
> au déficit hydrique, adaptation future du hêtre incertaine,
> diversification recommandée.

### 21.3 Principe d'explicabilité

La synthèse reste **explicable** : données utilisées, niveau de confiance,
date et origine des références. Le technicien peut consulter le détail des
sources et contester la conclusion.

**Lien avec l'existant** : §7.4 (pathogènes et parcelles voisines) traite
déjà le contexte de risque géographique. Le diagnostic de station
généralise cette approche à l'ensemble du contexte stationnel. Les
données pédologiques et climatiques proviennent des moteurs GSIE Pedology
et Climate (§14 canal 1).

**Dépendances** : RFC-0009 (Scientific Method Registry), moteurs GSIE
Pedology et Climate. Lien avec §22 (scénarios sylvicoles — l'adéquation
essence/station est un critère de comparaison).

---

## 22. Scénarios sylvicoles

GeoSylva doit pouvoir comparer plusieurs scénarios sylvicoles pour aider
le technicien à raisonner les actions de gestion dans l'espace et dans le
temps. Cette section synthétise le domaine 4 de la conversation ChatGPT.

### 22.1 Analyse du peuplement

L'application aide à comprendre :

- la structure du peuplement ;
- son stade de développement ;
- sa stabilité ;
- son niveau de concurrence ;
- sa capacité de régénération ;
- la qualité des tiges ;
- les défauts ;
- la présence d'arbres habitats ;
- les risques sanitaires ;
- l'adéquation entre essence et station (§21) ;
- les trajectoires sylvicoles possibles.

### 22.2 Scénarios comparables

| Scénario | Description |
|---|---|
| **Aucune intervention** | Laisser évoluer sans action |
| **Éclaircie faible** | Prélèvement modéré pour réduire la concurrence |
| **Éclaircie forte** | Prélèvement marqué pour favoriser les arbres objectifs |
| **Conversion vers l'irrégulier** | Transformation progressive vers une structure irrégulière |
| **Renouvellement progressif** | Régénération par coupes progressives |
| **Plantation** | Reconstitution par plantation |
| **Enrichissement** | Introduction d'essences complémentaires |
| **Diversification** | Introduction d'essences adaptées au changement climatique |
| **Mise en libre évolution** | Conservation sans intervention active |

### 22.3 Comparaison

Chaque scénario est évalué sur trois dimensions : **économique**
(valorisation §7.9), **sylvicole** (structure, stabilité, régénération)
et **écologique** (biodiversité, adaptation climatique, adéquation
station §21). Le moteur `ScenarioComparisonEngine` (§7.7) produit un
tableau comparatif et un compte rendu explicable.

**Dépendances** : RFC-0001 (Forestry Scientific Core). Lien avec §7.7
(moteurs silviculture/), §21 (diagnostic de station).

---

## 23. Organisation des travaux forestiers

GeoSylva doit couvrir l'organisation et le suivi des travaux forestiers.
Cette section synthétise le domaine 7 de la conversation ChatGPT.

### 23.1 Types de travaux couverts

- plantation ;
- préparation du sol ;
- dégagement ;
- dépressage ;
- nettoiement ;
- taille ;
- élagage ;
- protection contre le gibier ;
- entretien des cloisonnements ;
- entretien des dessertes ;
- restauration des milieux ;
- travaux de prévention incendie.

### 23.2 Gestion de chantier

Pour chaque chantier, GeoSylva gère :

| Étape | Données |
|---|---|
| **Prescription** | type de travail, localisation, quantités, coût prévisionnel |
| **Organisation** | entreprise, calendrier, risques, consignes |
| **Suivi** | photos avant et après, avancement |
| **Contrôle** | contrôle de conformité, réception, réserves |
| **Clôture** | facture, historique |

Le conducteur de travaux forestiers pilote les chantiers depuis leur
planification jusqu'à leur réalisation et leur livraison. GeoSylva assure
la **continuité entre le technicien qui prescrit et les personnes qui
exécutent**.

**Dépendances** : RFC-0005 (Protocol and Form Engine). Lien avec §17
(Mission Engine — un chantier est une mission), §16 (QPIS — protocoles
organisationnels).

---

## 24. Documents de gestion durable

GeoSylva doit aider à élaborer et suivre les documents de gestion
durable. Cette section synthétise le domaine 8 de la conversation ChatGPT.

### 24.1 Types de documents

- plans simples de gestion (PSG) ;
- règlements types de gestion (RTG) ;
- codes de bonnes pratiques ;
- aménagements forestiers ;
- programmes de coupes ;
- programmes de travaux ;
- bilans périodiques ;
- avenants ;
- cartes réglementaires.

### 24.2 Contrôles automatiques

GeoSylva contrôle automatiquement :

- les interventions en retard ;
- les coupes non réalisées ;
- les écarts par rapport au document ;
- les parcelles sans diagnostic récent ;
- les incompatibilités entre programme et contraintes environnementales ;
- les conséquences d'un changement de scénario (§22).

### 24.3 Rédaction

La rédaction d'un document de gestion implique un diagnostic (§21), des
objectifs, des choix sylvicoles (§22) et une programmation pluriannuelle.
Les gestionnaires privés peuvent également assurer martelage, vente,
suivi de coupe, maîtrise d'œuvre, diagnostic et rédaction des documents
de gestion.

**Dépendances** : Lien avec §21 (diagnostic de station), §22 (scénarios
sylvicoles), §23 (travaux forestiers), §7.9 (valorisation économique).

---

## 25. Références locales de marché

GeoSylva archive les **prix réellement obtenus** lors des ventes de bois
afin de constituer progressivement des **références locales de marché**.

### 25.1 Principe

Chaque vente conclue (sur pied ou bord de route) enregistre : essence,
qualité, produit, volume, prix unitaire, date, lieu, conditions de vente,
acheteur (anonymisé). Ces données alimentent le `PriceCatalogResolver`
(§7.7) pour affiner les estimations futures.

### 25.2 Confidentialité

Les prix individuels sont **privés** (données du client, §20.13). Les
références agrégées (prix moyen par essence/qualité/territoire/mois)
peuvent être partagées anonymisées (catégorie de consentement §10) pour
alimenter la communauté et la recherche.

**Dépendances** : Lien avec §7.9 (chaîne de valorisation),
§7.7 (`PriceCatalogResolver`), §10 (confidentialité).

---

## 26. Références

### 21.1 Documents GeoSylva

- [MASTER_PLAN] `MASTER_PLAN.md`, vision, programme DENDRO-EXCELLENCE et plan
  technique GeoSylva.
- [VOLUME-NEXT-GEN] `docs/VOLUME_CALCULATION_NEXT_GEN.md`, architecture du
  moteur de volume nouvelle génération (LiDAR, IA on-device, multi-tier LLM).
- [RESEARCH-OPP] `RESEARCH_OPPORTUNITIES.md`, 150+ opportunités techniques,
  stack IA séquencée (§3), modèles on-device, datasets forestiers.
- [AUDIT-FORESTIER] `AUDIT_FORESTIER_COMPLET.md`, audit vague 1 (DB, calculs,
  tarifs, logique forestière).
- [AUDIT-GLOBAL] `AUDIT_GLOBAL_GEOSYLVA.md`, audit vague 2 (sécurité, GIS, UI,
  i18n, build, RGPD, performance).
- [AUDIT-UI] `AUDIT_UI_UX_GLOBAL.md`, état des écarts d'interface connu.
- [RGPD] `docs/RGPD_AUDIT_REPORT.md`, audit conformité RGPD.
- [REFERENTIELS] `docs/REFERENTIELS_FORESTIERS_EXTERNES.md`, sources
  officielles tarifs, prix, IBP, GRECO, APIs externes.
- [CODE-PLACETTES] `app/src/main/java/com/forestry/counter/presentation/screens/PlacettesScreen.kt`.
- [CODE-ESSENCES] `app/src/main/java/com/forestry/counter/presentation/screens/EssenceDiamScreen.kt`.
- [CODE-MARTELAGE] `app/src/main/java/com/forestry/counter/presentation/screens/MartelageScreen.kt`.
- [CALCULS] `app/src/main/java/com/forestry/counter/domain/calculation/MartelageModels.kt`.
- [DEV-PACK] `21_EXPERIMENTS/GEOSYLVA_DEV_PACK_2026-08-04/`, brainstorming ChatGPT (13 documents + DOCX maître), vision produit long terme GeoSylva-Quintessences.

### 21.2 Documents GSIE

- [RFC-0003] `02_RFC/RFC-0003.md`, architecture distribuée GSIE-Net,
  offline-first, intelligence distribuée, synchronisation orientée données.
- [RFC-0018] `02_RFC/RFC-0018-identification-botanique-plantnet.md`,
  identification botanique assistée Pl@ntNet (volet en ligne adopté).
- [RFC-0019] `02_RFC/RFC-0019-gsie-ai-gateway-nvidia-nim.md`, couche IA
  serveur transverse, RAG scientifique, routes `/ai/embed`, `/ai/rerank`,
  `/ai/research`.
- [RFC-0031] `02_RFC/RFC-0031-feuille-de-route-post-veille-2026-08-02.md`,
  feuille de route post-veille (NeuralProphet, vLLM différé).
- [VISION-LLM] `GSIE/RESEARCH/VISION_LLM_SPECIALISES_GSIE_CORE_2026-07-20.md`,
  adaptateurs LoRA spécialisés, famille de modèles, principe "LLM appelle
  moteurs".
- [ENGINE-CONTRACTS] `GSIE/ARCHITECTURE/ENGINE_INTERFACE_CONTRACTS.md`,
  contrats d'interface des 14 moteurs, matrice d'interactions.
- [ADR-009] `GSIE/ARCHITECTURE/ADR-009-garde-fou-anti-invention.md`,
  garde-fou anti-invention de données.
- [MOTEURS] `GSIE/ENGINES/*/`, contrats d'interface détaillés par moteur
  (Correlation, Reasoning, Diagnostic, Recommendation, Forest Dynamics,
  Simulation, Botanical, Learning, GIS, Climate, Pedology).
- [GEO-001] `05_SPECIFICATIONS/GEOSYLVA/GEO_001_SPECIFICATION.md`,
  spécification fonctionnelle GeoSylva (Phase 3 Connaissance).
- [GEO-002] `05_SPECIFICATIONS/GEOSYLVA/GEO_002_NON_FUNCTIONAL.md`,
  spécification non fonctionnelle (performance, offline-first, résilience).
- [GEO-004] `05_SPECIFICATIONS/GEOSYLVA/GEO_004_IDENTIFICATION_BOTANIQUE_PLANTNET.md`,
  spécification identification botanique assistée.

### 21.3 Référentiels externes

- [ONF] Office national des forêts, référentiels et méthodes sylvicoles :
  <https://www.onf.fr/>.
- [IGN-CARTO] IGN, API et services cartographiques :
  <https://geoservices.ign.fr/documentation/services/api-et-services-ogc/api-carto-rest>.
- [IGN-BDFORET] IGN, BD Forêt : <https://foret.ign.fr/IGD/fr/ressources>.

## 27. Historique

| Version | Date | Modification |
|---|---|---|
| 0.1.0 | 2026-08-03 | Création de la liste fonctionnelle et de la doctrine scientifique issue du brainstorming validé. |
| 0.2.0 | 2026-08-03 | Roadmap structurée (§12) : architecture cible, cascade LLM multi-tier, connexion GSIE Serveur (moteurs et contrats), 8 phases, décisions/RFC requises, critères de sortie. Sources consolidées (§16). |
| 0.3.0 | 2026-08-03 | §14 Connexion GSIE Serveur détaillée (enveloppes communes, moteurs, chaîne d'appel, cache, pull/conflits, SDK Kotlin, garde-fous). §15 LLM on-device et multi-tier (architecture 3 tiers, cascade, LoRA, RAG, identification on-device, assistant vocal, distribution, évaluation). |
| 0.4.0 | 2026-08-04 | Intégration du Dev Pack (brainstorming ChatGPT) : §16 QPIS, §17 Mission/Protocol Engine, §18 TreeVision, §19 Métiers/objets communs/architecture modulaire, §20 Identité fédérée Keycloak/OIDC. Vision long terme GeoSylva comme poste de travail numérique complet du technicien forestier. |
| 0.5.0 | 2026-08-04 | Vérification et complétion de l'intégration Dev Pack : §4.2 amendé (pointe vers §20 cible), §16.9 Droits et abonnements (Subscription ↔ QPIS), §17.9 Catalogue de protocoles, §18.10 Modes TreeVision, §20.2.1 Méthodes connexion Quintessences (passkey/TOTP/mot de passe compatibilité), §20.5 Interdictions Android, §20.9 Migration comptes existants, §20.10 Connexion entreprise (petite/grande structure), §20.11 Sécurité administrative, §20.12 Gestion des jetons. |
| 0.6.0 | 2026-08-04 | Intégration complète de la conversation ChatGPT source : 23 recommandations. §7 enrichi (7 sous-sections : qualité données, campagnes multiannuelles, architecture moteurs, règles déclaratives, valorisation, versionnement, IA vs déterministe). §16 enrichi (usine packs, Pack Store commun, intelligence locale). §17 enrichi (exemple protocole ODK YAML). §18 enrichi (8 sous-sections : philosophie coopérative, méthodes A/B RANSAC, modèle confiance, contrôles cohérence, GNSS immobilisation, constellations, SpatialEvidence, calibration). §19 enrichi (services techniques, technologies open source, Meshtastic détaillé, décisions MapLibre/Room). §20 enrichi (capacités, alternatives rejetées, SCIM, 4 phases déploiement, architecture finale). Nouvelles sections : §21 Diagnostic de station, §22 Scénarios sylvicoles, §23 Travaux forestiers, §24 Documents de gestion durable, §25 Références locales de marché. |
| 0.7.0 | 2026-08-04 | **Cadrage** suite à la revue critique du Fondateur. 10 corrections critiques : (1) avertissement monolithique + §28 RFC à extraire, (2) modèles IA remplacés par profils T1-MICRO/STANDARD/T2-EDGE/T3-SERVER + RFC renouvelable, (3) PureForest TFLite reformulé (modèle à entraîner + audit dataset), (4) TreeVision précision remplacée par statut initial + seuils de passage, (5) GNSS exemples → objectifs (covariance, poids dynamiques), (6) migration Google→Keycloak 3 cas + UUID indépendant, (7) identifiant appareil UUID + Keystore, (8) jetons stockage chiffré + clé Keystore, (9) séparation Entitlement / Feature module / Pack QPIS, (10) tableau licences enrichi (8 colonnes). Roadmap refondue : 11 lots (0-10). Structure territoriale définie (8 entités). Corrections de forme. |
| 0.8.0 | 2026-08-04 | **Section §29 — Architecture des écrans, navigation et refonte UI/UX**. Audit des 27 écrans existants (5 NavGraphs) : classification en 3 catégories (conservés/enrichis, transformés, nouveaux). Décisions de cadrage : bottom nav 5 entrées remplace démarrage direct sur Forets, écran Martelage devient SynthèseMartelage (saisie dans nouvel écran terrain), Carte refonte complète (3ème entrée bottom nav), Settings supprimé (tout dans Compte 16 sections), diagnostics (stationnel/ripisylve/IBP) déplacés en onglets fiche parcelle + protocoles Mission Engine. 31 sous-sections : navigation, splash, onboarding, connexion, workspace, accueil, projets, forêt, parcelle, placette, saisie tige, martelage/synthèse, carte, missions, données, compte, QPIS, sync, conflits, centre scientifique, analyse GSIE, diagnostics, TreeVision, travaux, documents, design system, audit préalable, roadmap UI transversale, critères acceptation. Roadmap §12.4 enrichie : colonne « Pages UI » par lot + Quality Pass final. |
| 0.9.0 | 2026-08-04 | **Candidate for Review** — 11 corrections structurantes du Fondateur. (1) Hiérarchie territoriale : ⊇ remplacés par relations nommées (graphe relationnel N-N, pas arbre rigide). (2) Navigation contextuelle : clarifiée comme vue utilisateur, pas propriété des données. (3) Surcharge d'onglets : fiche parcelle 13 onglets → 5 groupes (Aperçu/Terrain/Interventions/Analyse/Plus) avec sous-navigation ; fiche placette 11 onglets → 5 groupes. (4) Bottom nav : « Données » → « Explorer » (variante B) + variantes A/B à tester. (5) Compte : 16 sections regroupées en 4 groupes visuels (Identité/Offre/Application/Confidentialité). (6) Permissions onboarding : pas toutes dès le départ, au premier usage de chaque fonction. (7) Fond vidéo connexion : ressource APK légère par défaut + pack signé facultatif. (8) Splash : séparation bloquant/non-bloquant, démarrage rapide. (9) Contradiction roadmap : ancien P7 « Reporté » → « Obsolète, refonte UI transversale ». (10) Contradiction 12 métiers : « 12 métiers v1 » → « 12 profils cible longue, 1 métier v1 ». (11) Diagnostics : onglets fixes → cartes conditionnelles (protocoles installés, territoire, métier, abonnement, mission, données). |
| 0.9.1 | 2026-08-04 | **Nettoyage** — 8 corrections résiduelles. (1) §3.1 : « parent » unique → relations structurelles/contextuelles/workspace (pas de parentId universel). (2) Mentions résiduelles « Données » → « Explorer » (8 occurrences). (3) §29.1 tableau cadrage actualisé (Explorer, cartes conditionnelles). (4) « 16 sections » → « 4 domaines, 16 destinations secondaires ». (5) §12.8 : P0-P7 « plan d'exécution immédiat » → « archivées pour traçabilité, lots 0-10 = plan actuel ». (6) §12.5 déclencheurs P0/P2/P3/P4/P5/P6 → Lot 0/3/2/5/9/10. (7) Avertissement routes altérées (Devin doit relire le code). (8) §14 et §15 marqués NON NORMATIF (endpoints, tables, JSON, WorkManager = exemples de cadrage, pas contrats). |

## 28. Annexe — RFC à extraire

> Le document GeoSylva-003 v0.7.0 est la **spécification produit
> maîtresse**. Les détails techniques (modèles de données précis,
> endpoints API, schémas SQL, bibliothèques) doivent être extraits vers
> des **RFC indépendantes**. Cette annexe liste les RFC à créer ou
> enrichir.

| RFC | Objet | Section source |
|---|---|---|
| **RFC-0001** | Scientific Forest Core (cubage, dendrométrie, Method Registry) | §7, §7.7-§7.11 |
| **RFC-0002** | Identity and Organizations (Keycloak, OIDC, capacités) | §20 |
| **RFC-0003** | Synchronization Protocol (journal, push/pull, conflits) | §8, §14 |
| **RFC-0004** | QPIS Pack Format (manifeste, signature, installation atomique) | §9, §16 |
| **RFC-0005** | Mission and Protocol Engine (protocoles déclaratifs, métiers) | §17 |
| **RFC-0006** | Geo Engine (MapLibre, PMTiles, GeoPackage, QGIS/QField) | §19.10-§19.11 |
| **RFC-0007** | TreeVision Measurement Pipeline | §18 |
| **RFC-0008** | Subscription and Entitlements (abonnements, entitlements) | §16.9 |
| **RFC-IA-MODEL-SELECTION-YYYY-MM** | Sélection modèles IA (renouvelable) | §15 |
| **RFC-0018** | Identification essence on-device (audit dataset, entraînement) | §15.6 |
| **RFC-0019** | gsie-ai-gateway serveur | §15, §14 |
| **RFC-UI-001** | Architecture écrans, navigation et design system | §29 |

---

## 29. Architecture des écrans, navigation et refonte UI/UX GeoSylva 3.0

> Cette section spécifie précisément l'organisation des écrans, la
> navigation, les parcours métier et la refonte UI/UX. Elle est
> directement exploitable par Devin. La refonte UI/UX n'est pas une phase
> finale isolée : **elle accompagne chaque lot fonctionnel** (§29.30).

### 29.1 Décisions de cadrage

| Décision | Choix | Justification |
|---|---|---|
| Navigation principale | **Bottom nav 5 entrées** (Accueil, Missions, Carte, Explorer, Compte) remplace le démarrage direct sur Forets. Nom « Explorer » à confirmer par tests utilisateurs (variante A : Données, variante B : Explorer, §29.2) | Le démarrage actuel sur Forets est trop limité pour un produit multi-métier |
| Écrans existants | **Conservés** (enrichis avec features) sauf exceptions ci-dessous | La base existante est solide |
| Écran Martelage actuel | **Transformé en SynthèseMartelage** — s'ouvre automatiquement après le martelage, plus écran de saisie | La saisie passe dans un nouvel écran terrain ergonomique |
| Carte | **Refonte complète** — devient 3ème entrée bottom nav (carte globale workspace) + ancien Map par parcelle conservé depuis les fiches | La carte est un outil transversal, pas lié à une parcelle |
| Paramètres | **Refonte complète** — écran Settings supprimé, tout passe dans Compte organisé en **4 domaines** contenant 16 destinations secondaires (§29.23) | Regroupe identité, organisation, application, terrain |
| Diagnostic stationnel | **Refonte profonde** — devient **carte conditionnelle** dans la section Analyse de la fiche parcelle + protocole Mission Engine | Plus de navigation séparée ; affichage selon protocoles installés, territoire, métier |
| Ripisylve | **Bouge** — devient **carte conditionnelle** (uniquement si zone ripicole ou protocole installé) + protocole Mission Engine | Diagnostic spécialisé intégré au flux, pas permanent |
| IBP | **Bouge** — devient **carte conditionnelle** (uniquement si protocole IBP installé) + protocole Mission Engine | Module biodiversité intégré, plus de navGraph séparé |

### 29.2 Navigation principale

GeoSylva utilise une navigation principale stable contenant cinq
destinations :

```text
Accueil    Missions    Carte    Explorer    Compte
```

> **Hypothèse à tester** : la 4ème entrée s'appelle « Explorer » plutôt
> que « Données », car elle contient des objets métier (projets, forêts,
> arbres, missions, travaux, calculs, packs, méthodes) et pas seulement
> des tables. Deux variantes doivent être testées avec des techniciens,
> étudiants et gestionnaires avant fixation définitive :
>
> - **Variante A** : Accueil / Missions / Carte / Données / Compte
> - **Variante B** : Accueil / Missions / Carte / Explorer / Compte
>
> La variante B est privilégiée par défaut dans cette spécification.

Ces cinq entrées sont disponibles dans une **barre de navigation
inférieure** sur téléphone. Sur tablette ou grand écran, elles sont
affichées dans un **panneau latéral**.

La destination active est conservée lorsque l'utilisateur revient dans
l'application. L'ancien démarrage direct sur `Forets` est supprimé :
`Forets` devient une sous-page accessible depuis `Accueil` et `Explorer`.

### 29.3 Navigation contextuelle

Lorsqu'un utilisateur ouvre un projet, une forêt, une parcelle, une
placette ou une mission, une navigation contextuelle remplace
temporairement certaines actions générales.

Hiérarchie canonique de **navigation** (vue utilisateur) :

```text
Accueil
└── Workspace
    └── Projet
        └── Forêt
            └── Parcelle forestière
                ├── Peuplement
                ├── Placette
                │   └── Tige
                ├── Mission
                ├── Chantier
                └── Document de gestion
```

> **Distinction fondamentale** : cette hiérarchie est une **vue
> contextuelle** destinée à l'utilisateur pour la navigation. Elle **ne
> définit pas la propriété exclusive des entités** dans le modèle de
> données. Dans le modèle réel :
>
> - une forêt peut apparaître dans plusieurs projets ;
> - une mission peut concerner plusieurs forêts ;
> - un chantier peut couvrir plusieurs parcelles ;
> - une placette est parfois rattachée à une unité d'échantillonnage
>   plutôt qu'à une seule parcelle ;
> - un document de gestion concerne souvent une unité de gestion
>   entière ;
> - un peuplement peut traverser plusieurs limites de parcelles.
>
> Les entités utilisent des **relations plusieurs-à-plusieurs**
> lorsque le métier l'exige (§19.1.1). La hiérarchie de navigation est
> une simplification ergonomique de ce graphe relationnel.

La navigation contextuelle affiche toujours : le workspace actif, le
projet actif, l'objet actuellement ouvert, l'état de synchronisation, le
mode hors ligne, un bouton retour explicite et un accès rapide à la carte
de l'objet.

### 29.4 Distinction des objets

Ne jamais confondre : propriété, forêt, parcelle cadastrale, parcelle
forestière, unité de gestion, peuplement, placette, mission, chantier.
Les écrans doivent refléter ces distinctions (§19.1.1 — 8 entités
territoriales distinctes).

### 29.5 Audit des écrans existants — classification

> **Avant toute refonte**, Devin doit produire les documents d'audit
> listés en §29.31. Le tableau ci-dessous est la **classification
> préliminaire** basée sur l'analyse du code existant (27 routes, 5
> NavGraphs).

> **Avertissement v0.9.1** : les routes listées dans les tableaux
> ci-dessous peuvent être **altérées** par le rendu Markdown (points
> remplacés, paramètres tronqués). Devin doit **relire les routes
> directement dans le code source**
> (`app/src/main/java/com/forestry/counter/presentation/navigation/`)
> et ne pas considérer les chaînes affichées ici comme exactes.

#### Écrans conservés (enrichis)

| Écran existant | Route actuelle | Nouvelle position | Enrichissements |
|---|---|---|---|
| `Forets` (GroupsScreen) | `forets` | Sous-page Accueil > Forêts + Explorer > Forêts | Filtres, provenance, sync, états hors ligne |
| `Parcelles` | `parcelles/{forestId}` | Forêt > Parcelles (onglet) | Provenance surfaces, sync, alertes |
| `Placettes` | `placettes/{parcelleId}` | Parcelle > Placettes (onglet) | Protocole, état validation, sync |
| `PlacetteDetail` | `placette/{parcelleId}/{placetteId}` | Parcelle > Placettes > Fiche placette | Onglets évolution, santé, biodiversité, calculs |
| `PlacetteEvolution` | `placette/.../evolution/{year}` | Fiche placette > Évolution (onglet) | Comparaison campagnes multiannuelles (§7.6) |
| `EssenceDiam` | `placette/.../essence/{essenceCode}` | Fiche placette > Essences (onglet) | Recherche, tri, agrégations, provenance |
| `Dashboard` | `dashboard/{parcelleId}` | Fiche parcelle > Calculs (onglet) | Méthodes, comparaison, incertitude |
| `Map` (par parcelle) | `map/{parcelleId}?...` | Fiche parcelle/forêt > Carte (onglet) | Conservé, accessible depuis fiches |
| `GroupDetail` | `group/{groupId}` | Explorer > Groupes > Fiche groupe | Sync, export |
| `Formulas` | `group/{groupId}/formulas` | Explorer > Groupes > Formules | Method Registry (§7.10) |
| `Calculator` | `group/{groupId}/calculator` | Explorer > Groupes > Calculateur | Versionnement méthodes |
| `PriceTablesEditor` | `settings/price_tables` | Compte > Tarifs | Abonnement, organisation |
| `TarifDocs` | `settings/tarif_docs` | Compte > Documentation scientifique (§29.26) | Method Registry |
| `PrivacyPolicy` | `settings/privacy_policy` | Compte > Confidentialité | RGPD, consentement (§10.1) |
| `PackManager` | `packs` | Compte > Packs (§29.24) | QPIS complet (§16) |
| `DeveloperOptions` | `settings/developer` | Compte > Développeur | Inchangé, enrichi |
| `SuperCorrelateur` | `super_correlateur/{parcelleId}` | Fiche parcelle > Analyse GSIE (§29.27) | Refonte en analyse GSIE |
| `Onboarding` | `onboarding` | Inchangé (§29.8) | Étapes enrichies |

#### Écrans transformés

| Écran existant | Route actuelle | Transformation | Nouvelle position |
|---|---|---|---|
| `Martelage` | `martelage/{scope}/...` | **Devient SynthèseMartelage** — écran de synthèse qui s'ouvre automatiquement après le martelage. Plus écran de saisie. | Placette > Martelages > Synthèse session |
| `Settings` | `settings` | **Supprimé** — tout passe dans Compte (4 domaines, 16 destinations secondaires, §29.23) | Compte (bottom nav) |
| `Account` | `settings/account` | **Refondu** dans Compte > Profil | Compte > Profil |
| `Login` | `settings/account/login` | **Refondu** en Connexion Quintessences (§29.9) | Avant Accueil, après Onboarding |
| `PasswordRecovery` | `settings/account/password-recovery` | **Refondu** dans Compte > Sécurité | Compte > Sécurité |
| `Map` (carte globale) | — | **Refonte complète** — devient entrée bottom nav Carte | Bottom nav 3ème entrée (§29.20) |
| `DiagnosticMenu` | `diagnostic/menu/{parcelleId}` | **Refondu** — devient onglet Diagnostic dans fiche parcelle/forêt | Fiche parcelle > Diagnostic stationnel (onglet) |
| `DiagnosticResult` | `diagnostic/result/{diagnosticId}` | **Refondu** — devient sous-onglet du diagnostic stationnel | Fiche parcelle > Diagnostic > Résultat |
| `RipisylveDiagnostic` | `ripisylve/diagnostic/{parcelleId}` | **Bouge** — devient onglet Ripisylve dans fiche parcelle + protocole Mission Engine | Fiche parcelle > Ripisylve (onglet) |
| `RipisylveDiagnosticStandalone` | `ripisylve/standalone` | **Bouge** — devient mission de type diagnostic ripisylve | Missions > Diagnostic ripisylve |
| `StandClassification` | `stand/classification/{parcelleId}` | **Bouge** — devient onglet Peuplement dans fiche parcelle | Fiche parcelle > Peuplement (onglet) |
| `IbpProjects` | `ibp/projects` | **Bouge** — devient liste de missions IBP | Missions > IBP |
| `IbpStandalone` | `ibp/standalone` | **Bouge** — devient mission IBP standalone | Missions > IBP |
| `IbpHistory` | `ibp/history/{parcelleId}` | **Bouge** — devient onglet Biodiversité dans fiche parcelle | Fiche parcelle > Biodiversité (onglet) |
| `IbpEvaluation` | `ibp/{parcelleId}/{placetteId}` | **Bouge** — devient sous-onglet IBP dans fiche placette | Fiche placette > Biodiversité > IBP |
| `IbpReference` | `ibp/reference` | **Bouge** — devient page de référence dans Centre scientifique | Compte > Documentation > IBP |
| `IbpDiagnostic` | `ibp/diagnostic/{parcelleId}` | **Bouge** — devient onglet Biodiversité dans fiche parcelle | Fiche parcelle > Biodiversité (onglet) |
| `IbpCompare` | `ibp/compare/{parcelleId}` | **Bouge** — devient sous-onglet comparaison IBP | Fiche placette > Biodiversité > Comparer |

#### Écrans nouveaux

| Écran | Position | Lot |
|---|---|---|
| Splash (écran de lancement) | Avant toute navigation | Lot 0 |
| Accueil (tableau de bord) | Bottom nav 1ère entrée | Lot 1 |
| Sélection workspace | Après connexion | Lot 4 |
| Saisie martelage terrain | Placette > Martelage > Saisie | Lot 3 |
| Liste missions + dashboard mission | Bottom nav 2ème entrée | Lot 3 |
| Carte globale (refonte) | Bottom nav 3ème entrée | Lot 7 |
| Explorer (navigateur global) | Bottom nav 4ème entrée | Lot 1 |
| Compte (4 domaines, 16 destinations) | Bottom nav 5ème entrée | Lot 4 |
| Centre synchronisation | Compte > Synchronisation | Lot 5 |
| Résolution conflits | Centre sync > Conflits | Lot 5 |
| Gestionnaire QPIS (refonte) | Compte > Packs | Lot 6 |
| Centre scientifique | Compte > Documentation | Lot 2 |
| Analyse GSIE | Fiche parcelle > Analyse | Lot 5 |
| TreeVision caméra | Martelage > TreeVision | Lot 8 |
| Chantier travaux | Parcelle > Travaux | Lot 3 |
| Documents de gestion | Forêt > Documents | Lot 3 |
| Fiche projet | Accueil > Projets > Fiche | Lot 1 |
| Fiche forêt (refonte) | Projet > Forêts > Fiche | Lot 1 |
| Création forêt guidée | Accueil > Créer > Forêt | Lot 1 |
| Création parcelle guidée | Forêt > Parcelles > Ajouter | Lot 1 |
| Création placette guidée | Parcelle > Placettes > Ajouter | Lot 1 |

### 29.6 Écran de lancement (Splash)

**Emplacement** : avant toute navigation principale. Nouvel écran.

**Contenu** : logo GeoSylva, identité visuelle Quintessences, animation
courte, vérifications de démarrage.

> **Correction v0.9.0** : le splash ne doit pas bloquer sur toutes les
> vérifications. Séparer **bloquant** et **non-bloquant** :

**Vérifications bloquantes** (avant affichage de l'accueil) :
- ouverture de la base ;
- migration indispensable ;
- intégrité minimale ;
- restauration critique de session.

**Vérifications non-bloquantes** (reportées après affichage de l'accueil,
en arrière-plan) :
- recherche de nouveaux packs ;
- synchronisation ;
- actualisation de session (si la politique hors ligne reste valide) ;
- contrôles analytiques ;
- téléchargements.

**États** : démarrage normal, migration en cours, restauration de
session, pack système manquant, base endommagée, mode hors ligne, mise
à jour obligatoire.

**Règles** : ne jamais afficher un écran blanc prolongé ; ne jamais
lancer une synchronisation lourde avant d'avoir ouvert l'application ;
permettre l'ouverture locale même si GSIE est inaccessible ; afficher une
progression compréhensible pour les migrations longues. **Le démarrage
doit rester extrêmement rapide** — seules les vérifications bloquantes
sont au splash, le reste est asynchrone.

### 29.7 Onboarding

**Emplacement** : après le premier démarrage, avant la connexion. Écran
existant conservé et enrichi. Accessible depuis `Compte > Aide > Revoir
le didacticiel`.

**Parcours** :

1. **Présentation** — rôle de GeoSylva, fonctionnement hors ligne,
   différence entre données locales et serveur GSIE.
2. **Profil** — métier principal, niveau de connaissance, usage
   personnel/professionnel/pédagogique, préférence interface simple ou
   complète.
3. **Capacités** — présentation des capacités de l'app (localisation,
   caméra, microphone, Bluetooth, notifications, fichiers) avec
   explication de leur usage. **Les permissions ne sont pas demandées
   ici** — elles sont demandées **au premier usage** de chaque fonction
   (voir ci-dessous).
4. **Démonstration** — création d'un projet fictif (forêt, parcelle,
   placette, arbres, mini martelage, synthèse).
5. **Packs** — présentation du fonctionnement des packs (système,
   territoire, cartes, modèles, protocoles).

> **Correction v0.9.0** : demander toutes les permissions pendant
> l'onboarding nuit à la confiance et au taux d'acceptation. Les
> permissions sont demandées **juste au moment où elles deviennent
> utiles** :
>
> | Permission | Moment de demande |
> |---|---|
> | Localisation | Premier usage de la carte ou d'une mission |
> | Caméra | Premier scan, ajout de photo ou TreeVision |
> | Microphone | Activation du mode vocal |
> | Bluetooth | Connexion d'un instrument (compas, etc.) |
> | Notifications | Quand une utilité réelle est présentée |
> | Fichiers | Lors d'un import/export précis |
>
> L'onboarding **présente** les capacités, mais ne **demande** pas les
> permissions.

### 29.8 Connexion Quintessences

**Emplacement** : après l'onboarding ou lorsqu'aucune session locale
valide n'existe. Refonte de l'écran `Login` actuel.

**Contenu** : Continuer avec Google, Continuer avec une passkey
Quintessences, Se connecter avec mon organisation, Créer un compte, Mode
découverte, Utiliser l'application hors ligne avec une session existante.

**Organisation visuelle** : fond vidéo ou image forestière. Panneau
inférieur contenant les actions de connexion.

> **Correction v0.9.0** : avant la première connexion et avant
> l'installation des packs, le pack visuel signé peut ne pas exister.
> Prévoir :
>
> 1. **Ressource légère intégrée à l'APK** → utilisée au premier
>    lancement (image statique, pas de vidéo).
> 2. **Pack visuel signé facultatif** → remplace la ressource après
>    installation.
>
> Contraintes : économie de batterie, réduction des animations, support
> des appareils modestes, accessibilité, possibilité de désactiver la
> vidéo.

**Après connexion** : l'utilisateur arrive sur la sélection du workspace
s'il appartient à plusieurs espaces. Sinon, il arrive directement sur
l'accueil.

### 29.9 Sélection du workspace

**Emplacement** : après connexion, depuis le haut de l'accueil, depuis
`Compte > Organisations et espaces`. Nouvel écran.

**Contenu** : pour chaque workspace — nom, organisation, rôle,
abonnement, dernière activité, volume de données locales,
synchronisation, éventuelle alerte de sécurité.

**Actions** : ouvrir, épingler comme workspace par défaut, consulter les
droits, quitter l'organisation, accepter une invitation, créer un espace
personnel ou professionnel.

**Comportement** : le changement de workspace modifie les missions,
projets, données, protocoles, packs, abonnements, capacités et
éventuellement les couleurs de l'organisation.

### 29.10 Accueil (tableau de bord)

**Emplacement** : première entrée de la navigation principale. Nouvel
écran remplaçant le démarrage direct sur Forets.

**Structure** :

- **En-tête** : workspace actif, avatar, état réseau, état
  synchronisation, batterie, bouton recherche globale.
- **Bloc « Reprendre »** : dernière mission, dernier martelage, dernière
  placette, dernière carte, saisie interrompue.
- **Bloc « Aujourd'hui »** : missions prévues, alertes, données à
  synchroniser, packs recommandés, échéances, travaux à contrôler.
- **Bloc « Projets récents »** : grille ou liste, réorganisation,
  dossiers, favoris, état sync, nombre de forêts, nombre de missions.
- **Bouton principal** « Créer ou démarrer » → nouveau projet, nouvelle
  mission, nouvelle forêt, nouvel inventaire, nouveau martelage,
  nouvelle observation rapide.

### 29.11 Projets et dossiers

**Emplacement** : `Accueil > Tous les projets` et `Explorer > Projets`.
Nouvel écran.

**Liste** : filtres (récents, favoris, organisation, territoire, statut,
sync, date, mission associée).

**Carte projet** : nom, couleur, forêt/territoire, nombre de parcelles,
nombre de missions, dernière modification, sync, alertes.

**Page détail projet** — onglets : Vue générale, Forêts, Missions,
Documents, Carte, Équipe, Historique.

### 29.12 Forêt (refonte)

**Emplacement** : `Projet > Forêts > Fiche forêt` ou depuis la carte.
Refonte de l'organisation actuelle (Forets/GroupsScreen devient liste
de forêts, fiche forêt est nouvelle).

**En-tête** : nom, propriétaire/gestionnaire si autorisé, surface
officielle, surface calculée, territoire, statut, synchronisation.

**Onglets** : Résumé, Parcelles, Peuplements, Missions, Carte, Documents,
Historique.

- **Résumé** : identité, surfaces, composition, principaux peuplements,
  contraintes, risques, dernières observations, indicateurs.
- **Parcelles** : liste et carte des parcelles forestières.
- **Peuplements** : type, essences, âge/stade, surface, diagnostic,
  dernier inventaire.
- **Documents** : PSG, aménagement, programme de coupes, cartes (§29.28).

### 29.13 Création d'une forêt (guidée)

**Emplacement** : `Accueil > Créer > Forêt`, `Projet > Ajouter une forêt`,
`Carte > Ajouter un territoire`. Nouvel écran.

**Étapes** : méthode (recherche institutionnelle/GPS/dessin/import/sans
géométrie) → identification (nom, type, gestionnaire, référence,
description) → géométrie (packs, import, dessin, sélection) → provenance
(organisme, date, licence, précision, statut) → synthèse (surfaces,
écarts, avertissements) → après création (ajouter parcelles, télécharger
packs, créer mission, ouvrir fiche).

### 29.14 Parcelle forestière (refonte)

**Emplacement** : `Forêt > Parcelles > Fiche parcelle` ou sélection
carte. Écran `Parcelles` existant conservé, fiche parcelle enrichie.

> **Correction v0.9.0** : la fiche parcelle avait 13 onglets en v0.8.0,
> ce qui est inexploitable sur smartphone. La navigation locale est
> désormais organisée en **5 groupes** avec sous-navigation :

**Navigation principale locale** :

```text
Aperçu    Terrain    Interventions    Analyse    Plus
```

**Aperçu** : surface, géométrie, références cadastrales, peuplement
principal, dernier inventaire, dernier martelage, travaux prévus,
alertes, indicateurs.

**Terrain** (sous-navigation) :
- **Peuplement** : structure, essences, classes de diamètre, densité,
  surface terrière, volume, qualité, état sanitaire, habitat. (Ancien
  `StandClassification` devient cet onglet.)
- **Placettes** : type, surface, protocole, date, état, nombre de tiges.
- **Inventaires** : campagnes successives et comparaisons.
- **Carte** : vue cartographique de la parcelle (ancien Map conservé).

**Interventions** (sous-navigation) :
- **Martelages** : sessions, objectifs, prélèvements, synthèses.
- **Travaux / Chantier** : chantiers et prescriptions (§29.31).
- **Historique** : évolution des limites, inventaires, travaux,
  interventions.

**Analyse** (sous-navigation, **cartes conditionnelles** — voir §29.29) :
- **Diagnostic stationnel** (si protocole installé et territoire pertinent).
- **Santé** (si observations disponibles).
- **Biodiversité / IBP** (si protocole IBP installé).
- **Ripisylve** (uniquement si la parcelle est concernée — zone
  ripicole — ou si le protocole est installé).
- **Analyse GSIE** (si connexion serveur disponible, §29.28).

> Les analyses apparaissent sous forme de **cartes conditionnelles**,
> pas d'onglets fixes. Elles dépendent des protocoles installés, du type
> de territoire, du métier, de l'abonnement, de la mission et des
> données disponibles (§29.29).

**Plus** (sous-navigation) :
- **Documents** : PSG, aménagement, programme de coupes, cartes (§29.32).
- **Provenance** : traçabilité des sources de données.
- **Paramètres** : préférences locales de la parcelle.

### 29.15 Création de parcelle (guidée)

**Emplacement** : `Forêt > Parcelles > Ajouter`. Nouvel écran.

**Étapes** : recherche automatique (GPS, forêt active, packs locaux,
données institutionnelles — 5 propositions max) → comparaison (source,
date, surface, précision, nom, référence, géométrie) → sélection
(choisir, combiner, dessiner, importer, créer non référencée) → surfaces
(cadastrale, officielle, SIG, saisie, réellement travaillée — affichées
séparément) → validation (résumé complet et provenance).

### 29.16 Placette (refonte)

**Emplacement** : `Parcelle > Placettes > Fiche placette` ou depuis une
mission d'inventaire. Écran `PlacetteDetail` existant conservé et
enrichi.

> **Correction v0.9.0** : la fiche placette avait 11 onglets en v0.8.0.
> Même logique que la fiche parcelle : 5 groupes avec sous-navigation.

**Navigation principale locale** :

```text
Aperçu    Tiges    Martelage    Analyse    Plus
```

**Aperçu** : type, protocole, surface, rayon, forme, date, opérateurs,
nombre de tiges, état de validation, synchronisation.

**Tiges** (sous-navigation) — visualisation et consultation :
- **Liste des tiges** : filtrable (numéro, essence, diamètre, hauteur,
  état, catégorie martelage, qualité, confiance). Chaque tige affiche
  son emplacement, ses informations détaillées (essence, dimensions,
  état sanitaire, photos, provenance).
- **Carte des tiges** : vue cartographique de la placette avec
  positionnement de chaque tige, sélection interactive, affichage des
  infos au tap.
- **Essences** : cartes par essence (ancien `EssenceDiam` enrichi —
  recherche, tri, agrégations, provenance, comparaison, accessibilité).
- **Évolution** : comparaison entre campagnes (ancien `PlacetteEvolution`
  enrichi — croissance, mortalité, recrutement, changements, évolution
  sanitaire).

> **Correction v0.9.1** : TreeVision ne se trouve **pas** dans Tiges.
> Tiges est dédié à la **visualisation** (liste + carte + infos par
> tige). La mesure par caméra se fait depuis **Martelage**.

**Martelage** (sous-navigation) :
- **Sessions** : liste des sessions de martelage, synthèses.
- **Saisie** : écran de saisie terrain — **écran actuel
  `PlacetteDetailScreen` (titre « Placette essences ») conservé et
  adapté** avec ajouts (indicateurs session, prélèvement, TreeVision,
  actions session), §29.19.
- **TreeVision** : mesure caméra d'une tige pendant le martelage
  (§29.30).

**Analyse** (sous-navigation, **cartes conditionnelles** — §29.29) :
- **Calculs** : surface terrière, densité, volume, incertitude, méthode,
  comparaison des méthodes, provenance (ancien `Dashboard` enrichi).
- **Santé** (si observations disponibles).
- **Biodiversité / IBP** (si protocole IBP installé — ancien
  `IbpEvaluation` déplacé).
- **Analyse GSIE** (si connexion serveur disponible, §29.28).

**Plus** (sous-navigation) :
- **Pièces jointes** : photos, documents.
- **Provenance** : traçabilité des sources.
- **Historique** : modifications, sync.

### 29.17 Création de placette (guidée)

**Emplacement** : `Parcelle > Placettes > Ajouter`,
`Mission inventaire > Ajouter placette`, `Carte > Ajouter placette`.
Nouvel écran.

**Choix initial** : martelage intégral de la parcelle, placette de
surface définie, protocole d'échantillonnage, placette permanente,
placette temporaire.

**Paramètres** : forme, surface, rayon, centre, orientation, protocole,
méthode de positionnement, précision.

**Contrôles** : surface supérieure à la parcelle, placette hors limite,
chevauchement, rayon incohérent, GPS insuffisant, pack manquant.

**Après création** : ouvrir la fiche placette, pas directement le
martelage.

### 29.18 Saisie d'une tige

**Emplacement** : `Placette > Tiges > Ajouter`, session de martelage,
TreeVision, commande vocale, compas Bluetooth. Nouvel écran.

**Écran principal** :
- **Zone supérieure** : essence, numéro de tige, état sync, méthode de
  saisie.
- **Zone diamètre** : valeur, classe, compas, caméra, saisie vocale,
  correction.
- **Zone hauteur** : saisie, clinomètre, visée, estimation, TreeVision.
- **Zone qualité** : A/B/C/D, défauts, rectitude, longueur marchande.
- **Zone sanitaire** : sain, dépérissant, mort, symptômes, pathogène,
  photo.
- **Zone biodiversité** : arbre habitat, cavité, bois mort, microhabitat.
- **Action principale** : « Enregistrer et passer à la tige suivante ».

**Ergonomie terrain** : gros boutons, utilisation à une main, mode
gaucher, retour haptique, raccourcis, dernière essence conservée,
correction immédiate, fonctionnement avec gants, mode pluie.

### 29.19 Session de martelage et SynthèseMartelage

**Emplacement** : `Placette > Martelage > Démarrer`,
`Parcelle > Martelages > Nouvelle session`,
`Mission > Démarrer le martelage`.

> **Transformation clé** : l'ancien écran `Martelage` devient
> **SynthèseMartelage**. La saisie se fait dans l'**écran actuel
> `PlacetteDetailScreen`** (titre « Placette essences ») — **pas un
> nouvel écran créé de zéro**. Cet écran existant est **conservé et
> adapté** avec quelques modifications et ajouts pour devenir l'écran
> de saisie martelage. La synthèse s'ouvre **automatiquement** après la
> fin du martelage.

> **Écran conservé et adapté** (`PlacetteDetailScreen` → écran de
> saisie martelage) :
>
> L'écran actuel affiche déjà les essences d'une placette avec blocs
> par essence, compte de tiges, recherche, réordonnancement, onglets
> Essences/Évolution, navigation vers diamètres/martelage/IBP. Il
> devient l'écran de saisie martelage avec les **modifications et ajouts
> suivants** :
>
> - indicateur de session active (durée, tiges enregistrées) ;
> - indicateurs de prélèvement (G avant/après, taux, volume prélevé) ;
> - catégorie martelage dans la saisie (prélevé / conservé / avenir) ;
> - bouton TreeVision (mesure caméra, §29.30) ;
> - actions session (pause, annuler dernière, terminer) ;
> - mode vocal (si activé) ;
> - intégration compas Bluetooth (si connecté).
>
> L'essence de l'écran (blocs par essence, liste des tiges, réordonnancement,
> recherche, onglets) **reste identique** — ce sont les fonctionnalités
> session martelage qui s'ajoutent par-dessus.

**Écran de préparation** (nouveau, avant la saisie) : objectif,
protocole, participants, surface, peuplement, seuils, catégories, packs,
batterie, GPS, matériel connecté.

**Écran de saisie actif** (`PlacetteDetailScreen` adapté) :
- **En-tête permanent** : durée active, durée totale, tiges enregistrées,
  synchronisation, batterie, GPS.
- **Indicateurs** : nombre prélevé, nombre conservé, G avant, G prélevée,
  taux de prélèvement, volume prélevé, objectifs.
- **Zone de saisie** : essence, diamètre, catégorie, qualité, défaut,
  observation, voix, compas, TreeVision.
- **Actions** : pause, annuler dernière tige, afficher liste, voir carte,
  ajouter photo, signaler anomalie, terminer.

**Pause** : la session reste active mais la saisie est suspendue.
L'application reste accessible.

**Fin** : deux validations (arrêter la saisie, confirmer la clôture).
Produire un instantané immuable.

**SynthèseMartelage** (s'ouvre automatiquement après fin) :
- Données locales, contrôles, incohérences.
- Analyse GSIE disponible (§29.27).
- Export, validation.
- Graphiques (G avant/après, répartition par essence, volume prélevé).

### 29.20 Carte principale (refonte complète)

**Emplacement** : troisième entrée de la navigation principale (bottom
nav). Refonte complète de l'ancien écran `Map`.

> L'ancien écran `Map` par parcelle est **conservé** et reste accessible
> depuis les fiches parcelle/forêt (onglet Carte). La nouvelle carte
> globale est un **écran différent**.

**Modes** : Explorer, Mission, Éditer, Mesurer, Télécharger, Analyser.

**Barre supérieure** : recherche, territoire, mode, position, hors
ligne, packs.

**Bouton couches** — panneau inférieur : Travail, Forêt, Parcellaire,
Référentiels, IGN, GSIE, Personnel.

**Fiche d'objet** (sélection) : nom, type, source, date, actions (ouvrir
la fiche, démarrer une mission, naviguer, modifier si autorisé).

**Outils** : distance, surface, profil, dessin, snapping, sélection,
buffer, intersection, export, téléchargement local.

**Cartes hors ligne** : accès direct au gestionnaire QPIS (§29.24).

### 29.21 Missions

**Emplacement** : deuxième entrée de la navigation principale. Nouvel
écran.

**Liste** : filtres (aujourd'hui, à venir, en cours, terminées, à
synchroniser, assignées, créées par moi).

**Carte mission** : type, territoire, date, responsable, progression,
packs, synchronisation, risques.

**Tableau de bord mission** — onglets : Résumé, Parcours, Données,
Carte, Équipe, Matériel, Livrables, Synchronisation, Historique.

- **Parcours** : étapes guidées (Préparation, Arrivée, Collecte, Contrôle,
  Validation, Restitution, Synchronisation).
- **Livrables** : rapport, export, carte, synthèse, validation.

> Les diagnostics (stationnel, ripisylve, IBP) sont également
> accessibles comme **missions** de type diagnostic (protocoles du
> Mission Engine, §17).

### 29.22 Explorer (navigateur global)

**Emplacement** : quatrième entrée de la navigation principale. Nouvel
écran. Le terme « Explorer » est privilégié sur « Données » (variante B,
§29.2 — à tester avec utilisateurs).

**Catégories** : Projets, Forêts, Parcelles, Peuplements, Placettes,
Tiges, Observations, Missions, Travaux, Documents, Calculs, Packs,
Méthodes.

**Fonctions** : recherche globale, filtres, export, import, archivage,
détection de doublons, synchronisation, contrôle de qualité.

### 29.23 Compte et paramètres (refonte complète)

**Emplacement** : cinquième entrée de la navigation principale. Refonte
complète — l'ancien écran `Settings` est **supprimé**, tout passe dans
Compte.

> **Correction v0.9.0** : les 16 sections ne doivent pas être présentées
> comme 16 lignes plates sans hiérarchie. Elles sont regroupées en
> **4 groupes visuels** :

**Groupe 1 — Identité** :
```text
Identité
├── Profil (avatar, nom, métier, préférences — ancien Account refondu)
├── Organisations
├── Workspace
├── Sécurité (passkeys, Google, entreprise, sessions, TOTP, récupération)
└── Appareils
```

**Groupe 2 — Offre Quintessences** :
```text
Offre Quintessences
├── Abonnement (offre, fonctions, stockage, facturation, historique)
├── Packs (gestionnaire QPIS, §29.24)
└── Stockage
```

**Groupe 3 — Application** :
```text
Application
├── Terrain (pluie, gants, gaucher, haptique, saisie rapide, écran)
├── Cartes (fonds, caches, téléchargements, QPIS cartographique)
├── IA (modèles installés, packs IA, assistant vocal)
├── Synchronisation (→ Centre sync, §29.25)
└── Accessibilité
```

**Groupe 4 — Confidentialité et assistance** :
```text
Confidentialité et assistance
├── Confidentialité (RGPD, consentement — ancien PrivacyPolicy)
├── Aide (→ Onboarding, §29.7)
├── Développeur (base, migrations, packs, sync, méthodes, logs, GPS,
│   performances — ancien DeveloperOptions enrichi)
└── À propos
```

### 29.24 Gestionnaire QPIS (refonte)

**Emplacement** : `Compte > Packs`, `Carte > Télécharger`,
`Mission > Packs requis`, `Explorer > Packs`. Refonte de l'ancien
`PackManager`.

**Onglets** : Recommandés, Installés, Territoires, Scientifiques, IA,
Organisation, Stockage, Historique.

**Carte pack** : nom, type, territoire, version, taille, date, licence,
source, abonnement, état, mise à jour.

**Stockage** : diagramme (projets, données non synchronisées, cartes,
photos, modèles, cache, espace libre).

**Actions** : télécharger, mettre à jour, suspendre, supprimer,
restaurer, voir les dépendances, voir la provenance.

### 29.25 Centre de synchronisation

**Emplacement** : indicateur global de synchronisation,
`Compte > Synchronisation`, `Mission > Synchronisation`. Nouvel écran.

**Onglets** : Vue générale, En attente, Erreurs, Conflits, Appareils,
Historique.

- **Vue générale** : dernière sync, éléments envoyés/reçus, taille,
  connexion, workspace, état GSIE.
- **Conflits** : liste avec objet, origine, version, auteur, date,
  gravité.

### 29.26 Résolution des conflits

**Emplacement** : depuis le centre de synchronisation ou une fiche
concernée. Nouvel écran.

**Présentation** : version locale, version serveur, version d'un autre
appareil. Affichage des différences, provenance, utilisateur, appareil,
date, méthode, preuves.

**Actions** : garder local, garder serveur, fusionner champ par champ,
dupliquer, demander validation, reporter. La décision est enregistrée
dans l'audit.

### 29.27 Centre scientifique

**Emplacement** : `Explorer > Méthodes`, `Compte > Documentation
scientifique`, `Calcul > Voir la méthode`. Nouvel écran (ancien
`TarifDocs` enrichi).

**Contenu** : méthodes installées, versions, équations, unités, domaines
de validité, sources, licences, tests, statut expérimental ou validé.

**Page méthode** : description, variables, formule, source, territoire,
espèces, plages, incertitude, historique, comparaison des versions.

### 29.28 Analyse GSIE

**Emplacement** : après inventaire, martelage, diagnostic, sélection
parcelle, demande explicite. Refonte de l'ancien `SuperCorrelateur`.

**Structure** : Analyse locale, Analyse GSIE, Diagnostic,
Recommandations, Scénarios, Sources.

- **Analyse locale** : disponible immédiatement hors ligne.
- **Analyse GSIE** : moteur utilisé, date, version, sources, niveau de
  preuve, incertitude, chaîne d'inférence.
- **Recommandations** : toujours explicables, modifiables, refusables,
  comparables.

### 29.29 Diagnostics — nouvelle organisation

> **Refonte profonde** : les diagnostics (stationnel, ripisylve, IBP)
> ne sont plus des NavGraphs séparés. Ils deviennent des **cartes
> conditionnelles** dans la section « Analyse » des fiches parcelle et
> placette (§29.14, §29.16) ET des **protocoles** du Mission Engine (§17).

> **Correction v0.9.0** : les diagnostics ne doivent **pas** devenir des
> onglets fixes sur toutes les parcelles. La surcharge visuelle serait
> trop importante. La bonne logique est une section « Analyse » avec des
> **cartes conditionnelles** qui apparaissent selon :
>
> - les **protocoles installés** (QPIS) ;
> - le **type de territoire** (ripicole, montagne, plaine) ;
> - le **métier** de l'utilisateur (capabilities) ;
> - l'**abonnement** (entitlements) ;
> - la **mission** en cours ;
> - les **données disponibles**.
>
> C'est exactement l'intérêt du Mission/Protocol Engine.

#### Cartes conditionnelles d'analyse

| Carte | Condition d'affichage |
|---|---|
| Diagnostic stationnel | Protocole station installé + territoire pertinent |
| IBP | Protocole IBP installé |
| Ripisylve | Parcelle en zone ripicole **ou** protocole ripisylve installé |
| Diagnostic sanitaire | Observations sanitaires disponibles |
| Analyse climatique | Pack climat installé + données disponibles |
| Analyse GSIE | Connexion serveur disponible (§29.28) |

> La ripisylve ne doit **pas** apparaître comme onglet permanent pour
> toutes les parcelles. L'IBP et les diagnostics spécialisés suivent la
> même logique.

#### Diagnostic stationnel (refonte)

**Nouvelle position** : `Fiche parcelle > Analyse > Diagnostic stationnel`
(carte conditionnelle) et `Missions > Diagnostic stationnel` (mission).

Anciens écrans `DiagnosticMenu` et `DiagnosticResult` → refondus en
carte d'analyse avec sous-onglets (saisie, résultat, synthèse). La
synthèse automatique (§21.2) et l'explicabilité (§21.3) sont intégrées.

#### Ripisylve (déplacé)

**Nouvelle position** : `Fiche parcelle > Analyse > Ripisylve` (carte
conditionnelle — uniquement si zone ripicole ou protocole installé) et
`Missions > Diagnostic ripisylve` (mission).

Anciens écrans `RipisylveDiagnostic` et `RipisylveDiagnosticStandalone`
→ refondus en carte d'analyse. Le mode standalone devient une mission
de type diagnostic ripisylve.

#### IBP (déplacé)

**Nouvelle position** : `Fiche parcelle > Analyse > Biodiversité/IBP`
(carte conditionnelle — uniquement si protocole IBP installé) et
`Missions > IBP` (mission).

Anciens écrans IBP (7 routes) → refondus :
- `IbpProjects`/`IbpStandalone` → liste de missions IBP.
- `IbpHistory`/`IbpDiagnostic`/`IbpCompare` → cartes d'analyse de la
  fiche parcelle.
- `IbpEvaluation` → carte d'analyse de la fiche placette.
- `IbpReference` → page de référence dans le Centre scientifique
  (§29.27).

### 29.30 TreeVision

**Emplacement** : `Placette > Martelage > TreeVision`,
`Mission martelage > TreeVision`. Nouvel écran.

> **Correction v0.9.1** : TreeVision est accessible depuis le
> **martelage**, pas depuis Tiges. Tiges est dédié à la visualisation
> (liste, carte, infos par tige). La mesure caméra se fait pendant la
> saisie d'un martelage.

**Écran caméra** :
- **Haut** : arbre actif, mode (rapide/précis/calibration), stabilité,
  profondeur, GPS, lumière.
- **Centre** : viseur, ligne 1,30 m, contour du tronc, guide de
  déplacement, couverture angulaire.
- **Bas** : viser la base, scanner, viser la cime, zoom, ajouter compas,
  valider.

**Page résultat** : diamètre automatique, diamètre manuel, hauteur,
position, incertitude, confiance, anomalies, correction, preuves.

**Restrictions** : TreeVision reste marqué **expérimental** tant que son
banc de validation ne prouve pas sa précision (§18.10).

### 29.31 Travaux forestiers

**Emplacement** : `Parcelle > Travaux`, `Mission > Chantier`,
`Accueil > Travaux à suivre`. Nouvel écran.

**Fiche chantier** — onglets : Prescription, Planification, Carte,
Exécution, Contrôle, Réception, Documents, Historique.

### 29.32 Documents de gestion

**Emplacement** : `Forêt > Documents`, `Projet > Documents`,
`Explorer > Documents`. Nouvel écran.

**Catégories** : PSG, aménagement, programme de coupes, programme de
travaux, cartes, bilans, avenants.

**Page document** : statut, période, territoire, objectifs,
interventions, écarts, pièces jointes, validations, historique.

### 29.33 Design System

Créer un module commun `design-system/` contenant : couleurs,
typographie, espacements, icônes, boutons, cartes, champs, formulaires,
tableaux, graphiques, badges, alertes, panneaux cartographiques, états,
animations, accessibilité.

**Badges obligatoires** :

```text
Synchronisé    En attente     Hors ligne     Conflit
Validé         Expérimental   Estimé         Observé
Calculé        Corrigé        Pack manquant  Abonnement requis
Lecture seule
```

**États de page** — chaque page doit gérer : chargement, vide, erreur,
hors ligne, droits insuffisants, pack manquant, donnée obsolète,
conflit, synchronisation, lecture seule.

### 29.34 Audit préalable des pages existantes

Avant toute refonte, Devin doit produire :

```text
UI_SCREEN_INVENTORY.md       — inventaire de chaque écran
UI_NAVIGATION_MAP.md         — carte de navigation entrante/sortante
UI_COMPONENT_DUPLICATION.md  — duplication de composants
UI_ACCESSIBILITY_AUDIT.md    — audit accessibilité
UI_PERFORMANCE_AUDIT.md      — audit performances
UI_REFACTOR_PLAN.md          — plan de refonte détaillé
```

Pour chaque écran existant : chemin du fichier, rôle, captures,
navigation entrante, navigation sortante, données affichées, état local,
ViewModel, composants, problèmes, décision (conserver, refondre, fusionner,
supprimer).

### 29.35 Roadmap UI transversale

La refonte UI/UX accompagne chaque lot technique (§12.4). La phase finale
ne sert qu'à harmoniser et optimiser ce qui a déjà été refondu.

| Lot | Pages à traiter |
|---|---|
| **Lot 0 — Audit existant** | Splash, états globaux, erreurs, provenance, navigation |
| **Lot 1 — Contrat données** | Accueil, Explorer, Fiche projet, Fiche forêt (refonte), Création forêt/parcelle/placette guidée, Parcelles/Placettes enrichis |
| **Lot 2 — Noyau scientifique** | Fiche placette (Calculs, Essences enrichis), Centre scientifique, Dashboard enrichi |
| **Lot 3 — Mission Engine** | Liste missions, dashboard mission, Saisie martelage terrain, SynthèseMartelage, Chantier travaux, Documents gestion, Diagnostics (stationnel/ripisylve/IBP déplacés) |
| **Lot 4 — Identité** | Connexion Quintessences (refonte Login), Sélection workspace, Compte (4 domaines, 16 destinations, refonte Settings/Account), Appareils |
| **Lot 5 — Synchronisation** | Centre synchronisation, Résolution conflits, Analyse GSIE (refonte SuperCorrelateur) |
| **Lot 6 — QPIS** | Gestionnaire QPIS (refonte PackManager) |
| **Lot 7 — Geo Engine** | Carte principale (refonte complète), fiches cartographiques |
| **Lot 8 — TreeVision** | Caméra, mesure, validation, résultat |
| **Lot 9 — IA locale** | Assistant, dictée, explications |
| **Lot 10 — Meshtastic** | (pas d'UI dédiée, intégration dans Mission) |
| **Quality Pass final** | Harmonisation, accessibilité, performances, cohérence visuelle |

### 29.36 Critères d'acceptation UI

- parcours complet sans réseau ;
- aucune perte de saisie lors d'un changement de page ;
- retour arrière cohérent ;
- reprise exacte après fermeture ;
- utilisation à une main ;
- mode gaucher ;
- utilisation avec gants ;
- lisibilité au soleil ;
- mode pluie ;
- TalkBack ;
- textes agrandis ;
- aucune information portée uniquement par une couleur ;
- temps d'affichage mesuré ;
- navigation testée sur téléphone ancien et récent ;
- fonctionnement tablette ;
- cohérence visuelle entre toutes les pages ;
- aucune page nouvelle sans état vide, erreur et hors ligne ;
- aucun écran métier sans provenance des données importantes.

