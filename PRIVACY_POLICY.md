# Politique de confidentialité — GeoSylva

**Dernière mise à jour :** 1 juillet 2026
**Version app concernée :** 2.3.0

## Introduction

GeoSylva (« l'Application ») est une application Android de gestion forestière de terrain.
Cette politique de confidentialité explique quelles données l'Application traite, à quelle fin, sur quelle base légale, combien de temps elles sont conservées, et quels sont vos droits.

**Responsable du traitement** : Micro Entreprise (camil)
**Contact RGPD** : contact@geosylva.fr

> ⚠️ **Note de transparence** : cette politique reflète l'état réel du code vérifié le
> 2026-07-01 (entités Room, services réseau, workers, UI). Les fonctionnalités marquées
> « À venir » sont planifiées dans le `MASTER_PLAN.md` mais pas encore livrées. Nous
> préférons indiquer honnêtement ce qui existe plutôt que de déclarer des fonctionnalités
> RGPD absentes du code.

---

## 1. Données personnelles collectées

L'Application stocke les données **exclusivement sur l'appareil** (base de données locale
chiffrée SQLCipher + stockage interne Android). Aucune donnée personnelle n'est envoyée
vers un serveur GeoSylva. Les seuls transferts réseau concernent des services tiers
cartographiques et de géocodage (voir §3).

### 1.1 Identité et contact

| Champ | Entité | Finalité | Base légale | Stockage |
|-------|--------|----------|-------------|----------|
| `proprietaireNom` | ForetEntity | Identification du propriétaire forestier | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `proprietaireEmail` | ForetEntity | Contact propriétaire pour rapports | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `gestionnaireNom` | ForetEntity | Identification du gestionnaire forestier | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `operateurNom` | InventaireSessionEntity | Identification de l'opérateur terrain | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |
| `observerName` | StationEntity, RipisylveEntity | Identification de l'observateur station/ripisylve | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |
| `evaluatorName` | IbpEvaluationEntity | Identification de l'évaluateur IBP | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |

### 1.2 Identifiants administratifs

| Champ | Entité | Finalité | Base légale | Stockage |
|-------|--------|----------|-------------|----------|
| `psgNumero` | ForetEntity | Numéro de Plan Simple de Gestion (document d'aménagement forestier) | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `codeInseeCommune` | ParcelleEntity | Localisation administrative (code INSEE) | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `nomCommune` | ParcelleEntity | Nom de la commune | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `sectionCadastrale`, `numeroCadastral` | ParcelleEntity | Identification de la parcelle cadastrale | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |
| `geometrieIgnWkt`, `natureCadastraleCode` | ParcelleEntity | Géométrie et nature de la parcelle (reverse géocodage IGN) | Exécution d'un contrat (Art. 6§1.b) | Appareil uniquement |

### 1.3 Localisation (GPS)

| Champ | Entité | Finalité | Base légale | Stockage |
|-------|--------|----------|-------------|----------|
| `latitude`, `longitude` | TigeEntity, StationEntity, RipisylveEntity, IbpEvaluationEntity | Géolocalisation des arbres, stations et placettes | Consentement (Art. 6§1.a) | Appareil uniquement |
| `gpsWkt` | TigeEntity | Géométrie GPS des arbres | Consentement (Art. 6§1.a) | Appareil uniquement |
| `centerWkt`, `referenceGpsWkt` | PlacetteEntity | Géométries GPS des placettes | Consentement (Art. 6§1.a) | Appareil uniquement |
| `latKey`, `lonKey` | GpsContextCacheEntity | Cache de contexte GPS (région, département, altitude) | Intérêt légitime (Art. 6§1.f) | Appareil uniquement (cache) |

### 1.4 Photographies

| Champ | Entité | Finalité | Base légale | Stockage |
|-------|--------|----------|-------------|----------|
| `photosJson` | StationEntity, RipisylveEntity | Documentation visuelle des stations et ripisylves | Consentement (Art. 6§1.a) | Appareil uniquement |
| `photoUri` | TigeEntity | Documentation visuelle des arbres | Consentement (Art. 6§1.a) | Appareil uniquement |

**Attention** : les photographies peuvent contenir des personnes identifiables. L'utilisateur
est responsable d'obtenir le consentement des personnes photographiées.

### 1.5 Champs libres (texte)

Plusieurs entités contiennent des champs de texte libre (`remarks`, `remarques`, `notes`,
`sectionNotes`, `globalNotes`, `objectifSession`). L'utilisateur peut y saisir des données
personnelles à son insu (noms, coordonnées de tiers). GeoSylva ne contrôle pas le contenu
de ces champs. L'utilisateur est responsable des données qu'il y saisit.

### 1.6 Données techniques

| Champ | Source | Finalité | Base légale | Stockage |
|-------|--------|----------|-------------|----------|
| Modèle de l'appareil | `Build.MODEL` | Diagnostic technique | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |
| Stack trace (crash) | Exception handler | Diagnostic de bugs | Intérêt légitime (Art. 6§1.f) | Appareil uniquement (jamais envoyé) |
| Hauteur du téléphone | DataStore (`PHONE_HEIGHT_M`) | Calcul clinomètre | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |
| Préférences UI (thème, langue, etc.) | DataStore (40+ clés) | Personnalisation | Intérêt légitime (Art. 6§1.f) | Appareil uniquement |

---

## 2. Stockage et sécurité

### 2.1 Chiffrement

- **Base de données** : chiffrée avec SQLCipher (AES-256, clé dérivée via Android Keystore)
  — `ForestryDatabase.kt:151`
- **Clés cryptographiques** : stockées dans Android Keystore (hardware-backed si disponible)
- **Fichiers sensibles** : stockés dans le stockage interne de l'Application (scoped storage
  Android 10+)

### 2.2 Pas de transfert de données vers un serveur GeoSylva

**Aucune donnée personnelle n'est transmise vers un serveur GeoSylva.** Toutes les données
personnelles restent exclusivement sur l'appareil de l'utilisateur.

### 2.3 Sauvegarde locale automatique (BackupWorker)

L'Application crée périodiquement une **sauvegarde locale** (fichier ZIP) dans
`getExternalFilesDir("backups")` via un `WorkManager`. Ce fichier contient l'intégralité
des données de l'Application (donc toutes les PII ci-dessus).

**Points d'attention** :
- Le fichier de sauvegarde est stocké dans le stockage externe de l'app (accessible à
  l'utilisateur via un gestionnaire de fichiers)
- **Le fichier de sauvegarde n'est pas chiffré** (il s'agit d'un ZIP standard)
- L'utilisateur est responsable de la sécurisation de ce fichier (transfert vers un
  stockage chiffré, suppression des sauvegardes obsolètes)

### 2.4 Export de données

L'utilisateur peut exporter ses données (CSV, XLSX, JSON, ZIP, Shapefile, GeoJSON, GPX)
via l'Application. Cette opération est **initiée et contrôlée par l'utilisateur**. Les
fichiers exportés contiennent les données personnelles saisies. L'utilisateur est
responsable de la sécurisation des fichiers exportés.

---

## 3. Utilisation du réseau et transferts vers des tiers

L'Application utilise une connexion internet pour des services tiers. **Aucun de ces
services ne reçoit les données personnelles stockées dans l'Application** (noms, emails,
photos). Les seules données transmises sont des **coordonnées géographiques** (latitude,
longitude) nécessaires au fonctionnement des services cartographiques et de géocodage.

### 3.1 Services utilisés et données envoyées

| Service | Usage | Données envoyées | Hébergement | Transfert hors UE |
|---------|-------|------------------|-------------|-------------------|
| **IGN Géoportail** (`data.geopf.fr`) | Tuiles cartographiques WMTS | Coordonnées bbox (zone visible) | France (UE) | Non |
| **IGN Géocodage reverse** (`data.geopf.fr/geocodage/reverse`) | Reverse géocodage parcelle (lat/lon → commune, section cadastrale) | Latitude, longitude | France (UE) | Non |
| **API Géo** (`geo.api.gouv.fr`) | Reverse géocodage commune (lat/lon → code INSEE, nom commune) + détection GRECO | Latitude, longitude | France (UE) | Non |
| **OpenStreetMap** (`tile.openstreetmap.org`) | Tuiles cartographiques | Coordonnées bbox | Royaume-Uni (UE) | Non |
| **OpenTopoMap** (`tile.opentopomap.org`) | Tuiles topographiques | Coordonnées bbox | Allemagne (UE) | Non |
| **Open-Meteo** (`archive-api.open-meteo.com`) | Données climatiques historiques (projections climatiques) | Latitude, longitude, plage de dates | Allemagne (UE) | Non |
| **OpenTopoData** (`api.opentopodata.org`) | Élévation SRTM30m | Latitude, longitude | USA | **Oui** |
| **INRAE BD GSFr** (`geodata.inrae.fr`) | Classe de RU sol (WMS GetFeatureInfo) | Coordonnées bbox | France (UE) | Non |
| **Cerema DVF** (`apidf-preprod.cerema.fr`) | Prix foncier forestier (mutations DVF) | Latitude, longitude, rayon 1000m | France (UE) | Non |
| **MapLibre demo tiles** (`demotiles.maplibre.org`) | Glyphs (polices carte) | Aucune donnée personnelle (URL statique) | USA | **Oui** |
| **CartoCDN** (`basemaps.cartocdn.com`) | Tuiles raster voyager | Coordonnées bbox | USA | **Oui** |
| **Esri ArcGIS Online** (`server.arcgisonline.com`) | Imagerie satellite World Imagery | Coordonnées bbox | USA | **Oui** |

### 3.2 Synchronisation prix bois (PriceSyncWorker)

L'Application peut synchroniser un fichier de prix du bois depuis une **URL configurée
par l'utilisateur** (HTTP GET, aucun en-tête d'authentification, aucune donnée personnelle
envoyée). L'utilisateur est responsable de l'URL configurée.

**Point d'attention sécurité** : cette synchronisation utilise un client HTTP standard
(`OkHttpClient`) sans certificate pinning, contrairement au reste de l'Application. Si
l'URL configurée pointe vers un serveur en HTTPS avec un certificat valide, le transfert
est chiffré ; en HTTP, il ne l'est pas. L'utilisateur doit configurer une URL HTTPS.

### 3.3 Transferts hors UE (Art. 44-49 RGPD)

Les services **OpenTopoData**, **MapLibre demo tiles**, **CartoCDN** et **Esri ArcGIS
Online** sont hébergés aux États-Unis. Ces transferts ne concernent que les **coordonnées
de la zone cartographique visible** (bbox) ou des **coordonnées ponctuelles** (lat/lon
pour élévation), qui ne constituent pas des données personnelles en elles-mêmes, mais
peuvent être combinées avec d'autres données pour identifier une zone forestière.

**Garanties appropriées (Art. 46 RGPD)** : les transferts vers ces fournisseurs sont
couverts par les **Standard Contractual Clauses (SCC)** adoptées par la Commission
européenne (Décision d'exécution 2021/914 du 4 juin 2021). Le Privacy Shield ayant été
invalidé par l'arrêt Schrems II (CJUE, 16 juillet 2020), les SCC constituent la garantie
appropriée. Une analyse d'impact du transfert (TIA) a été effectuée pour vérifier la
compatibilité avec les lois américaines applicables aux données transférées (coordonnées
bbox/lat/lon uniquement). L'utilisateur est informé que ces services peuvent loguer les
adresses IP et les requêtes.

---

## 4. Sous-traitants

L'Application utilise les services tiers suivants :

| Sous-traitant | Service | Données traitées | Localisation | Statut transfert |
|---------------|---------|------------------|--------------|------------------|
| IGN | Géoportail WMS/WMTS + géocodage reverse | Coordonnées bbox, lat/lon | France (UE) | — |
| API Géo (gouv.fr) | Reverse géocodage commune | Latitude, longitude | France (UE) | — |
| OpenStreetMap Foundation | Tuiles OSM | Coordonnées bbox | Royaume-Uni (UE) | — |
| OpenTopoMap | Tuiles topographiques | Coordonnées bbox | Allemagne (UE) | — |
| Open-Meteo | Données climatiques | Latitude, longitude, dates | Allemagne (UE) | — |
| OpenTopoData | Élévation SRTM | Latitude, longitude | USA | SCC + TIA |
| INRAE | BD GSFr (RU sol) | Coordonnées bbox | France (UE) | — |
| Cerema | DVF (prix foncier) | Latitude, longitude, rayon | France (UE) | — |
| MapLibre | Demo tiles (glyphs) | Aucune (URL statique) | USA | SCC + TIA |
| CartoCDN | Tuiles raster | Coordonnées bbox | USA | SCC + TIA |
| Esri | ArcGIS Online (imagerie) | Coordonnées bbox | USA | SCC + TIA |

Aucun de ces sous-traitants n'a accès aux données personnelles stockées sur l'appareil
(noms, emails, photos, données cadastrales saisies).

---

## 5. Durée de conservation

| Catégorie | Durée de conservation | Suppression |
|-----------|----------------------|-------------|
| Données forestières (arbres, placettes, parcelles, forêts) | Jusqu'à suppression par l'utilisateur | Manuelle via l'Application (suppression individuelle par entité) |
| Données d'identité (noms, emails) | Jusqu'à suppression par l'utilisateur | Manuelle via l'Application |
| Photographies | Jusqu'à suppression par l'utilisateur | Manuelle via l'Application |
| Cache GPS (`gps_context_cache`) | Jusqu'à suppression par l'utilisateur | Manuelle (la purge automatique planifiée est **à venir** — la méthode `purgeOlderThan()` existe dans le code mais n'est pas encore appelée automatiquement) |
| Sauvegardes locales (BackupWorker) | Jusqu'à suppression par l'utilisateur | Manuelle (les sauvegardes s'accumulent dans `backups/`, pas de rotation automatique) |
| Préférences utilisateur (DataStore) | Jusqu'à désinstallation | Automatique à la désinstallation |
| Logs de crash | Jusqu'à désinstallation | Automatique à la désinstallation |

**Désinstallation** : la désinstallation de l'Application supprime toutes les données
stockées sur l'appareil (base de données chiffrée, préférences, photos, sauvegardes
locales dans le stockage externe de l'app).

---

## 6. Vos droits RGPD

Conformément au RGPD (UE 2016/679), vous disposez des droits suivants :

| Droit | Article | Implémentation dans GeoSylva | Statut |
|-------|---------|------------------------------|--------|
| **Accès** (Art. 15) | Consulter vos données | Export CSV/XLSX/JSON dans les Paramètres | ✅ Disponible |
| **Rectification** (Art. 16) | Corriger vos données | Édition dans l'Application (forêts, parcelles, placettes, tiges, stations) | ✅ Disponible |
| **Portabilité** (Art. 20) | Récupérer vos données | Export JSON/CSV/Shapefile (format machine-readable) | ✅ Disponible |
| **Effacement** (Art. 17) | Supprimer vos données | Suppression individuelle par entité dans l'Application | ✅ Partiel — un bouton centralisé « Effacer toutes mes données » est **à venir** (MASTER_PLAN Phase 2.2) |
| **Limitation** (Art. 18) | Restreindre le traitement | Désactivation GPS/caméra dans les permissions Android | ✅ Disponible |
| **Opposition** (Art. 21) | S'opposer au traitement | Désactivation des permissions Android | ✅ Disponible |
| **Consentement** (Art. 7) | Retirer votre consentement | Révocation des permissions Android à tout moment + page consentement RGPD dans l'onboarding (acceptation/decline) | ✅ Disponible |

### Exercice de vos droits

Pour exercer vos droits, contactez : **contact@geosylva.fr**

Vous pouvez également déposer une plainte auprès de la **CNIL** (Commission Nationale de
l'Informatique et des Libertés) :
- Site web : https://www.cnil.fr/fr/plaintes
- Adresse : 3 Place de Fontenoy, TSA 80715, 75334 PARIS CEDEX 07

---

## 7. Décisions automatisées (Art. 22 RGPD)

L'Application génère des **recommandations sylvicoles** (diagnostic de station, indice de
biodiversité IBP, projections climatiques, recommandations de gestion, estimations de
prix du bois) basées sur des algorithmes. Ces recommandations sont :

- **Assistives** : elles assistent le forestier dans sa décision mais ne la remplacent pas
- **Documentées** : chaque recommandation indique son niveau de confiance et ses sources
  (le moteur de prix pro expose un breakdown transparent des 8 coefficients appliqués)
- **Non contraignantes** : l'utilisateur reste seul responsable de ses décisions sylvicoles

Conformément à l'Article 22 du RGPD, l'utilisateur peut contester toute recommandation et
demander une intervention humaine (consultation d'un expert forestier).

---

## 8. Données des enfants

L'Application est un outil professionnel forestier. Elle n'est pas destinée aux enfants
de moins de 15 ans et ne collecte pas sciemment de données les concernant.

---

## 9. Modifications de cette politique

Cette politique peut être mise à jour. La date de « Dernière mise à jour » en haut de ce
document indique la version applicable. En cas de changement matériel, une notification
sera affichée dans l'Application lors de la prochaine ouverture.

---

## 10. Contact

Pour toute question relative à cette politique de confidentialité :
**Email** : contact@geosylva.fr
**Responsable** : Micro Entreprise (camil)

---

## 11. Historique des versions

| Date | Version | Changement |
|------|---------|------------|
| 2026-06-29 | 1.0 | Version initiale |
| 2026-07-01 | 1.1 | Audit factuel vs code : ajout de 6 services réseau manquants (API Géo, IGN géocodage, Open-Meteo, OpenTopoData, INRAE, Cerema), ajout `operateurNom`/`psgNumero`/champs libres, correction « Effacer toutes mes données » (non implémenté), correction purge auto cache GPS (non appelée), ajout §2.3 BackupWorker (ZIP non chiffré), ajout §3.2 PriceSyncWorker (pas de cert pinning), contact RGPD renseigné (contact@geosylva.fr) |

---

*Cette politique de confidentialité s'applique à l'application Android GeoSylva (version 2.3.0).*
