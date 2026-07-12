# Copernicus ERA5 / ERA5-Land — climatologie de référence communale

**Domaine** : docs/recherche/03_climat/
**Date de recherche** : 2026-07-02 (relance après bug de persistance)
**Agent** : climat-03-era5

> Document reconstitué à partir du résumé détaillé du sous-agent initial (fichier
> non persisté). Catalogue CDS consulté par webfetch réel ; code source cdsapi examiné.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| Copernicus Climate Data Store (CDS) | Officielle (UE) | officielle | https://cds.climate.copernicus.eu | — |
| ERA5 single-levels (catalogue) | Officielle | officielle | https://cds.climate.copernicus.eu/datasets/reanalysis-era5-single-levels | DOI 10.24381/cds.adbb2d47 |
| ERA5-Land | Officielle | officielle | https://cds.climate.copernicus.eu/datasets/reanalysis-era5-land | — |
| cdsapi (client Python) | Code ouvert | scientifique | https://github.com/ecmwf/cdsapi | — |
| Licence ERA5 | Officielle | officielle | CC-BY 4.0 (ERA5 confirmé) | — |

---

## 2. Données détaillées

### 2.1 ERA5 vs ERA5-Land

| Critère | ERA5 | ERA5-Land |
|---------|------|----------|
| Résolution spatiale | 0.25° ≈ 31 km | 0.1° ≈ 9 km |
| Pas de temps | Horaire | Horaire |
| Couverture temporelle | 1940 → présent | 1950 → présent |
| Variables de surface | ~240 (atmo + surface) | ~50 (surface uniquement) |
| Humidité sol | Oui | **Oui, 4 couches 0-289 cm** |
| `potential_evaporation` | Oui | Oui |
| Pixels / commune moyenne (~15 km²) | ~0,2 | **~1-2** |
| Licence | CC-BY 4.0 | CC-BY (à confirmer) |

**Conclusion** : ERA5-Land est préférable pour la climatologie communale française
(résolution 9 km → 1-2 pixels par commune vs 0,2 pour ERA5).

### 2.2 Accès technique

- **Client officiel** : `cdsapi` (Python uniquement)
- **API REST HTTP sous-jacente** (examinée dans `cdsapi/api.py`) :
  - PUT `/tasks/services/...` (soumission job)
  - Polling `/jobs/{id}` (statut, minutes → heures)
  - Download (fichier résultat)
  - ⚠️ **Explicitement « non supportée » par ECMWF** (peut casser sans préavis)
- **Format de sortie** : GRIB / NetCDF (grille entière, pas de point unique)
- **Authentification** : clé CDS personnelle (inscription gratuite)

---

## 3. Faisabilité Android : NON en appel direct

Raisons cumulées :
1. `cdsapi` est Python-only (pas de client Kotlin/Java officiel)
2. L'API REST est asynchrone (job + polling minutes→heures) — incompatible avec un
   appel mobile synchrone temps réel
3. La sortie est un fichier GRIB/NetCDF volumineux (grille mondiale ou européenne
   entière, pas de extraction point unique côté serveur)
4. Embarquer la clé CDS dans l'APK = **faille de sécurité** (clé personnelle
   exposée, pas de rotation)

**→ Un backend intermédiaire Python/FastAPI est obligatoire** :
- Télécharge ERA5-Land via cdsapi (une fois / périodiquement)
- Pré-calcule la climatologie par commune (normales 30 ans, ETP, bilan hydrique)
- Sert une API JSON légère à l'app Android

---

## 4. Recommandation pour GeoSylva (séquence)

1. **Court terme** : exploiter **Open-Meteo** (déjà intégré, dérivé d'ERA5, API REST
   sans clé) pour la série historique par point GPS — voir
   `03_climat/04_open_meteo_comparatif.md`. Aucun backend nécessaire.
2. **Moyen terme** : backend ERA5-Land pour les variables sol détaillées (humidité
   4 couches) et la série 1950→ (normales 30 ans robustes).
3. **Long terme** : croiser ERA5-Land (climatologie passée) avec **DRIAS**
   (projections 2050/2100) pour l'aptitude future des essences — voir
   `03_climat/02_drias_projections_climatiques.md`.

Aucun intérêt à court-circuiter Open-Meteo pour ERA5 direct sur mobile.

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Licence exacte ERA5-Land (CC-BY supposé par analogie
  avec ERA5, à confirmer sur la page du dataset).
- `[À VÉRIFIER MANUELLEMENT]` Inscription CDS + test end-to-end d'un téléchargement
  ERA5-Land non effectué (compte non créé) — à faire pour valider le format NetCDF
  et la structure des variables sol.
- `[À VÉRIFIER MANUELLEMENT]` Quotas CDS (limite de volume/tâches simultanées par
  compte gratuit) — à vérifier après inscription.
- `[À VÉRIFIER MANUELLEMENT]` Sémantique d'accumulation des variables ERA5-Land
  (horaire vs cumul journalier, signe de `potential_evaporation`) — à confirmer sur
  la documentation CDS avant tout calcul de bilan hydrique.
- `[À VÉRIFIER MANUELLEMENT]` Architecture réseau actuelle de l'app (existe-t-il
  déjà un backend GeoSylva, ou tout est client-side ?) — conditionne la faisabilité
  du backend ERA5-Land.
