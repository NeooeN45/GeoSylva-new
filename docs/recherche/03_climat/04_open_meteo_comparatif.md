# Open-Meteo — usage actuel, variables exploitables et comparatif

**Domaine** : docs/recherche/03_climat/
**Date de recherche** : 2026-07-02 (relance après bug de persistance)
**Agent** : climat-04-openmeteo

> Document reconstitué à partir du résumé détaillé du sous-agent initial (fichier
> non persisté). Tests d'API réels effectués le 2026-07-02.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| Open-Meteo (API) | Tierce (open) | commerciale/tierce | https://open-meteo.com | — |
| Open-Meteo Forecast API | Tierce | commerciale/tierce | https://api.open-meteo.com/v1/forecast | — |
| Open-Meteo Archive API | Tierce | commerciale/tierce | https://archive-api.open-meteo.com | Données ERA5 |
| Open-Meteo Climate API (CMIP6) | Tierce | commerciale/tierce | https://climate-api.open-meteo.com | — |
| `ClimateContextService.kt` (code app) | Code interne | — | app/src/main/.../ | — |

---

## 2. Données détaillées

### 2.1 Usage actuel dans GeoSylva (`ClimateContextService.kt`)

- Endpoint : `archive-api.open-meteo.com`
- **2 variables seulement** : `temperature_2m_mean` + `precipitation_sum`
- Année **2023 figée** (pas de normale 30 ans)
- Modèle **"Best Match"** par défaut
- Cache 0.1°
- Calcul du mois par index `(i*12)/n` — **approximation à corriger**

### 2.2 Variables forestièrement clés DISPONIBLES mais non exploitées

- `et0_fao_evapotranspiration` (ET₀ FAO-56)
- `soil_moisture` / `soil_temperature` (4 horizons 0-289 cm)
- `wind_speed_10m_max` (vent)
- VPD (déficit de pression de vapeur)
- Rayonnement
- Gel

### 2.3 Tests réels d'API (2026-07-02)

**Forecast Paris** (7 jours) :
```
GET https://api.open-meteo.com/v1/forecast?latitude=48.85&longitude=2.35&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=Europe/Paris
→ HTTP 200, latence 0.17 ms
```

**Archive daily "Best Match"** :
- `et0_fao_evapotranspiration_sum` → **`null`**
- `wind_speed_10m_max` → **`null`**
- ⚠ **Découverte critique** : ces variables renvoient `null` en daily avec le modèle
  "Best Match" par défaut.

**Archive hourly avec `models=era5`** :
- ET₀ horaire : 0.01-0.60 mm/h ✅
- soil moisture : 0.17 m³/m³ ✅
- vent : 15-21 km/h ✅
- → **Toutes variables forestières renseignées** dès qu'on force `&models=era5` (ou `era5-land`)

**Conclusion** : il faut ajouter `&models=era5` (ou `era5-land`) pour débloquer ET₀/vent
en daily, sinon ces variables restent `null`.

---

## 3. Comparatif Open-Meteo / Météo-France / ERA5 direct

| Critère | Open-Meteo | Météo-France API | ERA5 direct (CDS) |
|---------|-----------|------------------|-------------------|
| Clé requise | ❌ Non | ✅ OAuth2 | ✅ CDS |
| Appel mobile direct | ✅ Oui | ✅ Oui (proxy) | ❌ Non (backend requis) |
| Précision locale FR | Bonne (dérivé ERA5) | **Meilleure** (stations + AROME 1,3 km) | Bonne (9 km ERA5-Land) |
| Vigilance officielle FR | ❌ | ✅ | ❌ |
| Météo des forêts | ❌ | ✅ | ❌ |
| Coût | Gratuit (fair use) | Gratuit (quota) | Gratuit |
| Série historique | ✅ (via ERA5) | Partiel | ✅ 1950→ |
| Licence | Non-clé, fair use | Etalab-like | CC-BY |

---

## 4. Recommandation pour GeoSylva

1. **Étendre `ClimateContextService`** :
   - Ajouter `&models=era5` explicite (déblocage ET₀/soil/vent)
   - Ajouter les variables forestières : ET₀ FAO-56, soil moisture/temp, vent, gel, VPD
   - Remplacer l'année 2023 figée par une **normale 30 ans** (1991-2020) calculée
     côté app ou backend
   - Corriger le calcul du mois `(i*12)/n` (approximation fausse)
2. **Open-Meteo = proxy sans clé sur ERA5** : meilleur compromis intégration mobile,
   déjà opérationnel — aucun intérêt à court-circuiter vers ERA5 direct.
3. **Météo-France à intégrer en Priorité 1** pour le temps réel / alertes (vigilance,
   Météo des forêts) — voir `03_climat/01_metéo_france_api.md`.
4. **ERA5 direct (CDS)** : source ultime mais inadapté à un appel mobile direct —
   voir `03_climat/03_copernicus_era5.md` (backend requis).

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Licence commerciale Open-Meteo et quota "fair use"
  exact (limite req/jour pour usage commercial) — à vérifier sur open-meteo.com/terms.
- `[À VÉRIFIER MANUELLEMENT]` Disponibilité réelle d'une normale 30 ans via
  Open-Meteo Archive (nécessite de boucler 30 ans d'appels, ou endpoint dédié ?).
- `[À VÉRIFIER MANUELLEMENT]` Cohérence ET₀ Open-Meteo (ERA5) vs ETP Safran
  Météo-France (grille 8 km) — à comparer sur un point test avant d'adopter.
- `[À VÉRIFIER MANUELLEMENT]` Le calcul du mois `(i*12)/n` dans
  `ClimateContextService.kt` — à auditer et corriger (logique suspecte).
