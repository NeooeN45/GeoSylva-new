# GeoSylva ↔ GSIE — Intégration au jumeau numérique environnemental fédéré

| Champ | Valeur |
|---|---|
| **Application** | GeoSylva |
| **Rôle** | Projection métier forestière du jumeau numérique GSIE |
| **Statut** | Draft — cadrage d'intégration |
| **Référence** | RFC-0037, `GSIE/ARCHITECTURE/GSIE_ENVIRONMENTAL_DIGITAL_TWIN_PLATFORM.md` |

## 1. Positionnement

GeoSylva reste une application forestière **offline-first**. Elle peut
fonctionner entièrement seule en forêt. Lorsqu'une connexion est
disponible, elle devient une projection forestière du jumeau numérique
GSIE et publie des observations, mesures, diagnostics et résultats
sylvicoles versionnés.

GeoSylva ne devient pas dépendante d'Ignis, Hydro ou du Hub Unreal. Les
échanges inter-domaines passent par les contrats GSIE et non par un accès
à la base interne d'une autre application.

## 2. Données publiées par GeoSylva

- peuplements et structures forestières ;
- essences, strates et observations dendrométriques ;
- martelages et interventions ;
- état sanitaire et biodiversité ;
- biomasse et rendement estimés ;
- observations terrain géolocalisées ;
- provenance, méthode, précision, confiance et horodatage.

Chaque valeur distingue observation, calcul, estimation et proposition.

## 3. Consommations inter-domaines

GeoSylva peut consommer, selon les droits et la disponibilité locale :

- contours et impacts d'incendie produits par Ignis ;
- données hydrologiques et risques de ruissellement produits par Hydro ;
- observations de végétation produites par Flora ;
- observations d'habitats produites par Artemis ;
- connaissances et modèles qualifiés par GSIE.

Les données reçues sont mises en cache localement et restent identifiées
par leur source et leur version.

## 4. Scénarios forestiers

Un changement d'essence, une intervention ou une hypothèse de rendement
est d'abord un scénario. Il ne modifie pas l'état réel de la forêt sans
validation explicite et intégration versionnée.

```text
État réel de la parcelle
  ├── évolution sans intervention
  ├── changement d'essence
  ├── restauration post-incendie
  └── scénario de rendement
```

## 5. Hub GeoSylva

Le Hub GeoSylva permet d'explorer la forêt dans le temps, de comparer les
scénarios et de consulter les observations terrain. Il consomme le
contrat Hub commun ; il ne calcule pas dans Unreal les modèles forestiers
qui appartiennent aux moteurs GSIE ou aux calculateurs locaux validés.

## 6. Contraintes

- aucun réseau requis pour le cœur terrain ;
- aucune perte silencieuse lors de la synchronisation ;
- aucune donnée sensible publiée sans autorisation ;
- aucune fusion de géométries provenant de sources différentes sans règle
  explicite ;
- toute recommandation reste contournable par le forestier.
