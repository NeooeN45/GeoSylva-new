# Protocole de coordination multi-agents — GeoSylva

**Rôle de ce document** : je (Devin, session courante) sers de point de passage obligé entre
toi et les instances d'IA externes (Claude Code, GPT, GLM, Kimi, Gemini, autres sessions
Devin...) qui travaillent en parallèle sur GeoSylva. Objectif n°1, non négociable :
**rien ne casse, rien ne se perd, l'existant continue de fonctionner**.

Tu me colles le rapport/diff d'un agent → je l'audite selon la checklist ci-dessous →
je te dis si c'est safe à committer/merger, ce qu'il faut corriger, ou ce qu'il faut
rejeter → seulement après je te dis si l'agent peut continuer et sur quoi.

---

## 1. Checklist d'admission d'un rapport d'agent

Avant d'accepter le travail d'un agent (commit, merge, "continue"), je vérifie systématiquement :

1. **Portée** — Le diff touche-t-il uniquement ce qui était demandé à cet agent ? Sinon,
   pourquoi (refactor annexe légitime vs dérive de scope) ?
2. **Compile** — `./gradlew compileDebugKotlin` (ou build complet si le diff touche
   Gradle/manifest/ressources) passe sans nouvelle erreur.
3. **Tests** — `./gradlew testDebugUnitTest` : 0 régression. Si l'agent a supprimé ou
   affaibli un test existant sans le remplacer par un test équivalent ou meilleur → rejet.
4. **Architecture** — respect des règles du projet (`AGENTS.md` racine, `global_rules.md`) :
   domain ne dépend pas de data/infra, pas de logique métier dans les composables/ViewModels,
   pas de `!!`, pas de valeur magique non nommée, DTOs aux frontières.
5. **Sécurité / RGPD** — pas de secret en dur, pas de nouvelle fuite de PII, pas de
   désactivation silencieuse d'un mécanisme de sécurité existant (SQLCipher, cert pinning...).
6. **Cohérence métier forestière** — si le diff touche cubage/tarifs/IBP/GRECO, croiser avec
   `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` et `docs/recherche/`. Toute formule ou
   coefficient modifié doit être sourcé (commentaire + référence).
7. **i18n** — toute nouvelle chaîne visible utilisateur doit être dans `strings.xml`
   (FR **et** EN), pas de chaîne codée en dur.
8. **Pas de fichiers parasites** — pas de scratch/debug laissé à la racine (voir
   `.devin/scratch/` comme dépotoir dédié si besoin), pas de `.env`/clé committée.
9. **Message de commit** — Conventional Commits, explique le "pourquoi".

Si un point échoue → je te donne un verdict clair : **BLOQUANT** (à corriger avant tout
merge) ou **À SURVEILLER** (mergeable mais à traiter en dette technique documentée).

---

## 2. Registre des chantiers en cours (état au 2026-07-01)

| Branche / agent | Contenu constaté | Statut | Action |
|---|---|---|---|
| `feature/pro-pricing-engine` (courante) | Moteur pricing pro 8 coefficients, GIS Lambert93, compas TYPE_ROTATION_VECTOR, refactor repositories→domain models | ✅ build + 467 tests OK, commité (3 commits ce jour) | Active, à continuer |
| `origin/feature/diagnostics-rework` | SRTM/DEM, Ripisylve, Station diagnostic (mars 2026) | ⚠️ Probablement obsolète — ces fonctionnalités semblent déjà présentes sur `main` sous une autre forme | **À auditer avant tout merge** : comparer avec l'état actuel de `StationDiagnosticScreen`/`RipisylveDiagnostic`, risque de régression si mergé tel quel |
| `feature/integrate-disk-rework` (locale) | Aucun commit d'avance sur `main` | Vide/fusionnée | Candidate à suppression après confirmation |
| `origin/devin/1778324660-code-quality-improvements` | CI GitHub Actions, refactor MartelageModels, tests migration Room, permissions runtime | Non mergée sur `main` — travail de qualité pertinent | **À évaluer en priorité** : semble sûr et utile (CI manquante actuellement) |

**Remarque** : je n'ai pas encore vérifié si ces branches distantes sont des travaux
d'agents actuellement actifs ou des reliquats anciens. À confirmer avec toi avant toute
action (merge, rebase, suppression).

---

## 3. Registre des agents externes (à remplir avec toi)

| Agent / outil | Domaine assigné | Branche | Dernier rapport reçu | Statut |
|---|---|---|---|---|
| _(à compléter)_ | | | | |

Complète ce tableau au fur et à mesure que tu m'indiques qui travaille sur quoi.

---

## 4. Format attendu quand tu me transmets un rapport d'agent

Pour que je puisse auditer vite et bien, idéalement transmets-moi :
1. Nom de l'agent/outil + branche/commit concerné (ou colle directement le diff/patch)
2. Ce qui lui avait été demandé
3. Son rapport tel quel (je ne réinterprète pas, je vérifie)

Si tu n'as que le rapport texte sans le code, je vais chercher le diff correspondant
dans le repo (`git log`, `git diff`) avant de me prononcer.

---

## 5. Règles de non-régression (rappel)

- Aucun merge sur `main` sans : build OK + tests OK + revue de la checklist §1.
- Aucune fonctionnalité existante documentée dans `AI_CONTEXT.md` §2.3/2.4 (couverture
  fonctionnelle) ne doit régresser silencieusement — si un agent la modifie, il doit le
  signaler explicitement et je le vérifie manuellement (parcours utilisateur, pas juste
  compilation).
- Toute décision de merge/rejet significative est consignée dans
  `.obsidian-vault/20_DECISIONS/` (format ADR) pour traçabilité inter-outils.

---

## 6. Historique des sessions de coordination

### 2026-07-01
- Découverte de ~35 fichiers modifiés + 7 nouveaux non commités sur `feature/pro-pricing-engine`.
  Analysé : refactor architecture domain/data (repositories → modèles domaine) + polish UI
  (couleurs sémantiques, loading state). Build + 467 tests unitaires vérifiés OK.
  → Commité en 3 commits séparés (refactor, UI, docs).
- Lancement d'un sous-agent de recherche → production de
  `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` (18 sources) + scaffold `docs/recherche/`.
- Nettoyage : fichiers de scratch (`.check_missing.py`, `.ui.xml`, brouillons d'issues
  GitHub) déplacés de la racine vers `.devin/scratch/`.
- Création de ce protocole de coordination.
- **À faire ensuite** : refonte complète de `MASTER_PLAN.md`.
