## Contexte
3 TODO métier identifiés pour améliorer la précision des données terrain.

## Fichiers concernés

### 1. MNT 75m embarqué (SRTM France)
- `TerritorialResolver.kt:20,315` — Remplacer par MNT 75m embarqué (SRTM France)
- **Objectif** : Résolution d'altitude offline sans dépendre d'un service externe
- **Approche** : Embarquer un MNT SRTM 75m pour la France (~50MB compressé) avec interpolation bilinéaire

### 2. Manifest remote + cache signé
- `PackResolver.kt:136` — Charger depuis manifest remote + cache signé
- **Objectif** : Permettre la mise à jour des packs régionaux sans update app
- **Approche** : Manifest JSON signé (ed25519), cache local avec validation de signature

### 3. Indicateurs qualité terrain
- `GeoPackDescriptor.kt:102` — Ajouter indicateurs qualité terrain
- **Objectif** : Permettre à l'utilisateur d'évaluer la qualité des données d'un pack
- **Approche** : Métadonnées dans le descriptor (date collecte, source, couverture, précision)

## Priorité
Basse — améliorations produit, pas bloquantes

Generated with [Devin](https://devin.ai)
