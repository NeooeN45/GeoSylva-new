# Indices bioclimatiques forestiers — formules, seuils et implémentation GeoSylva

**Domaine** : docs/recherche/03_climat/
**Date de recherche** : 2026-07-01 (relance 2026-07-02 après bug de persistance)
**Agent** : climat-05-indices-bioclim

> Document reconstitué à partir du résumé détaillé du sous-agent initial (fichier
> non persisté). Test Climessences par webfetch le 2026-07-01.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| Persée (Wacussel 1926) | Scientifique | scientifique | https://www.persee.fr | 1926 |
| Wikipédia Emberger Q2 | Tierce | commerciale/tierce | https://fr.wikipedia.org/wiki/Indice_ombrothermique | — |
| Climessences V2 (CNPF/INRAE) | Officielle | officielle | https://climessences.fr | V2 |
| INRAE BILJOU | Scientifique | scientifique | https://www.gip-ecofor.org/biljou | — |
| data.gouv.fr ETP Hargreaves Safran 8 km | Officielle | officielle | https://www.data.gouv.fr | Licence Ouverte 2.0 |
| CNPF Bourgogne-Franche-Comté | Officielle | officielle | https://www.cnpf.fr | — |
| Lebourgeois RENECOFOR | Scientifique | scientifique | INRAE | — |

---

## 2. Données détaillées

### 2.1 État actuel dans GeoSylva

- **De Martonne** (`P/(T+10)`) : déjà calculé dans `ClimateContextService.kt` (l. 112)
  — mais sur **1 an ERA5 2023**, pas sur normales 30 ans.
- **ETP Turc annuelle** : stockée dans `NormalesClimatiques.kt` — mais **non exploitée**
  dans un bilan hydrique.
- `BioClimaticRiskDatabase.kt` : catalogue **qualitatif**, pas des indices calculés.
- **Wacussel, Emberger Q2, bilan P−ETP, DHYa, BILJOU** : **non implémentés**.

### 2.2 Indices forestiers français — tableau central

| Indice | Formule | Données requises | Seuils par essence | Source |
|--------|---------|------------------|---------------------|--------|
| De Martonne | `P/(T+10)` | P, T annuelles | Seuils aridité génériques | Persée 1926 |
| Wacussel | `P/T` (ou variantes) | P, T annuelles | Par essence (à extraire) | Persée 1926 |
| Emberger Q2 | `2000·P/(Tmax+Tmin)` | P, Tmax, Tmin | Usage méditerranéen | Wikipédia / Emberger |
| Bilan P−ETP | `P − ETP` mensuel | P, ETP mensuelles | Par essence | INRAE |
| **DHYa (Climessences)** | Bilan mensuel P−ETP avec tampon RUM, boucle 3 ans | P, ETP mensuelles, RUM | **Par essence (Climessences)** | ONF/CNPF |
| BILJOU (INRAE) | Bilan hydrique journalier | P, ETP, RUM journaliers | Par essence | INRAE |

### 2.3 Formule Hargreaves (ETP recommandée par Climessences)

```
ETP = 0,0023 · (Tmoy + 17,8) · (Tmax − Tmin)^0,5 · Ra
```

où `Ra` = rayonnement extraterrestre (fonction de la latitude et du jour julien).

**Pourquoi Hargreaves et pas Penman-Monteith** : les pros forestiers français
(Climessences) utilisent Hargreaves car Penman-Monteith est trop gourmand en inputs
(rayonnement net, vent, humidité) — Hargreaves ne nécessite que Tmoy/Tmax/Tmin + latitude.

### 2.4 Test Climessences (webfetch 2026-07-01)

- **Aucune API ouverte** : site Drupal avec login obligatoire
- Export CSV/GEOJSON par espèce **derrière compte** (pas de REST/JSON public)
- **Méthodologie DHYa publique et réimplémentable** en Kotlin offline
- **Seuils par essence à extraire manuellement** via un compte Climessences
- BILJOU : simulation à accès restreint, fiches publiques

---

## 3. Comparatif / analyse critique

| Indice | Pertinence forestière FR | Faisabilité mobile offline | Priorité GeoSylva |
|--------|--------------------------|----------------------------|-------------------|
| De Martonne | Moyenne (déjà présent) | ✅ Triviale | Déjà fait (à étendre 30 ans) |
| Wacussel | Moyenne | ✅ Triviale | P2 |
| Emberger Q2 | Méditerranée seulement | ✅ Triviale | P2 (PACA, Corse) |
| Bilan P−ETP | Bonne | ✅ Facile (mensuel) | P1 |
| **DHYa (Climessences)** | **Excellente (standard FR)** | ✅ **Réimplémentable** | **P1 absolue** |
| BILJOU journalier | Excellente | ❌ Trop gourmand (données journalières) | Écarté |

---

## 4. Recommandation pour GeoSylva

1. **Priorité absolue** : implémenter **DHYa de Climessences** (ONF/CNPF) dans un
   nouveau `BioclimaticIndicesEngine.kt` :
   - Bilan mensuel P−ETP avec tampon RUM (réserve utile maximale du sol)
   - Boucle sur 3 ans (sécheresses cumulées)
   - ETP via **Hargreaves** (`0,0023·(Tmoy+17,8)·(Tmax−Tmin)^0,5·Ra`)
   - Standard professionnel français de fait pour le choix d'essence sous CC
2. **Étoffer `NormalesClimatiques.kt`** : actuellement T/P annuels + JJA/DJF seulement
   — ajouter les **12 mois** (Tmoy/Tmax/Tmin/P mensuels) pour alimenter DHYa.
   - Sources : Météo-France normales 1991-2020, ou data.gouv.fr ETP Safran Hargreaves
     8 km (Licence Ouverte 2.0)
3. **Extraire manuellement les seuils par essence** via un compte Climessences pour
   alimenter une table `EssenceBioclimThresholds` (DHYa min/max par essence).
   **Ne pas crawler** Climessences (pas d'API, login requis).
4. **Écarter BILJOU journalier** : trop gourmand en données pour mobile offline.
5. **Corriger De Martonne** : passer de 1 an ERA5 2023 à une normale 30 ans.

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Seuils DHYa par essence derrière compte Climessences —
  extraction manuelle requise (login, export CSV par espèce, compilation).
- `[À VÉRIFIER MANUELLEMENT]` Formule exacte du tampon RUM dans DHYa (règle de
  report mois suivant, plafond RUM) — à confirmer sur la documentation publique
  Climessences / CNPF.
- `[À VÉRIFIER MANUELLEMENT]` Cohérence ETP Hargreaves (Climessences) vs ETP Turc
  (déjà dans `NormalesClimatiques.kt`) — à trancher (Hargreaves recommandé, Turc à
  déprécier ou garder en complément).
- `[À VÉRIFIER MANUELLEMENT]` Disponibilité des normales 1991-2020 mensuelles par
  commune (Météo-France vs Safran data.gouv.fr) — à vérifier format et granularité.
- `[À VÉRIFIER MANUELLEMENT]` Valeurs RUM par type de sol — à croiser avec la
  recherche sol/RHU (vague 4, `04_sol_rhu/`).
