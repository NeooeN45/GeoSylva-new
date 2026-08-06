package com.forestry.counter.presentation.screens.forestry

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
                title = { Text("Nouvelle forêt (${currentStep + 1}/$totalSteps)") },
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
                        Text("Précédent")
                    }
                } else {
                    TextButton(onClick = onNavigateBack) { Text("Annuler") }
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
                        Text("Suivant")
                        Icon(Icons.Filled.NavigateNext, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = viewModel::save,
                        enabled = form.isValid,
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Text("Créer")
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
    StepTitle("Identité")
    OutlinedTextField(
        value = form.nom,
        onValueChange = onNomChange,
        label = { Text("Nom de la forêt *") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.departement ?: "",
        onValueChange = onDepartementChange,
        label = { Text("Département") },
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
    StepTitle("Propriétaire")
    OutlinedTextField(
        value = form.proprietaireNom,
        onValueChange = onProprietaireNomChange,
        label = { Text("Nom du propriétaire *") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.proprietaireEmail ?: "",
        onValueChange = onProprietaireEmailChange,
        label = { Text("Email du propriétaire") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.gestionnaireNom ?: "",
        onValueChange = onGestionnaireNomChange,
        label = { Text("Nom du gestionnaire") },
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
    StepTitle("Gestion")
    OutlinedTextField(
        value = form.typeForet ?: "",
        onValueChange = onTypeForetChange,
        label = { Text("Type de forêt") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.objectifGestion ?: "",
        onValueChange = onObjectifGestionChange,
        label = { Text("Objectif de gestion") },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.psgNumero ?: "",
        onValueChange = onPsgNumeroChange,
        label = { Text("N° PSG") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.remarques ?: "",
        onValueChange = onRemarquesChange,
        label = { Text("Remarques") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth(),
    )
}
