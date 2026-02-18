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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.core.content.ContextCompat
import com.forestry.counter.R
import com.forestry.counter.presentation.components.AppMiniDialog
import com.forestry.counter.presentation.components.WoodQualityDialog
import com.forestry.counter.presentation.components.TipCard
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.HeightModeEntry
import com.forestry.counter.domain.calculation.tarifs.TarifMethod
import com.forestry.counter.domain.calculation.tarifs.TarifCalculator
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.data.preferences.GpsCaptureMode
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.calculation.quality.WoodQualityGrade
import com.forestry.counter.presentation.utils.parseHeightInputMean
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
    val gpsCaptureMode by userPreferences.gpsCaptureMode.collectAsState(initial = GpsCaptureMode.STANDARD)
    val gpsMaxAcceptablePrecisionM by userPreferences.gpsMaxAcceptablePrecisionM.collectAsState(initial = 15f)
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
                val profile = when (gpsCaptureMode) {
                    GpsCaptureMode.FAST -> Triple(3, 35f, 7_000L)
                    GpsCaptureMode.STANDARD -> Triple(5, 25f, 12_000L)
                    GpsCaptureMode.PRECISE -> Triple(8, 12f, 18_000L)
                }

                // Moyennage multi-lectures pour une meilleure précision
                val averaged = GpsAverager.collectAndAverage(
                    context = appContext,
                    targetReadings = profile.first,
                    maxAccuracyM = profile.second,
                    timeoutMs = profile.third
                )

                if (averaged == null) {
                    snackbar.showSnackbar(appContext.getString(R.string.location_unavailable))
                    return@launch
                }

                if (averaged.precisionM > gpsMaxAcceptablePrecisionM.toDouble()) {
                    snackbar.showSnackbar(
                        appContext.getString(
                            R.string.gps_precision_rejected_format,
                            averaged.precisionM,
                            gpsMaxAcceptablePrecisionM
                        )
                    )
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

    // ── Surveillance GPS périodique (toutes les 10 min) ──
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return@LaunchedEffect

        while (true) {
            kotlinx.coroutines.delay(10 * 60 * 1000L) // 10 minutes
            try {
                val quick = GpsAverager.collectAndAverage(
                    context = appContext,
                    targetReadings = 2,
                    maxAccuracyM = 100f,
                    timeoutMs = 5_000L
                )
                if (quick == null) {
                    snackbar.showSnackbar(
                        appContext.getString(R.string.gps_periodic_unavailable),
                        duration = SnackbarDuration.Long
                    )
                } else if (quick.precisionM > gpsMaxAcceptablePrecisionM.toDouble()) {
                    snackbar.showSnackbar(
                        appContext.getString(R.string.gps_periodic_poor, quick.precisionM, gpsMaxAcceptablePrecisionM),
                        duration = SnackbarDuration.Long
                    )
                }
            } catch (_: Throwable) { /* permission lost or other error — silently skip */ }
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

    // Nom d'essence résolu pour les dialogues
    val essenceName by produceState<String>(initialValue = essenceCode, key1 = essenceRepository, key2 = essenceCode) {
        value = try {
            essenceRepository?.getEssenceByCode(essenceCode)?.first()?.name ?: essenceCode
        } catch (_: Throwable) { essenceCode }
    }

    // Tarif actif — déterminer si les hauteurs sont requises
    val activeTarifMethod by produceState<TarifMethod?>(initialValue = null, key1 = calculator) {
        value = try {
            val params = calculator?.loadSynthesisParams()
            params?.tarifSelection?.method?.let { TarifMethod.fromCode(it) }
        } catch (_: Throwable) { null }
    }
    val tarifRequiresHeight = remember(activeTarifMethod) {
        activeTarifMethod?.let { TarifCalculator.requiresHeight(it) } ?: true
    }

    val missingHeightClasses = remember(tigesEssence, orderedClasses, martelageHeights, tarifRequiresHeight) {
        if (!tarifRequiresHeight) emptyList()
        else {
            val manual = martelageHeights[normalizedEssenceCode].orEmpty()
            orderedClasses.filter { d ->
                val list = tigesByDiamClass[d].orEmpty()
                list.isNotEmpty() && list.any { it.hauteurM == null } && manual[d] == null
            }
        }
    }

    // Classes avec des tiges (pour le dialogue filtré)
    val populatedClasses = remember(orderedClasses, tigesByDiamClass) {
        orderedClasses.filter { (tigesByDiamClass[it]?.size ?: 0) > 0 }
    }

    var showMissingHeightsDialog by remember { mutableStateOf(false) }
    var showSnoozeHeightsDialog by remember { mutableStateOf(false) }
    var snoozeHours by rememberSaveable { mutableStateOf(1) }
    var skipMissingHeightsPrompt by rememberSaveable { mutableStateOf(false) }
    val heightPromptSnoozeUntilMs by userPreferences.heightPromptSnoozeUntilMs.collectAsState(initial = 0L)
    val isHeightPromptSnoozed = heightPromptSnoozeUntilMs > System.currentTimeMillis()

    val safeNavigateBack = {
        if (!skipMissingHeightsPrompt && !isHeightPromptSnoozed && missingHeightClasses.isNotEmpty() && calculator != null) {
            showMissingHeightsDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        safeNavigateBack()
    }

    // Compteurs de hauteurs au niveau composable (utilisés dans le Scaffold et les dialogues)
    val heightTotalPopulated = populatedClasses.size

    // Hauteur globales / par classe de diamètre pour cette essence
    var showHeightDialog by remember { mutableStateOf(false) }
    val heightByClassInput = remember { mutableStateMapOf<Int, String>() }

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

            // Bouton Hauteur amélioré avec badge de statut
            val heightSetCount = remember(populatedClasses, martelageHeights) {
                val manual = martelageHeights[normalizedEssenceCode].orEmpty()
                populatedClasses.count { d ->
                    val tiges = tigesByDiamClass[d].orEmpty()
                    manual[d] != null || tiges.all { it.hauteurM != null }
                }
            }
            val heightTotalPopulated = populatedClasses.size
            val heightMissingCount = heightTotalPopulated - heightSetCount
            val heightButtonColor = when {
                !tarifRequiresHeight -> MaterialTheme.colorScheme.surfaceVariant
                heightMissingCount == 0 && heightTotalPopulated > 0 -> MaterialTheme.colorScheme.primaryContainer
                heightMissingCount > 0 -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info tarif
                if (activeTarifMethod != null) {
                    Text(
                        text = if (tarifRequiresHeight) stringResource(R.string.height_tarif_info_2e) else stringResource(R.string.height_tarif_info_1e),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tarifRequiresHeight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
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

                        heightByClassInput.clear()
                        populatedClasses.forEach { d ->
                            val v = manual[d] ?: fixed[d]
                            heightByClassInput[d] = v?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: ""
                        }
                        showHeightDialog = true
                    },
                    enabled = calculator != null,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = heightButtonColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Height,
                        contentDescription = stringResource(R.string.configure_heights),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.height))
                    if (heightTotalPopulated > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$heightSetCount/$heightTotalPopulated",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (heightMissingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TipCard(
                tipKey = "essences_tip",
                title = stringResource(R.string.tip_essences_title),
                message = stringResource(R.string.tip_essences_msg)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        val hStatus = if (count > 0 && tarifRequiresHeight) {
                            val tiges = tigesByDiamClass[d].orEmpty()
                            val manual = martelageHeights[normalizedEssenceCode]?.get(d)
                            when {
                                tiges.all { it.hauteurM != null } -> HeightStatus.MEASURED
                                manual != null -> HeightStatus.SET
                                else -> HeightStatus.MISSING
                            }
                        } else HeightStatus.NONE
                        DiameterCard(
                            diameter = d,
                            count = count,
                            heightStatus = hStatus,
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

    // Dialogue de configuration des hauteurs (amélioré)
    var quickFillInput by remember { mutableStateOf("") }

    if (showHeightDialog && calculator != null) {
        AppMiniDialog(
            onDismissRequest = { showHeightDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Height,
            title = stringResource(R.string.height_dialog_essence_title_format, essenceName),
            description = stringResource(R.string.martelage_height_by_class_hint),
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                scope.launch {
                    val cleaned: Map<Int, Double> = populatedClasses.mapNotNull { d ->
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

                    // Propager vers le scope parcelle
                    val parcelleScopeKey = "PARCELLE_${parcelleId}"
                    if (parcelleScopeKey != scopeKey) {
                        val parcelleHeights = userPreferences.martelageHeightsFlow(parcelleScopeKey).first()
                        val updatedParcelleHeights = parcelleHeights.toMutableMap().apply {
                            put(normalizedEssenceCode, cleaned)
                        }
                        userPreferences.setMartelageHeights(parcelleScopeKey, updatedParcelleHeights)
                    }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bannière info tarif
                val tarifLabel = activeTarifMethod?.label ?: ""
                Surface(
                    color = if (tarifRequiresHeight) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (tarifRequiresHeight) stringResource(R.string.height_tarif_info_2e) + " ($tarifLabel)"
                            else stringResource(R.string.height_tarif_info_1e) + " ($tarifLabel)",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Quick-fill : appliquer une même hauteur à toutes les classes vides
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quickFillInput,
                        onValueChange = { quickFillInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.height_quick_fill_title)) },
                        placeholder = { Text("ex : 22") },
                        suffix = { Text("m") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                    FilledTonalButton(
                        onClick = {
                            val v = quickFillInput.replace(',', '.').toDoubleOrNull()
                            if (v != null && v > 0.0) {
                                val formatted = String.format(Locale.getDefault(), "%.1f", v)
                                populatedClasses.forEach { d ->
                                    if (heightByClassInput[d].isNullOrBlank()) {
                                        heightByClassInput[d] = formatted
                                    }
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.height_quick_fill_apply), style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider()

                // Liste des classes avec des tiges uniquement
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    columnItems(populatedClasses, key = { it }) { d ->
                        val count = counts[d] ?: 0
                        val tiges = tigesByDiamClass[d].orEmpty()
                        val measuredCount = tiges.count { it.hauteurM != null }
                        val missingCount = tiges.count { it.hauteurM == null }
                        val (meanVal, meanCount) = parseHeightInputMean(heightByClassInput[d] ?: "")
                        val hasValue = meanVal != null && meanVal > 0.0
                        val needsValue = missingCount > 0 && !hasValue

                        val cardTargetColor = when {
                            needsValue && tarifRequiresHeight -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                            hasValue -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                            targetValue = if (needsValue && tarifRequiresHeight) 4.dp else 1.dp,
                            animationSpec = tween(
                                durationMillis = if (animationsEnabled) 220 else 0,
                                easing = FastOutSlowInEasing
                            ),
                            label = "heightClassCardElevation"
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = cardElevation)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                    Text(
                                        text = stringResource(R.string.height_n_stems_format, count),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (needsValue && tarifRequiresHeight) {
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
                                }
                                OutlinedTextField(
                                    value = heightByClassInput[d] ?: "",
                                    onValueChange = { heightByClassInput[d] = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = needsValue && tarifRequiresHeight,
                                    label = { Text(stringResource(R.string.height)) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    placeholder = { Text("ex : 18,5") },
                                    suffix = { Text("m") },
                                    supportingText = {
                                        val parts = mutableListOf<String>()
                                        if (missingCount > 0) parts += stringResource(R.string.height_n_missing_format, missingCount)
                                        if (measuredCount > 0) parts += "$measuredCount ${stringResource(R.string.height_status_measured).lowercase()}"
                                        if (meanCount > 1 && meanVal != null) {
                                            parts += stringResource(R.string.height_mean_computed_format, meanVal, meanCount)
                                        }
                                        if (parts.isNotEmpty()) Text(parts.joinToString(" · "))
                                    },
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMissingHeightsDialog) {
        val list = missingHeightClasses
        val tarifLabel = activeTarifMethod?.label ?: ""
        val descText = if (tarifRequiresHeight && tarifLabel.isNotBlank()) {
            stringResource(R.string.mandatory_heights_desc_tarif, tarifLabel)
        } else {
            stringResource(R.string.mandatory_heights_desc)
        }
        AppMiniDialog(
            onDismissRequest = { showMissingHeightsDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Warning,
            title = stringResource(R.string.mandatory_heights_title) + " — $essenceName",
            description = descText,
            confirmText = stringResource(R.string.configure_heights),
            dismissText = stringResource(R.string.mandatory_heights_skip),
            neutralText = stringResource(R.string.mandatory_heights_snooze_title),
            onDismiss = {
                showMissingHeightsDialog = false
                skipMissingHeightsPrompt = true
                onNavigateBack()
            },
            onNeutral = {
                showMissingHeightsDialog = false
                snoozeHours = 1
                showSnoozeHeightsDialog = true
            },
            onConfirm = {
                showMissingHeightsDialog = false
                val fixed = heightModes
                    .filter {
                        it.essence.equals(essenceCode, true) &&
                            it.mode.uppercase(Locale.getDefault()) == "FIXED" &&
                            it.fixed != null && it.fixed > 0.0
                    }
                    .associate { it.diamClass to (it.fixed ?: 0.0) }
                val manual = martelageHeights[normalizedEssenceCode].orEmpty()
                heightByClassInput.clear()
                populatedClasses.forEach { d ->
                    val v = manual[d] ?: fixed[d]
                    heightByClassInput[d] = v?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: ""
                }
                showHeightDialog = true
            }
        ) {
            if (list.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.mandatory_heights_classes_format,
                                list.joinToString(", ") { d -> "$d cm" }),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${list.size} / $heightTotalPopulated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    if (showSnoozeHeightsDialog) {
        AppMiniDialog(
            onDismissRequest = { showSnoozeHeightsDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Warning,
            title = stringResource(R.string.mandatory_heights_snooze_title),
            description = stringResource(R.string.mandatory_heights_desc),
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                val hours = snoozeHours.coerceAtLeast(1)
                showSnoozeHeightsDialog = false
                skipMissingHeightsPrompt = true
                scope.launch {
                    userPreferences.snoozeHeightPromptForHours(hours)
                    snackbar.showSnackbar(appContext.getString(R.string.mandatory_heights_snoozed_format, hours))
                }
                onNavigateBack()
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 4, 24).forEach { hours ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = snoozeHours == hours,
                            onClick = { snoozeHours = hours }
                        )
                        Text(stringResource(R.string.mandatory_heights_snooze_format, hours))
                    }
                }
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

/**
 * Statut hauteur pour une classe de diamètre.
 */
private enum class HeightStatus { NONE, SET, MEASURED, MISSING }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiameterCard(
    diameter: Int,
    count: Int,
    heightStatus: HeightStatus = HeightStatus.NONE,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val borderColor = when (heightStatus) {
        HeightStatus.SET, HeightStatus.MEASURED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        HeightStatus.MISSING -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        HeightStatus.NONE -> Color.Transparent
    }
    ElevatedCard(
        modifier = Modifier
            .then(
                if (heightStatus != HeightStatus.NONE) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier
            )
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.diameter_cm_value_format, diameter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
            if (count > 0 && heightStatus != HeightStatus.NONE) {
                val (label, color) = when (heightStatus) {
                    HeightStatus.SET -> stringResource(R.string.height_status_set) to MaterialTheme.colorScheme.primary
                    HeightStatus.MEASURED -> stringResource(R.string.height_status_measured) to MaterialTheme.colorScheme.tertiary
                    HeightStatus.MISSING -> stringResource(R.string.height_status_missing) to MaterialTheme.colorScheme.error
                    HeightStatus.NONE -> "" to Color.Transparent
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            } else {
                Text(stringResource(R.string.diameter_card_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
