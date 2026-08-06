package com.forestry.counter.presentation.screens.forestry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestry.counter.domain.model.Foret
import com.forestry.counter.domain.repository.ForetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel pour CreateForestWizard — spec GEOSYLVA-003 §29.6.
 *
 * Wizard 3 étapes : Identité, Propriétaire, Gestion.
 */
class CreateForestViewModel(
    private val foretRepository: ForetRepository,
) : ViewModel() {

    private val _formState = MutableStateFlow(CreateForestFormState())
    val formState: StateFlow<CreateForestFormState> = _formState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun updateNom(nom: String) {
        _formState.value = _formState.value.copy(nom = nom)
    }

    fun updateProprietaireNom(nom: String) {
        _formState.value = _formState.value.copy(proprietaireNom = nom)
    }

    fun updateProprietaireEmail(email: String) {
        _formState.value = _formState.value.copy(proprietaireEmail = email.ifBlank { null })
    }

    fun updateGestionnaireNom(nom: String) {
        _formState.value = _formState.value.copy(gestionnaireNom = nom.ifBlank { null })
    }

    fun updateTypeForet(type: String) {
        _formState.value = _formState.value.copy(typeForet = type.ifBlank { null })
    }

    fun updateObjectifGestion(objectif: String) {
        _formState.value = _formState.value.copy(objectifGestion = objectif.ifBlank { null })
    }

    fun updateDepartement(dept: String) {
        _formState.value = _formState.value.copy(departement = dept.ifBlank { null })
    }

    fun updatePsgNumero(numero: String) {
        _formState.value = _formState.value.copy(psgNumero = numero.ifBlank { null })
    }

    fun updateRemarques(remarques: String) {
        _formState.value = _formState.value.copy(remarques = remarques.ifBlank { null })
    }

    fun save() {
        val form = _formState.value
        if (!form.isValid) return
        viewModelScope.launch {
            val foret = Foret(
                foretId = UUID.randomUUID().toString(),
                nom = form.nom,
                proprietaireNom = form.proprietaireNom,
                proprietaireEmail = form.proprietaireEmail,
                gestionnaireNom = form.gestionnaireNom,
                typeForet = form.typeForet,
                objectifGestion = form.objectifGestion,
                psgNumero = form.psgNumero,
                psgDateExpiration = null,
                departement = form.departement,
                remarques = form.remarques,
            )
            foretRepository.insert(foret)
            _isSaved.value = true
        }
    }
}

data class CreateForestFormState(
    val nom: String = "",
    val proprietaireNom: String = "",
    val proprietaireEmail: String? = null,
    val gestionnaireNom: String? = null,
    val typeForet: String? = null,
    val objectifGestion: String? = null,
    val psgNumero: String? = null,
    val departement: String? = null,
    val remarques: String? = null,
) {
    val isValid: Boolean
        get() = nom.isNotBlank() && proprietaireNom.isNotBlank()

    val step1Valid: Boolean get() = nom.isNotBlank()
    val step2Valid: Boolean get() = proprietaireNom.isNotBlank()
    val step3Valid: Boolean get() = true
}
