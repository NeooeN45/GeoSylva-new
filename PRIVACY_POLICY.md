# Politique de confidentialité — GeoSylva

**Dernière mise à jour :** 3 août 2026
**Version app concernée :** 2.4.0 (identité et synchronisation optionnelle en développement)

## Introduction

GeoSylva (« l'Application ») est une application Android de gestion forestière de terrain.
Cette politique de confidentialité explique quelles données l'Application traite, à quelle fin, sur quelle base légale, combien de temps elles sont conservées, et quels sont vos droits.

**Responsable du traitement** : Micro Entreprise (camil)
**Contact RGPD** : contact@geosylva.fr

> ⚠️ **Note de transparence** : cette politique reflète l'état réel du code vérifié le
> 2026-08-03 (entités Room, services réseau, identité, workers, UI). Les fonctionnalités marquées
> « À venir » sont planifiées dans le `MASTER_PLAN.md` mais pas encore livrées. Nous
> préférons indiquer honnêtement ce qui existe plutôt que de déclarer des fonctionnalités
> RGPD absentes du code.

---

## 1. Données personnelles collectées

Les données forestières restent **locales par défaut** dans une base chiffrée
SQLCipher. Si l’utilisateur choisit de créer ou de connecter un compte
Quintessences, les données d’identité nécessaires sont transmises à l’API
GSIE (voir §1.7 et §3). La connexion seule n’envoie aucune donnée forestière.
Si l’utilisateur active ensuite explicitement la synchronisation, les données
de parcelles décrites au §1.8 sont transmises ; les tiges, placettes, photos et
diagnostics restent locaux dans cette tranche.

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

### 1.7 Compte Quintessences facultatif

| Donnée | Finalité | Base légale | Stockage |
|---|---|---|---|
| Adresse e-mail normalisée | Création du compte et connexion locale | Exécution du service demandé (Art. 6§1.b) | Serveur GSIE, périmètre d’identités isolé |
| Nom affiché facultatif | Personnalisation du compte | Exécution du service demandé (Art. 6§1.b) | Serveur GSIE |
| Identifiant canonique, fournisseur et rôles | Session commune à l’écosystème Quintessences | Exécution du service demandé (Art. 6§1.b) | Serveur GSIE et métadonnées locales chiffrées |
| Identifiant externe Google (`issuer`, `sub`) | Connexion Google choisie par l’utilisateur | Exécution du service demandé (Art. 6§1.b) | Serveur GSIE |
| Horodatages de création et de connexion | Sécurité et audit du compte | Intérêt légitime (Art. 6§1.f) | Serveur GSIE |
| Empreinte d'un code d'action et son expiration | Vérification de l'adresse ou récupération demandée | Exécution du service demandé (Art. 6§1.b) | Serveur GSIE, 15 minutes maximum |

Le mot de passe est transmis uniquement à l’API GSIE via HTTPS puis haché
avec Argon2id côté serveur. Il n’est jamais stocké par GeoSylva. Les jetons
GSIE sont conservés dans un coffre Android chiffré et ne sont ni affichés ni
journalisés.
Les codes de vérification et de récupération sont envoyés par courrier,
hachés côté serveur, utilisables une seule fois et supprimables après leur
expiration de quinze minutes. Mailpit est réservé au développement local et
ne doit contenir aucune donnée réelle.

### 1.8 Synchronisation facultative des parcelles

| Donnée | Finalité | Base légale | Stockage |
|---|---|---|---|
| Identifiant local, nom, surface et paramètres sylvicoles de la parcelle | Continuité entre appareils et futurs services GSIE | Exécution du service demandé après activation explicite (Art. 6§1.b) | Appareil et serveur GSIE |
| Commune, références cadastrales, géométrie IGN/WKT, altitude et SER | Restitution de la parcelle synchronisée | Exécution du service demandé (Art. 6§1.b) | Appareil et serveur GSIE |
| Remarques libres | Conservation de la fiche choisie par l’utilisateur ; elles peuvent contenir des données personnelles | Exécution du service demandé (Art. 6§1.b) | Appareil et serveur GSIE |
| UUID d’opération, version serveur, état, tentatives et horodatages | Idempotence, reprise réseau, détection des conflits et audit technique | Intérêt légitime de sécurité et de fiabilité (Art. 6§1.f) | File locale chiffrée et serveur GSIE |

La première transmission nécessite l’action « Activer et synchroniser les
parcelles ». Après cette activation, les modifications et suppressions de
parcelles sont automatiquement ajoutées à la file du compte. Une suppression
est conservée côté serveur sous forme de tombstone pour éviter qu’un autre
appareil ne recrée silencieusement une ancienne version.

---

## 2. Stockage et sécurité

### 2.1 Chiffrement

- **Base de données** : chiffrée avec SQLCipher (AES-256, clé dérivée via Android Keystore)
  — `ForestryDatabase.kt:151`
- **Clés cryptographiques** : stockées dans Android Keystore (hardware-backed si disponible)
- **Fichiers sensibles** : stockés dans le stockage interne de l'Application (scoped storage
  Android 10+)

### 2.2 Transferts optionnels vers GSIE

Sans compte, aucune donnée d’identité n’est envoyée à GSIE. Lors d’une
inscription ou d’une connexion volontaire, les données décrites au §1.7 sont
transmises via HTTPS. Les parcelles du §1.8 ne sont transmises qu’après leur
activation explicite. La file reste dans la base SQLCipher et WorkManager
attend un réseau disponible ; un conflit est conservé sans écrasement.
Le lieu d’hébergement de production, le sous-traitant éventuel et la procédure
d’effacement opérationnelle doivent être publiés avant l’ouverture publique du
service de comptes.

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

L'Application utilise une connexion internet pour GSIE et des services tiers.
Les données de compte ne sont envoyées qu’à GSIE et, si l’utilisateur choisit
Google, au flux d’identité Google. Les services cartographiques ne reçoivent
pas les noms, e-mails ou photos stockés dans l’application ; ils reçoivent les
coordonnées nécessaires à leur fonctionnement.

### 3.1 Services utilisés et données envoyées

| Service | Usage | Données envoyées | Hébergement | Transfert hors UE |
|---------|-------|------------------|-------------|-------------------|
| **API GSIE / Quintessences** | Compte, session et synchronisation facultative des parcelles | Données décrites aux §1.7 et §1.8 après les actions correspondantes | À documenter avant ouverture publique | À déterminer selon l’hébergement retenu |
| **Cloudflare** (bordure GSIE prévue) | Protection DDoS/WAF et tunnel vers l'API | Adresse IP et métadonnées techniques HTTP | Réseau mondial ; garanties à valider | DPA, localisation des journaux et transferts à finaliser avant activation publique |
| **Google Identity** (facultatif) | Connexion Google via Credential Manager | Nonce, client OAuth et données du compte Google choisies | Google | Potentiellement oui — à finaliser avant ouverture publique |
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
| Hébergeur GSIE | Identité et synchronisation Quintessences | E-mail, profil, identifiants et parcelles activées | À sélectionner | À documenter avant ouverture publique |
| Google | Connexion Google facultative | Identité Google choisie par l’utilisateur | International | Conditions et garanties à finaliser avant ouverture publique |
| Cloudflare | Bordure de sécurité de l'API GSIE | IP et métadonnées techniques | International | DPA et garanties de transfert à finaliser avant ouverture publique |
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

Les fournisseurs cartographiques n’ont pas accès aux données personnelles
stockées sur l’appareil. Google traite uniquement l’identité du parcours
choisi. GSIE traite l’identité et, uniquement après activation, les parcelles
décrites au §1.8.

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
| Compte Quintessences | Jusqu’à la demande d’effacement ou l’application de la politique d’inactivité à définir avant ouverture publique | Procédure serveur dédiée ; la déconnexion ne supprime que la session locale |
| Jetons de session locaux | Jusqu’à déconnexion, expiration ou désinstallation | Effacement du coffre local lors de la déconnexion |
| Empreintes des codes d'action | 15 minutes maximum, ou consommation antérieure | Expiration et purge serveur |
| Répliques serveur des parcelles et tombstones | Jusqu’à désactivation/effacement demandé ou politique contractuelle à publier avant ouverture publique | Procédure serveur ; l’effacement centralisé autonome reste à compléter |

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
| **Compte Quintessences** | Accès, rectification et effacement de l’identité serveur | Demande à `contact@geosylva.fr` ; interface autonome à compléter avant ouverture publique | ⚠️ Procédure manuelle en développement |
| **Parcelles synchronisées** | Accès, rectification, portabilité et effacement de la copie serveur | Données locales éditables ; demande serveur à `contact@geosylva.fr` avant l’interface autonome | ⚠️ Procédure serveur manuelle en développement |

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
| 2026-08-03 | 1.2 | Ajout transparent du compte Quintessences facultatif, du stockage chiffré de session et des flux GSIE/Google ; aucune synchronisation de donnée forestière dans cette tranche. |
| 2026-08-03 | 1.3 | Ajout du profil, de la vérification e-mail, de la récupération et de la conservation maximale de 15 minutes des empreintes de codes. |
| 2026-08-03 | 1.4 | Transparence sur la bordure Cloudflare prévue et ses métadonnées techniques ; aucun secret Cloudflare embarqué dans l'application. |
| 2026-08-03 | 1.5 | Ajout de la synchronisation facultative des parcelles, de son activation explicite, de la file chiffrée, des tombstones et des limites de la première tranche. |

---

*Cette politique de confidentialité s'applique à l'application Android GeoSylva (version 2.4.0 en développement).*
