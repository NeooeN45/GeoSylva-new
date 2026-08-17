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
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Wizard de création de forêt — 3 étapes.
 *
 * Étape 1 : Identité (nom, département)
 * Étape 2 : Propriétaire (nom, email, gestionnaire)
 * Étape 3 : Gestion (type, objectif, PSG, remarques)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForestWizard(
    viewModel: CreateForestViewModel,
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    if (isSaved) {
        onCreated()
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_forest_title, currentStep + 1, totalSteps)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
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
            LinearProgressIndicator(
                progress = { (currentStep + 1f) / totalSteps },
                modifier = Modifier.fillMaxWidth(),
            )

            when (currentStep) {
                0 -> StepIdentity(
                    form = form,
                    onNomChange = viewModel::updateNom,
                    onDepartementChange = viewModel::updateDepartement,
                )
                1 -> StepOwner(
                    form = form,
                    onProprietaireNomChange = viewModel::updateProprietaireNom,
                    onProprietaireEmailChange = viewModel::updateProprietaireEmail,
                    onGestionnaireNomChange = viewModel::updateGestionnaireNom,
                )
                2 -> StepManagement(
                    form = form,
                    onTypeForetChange = viewModel::updateTypeForet,
                    onObjectifGestionChange = viewModel::updateObjectifGestion,
                    onPsgNumeroChange = viewModel::updatePsgNumero,
                    onRemarquesChange = viewModel::updateRemarques,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text(stringResource(R.string.previous))
                    }
                } else {
                    TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.cancel)) }
                }

                if (currentStep < totalSteps - 1) {
                    val canAdvance = when (currentStep) {
                        0 -> form.step1Valid
                        1 -> form.step2Valid
                        else -> form.step3Valid
                    }
                    Button(
                        onClick = { currentStep++ },
                        enabled = canAdvance,
                    ) {
                        Text(stringResource(R.string.next))
                        Icon(Icons.Filled.NavigateNext, contentDescription = null)
                    }
                } else {
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
}

@Composable
private fun StepTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun StepIdentity(
    form: CreateForestFormState,
    onNomChange: (String) -> Unit,
    onDepartementChange: (String) -> Unit,
) {
    StepTitle(stringResource(R.string.create_forest_step_identity))
    OutlinedTextField(
        value = form.nom,
        onValueChange = onNomChange,
        label = { Text(stringResource(R.string.create_forest_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.departement ?: "",
        onValueChange = onDepartementChange,
        label = { Text(stringResource(R.string.create_forest_department)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepOwner(
    form: CreateForestFormState,
    onProprietaireNomChange: (String) -> Unit,
    onProprietaireEmailChange: (String) -> Unit,
    onGestionnaireNomChange: (String) -> Unit,
) {
    StepTitle(stringResource(R.string.create_forest_step_owner))
    OutlinedTextField(
        value = form.proprietaireNom,
        onValueChange = onProprietaireNomChange,
        label = { Text(stringResource(R.string.create_forest_owner_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.proprietaireEmail ?: "",
        onValueChange = onProprietaireEmailChange,
        label = { Text(stringResource(R.string.create_forest_owner_email)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.gestionnaireNom ?: "",
        onValueChange = onGestionnaireNomChange,
        label = { Text(stringResource(R.string.create_forest_manager_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepManagement(
    form: CreateForestFormState,
    onTypeForetChange: (String) -> Unit,
    onObjectifGestionChange: (String) -> Unit,
    onPsgNumeroChange: (String) -> Unit,
    onRemarquesChange: (String) -> Unit,
) {
    StepTitle(stringResource(R.string.create_forest_step_management))
    OutlinedTextField(
        value = form.typeForet ?: "",
        onValueChange = onTypeForetChange,
        label = { Text(stringResource(R.string.create_forest_type)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.objectifGestion ?: "",
        onValueChange = onObjectifGestionChange,
        label = { Text(stringResource(R.string.create_forest_objective)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.psgNumero ?: "",
        onValueChange = onPsgNumeroChange,
        label = { Text(stringResource(R.string.create_forest_psg_number)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.remarques ?: "",
        onValueChange = onRemarquesChange,
        label = { Text(stringResource(R.string.create_forest_remarks)) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth(),
    )
}
