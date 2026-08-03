package com.forestry.counter.presentation.screens.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestry.counter.R
import com.forestry.counter.data.remote.identity.GoogleCredentialClient
import com.forestry.counter.domain.model.IdentityProvider
import com.forestry.counter.domain.model.ProviderAvailability
import com.forestry.counter.domain.repository.IdentityRepository
import com.forestry.counter.presentation.viewmodel.GeoSylvaViewModelFactory
import com.forestry.counter.presentation.viewmodel.LoginMode
import com.forestry.counter.presentation.viewmodel.LoginUiState
import com.forestry.counter.presentation.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    repository: IdentityRepository,
    onAuthenticated: () -> Unit,
    onContinueOffline: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val factory = remember(repository) { GeoSylvaViewModelFactory { LoginViewModel(repository) } }
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val googleClient = remember(context) { GoogleCredentialClient(context) }

    LaunchedEffect(state.completed) {
        if (state.completed) onAuthenticated()
    }

    LoginBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { LoginTopBar(onContinueOffline) },
        ) { padding ->
            LoginContent(
                state = state,
                googleClientConfigured = repository.isGoogleClientConfigured,
                onModeChange = viewModel::setMode,
                onEmailChange = viewModel::setEmail,
                onPasswordChange = viewModel::setPassword,
                onPasswordConfirmationChange = viewModel::setPasswordConfirmation,
                onDisplayNameChange = viewModel::setDisplayName,
                onSubmit = viewModel::submit,
                onGoogle = { viewModel.signInWithGoogle(googleClient) },
                onRetry = viewModel::reloadProviders,
                onForgotPassword = onForgotPassword,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun LoginBackdrop(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.forest_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xD9142419), Color(0xF20B160E)),
                    )
                ),
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTopBar(onContinueOffline: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onContinueOffline) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color.White,
                )
            }
        },
        actions = {
            TextButton(onClick = onContinueOffline) {
                Text(stringResource(R.string.identity_continue_offline), color = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    googleClientConfigured: Boolean,
    onModeChange: (LoginMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onRetry: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        LoginBrandHeader()
        Spacer(Modifier.height(24.dp))
        LoginCard(
            state,
            googleClientConfigured,
            onModeChange,
            onEmailChange,
            onPasswordChange,
            onPasswordConfirmationChange,
            onDisplayNameChange,
            onSubmit,
            onGoogle,
            onRetry,
            onForgotPassword,
        )
        Spacer(Modifier.height(20.dp))
        LoginSecurityNotice()
    }
}

@Composable
private fun LoginBrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                Icons.Default.Forest,
                contentDescription = null,
                modifier = Modifier.padding(16.dp).size(36.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.identity_login_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            stringResource(R.string.identity_login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.78f),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LoginCard(
    state: LoginUiState,
    googleClientConfigured: Boolean,
    onModeChange: (LoginMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onRetry: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProviderConnectionStatus(state, onRetry)
            GoogleButton(state, googleClientConfigured, onGoogle)
            OrDivider()
            LoginModeTabs(state.mode, onModeChange)
            LocalCredentialsForm(
                state,
                onEmailChange,
                onPasswordChange,
                onPasswordConfirmationChange,
                onDisplayNameChange,
                onSubmit,
                onForgotPassword,
            )
            ProfessionalButton()
            state.error?.let { IdentityErrorBanner(it) }
        }
    }
}

@Composable
private fun ProviderConnectionStatus(state: LoginUiState, onRetry: () -> Unit) {
    if (state.isLoadingProviders) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.identity_retry))
        }
    } else if (state.providers.isEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.identity_server_not_configured),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.identity_retry)) }
        }
    }
}

@Composable
private fun GoogleButton(
    state: LoginUiState,
    googleClientConfigured: Boolean,
    onGoogle: () -> Unit,
) {
    val capability = state.providers.firstOrNull { it.provider == IdentityProvider.GOOGLE }
    val available = capability?.availability == ProviderAvailability.AVAILABLE
    OutlinedButton(
        onClick = onGoogle,
        enabled = available && googleClientConfigured && !state.isSubmitting,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text("G", fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))
        Text(stringResource(R.string.identity_google_continue))
    }
    if (!available || !googleClientConfigured) {
        Text(
            stringResource(R.string.identity_google_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.identity_or),
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LoginModeTabs(mode: LoginMode, onModeChange: (LoginMode) -> Unit) {
    TabRow(selectedTabIndex = if (mode == LoginMode.SIGN_IN) 0 else 1) {
        Tab(
            selected = mode == LoginMode.SIGN_IN,
            onClick = { onModeChange(LoginMode.SIGN_IN) },
            text = { Text(stringResource(R.string.identity_mode_sign_in)) },
        )
        Tab(
            selected = mode == LoginMode.REGISTER,
            onClick = { onModeChange(LoginMode.REGISTER) },
            text = { Text(stringResource(R.string.identity_mode_register)) },
        )
    }
}

@Composable
private fun LocalCredentialsForm(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    if (state.mode == LoginMode.REGISTER) {
        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.identity_display_name)) },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    EmailField(state.email, onEmailChange)
    PasswordField(state.password, passwordVisible, onPasswordChange) {
        passwordVisible = !passwordVisible
    }
    if (state.mode == LoginMode.SIGN_IN) {
        TextButton(onClick = onForgotPassword, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.identity_forgot_password))
        }
    }
    if (state.mode == LoginMode.REGISTER) {
        PasswordConfirmationField(state.passwordConfirmation, onPasswordConfirmationChange)
    }
    Button(
        onClick = onSubmit,
        enabled = !state.isSubmitting && state.providers.any {
            it.provider == IdentityProvider.LOCAL &&
                it.availability == ProviderAvailability.AVAILABLE
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        if (state.isSubmitting) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        else Text(
            stringResource(
                if (state.mode == LoginMode.SIGN_IN) R.string.identity_sign_in
                else R.string.identity_create_account
            )
        )
    }
}

@Composable
private fun EmailField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.identity_email)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.identity_password)) },
        supportingText = { Text(stringResource(R.string.identity_password_hint)) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(
                        if (visible) R.string.identity_hide_password else R.string.identity_show_password
                    ),
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordConfirmationField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.identity_password_confirmation)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProfessionalButton() {
    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.identity_professional))
        Spacer(Modifier.size(8.dp))
        StatusPill(stringResource(R.string.identity_in_development), positive = false)
    }
}

@Composable
private fun LoginSecurityNotice() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
        Text(
            stringResource(R.string.identity_session_secured),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
        )
    }
}
