package com.forestry.counter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.data.remote.identity.GoogleCredentialClient
import com.forestry.counter.domain.model.AccountSession
import com.forestry.counter.domain.model.AccountProfile
import com.forestry.counter.domain.model.ApiDiagnostic
import com.forestry.counter.domain.model.IdentityClientException
import com.forestry.counter.domain.model.IdentityError
import com.forestry.counter.domain.model.ProviderCapability
import com.forestry.counter.domain.model.ParcelSyncPullResult
import com.forestry.counter.domain.model.ParcelSyncSummary
import com.forestry.counter.domain.repository.IdentityRepository
import com.forestry.counter.domain.repository.ParcelSyncRepository
import com.forestry.counter.presentation.account.AccountInputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginMode {
    SIGN_IN,
    REGISTER,
}

data class LoginUiState(
    val mode: LoginMode = LoginMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val displayName: String = "",
    val providers: List<ProviderCapability> = emptyList(),
    val isLoadingProviders: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: IdentityError? = null,
    val completed: Boolean = false,
)

class LoginViewModel(private val repository: IdentityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        reloadProviders()
    }

    fun setMode(mode: LoginMode) {
        _uiState.update {
            it.copy(mode = mode, password = "", passwordConfirmation = "", error = null)
        }
    }

    fun setEmail(value: String) = updateInput { copy(email = value, error = null) }

    fun setPassword(value: String) = updateInput { copy(password = value, error = null) }

    fun setPasswordConfirmation(value: String) = updateInput {
        copy(passwordConfirmation = value, error = null)
    }

    fun setDisplayName(value: String) = updateInput { copy(displayName = value, error = null) }

    fun reloadProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProviders = true, error = null) }
            repository.getProviders().fold(
                onSuccess = { providers ->
                    _uiState.update {
                        it.copy(providers = providers, isLoadingProviders = false)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingProviders = false,
                            error = error.identityError(),
                        )
                    }
                },
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val result = if (state.mode == LoginMode.SIGN_IN) {
                repository.loginWithPassword(state.email, state.password)
            } else {
                repository.register(
                    email = state.email,
                    password = state.password,
                    displayName = state.displayName,
                )
            }
            completeAuthentication(result)
        }
    }

    fun signInWithGoogle(client: GoogleCredentialClient) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val nonce = repository.requestGoogleNonce().getOrElse { error ->
                finishWith(error.identityError())
                return@launch
            }
            val idToken = client.requestIdToken(nonce.value).getOrElse { error ->
                val identityError = error.identityError()
                finishWith(identityError.takeUnless { it == IdentityError.CANCELLED })
                return@launch
            }
            completeAuthentication(repository.loginWithGoogle(idToken, nonce.value))
        }
    }

    private fun completeAuthentication(result: Result<AccountSession>) {
        result.fold(
            onSuccess = {
                _uiState.update { state ->
                    state.copy(isSubmitting = false, error = null, completed = true)
                }
            },
            onFailure = { finishWith(it.identityError()) },
        )
    }

    private fun finishWith(error: IdentityError?) {
        _uiState.update { it.copy(isSubmitting = false, error = error) }
    }

    private fun validate(state: LoginUiState): IdentityError? {
        if (!EMAIL_PATTERN.matches(state.email.trim()) || state.password.isBlank()) {
            return IdentityError.INVALID_INPUT
        }
        if (state.mode == LoginMode.REGISTER) {
            if (state.password.length !in 12..128) return IdentityError.INVALID_INPUT
            if (state.password != state.passwordConfirmation) return IdentityError.INVALID_INPUT
        }
        return null
    }

    private fun updateInput(transform: LoginUiState.() -> LoginUiState) {
        _uiState.update(transform)
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

data class AccountUiState(
    val session: AccountSession? = null,
    val profile: AccountProfile? = null,
    val providers: List<ProviderCapability> = emptyList(),
    val isLoadingProviders: Boolean = true,
    val isLoggingOut: Boolean = false,
    val isWorking: Boolean = false,
    val verificationCode: String = "",
    val notice: AccountNotice? = null,
    val error: IdentityError? = null,
)

enum class AccountNotice {
    PROFILE_UPDATED,
    VERIFICATION_SENT,
    EMAIL_VERIFIED,
}

class AccountViewModel(private val repository: IdentityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AccountUiState(
            session = repository.session.value,
            profile = repository.profile.value,
        )
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }
        viewModelScope.launch {
            repository.profile.collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
        if (repository.session.value != null) refreshProfile()
        reloadProviders()
    }

    fun setVerificationCode(value: String) {
        _uiState.update { it.copy(verificationCode = value.take(9), error = null, notice = null) }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            repository.loadProfile().onFailure { error ->
                _uiState.update { it.copy(error = error.identityError()) }
            }
        }
    }

    fun updateDisplayName(displayName: String?) = runAccountAction(
        action = { repository.updateDisplayName(displayName).map { Unit } },
        notice = AccountNotice.PROFILE_UPDATED,
    )

    fun requestEmailVerification() = runAccountAction(
        action = repository::requestEmailVerification,
        notice = AccountNotice.VERIFICATION_SENT,
    )

    fun confirmEmailVerification() {
        val code = _uiState.value.verificationCode
        if (!AccountInputValidator.isValidActionCode(code)) {
            _uiState.update { it.copy(error = IdentityError.INVALID_INPUT) }
            return
        }
        runAccountAction(
            action = { repository.confirmEmailVerification(code).map { Unit } },
            notice = AccountNotice.EMAIL_VERIFIED,
        )
    }

    fun reloadProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProviders = true) }
            val providers = repository.getProviders().getOrDefault(emptyList())
            _uiState.update { it.copy(providers = providers, isLoadingProviders = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, error = null) }
            repository.logout().fold(
                onSuccess = { _uiState.update { it.copy(isLoggingOut = false) } },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoggingOut = false, error = error.identityError())
                    }
                },
            )
        }
    }

    private fun runAccountAction(
        action: suspend () -> Result<Unit>,
        notice: AccountNotice,
    ) {
        if (_uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, error = null, notice = null) }
            action().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            verificationCode = if (notice == AccountNotice.EMAIL_VERIFIED) "" else it.verificationCode,
                            notice = notice,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isWorking = false, error = error.identityError())
                    }
                },
            )
        }
    }
}

enum class RecoveryStep {
    REQUEST_CODE,
    CONFIRM_RESET,
}

data class PasswordRecoveryUiState(
    val step: RecoveryStep = RecoveryStep.REQUEST_CODE,
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val isSubmitting: Boolean = false,
    val completed: Boolean = false,
    val error: IdentityError? = null,
)

class PasswordRecoveryViewModel(private val repository: IdentityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun setEmail(value: String) = update { copy(email = value, error = null) }
    fun setCode(value: String) = update { copy(code = value.take(9), error = null) }
    fun setPassword(value: String) = update { copy(password = value, error = null) }
    fun setPasswordConfirmation(value: String) = update {
        copy(passwordConfirmation = value, error = null)
    }

    fun requestCode() {
        val email = _uiState.value.email
        if (!EMAIL_PATTERN.matches(email.trim())) {
            update { copy(error = IdentityError.INVALID_INPUT) }
            return
        }
        submit(action = { repository.requestPasswordReset(email) }) { state ->
            state.copy(step = RecoveryStep.CONFIRM_RESET)
        }
    }

    fun confirmReset() {
        val state = _uiState.value
        if (
            !AccountInputValidator.isValidActionCode(state.code) ||
            !AccountInputValidator.isValidPasswordReset(
                state.password,
                state.passwordConfirmation,
            )
        ) {
            update { copy(error = IdentityError.INVALID_INPUT) }
            return
        }
        submit(
            action = {
                repository.confirmPasswordReset(state.email, state.code, state.password)
            }
        ) { current -> current.copy(completed = true) }
    }

    fun restart() = update {
        copy(
            step = RecoveryStep.REQUEST_CODE,
            code = "",
            password = "",
            passwordConfirmation = "",
            error = null,
        )
    }

    private fun submit(
        action: suspend () -> Result<Unit>,
        onSuccess: (PasswordRecoveryUiState) -> PasswordRecoveryUiState,
    ) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            update { copy(isSubmitting = true, error = null) }
            action().fold(
                onSuccess = { update { onSuccess(this).copy(isSubmitting = false) } },
                onFailure = { error ->
                    update { copy(isSubmitting = false, error = error.identityError()) }
                },
            )
        }
    }

    private fun update(transform: PasswordRecoveryUiState.() -> PasswordRecoveryUiState) {
        _uiState.update(transform)
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

data class DeveloperOptionsUiState(
    val developerModeEnabled: Boolean? = null,
    val session: AccountSession? = null,
    val diagnostic: ApiDiagnostic? = null,
    val providers: List<ProviderCapability> = emptyList(),
    val isRefreshing: Boolean = false,
    val parcelSync: ParcelSyncSummary = ParcelSyncSummary(),
    val isQueueingParcels: Boolean = false,
    val queuedParcelCount: Int? = null,
    val parcelSyncError: Boolean = false,
    val isPullingParcels: Boolean = false,
    val pullResult: ParcelSyncPullResult? = null,
    val pullError: Boolean = false,
)

class DeveloperOptionsViewModel(
    private val repository: IdentityRepository,
    private val preferences: UserPreferencesManager,
    private val parcelSyncRepository: ParcelSyncRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DeveloperOptionsUiState(session = repository.session.value)
    )
    val uiState: StateFlow<DeveloperOptionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }
        viewModelScope.launch {
            preferences.developerModeEnabled.collect { enabled ->
                _uiState.update { it.copy(developerModeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            parcelSyncRepository.observeSummary().collect { summary ->
                _uiState.update { it.copy(parcelSync = summary) }
            }
        }
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val diagnostic = repository.diagnoseApi()
            val providers = repository.getProviders().getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    diagnostic = diagnostic,
                    providers = providers,
                    isRefreshing = false,
                )
            }
        }
    }

    fun disableDeveloperMode() {
        viewModelScope.launch { preferences.setDeveloperModeEnabled(false) }
    }

    fun synchronizeParcels() {
        if (_uiState.value.isQueueingParcels) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isQueueingParcels = true,
                    queuedParcelCount = null,
                    parcelSyncError = false,
                )
            }
            parcelSyncRepository.enqueueAll().fold(
                onSuccess = { count ->
                    _uiState.update {
                        it.copy(isQueueingParcels = false, queuedParcelCount = count)
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isQueueingParcels = false, parcelSyncError = true)
                    }
                },
            )
        }
    }

    fun pullParcels() {
        if (_uiState.value.isPullingParcels) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isPullingParcels = true, pullResult = null, pullError = false)
            }
            parcelSyncRepository.pull().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(isPullingParcels = false, pullResult = result)
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isPullingParcels = false, pullError = true)
                    }
                },
            )
        }
    }
}

class GeoSylvaViewModelFactory<T : ViewModel>(
    private val create: () -> T,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}

private fun Throwable.identityError(): IdentityError =
    (this as? IdentityClientException)?.error ?: IdentityError.UNKNOWN
