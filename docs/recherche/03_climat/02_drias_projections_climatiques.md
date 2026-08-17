# DRIAS — projections climatiques 2030-2100 pour la vulnérabilité forestière

**Domaine** : docs/recherche/03_climat/
**Date de recherche** : 2026-07-02 (relance après bug de persistance)
**Agent** : climat-02-drias

> Document reconstitué à partir du résumé détaillé du sous-agent initial (fichier
> non persisté). Tests d'accès réels effectués le 2026-07-02.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| DRIAS-2020 (portail) | Officielle | officielle | https://www.drias-climat.fr | 2020 (référence actuelle) |
| Euro-CORDEX (modèles) | Scientifique | scientifique | https://www.euro-cordex.net | CMIP5 / RCP |
| TRACC (doctrine adaptation) | Officielle | officielle | https://www.ecologie.gouv.fr | Giec France |
| Météo-France / CERFACS / IPSL / CNRM | Officielle | officielle | https://www.drias-climat.fr/acces | — |
| `ProjectionClimatiqueSerData.kt` (code app) | Code interne | — | app/src/main/.../geo/ | — |

---

## 2. Données détaillées

### 2.1 DRIAS-2020 (référence actuelle)

- **Modèles** : 12 couples GCM/RCM Euro-Cordex (ALADIN63, RACMO22E, etc.)
- **Reprojection** : corrigés sur grille **SAFRAN 8 km** (maille française native)
- **Pas de temps** : journalier, 1950 → 2100
- **Scénarios** : **RCP 2.6 / 4.5 / 8.5** (CMIP5 / AR5) — **PAS SSP** (AR6 non disponible dans DRIAS-2020)
- **Indicateurs** : ~24 (températures, jours de gel/chaleur, précipitations, ETP,
  **bilan hydrique**) aux fréquences mensuelle / saisonnière / annuelle

### 2.2 Approche complémentaire TRACC

- Doctrine officielle française d'adaptation au changement climatique
- Scénarios simplifiés : +2 °C / +2,7 °C / +4 °C France à horizons 2030 / 2050 / 2100
- Utile pour une communication utilisateur claire (pas besoin de comprendre RCP/SSP)

### 2.3 Test d'accès réel (2026-07-02)

- Consultation cartes / documentation : **libre sans compte**
- **Téléchargement de données : compte personnel requis** (formulaire web, gratuit)
- **Aucune API REST/JSON publique** : les données sont livrées en fichiers NetCDF
- → Accès programmatique direct depuis l'APK Android **impossible**

---

## 3. Comparatif / analyse critique

| Critère | DRIAS-2020 | TRACC | Open-Meteo Climate (CMIP6) |
|---------|-----------|-------|----------------------------|
| Scénarios | RCP 2.6/4.5/8.5 (AR5) | +2/+2,7/+4 °C | SSP (AR6) |
| Résolution | SAFRAN 8 km (FR) | National | ~25 km mondial |
| Accès API | ❌ (NetCDF + compte) | ❌ (doctrine) | ✅ (REST sans clé) |
| Bilan hydrique | ✅ | — | Partiel |
| Adapté mobile direct | ❌ | ❌ | ✅ (mais moins précis FR) |

---

## 4. Recommandation pour GeoSylva

1. **Stratégie viable** : pré-traitement hors-ligne + embarquement d'un sous-ensemble
   compact de deltas DRIAS par SER (déjà partiellement fait dans
   `ProjectionClimatiqueSerData.kt`).
2. **Écart critique à corriger** : `ProjectionClimatiqueSerData.kt` étiquette ses
   scénarios `SSP1-2.6 / SSP2-4.5 / SSP5-8.5` (AR6) et cite « DRIAS Météo-France »,
   alors que DRIAS-2020 livre du **RCP** (AR5). Les valeurs chiffrées des deltas ne
   sont pas sourcées point par point `[À VÉRIFIER MANUELLEMENT]`.
   - Action : **re-sourcer / re-étiqueter** les scénarios (RCP, pas SSP) ou basculer
     sur Open-Meteo Climate (CMIP6/SSP) si on veut rester en SSP — mais pas mélanger.
3. **Ajouter une vue TRACC** dans `StationDiagnosticScreen.kt` : communication
   utilisateur plus claire que RCP/SSP (« +2 °C à 2030 » parle à un forestier).
4. **Créer un compte DRIAS** pour remplacer les deltas en dur par des valeurs
   traçables téléchargées et documentées (date de téléchargement, modèle, scénario).
5. Fichiers Kotlin cibles : `ProjectionClimatiqueSerData.kt`, `StationDiagnosticScreen.kt`,
   `StationEnvironnementale.kt`.

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Origine point par point des deltas actuellement embarqués
  dans `ProjectionClimatiqueSerData.kt` — non vérifiée, à retracer depuis DRIAS ou
  Open-Meteo Climate.
- `[À VÉRIFIER MANUELLEMENT]` Disponibilité future de DRIAS-CORDEX AR6 (SSP) — non
  confirmée à la date de recherche ; surveiller drias-climat.fr.
- `[À VÉRIFIER MANUELLEMENT]` Téléchargement réel d'un fichier NetCDF DRIAS non
  effectué (compte non créé) — à faire pour valider le format et la structure des
  indicateurs bilan hydrique.
- `[À VÉRIFIER MANUELLEMENT]` Cohérence TRACC ↔ RCP : vérifier que les horizons
  TRACC (+2/+2,7/+4 °C) correspondent bien aux RCP 2.6/4.5/8.5 dans la doctrine officielle.
