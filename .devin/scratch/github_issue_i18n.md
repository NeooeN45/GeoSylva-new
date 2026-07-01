## Contexte
6 chaînes hardcoded (TODO i18n) restent dans le code au lieu d'utiliser `stringResource()`.

## Fichiers concernés
- [ ] `StationDiagnosticScreen.kt:1308` — chaînes contextuelles diagnostic
- [ ] `StationPhotoGalleryBlock.kt:46` — libellés PhotoCategory (servent aussi de clés)
- [ ] `DiagnosticPhotoCaptureSection.kt:47` — photoTypeOptions comme clés persistées
- [ ] `EditableSynthesisBlock.kt:265` — buildAutoSynthesis et labels
- [ ] `DataInterpretationEngine.kt:428` — chaîne titre+description

## Approche
1. Créer les strings dans `values/strings.xml` et `values-fr/strings.xml`
2. Remplacer les chaînes hardcoded par `stringResource(R.string.xxx)` dans les Composable
3. Pour les chaînes utilisées comme clés persistées (PhotoCategory, photoTypeOptions), introduire un enum ou un mapping clé→label pour séparer la persistance de l'affichage

## Priorité
Moyenne — n'empêche pas le fonctionnement mais casse l'i18n

Generated with [Devin](https://devin.ai)
