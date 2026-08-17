# API Météo-France — accès, endpoints et cas d'usage forestier

**Domaine** : docs/recherche/03_climat/
**Date de recherche** : 2026-07-01 (relance 2026-07-02 après bug de persistance)
**Agent** : climat-01-meteofrance

> Ce document a été reconstitué à partir du résumé de recherche détaillé produit
> par le sous-agent initial (dont le fichier markdown n'avait pas persisté). Le
> contenu factuel (endpoints, tests, payloads) provient de tests webfetch réels
> effectués le 2026-07-01. Les payloads intégraux extraits par l'agent initial
> ne sont pas reproduits ici in extenso — voir section Limites.

---

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|--------|------|-----------|-----|--------------|
| API officielle Open Data Météo-France | Officielle | officielle | https://public-api.meteofrance.fr | Ouverture gratuite 01/01/2024 |
| API mobile (non documentée) | Tierce (reverse-engineered) | commerciale/tierce | https://webservice.meteofrance.com | Embarquée app officielle MF |
| Portail développeur Météo-France | Officielle | officielle | https://portail-api.meteofrance.fr | 2024+ |
| Météo des forêts (saisonnière) | Officielle | officielle | https://meteofrance.com/meteo-des-forets | Juin-sept annuel |

---

## 2. Données détaillées

### 2.1 Deux API distinctes à ne pas confondre

**API officielle Open Data** (`public-api.meteofrance.fr`) :
- Authentification : OAuth2 Bearer token obligatoire
- Gratuit depuis le 01/01/2024 (inscription sur portail-api.meteofrance.fr)
- Quotas : 60–100 requêtes/min selon endpoint
- Endpoints identifiés : `DPObs/liste-stations`, `DPVigilance/v1/cartevigilance/encours`,
  Météo des forêts saisonnière (juin–sept), observations par station
- **Seule sûre pour un usage commercial en production** (token propre, quotas officiels)

**API « mobile » non documentée** (`webservice.meteofrance.com`) :
- Token statique embarqué dans l'app officielle Météo-France (`__Wj7d...kj8__`)
- Zéro inscription nécessaire
- Résolution du point GPS en commune INSEE côté serveur
- 14 jours de prévisions humanisées (daily + horaire)
- ⚠️ **Non documentée, token non officiel — à utiliser en POC uniquement, pas en production commerciale**
  (risque : retrait/changement du token sans préavis, CGU potentiellement non respectées)

### 2.2 Tests réels documentés (2026-07-01)

**Sans token (API officielle)** :
- `GET DPObs/liste-stations` → HTTP 401 `{"code":"900902","message":"Missing Credentials"}`
- `GET DPVigilance/v1/cartevigilance/encours` → HTTP 401 (même erreur)
- Confirme : token OAuth2 obligatoire sur l'officielle.

**Avec token mobile (API non documentée)** — tous HTTP 200 :
- `forecast` (Lyon) : 14 jours daily + horaire, champs `insee`, `dept`, `sun`, `wind.gust`
- `v3/warning/currentphenomenons` : vigilance tous départements, phénomènes 1–9 / couleurs 1–4
- `v2/observation` : GeoJSON Feature gridded au point GPS

### 2.3 Granularité

- Prévisions : résolution par commune INSEE (point GPS → commune)
- Vigilance : départementale
- Observations : par station météo + grille au point GPS
- Météo des forêts : départementale, 4 niveaux de danger feux, J+1/J+2, saison juin–sept

---

## 3. Comparatif / analyse critique

| Critère | API officielle Open Data | API mobile non documentée |
|---------|--------------------------|---------------------------|
| Inscription | Requise (gratuite) | Aucune |
| Token | OAuth2 Bearer propre | Statique embarqué (non officiel) |
| Production commerciale | ✅ Oui | ⚠️ Non (POC seulement) |
| Quotas documentés | 60–100 req/min | Inconnu |
| Vigilance officielle FR | ✅ | ✅ |
| Météo des forêts | ✅ (saisonnière) | Partiel |
| Stabilité contrat | ✅ | ❌ (peut casser à tout moment) |

**Apport unique vs Open-Meteo déjà intégré** : vigilance officielle française +
Météo des forêts (danger feux) — Open-Meteo ne fournit ni l'un ni l'aut.

---

## 4. Recommandation pour GeoSylva

1. **POC court terme** via API mobile (vigilance départementale + prévisions 7 j +
   obs temps réel par point GPS parcelle) — permet de valider l'UX sans friction d'inscription.
2. **Migration production** vers API officielle (DPVigilance, Météo des forêts
   saisonnière juin–sept, DPObs par station) via proxy backend OAuth2 — le token ne doit
   pas être embarqué dans l'APK (faille de sécurité + rotation impossible).
3. **Météo des forêts** (danger feux, 4 niveaux, J+1/J+2, départemental) = l'apport le
   plus spécifique au forestier sur le terrain. Limite : reste grossière (pas d'IFM maillé
   public disponible).
4. Fichiers Kotlin cibles : `ClimateContextService.kt` (extension), nouvel écran
   vigilance/feux dans la section terrain.

---

## 5. Limites et points à vérifier manuellement

- `[À VÉRIFIER MANUELLEMENT]` Flow OAuth2 complet de l'API officielle (inscription réelle
  + échange token + refresh) non effectué — à tester avec un compte développeur créé.
- `[À VÉRIFIER MANUELLEMENT]` Payloads intégraux extraits par l'agent initial non
  reproduits in extenso ici (fichier source perdu) — à régénérer si besoin produit.
- `[À VÉRIFIER MANUELLEMENT]` Quotas exacts par endpoint (60 vs 100 req/min) à confirmer
  sur le portail développeur après inscription.
- `[À VÉRIFIER MANUELLEMENT]` Légalité de l'usage du token mobile statique en POC
  (CGU Météo-France à lire) — recommandé de ne pas shipper en production.
- `[À VÉRIFIER MANUELLEMENT]` Disponibilité réelle d'un IFM (Indice Forêt Météo) maillé
  public — non trouvé en accès libre, semble réservé à ONF/Météo-France interne.
