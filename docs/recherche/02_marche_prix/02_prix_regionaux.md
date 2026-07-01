# Prix régionaux du bois sur pied — observatoires par région française
**Domaine** : docs/recherche/02_marche_prix/
**Date de recherche** : 2026-07-XX (à dater précisément par le relecteur — session de recherche web)
**Agent** : sous-agent recherche "prix régionaux"

---

## 0. Correction préalable par rapport à la commande initiale

⚠️ **Point à vérifier / déjà corrigé ici** : la consigne de départ mentionnait « 7 presets régionaux
existants » dans `RegionalPricePresets.kt`. À la lecture réelle du fichier (2026), `RegionalPricePresets.ALL`
contient en fait **13 presets** : 1 preset `NATIONAL` + 12 presets **GRECO** (Grandes Régions Écologiques
IGN, A à L — `grecoAPreset()` … `grecoLPreset()`), pas 7, et **pas un découpage par région administrative**
mais par grande région **écologique** (GRECO). Ce point doit être signalé au commanditaire :
le système de prix de l'app est actuellement organisé par écologie forestière (GRECO), pas par région
administrative — voir section 4 pour la recommandation d'articulation entre les deux approches.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version constatée |
|---|---|---|---|---|
| Observatoire économique France Bois Forêt (FBF) — indicateur national | officielle (interprofession nationale, co-produit ASFFOR/EFF/Société Forestière CDC) | officielle | https://observatoire.franceboisforet.com/ | Indicateur 2026 publié, basé sur ventes 2025 (paru ~avril-juin 2026) ; 86 €/m³ moyen 2025 (-4 %), 90 €/m³ en 2024 (+7 %), 84 €/m³ en 2023 (-10 %), 94 €/m³ en 2022 |
| La Forêt bouge — Bretagne | commerciale/tierce (portail CNPF grand public) | officielle (relais CNPF) | https://www.laforetbouge.fr/bretagne/prix-indicatif-des-bois-sur-pied-0 | déjà référencé dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.2 |
| La Forêt bouge — Grand Est | commerciale/tierce (portail CNPF) | officielle (relais CNPF) | https://www.laforetbouge.fr/grandest/observatoire-du-prix-des-bois | Page indique elle-même qu'un observatoire "sera probablement disponible en 2018" — **statut de mise en œuvre effective à vérifier manuellement**, incertitude sur la fraîcheur réelle des données |
| CNPF Nouvelle-Aquitaine | officielle (CNPF, établissement public) | officielle | https://nouvelle-aquitaine.cnpf.fr/gestion-durable-des-forets/coupes-et-travaux/le-prix-des-bois | déjà référencé §2.2 |
| CNPF national — portail régions | officielle | officielle | https://www.cnpf.fr/ (menu "Régions") | 2026 — liste les 10 délégations régionales CNPF actuelles (voir §2) |
| Fibois Occitanie — Observabois | commerciale/tierce (interprofession régionale) | commerciale/tierce (mais rediffuse des chiffres FBF officiels + commentaire régional propre) | https://www.fibois-occitanie.com/ressources/observabois/secteur-foret/indicateurs-prix-bois-sur-pied-prive-2025/ | Indicateur 2025 (ventes 2024), commentaire régional Occitanie (douglas/épicéa) publié |
| Fibois Auvergne-Rhône-Alpes | commerciale/tierce | commerciale/tierce | https://www.fibois-aura.org/2020/05/20/point-sur-marche-des-ventes-de-bois-sur-pied-en-foret-privee/ | Article ponctuel constaté pour 2020 (indicateur 2019/2020) — **pas de page pérenne dédiée identifiée**, existence d'une continuité annuelle non confirmée |
| Fibois AURA — Observatoire bois bûche | commerciale/tierce | commerciale/tierce | https://www.fibois-aura.org/energie/telechargements/ | "Observatoire production de bois bûche en AuRA — données 2024, édité oct. 2025" — **hors périmètre bois sur pied/bois d'œuvre**, concerne le bois de chauffage uniquement |
| Fibois Hauts-de-France / Observabois Hauts-de-France | commerciale/tierce (interprofession) | commerciale/tierce | https://www.observabois-hautsdefrance.fr/ | "Baromètre de conjoncture" trimestriel avec rubrique "Prix" — accès à l'espace de données conditionné ("espace personnel soumis à conditions") — **contenu détaillé non consultable en accès libre** |
| Fibois Normandie | commerciale/tierce | commerciale/tierce | https://www.fibois-normandie.fr/ | Observatoire trouvé = coût technique des machines forestières (FCBA), pas prix de vente des bois sur pied — **aucun observatoire prix identifié pour cette région** |
| Fibois Bourgogne-Franche-Comté | commerciale/tierce | commerciale/tierce | https://fibois-bfc.fr/ | Observatoire trouvé = bois énergie (bûche), pas bois sur pied/bois d'œuvre — **aucun observatoire prix bois d'œuvre identifié** |
| Fibois Grand Est | commerciale/tierce | commerciale/tierce | https://fibois-grandest.com/secteurs/observatoire-du-bois-industrie-et-bois-energie/ | Observatoire "bois industrie et bois énergie", pas prix bois sur pied — complète La Forêt bouge Grand Est mais sur un périmètre différent |
| Fibois Île-de-France | commerciale/tierce | commerciale/tierce | https://www.fibois-idf.fr/observatoire-construction-bois | Observatoire = coûts de la construction bois, pas prix du bois sur pied |
| Fibois PACA / Fibois Sud | commerciale/tierce | commerciale/tierce | https://www.fibois-paca.fr/ | Aucun observatoire de prix du bois sur pied identifié ; seul un "Prix de la Construction Bois" (concours d'architecture, homonyme trompeur) |
| CNPF Auvergne-Rhône-Alpes | officielle | officielle | https://auvergnerhonealpes.cnpf.fr/ | Pas de page dédiée "prix des bois" pérenne trouvée ; mentions ponctuelles dans "Actualités" et fiches essences PDF anciennes (ex. fiche frêne 2014) |
| CNPF Bourgogne-Franche-Comté | officielle | officielle | https://bourgognefranchecomte.cnpf.fr/ | Idem : pas de page "prix des bois" pérenne ; documents SRGS anciens mentionnant la tendance baissière des prix (constat général, pas de tableau chiffré actualisé) |
| CNPF Bretagne - Pays de la Loire (délégation fusionnée) | officielle | officielle | https://bretagne-paysdelaloire.cnpf.fr/gestion-durable-des-forets/la-mise-en-oeuvre/la-vente-de-bois | Page "Connaître le prix des bois" + bulletins PDF "BFO" avec **tableaux de prix chiffrés par essence et par diamètre** issus des ventes groupées EFF (Solesmes/Carhaix) — la meilleure source régionale identifiée après FBF national |
| CNPF Hauts-de-France - Normandie (délégation fusionnée) | officielle | officielle | https://hautsdefrance-normandie.cnpf.fr/gestion-durable-des-forets/en-pratique/la-commercialisation-des-bois | Pas de tableau de prix propre : renvoie explicitement vers la page nationale CNPF "Vente de bois" — **aucune donnée chiffrée régionale spécifique** |
| CNPF Île-de-France - Centre-Val de Loire (délégation fusionnée, domaine `ifc.cnpf.fr`) | officielle | officielle | https://ifc.cnpf.fr/sites/ifc/files/2024-03/Fiche%20Gestion%2021%20-%20Estimer%20et%20Vendre%20ses%20Bois.pdf | Fiche méthodologique "Estimer et Vendre ses Bois" (critères de valorisation) — **pas de barème de prix chiffré actualisé** ; existe aussi un "Référentiel coûts des travaux 2019" (coûts de travaux, pas prix de vente bois) |
| CNPF Occitanie | officielle | officielle | https://occitanie.cnpf.fr/sites/occitanie/files/2022-09/March%C3%A9%20du%20bois%20en%20Occitanie.pdf | Document ponctuel "Une augmentation forte du prix des bois en 2021" (2022-09) — **usage documentaire mais pas d'observatoire récurrent identifié sur le site CNPF Occitanie lui-même** (à distinguer de Fibois Occitanie/Observabois, plus régulier) |
| CNPF PACA | officielle | officielle | https://paca.cnpf.fr/une-coupe-de-bois-les-differentes-etapes | Pages méthodologiques (comment vendre), aucun barème de prix chiffré trouvé |
| CNPF Corse | officielle | officielle | https://corse.cnpf.fr/ | Aucun observatoire de prix ; seules mentions anecdotiques (ex. bois de chauffage chêne vert 12-15 €/stère, contexte PACA cité dans un guide chêne vert) — marché très spécifique (chêne vert, liège), quasi absence de bois d'œuvre valorisé |
| ONF — ventes de bois | officielle | officielle | https://www.ventesdebois.onf.fr | Déjà référencé §2.3 REFERENTIELS — données trimestrielles nationales, distinction domaniales/communales, pas de vue par région administrative simple |

---

## 2. Données détaillées — région par région

### 2.1 Rappel : la structure territoriale CNPF ne correspond pas 1:1 aux 13 régions administratives

Le CNPF a organisé ses délégations régionales par **fusion** de plusieurs anciennes régions
administratives, ce qui explique pourquoi certaines "régions" du référentiel officiel n'ont
**pas de site CNPF propre** mais partagent un site avec une région voisine :

| Région administrative (13 + Corse) | Délégation CNPF correspondante | Domaine constaté |
|---|---|---|
| Bretagne | Bretagne - Pays de la Loire (fusionnée) | bretagne-paysdelaloire.cnpf.fr |
| Pays de la Loire | Bretagne - Pays de la Loire (fusionnée) | bretagne-paysdelaloire.cnpf.fr |
| Grand Est | Grand Est | grandest.cnpf.fr |
| Nouvelle-Aquitaine | Nouvelle-Aquitaine | nouvelle-aquitaine.cnpf.fr |
| Occitanie | Occitanie | occitanie.cnpf.fr |
| Auvergne-Rhône-Alpes | Auvergne-Rhône-Alpes | auvergnerhonealpes.cnpf.fr |
| Provence-Alpes-Côte d'Azur | Provence-Alpes-Côte d'Azur | paca.cnpf.fr |
| Bourgogne-Franche-Comté | Bourgogne-Franche-Comté | bourgognefranchecomte.cnpf.fr |
| Centre-Val de Loire | Île-de-France - Centre-Val de Loire (fusionnée) | ifc.cnpf.fr |
| Île-de-France | Île-de-France - Centre-Val de Loire (fusionnée) | ifc.cnpf.fr |
| Hauts-de-France | Hauts-de-France - Normandie (fusionnée) | hautsdefrance-normandie.cnpf.fr |
| Normandie | Hauts-de-France - Normandie (fusionnée) | hautsdefrance-normandie.cnpf.fr |
| Corse | Corse | corse.cnpf.fr |

`[À VÉRIFIER MANUELLEMENT]` : ce découpage a été reconstitué à partir du menu "Régions" affiché sur
`www.cnpf.fr` au moment de la recherche (2026) — il peut évoluer si le CNPF réorganise ses délégations.

### 2.2 Tableau récapitulatif — couverture et fiabilité par région

| Région | Couverte par un observatoire de PRIX DE VENTE régional actif et daté ? | Meilleure source identifiée | Nature des données | Fiabilité |
|---|---|---|---|---|
| Bretagne | ✅ Oui (déjà dans REFERENTIELS §2.2) | La Forêt bouge Bretagne | Barème indicatif par essence/qualité | officielle |
| Pays de la Loire | ⚠️ Partielle (via délégation fusionnée avec Bretagne) | CNPF Bretagne-Pays de la Loire — page "vente de bois" + bulletins BFO (tableaux prix par essence/diamètre) | Bulletins ponctuels avec tableaux chiffrés (ventes groupées EFF Solesmes/Carhaix) | officielle, mais fréquence irrégulière (bulletin, pas mise à jour continue) |
| Grand Est | ⚠️ Oui mais fraîcheur incertaine (déjà dans REFERENTIELS §2.2) | La Forêt bouge Grand Est | La page annonçait elle-même en 2018 un observatoire "à venir" — **statut réel à vérifier manuellement** | officielle mais non revérifiée |
| Nouvelle-Aquitaine | ✅ Oui (déjà dans REFERENTIELS §2.2) | CNPF Nouvelle-Aquitaine | Page dédiée "le prix des bois" | officielle |
| Occitanie | ✅ Oui (nouveau, identifié dans cette recherche) | **Fibois Occitanie — Observabois** (rediffusion FBF + commentaire régional douglas/épicéa) | Indicateur annuel FBF + analyse régionale qualitative (pas de tableau chiffré propre par essence trouvé en accès libre) | commerciale/tierce (interprofession), s'appuie sur données officielles FBF |
| Auvergne-Rhône-Alpes | ❌ Non (pas de page pérenne identifiée) | Fibois AURA — articles ponctuels de reprise de l'indicateur FBF national ; observatoire bois-bûche AURA hors périmètre BO | Article isolé (2020), pas de série régulière confirmée pour le bois sur pied/bois d'œuvre | commerciale/tierce, incomplet |
| Provence-Alpes-Côte d'Azur | ❌ Non | Aucune source régionale de prix de vente identifiée (Fibois PACA = concours construction ; CNPF PACA = méthodologie) | — | non disponible en accès libre |
| Bourgogne-Franche-Comté | ❌ Non | Fibois BFC = observatoire bois énergie uniquement ; CNPF BFC = documents SRGS anciens, constats qualitatifs | Aucun barème chiffré actualisé | non disponible en accès libre |
| Centre-Val de Loire | ❌ Non | CNPF IFC = fiche méthodologique "Estimer et Vendre ses Bois" (pas de prix chiffrés) | — | non disponible en accès libre |
| Île-de-France | ❌ Non | Fibois IDF = observatoire construction bois uniquement | — | non disponible en accès libre |
| Hauts-de-France | ⚠️ Partielle, accès restreint | Observabois Hauts-de-France (Fibois HdF) — "Baromètre de conjoncture" trimestriel avec rubrique "Prix" | Existe mais **espace de données conditionné/restreint**, contenu détaillé non consultable en accès libre lors de cette recherche | commerciale/tierce, accès limité |
| Normandie | ❌ Non | Fibois Normandie = observatoire coût technique des machines FCBA (hors périmètre prix de vente bois) ; CNPF renvoie vers page nationale | — | non disponible en accès libre |
| Corse | ❌ Non | Aucun observatoire ; marché de niche (chêne vert bois de chauffage, liège) documenté de façon anecdotique seulement | — | non disponible en accès libre |

**Synthèse chiffrée** : sur les 13 régions administratives françaises (métropole), au moment de cette
recherche, **4 régions** disposent d'une source de prix régionalisée exploitable en accès libre avec un
minimum de récurrence (Bretagne, Pays de la Loire via délégation fusionnée, Nouvelle-Aquitaine, Occitanie
via Fibois), **1 région** a un statut incertain à revérifier (Grand Est), **1 région** a un observatoire
existant mais à accès restreint (Hauts-de-France), et **7 régions** n'ont **aucune source de prix
régionalisée en accès libre identifiée** (Auvergne-Rhône-Alpes, PACA, Bourgogne-Franche-Comté,
Centre-Val de Loire, Île-de-France, Normandie, Corse).

`[À VÉRIFIER MANUELLEMENT]` : cette absence constatée reflète l'état de l'accès **public/gratuit** au
moment de la recherche web (2026) ; certains CNPF régionaux ou interprofessions Fibois publient
peut-être des données similaires dans des newsletters imprimées, revues papier (« Parlons Forêt », «
Forêt & Bois du Nord », etc.) non indexées par les moteurs de recherche, ou réservées aux adhérents —
ne pas conclure à une absence totale de toute donnée, seulement à une absence d'accès libre en ligne.

---

## 3. Comparatif / analyse critique

- **FBF national reste la référence la plus fiable et la plus régulière** (annuelle, méthodologie
  stable depuis >20 ans, cosignée par ASFFOR/EFF/Société Forestière CDC) — c'est la seule source
  couvrant fiablement la France entière avec une série longue.
- **Les observatoires "régionaux" identifiés sont presque tous des rediffusions/commentaires locaux
  de l'indicateur national FBF**, pas des observatoires de prix indépendants avec méthodologie propre
  (exception partielle : les bulletins CNPF Bretagne-Pays de la Loire qui publient des tableaux de
  prix chiffrés propres, issus de ventes groupées EFF locales).
- **Confusion terminologique fréquente à éviter** : plusieurs interprofessions Fibois régionales ont
  des "observatoires" qui ne portent PAS sur le prix du bois sur pied mais sur d'autres sujets :
  bois énergie/bois bûche (AURA, BFC), coût technique des machines (Normandie), construction bois
  (Île-de-France), bois industrie/énergie (Grand Est). Un intégrateur pressé pourrait croire à tort
  que ces pages fournissent des prix de bois d'œuvre sur pied.
- **La structure GRECO de `RegionalPricePresets.kt` (12 zones écologiques A-L) est probablement plus
  pertinente scientifiquement que les 13 régions administratives** pour la variation des prix
  (le prix dépend de la station forestière/qualité stationnelle plus que de la frontière
  administrative), mais elle ne correspond pas aux découpages des sources externes disponibles
  (CNPF, Fibois, La Forêt bouge sont tous organisés par région administrative). Un travail de
  correspondance GRECO ↔ région administrative serait nécessaire pour exploiter ces sources.

---

## 4. Recommandation pour GeoSylva

1. **Ne pas ajouter de presets par région administrative dans `RegionalPricePresets.kt` tel quel** —
   l'architecture actuelle (GRECO) est cohérente avec l'reste du code (`GrecoRegion.kt`/`GrecoDetector.kt`)
   et il n'existe pas de données de prix administratives suffisamment denses/fiables pour justifier un
   second système parallèle par région. **Priorité basse.**
2. **Enrichir les commentaires de sourcing de `RegionalPricePresets.kt`** en ajoutant une note
   explicite indiquant que seules 4 régions administratives (Bretagne, Pays de la Loire, Nouvelle-
   Aquitaine, Occitanie) disposent d'un recoupement régional direct avec les prix nationaux FBF déjà
   utilisés comme base (`SRC = "FBF/ONF/CNPF 2025"`), le reste des GRECO restant calé sur la moyenne
   nationale FBF faute de mieux.
3. **Si un futur besoin business justifie un raffinement régional fin** (ex. affichage "prix
   indicatif dans votre région" dans l'app), la source la plus exploitable serait :
   - CNPF Bretagne-Pays de la Loire (bulletins BFO, tableaux chiffrés par essence/diamètre) pour
     l'Ouest,
   - Fibois Occitanie/Observabois pour l'Occitanie,
   - FBF national pour tout le reste, avec mention explicite "moyenne nationale, pas de donnée
     régionale fiable disponible" pour les 7 régions non couvertes.
4. **Ne pas intégrer les "observatoires" trouvés hors périmètre** (bois énergie AURA/BFC, machines
   Normandie, construction IDF) dans `RegionalPricePresets.kt` — ils ne concernent pas le prix du
   bois sur pied et créeraient une confusion de source.
5. **Documenter dans `TarifDocumentationScreen`** (déjà mentionné comme cible dans
   `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.1) la liste des sources par région avec leur statut
   (voir tableau §2.2), pour que l'utilisateur professionnel sache où vérifier lui-même un prix
   dans sa région plutôt que de faire croire à une couverture nationale homogène qui n'existe pas.

---

## 5. Limites et points à vérifier manuellement

- Cette recherche s'appuie uniquement sur des résultats de moteur de recherche web et quelques pages
  fetchées directement (Fibois Occitanie, CNPF national) au cours d'une seule session (2026) — les
  sites CNPF régionaux et Fibois n'ont pas tous été explorés exhaustivement page par page ; des pages
  "prix des bois" dédiées pourraient exister sans être bien indexées par les moteurs de recherche
  utilisés.
- Le statut "observatoire à venir" de La Forêt bouge Grand Est (annoncé en 2018 sur la page consultée)
  n'a **pas pu être revérifié en profondeur** : il est possible qu'il ait été mis en ligne depuis et
  que la page indexée soit une version cache obsolète — `[À VÉRIFIER MANUELLEMENT]` avant toute
  décision produit.
- L'accès restreint constaté pour Observabois Hauts-de-France ("espace personnel soumis à
  conditions") n'a pas permis de vérifier le contenu réel de la rubrique "Prix" du baromètre de
  conjoncture — un contact direct avec Fibois Hauts-de-France serait nécessaire pour confirmer la
  nature exacte des données (prix sur pied vs prix bord de route vs prix rendu usine).
- Les chiffres FBF nationaux cités (86 €/m³ en 2025, 90 €/m³ en 2024, 84 €/m³ en 2023, 94 €/m³ en
  2022) sont repris de résumés d'articles CNPF/Fibois/observatoire FBF — cohérents avec ceux déjà
  cités dans `REFERENTIELS_FORESTIERS_EXTERNES.md` §2.1, mais **le document PDF source officiel
  (indicateur 2026) n'a pas été téléchargé et lu intégralement** dans cette session ; à faire avant
  toute intégration dans le code de calcul.
- Aucune vérification n'a été faite sur d'éventuelles newsletters papier ou espaces adhérents
  payants des interprofessions régionales qui pourraient contenir des données de prix plus fines
  non accessibles publiquement.
