# GEOSYLVA-003 — Spécification fonctionnelle et scientifique GeoSylva 3.0

| Champ | Valeur |
|---|---|
| Identifiant | GEOSYLVA-003 |
| Statut | Draft |
| Version | 0.5.0 |
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

### 3.1 Donnée et base locale

GeoSylva est un client de saisie et de consultation d’une base locale, pas une
collection d’écrans qui recalculent leur propre état. Toutes les écritures passent
par un domaine de données transactionnel et versionné. Chaque objet doit avoir :

- un identifiant stable et son parent (projet, forêt, parcelle, placette,
  martelage ou tige) ;
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
│  ├─ LLM on-device léger (SmolLM3 3B / Phi-3-mini)           │
│  │   → assistance vocale, explication, jamais calcul        │
│  └─ Cache local : packs, référentiels, connaissances GSIE   │
├─────────────────────────────────────────────────────────────┤
│  Canal 1 — GSIE Serveur (Wi-Fi/4G stable)                    │
│  ├─ Moteurs lourds : Correlation, Reasoning, Diagnostic,    │
│  │   Recommendation, Forest Dynamics, Simulation            │
│  ├─ LLM serveur (Mistral 7B / Phi-4-reasoning via vLLM)     │
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

| Tier | Modèle cible | Rôle | Réseau | Latence cible |
|---|---|---|---|---|
| **T1 — Mobile** | SmolLM3 3B (quantifié INT4) ou Phi-3-mini 4B | Assistance vocale, explication des calculs locaux, saisie contextuelle, identification essence (TFLite) | Aucun | < 500 ms |
| **T2 — Edge** | Mistral 7B (NVIDIA NIM dev / Jetson) | RAG sur documentation forestière, raisonnement intermédiaire, cascade si T1 insuffisant | Wi-Fi local | < 3 s |
| **T3 — Serveur** | Phi-4-reasoning 14B (vLLM) ou Mistral 7B servé | Raisonnement profond via moteurs GSIE, diagnostic, recommandation, simulation | 4G/Wi-Fi | < 10 s |

**Règles de cascade** :

- T1 répond seul tant que la question reste dans le périmètre des calculs
  locaux et des connaissances cachées. Aucune donnée n'est envoyée au serveur
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

- Chaque appel expose : `requete_id`, `session_id`, `auteur`, `device_id`,
  `source` (manual | sync | gps), `version` des données envoyées.
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

### 12.4 Phases de réalisation

Chaque phase produit ses tests, preuves et décision de validation (DEC).
L'ordre respecte les dépendances : une phase ne démarre que si la précédente
est au minimum en Review.

| Phase | Livrables | Dépendances | Décisions requises |
|---|---|---|---|
| **P0 — Fondations** | Corrections audits bloquants (Hdom, indice station, SQLCipher, certificate pinning, RGPD) + contrat de données (métadonnées §3.1, migration v34 amorcée) + tests de restauration | — | DEC corrections audits |
| **P1 — Création guidée** | Forêt/parcelle/placette avec questionnaires (§5), contrôles de surface, provenance du choix, recherche tolérante | P0 | — |
| **P2 — Martelage persistant** | Session de martelage (§6.3) complète, modes classique/vocal/hybride, métriques de session, instantané immuable post-martelage | P1 | DEC format session |
| **P3 — Moteurs scientifiques locaux** | Fiches méthodes versionnées (§7.2), qualité bois, pathogènes, incertitudes, séparation observation/calcul, tests de référence | P2 | RFC fiches méthodes |
| **P4 — Connexion GSIE Serveur** | SDK Kotlin, contrats API moteurs (Correlation, Reasoning, Diagnostic, Recommendation, Forest Dynamics), cache local, pull serveur→mobile, résolution conflits, analyse GSIE approfondie | P3 | RFC contrats GeoSylva ↔ moteurs |
| **P5 — LLM on-device et multi-tier** | Modèle T1 embarqué (SmolLM3/Phi-3), RAG local, cascade T1→T2→T3, assistant vocal, identification essence on-device, garde-fous ADR-009 | P4 | RFC IA forestière on-device |
| **P6 — Synchronisation terrain** | Bluetooth (canal 2), QR code team key, Meshtastic (canal 3), paquets signés, journal de fusion, reprise après interruption | P4 | RFC sync terrain |
| **P7 — Refonte visuelle** | Onboarding, animation, packs hors ligne, optimisations batterie/accessibilité, mode économie, mode pluie | P1-P6 | — |

**Phases parallélisables** : P7 (refonte visuelle) peut démarrer dès P1
terminé et progresser en parallèle de P2-P6. Les autres phases sont
séquentielles.

### 12.5 Décisions et RFC à produire

| # | Document | Objet | Phase déclencheuse |
|---|---|---|---|
| 1 | DEC — corrections audits | Hdom, indice station, SQLCipher, certificate pinning, RGPD | P0 |
| 2 | DEC — format session martelage | Structure session, événements, instantané immuable | P2 |
| 3 | RFC — fiches méthodes versionnées | Format fiche méthode (§7.2), registre, versionnement | P3 |
| 4 | RFC — contrats GeoSylva ↔ moteurs GSIE | Contrats d'interface par moteur, cache, résolution conflits, pull | P4 |
| 5 | RFC — IA forestière on-device | Architecture multi-tier, choix modèles, RAG, cascade, garde-fous | P5 |
| 6 | RFC — synchronisation terrain | Bluetooth, QR, Meshtastic, paquets signés, journal de fusion | P6 |

### 12.6 Critères de sortie GeoSylva 3.0

Repris et étendus depuis §11 :

- Aucune perte de donnée dans les scénarios offline et de fusion.
- 100 % des méthodes opérationnelles sourcées et testées (§7).
- Aucun résultat sans provenance, unité et incertitude (ADR-009).
- Parcours terrain complet démontré sur les profils d'appareils supportés.
- Le cœur forestier fonctionne sans réseau (canal 1 absent).
- Toute sortie LLM cite le moteur ou la source qu'elle invoque (ADR-009).
- Le forestier peut contournée toute recommandation et tracer sa décision
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

Le Dev Pack (`21_EXPERIMENTS/GEOSYLVA_DEV_PACK_2026-08-04/`,
`10_ROADMAP_IMPLEMENTATION.md`) propose une vision long terme en 10 phases
(0-9) qui s'étend au-delà de la roadmap GeoSylva 3.0 (§12.4, P0-P7). Ces
phases couvrent la transformation de GeoSylva en poste de travail numérique
complet du technicien forestier. Elles sont **complémentaires** de la roadmap
existante — les phases P0-P7 de §12.4 restent le plan d'exécution immédiat.

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

### 14.1 Principe

GeoSylva 3.0 ne réimplémente pas la science forestière côté serveur. Elle
délègue aux moteurs GSIE via le **canal 1** (Wi-Fi/4G stable) et consomme
leurs contrats documentés (`GSIE/ENGINES/*/`, `ENGINE_INTERFACE_CONTRACTS.md`).
La spécification détaillée des contrats d'interface fait l'objet de la
**RFC-0033** (§12.5, Phase P4). Cette section pose le cadre et les formats
communs ; la RFC détaillera les endpoints REST, les codes d'erreur et les
schémas JSON complets.

### 14.2 Enveloppe commune de requête

Toute requête GeoSylva → GSIE porte une enveloppe commune garantissant la
traçabilité (ADR-009, GSIE-CON-005) :

```text
GeoSylvaRequest = {
  requete_id     : UUID          — généré côté mobile, idempotence
  session_id     : UUID          — session de martelage courante (§6.3)
  auteur         : texte         — identifiant compte Quintessences
  device_id      : texte         — identifiant appareil (Android ID hashé)
  source         : enum { manual, sync, gps }
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

### 15.1 Vision consolidée

Cette section consolide sans les réinventer les visions existantes :
`VOLUME_CALCULATION_NEXT_GEN.md` §10 (multi-tier LLM),
`RESEARCH_OPPORTUNITIES.md` §3 (stack IA séquencée),
`VISION_LLM_SPECIALISES_GSIE_CORE` (adaptateurs LoRA, famille de modèles),
`RFC-0019` (gsie-ai-gateway serveur). La spécification opérationnelle
détaillée (formats de prompts, schéma RAG, stratégie de quantification,
banc d'évaluation) fait l'objet de la **RFC-0034** (§12.5, Phase P5).

### 15.2 Architecture multi-tier

```text
┌──────────────────────────────────────────────────────────────┐
│  Tier 1 — Mobile (on-device, offline)                        │
│  Modèle    : SmolLM3 3B (quantifié INT4) ou Phi-3-mini 4B     │
│  Runtime   : ONNX Runtime / llama.cpp Android                 │
│  Mémoire   : < 2 GB stockage, < 1.5 GB RAM au runtime         │
│  Rôle      : assistance vocale, explication des calculs      │
│              locaux, saisie contextuelle, identification     │
│              essence (TFLite + PureForest)                    │
│  Réseau    : aucun                                            │
│  Latence   : < 500 ms (génération courte)                     │
│  Garde-fou : n'invoque jamais un moteur GSIE (pas de réseau) │
│              → explique les résultats locaux, ne calcule pas │
├──────────────────────────────────────────────────────────────┤
│  Tier 2 — Edge (Wi-Fi local, Jetson / NIM dev)                │
│  Modèle    : Mistral 7B (quantifié AWQ 4-bit)                │
│  Runtime   : vLLM ou NIM sur Jetson Orin / poste GSIE PC      │
│  Rôle      : RAG sur documentation forestière locale,        │
│              raisonnement intermédiaire, cascade si T1        │
│              insuffisant                                      │
│  Réseau    : Wi-Fi local (terrain, base vie)                 │
│  Latence   : < 3 s                                            │
│  Garde-fou : peut invoquer les moteurs GSIE locaux (cache)   │
│              → cite le moteur, ne calcule pas de mémoire      │
├──────────────────────────────────────────────────────────────┤
│  Tier 3 — Serveur (4G/Wi-Fi, gsie-ai-gateway RFC-0019)        │
│  Modèle    : Phi-4-reasoning 14B (vLLM) ou Mistral 7B servé   │
│  Runtime   : vLLM sur GPU serveur (RFC-0019)                  │
│  Rôle      : raisonnement profond via moteurs GSIE,          │
│              diagnostic, recommandation, simulation,         │
│              RAG scientifique (pgvector, /ai/research)        │
│  Réseau    : 4G/Wi-Fi stable                                  │
│  Latence   : < 10 s                                           │
│  Garde-fou : invoque toujours un moteur pour toute valeur     │
│              numérique forestière (ADR-009)                  │
└──────────────────────────────────────────────────────────────┘
```

### 15.3 Règles de cascade

1. **T1 répond seul** tant que la question reste dans le périmètre des
   calculs locaux et des connaissances cachées. Aucune donnée n'est
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

- Modèle TFLite/ONNX embarqué, entraîné sur PureForest dataset IGN
- Classification des essences françaises les plus courantes (~50 espences
  en première tranche)
- Quantification INT8 pour Android (taille < 50 MB)
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

- Pack « Assistant terrain FR » : SmolLM3 3B INT4 + index RAG local +
  modèle TFLite identification essences (~500 MB total)
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

### 15.10 Choix de modèles — synthèse

| Modèle | Taille | Licence | Tier | Cas d'usage | Statut |
|---|---|---|---|---|---|
| SmolLM3 3B | 3B | Apache 2.0 | T1 | Assistant terrain offline | Cible P5 |
| Phi-3-mini 4B | 3.8B | MIT | T1 (alternative) | Assistant léger | Cible P5 |
| Llama 3.2 3B | 3B | Llama 3.2 Community | T1 (alternative) | Assistant terrain offline | Étude |
| Mistral 7B | 7B | Apache 2.0 | T2 | RAG edge, raisonnement intermédiaire | Cible P5 |
| Phi-4-reasoning 14B | 14B | MIT | T3 | Raisonnement profond serveur | Différé (RFC-0031) |
| TFLite (PureForest) | < 50 MB | — | T1 | Identification essence on-device | Étude (RFC-0018) |

**Note** : vLLM + Phi-4-reasoning est explicitement différé (RFC-0031) car
le Reasoning Engine doit être spécifié avant changement d'inférence. Le
T3 utilise Mistral 7B servé en attendant.

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
| **Fonctionnels** | Inventaire avancé, martelage pro, valorisation, santé, travaux, DFCI, SIG avancé, IA locale, collaboration | Module martelage pro |
| **Géographiques** | Hiérarchie France → région → département → territoire → forêt → mission | Découpage départemental |
| **Cartographiques** | PMTiles, MBTiles, orthophotos, fond topographique, cadastre, DFCI, relief, couches forestières, MNT | Orthophoto IGN départementale |
| **Scientifiques** | Tarifs de cubage, équations, allométrie, biomasse, carbone, station, santé, sylviculture, produits | Tarifs ONF, équations Vallet et al. |
| **Organisationnels** | Protocoles privés, tarifs internes, couches privées, nomenclatures, modèles de rapports, paramètres, missions | Protocole de martelage ONF |
| **IA** | Reconnaissance d'essences, TreeVision, voix, OCR, assistant local, modèle sanitaire | Modèle TFLite PureForest |

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

**Dépendances** : RFC-0004 (QPIS Pack Format, §22 RFC prioritaires du Dev
Pack). ADR : packs signés, Room/SQLCipher conservé comme base locale métier.

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

12 métiers sont identifiés pour la première version :

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

| Mode | Usage | Précision | Vitesse |
|---|---|---|---|
| **Rapide** | Inventaire de reconnaissance, estimation préliminaire | Faible (vision simple) | Élevée |
| **Précis** | Inventaire officiel, martelage, données contractuelles | Élevée (scan multi-angle + instruments) | Modérée |
| **Calibration** | Étalonnage de l'appareil sur un arbre de référence connu | Référence | Lente |
| **Placette semi-automatique** | Scan d'une placette entière avec détection automatique des tiges | Modérée à élevée | Variable |

Le mode détermine le nombre de visées requises, le niveau de détail du scan,
la tolérance d'incertitude acceptée et les champs du formulaire de saisie
(§17.6 formulaires contextuels). Le technicien peut changer de mode en cours
de mission, mais le mode utilisé est **conservé avec chaque mesure** pour
traçabilité.

**Dépendances** : RFC-0007 (TreeVision Measurement Pipeline). Lien avec
§18.7 (indice de confiance), §17.6 (formulaires contextuels).

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
- stockage protégé par Android Keystore ;
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
migration vers Keycloak suit la procédure suivante :

1. **Premier login post-migration** : l'utilisateur ouvre GeoSylva mise à
   jour → l'app redirige vers Keycloak au lieu de Google directement.
2. **Liaison automatique** : Keycloak reconnaît le `sub` Google (déjà
   enregistré dans `ExternalIdentity`) → crée ou retrouve l'identité
   Quintessences → l'utilisateur est connecté sans action supplémentaire.
3. **Invitation passkey** : après migration, l'utilisateur est invité à
   enregistrer une passkey comme méthode principale (§20.2.1).
4. **Période de transition** : Google reste disponible comme fournisseur
   fédéré pendant toute la période de transition — l'authentification Google
   via Keycloak est transparente pour l'utilisateur.
5. **Fallback** : si l'utilisateur refuse la passkey, le mot de passe de
   compatibilité (§20.2.1) reste disponible.

Aucune donnée utilisateur n'est perdue : l'UUID Quintessences est créé à
partir de l'identité Google existante, et toutes les données GeoSylva
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

Les jetons sont stockés dans un espace protégé par Android Keystore. L'API
GSIE vérifie systématiquement : la signature, l'émetteur (`iss`), l'audience
(`aud`), l'expiration (`exp`), l'identifiant de session, les rôles ou
permissions, et l'organisation active.

## 21. Références

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

## 22. Historique

| Version | Date | Modification |
|---|---|---|
| 0.1.0 | 2026-08-03 | Création de la liste fonctionnelle et de la doctrine scientifique issue du brainstorming validé. |
| 0.2.0 | 2026-08-03 | Roadmap structurée (§12) : architecture cible, cascade LLM multi-tier, connexion GSIE Serveur (moteurs et contrats), 8 phases, décisions/RFC requises, critères de sortie. Sources consolidées (§16). |
| 0.3.0 | 2026-08-03 | §14 Connexion GSIE Serveur détaillée (enveloppes communes, moteurs, chaîne d'appel, cache, pull/conflits, SDK Kotlin, garde-fous). §15 LLM on-device et multi-tier (architecture 3 tiers, cascade, LoRA, RAG, identification on-device, assistant vocal, distribution, évaluation). |
| 0.4.0 | 2026-08-04 | Intégration du Dev Pack (brainstorming ChatGPT) : §16 QPIS, §17 Mission/Protocol Engine, §18 TreeVision, §19 Métiers/objets communs/architecture modulaire, §20 Identité fédérée Keycloak/OIDC. Vision long terme GeoSylva comme poste de travail numérique complet du technicien forestier. |
| 0.5.0 | 2026-08-04 | Vérification et complétion de l'intégration Dev Pack : §4.2 amendé (pointe vers §20 cible), §16.9 Droits et abonnements (Subscription ↔ QPIS), §17.9 Catalogue de protocoles, §18.10 Modes TreeVision, §20.2.1 Méthodes connexion Quintessences (passkey/TOTP/mot de passe compatibilité), §20.5 Interdictions Android, §20.9 Migration comptes existants, §20.10 Connexion entreprise (petite/grande structure), §20.11 Sécurité administrative, §20.12 Gestion des jetons. |

