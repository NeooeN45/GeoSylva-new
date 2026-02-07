package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import android.net.Uri
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.ForestrySynthesisParams
import com.forestry.counter.domain.calculation.ClassSynthesis
import com.forestry.counter.domain.calculation.SynthesisTotals
import com.forestry.counter.domain.calculation.tarifs.TarifMethod
import com.forestry.counter.domain.calculation.tarifs.TarifSelection
import com.forestry.counter.domain.calculation.tarifs.TarifCalculator
import com.forestry.counter.domain.usecase.export.QgisExportHelper
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.presentation.components.AppMiniDialog
import com.forestry.counter.presentation.utils.rememberHapticFeedback
import com.forestry.counter.presentation.utils.rememberSoundFeedback
import com.forestry.counter.presentation.utils.ColorUtils
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import java.util.UUID
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.PI
import kotlin.math.sqrt
import com.forestry.counter.R
import android.os.Build
import android.graphics.Color as AndroidColor
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MartelageScreen(
    scope: String,
    forestId: String?,
    parcelleId: String?,
    placetteId: String?,
    essenceRepository: EssenceRepository,
    tigeRepository: TigeRepository,
    parcelleRepository: ParcelleRepository,
    forestryCalculator: ForestryCalculator,
    userPreferences: UserPreferencesManager,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToPriceTablesEditor: (() -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val hapticEnabled by userPreferences.hapticEnabled.collectAsState(initial = true)
    val soundEnabled by userPreferences.soundEnabled.collectAsState(initial = true)
    val hapticIntensity by userPreferences.hapticIntensity.collectAsState(initial = 2)
    val backgroundImageEnabled by userPreferences.backgroundImageEnabled.collectAsState(initial = true)
    val backgroundImageUri by userPreferences.backgroundImageUri.collectAsState(initial = null)
    val animationsEnabled by userPreferences.animationsEnabled.collectAsState(initial = true)
    val haptic = rememberHapticFeedback()
    val sound = rememberSoundFeedback()

    val parcelles by parcelleRepository.getAllParcelles().collectAsState(initial = emptyList())

    // Clé de portée pour mémoriser les paramètres par parcelle / placette
    val scopeKey = remember(scope, forestId, parcelleId, placetteId) {
        when {
            placetteId != null -> "PLACETTE_${placetteId}"
            parcelleId != null -> "PARCELLE_${parcelleId}"
            forestId != null -> "FOREST_${forestId}"
            else -> "GLOBAL"
        }
    }

    val savedSurface by userPreferences.martelageSurfaceFlow(scopeKey).collectAsState(initial = null)
    val savedHo by userPreferences.martelageHoFlow(scopeKey).collectAsState(initial = null)

    val martelageHeightsLocal by userPreferences.martelageHeightsFlow(scopeKey).collectAsState(initial = emptyMap())
    val forestIdForHeights = remember(forestId, parcelleId, parcelles) {
        forestId ?: parcelles.firstOrNull { it.id == parcelleId }?.forestId
    }
    val forestScopeKeyForHeights = remember(forestIdForHeights) {
        forestIdForHeights?.let { "FOREST_${it}" }
    }

    val martelageHeightsForest by userPreferences
        .martelageHeightsFlow(forestScopeKeyForHeights ?: scopeKey)
        .collectAsState(initial = emptyMap())
    val martelageHeightsGlobal by userPreferences
        .martelageHeightsFlow("GLOBAL")
        .collectAsState(initial = emptyMap())

    val martelageHeights = remember(martelageHeightsLocal, martelageHeightsForest, martelageHeightsGlobal, scopeKey, forestScopeKeyForHeights) {
        fun normalizeMap(src: Map<String, Map<Int, Double>>): Map<String, Map<Int, Double>> {
            val out = mutableMapOf<String, MutableMap<Int, Double>>()
            src.forEach { (k, v) ->
                val nk = k.trim().uppercase(Locale.getDefault())
                val m = out.getOrPut(nk) { mutableMapOf() }
                v.forEach { (cls, h) -> m[cls] = h }
            }
            return out.mapValues { it.value.toMap() }
        }

        fun merge(base: Map<String, Map<Int, Double>>, over: Map<String, Map<Int, Double>>): Map<String, Map<Int, Double>> {
            val out = base.mapValues { it.value.toMutableMap() }.toMutableMap()
            over.forEach { (ess, map) ->
                val m = out.getOrPut(ess) { mutableMapOf() }
                m.putAll(map)
            }
            return out.mapValues { it.value.toMap() }
        }

        val globalN = normalizeMap(martelageHeightsGlobal)
        val forestN = if (forestScopeKeyForHeights != null && forestScopeKeyForHeights != scopeKey) normalizeMap(martelageHeightsForest) else emptyMap()
        val localN = normalizeMap(martelageHeightsLocal)

        merge(merge(globalN, forestN), localN)
    }
    val coroutineScope = rememberCoroutineScope()

    fun playClickFeedback() {
        if (hapticEnabled) haptic.performWithIntensity(hapticIntensity)
        if (soundEnabled) sound.click()
    }

    // Données de base
    val essences by essenceRepository.getAllEssences().collectAsState(initial = emptyList())
    val allTiges by tigeRepository.getAllTiges().collectAsState(initial = emptyList())

    // Parcelles incluses dans le périmètre courant
    val parcellesInScope = remember(parcelles, scope, forestId, parcelleId) {
        when (scope.uppercase(Locale.getDefault())) {
            "FOREST" -> parcelles.filter { it.forestId == forestId }
            "PARCELLE", "PLACETTE" -> parcelles.filter { it.id == parcelleId }
            else -> parcelles
        }
    }

    val parcelleIdsInScope = remember(parcellesInScope) { parcellesInScope.map { it.id }.toSet() }

    // Vue locale (Placette / Parcelle / Global) pour la synthèse
    val initialViewScope = remember(scope, placetteId, parcelleId) {
        when (scope.uppercase(Locale.getDefault())) {
            "PLACETTE" -> MartelageViewScope.PLACETTE
            "PARCELLE" -> MartelageViewScope.PARCELLE
            else -> MartelageViewScope.GLOBAL
        }
    }
    var viewScope by remember { mutableStateOf(initialViewScope) }

    // Tiges selon la vue locale
    val tigesInScope = remember(allTiges, viewScope, scope, parcelleIdsInScope, parcelleId, placetteId) {
        when (viewScope) {
            MartelageViewScope.PLACETTE ->
                placetteId?.let { id -> allTiges.filter { it.placetteId == id } } ?: emptyList()

            MartelageViewScope.PARCELLE ->
                parcelleId?.let { id -> allTiges.filter { it.parcelleId == id } } ?: emptyList()

            MartelageViewScope.GLOBAL -> when (scope.uppercase(Locale.getDefault())) {
                "FOREST" -> allTiges.filter { it.parcelleId in parcelleIdsInScope }
                "PARCELLE" -> parcelleId?.let { id -> allTiges.filter { it.parcelleId == id } } ?: allTiges
                "PLACETTE" -> placetteId?.let { id -> allTiges.filter { it.placetteId == id } } ?: allTiges
                else -> allTiges
            }
        }
    }

    fun normalizeEssenceCode(code: String): String = code.trim().uppercase(Locale.getDefault())

    val usedEssenceCodes = remember(tigesInScope) {
        tigesInScope.map { normalizeEssenceCode(it.essenceCode) }.distinct()
    }

    // Toutes les essences présentes dans le périmètre
    val availableEssenceCodes = remember(usedEssenceCodes) { usedEssenceCodes }

    val availableEssences = remember(essences, availableEssenceCodes) {
        val codes = availableEssenceCodes.toSet()
        essences.filter { normalizeEssenceCode(it.code) in codes }
    }

    var selectedEssenceCodes by remember(availableEssences) {
        mutableStateOf(availableEssences.map { normalizeEssenceCode(it.code) }.toSet())
    }

    // Paramètres communs au calcul (surface d'échantillonnage, Ho, hauteurs par classe)
    val fixedClasses = remember { listOf(20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75) }

    var surfaceInput by remember(parcellesInScope) { mutableStateOf("") }
    var hoInput by remember { mutableStateOf("") }
    val hauteurParClasseState = remember {
        mutableStateMapOf<Int, String>().apply {
            fixedClasses.forEach { put(it, "") }
        }
    }

    var showParamDialog by remember { mutableStateOf(false) }
    var askedParamsOnce by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showParamPanel by remember { mutableStateOf(false) }
    var editingHeightsEssenceCode by remember { mutableStateOf<String?>(null) }
    var persistParamsRequested by remember { mutableStateOf(false) }

    // Tarif de cubage — sélection depuis le martelage
    var showTarifMethodDialog by remember { mutableStateOf(false) }
    var currentTarifMethod by remember { mutableStateOf(TarifMethod.ALGAN) }
    var currentTarifNumero by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val sel = forestryCalculator.loadTarifSelection()
        currentTarifMethod = TarifMethod.fromCode(sel?.method ?: "") ?: TarifMethod.ALGAN
        currentTarifNumero = sel?.schaefferNumero ?: sel?.ifnNumero
    }

    // Export QGIS rapide
    var showExportDialog by remember { mutableStateOf(false) }
    val exportGeoJsonLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/geo+json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val (geojson, count) = QgisExportHelper.buildGeoJson(
                    tiges = tigesInScope,
                    essences = essences
                )
                if (count == 0) {
                    snackbar.showSnackbar(context.getString(R.string.export_qgis_no_gps_stems))
                    return@launch
                }
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(geojson) }
                }.onSuccess {
                    snackbar.showSnackbar(context.getString(R.string.export_qgis_geojson_done, count))
                }.onFailure { e ->
                    snackbar.showSnackbar(context.getString(R.string.export_failed_format, e.message ?: ""))
                }
            }
        }
    }
    val exportCsvXyLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val (csv, count) = QgisExportHelper.buildCsvXY(
                    tiges = tigesInScope,
                    essences = essences
                )
                if (count == 0) {
                    snackbar.showSnackbar(context.getString(R.string.export_qgis_no_gps_stems))
                    return@launch
                }
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(csv) }
                }.onSuccess {
                    snackbar.showSnackbar(context.getString(R.string.export_qgis_csv_xy_done, count))
                }.onFailure { e ->
                    snackbar.showSnackbar(context.getString(R.string.export_failed_format, e.message ?: ""))
                }
            }
        }
    }

    // Pré-remplissage des champs si des valeurs ont déjà été enregistrées pour cette portée
    LaunchedEffect(savedSurface) {
        if (savedSurface != null && surfaceInput.isBlank()) {
            surfaceInput = String.format(Locale.US, "%.0f", savedSurface)
        }
    }
    LaunchedEffect(savedHo) {
        if (savedHo != null && hoInput.isBlank()) {
            hoInput = String.format(Locale.US, "%.1f", savedHo)
        }
    }

    fun parseSurfaceM2(raw: String): Double? {
        val s = raw.trim()
        if (s.isBlank()) return null
        val compact = s.lowercase(Locale.getDefault()).replace(" ", "")
        return when {
            compact.endsWith("ha") -> compact.removeSuffix("ha").replace(',', '.').toDoubleOrNull()?.let { it * 10_000.0 }
            compact.endsWith("m2") -> compact.removeSuffix("m2").replace(',', '.').toDoubleOrNull()
            compact.endsWith("m²") -> compact.removeSuffix("m²").replace(',', '.').toDoubleOrNull()
            else -> compact.replace(',', '.').toDoubleOrNull()
        }
    }

    val surfaceInputValueM2 = parseSurfaceM2(surfaceInput)
    val surfaceM2 = when {
        surfaceInput.isBlank() -> savedSurface
        surfaceInputValueM2 != null -> surfaceInputValueM2
        else -> null
    }
    val hoInputValue = hoInput.replace(',', '.').toDoubleOrNull()
    val hoM = hoInputValue ?: savedHo
    val hauteurMap: Map<Int, Double> = fixedClasses.associateWith { d ->
        hauteurParClasseState[d]?.replace(',', '.')?.toDoubleOrNull() ?: hoM ?: 0.0
    }

    // Seule la surface est obligatoire pour activer la synthèse; Ho reste optionnel
    val missingParams = surfaceM2 == null || surfaceM2 <= 0.0

    // Persistance automatique de la surface / Ho valides pour cette portée
    LaunchedEffect(persistParamsRequested) {
        if (persistParamsRequested) {
            if (surfaceInputValueM2 != null && surfaceInputValueM2 > 0.0) {
                userPreferences.setMartelageSurface(scopeKey, surfaceInputValueM2)
            }
            if (hoInputValue != null && hoInputValue > 0.0) {
                userPreferences.setMartelageHo(scopeKey, hoInputValue)
            }
            persistParamsRequested = false
        }
    }

    val synthesisParams by produceState<ForestrySynthesisParams?>(
        initialValue = null,
        forestryCalculator
    ) {
        value = try {
            forestryCalculator.loadSynthesisParams()
        } catch (_: Throwable) {
            null
        }
    }

    val diameterClasses by produceState<List<Int>>(
        initialValue = emptyList(),
        forestryCalculator
    ) {
        value = try {
            forestryCalculator.diameterClasses()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    val promptClasses = remember(diameterClasses) {
        if (diameterClasses.isNotEmpty()) diameterClasses else (5..120 step 5).toList()
    }
    val missingHeightEssenceCodesForPrompt = remember(tigesInScope, martelageHeights, promptClasses, synthesisParams) {
        val heightModes = synthesisParams?.heightModes.orEmpty()
        val byEssence = tigesInScope.groupBy { it.essenceCode.trim().uppercase(Locale.getDefault()) }
        byEssence.keys.filter { code ->
            val tigesEss = byEssence[code].orEmpty()
            val byClass = tigesEss.groupBy { forestryCalculator.diameterClassFor(it.diamCm, promptClasses) }
            val manual = martelageHeights[code.trim().uppercase(Locale.getDefault())].orEmpty()
            byClass.entries.any { (diamClass, list) ->
                val hasMissingMeasured = list.any { it.hauteurM == null }
                if (!hasMissingMeasured) {
                    false
                } else {
                    val manualH = manual[diamClass]
                    val mode = heightModes.firstOrNull { it.essence.equals(code, true) && it.diamClass == diamClass }
                    val fixedH = mode?.mode?.equals("FIXED", ignoreCase = true) == true && (mode.fixed ?: 0.0) > 0.0
                    val sampleH = mode?.mode?.equals("SAMPLES", ignoreCase = true) == true && list.any { it.hauteurM != null }
                    val canResolve = manualH != null || fixedH || sampleH
                    !canResolve
                }
            }
        }.sorted()
    }
    val missingHeightsForPrompt = missingHeightEssenceCodesForPrompt.isNotEmpty()

    LaunchedEffect(missingParams, missingHeightsForPrompt, tigesInScope.size, missingHeightEssenceCodesForPrompt) {
        if (!askedParamsOnce && tigesInScope.isNotEmpty() && (missingParams || missingHeightsForPrompt)) {
            askedParamsOnce = true
            showParamPanel = true
            if (missingParams) {
                showParamDialog = true
            }
            if (missingHeightsForPrompt && editingHeightsEssenceCode == null) {
                editingHeightsEssenceCode = missingHeightEssenceCodesForPrompt.firstOrNull()
            }
        }
    }

    // Agrégation martelage (volumes, G, recettes) pour la vue courante
    val stats by produceState<MartelageStats?>(
        initialValue = null,
        tigesInScope,
        surfaceM2,
        viewScope,
        selectedEssenceCodes,
        martelageHeights,
        hoM,
        hauteurMap,
        synthesisParams,
        diameterClasses
    ) {
        if (tigesInScope.isEmpty() || surfaceM2 == null || surfaceM2 <= 0.0) {
            value = null
            return@produceState
        }

        val surfaceHa = surfaceM2 / 10_000.0
        if (surfaceHa <= 0.0) {
            value = null
            return@produceState
        }

        val classes = if (diameterClasses.isNotEmpty()) diameterClasses else (5..120 step 5).toList()

        val byEssence = tigesInScope.groupBy { normalizeEssenceCode(it.essenceCode) }
        val perEssence = mutableListOf<PerEssenceStats>()

        val heightModes = synthesisParams?.heightModes.orEmpty()
        val missingHeightsByEssence = mutableMapOf<String, List<Int>>()
        byEssence.forEach { (code, tigesEss) ->
            if (selectedEssenceCodes.isNotEmpty() && code !in selectedEssenceCodes) return@forEach
            val byClass = tigesEss.groupBy { forestryCalculator.diameterClassFor(it.diamCm, classes) }
            val manual = martelageHeights[normalizeEssenceCode(code)].orEmpty()
            val missing = byClass.entries
                .filter { (diamClass, list) ->
                    val hasMissingMeasured = list.any { it.hauteurM == null }
                    if (!hasMissingMeasured) {
                        false
                    } else {
                        val manualH = manual[diamClass]
                        val mode = heightModes.firstOrNull { it.essence.equals(code, true) && it.diamClass == diamClass }
                        val fixedH = mode?.mode?.equals("FIXED", ignoreCase = true) == true && (mode.fixed ?: 0.0) > 0.0
                        val sampleH = mode?.mode?.equals("SAMPLES", ignoreCase = true) == true && list.any { it.hauteurM != null }
                        val canResolve = manualH != null || fixedH || sampleH
                        !canResolve
                    }
                }
                .map { it.key }
                .sorted()
            if (missing.isNotEmpty()) {
                missingHeightsByEssence[code] = missing
            }
        }
        val volumeAvailable = missingHeightsByEssence.isEmpty()
        val missingHeightEssenceCodes = missingHeightsByEssence.keys.sorted()
        val missingHeightEssenceNames = missingHeightEssenceCodes.map { code ->
            essences.firstOrNull { normalizeEssenceCode(it.code) == code }?.name ?: code
        }

        var nTotal = 0
        var gTotal = 0.0
        var vTotal = 0.0
        var revenueTotal = 0.0
        var unpricedVolumeTotal = 0.0
        val unpricedEssenceNames = mutableListOf<String>()
        var dmSum = 0.0
        var dmWeight = 0
        var hSum = 0.0
        var hWeight = 0
        var loreyGhSum = 0.0
        var loreyGSum = 0.0

        byEssence.forEach { (code, tigesEss) ->
            if (selectedEssenceCodes.isNotEmpty() && code !in selectedEssenceCodes) return@forEach

            val manualHeightsForEss = martelageHeights[normalizeEssenceCode(code)]
            val (rows, totals) = try {
                forestryCalculator.synthesisForEssence(
                    essenceCode = code,
                    classes = classes,
                    tiges = tigesEss,
                    manualHeights = manualHeightsForEss,
                    method = null,
                    params = synthesisParams,
                    requireHeights = true
                )
            } catch (_: Throwable) {
                emptyList<ClassSynthesis>() to SynthesisTotals(0, null, null, null)
            }

            val vEss = if (volumeAvailable) (totals.vTotal ?: 0.0) else 0.0
            val gEss = tigesEss.sumOf { forestryCalculator.computeG(it.diamCm) }
            val revEss = if (volumeAvailable) rows.sumOf { it.valueSumEur ?: 0.0 } else 0.0
            val nEss = tigesEss.size

            nTotal += nEss
            gTotal += gEss
            vTotal += vEss
            revenueTotal += revEss
            totals.dmWeighted?.let { dm ->
                dmSum += dm * nEss
                dmWeight += nEss
            }
            totals.hMean?.let { hm ->
                hSum += hm * nEss
                hWeight += nEss
            }

            if (volumeAvailable) {
                val manual = manualHeightsForEss.orEmpty()
                tigesEss.forEach { t ->
                    val diamClass = forestryCalculator.diameterClassFor(t.diamCm, classes)
                    val h = t.hauteurM ?: manual[diamClass]
                    if (h != null) {
                        val g = forestryCalculator.computeG(t.diamCm)
                        loreyGhSum += g * h
                        loreyGSum += g
                    }
                }
            }

            val essenceName = essences.firstOrNull { normalizeEssenceCode(it.code) == code }?.name ?: code

            if (volumeAvailable) {
                val unpricedVEss = rows.asSequence()
                    .filter { r -> r.count > 0 && (r.vSum ?: 0.0) > 0.0 && r.valueSumEur == null }
                    .sumOf { it.vSum ?: 0.0 }
                if (unpricedVEss > 0.0) {
                    unpricedVolumeTotal += unpricedVEss
                    unpricedEssenceNames += essenceName
                }
            }

            perEssence += PerEssenceStats(
                essenceCode = code,
                essenceName = essenceName,
                n = nEss,
                vTotal = vEss,
                vPerHa = if (surfaceHa > 0.0) vEss / surfaceHa else 0.0,
                gTotal = gEss,
                gPerHa = if (surfaceHa > 0.0) gEss / surfaceHa else 0.0,
                meanPricePerM3 = if (volumeAvailable && vEss > 0.0 && revEss > 0.0) revEss / vEss else null,
                revenueTotal = if (volumeAvailable && revEss > 0.0) revEss else null,
                revenuePerHa = if (volumeAvailable && revEss > 0.0 && surfaceHa > 0.0) revEss / surfaceHa else null
            )
        }

        if (nTotal == 0 && vTotal == 0.0 && gTotal == 0.0) {
            value = null
            return@produceState
        }

        val nPerHa = if (surfaceHa > 0.0) nTotal / surfaceHa else 0.0
        val gPerHa = if (surfaceHa > 0.0) gTotal / surfaceHa else 0.0
        val vPerHa = if (surfaceHa > 0.0) vTotal / surfaceHa else 0.0
        val unpricedVolumePerHa = if (surfaceHa > 0.0) unpricedVolumeTotal / surfaceHa else 0.0
        val revenuePerHa = if (surfaceHa > 0.0 && revenueTotal > 0.0) revenueTotal / surfaceHa else null

        val dm = if (dmWeight > 0) dmSum / dmWeight else null
        val hMean = if (hWeight > 0) hSum / hWeight else null
        val dg = if (nTotal > 0 && gTotal > 0.0) sqrt((4.0 * gTotal) / (PI * nTotal.toDouble())) * 100.0 else null
        val hLorey = if (volumeAvailable && loreyGSum > 0.0) loreyGhSum / loreyGSum else null

        value = MartelageStats(
            nTotal = nTotal,
            nPerHa = nPerHa,
            gTotal = gTotal,
            gPerHa = gPerHa,
            vTotal = vTotal,
            vPerHa = vPerHa,
            unpricedVolumeTotal = unpricedVolumeTotal,
            unpricedVolumePerHa = unpricedVolumePerHa,
            unpricedEssenceNames = unpricedEssenceNames,
            revenueTotal = if (volumeAvailable && revenueTotal > 0.0) revenueTotal else null,
            revenuePerHa = if (volumeAvailable) revenuePerHa else null,
            dm = dm,
            meanH = if (volumeAvailable) hMean else null,
            dg = dg,
            hLorey = hLorey,
            perEssence = perEssence.sortedBy { it.essenceName },
            volumeAvailable = volumeAvailable,
            missingHeightEssenceCodes = missingHeightEssenceCodes,
            missingHeightEssenceNames = missingHeightEssenceNames
        )
    }

    val exportMartelageCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val s = stats
                if (s == null) {
                    snackbar.showSnackbar(context.getString(R.string.martelage_stats_unavailable_error))
                    return@launch
                }
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { w ->
                        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                        w.write("martelage_export_at;${now}\n")
                        w.write("scope;view;surface_m2;ho_m;volume_total_m3;volume_per_ha_m3;g_total_m2;g_per_ha_m2;revenue_total_eur;revenue_per_ha_eur;unpriced_volume_m3;unpriced_essences;dm_cm;h_mean_m;dg_cm;h_lorey_m\n")
                        val unpricedNames = s.unpricedEssenceNames.distinct().joinToString(",")
                        val missingNames = s.missingHeightEssenceNames.distinct().joinToString(",")
                        w.write(
                            listOf(
                                scope,
                                viewScope.name,
                                String.format(Locale.US, "%.1f", surfaceM2 ?: 0.0),
                                String.format(Locale.US, "%.1f", hoM ?: 0.0),
                                String.format(Locale.US, "%.3f", s.vTotal),
                                String.format(Locale.US, "%.3f", s.vPerHa),
                                String.format(Locale.US, "%.3f", s.gTotal),
                                String.format(Locale.US, "%.3f", s.gPerHa),
                                s.revenueTotal?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                s.revenuePerHa?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                String.format(Locale.US, "%.3f", s.unpricedVolumeTotal),
                                unpricedNames,
                                s.dm?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                s.meanH?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                s.dg?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                s.hLorey?.let { String.format(Locale.US, "%.2f", it) } ?: ""
                            ).joinToString(";") + "\n"
                        )

                        if (!s.volumeAvailable) {
                            w.write("missing_heights;${missingNames}\n")
                        }

                        w.write("\n")
                        w.write("per_essence\n")
                        w.write("essence_code;essence_name;n;v_total_m3;v_per_ha_m3;g_total_m2;g_per_ha_m2;mean_price_eur_m3;revenue_total_eur;revenue_per_ha_eur\n")
                        s.perEssence.forEach { row ->
                            w.write(
                                listOf(
                                    row.essenceCode,
                                    row.essenceName,
                                    row.n.toString(),
                                    String.format(Locale.US, "%.3f", row.vTotal),
                                    String.format(Locale.US, "%.3f", row.vPerHa),
                                    String.format(Locale.US, "%.3f", row.gTotal),
                                    String.format(Locale.US, "%.3f", row.gPerHa),
                                    row.meanPricePerM3?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                    row.revenueTotal?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                                    row.revenuePerHa?.let { String.format(Locale.US, "%.2f", it) } ?: ""
                                ).joinToString(";") + "\n"
                            )
                        }
                    }
                }.onSuccess {
                    snackbar.showSnackbar(context.getString(R.string.result_exported))
                }.onFailure { e ->
                    snackbar.showSnackbar(context.getString(R.string.export_failed_format, e.message ?: ""))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    if (backgroundImageEnabled) {
        val uriString = backgroundImageUri
        if (uriString != null) {
            val uri = remember(uriString) { Uri.parse(uriString) }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    imageView.setImageURI(uri)
                }
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.forest_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        topBar = {
            val topBarBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            val topBarContent = ColorUtils.getContrastingTextColor(topBarBackground)
            TopAppBar(
                title = { Text(stringResource(R.string.martelage_before_cut_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        playClickFeedback()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Export QGIS rapide
                    IconButton(
                        onClick = {
                            playClickFeedback()
                            showExportDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Map, contentDescription = stringResource(R.string.export_qgis))
                    }
                    // Export CSV martelage
                    IconButton(
                        onClick = {
                            playClickFeedback()
                            val ts = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                            exportMartelageCsv.launch("martelage-${scopeKey}-${viewScope.name.lowercase(Locale.getDefault())}-${ts}.csv")
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBackground,
                    titleContentColor = topBarContent,
                    navigationIconContentColor = topBarContent,
                    actionIconContentColor = topBarContent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
            val contentScrollState = rememberScrollState()

            // Fond semi-opaque pour le contenu et couleur de texte auto-contrastée globale
            val pageBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            val pageTextColor = ColorUtils.getContrastingTextColor(pageBackground)

            CompositionLocalProvider(LocalContentColor provides pageTextColor) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(contentScrollState)
                        .padding(padding)
                        .background(pageBackground)
                        .padding(12.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                // Contexte du périmètre
                val scopeLabel = when (scope.uppercase(Locale.getDefault())) {
                    "PLACETTE" -> stringResource(R.string.martelage_scope_placette_desc)
                    "PARCELLE" -> stringResource(R.string.martelage_scope_parcelle_desc)
                    "FOREST" -> stringResource(R.string.martelage_scope_forest_desc)
                    else -> stringResource(R.string.martelage_scope_global_desc)
                }
                Text(scopeLabel, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(4.dp))

                // Sélecteur de vue locale (Placette / Parcelle / Global)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (placetteId != null) {
                        FilterChip(
                            selected = viewScope == MartelageViewScope.PLACETTE,
                            onClick = { viewScope = MartelageViewScope.PLACETTE },
                            label = { Text(stringResource(R.string.martelage_view_placette)) }
                        )
                    }
                    if (parcelleId != null) {
                        FilterChip(
                            selected = viewScope == MartelageViewScope.PARCELLE,
                            onClick = { viewScope = MartelageViewScope.PARCELLE },
                            label = { Text(stringResource(R.string.martelage_view_parcelle)) }
                        )
                    }
                    FilterChip(
                        selected = viewScope == MartelageViewScope.GLOBAL,
                        onClick = { viewScope = MartelageViewScope.GLOBAL },
                        label = { Text(stringResource(R.string.martelage_view_global)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // En-tête et bouton d'ouverture du mini-menu de paramètres
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.martelage_summary_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {
                                playClickFeedback()
                                showParamPanel = !showParamPanel
                            },
                            label = { Text(stringResource(R.string.martelage_parameters)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null
                                )
                            }
                        )
                        FilterChip(
                            selected = showDetails,
                            onClick = {
                                playClickFeedback()
                                showDetails = !showDetails
                            },
                            label = { Text(stringResource(R.string.martelage_details)) }
                        )
                    }
                }

                if (parcellesInScope.isNotEmpty()) {
                    val includedNames = parcellesInScope.joinToString { it.name }
                    Text(
                        stringResource(R.string.martelage_included_parcelles_format, includedNames),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge méthode de cubage + compteur GPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val gpsCount = remember(tigesInScope) {
                        tigesInScope.count { !it.gpsWkt.isNullOrBlank() }
                    }
                    AssistChip(
                        onClick = { showTarifMethodDialog = true },
                        label = {
                            Text(
                                stringResource(R.string.martelage_cubage_method_current_format, currentTarifMethod.label),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    if (gpsCount > 0) {
                        AssistChip(
                            onClick = { showExportDialog = true },
                            label = {
                                Text("GPS: $gpsCount", style = MaterialTheme.typography.labelSmall)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                if (tigesInScope.isEmpty()) {
                    Text(
                        stringResource(R.string.martelage_no_tiges_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val placeholderDash = stringResource(R.string.placeholder_dash)
                    val ellipsis = stringResource(R.string.ellipsis)
                    val euroSymbol = stringResource(R.string.euro_symbol)

                    // Sélection d'essences (multi)
                    Text(stringResource(R.string.martelage_available_species), style = MaterialTheme.typography.titleMedium)
                    if (availableEssences.isEmpty()) {
                        Text(
                            stringResource(R.string.martelage_no_resinous_species_detected),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableEssences.forEach { e ->
                                val normalized = normalizeEssenceCode(e.code)
                                val selected = selectedEssenceCodes.contains(normalized)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        playClickFeedback()
                                        selectedEssenceCodes = if (selected) {
                                            (selectedEssenceCodes - normalized).ifEmpty { setOf(normalized) }
                                        } else {
                                            selectedEssenceCodes + normalized
                                        }
                                    },
                                    label = { Text(e.name) }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showDetails && !missingParams && stats != null,
                            enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing)),
                            exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing))
                        ) {
                            val s = stats ?: return@AnimatedVisibility

                            Spacer(modifier = Modifier.height(8.dp))

                            val detailsCardBg = MaterialTheme.colorScheme.surfaceVariant
                            val detailsCardContent = ColorUtils.getContrastingTextColor(detailsCardBg)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = detailsCardBg,
                                    contentColor = detailsCardContent
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(stringResource(R.string.martelage_dendro_details_title), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        stringResource(R.string.martelage_dg_format, formatDiameter(s.dg, placeholderDash)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        stringResource(R.string.martelage_lorey_height_format, formatHeight(s.hLorey, placeholderDash)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // Paramètres de calcul (mini menu animé)
                    AnimatedVisibility(
                        visible = showParamPanel,
                        enter = fadeIn(animationSpec = tween(durationMillis = 200)) + expandVertically(),
                        exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkVertically()
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stringResource(R.string.martelage_calc_parameters_title),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                playClickFeedback()
                                                if (surfaceInputValueM2 == null || surfaceInputValueM2 <= 0.0) {
                                                    coroutineScope.launch {
                                                        snackbar.showSnackbar(context.getString(R.string.martelage_sample_area_missing_error))
                                                    }
                                                    return@FilledTonalButton
                                                } else {
                                                    persistParamsRequested = true
                                                    if (!missingHeightsForPrompt) {
                                                        showParamPanel = false
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(stringResource(R.string.validate))
                                        }
                                        IconButton(
                                            onClick = {
                                                playClickFeedback()
                                                showParamPanel = false
                                            }
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = null)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = surfaceInput,
                                    onValueChange = { surfaceInput = it },
                                    label = { Text(stringResource(R.string.martelage_sample_area_m2)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    placeholder = { Text("ex : 314") },
                                    suffix = { Text("m²") },
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = hoInput,
                                    onValueChange = { hoInput = it },
                                    label = { Text(stringResource(R.string.martelage_ho_dominant_optional)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    placeholder = { Text("ex : 22,5") },
                                    suffix = { Text("m") },
                                    singleLine = true
                                )

                                // ── Sélecteur de méthode de cubage ──
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            stringResource(R.string.martelage_cubage_method),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            stringResource(R.string.martelage_cubage_method_current_format, currentTarifMethod.label),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (currentTarifNumero != null) {
                                            Text(
                                                stringResource(R.string.martelage_cubage_method_numero_format, currentTarifNumero!!),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            playClickFeedback()
                                            showTarifMethodDialog = true
                                        }
                                    ) {
                                        Text(stringResource(R.string.martelage_cubage_method_change))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    stringResource(R.string.martelage_heights_by_species_or_ho_desc),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                val heightModes = synthesisParams?.heightModes.orEmpty()
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    availableEssences.forEach { ess ->
                                        val customMap = martelageHeights[normalizeEssenceCode(ess.code)]
                                        val hasCustomManual = customMap != null && customMap.isNotEmpty()
                                        val hasCustomFixed = heightModes.any {
                                            it.essence.equals(ess.code, true) &&
                                                it.mode.equals("FIXED", ignoreCase = true) &&
                                                (it.fixed ?: 0.0) > 0.0
                                        }
                                        val hasCustom = hasCustomManual || hasCustomFixed
                                        val missing = missingHeightEssenceCodesForPrompt.contains(normalizeEssenceCode(ess.code))

                                        val targetCardColor = when {
                                            missing -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                            hasCustom -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                        val cardColor by animateColorAsState(
                                            targetValue = targetCardColor,
                                            animationSpec = tween(
                                                durationMillis = if (animationsEnabled) 220 else 0,
                                                easing = FastOutSlowInEasing
                                            ),
                                            label = "martelageHeightEssenceCardColor"
                                        )
                                        val targetElevation = when {
                                            missing -> 4.dp
                                            hasCustom -> 2.dp
                                            else -> 0.dp
                                        }
                                        val cardElevation by animateDpAsState(
                                            targetValue = targetElevation,
                                            animationSpec = tween(
                                                durationMillis = if (animationsEnabled) 220 else 0,
                                                easing = FastOutSlowInEasing
                                            ),
                                            label = "martelageHeightEssenceCardElevation"
                                        )
                                        Card(
                                            onClick = {
                                                playClickFeedback()
                                                editingHeightsEssenceCode = ess.code
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = cardColor),
                                            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        ess.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    AnimatedVisibility(
                                                        visible = missing,
                                                        enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 160 else 0, easing = FastOutSlowInEasing)) +
                                                            expandHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing)),
                                                        exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 140 else 0, easing = FastOutSlowInEasing)) +
                                                            shrinkHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing))
                                                    ) {
                                                        ToCompleteBadge()
                                                    }
                                                }
                                                Text(
                                                    stringResource(if (hasCustom) R.string.martelage_custom_heights_defined else R.string.martelage_default_height_tables_used),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (missingParams) {
                                    Row(
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.martelage_sample_area_missing_error),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { showParamDialog = true }) {
                                            Text(stringResource(R.string.martelage_fill_in))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!missingParams && stats != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val s = stats!!

                        val vTotalAnim by animateFloatAsState(
                            targetValue = if (s.volumeAvailable) s.vTotal.toFloat() else 0f,
                            animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0, easing = FastOutSlowInEasing),
                            label = "vTotalAnim"
                        )
                        val vPerHaAnim by animateFloatAsState(
                            targetValue = if (s.volumeAvailable) s.vPerHa.toFloat() else 0f,
                            animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0, easing = FastOutSlowInEasing),
                            label = "vPerHaAnim"
                        )
                        val revenueTotalAnim by animateFloatAsState(
                            targetValue = if (s.volumeAvailable) (s.revenueTotal ?: 0.0).toFloat() else 0f,
                            animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0, easing = FastOutSlowInEasing),
                            label = "revenueTotalAnim"
                        )
                        val revenuePerHaAnim by animateFloatAsState(
                            targetValue = if (s.volumeAvailable) (s.revenuePerHa ?: 0.0).toFloat() else 0f,
                            animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0, easing = FastOutSlowInEasing),
                            label = "revenuePerHaAnim"
                        )
                        val missingPriceVolAnim by animateFloatAsState(
                            targetValue = if (s.volumeAvailable) s.unpricedVolumeTotal.toFloat() else 0f,
                            animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0, easing = FastOutSlowInEasing),
                            label = "missingPriceVolAnim"
                        )

                        val revenueTotalText = if (s.revenueTotal == null) placeholderDash else formatMoney(revenueTotalAnim.toDouble(), placeholderDash, euroSymbol)
                        val revenuePerHaText = if (s.revenuePerHa == null) placeholderDash else formatMoney(revenuePerHaAnim.toDouble(), placeholderDash, euroSymbol)

                        val missingPriceVol = s.unpricedVolumeTotal
                        val missingPriceVisible = missingPriceVol > 0.0 && s.vTotal > 0.0

                        AnimatedVisibility(
                            visible = !s.volumeAvailable,
                            enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing)),
                            exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing))
                        ) {
                            val warnBg = MaterialTheme.colorScheme.errorContainer
                            val warnFg = ColorUtils.getContrastingTextColor(warnBg)
                            val names = s.missingHeightEssenceNames.take(3).joinToString(", ") +
                                if (s.missingHeightEssenceNames.size > 3) ellipsis else ""
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = warnBg,
                                    contentColor = warnFg
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null)
                                        Text(
                                            stringResource(R.string.martelage_missing_heights_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        stringResource(R.string.martelage_missing_heights_desc, names),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    TextButton(
                                        onClick = {
                                            playClickFeedback()
                                            showParamPanel = true
                                            editingHeightsEssenceCode = s.missingHeightEssenceCodes.firstOrNull()
                                        }
                                    ) {
                                        Text(stringResource(R.string.martelage_fill_heights))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        AnimatedVisibility(
                            visible = s.volumeAvailable && missingPriceVisible,
                            enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                slideInVertically(
                                    animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing),
                                    initialOffsetY = { -it / 12 }
                                ) +
                                expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing)),
                            exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing),
                                    targetOffsetY = { -it / 12 }
                                ) +
                                shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing))
                        ) {
                            val pct = ((missingPriceVol / s.vTotal) * 100.0)
                                .coerceIn(0.0, 100.0)
                                .roundToInt()

                            Column {
                                val warnBg = MaterialTheme.colorScheme.errorContainer
                                val warnFg = ColorUtils.getContrastingTextColor(warnBg)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = warnBg,
                                        contentColor = warnFg
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null)
                                            Text(
                                                stringResource(R.string.martelage_missing_prices_title),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        Text(
                                            stringResource(
                                                R.string.martelage_missing_prices_desc,
                                                formatVolume(missingPriceVolAnim.toDouble()),
                                                pct
                                            ),
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (s.unpricedEssenceNames.isNotEmpty()) {
                                            val names = s.unpricedEssenceNames.take(3).joinToString(", ") +
                                                if (s.unpricedEssenceNames.size > 3) ellipsis else ""
                                            Text(
                                                stringResource(R.string.martelage_missing_prices_essences, names),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        (onNavigateToPriceTablesEditor ?: onNavigateToSettings)?.let { nav ->
                                            TextButton(
                                                onClick = {
                                                    playClickFeedback()
                                                    nav()
                                                }
                                            ) {
                                                Text(stringResource(R.string.martelage_configure_prices))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        AnimatedVisibility(
                            visible = s.volumeAvailable,
                            enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing)),
                            exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
                                shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing))
                        ) {
                            // Carte Volume & Prix
                            val volumeCardBg = MaterialTheme.colorScheme.primaryContainer
                            val volumeCardContent = ColorUtils.getContrastingTextColor(volumeCardBg)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = volumeCardBg,
                                    contentColor = volumeCardContent
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(stringResource(R.string.martelage_volume_price_title), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        stringResource(R.string.martelage_total_volume_format, formatVolume(vTotalAnim.toDouble())),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        stringResource(R.string.martelage_volume_per_ha_format, formatVolume(vPerHaAnim.toDouble())),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        stringResource(R.string.martelage_estimated_revenue_format, revenueTotalText, revenuePerHaText),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Carte Surface terrière (S% affiché seulement si on aura G initial plus tard)
                        val surfaceCardBg = MaterialTheme.colorScheme.secondaryContainer
                        val surfaceCardContent = ColorUtils.getContrastingTextColor(surfaceCardBg)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceCardBg,
                                contentColor = surfaceCardContent
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(stringResource(R.string.martelage_basal_area_title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.martelage_g_removed_format, formatG(s.gTotal)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    stringResource(R.string.martelage_g_per_ha_format, formatG(s.gPerHa)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Carte Densité & structure
                        val densityCardBg = MaterialTheme.colorScheme.tertiaryContainer
                        val densityCardContent = ColorUtils.getContrastingTextColor(densityCardBg)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = densityCardBg,
                                contentColor = densityCardContent
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(stringResource(R.string.martelage_density_structure_title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.martelage_stems_count_format, s.nTotal, formatIntPerHa(s.nPerHa)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    stringResource(R.string.martelage_dm_hm_format, formatDiameter(s.dm, placeholderDash), formatHeight(s.meanH, placeholderDash)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (s.volumeAvailable && s.perEssence.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(stringResource(R.string.martelage_per_species_title), style = MaterialTheme.typography.titleMedium)

                            val perEssenceCardBg = MaterialTheme.colorScheme.surfaceVariant
                            val perEssenceCardContent = ColorUtils.getContrastingTextColor(perEssenceCardBg)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = perEssenceCardBg,
                                    contentColor = perEssenceCardContent
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // En-tête du tableau
                                    Row {
                                        Text(stringResource(R.string.martelage_table_species), modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                                        Text(stringResource(R.string.martelage_table_v_m3), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text(stringResource(R.string.martelage_table_v_per_ha), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text(stringResource(R.string.martelage_table_g_m2), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text(stringResource(R.string.martelage_table_eur_per_m3), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text(stringResource(R.string.martelage_table_revenue), modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium)
                                    }

                                    HorizontalDivider()

                                    for (row in s.perEssence) {
                                        val tint = essenceTintColor(row.essenceCode, essences)
                                        val bg = tint?.copy(alpha = 0.18f)
                                            ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.04f)
                                        val rowTextColor = ColorUtils.getContrastingTextColor(bg)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                        ) {
                                            Text(row.essenceName, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium, color = rowTextColor)
                                            Text(formatVolume(row.vTotal), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = rowTextColor)
                                            Text(formatVolume(row.vPerHa), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = rowTextColor)
                                            Text(formatG(row.gTotal), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = rowTextColor)
                                            Text(formatPrice(row.meanPricePerM3, placeholderDash), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = rowTextColor)
                                            Text(formatMoney(row.revenueTotal, placeholderDash, euroSymbol), modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall, color = rowTextColor)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!missingParams && stats == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.martelage_stats_unavailable_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    } 
    if (showParamDialog) {
        AppMiniDialog(
            onDismissRequest = { showParamDialog = false },
            animationsEnabled = animationsEnabled,
            icon = Icons.Filled.Tune,
            title = stringResource(R.string.martelage_calc_parameters_title),
            description = stringResource(R.string.martelage_params_dialog_desc),
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                persistParamsRequested = true
                showParamDialog = false
            }
        ) {
            OutlinedTextField(
                value = surfaceInput,
                onValueChange = { surfaceInput = it },
                label = { Text(stringResource(R.string.martelage_sample_area_m2)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text("ex : 314") },
                suffix = { Text("m²") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = hoInput,
                onValueChange = { hoInput = it },
                label = { Text(stringResource(R.string.martelage_ho_dominant_optional)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                placeholder = { Text("ex : 22,5") },
                suffix = { Text("m") },
                singleLine = true
            )
            Text(
                stringResource(R.string.martelage_params_dialog_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Dialog de saisie des hauteurs par essence et par classe de diamètre (uniquement classes où il y a des tiges)
    val editingEssenceCode = editingHeightsEssenceCode
    if (editingEssenceCode != null) {
        val normalizedEditingEssenceCode = remember(editingEssenceCode) { editingEssenceCode.trim().uppercase(Locale.getDefault()) }
        val tigesEssence = remember(tigesInScope, editingEssenceCode) {
            tigesInScope.filter { it.essenceCode.trim().equals(editingEssenceCode.trim(), ignoreCase = true) }
        }
        val classesForDialog = remember(diameterClasses) {
            if (diameterClasses.isNotEmpty()) diameterClasses else (5..120 step 5).toList()
        }
        val byClass = remember(tigesEssence, classesForDialog) {
            tigesEssence.groupBy { forestryCalculator.diameterClassFor(it.diamCm, classesForDialog) }
        }
        val presentClasses = remember(byClass) {
            val counts = byClass.mapValues { it.value.size }
            byClass.keys.sortedWith(compareByDescending<Int> { counts[it] ?: 0 }.thenBy { it })
        }

        val existingForEssence = martelageHeights[normalizedEditingEssenceCode] ?: emptyMap()
        val fixedForEssence = remember(synthesisParams, editingEssenceCode) {
            synthesisParams?.heightModes
                .orEmpty()
                .filter {
                    it.essence.trim().equals(normalizedEditingEssenceCode, true) &&
                        it.mode.equals("FIXED", ignoreCase = true) &&
                        (it.fixed ?: 0.0) > 0.0
                }
                .associate { it.diamClass to (it.fixed ?: 0.0) }
        }
        var localInputs by remember(editingEssenceCode, presentClasses, fixedForEssence) {
            mutableStateOf(
                presentClasses.associateWith { cls ->
                    val v = existingForEssence[cls] ?: fixedForEssence[cls]
                    v?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: ""
                }
            )
        }

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

        val essenceName = availableEssences.firstOrNull { normalizeEssenceCode(it.code) == normalizedEditingEssenceCode }?.name ?: editingEssenceCode

        val dialogDesc = if (presentClasses.isEmpty()) {
            stringResource(R.string.martelage_no_stem_for_species)
        } else {
            stringResource(R.string.martelage_height_by_class_hint)
        }
        AppMiniDialog(
            onDismissRequest = { editingHeightsEssenceCode = null },
            animationsEnabled = animationsEnabled,
            icon = Icons.Default.Height,
            title = stringResource(R.string.martelage_heights_dialog_title_format, essenceName),
            description = dialogDesc,
            confirmText = stringResource(R.string.validate),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                val cleanedForEssence: Map<Int, Double> = localInputs.mapNotNull { (cls, str) ->
                    val (mean, _) = parseHeightInputMean(str)
                    if (mean != null && mean > 0.0) cls to mean else null
                }.toMap()

                val newMap = martelageHeightsLocal.toMutableMap().apply {
                    if (cleanedForEssence.isEmpty()) remove(normalizedEditingEssenceCode) else put(normalizedEditingEssenceCode, cleanedForEssence)
                    if (editingEssenceCode != normalizedEditingEssenceCode) remove(editingEssenceCode)
                }

                coroutineScope.launch {
                    userPreferences.setMartelageHeights(scopeKey, newMap)

                    // Propager vers le scope FOREST pour éviter l'effet "re-demande" en vue projet / global.
                    val forestKey = forestScopeKeyForHeights
                    if (!forestKey.isNullOrBlank() && forestKey != scopeKey) {
                        val forestMap = martelageHeightsForest.toMutableMap().apply {
                            if (cleanedForEssence.isEmpty()) {
                                remove(normalizedEditingEssenceCode)
                            } else {
                                put(normalizedEditingEssenceCode, cleanedForEssence)
                            }
                        }
                        userPreferences.setMartelageHeights(forestKey, forestMap)
                    }
                }

                editingHeightsEssenceCode = null
            }
        ) {
            if (presentClasses.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(presentClasses, key = { it }) { cls ->
                        val count = byClass[cls]?.size ?: 0
                        val missingCount = byClass[cls]?.count { it.hauteurM == null } ?: 0
                        val (meanVal, meanCount) = parseHeightInputMean(localInputs[cls] ?: "")
                        val hasValue = meanVal != null && meanVal > 0.0
                        val needsValue = missingCount > 0 && !hasValue

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
                            label = "martelageHeightClassCardColor"
                        )
                        val cardElevation by animateDpAsState(
                            targetValue = if (needsValue) 4.dp else 1.dp,
                            animationSpec = tween(
                                durationMillis = if (animationsEnabled) 220 else 0,
                                easing = FastOutSlowInEasing
                            ),
                            label = "martelageHeightClassCardElevation"
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.martelage_diameter_class_cm_format, cls),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AnimatedVisibility(
                                        visible = needsValue,
                                        enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 160 else 0, easing = FastOutSlowInEasing)) +
                                            expandHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 140 else 0, easing = FastOutSlowInEasing)) +
                                            shrinkHorizontally(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing))
                                    ) {
                                        ToCompleteBadge()
                                    }
                                }
                                OutlinedTextField(
                                    value = localInputs[cls] ?: "",
                                    onValueChange = { newVal ->
                                        localInputs = localInputs.toMutableMap().also { it[cls] = newVal }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = needsValue,
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
    }

    // ── Dialogue sélection méthode de cubage ──
    if (showTarifMethodDialog) {
        var selectedMethod by remember { mutableStateOf(currentTarifMethod) }
        var selectedNumero by remember { mutableStateOf(currentTarifNumero) }
        val availableRange = TarifCalculator.availableTarifNumbers(selectedMethod)
        val needsNumero = availableRange != null

        AlertDialog(
            onDismissRequest = { showTarifMethodDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    currentTarifMethod = selectedMethod
                    currentTarifNumero = selectedNumero
                    coroutineScope.launch {
                        forestryCalculator.saveTarifSelection(
                            TarifSelection(
                                method = selectedMethod.code,
                                schaefferNumero = if (selectedMethod == TarifMethod.SCHAEFFER_1E || selectedMethod == TarifMethod.SCHAEFFER_2E) selectedNumero else null,
                                ifnNumero = if (selectedMethod == TarifMethod.IFN_RAPIDE || selectedMethod == TarifMethod.IFN_LENT) selectedNumero else null
                            )
                        )
                        snackbar.showSnackbar(context.getString(R.string.martelage_cubage_method_saved))
                    }
                    showTarifMethodDialog = false
                }) {
                    Text(stringResource(R.string.validate))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTarifMethodDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.martelage_cubage_method)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TarifMethod.entries.forEach { method ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .let { mod ->
                                    mod
                                }
                                .background(
                                    if (selectedMethod == method) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = {
                                    selectedMethod = method
                                    if (TarifCalculator.availableTarifNumbers(method) == null) {
                                        selectedNumero = null
                                    } else if (selectedNumero == null) {
                                        selectedNumero = TarifCalculator.recommendedTarifNumero(method, "HETRE_COMMUN")
                                    }
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(method.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (method.entrees == 1) "1 entrée (D seul)" else "2 entrées (D + H)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (needsNumero && availableRange != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            stringResource(R.string.settings_tarif_numero_label),
                            style = MaterialTheme.typography.titleSmall
                        )
                        var numeroInput by remember(selectedMethod) {
                            mutableStateOf(selectedNumero?.toString() ?: "")
                        }
                        OutlinedTextField(
                            value = numeroInput,
                            onValueChange = { v ->
                                numeroInput = v
                                selectedNumero = v.toIntOrNull()?.coerceIn(availableRange)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("${availableRange.first}–${availableRange.last}") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        )
    }

    // ── Dialogue export QGIS ──
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.export_qgis_choose_format)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val gpsCount = remember(tigesInScope) {
                        tigesInScope.count { !it.gpsWkt.isNullOrBlank() }
                    }
                    Text(
                        stringResource(R.string.gps_satellites_format, gpsCount).replace("Sat", "GPS"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilledTonalButton(
                        onClick = {
                            playClickFeedback()
                            showExportDialog = false
                            val ts = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                            exportGeoJsonLauncher.launch("tiges-${scopeKey}-${ts}.geojson")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.export_qgis_geojson))
                            Text(
                                stringResource(R.string.export_qgis_geojson_desc),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            playClickFeedback()
                            showExportDialog = false
                            val ts = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                            exportCsvXyLauncher.launch("tiges-${scopeKey}-${ts}.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.export_qgis_csv_xy))
                            Text(
                                stringResource(R.string.export_qgis_csv_xy_desc),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        )
    }
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


// Vue locale pour la synthèse (indépendante du scope de navigation)
private enum class MartelageViewScope { PLACETTE, PARCELLE, GLOBAL }

// Agrégats globaux martelage pour une vue donnée
private data class MartelageStats(
    val nTotal: Int,
    val nPerHa: Double,
    val gTotal: Double,
    val gPerHa: Double,
    val vTotal: Double,
    val vPerHa: Double,
    val unpricedVolumeTotal: Double,
    val unpricedVolumePerHa: Double,
    val unpricedEssenceNames: List<String>,
    val revenueTotal: Double?,
    val revenuePerHa: Double?,
    val dm: Double?,
    val meanH: Double?,
    val dg: Double?,
    val hLorey: Double?,
    val perEssence: List<PerEssenceStats>,
    val volumeAvailable: Boolean,
    val missingHeightEssenceCodes: List<String>,
    val missingHeightEssenceNames: List<String>
)

// Agrégats par essence pour le tableau
private data class PerEssenceStats(
    val essenceCode: String,
    val essenceName: String,
    val n: Int,
    val vTotal: Double,
    val vPerHa: Double,
    val gTotal: Double,
    val gPerHa: Double,
    val meanPricePerM3: Double?,
    val revenueTotal: Double?,
    val revenuePerHa: Double?
)

// Helpers de formatage simples, uniquement pour l'affichage
private fun formatVolume(v: Double): String {
    val a = abs(v)
    val decimals = when {
        a < 10.0 -> 3
        a < 100.0 -> 2
        else -> 1
    }
    return String.format(Locale.getDefault(), "%.${decimals}f", v)
}

private fun formatG(g: Double): String = String.format(Locale.getDefault(), "%.2f", g)

private fun formatMoney(v: Double?, placeholder: String, euroSymbol: String): String =
    v?.let { String.format(Locale.getDefault(), "%.0f %s", it, euroSymbol) } ?: placeholder

private fun formatPrice(p: Double?, placeholder: String): String =
    p?.let { String.format(Locale.getDefault(), "%.0f", it) } ?: placeholder

private fun formatIntPerHa(nPerHa: Double): String = String.format(Locale.getDefault(), "%.0f", nPerHa)

private fun formatDiameter(dm: Double?, placeholder: String): String =
    dm?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: placeholder

private fun formatHeight(h: Double?, placeholder: String): String =
    h?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: placeholder

private fun essenceTintColor(
    essenceCode: String,
    essences: List<com.forestry.counter.domain.model.Essence>
): Color? {
    val hex = essences.firstOrNull { it.code == essenceCode }?.colorHex
    if (hex.isNullOrBlank()) return null
    return try {
        Color(AndroidColor.parseColor(hex))
    } catch (_: Throwable) {
        null
    }
}

