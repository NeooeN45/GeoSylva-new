# CLAUDE.md — GeoSylva

> Règles locales pour les agents IA travaillant sur ce dépôt.

---

## 1. Ce qu'est ce dépôt

GeoSylva est une application Android d'inventaire forestier et de
martelage, **spécialisation forestière de l'écosystème Quintessences**
(propulsé par le moteur GSIE). C'est un dépôt git **indépendant**
(voir `Quintessences/CLAUDE.md` §10 « Repos externes intégrés ») — pas
soumis à la Constitution/RFC/gouvernance GSIE au sens strict, mais
client de l'écosystème et attendu cohérent avec ses principes.

Documentation de référence :
- `README.md` — présentation, architecture, stack technique
- `docs/VOLUME_CALCULATION_NEXT_GEN.md` — vision moteur de volume nouvelle
  génération (LiDAR, photogrammétrie, IA on-device, mesh LoRa terrain)
- `MASTER_PLAN.md`, `RESEARCH_OPPORTUNITIES.md` — plans et pistes

## 2. Principe fondateur : offline-first

GeoSylva fonctionne **entièrement seule**, sans réseau. Les algorithmes
de cubage (tarifs, Schumacher-Hall, etc.) et les futurs modèles IA
on-device tournent localement. Aucune fonctionnalité cœur ne doit
dépendre d'une connexion internet.

## 3. Articulation avec GSIE — trois canaux réseau distincts

Le principe (confirmé par le Fondateur, 2026-07-17) distingue trois
canaux de communication, à ne jamais confondre dans le code ou la doc :

1. **GeoSylva ↔ GSIE serveur** : uniquement en bonne condition réseau
   (Wi-Fi/4G stable). Sert à **augmenter la capacité de réflexion** —
   déléguer aux moteurs GSIE lourds (Correlation, Reasoning, Diagnostic)
   ce que le téléphone ne peut pas calculer seul, et à récupérer des
   connaissances sourcées (coefficients scientifiques vérifiés, voir §4)
   depuis le Knowledge Engine. Ce n'est **pas** un simple canal de
   synchronisation de sauvegarde — c'est un canal d'amplification.
2. **Technicien ↔ technicien (Bluetooth)** : proximité immédiate,
   partage terrain instantané entre deux appareils.
3. **Technicien ↔ technicien / GSIE (LoRa mesh)** : portée longue,
   bas débit, sans infrastructure. Synchronisation de données entre
   techniciens sur le terrain, et à terme remontée vers un serveur/PC
   GSIE en LoRa. Rejoindre un mesh d'équipe se fait par QR code (clé
   partagée par le chef d'équipe — voir
   `docs/VOLUME_CALCULATION_NEXT_GEN.md` §17, `encodeTeamKeyQr`).

Ces trois canaux sont déjà posés au niveau architecture réseau générique
dans **RFC-0003** (`Quintessences/02_RFC/RFC-0003.md`, statut *Proposé*)
— stack GSIE-Net en couches (physique/transport/GSIE-Net/métier). Toute
évolution de cette architecture réseau doit rester cohérente avec
RFC-0003 ; la valider/amender relève du Fondateur (Quintessences), pas
de ce dépôt.

## 4. Garde-fou scientifique — même discipline qu'ADR-007 (GSIE)

Le Knowledge Engine GSIE applique ADR-007 : aucun coefficient
scientifique n'est inventé, tout est sourcé. GeoSylva n'est pas
contractuellement lié à cet ADR, mais **doit appliquer le même principe
en pratique** : les coefficients dendrométriques (Schaeffer, Algan, IFN
dans `TarifData.kt`) sont déjà bien sourcés (citations en en-tête). Un
écart connu existe : `ExpertForestryCalculator.kt` fonction
`getSchumacherHallParameters()` — coefficients par essence (chêne,
hêtre, sapin) **sans citation**, à corriger en priorité (soit sourcer
depuis une vraie publication INRAE/IGN, soit brancher sur le Knowledge
Engine GSIE une fois ses propres coefficients sourcés).

## 5. Conventions

- Documentation et commentaires en français, cohérent avec le reste de
  Quintessences.
- Ne pas dupliquer indéfiniment la science forestière en dur dans le
  code Kotlin — à terme, les coefficients sourcés devraient venir du
  Knowledge Engine GSIE (canal 1, §3), avec cache local pour le mode
  hors-ligne.
- Avant toute modification du moteur de volume, lire
  `docs/VOLUME_CALCULATION_NEXT_GEN.md` — c'est un document de vision
  (4458 lignes), pas un plan d'implémentation immédiat : introduire les
  nouvelles stratégies de façon additive (pattern Strategy), sans
  jamais remplacer les calculateurs existants qui fonctionnent déjà en
  production.

## 6. Rappel final

> GeoSylva doit rester utilisable seule, en forêt, sans réseau. Le
> serveur GSIE et le mesh LoRa sont des amplificateurs de capacité,
> jamais des dépendances.
