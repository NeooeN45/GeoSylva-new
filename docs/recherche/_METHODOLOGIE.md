# Méthodologie de recherche — docs/recherche/

**Contexte** : GeoSylva est une application Android professionnelle de gestion forestière
(dendrométrie, cubage, pricing, diagnostic stationnel). Cette recherche vise à bâtir une base
de données de référence, fiable et vérifiée, pour alimenter la logique métier de l'app ET
la documentation utilisateur.

## Règles impératives pour chaque document produit

1. **Sourcer systématiquement** : chaque donnée chiffrée doit avoir une URL et, si possible,
   une date de publication/version. Aucune donnée sans source.
2. **Distinguer les faits vérifiés des estimations d'IA** : si une IA (vous) reformule ou
   calcule quelque chose qui n'est pas directement lisible dans la source, le marquer
   explicitement `[À VÉRIFIER MANUELLEMENT]`.
3. **Fiabilité de la source** : classer chaque source en `officielle` (IGN, INRAE, ONF, CNPF,
   Météo-France, BRGM, gouvernement), `scientifique` (revues, HAL, thèses), ou `commerciale/tierce`
   (coopératives, blogs spécialisés) — et le préciser.
4. **Franc-parler sur les lacunes** : si une donnée n'existe pas en accès libre, ou si son
   accès est payant/restreint, le dire clairement plutôt que d'inventer un chiffre.
5. **Format de sortie** : Markdown, structuré avec table des matières, tableaux pour les
   données chiffrées, section "Recommandation pour GeoSylva" à la fin de chaque sous-section.
6. **Tests d'API réels** (quand demandé) : documenter la requête exacte (méthode, URL, params),
   la réponse réelle obtenue (extrait JSON/XML), le code HTTP, les limites de quota, et la
   nécessité ou non d'une clé.
7. **Granularité géographique cible** : France entière, viser la précision communale quand
   possible pour climat/sol (ex : donnée disponible par commune, code INSEE, ou point GPS).
8. **Ne pas dupliquer inutilement** : consulter `docs/REFERENTIELS_FORESTIERS_EXTERNES.md` et
   `RESEARCH_OPPORTUNITIES.md` (déjà en place) avant de repartir de zéro ; les compléter/affiner,
   ne pas les réécrire.

## Structure de fichier attendue (hors fiches essences, cf. ci-dessous)

```markdown
# [Titre du sujet]
**Domaine** : docs/recherche/0X_.../
**Date de recherche** : YYYY-MM-DD
**Agent** : [nom court du sujet]

## 1. Sources identifiées
(tableau : Source | Type | Fiabilité | URL | Date/version)

## 2. Données détaillées
...

## 3. Comparatif / analyse critique
(si plusieurs sources concurrentes, comparer précision, coût, licence)

## 4. Recommandation pour GeoSylva
(quoi intégrer, où dans le code — citer les fichiers Kotlin concernés si connus, priorité)

## 5. Limites et points à vérifier manuellement
```

## Structure spécifique pour les fiches essences (docs/recherche/06_essences/)

Chaque fichier de groupe contient, **pour chaque essence** :

### Couche MOTEUR (structurée, prête à seeder le code)
Tableau strict avec colonnes : code espèce (aligné sur `CanonicalEssences.kt`), tarif de
cubage recommandé (Schaeffer/Algan/IFN + numéro), coefficient de forme, RHU optimal (mm),
texture de sol optimale, pH optimal, exigence en eau (bilan hydrique), température
moyenne annuelle optimale/tolérée, altitude min/max, aire de répartition (GRECO/SER),
vitesse de croissance quantifiée (m³/ha/an si dispo), prix de marché indicatif (€/m³ par
qualité), sensibilité au changement climatique (cote qualitative + source Climessences si
possible).

### Couche UTILISATEUR (texte pédagogique)
Un paragraphe par essence, destiné à être affiché dans l'app (fiche espèce), rédigé pour un
professionnel forestier (pas de vulgarisation excessive), citant en une ligne les sources.

## Index

Chaque agent doit ajouter une ligne dans `docs/recherche/INDEX.md` (créer la section
correspondante si absente) résumant : fichier produit, statut (brouillon/vérifié), et
3 points clés.
