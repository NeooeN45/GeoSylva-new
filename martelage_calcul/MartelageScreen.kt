package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import android.net.Uri
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.ClassSynthesis
import com.forestry.counter.domain.calculation.SynthesisTotals
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.presentation.utils.rememberHapticFeedback
import com.forestry.counter.presentation.utils.rememberSoundFeedback
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.ui.platform.LocalContext
import java.util.UUID
import java.util.Locale
import kotlin.math.roundToInt
import com.forestry.counter.R
import android.os.Build
import android.graphics.Color as AndroidColor

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
    onNavigateBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }

    val hapticEnabled by userPreferences.hapticEnabled.collectAsState(initial = true)
    val soundEnabled by userPreferences.soundEnabled.collectAsState(initial = true)
    val hapticIntensity by userPreferences.hapticIntensity.collectAsState(initial = 2)
    val backgroundImageEnabled by userPreferences.backgroundImageEnabled.collectAsState(initial = true)
    val backgroundImageUri by userPreferences.backgroundImageUri.collectAsState(initial = null)
    val haptic = rememberHapticFeedback()
    val sound = rememberSoundFeedback()

    fun playClickFeedback() {
        if (hapticEnabled) haptic.performWithIntensity(hapticIntensity)
        if (soundEnabled) sound.click()
    }

    // Données de base
    val essences by essenceRepository.getAllEssences().collectAsState(initial = emptyList())
    val parcelles by parcelleRepository.getAllParcelles().collectAsState(initial = emptyList())
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

    // Essences résineuses présentes dans ce périmètre
    val resinousCodes = remember(essences) {
        essences
            .filter {
                val cat = (it.categorie ?: "").lowercase(Locale.getDefault())
                cat.contains("résineux") || cat.contains("resineux") || cat.contains("conif")
            }
            .map { it.code }
            .toSet()
    }

    val usedEssenceCodes = remember(tigesInScope) {
        tigesInScope.map { it.essenceCode }.distinct()
    }

    // On limite aux essences présentes ET résineuses, sinon on prend tout ce qui est présent
    val availableEssenceCodes = remember(usedEssenceCodes, resinousCodes) {
        val onlyResinous = usedEssenceCodes.filter { it in resinousCodes }
        if (onlyResinous.isNotEmpty()) onlyResinous else usedEssenceCodes
    }

    val availableEssences = remember(essences, availableEssenceCodes) {
        essences.filter { it.code in availableEssenceCodes }
    }

    var selectedEssenceCodes by remember(availableEssences) {
        mutableStateOf(availableEssences.map { it.code }.toSet())
    }

    // Paramètres communs au calcul (surface d'échantillonnage, Ho, hauteurs par classe)
    val fixedClasses = remember { listOf(20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75) }

    val defaultSampleArea = remember(parcellesInScope) {
        parcellesInScope.firstOrNull()?.sampleAreaM2 ?: 2000.0
    }

    var surfaceInput by remember(parcellesInScope) {
        mutableStateOf(
            defaultSampleArea?.let { String.format(Locale.US, "%.0f", it) } ?: ""
        )
    }
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

    val surfaceM2 = surfaceInput.replace(',', '.').toDoubleOrNull()
    val hoM = hoInput.replace(',', '.').toDoubleOrNull()
    val hauteurMap: Map<Int, Double> = fixedClasses.associateWith { d ->
        hauteurParClasseState[d]?.replace(',', '.')?.toDoubleOrNull() ?: hoM ?: 0.0
    }

    val missingParams = surfaceM2 == null || surfaceM2 <= 0.0 || hoM == null || hoM <= 0.0

    LaunchedEffect(missingParams, tigesInScope.size) {
        if (!askedParamsOnce && missingParams && tigesInScope.isNotEmpty()) {
            askedParamsOnce = true
            showParamDialog = true
        }
    }

    // Agrégation martelage (volumes, G, recettes) pour la vue courante
    val stats by produceState<MartelageStats?>(
        initialValue = null,
        tigesInScope,
        surfaceM2,
        viewScope,
        selectedEssenceCodes
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

        val classes = try {
            val list = forestryCalculator.diameterClasses()
            if (list.isNotEmpty()) list else (5..120 step 5).toList()
        } catch (_: Throwable) {
            (5..120 step 5).toList()
        }

        val byEssence = tigesInScope.groupBy { it.essenceCode }
        val perEssence = mutableListOf<PerEssenceStats>()

        var nTotal = 0
        var gTotal = 0.0
        var vTotal = 0.0
        var revenueTotal = 0.0
        val dmAll = mutableListOf<Double>()
        val hAll = mutableListOf<Double>()

        byEssence.forEach { (code, tigesEss) ->
            if (selectedEssenceCodes.isNotEmpty() && code !in selectedEssenceCodes) return@forEach

            val (rows, totals) = try {
                forestryCalculator.synthesisForEssence(code, classes, tigesEss)
            } catch (_: Throwable) {
                emptyList<ClassSynthesis>() to SynthesisTotals(0, null, null, null)
            }

            val vEss = totals.vTotal ?: 0.0
            val gEss = tigesEss.sumOf { forestryCalculator.computeG(it.diamCm) }
            val revEss = rows.sumOf { it.valueSumEur ?: 0.0 }
            val nEss = totals.nTotal

            nTotal += nEss
            gTotal += gEss
            vTotal += vEss
            revenueTotal += revEss
            totals.dmWeighted?.let { dmAll += it }
            totals.hMean?.let { hAll += it }

            val essenceName = essences.firstOrNull { it.code == code }?.name ?: code

            perEssence += PerEssenceStats(
                essenceCode = code,
                essenceName = essenceName,
                n = nEss,
                vTotal = vEss,
                vPerHa = if (surfaceHa > 0.0) vEss / surfaceHa else 0.0,
                gTotal = gEss,
                gPerHa = if (surfaceHa > 0.0) gEss / surfaceHa else 0.0,
                meanPricePerM3 = if (vEss > 0.0 && revEss > 0.0) revEss / vEss else null,
                revenueTotal = if (revEss > 0.0) revEss else null,
                revenuePerHa = if (revEss > 0.0 && surfaceHa > 0.0) revEss / surfaceHa else null
            )
        }

        if (nTotal == 0 && vTotal == 0.0 && gTotal == 0.0) {
            value = null
            return@produceState
        }

        val nPerHa = if (surfaceHa > 0.0) nTotal / surfaceHa else 0.0
        val gPerHa = if (surfaceHa > 0.0) gTotal / surfaceHa else 0.0
        val vPerHa = if (surfaceHa > 0.0) vTotal / surfaceHa else 0.0
        val revenuePerHa = if (surfaceHa > 0.0 && revenueTotal > 0.0) revenueTotal / surfaceHa else null

        val dm = dmAll.takeIf { it.isNotEmpty() }?.average()
        val hMean = hAll.takeIf { it.isNotEmpty() }?.average()

        value = MartelageStats(
            nTotal = nTotal,
            nPerHa = nPerHa,
            gTotal = gTotal,
            gPerHa = gPerHa,
            vTotal = vTotal,
            vPerHa = vPerHa,
            revenueTotal = if (revenueTotal > 0.0) revenueTotal else null,
            revenuePerHa = revenuePerHa,
            dm = dm,
            meanH = hMean,
            perEssence = perEssence.sortedBy { it.essenceName }
        )
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
            TopAppBar(
                title = { Text("Peuplement avant coupe") },
                navigationIcon = {
                    IconButton(onClick = {
                        playClickFeedback()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
            val contentScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
                    .padding(padding)
                    .padding(12.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Contexte du périmètre
                val scopeLabel = when (scope.uppercase(Locale.getDefault())) {
                    "PLACETTE" -> "Placette : synthèse limitée à cette placette"
                    "PARCELLE" -> "Parcelle : toutes les placettes de la parcelle"
                    "FOREST" -> "Projet / forêt : toutes les parcelles de ce projet"
                    else -> "Global : toutes les parcelles et placettes de l'application"
                }
                Text(scopeLabel, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(4.dp))

                // Sélecteur de vue locale (Placette / Parcelle / Global)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (placetteId != null) {
                        FilterChip(
                            selected = viewScope == MartelageViewScope.PLACETTE,
                            onClick = { viewScope = MartelageViewScope.PLACETTE },
                            label = { Text("Placette") }
                        )
                    }
                    if (parcelleId != null) {
                        FilterChip(
                            selected = viewScope == MartelageViewScope.PARCELLE,
                            onClick = { viewScope = MartelageViewScope.PARCELLE },
                            label = { Text("Parcelle") }
                        )
                    }
                    FilterChip(
                        selected = viewScope == MartelageViewScope.GLOBAL,
                        onClick = { viewScope = MartelageViewScope.GLOBAL },
                        label = { Text("Global") }
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
                        "Martelage – Synthèse",
                        style = MaterialTheme.typography.titleLarge
                    )
                    AssistChip(
                        onClick = {
                            playClickFeedback()
                            showParamPanel = !showParamPanel
                        },
                        label = { Text("Paramètres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null
                            )
                        }
                    )
                }

                if (parcellesInScope.isNotEmpty()) {
                    Text(
                        "Parcelles incluses : ${parcellesInScope.joinToString { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (tigesInScope.isEmpty()) {
                    Text(
                        "Aucune tige trouvée pour ce périmètre.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Sélection d'essences (multi)
                    Text("Essences disponibles", style = MaterialTheme.typography.titleMedium)
                    if (availableEssences.isEmpty()) {
                        Text(
                            "Aucune essence résineuse détectée dans ce périmètre.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableEssences.forEach { e ->
                                val selected = selectedEssenceCodes.contains(e.code)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        playClickFeedback()
                                        selectedEssenceCodes = if (selected) {
                                            (selectedEssenceCodes - e.code).ifEmpty { setOf(e.code) }
                                        } else {
                                            selectedEssenceCodes + e.code
                                        }
                                    },
                                    label = { Text(e.name) }
                                )
                            }
                        }
                    }

                    // Paramètres de calcul (mini menu animé)
                    AnimatedVisibility(
                        visible = showParamPanel,
                        enter = fadeIn(animationSpec = tween(durationMillis = 200)) + expandVertically(),
                        exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkVertically()
                    ) {
                        Card(
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
                                Text("Paramètres de calcul", style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = surfaceInput,
                                        onValueChange = { surfaceInput = it },
                                        label = { Text("Surface d'échantillon (m²)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = hoInput,
                                        onValueChange = { hoInput = it },
                                        label = { Text("Ho (m)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    "Les hauteurs utilisées pour le cubage viennent des écrans de diamètres (bouton « Hauteur ») ou, à défaut, des tables de hauteurs par défaut.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (missingParams) {
                                    Row(
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Renseigner Surface et Ho pour activer les tableaux",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { showParamDialog = true }) {
                                            Text("Renseigner")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!missingParams && stats != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val s = stats!!

                        // Carte Volume & Prix
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Volume & Prix", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Volume total : ${formatVolume(s.vTotal)} m³",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Volume / ha : ${formatVolume(s.vPerHa)} m³/ha",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Recette estimée : ${formatMoney(s.revenueTotal)}  ·  /ha : ${formatMoney(s.revenuePerHa)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Carte Surface terrière (S% affiché seulement si on aura G initial plus tard)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Surface terrière", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "G prélevée : ${formatG(s.gTotal)} m²",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "G / ha : ${formatG(s.gPerHa)} m²/ha",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Carte Densité & structure
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Densité & structure", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "N tiges : ${s.nTotal}  ·  N / ha : ${formatIntPerHa(s.nPerHa)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Dm moyen : ${formatDiameter(s.dm)} cm  ·  Hm : ${formatHeight(s.meanH)} m",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (s.perEssence.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Par essence", style = MaterialTheme.typography.titleMedium)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // En-tête du tableau
                                    Row {
                                        Text("Essence", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                                        Text("V (m³)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text("V/ha", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text("G (m²)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text("€/m³", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                        Text("Recette", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium)
                                    }

                                    Divider()

                                    for (row in s.perEssence) {
                                        val tint = essenceTintColor(row.essenceCode, essences)
                                        val bg = tint?.copy(alpha = 0.18f)
                                            ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.04f)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                        ) {
                                            Text(row.essenceName, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                                            Text(formatVolume(row.vTotal), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Text(formatVolume(row.vPerHa), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Text(formatG(row.gTotal), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Text(formatPrice(row.meanPricePerM3), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Text(formatMoney(row.revenueTotal), modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!missingParams && stats == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Impossible de calculer la synthèse avec les données actuelles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showParamDialog) {
        AlertDialog(
            onDismissRequest = { showParamDialog = false },
            title = { Text("Paramètres de calcul") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ces valeurs sont nécessaires pour calculer le tableau avant coupe.")
                    OutlinedTextField(
                        value = surfaceInput,
                        onValueChange = { surfaceInput = it },
                        label = { Text("Surface d'échantillon (m²)") }
                    )
                    OutlinedTextField(
                        value = hoInput,
                        onValueChange = { hoInput = it },
                        label = { Text("Ho (m)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showParamDialog = false }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { showParamDialog = false }) { Text("Annuler") }
            }
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
    val revenueTotal: Double?,
    val revenuePerHa: Double?,
    val dm: Double?,
    val meanH: Double?,
    val perEssence: List<PerEssenceStats>
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
private fun formatVolume(v: Double): String = String.format(Locale.getDefault(), "%.1f", v)

private fun formatG(g: Double): String = String.format(Locale.getDefault(), "%.2f", g)

private fun formatMoney(v: Double?): String =
    v?.let { String.format(Locale.getDefault(), "%.0f €", it) } ?: "—"

private fun formatPrice(p: Double?): String =
    p?.let { String.format(Locale.getDefault(), "%.0f", it) } ?: "—"

private fun formatIntPerHa(nPerHa: Double): String = String.format(Locale.getDefault(), "%.0f", nPerHa)

private fun formatDiameter(dm: Double?): String =
    dm?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—"

private fun formatHeight(h: Double?): String =
    h?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—"

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

