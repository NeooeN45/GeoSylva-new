package com.forestry.counter.presentation.screens.forestry

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.R
import com.forestry.counter.domain.model.*
import com.forestry.counter.domain.repository.IbpRepository
import com.forestry.counter.domain.repository.PlacetteRepository
import com.forestry.counter.domain.usecase.export.IbpPdfExporter
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbpEvaluationScreen(
    parcelleId: String,
    placetteId: String,
    ibpRepository: IbpRepository,
    placetteRepository: PlacetteRepository? = null,
    userPreferences: com.forestry.counter.data.preferences.UserPreferencesManager? = null,
    evaluationId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic  = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Load existing evaluation if editing
    val existingFlow = remember(ibpRepository, evaluationId) {
        if (evaluationId != null) ibpRepository.getById(evaluationId)
        else kotlinx.coroutines.flow.flowOf(null)
    }
    val existing by existingFlow.collectAsState(initial = null)

    val ibpAnswersSaver = remember {
        val key = "ibp_answers_json"
        val safeJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serializer = IbpAnswers.serializer()
        mapSaver<IbpAnswers>(
            save = { mapOf(key to safeJson.encodeToString(serializer, it)) },
            restore = { map -> runCatching { safeJson.decodeFromString(serializer, map[key] as String) }.getOrElse { IbpAnswers() } }
        )
    }
    var answers by rememberSaveable(stateSaver = ibpAnswersSaver) { mutableStateOf(IbpAnswers()) }
    var evaluatorName by rememberSaveable { mutableStateOf("") }
    var globalNote by rememberSaveable { mutableStateOf("") }
    var observationDate by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var initialized by rememberSaveable { mutableStateOf(false) }

    val placetteFlow = remember(placetteRepository, placetteId) {
        placetteRepository?.getPlacetteById(placetteId) ?: kotlinx.coroutines.flow.flowOf(null)
    }
    val placette by placetteFlow.collectAsState(initial = null)
    val placetteLabel = placette?.name?.takeIf { it.isNotBlank() } ?: placetteId.take(8)

    LaunchedEffect(existing) {
        if (!initialized && existing != null) {
            answers = existing!!.answers
            evaluatorName = existing!!.evaluatorName
            globalNote = existing!!.globalNote
            observationDate = existing!!.observationDate
            initialized = true
        } else if (!initialized && evaluationId == null) {
            initialized = true
        }
    }

    var showResultDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val ibpOnboardingSeen by remember(userPreferences) {
        userPreferences?.ibpOnboardingSeen ?: kotlinx.coroutines.flow.flowOf(true)
    }.collectAsState(initial = true)
    var onboardingStep by remember { mutableStateOf(0) }
    val showOnboarding = !ibpOnboardingSeen && onboardingStep < 3

    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val eval = IbpEvaluation(
                    id = evaluationId ?: UUID.randomUUID().toString(),
                    placetteId = placetteId,
                    parcelleId = parcelleId,
                    observationDate = observationDate,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    evaluatorName = evaluatorName,
                    answers = answers,
                    globalNote = globalNote
                )
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        IbpPdfExporter.export(context, eval, out, placetteLabel)
                    }
                    snackbar.showSnackbar(context.getString(R.string.pdf_exported))
                }.onFailure {
                    snackbar.showSnackbar(context.getString(R.string.export_error))
                }
            }
        }
    }

    fun saveEvaluation() {
        scope.launch {
            val id = evaluationId ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val eval = IbpEvaluation(
                id = id,
                placetteId = placetteId,
                parcelleId = parcelleId,
                observationDate = observationDate,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                evaluatorName = evaluatorName,
                answers = answers,
                globalNote = globalNote
            )
            ibpRepository.save(eval)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            snackbar.showSnackbar(context.getString(R.string.ibp_saved))
        }
    }

    val savedAnswers = existing?.answers ?: IbpAnswers()
    val savedName = existing?.evaluatorName ?: ""
    val savedNote = existing?.globalNote ?: ""
    val hasUnsavedChanges = initialized && (answers != savedAnswers || evaluatorName != savedName || globalNote != savedNote)

    BackHandler(enabled = hasUnsavedChanges) { showUnsavedDialog = true }

    val scoreTotal = answers.scoreTotal
    val scoreA = answers.scoreA
    val scoreB = answers.scoreB
    val level = IbpLevel.fromScore(scoreTotal)
    val levelColor = ibpLevelColor(level)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ibp_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (hasUnsavedChanges) showUnsavedDialog = true else onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (answers.isComplete) {
                        IconButton(onClick = { showResultDialog = true }) {
                            Icon(Icons.Default.Assessment, contentDescription = stringResource(R.string.ibp_result))
                        }
                    }
                    IconButton(onClick = {
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(observationDate))
                        exportPdfLauncher.launch("IBP_${dateStr}.pdf")
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.ibp_export_pdf))
                    }
                    IconButton(onClick = { saveEvaluation() }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                    if (evaluationId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.ibp_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // ── Score Header ──────────────────────────────────────────
            IbpScoreHeader(
                scoreTotal = scoreTotal,
                scoreA = scoreA,
                scoreB = scoreB,
                answeredCount = answers.answeredCount,
                levelColor = levelColor,
                level = level
            )

            // ── Meta (evaluator + date) ────────────────────────────
            IbpMetaSection(
                evaluatorName = evaluatorName,
                onEvaluatorChange = { evaluatorName = it },
                observationDate = observationDate,
                onDateClick = { showDatePicker = true }
            )

            // ── Group A ─────────────────────────────────────────────
            IbpGroupHeader(
                label = stringResource(R.string.ibp_group_a_title),
                subtitle = stringResource(R.string.ibp_group_a_subtitle),
                score = scoreA,
                maxScore = 14,
                color = Color(0xFF2E7D32)
            )
            IbpCriterionId.GROUP_A.forEach { cid ->
                IbpCriterionCard(
                    criterionId = cid,
                    currentValue = answers.get(cid),
                    onAnswer = { v -> answers = answers.set(cid, v) }
                )
            }

            // ── Group B ─────────────────────────────────────────────
            IbpGroupHeader(
                label = stringResource(R.string.ibp_group_b_title),
                subtitle = stringResource(R.string.ibp_group_b_subtitle),
                score = scoreB,
                maxScore = 6,
                color = Color(0xFF1565C0)
            )
            IbpCriterionId.GROUP_B.forEach { cid ->
                IbpCriterionCard(
                    criterionId = cid,
                    currentValue = answers.get(cid),
                    onAnswer = { v -> answers = answers.set(cid, v) }
                )
            }

            // ── Global note ──────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.ibp_global_note), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = globalNote,
                    onValueChange = { globalNote = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                    placeholder = { Text(stringResource(R.string.ibp_global_note_hint), style = MaterialTheme.typography.bodySmall) }
                )
            }

            // ── Result card if complete ───────────────────────────
            if (answers.isComplete) {
                IbpResultCard(
                    level = level,
                    scoreTotal = scoreTotal,
                    levelColor = levelColor
                )
            }
        }
    }

    if (showOnboarding) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.EmojiNature, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp)) },
            title = {
                Text(stringResource(when (onboardingStep) {
                    0 -> R.string.ibp_onboarding_title_1
                    1 -> R.string.ibp_onboarding_title_2
                    else -> R.string.ibp_onboarding_title_3
                }))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(when (onboardingStep) {
                        0 -> R.string.ibp_onboarding_body_1
                        1 -> R.string.ibp_onboarding_body_2
                        else -> R.string.ibp_onboarding_body_3
                    }))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        repeat(3) { i ->
                            Surface(
                                modifier = Modifier.size(if (i == onboardingStep) 10.dp else 6.dp),
                                shape = CircleShape,
                                color = if (i == onboardingStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ) {}
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (onboardingStep < 2) {
                        onboardingStep++
                    } else {
                        scope.launch { userPreferences?.setIbpOnboardingSeen() }
                        onboardingStep = 3
                    }
                }) {
                    Text(if (onboardingStep < 2) stringResource(R.string.ibp_onboarding_next) else stringResource(R.string.ibp_onboarding_start))
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = observationDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { observationDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.ibp_delete)) },
            text = { Text(stringResource(R.string.ibp_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        if (evaluationId != null) ibpRepository.delete(evaluationId)
                        onNavigateBack()
                    }
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.ibp_unsaved_title)) },
            text = { Text(stringResource(R.string.ibp_unsaved_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    saveEvaluation()
                    onNavigateBack()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onNavigateBack()
                }) { Text(stringResource(R.string.discard)) }
            }
        )
    }

    if (showResultDialog) {
        IbpResultDialog(
            answers = answers,
            scoreTotal = scoreTotal,
            scoreA = scoreA,
            scoreB = scoreB,
            level = level,
            onDismiss = { showResultDialog = false }
        )
    }
}

/* ─────────────── Score Header ─────────────────────────────────── */
@Composable
private fun IbpScoreHeader(
    scoreTotal: Int,
    scoreA: Int,
    scoreB: Int,
    answeredCount: Int,
    levelColor: Color,
    level: IbpLevel
) {
    val progress = if (scoreTotal >= 0) scoreTotal / 20f else 0f
    val animProgress by animateFloatAsState(progress, animationSpec = tween(600), label = "ibpProg")

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.ibp_score_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (scoreTotal >= 0) "$scoreTotal" else "—",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = levelColor
                        )
                        Text(" / 20", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    IbpLevelBadge(level = level, color = levelColor)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$answeredCount / 10 ${stringResource(R.string.ibp_answered)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Progress bar
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                if (animProgress > 0f) {
                    Box(modifier = Modifier.fillMaxWidth(animProgress).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF66BB6A), levelColor))))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                ScorePill(label = "A", score = scoreA, max = 14, color = Color(0xFF2E7D32))
                ScorePill(label = "B", score = scoreB, max = 6, color = Color(0xFF1565C0))
            }
        }
    }
}

@Composable
private fun ScorePill(label: String, score: Int, max: Int, color: Color) {
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text("$label : ${if (score >= 0) "$score" else "—"} / $max", style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ─────────────── Level Badge ──────────────────────────────────── */
@Composable
fun IbpLevelBadge(level: IbpLevel, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Text(
            ibpLevelLabel(level),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ibpLevelLabel(level: IbpLevel): String = when (level) {
    IbpLevel.VERY_LOW  -> stringResource(R.string.ibp_level_very_low)
    IbpLevel.LOW       -> stringResource(R.string.ibp_level_low)
    IbpLevel.MEDIUM    -> stringResource(R.string.ibp_level_medium)
    IbpLevel.GOOD      -> stringResource(R.string.ibp_level_good)
    IbpLevel.VERY_GOOD -> stringResource(R.string.ibp_level_very_good)
}

fun ibpLevelColor(level: IbpLevel): Color = when (level) {
    IbpLevel.VERY_LOW  -> Color(0xFFC62828)
    IbpLevel.LOW       -> Color(0xFFE65100)
    IbpLevel.MEDIUM    -> Color(0xFFF9A825)
    IbpLevel.GOOD      -> Color(0xFF2E7D32)
    IbpLevel.VERY_GOOD -> Color(0xFF1565C0)
}

/* ─────────────── Meta Section ─────────────────────────────────── */
@Composable
private fun IbpMetaSection(
    evaluatorName: String,
    onEvaluatorChange: (String) -> Unit,
    observationDate: Long,
    onDateClick: () -> Unit = {}
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(observationDate))
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = evaluatorName,
            onValueChange = onEvaluatorChange,
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.ibp_evaluator), style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Box(modifier = Modifier.width(140.dp).clickable { onDateClick() }) {
            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.ibp_date), style = MaterialTheme.typography.bodySmall) },
                readOnly = true,
                enabled = false,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/* ─────────────── Group Header ─────────────────────────────────── */
@Composable
private fun IbpGroupHeader(label: String, subtitle: String, score: Int, maxScore: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp, 40.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(color = color.copy(alpha = .15f), shape = RoundedCornerShape(10.dp)) {
            Text(
                "${if (score >= 0) score else "—"} / $maxScore",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = color.copy(alpha = .2f))
}

/* ─────────────── Criterion Card ────────────────────────────────── */
@Composable
private fun IbpCriterionCard(
    criterionId: IbpCriterionId,
    currentValue: Int,
    onAnswer: (Int) -> Unit
) {
    val isAnswered = currentValue >= 0

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAnswered)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isAnswered) 2.dp else 1.dp),
        border = if (isAnswered) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: code badge + title
            Row(verticalAlignment = Alignment.Top) {
                // Code badge
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        criterionId.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(ibpCriterionTitle(criterionId), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(ibpCriterionSubtitle(criterionId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isAnswered) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("$currentValue", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Illustration + Options side by side (large screen) or stacked
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Illustration
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IbpCriterionIllustration(criterionId = criterionId, modifier = Modifier.size(76.dp))
                }

                Spacer(Modifier.width(14.dp))

                // QCM options
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ibpCriterionOptions(criterionId).forEachIndexed { score, option ->
                        IbpOptionRow(
                            score = score,
                            label = option,
                            selected = currentValue == score,
                            onSelect = { onAnswer(score) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IbpOptionRow(score: Int, label: String, selected: Boolean, onSelect: () -> Unit) {
    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "ibpOptionBg"
    )
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f),
        label = "ibpOptionBorder"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = when (score) {
                0 -> Color(0xFFC62828)
                1 -> Color(0xFFF9A825)
                else -> Color(0xFF2E7D32)
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                "$score pt${if (score > 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
}

/* ─────────────── Result Card (inline) ─────────────────────────── */
@Composable
private fun IbpResultCard(level: IbpLevel, scoreTotal: Int, levelColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = .10f)),
        border = BorderStroke(1.5.dp, levelColor.copy(alpha = .4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EmojiNature, contentDescription = null, tint = levelColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(ibpLevelLabel(level), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = levelColor)
            Spacer(Modifier.height(6.dp))
            Text(ibpLevelComment(level, scoreTotal), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = levelColor.copy(alpha = .3f))
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.ibp_recommendations), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            ibpLevelRecommendations(level).forEach { rec ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text("• ", color = levelColor, fontWeight = FontWeight.Bold)
                    Text(rec, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/* ─────────────── Result Dialog ─────────────────────────────────── */
@Composable
private fun IbpResultDialog(
    answers: IbpAnswers,
    scoreTotal: Int,
    scoreA: Int,
    scoreB: Int,
    level: IbpLevel,
    onDismiss: () -> Unit
) {
    val levelColor = ibpLevelColor(level)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.EmojiNature, contentDescription = null, tint = levelColor, modifier = Modifier.size(36.dp)) },
        title = { Text(stringResource(R.string.ibp_result_title), textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Big score
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$scoreTotal / 20", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = levelColor)
                        IbpLevelBadge(level = level, color = levelColor)
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Score breakdown
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$scoreA / 14", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(stringResource(R.string.ibp_group_a_short), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$scoreB / 6", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Text(stringResource(R.string.ibp_group_b_short), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0))
                    }
                }
                HorizontalDivider()
                IbpRadarChart(answers = answers, color = levelColor, modifier = Modifier.fillMaxWidth().height(200.dp))
                HorizontalDivider()
                Text(ibpLevelComment(level, scoreTotal), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
                HorizontalDivider()
                Text(stringResource(R.string.ibp_recommendations), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                ibpLevelRecommendations(level).forEach { rec ->
                    Row {
                        Text("• ", color = levelColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(rec, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        }
    )
}

/* ─────────────── Criterion data: titles ─────────────────────────── */
@Composable
fun ibpCriterionTitle(id: IbpCriterionId): String = when (id) {
    IbpCriterionId.E1  -> stringResource(R.string.ibp_e1_title)
    IbpCriterionId.E2  -> stringResource(R.string.ibp_e2_title)
    IbpCriterionId.GB  -> stringResource(R.string.ibp_gb_title)
    IbpCriterionId.BMS -> stringResource(R.string.ibp_bms_title)
    IbpCriterionId.BMC -> stringResource(R.string.ibp_bmc_title)
    IbpCriterionId.DMH -> stringResource(R.string.ibp_dmh_title)
    IbpCriterionId.VS  -> stringResource(R.string.ibp_vs_title)
    IbpCriterionId.CF  -> stringResource(R.string.ibp_cf_title)
    IbpCriterionId.CO  -> stringResource(R.string.ibp_co_title)
    IbpCriterionId.HC  -> stringResource(R.string.ibp_hc_title)
}

@Composable
fun ibpCriterionSubtitle(id: IbpCriterionId): String = when (id) {
    IbpCriterionId.E1  -> stringResource(R.string.ibp_e1_subtitle)
    IbpCriterionId.E2  -> stringResource(R.string.ibp_e2_subtitle)
    IbpCriterionId.GB  -> stringResource(R.string.ibp_gb_subtitle)
    IbpCriterionId.BMS -> stringResource(R.string.ibp_bms_subtitle)
    IbpCriterionId.BMC -> stringResource(R.string.ibp_bmc_subtitle)
    IbpCriterionId.DMH -> stringResource(R.string.ibp_dmh_subtitle)
    IbpCriterionId.VS  -> stringResource(R.string.ibp_vs_subtitle)
    IbpCriterionId.CF  -> stringResource(R.string.ibp_cf_subtitle)
    IbpCriterionId.CO  -> stringResource(R.string.ibp_co_subtitle)
    IbpCriterionId.HC  -> stringResource(R.string.ibp_hc_subtitle)
}

@Composable
fun ibpCriterionOptions(id: IbpCriterionId): List<String> = when (id) {
    IbpCriterionId.E1  -> listOf(stringResource(R.string.ibp_e1_o0), stringResource(R.string.ibp_e1_o1), stringResource(R.string.ibp_e1_o2))
    IbpCriterionId.E2  -> listOf(stringResource(R.string.ibp_e2_o0), stringResource(R.string.ibp_e2_o1), stringResource(R.string.ibp_e2_o2))
    IbpCriterionId.GB  -> listOf(stringResource(R.string.ibp_gb_o0), stringResource(R.string.ibp_gb_o1), stringResource(R.string.ibp_gb_o2))
    IbpCriterionId.BMS -> listOf(stringResource(R.string.ibp_bms_o0), stringResource(R.string.ibp_bms_o1), stringResource(R.string.ibp_bms_o2))
    IbpCriterionId.BMC -> listOf(stringResource(R.string.ibp_bmc_o0), stringResource(R.string.ibp_bmc_o1), stringResource(R.string.ibp_bmc_o2))
    IbpCriterionId.DMH -> listOf(stringResource(R.string.ibp_dmh_o0), stringResource(R.string.ibp_dmh_o1), stringResource(R.string.ibp_dmh_o2))
    IbpCriterionId.VS  -> listOf(stringResource(R.string.ibp_vs_o0), stringResource(R.string.ibp_vs_o1), stringResource(R.string.ibp_vs_o2))
    IbpCriterionId.CF  -> listOf(stringResource(R.string.ibp_cf_o0), stringResource(R.string.ibp_cf_o1), stringResource(R.string.ibp_cf_o2))
    IbpCriterionId.CO  -> listOf(stringResource(R.string.ibp_co_o0), stringResource(R.string.ibp_co_o1), stringResource(R.string.ibp_co_o2))
    IbpCriterionId.HC  -> listOf(stringResource(R.string.ibp_hc_o0), stringResource(R.string.ibp_hc_o1), stringResource(R.string.ibp_hc_o2))
}

@Composable
fun ibpLevelComment(level: IbpLevel, score: Int): String = when (level) {
    IbpLevel.VERY_LOW  -> stringResource(R.string.ibp_comment_very_low)
    IbpLevel.LOW       -> stringResource(R.string.ibp_comment_low)
    IbpLevel.MEDIUM    -> stringResource(R.string.ibp_comment_medium)
    IbpLevel.GOOD      -> stringResource(R.string.ibp_comment_good)
    IbpLevel.VERY_GOOD -> stringResource(R.string.ibp_comment_very_good)
}

/* ─────────────── IBP card in MartelageScreen ────────────────── */
@Composable
fun IbpMartelageCard(
    ibpEval: com.forestry.counter.domain.model.IbpEvaluation?,
    parcelleId: String?,
    placetteId: String?,
    onNavigateToIbp: ((parcelleId: String, placetteId: String) -> Unit)? = null,
    onNavigateToHistory: ((parcelleId: String, placetteId: String?) -> Unit)? = null,
    evaluationCount: Int = 0
) {
    val canNavigate = onNavigateToIbp != null && parcelleId != null && placetteId != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EmojiNature, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ibp_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (ibpEval != null && ibpEval.answers.isComplete) {
                    val level = ibpEval.levelColor()
                    val color = ibpLevelColor(level)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${ibpEval.scoreTotal} / 20", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
                        IbpLevelBadge(level = level, color = color)
                    }
                } else {
                    Text(stringResource(R.string.ibp_no_evaluation), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (canNavigate) {
                    TextButton(onClick = { onNavigateToIbp!!(parcelleId!!, placetteId!!) }) {
                        Text(if (ibpEval != null) stringResource(R.string.ibp_edit) else stringResource(R.string.ibp_start))
                    }
                }
                if (evaluationCount >= 2 && onNavigateToHistory != null && parcelleId != null) {
                    TextButton(onClick = { onNavigateToHistory(parcelleId, placetteId) }) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ibp_history_title), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun ibpLevelRecommendations(level: IbpLevel): List<String> = when (level) {
    IbpLevel.VERY_LOW  -> listOf(
        stringResource(R.string.ibp_rec_vl_1),
        stringResource(R.string.ibp_rec_vl_2),
        stringResource(R.string.ibp_rec_vl_3)
    )
    IbpLevel.LOW       -> listOf(
        stringResource(R.string.ibp_rec_l_1),
        stringResource(R.string.ibp_rec_l_2),
        stringResource(R.string.ibp_rec_l_3)
    )
    IbpLevel.MEDIUM    -> listOf(
        stringResource(R.string.ibp_rec_m_1),
        stringResource(R.string.ibp_rec_m_2),
        stringResource(R.string.ibp_rec_m_3)
    )
    IbpLevel.GOOD      -> listOf(
        stringResource(R.string.ibp_rec_g_1),
        stringResource(R.string.ibp_rec_g_2)
    )
    IbpLevel.VERY_GOOD -> listOf(
        stringResource(R.string.ibp_rec_vg_1),
        stringResource(R.string.ibp_rec_vg_2)
    )
}

/* ─────────────── Radar Chart ────────────────────────────────────── */
@Composable
fun IbpRadarChart(
    answers: IbpAnswers,
    color: Color,
    modifier: Modifier = Modifier
) {
    val criteria = IbpCriterionId.ALL
    val n = criteria.size
    val values = criteria.map { id -> answers.get(id).coerceAtLeast(0).toFloat() / 2f }

    val gridColor = color.copy(alpha = 0.15f)
    val fillColor = color.copy(alpha = 0.25f)
    val strokeColor = color

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * 0.72f
        val angleStep = (2 * Math.PI / n).toFloat()
        val startAngle = (-Math.PI / 2).toFloat()

        fun axisPoint(index: Int, r: Float): Offset {
            val angle = startAngle + index * angleStep
            return Offset(cx + r * kotlin.math.cos(angle), cy + r * kotlin.math.sin(angle))
        }

        // Draw grid rings (0.5 and 1.0)
        listOf(0.5f, 1.0f).forEach { fraction ->
            val gridPath = Path()
            for (i in 0 until n) {
                val pt = axisPoint(i, radius * fraction)
                if (i == 0) gridPath.moveTo(pt.x, pt.y) else gridPath.lineTo(pt.x, pt.y)
            }
            gridPath.close()
            drawPath(gridPath, color = gridColor, style = Stroke(width = 1.dp.toPx()))
        }

        // Draw axes
        for (i in 0 until n) {
            val outer = axisPoint(i, radius)
            drawLine(color = gridColor, start = Offset(cx, cy), end = outer, strokeWidth = 1.dp.toPx())
        }

        // Draw filled polygon
        val dataPath = Path()
        for (i in 0 until n) {
            val pt = axisPoint(i, radius * values[i])
            if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
        }
        dataPath.close()
        drawPath(dataPath, color = fillColor)
        drawPath(dataPath, color = strokeColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Draw dots at each vertex
        for (i in 0 until n) {
            val pt = axisPoint(i, radius * values[i])
            drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = pt)
        }
    }
}
