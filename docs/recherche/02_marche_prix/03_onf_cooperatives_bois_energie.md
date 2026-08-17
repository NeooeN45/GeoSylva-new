# ONF (ventesdebois.onf.fr), coopératives forestières et bois-énergie — sources de prix
**Domaine** : docs/recherche/02_marche_prix/
**Date de recherche** : 2026-07-02 (approfondissement des sections 2.3 et 2.4 de
`docs/REFERENTIELS_FORESTIERS_EXTERNES.md`)
**Agent** : recherche marché/prix — ONF / coopératives / bois-énergie

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| ONF — Indice de prix moyen des bois vendus sur pied (publié via l'Observatoire France Bois Forêt) | officielle (ONF, données produites par l'ONF, diffusées par l'interprofession) | Officielle | https://observatoire.franceboisforet.com/donnees-de-la-filiere/amont-forestier/office-national-des-forets/ | Trimestriel — dernier repéré : « 1er trimestre 2026 » (page consultée 2026-07-02, publiée 2026-04-22) |
| ventesdebois.onf.fr (VEL — Ventes En Ligne) | officielle (ONF) | Officielle mais **accès technique restreint** (cf. §2.1) | https://www.ventesdebois.onf.fr / https://ventesdebois.onf.fr/vel/ | Testé le 2026-07-02 |
| ONF Open Data (portail géo-onf.opendata.arcgis.com + page onf.fr Open Data) | officielle | Officielle | https://www.onf.fr/onf/connaitre-lonf/+/35::opendata-onf.html ; https://geo-onf.opendata.arcgis.com/ | Consulté 2026-07-02 — **aucun jeu de données « prix de vente » identifié** dans le catalogue (thèmes listés : RSE, biodiversité, aménagements, foncier) |
| UCFF — Les Coopératives Forestières (fédération, 12 coopératives) | commerciale/tierce (fédération professionnelle) | Commerciale/tierce mais institutionnelle | https://lescooperativesforestieres.fr/ | Consulté 2026-07-02 |
| Alliance Forêts Bois — page « Estimation des bois sur pied » | commerciale/tierce | Commerciale/tierce | https://www.allianceforetsbois.fr/proprietaires-forestiers/exploitation-achat-de-bois/estimation-bois-pied/ | Consulté 2026-07-02 |
| UNISYLVA | commerciale/tierce | Commerciale/tierce | https://www.unisylva.fr/ | Non consultée en détail (site non fetché, cf. limites) |
| Groupe Coopération Forestière (GCF) — union de 12 coopératives | commerciale/tierce (institutionnelle) | Commerciale/tierce | https://www.gcf-coop.fr/ | Consulté 2026-07-02 |
| ADEME — dataset ouvert « Prix des combustibles bois pour le chauffage domestique » | **officielle** (ADEME, Licence Ouverte Etalab) | Officielle — la meilleure source trouvée dans cette recherche | https://data.ademe.fr/datasets/prix-bois-domestique | Mis à jour le 18/08/2025 ; série 2005→2024 ; API testée en direct le 2026-07-02 |
| ADEME — rapport complet « Enquête sur les prix des combustibles bois en 2024 » (réalisée par CODA Stratégies) | officielle | Officielle | https://librairie.ademe.fr/energies/8718-enquete-sur-les-prix-des-combustibles-bois-en-2024.html | 2024 |
| CIBE (Comité Interprofessionnel du Bois Énergie) — indices CEEB (Centre d'Études de l'Économie du Bois) | commerciale/tierce mais technique/reconnue filière | Commerciale/tierce | https://cibe.fr/prix-du-bois-energie/ ; https://cibe.fr/documents/prix-indices-nationaux-bois-energie/ | Mercuriales trimestrielles depuis 2011 (base 100 = T4 2011) ; dernières citées : fév-26, mai-26 |
| France Bois Forêt — Indicateur annuel « Prix de vente des bois sur pied en forêt privée » | officielle (co-produit ASFFOR, Experts Forestiers de France, La Forestière) | Officielle/interprofessionnelle | https://observatoire.franceboisforet.com/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2026/ | 2026 (annuel) |
| bois-de-chauffage.net — baromètre bois de chauffage | commerciale/tierce | **Commerciale, non officielle** — utile pour ordre de grandeur grand public uniquement | https://www.bois-de-chauffage.net/tarifs-bois.php | Juillet 2024 cité sur la page (consultée 2026-07-02, contenu non daté précisément par bloc) |

---

## 2. Données détaillées

### 2.1 ventesdebois.onf.fr — test d'accès réel et faisabilité technique

**Requêtes effectuées le 2026-07-02** :

| Requête | Résultat |
|---|---|
| `GET https://www.ventesdebois.onf.fr/robots.txt` | **HTTP 401** (échec, refusé avant même de servir le fichier — comportement inhabituel pour un `robots.txt`, suggère un WAF/pare-feu applicatif filtrant les requêtes automatisées ou un accès non-navigateur) |
| `GET https://ventesdebois.onf.fr/robots.txt` (sans `www.`) | Échec de récupération (« Failed to fetch URL ») |
| `GET https://ventesdebois.onf.fr/` | Échec de récupération |
| `GET https://ventesdebois.onf.fr/vel/` | HTTP 200, mais contenu retourné = **`Loading...`** uniquement |
| `GET https://ventesdebois.onf.fr/vel/#/accueil` | Idem — `Loading...` |

**Interprétation `[À VÉRIFIER MANUELLEMENT]`** : le module « VEL » (Ventes En Ligne) est une
**Single Page Application** (JS côté client, probablement Angular/React vu le chemin `/vel/#/...`
en hash-routing) qui ne restitue son contenu qu'après exécution du JavaScript dans un vrai
navigateur. Un outil de fetch HTTP simple (comme celui utilisé ici) ne peut donc **pas** lire les
données de vente sans un moteur de rendu JS (headless browser type Playwright/Puppeteer). Le
HTTP 401 sur `robots.txt` lui-même est à revérifier manuellement avec un navigateur/proxy
différent — il peut s'agir d'un blocage temporaire, d'une géo-restriction, ou d'un WAF anti-bot
(Cloudflare/Akamai) plutôt que d'une politique volontaire de l'ONF vis-à-vis des robots.

**Recoupement documentaire** (articles filière, cf. §1) : les catalogues de vente et les
**comptes-rendus de vente restent disponibles 60 jours** après chaque vente sur le site VEL, ce qui
confirme qu'il existe bien des données de prix par lot/essence/région en ligne — mais :
- aucune trace d'un **export CSV/JSON/RSS officiel** n'a été trouvée sur le site ONF ni dans la
  documentation Open Data ONF (catalogue Open Data centré sur RSE, biodiversité, foncier — pas sur
  les ventes de bois, cf. §1) ;
- l'inscription à un **compte acheteur professionnel** (registre du commerce ou équivalent, cf.
  page « Comment faire une offre d'achat ? ») est nécessaire pour un usage transactionnel complet,
  et pourrait aussi conditionner l'accès à certaines données détaillées de résultats de vente.

**Conclusion faisabilité technique** :
- Un scraping léger (requêtes HTTP simples) est **inopérant** : le site est une SPA.
- Un scraping avec navigateur headless serait **techniquement possible en théorie**, mais :
  1. le comportement 401 observé sur `robots.txt` doit être revérifié — s'il se confirme comme un
     blocage anti-bot volontaire, un contournement poserait un **risque légal et de CGU**
     (violation probable des conditions générales d'utilisation du site, potentiellement du RGPD
     si des données à caractère personnel — noms d'acheteurs par ex. — apparaissent dans les
     comptes-rendus) ;
  2. la donnée n'étant disponible que 60 jours par vente, un scraping devrait être **récurrent et
     automatisé**, ce qui aggrave le risque de sollicitation excessive du serveur (à proscrire sans
     accord explicite de l'ONF) ;
  3. **`[À VÉRIFIER MANUELLEMENT]`** — contacter l'ONF (service Open Data, cf.
     `onf.fr/.../opendata-onf.html`, page « Contact ») pour demander explicitement s'il existe une
     API ou un export de données de vente, avant d'envisager tout scraping.

**Recommandation immédiate pour GeoSylva** : **ne pas scraper ventesdebois.onf.fr**. Utiliser à la
place l'**indice de prix moyen ONF publié trimestriellement en PDF** via l'Observatoire France Bois
Forêt (§2.2) — c'est une donnée agrégée, déjà publique, sourcée, et mise à jour régulièrement, sans
ambiguïté légale.

### 2.2 ONF — Indice de prix moyen (publication trimestrielle via France Bois Forêt)

- Format : **PDF téléchargeable** (ex. `ONF-Prix-bois-2026-T1-Graph.pdf`), pas de flux
  structuré (API/CSV) identifié.
- Fréquence : trimestrielle, avec historique d'« Articles » consultable (T1 2025, T2 2025,
  T1 2026 repérés lors de cette recherche).
- Contenu attendu (d'après le titre et le contexte documentaire recoupé avec
  `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.3) : indice de prix moyen des bois vendus sur pied par
  l'ONF, avec **distinction forêt domaniale / forêt communale** — **`[À VÉRIFIER
  MANUELLEMENT]`** : le contenu détaillé du PDF n'a pas pu être extrait par le fetch web (limite de
  l'outil sur les PDF) ; il faut télécharger et lire le PDF directement pour confirmer la
  granularité exacte (essence, région, qualité).
- Accès : la page consultée ne présentait pas de mur de paywall pour le lien PDF direct, mais le
  site affiche aussi une « Zone membres » réservée aux adhérents de France Bois Forêt pour
  d'autres contenus — à vérifier si le PDF trimestriel ONF est bien public ou visible seulement
  après connexion (le lien de téléchargement était visible sans connexion lors du test).

### 2.3 Coopératives forestières — publication de prix

**Constat central : aucune coopérative française ne publie d'indicateur de prix chiffré,
structuré et daté, en accès libre**, à la différence de l'ONF (indice trimestriel) ou de France
Bois Forêt (indicateur annuel forêt privée).

- **UCFF / Les Coopératives Forestières** (fédération, https://lescooperativesforestieres.fr/) :
  site vitrine + annuaire de recherche de coopérative par département. Chiffres clés publiés :
  120 000 sylviculteurs coopérateurs, 2,2 Mha gérés, 20 % de la récolte nationale commercialisée
  (7,2 Mm³/an), 40 % du reboisement national, 1 500 salariés (chiffres institutionnels, sans date
  précise de mise à jour visible sur les pages consultées — **`[À VÉRIFIER
  MANUELLEMENT]`** pour la date exacte). **Aucun indicateur de prix** trouvé sur ces pages.
- **Alliance Forêts Bois** (https://www.allianceforetsbois.fr/) : propose un service
  d'« **Estimation des bois sur pied** » décrit comme une analyse interne
  (coûts techniques d'exploitation + prix de vente prévisionnels → valorisation résiduelle pour le
  propriétaire), mais c'est un **service commercial individualisé**, pas une grille de prix
  publique consultable.
- **UNISYLVA, Coforêt, Forestarn** et les autres coopératives listées (AFB Forestarn, Aussill,
  Cofnor, Coforêt, Unisylva + ses antennes régionales) : d'après le recoupement effectué, elles
  fonctionnent selon le même modèle — accompagnement individualisé du sylviculteur, sans barème de
  prix public en ligne. **`[À VÉRIFIER MANUELLEMENT]`** — vérifier directement sur unisylva.fr et
  forestarn.fr (non fetchés dans cette recherche) s'il existe une page « prix indicatifs » cachée.
- **Groupe Coopération Forestière (GCF)** (https://www.gcf-coop.fr/) : structure de mutualisation
  de services entre les 12 coopératives (dont Unisylva, Forêt d'ici, Alliance Forêts Bois au
  conseil d'administration) — pas de données de prix publiques identifiées non plus.

**Conclusion** : les coopératives ne sont **pas** une source de prix publics exploitable pour
GeoSylva. Leur valeur d'usage pour l'app serait plutôt en tant que **futur partenaire/canal
commercial** (ex. bouton « obtenir une estimation via une coopérative de votre région »), pas comme
source de données chiffrées à intégrer dans un moteur de calcul.

### 2.4 Bois-énergie / bois de chauffage — deux sources distinctes identifiées

Il existe bien un **marché distinct** du bois-énergie (bûches, granulés, plaquettes), avec deux
familles de sources :

#### 2.4.1 ADEME — dataset ouvert « Prix des combustibles bois pour le chauffage domestique »

**Source officielle, la plus solide de toute cette recherche.**

- URL du dataset : https://data.ademe.fr/datasets/prix-bois-domestique
- Licence : **Licence Ouverte / Open Licence (Etalab)** — réutilisation libre, y compris
  commerciale, avec mention de la source.
- Taille : 161 enregistrements (5,4 ko), mise à jour « irrégulière », dernière mise à jour listée :
  **18 août 2025**.
- Couverture géographique : nationale (pas de granularité régionale/communale dans ce dataset —
  limite pour l'objectif de granularité communale visé par la méthodologie GeoSylva).
- Méthodologie sous-jacente : enquête menée auprès de 324 revendeurs + 275 relevés complémentaires
  (Internet, grandes surfaces), réalisée par CODA Stratégies pour l'ADEME (rapport complet :
  https://librairie.ademe.fr/energies/8718-enquete-sur-les-prix-des-combustibles-bois-en-2024.html).

**Test d'API réel effectué le 2026-07-02** :

- Requête : `GET https://data.ademe.fr/data-fair/api/v1/datasets/prix-bois-domestique/lines?size=5`
- Résultat : **HTTP 200**, réponse JSON directe, **sans clé API requise**, format :
  ```json
  {"total":161,"next":"https://data.ademe.fr/data-fair/api/v1/datasets/cs96hfhstex57-iurc8jdqu5/lines?size=5&after=5",
   "results":[
     {"_i":1,"annee":2005,"type_combustible":"Bûches de 25 cm","prix_non_livre":26,"prix_livre":27, ...},
     {"_i":2,"annee":2005,"type_combustible":"Bûches de 33 cm","prix_non_livre":28,"prix_livre":30, ...},
     ...
   ]}
  ```
- Requête triée par année (`?size=20&sort=-annee`) → dernières valeurs obtenues (**prix en
  €/MWh PCI**, pas en €/stère directement — attention à la conversion), extrait réel pour
  l'année **2024** :

  | Type de combustible | Prix non livré (€/MWh PCI) | Prix livré (€/MWh PCI) |
  |---|---|---|
  | Bûches de 25 cm | 48,39 | 52,00 |
  | Bûches de 33 cm | 46,50 | 49,88 |
  | Bûches de 40 cm | 49,68 | 52,18 |
  | Bûches de 50 cm | 43,00 | 44,77 |
  | Bûches de 1 m | 35,79 | 38,29 |
  | Granulés vrac | 79,35 | 85,00 |
  | Granulé en sac vendu au détail | 73,71 | 80,02 |
  | Granulé en sac vendu par palette | 83,29 | 86,54 |
  | Bûches et bûchettes reconstituées | 84,48 | 89,63 |

  et pour **2023** (extrait) :

  | Type de combustible | Prix non livré (€/MWh PCI) | Prix livré (€/MWh PCI) |
  |---|---|---|
  | Bûches de 25 cm | 47 | 50 |
  | Bûches de 1 m | 35 | 39 |
  | Granulés vrac | 104 | 104 |

- Série historique disponible **depuis 2005** jusqu'à 2024 (161 lignes ≈ 9 types de combustible ×
  ~18 années), permettant un calcul d'évolution/indice si besoin.
- Documentation d'API dédiée existe (`/datasets/prix-bois-domestique/api-doc`) mais le fetch de
  cette page n'a montré que la navigation, pas le contenu Swagger détaillé — **`[À VÉRIFIER
  MANUELLEMENT]`** pour la liste complète des paramètres de filtre disponibles (le portail
  data.ademe.fr est basé sur **data-fair** (Koumoul), dont l'API générique est documentée
  publiquement : filtrage, tri, pagination, export CSV/JSON/GeoJSON selon les capacités standard
  de data-fair — à confirmer précisément pour ce dataset).
- **Point d'attention unité** : les prix sont en **€/MWh PCI**, pas en €/stère ni €/tonne. Une
  conversion est nécessaire pour un affichage « grand public » (ex. 1 stère de bois dur ≈ 1,5-2 MWh
  PCI selon essence/humidité — **`[À VÉRIFIER MANUELLEMENT]`**, facteur de conversion à sourcer
  précisément, ex. via CIBE/ADEME, avant tout affichage dans l'app).

#### 2.4.2 CIBE / CEEB — indices professionnels bois-énergie industriel (chaufferies, réseaux de chaleur)

- Le **CEEB (Centre d'Études de l'Économie du Bois)** publie depuis 2011 une **mercuriale
  trimestrielle** d'indices de prix du bois-énergie (base 100 = T4 2011), diffusée via le CIBE
  (https://cibe.fr/prix-du-bois-energie/ et
  https://cibe.fr/documents/prix-indices-nationaux-bois-energie/).
- Cette source cible un **marché différent** de l'ADEME (particuliers) : elle concerne les
  **plaquettes forestières et combustibles industriels pour chaufferies collectives/réseaux de
  chaleur**, avec des indices dédiés (dont, depuis nov. 2024, 2 nouveaux indices pour le
  « bois B » — bois de recyclage en fin de vie).
- Statut d'accès : la page liste des liens « Résultats fév-26 », « Résultats mai-26 », etc. — non
  vérifié si le PDF est en accès libre ou réservé aux adhérents CIBE (**`[À VÉRIFIER
  MANUELLEMENT]`** — la page « Espace adhérents » avec identifiant/mot de passe visible en haut du
  site suggère qu'une partie du contenu CIBE est réservée aux membres).
- **Pertinence pour GeoSylva** : moindre à court terme (public cible = petits propriétaires/
  particuliers, pas des gestionnaires de chaufferies industrielles), mais utile si l'app devait un
  jour adresser la valorisation de rémanents/bois d'éclaircie vers les circuits bois-énergie
  professionnels.

#### 2.4.3 Sources commerciales grand public (à ne pas utiliser comme référence officielle)

- **bois-de-chauffage.net** publie un « baromètre » (prix moyen du stère en vrac ≈ 85-90 € en 2024
  d'après la page consultée) avec des tableaux par longueur de bûche et par région — utile comme
  **repère indicatif grand public**, mais c'est une source **commerciale non officielle**
  (comparateur de fournisseurs), sans méthodologie statistique publiée équivalente à celle de
  l'ADEME. À ne pas utiliser comme donnée de référence dans les calculs métier, seulement — si
  besoin — en complément pédagogique clairement identifié comme tel.

---

## 3. Comparatif / analyse critique

| Source | Granularité | Format exploitable par l'app | Coût/accès | Fiabilité | Fraîcheur |
|---|---|---|---|---|---|
| ONF (indice trimestriel via FBF) | Nationale, distinction domaniale/communale à confirmer | PDF uniquement — nécessite ressaisie manuelle | Gratuit, lien direct | Officielle | Trimestrielle, très à jour |
| ventesdebois.onf.fr (VEL) | Par lot/vente (la plus fine en théorie) | **Aucun** (SPA JS, pas d'API/export identifié) | Accès web gratuit mais techniquement fermé à l'automatisation | Officielle | Temps réel mais fenêtre de 60 j |
| Coopératives (UCFF, Alliance, Unisylva...) | Aucune donnée de prix publique | N/A | N/A | Commerciale/tierce | N/A |
| ADEME (bois domestique) | Nationale uniquement, par type de combustible | **API JSON réelle, sans clé, licence ouverte** — la meilleure option technique | Gratuit, open data | **Officielle** | Annuelle (dernier point 2024, dataset mis à jour août 2025) |
| CIBE/CEEB (bois-énergie industriel) | Nationale, indices base 100 | PDF, accès partiellement adhérents | Partiellement gratuit | Commerciale/tierce (technique) | Trimestrielle |
| bois-de-chauffage.net | Régionale (grand public) | Page web, pas d'API | Gratuit | Commerciale, non officielle | Ponctuelle (2024) |

**Analyse** : pour le bois-énergie/chauffage, l'**ADEME est nettement supérieure** en fiabilité et
en exploitabilité technique (API JSON réelle testée avec succès) à toutes les autres sources de ce
périmètre. Pour le bois d'œuvre/industrie ONF, en revanche, **aucune source structurée
(API/CSV) n'existe** — seul le PDF trimestriel FBF/ONF et l'indicateur annuel forêt privée FBF
restent exploitables, via ressaisie manuelle périodique (déjà identifiée en §2.1 de
`REFERENTIELS_FORESTIERS_EXTERNES.md`).

---

## 4. Recommandation pour GeoSylva

1. **Intégrer le dataset ADEME `prix-bois-domestique`** comme nouvelle source de données pour un
   futur module « prix du bois de chauffage » (marché actuellement non couvert par
   `RegionalPricePresets.kt`, qui semble centré sur le bois d'œuvre sur pied) :
   - Appel API simple, sans clé, à intégrer côté backend/ETL (pas d'appel direct depuis l'app
     mobile en production — préférer une synchronisation périodique côté build/CI ou un cache
     serveur, car l'API est un service tiers hors contrôle de GeoSylva) ;
   - Prévoir la conversion **€/MWh PCI → €/stère** avec un facteur documenté par essence/humidité
     avant affichage utilisateur (actuellement non sourcé, `[À VÉRIFIER MANUELLEMENT]`) ;
   - Prioriser en priorité **moyenne** : fonctionnalité nouvelle (bois-énergie/affouage), pas une
     correction de donnée existante, mais forte valeur perçue pour les petits propriétaires
     forestiers évoqués dans la demande.
2. **Ne pas développer de scraper pour ventesdebois.onf.fr** : risque légal/CGU non maîtrisé, pas
   d'API, SPA difficile à automatiser proprement. Si la donnée par lot ONF est jugée stratégique,
   la voie recommandée est un **contact direct avec l'ONF (service Open Data / direction des
   ventes)** pour explorer un accès conventionné, plutôt qu'un scraping.
3. **Continuer à s'appuyer sur le PDF trimestriel ONF (via FBF)** pour la mise à jour manuelle,
   annuelle ou trimestrielle, des presets déjà identifiés en §2.1/§2.3 de
   `REFERENTIELS_FORESTIERS_EXTERNES.md` — aucune automatisation possible à ce stade, mais source
   fiable pour une ressaisie humaine périodique (ex. dans `RegionalPricePresets.kt`).
4. **Ne pas intégrer de données « coopératives »** comme source de prix : aucune donnée
   exploitable trouvée. En revanche, envisager (hors périmètre de cette recherche, à discuter
   produit) un futur **annuaire des coopératives par département** (source UCFF/GCF) comme
   fonctionnalité de mise en relation, pas de pricing.
5. **Citer explicitement les sources** (ADEME, ONF/FBF) dans `TarifDocumentationScreen` ou
   équivalent, avec la date de la donnée affichée, conformément à la règle méthodologique n°1.

---

## 5. Limites et points à vérifier manuellement

- Le comportement **HTTP 401 sur `https://www.ventesdebois.onf.fr/robots.txt`** doit être revérifié
  avec un autre outil/navigateur : il peut s'agir d'un artefact de l'outil de fetch utilisé ici et
  non d'une politique réelle de l'ONF. Ne pas conclure définitivement sans un second test
  indépendant (ex. `curl` depuis une machine dédiée, en respectant malgré tout les CGU).
- Le **contenu exact du PDF trimestriel ONF** (granularité domaniale/communale, essences, régions)
  n'a pas pu être lu par l'outil de fetch web (limite technique sur les PDF) — à télécharger et
  lire manuellement avant toute intégration dans `RegionalPricePresets.kt`.
- **UNISYLVA (unisylva.fr) et Forestarn (forestarn.fr)** n'ont pas été fetchés directement dans
  cette recherche (seulement mentionnés via les résultats de recherche et la page UCFF) — à
  vérifier manuellement s'il existe malgré tout une page de prix indicatifs cachée.
- Le **facteur de conversion €/MWh PCI ↔ €/stère** n'est pas sourcé dans ce document — à
  rechercher spécifiquement (CIBE, ADEME, ou fiches techniques bois-énergie) avant tout affichage
  utilisateur d'un prix en €/stère calculé depuis le dataset ADEME.
- La **date de mise à jour exacte des chiffres institutionnels UCFF** (120 000 sylviculteurs,
  2,2 Mha, 7,2 Mm³/an) n'est pas affichée clairement sur les pages consultées — à confirmer avant
  toute citation dans une documentation utilisateur GeoSylva.
- L'**accès complet à la « Zone membres » France Bois Forêt** et à l'« Espace adhérents » CIBE n'a
  pas été testé (pas d'identifiants) — certains contenus plus détaillés (mercuriales complètes,
  archives) pourraient être réservés aux adhérents de ces structures ; le présent document ne
  documente que ce qui est visible en accès public anonyme.
- Le **catalogue Open Data ONF** (geo-onf.opendata.arcgis.com) n'a pas pu être exploré en détail
  (page chargée mais vide au fetch, probablement aussi une SPA/portail ArcGIS nécessitant JS) — à
  revisiter manuellement pour confirmer l'absence totale de données de prix/ventes dans ce
  catalogue.
