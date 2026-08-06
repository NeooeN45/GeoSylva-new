package com.forestry.counter.presentation.screens.forestry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestry.counter.domain.model.Parcelle
import com.forestry.counter.domain.model.Placette
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.repository.PlacetteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel pour CreateParcelleWizard — spec GEOSYLVA-003 §29.7.
 */
class CreateParcelleViewModel(
    private val parcelleRepository: ParcelleRepository,
    private val foretId: String,
) : ViewModel() {

    private val _formState = MutableStateFlow(CreateParcelleFormState())
    val formState: StateFlow<CreateParcelleFormState> = _formState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateSurfaceHa(surface: String) {
        _formState.value = _formState.value.copy(surfaceHa = surface.toDoubleOrNull())
    }

    fun updateSlopePct(slope: String) {
        _formState.value = _formState.value.copy(slopePct = slope.toDoubleOrNull())
    }

    fun updateAltitudeM(altitude: String) {
        _formState.value = _formState.value.copy(altitudeM = altitude.toDoubleOrNull())
    }

    fun updateRemarks(remarks: String) {
        _formState.value = _formState.value.copy(remarks = remarks.ifBlank { null })
    }

    fun save() {
        val form = _formState.value
        if (!form.isValid) return
        viewModelScope.launch {
            val parcelle = Parcelle(
                id = UUID.randomUUID().toString(),
                forestId = foretId,
                foretId = foretId,
                name = form.name,
                surfaceHa = form.surfaceHa,
                shape = null,
                slopePct = form.slopePct,
                aspect = null,
                access = null,
                altitudeM = form.altitudeM,
                objectifType = null,
                objectifVal = null,
                tolerancePct = null,
                samplingMode = null,
                sampleAreaM2 = null,
                targetSpeciesCsv = null,
                srid = null,
                remarks = form.remarks,
            )
            parcelleRepository.insertParcelle(parcelle)
            _isSaved.value = true
        }
    }
}

data class CreateParcelleFormState(
    val name: String = "",
    val surfaceHa: Double? = null,
    val slopePct: Double? = null,
    val altitudeM: Double? = null,
    val remarks: String? = null,
) {
    val isValid: Boolean get() = name.isNotBlank()
}

/**
 * ViewModel pour CreatePlacetteWizard — spec GEOSYLVA-003 §29.8.
 */
class CreatePlacetteViewModel(
    private val placetteRepository: PlacetteRepository,
    private val parcelleId: String,
) : ViewModel() {

    private val _formState = MutableStateFlow(CreatePlacetteFormState())
    val formState: StateFlow<CreatePlacetteFormState> = _formState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name.ifBlank { null })
    }

    fun updateType(type: String) {
        _formState.value = _formState.value.copy(type = type.ifBlank { null })
    }

    fun updateRayonM(rayon: String) {
        _formState.value = _formState.value.copy(rayonM = rayon.toDoubleOrNull())
    }

    fun save() {
        val form = _formState.value
        if (!form.isValid) return
        viewModelScope.launch {
            val placette = Placette(
                id = UUID.randomUUID().toString(),
                parcelleId = parcelleId,
                name = form.name,
                type = form.type,
                rayonM = form.rayonM,
                surfaceM2 = null,
                centerWkt = null,
            )
            placetteRepository.insertPlacette(placette)
            _isSaved.value = true
        }
    }
}

data class CreatePlacetteFormState(
    val name: String? = null,
    val type: String? = null,
    val rayonM: Double? = null,
) {
    val isValid: Boolean get() = type != null && rayonM != null && rayonM > 0
}
