package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.core.content.ContextCompat
import com.forestry.counter.R
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.HeightModeEntry
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.presentation.components.AppMiniDialog
import com.forestry.counter.presentation.components.WoodQualityDialog
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.calculation.quality.WoodQualityGrade
import com.forestry.counter.presentation.utils.rememberHapticFeedback
import com.forestry.counter.presentation.utils.rememberSoundFeedback
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.forestry.counter.domain.location.GpsAverager
import com.forestry.counter.domain.location.GpsQuality
import androidx.compose.material.icons.filled.Star
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun EssenceDiamScreen(
    parcelleId: String,
    placetteId: String,
    essenceCode: String,
    tigeRepository: TigeRepository,
    calculator: ForestryCalculator?,
    userPreferences: UserPreferencesManager,
    essenceRepository: EssenceRepository? = null,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val appContext = LocalContext.current
    val finePermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val hapticEnabled by userPreferences.hapticEnabled.collectAsState(initial = true)
    val soundEnabled by userPreferences.soundEnabled.collectAsState(initial = true)
    val hapticIntensity by userPreferences.hapticIntensity.collectAsState(initial = 2)
    val animationsEnabled by userPreferences.animationsEnabled.collectAsState(initial = true)
    val haptic = rememberHapticFeedback()
    val sound = rememberSoundFeedback()

    fun playClickFeedback() {
        if (hapticEnabled) {
            haptic.performWithIntensity(hapticIntensity)
        }
        if (soundEnabled) {
            sound.click()
        }
    }

    fun attemptAutoGpsForTige(tigeId: String) {
        scope.launch {
            val hasPermission = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                if (!finePermission.status.isGranted) finePermission.launchPermissionRequest()
                snackbar.showSnackbar(appContext.getString(R.string.gps_permission_required))
                return@launch
            }

            try {
                // Moyennage multi-lectures pour une meilleure précision
                val averaged = GpsAverager.collectAndAverage(
                    context = appContext,
                    targetReadings = 5,
                    maxAccuracyM = 25f,
                    timeoutMs = 12_000L
                )

                if (averaged == null) {
                    snackbar.showSnackbar(appContext.getString(R.string.location_unavailable))
                    return@launch
                }

                val ok = tigeRepository.setTigeGps(
                    tigeId = tigeId,
                    wkt = averaged.wkt,
                    precisionM = averaged.precisionM,
                    altitudeM = averaged.altitude
                )
                if (ok) {
                    val qualityMsg = when (averaged.qualityLabel) {
                        GpsQuality.EXCELLENT -> appContext.getString(R.string.gps_precision_excellent, averaged.precisionM)
                        GpsQuality.GOOD -> appContext.getString(R.string.gps_precision_good, averaged.precisionM)
                        GpsQuality.MODERATE -> appContext.getString(R.string.gps_precision_moderate, averaged.precisionM)
                        GpsQuality.POOR -> appContext.getString(R.string.gps_precision_poor, averaged.precisionM)
                    }
                    snackbar.showSnackbar("${appContext.getString(R.string.gps_averaging_done)} · $qualityMsg")
                } else {
                    snackbar.showSnackbar(appContext.getString(R.string.gps_save_failed))
                }
            } catch (e: SecurityException) {
                snackbar.showSnackbar(appContext.getString(R.string.gps_permission_required))
            } catch (e: Exception) {
                snackbar.showSnackbar(appContext.getString(R.string.gps_error_format, e.message ?: ""))
            }
        }
    }

    val normalizedEssenceCode = remember(essenceCode) { essenceCode.trim().uppercase(Locale.getDefault()) }

    val specialCategories = remember {
        setOf("DEPERISSANT", "ARBRE_BIO", "MORT", "PARASITE")
    }

    // Charger les tiges de la placette
    val tiges by tigeRepository.getTigesByPlacette(placetteId).collectAsState(initial = emptyList())

    // Tiges "normales" uniquement : les arbres spéciaux doivent rester dans l'onglet "feuille"
    val tigesEssence = remember(tiges, essenceCode) {
        tiges.filter { t ->
            t.essenceCode.equals(essenceCode, true) &&
                (t.categorie?.uppercase(Locale.getDefault()) !in specialCategories)
        }
    }

    val counts = remember(tigesEssence) {
        tigesEssence
            .groupBy { it.diamCm.toInt() }
            .mapValues { it.value.size }
    }

    val scopeKey = remember(placetteId) { "PLACETTE_${placetteId}" }
    val martelageHeights by userPreferences.martelageHeightsFlow(scopeKey).collectAsState(initial = emptyMap())

    val tigesByDiamClass = remember(tigesEssence) {
        tigesEssence.groupBy { it.diamCm.toInt() }
    }

    val specialTiges = remember(tiges) {
        tiges.filter { t ->
            t.essenceCode.equals(essenceCode, true) &&
                (t.categorie?.uppercase(Locale.getDefault()) in specialCategories)
        }
    }

    // Qualité bois — optionnel
    var showQualityDialog by remember { mutableStateOf(false) }
    var qualityTargetTigeId by remember { mutableStateOf<String?>(null) }
    var qualityTargetDiam by remember { mutableStateOf(30.0) }
    val essenceCategorie by produceState<String?>(initialValue = null, key1 = essenceRepository, key2 = essenceCode) {
        value = try {
            essenceRepository?.getEssenceByCode(essenceCode)?.first()?.categorie
        } catch (_: Throwable) { null }
    }

    var showSpecialList by remember { mutableStateOf(false) }

    var showSpecialDialog by remember { mutableStateOf(false) }
    var specialType by remember { mutableStateOf<SpecialTreeType?>(null) }
    var specialDiameterInput by remember { mutableStateOf("") }
    var specialHeightInput by remember { mutableStateOf("") }
    var specialParasiteInput by remember { mutableStateOf("") }
    var specialNoteInput by remember { mutableStateOf("") }

    fun openSpecialDialog(type: SpecialTreeType) {
        specialType = type
        specialDiameterInput = ""
        specialHeightInput = ""
        specialParasiteInput = ""
        specialNoteInput = ""
        showSpecialDialog = true
    }

    // Classes de base (paramètres) + extensions locales via un petit bouton +
    val baseClasses by produceState(initialValue = (5..120 step 5).toList(), key1 = calculator) {
        value = try {
            val list = calculator?.diameterClasses()
            if (!list.isNullOrEmpty()) list else (5..120 step 5).toList()
        } catch (_: Throwable) {
            (5..120 step 5).toList()
        }
    }
    var extraClasses by remember { mutableStateOf<List<Int>>(emptyList()) }
    val classes = remember(baseClasses, extraClasses, counts) {
        // Toujours inclure les classes réellement utilisées dans les tiges
        (baseClasses + extraClasses + counts.keys).distinct().sorted()
    }

    val orderedClasses = remember(classes) { classes }

    val heightModes by produceState<List<HeightModeEntry>>(
        initialValue = emptyList(),
        key1 = calculator,
        key2 = classes,
        key3 = tigesEssence.size
    ) {
        value = try {
            if (calculator == null) emptyList() else calculator.loadSynthesisParams().heightModes
        } catch (_: Throwable) {
            emptyList()
        }
    }

    val missingHeightClasses = remember(tigesEssence, orderedClasses, martelageHeights) {
        val manual = martelageHeights[normalizedEssenceCode].orEmpty()
        orderedClasses.filter { d ->
            val list = tigesByDiamClass[d].orEmpty()
            list.isNotEmpty() && list.any { it.hauteurM == null } && manual[d] == null
        }
    }

    var showMissingHeightsDialog by remember { mutableStateOf(false) }
    var skipMissingHeightsPrompt by rememberSaveable { mutableStateOf(false) }

    val safeNavigateBack = {
        if (!skipMissingHeightsPrompt && missingHeightClasses.isNotEmpty() && calculator != null) {
            showMissingHeightsDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        safeNavigateBack()
    }

    // Hauteur globales / par classe de diamètre pour cette essence
    var showHeightDialog by remember { mutableStateOf(false) }
    val heightByClassInput = remember { mutableStateMapOf<Int, String>() }

    fun parseHeightInputMean(raw: String): Pair<Double?, Int> {
        val s = raw.trim()
        if (s.isBlank()) return null to 0
        val compact = s.replace(" ", "")
        val commaCount = compact.count { it == ',' }
        if (commaCount >= 2) {
            val values = compact.split(',')
                .mapNotNull { it.toDoubleOrNull() }
                .filter { it > 0.0 }
            if (values.isEmpty()) return null to 0
            return values.average() to values.size
        }
        if (compact.contains(';')) {
            val values = compact.split(';')
                .mapNotNull { it.replace(',', '.').toDoubleOrNull() }
                .filter { it > 0.0 }
            if (values.isEmpty()) return null to 0
            return values.average() to values.size
        }
        val v = compact.replace(',', '.').toDoubleOrNull()
        return if (v != null && v > 0.0) v to 1 else null to 0
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.essence_diameters_title_format, essenceCode)) },
                navigationIcon = {
                    IconButton(onClick = safeNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSpecialList = !showSpecialList
                        playClickFeedback()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = stringResource(if (showSpecialList) R.string.hide_special_trees else R.string.show_special_trees)
                        )
                    }
                    // Bouton qualité bois (optionnel) — évalue la dernière tige pointée
                    IconButton(onClick = {
                        playClickFeedback()
                        val lastTige = tigesEssence.maxByOrNull { it.timestamp }
                        if (lastTige != null) {
                            qualityTargetTigeId = lastTige.id
                            qualityTargetDiam = lastTige.diamCm
                            showQualityDialog = true
                        } else {
                            scope.launch {
                                snackbar.showSnackbar(appContext.getString(R.string.no_tige_to_remove_for_diameter_format, 0))
                            }
                        }
                    }) {
                        Icon(Icons.Default.Star, contentDescription = stringResource(R.string.quality_assess))
                    }
                    IconButton(onClick = {
                        playClickFeedback()
                        val currentMax = classes.maxOrNull() ?: 0
                        val next = if (currentMax < 5) 5 else currentMax + 5
                        if (next !in classes) {
                            extraClasses = extraClasses + next
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_diameter_class))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpecialTreeButton(label = stringResource(R.string.special_tree_dying)) {
                    playClickFeedback()
                    openSpecialDialog(SpecialTreeType.DEPERISSANT)
                }
                SpecialTreeButton(label = stringResource(R.string.special_tree_bio)) {
                    playClickFeedback()
                    openSpecialDialog(SpecialTreeType.ARBRE_BIO)
                }
                SpecialTreeButton(label = stringResource(R.string.special_tree_dead)) {
                    playClickFeedback()
                    openSpecialDialog(SpecialTreeType.MORT)
                }
                SpecialTreeButton(label = stringResource(R.string.special_tree_parasite)) {
                    playClickFeedback()
                    openSpecialDialog(SpecialTreeType.PARASITE)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton Hauteur (synchronisé aux classes de diamètre)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        playClickFeedback()
                        val fixed = heightModes
                            .filter {
                                it.essence.equals(essenceCode, true) &&
                                    it.mode.uppercase(Locale.getDefault()) == "FIXED" &&
                                    it.fixed != null &&
                                    it.fixed > 0.0
                            }
                            .associate { it.diamClass to (it.fixed ?: 0.0) }
                        val manual = martelageHeights[normalizedEssenceCode].orEmpty()

                        // Pré-initialiser uniquement les classes affichées, en priorité depuis martelageHeights
                        heightByClassInput.clear()
                        orderedClasses.forEach { d ->
                            val v = manual[d] ?: fixed[d]
                            heightByClassInput[d] = v?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: ""
                        }
                        showHeightDialog = true
                    },
                    enabled = calculator != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Height,
                        contentDescription = stringResource(R.string.configure_heights)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.height))
                }
            }

            if (showSpecialList) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(specialTiges, key = { it.id }) { t ->
                        SpecialTreeCard(
                            tige = t,
                            onLongPress = {
                                playClickFeedback()
                                scope.launch {
                                    tigeRepository.deleteTige(t.id)
                                    snackbar.showSnackbar(appContext.getString(R.string.tree_deleted))
                                }
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(orderedClasses, key = { it }) { d ->
                        val count = counts[d] ?: 0
                        DiameterCard(
                            diameter = d,
                            count = count,
                            onTap = {
                                playClickFeedback()
                                scope.launch {
                                    val id = UUID.randomUUID().toString()
                                    val ok = runCatching {
                                        tigeRepository.insertTige(
                                            com.forestry.counter.domain.model.Tige(
                                                id = id,
                                                parcelleId = parcelleId,
                                                placetteId = placetteId,
                                                essenceCode = essenceCode,
                                                diamCm = d.toDouble(),
                                                hauteurM = null,
                                                gpsWkt = null,
                                                precisionM = null,
                                                altitudeM = null,
                                                note = null,
                                                produit = null,
                                                fCoef = null,
                                                valueEur = null
                                            )
                                        )
                                    }.isSuccess
                                    if (ok) {
                                        attemptAutoGpsForTige(id)
                                        val res = snackbar.showSnackbar(
                                            message = appContext.getString(R.string.tige_added),
                                            actionLabel = appContext.getString(R.string.undo)
                                        )
                                        if (res == SnackbarResult.ActionPerformed) {
                                            val undone = tigeRepository.deleteLatest(
                                                parcelleId = parcelleId,
                                                placetteId = placetteId,
                                                essenceCode = essenceCode,
                                                diamCm = d.toDouble()
                                            )
                                            if (undone) {
                                                snackbar.showSnackbar(appContext.getString(R.string.add_undone))
                                            }
                                        }
                                    } else {
                                        snackbar.showSnackbar(appContext.getString(R.string.insert_failed))
                                    }
                                }
                            },
                            onLongPress = {
                                playClickFeedback()
                                scope.launch {
                                    val ok = tigeRepository.deleteLatest(
                                        parcelleId = parcelleId,
                                        placetteId = placetteId,
                                        essenceCode = essenceCode,
                                        diamCm = d.toDouble()
                                    )
                                    if (!ok) snackbar.showSnackbar(appContext.getString(R.string.no_tige_to_remove_for_diameter_format, d))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogue de configuration des hauteurs
    if (showHeightDialog && calculator != null) {
        AppMiniDialog(
            onDismissRequest = { showHeightDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Height,
            title = stringResource(R.string.height_dialog_title_format, essenceCode),
            description = stringResource(R.string.martelage_height_by_class_hint),
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                scope.launch {

                    val cleaned: Map<Int, Double> = orderedClasses.mapNotNull { d ->
                        val (mean, _) = parseHeightInputMean(heightByClassInput[d] ?: "")
                        if (mean != null && mean > 0.0) d to mean else null
                    }.toMap()

                    if (cleaned.isEmpty()) {
                        snackbar.showSnackbar(appContext.getString(R.string.no_valid_height_entered))
                        return@launch
                    }

                    val newMartelageHeights = martelageHeights.toMutableMap().apply {
                        put(normalizedEssenceCode, cleaned)
                    }
                    userPreferences.setMartelageHeights(scopeKey, newMartelageHeights)

                    // Maintenir l'ancien stockage "HEIGHT_MODES" (global) en cohérence
                    orderedClasses.forEach { d ->
                        val v = cleaned[d]
                        if (v != null && v > 0.0) {
                            calculator.setHeightMode(
                                HeightModeEntry(
                                    essence = essenceCode,
                                    diamClass = d,
                                    mode = "FIXED",
                                    fixed = v
                                )
                            )
                        } else {
                            calculator.setHeightMode(
                                HeightModeEntry(
                                    essence = essenceCode,
                                    diamClass = d,
                                    mode = "SAMPLES",
                                    fixed = null
                                )
                            )
                        }
                    }

                    showHeightDialog = false
                    snackbar.showSnackbar(appContext.getString(R.string.heights_saved))
                }
            }
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                columnItems(orderedClasses) { d ->
                    val count = counts[d] ?: 0
                    val missingCount = tigesByDiamClass[d]?.count { it.hauteurM == null } ?: 0
                    val (meanVal, meanCount) = parseHeightInputMean(heightByClassInput[d] ?: "")
                    val hasValue = meanVal != null && meanVal > 0.0
                    val needsValue = count > 0 && missingCount > 0 && !hasValue
                    val enabled = count > 0 || (heightByClassInput[d]?.isNotBlank() == true)

                    val cardTargetColor = if (needsValue) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                    val cardColor by animateColorAsState(
                        targetValue = cardTargetColor,
                        animationSpec = tween(
                            durationMillis = if (animationsEnabled) 220 else 0,
                            easing = FastOutSlowInEasing
                        ),
                        label = "heightClassCardColor"
                    )
                    val cardElevation by animateDpAsState(
                        targetValue = if (needsValue) 4.dp else 1.dp,
                        animationSpec = tween(
                            durationMillis = if (animationsEnabled) 220 else 0,
                            easing = FastOutSlowInEasing
                        ),
                        label = "heightClassCardElevation"
                    )

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = cardColor
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = cardElevation)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.diameter_cm_value_format, d),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                val badgeVisibleState = remember(needsValue) { MutableTransitionState(needsValue).apply { targetState = needsValue } }
                                AnimatedVisibility(
                                    visibleState = badgeVisibleState,
                                    enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 160 else 0, easing = FastOutSlowInEasing)) +
                                        expandHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 140 else 0, easing = FastOutSlowInEasing)) +
                                        shrinkHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing))
                                ) {
                                    ToCompleteBadge()
                                }
                            }
                            OutlinedTextField(
                                value = heightByClassInput[d] ?: "",
                                onValueChange = { heightByClassInput[d] = it },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = enabled,
                                isError = needsValue,
                                label = { Text(stringResource(R.string.height)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                placeholder = { Text("ex : 18,5") },
                                suffix = { Text("m") },
                                supportingText = {
                                    if (count > 0) {
                                        val label = if (missingCount > 0) {
                                            "N=${count} · manquantes=${missingCount}"
                                        } else {
                                            "N=${count}"
                                        }
                                        val suffix = if (meanCount > 1 && meanVal != null) {
                                            " · moy=${String.format(Locale.getDefault(), "%.1f", meanVal)}"
                                        } else {
                                            ""
                                        }
                                        Text(label + suffix)
                                    }
                                },
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMissingHeightsDialog) {
        val list = missingHeightClasses
        AppMiniDialog(
            onDismissRequest = { showMissingHeightsDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Warning,
            title = stringResource(R.string.mandatory_heights_title),
            description = stringResource(R.string.mandatory_heights_desc),
            confirmText = stringResource(R.string.configure_heights),
            dismissText = stringResource(R.string.cancel),
            onDismiss = {
                showMissingHeightsDialog = false
                skipMissingHeightsPrompt = true
                onNavigateBack()
            },
            onConfirm = {
                showMissingHeightsDialog = false
                // Pré-initialiser les champs par classe si besoin
                orderedClasses.forEach { d ->
                    if (heightByClassInput[d] == null) heightByClassInput[d] = ""
                }
                showHeightDialog = true
            }
        ) {
            if (list.isNotEmpty()) {
                Text(
                    text = list.joinToString(", ") { d -> appContext.getString(R.string.diameter_cm_value_format, d) },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showSpecialDialog && specialType != null) {
        val type = specialType!!
        val titleRes = when (type) {
            SpecialTreeType.DEPERISSANT -> R.string.add_dying_tree_title
            SpecialTreeType.ARBRE_BIO -> R.string.add_bio_tree_title
            SpecialTreeType.MORT -> R.string.add_dead_tree_title
            SpecialTreeType.PARASITE -> R.string.add_parasitized_tree_title
        }
        AppMiniDialog(
            onDismissRequest = { showSpecialDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Eco,
            title = stringResource(titleRes),
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                val diam = specialDiameterInput.replace(',', '.').toDoubleOrNull()
                val h = specialHeightInput.replace(',', '.').toDoubleOrNull()
                if (diam == null || diam <= 0.0) {
                    scope.launch { snackbar.showSnackbar(appContext.getString(R.string.invalid_diameter)) }
                    return@AppMiniDialog
                }
                val id = UUID.randomUUID().toString()
                val categorie = when (type) {
                    SpecialTreeType.DEPERISSANT -> "DEPERISSANT"
                    SpecialTreeType.ARBRE_BIO -> "ARBRE_BIO"
                    SpecialTreeType.MORT -> "MORT"
                    SpecialTreeType.PARASITE -> "PARASITE"
                }
                val defects = if (type == SpecialTreeType.PARASITE && specialParasiteInput.isNotBlank()) {
                    listOf(specialParasiteInput.trim())
                } else {
                    null
                }
                showSpecialDialog = false
                scope.launch {
                    val ok = runCatching {
                        tigeRepository.insertTige(
                            com.forestry.counter.domain.model.Tige(
                                id = id,
                                parcelleId = parcelleId,
                                placetteId = placetteId,
                                essenceCode = essenceCode,
                                diamCm = diam,
                                hauteurM = h,
                                gpsWkt = null,
                                precisionM = null,
                                altitudeM = null,
                                note = specialNoteInput.ifBlank { null },
                                produit = null,
                                fCoef = null,
                                valueEur = null,
                                numero = null,
                                categorie = categorie,
                                qualite = null,
                                defauts = defects,
                                photoUri = null
                            )
                        )
                    }.isSuccess
                    if (ok) {
                        playClickFeedback()
                        attemptAutoGpsForTige(id)
                        snackbar.showSnackbar(appContext.getString(R.string.tree_added))
                    } else {
                        snackbar.showSnackbar(appContext.getString(R.string.insert_failed))
                    }
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = specialDiameterInput,
                    onValueChange = { specialDiameterInput = it },
                    label = { Text(stringResource(R.string.diameter_cm_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialHeightInput,
                    onValueChange = { specialHeightInput = it },
                    label = { Text(stringResource(R.string.height_m_optional_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (type == SpecialTreeType.PARASITE) {
                    OutlinedTextField(
                        value = specialParasiteInput,
                        onValueChange = { specialParasiteInput = it },
                        label = { Text(stringResource(R.string.special_tree_parasite)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = specialNoteInput,
                    onValueChange = { specialNoteInput = it },
                    label = { Text(stringResource(R.string.note_optional_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Dialogue d'évaluation qualité bois (optionnel)
    if (showQualityDialog && qualityTargetTigeId != null) {
        val targetTige = tigesEssence.firstOrNull { it.id == qualityTargetTigeId }
        WoodQualityDialog(
            essenceCode = essenceCode,
            categorie = essenceCategorie,
            diamCm = qualityTargetDiam,
            hauteurM = targetTige?.hauteurM,
            initialDetail = targetTige?.qualiteDetail,
            animationsEnabled = animationsEnabled,
            onDismiss = { showQualityDialog = false },
            onConfirm = { grade, classification, detailJson ->
                showQualityDialog = false
                val tigeId = qualityTargetTigeId ?: return@WoodQualityDialog
                scope.launch {
                    try {
                        tigeRepository.updateTigeQuality(
                            tigeId = tigeId,
                            qualite = grade.ordinal,
                            produit = classification.primary.code,
                            qualiteDetail = detailJson
                        )
                        snackbar.showSnackbar(
                            appContext.getString(
                                R.string.quality_saved,
                                grade.shortLabel,
                                classification.primary.label
                            )
                        )
                    } catch (e: Exception) {
                        snackbar.showSnackbar(appContext.getString(R.string.insert_failed))
                    }
                }
            }
        )
    }
}

@Composable
private fun ToCompleteBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = stringResource(R.string.badge_to_complete),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiameterCard(
    diameter: Int,
    count: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.diameter_cm_value_format, diameter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.diameter_card_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class SpecialTreeType {
    DEPERISSANT,
    ARBRE_BIO,
    MORT,
    PARASITE
}

@Composable
private fun SpecialTreeButton(
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpecialTreeCard(
    tige: com.forestry.counter.domain.model.Tige,
    onLongPress: () -> Unit
) {
    val catLabel = when (tige.categorie?.uppercase(Locale.getDefault())) {
        "DEPERISSANT" -> stringResource(R.string.special_tree_dying)
        "ARBRE_BIO" -> stringResource(R.string.special_tree_bio)
        "MORT" -> stringResource(R.string.special_tree_dead)
        "PARASITE" -> stringResource(R.string.special_tree_parasite)
        else -> stringResource(R.string.special_tree_special)
    }
    val parasite = tige.defauts?.joinToString(", ") ?: ""
    val diameterLabel = stringResource(R.string.diameter_cm_value_format, tige.diamCm.toInt())
    val heightLabel = tige.hauteurM?.let { stringResource(R.string.height_m_value_format, it) }
    val subtitle = buildString {
        append(diameterLabel)
        heightLabel?.let { append(" · $it") }
        if (parasite.isNotBlank()) {
            append(" · $parasite")
        }
    }

    ElevatedCard(
        modifier = Modifier
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(catLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
