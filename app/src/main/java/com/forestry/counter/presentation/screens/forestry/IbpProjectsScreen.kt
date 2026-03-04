package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.IbpEvaluation
import com.forestry.counter.domain.model.IbpLevel
import com.forestry.counter.domain.repository.IbpRepository
import com.forestry.counter.domain.repository.PlacetteRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbpProjectsScreen(
    ibpRepository: IbpRepository,
    parcelleRepository: ParcelleRepository? = null,
    placetteRepository: PlacetteRepository? = null,
    onNavigateBack: () -> Unit,
    onOpenEvaluation: (parcelleId: String, placetteId: String, evalId: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val evaluations by ibpRepository.getAll().collectAsState(initial = emptyList())

    val parcelles by remember(parcelleRepository) {
        parcelleRepository?.getAllParcelles() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val parcelleNames = remember(parcelles) { parcelles.associate { it.id to it.name } }

    // Placette names: one-shot per unique parcelleId
    val uniqueParcelleIds = remember(evaluations) { evaluations.map { it.parcelleId }.distinct() }
    val placetteNamesState = remember { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(uniqueParcelleIds, placetteRepository) {
        if (placetteRepository == null) return@LaunchedEffect
        val combined = mutableMapOf<String, String>()
        for (pid in uniqueParcelleIds) {
            runCatching {
                placetteRepository.getPlacettesByParcelle(pid).first()
            }.getOrElse { emptyList() }
                .forEach { p -> combined[p.id] = p.name ?: "" }
        }
        placetteNamesState.value = combined
    }
    val placetteNames by placetteNamesState

    var showDeleteDialog by remember { mutableStateOf<IbpEvaluation?>(null) }

    val grouped = remember(evaluations) {
        evaluations
            .sortedByDescending { it.observationDate }
            .groupBy { it.parcelleId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ibp_projects_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (evaluations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiNature,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        stringResource(R.string.ibp_projects_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.ibp_projects_empty_hint),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                grouped.forEach { (parcelleId, evals) ->
                    item(key = "header_$parcelleId") {
                        val parcelleName = parcelleNames[parcelleId]?.takeIf { it.isNotBlank() }
                            ?: parcelleId.take(8)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Forest,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF2E7D32)
                            )
                            Text(
                                parcelleName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                "${evals.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(evals, key = { it.id }) { eval ->
                        IbpProjectCard(
                            eval = eval,
                            placetteName = placetteNames[eval.placetteId]?.takeIf { it.isNotBlank() }
                                ?: eval.placetteId.take(8),
                            dateFormat = dateFormat,
                            onClick = { onOpenEvaluation(eval.parcelleId, eval.placetteId, eval.id) },
                            onDelete = { showDeleteDialog = eval }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { eval ->
        AppMiniDialogForIbp(
            onDismissRequest = { showDeleteDialog = null },
            onConfirm = {
                showDeleteDialog = null
                scope.launch { ibpRepository.delete(eval.id) }
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
private fun AppMiniDialogForIbp(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
        title = { Text(stringResource(R.string.ibp_delete_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun IbpProjectCard(
    eval: IbpEvaluation,
    placetteName: String,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val score = eval.scoreTotal
    val isComplete = score >= 0
    val level = IbpLevel.fromScore(score)
    val levelColor = if (!isComplete) Color(0xFF78909C)
    else when (level) {
        IbpLevel.VERY_LOW  -> Color(0xFFC62828)
        IbpLevel.LOW       -> Color(0xFFE65100)
        IbpLevel.MEDIUM    -> Color(0xFFF57C00)
        IbpLevel.GOOD      -> Color(0xFF558B2F)
        IbpLevel.VERY_GOOD -> Color(0xFF1B5E20)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = levelColor.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isComplete) "$score" else "–",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                    Text(
                        if (isComplete) "/ 20" else "NC",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor.copy(alpha = 0.7f)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    placetteName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    dateFormat.format(Date(eval.observationDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (eval.evaluatorName.isNotBlank()) {
                    Text(
                        eval.evaluatorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                if (eval.globalNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        eval.globalNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}
