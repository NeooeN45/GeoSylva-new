package com.forestry.counter.presentation.screens.forestry

import com.forestry.counter.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Wizard de création de parcelle — spec GEOSYLVA-003 §29.7.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateParcelleWizard(
    viewModel: CreateParcelleViewModel,
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    if (isSaved) {
        onCreated()
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_parcelle_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Annuler")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.create_wizard_section_info), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.create_parcelle_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.surfaceHa?.toString() ?: "",
                onValueChange = viewModel::updateSurfaceHa,
                label = { Text(stringResource(R.string.create_parcelle_surface_ha)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.slopePct?.toString() ?: "",
                onValueChange = viewModel::updateSlopePct,
                label = { Text(stringResource(R.string.create_parcelle_slope_pct)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.altitudeM?.toString() ?: "",
                onValueChange = viewModel::updateAltitudeM,
                label = { Text(stringResource(R.string.create_parcelle_altitude_m)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.remarks ?: "",
                onValueChange = viewModel::updateRemarks,
                label = { Text(stringResource(R.string.create_forest_remarks)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = viewModel::save,
                    enabled = form.isValid,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}

/**
 * Wizard de création de placette — spec GEOSYLVA-003 §29.8.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlacetteWizard(
    viewModel: CreatePlacetteViewModel,
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    if (isSaved) {
        onCreated()
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_placette_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Annuler")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.create_wizard_section_info), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = form.name ?: "",
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.create_placette_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.type ?: "",
                onValueChange = viewModel::updateType,
                label = { Text(stringResource(R.string.create_parcelle_placette_type_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.rayonM?.toString() ?: "",
                onValueChange = viewModel::updateRayonM,
                label = { Text(stringResource(R.string.create_placette_radius_m)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = viewModel::save,
                    enabled = form.isValid,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}
