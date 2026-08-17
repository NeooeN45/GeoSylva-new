# Backlog Lot 1 — Contrat universel de données

| Champ | Valeur |
|---|---|
| **Lot** | 1 — Contrat universel de données |
| **Spec de référence** | GEOSYLVA-003 v0.9.1 (Frozen), §7.6, §29.10–29.22 |
| **Dépendance amont** | Lot 0 (clôturé 2026-08-04) |
| **RFC associée** | À amorcer — RFC-GEOSYLVA-DATA-CONTRACT (la RFC-0001 existante porte sur la Constitution, pas sur le contrat de données) |
| **Date** | 2026-08-04 |
| **Auteur** | Devin (GLM-5.2 High) |
| **Statut** | Draft — en attente de validation du Fondateur |

---

## 1. Objectif du Lot 1

Poser la **fondation du modèle de données** de GeoSylva 3.0 : un contrat
universel qui distingue explicitement les niveaux **arbre / observation /
mesure / résultat calculé**, généralise les UUID, formalise la provenance,
les unités et les événements, et prépare le terrain pour le noyau
scientifique (Lot 2) et le Mission Engine (Lot 3).

Le Lot 1 livre aussi les **premières pages UI de la refonte §29** :
Accueil (tableau de bord), Explorer (navigateur global), Fiche projet,
Fiche forêt refondue, création guidée forêt/parcelle/placette, et
l'enrichissement des listes Parcelles/Placettes.

---

## 2. Périmètre

### 2.1 Modèle de données (cœur du lot)

| Épic | Description | Spec |
|---|---|---|
| **UUID globaux** | Toute entité persistée porte un UUID stable (inter-opérable avec GSIE serveur). Migration des IDs Long existants vers UUID optionnels (mapping Long→UUID pendant la transition). | §7.6, §3.1 |
| **Entité `Observation`** | Nouvelle entité racine : une observation = un événement daté, localisé, rattaché à un arbre/placette/parcelle, avec protocole et observateur. | §7.6 |
| **Entité `Measurement`** | Mesure atomique rattachée à une observation : type (diamètre, hauteur, etc.), valeur, unité, incertitude, méthode. | §7.6 |
| **Entité `Evidence`** | Pièce jointe rattachée à une observation ou mesure : photo, audio, document, coordonnée GPS. | §7.6 |
| **Entité `CalculationRun`** | Résultat calculé : entrées (mesures), méthode (Method Registry), sorties, incertitude, horodatage, statut (validé/rejeté). | §7.6, §7.10 |
| **Provenance** | Champ normalisé sur toute entité : organisme source, date, licence, précision, statut. | §29.13 |
| **Unités** | Catalogue d'unités (m, cm, m², m³/ha, kg, t) + conversion automatique aux frontières. | §7.6 |
| **Événements** | Journal d'événements (création, modification, sync, conflit, suppression) — base du Lot 5 (sync). | §7.6, Lot 5 |
| **Distinction arbre/observation/mesure/résultat** | Refonte de l'arbre actuel (TreeEntity) pour séparer : `PermanentTree` (UUID, identité) / `TreeObservation` (campagne) / `TreeMeasurement` (mesure) / `CalculationRun` (résultat). Aucun niveau n'écrase l'autre. | §7.6 |

### 2.2 Pages UI (§29)

| Épic | Page | Spec | État actuel |
|---|---|---|---|
| **Bottom nav 5 entrées** | Scaffold principal avec Accueil / Missions / Carte / Explorer / Compte. Remplace le démarrage direct sur Forets. | §29.2 | Nouveau |
| **Accueil** | Tableau de bord : en-tête workspace, bloc « Reprendre », bloc « Aujourd'hui », bloc « Projets récents », bouton « Créer ou démarrer ». | §29.10 | Nouveau |
| **Explorer** | Navigateur global : 13 catégories (Projets, Forêts, Parcelles, Peuplements, Placettes, Tiges, Observations, Missions, Travaux, Documents, Calculs, Packs, Méthodes), recherche, filtres, export, import, archivage, doublons. | §29.22 | Nouveau |
| **Fiche projet** | Liste projets + page détail (onglets Vue générale, Forêts, Missions, Documents, Carte, Équipe, Historique). | §29.11 | Nouveau |
| **Fiche forêt (refonte)** | En-tête (nom, surfaces, territoire, sync) + 7 onglets (Résumé, Parcelles, Peuplements, Missions, Carte, Documents, Historique). | §29.12 | Refonte (Forets/Groups → liste forêts) |
| **Création forêt guidée** | Wizard : méthode → identification → géométrie → provenance → synthèse → après création. | §29.13 | Nouveau |
| **Création parcelle guidée** | Wizard similaire (§29.15). | §29.15 | Nouveau |
| **Création placette guidée** | Wizard (§29.16). | §29.16 | Nouveau |
| **Parcelles enrichi** | Liste + carte des parcelles, filtres, provenance, sync, états hors ligne. | §29.14 | Enrichissement (écran existant conservé) |
| **Placettes enrichi** | Liste + carte, type, surface, protocole, date, état, nombre de tiges. | §29.14 | Enrichissement (écran existant conservé) |

### 2.3 Hors périmètre Lot 1 (explicitement reporté)

- **Method Registry complet** → Lot 2 (§7.10)
- **Cubage, surface terrière, agrégations** → Lot 2
- **Mission Engine, protocoles, martelage terrain** → Lot 3
- **Authentification fédérée, workspaces serveur** → Lot 4
- **Synchronisation GSIE (push/pull/conflits)** → Lot 5 (le journal d'événements Lot 1 prépare le terrain)
- **Carte refonte complète (MapLibre/PMTiles)** → Lot 7 (l'entrée bottom nav Carte est un stub en Lot 1)
- **Compte refonte (4 domaines, 16 destinations)** → Lot 4 (l'entrée bottom nav Compte est un stub en Lot 1)
- **Missions** → Lot 3 (l'entrée bottom nav Missions est un stub en Lot 1)

---

## 3. Découpage en sprints / épics

### Sprint 1 — Fondation modèle (sans UI)

| # | Tâche | Livrable | Tests |
|---|---|---|---|
| 1.1 | Définir le schéma cible (Room entities) : `PermanentTree`, `TreeObservation`, `TreeMeasurement`, `CalculationRun`, `Evidence`, `Observation` (générique), `Provenance` embedded, `Unit` catalog | Entités + diagramme ER | Tests Room schema validation |
| 1.2 | Migration DB v34 → v35 : ajout tables, mapping Long→UUID (colonne `legacy_id` + `uuid` nullable puis backfill), `DEFAULT 1` sur `version` | Migration 34→35 + schema 35.json | Test instrumenté `Migration34To35Test` (upgrade + downgrade) |
| 1.3 | Repository interfaces + impl pour les nouvelles entités | 6 repos | Tests unitaires repo (fake DB) |
| 1.4 | Catalogue d'unités + convertisseur | `UnitCatalog`, `UnitConverter` | Tests unitaires (conversions, edge cases) |
| 1.5 | Journal d'événements (table `event_log`) + service d'émission | `EventLogDao`, `EventLogger` | Tests unitaires |

### Sprint 2 — Bridge données existantes

| # | Tâche | Livrable | Tests |
|---|---|---|---|
| 2.1 | Adapter les use cases existants (Tree, Placette, Parcelle) pour écrire dans le nouveau modèle sans casser l'existant | Refactor use cases | Tests de non-régression (40 tests actuels doivent rester verts) |
| 2.2 | BackupService (reporté de Lot 0) — implémenter export/import avec le nouveau modèle | `BackupService.export()`, `.import()` | Tests instrumentés (avant @Ignore) |
| 2.3 | Outil de migration données : script Kotlin qui backfill les UUID sur l'existant | `UuidBackfillWorker` | Tests unitaires |

### Sprint 3 — UI : Bottom nav + Accueil + Explorer

| # | Tâche | Livrable | Tests |
|---|---|---|---|
| 3.1 | `MainScaffold` Compose avec bottom nav 5 entrées (Accueil/Missions/Carte/Explorer/Compte). Missions/Carte/Compte = stubs « à venir ». | Composant + NavGraph restructuré | Test instrumenté navigation |
| 3.2 | Écran **Accueil** (tableau de bord) — en-tête workspace, bloc Reprendre, bloc Aujourd'hui, bloc Projets récents, bouton Créer. | `HomeScreen` | Test UI (Compose) |
| 3.3 | Écran **Explorer** — 13 catégories, recherche globale, filtres de base. | `ExplorerScreen` | Test UI |
| 3.4 | Rediriger l'ancien démarrage `Forets` vers `Accueil > Forêts` + `Explorer > Forêts`. | Refactor NavGraph | Test navigation |

### Sprint 4 — UI : Projets + Fiche forêt + Création guidée

| # | Tâche | Livrable | Tests |
|---|---|---|---|
| 4.1 | Écran **Projets** (liste + filtres) + **Fiche projet** (7 onglets). | `ProjectsScreen`, `ProjectDetailScreen` | Test UI |
| 4.2 | **Fiche forêt refondue** (7 onglets). `Forets`/`GroupsScreen` devient liste de forêts. | `ForestDetailScreen` (refonte) | Test UI |
| 4.3 | **Wizard création forêt** (6 étapes : méthode → identification → géométrie → provenance → synthèse → après création). | `CreateForestWizard` | Test UI + test logique wizard |
| 4.4 | **Wizard création parcelle** + **Wizard création placette**. | `CreateParcelleWizard`, `CreatePlacetteWizard` | Test UI |

### Sprint 5 — Enrichissement Parcelles/Placettes + Quality Pass Lot 1

| # | Tâche | Livrable | Tests |
|---|---|---|---|
| 5.1 | Enrichir `ParcellesScreen` : carte, filtres, provenance, sync, états hors ligne. | Refonte écran existant | Test UI |
| 5.2 | Enrichir `PlacettesScreen` : type, surface, protocole, date, état, nombre de tiges. | Refonte écran existant | Test UI |
| 5.3 | Quality Pass Lot 1 : états vides / erreurs / hors ligne sur toutes les nouvelles pages, accessibilité de base. | Review + fixes | Audit manuel |
| 5.4 | Documentation : `docs/LOT1_IMPLEMENTATION.md` (schéma ER, décisions, migrations). | Document | — |

---

## 4. Risques et points d'attention

| Risque | Mitigation |
|---|---|
| **Migration 34→35 lourde** (mapping Long→UUID sur données existantes) | Colonne `legacy_id` conservée, UUID nullable puis backfill asynchrone. Tests instrumentés upgrade+downgrade obligatoires. |
| **Casser les 40 tests existants** | Sprint 2 priorise la non-régression. Tout use case refactoré doit garder ses tests verts. |
| **UI bottom nav = changement structurel majeur** | Les anciennes routes (Forets, Settings, etc.) restent accessibles pendant la transition (feature flag `lot1_bottom_nav`). |
| **Stub Missions/Carte/Compte** | Marquer clairement « à venir » dans l'UI. Pas de fausse promesse. |
| **RFC contrat de données manquante** | Amorcer RFC-GEOSYLVA-DATA-CONTRACT dès le Sprint 1 (avant le code métier). |

---

## 5. Critères d'acceptation Lot 1

- [ ] Migration 34→35 passe (upgrade + downgrade) sur émulateur
- [ ] Nouvelles entités (Observation, Measurement, Evidence, CalculationRun) persistées et queryables
- [ ] UUID présents sur toutes les nouvelles entités + backfill sur l'existant
- [ ] Provenance normalisée sur forêt/parcelle/placette
- [ ] Catalogue d'unités + conversions testés
- [ ] Journal d'événements opérationnel
- [ ] BackupService implémenté (export + import) — dette Lot 0 résorbée
- [ ] Bottom nav 5 entrées fonctionnelle (Accueil/Missions/Carte/Explorer/Compte)
- [ ] Accueil affiche les 4 blocs (en-tête, Reprendre, Aujourd'hui, Projets récents)
- [ ] Explorer affiche les 13 catégories + recherche globale
- [ ] Fiche projet + Fiche forêt refondue navigables
- [ ] Wizards création forêt/parcelle/placette fonctionnels bout-en-bout
- [ ] Parcelles/Placettes enrichis (carte, filtres, provenance, sync)
- [ ] 40 tests existants toujours verts + nouveaux tests Lot 1 verts
- [ ] Build debug APK généré + installé sur émulateur sans crash
- [ ] `docs/LOT1_IMPLEMENTATION.md` rédigé
- [ ] RFC-GEOSYLVA-DATA-CONTRACT amorcée (Draft)

---

## 6. Estimation relative

| Sprint | Complexité | Bloquant pour |
|---|---|---|
| Sprint 1 (modèle) | Élevée (migration + schéma) | Tous les suivants |
| Sprint 2 (bridge) | Moyenne | Sprint 3-5 |
| Sprint 3 (Accueil/Explorer) | Moyenne | Sprint 4 |
| Sprint 4 (Projets/Forêt/Wizards) | Élevée (UI + logique wizard) | Sprint 5 |
| Sprint 5 (Enrichissement + QA) | Moyenne | — |

**Recommandation** : Sprint 1 d'abord (fondation), puis Sprint 2 et 3
peuvent partiellement se chevaucher. Sprint 4 après validation Sprint 3.

---

## 7. Décisions à valider par le Fondateur

1. **Stratégie UUID** : colonne `legacy_id` + backfill asynchrone (proposé) vs migration destructive one-shot ?
2. **Feature flag bottom nav** : garder l'ancienne navigation accessible pendant Lot 1 (proposé) ou casser immédiatement ?
3. **Stubs Missions/Carte/Compte** : afficher « à venir » (proposé) ou masquer les entrées jusqu'à Lot 3/7/4 ?
4. **RFC contrat de données** : amorcer une nouvelle RFC dédiée (proposé) ou étendre une RFC existante ?
5. **BackupService** : implémenter en Lot 1 (proposé, dette Lot 0) ou reporter ?
