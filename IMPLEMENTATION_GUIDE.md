# Guide d'Implémentation - GéoSylva

## 📋 Checklist de Développement

### Phase 1: Foundation ✅ (Complété)
- [x] Configuration Gradle et dépendances
- [x] Entities Room (Groups, Counters, Formulas, Variables)
- [x] DAOs Room avec Flow
- [x] Database Room avec migrations
- [x] Domain models
- [x] Repositories (interfaces + implémentations)
- [x] Entity mappers
- [x] FormulaParser avec exp4j
- [x] DataStore preferences
- [x] Application class avec DI manuel

### Phase 2: UI Core ✅ (Complété)
- [x] Material 3 theme
- [x] Color scheme (light/dark)
- [x] Typography et Shapes
- [x] Navigation Compose
- [x] MainActivity
- [x] GroupsScreen (liste des groupes)
- [x] GroupScreen (détail groupe avec compteurs)
- [x] SettingsScreen
- [x] ViewModels
- [x] Haptic feedback utility
- [x] Color contrast utilities

### Phase 3: Import/Export (En cours)
- [x] Models d'import/export
- [x] ExportDataUseCase (JSON, CSV, XLSX, ZIP)
- [x] ImportDataUseCase (JSON, CSV, XLSX)
- [ ] UI pour import/export
- [ ] Mapping assistant UI
- [ ] Preview import data
- [ ] Progress indicators
- [ ] SQLite import/export

### Phase 4: Formulas & Calculations
- [x] Formula parser avec fonctions
- [x] Filter support (name:, tag:)
- [x] Conditional expressions (if)
- [x] Variable support
- [ ] Formula editor UI
- [ ] Autocomplete pour compteurs
- [ ] Formula validation UI
- [ ] Real-time preview
- [ ] Formula management screen

### Phase 5: Advanced Features
- [ ] Counter options bottom sheet (tabs)
  - [ ] General tab
  - [ ] Display tab (colors, icons)
  - [ ] Formula tab (if computed)
  - [ ] Data tab (import/export mapping)
  - [ ] Automation tab (optional)
- [ ] Field view mode optimized
- [ ] Backup scheduling (WorkManager)
- [ ] Batch operations
- [ ] Search & filter counters
- [ ] Sort options
- [ ] Counter templates

### Phase 6: Polish & Testing
- [ ] Animations et transitions
- [ ] Loading states
- [ ] Error handling UI
- [ ] Empty states
- [ ] Confirmation dialogs
- [ ] Undo/Redo
- [ ] Accessibility improvements
- [ ] Unit tests
- [ ] Integration tests
- [ ] UI tests (Compose)
- [ ] Performance optimization

## 🚧 Tâches Prioritaires

### Critique
1. **Import/Export UI** - Permettre à l'utilisateur d'importer/exporter
2. **Formula Editor** - Interface pour créer/éditer des formules
3. **Counter Options Sheet** - Options détaillées avec onglets

### Important
4. **Field View Mode** - Vue terrain optimisée (gros boutons)
5. **Backup System** - Sauvegardes automatiques
6. **Error Handling** - Gestion globale des erreurs

### Nice to Have
7. **Templates** - Templates prédéfinis (essence×classe)
8. **Statistics** - Graphiques et stats
9. **Export PDF** - Rapports PDF

## 🔧 Points Techniques à Vérifier

### Performance
- [ ] Streaming pour gros fichiers XLSX
- [ ] Pagination pour grandes listes
- [ ] Lazy loading des compteurs
- [ ] Cache des formules calculées
- [ ] Background processing pour import/export

### Sécurité
- [ ] Input validation
- [ ] SQL injection prevention (Room le gère)
- [ ] File access permissions (SAF)
- [ ] ProGuard rules complètes

### UX
- [ ] Loading indicators
- [ ] Error messages clairs
- [ ] Confirmation dialogs
- [ ] Undo actions
- [ ] Keyboard navigation
- [ ] Screen reader support

## 📝 Notes d'Implémentation

### Import/Export UI à Créer

```kotlin
// Screens à ajouter:
ImportScreen(
    uri: Uri,
    format: ExportFormat,
    onMappingComplete: (ImportMapping) -> Unit
)

ExportScreen(
    groupId: String?,
    format: ExportFormat,
    layout: ExportLayout,
    onExport: (Uri) -> Unit
)

MappingScreen(
    headers: List<String>,
    previewData: List<List<String>>,
    onMappingChange: (List<ImportMapping>) -> Unit
)
```

### Formula Editor à Créer

```kotlin
FormulaEditorScreen(
    groupId: String,
    formula: Formula?,
    counters: List<Counter>,
    onSave: (Formula) -> Unit
)

// Features:
// - Syntax highlighting
// - Autocomplete des compteurs
// - Validation en temps réel
// - Preview du résultat
// - Liste des fonctions disponibles
```

### Counter Options Sheet (Tabs)

```kotlin
CounterOptionsSheet(
    counter: Counter,
    tabs: List<Tab> = listOf(
        Tab.GENERAL,    // nom, pas, min/max, valeur
        Tab.DISPLAY,    // couleurs, icône, taille
        Tab.FORMULA,    // si computed
        Tab.DATA,       // import/export mapping
        Tab.AUTOMATION  // objectif, alertes
    )
)
```

## 🎯 Prochaines Étapes Recommandées

1. **Créer ImportExportScreen**
   - Sélection de fichier (SAF)
   - Choix du format
   - Mode import (Replace/Merge/Add)
   - Progress indicator

2. **Créer FormulaEditorScreen**
   - TextField avec syntax highlighting
   - Autocomplete dropdown
   - Preview panel
   - Validation feedback

3. **Améliorer CounterOptionsSheet**
   - Ajouter onglets
   - Formulaire complet
   - Preview en direct

4. **Ajouter Tests**
   - Tests unitaires pour FormulaParser
   - Tests de repository
   - Tests UI Compose

5. **Optimisations**
   - Streaming XLSX
   - Cache Room
   - Background jobs

## 🐛 Bugs Connus / À Tester

- [ ] Import CSV avec encodages différents
- [ ] Import XLSX avec formules Excel
- [ ] Gros fichiers (>10MB)
- [ ] Formules avec références circulaires
- [ ] Rotation d'écran (state preservation)
- [ ] Deep links
- [ ] Process death handling

## 📚 Documentation à Compléter

- [ ] Architecture Decision Records (ADR)
- [ ] API documentation
- [ ] User guide complet
- [ ] Video tutorials
- [ ] FAQ
- [ ] Troubleshooting guide

## 🎨 Design Tokens

```kotlin
// À centraliser dans theme/
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

object Elevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
}

object Animation {
    const val durationShort = 150
    const val durationMedium = 300
    const val durationLong = 500
}
```

---

**Status du Projet:** 60% complété
**Prochaine Milestone:** Import/Export UI + Formula Editor
**ETA:** 2-3 semaines de développement restantes
