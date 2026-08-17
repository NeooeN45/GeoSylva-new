package com.forestry.counter.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestry.counter.domain.repository.ForetRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel du tableau de bord Accueil — spec GEOSYLVA-003 §29.2.
 *
 * Agrège les statistiques rapides : nombre de forêts, parcelles,
 * et activités récentes.
 */
class HomeViewModel(
    private val foretRepository: ForetRepository,
    private val parcelleRepository: ParcelleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                foretRepository.getAll(),
                parcelleRepository.getAllParcelles(),
            ) { forets, parcelles ->
                HomeUiState.Success(
                    foretCount = forets.size,
                    parcelleCount = parcelles.size,
                    recentForets = forets.take(5),
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val foretCount: Int,
        val parcelleCount: Int,
        val recentForets: List<com.forestry.counter.domain.model.Foret>,
    ) : HomeUiState
}
