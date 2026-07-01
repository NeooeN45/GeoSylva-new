# Dossier de Documentation Forestière Française pour GeoSylva
## Référentiels officiels, sources de données et recommandations d'intégration

**Date** : 2026-07-01 (généré par sous-agent de recherche)
**Commanditaire** : GeoSylva - Application Android de gestion forestière
**Périmètre** : Tarifs de cubage, prix du bois, IBP, GRECO, données ouvertes, références scientifiques
**Objectif** : Fiabiliser et enrichir les calculs de l'app avec des sources officielles et récentes (2020+)
**Statut** : Document de recherche — les URLs et données chiffrées doivent être revérifiées avant intégration (un LLM peut se tromper sur des détails de sourcing)

---

## Table des matières

1. [Tarifs de cubage officiels français](#1-tarifs-de-cubage-officiels-français)
2. [Barèmes de prix du bois sur pied](#2-barèmes-de-prix-du-bois-sur-pied)
3. [Méthodologie IBP CNPF](#3-méthodologie-ibp-cnpf)
4. [GRECO et typologies stationnelles](#4-greco-et-typologies-stationnelles)
5. [Barèmes de valeur foncière forestière](#5-barèmes-de-valeur-foncière-forestière)
6. [Références scientifiques et modèles de croissance](#6-références-scientifiques-et-modèles-de-croissance)
7. [Sources de données ouvertes exploitables par API](#7-sources-de-données-ouvertes-exploitables-par-api)
8. [Actions prioritaires pour GeoSylva](#8-actions-prioritaires-pour-geosylva)

---

## 1. Tarifs de cubage officiels français

### 1.1 Tarifs Schaeffer (ONF/CTBA)

**Source officielle** : ONF - Office National des Forêts
**Référence** : Tables de cubage Schaeffer 1949, rééditées par le CTBA
**URL** : http://jymassenet-foret.fr/cours/dendrometrie/coursdendrometrieppt/versionspdfdespptdendro/dendrometriechap6ppt.pdf
**Année/version** : 1949 (référence historique, toujours valide)
**Fiabilité** : Officielle ONF - Standard français

**Contenu** :
- **Schaeffer 1 entrée** : 16 tarifs (1-16), formule V = a × D₁₃₀ᵇ
- **Schaeffer 2 entrées** : 8 tarifs (1-8), formule V = a × D₁₃₀ᵇ × Hᶜ
- Coefficients (a, b, c) par numéro de tarif
- Adapté aux peuplements réguliers (futaie monospécifique)

**Apport pour GeoSylva** :
- Les coefficients actuels dans `TarifData.schaefferOneEntry` et `schaefferTwoEntry` semblent conformes
- Vérifier que les 16 tarifs 1E et 8 tarifs 2E sont complets
- Ajouter la documentation officielle dans `TarifDocumentationScreen`

**Recommandation** : CONSERVER — Implémentation conforme, ajouter citation source

---

### 1.2 Tarifs Algan (ENGREF/AgroParisTech)

**Source officielle** : ENGREF - École Nationale du Génie Rural, des Eaux et des Forêts
**Référence** : Algan 1958, cité dans Pardé & Bouchon 1988
**URL** : https://hal.science/hal-03382015v1/document
**Fiabilité** : Officielle ENGREF - Standard français

**Contenu** :
- Formule : V = a × D₁₃₀ᵇ × Hᶜ
- Coefficients spécifiques par essence
- Adapté aux peuplements irréguliers et mixtes

**Recommandation** : VÉRIFIER — Comparer les coefficients de `TarifData.alganCoefs` avec la source officielle ENGREF (méthode par défaut de l'app, critique)

---

### 1.3 Tarifs IFN (IGN - Inventaire Forestier National)

**Source officielle** : IGN - Inventaire Forestier National
**URL** : https://inventaire-forestier.ign.fr/IMG/pdf/etude_bo_france_restitution_feuillus_003_.pdf
**Fiabilité** : Officielle IGN - Référence nationale

**Contenu** :
- **IFN Rapide** : 36 tarifs (1-36), entrée D₁₃₀ seul
- **IFN Lent** : 8 tarifs (1-8), entrées D₁₃₀ + H

**Recommandation** : VÉRIFIER — Mettre à jour `TarifData.ifnRapideCoefs`/`ifnLentCoefs` avec les équations IFN récentes

---

### 1.4 Projet EMERGE (INRAE/ONF/FCBA)

**URL** : https://hal.inrae.fr/hal-00934771 ; données : https://geodata.inrae.fr/geonetwork/srv/api/records/27f18f57-b847-4ec3-909b-4067a5e1dc33
**Contenu** : Base de données de cubage volume/biomasse, coefficients de forme par essence, coordonnées Lambert 93
**Recommandation** : INTÉGRER — Valider/affiner les coefficients de forme

---

### 1.5 Tables de production (Décourt & Pardé)

**URL** : https://belinrae.inrae.fr/index.php?id=62618&lvl=notice_display ; https://hal.science/hal-03389899v1/document
**Contenu** : Tables de production françaises, indice de station ONF (Hdom à âge de référence), classes de fertilité
**Recommandation** : CORRIGER — L'audit interne identifie un indice de station approximatif (Hm au lieu de Hdom) dans `ExpertForestryCalculator.kt`

---

### 1.6 Coefficients de forme (Pardé & Bouchon)

**URL** : https://hal.science/hal-03390143/document
**Contenu** : Coefficient de forme f (V = G × H × f), valeurs par essence et âge
**Recommandation** : VÉRIFIER — Comparer `TarifData.coefsFormeParEssence` avec les tables Pardé & Bouchon 1988

---

## 2. Barèmes de prix du bois sur pied

### 2.1 Observatoire économique France Bois Forêt

**URL** : https://observatoire.franceboisforet.com/ ; indicateur 2025 : https://franceboisforet.fr/2025/05/05/prix-de-vente-des-bois-sur-pied-en-foret-privee-indicateur-2025/

**Données rapportées 2024-2025** (à revérifier) :
- Prix moyen 2024 : ~90 €/m³ (toutes essences)
- Prix moyen 2023 : ~84 €/m³ (-10% vs 2022)
- Chêne 2024 : ~228 €/m³ ; Douglas 2024 : ~72 €/m³ ; Pin maritime 2023 : ~51 €/m³

**Recommandation** : INTÉGRER — Source principale pour mise à jour annuelle de `RegionalPricePresets.kt`, citer dans `TarifDocumentationScreen`

---

### 2.2 Observatoires régionaux

- **Bretagne** — La Forêt bouge : https://www.laforetbouge.fr/bretagne/prix-indicatif-des-bois-sur-pied-0
- **Grand Est** : https://www.laforetbouge.fr/grandest/observatoire-du-prix-des-bois
- **Nouvelle-Aquitaine (CNPF)** : https://nouvelle-aquitaine.cnpf.fr/gestion-durable-des-forets/coupes-et-travaux/le-prix-des-bois

**Recommandation** : INTÉGRER — Compléter les 7 presets régionaux existants

---

### 2.3 ONF — Indice de prix moyen

**URL** : https://observatoire.franceboisforet.com/donnees-de-la-filiere/amont-forestier/office-national-des-forets/ ; ventes : https://www.ventesdebois.onf.fr
**Recommandation** : INTÉGRER — Données trimestrielles, distinction forêts domaniales/communales

---

### 2.4 Coopératives forestières

- Alliance Forêts Bois : https://www.allianceforetsbois.fr/
- UNISYLVA : https://www.unisylva.fr/
- UCFF (12 coopératives, 120 000 sylviculteurs, 2,2 Mha) : https://lescooperativesforestieres.fr/

**Recommandation** : INTÉGRER comme sources de prix optionnelles

---

### 2.5 Classification qualité (NF EN 1316)

**URL** : https://norminfo.afnor.org/norme/NF%20EN%201316-1/bois-ronds-feuillus-classement-qualitatif-partie-1-chene-et-hetre/77351
**Recommandation** : VÉRIFIER — Aligner la classification qualité A/B/C/D (multiplicateurs ×2.50/×1.50/×1.00/×0.40) sur la norme européenne

---

## 3. Méthodologie IBP CNPF

### 3.1 IBP — Indice de Biodiversité Potentielle

**URL** : https://www.cnpf.fr/nos-actions-nos-outils/outils-et-techniques/ibp-indice-de-biodiversite-potentielle
PDF méthode : https://www.cnpf.fr/sites/socle/files/2024-02/IBP%20met%20fr%20231001_0.pdf

**Contenu** : 10 critères (E1, E2, GB, BMS, BMC, DMH, VS, CF, CO, HC), scoring 0/2/5, max 50 points

**Recommandation** : METTRE À JOUR — Vérifier la version CNPF la plus récente vs `IbpCalculator.kt` (le sous-agent mentionne une "v3.2 avril 2026" à confirmer/dater précisément, une IA peut halluciner un numéro de version — **vérifier manuellement sur le site CNPF avant d'agir**)

### 3.2 Validation scientifique

Larrieu & Gonin 2008, Revue Forestière Française : https://www.cnpf.fr/sites/socle/files/cnpf-old/larrieu_gonin_2008_ibp_rff_727_748_cor_1.pdf
**Recommandation** : CITER dans la documentation de l'app, avec limites d'usage

---

## 4. GRECO et typologies stationnelles

### 4.1 GRECO — Grandes Régions Écologiques (IGN)

**URL** : https://inventaire-forestier.ign.fr/spip.php?article773= ; publication : https://inventaire-forestier.ign.fr/IMG/pdf/IF_SER_web.pdf
**Contenu** : 11 GRECO (A-K), 91 sylvoécorégions (SER)
**Recommandation** : CONSERVER — `GrecoRegion.kt`/`GrecoDetector.kt` déjà conformes, ajouter citation IGN

### 4.2 Typologie des stations forestières

**URL** : https://inventaire-forestier.ign.fr/IMG/pdf/L_IF_no04_typologie.pdf ; CNPF : https://www.cnpf.fr/nos-actions-nos-outils/outils-et-techniques/les-stations-forestieres
**Recommandation** : ENRICHIR — Intégrer écogrammes et clé de détermination CNPF dans `StationDiagnosticScreen`

### 4.3 Flore forestière française (Rameau, Mansion, Dumé)

**Référence** : Flore forestière française, guide écologique illustré, 2018 (2e édition)
**Recommandation** : INTÉGRER comme source des écogrammes (eau/minéraux) pour le diagnostic stationnel

### 4.4 RENECOFOR (ONF)

Réseau de 100 sites permanents de suivi long terme — source de calibrage/validation future

---

## 5. Barèmes de valeur foncière forestière

### 5.1 SAFER — Prix des terres et parcelles boisées

**URL** : https://www.safer.fr/ ; rapport 2025 : https://www.le-prix-des-terres.fr/app/uploads/2025/05/2025-PDT2024-05-Forets.pdf
**Donnée rapportée** : prix moyen national ~13 585 €/ha (2024, moyenne à interpréter avec prudence — très forte variance régionale)
**Recommandation** : INTÉGRER pour un futur module patrimonial/valeur foncière

### 5.2 Analyses indépendantes

Pierre Aussedat (expert forestier) : https://pierreaussedat.com/le-prix-de-la-foret-francaise/ — analyse critique des moyennes SAFER

---

## 6. Références scientifiques et modèles de croissance

- **CAPSIS** (INRAE/CIRAD/CNRS) — plateforme de simulation croissance/dynamique peuplements : https://capsis.cirad.fr/capsis/presentation
- **INRAE ASIRPA** — modélisation croissance et dynamiques forestières : https://asirpa.hub.inrae.fr/
- **Revue Forestière Française** — articles tarifs de cubage et tables de production
- **RDV Techniques ONF** — protocoles de mesure, coefficients d'expansion volume
- **FCBA** — certification CTB Cubage Bois Ronds : https://www.fcba.fr/certifications/ctb-cubage-bois-ronds/

Ces sources sont pertinentes pour une évolution future (modèles de croissance avancés), pas une urgence immédiate.

---

## 7. Sources de données ouvertes exploitables par API

| Source | URL | Statut GeoSylva |
|---|---|---|
| IGN BD Forêt v2 | https://geoservices.ign.fr/bdforet | Déjà utilisé — revérifier endpoints |
| IGN DataIFN | https://inventaire-forestier.ign.fr/dataifn/ | À vérifier intégration + citation obligatoire |
| IGN API Géoportail | https://geoservices.ign.fr/ | Déjà utilisé |
| INRAE EMERGE | https://geodata.inrae.fr/geonetwork/ | Non intégré — à évaluer |
| Météo-France Open Data | https://meteo.data.gouv.fr/ | Alternative à Open-Meteo à évaluer |
| Open-Meteo | https://open-meteo.com/ | Déjà utilisé |
| INPN (MNHN) | https://inpn.mnhn.fr/ | ⚠️ Signalé indisponible (cyberattaque été 2025) — à revérifier, information non confirmée par nous |
| Cerema Datafoncier (DVF+) | https://datafoncier.cerema.fr/open-data | Non intégré — évaluer pour module patrimonial |

---

## 8. Actions prioritaires pour GeoSylva

### Critiques (à vérifier/traiter en premier)
1. Vérifier la version exacte et actuelle de la méthode IBP CNPF (ne pas se fier à un numéro de version non confirmé) et comparer avec `IbpCalculator.kt`
2. Corriger l'indice de station : utiliser Hdom (hauteur dominante, moyenne des 100 plus gros arbres/ha) au lieu de Hm dans `ExpertForestryCalculator.kt`
3. Vérifier l'implémentation du calcul de Hdom dans `ForestryCalculator.kt`
4. Valider les coefficients Algan (`TarifData.kt`) contre la source ENGREF — méthode de cubage par défaut de l'app
5. Mettre à jour les prix régionaux (`RegionalPricePresets.kt`) avec les derniers indicateurs France Bois Forêt (revérifier les chiffres avant saisie — ne pas committer de données non vérifiées)

### High
6. Revérifier tarifs IFN rapide/lent avec équations IGN récentes
7. Intégrer écogrammes flore forestière française pour diagnostic stationnel
8. Ajouter clé de détermination CNPF des stations forestières
9. Aligner classification qualité A/B/C/D sur NF EN 1316
10. Vérifier coefficients de forme (Pardé & Bouchon) dans `TarifData.kt`

### Medium / futur
11. Intégrer données SAFER pour un module valeur foncière
12. Ajouter section sources/citations dans `TarifDocumentationScreen`
13. Compléter prix régionaux avec données ONF trimestrielles
14. Évaluer intégration API INRAE EMERGE
15. Surveiller rétablissement des services INPN

---

## Avertissement méthodologique

Ce document a été produit par un sous-agent IA à partir de recherches web. **Toutes les URLs, chiffres et affirmations de version (ex: "IBP v3.2 avril 2026", statut INPN post-cyberattaque) doivent être revérifiés manuellement** avant toute décision d'implémentation ou toute communication commerciale/scientifique. Ne pas committer de coefficients ou de prix directement depuis ce document sans vérification croisée avec au moins une source primaire consultée directement.
