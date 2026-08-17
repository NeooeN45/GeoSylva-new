package com.forestry.counter.presentation.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestry.counter.R
import com.forestry.counter.domain.repository.IdentityRepository
import com.forestry.counter.presentation.viewmodel.GeoSylvaViewModelFactory
import com.forestry.counter.presentation.viewmodel.PasswordRecoveryUiState
import com.forestry.counter.presentation.viewmodel.PasswordRecoveryViewModel
import com.forestry.counter.presentation.viewmodel.RecoveryStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecoveryScreen(
    repository: IdentityRepository,
    onCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val factory = remember(repository) {
        GeoSylvaViewModelFactory { PasswordRecoveryViewModel(repository) }
    }
    val viewModel: PasswordRecoveryViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recovery_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.completed) {
            RecoveryCompleted(onCompleted, Modifier.padding(padding))
        } else {
            RecoveryContent(
                state = state,
                onEmailChange = viewModel::setEmail,
                onCodeChange = viewModel::setCode,
                onPasswordChange = viewModel::setPassword,
                onPasswordConfirmationChange = viewModel::setPasswordConfirmation,
                onRequestCode = viewModel::requestCode,
                onConfirm = viewModel::confirmReset,
                onRestart = viewModel::restart,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun RecoveryCompleted(onCompleted: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.LockReset,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Text(
            stringResource(R.string.recovery_completed),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 20.dp),
        )
        Button(onClick = onCompleted, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.recovery_back_to_login))
        }
    }
}

@Composable
private fun RecoveryContent(
    state: PasswordRecoveryUiState,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onConfirm: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Default.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp),
                )
                Text(
                    stringResource(R.string.recovery_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    stringResource(
                        if (state.step == RecoveryStep.REQUEST_CODE) {
                            R.string.recovery_request_desc
                        } else {
                            R.string.recovery_confirm_desc
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                enabled = state.step == RecoveryStep.REQUEST_CODE,
                label = { Text(stringResource(R.string.identity_email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.step == RecoveryStep.CONFIRM_RESET) {
            item {
                OutlinedTextField(
                    value = state.code,
                    onValueChange = onCodeChange,
                    label = { Text(stringResource(R.string.account_verification_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                RecoveryPasswordField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.recovery_new_password),
                )
            }
            item {
                RecoveryPasswordField(
                    value = state.passwordConfirmation,
                    onValueChange = onPasswordConfirmationChange,
                    label = stringResource(R.string.identity_password_confirmation),
                )
            }
        }
        state.error?.let { error -> item { IdentityErrorBanner(error) } }
        item {
            Button(
                onClick = if (state.step == RecoveryStep.REQUEST_CODE) onRequestCode else onConfirm,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(
                            if (state.step == RecoveryStep.REQUEST_CODE) {
                                R.string.recovery_send_code
                            } else {
                                R.string.recovery_reset_password
                            }
                        )
                    )
                }
            }
        }
        if (state.step == RecoveryStep.CONFIRM_RESET) {
            item {
                OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.recovery_change_email))
                }
            }
        }
    }
}

@Composable
private fun RecoveryPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
