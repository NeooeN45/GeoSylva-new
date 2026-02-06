package com.forestry.counter.presentation.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.data.parameters.ParameterDefaults
import com.forestry.counter.domain.calculation.PriceEntry
import com.forestry.counter.domain.model.ParameterItem
import com.forestry.counter.domain.parameters.ParameterKeys
import com.forestry.counter.domain.repository.ParameterRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private data class EditablePriceRow(
    val essence: String,
    val product: String,
    val min: String,
    val max: String,
    val eurPerM3: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceTablesEditorScreen(
    parameterRepository: ParameterRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val json = remember { Json { ignoreUnknownKeys = true } }

    var showResetDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val rows = remember { mutableStateListOf<EditablePriceRow>() }

    fun rowsToEntriesOrError(): Pair<List<PriceEntry>?, String?> {
        val result = mutableListOf<PriceEntry>()
        rows.forEachIndexed { idx, r ->
            val essence = r.essence.trim()
            val product = r.product.trim()
            val min = r.min.trim().toIntOrNull()
            val max = r.max.trim().toIntOrNull()
            val eur = r.eurPerM3.trim().replace(',', '.').toDoubleOrNull()

            if (essence.isBlank()) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "essence")
            if (product.isBlank()) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "product")
            if (min == null) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "min")
            if (max == null) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "max")
            if (eur == null) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "eurPerM3")
            if (min > max) return null to context.getString(R.string.price_tables_invalid_row_format, idx + 1, "min>max")

            result += PriceEntry(
                essence = essence,
                product = product,
                min = min,
                max = max,
                eurPerM3 = eur
            )
        }
        return result to null
    }

    suspend fun loadFromJsonString(valueJson: String?) {
        rows.clear()
        val list = runCatching {
            if (valueJson.isNullOrBlank()) emptyList() else json.decodeFromString<List<PriceEntry>>(valueJson)
        }.getOrElse { emptyList() }

        if (list.isEmpty()) {
            rows += EditablePriceRow(essence = "*", product = "*", min = "0", max = "999", eurPerM3 = "0")
        } else {
            list.forEach { e ->
                rows += EditablePriceRow(
                    essence = e.essence,
                    product = e.product,
                    min = e.min.toString(),
                    max = e.max.toString(),
                    eurPerM3 = e.eurPerM3.toString()
                )
            }
        }
    }

    LaunchedEffect(parameterRepository) {
        isLoading = true
        val item = parameterRepository.getParameter(ParameterKeys.PRIX_MARCHE).first()
        loadFromJsonString(item?.valueJson)
        isLoading = false
    }

    BackHandler(enabled = true) {
        onNavigateBack()
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val defaultJson = ParameterDefaults.prixMarcheJson
                            parameterRepository.setParameter(ParameterItem(ParameterKeys.PRIX_MARCHE, defaultJson))
                            loadFromJsonString(defaultJson)
                            snackbarHostState.showSnackbar(context.getString(R.string.price_tables_saved))
                            showResetDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                FilledTonalButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.reset_price_tables_title)) },
            text = { Text(stringResource(R.string.reset_price_tables_desc)) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.price_tables_editor_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val (entries, err) = rowsToEntriesOrError()
                                if (entries == null) {
                                    snackbarHostState.showSnackbar(err ?: "Erreur")
                                    return@launch
                                }
                                val outJson = json.encodeToString(entries)
                                parameterRepository.setParameter(ParameterItem(ParameterKeys.PRIX_MARCHE, outJson))
                                snackbarHostState.showSnackbar(context.getString(R.string.price_tables_saved))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.loading), style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        rows.add(
                            EditablePriceRow(
                                essence = "*",
                                product = "*",
                                min = "0",
                                max = "999",
                                eurPerM3 = "0"
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(stringResource(R.string.add_line))
                }
            }

            rows.forEachIndexed { index, row ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = row.essence,
                            onValueChange = { v ->
                                rows[index] = row.copy(essence = v)
                            },
                            label = { Text(stringResource(R.string.essence)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = row.product,
                            onValueChange = { v ->
                                rows[index] = row.copy(product = v)
                            },
                            label = { Text(stringResource(R.string.product)) },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (rows.size > 1) rows.removeAt(index)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = row.min,
                            onValueChange = { v -> rows[index] = row.copy(min = v) },
                            label = { Text(stringResource(R.string.min)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = row.max,
                            onValueChange = { v -> rows[index] = row.copy(max = v) },
                            label = { Text(stringResource(R.string.max)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = row.eurPerM3,
                            onValueChange = { v -> rows[index] = row.copy(eurPerM3 = v) },
                            label = { Text(stringResource(R.string.martelage_table_eur_per_m3)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
