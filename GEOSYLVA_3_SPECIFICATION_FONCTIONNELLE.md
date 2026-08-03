# GEOSYLVA-003 — Spécification fonctionnelle et scientifique GeoSylva 3.0

| Champ | Valeur |
|---|---|
| Identifiant | GEOSYLVA-003 |
| Statut | Draft |
| Version | 0.1.0 |
| Date | 2026-08-03 |
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

## 13. Références

### 13.1 Documents GeoSylva

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

### 13.2 Documents GSIE

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

### 13.3 Référentiels externes

- [ONF] Office national des forêts, référentiels et méthodes sylvicoles :
  <https://www.onf.fr/>.
- [IGN-CARTO] IGN, API et services cartographiques :
  <https://geoservices.ign.fr/documentation/services/api-et-services-ogc/api-carto-rest>.
- [IGN-BDFORET] IGN, BD Forêt : <https://foret.ign.fr/IGD/fr/ressources>.

## 14. Historique

| Version | Date | Modification |
|---|---|---|
| 0.1.0 | 2026-08-03 | Création de la liste fonctionnelle et de la doctrine scientifique issue du brainstorming validé. |
| 0.2.0 | 2026-08-03 | Roadmap structurée (§12) : architecture cible, cascade LLM multi-tier, connexion GSIE Serveur (moteurs et contrats), 8 phases, décisions/RFC requises, critères de sortie. Sources consolidées (§13). |

