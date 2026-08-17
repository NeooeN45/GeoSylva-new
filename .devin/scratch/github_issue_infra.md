## Contexte
3 TODO infrastructure pour le téléchargement HTTP signé des packs régionaux.

## Fichiers concernés
- `PackManager.kt:17,109,120` — Téléchargement HTTP signé depuis serveur

## Objectif
Mettre en place un système de téléchargement sécurisé pour les packs régionaux (cartes, flore, sols, climat) :
1. **Serveur de distribution** : endpoint HTTP avec packs signés (ed25519)
2. **Client de téléchargement** : `PackManager` avec retry, reprise, validation signature
3. **Cache local signé** : intégrité vérifiée à chaque chargement

## Approche technique
- Format : fichier `.pack` (tar.gz + manifest JSON + signature)
- Signature : ed25519 (clé publique embarquée dans l'app)
- Retry : exponential backoff (3 tentatives max)
- Reprise : HTTP Range pour gros fichiers
- Validation : vérifier signature après téléchargement complet

## Dépendances
- Nécessite un serveur de distribution (S3 + CloudFront ou équivalent)
- Nécessite un pipeline de signature des packs

## Priorité
Moyenne — actuellement les packs sont embarqués dans l'APK, ce qui limite la scalabilité

Generated with [Devin](https://devin.ai)
