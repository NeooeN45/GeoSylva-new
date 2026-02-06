package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.PlacetteRepository
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.presentation.components.AppMiniDialog
import com.forestry.counter.presentation.utils.rememberHapticFeedback
import com.forestry.counter.presentation.utils.rememberSoundFeedback
import kotlinx.coroutines.launch
import java.text.Normalizer

private fun essenceColor(essence: Essence?): Color? {
    if (essence == null) return null
    // Couleur personnalisée prioritaire si définie
    essence.colorHex?.let { hex ->
        return try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { null }
    }

    // Sinon, couleurs par catégorie, puis fallback hash
    val cat = essence.categorie?.uppercase()?.trim()
    return when (cat) {
        "AVENIR" -> Color(0xFF4CAF50)
        "RESERVE" -> Color(0xFF2196F3)
        "ENLEVER" -> Color(0xFFF44336)
        "DEPERIR" -> Color(0xFFFF9800)
        "BIODIV" -> Color(0xFF26A69A)
        else -> hashColorFromCode(essence.code)
    }
}

private fun hashColorFromCode(code: String): Color {
    val palette = listOf(
        Color(0xFF4CAF50), // green
        Color(0xFF2196F3), // blue
        Color(0xFFF44336), // red
        Color(0xFFFF9800), // orange
        Color(0xFF26A69A), // teal
        Color(0xFF9C27B0), // purple
        Color(0xFF009688), // deep teal
        Color(0xFF795548), // brown
        Color(0xFF607D8B)  // blue gray
    )
    val idx = (code.hashCode() and 0x7FFFFFFF) % palette.size
    return palette[idx]
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlacetteDetailScreen(
    parcelleId: String,
    placetteId: String,
    essenceRepository: EssenceRepository,
    tigeRepository: TigeRepository,
    placetteRepository: PlacetteRepository,
    userPreferences: UserPreferencesManager,
    onNavigateToDiametres: (parcelleId: String, placetteId: String, essenceCode: String) -> Unit,
    onNavigateToMartelage: (parcelleId: String, placetteId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showColorDialog by remember { mutableStateOf(false) }
    var colorTargetEssence by remember { mutableStateOf<Essence?>(null) }
    var actionTargetEssenceCode by remember { mutableStateOf<String?>(null) }
    var showEssenceActionsDialog by remember { mutableStateOf(false) }
    var deleteTargetEssenceCode by remember { mutableStateOf<String?>(null) }
    var showDeletePlacetteDialog by remember { mutableStateOf(false) }

    val allEssences by essenceRepository.getAllEssences().collectAsState(initial = emptyList())
    val tiges by tigeRepository.getTigesByPlacette(placetteId).collectAsState(initial = emptyList())
    val persistedOrder by userPreferences.essenceOrderFlow(placetteId).collectAsState(initial = emptyList())

    val hapticEnabled by userPreferences.hapticEnabled.collectAsState(initial = true)
    val soundEnabled by userPreferences.soundEnabled.collectAsState(initial = true)
    val hapticIntensity by userPreferences.hapticIntensity.collectAsState(initial = 2)
    val animationsEnabled by userPreferences.animationsEnabled.collectAsState(initial = true)
    val haptic = rememberHapticFeedback()
    val sound = rememberSoundFeedback()

    fun playClickFeedback() {
        if (hapticEnabled) haptic.performWithIntensity(hapticIntensity)
        if (soundEnabled) sound.click()
    }

    // Usage par essence dans cette placette
    val usageByEssence = remember(tiges) {
        tiges.groupBy { it.essenceCode }.mapValues { it.value.size }
    }

    // Essences actuellement présentes (blocs existants)
    // On garde aussi les essences présentes dans l'ordre persistant, même sans tige,
    // pour éviter que les blocs disparaissent au retour.
    val presentEssences = remember(usageByEssence, persistedOrder) {
        (usageByEssence.keys + persistedOrder).distinct()
    }

    // Ordre override (drag & drop). Si vide => tri par usage desc puis nom, ou ordre persistant si disponible.
    var orderOverride by remember { mutableStateOf<List<String>>(emptyList()) }
    var reorderMode by remember { mutableStateOf(false) }
    var draggingCode by remember { mutableStateOf<String?>(null) }
    var dragAccum by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val itemStepPx = with(density) { (84.dp + 12.dp).toPx() }

    fun defaultSorted(): List<String> = presentEssences.sortedWith(
        compareByDescending<String> { usageByEssence[it] ?: 0 }.thenBy { code ->
            allEssences.firstOrNull { it.code == code }?.name ?: code
        }
    )

    fun mergedPersisted(): List<String> {
        if (persistedOrder.isEmpty()) return defaultSorted()
        val filtered = persistedOrder.filter { it in presentEssences }
        val missing = presentEssences.filter { it !in filtered }
        return filtered + missing
    }

    fun displayEssenceOrder(): List<String> {
        return when {
            orderOverride.isNotEmpty() -> orderOverride
            persistedOrder.isNotEmpty() -> mergedPersisted()
            else -> defaultSorted()
        }
    }

    // Dialog ajout essence
    var showAddDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.placette_essences_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        playClickFeedback()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        playClickFeedback()
                        onNavigateToMartelage(parcelleId, placetteId)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Straighten, contentDescription = null)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.Description, contentDescription = stringResource(R.string.martelage))
                        }
                    }
                    IconButton(onClick = {
                        playClickFeedback()
                        reorderMode = !reorderMode; if (reorderMode && orderOverride.isEmpty()) orderOverride = displayEssenceOrder()
                    }) {
                        Icon(Icons.Default.SwapVert, contentDescription = stringResource(R.string.reorder))
                    }

                    IconButton(onClick = {
                        playClickFeedback()
                        showDeletePlacetteDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_placette))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                playClickFeedback()
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            val displayOrder = displayEssenceOrder()

            Crossfade(
                targetState = displayOrder.isEmpty(),
                animationSpec = if (animationsEnabled) {
                    tween(durationMillis = 220, easing = FastOutSlowInEasing)
                } else {
                    tween(durationMillis = 0)
                },
                label = "placetteDetailEssencesCrossfade"
            ) { isEmpty ->
                if (isEmpty) {
                    Text(stringResource(R.string.placette_essences_empty_desc))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayOrder, key = { it }) { code ->
                            val essence = allEssences.firstOrNull { it.code == code }
                            val containerColor: Color? = essenceColor(essence)

                            val baseModifier = if (animationsEnabled) Modifier.animateItemPlacement() else Modifier
                            val itemModifier = if (reorderMode) {
                                baseModifier.pointerInput(orderOverride) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingCode = code
                                            dragAccum = 0f
                                            if (orderOverride.isEmpty()) orderOverride = displayOrder
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAccum += dragAmount.y
                                            var idx = orderOverride.indexOf(code)
                                            while (dragAccum <= -itemStepPx && idx > 0) {
                                                val list = orderOverride.toMutableList()
                                                list.removeAt(idx)
                                                list.add(idx - 1, code)
                                                orderOverride = list
                                                idx -= 1
                                                dragAccum += itemStepPx
                                            }
                                            while (dragAccum >= itemStepPx && idx < orderOverride.lastIndex) {
                                                val list = orderOverride.toMutableList()
                                                list.removeAt(idx)
                                                list.add(idx + 1, code)
                                                orderOverride = list
                                                idx += 1
                                                dragAccum -= itemStepPx
                                            }
                                        },
                                        onDragEnd = {
                                            draggingCode = null
                                            scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                                        },
                                        onDragCancel = {
                                            draggingCode = null
                                            scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                                        }
                                    )
                                }
                            } else {
                                baseModifier
                            }

                            EssenceBlock(
                                code = code,
                                name = essence?.name ?: code,
                                count = usageByEssence[code] ?: 0,
                                reorderMode = reorderMode,
                                animationsEnabled = animationsEnabled,
                                onMoveUp = {
                                    val idx = orderOverride.indexOf(code)
                                    if (idx > 0) {
                                        orderOverride = orderOverride.toMutableList().apply {
                                            add(idx - 1, removeAt(idx))
                                        }
                                        scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                                    }
                                },
                                onMoveDown = {
                                    val idx = orderOverride.indexOf(code)
                                    if (idx >= 0 && idx < orderOverride.lastIndex) {
                                        orderOverride = orderOverride.toMutableList().apply {
                                            add(idx + 1, removeAt(idx))
                                        }
                                        scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                                    }
                                },
                                onClick = {
                                    playClickFeedback()
                                    onNavigateToDiametres(parcelleId, placetteId, code)
                                },
                                onLongPress = {
                                    playClickFeedback()
                                    // En mode réorganisation: garder le comportement suppression protégée.
                                    if (reorderMode) {
                                        val uses = usageByEssence[code] ?: 0
                                        if (uses == 0 && orderOverride.isNotEmpty()) {
                                            orderOverride = orderOverride.filterNot { it == code }
                                            scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                                        } else {
                                            scope.launch { snackbar.showSnackbar(context.getString(R.string.cannot_delete_existing_data)) }
                                        }
                                    } else {
                                        actionTargetEssenceCode = code
                                        showEssenceActionsDialog = true
                                    }
                                },
                                modifier = itemModifier,
                                containerColor = containerColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (showColorDialog && colorTargetEssence != null) {
        val target = colorTargetEssence!!
        AppMiniDialog(
            onDismissRequest = { showColorDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Palette,
            title = stringResource(R.string.color_for_format, target.name),
            description = stringResource(R.string.choose_color_for_essence),
            confirmText = stringResource(R.string.close),
            onConfirm = { showColorDialog = false }
        ) {
            val options = listOf(
                "#4CAF50", "#388E3C", "#1B5E20",
                "#2196F3", "#1976D2", "#0D47A1",
                "#F44336", "#D32F2F", "#B71C1C",
                "#FF9800", "#F57C00", "#E65100",
                "#FFEB3B", "#FBC02D",
                "#26A69A", "#009688",
                "#9C27B0", "#6A1B9A",
                "#795548", "#607D8B"
            )
            val rows = options.chunked(6)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { hex ->
                        val col = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .combinedClickable(
                                    onClick = {
                                        playClickFeedback()
                                        scope.launch {
                                            essenceRepository.updateEssence(target.copy(colorHex = hex))
                                            showColorDialog = false
                                        }
                                    },
                                    onLongClick = {
                                        playClickFeedback()
                                        scope.launch {
                                            essenceRepository.updateEssence(target.copy(colorHex = null))
                                            showColorDialog = false
                                        }
                                    }
                                ),
                            color = col,
                            shape = MaterialTheme.shapes.small
                        ) {}
                    }
                }
            }
            Text(
                stringResource(R.string.color_long_press_tip),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showEssenceActionsDialog && actionTargetEssenceCode != null) {
        val code = actionTargetEssenceCode!!
        val e = allEssences.firstOrNull { it.code == code }
        val name = e?.name ?: code
        AppMiniDialog(
            onDismissRequest = {
                showEssenceActionsDialog = false
                actionTargetEssenceCode = null
            },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Palette,
            title = stringResource(R.string.essence_actions_title_format, name),
            confirmText = stringResource(R.string.close),
            onConfirm = {
                showEssenceActionsDialog = false
                actionTargetEssenceCode = null
            }
        ) {
            FilledTonalButton(
                onClick = {
                    showEssenceActionsDialog = false
                    actionTargetEssenceCode = null
                    if (e != null) {
                        colorTargetEssence = e
                        showColorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.change_color))
            }
            TextButton(
                onClick = {
                    showEssenceActionsDialog = false
                    actionTargetEssenceCode = null
                    deleteTargetEssenceCode = code
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.delete))
            }
        }
    }

    if (deleteTargetEssenceCode != null) {
        val code = deleteTargetEssenceCode!!
        val e = allEssences.firstOrNull { it.code == code }
        val name = e?.name ?: code
        val uses = usageByEssence[code] ?: 0
        AppMiniDialog(
            onDismissRequest = { deleteTargetEssenceCode = null },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Delete,
            title = stringResource(R.string.delete_essence_title),
            description = stringResource(R.string.delete_essence_confirm_format, name, uses),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel),
            confirmIsDestructive = true,
            onConfirm = {
                scope.launch {
                    tigeRepository.deleteTigesByPlacetteAndEssence(placetteId, code)
                    val baseOrder = if (orderOverride.isNotEmpty()) orderOverride else displayEssenceOrder()
                    val newOrder = baseOrder.filterNot { it == code }
                    orderOverride = newOrder
                    userPreferences.setEssenceOrder(placetteId, newOrder)
                    deleteTargetEssenceCode = null
                }
            }
        )
    }

    if (showDeletePlacetteDialog) {
        val uses = tiges.size
        AppMiniDialog(
            onDismissRequest = { showDeletePlacetteDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Delete,
            title = stringResource(R.string.delete_placette_title),
            description = stringResource(R.string.delete_placette_confirm_format, uses),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel),
            confirmIsDestructive = true,
            onConfirm = {
                scope.launch {
                    try {
                        tigeRepository.deleteTigesByPlacette(placetteId)
                        placetteRepository.deletePlacette(placetteId)
                        showDeletePlacetteDialog = false
                        onNavigateBack()
                    } catch (e: Exception) {
                        showDeletePlacetteDialog = false
                        snackbar.showSnackbar(e.message ?: context.getString(R.string.error))
                    }
                }
            }
        )
    }

    if (showAddDialog) {
        AddEssenceDialog(
            allEssences = allEssences,
            presentEssenceCodes = presentEssences.toSet(),
            usageByEssence = usageByEssence,
            query = query,
            onQueryChange = { query = it },
            animationsEnabled = animationsEnabled,
            onDismiss = { showAddDialog = false; query = "" },
            onAdd = { code ->
                // On ajoute l'essence dans l'ordre et on persiste pour qu'elle reste visible.
                if (orderOverride.isEmpty()) {
                    orderOverride = displayEssenceOrder()
                }
                if (code !in orderOverride) {
                    orderOverride = orderOverride + code
                    scope.launch { userPreferences.setEssenceOrder(placetteId, orderOverride) }
                }
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EssenceBlock(
    code: String,
    name: String,
    count: Int,
    reorderMode: Boolean,
    animationsEnabled: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (containerColor != null) CardDefaults.cardColors(containerColor = containerColor) else CardDefaults.cardColors()
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(count.toString(), style = MaterialTheme.typography.titleLarge)
                AnimatedVisibility(
                    visible = reorderMode,
                    enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 160 else 0, easing = FastOutSlowInEasing)) +
                        expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 160 else 0, easing = FastOutSlowInEasing)) +
                        shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing))
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        FilledTonalButton(onClick = onMoveUp, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text(stringResource(R.string.move_up)) }
                        FilledTonalButton(onClick = onMoveDown, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text(stringResource(R.string.move_down)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEssenceDialog(
    allEssences: List<Essence>,
    presentEssenceCodes: Set<String>,
    usageByEssence: Map<String, Int>,
    query: String,
    onQueryChange: (String) -> Unit,
    animationsEnabled: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    AppMiniDialog(
        onDismissRequest = onDismiss,
        animationsEnabled = animationsEnabled,
        icon = Icons.Default.Add,
        title = stringResource(R.string.add_essence_title),
        confirmText = stringResource(R.string.close),
        onConfirm = onDismiss
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.search_tolerant_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        val candidates = remember(allEssences, presentEssenceCodes, usageByEssence, query) {
            val base = allEssences.filter { it.code !in presentEssenceCodes }
            if (query.isBlank()) {
                base.sortedWith(
                    compareByDescending<Essence> { usageByEssence[it.code] ?: 0 }
                        .thenBy { it.name }
                )
            } else {
                val normQuery = normalizeText(query)
                val tokens = normQuery.split(' ')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val filteredBySubstring = if (tokens.isEmpty()) {
                    base
                } else {
                    base.filter { e ->
                        val normText = normalizeText("${e.name} ${e.code}")
                        tokens.all { tok -> normText.contains(tok) }
                    }
                }

                val list = if (filteredBySubstring.isNotEmpty()) filteredBySubstring else base
                list.sortedWith(
                    compareByDescending<Essence> { fuzzyScore("${it.name} ${it.code}", query) }
                        .thenBy { it.name }
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            items(candidates, key = { it.code }) { e ->
                ElevatedCard(onClick = { onAdd(e.code) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(e.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(2.dp))
                        Text(e.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun normalizeText(input: String): String {
    val normalized = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
    return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

// Fuzzy score simple basé sur la distance de Levenshtein (normalisée 0..1)
private fun fuzzyScore(text: String, query: String): Double {
    val t = normalizeText(text)
    val q = normalizeText(query)
    if (q.isEmpty()) return 0.0
    val d = levenshtein(t, q).toDouble()
    return 1.0 - (d / (t.length.coerceAtLeast(q.length).coerceAtLeast(1)))
}

private fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var prev = i - 1
        dp[0] = i
        for (j in 1..b.length) {
            val temp = dp[j]
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[j] = minOf(
                dp[j] + 1,      // deletion
                dp[j - 1] + 1,  // insertion
                prev + cost     // substitution
            )
            prev = temp
        }
    }
    return dp[b.length]
}
